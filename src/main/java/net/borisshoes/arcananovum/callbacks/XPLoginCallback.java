package net.borisshoes.arcananovum.callbacks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

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
   public void combineCallbacks(LoginCallback callback){
      if(callback instanceof XPLoginCallback newCallback)
         this.xp += newCallback.xp;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         PLAYER_DATA.get(player).addXP(xp);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      this.data = data;
      xp = data.getInt("xp");
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putInt("xp", xp);
      this.data = data;
      return this.data;
   }
}