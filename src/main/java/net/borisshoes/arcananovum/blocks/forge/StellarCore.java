package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.AbstractBlock;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class StellarCore extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "stellar_core";
   
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
      id = ID;
      name = "Stellar Core";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.BLAST_FURNACE;
      block = new StellarCoreBlock(AbstractBlock.Settings.create().requiresTool().strength(3.5f,1200.0f).luminance(StellarCoreBlock::getLightLevel).sounds(BlockSoundGroup.METAL));
      item = new StellarCoreItem(block,addArcanaItemComponents(new Item.Settings().maxCount(1).fireproof()));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.GOLD);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TWILIGHT_ANVIL,ResearchTasks.UNLOCK_STARLIGHT_FORGE,ResearchTasks.OBTAIN_BLAST_FURNACE,ResearchTasks.OBTAIN_NETHERITE_INGOT};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.YELLOW))
            .append(Text.literal("Forge Structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" addon to the ").formatted(Formatting.YELLOW))
            .append(Text.literal("Starlight Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("The core of a ").formatted(Formatting.YELLOW))
            .append(Text.literal("star ").formatted(Formatting.GOLD))
            .append(Text.literal("contained to ").formatted(Formatting.YELLOW))
            .append(Text.literal("melt ").formatted(Formatting.GOLD))
            .append(Text.literal("even the ").formatted(Formatting.YELLOW))
            .append(Text.literal("strongest ").formatted(Formatting.AQUA))
            .append(Text.literal("materials.").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.YELLOW))
            .append(Text.literal("Stellar Core").formatted(Formatting.GOLD))
            .append(Text.literal(" lets you ").formatted(Formatting.YELLOW))
            .append(Text.literal("melt ").formatted(Formatting.GOLD))
            .append(Text.literal("down ").formatted(Formatting.YELLOW))
            .append(Text.literal("equipment ").formatted(Formatting.AQUA))
            .append(Text.literal("for salvage.").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Enchantments ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("on salvaged ").formatted(Formatting.YELLOW))
            .append(Text.literal("equipment ").formatted(Formatting.AQUA))
            .append(Text.literal("become ").formatted(Formatting.YELLOW))
            .append(Text.literal("Stardust").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
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
      return new Vec3i(-2,-1,-4);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.CRYING_OBSIDIAN,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient g = new ArcanaIngredient(Items.BLAZE_POWDER,32);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient m = new ArcanaIngredient(Items.BLAST_FURNACE,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("    Stellar Core").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nSo often I find my storage plagued with decrepit armor. A furnace is far too crude to extract any worthwhile material, and it can’t even get close to hot enough to melt Diamond or Netherite.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Stellar Core").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nWhat I need is the power of the sun! A cleverly enchanted alloy of Obsidian, Blackstone and Netherite should be able to withstand the temperatures to melt even Netherite coated Diamond, allowing me to extract a large \n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Stellar Core").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nportion of the material.\n\nInterestingly enough, the Arcane Sun I have created interacts with the enchantments bound to equipment and produces a glittery dust that is nearly indestructible \n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("    Stellar Core").formatted(Formatting.GOLD,Formatting.BOLD),Text.literal("\nand contains trace amounts of Arcana. This might be the key to making better equipment.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class StellarCoreItem extends ArcanaPolymerBlockItem {
      public StellarCoreItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class StellarCoreBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty LIT = Properties.LIT;
      public static final EnumProperty<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public StellarCoreBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.BLAST_FURNACE.getDefaultState().with(LIT,state.get(LIT)).with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx){
         return this.getDefaultState().with(LIT,false).with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
         stateManager.add(LIT,HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation){
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror){
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      
      @Nullable
      public static StellarCoreBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof StellarCoreBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof StellarCoreBlockEntity core ? core : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new StellarCoreBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, StellarCoreBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
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
                  multiblock.displayStructure(core.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
         if(state.isOf(newState.getBlock())){
            return;
         }
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if(!(blockEntity instanceof ArcanaBlockEntity mbe)) return;
         ItemScatterer.onStateReplaced(state, newState, world, pos);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(placer instanceof ServerPlayerEntity player && entity instanceof StellarCoreBlockEntity core){
            initializeArcanaBlock(stack,core);
         }
      }
      
      public static int getLightLevel(BlockState state){
         return state.get(Properties.LIT) ? 13 : 0;
      }
   }
}

