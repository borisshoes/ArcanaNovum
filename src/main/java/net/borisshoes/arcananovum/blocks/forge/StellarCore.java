package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerBlockItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.Map.entry;

public class StellarCore extends MagicBlock implements MultiblockCore {
   
   private Multiblock multiblock;
   public static Map<Item,Item> MOLTEN_CORE_ITEMS = Map.ofEntries(
         entry(Items.RAW_IRON,Items.IRON_INGOT),
         entry(Items.RAW_COPPER,Items.COPPER_INGOT),
         entry(Items.RAW_GOLD,Items.GOLD_INGOT),
         entry(Items.RAW_IRON_BLOCK,Items.IRON_BLOCK),
         entry(Items.RAW_COPPER_BLOCK,Items.COPPER_BLOCK),
         entry(Items.RAW_GOLD_BLOCK,Items.GOLD_BLOCK)
   );
   
   public StellarCore(){
      id = "stellar_core";
      name = "Stellar Core";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.BLOCKS, ArcaneTome.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.BLAST_FURNACE;
      this.block = new StellarCoreBlock(FabricBlockSettings.create().requiresTool().strength(3.5f,1200.0f).luminance(StellarCoreBlock::getLightLevel).sounds(BlockSoundGroup.METAL));
      item = new StellarCoreItem(block,new FabricItemSettings().maxCount(1).fireproof());
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Stellar Core\",\"italic\":false,\"color\":\"gold\",\"bold\":true}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      buildItemLore(stack, ArcanaNovum.SERVER);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      stack.setNbt(prefNBT);
      prefItem = stack;
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"A \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Forge Structure\",\"color\":\"light_purple\"},{\"text\":\" addon to the \"},{\"text\":\"Starlight Forge\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The core of a \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"star \",\"color\":\"gold\"},{\"text\":\"contained to \"},{\"text\":\"melt \",\"color\":\"gold\"},{\"text\":\"even the \"},{\"text\":\"strongest \",\"color\":\"aqua\"},{\"text\":\"materials.\",\"color\":\"yellow\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"The \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"Stellar Core\",\"color\":\"gold\"},{\"text\":\" lets you \"},{\"text\":\"melt \",\"color\":\"gold\"},{\"text\":\"down \"},{\"text\":\"equipment \",\"color\":\"aqua\"},{\"text\":\"for salvage.\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Enchantments \",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"on salvaged \",\"color\":\"yellow\"},{\"text\":\"equipment \",\"color\":\"aqua\"},{\"text\":\"become \",\"color\":\"yellow\"},{\"text\":\"Stardust\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"yellow\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      addForgeLore(loreList);
      return loreList;
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.NETHER_STAR,4,null);
      MagicItemIngredient b = new MagicItemIngredient(Items.OBSIDIAN,64,null);
      MagicItemIngredient c = new MagicItemIngredient(Items.CRYING_OBSIDIAN,64,null);
      MagicItemIngredient g = new MagicItemIngredient(Items.BLAZE_ROD,64,null);
      MagicItemIngredient h = new MagicItemIngredient(Items.NETHERITE_INGOT,2,null);
      MagicItemIngredient m = new MagicItemIngredient(Items.BLAST_FURNACE,64,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new MagicItemRecipe(ingredients, new ForgeRequirement().withAnvil());
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"      Stellar Core\\n\\nRarity: Exotic\\n\\nSo often I find my storage plagued with decrepid armor. A furnace is far too crude to extract any worthwhile material, and it can't even get close to hot enough to melt diamond or netherite.\"}");
      list.add("{\"text\":\"      Stellar Core\\n\\nWhat I need is the power of the sun!\\nA cleverly enchanted alloy of Obsidian, Blackstone and Netherite should be able to withstand the temperatures to melt even netherite coated diamond; allowing me to extract a large portion of material.\"}");
      list.add("{\"text\":\"      Stellar Core\\n\\nInterestingly enough, the Arcane Sun I have created interacts with the enchantments bound to equipment and produces a glittery dust that is nearly indestructible and emits trace amounts of Arcana.\\nPerhaps this can be used for something...\"}");
      return list;
   }
   
   public class StellarCoreItem extends MagicPolymerBlockItem {
      public StellarCoreItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StellarCoreBlock extends MagicPolymerBlockEntity {
      public static final BooleanProperty LIT = Properties.LIT;
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public StellarCoreBlock(Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return super.getPolymerBlockState(state).with(LIT,state.get(LIT)).with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(LIT,false).with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(LIT,HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation) {
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror) {
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public Block getPolymerBlock(BlockState state) {
         return Blocks.BLAST_FURNACE;
      }
      
      @Nullable
      public static StellarCoreBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof StellarCoreBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof StellarCoreBlockEntity core ? core : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new StellarCoreBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return checkType(type, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, StellarCoreBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, Hand hand, BlockHitResult hit){
         if(hand != Hand.MAIN_HAND) return ActionResult.PASS;
         StellarCoreBlockEntity core = (StellarCoreBlockEntity) world.getBlockEntity(pos);
         if(core != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(core.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Core must be within the range of an active Starlight Forge"));
                  }else{
                     core.openGui(player);
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(core.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
         if (world.getBlockEntity(pos) instanceof StellarCoreBlockEntity core) {
            dropBlockItem(world, pos, state, player, core);
            world.removeBlockEntity(pos);
         }
         
         world.removeBlock(pos, false);
         
         super.onBreak(world, pos, state, player);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof StellarCoreBlockEntity core) {
            initializeMagicBlock(stack,core);
         }
      }
      
      public static int getLightLevel(BlockState state) {
         return state.get(Properties.LIT) ? 13 : 0;
      }
   }
}
