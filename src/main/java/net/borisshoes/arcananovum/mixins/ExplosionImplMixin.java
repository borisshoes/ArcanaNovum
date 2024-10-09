package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExplosionImpl.class)
public class ExplosionImplMixin {

   @Shadow @Final private ServerWorld world;

   // Reduce damage from Detonation Arrows to players and delete damage from terrain explosion
   @Redirect(method="damageEntities",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
   private boolean arcananovum_redirectDamage(Entity entity, DamageSource source, float amount){
      try{
         Explosion explosion = (Explosion) (Object) this;
         String explosionName = source.getName();
         if(explosionName.contains("arcananovum.detonation_terrain")){
            return true;
         }else if(explosionName.contains("arcananovum.detonation_damage")){
            if(entity instanceof ServerPlayerEntity hitPlayer){
               float newDmg = amount / 5;
               Entity attacker = source.getAttacker();
               entity.damage(world,source,newDmg);
               
               if(attacker != null && hitPlayer.getUuid().equals(attacker.getUuid()) && hitPlayer.getHealth() > 0f && hitPlayer.getHealth() < 2f) ArcanaAchievements.grant(hitPlayer,ArcanaAchievements.SAFETY_THIRD.id);
               return true;
            }
         }
      
         entity.damage(world,source,amount);
      }catch(NumberFormatException e){
         e.printStackTrace();
      }
      return true;
   }
}
