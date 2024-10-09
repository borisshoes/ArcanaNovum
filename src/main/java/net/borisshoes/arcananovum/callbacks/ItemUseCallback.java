package net.borisshoes.arcananovum.callbacks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class ItemUseCallback {
   public static ActionResult useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      ActionResult result = ActionResult.PASS;
      try{
         // Stub from V 1.0
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return result;
      }
   }
}
