package net.borisshoes.arcananovum.blocks;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.core.polymer.PackAwareBlockModel;
import net.borisshoes.arcananovum.datastorage.AnchorData;
import net.borisshoes.arcananovum.gui.arcanetome.ArcaneTomeGui;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.ANCHOR_CHUNKS;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.blocks.ContinuumAnchor.ContinuumAnchorBlock.ACTIVE;
import static net.borisshoes.arcananovum.blocks.ContinuumAnchor.ContinuumAnchorBlock.CHARGES;

// Credit to xZarex for some of the Chunk Loading mixin code
public class ContinuumAnchor extends ArcanaBlock {
   public static final int RANGE = 2;
   
   public static final String ID = "continuum_anchor";
   
   public ContinuumAnchor(){
      id = ID;
      name = "Continuum Anchor";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new ArcaneTomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), ArcaneTomeGui.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.RESPAWN_ANCHOR;
      block = new ContinuumAnchorBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0f, 1200.0f).lightLevel(state -> ContinuumAnchorBlock.getLightLevel(state, 15)));
      item = new ContinuumAnchorItem(block);
      displayName = Component.translatableWithFallback("item." + MOD_ID + "." + ID, name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT, ResearchTasks.UNLOCK_EXOTIC_MATTER, ResearchTasks.ADVANCEMENT_CHARGE_RESPAWN_ANCHOR, ResearchTasks.UNLOCK_STELLAR_CORE};
      attributions = new Tuple[]{new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.texture_by"), Component.literal("SnivyXXY")), new Tuple<>(Component.translatable("credits_and_attribution.arcananovum.model_by"), Component.literal("SnivyXXY"))};
   }
   
   @Override
   public List<Component> getItemLore(@Nullable ItemStack itemStack){
      List<MutableComponent> lore = new ArrayList<>();
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("continuum anchor").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(" has the extraordinary ability to manipulate ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("spacetime").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("It just needs the ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("right type").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(" of ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("fuel").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("continuum anchor").withColor(ArcanaColors.BETTER_DARK_BLUE))
            .append(Component.literal(" consumes ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("exotic matter").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" to ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("chunk load").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("5x5 area").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("area ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal("also receives ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("random ticks").withStyle(ChatFormatting.BLUE))
            .append(Component.literal(" and keeps ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("mobs ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("loaded").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.DARK_GRAY)));
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static void loadChunks(ServerLevel serverWorld, ChunkPos pos){
      for(int i = -RANGE; i <= RANGE; i++){
         for(int j = -RANGE; j <= RANGE; j++){
            ContinuumAnchor.addChunk(serverWorld, new ChunkPos(pos.x() + i, pos.z() + j));
         }
      }
   }
   
   public static void addChunk(ServerLevel world, ChunkPos chunk){
      world.getChunkSource().addTicketWithRadius(ArcanaRegistry.ANCHOR_TICKET_TYPE, chunk, 2);
      Long2IntOpenHashMap m = ANCHOR_CHUNKS.computeIfAbsent(world, w -> new Long2IntOpenHashMap());
      m.put(chunk.pack(), 40);
   }
   
   public static boolean isChunkLoaded(ServerLevel serverWorld, ChunkPos chunk){
      Long2IntOpenHashMap chunks = ANCHOR_CHUNKS.get(serverWorld);
      return chunks != null && chunks.containsKey(chunk.pack());
   }
   
   public static void updateLoadedChunks(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         Long2IntOpenHashMap chunks = ANCHOR_CHUNKS.get(world);
         if(chunks == null || chunks.isEmpty()) continue;
         boolean nonEmpty = false;
         ObjectIterator<Long2IntMap.Entry> it = chunks.long2IntEntrySet().fastIterator();
         while(it.hasNext()){
            Long2IntMap.Entry e = it.next();
            int t = e.getIntValue();
            if(t <= 1){
               it.remove();
            }else{
               e.setValue(t - 1);
               nonEmpty = true;
            }
         }
         if(nonEmpty) world.resetEmptyTime();
      }
   }
   
   public static void initLoadedChunks(MinecraftServer minecraftServer){
      for(ServerLevel serverWorld : minecraftServer.getAllLevels()){
         for(BlockPos anchorPos : DataAccess.getWorld(serverWorld.dimension(), AnchorData.KEY).getAnchors()){
            ChunkPos chunkPos = ChunkPos.containing(anchorPos);
            loadChunks(serverWorld, chunkPos);
         }
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(), false)), Component.literal("\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing study casing that can channel Arcana, I have made additional efforts to reinforce").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nthis chassis against dimensional shear. By combining all known techniques of manipulating dimensional energy, I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nWhen fed with Exotic Matter, the Anchor chunk loads a 5x5 chunk area and produces lazy chunks in the 7x7 ring around it. \nIt is able to stimulate mobs such that they despawn slower when a player isn’t nearby,").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nwhile also inducing new spawns and activating spawners.\nThe Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. Additional fuel may also be added while still in use.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class ContinuumAnchorItem extends ArcanaPolymerBlockItem {
      public ContinuumAnchorItem(Block block){
         super(getThis(), block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class ContinuumAnchorBlock extends ArcanaPolymerBlockEntity implements FactoryBlock, PolymerTexturedBlock {
      public static final IntegerProperty CHARGES = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public ContinuumAnchorBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         if(PolymerResourcePackUtils.hasMainPack(context)){
            return Blocks.BARRIER.defaultBlockState();
         }else{
            return Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(CHARGES, state.getValue(CHARGES));
         }
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new ContinuumAnchorBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, ContinuumAnchorBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(ACTIVE, false).setValue(CHARGES, 0);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(CHARGES, ACTIVE);
      }
      
      @Override
      public boolean forceLightUpdates(BlockState blockState){
         return true;
      }
      
      @Override
      protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
         ContinuumAnchorBlockEntity anchor = (ContinuumAnchorBlockEntity) world.getBlockEntity(pos);
         if(anchor != null && anchor.interact(player, stack)) return InteractionResult.SUCCESS_SERVER;
         return InteractionResult.TRY_WITH_EMPTY_HAND;
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit){
         ContinuumAnchorBlockEntity anchor = (ContinuumAnchorBlockEntity) world.getBlockEntity(pos);
         if(anchor != null && anchor.interact(player, ItemStack.EMPTY)) return InteractionResult.SUCCESS_SERVER;
         return InteractionResult.PASS;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof ContinuumAnchorBlockEntity anchor){
            initializeArcanaBlock(stack, anchor);
            
            if(placer instanceof ServerPlayer player){
               player.sendSystemMessage(Component.literal("Placing the Continuum Anchor sends a ripple across spacetime.").withStyle(ChatFormatting.DARK_BLUE), true);
               SoundUtils.playSound(world, pos, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 5, .8f);
            }
         }
      }
      
      @Override
      protected boolean isPathfindable(BlockState state, PathComputationType type){
         return false;
      }
      
      public static int getLightLevel(BlockState state, int maxLevel){
         return Mth.floor((float) (state.getValue(CHARGES)) / 4.0f * (float) maxLevel);
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
      public static final LazyItemStack ANCHOR_BASE_0 = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_0"));
      public static final LazyItemStack ANCHOR_BASE_1 = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_1"));
      public static final LazyItemStack ANCHOR_BASE_2 = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_2"));
      public static final LazyItemStack ANCHOR_BASE_3 = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_3"));
      public static final LazyItemStack ANCHOR_BASE_4 = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_4"));
      public static final LazyItemStack ANCHOR_SPIKE = ItemDisplayElementUtil.getModel(ArcanaRegistry.arcanaId("block/continuum_anchor_spike"));
      
      // Spike animation constants
      private static final float SPIKE_TILT = 7.5f * Mth.DEG_TO_RAD; // Tilt towards center
      private static final float SPIKE_OFFSET = 0.3875f; // Offset from center along cardinal direction
      private static final float SPIKE_Y_OFFSET = -0.03125f; // Half a pixel lower (0.5 / 16)
      private static final float SPIKE_GROW_SPEED = 0.02f; // Slow growth when activating
      private static final float SPIKE_SHRINK_SPEED = 0.08f; // Fast shrink when deactivating
      
      // Matter animation constants
      private static final float MATTER_Y_OFFSET = 0.75f;
      private static final float MATTER_OSCILLATION_AMPLITUDE = 0.02f;
      private static final float MATTER_SPIN_SPEED = 2.0f * Mth.DEG_TO_RAD; // Degrees per tick
      private static final float MATTER_SCALE = 0.175f;
      
      private final ServerLevel world;
      private final ItemDisplayElement base;
      private final ItemDisplayElement northSpike;
      private final ItemDisplayElement westSpike;
      private final ItemDisplayElement eastSpike;
      private final ItemDisplayElement southSpike;
      private final ItemDisplayElement matter;
      private boolean active;
      private int charges;
      private int ticks;
      
      // Spike scale for animation (0 = hidden, 1 = fully grown)
      private float spikeScale;
      private float targetSpikeScale;
      private boolean spikesAdded;
      
      // Matter spin angle
      private float matterSpinAngle;
      
      public Model(ServerLevel world, BlockState state){
         this.charges = state.getValue(CHARGES);
         this.active = state.getValue(ACTIVE);
         this.world = world;
         this.spikeScale = active ? 1f : 0f;
         this.targetSpikeScale = active ? 1f : 0f;
         this.spikesAdded = false;
         this.matterSpinAngle = 0f;
         
         this.base = ItemDisplayElementUtil.createSimple(getBaseModelForCharges(charges));
         this.base.setScale(new Vector3f(1f));
         this.addElement(this.base);
         
         // North spike - offset towards -Z, tilts towards +Z (center)
         this.northSpike = ItemDisplayElementUtil.createSimple(ANCHOR_SPIKE);
         this.northSpike.setScale(new Vector3f(0f));
         this.northSpike.setInterpolationDuration(2);
         
         // South spike - offset towards +Z, tilts towards -Z (center)
         this.southSpike = ItemDisplayElementUtil.createSimple(ANCHOR_SPIKE);
         this.southSpike.setScale(new Vector3f(0f));
         this.southSpike.setInterpolationDuration(2);
         
         // West spike - offset towards -X, tilts towards +X (center)
         this.westSpike = ItemDisplayElementUtil.createSimple(ANCHOR_SPIKE);
         this.westSpike.setScale(new Vector3f(0f));
         this.westSpike.setInterpolationDuration(2);
         
         // East spike - offset towards +X, tilts towards -X (center)
         this.eastSpike = ItemDisplayElementUtil.createSimple(ANCHOR_SPIKE);
         this.eastSpike.setScale(new Vector3f(0f));
         this.eastSpike.setInterpolationDuration(2);
         
         this.matter = ItemDisplayElementUtil.createSimple(new ItemStack(ArcanaRegistry.EXOTIC_MATTER.getItem()));
         this.matter.setInterpolationDuration(2);
         if(charges > 0) this.addElement(this.matter);
         
         // Initialize spike transforms
         updateSpikeTransformations();
         updateMatterTransformation();
         
         // Add spikes if active on load
         if(active && spikeScale > 0){
            addSpikes();
         }
      }
      
      private void addSpikes(){
         if(!spikesAdded){
            this.addElement(this.northSpike);
            this.addElement(this.southSpike);
            this.addElement(this.westSpike);
            this.addElement(this.eastSpike);
            spikesAdded = true;
         }
      }
      
      private void removeSpikes(){
         if(spikesAdded){
            this.removeElement(this.northSpike);
            this.removeElement(this.southSpike);
            this.removeElement(this.westSpike);
            this.removeElement(this.eastSpike);
            spikesAdded = false;
         }
      }
      
      private static ItemStack getBaseModelForCharges(int charges){
         return switch(charges){
            case 1 -> ANCHOR_BASE_1.get();
            case 2 -> ANCHOR_BASE_2.get();
            case 3 -> ANCHOR_BASE_3.get();
            case 4 -> ANCHOR_BASE_4.get();
            default -> ANCHOR_BASE_0.get();
         };
      }
      
      private void updateSpikeTransformations(){
         // Spikes are centered at (8,8,8) in the model. When we scale Y, the spike shrinks towards center.
         // To make it appear to rise from ground, we scale Y and translate down to keep base at ground level.
         // The spike base is at Y=0 in model space (8 pixels below center), so half the spike height is 8 pixels = 0.5 blocks
         // When scaled, we need to translate by (1 - scale) * 0.5 downward to keep base anchored
         float yCompensation = (1f - spikeScale) * 0.5f;
         
         // North spike: offset -Z, tilt around X axis (positive rotation tilts top towards +Z)
         Matrix4f northMatrix = new Matrix4f();
         northMatrix.translate(0, SPIKE_Y_OFFSET - yCompensation, -SPIKE_OFFSET);
         northMatrix.rotateX(SPIKE_TILT);
         northMatrix.scale(1f, spikeScale, 1f); // Only scale Y
         this.northSpike.setTransformation(northMatrix);
         
         // South spike: offset +Z, tilt around X axis (negative rotation tilts top towards -Z)
         Matrix4f southMatrix = new Matrix4f();
         southMatrix.translate(0, SPIKE_Y_OFFSET - yCompensation, SPIKE_OFFSET);
         southMatrix.rotateX(-SPIKE_TILT);
         southMatrix.scale(1f, spikeScale, 1f);
         this.southSpike.setTransformation(southMatrix);
         
         // West spike: offset -X, tilt around Z axis (negative rotation tilts top towards +X)
         Matrix4f westMatrix = new Matrix4f();
         westMatrix.translate(-SPIKE_OFFSET, SPIKE_Y_OFFSET - yCompensation, 0);
         westMatrix.rotateZ(-SPIKE_TILT);
         westMatrix.scale(1f, spikeScale, 1f);
         this.westSpike.setTransformation(westMatrix);
         
         // East spike: offset +X, tilt around Z axis (positive rotation tilts top towards -X)
         Matrix4f eastMatrix = new Matrix4f();
         eastMatrix.translate(SPIKE_OFFSET, SPIKE_Y_OFFSET - yCompensation, 0);
         eastMatrix.rotateZ(SPIKE_TILT);
         eastMatrix.scale(1f, spikeScale, 1f);
         this.eastSpike.setTransformation(eastMatrix);
      }
      
      private void updateMatterTransformation(){
         float oscillation = MATTER_OSCILLATION_AMPLITUDE * Mth.sin(Mth.TWO_PI * ticks / 40);
         
         Matrix4f matterMatrix = new Matrix4f();
         matterMatrix.translate(0, MATTER_Y_OFFSET + oscillation, 0);
         if(active){
            matterMatrix.rotateY(matterSpinAngle);
         }
         matterMatrix.scale(MATTER_SCALE); // Scale must be in the matrix since setTransformation overrides setScale
         this.matter.setTransformation(matterMatrix);
      }
      
      @Override
      public void tick(){
         super.tick();
         
         // Animate spike scale towards target
         if(Math.abs(spikeScale - targetSpikeScale) > 0.001f){
            if(spikeScale < targetSpikeScale){
               spikeScale = Math.min(spikeScale + SPIKE_GROW_SPEED, targetSpikeScale);
            }else{
               spikeScale = Math.max(spikeScale - SPIKE_SHRINK_SPEED, targetSpikeScale);
            }
            updateSpikeTransformations();
            northSpike.startInterpolation();
            southSpike.startInterpolation();
            westSpike.startInterpolation();
            eastSpike.startInterpolation();
            
            // Remove spikes when fully shrunk
            if(spikeScale <= 0.001f && targetSpikeScale == 0f){
               removeSpikes();
            }
         }
         
         // Spin matter when active
         if(active){
            matterSpinAngle += MATTER_SPIN_SPEED;
            if(matterSpinAngle > Mth.TWO_PI) matterSpinAngle -= Mth.TWO_PI;
         }
         
         // Always update matter for oscillation
         updateMatterTransformation();
         matter.startInterpolation();
         
         ticks++;
      }
      
      @Override
      public void notifyUpdate(HolderAttachment.UpdateType updateType){
         if(updateType == BlockAwareAttachment.BLOCK_STATE_UPDATE){
            BlockState state = this.blockState();
            if(this.active != state.getValue(ACTIVE)){
               this.active = state.getValue(ACTIVE);
               if(this.active){
                  addSpikes();
                  targetSpikeScale = 1f;
               }else{
                  targetSpikeScale = 0f;
               }
            }
            if(this.charges != state.getValue(CHARGES)){
               int oldCharges = this.charges;
               this.charges = state.getValue(CHARGES);
               this.base.setItem(getBaseModelForCharges(this.charges));
               if(this.charges == 0 && oldCharges > 0){
                  this.removeElement(this.matter);
               }else if(this.charges > 0 && oldCharges == 0){
                  this.addElement(this.matter);
               }
            }
         }
      }
   }
}

