package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.borislib.events.Event;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RunicArrowHitEvent extends Event {
   public static final Identifier ID = Identifier.of(MOD_ID,"runic_arrow_hit_event");
   
   private final ServerPlayerEntity player;
   private final RunicArrow arrowType;
   
   public RunicArrowHitEvent(ServerPlayerEntity player, RunicArrow arrowType){
      super(ID, ((TimedAchievement) ArcanaAchievements.ARROW_FOR_EVERY_FOE).getTimeFrame());
      this.player = player;
      this.arrowType = arrowType;
   }
   
   public RunicArrow getArrowType(){
      return arrowType;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}
