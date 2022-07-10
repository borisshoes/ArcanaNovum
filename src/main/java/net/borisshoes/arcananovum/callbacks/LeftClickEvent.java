package net.borisshoes.arcananovum.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;

@Deprecated
public interface LeftClickEvent {
   Event<LeftClickEvent> EVENT = EventFactory.createArrayBacked(LeftClickEvent.class, listeners -> (player, world, hand) ->{
      for (LeftClickEvent event : listeners){
         event.onPlayerLeftClick(player, world, hand);
      }
   });
   
   void onPlayerLeftClick(ServerPlayerEntity player, ServerWorld server, Hand hand);
}
