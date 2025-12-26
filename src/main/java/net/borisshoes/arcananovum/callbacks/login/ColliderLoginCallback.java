package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ColliderLoginCallback extends LoginCallback {
   
   private int blocks;
   
   public ColliderLoginCallback(){
      super(Identifier.fromNamespaceAndPath(MOD_ID,"igneous_collider"));
   }
   
   public ColliderLoginCallback(MinecraftServer server, String player, int blocks){
      this();
      this.playerUUID = player;
      this.blocks = blocks;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof ColliderLoginCallback newCallback){
         this.blocks += newCallback.blocks;
         return true;
      }
      return false;
   }
   
   @Override
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         ArcanaAchievements.progress(player,ArcanaAchievements.ENDLESS_EXTRUSION.id,blocks);
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
      blocks = data.getIntOr("blocks", 0);
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
      data.putInt("blocks",blocks);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new ColliderLoginCallback();
   }
}

