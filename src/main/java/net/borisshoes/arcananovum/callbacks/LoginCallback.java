package net.borisshoes.arcananovum.callbacks;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;

public abstract class LoginCallback {
   protected String playerUUID;
   protected String id;
   protected NbtCompound data;
   protected ServerWorld world;
   
   public abstract void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server);
   
   public abstract void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup);
   
   public abstract NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup);
   
   public abstract boolean combineCallbacks(LoginCallback callback);
   
   public abstract LoginCallback makeNew();
   
   public void setPlayer(String playerUUID){ this.playerUUID = playerUUID;}
   
   public String getId(){
      return id;
   }
   
   public String getPlayer(){
      return playerUUID;
   }
   
   public ServerWorld getWorld(){
      return world;
   }
}
