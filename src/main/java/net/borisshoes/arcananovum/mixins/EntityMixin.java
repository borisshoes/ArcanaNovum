package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
   
   @Inject(method = "isSteppingCarefully", at = @At("RETURN"), cancellable = true)
   private void arcananovum$greavesWindGrace(CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValueZ()) return;
      if(((Entity)(Object)this) instanceof LivingEntity living){
         ItemStack pants = living.getItemBySlot(EquipmentSlot.LEGS);
         if(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus && ArcanaAugments.getAugmentOnItem(pants,ArcanaAugments.WINDS_GRACE) >= 1) cir.setReturnValue(true);
      }
   }
}
