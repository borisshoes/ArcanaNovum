package net.borisshoes.arcananovum.callbacks.login;

import net.borisshoes.borislib.callbacks.LoginCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class MaxHealthLoginCallback extends LoginCallback {
   private float hp;
   
   public MaxHealthLoginCallback(){
      super(Identifier.of(MOD_ID,"max_health_login_callback"));
   }
   
   public MaxHealthLoginCallback(MinecraftServer server, String player, float health){
      this();
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
      hp = data.getFloat("hp", 0.0f);
   }
   
   @Override
   public NbtCompound getData(){
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
   public boolean canCombine(LoginCallback loginCallback){
      return true;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new MaxHealthLoginCallback();
   }
}
