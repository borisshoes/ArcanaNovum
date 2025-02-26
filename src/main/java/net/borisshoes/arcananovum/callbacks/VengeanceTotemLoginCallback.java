package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class VengeanceTotemLoginCallback extends LoginCallback{
   
   public VengeanceTotemLoginCallback(){
      this.id = "totem_of_vengeance";
   }
   
   public VengeanceTotemLoginCallback(ServerPlayerEntity player){
      this.id = "totem_of_vengeance";
      this.world = player.getServer().getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player.getUuidAsString();
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaNovum.TOTEM_KILL_LIST.add(player.getUuid());
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      this.data = new NbtCompound();
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      return true;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new VengeanceTotemLoginCallback();
   }
}
