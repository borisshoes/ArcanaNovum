package net.borisshoes.arcananovum.callbacks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MaxHealthLoginCallback extends LoginCallback{
   private float hp;
   
   public MaxHealthLoginCallback(){
      this.id = "max_health_login_callback";
   }
   
   public MaxHealthLoginCallback(MinecraftServer server, String player, float health){
      this.id = "max_health_login_callback";
      this.world = server.getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player;
      this.hp = health;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         player.getEquipmentChanges();
         player.setHealth(hp);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      this.data = data;
      hp = data.getFloat("hp");
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putFloat("hp",hp);
      this.data = data;
      return this.data;
   }
   
   @Override
   public void combineCallbacks(LoginCallback callback){
      if(callback instanceof MaxHealthLoginCallback newCallback && newCallback.hp > hp)
         this.hp = newCallback.hp;
   }
}
