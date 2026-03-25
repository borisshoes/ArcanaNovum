package net.borisshoes.arcananovum.blocks;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannels;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateChannelGui;
import net.borisshoes.arcananovum.gui.endercrate.EnderCrateGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DefaultPlayerData;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.EnderCrate.EnderCrateBlock.HORIZONTAL_FACING;

public class EnderCrate extends ArcanaBlock {
   public static final String CHANNEL_TAG = "channel";
   public static final String LOCK_TAG = "lock";
   public static final DyeColor[] DEFAULT_CHANNEL = new DyeColor[]{null, null, null, null, null, null, null, null, null};
   
   public static final String ID = "ender_crate";
   
   public EnderCrate(){
      id = ID;
      name = "Ender Crate";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.ENDER_CHEST;
      block = new EnderCrateBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops().strength(4.0f, 1200.0f).sound(SoundType.WOOD));
      item = new EnderCrateItem(block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_EYE_OF_ENDER,ResearchTasks.USE_ENDER_CHEST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, CHANNEL_TAG, colorsToTag(DEFAULT_CHANNEL));
      putProperty(stack, LOCK_TAG, "");
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag channel = getListProperty(stack, CHANNEL_TAG);
      String lock = getStringProperty(stack, LOCK_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, CHANNEL_TAG, channel);
      putProperty(newStack, LOCK_TAG, lock);
      return buildItemLore(newStack, server);
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
            .append(Component.literal(" a placed ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Crate ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("alter").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" its ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("channel").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Use").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" on a placed ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Crate ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("copy").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(" its ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("channel").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      
      if(itemStack != null && BorisLib.SERVER != null){
         EnderCrateChannel channel = EnderCrate.getChannelOrDefault(itemStack);
         DyeColor[] colors = channel.getColors();
         MutableComponent channelComp = Component.literal("");
         if(channel.isLocked()){
            channelComp.append(Component.literal("\uD83D\uDD12 Private Channel: ").withStyle(ChatFormatting.GREEN));
            DefaultPlayerData playerData = DataAccess.getPlayer(channel.getIdLock(), BorisLib.PLAYER_DATA_KEY);
            if(playerData.getUsername().isEmpty()) playerData.tryResolve(BorisLib.SERVER);
            channelComp.append(playerData.getFaceTextComponent().copy().withStyle(ChatFormatting.WHITE));
            channelComp.append(Component.literal(" - "));
         }else{
            channelComp.append(Component.literal("\uD83D\uDD13 Public Channel: ").withStyle(ChatFormatting.LIGHT_PURPLE));
         }
         for(DyeColor color : colors){
            MutableComponent dyeComp = color == null ? MinecraftUtils.getAtlasedTexture(Blocks.GLASS) : MinecraftUtils.getAtlasedTexture(DyeItem.byColor(color));
            channelComp.append(dyeComp.withStyle(ChatFormatting.WHITE));
            channelComp.append(" ");
         }
         lore.add(Component.literal(""));
         lore.add(channelComp);
      }
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Ender Crate").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD),Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Component.literal("\nEnder Chests are endlessly useful, yet their design is antiquated, and their limits known. I can unbind them from my personal Arcane signature and get them to be self-sufficient. ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Ender Crate").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD),Component.literal("\nNow, who knows how many pocket dimensions worth of storage I can open up!\n\nI have coded each dimension to a signature channel, composed of 9 sequential color frequencies.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Ender Crate").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD),Component.literal("\nLike a normal Ender Chest, the contents are globally accessible as long as you tune the Crate to the correct channel.\n\nThe advantage of self-sufficient crates is that they can be filled and emptied by the standard").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Ender Crate").withStyle(ChatFormatting.LIGHT_PURPLE,ChatFormatting.BOLD),Component.literal("\nassortment of hoppers and copper golems.\n\nPlacing a Crate down and Sneak Using it will allow me to alter the frequency.\nOpening the Crate like usual grants me access to the channel's contents.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public static EnderCrateChannel getChannelOrDefault(ItemStack stack){
      DyeColor[] dyes = tagToColors(getListProperty(stack, CHANNEL_TAG));
      UUID lockId = AlgoUtils.getUUID(getStringProperty(stack, LOCK_TAG));
      if(lockId.toString().equals(BorisLib.BLANK_UUID)){
         return EnderCrateChannels.getChannel(dyes);
      }else{
         return EnderCrateChannels.getChannel(lockId, dyes);
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
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(new ArrayList<>(), new ArrayList<>(), stringList, new ArrayList<>()));
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
   
   public class EnderCrateBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock, SimpleWaterloggedBlock {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
      public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
      private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);
      
      public EnderCrateBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.ENDER_CHEST.defaultBlockState().setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
         }
      }
      
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext){
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
      protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext){
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
      ){
         if((Boolean) blockState.getValue(WATERLOGGED)){
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
         }
         
         return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
      }
      
      @Override
      protected FluidState getFluidState(BlockState blockState){
         return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
      }
      
      @Override
      protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType){
         return false;
      }
      
      @Override
      public void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, PacketContext.NotNullWithPlayer contexts){
         if(!PolymerResourcePackUtils.hasMainPack(contexts.getPlayer())){
            CompoundTag main = new CompoundTag();
            main.putString("id", "minecraft:ender_chest");
            main.putInt("x", pos.getX());
            main.putInt("y", pos.getY());
            main.putInt("z", pos.getZ());
            contexts.getPlayer().connection.send(PolymerBlockUtils.createBlockEntityPacket(pos.immutable(), BlockEntityType.ENDER_CHEST,main));
         }
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
         return new EnderCrateBlockEntity(blockPos, blockState);
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof EnderCrateBlockEntity crate){
            initializeArcanaBlock(stack, crate);
            EnderCrateChannel channel = getChannelOrDefault(stack);
            crate.setChannel(channel);
         }
      }
      
      @Override
      protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult){
         if(!(level.getBlockEntity(blockPos) instanceof EnderCrateBlockEntity crate))
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
         if(ArcanaItemUtils.identifyItem(itemStack) instanceof EnderCrate enderCrate){
            EnderCrateChannel channel = crate.getChannel();
            EnderCrateChannel otherChannel = EnderCrate.getChannelOrDefault(itemStack);
            UUID lock = channel.getIdLock();
            boolean canLock = ArcanaAugments.getAugmentOnItem(itemStack,ArcanaAugments.BIOMETRIC_CHANNELS) > 0;
            if(channel.equals(otherChannel) || (channel.isLocked() && !canLock)){
               return InteractionResult.TRY_WITH_EMPTY_HAND;
            }else if(lock == null || lock.equals(player.getUUID())){
               ArcanaItem.putProperty(itemStack,CHANNEL_TAG,EnderCrate.colorsToTag(channel.getColors()));
               ArcanaItem.putProperty(itemStack,LOCK_TAG,lock == null ? "" : lock.toString());
               buildItemLore(itemStack,level.getServer());
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,1,0.6f);
               return InteractionResult.SUCCESS_SERVER;
            }else{
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.IRON_TRAPDOOR_OPEN,1,1.2f);
               return InteractionResult.SUCCESS_SERVER;
            }
         }else if(ArcanaItemUtils.identifyItem(itemStack) instanceof ArcaneTome tome){
            UUID tomeId = AlgoUtils.getUUID(tome.getCrafter(itemStack));
            if(tomeId.toString().equals(BorisLib.BLANK_UUID)) return InteractionResult.TRY_WITH_EMPTY_HAND;
            EnderCrateChannel channel = crate.getChannel();
            boolean canLock = ArcanaAugments.getAugmentFromMap(crate.getAugments(),ArcanaAugments.BIOMETRIC_CHANNELS) > 0;
            UUID lock = channel.getIdLock();
            if(canLock && lock == null){
               crate.setChannel(EnderCrateChannels.getChannel(tomeId,crate.getChannel().getColors()));
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.VAULT_INSERT_ITEM_FAIL,1,1f);
               return InteractionResult.SUCCESS_SERVER;
            }else if(canLock && lock.equals(tomeId)){
               crate.setChannel(EnderCrateChannels.getChannel(crate.getChannel().getColors()));
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.VAULT_INSERT_ITEM,1,1f);
               return InteractionResult.SUCCESS_SERVER;
            }else if(canLock){
               SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.IRON_TRAPDOOR_OPEN, 1,1.2f);
               return InteractionResult.SUCCESS_SERVER;
            }
         }
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult){
         if(level.getBlockEntity(blockPos) instanceof EnderCrateBlockEntity crate){
            BlockPos blockPos2 = blockPos.above();
            if(level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)){
               return InteractionResult.SUCCESS_SERVER;
            }else{
               if(level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer){
                  EnderCrateChannel channel = crate.getChannel();
                  boolean canEditChannel = player.isShiftKeyDown() && (!channel.isLocked() || channel.getIdLock().equals(player.getUUID()));
                  if(canEditChannel){
                     EnderCrateChannelGui gui = new EnderCrateChannelGui(serverPlayer, channel, false);
                     gui.setOnConfirm(crate::setChannel);
                     gui.setWatched(crate);
                     gui.build();
                     gui.open();
                     level.gameEvent(GameEvent.CONTAINER_OPEN, blockPos, GameEvent.Context.of(blockState));
                  }else{
                     EnderCrateGui gui = new EnderCrateGui(serverPlayer, crate);
                     gui.open();
                     PiglinAi.angerNearbyPiglins(serverLevel, player, true);
                  }
                  SoundUtils.playSound(level,blockPos, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS,1,1.2f);
               }
               return InteractionResult.SUCCESS_SERVER;
            }
         }else{
            return InteractionResult.SUCCESS_SERVER;
         }
      }
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
         return new Model(world, initialBlockState);
      }
      
      @Override
      public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return true;
      }
   }
   
   public static final class Model extends BlockModel {
      public static final ItemStack CRATE = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/ender_crate"));
      
      // Direction offsets for each horizontal face (North, South, East, West)
      private static final Direction[] FACES = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
      private static final float TEXT_OFFSET = 0.455f; // Distance from center to face
      private static final float TEXT_SCALE_VERT = 0.25f;
      private static final float TEXT_SCALE_HORZ = 0.275f;
      private static final byte TEXT_OPACITY = (byte)(255 * 1.00); // 80% opacity
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      private final TextDisplayElement[] codes;
      private EnderCrateChannel channel;
      private int ticks;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         this.main = ItemDisplayElementUtil.createSimple(CRATE);
         this.main.setYaw(state.getValue(HORIZONTAL_FACING).toYRot());
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
         
         // Initialize the 4 text displays for each horizontal face
         this.codes = new TextDisplayElement[4];
         for(int i = 0; i < 4; i++){
            codes[i] = new TextDisplayElement();
            codes[i].setScale(new Vector3f(TEXT_SCALE_HORZ,TEXT_SCALE_VERT,TEXT_SCALE_HORZ));
            codes[i].setTextOpacity(TEXT_OPACITY);
            codes[i].setLineWidth(1000); // Prevent line wrapping
            
            // Position and rotate based on face direction
            Direction face = FACES[i];
            float yaw = face.toYRot();
            Vec3 offset = new Vec3(
                  face.getStepX() * TEXT_OFFSET,
                  -0.0625, // 3 pixels lower
                  face.getStepZ() * TEXT_OFFSET
            );
            codes[i].setOffset(offset);
            codes[i].setYaw(yaw);
            
            this.addElement(codes[i]);
         }
      }
      
      private void updateCodeDisplays(){
         if(channel == null) return;
         
         DyeColor[] colors = channel.getColors();
         MutableComponent text = Component.literal("");
         
         for(DyeColor color : colors){
            if(color == null){
               text.append(Component.literal("░").withColor(0xc7fcfb));
            }else{
               text.append(Component.literal("█").withColor(color.getFireworkColor()));
            }
         }
         
         // Set background based on lock status
         int background;
         if(channel.isLocked()){
            // ARGB format: alpha in high byte, then RGB
            background = (0xCC << 24) | ArcanaColors.ARCANA_COLOR; // ~80% alpha
         }else{
            background = 0; // Fully transparent
         }
         
         for(TextDisplayElement code : codes){
            code.setText(text);
            code.setBackground(background);
         }
      }
      
      @Override
      public void tick(){
         super.tick();
         
         if(this.ticks % 20 == 0){
            BlockEntity entity = world.getBlockEntity(blockPos());
            if(entity instanceof EnderCrateBlockEntity crate){
               this.channel = crate.getChannel();
               updateCodeDisplays();
            }
         }
         
         this.ticks++;
      }
   }
}

