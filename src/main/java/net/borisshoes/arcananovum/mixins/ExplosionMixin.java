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
   
   // Reduce damage from Detonation Arrows to players and delete damage from terrain explosion
   @Redirect(method="collectBlocksAndDamageEntities",at=@At(value="INVOKE",target="Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
   private boolean arcananovum_redirectDamage(Entity entity, DamageSource source, float amount){
      try{
         Explosion explosion = (Explosion) (Object) this;
         String explosionName = explosion.getDamageSource().name;
         if(explosionName.contains("explosion.player.ArcanaNovum.DetonationArrows.Terrain")){
            return true; // Cancel all damage from terrain explosion
         }else if(explosionName.contains("explosion.player.ArcanaNovum.DetonationArrows")){
            int blastLvl = Integer.parseInt(""+explosionName.charAt(52)); // Char 52
            int personLvl = Integer.parseInt(""+explosionName.charAt(54)); // Char 54
            float dmgMod = (float) (1 + personLvl*.5 - blastLvl*.3);
            float newDmg = amount * dmgMod;
            
            if(entity instanceof ServerPlayerEntity hitPlayer){
               newDmg /= 3;
               Entity attacker = explosion.getDamageSource().getAttacker();
               entity.damage((new EntityDamageSource("explosion.player", explosion.getDamageSource().getAttacker())).setScaledWithDifficulty().setExplosive(),newDmg);
               
               if(attacker != null && hitPlayer.getUuid().equals(attacker.getUuid()) && hitPlayer.getHealth() > 0f && hitPlayer.getHealth() < 2f) ArcanaAchievements.grant(hitPlayer,"safety_third");
               return true;
            }
         }
      
         entity.damage(source,amount);
      }catch(NumberFormatException e){
         e.printStackTrace();
      }
      return true;
   }
}
