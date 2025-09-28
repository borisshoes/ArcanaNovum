package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.borislib.events.Event;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class BinaryBladesMaxEnergyEvent extends Event {
   public static final Identifier ID = Identifier.of(MOD_ID,"binary_blades_max_energy_event");
   
   private final ServerPlayerEntity player;
   
   public BinaryBladesMaxEnergyEvent(ServerPlayerEntity player){
      super(ID, ((TimedAchievement) ArcanaAchievements.STARBURST_STREAM).getTimeFrame());
      this.player = player;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}