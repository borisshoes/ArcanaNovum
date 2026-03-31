package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.callbacks.login.ShieldLoginCallback;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public class ShieldTimerCallback extends TickTimerCallback {
   private final float hearts;
   
   public ShieldTimerCallback(int time, ItemStack item, ServerPlayer player, float hearts){
      super(time, item, player);
      this.hearts = hearts;
   }
   
   public float getHearts(){
      return hearts;
   }
   
   @Override
   public void onTimer(){
      try{
         ServerPlayer player1 = player.level().getServer().getPlayerList().getPlayer(player.getUUID());
         if(player1 == null){
            //log("Player ("+player.getEntityName()+") is not connected, creating login callback");
            BorisLib.addLoginCallback(new ShieldLoginCallback(player, hearts));
         }else{
            float removed = Math.max(0, player1.getAbsorptionAmount() - hearts);
            float diff = hearts - player1.getAbsorptionAmount() + removed;
            if(diff != 0){
               ArcanaNovum.data(player1).addXP((int) (ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_SHIELD_OF_FORTITUDE_ABSORB_DAMAGE) * diff)); // Give XP
            }
            if(player1.getAbsorptionAmount() != 0){
               SoundUtils.playSongToPlayer(player1, SoundEvents.AMETHYST_CLUSTER_FALL, .3f, .3f);
            }
            MinecraftUtils.removeMaxAbsorption(player1, ShieldOfFortitude.EFFECT_ID, hearts);
            player1.setAbsorptionAmount(removed);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
