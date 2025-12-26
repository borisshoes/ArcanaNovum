package net.borisshoes.arcananovum.core.polymer;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.TreeMap;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerBlockEntity extends BaseEntityBlock implements PolymerBlock {
   protected final ArcanaItem arcanaItem;
   protected ArcanaPolymerBlockEntity(ArcanaItem arcanaItem, Properties settings){
      super(settings.setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   public static void initializeArcanaBlock(ItemStack stack, ArcanaBlockEntity arcanaBlock){
      ArcanaItem arcanaItem = arcanaBlock.getArcanaItem();
      
      TreeMap<ArcanaAugment,Integer> augments = ArcanaAugments.getAugmentsOnItem(stack);
      String crafterId = arcanaItem.getCrafter(stack);
      String uuid = ArcanaItem.getUUID(stack);
      int origin = arcanaItem.getOrigin(stack);
      String customName = null;
      if(stack.has(DataComponents.CUSTOM_NAME)){
         customName = stack.get(DataComponents.CUSTOM_NAME).getString();
      }
      
      if(uuid == null || uuid.isEmpty()){
         uuid = UUID.randomUUID().toString();
      }
      arcanaBlock.initialize(augments,crafterId,uuid,origin,customName);
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
   
   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
      return null;
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
      return super.getTicker(world, state, type);
   }
   
   @Nullable
   @Override
   public <T extends BlockEntity> GameEventListener getListener(ServerLevel world, T blockEntity){
      return super.getListener(world, blockEntity);
   }
   
   @Override
   protected MapCodec<? extends BaseEntityBlock> codec(){
      return null;
   }
}
