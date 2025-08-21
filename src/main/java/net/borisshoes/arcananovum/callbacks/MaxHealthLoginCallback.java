package net.borisshoes.arcananovum.callbacks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
      hp = data.getFloat("hp", 0.0f);
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      NbtCompound data = new NbtCompound();
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
   public LoginCallback makeNew(){
      return new MaxHealthLoginCallback();
   }
}
