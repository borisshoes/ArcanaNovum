package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class RunicArrowHitEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("runic_arrow_hit_event");
   
   private final ServerPlayer player;
   private final RunicArrow arrowType;
   
   public RunicArrowHitEvent(ServerPlayer player, RunicArrow arrowType){
      super(ID, ((TimedAchievement) ArcanaAchievements.ARROW_FOR_EVERY_FOE).getTimeFrame());
      this.player = player;
      this.arrowType = arrowType;
   }
   
   public RunicArrow getArrowType(){
      return arrowType;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}
