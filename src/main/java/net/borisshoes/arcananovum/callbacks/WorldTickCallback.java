package net.borisshoes.arcananovum.callbacks;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.borisshoes.arcananovum.ArcanaNovum.WORLD_TIMER_CALLBACKS;

public class WorldTickCallback {
   
   public static void onWorldTick(ServerWorld serverWorld){
      try{
         // Tick Timer Callbacks
         WORLD_TIMER_CALLBACKS.removeIf(tickTimers(serverWorld)::contains);
         
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @NotNull
   private static ArrayList<Pair<ServerWorld, TickTimerCallback>> tickTimers(ServerWorld serverWorld){
      ArrayList<Pair<ServerWorld,TickTimerCallback>> toRemove = new ArrayList<>();
      for(int i = 0; i < WORLD_TIMER_CALLBACKS.size(); i++){
         Pair<ServerWorld,TickTimerCallback> pair = WORLD_TIMER_CALLBACKS.get(i);
         TickTimerCallback t = pair.getRight();
         if(pair.getLeft().getRegistryKey() == serverWorld.getRegistryKey()){
            if(t.decreaseTimer() == 0){
               t.onTimer();
               toRemove.add(pair);
            }
         }
      }
      return toRemove;
   }
}
