package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Phantom.PhantomMoveControl.class)
public class PhantomMoveControlMixin {
   
   @Shadow
   private float speed;
   
   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void arcananovum$ensnarementMobPhantom(CallbackInfo ci){
      Phantom.PhantomMoveControl moveControl = (Phantom.PhantomMoveControl) (Object) this;
      Mob entity = moveControl.mob;
      
      MobEffectInstance effect = entity.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         entity.setDeltaMovement(0, 0, 0);
         speed = 0.0f;
         ci.cancel();
      }
   }
}
