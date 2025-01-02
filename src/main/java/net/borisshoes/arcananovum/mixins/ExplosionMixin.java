package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosionImpl.class)
public class ExplosionMixin {
   
   // Reduce damage from Detonation Arrows to players and delete damage from terrain explosion
   
   @Final
   @Shadow
   private DamageSource damageSource;
   
   @ModifyReturnValue(method = "shouldDestroyBlocks", at = @At("RETURN"))
   private boolean arcananovum_detArrowDestroyBlocks(boolean original){
      if(damageSource.isOf(ArcanaDamageTypes.DETONATION_TERRAIN)){
         return false;
      }else{
         return original;
      }
   }
   
   @ModifyExpressionValue(method = "damageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionBehavior;calculateDamage(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/entity/Entity;F)F"))
   private float arcananovum_detArrowDamage(float original, @Local Entity entity){
      if(damageSource.isOf(ArcanaDamageTypes.DETONATION_DAMAGE)){
         if(entity instanceof PlayerEntity){
            return original / 5;
         }
      }
      return original;
   }
   
   @Inject(method = "damageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
   private void arcananovum_detArrowAchievement(CallbackInfo ci, @Local Entity entity){
      if(entity instanceof ServerPlayerEntity hitPlayer){
         Entity attacker = damageSource.getAttacker();
         if(attacker != null && hitPlayer.getUuid().equals(attacker.getUuid()) && hitPlayer.getHealth() > 0f && hitPlayer.getHealth() < 2f)
            ArcanaAchievements.grant(hitPlayer, ArcanaAchievements.SAFETY_THIRD.id);
      }
   }
}
