package net.borisshoes.arcananovum.mixins;

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
         if(entity instanceof ServerPlayerEntity){
            float newDmg = amount / 3;
            entity.damage((new EntityDamageSource("explosion.player", explosion.getDamageSource().getAttacker())).setScaledWithDifficulty().setExplosive(),newDmg);
            return true;
         }
      }
      
      entity.damage(source,amount);
      return true;
   }
}
