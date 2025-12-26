package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.core.LeftClickItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlockAttackCallback {
   
   public static InteractionResult attackBlock(Player playerEntity, Level world, InteractionHand hand, BlockPos blockPos, Direction direction){
      ItemStack item = playerEntity.getItemInHand(hand);
      InteractionResult result = InteractionResult.PASS;
      try{
         LeftClickItem arcanaItem = null;
         if(ArcanaItemUtils.isLeftClickItem(item)){
            arcanaItem = ArcanaItemUtils.identifyLeftClickItem(item);
            boolean useReturn = arcanaItem.attackBlock(playerEntity, world, hand, blockPos, direction);
            result = useReturn ? InteractionResult.PASS : InteractionResult.SUCCESS_SERVER;
         }
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return InteractionResult.PASS;
      }
      
   }
}
