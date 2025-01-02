package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

public class CleansingCharmEvent extends ArcanaEvent{
   public static final String ID = "cleansing_charm_event";
   
   private final ServerPlayerEntity player;
   private final RegistryEntry<StatusEffect> effect;
   
   public CleansingCharmEvent(ServerPlayerEntity player, RegistryEntry<StatusEffect> effect){
      super(ID, ((TimedAchievement) ArcanaAchievements.CHRONIC_AILMENT).getTimeFrame());
      this.player = player;
      this.effect = effect;
   }
   
   public RegistryEntry<StatusEffect> getEffect(){
      return effect;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}
