package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.blocks.astralgateway.GatewayMode;
import net.borisshoes.arcananovum.blocks.astralgateway.GatewayState;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannels;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateGui;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class EnderCrate extends ArcanaBlock {
   public static final String CHANNEL_TAG = "channel";
   public static final String LOCK_TAG = "lock";
   public static final DyeColor[] DEFAULT_CHANNEL = new DyeColor[]{null,null,null,null,null,null,null,null,null};
   
   public static final String ID = "ender_crate";
   
   public EnderCrate(){
      id = ID;
      name = "Ender Crate";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.ENDER_CHEST;
      block = new EnderCrateBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(4.0f, 1200.0f).sound(SoundType.WOOD));
      item = new EnderCrateItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{};  // TODO
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,CHANNEL_TAG,colorsToTag(DEFAULT_CHANNEL));
      putProperty(stack,LOCK_TAG,"");
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("An ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Ender Chest").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" that has been adapted to need ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("no player").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Crate ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("links ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("to any other ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Crates ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("on its ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("color channel").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Items can be ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("transferred ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("in and out via ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("hoppers").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Sneak Use").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Crate ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("in your ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("offhand ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("alter").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" its ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("channel").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("TODO").withStyle(ChatFormatting.BLACK))); // TODO
      return list;
   }
   
   public static EnderCrateChannel getChannelOrDefault(ItemStack stack){
      if(!stack.is(ArcanaRegistry.ENDER_CRATE.getItem())) return EnderCrateChannels.getChannel(DEFAULT_CHANNEL);
      DyeColor[] dyes = tagToColors(getListProperty(stack,CHANNEL_TAG));
      UUID lockId = AlgoUtils.getUUID(getStringProperty(stack,LOCK_TAG));
      if(lockId.toString().equals(BorisLib.BLANK_UUID)){
         return EnderCrateChannels.getChannel(dyes);
      }else{
         return EnderCrateChannels.getChannel(lockId,dyes);
      }
   }
   
   public static ListTag colorsToTag(DyeColor... colors){
      ListTag list = new ListTag();
      for(int i = 0; i < 9; i++){
         DyeColor color = (i < colors.length) ? colors[i] : null;
         CompoundTag entry = new CompoundTag();
         entry.putInt("slot", i);
         entry.putString("color", color == null ? "" : color.getName());
         list.add(entry);
      }
      return list;
   }
   
   public static DyeColor[] tagToColors(ListTag list){
      DyeColor[] colors = new DyeColor[9];
      Arrays.fill(colors, null);
      for(int i = 0; i < list.size(); i++){
         CompoundTag entry = list.getCompoundOrEmpty(i);
         int slot = entry.getIntOr("slot", -1);
         String colorName = entry.getStringOr("color", "");
         if(slot >= 0 && slot < 9 && !colorName.isEmpty()){
            colors[slot] = DyeColor.byName(colorName, null);
         }
      }
      return colors;
   }
   
   public class EnderCrateItem extends ArcanaPolymerBlockItem {
      public EnderCrateItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         
         return InteractionResult.PASS;
      }
   }
   
   public class EnderCrateBlock extends ArcanaPolymerBlockEntity implements SimpleWaterloggedBlock{
      public static final EnumProperty<Direction> HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
      public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
      private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);
      
      public EnderCrateBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.ENDER_CHEST.defaultBlockState().setValue(WATERLOGGED,state.getValue(WATERLOGGED)).setValue(HORIZONTAL_FACING,state.getValue(HORIZONTAL_FACING));
      }
      
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
         FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
         return this.defaultBlockState()
               .setValue(HORIZONTAL_FACING, blockPlaceContext.getHorizontalDirection().getOpposite())
               .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING, WATERLOGGED);
      }
      
      @Override
      public BlockState rotate(BlockState state, Rotation rotation){
         return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, Mirror mirror){
         return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
         return SHAPE;
      }
      
      @Override
      protected BlockState updateShape(
            BlockState blockState,
            LevelReader levelReader,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos blockPos,
            Direction direction,
            BlockPos blockPos2,
            BlockState blockState2,
            RandomSource randomSource
      ) {
         if ((Boolean)blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
         }
         
         return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
      }
      
      @Override
      protected FluidState getFluidState(BlockState blockState) {
         return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
      }
      
      @Override
      protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
         return false;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
         return new EnderCrateBlockEntity(blockPos, blockState);
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof EnderCrateBlockEntity crate){
            initializeArcanaBlock(stack,crate);
            EnderCrateChannel channel = getChannelOrDefault(stack);
            crate.setChannel(channel);
         }
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
         if (level.getBlockEntity(blockPos) instanceof EnderCrateBlockEntity crate) {
            BlockPos blockPos2 = blockPos.above();
            if (level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
               return InteractionResult.SUCCESS;
            } else {
               if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                  EnderCrateChannel channel = crate.getChannel();
                  boolean canEditChannel = player.isShiftKeyDown() && (!channel.isLocked() || channel.getIdLock().equals(player.getUUID()));
                  if(canEditChannel){
                     // Open channel
                     
                  }else{
                     EnderCrateGui gui = new EnderCrateGui(serverPlayer, crate);
                     gui.open();
                     PiglinAi.angerNearbyPiglins(serverLevel, player, true);
                  }
               }
               return InteractionResult.SUCCESS;
            }
         } else {
            return InteractionResult.SUCCESS;
         }
      }
   }
}

