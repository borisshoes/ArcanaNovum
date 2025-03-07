package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
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

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
   
   @Shadow
   @Final
   public PlayerEntity player;
   
   @Shadow
   @Final
   private List<DefaultedList<ItemStack>> combinedInventory;
   
   @Unique
   private final Map<Pair<Integer,Integer>, ItemStack> anchoredItems = new HashMap<>();
   
   @Inject(method = "dropAll", at = @At("HEAD"))
   private void arcananovum_fateAnchorSave(CallbackInfo ci){
      int invIndex = 0;
      for (List<ItemStack> list : this.combinedInventory) {
         for (int i = 0; i < list.size(); i++) {
            ItemStack itemStack = (ItemStack)list.get(i);
            if (!itemStack.isEmpty() && (player.isAlive() || EnchantmentHelper.getLevel(MiscUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), itemStack) > 0)) {
               anchoredItems.put(new Pair<>(invIndex,i),itemStack);
               list.set(i, ItemStack.EMPTY);
            }
         }
         invIndex++;
      }
   }
   
   @Inject(method = "dropAll", at = @At("RETURN"))
   private void arcananovum_fateAnchorRestore(CallbackInfo ci){
      for(Map.Entry<Pair<Integer, Integer>, ItemStack> entry : anchoredItems.entrySet()){
         combinedInventory.get(entry.getKey().getLeft()).set(entry.getKey().getRight(), entry.getValue());
      }
      anchoredItems.clear();
   }
   
   // Charm of Cinders Auto Smelt
   @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
   private void arcananovum_onPickup(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      try{
         PlayerInventory playerInv = (PlayerInventory) (Object) this;
         List<Pair<List<ItemStack>,ItemStack>> allItems = MiscUtils.getAllItems(playerInv.player);
         if(playerInv.player.isSneaking()) return;
         
         for(int i = 0; i < allItems.size(); i++){
            List<ItemStack> itemList = allItems.get(i).getLeft();
            ItemStack carrier = allItems.get(i).getRight();
            SimpleInventory sinv = new SimpleInventory(itemList.size());
            
            for(int j = 0; j < itemList.size(); j++){
               ItemStack item = itemList.get(j);
               
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  sinv.setStack(j,item);
                  continue; // Item not arcane, skip
               }
               
               // Look for charm
               if(ArcanaItemUtils.identifyItem(item) instanceof CindersCharm charm){
                  ItemStack output = charm.smelt(item, playerInv.player, stack);
                  if(output != null){
                     cir.setReturnValue(customPickUp(stack,slot,playerInv,output));
                     cir.cancel();
                  }
               }
               
               sinv.setStack(j,item);
            }
            
            if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
               belt.buildItemLore(carrier, ArcanaNovum.SERVER);
            }
         }
         
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Shadow
   protected abstract int addStack(ItemStack stack);
   
   @Shadow
   protected abstract int addStack(int slot, ItemStack stack);
   
   private boolean customPickUp(ItemStack oldStack, int slot, PlayerInventory inv, ItemStack stack){
      oldStack.setCount(0);
      
      try {
         if(stack.isDamaged()){
            if(slot == -1){
               slot = inv.getEmptySlot();
            }
         
            if(slot >= 0){
               inv.main.set(slot, stack.copy());
               ((ItemStack)inv.main.get(slot)).setBobbingAnimationTime(5);
               stack.setCount(0);
               return true;
            } else if(inv.player.getAbilities().creativeMode){
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
                  stack.setCount(this.addStack(stack));
               }else{
                  stack.setCount(this.addStack(slot, stack));
               }
            }while(!stack.isEmpty() && stack.getCount() < i);
         
            if(stack.getCount() == i && inv.player.getAbilities().creativeMode){
               stack.setCount(0);
               return true;
            }else{
               return stack.getCount() < i;
            }
         }
      } catch (Throwable var6){
         CrashReport crashReport = CrashReport.create(var6, "Adding item to inventory");
         CrashReportSection crashReportSection = crashReport.addElement("Item being added");
         crashReportSection.add("Item ID", Item.getRawId(stack.getItem()));
         crashReportSection.add("Item data", stack.getDamage());
         crashReportSection.add("Item name", () -> {
            return stack.getName().getString();
         });
         throw new CrashException(crashReport);
      }
   }
}
