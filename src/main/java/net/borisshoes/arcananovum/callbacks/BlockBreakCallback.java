package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.GravitonMaul;
import net.borisshoes.arcananovum.items.PickaxeOfCeptyus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBreakCallback {
   public static boolean breakBlock(Level world, Player playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
      ItemStack tool = playerEntity.getMainHandItem();
      if(ArcanaItemUtils.identifyItem(tool) instanceof PickaxeOfCeptyus pick){
         ArcanaNovum.data(playerEntity).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_PICKAXE_OF_CEPTYUS_MINE_BLOCK));
         if(!playerEntity.isShiftKeyDown()){
            pick.veinMine(world,playerEntity,tool,blockPos);
         }
         if(playerEntity instanceof ServerPlayer player && blockState.is(BlockTags.BASE_STONE_OVERWORLD)){
            ArcanaAchievements.progress(player,ArcanaAchievements.DIGGY_HOLE, 1);
         }
      }else if(ArcanaItemUtils.identifyItem(tool) instanceof GravitonMaul maul){
         if(!playerEntity.isShiftKeyDown()){
            maul.treeFell(world,playerEntity,tool,blockPos);
         }
      }
      return true;
   }
}
