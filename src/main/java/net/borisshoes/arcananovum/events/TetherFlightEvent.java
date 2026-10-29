package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

// A player flung by a Tether Arrow. Until it expires, moves that follow the launch skip vanilla's "moved too quickly" check (see ServerGamePacketListenerImplMixin)
public class TetherFlightEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("tether_flight_event");
   public static final int LAG_ALLOWANCE = 40; // Ticks past the expected landing, for move packets that arrive late
   private static final double MIN_ALIGNMENT = 0.8; // Cosine between a move's heading and the launch's, about 37 degrees
   private static final double FALL_SPEED = 4.0; // Just above terminal velocity, so a long drop after a slow launch still fits
   
   private final UUID playerId;
   private final Vec3 launch;
   
   public TetherFlightEvent(ServerPlayer player, Vec3 launch, int flightTicks){
      super(ID, flightTicks + LAG_ALLOWANCE);
      this.playerId = player.getUUID();
      this.launch = launch;
   }
   
   // Replaces any flight the player is already on
   public static void start(ServerPlayer player, Vec3 launch, int flightTicks){
      for(TetherFlightEvent event : Event.getEventsOfType(TetherFlightEvent.class)){
         if(event.playerId.equals(player.getUUID())) event.markForRemoval();
      }
      Event.addEvent(new TetherFlightEvent(player, launch, flightTicks));
   }
   
   // Whether a move (from where the player was at the start of the server tick, over this many move packets) is part of a tether flight
   public static boolean excuses(ServerPlayer player, Vec3 moved, int packets){
      for(TetherFlightEvent event : Event.getEventsOfType(TetherFlightEvent.class)){
         if(!event.isExpired() && event.playerId.equals(player.getUUID()) && event.fits(moved, packets)) return true;
      }
      return false;
   }
   
   // No faster than the launch (or a fall) could carry the player, and heading the same way horizontally
   private boolean fits(Vec3 moved, int packets){
      double maxStep = Math.max(launch.length(), FALL_SPEED) + 1;
      if(moved.length() > maxStep * Math.max(1, packets)) return false;
      Vec3 heading = launch.multiply(1, 0, 1);
      Vec3 step = moved.multiply(1, 0, 1);
      if(heading.length() < 0.1) return step.length() < 2; // Launched straight up or down
      if(step.length() < 1) return true;
      return heading.normalize().dot(step.normalize()) >= MIN_ALIGNMENT;
   }
}
