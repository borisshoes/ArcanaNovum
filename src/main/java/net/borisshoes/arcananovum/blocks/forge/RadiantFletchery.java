package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
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
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.forge.RadiantFletchery.RadiantFletcheryBlock.HORIZONTAL_FACING;

public class RadiantFletchery extends ArcanaBlock implements MultiblockCore {
   public static final String ID = "radiant_fletchery";
   
   private Multiblock multiblock;
   
   public RadiantFletchery(){
      id = ID;
      name = "Radiant Fletchery";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.FLETCHING_TABLE;
      block = new RadiantFletcheryBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.5f, 1200.0f).sound(SoundType.WOOD));
      item = new RadiantFletcheryItem(this.block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_RUNIC_MATRIX, ResearchTasks.ADVANCEMENT_SHOOT_ARROW, ResearchTasks.ADVANCEMENT_OL_BETSY, ResearchTasks.OBTAIN_TIPPED_ARROW, ResearchTasks.OBTAIN_SPECTRAL_ARROW, ResearchTasks.ADVANCEMENT_BREW_POTION, ResearchTasks.UNLOCK_STARLIGHT_FORGE};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("A ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" addon to the ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Fletchery ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("enables ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("efficient ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("creation of ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("tipped arrows").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Fletchery ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("also ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("unlocks ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("the ability to make ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("Runic Arrows").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.LIGHT_PURPLE)));
      addForgeLore(lore);
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
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
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nI have yet to put my Runic Matrix to good use. Fortunately, this might be my chance. The Matrix should be able to take on the effect of potions to boost the amount of Tipped Arrows I can make from a single ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\npotion. The Arrows themselves could make an excellent candidate for use of the Matrix once I master more of its capabilities. Perhaps if I make an arrow out of a Matrix it could activate powerful effects upon hitting a target.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal(" Radiant Fletchery").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nThe Fletchery boosts the amount of Tipped Arrows made per potion, as well as allowing non-lingering potions to be used.\n\nThe Fletchery also unlocks a host of Archery related recipes for the Starlight Forge.\n").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class RadiantFletcheryItem extends ArcanaPolymerBlockItem {
      public RadiantFletcheryItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class RadiantFletcheryBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      
      public RadiantFletcheryBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.FLETCHING_TABLE.defaultBlockState();
         }
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection());
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING);
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
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new RadiantFletcheryBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, RadiantFletcheryBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         RadiantFletcheryBlockEntity fletchery = (RadiantFletcheryBlockEntity) world.getBlockEntity(pos);
         if(fletchery != null){
            if(playerEntity instanceof ServerPlayer player){
               if(fletchery.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(), pos) == null){
                     player.sendSystemMessage(Component.literal("The Fletchery must be within the range of an active Starlight Forge"));
                  }else{
                     fletchery.openGui(player);
                     player.getCooldowns().addCooldown(playerEntity.getMainHandItem(), 1);
                     player.getCooldowns().addCooldown(playerEntity.getOffhandItem(), 1);
                  }
               }else{
                  if(player.isShiftKeyDown() && player.isCreative()){
                     multiblock.build(fletchery.getMultiblockCheck());
                  }else{
                     player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                     multiblock.displayStructure(fletchery.getMultiblockCheck(), player);
                  }
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
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
      public static final LazyItemStack FLETCHERY = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/radiant_fletchery"));
      public static final LazyItemStack MATRIX = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/radiant_fletchery_matrix"));
      
      // Rubik's cube-like basis vectors - each disc rotates around its assigned axis
      private static final Vector3f[] BASIS_VECTORS = {
            new Vector3f(1, 0, 0),  // X axis - disc will be tilted to face X direction
            new Vector3f(0, 1, 0),  // Y axis - disc stays horizontal
            new Vector3f(0, 0, 1)   // Z axis - disc will be tilted to face Z direction
      };
      
      // Initial rotations to orient each disc like a Rubik's cube face
      // Disc 0 (X-axis): rotate 90° around Z to make it face X direction
      // Disc 1 (Y-axis): no rotation needed, stays horizontal
      // Disc 2 (Z-axis): rotate 90° around X to make it face Z direction
      private static final float[] INITIAL_TILTS = {
            Mth.HALF_PI,   // 90° for X-facing disc (rotated around Z)
            0f,            // 0° for Y-facing disc (horizontal)
            Mth.HALF_PI    // 90° for Z-facing disc (rotated around X)
      };
      
      // Axes to apply initial tilt around
      private static final Vector3f[] TILT_AXES = {
            new Vector3f(0, 0, 1),  // Tilt around Z for X-facing disc
            new Vector3f(0, 1, 0),  // No tilt needed for Y-facing disc
            new Vector3f(1, 0, 0)   // Tilt around X for Z-facing disc
      };
      
      // Disc model offset from block center (in block units)
      // Model is at pixel (6.5, 15, 6.5), center of 3x1x3 disc is at (8, 15.5, 8)
      // In block units from (8, 8, 8): (0, 7.5, 0) pixels = (0, 0.46875, 0) blocks
      private static final float DISC_Y_OFFSET = 0.46875f; // Height above block center
      private static final Vector3f SLOW_ROTATION_AXIS = new Vector3f(0.577f, 0.577f, 0.577f); // Roughly (1,1,1) normalized
      private static final float SLOW_ROTATION_SPEED = 1.5f * Mth.DEG_TO_RAD; // Degrees per tick converted to radians
      private static final float FLICK_EASE_FACTOR = 0.25f; // Easing factor - starts fast, slows down (drift effect)
      private static final float MIN_FLICK_DEGREES = 90f; // Minimum degrees to rotate per flick
      private static final float MAX_FLICK_DEGREES = 270f; // Maximum degrees to rotate per flick
      private static final int MIN_WAIT_TICKS = 20;
      private static final int MAX_WAIT_TICKS = 60;
      private static final float GROUP_OSCILLATION_AMPLITUDE = 0.020f; // Main group vertical oscillation
      private static final float INDIVIDUAL_Y_OSCILLATION_AMPLITUDE = 0.005f; // Small individual vertical variation
      private static final float INDIVIDUAL_XZ_OSCILLATION_AMPLITUDE = 0.005f; // Tiny individual horizontal variation
      
      private final ServerLevel world;
      private final ItemDisplayElement main;
      private final ItemDisplayElement mat1;
      private final ItemDisplayElement mat2;
      private final ItemDisplayElement mat3;
      private final int[] basisIndices = new int[3];
      private final float[] currentAngles = new float[3];
      private final float[] targetAngles = new float[3];
      private final int[] flickCooldowns = new int[3];
      private final float[] oscillationPhases = new float[3];
      private final float[] xOscillationPhases = new float[3];
      private final float[] zOscillationPhases = new float[3];
      private boolean active;
      private int ticks;
      private float slowRotationAngle;
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         
         this.main = ItemDisplayElementUtil.createSimple(FLETCHERY);
         this.main.setScale(new Vector3f(1f));
         this.main.setYaw(state.getValue(HORIZONTAL_FACING).toYRot());
         this.addElement(this.main);
         
         basisIndices[0] = 0;
         basisIndices[1] = 1;
         basisIndices[2] = 2;
         
         // Use ThreadLocalRandom.current() for initialization since world.getRandom() may not be accessible from this thread
         Random initRandom = ThreadLocalRandom.current();
         for(int i = 0; i < 3; i++){
            oscillationPhases[i] = initRandom.nextFloat() * Mth.TWO_PI;
            xOscillationPhases[i] = initRandom.nextFloat() * Mth.TWO_PI;
            zOscillationPhases[i] = initRandom.nextFloat() * Mth.TWO_PI;
            flickCooldowns[i] = MIN_WAIT_TICKS + initRandom.nextInt(MAX_WAIT_TICKS - MIN_WAIT_TICKS);
         }
         
         this.mat1 = ItemDisplayElementUtil.createSimple(MATRIX);
         this.mat1.setScale(new Vector3f(1f));
         this.addElement(this.mat1);
         
         this.mat2 = ItemDisplayElementUtil.createSimple(MATRIX);
         this.mat2.setScale(new Vector3f(1f));
         this.addElement(this.mat2);
         
         this.mat3 = ItemDisplayElementUtil.createSimple(MATRIX);
         this.mat3.setScale(new Vector3f(1f));
         this.addElement(this.mat3);
         
         // Set initial transformations immediately (no interpolation) to ensure proper initialization on world reload
         updateMatrixTransformations();
         
         // Now set interpolation duration for smooth animations during gameplay
         this.mat1.setInterpolationDuration(3);
         this.mat2.setInterpolationDuration(3);
         this.mat3.setInterpolationDuration(3);
      }
      
      private Matrix4f createDiscTransformation(int discIndex, float groupYOffset, float individualYOffset, float individualXOffset, float individualZOffset){
         Vector3f basis = BASIS_VECTORS[basisIndices[discIndex]];
         Vector3f tiltAxis = TILT_AXES[basisIndices[discIndex]];
         float initialTilt = INITIAL_TILTS[basisIndices[discIndex]];
         float angle = currentAngles[discIndex];
         
         Matrix4f matrix = new Matrix4f();
         matrix.translate(individualXOffset, DISC_Y_OFFSET + groupYOffset + individualYOffset, individualZOffset);
         matrix.rotate(slowRotationAngle, SLOW_ROTATION_AXIS.x, SLOW_ROTATION_AXIS.y, SLOW_ROTATION_AXIS.z);
         matrix.rotate(angle, basis.x, basis.y, basis.z);
         matrix.rotate(initialTilt, tiltAxis.x, tiltAxis.y, tiltAxis.z);
         return matrix;
      }
      
      private void updateMatrixTransformations(){
         ItemDisplayElement[] matrices = {mat1, mat2, mat3};
         float groupOscillation = GROUP_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / 60);
         for(int i = 0; i < 3; i++){
            float individualY = INDIVIDUAL_Y_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / 40 + oscillationPhases[i]);
            float individualX = INDIVIDUAL_XZ_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / 50 + xOscillationPhases[i]);
            float individualZ = INDIVIDUAL_XZ_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / 45 + zOscillationPhases[i]);
            matrices[i].setTransformation(createDiscTransformation(i, groupOscillation, individualY, individualX, individualZ));
         }
      }
      
      @Override
      public void tick(){
         super.tick();
         
         slowRotationAngle += SLOW_ROTATION_SPEED;
         if(slowRotationAngle > Mth.TWO_PI) slowRotationAngle -= Mth.TWO_PI;
         
         if(active){
            for(int i = 0; i < 3; i++){
               float angleDiff = targetAngles[i] - currentAngles[i];
               if(Math.abs(angleDiff) > 0.005f){
                  currentAngles[i] += angleDiff * FLICK_EASE_FACTOR;
               }else{
                  currentAngles[i] = targetAngles[i];
                  flickCooldowns[i]--;
                  if(flickCooldowns[i] <= 0){
                     float flickDegrees = MIN_FLICK_DEGREES + world.getRandom().nextFloat() * (MAX_FLICK_DEGREES - MIN_FLICK_DEGREES);
                     float flickAmount = flickDegrees * Mth.DEG_TO_RAD * (world.getRandom().nextBoolean() ? 1 : -1);
                     targetAngles[i] += flickAmount;
                     flickCooldowns[i] = MIN_WAIT_TICKS + world.getRandom().nextInt(MAX_WAIT_TICKS - MIN_WAIT_TICKS);
                  }
               }
            }
         }
         
         updateMatrixTransformations();
         mat1.startInterpolation();
         mat2.startInterpolation();
         mat3.startInterpolation();
         
         // Check active state every second
         if(ticks % 20 == 0){
            RadiantFletcheryBlockEntity fletchery = (RadiantFletcheryBlockEntity) world.getBlockEntity(this.blockPos());
            if(fletchery != null){
               this.active = fletchery.isAssembled() && StarlightForge.findActiveForge(world, this.blockPos()) != null;
            }else{
               this.active = false;
            }
         }
         ticks++;
      }
   }
}

