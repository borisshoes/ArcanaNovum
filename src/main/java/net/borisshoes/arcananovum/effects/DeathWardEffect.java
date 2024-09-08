package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;

public class DeathWardEffect extends StatusEffect implements PolymerStatusEffect {
   public DeathWardEffect(){
      super(StatusEffectCategory.BENEFICIAL,0x270000, ParticleTypes.TRIAL_OMEN);
   }
   
   @Override
   public boolean applyUpdateEffect(LivingEntity entity, int amplifier){
      return super.applyUpdateEffect(entity,amplifier);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
}
