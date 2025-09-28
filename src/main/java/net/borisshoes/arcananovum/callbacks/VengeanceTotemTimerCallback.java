package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.callbacks.login.VengeanceTotemLoginCallback;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class VengeanceTotemTimerCallback extends TickTimerCallback {
   
   private final Entity attacker;
   private boolean avenged;
   
   public VengeanceTotemTimerCallback(int time, ItemStack item, ServerPlayerEntity player, @Nullable Entity attacker){
      super(time, item, player);
      this.attacker = attacker;
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayerEntity player1 = player.getServer().getPlayerManager().getPlayer(player.getUuid());
         if(player1 == null){
            if(attacker != null){
               if(avenged){
                  BorisLib.addLoginCallback(new XPLoginCallback(player.getServer(),player.getUuidAsString(),ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_SURVIVE))); // Give XP
               }else{
                  BorisLib.addLoginCallback(new VengeanceTotemLoginCallback(player));
               }
            }
         }else{
            if(attacker != null){
               if(avenged){
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_SURVIVE)); // Give XP
               }else{
                  player1.damage(player.getWorld(), ArcanaDamageTypes.of(player1.getWorld(),ArcanaDamageTypes.VENGEANCE_TOTEM,attacker), player1.getMaxHealth()*10);
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public Entity getAttacker(){
      return attacker;
   }
   
   public void setAvenged(){
      this.avenged = true;
   }
}
