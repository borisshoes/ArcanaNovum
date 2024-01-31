package net.borisshoes.arcananovum.core.polymer;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public abstract class MagicPolymerBlockEntity extends BlockWithEntity implements PolymerBlock {
   protected MagicPolymerBlockEntity(Settings settings){
      super(settings);
   }
   
   
   public static void initializeMagicBlock(ItemStack stack, MagicBlockEntity magicBlock){
      NbtCompound itemNbt = stack.getNbt();
      MagicItem magicItem = magicBlock.getMagicItem();
      
      TreeMap<ArcanaAugment,Integer> augments = ArcanaAugments.getAugmentsOnItem(stack);
      String crafterId = magicItem.getCrafter(stack);
      String uuid = magicItem.getUUID(stack);
      boolean synthetic = magicItem.isSynthetic(stack);
      String customName = null;
      if(itemNbt != null && itemNbt.contains("display") && itemNbt.getCompound("display").contains("Name")){
         customName = itemNbt.getCompound("display").getString("Name");
      }
      
      magicBlock.initialize(augments,crafterId,uuid,synthetic,customName);
   }
   
   protected ItemStack getDroppedBlockItem(BlockState state, LootContextParameterSet.Builder builder){
      ServerWorld world = builder.getWorld();
      BlockPos pos = BlockPos.ofFloored(builder.get(LootContextParameters.ORIGIN));
      Entity entity = builder.getOptional(LootContextParameters.THIS_ENTITY);
      BlockEntity be = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
      return getDroppedBlockItem(state,world,entity,be);
   }
   
   protected ItemStack getDroppedBlockItem(BlockState state, World world, Entity entity, BlockEntity be){
      boolean harvest = true;
      if(entity instanceof PlayerEntity player){
         harvest = player.canHarvest(state) || player.isCreative();
      }

      if(!(be instanceof MagicBlockEntity magicBlock) || !harvest){
         return ItemStack.EMPTY;
      }
      
      String uuid = magicBlock.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      NbtCompound augmentsTag = new NbtCompound();
      if(magicBlock.getAugments() != null){
         for(Map.Entry<ArcanaAugment, Integer> entry : magicBlock.getAugments().entrySet()){
            augmentsTag.putInt(entry.getKey().id, entry.getValue());
         }
      }else{
         augmentsTag = null;
      }
      
      MagicItem magicItem = magicBlock.getMagicItem();
      ItemStack newItem = new ItemStack(magicItem.getItem());
      newItem.setNbt(magicItem.getNewItem().getNbt());
      ItemStack drop = magicItem.addCrafter(newItem, magicBlock.getCrafterId(), magicBlock.isSynthetic(),world.getServer());
      NbtCompound dropNbt = drop.getNbt();
      NbtCompound magicTag = dropNbt.getCompound("arcananovum");
      if(augmentsTag != null) {
         magicTag.put("augments",augmentsTag);
         magicItem.buildItemLore(drop,world.getServer());
      }
      magicTag.putString("UUID",uuid);
      
      if(magicBlock.getCustomArcanaName() != null && !magicBlock.getCustomArcanaName().isEmpty()){
         dropNbt.getCompound("display").putString("Name",magicBlock.getCustomArcanaName());
      }
      
      if(magicBlock instanceof ArcaneSingularityBlockEntity singularity){
         magicTag.put("books",singularity.writeBooks());
      }
      if(magicBlock instanceof StarpathAltarBlockEntity altar){
         magicTag.put("targets",altar.writeTargets());
      }
      
      return drop;
   }
   
   @Override
   public abstract Block getPolymerBlock(BlockState state);
   
   @Override
   public Block getPolymerBlock(BlockState state, ServerPlayerEntity player){
      return PolymerBlock.super.getPolymerBlock(state, player);
   }
   
   @Override
   public BlockState getPolymerBlockState(BlockState state){
      return PolymerBlock.super.getPolymerBlockState(state);
   }
   
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
