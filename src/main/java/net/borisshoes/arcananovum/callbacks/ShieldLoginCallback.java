package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShieldLoginCallback extends LoginCallback{
   
   private float hearts;
   
   public ShieldLoginCallback(){
      this.id = "shield_of_fortitude";
   }
   
   public ShieldLoginCallback(ServerPlayerEntity player, float hearts){
      this.id = "shield_of_fortitude";
      this.world = player.getServer().getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player.getUuidAsString();
      this.hearts = hearts;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
   
         float removed = Math.max(0,player.getAbsorptionAmount()-hearts);
         float diff = hearts - player.getAbsorptionAmount() + removed;
         if(diff != 0){
            PLAYER_DATA.get(player).addXP((int)diff*20); // Give XP
         }
         if(player.getAbsorptionAmount() != 0){
            Utils.playSongToPlayer(player, SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
         }
         player.setAbsorptionAmount(removed);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      //Data tag just has single float for hearts
      this.data = data;
      hearts = data.getFloat("hearts");
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putFloat("hearts",hearts);
      this.data = data;
      return this.data;
   }
}
