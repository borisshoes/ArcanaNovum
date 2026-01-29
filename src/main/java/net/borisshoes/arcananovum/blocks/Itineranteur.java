package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class Itineranteur extends ArcanaBlock {
   public static final String COLOR_TAG = "color";
   
   public static final String ID = "itineranteur";
   
   public Itineranteur(){
      id = ID;
      name = "Itineranteur";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.LANTERN;
      block = new ItineranteurBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.0f, 1200.0f).sound(SoundType.METAL));
      item = new ItineranteurItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.EFFECT_SWIFTNESS,ResearchTasks.OBTAIN_BEACON,ResearchTasks.WALK_ONE_KILOMETER,ResearchTasks.OBTAIN_LANTERN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack,COLOR_TAG,LanternType.YELLOW.getId());
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      String color = getStringProperty(stack,COLOR_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,COLOR_TAG,color);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A seemingly simple ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("lantern ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("quickens ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("your ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("feet").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Its ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("magical aura").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("enchants ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("designated path").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Travelers in its ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("light").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" find their ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("pace ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("hastened").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a placed ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("Itineranteur ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("assign ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("a ").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("path").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.BLUE)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack forgeItem(Container inv, List<Integer> centerpieces, StarlightForgeBlockEntity starlightForge){
      ItemStack newArcanaItem = getNewItem();
      if(centerpieces.isEmpty()) return newArcanaItem;
      ItemStack lanternStack = inv.getItem(centerpieces.getFirst()); // Should be the lanterns
      
      LanternType lanternType = LanternType.fromItemStack(lanternStack);
      putProperty(newArcanaItem, COLOR_TAG, lanternType.getId());
      
      return newArcanaItem;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Itineranteur").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD),Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Component.literal("\nBeacons are expensive, and walking is tiresome and slow. I've used some arcane and alchemical trickery to make a mini-beacon out of a lantern. It will put some swiftness in the step of anyone ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Itineranteur").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD),Component.literal("\nwalking along its designated path.\n\nUse a placed Itineranteur to designate its path. Selecting blocks on the ground will add or remove them from its path. Those walking on any of these blocks will feel its effects.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Itineranteur").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD),Component.literal("\nUsing the Itineranteur again will exit configuration mode.\n\nAs someone who never passes up a chance for a bit of aesthetic choice, I can craft different looking Itineranteurs out of most lantern types.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ItineranteurItem extends ArcanaPolymerBlockItem {
      public ItineranteurItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public Item getPolymerItem(ItemStack itemStack, PacketContext context){
         return LanternType.fromString(getStringProperty(itemStack, COLOR_TAG)).getItem();
      }
      
      @Override
      public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
         ItemStack baseStack = super.getPolymerItemStack(itemStack, tooltipType, context);
         if(!ArcanaItemUtils.isArcane(itemStack)) return baseStack;
         
         List<String> stringList = new ArrayList<>();
         stringList.add(getStringProperty(itemStack,COLOR_TAG));
         baseStack.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(new ArrayList<>(),new ArrayList<>(),stringList,new ArrayList<>()));
         return baseStack;
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
      
      @Override
      public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(entity instanceof ServerPlayer player)) return;
         
         
      }
      
      @Override
      public @NonNull InteractionResult use(@NonNull Level world, Player playerEntity, @NonNull InteractionHand hand){
         ItemStack stack = playerEntity.getItemInHand(hand);
         if(!(playerEntity instanceof ServerPlayer player)) return InteractionResult.PASS;
         if(player.isCreative() && player.isShiftKeyDown()){
            LanternType currentType = LanternType.fromString(getStringProperty(stack, COLOR_TAG));
            LanternType nextType = currentType.cycle();
            putProperty(stack, COLOR_TAG, nextType.getId());
            return InteractionResult.SUCCESS_SERVER;
         }
         return InteractionResult.PASS;
      }
   }
   
   public class ItineranteurBlock extends ArcanaPolymerBlockEntity implements SimpleWaterloggedBlock {
      public static final EnumProperty<LanternType> TYPE = EnumProperty.create("lantern_type",LanternType.class);
      public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
      public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
      private static final VoxelShape SHAPE_STANDING = Shapes.or(Block.column(4.0, 7.0, 9.0), Block.column(6.0, 0.0, 7.0));
      private static final VoxelShape SHAPE_HANGING = SHAPE_STANDING.move(0.0, 0.0625, 0.0).optimize();
      
      public ItineranteurBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public Block getPolymerReplacement(Block block, PacketContext context){
         return super.getPolymerReplacement(block, context);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         LanternType type = state.getValue(TYPE);
         return type.getBlock().defaultBlockState().setValue(HANGING, state.getValue(HANGING)).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.ITINERANTEUR_BLOCK_ENTITY, ItineranteurBlockEntity::ticker);
      }
      
      @org.jspecify.annotations.Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
         FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
         
         for (Direction direction : blockPlaceContext.getNearestLookingDirections()) {
            if (direction.getAxis() == Direction.Axis.Y) {
               BlockState blockState = this.defaultBlockState().setValue(HANGING, direction == Direction.UP);
               if (blockState.canSurvive(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos())) {
                  blockState = blockState.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
                  blockState = blockState.setValue(TYPE, LanternType.fromString(getStringProperty(blockPlaceContext.getItemInHand(),COLOR_TAG)));
                  return blockState;
               }
            }
         }
         
         return null;
      }
      
      @Override
      protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
         return blockState.getValue(HANGING) ? SHAPE_HANGING : SHAPE_STANDING;
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
         builder.add(HANGING, WATERLOGGED, TYPE);
      }
      
      @Override
      protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
         Direction direction = getConnectedDirection(blockState).getOpposite();
         return Block.canSupportCenter(levelReader, blockPos.relative(direction), direction.getOpposite());
      }
      
      protected static Direction getConnectedDirection(BlockState blockState) {
         return blockState.getValue(HANGING) ? Direction.DOWN : Direction.UP;
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
         
         return getConnectedDirection(blockState).getOpposite() == direction && !blockState.canSurvive(levelReader, blockPos)
               ? Blocks.AIR.defaultBlockState()
               : super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
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
      public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
         return new ItineranteurBlockEntity(blockPos, blockState);
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult){
         if(!(level.getBlockEntity(blockPos) instanceof ItineranteurBlockEntity itineranteur)) return InteractionResult.PASS;
         if(itineranteur.getEditor() == null && player instanceof ServerPlayer serverPlayer){
            SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.LANTERN_PLACE,1,0.75f + level.random.nextFloat()*0.5f);
            itineranteur.setEditor(serverPlayer);
            return InteractionResult.SUCCESS_SERVER;
         }else if(itineranteur.getEditor().equals(player)){
            SoundUtils.playSongToPlayer((ServerPlayer) player, SoundEvents.LANTERN_PLACE,1,0.75f + level.random.nextFloat()*0.5f);
            itineranteur.setEditor(null);
            return InteractionResult.SUCCESS_SERVER;
         }else{
            player.displayClientMessage(Component.literal("Someone else is editing the Itineranteur").withStyle(ChatFormatting.RED),true);
         }
         return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
      }
      
   }
   
   public enum LanternType implements StringRepresentable {
      YELLOW("yellow", Items.LANTERN, Blocks.LANTERN),
      BLUE("blue", Items.SOUL_LANTERN, Blocks.SOUL_LANTERN),
      COPPER("copper", Items.COPPER_LANTERN.waxed(), ((BlockItem)Items.COPPER_LANTERN.waxed()).getBlock()),
      GREEN("green", Items.COPPER_LANTERN.waxedOxidized(), ((BlockItem)Items.COPPER_LANTERN.waxedOxidized()).getBlock());
      
      private final String id;
      private final Item item;
      private final Block block;
      
      LanternType(String id, Item item, Block block){
         this.id = id;
         this.item = item;
         this.block = block;
      }
      
      public String getId(){
         return id;
      }
      
      public Item getItem(){
         return item;
      }
      
      public Block getBlock(){
         return block;
      }
      
      public BlockState getBlockState(){
         return block.defaultBlockState();
      }
      
      public LanternType cycle(){
         LanternType[] values = values();
         return values[(this.ordinal() + 1) % values.length];
      }
      
      public static LanternType fromString(String id){
         for(LanternType type : values()){
            if(type.id.equals(id)) return type;
         }
         return YELLOW;
      }
      
      public static LanternType fromItemStack(ItemStack stack){
         if(stack.is(Items.SOUL_LANTERN)) return BLUE;
         if(stack.is(Items.COPPER_LANTERN.oxidized()) || stack.is(Items.COPPER_LANTERN.weathered()) ||
               stack.is(Items.COPPER_LANTERN.waxedOxidized()) || stack.is(Items.COPPER_LANTERN.waxedWeathered())) return GREEN;
         if(stack.is(Items.COPPER_LANTERN.exposed()) || stack.is(Items.COPPER_LANTERN.unaffected()) ||
               stack.is(Items.COPPER_LANTERN.waxedExposed()) || stack.is(Items.COPPER_LANTERN.waxed())) return COPPER;
         return YELLOW;
      }
      
      @Override
      public @NonNull String getSerializedName(){
         return getId();
      }
   }
}


