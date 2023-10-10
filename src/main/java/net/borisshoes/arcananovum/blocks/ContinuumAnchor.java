package net.borisshoes.arcananovum.blocks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Credit to xZarex for some of the Chunk Loading mixin code
public class ContinuumAnchor extends MagicBlock {
   public static final ChunkTicketType<ChunkPos> TICKET_TYPE = ChunkTicketType.create("ArcanaNovum_Anchor", Comparator.comparingLong(ChunkPos::toLong));
   
   public ContinuumAnchor(){
      id = "continuum_anchor";
      name = "Continuum Anchor";
      rarity = MagicRarity.LEGENDARY;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.LEGENDARY, ArcaneTome.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.RESPAWN_ANCHOR;
      block = new ContinuumAnchorBlock(FabricBlockSettings.create().mapColor(MapColor.BLACK).requiresTool().strength(50.0f, 1200.0f).luminance(state -> ContinuumAnchorBlock.getLightLevel(state, 15)));
      item = new ContinuumAnchorItem(block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Continuum Anchor\",\"italic\":false,\"bold\":true,\"color\":\"dark_blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"anchor\",\"color\":\"dark_blue\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"has the extraordinary ability to manipulate \"},{\"text\":\"spacetime\",\"color\":\"dark_purple\"},{\"text\":\".\",\"color\":\"dark_gray\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"It just needs the \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"right type\",\"color\":\"gray\"},{\"text\":\" of\"},{\"text\":\" \",\"color\":\"dark_purple\"},{\"text\":\"fuel\",\"color\":\"gold\"},{\"text\":\"...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"continuum anchor\",\"color\":\"dark_blue\"},{\"text\":\" consumes \"},{\"text\":\"exotic matter\",\"color\":\"blue\"},{\"text\":\" to \"},{\"text\":\"chunk load\",\"color\":\"aqua\"},{\"text\":\" a \"},{\"text\":\"5x5 area\",\"color\":\"dark_green\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"dark_gray\"},{\"text\":\"area \",\"color\":\"dark_green\"},{\"text\":\"also receives \"},{\"text\":\"random ticks\",\"color\":\"blue\"},{\"text\":\" and keeps \"},{\"text\":\"mobs \",\"color\":\"gray\"},{\"text\":\"loaded\",\"color\":\"aqua\"},{\"text\":\".\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Legendary\",\"italic\":false,\"color\":\"gold\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   public static void addChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().addTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static void removeChunk(ServerWorld world, ChunkPos chunk){
      world.getChunkManager().threadedAnvilChunkStorage.getTicketManager().removeTicket(ContinuumAnchor.TICKET_TYPE,chunk,2,chunk);
   }
   
   public static List<ChunkPos> getLoadedChunks(ServerWorld serverWorld){
      ArrayList<ChunkPos> chunks = new ArrayList<>();
      
      for(Pair<ServerWorld, ChunkPos> pair : Arcananovum.ANCHOR_CHUNKS){
         if(!serverWorld.getRegistryKey().getValue().equals(pair.getLeft().getRegistryKey().getValue())) continue;
         chunks.add(pair.getRight());
      }
      return chunks;
   }
   
   public static boolean isChunkLoaded(ServerWorld serverWorld, ChunkPos chunk){
      List<ChunkPos> chunks = getLoadedChunks(serverWorld);
      for(ChunkPos chunkPos : chunks){
         if(chunkPos.toLong() == chunk.toLong()) return true;
      }
      return false;
   }
   
   public static void initLoadedChunks(MinecraftServer minecraftServer){
      for(ServerWorld serverWorld : minecraftServer.getWorlds()){
         //log("Initializing chunks in "+serverWorld.getRegistryKey().getValue().toString());
         List<ChunkPos> chunks = getLoadedChunks(serverWorld);
         for(ChunkPos chunk : chunks){
            addChunk(serverWorld,chunk);
         }
      }
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Continuum Anchor\\n\\nRarity: Legendary\\n\\nExotic Matter has given useful insight into warping spacetime. On top of being more practiced in constructing sturdy casings that can withstand the flow of Arcana, I have made additional efforts to \"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nreinforce this chassis against dimensional shear.\\nBy combining all known techniques of manipulating dimensional energy I believe I can cause a section of space to be locked in time so that the world cannot be unloaded.\"}");
      list.add("{\"text\":\"   Continuum Anchor\\n\\nWhen fed with Exotic Matter the Anchor chunk loads a 5x5 chunk area with Mob Spawning & Random Ticks. The Anchor can be turned off with a redstone signal and its fuel can be removed by an empty hand. It can be refueled in use.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient s = new MagicItemIngredient(Items.NETHER_STAR,16,null);
      MagicItemIngredient p = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient o = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient a = new MagicItemIngredient(Items.RESPAWN_ANCHOR,32,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,64,null);
      MagicItemIngredient n = new MagicItemIngredient(Items.NETHERITE_INGOT,4,null);
      
      MagicItemIngredient[][] ingredients = {
            {o,p,n,p,o},
            {p,e,a,e,p},
            {n,a,s,a,n},
            {p,e,a,e,p},
            {o,p,n,p,o}};
      return new MagicItemRecipe(ingredients);
   }
   
   public class ContinuumAnchorItem extends MagicPolymerBlockItem {
      public ContinuumAnchorItem(Block block, Settings settings){
         super(getThis(), block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class ContinuumAnchorBlock extends MagicPolymerBlockEntity {
      public static final IntProperty CHARGES = Properties.CHARGES;
      public static final BooleanProperty ACTIVE = BooleanProperty.of("active");
      
      public ContinuumAnchorBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.RESPAWN_ANCHOR;
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(CHARGES,state.get(CHARGES));
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
         return checkType(type, ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, ContinuumAnchorBlockEntity::ticker);
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
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
         ItemStack stack = player.getStackInHand(hand);
         ContinuumAnchorBlockEntity anchor = (ContinuumAnchorBlockEntity) world.getBlockEntity(pos);
         if(anchor != null && anchor.interact(player, stack)) return ActionResult.SUCCESS;
         return ActionResult.PASS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof ContinuumAnchorBlockEntity anchor) {
            if (!player.isCreative() && player.canHarvest(world.getBlockState(pos)) && world instanceof ServerWorld serverWorld) {
               if (!world.isClient) {
                  dropBlockItem(world,pos,state,player,anchor);
                  
                  int fuel = anchor.getFuel();
                  if(fuel > 0){
                     ItemScatterer.spawn(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, anchor.getFuelStack());
                  }
                  
                  ChunkPos chunkPos = new ChunkPos(pos);
                  for(int i = -ContinuumAnchorBlockEntity.RANGE; i <= ContinuumAnchorBlockEntity.RANGE; i++){
                     for(int j = -ContinuumAnchorBlockEntity.RANGE; j <= ContinuumAnchorBlockEntity.RANGE; j++){
                        ContinuumAnchor.removeChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
                     }
                  }
               }
               Arcananovum.removeActiveAnchor(serverWorld, pos);
            }
            
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof ContinuumAnchorBlockEntity anchor) {
            initializeMagicBlock(stack,anchor);
            
            player.sendMessage(Text.literal("Placing the Continuum Anchor sends a ripple across spacetime.").formatted(Formatting.DARK_BLUE),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, 5,.8f);
         }
      }
      
      @Override
      public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
         return false;
      }
      
      public static int getLightLevel(BlockState state, int maxLevel) {
         return MathHelper.floor((float)(state.get(CHARGES)) / 4.0f * (float)maxLevel);
      }
   }
}
