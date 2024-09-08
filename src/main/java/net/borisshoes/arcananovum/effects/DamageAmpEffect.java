package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class DamageAmpEffect extends StatusEffect implements PolymerStatusEffect {
   
   public static final HashMap<LivingEntity,LivingEntity> AMP_TRACKER = new HashMap<>();
   
   public DamageAmpEffect(){
      super(StatusEffectCategory.HARMFUL,0xdff595, ParticleTypes.CRIT);
   }
   
   @Override
   public boolean applyUpdateEffect(LivingEntity entity, int amplifier){
      return super.applyUpdateEffect(entity,amplifier);
   }
   
   @Override
   public boolean canApplyUpdateEffect(int duration, int amplifier) {
      return true;
   }
   
   public static float buffDamage(float damage, int level){
      return damage * (1.25f + (0.25f*level));
   }
   
   public static void tryTrackDamage(int level, float damage, LivingEntity target){
      if(AMP_TRACKER.containsKey(target)){
         if(target.hasStatusEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT) && AMP_TRACKER.get(target) instanceof ServerPlayerEntity player){
            float buffDmg = buffDamage(damage,level) - damage;
            ArcanaAchievements.progress(player,ArcanaAchievements.SPECTRAL_SUPPORT.id, (int) buffDmg);
            PLAYER_DATA.get(player).addXP((int) (1*buffDmg)); // Add xp
         }else{
            AMP_TRACKER.remove(target);
         }
      }
   }
}
