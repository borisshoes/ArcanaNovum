package net.borisshoes.arcananovum.blocks.astralgateway;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.astralgateway.AstralGatewayGui;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.astralgateway.AstralGateway.AstralGatewayBlock.STATE;

public class AstralGateway extends ArcanaBlock {
   public static final String ID = "astral_gateway";
   
   public static final String STARDUST_TAG = "stardust";
   public static final String WAYSTONES_TAG = "waystones";
   
   public AstralGateway(){
      id = ID;
      name = "Astral Gateway";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.END_PORTAL_FRAME;
      block = new AstralGatewayBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops().strength(4.0f, 1200.0f).sound(SoundType.HEAVY_CORE));
      item = new AstralGatewayItem(block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.WHITE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_EXOTIC_MATTER, ResearchTasks.UNLOCK_WAYSTONE, ResearchTasks.USE_ENDER_PEARL, ResearchTasks.USE_ENDER_EYE, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.OBTAIN_NETHER_STAR, ResearchTasks.OBTAIN_STARDUST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, STARDUST_TAG, 0);
      putProperty(stack, WAYSTONES_TAG, new ListTag());
      setPrefStack(stack);
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ListTag stonesList = getListProperty(stack, WAYSTONES_TAG);
      long stardust = getLongProperty(stack, STARDUST_TAG);
      ItemStack newStack = super.updateItem(stack, server);
      putProperty(newStack, WAYSTONES_TAG, stonesList);
      putProperty(newStack, STARDUST_TAG, stardust);
      return buildItemLore(newStack, server);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Portals ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("have been such ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("unreliable constructs").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Stellar-Leyline").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("navigation ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("has proven much more ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("reliable").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Gateway").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("syncs ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("to another using a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Waystone").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Gateway ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("will fill ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("any suitable frame").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(" it is placed near.").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("Maintaining ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("portal ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("requires a steady supply of ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("Stardust").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" will ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("activate ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("this ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("and the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("synced Gateway").withStyle(ChatFormatting.WHITE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack, WAYSTONES_TAG).size();
         int dust = getIntProperty(itemStack, STARDUST_TAG);
         if(size > 0 || dust > 0){
            lore.add(Component.literal(""));
         }
         if(size > 0){
            lore.add(Component.literal("")
                  .append(Component.literal("Contains ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("" + size).withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(" Waystones").withStyle(ChatFormatting.GRAY)));
         }
         if(dust > 0){
            lore.add(Component.literal("")
                  .append(Component.literal("Contains ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("" + dust).withStyle(ChatFormatting.YELLOW))
                  .append(Component.literal(" Stardust").withStyle(ChatFormatting.GOLD)));
         }
      }
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nThe primitive portals I have seen are so deceiving, they give the illusion of reliable two-way passage, when no such connection exists. They simply shunt me wherever they feel, and I'm fed up with it.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nMy advancements in Stellar-Leyline cartography and navigation can pinpoint any location in any dimension and chart a path through the leylines with frankly stunning accuracy.\nA marked Waystone should be all I need to create a two-way").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nbridge as long as there is something to stabilize the bridge on both ends.\n\nThus enters my Astral Gateway, a receptacle for a Waystone which will create a two-way portal to another Astral Gateway!").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nIt needs only three things, a Waystone to be attuned to the receiving Gateway by Using an unattuned Waystone on a placed Astral Gateway, a suitable rectangular frame made of any solid material for both Gateways, and a steady supply of ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nStardust for both Gateways to maintain the link.\n\nOnce given these three things, a simple redstone signal will begin the linking process. Even a short pulse will cause the Gateway to be locked open for long enough ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nfor me to pass through.\nAdditionally, the Gateway can store many Waystones in its storage, waiting for use, as well as the active Waystone being able to be hoppered in and out. Stardust can also be supplied via hoppers.\n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Astral Gateway").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nLastly, I can allow each Gateway to let it act as both a sender and receiver, or restrict it such that it can only receive an activation from other Gateways, or make it so it can only be the one to activate other Gateways. ").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class AstralGatewayItem extends ArcanaPolymerBlockItem {
      public AstralGatewayItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public @NonNull ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class AstralGatewayBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final EnumProperty<GatewayState> STATE = EnumProperty.create("gateway_state", GatewayState.class);
      public static final EnumProperty<GatewayMode> MODE = EnumProperty.create("gateway_mode", GatewayMode.class);
      public static final EnumProperty<Direction> HORIZONTAL_FACING = HorizontalDirectionalBlock.FACING;
      public static final BooleanProperty HAS_EYE = BlockStateProperties.EYE;
      
      public AstralGatewayBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(HAS_EYE, state.getValue(HAS_EYE)).setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
         }
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState()
               .setValue(STATE, GatewayState.CLOSED)
               .setValue(MODE, GatewayMode.BOTH)
               .setValue(HAS_EYE, false)
               .setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite());
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING, HAS_EYE, STATE, MODE);
      }
      
      @Override
      public BlockState rotate(BlockState state, Rotation rotation){
         return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, Mirror mirror){
         return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.ASTRAL_GATEWAY_BLOCK_ENTITY, AstralGatewayBlockEntity::ticker);
      }
      
      @Override
      public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new AstralGatewayBlockEntity(pos, state);
      }
      
      @Override
      protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult){
         AstralGatewayBlockEntity gateway = (AstralGatewayBlockEntity) level.getBlockEntity(blockPos);
         if(gateway != null){
            boolean validWaystone = gateway.validWaystone(itemStack);
            if(itemStack.is(ArcanaRegistry.WAYSTONE.getItem()) && !validWaystone){
               return InteractionResult.PASS;
            }
            if(validWaystone && gateway.getInventory().getItem(0).isEmpty() && blockState.getValue(MODE) != GatewayMode.RECEIVE_ONLY){
               if(ArcanaAugments.getAugmentFromMap(gateway.getAugments(), ArcanaAugments.ASTRAL_STARGATE) < 1 && !Waystone.getTarget(itemStack).world().identifier().equals(level.dimension().identifier()))
                  return InteractionResult.TRY_WITH_EMPTY_HAND;
               gateway.getInventory().setItem(0, itemStack.copy());
               gateway.setChanged();
               player.getInventory().removeItem(itemStack);
               SoundUtils.playSound(level, gateway.getBlockPos(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.5f, 2.0f);
               return InteractionResult.SUCCESS_SERVER;
            }else if(itemStack.is(ArcanaRegistry.STARDUST)){
               gateway.getInventory().setItem(1, itemStack.copy());
               gateway.setChanged();
               player.getInventory().removeItem(itemStack);
               SoundUtils.playSound(level, gateway.getBlockPos(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 0.5f, 2f);
               return InteractionResult.SUCCESS_SERVER;
            }
         }
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         AstralGatewayBlockEntity gateway = (AstralGatewayBlockEntity) world.getBlockEntity(pos);
         if(gateway != null){
            if(playerEntity instanceof ServerPlayer player){
               boolean hitTop = hit.getDirection() == Direction.UP || (hit.getLocation().y - pos.getY()) > 0.8125;
               if(!gateway.getInventory().getItem(0).isEmpty() && hitTop && player.getMainHandItem().isEmpty()){
                  ItemStack stone = gateway.getInventory().getItem(0).copy();
                  gateway.getInventory().setItem(0, ItemStack.EMPTY);
                  gateway.setChanged();
                  SoundUtils.playSound(world, gateway.getBlockPos(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.5f, 2.0f);
                  if(!player.addItem(stone)){
                     ItemEntity itemEntity = player.drop(stone, false);
                     if(itemEntity == null) return InteractionResult.SUCCESS_SERVER;
                     itemEntity.setNoPickUpDelay();
                     itemEntity.setTarget(player.getUUID());
                  }
                  return InteractionResult.SUCCESS_SERVER;
               }else if(!player.isShiftKeyDown()){
                  AstralGatewayGui gui = new AstralGatewayGui(player, gateway);
                  player.getCooldowns().addCooldown(playerEntity.getMainHandItem(), 1);
                  player.getCooldowns().addCooldown(playerEntity.getOffhandItem(), 1);
               }
               
            }
         }
         return InteractionResult.PASS;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof AstralGatewayBlockEntity gateway){
            initializeArcanaBlock(stack, gateway);
            gateway.readStardustAndStones(getIntProperty(stack, STARDUST_TAG), getListProperty(stack, WAYSTONES_TAG), world.registryAccess());
         }
      }
      
      @Override
      protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof AstralGatewayBlockEntity gateway){
            gateway.evaluateForOpenOrClose();
         }
      }
      
      @Override
      public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return new Model(world, initialBlockState);
      }
      
      @Override
      public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState){
         return true;
      }
   }
   
   public static final class Model extends BlockModel {
      public static final ItemStack GATEWAY_BASE = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway"));
      public static final ItemStack GATEWAY_BASE_EMPTY = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_empty"));
      public static final ItemStack GATEWAY_SMALL_RING = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_small_ring"));
      public static final ItemStack GATEWAY_SMALL_RING_EMPTY = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_small_ring_empty"));
      public static final ItemStack GATEWAY_BIG_RING = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_big_ring"));
      public static final ItemStack GATEWAY_BIG_RING_EMPTY = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_big_ring_empty"));
      public static final ItemStack GATEWAY_KNOBS = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_knobs"));
      public static final ItemStack GATEWAY_KNOBS_EMPTY = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/astral_gateway_knobs_empty"));
      
      // Knob animation constants
      private static final float KNOB_AMPLITUDE_NORMAL = 0.01f;
      private static final float KNOB_AMPLITUDE_STARDUST = 0.045f;
      private static final float KNOB_PERIOD_NORMAL = 100f;
      private static final float KNOB_PERIOD_STARDUST = 80f;
      private static final float KNOB_BASE_HEIGHT_NORMAL = -0.03125f; // Half pixel lower (0.5/16)
      private static final float KNOB_BASE_HEIGHT_STARDUST = 0.0625f; // One pixel higher (1/16)
      
      // Ring animation constants - no frame (low, horizontal, slow oscillation)
      private static final float RING_BIG_NO_FRAME_HEIGHT = 0.225f;
      private static final float RING_SMALL_NO_FRAME_HEIGHT = 0.30f;
      private static final float RING_NO_FRAME_AMPLITUDE = 0.01f;
      private static final float RING_BIG_NO_FRAME_PERIOD = 80f;
      private static final float RING_SMALL_NO_FRAME_PERIOD = 60f;
      
      // Ring animation constants - has frame, closed (horizontal, higher hover)
      private static final float RING_BIG_CLOSED_HEIGHT = 0.35f;
      private static final float RING_SMALL_CLOSED_HEIGHT = 0.475f;
      private static final float RING_CLOSED_AMPLITUDE = 0.025f;
      private static final float RING_CLOSED_PERIOD = 50f;
      
      // Ring animation constants - active/not closed (upright, spinning)
      private static final float RING_ACTIVE_HEIGHT = 0.6875f;
      private static final float RING_ACTIVE_OSCILLATION = 0.015f;
      private static final float RING_ACTIVE_OSCILLATION_PERIOD = 80f;
      private static final float RING_BIG_SPIN_SPEED = 5f * Mth.DEG_TO_RAD;
      private static final float RING_SMALL_SPIN_SPEED = 3f * Mth.DEG_TO_RAD;
      
      // Interpolation speed for smooth transitions
      private static final float LERP_SPEED = 0.05f;
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      private final ItemDisplayElement bigRing;
      private final ItemDisplayElement smallRing;
      private final ItemDisplayElement knobs;
      private GatewayState state;
      private boolean hasStardust;
      private boolean hasFrame;
      private int ticks;
      
      // Ring animation state
      private float bigRingYaw = 0f;
      private float smallRingYaw = 0f;
      private float currentBigRingPitch = 0f; // 0 = horizontal, PI/2 = upright
      private float currentSmallRingPitch = 0f;
      private float targetBigRingPitch = 0f;
      private float targetSmallRingPitch = 0f;
      private float currentBigRingBaseHeight = RING_BIG_NO_FRAME_HEIGHT;
      private float currentSmallRingBaseHeight = RING_SMALL_NO_FRAME_HEIGHT;
      private float targetBigRingBaseHeight = RING_BIG_NO_FRAME_HEIGHT;
      private float targetSmallRingBaseHeight = RING_SMALL_NO_FRAME_HEIGHT;
      private float gimbalAmount = 0f; // 0 = closed (horizontal), 1 = active (gimbal behavior)
      private float targetGimbalAmount = 0f;
      private boolean wasActive = false;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         BlockEntity entity = world.getBlockEntity(blockPos());
         if(entity instanceof AstralGatewayBlockEntity gateway){
            this.hasStardust = gateway.getStardust() >= gateway.getOpeningStardust();
            this.hasFrame = gateway.getFrame() != null && gateway.getFrame().finishedAndValid();
         }
         this.state = state.getValue(STATE);
         
         this.main = ItemDisplayElementUtil.createSimple(this.hasStardust ? GATEWAY_BASE : GATEWAY_BASE_EMPTY);
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
         
         this.bigRing = ItemDisplayElementUtil.createSimple(this.hasStardust ? GATEWAY_BIG_RING : GATEWAY_BIG_RING_EMPTY);
         this.bigRing.setInterpolationDuration(3);
         this.addElement(this.bigRing);
         
         this.smallRing = ItemDisplayElementUtil.createSimple(this.hasStardust ? GATEWAY_SMALL_RING : GATEWAY_SMALL_RING_EMPTY);
         this.smallRing.setInterpolationDuration(3);
         this.addElement(this.smallRing);
         
         this.knobs = ItemDisplayElementUtil.createSimple(this.hasStardust ? GATEWAY_KNOBS : GATEWAY_KNOBS_EMPTY);
         this.knobs.setInterpolationDuration(3);
         this.addElement(this.knobs);
         
         // Initialize ring state based on current conditions
         updateTargetRingState();
         // Snap to initial state
         currentBigRingPitch = targetBigRingPitch;
         currentSmallRingPitch = targetSmallRingPitch;
         currentBigRingBaseHeight = targetBigRingBaseHeight;
         currentSmallRingBaseHeight = targetSmallRingBaseHeight;
      }
      
      private void updateTargetRingState(){
         boolean isActive = this.state != GatewayState.CLOSED;
         
         if(isActive){
            // Active - upright and spinning
            targetBigRingPitch = Mth.HALF_PI;
            targetSmallRingPitch = Mth.HALF_PI;
            targetBigRingBaseHeight = RING_ACTIVE_HEIGHT;
            targetSmallRingBaseHeight = RING_ACTIVE_HEIGHT;
            targetGimbalAmount = 1f;
            wasActive = true;
         }else{
            // Mark transition from active to closed
            if(wasActive){
               wasActive = false;
            }
            
            targetGimbalAmount = 0f;
            
            if(hasFrame){
               // Closed with frame - horizontal, higher hover, with space between rings
               targetBigRingPitch = 0f;
               targetSmallRingPitch = 0f;
               targetBigRingBaseHeight = RING_BIG_CLOSED_HEIGHT;
               targetSmallRingBaseHeight = RING_SMALL_CLOSED_HEIGHT;
            }else{
               // No frame - horizontal, low, slow oscillation, with space between rings
               targetBigRingPitch = 0f;
               targetSmallRingPitch = 0f;
               targetBigRingBaseHeight = RING_BIG_NO_FRAME_HEIGHT;
               targetSmallRingBaseHeight = RING_SMALL_NO_FRAME_HEIGHT;
            }
         }
      }
      
      private void updateKnobsTransformation(){
         float amplitude = hasStardust ? KNOB_AMPLITUDE_STARDUST : KNOB_AMPLITUDE_NORMAL;
         float period = hasStardust ? KNOB_PERIOD_STARDUST : KNOB_PERIOD_NORMAL;
         float baseHeight = hasStardust ? KNOB_BASE_HEIGHT_STARDUST : KNOB_BASE_HEIGHT_NORMAL;
         float oscillation = amplitude * Mth.sin(Mth.TWO_PI * ticks / period);
         
         Matrix4f matrix = new Matrix4f();
         matrix.translate(0, baseHeight + oscillation, 0);
         knobs.setTransformation(matrix);
         knobs.startInterpolation();
      }
      
      private void updateRingsTransformation(){
         // Interpolate towards target values
         if(Math.abs(currentBigRingPitch - targetBigRingPitch) > 0.001f){
            currentBigRingPitch = Mth.lerp(LERP_SPEED, currentBigRingPitch, targetBigRingPitch);
         }else{
            currentBigRingPitch = targetBigRingPitch;
         }
         
         if(Math.abs(currentSmallRingPitch - targetSmallRingPitch) > 0.001f){
            currentSmallRingPitch = Mth.lerp(LERP_SPEED, currentSmallRingPitch, targetSmallRingPitch);
         }else{
            currentSmallRingPitch = targetSmallRingPitch;
         }
         
         if(Math.abs(currentBigRingBaseHeight - targetBigRingBaseHeight) > 0.001f){
            currentBigRingBaseHeight = Mth.lerp(LERP_SPEED, currentBigRingBaseHeight, targetBigRingBaseHeight);
         }else{
            currentBigRingBaseHeight = targetBigRingBaseHeight;
         }
         
         if(Math.abs(currentSmallRingBaseHeight - targetSmallRingBaseHeight) > 0.001f){
            currentSmallRingBaseHeight = Mth.lerp(LERP_SPEED, currentSmallRingBaseHeight, targetSmallRingBaseHeight);
         }else{
            currentSmallRingBaseHeight = targetSmallRingBaseHeight;
         }
         
         // Interpolate gimbal amount for smooth transition between closed and active rotation behaviors
         if(Math.abs(gimbalAmount - targetGimbalAmount) > 0.001f){
            gimbalAmount = Mth.lerp(LERP_SPEED, gimbalAmount, targetGimbalAmount);
         }else{
            gimbalAmount = targetGimbalAmount;
         }
         
         // Calculate oscillation and spin based on state
         float bigRingOscillation;
         float smallRingOscillation;
         
         // Always update spin when gimbal is partially or fully active
         if(gimbalAmount > 0.01f){
            bigRingYaw += RING_BIG_SPIN_SPEED * gimbalAmount;
            smallRingYaw += RING_SMALL_SPIN_SPEED * gimbalAmount;
            if(bigRingYaw > Mth.TWO_PI) bigRingYaw -= Mth.TWO_PI;
            if(smallRingYaw > Mth.TWO_PI) smallRingYaw -= Mth.TWO_PI;
         }
         
         // Interpolate yaw back to 0 when gimbal is decreasing
         if(gimbalAmount < 0.99f){
            if(Math.abs(bigRingYaw) > 0.01f){
               while(bigRingYaw > Mth.PI) bigRingYaw -= Mth.TWO_PI;
               while(bigRingYaw < -Mth.PI) bigRingYaw += Mth.TWO_PI;
               bigRingYaw = Mth.lerp(LERP_SPEED * (1f - gimbalAmount), bigRingYaw, 0f);
            }else if(gimbalAmount < 0.01f){
               bigRingYaw = 0f;
            }
            
            if(Math.abs(smallRingYaw) > 0.01f){
               while(smallRingYaw > Mth.PI) smallRingYaw -= Mth.TWO_PI;
               while(smallRingYaw < -Mth.PI) smallRingYaw += Mth.TWO_PI;
               smallRingYaw = Mth.lerp(LERP_SPEED * (1f - gimbalAmount), smallRingYaw, 0f);
            }else if(gimbalAmount < 0.01f){
               smallRingYaw = 0f;
            }
         }
         
         // Calculate oscillation based on current state blend
         float activeOscillation = RING_ACTIVE_OSCILLATION * Mth.sin(Mth.TWO_PI * ticks / RING_ACTIVE_OSCILLATION_PERIOD);
         float closedBigOscillation;
         float closedSmallOscillation;
         
         if(hasFrame){
            closedBigOscillation = RING_CLOSED_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / RING_CLOSED_PERIOD);
            closedSmallOscillation = RING_CLOSED_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / RING_CLOSED_PERIOD + Mth.PI * 0.5f);
         }else{
            closedBigOscillation = RING_NO_FRAME_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / RING_BIG_NO_FRAME_PERIOD);
            closedSmallOscillation = RING_NO_FRAME_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / RING_SMALL_NO_FRAME_PERIOD + Mth.PI * 0.7f);
         }
         
         // Blend oscillations based on gimbal amount
         bigRingOscillation = Mth.lerp(gimbalAmount, closedBigOscillation, activeOscillation);
         smallRingOscillation = Mth.lerp(gimbalAmount, closedSmallOscillation, activeOscillation);
         
         // Big ring transformation
         Matrix4f bigMatrix = new Matrix4f();
         bigMatrix.translate(0, currentBigRingBaseHeight + bigRingOscillation, 0);
         bigMatrix.rotateY(bigRingYaw);
         bigMatrix.rotateX(currentBigRingPitch);
         bigRing.setTransformation(bigMatrix);
         bigRing.startInterpolation();
         
         // Small ring transformation - blend between closed and active (gimbal) behavior
         Matrix4f smallMatrix = new Matrix4f();
         smallMatrix.translate(0, currentSmallRingBaseHeight + smallRingOscillation, 0);
         
         // Blend the rotation: interpolate between closed rotation and gimbal rotation
         // Closed: rotateY(smallRingYaw), rotateX(currentSmallRingPitch)
         // Active: rotateY(bigRingYaw + PI/2), rotateZ(PI/2), rotateX(PI/2), rotateZ(smallRingYaw)
         
         // Interpolate the Y rotation between closed (smallRingYaw) and active (bigRingYaw + PI/2)
         float closedYaw = smallRingYaw;
         float activeYaw = bigRingYaw + Mth.HALF_PI;
         float blendedYaw = Mth.lerp(gimbalAmount, closedYaw, activeYaw);
         smallMatrix.rotateY(blendedYaw);
         
         // Interpolate the Z rotation (0 when closed, PI/2 when active)
         float blendedZRot = Mth.lerp(gimbalAmount, 0f, Mth.HALF_PI);
         smallMatrix.rotateZ(blendedZRot);
         
         // Interpolate the X rotation between pitch (when closed) and PI/2 (when active gimbal)
         float blendedXRot = Mth.lerp(gimbalAmount, currentSmallRingPitch, Mth.HALF_PI);
         smallMatrix.rotateX(blendedXRot);
         
         // Additional spin around local axis when active (interpolated)
         float blendedLocalSpin = Mth.lerp(gimbalAmount, 0f, smallRingYaw);
         smallMatrix.rotateZ(blendedLocalSpin);
         
         smallRing.setTransformation(smallMatrix);
         smallRing.startInterpolation();
      }
      
      
      @Override
      public void tick(){
         super.tick();
         
         BlockEntity entity = world.getBlockEntity(blockPos());
         if(entity instanceof AstralGatewayBlockEntity gateway){
            if(this.hasStardust != (gateway.getStardust() >= gateway.getOpeningStardust())){
               this.hasStardust = !this.hasStardust;
               this.main.setItem(this.hasStardust ? GATEWAY_BASE : GATEWAY_BASE_EMPTY);
               this.bigRing.setItem(this.hasStardust ? GATEWAY_BIG_RING : GATEWAY_BIG_RING_EMPTY);
               this.smallRing.setItem(this.hasStardust ? GATEWAY_SMALL_RING : GATEWAY_SMALL_RING_EMPTY);
               this.knobs.setItem(this.hasStardust ? GATEWAY_KNOBS : GATEWAY_KNOBS_EMPTY);
            }
            if(this.hasFrame != (gateway.getFrame() != null && gateway.getFrame().finishedAndValid())){
               this.hasFrame = !this.hasFrame;
               updateTargetRingState();
            }
         }
         
         updateKnobsTransformation();
         updateRingsTransformation();
         
         ticks++;
      }
      
      @Override
      public void notifyUpdate(HolderAttachment.UpdateType updateType){
         if(updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE){
            BlockState state = this.blockState();
            if(this.state != state.getValue(STATE)){
               this.state = state.getValue(STATE);
               updateTargetRingState();
            }
         }
      }
   }
}

