package net.borisshoes.arcananovum.callbacks;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public abstract class LoginCallback {
   protected String playerUUID;
   protected String id;
   protected NbtCompound data;
   protected ServerWorld world;
   
   public abstract void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server);
   
   public abstract void setData(NbtCompound data);
   
   public abstract NbtCompound getData();
   
   public abstract void combineCallbacks(LoginCallback callback);
   
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
