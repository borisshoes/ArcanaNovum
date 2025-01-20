package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.minecraft.server.network.ServerPlayerEntity;

public class SojournersMaxRunEvent extends ArcanaEvent{
   public static final String ID = "sojourners_max_run_event";
   
   private final ServerPlayerEntity player;
   
   public SojournersMaxRunEvent(ServerPlayerEntity player){
      super(ID, ((TimedAchievement) ArcanaAchievements.RUNNING).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}