package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class RepeatTimer extends TickTimerCallback {
   private final Runnable task;
   private final int ticks;
   private final int interval;
   private final ServerWorld world;
   
   
   public RepeatTimer(int interval, int ticks, Runnable task, @Nullable ServerWorld world){
      super(interval, null, null);
      this.ticks = ticks;
      this.world = world;
      this.interval = interval;
      this.task = task;
   }
   
   @Override
   public void onTimer(){
      task.run();
      
      if(ticks > 0){
         if(world != null){
            ArcanaNovum.addTickTimerCallback(world, new RepeatTimer(interval,ticks-1, task, world));
         }else{
            ArcanaNovum.addTickTimerCallback(new RepeatTimer(interval, ticks-1, task, null));
         }
      }
   }
}
