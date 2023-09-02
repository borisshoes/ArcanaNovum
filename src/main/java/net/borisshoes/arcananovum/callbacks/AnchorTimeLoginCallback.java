package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class AnchorTimeLoginCallback extends LoginCallback{
   
   private int seconds;
   
   public AnchorTimeLoginCallback(){
      this.id = "continuum_anchor";
   }
   
   public AnchorTimeLoginCallback(MinecraftServer server, String player, int seconds){
      this.id = "continuum_anchor";
      this.world = server.getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player;
      this.seconds = seconds;
   }
   
   @Override
   public void combineCallbacks(LoginCallback callback){
      if(callback instanceof AnchorTimeLoginCallback newCallback)
         this.seconds += newCallback.seconds;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaAchievements.progress(player,"timey_wimey",seconds);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      this.data = data;
      seconds = data.getInt("seconds");
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putInt("seconds",seconds);
      this.data = data;
      return this.data;
   }
}

