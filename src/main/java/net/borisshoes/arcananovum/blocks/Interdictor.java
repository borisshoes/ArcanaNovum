package net.borisshoes.arcananovum.blocks;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.PackAwareBlockModel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.gui.interdictor.InterdictorGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.Interdictor.InterdictorBlock.ACTIVE;

public class Interdictor extends ArcanaBlock implements MultiblockCore {
   public static final String ID = "interdictor";
   
   private Multiblock multiblock;
   
   public Interdictor(){
      id = ID;
      name = "Interdictor";
      rarity = ArcanaRarity.EXOTIC;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 0;
      vanillaItem = Items.BEACON;
      block = new InterdictorBlock(BlockBehaviour.Properties.of().noOcclusion().requiresCorrectToolForDrops().strength(6.0f, 1200.0f).lightLevel(InterdictorBlock::getLightLevel).sound(SoundType.VAULT));
      item = new InterdictorItem(block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_EXOTIC_MATTER, ResearchTasks.OBTAIN_BEACON, ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN, ResearchTasks.OBTAIN_END_CRYSTAL, ResearchTasks.USE_ENDER_EYE, ResearchTasks.ADVANCEMENT_KILL_A_MOB};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("beacon ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("that has been modified with ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("dimensional energy").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("It ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("blocks ").withStyle(ChatFormatting.RED))
            .append(Component.literal("the coalescence of new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob essence").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("is comprised of a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("3x3x3").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("multiblock").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("When active, it ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("stops ").withStyle(ChatFormatting.RED))
            .append(Component.literal("new ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("mob spawns").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" in a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("large area").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("redstone ").withStyle(ChatFormatting.RED))
            .append(Component.literal("signal to the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Interdictor ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("activates ").withStyle(ChatFormatting.RED))
            .append(Component.literal("it.").withStyle(ChatFormatting.YELLOW)));
      
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nCreepers have blown up my house for the last time! Sometimes, no amount of torches is enough! Taking the area effects of a Beacon, and adding in a bit of dimensional energy using some techniques I picked").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nup from the End-dwellers, I have created a contraption that broadcasts a jamming signal that prevents new mob essence from coalescing.\n\nLike a normal Beacon, the Interdictor requires some").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nstructural support, though a bit more involved than a Beacon, just something to broadcast the specialized field.\n\nWhen assembled and given a redstone signal through a lever or torch on the").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("     Interdictor").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), Component.literal("\nstructure's diamond block, the Interdictor will stop hostile mobs from spawning in a 32 block radial cube.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-1, -1, -1);
   }
   
   public class InterdictorItem extends ArcanaPolymerBlockItem {
      public InterdictorItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
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
         
         return InteractionResult.PASS;
      }
   }
   
   public class InterdictorBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public InterdictorBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.BEACON.defaultBlockState();
         }
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @org.jspecify.annotations.Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext){
         return this.defaultBlockState().setValue(ACTIVE, false);
      }
      
      @Override
      public boolean forceLightUpdates(BlockState blockState){
         return true;
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.INTERDICTOR_BLOCK_ENTITY, InterdictorBlockEntity::ticker);
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
         return new InterdictorBlockEntity(blockPos, blockState);
      }
      
      @Override
      protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @org.jspecify.annotations.Nullable Orientation orientation, boolean bl){
         if(!level.isClientSide()){
            boolean currentlyActive = blockState.getValue(ACTIVE);
            if(currentlyActive != level.hasNeighborSignal(blockPos)){
               if(currentlyActive){
                  level.scheduleTick(blockPos, this, 4);
               }else if(level.getBlockEntity(blockPos) instanceof InterdictorBlockEntity interdictor && interdictor.isAssembled()){
                  level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(blockState));
                  level.setBlock(blockPos, blockState.setValue(ACTIVE, true), Block.UPDATE_ALL);
               }
            }
         }
      }
      
      @Override
      protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource){
         if(blockState.getValue(ACTIVE) && !serverLevel.hasNeighborSignal(blockPos)){
            serverLevel.setBlock(blockPos, blockState.cycle(ACTIVE), 2);
            serverLevel.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockPos, GameEvent.Context.of(blockState));
         }
      }
      
      @Override
      protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult){
         if(level.getBlockEntity(blockPos) instanceof InterdictorBlockEntity interdictor){
            if(player instanceof ServerPlayer serverPlayer && !player.isShiftKeyDown()){
               if(interdictor.isAssembled()){
                  InterdictorGui gui = new InterdictorGui(serverPlayer, interdictor);
                  gui.open();
               }else{
                  if(player.isShiftKeyDown() && player.isCreative()){
                     multiblock.build(interdictor.getMultiblockCheck());
                  }else{
                     serverPlayer.sendSystemMessage(Component.literal("Multiblock not constructed."));
                     multiblock.displayStructure(interdictor.getMultiblockCheck(), serverPlayer);
                  }
               }
            }
            return InteractionResult.PASS;
         }else{
            return InteractionResult.PASS;
         }
      }
      
      public static int getLightLevel(BlockState state){
         return 15;
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
   
   public static final class Model extends PackAwareBlockModel {
      public static final ItemStack INTERDICTOR_BASE = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/interdictor"));
      public static final ItemStack INTERDICTOR_TOP = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/interdictor_top_shell"));
      public static final ItemStack INTERDICTOR_BOT = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/interdictor_bottom_shell"));
      public static final ItemStack INTERDICTOR_CORE = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/interdictor_core"));
      
      // Base rotation speeds (radians per tick)
      private static final float SHELL_BASE_SPEED = 0.5f * Mth.DEG_TO_RAD;
      private static final float CORE_BASE_SPEED = 0.8f * Mth.DEG_TO_RAD;
      
      // Active rotation speed multipliers
      private static final float ACTIVE_SPEED_MULTIPLIER = 100.0f;
      
      // Oscillation constants
      private static final float SHELL_OSCILLATION_AMPLITUDE = 0.03f;
      private static final float CORE_OSCILLATION_AMPLITUDE = 0.02f;
      private static final float TOP_SHELL_OSCILLATION_PERIOD = 60f;
      private static final float BOT_SHELL_OSCILLATION_PERIOD = 50f;
      private static final float CORE_OSCILLATION_PERIOD = 40f;
      
      // Surge constants (for active state)
      private static final float SURGE_SPEED_BOOST = 2.0f * Mth.DEG_TO_RAD;
      private static final float SURGE_DECAY = 0.98f; // How quickly surge decays
      private static final int MIN_SURGE_INTERVAL = 40;
      private static final int MAX_SURGE_INTERVAL = 120;
      
      // Core instability constants (for active state)
      private static final float SHAKE_AMPLITUDE = 0.015f;
      private static final float CORE_BURST_SPEED = 50.0f * Mth.DEG_TO_RAD;
      private static final int MIN_BURST_INTERVAL = 80;
      private static final int MAX_BURST_INTERVAL = 200;
      
      // State transition speed
      private static final float TRANSITION_SPEED = 0.05f;
      
      private final ServerLevel world;
      private final ItemDisplayElement base;
      private final ItemDisplayElement top;
      private final ItemDisplayElement bot;
      private final ItemDisplayElement core;
      private boolean active;
      private int ticks;
      
      // Current rotation angles
      private float topShellAngle;
      private float botShellAngle;
      private float coreAngle;
      
      // Current speed (for smooth transitions)
      private float currentSpeedMultiplier = 1.0f;
      private float targetSpeedMultiplier = 1.0f;
      
      // Surge state for shells
      private float topShellSurge;
      private float botShellSurge;
      private int topShellSurgeTimer;
      private int botShellSurgeTimer;
      
      // Core burst and shake state
      private float coreBurst;
      private int coreBurstTimer;
      private float coreShakeX;
      private float coreShakeZ;
      
      // Oscillation phase offsets (for independent movement)
      private final float topShellPhase;
      private final float botShellPhase;
      private final float corePhase;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         this.active = state.getValue(ACTIVE);
         this.targetSpeedMultiplier = active ? ACTIVE_SPEED_MULTIPLIER : 1.0f;
         this.currentSpeedMultiplier = targetSpeedMultiplier;
         
         // Use ThreadLocalRandom for initialization since world.random may not be accessible from this thread
         Random initRandom = ThreadLocalRandom.current();
         
         // Random phase offsets for independent oscillation
         this.topShellPhase = initRandom.nextFloat() * Mth.TWO_PI;
         this.botShellPhase = initRandom.nextFloat() * Mth.TWO_PI;
         this.corePhase = initRandom.nextFloat() * Mth.TWO_PI;
         
         // Initialize surge/burst timers with staggered starts
         this.topShellSurgeTimer = MIN_SURGE_INTERVAL + initRandom.nextInt(MAX_SURGE_INTERVAL - MIN_SURGE_INTERVAL);
         this.botShellSurgeTimer = MIN_SURGE_INTERVAL / 2 + initRandom.nextInt(MAX_SURGE_INTERVAL - MIN_SURGE_INTERVAL);
         this.coreBurstTimer = MIN_BURST_INTERVAL / 3 + initRandom.nextInt(MAX_BURST_INTERVAL - MIN_BURST_INTERVAL);
         
         this.base = ItemDisplayElementUtil.createSimple(INTERDICTOR_BASE);
         this.base.setScale(new Vector3f(1f));
         this.addElement(this.base);
         
         this.top = ItemDisplayElementUtil.createSimple(INTERDICTOR_TOP);
         this.top.setInterpolationDuration(2);
         this.addElement(this.top);
         
         this.bot = ItemDisplayElementUtil.createSimple(INTERDICTOR_BOT);
         this.bot.setInterpolationDuration(2);
         this.addElement(this.bot);
         
         this.core = ItemDisplayElementUtil.createSimple(INTERDICTOR_CORE);
         this.core.setInterpolationDuration(2);
         this.addElement(this.core);
         
         // Initialize transformations
         updateTransformations();
      }
      
      private void updateTransformations(){
         // Top shell: rotates clockwise, oscillates up
         float topOscillation = SHELL_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / TOP_SHELL_OSCILLATION_PERIOD + topShellPhase);
         Matrix4f topMatrix = new Matrix4f();
         topMatrix.translate(0, topOscillation, 0);
         topMatrix.rotateY(topShellAngle);
         this.top.setTransformation(topMatrix);
         
         // Bottom shell: rotates counter-clockwise, oscillates (opposite phase from top)
         float botOscillation = SHELL_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / BOT_SHELL_OSCILLATION_PERIOD + botShellPhase);
         Matrix4f botMatrix = new Matrix4f();
         botMatrix.translate(0, botOscillation, 0);
         botMatrix.rotateY(botShellAngle);
         this.bot.setTransformation(botMatrix);
         
         // Core: rotates, oscillates, and when active can shake
         float coreOscillation = CORE_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / CORE_OSCILLATION_PERIOD + corePhase);
         Matrix4f coreMatrix = new Matrix4f();
         coreMatrix.translate(coreShakeX, coreOscillation, coreShakeZ);
         coreMatrix.rotateY(coreAngle);
         this.core.setTransformation(coreMatrix);
      }
      
      @Override
      public void tick(){
         super.tick();
         
         // Smooth transition between speed states
         if(Math.abs(currentSpeedMultiplier - targetSpeedMultiplier) > 0.01f){
            currentSpeedMultiplier += (targetSpeedMultiplier - currentSpeedMultiplier) * TRANSITION_SPEED;
         }else{
            currentSpeedMultiplier = targetSpeedMultiplier;
         }
         
         // Calculate shell speeds (shells rotate in opposite directions)
         float shellSpeed = SHELL_BASE_SPEED * currentSpeedMultiplier;
         float coreSpeed = CORE_BASE_SPEED * currentSpeedMultiplier;
         
         // Handle active-only effects
         if(active){
            // Shell surges
            topShellSurgeTimer--;
            if(topShellSurgeTimer <= 0){
               topShellSurge = SURGE_SPEED_BOOST;
               topShellSurgeTimer = MIN_SURGE_INTERVAL + world.random.nextInt(MAX_SURGE_INTERVAL - MIN_SURGE_INTERVAL);
            }
            
            botShellSurgeTimer--;
            if(botShellSurgeTimer <= 0){
               botShellSurge = SURGE_SPEED_BOOST;
               botShellSurgeTimer = MIN_SURGE_INTERVAL + world.random.nextInt(MAX_SURGE_INTERVAL - MIN_SURGE_INTERVAL);
            }
            
            // Core bursts and shakes
            coreBurstTimer--;
            if(coreBurstTimer <= 0){
               coreBurst = CORE_BURST_SPEED;
               // Also add a shake when bursting
               coreShakeX = (world.random.nextFloat() * 2 - 1) * SHAKE_AMPLITUDE;
               coreShakeZ = (world.random.nextFloat() * 2 - 1) * SHAKE_AMPLITUDE;
               coreBurstTimer = MIN_BURST_INTERVAL + world.random.nextInt(MAX_BURST_INTERVAL - MIN_BURST_INTERVAL);
            }
            
            // Random small shakes while active
            if(world.random.nextFloat() < 0.1f){
               coreShakeX = (world.random.nextFloat() * 2 - 1) * SHAKE_AMPLITUDE * 0.5f;
               coreShakeZ = (world.random.nextFloat() * 2 - 1) * SHAKE_AMPLITUDE * 0.5f;
            }
         }
         
         // Decay surges and bursts
         topShellSurge *= SURGE_DECAY;
         botShellSurge *= SURGE_DECAY;
         coreBurst *= SURGE_DECAY;
         
         // Decay shakes
         coreShakeX *= 0.85f;
         coreShakeZ *= 0.85f;
         
         // Apply rotations
         topShellAngle += shellSpeed + topShellSurge;  // Clockwise
         botShellAngle -= shellSpeed + botShellSurge;  // Counter-clockwise
         coreAngle += coreSpeed + coreBurst;
         
         // Normalize angles
         if(topShellAngle > Mth.TWO_PI) topShellAngle -= Mth.TWO_PI;
         if(botShellAngle < -Mth.TWO_PI) botShellAngle += Mth.TWO_PI;
         if(coreAngle > Mth.TWO_PI) coreAngle -= Mth.TWO_PI;
         
         updateTransformations();
         top.startInterpolation();
         bot.startInterpolation();
         core.startInterpolation();
         
         ticks++;
      }
      
      @Override
      public void notifyUpdate(HolderAttachment.UpdateType updateType){
         if(updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE){
            BlockState state = this.blockState();
            if(this.active != state.getValue(ACTIVE)){
               this.active = state.getValue(ACTIVE);
               this.targetSpeedMultiplier = active ? ACTIVE_SPEED_MULTIPLIER : 1.0f;
            }
         }
      }
   }
}

