package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class EnsnarementEffect extends StatusEffect implements PolymerStatusEffect {
   public EnsnarementEffect(){
      super(StatusEffectCategory.HARMFUL,0x320b75);
   }
   
   @Override
   public void onApplied(LivingEntity entity, int amplifier){
      super.onApplied(entity, amplifier);
      ArcanaEffectUtils.ensnaredEffect(entity,amplifier,0);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier){
      return true;
   }
}
