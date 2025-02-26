package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ItemReturnLoginCallback extends LoginCallback{
   
   private ItemStack item;
   
   public ItemReturnLoginCallback(){
      this.id = "item_return";
   }
   
   public ItemReturnLoginCallback(ServerPlayerEntity player, ItemStack item){
      this.id = "item_return";
      this.world = player.getServer().getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player.getUuidAsString();
      this.item = item;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         if(!player.isAlive() || !player.getInventory().insertStack(item)){
            ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(item,player));
         }
      }
   }
   
   @Override
   public void setData(NbtCompound data, RegistryWrapper.WrapperLookup registryLookup){
      this.data = data;
      this.item = ItemStack.fromNbtOrEmpty(registryLookup,data.getCompound("item"));
   }
   
   @Override
   public NbtCompound getData(RegistryWrapper.WrapperLookup registryLookup){
      NbtCompound data = new NbtCompound();
      data.put("item",this.item.toNbtAllowEmpty(registryLookup));
      this.data = data;
      return this.data;
   }
   
   @Override
   public boolean combineCallbacks(LoginCallback callback){
      if(callback instanceof ItemReturnLoginCallback returnCallback){
         return this.item.equals(returnCallback.item) || returnCallback.item.isEmpty();
      }
      return false;
   }
   
   @Override
   public LoginCallback makeNew(){
      return new ItemReturnLoginCallback();
   }
}
