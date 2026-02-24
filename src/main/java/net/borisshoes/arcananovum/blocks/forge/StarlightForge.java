package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
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
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StarlightForge extends ArcanaBlock implements MultiblockCore {
   public static final String SEED_USES_TAG = "seedUses";
   
   public static final String ID = "starlight_forge";
   
   private Multiblock multiblock;
   
   public StarlightForge(){
      id = ID;
      name = "Starlight Forge";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS, ArcaneTomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.SMITHING_TABLE;
      block = new StarlightForgeBlock(BlockBehaviour.Properties.of().noOcclusion().strength(2.5f, 1200.0f).sound(SoundType.WOOD));
      item = new StarlightForgeItem(this.block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR);
      researchTasks = new ResourceKey[]{ResearchTasks.OBTAIN_ARCANE_TOME, ResearchTasks.OBTAIN_ENCHANTED_GOLDEN_APPLE};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("SnivyXXY")), new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("SnivyXXY"))};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      putProperty(stack, SEED_USES_TAG, 0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("With the ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("stars ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("as your witness...").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("Your ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("journey ").withStyle(ChatFormatting.WHITE))
            .append(Component.literal("of ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("forging ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("new ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("begins!").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal(""));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" lets you craft ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" and ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("enhanced equipment").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" acts as a ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("hub ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal("for other ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal("Forge Structures").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_PURPLE)));
      addForgeLore(lore);
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-1, -1, -1);
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   public static StarlightForgeBlockEntity findActiveForge(ServerLevel world, BlockPos searchingPos){
      BlockPos range = new BlockPos(15, 8, 15);
      for(BlockPos blockPos : BlockPos.betweenClosed(searchingPos.offset(range), searchingPos.subtract(range))){
         BlockEntity be = world.getBlockEntity(blockPos);
         if(be instanceof StarlightForgeBlockEntity forge && forge.isAssembled()){
            BlockPos offset = blockPos.subtract(searchingPos);
            BlockPos forgeRange = forge.getForgeRange();
            if(Math.abs(offset.getX()) <= forgeRange.getX() && Math.abs(offset.getY()) <= forgeRange.getY() && Math.abs(offset.getZ()) <= forgeRange.getZ())
               return forge;
         }
      }
      return null;
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nEnchanted Golden Apples are a unique arcane artifact that I have discovered. Modern replicants do not seem to hold the same caliber of properties. My latest theories of Arcana suggest that the magic ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nof this land is far more versatile than the old scholars believed. I just need something to kickstart my new field of research. It is possible that I can use some energy from starlight to transfer the ancient enchantment of a ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\nGolden Apple. If I am to be successful in my research, I will need a forge…\n\nThe Starlight Forge allows the creation of infused weapons, tools, and armor.\n\nIt creates a 17x11x17 workspace that can ").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Starlight Forge").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.STARLIGHT_FORGE_COLOR), Component.literal("\ninteract with additions to the forge that can be crafted as I advance my research.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StarlightForgeItem extends ArcanaPolymerBlockItem {
      public StarlightForgeItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class StarlightForgeBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public StarlightForgeBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context.getPlayer())){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.SMITHING_TABLE.defaultBlockState();
         }
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new StarlightForgeBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.STARLIGHT_FORGE_BLOCK_ENTITY, StarlightForgeBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         StarlightForgeBlockEntity forge = (StarlightForgeBlockEntity) world.getBlockEntity(pos);
         if(forge != null){
            if(playerEntity instanceof ServerPlayer player){
               if(forge.isAssembled()){
                  forge.openMainGui(player, null);
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(forge.getMultiblockCheck(), player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StarlightForgeBlockEntity forge){
            initializeArcanaBlock(stack, forge);
            forge.setSeedUses(getIntProperty(stack, SEED_USES_TAG));
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
      public static final ItemStack FORGE_BASE = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge"));
      public static final ItemStack FORGE_APPLE = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge_apple"));
      public static final ItemStack STAR = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge_star"));
      public static final ItemStack PULSAR = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge_pulsar"));
      public static final ItemStack QUASAR = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge_quasar"));
      public static final ItemStack BLACK_HOLE = ItemDisplayElementUtil.getTransparentModel(Identifier.fromNamespaceAndPath(MOD_ID, "block/starlight_forge_black_hole"));
      
      // Apple animation constants
      private static final float APPLE_SPIN_SPEED = 1.0f * Mth.DEG_TO_RAD; // Degrees per tick
      private static final float APPLE_OSCILLATION_AMPLITUDE = 0.015f;
      private static final float APPLE_OSCILLATION_PERIOD = 50f;
      
      // Decal animation constants
      private static final float DECAL_Y_MIN = 1.5f; // Minimum height above forge
      private static final float DECAL_Y_MAX = 2.5f; // Maximum height above forge
      private static final float DECAL_HORIZONTAL_RANGE = 1.25f; // Max horizontal distance
      private static final float DECAL_FADE_SPEED = 0.015f; // Fade in/out speed
      private static final float DECAL_FADE_OUT_POINT = 0.3f; // Start fading at 70% through path (30% remaining)
      private static final int DECAL_MIN_LIFETIME = 250; // Minimum ticks visible
      private static final int DECAL_MAX_LIFETIME = 500; // Maximum ticks visible
      private static final float DECAL_SCALE = 1.0f;
      private static final float DECAL_SPIN_SPEED = 0.5f * Mth.DEG_TO_RAD; // Slow rotation during path
      
      private final ServerLevel world;
      private final ItemDisplayElement base;
      private final ItemDisplayElement apple;
      private final ItemDisplayElement decal1;
      private final ItemDisplayElement decal2;
      private int ticks;
      private float appleSpinAngle;
      
      // Decal animation state - each decal has its own path and timing
      private final float[] decalOpacity = new float[2]; // Current opacity for fade
      private final float[] decalTargetOpacity = new float[2]; // Target opacity
      private final int[] decalLifetime = new int[2]; // Ticks remaining before fade out
      private final int[] decalInitialLifetime = new int[2]; // Original lifetime for path progress calculation
      private final boolean[] decalActive = new boolean[2]; // Whether decal is currently showing
      
      // Random path control points for bezier curves (regenerated each cycle)
      private final Vector3f[] decalStartPos = new Vector3f[2];
      private final Vector3f[] decalMidPos = new Vector3f[2];
      private final Vector3f[] decalEndPos = new Vector3f[2];
      private final float[] decalRotationX = new float[2];
      private final float[] decalRotationY = new float[2];
      private final float[] decalRotationZ = new float[2];
      private final Vector3f[] decalSpinAxis = new Vector3f[2];
      private final float[] decalSpinAngle = new float[2];
      
      public Model(ServerLevel world, BlockState state){
         this.world = world;
         
         this.base = ItemDisplayElementUtil.createSimple(FORGE_BASE);
         this.base.setScale(new Vector3f(1f));
         this.addElement(this.base);
         
         this.apple = ItemDisplayElementUtil.createSimple(FORGE_APPLE);
         this.apple.setInterpolationDuration(2);
         this.addElement(this.apple);
         
         this.decal1 = ItemDisplayElementUtil.createSimple(STAR);
         this.decal1.setInterpolationDuration(2);
         
         this.decal2 = ItemDisplayElementUtil.createSimple(STAR);
         this.decal2.setInterpolationDuration(2);
         
         // Use ThreadLocalRandom for initialization since world.random may not be accessible from this thread
         Random initRandom = ThreadLocalRandom.current();
         
         // Initialize decal paths
         for(int i = 0; i < 2; i++){
            decalStartPos[i] = new Vector3f();
            decalMidPos[i] = new Vector3f();
            decalEndPos[i] = new Vector3f();
            decalSpinAxis[i] = new Vector3f();
            initializeDecalPath(i, initRandom::nextFloat, initRandom::nextInt);
         }
         
         // Stagger the decals so they don't appear at the same time
         decalLifetime[1] = DECAL_MIN_LIFETIME / 2;
         
         updateAppleTransformation();
      }
      
      private ItemStack getRandomDecalType(float roll){
         // Weighted random: Star (common), Pulsar (uncommon), Black Hole (rare), Quasar (very rare)
         if(roll < 0.6f){
            return STAR;
         }else if(roll < 0.85f){
            return PULSAR;
         }else if(roll < 0.95f){
            return BLACK_HOLE;
         }else{
            return QUASAR;
         }
      }
      
      private void initializeDecalPath(int index){
         initializeDecalPath(index, world.random::nextFloat, world.random::nextInt);
      }
      
      private void initializeDecalPath(int index, Supplier<Float> nextFloat, IntUnaryOperator nextInt){
         // Generate random start, middle, and end points for a curved path
         decalStartPos[index].set(
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE,
               DECAL_Y_MIN + nextFloat.get() * (DECAL_Y_MAX - DECAL_Y_MIN),
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE
         );
         
         decalMidPos[index].set(
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE,
               DECAL_Y_MIN + nextFloat.get() * (DECAL_Y_MAX - DECAL_Y_MIN),
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE
         );
         
         decalEndPos[index].set(
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE,
               DECAL_Y_MIN + nextFloat.get() * (DECAL_Y_MAX - DECAL_Y_MIN),
               (nextFloat.get() * 2 - 1) * DECAL_HORIZONTAL_RANGE
         );
         
         // Random initial rotation for this decal
         decalRotationX[index] = nextFloat.get() * Mth.TWO_PI;
         decalRotationY[index] = nextFloat.get() * Mth.TWO_PI;
         decalRotationZ[index] = nextFloat.get() * Mth.TWO_PI;
         
         // Random spin axis (normalized)
         decalSpinAxis[index].set(
               nextFloat.get() * 2 - 1,
               nextFloat.get() * 2 - 1,
               nextFloat.get() * 2 - 1
         ).normalize();
         decalSpinAngle[index] = 0f;
         
         // Set random lifetime and store initial value for path progress calculation
         decalInitialLifetime[index] = DECAL_MIN_LIFETIME + nextInt.applyAsInt(DECAL_MAX_LIFETIME - DECAL_MIN_LIFETIME);
         decalLifetime[index] = decalInitialLifetime[index];
         
         // Set to fade in
         decalTargetOpacity[index] = 1f;
         
         // Assign random decal type
         ItemDisplayElement decal = index == 0 ? decal1 : decal2;
         decal.setItem(getRandomDecalType(nextFloat.get()));
      }
      
      private Vector3f getDecalPosition(int index, float t){
         // Quadratic bezier curve: B(t) = (1-t)²P0 + 2(1-t)tP1 + t²P2
         float oneMinusT = 1f - t;
         float oneMinusTSq = oneMinusT * oneMinusT;
         float tSq = t * t;
         
         Vector3f start = decalStartPos[index];
         Vector3f mid = decalMidPos[index];
         Vector3f end = decalEndPos[index];
         
         return new Vector3f(
               oneMinusTSq * start.x + 2 * oneMinusT * t * mid.x + tSq * end.x,
               oneMinusTSq * start.y + 2 * oneMinusT * t * mid.y + tSq * end.y,
               oneMinusTSq * start.z + 2 * oneMinusT * t * mid.z + tSq * end.z
         );
      }
      
      private void updateAppleTransformation(){
         float oscillation = APPLE_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / APPLE_OSCILLATION_PERIOD);
         
         Matrix4f matrix = new Matrix4f();
         matrix.translate(0, oscillation, 0);
         matrix.rotateY(appleSpinAngle);
         this.apple.setTransformation(matrix);
      }
      
      private void updateDecalTransformation(int index){
         ItemDisplayElement decal = index == 0 ? decal1 : decal2;
         
         // Calculate path progress from elapsed lifetime (0 at start, 1 at end)
         float t = 1f - ((float) decalLifetime[index] / (float) decalInitialLifetime[index]);
         t = Math.max(0f, Math.min(1f, t)); // Clamp to 0-1
         
         // Apply smootherstep for stronger ease-in-out movement (much slower at start and end)
         // smootherstep: 6t^5 - 15t^4 + 10t^3
         float smoothT = t * t * t * (t * (t * 6f - 15f) + 10f);
         
         Vector3f pos = getDecalPosition(index, smoothT);
         float opacity = decalOpacity[index];
         
         // Scale calculation:
         // - Fade in: scale grows from 0 to 1.0 during the first portion of the path
         // - Middle: scale stays at 1.0
         // - Fade out: scale shrinks from 1.0 to 0 as opacity decreases
         float pathScale;
         if(decalTargetOpacity[index] > 0.5f){
            // Fading in or fully visible - use sine curve for growth based on path progress
            // Only use the first half of the sine curve (0 to PI/2) for growth, then stay at 1.0
            float growthT = Math.min(t / (1f - DECAL_FADE_OUT_POINT), 1f); // Normalize to 0-1 over the non-fade portion
            float sinValue = Mth.sin(Mth.HALF_PI * growthT); // 0 -> 1 as t goes 0 -> fade point
            pathScale = sinValue; // Scale from 0 to 1
         }else{
            // Fading out - scale shrinks proportionally with opacity (from 1 to 0)
            pathScale = opacity;
         }
         
         Vector3f spinAxis = decalSpinAxis[index];
         
         Matrix4f matrix = new Matrix4f();
         matrix.translate(pos.x, pos.y, pos.z);
         // Apply initial random rotation
         matrix.rotateX(decalRotationX[index]);
         matrix.rotateY(decalRotationY[index]);
         matrix.rotateZ(decalRotationZ[index]);
         // Apply continuous spin around random local axis
         matrix.rotate(decalSpinAngle[index], spinAxis.x, spinAxis.y, spinAxis.z);
         matrix.scale(DECAL_SCALE * pathScale); // Scale based on path/fade state
         
         decal.setTransformation(matrix);
      }
      
      @Override
      public void tick(){
         super.tick();
         
         // Apple animation
         appleSpinAngle += APPLE_SPIN_SPEED;
         if(appleSpinAngle > Mth.TWO_PI) appleSpinAngle -= Mth.TWO_PI;
         updateAppleTransformation();
         apple.startInterpolation();
         
         // Decal animations
         for(int i = 0; i < 2; i++){
            if(decalOpacity[i] < decalTargetOpacity[i]){
               decalOpacity[i] = Math.min(decalOpacity[i] + DECAL_FADE_SPEED, decalTargetOpacity[i]);
            }else if(decalOpacity[i] > decalTargetOpacity[i]){
               decalOpacity[i] = Math.max(decalOpacity[i] - DECAL_FADE_SPEED, decalTargetOpacity[i]);
            }
            
            // Add/remove decal element based on opacity
            if(decalOpacity[i] > 0.01f && !decalActive[i]){
               this.addElement(i == 0 ? decal1 : decal2);
               decalActive[i] = true;
            }else if(decalOpacity[i] <= 0.01f && decalActive[i]){
               this.removeElement(i == 0 ? decal1 : decal2);
               decalActive[i] = false;
               initializeDecalPath(i);
            }
            
            // Progress along path
            if(decalActive[i]){
               decalSpinAngle[i] += DECAL_SPIN_SPEED;
               if(decalSpinAngle[i] > Mth.TWO_PI) decalSpinAngle[i] -= Mth.TWO_PI;
               
               decalLifetime[i]--;
               
               // Start fading out when DECAL_FADE_OUT_POINT of lifetime remains
               // This gives the fade plenty of time to complete as the decal finishes its path
               int fadeOutThreshold = (int) (decalInitialLifetime[i] * DECAL_FADE_OUT_POINT);
               if(decalLifetime[i] <= fadeOutThreshold){
                  decalTargetOpacity[i] = 0f;
               }
               
               updateDecalTransformation(i);
               (i == 0 ? decal1 : decal2).startInterpolation();
            }
         }
         
         ticks++;
      }
   }
}

