package net.borisshoes.arcananovum.callbacks;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;

public abstract class TickTimerCallback {
   private int timer;
   protected ItemStack item;
   protected ServerPlayerEntity player;
   
   public TickTimerCallback(int time, @Nullable ItemStack item, @Nullable ServerPlayerEntity player){
      timer = time;
      this.item = item;
      this.player = player;
   }
   
   public abstract void onTimer();
   
   public int getTimer(){
      return timer;
   }
   
   public int decreaseTimer(){
      return this.timer--;
   }
   
   public void setTimer(int timer){
      this.timer = timer;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}
