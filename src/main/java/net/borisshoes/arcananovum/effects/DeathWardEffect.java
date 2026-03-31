package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DeathWardEffect extends MobEffect implements PolymerStatusEffect {
   public DeathWardEffect(){
      super(MobEffectCategory.BENEFICIAL, 0x270000, ParticleTypes.TRIAL_OMEN);
   }
   
   @Override
   public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier){
      return super.applyEffectTick(world, entity, amplifier);
   }
   
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
      return true;
   }
   
}
