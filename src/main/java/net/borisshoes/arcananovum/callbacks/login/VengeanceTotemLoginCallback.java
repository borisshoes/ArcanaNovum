package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class VengeanceTotemLoginCallback extends LoginCallback {
   
   public VengeanceTotemLoginCallback(){
      super(Identifier.fromNamespaceAndPath(MOD_ID,"totem_of_vengeance"));
   }
   
   public VengeanceTotemLoginCallback(ServerPlayer player){
      this();
      this.playerUUID = player.getStringUUID();
   }
   
   @Override
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         ArcanaNovum.TOTEM_KILL_LIST.add(player.getUUID());
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
   }
   
   @Override
   public CompoundTag getData(){
      this.data = new CompoundTag();
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      return true;
   }
   
   @Override
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new VengeanceTotemLoginCallback();
   }
}
