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
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class MidnightEnchanter extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "midnight_enchanter";
   
   private Multiblock multiblock;
   
   public MidnightEnchanter(){
      id = ID;
      name = "Midnight Enchanter";
      rarity = ArcanaRarity.EMPOWERED;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EMPOWERED, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.FORGE};
      itemVersion = 0;
      vanillaItem = Items.ENCHANTING_TABLE;
      block = new MidnightEnchanterBlock(AbstractBlock.Settings.create().mapColor(MapColor.RED).strength(5.0f, 1200.0f).requiresTool().luminance(state -> 7));
      item = new MidnightEnchanterItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.ADVANCEMENT_ENCHANT_ITEM,ResearchTasks.OBTAIN_BOTTLES_OF_ENCHANTING,ResearchTasks.ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN,ResearchTasks.UNLOCK_STARLIGHT_FORGE};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A ").formatted(Formatting.BLUE))
            .append(Text.literal("Forge Structure").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" addon to the ").formatted(Formatting.BLUE))
            .append(Text.literal("Starlight Forge").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("Normal ").formatted(Formatting.BLUE))
            .append(Text.literal("Enchanting Tables").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" are ").formatted(Formatting.BLUE))
            .append(Text.literal("unpredictable ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.BLUE))
            .append(Text.literal("inconsistent").formatted(Formatting.AQUA))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.BLUE))
            .append(Text.literal("Table ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("not only enables ").formatted(Formatting.BLUE))
            .append(Text.literal("precise control ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("of ").formatted(Formatting.BLUE))
            .append(Text.literal("enchantments").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("...").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("It also allows for ").formatted(Formatting.BLUE))
            .append(Text.literal("enchantments ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("to be ").formatted(Formatting.BLUE))
            .append(Text.literal("removed ").formatted(Formatting.AQUA))
            .append(Text.literal("and ").formatted(Formatting.BLUE))
            .append(Text.literal("placed ").formatted(Formatting.AQUA))
            .append(Text.literal("onto ").formatted(Formatting.BLUE))
            .append(Text.literal("books").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
      lore.add(Text.literal("")
            .append(Text.literal("Enchantments ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("can also be ").formatted(Formatting.BLUE))
            .append(Text.literal("broken down").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" into ").formatted(Formatting.BLUE))
            .append(Text.literal("Nebulous Essence").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal(".").formatted(Formatting.BLUE)));
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
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,24);
      ArcanaIngredient b = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.PROTECTION),4));
      ArcanaIngredient c = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.THORNS),3));
      ArcanaIngredient d = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.BINDING_CURSE),1));
      ArcanaIngredient f = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.EFFICIENCY),5));
      ArcanaIngredient g = new ArcanaIngredient(Items.EXPERIENCE_BOTTLE,16);
      ArcanaIngredient h = new ArcanaIngredient(Items.LAPIS_BLOCK,12);
      ArcanaIngredient j = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.SWIFT_SNEAK),3));
      ArcanaIngredient k = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.UNBREAKING),3));
      ArcanaIngredient m = new ArcanaIngredient(Items.ENCHANTING_TABLE,16);
      ArcanaIngredient o = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.SOUL_SPEED),3));
      ArcanaIngredient p = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.MENDING),1));
      ArcanaIngredient t = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.FEATHER_FALLING),4));
      ArcanaIngredient v = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.SHARPNESS),5));
      ArcanaIngredient w = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.LOOTING),3));
      ArcanaIngredient x = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.VANISHING_CURSE),1));
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {f,g,h,g,j},
            {k,h,m,h,o},
            {p,g,h,g,t},
            {a,v,w,x,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Midnight Enchanter\n\nRarity: Exotic\n\nAn enchanting table is an old, but not ancient, design. It only scratches the surface of how Arcana can be bound to equipment, and relies too much on the random fluctuations of the environment.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Midnight Enchanter\n\nIf my predictive equations are correct, I should be able to cancel out the environmental noise in the Enchantment matrix and reduce Enchantment Arcana to a pure form, afterwhich I can make it take any shape of my choosing. ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Midnight Enchanter\n\nThe Enchanter allows for disenchanting of items to gain Nebulous Essence, which can then be spent to choose exact enchantments and levels to place on items. The Enchanter also gives access to normally unavailable enchantments.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class MidnightEnchanterItem extends ArcanaPolymerBlockItem {
      public MidnightEnchanterItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class MidnightEnchanterBlock extends ArcanaPolymerBlockEntity {
      public MidnightEnchanterBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state) {
         return Blocks.ENCHANTING_TABLE.getDefaultState();
      }
      
      @Nullable
      public static MidnightEnchanterBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof MidnightEnchanterBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof MidnightEnchanterBlockEntity enchanter ? enchanter : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new MidnightEnchanterBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.MIDNIGHT_ENCHANTER_BLOCK_ENTITY, MidnightEnchanterBlockEntity::ticker);
      }
      
      @Override
      public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         MidnightEnchanterBlockEntity enchanter = (MidnightEnchanterBlockEntity) world.getBlockEntity(pos);
         if(enchanter != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(enchanter.isAssembled()){
                  if(StarlightForge.findActiveForge(player.getServerWorld(),pos) == null){
                     player.sendMessage(Text.literal("The Enchanter must be within the range of an active Starlight Forge"));
                  }else{
                     if(enchanter.hasBooks()){
                        enchanter.openGui(player);
                     }else{
                        player.sendMessage(Text.literal("The Enchanter needs at least 20 bookshelves nearby"));
                     }
                  }
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(enchanter.getMultiblockCheck());
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof MidnightEnchanterBlockEntity enchanter) {
            initializeArcanaBlock(stack,enchanter);
         }
      }
   }
}

