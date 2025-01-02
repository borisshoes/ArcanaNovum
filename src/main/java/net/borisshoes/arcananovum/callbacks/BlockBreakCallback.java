package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.GravitonMaul;
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

public class BlockBreakCallback {
   public static boolean breakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
      ItemStack tool = playerEntity.getMainHandStack();
      if(ArcanaItemUtils.identifyItem(tool) instanceof PickaxeOfCeptyus pick){
         ArcanaNovum.data(playerEntity).addXP(ArcanaConfig.getInt(ArcanaRegistry.PICKAXE_OF_CEPTYUS_MINE_BLOCK));
         if(!playerEntity.isSneaking()){
            pick.veinMine(world,playerEntity,tool,blockPos);
         }
         if(playerEntity instanceof ServerPlayerEntity player && blockState.isIn(BlockTags.BASE_STONE_OVERWORLD)){
            ArcanaAchievements.progress(player,ArcanaAchievements.DIGGY_HOLE.id, 1);
         }
      }else if(ArcanaItemUtils.identifyItem(tool) instanceof GravitonMaul maul){
         if(!playerEntity.isSneaking()){
            maul.treeFell(world,playerEntity,tool,blockPos);
         }
      }
      return true;
   }
}
