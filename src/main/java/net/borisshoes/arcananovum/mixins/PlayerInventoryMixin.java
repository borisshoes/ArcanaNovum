package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
   
   // Charm of Cinders Auto Smelt
   @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
   private void arcananovum_onPickup(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      try{
         PlayerInventory inv = (PlayerInventory) (Object) this;
         if(inv.player.isSneaking()) return;
   
         for(int i = 0; i < inv.size(); i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
      
            boolean isMagic = MagicItemUtils.isMagic(item);
            if(!isMagic)
               continue; // Item not magic, skip
      
            // Look for charm
            if(MagicItemUtils.identifyItem(item) instanceof CindersCharm charm){
               ItemStack output = charm.smelt(item, inv.player, stack);
               if(output != null){
                  cir.setReturnValue(customPickUp(stack,slot,inv,output));
                  cir.cancel();
               }
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
         if (stack.isDamaged()) {
            if (slot == -1) {
               slot = inv.getEmptySlot();
            }
         
            if (slot >= 0) {
               inv.main.set(slot, stack.copy());
               ((ItemStack)inv.main.get(slot)).setBobbingAnimationTime(5);
               stack.setCount(0);
               return true;
            } else if (inv.player.getAbilities().creativeMode) {
               stack.setCount(0);
               return true;
            } else {
               return false;
            }
         } else {
            int i;
            do {
               i = stack.getCount();
               if (slot == -1) {
                  stack.setCount(this.addStack(stack));
               } else {
                  stack.setCount(this.addStack(slot, stack));
               }
            } while(!stack.isEmpty() && stack.getCount() < i);
         
            if (stack.getCount() == i && inv.player.getAbilities().creativeMode) {
               stack.setCount(0);
               return true;
            } else {
               return stack.getCount() < i;
            }
         }
      } catch (Throwable var6) {
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
