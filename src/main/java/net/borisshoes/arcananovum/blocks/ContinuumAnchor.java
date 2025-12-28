package net.borisshoes.arcananovum.blocks;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
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
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.ANCHOR_CHUNKS;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;

// Credit to xZarex for some of the Chunk Loading mixin code
public class ContinuumAnchor extends ArcanaBlock {
   public static final int RANGE = 2;
   
   public static final String ID = "continuum_anchor";
   
   public ContinuumAnchor(){
      id = ID;
      name = "Continuum Anchor";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.RESPAWN_ANCHOR;
      block = new ContinuumAnchorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0f, 1200.0f).lightLevel(state -> ContinuumAnchorBlock.getLightLevel(state, 15)));
      item = new ContinuumAnchorItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.UNLOCK_EXOTIC_MATTER,ResearchTasks.ADVANCEMENT_CHARGE_RESPAWN_ANCHOR,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getDefaultMaxStackSize());
      setPrefStack(stack);
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
            ContinuumAnchor.addChunk(serverWorld,new ChunkPos(pos.x+i,pos.z+j));
         }
      }
   }
   
   public static void addChunk(ServerLevel world, ChunkPos chunk){
      world.getChunkSource().addTicketWithRadius(ArcanaRegistry.ANCHOR_TICKET_TYPE, chunk, 2);
      Long2IntOpenHashMap m = ANCHOR_CHUNKS.computeIfAbsent(world, w -> new Long2IntOpenHashMap());
      m.put(chunk.toLong(), 40);
   }
   
   public static boolean isChunkLoaded(ServerLevel serverWorld, ChunkPos chunk){
      return ANCHOR_CHUNKS.getOrDefault(serverWorld,new Long2IntOpenHashMap()).containsKey(chunk.toLong());
   }
   
   public static void updateLoadedChunks(MinecraftServer server){
      for (ServerLevel world : server.getAllLevels()){
         Long2IntOpenHashMap chunks = ANCHOR_CHUNKS.get(world);
         if (chunks == null || chunks.isEmpty()) continue;
         boolean nonEmpty = false;
         ObjectIterator<Long2IntMap.Entry> it = chunks.long2IntEntrySet().fastIterator();
         while (it.hasNext()){
            Long2IntMap.Entry e = it.next();
            int t = e.getIntValue();
            if (t <= 1){
               it.remove();
            }else{
               e.setValue(t - 1);
               nonEmpty = true;
            }
         }
         if (nonEmpty) world.resetEmptyTime();
      }
   }
   
   public static void initLoadedChunks(MinecraftServer minecraftServer){
      for(ServerLevel serverWorld : minecraftServer.getAllLevels()){
         for(BlockPos anchorPos : ACTIVE_ANCHORS.get(serverWorld).getAnchors()){
            ChunkPos chunkPos = new ChunkPos(anchorPos);
            loadChunks(serverWorld,chunkPos);
         }
      }
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing study casing that can channel Arcana, I have made additional efforts to reinforce").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nthis chassis against dimensional shear. By combining all known techniques of manipulating dimensional energy, I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nWhen fed with Exotic Matter, the Anchor chunk loads a 5x5 chunk area and produces lazy chunks in the 7x7 ring around it. \nIt is able to stimulate mobs such that they despawn slower when a player isn’t nearby,").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("  Continuum Anchor").withStyle(ChatFormatting.BOLD).withColor(ArcanaColors.BETTER_DARK_BLUE), Component.literal("\nwhile also inducing new spawns and activating spawners.\nThe Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. Additional fuel may also be added while still in use.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHERITE_INGOT,2);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient c = new ArcanaIngredient(Items.RESPAWN_ANCHOR,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient h = new ArcanaIngredient(Items.ENDER_EYE,16);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withAnvil().withCore());
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
   
   public class ContinuumAnchorBlock extends ArcanaPolymerBlockEntity {
      public static final IntegerProperty CHARGES = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
      public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
      
      public ContinuumAnchorBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.RESPAWN_ANCHOR.defaultBlockState().setValue(CHARGES,state.getValue(CHARGES));
      }
      
      @Nullable
      public static ContinuumAnchorBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof ContinuumAnchorBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof ContinuumAnchorBlockEntity anchor ? anchor : null;
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
         return this.defaultBlockState().setValue(ACTIVE,false).setValue(CHARGES,0);
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(CHARGES, ACTIVE);
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
            initializeArcanaBlock(stack,anchor);
            
            if(placer instanceof ServerPlayer player){
               player.displayClientMessage(Component.literal("Placing the Continuum Anchor sends a ripple across spacetime.").withStyle(ChatFormatting.DARK_BLUE), true);
               SoundUtils.playSound(world, pos, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 5, .8f);
            }
         }
      }
      
      @Override
      protected boolean isPathfindable(BlockState state, PathComputationType type){
         return false;
      }
      
      public static int getLightLevel(BlockState state, int maxLevel){
         return Mth.floor((float)(state.getValue(CHARGES)) / 4.0f * (float)maxLevel);
      }
   }
}

