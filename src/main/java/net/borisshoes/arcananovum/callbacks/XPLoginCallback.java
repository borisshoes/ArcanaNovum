package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class XPLoginCallback extends LoginCallback {
   
   private int xp;
   
   public XPLoginCallback(){
      super(Identifier.fromNamespaceAndPath(MOD_ID,"xp_login_callback"));
   }
   
   public XPLoginCallback(MinecraftServer server, String player, int xp){
      this();
      this.playerUUID = player;
      this.xp = xp;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof XPLoginCallback newCallback){
         this.xp += newCallback.xp;
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
         ArcanaNovum.data(player).addXP(xp);
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      this.data = data;
      xp = data.getIntOr("xp", 0);
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
      data.putInt("xp", xp);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new XPLoginCallback();
   }
}