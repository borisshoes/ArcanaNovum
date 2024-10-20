package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class VengeanceTotemTimerCallback extends TickTimerCallback{
   
   private final Entity attacker;
   
   public VengeanceTotemTimerCallback(int time, ItemStack item, ServerPlayerEntity player, @Nullable Entity attacker){
      super(time, item, player);
      this.attacker = attacker;
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayerEntity player1 = player.getServer().getPlayerManager().getPlayer(player.getUuid());
         if(player1 == null){
            //log("Player ("+player.getEntityName()+") is not connected, creating login callback");
            ArcanaNovum.addLoginCallback(new VengeanceTotemLoginCallback(player,attacker == null ? null : attacker.getUuid()));
         }else{
            boolean survives = false;
            if(attacker != null){
               boolean notFound = true;
               for(ServerWorld world : player.getServer().getWorlds()){
                  Entity foundAttacker = world.getEntity(attacker.getUuid());
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
               player1.damage(ArcanaDamageTypes.of(player1.getEntityWorld(),ArcanaDamageTypes.VENGEANCE_TOTEM,attacker), player1.getMaxHealth()*10);
            }else{
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_SURVIVE)); // Give XP
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
