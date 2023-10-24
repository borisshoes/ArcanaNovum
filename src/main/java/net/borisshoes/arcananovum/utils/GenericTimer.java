package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.callbacks.TickTimerCallback;

import java.util.TimerTask;

public class GenericTimer extends TickTimerCallback {
   private final TimerTask onTimer;
   
   public GenericTimer(int time, TimerTask onTimer){
      super(time, null, null);
      this.onTimer = onTimer;
   }
   
   @Override
   public void onTimer(){
      onTimer.run();
   }
}
