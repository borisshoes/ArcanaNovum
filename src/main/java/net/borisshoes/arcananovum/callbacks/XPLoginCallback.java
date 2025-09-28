package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class XPLoginCallback extends LoginCallback {
   
   private int xp;
   
   public XPLoginCallback(){
      super(Identifier.of(MOD_ID,"xp_login_callback"));
   }
   
   public XPLoginCallback(MinecraftServer server, String player, int xp){
      this();
      this.world = server.getWorld(ServerWorld.OVERWORLD);
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
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaNovum.data(player).addXP(xp);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      this.data = data;
      xp = data.getInt("xp", 0);
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putInt("xp", xp);
      this.data = data;
      return this.data;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new XPLoginCallback();
   }
}