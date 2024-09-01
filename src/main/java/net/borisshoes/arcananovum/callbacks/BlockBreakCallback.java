package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.PickaxeOfCeptyus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class BlockBreakCallback {
   public static boolean breakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
      ItemStack tool = playerEntity.getMainHandStack();
      if(ArcanaItemUtils.identifyItem(tool) instanceof PickaxeOfCeptyus pick){
         PLAYER_DATA.get(playerEntity).addXP(1);
         if(!playerEntity.isSneaking()){
            pick.veinMine(world,playerEntity,tool,blockPos);
         }
         if(playerEntity instanceof ServerPlayerEntity player && blockState.isIn(BlockTags.BASE_STONE_OVERWORLD)){
            ArcanaAchievements.progress(player,ArcanaAchievements.DIGGY_HOLE.id, 1);
         }
      }
      return true;
   }
}
