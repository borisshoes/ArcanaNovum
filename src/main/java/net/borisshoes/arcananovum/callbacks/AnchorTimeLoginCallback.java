package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof AnchorTimeLoginCallback newCallback){
         this.seconds += newCallback.seconds;
         return true;
      }
      return false;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaAchievements.progress(player,ArcanaAchievements.TIMEY_WIMEY.id, seconds);
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
      seconds = data.getInt("seconds", 0);
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      NbtCompound data = new NbtCompound();
      data.putInt("seconds",seconds);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new AnchorTimeLoginCallback();
   }
}

