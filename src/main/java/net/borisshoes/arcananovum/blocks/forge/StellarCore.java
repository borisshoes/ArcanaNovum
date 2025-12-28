package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
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
   public static Map<Item, ItemStack> MOLTEN_CORE_ITEMS = Map.ofEntries(
         entry(Items.RAW_IRON,new ItemStack(Items.IRON_INGOT,2)),
         entry(Items.RAW_COPPER,new ItemStack(Items.COPPER_INGOT,2)),
         entry(Items.RAW_GOLD,new ItemStack(Items.GOLD_INGOT,2)),
         entry(Items.RAW_IRON_BLOCK,new ItemStack(Items.IRON_BLOCK,2)),
         entry(Items.RAW_COPPER_BLOCK,new ItemStack(Items.COPPER_BLOCK,2)),
         entry(Items.RAW_GOLD_BLOCK,new ItemStack(Items.GOLD_BLOCK,2)),
         entry(Items.NETHER_GOLD_ORE,new ItemStack(Items.GOLD_INGOT,2)),
         entry(Items.SAND,new ItemStack(Items.GLASS,2)),
         entry(Items.RED_SAND,new ItemStack(Items.GLASS,2)),
         entry(Items.ANCIENT_DEBRIS,new ItemStack(Items.NETHERITE_SCRAP,1))
   );
   
   public StellarCore(){
      id = ID;
      name = "Stellar Core";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.BLAST_FURNACE;
      block = new StellarCoreBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(3.5f,1200.0f).lightLevel(StellarCoreBlock::getLightLevel).sound(SoundType.METAL));
      item = new StellarCoreItem(block);
      displayName = Component.translatableWithFallback("item."+MOD_ID+"."+ID,name).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD);
      researchTasks = new ResourceKey[]{ResearchTasks.UNLOCK_TWILIGHT_ANVIL,ResearchTasks.UNLOCK_STARLIGHT_FORGE,ResearchTasks.OBTAIN_BLAST_FURNACE,ResearchTasks.OBTAIN_NETHERITE_INGOT};
      
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
            .append(Component.literal("Forge Structure").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(" addon to the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The core of a ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("star ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("contained to ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("melt ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("even the ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("strongest ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("materials.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("The ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Stellar Core").withStyle(ChatFormatting.GOLD))
            .append(Component.literal(" lets you ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("melt ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("down ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("equipment ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("for salvage.").withStyle(ChatFormatting.YELLOW)));
      lore.add(Component.literal("")
            .append(Component.literal("Enchantments ").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.literal("on salvaged ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("equipment ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("become ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("Stardust").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(".").withStyle(ChatFormatting.YELLOW)));
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
      return new ArcanaRecipe(this, ingredients,new ForgeRequirement().withAnvil());
   }
   
   @Override
   public List<List<Component>> getBookLore(){
      List<List<Component>> list = new ArrayList<>();
      list.add(List.of(Component.literal("    Stellar Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nRarity: ").withStyle(ChatFormatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)), Component.literal("\nSo often I find my storage plagued with decrepit armor. A furnace is far too crude to extract any worthwhile material, and it can’t even get close to hot enough to melt Diamond or Netherite.").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Stellar Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nWhat I need is the power of the sun! A cleverly enchanted alloy of Obsidian, Blackstone and Netherite should be able to withstand the temperatures to melt even Netherite coated Diamond, allowing me to extract a large \n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Stellar Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nportion of the material.\n\nInterestingly enough, the Arcane Sun I have created interacts with the enchantments bound to equipment and produces a glittery dust that is nearly indestructible \n").withStyle(ChatFormatting.BLACK)));
      list.add(List.of(Component.literal("    Stellar Core").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), Component.literal("\nand contains trace amounts of Arcana. This might be the key to making better equipment.").withStyle(ChatFormatting.BLACK)));
      return list;
   }
   
   public class StellarCoreItem extends ArcanaPolymerBlockItem {
      public StellarCoreItem(Block block){
         super(getThis(),block, getArcanaItemComponents());
      }
      
      @Override
      public ItemStack getDefaultInstance(){
         return prefItem;
      }
   }
   
   public class StellarCoreBlock extends ArcanaPolymerBlockEntity {
      public static final BooleanProperty LIT = BlockStateProperties.LIT;
      public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
      
      public StellarCoreBlock(BlockBehaviour.Properties settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.BLAST_FURNACE.defaultBlockState().setValue(LIT,state.getValue(LIT)).setValue(HORIZONTAL_FACING,state.getValue(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockPlaceContext ctx){
         return this.defaultBlockState().setValue(LIT,false).setValue(HORIZONTAL_FACING,ctx.getHorizontalDirection().getOpposite());
      }
      
      @Override
      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateManager){
         stateManager.add(LIT,HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, Rotation rotation){
         return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, Mirror mirror){
         return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
      }
      
      
      @Nullable
      public static StellarCoreBlockEntity getEntity(Level world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof StellarCoreBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof StellarCoreBlockEntity core ? core : null;
      }
      
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
         return new StellarCoreBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
         return createTickerHelper(type, ArcanaRegistry.STELLAR_CORE_BLOCK_ENTITY, StellarCoreBlockEntity::ticker);
      }
      
      @Override
      public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player playerEntity, BlockHitResult hit){
         StellarCoreBlockEntity core = (StellarCoreBlockEntity) world.getBlockEntity(pos);
         if(core != null){
            if(playerEntity instanceof ServerPlayer player){
               if(core.isAssembled()){
                  if(StarlightForge.findActiveForge(player.level(),pos) == null){
                     player.sendSystemMessage(Component.literal("The Core must be within the range of an active Starlight Forge"));
                  }else{
                     core.openGui(player);
                  }
               }else{
                  player.sendSystemMessage(Component.literal("Multiblock not constructed."));
                  multiblock.displayStructure(core.getMultiblockCheck(),player);
               }
            }
         }
         return InteractionResult.SUCCESS_SERVER;
      }
      
      @Override
      public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof StellarCoreBlockEntity core){
            initializeArcanaBlock(stack,core);
         }
      }
      
      public static int getLightLevel(BlockState state){
         return state.getValue(BlockStateProperties.LIT) ? 13 : 0;
      }
   }
}

