package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class BinaryBladesMaxEnergyEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("binary_blades_max_energy_event");
   
   private final ServerPlayer player;
   
   public BinaryBladesMaxEnergyEvent(ServerPlayer player){
      super(ID, ((TimedAchievement) ArcanaAchievements.STARBURST_STREAM).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}