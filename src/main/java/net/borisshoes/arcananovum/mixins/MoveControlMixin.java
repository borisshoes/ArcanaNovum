package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MoveControl.class, SmoothSwimmingMoveControl.class, FlyingMoveControl.class, Drowned.DrownedMoveControl.class, Slime.SlimeMoveControl.class, AbstractFish.FishMoveControl.class, Turtle.TurtleMoveControl.class,
      Rabbit.RabbitMoveControl.class, Ghast.GhastMoveControl.class, Guardian.GuardianMoveControl.class, Phantom.PhantomMoveControl.class, Vex.VexMoveControl.class})
public class MoveControlMixin {
   
   @Inject(method = "tick", at = @At("HEAD"))
   private void arcananovum$ensnarementMob(CallbackInfo ci){
      MoveControl moveControl = (MoveControl) (Object) this;
      Mob entity = moveControl.mob;
      
      MobEffectInstance effect = entity.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         moveControl.operation = MoveControl.Operation.WAIT;
      }
   }
}
