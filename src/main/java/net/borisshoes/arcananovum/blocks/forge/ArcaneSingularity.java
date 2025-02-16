package net.borisshoes.arcananovum.blocks.forge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcaneSingularity extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "arcane_singularity";
   
   public static final String BOOKS_TAG = "books";
   public static final String SINGULARITY_TAG = "singularityId";
   
   private Multiblock multiblock;
   
   public ArcaneSingularity(){
      id = ID;
      name = "Arcane Singularity";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{ArcanaRarity.getTomeFilter(rarity), TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.LECTERN;
      block = new ArcaneSingularityBlock(AbstractBlock.Settings.create().strength(2.5f,1200.0f).sounds(BlockSoundGroup.WOOD));
      item = new ArcaneSingularityItem(this.block,addArcanaItemComponents(new Item.Settings().maxCount(1).fireproof()));
      displayName = Text.translatableWithFallback("item."+MOD_ID+"."+ID,name).formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE);
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.OBTAIN_STARDUST,ResearchTasks.OBTAIN_NEBULOUS_ESSENCE,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.UNLOCK_STARLIGHT_FORGE,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,BOOKS_TAG,new NbtList());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Forge Structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" addon to the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Starlight Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Storing ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("enchantments").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" on books is ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("crude").formatted(Formatting.GREEN))
            .append(Text.literal(" and ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("primitive.").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Condensing ").formatted(Formatting.GREEN))
            .append(Text.literal("their ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("essence ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("into a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("singularity ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("is much more ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("modern").formatted(Formatting.GREEN))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Allows for the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("storing of ").formatted(Formatting.GREEN))
            .append(Text.literal("enchantments").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" in a ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("virtual library.").formatted(Formatting.GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Works well").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" with the ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Midnight Enchanter").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      
      if(itemStack != null){
         int size = getListProperty(itemStack,BOOKS_TAG,NbtElement.COMPOUND_TYPE).size();
         if(size > 0){
            lore.add(Text.literal(""));
            lore.add(Text.literal("")
                  .append(Text.literal("Contains ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(""+size).formatted(Formatting.GREEN))
                  .append(Text.literal(" Enchanted Books").formatted(Formatting.LIGHT_PURPLE)));
         }
      }
      
      addForgeLore(lore);
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtList targetsList = getListProperty(stack,BOOKS_TAG, NbtElement.COMPOUND_TYPE);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,BOOKS_TAG,targetsList);
      return buildItemLore(newStack,server);
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
      return new Vec3i(-1,-1,-2);
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient b = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENDER_EYE,24);
      ArcanaIngredient g = new ArcanaIngredient(ArcanaRegistry.NEBULOUS_ESSENCE,32);
      ArcanaIngredient h = new ArcanaIngredient(ArcanaRegistry.STARDUST,32);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHER_STAR,4);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withEnchanter().withCore().withAnvil());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("Arcane Singularity").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nRarity: ").formatted(Formatting.BLACK).append(ArcanaRarity.getColoredLabel(getRarity(),false)),Text.literal("\nThe Midnight Enchanter has proven more useful than I imagined. Now I have more Enchanted Books than I can possibly store in a library. I need a new method of book-keeping.\nBy condensing raw ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Arcane Singularity").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nNebulous Essence down over and over, it forms a self-sustaining singularity. An Arcane black hole. Containing it was no easy feat, but now I have a massive storage space for my books…\n\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("Arcane Singularity").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),Text.literal("\nThe Singularity is a single purpose, high density storage unit for Enchanted Books. Books can be sorted and filtered, and can be extracted at any time. \nBooks are bound to the Singularity, so they are kept with the block when moved.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class ArcaneSingularityItem extends ArcanaPolymerBlockItem {
      
      public ArcaneSingularityItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class ArcaneSingularityBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public ArcaneSingularityBlock(AbstractBlock.Settings settings){
         super(getThis(), settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state, PacketContext context){
         return Blocks.LECTERN.getDefaultState().with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx){
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager){
         stateManager.add(HORIZONTAL_FACING);
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
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
         return validateTicker(type, ArcanaRegistry.ARCANE_SINGULARITY_BLOCK_ENTITY, ArcaneSingularityBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         ArcaneSingularityBlockEntity singularity = (ArcaneSingularityBlockEntity) world.getBlockEntity(pos);
         if(singularity != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(singularity.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     singularity.openGui(player);
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(singularity.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS_SERVER;
      }
      
      @Nullable
      public static ArcaneSingularityBlockEntity getEntity(World world, BlockPos pos){
         BlockState state = world.getBlockState(pos);
         if(!(state.getBlock() instanceof ArcaneSingularityBlock)){
            return null;
         }
         return world.getBlockEntity(pos) instanceof ArcaneSingularityBlockEntity singularity ? singularity : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
         return new ArcaneSingularityBlockEntity(pos, state);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
         BlockEntity entity = world.getBlockEntity(pos);
         if(entity instanceof ArcaneSingularityBlockEntity singularity){
            initializeArcanaBlock(stack,singularity);
            singularity.initializeBooks(getListProperty(stack,BOOKS_TAG,NbtElement.COMPOUND_TYPE),world.getRegistryManager());
         }
      }
   }
}

