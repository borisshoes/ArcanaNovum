package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class SojournersMaxRunEvent extends Event {
   public static final Identifier ID = Identifier.of(MOD_ID,"sojourners_max_run_event");
   
   private final ServerPlayerEntity player;
   
   public SojournersMaxRunEvent(ServerPlayerEntity player){
      super(ID, ((TimedAchievement) ArcanaAchievements.RUNNING).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}