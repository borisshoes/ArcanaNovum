package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.minecraft.server.network.ServerPlayerEntity;

public class BinaryBladesMaxEnergyEvent extends ArcanaEvent{
   public static final String ID = "binary_blades_max_energy_event";
   
   private final ServerPlayerEntity player;
   
   public BinaryBladesMaxEnergyEvent(ServerPlayerEntity player){
      super(ID, ((TimedAchievement) ArcanaAchievements.STARBURST_STREAM).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}