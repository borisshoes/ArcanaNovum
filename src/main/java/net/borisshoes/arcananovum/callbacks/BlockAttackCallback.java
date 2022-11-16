package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.core.LeftClickItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlockAttackCallback {
   
   public static ActionResult attackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction){
      ItemStack item = playerEntity.getStackInHand(hand);
      //System.out.println(hand+" "+item);
      ActionResult result = ActionResult.PASS;
      try{
         LeftClickItem magicItem = null;
         if(MagicItemUtils.isLeftClickItem(item)){
            magicItem = MagicItemUtils.identifyLeftClickItem(item);
            boolean useReturn = magicItem.attackBlock(playerEntity, world, hand, blockPos, direction);
            result = useReturn ? ActionResult.PASS : ActionResult.SUCCESS;
         }
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
      
   }
}
