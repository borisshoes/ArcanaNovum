package net.borisshoes.arcananovum.core.polymer;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.UUID;

public abstract class ArcanaPolymerBlockEntity extends BlockWithEntity implements PolymerBlock {
   protected ArcanaPolymerBlockEntity(Settings settings){
      super(settings);
   }
   
   
   public static void initializeArcanaBlock(ItemStack stack, ArcanaBlockEntity arcanaBlock){
      ArcanaItem arcanaItem = arcanaBlock.getArcanaItem();
      
      TreeMap<ArcanaAugment,Integer> augments = ArcanaAugments.getAugmentsOnItem(stack);
      String crafterId = arcanaItem.getCrafter(stack);
      String uuid = arcanaItem.getUUID(stack);
      boolean synthetic = arcanaItem.isSynthetic(stack);
      String customName = null;
      if(stack.contains(DataComponentTypes.CUSTOM_NAME)){
         customName = stack.get(DataComponentTypes.CUSTOM_NAME).getString();
      }
      
      if(uuid == null || uuid.isEmpty()){
         uuid = UUID.randomUUID().toString();
      }
      arcanaBlock.initialize(augments,crafterId,uuid,synthetic,customName);
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
   public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player){
      return PolymerBlock.super.canSynchronizeToPolymerClient(player);
   }
   
   @Override
   public boolean canSyncRawToClient(ServerPlayerEntity player){
      return PolymerBlock.super.canSyncRawToClient(player);
   }
   
   @Override
   public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player){
      return PolymerBlock.super.handleMiningOnServer(tool, state, pos, player);
   }
   
   @Nullable
   @Override
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
      return null;
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
      return super.getTicker(world, state, type);
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> GameEventListener getGameEventListener(ServerWorld world, T blockEntity){
      return super.getGameEventListener(world, blockEntity);
   }
   
   @Override
   public BlockState getAppearance(BlockState state, BlockRenderView renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos){
      return super.getAppearance(state, renderView, pos, side, sourceState, sourcePos);
   }
   
   @Override
   protected MapCodec<? extends BlockWithEntity> getCodec(){
      return null;
   }
}
