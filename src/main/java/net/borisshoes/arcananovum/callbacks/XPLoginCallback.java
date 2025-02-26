package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class XPLoginCallback extends LoginCallback{
   
   private int xp;
   
   public XPLoginCallback(){
      this.id = "xp_login_callback";
   }
   
   public XPLoginCallback(MinecraftServer server, String player, int xp){
      this.id = "xp_login_callback";
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
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaNovum.data(player).addXP(xp);
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
      xp = data.getInt("xp");
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
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