package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EnsnarementEffect extends MobEffect implements PolymerMobEffect {
   public EnsnarementEffect(){
      super(MobEffectCategory.HARMFUL, 0x320b75);
   }
   
   @Override
   public void onEffectStarted(LivingEntity entity, int amplifier){
      super.onEffectStarted(entity, amplifier);
      ArcanaEffectUtils.ensnaredEffect(entity, amplifier, 0);
   }
   
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
      return true;
   }
}
