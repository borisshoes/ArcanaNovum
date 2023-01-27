package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ColliderLoginCallback extends LoginCallback{
   
   private int blocks;
   
   public ColliderLoginCallback(){
      this.id = "igneous_collider";
   }
   
   public ColliderLoginCallback(MinecraftServer server, String player, int blocks){
      this.id = "igneous_collider";
      this.world = server.getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player;
      this.blocks = blocks;
   }
   
   @Override
   public void combineCallbacks(LoginCallback callback){
      if(callback instanceof ColliderLoginCallback newCallback)
         this.blocks += newCallback.blocks;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         ArcanaAchievements.progress(player,"endless_extrusion",blocks);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      this.data = data;
      blocks = data.getInt("blocks");
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putInt("blocks",blocks);
      this.data = data;
      return this.data;
   }
}

