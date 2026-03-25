package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class SojournersMaxRunEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("sojourners_max_run_event");
   
   private final ServerPlayer player;
   
   public SojournersMaxRunEvent(ServerPlayer player){
      super(ID, ((TimedAchievement) ArcanaAchievements.RUNNING).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}