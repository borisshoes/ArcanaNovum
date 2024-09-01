package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public abstract class ArcanaPolymerBlock extends Block implements PolymerBlock {
   
   public ArcanaPolymerBlock(Settings settings){
      super(settings);
   }
   
   @Override
   public abstract BlockState getPolymerBlockState(BlockState state);
   @Override
   public BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player){
      return PolymerBlock.super.getPolymerBlockState(state, player);
   }
   
   @Override
   public void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player){
      PolymerBlock.super.onPolymerBlockSend(blockState, pos, player);
   }
   
   @Override
   public boolean forceLightUpdates(BlockState blockState){
      return PolymerBlock.super.forceLightUpdates(blockState);
   }
   
   @Override
   public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player){
      return PolymerBlock.super.getPolymerBreakEventBlockState(state, player);
   }
   
   @Override
   public Block getPolymerReplacement(ServerPlayerEntity player){
      return PolymerBlock.super.getPolymerReplacement(player);
   }
   
   @Override
   public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player){
      return PolymerBlock.super.handleMiningOnServer(tool, state, pos, player);
   }
}
