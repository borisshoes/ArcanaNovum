package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.items.LeftClickItem;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.items.UsableItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

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
