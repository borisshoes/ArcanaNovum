package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
   
   @Shadow
   @Final
   public Player player;
   
   @Unique
   private final Map<Integer, ItemStack> anchoredItems = new HashMap<>();
   
   @Inject(method = "dropAll", at = @At("HEAD"))
   private void arcananovum$fateAnchorSave(CallbackInfo ci){
      Inventory inv = (Inventory) (Object) this;
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack itemStack = (ItemStack) inv.getItem(i);
         if(!itemStack.isEmpty() && (player.isAlive() || EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), itemStack) > 0)){
            anchoredItems.put(i, itemStack);
            inv.setItem(i, ItemStack.EMPTY);
         }
      }
   }
   
   @Inject(method = "dropAll", at = @At("RETURN"))
   private void arcananovum$fateAnchorRestore(CallbackInfo ci){
      Inventory inv = (Inventory) (Object) this;
      for(Map.Entry<Integer, ItemStack> entry : anchoredItems.entrySet()){
         inv.setItem(entry.getKey(), entry.getValue());
      }
      anchoredItems.clear();
   }
   
   // Charm of Cinders Auto Smelt
   @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
   private void arcananovum$onPickup(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      try{
         Inventory playerInv = (Inventory) (Object) this;
         List<Tuple<List<ItemStack>, ItemStack>> allItems = ArcanaUtils.getAllItems(playerInv.player);
         if(playerInv.player.isShiftKeyDown()) return;
         
         for(int i = 0; i < allItems.size(); i++){
            List<ItemStack> itemList = allItems.get(i).getA();
            ItemStack carrier = allItems.get(i).getB();
            SimpleContainer sinv = new SimpleContainer(itemList.size());
            
            for(int j = 0; j < itemList.size(); j++){
               ItemStack item = itemList.get(j);
               
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  sinv.setItem(j, item);
                  continue; // Item not arcane, skip
               }
               
               // Look for charm
               if(ArcanaItemUtils.identifyItem(item) instanceof CindersCharm charm){
                  ItemStack output = charm.smelt(item, playerInv.player, stack);
                  if(output != null){
                     cir.setReturnValue(customPickUp(stack, slot, playerInv, output));
                     cir.cancel();
                  }
               }
               
               sinv.setItem(j, item);
            }
            
            if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
               belt.buildItemLore(carrier, BorisLib.SERVER);
            }
         }
         
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Shadow
   protected abstract int addResource(ItemStack stack);
   
   @Shadow
   protected abstract int addResource(int slot, ItemStack stack);
   
   private boolean customPickUp(ItemStack oldStack, int slot, Inventory inv, ItemStack stack){
      oldStack.setCount(0);
      
      try{
         if(stack.isDamaged()){
            if(slot == -1){
               slot = inv.getFreeSlot();
            }
            
            if(slot >= 0){
               inv.setItem(slot, stack.copy());
               ((ItemStack) inv.getItem(slot)).setPopTime(5);
               stack.setCount(0);
               return true;
            }else if(inv.player.getAbilities().instabuild){
               stack.setCount(0);
               return true;
            }else{
               return false;
            }
         }else{
            int i;
            do{
               i = stack.getCount();
               if(slot == -1){
                  stack.setCount(this.addResource(stack));
               }else{
                  stack.setCount(this.addResource(slot, stack));
               }
            }while(!stack.isEmpty() && stack.getCount() < i);
            
            if(stack.getCount() == i && inv.player.getAbilities().instabuild){
               stack.setCount(0);
               return true;
            }else{
               return stack.getCount() < i;
            }
         }
      }catch(Throwable var6){
         CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
         CrashReportCategory crashReportSection = crashReport.addCategory("Item being added");
         crashReportSection.setDetail("Item ID", Item.getId(stack.getItem()));
         crashReportSection.setDetail("Item data", stack.getDamageValue());
         crashReportSection.setDetail("Item name", () -> {
            return stack.getHoverName().getString();
         });
         throw new ReportedException(crashReport);
      }
   }
}
