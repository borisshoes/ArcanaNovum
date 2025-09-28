package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.items.arrows.SiphoningArrows;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class OverhealLoginCallback extends LoginCallback {
   
   private float hearts;
   
   public OverhealLoginCallback(){
      super(Identifier.of(MOD_ID,"siphoning_overheal_augment"));
   }
   
   public OverhealLoginCallback(ServerPlayerEntity player, float hearts){
      this();
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
         
         if(player.getAbsorptionAmount() != 0){
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
         }
         MinecraftUtils.removeMaxAbsorption(player, SiphoningArrows.EFFECT_ID,hearts);
         player.setAbsorptionAmount(removed);
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      //Data tag just has single float for hearts
      this.data = data;
      hearts = data.getFloat("hearts", 0.0f);
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putFloat("hearts",hearts);
      this.data = data;
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof OverhealLoginCallback newCallback){
         this.hearts += newCallback.hearts;
         return true;
      }
      return false;
   }
   
   @Override
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new OverhealLoginCallback();
   }
}