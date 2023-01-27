package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class ExplosionMixin {
   
   // Reduce damage from Detonation Arrows to players
   @Redirect(method="collectBlocksAndDamageEntities",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
   private boolean arcananovum_redirectDamage(Entity entity, DamageSource source, float amount){
      Explosion explosion = (Explosion) (Object) this;
      if(explosion.getDamageSource().name.equals("explosion.player.ArcanaNovum.DetonationArrows")){
         if(entity instanceof ServerPlayerEntity hitPlayer){
            float newDmg = amount / 3;
            Entity attacker = explosion.getDamageSource().getAttacker();
            entity.damage((new EntityDamageSource("explosion.player", explosion.getDamageSource().getAttacker())).setScaledWithDifficulty().setExplosive(),newDmg);
            
            if(attacker != null && hitPlayer.getUuid().equals(attacker.getUuid()) && hitPlayer.getHealth() > 0f && hitPlayer.getHealth() < 2f) ArcanaAchievements.grant(hitPlayer,"safety_third");
            return true;
         }
      }
      
      entity.damage(source,amount);
      return true;
   }
}
