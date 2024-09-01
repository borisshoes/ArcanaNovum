package net.borisshoes.arcananovum.core;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;

public abstract class ArcanaBlock extends ArcanaItem {
   
   protected Block block;
   protected BlockEntity blockEntity;
   
   public Block getBlock(){
      return block;
   }
   
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
}
