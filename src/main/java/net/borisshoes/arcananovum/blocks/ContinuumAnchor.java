package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.ANCHOR_CHUNKS;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;

// Credit to xZarex for some of the Chunk Loading mixin code
public class ContinuumAnchor extends ArcanaBlock {
   public static final String ID = "continuum_anchor";
   
   public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("ArcanaNovum_Anchor", Comparator.comparingLong(ChunkPos::toLong));
   
   public ContinuumAnchor(){
      id = ID;
      name = "Continuum Anchor";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.RESPAWN_ANCHOR;
      block = new ContinuumAnchorBlock(AbstractBlock.Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(50.0f, 1200.0f).luminance(state -> ContinuumAnchorBlock.getLightLevel(state, 15)));
      item = new ContinuumAnchorItem(block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.UNLOCK_EXOTIC_MATTER,ResearchTasks.ADVANCEMENT_CHARGE_RESPAWN_ANCHOR,ResearchTasks.UNLOCK_STELLAR_CORE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("anchor").formatted(Formatting.DARK_BLUE))
            .append(Text.literal("has the extraordinary ability to manipulate ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("spacetime").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("It just needs the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("right type").formatted(Formatting.GRAY))
            .append(Text.literal(" of ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("fuel").formatted(Formatting.GOLD))
            .append(Text.literal("...").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("continuum anchor").formatted(Formatting.DARK_BLUE))
            .append(Text.literal(" consumes ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("exotic matter").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("chunk load").formatted(Formatting.AQUA))
            .append(Text.literal(" a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("5x5 area").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("area ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("also receives ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("random ticks").formatted(Formatting.BLUE))
            .append(Text.literal(" and keeps ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("mobs ").formatted(Formatting.GRAY))
            .append(Text.literal("loaded").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   public static void addChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().addTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static void removeChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().removeTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static List<ChunkPos> getLoadedChunks(ServerWorld serverWorld){
      return ArcanaNovum.ANCHOR_CHUNKS.get(serverWorld);
   }
   
   public static boolean isChunkLoaded(ServerWorld serverWorld, ChunkPos chunk){
      List<ChunkPos> chunks = getLoadedChunks(serverWorld);
      for(ChunkPos chunkPos : chunks){
         if(chunkPos.toLong() == chunk.toLong()) return true;
      }
      return false;
   }
   
   public static void updateLoadedChunks(MinecraftServer server){
      ANCHOR_CHUNKS.clear();
      for(ServerWorld world : server.getWorlds()){
         ArrayList<ChunkPos> chunkList = new ArrayList<>();
         for(BlockPos anchorPos : ACTIVE_ANCHORS.get(world).getAnchors()){
            ChunkPos chunkPos = new ChunkPos(anchorPos);
            for(int i = -ContinuumAnchorBlockEntity.RANGE; i <= ContinuumAnchorBlockEntity.RANGE; i++){
               for(int j = -ContinuumAnchorBlockEntity.RANGE; j <= ContinuumAnchorBlockEntity.RANGE; j++){
                  chunkList.add(new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
         }
         ANCHOR_CHUNKS.put(world,chunkList);
      }
   }
   
   public static void initLoadedChunks(MinecraftServer minecraftServer){
      updateLoadedChunks(minecraftServer);
      for(ServerWorld serverWorld : minecraftServer.getWorlds()){
         List<ChunkPos> chunks = getLoadedChunks(serverWorld);
         for(ChunkPos chunk : chunks){
            addChunk(serverWorld,chunk);
         }
      }
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Continuum Anchor\n\nRarity: Sovereign\n\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing sturdy casings that can withstand the flow of Arcana, I have made additional efforts to ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Continuum Anchor\n\nreinforce this chassis against dimensional shear.\nBy combining all known techniques of manipulating dimensional energy I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Continuum Anchor\n\nWhen fed with Exotic Matter the Anchor chunk loads a 5x5 chunk area with Mob Spawning & Random Ticks. The Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. It can be refueled in use.").formatted(Formatting.BLACK)));
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
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore());
   }
   
   public class ContinuumAnchorItem extends ArcanaPolymerBlockItem {
      public ContinuumAnchorItem(Block block, Settings settings){
         super(getThis(), block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class ContinuumAnchorBlock extends ArcanaPolymerBlockEntity {
      public static final IntProperty CHARGES = Properties.CHARGES;
      public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
      
      public ContinuumAnchorBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return Blocks.RESPAWN_ANCHOR.getDefaultState().with(CHARGES,state.get(CHARGES));
      }
      
      @Nullable
      public static ContinuumAnchorBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof ContinuumAnchorBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof ContinuumAnchorBlockEntity anchor ? anchor : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new ContinuumAnchorBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, ContinuumAnchorBlockEntity::ticker);
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(ACTIVE,false).with(CHARGES,0);
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(CHARGES, ACTIVE);
      }
      
      @Override
      protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
         ContinuumAnchorBlockEntity anchor = (ContinuumAnchorBlockEntity) world.getBlockEntity(pos);
         if(anchor != null && anchor.interact(player, stack)) return ItemActionResult.success(true);
         return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
         ContinuumAnchorBlockEntity anchor = (ContinuumAnchorBlockEntity) world.getBlockEntity(pos);
         if(anchor != null && anchor.interact(player, ItemStack.EMPTY)) return ActionResult.SUCCESS;
         return ActionResult.PASS;
      }
      
      
      @Override
      public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
         if (state.isOf(newState.getBlock())) {
            return;
         }
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof ContinuumAnchorBlockEntity anchor && world instanceof ServerWorld serverWorld) {
            ChunkPos chunkPos = new ChunkPos(pos);
            for(int i = -ContinuumAnchorBlockEntity.RANGE; i <= ContinuumAnchorBlockEntity.RANGE; i++){
               for(int j = -ContinuumAnchorBlockEntity.RANGE; j <= ContinuumAnchorBlockEntity.RANGE; j++){
                  ContinuumAnchor.removeChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
            
            ArcanaNovum.removeActiveAnchor(serverWorld, pos);
         }
         ItemScatterer.onStateReplaced(state, newState, world, pos);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof ContinuumAnchorBlockEntity anchor) {
            initializeArcanaBlock(stack,anchor);
            
            player.sendMessage(Text.literal("Placing the Continuum Anchor sends a ripple across spacetime.").formatted(Formatting.DARK_BLUE),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, 5,.8f);
         }
      }
      
      @Override
      protected boolean canPathfindThrough(BlockState state, NavigationType type){
         return false;
      }
      
      public static int getLightLevel(BlockState state, int maxLevel) {
         return MathHelper.floor((float)(state.get(CHARGES)) / 4.0f * (float)maxLevel);
      }
   }
}

