package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class MaxHealthLoginCallback extends LoginCallback {
   private float hp;
   
   public MaxHealthLoginCallback(){
      super(ArcanaRegistry.arcanaId("max_health_login_callback"));
   }
   
   public MaxHealthLoginCallback(MinecraftServer server, String player, float health){
      this();
      this.playerUUID = player;
      this.hp = health;
   }
   
   @Override
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         player.collectEquipmentChanges();
         player.setHealth(hp);
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
      hp = data.getFloatOr("hp", 0.0f);
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
      data.putFloat("hp",hp);
      this.data = data;
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof MaxHealthLoginCallback newCallback && newCallback.hp > hp){
         this.hp = newCallback.hp;
         return true;
      }
      return false;
   }
   
   @Override
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new MaxHealthLoginCallback();
   }
}
