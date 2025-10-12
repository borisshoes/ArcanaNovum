package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TurtleEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MoveControl.class, AquaticMoveControl.class, FlightMoveControl.class, DrownedEntity.DrownedMoveControl.class, SlimeEntity.SlimeMoveControl.class, FishEntity.FishMoveControl.class, TurtleEntity.TurtleMoveControl.class,
      RabbitEntity.RabbitMoveControl.class, GhastEntity.GhastMoveControl.class, GuardianEntity.GuardianMoveControl.class, PhantomEntity.PhantomMoveControl.class, VexEntity.VexMoveControl.class})
public class MoveControlMixin {
   
   @Inject(method = "tick", at=@At("HEAD"))
   private void arcananovum$ensnarementMob(CallbackInfo ci){
      MoveControl moveControl = (MoveControl)(Object)this;
      MobEntity entity = moveControl.entity;
      
      StatusEffectInstance effect = entity.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         moveControl.state = MoveControl.State.WAIT;
      }
   }
}
