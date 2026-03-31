package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import xyz.nucleoid.packettweaker.PacketContext;

public abstract class ArcanaPolymerBlock extends Block implements PolymerBlock {
   protected final ArcanaItem arcanaItem;
   
   public ArcanaPolymerBlock(ArcanaItem arcanaItem, Properties settings){
      super(settings.setId(ResourceKey.create(Registries.BLOCK, ArcanaRegistry.arcanaId(arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public abstract BlockState getPolymerBlockState(BlockState state, PacketContext context);
   
   @Override
   public void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, PacketContext.NotNullWithPlayer contexts){
      PolymerBlock.super.onPolymerBlockSend(blockState, pos, contexts);
   }
   
   @Override
   public boolean forceLightUpdates(BlockState blockState){
      return PolymerBlock.super.forceLightUpdates(blockState);
   }
   
   @Override
   public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context){
      return PolymerBlock.super.getPolymerBreakEventBlockState(state, context);
   }
   
   @Override
   public Block getPolymerReplacement(Block block, PacketContext context){
      return PolymerBlock.super.getPolymerReplacement(block, context);
   }
   
   @Override
   public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult){
      return PolymerBlock.super.isPolymerBlockInteraction(state, player, hand, stack, world, blockHitResult, actionResult);
   }
   
   @Override
   public boolean canSynchronizeToPolymerClient(PacketContext context){
      return PolymerBlock.super.canSynchronizeToPolymerClient(context);
   }
   
   @Override
   public boolean canSyncRawToClient(PacketContext context){
      return PolymerBlock.super.canSyncRawToClient(context);
   }
   
   @Override
   public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayer player){
      return PolymerBlock.super.handleMiningOnServer(tool, state, pos, player);
   }
}
