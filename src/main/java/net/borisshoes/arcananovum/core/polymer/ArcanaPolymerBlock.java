package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerBlock extends Block implements PolymerBlock {
   protected final ArcanaItem arcanaItem;
   public ArcanaPolymerBlock(ArcanaItem arcanaItem, Settings settings){
      super(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public abstract BlockState getPolymerBlockState(BlockState state, PacketContext context);
   
   @Override
   public void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, PacketContext.NotNullWithPlayer contexts){
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
   public boolean isPolymerBlockInteraction(BlockState state, ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, BlockHitResult blockHitResult, ActionResult actionResult){
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
   public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player){
      return PolymerBlock.super.handleMiningOnServer(tool, state, pos, player);
   }
}
