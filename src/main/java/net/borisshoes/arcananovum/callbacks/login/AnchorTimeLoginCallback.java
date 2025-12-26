package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AnchorTimeLoginCallback extends LoginCallback {
   
   private int seconds;
   
   public AnchorTimeLoginCallback(){
      super(Identifier.fromNamespaceAndPath(MOD_ID,"continuum_anchor"));
   }
   
   public AnchorTimeLoginCallback(MinecraftServer server, String player, int seconds){
      this();
      this.playerUUID = player;
      this.seconds = seconds;
   }
   
   @Override
   public boolean canCombine(LoginCallback loginCallback){
      return true;
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
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         ArcanaAchievements.progress(player,ArcanaAchievements.TIMEY_WIMEY.id, seconds);
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
      seconds = data.getIntOr("seconds", 0);
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
      data.putInt("seconds",seconds);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new AnchorTimeLoginCallback();
   }
}

