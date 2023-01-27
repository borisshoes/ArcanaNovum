package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.BlockItem;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.PickaxeOfPluto;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

public class BlockBreakCallback {
   public static boolean breakBlock(World world, PlayerEntity playerEntity, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity){
      List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
      for(MagicBlock magicBlock : blocks){
         if(magicBlock.getPos().equals(blockPos)){
            NbtCompound data = magicBlock.getData();
            String magicId = data.getString("id");
            MagicItem itemType = MagicItemUtils.getItemFromId(magicId);
            if(itemType instanceof BlockItem){
               List<ItemStack> drops = ((BlockItem) itemType).dropFromBreak(world,playerEntity,blockPos,blockState,blockEntity,data);
               for(ItemStack drop : drops){
                  world.spawnEntity(new ItemEntity(world,blockPos.getX(),blockPos.getY(),blockPos.getZ(),drop));
               }
            }
            world.breakBlock(blockPos,false,playerEntity);
            return false;
         }
      }
      
      ItemStack tool = playerEntity.getMainHandStack();
      if(MagicItemUtils.identifyItem(tool) instanceof PickaxeOfPluto pick){
         PLAYER_DATA.get(playerEntity).addXP(1);
         if(!playerEntity.isSneaking()){
            pick.veinMine(world,playerEntity,tool,blockPos);
         }
         if((blockState.getMaterial() == Material.STONE) && playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.progress(player,"diggy_hole",1);
      }
      return true;
   }
}
