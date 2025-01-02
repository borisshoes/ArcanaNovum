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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.TreeMap;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerBlockEntity extends BlockWithEntity implements PolymerBlock {
   protected final ArcanaItem arcanaItem;
   protected ArcanaPolymerBlockEntity(ArcanaItem arcanaItem, Settings settings){
      super(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
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
   public Block getPolymerReplacement(PacketContext context){
      return PolymerBlock.super.getPolymerReplacement(context);
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
   protected MapCodec<? extends BlockWithEntity> getCodec(){
      return null;
   }
}
