package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.TimerTask;

public class GenericTimer extends TickTimerCallback {
   private TimerTask onTimer;
   
   public GenericTimer(int time, TimerTask onTimer){
      super(time, null, null);
      this.onTimer = onTimer;
   }
   
   @Override
   public void onTimer(){
      onTimer.run();
   }
}
