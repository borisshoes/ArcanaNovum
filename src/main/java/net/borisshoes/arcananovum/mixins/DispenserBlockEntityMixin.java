package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DispenserBlockEntity.class)
public class DispenserBlockEntityMixin {
   
   @Shadow
   private DefaultedList<ItemStack> inventory;
   
   @Inject(method = "chooseNonEmptySlot", at = @At("RETURN"), cancellable = true)
   private void arcananovum_disableDispenser(Random random, CallbackInfoReturnable<Integer> cir){
      DispenserBlockEntity dispenser = (DispenserBlockEntity) (Object) this;
      
      int oldInd = cir.getReturnValue();
      if(oldInd < 0 || oldInd >= inventory.size()) return;
      ItemStack returnedStack = inventory.get(oldInd);
      if(MagicItemUtils.isMagic(returnedStack)){
         int i = -1;
         int j = 1;
   
         for(int k = 0; k < inventory.size(); ++k) {
            ItemStack testStack = inventory.get(k);
            if (!testStack.isEmpty() && !MagicItemUtils.isMagic(testStack) && random.nextInt(j++) == 0) {
               i = k;
            }
         }
         cir.setReturnValue(i);
      }
   }
}
