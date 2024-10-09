package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class VengeanceTotemLoginCallback extends LoginCallback{
   private UUID attacker;
   
   public VengeanceTotemLoginCallback(){
      this.id = "totem_of_vengeance";
   }
   
   public VengeanceTotemLoginCallback(ServerPlayerEntity player, @Nullable UUID attacker){
      this.id = "totem_of_vengeance";
      this.world = player.getServer().getWorld(ServerWorld.OVERWORLD);
      this.playerUUID = player.getUuidAsString();
      this.attacker = attacker;
   }
   
   @Override
   public void onLogin(ServerPlayNetworkHandler netHandler, MinecraftServer server){
      // Double check that this is the correct player before running timer
      ServerPlayerEntity player = netHandler.player;
      if(player.getUuidAsString().equals(playerUUID)){
         boolean survives = false;
         Entity foundAttacker = null;
         if(attacker != null){
            boolean notFound = true;
            for(ServerWorld world : player.getServer().getWorlds()){
               foundAttacker = world.getEntity(attacker);
               if(foundAttacker != null){
                  if(!foundAttacker.isAlive()){
                     survives = true;
                     notFound = false;
                  }else{
                     notFound = false;
                  }
                  break;
               }
            }
            if(notFound){
               survives = true;
            }
         }
         
         if(!survives){
            player.damage(world,ArcanaDamageTypes.of(player.getEntityWorld(),ArcanaDamageTypes.VENGEANCE_TOTEM,foundAttacker), player.getMaxHealth()*10);
         }else{
            PLAYER_DATA.get(player).addXP(4000); // Give XP
         }
      }
   }
   
   @Override
   public void setData(NbtCompound data){
      //Data tag just has single float for hearts
      this.data = data;
      String attackerString = data.getString("attacker");
      this.attacker = attackerString.isEmpty() ? null : MiscUtils.getUUID(attackerString);
   }
   
   @Override
   public NbtCompound getData(){
      NbtCompound data = new NbtCompound();
      data.putString("attacker",this.attacker == null ? "" : this.attacker.toString());
      this.data = data;
      return this.data;
   }
   
   @Override
   public void combineCallbacks(LoginCallback callback){

   }
}
