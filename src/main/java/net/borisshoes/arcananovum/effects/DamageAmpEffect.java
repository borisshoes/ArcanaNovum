package net.borisshoes.arcananovum.effects;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;

public class DamageAmpEffect extends MobEffect implements PolymerStatusEffect {
   
   public static final HashMap<LivingEntity, LivingEntity> AMP_TRACKER = new HashMap<>();
   
   public DamageAmpEffect(){
      super(MobEffectCategory.HARMFUL,0xdff595, ParticleTypes.CRIT);
   }
   
   @Override
   public boolean applyEffectTick(ServerLevel world, LivingEntity entity, int amplifier){
      return super.applyEffectTick(world, entity, amplifier);
   }
   
   @Override
   public boolean shouldApplyEffectTickThisTick(int duration, int amplifier){
      return true;
   }
   
   public static float buffDamage(float damage, int level){
      return damage * (1.25f + (0.25f*level));
   }
   
   public static void tryTrackDamage(int level, float damage, LivingEntity target){
      if(AMP_TRACKER.containsKey(target)){
         if(target.hasEffect(ArcanaRegistry.DAMAGE_AMP_EFFECT) && AMP_TRACKER.get(target) instanceof ServerPlayer player){
            float buffDmg = buffDamage(damage,level) - damage;
            ArcanaAchievements.progress(player,ArcanaAchievements.SPECTRAL_SUPPORT.id, (int) buffDmg);
            ArcanaNovum.data(player).addXP(Math.min(ArcanaConfig.getInt(ArcanaRegistry.DAMAGE_AMP_CAP),(int) (ArcanaConfig.getInt(ArcanaRegistry.DAMAGE_AMP_PER_10)*buffDmg/10.0))); // Add xp
         }else{
            AMP_TRACKER.remove(target);
         }
      }
   }
}
