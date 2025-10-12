package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PhantomEntity.PhantomMoveControl.class)
public class PhantomMoveControlMixin {
   
   @Shadow
   private float targetSpeed;
   
   @Inject(method = "tick", at=@At("HEAD"), cancellable = true)
   private void arcananovum$ensnarementMobPhantom(CallbackInfo ci){
      PhantomEntity.PhantomMoveControl moveControl = (PhantomEntity.PhantomMoveControl)(Object)this;
      MobEntity entity = moveControl.entity;
      
      StatusEffectInstance effect = entity.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         entity.setVelocity(0,0,0);
         targetSpeed = 0.0f;
         ci.cancel();
      }
   }
}
