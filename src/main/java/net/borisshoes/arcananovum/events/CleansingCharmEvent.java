package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CleansingCharmEvent extends Event {
   public static final Identifier ID = Identifier.of(MOD_ID,"cleansing_charm_event");
   
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
