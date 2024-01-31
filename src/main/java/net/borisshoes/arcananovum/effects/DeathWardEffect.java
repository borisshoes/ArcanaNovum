package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class DeathWardEffect extends StatusEffect implements PolymerStatusEffect {
   public DeathWardEffect(){
      super(StatusEffectCategory.BENEFICIAL,0x270000);
   }
   
   @Override
   public void applyUpdateEffect(LivingEntity entity, int amplifier){
      if(entity.getWorld() instanceof ServerWorld serverWorld && entity.getServer().getTicks() % 6 == 0){
         Vec3d pos = entity.getPos();
         serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL,pos.x,pos.y+entity.getHeight()/2,pos.z,1,.4,.4,.4,0.05);
      }
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
}
