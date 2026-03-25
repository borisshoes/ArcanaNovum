package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.arrows.SiphoningArrows;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;

public class OverhealLoginCallback extends LoginCallback {
   
   private float hearts;
   
   public OverhealLoginCallback(){
      super(ArcanaRegistry.arcanaId("siphoning_overheal_augment"));
   }
   
   public OverhealLoginCallback(ServerPlayer player, float hearts){
      this();
      this.playerUUID = player.getStringUUID();
      this.hearts = hearts;
   }
   
   @Override
   public void onLogin(ServerGamePacketListenerImpl netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayer player = netHandler.player;
      if(player.getStringUUID().equals(playerUUID)){
         
         float removed = Math.max(0,player.getAbsorptionAmount()-hearts);
         
         if(player.getAbsorptionAmount() != 0){
            SoundUtils.playSongToPlayer(player, SoundEvents.AMETHYST_CLUSTER_FALL, .3f, .3f);
         }
         MinecraftUtils.removeMaxAbsorption(player, SiphoningArrows.EFFECT_ID,hearts);
         player.setAbsorptionAmount(removed);
      }
   }
   
   @Override
   public void setData(CompoundTag data){
      //Data tag just has single float for hearts
      this.data = data;
      hearts = data.getFloatOr("hearts", 0.0f);
   }
   
   @Override
   public CompoundTag getData(){
      CompoundTag data = new CompoundTag();
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