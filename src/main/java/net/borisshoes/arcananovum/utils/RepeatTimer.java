package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.TimerTask;

public class RepeatTimer extends TickTimerCallback {
   private final TimerTask onTimer;
   private final int ticks;
   private final int interval;
   private final ServerWorld world;
   
   public RepeatTimer(int interval, int ticks, TimerTask onTimer, @Nullable ServerWorld world){
      super(interval, null, null);
      this.onTimer = onTimer;
      this.ticks = ticks;
      this.world = world;
      this.interval = interval;
   }
   
   @Override
   public void onTimer(){
      onTimer.run();
      
      if(ticks > 0){
         if(world != null){
            Arcananovum.addTickTimerCallback(world, new RepeatTimer(interval,ticks-1, onTimer, world));
         }else{
            Arcananovum.addTickTimerCallback(new RepeatTimer(interval, ticks-1, onTimer, null));
         }
      }
   }
}
