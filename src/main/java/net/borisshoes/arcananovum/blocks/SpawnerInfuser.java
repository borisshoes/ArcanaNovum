package net.borisshoes.arcananovum.blocks;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.PackAwareBlockModel;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.SpawnerInfuser.SpawnerInfuserBlock.ACTIVE;

public class SpawnerInfuser extends ArcanaBlock {
   public static final String ID = "spawner_infuser";
   
   public static final int[] pointsFromTier = {0, 16, 32, 64, 128, 256, 512, 1024};
   public static final Item POINTS_ITEM = Items.NETHER_STAR;
   
   public SpawnerInfuser(){
      id = ID;
      name = "Spawner Infuser";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      vanillaItem = Items.SCULK_SHRIEKER;
      block = new SpawnerInfuserBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.COLOR_BLACK).strength(3.0f, 1200.0f).sound(SoundType.SCULK_SHRIEKER));
      item = new SpawnerInfuserItem(this.block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_ARCANE_SINGULARITY, ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER, ResearchTasks.UNLOCK_SPAWNER_HARNESS, ResearchTasks.UNLOCK_STELLAR_CORE, ResearchTasks.OBTAIN_NETHERITE_INGOT, ResearchTasks.ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("Spawners ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("have their ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("natural limit").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(", ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("can now push them ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("further").withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Place ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("two blocks ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("below ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("spawner").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("requires a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("soulstone ").withStyle(ChatFormatting.DARK_RED))
            .append(Component.literal("matching the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("spawner ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("type").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("also requires ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Nether Stars").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to unlock ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("enhanced ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("infusions").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Apply ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("a ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Redstone signal").withStyle(ChatFormatting.RED))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("activate ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      lore.add(Component.literal("")
            .append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" the ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("Infuser ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("to ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("configure ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("its ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("abilities").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_AQUA)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nOne of my most intricate and powerful creations to date. This behemoth exploits a fascinating organism from the Deep Dark called Sculk. It acts as if soulsand was alive, growing, and feeding on souls.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nBy combining the tech from one of my earlier works, the Spawner Infuser, I can use Arcana to overload the innate magic that summons creatures. All the Sculk mechanisms need are some souls, provided easily from a").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nSoulstone, and some crystalline structure combined with a lot of energy. Nether stars work both as a focusing crystal and a power source, so that should do nicely.\n \nA simple Redstone signal will activate it.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Spawner Infuser").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD), Component.literal("\nAll aspects of the spawner can be configured, from range, to spawn delay, and a lot more. \nAs long as the Sculk has an adequate base of souls from the Soulstone, more and more upgrades can be added.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public static Item getPointsItem(){
      try{
         String itemId = ArcanaNovum.CONFIG.getValue(ArcanaConfig.SPAWNER_INFUSER_ITEM_ID).toString();
         Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(Identifier.parse(itemId));
         assert opt.isPresent();
         return opt.get().value();
      }catch(Exception e){
         return Items.NETHER_STAR;
      }
   }
   
   public class SpawnerInfuserItem extends ArcanaPolymerBlockItem {
      public SpawnerInfuserItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class SpawnerInfuserBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public SpawnerInfuserBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(BlockStateProperties.CAN_SUMMON, state.getValue(ACTIVE));
         }
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new SpawnerInfuserBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, SpawnerInfuserBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(ACTIVE, false);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(ACTIVE);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         SpawnerInfuserBlockEntity infuser = (SpawnerInfuserBlockEntity) world.getBlockEntity(pos);
         if(infuser != null){
            if(playerEntity instanceof ServerPlayer player){
               infuser.openGui(player);
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof SpawnerInfuserBlockEntity infuser){
            initializeArcanaBlock(stack, infuser);
            
            if(placer instanceof ServerPlayer player){
               SoundUtils.soulSounds(player.level(), pos, 5, 30);
               SoundUtils.playSound(world, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 1, .6f);
               player.displayClientMessage(Component.literal("The Infuser makes a most unsettling sound...").withStyle(ChatFormatting.DARK_GREEN), true);
            }
         }
      }
      
      @Override
      public boolean isPathfindable(BlockState state, PathComputationType type){
         return false;
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
      public static final ItemStack INFUSER = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/spawner_infuser"));
      public static final ItemStack INFUSER_ARM = ItemDisplayElementUtil.getTransparentModel(ArcanaRegistry.arcanaId("block/spawner_infuser_arm"));
      
      // Arm rotation constants - each arm points to a corner (45, 135, 225, 315 degrees)
      private static final float[] ARM_YAW = {45f, 135f, 225f, 315f};
      
      // Arm position offsets towards each corner (in blocks, from center)
      private static final float ARM_CORNER_OFFSET = -0.50f; // Distance towards corner
      
      // Arm state constants
      private static final float ARM_SCALE_HIDDEN = 0.001f; // noStone - invisible
      private static final float ARM_SCALE_RETRACTED = 0.5f; // noSpawner - small/retracted
      private static final float ARM_SCALE_READY = 0.75f; // ready - almost full size
      private static final float ARM_SCALE_ACTIVE = 1.0f; // active - full size
      
      private static final float ARM_PITCH_RETRACTED = -45f * Mth.DEG_TO_RAD; // noSpawner - partially retracted angle (negative = down)
      private static final float ARM_PITCH_LOWERED = -35f * Mth.DEG_TO_RAD; // ready - lowered position (negative = down)
      private static final float ARM_PITCH_ACTIVE = 0f; // active - fully upright
      
      private static final float LERP_SPEED = 0.15f; // Interpolation speed for smooth transitions
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      private final ItemDisplayElement[] arms = new ItemDisplayElement[4];
      private boolean active;
      private boolean ready;
      private boolean noSpawner;
      private boolean noStone;
      private int ticks;
      
      // Current interpolated values for smooth transitions
      private float currentScale = ARM_SCALE_HIDDEN;
      private float currentPitch = ARM_PITCH_RETRACTED;
      private float targetScale = ARM_SCALE_HIDDEN;
      private float targetPitch = ARM_PITCH_RETRACTED;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         this.active = state.getValue(SpawnerInfuserBlock.ACTIVE);
         this.main = ItemDisplayElementUtil.createSimple(INFUSER);
         this.main.setScale(new Vector3f(1f));
         this.addElement(this.main);
         
         // Initialize 4 arms, each rotated 45 degrees towards a corner
         for(int i = 0; i < 4; i++){
            arms[i] = ItemDisplayElementUtil.createSimple(INFUSER_ARM);
            if(!this.active) arms[i].setScale(new Vector3f(ARM_SCALE_HIDDEN));
            arms[i].setInterpolationDuration(3);
            this.addElement(arms[i]);
         }
         
         // Set initial state
         updateTargetState();
         // Snap to initial state immediately
         currentScale = targetScale;
         currentPitch = targetPitch;
         updateArmTransformations();
      }
      
      private void updateTargetState(){
         if(noStone){
            targetScale = ARM_SCALE_HIDDEN;
            targetPitch = ARM_PITCH_RETRACTED;
         }else if(noSpawner){
            targetScale = ARM_SCALE_RETRACTED;
            targetPitch = ARM_PITCH_RETRACTED;
         }else if(ready){
            targetScale = ARM_SCALE_READY;
            targetPitch = ARM_PITCH_LOWERED;
         }else if(active){
            targetScale = ARM_SCALE_ACTIVE;
            targetPitch = ARM_PITCH_ACTIVE;
         }else{
            // Default state (not ready, not active) - treat as ready/lowered
            targetScale = ARM_SCALE_READY;
            targetPitch = ARM_PITCH_LOWERED;
         }
      }
      
      private void updateArmTransformations(){
         for(int i = 0; i < 4; i++){
            float yawRad = ARM_YAW[i] * Mth.DEG_TO_RAD;
            
            // Calculate offset towards corner (45, 135, 225, 315 degrees from center)
            float offsetX = Mth.sin(yawRad) * ARM_CORNER_OFFSET;
            float offsetZ = Mth.cos(yawRad) * ARM_CORNER_OFFSET;
            
            Matrix4f matrix = new Matrix4f();
            // Translate towards the corner first
            matrix.translate(offsetX, 0, offsetZ);
            // Rotate to point towards corner
            matrix.rotateY(yawRad);
            // Apply pitch for up/down position
            matrix.rotateX(currentPitch);
            // Apply scale
            matrix.scale(currentScale);
            
            arms[i].setTransformation(matrix);
            arms[i].startInterpolation();
         }
      }
      
      @Override
      public void tick(){
         super.tick();
         
         BlockEntity entity = world.getBlockEntity(blockPos());
         if(entity instanceof SpawnerInfuserBlockEntity infuser){
            this.noStone = infuser.getSoulstone().isEmpty();
            BlockPos spawnerPos = blockPos().offset(0, 2, 0);
            BlockEntity blockEntity = world.getBlockEntity(spawnerPos);
            BlockState spawnerState = world.getBlockState(spawnerPos);
            this.noSpawner = !(spawnerState.is(Blocks.SPAWNER) && blockEntity instanceof SpawnerBlockEntity);
            this.ready = !this.active && !this.noStone && !this.noSpawner;
         }
         updateTargetState();
         
         // Smoothly interpolate towards target values
         boolean changed = false;
         
         if(Math.abs(currentScale - targetScale) > 0.001f){
            currentScale = Mth.lerp(LERP_SPEED, currentScale, targetScale);
            changed = true;
         }else if(currentScale != targetScale){
            currentScale = targetScale;
            changed = true;
         }
         
         if(Math.abs(currentPitch - targetPitch) > 0.001f){
            currentPitch = Mth.lerp(LERP_SPEED, currentPitch, targetPitch);
            changed = true;
         }else if(currentPitch != targetPitch){
            currentPitch = targetPitch;
            changed = true;
         }
         
         if(changed){
            updateArmTransformations();
         }
         
         ticks++;
      }
      
      @Override
      public void notifyUpdate(HolderAttachment.UpdateType updateType){
         if(updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE){
            BlockState state = this.blockState();
            if(this.active != state.getValue(ACTIVE)){
               this.active = state.getValue(ACTIVE);
               updateTargetState();
            }
         }
      }
   }
}

