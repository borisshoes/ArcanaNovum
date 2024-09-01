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
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
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
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TwilightAnvil extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "twilight_anvil";
   
   private Multiblock multiblock;
   
   public TwilightAnvil(){
      id = ID;
      name = "Twilight Anvil";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ANVIL;
      block = new TwilightAnvilBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(5.0f, 1200.0f).sounds(BlockSoundGroup.ANVIL));
      item = new TwilightAnvilItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.literal("Twilight Anvil").formatted(Formatting.BOLD,Formatting.BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_ANVIL,ResearchTasks.UNLOCK_STARLIGHT_FORGE, ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
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
            .append(Text.literal("An improved ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("anvil ").formatted(Formatting.BLUE))
            .append(Text.literal("with ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("no XP limit").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Anvil ").formatted(Formatting.BLUE))
            .append(Text.literal("can be used to rename ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(", and apply ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("augments").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("Anvil ").formatted(Formatting.BLUE))
            .append(Text.literal("can also be used to combine ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("enhanced equipment").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_AQUA)));
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
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.DIAMOND,4);
      ArcanaIngredient b = new ArcanaIngredient(Items.NETHERITE_SCRAP,1);
      ArcanaIngredient c = new ArcanaIngredient(Items.GLOWSTONE_DUST,16);
      ArcanaIngredient g = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE,8);
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      ArcanaIngredient m = new ArcanaIngredient(Items.ANVIL,8);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Twilight Anvil\n\nRarity: Empowered\n\nAnvils made of Iron have their limits. They don't interact with Arcana, and they are less durable than the diamond and netherite equipment used on them. An anvil reinforced with netherite and infused").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Twilight Anvil\n\nwith Arcana will have no such weaknesses.\n\nThe Anvil can act as a normal anvil, with its XP limit removed as well as allowing for the Augmentation and renaming of Arcana Items. It also is able to combine enhanced items from the Forge.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class TwilightAnvilItem extends ArcanaPolymerBlockItem {
      public TwilightAnvilItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class TwilightAnvilBlock extends ArcanaPolymerBlockEntity {
      public static final DirectionProperty HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public TwilightAnvilBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return Blocks.ANVIL.getDefaultState().with(HORIZONTAL_FACING,state.get(HORIZONTAL_FACING));
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().rotateYClockwise());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation) {
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror) {
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.TWILIGHT_ANVIL_BLOCK_ENTITY, TwilightAnvilBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         TwilightAnvilBlockEntity anvil = (TwilightAnvilBlockEntity) world.getBlockEntity(pos);
         if(anvil != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(anvil.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Anvil must be within the range of an active Starlight Forge"));
                  }else{
                     anvil.openGui(0, player,"");
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(anvil.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Nullable
      public static TwilightAnvilBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof TwilightAnvilBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof TwilightAnvilBlockEntity anvil ? anvil : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new TwilightAnvilBlockEntity(pos, state);
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof TwilightAnvilBlockEntity anvil) {
            initializeArcanaBlock(stack,anvil);
         }
      }
   }
}

