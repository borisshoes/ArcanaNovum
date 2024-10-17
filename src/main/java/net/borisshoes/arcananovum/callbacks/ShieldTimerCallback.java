package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

public class ShieldTimerCallback extends TickTimerCallback{
   private final float hearts;
   
   public ShieldTimerCallback(int time, ItemStack item, ServerPlayerEntity player, float hearts){
      super(time, item, player);
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
            ArcanaNovum.addLoginCallback(new ShieldLoginCallback(player,hearts));
         }else{
            float removed = Math.max(0,player1.getAbsorptionAmount()-hearts);
            float diff = hearts - player1.getAbsorptionAmount() + removed;
            if(diff != 0){
               ArcanaNovum.data(player1).addXP((int) (ArcanaConfig.getInt(ArcanaRegistry.SHIELD_OF_FORTITUDE_ABSORB_DAMAGE)*diff)); // Give XP
            }
            if(player1.getAbsorptionAmount() != 0){
               SoundUtils.playSongToPlayer(player1,SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
            }
            MiscUtils.removeMaxAbsorption(player1, ShieldOfFortitude.EFFECT_ID,hearts);
            player1.setAbsorptionAmount(removed);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
