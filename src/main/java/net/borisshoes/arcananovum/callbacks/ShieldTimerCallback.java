package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ShieldTimerCallback extends TickTimerCallback{
   private final float hearts;
   
   public ShieldTimerCallback(int time, ItemStack item, ServerPlayerEntity player, float hearts){
      super(time, item, player);
      this.hearts = hearts;
   }
   
   @Override
   public void onTimer(){
      float removed = Math.max(0,player.getAbsorptionAmount()-hearts);
      float diff = hearts - player.getAbsorptionAmount() + removed;
      if(diff != 0){
         PLAYER_DATA.get(player).addXP((int)diff*20); // Give XP
      }
      if(player.getAbsorptionAmount() != 0){
         Utils.playSongToPlayer(player,SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL, .3f, .3f);
      }
      player.setAbsorptionAmount(removed);
   }
}
