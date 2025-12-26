package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerExplosion.class)
public class ExplosionMixin {
   
   // Reduce damage from Detonation Arrows to players and delete damage from terrain explosion
   
   @Final
   @Shadow
   private DamageSource damageSource;
   
   @ModifyReturnValue(method = "interactsWithBlocks", at = @At("RETURN"))
   private boolean arcananovum$detArrowDestroyBlocks(boolean original){
      if(damageSource.is(ArcanaDamageTypes.DETONATION_DAMAGE)){
         return false;
      }else{
         return original;
      }
   }
   
   @ModifyExpressionValue(method = "hurtEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getEntityDamageAmount(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/entity/Entity;F)F"))
   private float arcananovum$detArrowDamage(float original, @Local Entity entity){
      if(damageSource.is(ArcanaDamageTypes.DETONATION_DAMAGE)){
         if(entity instanceof Player){
            return original / 5;
         }
      }else if(damageSource.is(ArcanaDamageTypes.DETONATION_TERRAIN)){
         return 0;
      }
      return original;
   }
   
   @Inject(method = "hurtEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(Lnet/minecraft/world/phys/Vec3;)V"))
   private void arcananovum$detArrowAchievement(CallbackInfo ci, @Local Entity entity){
      if(damageSource.is(ArcanaDamageTypes.DETONATION_DAMAGE) && entity instanceof ServerPlayer hitPlayer){
         Entity attacker = damageSource.getEntity();
         if(attacker != null && hitPlayer.getUUID().equals(attacker.getUUID()) && hitPlayer.getHealth() > 0f && hitPlayer.getHealth() < 2f)
            ArcanaAchievements.grant(hitPlayer, ArcanaAchievements.SAFETY_THIRD.id);
      }
   }
}
