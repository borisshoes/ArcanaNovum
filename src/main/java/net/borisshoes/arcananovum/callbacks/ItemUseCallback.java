package net.borisshoes.arcananovum.callbacks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemUseCallback {
   public static TypedActionResult<ItemStack> useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      TypedActionResult<ItemStack> result = TypedActionResult.pass(item);
      try{
         // Stub from V 1.0
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return result;
      }
   }
}
