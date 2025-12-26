package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.callbacks.login.VengeanceTotemLoginCallback;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VengeanceTotemTimerCallback extends TickTimerCallback {
   
   private final Entity attacker;
   private boolean avenged;
   
   public VengeanceTotemTimerCallback(int time, ItemStack item, ServerPlayer player, @Nullable Entity attacker){
      super(time, item, player);
      this.attacker = attacker;
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayer player1 = player.level().getServer().getPlayerList().getPlayer(player.getUUID());
         if(player1 == null){
            if(attacker != null){
               if(avenged){
                  BorisLib.addLoginCallback(new XPLoginCallback(player.level().getServer(),player.getStringUUID(),ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_SURVIVE))); // Give XP
               }else{
                  BorisLib.addLoginCallback(new VengeanceTotemLoginCallback(player));
               }
            }
         }else{
            if(attacker != null){
               if(avenged){
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.TOTEM_OF_VENGEANCE_SURVIVE)); // Give XP
               }else{
                  player1.hurtServer(player.level(), ArcanaDamageTypes.of(player1.level(),ArcanaDamageTypes.VENGEANCE_TOTEM,attacker), player1.getMaxHealth()*10);
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
