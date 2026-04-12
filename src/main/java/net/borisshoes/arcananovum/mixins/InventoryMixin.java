package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.utils.ArcanaInventory;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerPlayer;
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
         if(player.isShiftKeyDown() || !(player instanceof ServerPlayer serverPlayer)) return;
         
         ArcanaInventory arcanaInventory = ArcanaInventory.getPlayerItems(serverPlayer);
         List<ArcanaInventory.Entry> entries = arcanaInventory.getMatchingItems(invStack ->
               ArcanaItemUtils.identifyItem(invStack) instanceof CindersCharm && EnergyItem.getEnergy(invStack) > 0);
         for(ArcanaInventory.Entry entry : entries){
            ItemStack charmStack = entry.getStack();
            CindersCharm charm = (CindersCharm) ArcanaItemUtils.identifyItem(charmStack);
            ItemStack output = charm.smelt(charmStack, serverPlayer, stack); // TODO maybe do partial smelts?
            if(output != null){
               entry.setModified();
               arcanaInventory.close();
               cir.setReturnValue(customPickUp(stack, slot, playerInv, output));
               cir.cancel();
               return;
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
