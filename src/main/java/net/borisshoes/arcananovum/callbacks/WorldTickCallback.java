package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.gui.WatchedGui;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import static net.borisshoes.arcananovum.Arcananovum.*;

public class WorldTickCallback {
   
   public static void onWorldTick(ServerWorld serverWorld){
      try{
         
         // Check up on Guis
         Iterator<Map.Entry<ServerPlayerEntity, WatchedGui>> iter3 = OPEN_GUIS.entrySet().iterator();
         while(iter3.hasNext()){
            Map.Entry<ServerPlayerEntity, WatchedGui> openGui = iter3.next();
            ServerPlayerEntity player = openGui.getKey();
            WatchedGui watchedGui = openGui.getValue();
            BlockEntity blockEntity = watchedGui.getBlockEntity();
            
            if(blockEntity != null){
               BlockState state = player.getServerWorld().getBlockState(blockEntity.getPos());
   
               if(serverWorld.getRegistryKey().getValue().equals(blockEntity.getWorld().getRegistryKey().getValue())){
//               System.out.println("Open Gui: ");
//               System.out.println(openGui.getValue().getTokenBlock().getPos());
//               System.out.println(state.getBlock()+" "+player.getWorld().getRegistryKey().getValue());
                  if(!blockEntity.getType().supports(state)){
                     watchedGui.close();
                  }
                  if(player.currentScreenHandler == player.playerScreenHandler){
                     iter3.remove();
                  }
               }
            }
      
            
         }
         
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
