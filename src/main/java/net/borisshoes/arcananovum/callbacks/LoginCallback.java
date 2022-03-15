package net.borisshoes.arcananovum.callbacks;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;

public abstract class LoginCallback {
   protected ServerPlayerEntity player;
   
   public LoginCallback(ServerPlayerEntity player){
      this.player = player;
   }
   
   public abstract void onTimer();
}
