package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.callbacks.login.OverhealLoginCallback;
import net.borisshoes.arcananovum.items.arrows.SiphoningArrows;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class OverhealTimerCallback extends TickTimerCallback {
   private final float hearts;
   
   public OverhealTimerCallback(int time, ServerPlayerEntity player, float hearts){
      super(time, null, player);
      this.hearts = hearts;
   }
   
   public float getHearts(){
      return hearts;
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayerEntity player1 = player.getServer().getPlayerManager().getPlayer(player.getUuid());
         if(player1 == null){
            //log("Player ("+player.getEntityName()+") is not connected, creating login callback");
            BorisLib.addLoginCallback(new OverhealLoginCallback(player,hearts));
         }else{
            float removed = Math.max(0,player1.getAbsorptionAmount()-hearts);
            if(player1.getAbsorptionAmount() != 0){
               SoundUtils.playSongToPlayer(player1, SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
            }
            MinecraftUtils.removeMaxAbsorption(player1, SiphoningArrows.EFFECT_ID,hearts);
            player1.setAbsorptionAmount(removed);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
