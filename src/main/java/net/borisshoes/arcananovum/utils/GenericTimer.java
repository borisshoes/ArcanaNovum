package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.callbacks.TickTimerCallback;

public class GenericTimer extends TickTimerCallback {
   private final Runnable task;
   
   public GenericTimer(int time, Runnable task){
      super(time,null,null);
      this.task = task;
   }
   
   @Override
   public void onTimer(){
      task.run();
   }
}
