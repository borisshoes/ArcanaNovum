package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CleansingCharmEvent extends Event {
   public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID,"cleansing_charm_event");
   
   private final ServerPlayer player;
   private final Holder<MobEffect> effect;
   
   public CleansingCharmEvent(ServerPlayer player, Holder<MobEffect> effect){
      super(ID, ((TimedAchievement) ArcanaAchievements.CHRONIC_AILMENT).getTimeFrame());
      this.player = player;
      this.effect = effect;
   }
   
   public Holder<MobEffect> getEffect(){
      return effect;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}
