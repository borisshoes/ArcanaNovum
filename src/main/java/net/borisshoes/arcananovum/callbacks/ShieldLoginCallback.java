package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

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
            ArcanaNovum.data(player).addXP((int) (ArcanaConfig.getInt(ArcanaRegistry.SHIELD_OF_FORTITUDE_ABSORB_DAMAGE)*diff)); // Give XP
         }
         if(player.getAbsorptionAmount() != 0){
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
         }
         MiscUtils.removeMaxAbsorption(player, ShieldOfFortitude.EFFECT_ID,hearts);
         player.setAbsorptionAmount(removed);
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      //Data tag just has single float for hearts
      this.data = data;
      hearts = data.getFloat("hearts");
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      NbtCompound data = new NbtCompound();
      data.putFloat("hearts",hearts);
      this.data = data;
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof ShieldLoginCallback newCallback){
         this.hearts += newCallback.hearts;
         return true;
      }
      return false;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new ShieldLoginCallback();
   }
}
