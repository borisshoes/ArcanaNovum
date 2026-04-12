package net.borisshoes.arcananovum.gui.arcanesingularity;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.*;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArcaneSingularityGui extends PagedGui<ItemStack> {
   private final ArcaneSingularityBlockEntity blockEntity;
   private final int capacity;
   private final boolean accretion;
   private ItemStack selected = ItemStack.EMPTY;
   
   public ArcaneSingularityGui(ServerPlayer player, ArcaneSingularityBlockEntity blockEntity, int capacity){
      super(MenuType.GENERIC_9x6, player, new ArrayList<>(blockEntity.getBooks()));
      this.blockEntity = blockEntity;
      this.capacity = capacity;
      this.accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION) >= 1;
      setTitle(Component.literal("Arcane Singularity"));
      
      blankItem(GuiElementBuilder.from(GraphicalItem.with(ArcanaRegistry.GAS)).setName(Component.empty()).hideTooltip());
      
      curFilter(BookFilter.NONE);
      curSort(BookSort.HIGHEST_LEVEL);
      
      itemElemBuilder((item, index) -> {
         ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(item);
         
         GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow();
         enchantBook.setName((Component.literal("Enchanted Book").withStyle(ChatFormatting.YELLOW)));
         for(Object2IntMap.Entry<Holder<Enchantment>> entry : comp.entrySet()){
            enchantBook.addLoreLine(TextUtils.removeItalics(Enchantment.getFullname(entry.getKey(), entry.getIntValue())));
         }
         enchantBook.addLoreLine(TextUtils.removeItalics(Component.empty()));
         enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to take the book").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         
         if(accretion){
            enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to select the book for merging").withStyle(ChatFormatting.LIGHT_PURPLE)))));
            enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Right Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to split the book").withStyle(ChatFormatting.LIGHT_PURPLE)))));
            
            if(ItemStack.isSameItemSameComponents(item, selected)){
               enchantBook.addLoreLine(TextUtils.removeItalics(Component.empty()));
               enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("<< Selected >>").withStyle(ChatFormatting.BLUE))));
            }
         }
         return enchantBook;
      });
      
      elemClickFunction((targetBook, index, clickType) -> {
         List<ItemStack> books = getItemList();
         for(ItemStack book : books){
            if(!ItemStack.isSameItemSameComponents(targetBook, book)) continue;
            
            if(accretion && clickType == ClickType.MOUSE_LEFT_SHIFT){ // merge book
               if(ItemStack.isSameItemSameComponents(targetBook, selected)){ // deselect book
                  selected = ItemStack.EMPTY;
               }else if(selected != ItemStack.EMPTY){ // find selected and merge
                  for(ItemStack otherBook : books){
                     if(ItemStack.isSameItemSameComponents(selected, otherBook)){
                        ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.mergeBooks(book, otherBook);
                        
                        if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                           MinecraftUtils.returnItems(new SimpleContainer(new ItemStack(Items.BOOK)), player);
                           selected = ItemStack.EMPTY;
                        }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                           player.sendSystemMessage(Component.literal("Those books are incompatible").withStyle(ChatFormatting.RED), false);
                        }
                        break;
                     }
                  }
               }else{
                  selected = book;
               }
            }else if(accretion && clickType == ClickType.MOUSE_RIGHT){ // split book
               if(MinecraftUtils.removeItems(player, Items.BOOK, 1)){
                  ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.splitBook(book);
                  if(result == ArcaneSingularityBlockEntity.SingularityResult.FULL){
                     player.sendSystemMessage(Component.literal("That Singularity is full").withStyle(ChatFormatting.RED), false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                     player.sendSystemMessage(Component.literal("That book cannot be split").withStyle(ChatFormatting.RED), false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                     selected = ItemStack.EMPTY;
                  }
               }else{
                  player.sendSystemMessage(Component.literal("You need a book to split the enchants to").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 1);
               }
            }else{
               if(ItemStack.isSameItemSameComponents(book, selected)) selected = ItemStack.EMPTY;
               if(blockEntity.removeBook(book) == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                  ArcanaItem.removeProperty(book, ArcaneSingularity.SINGULARITY_TAG);
                  MinecraftUtils.returnItems(new SimpleContainer(book), player);
               }
            }
            break;
         }
         
         buildPage();
      });
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, ContainerInput action){
      if(index >= size && type == ClickType.MOUSE_LEFT_SHIFT){
         int invSlot = index >= 27 + size ? index - (27 + size) : index - 45;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(stack.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.hasAnyEnchantments(stack)){
            if(tryAddBook(stack)){
               player.getInventory().setItem(invSlot, ItemStack.EMPTY);
            }
         }
      }
      return super.onAnyClick(index, type, action);
   }
   
   private boolean tryAddBook(ItemStack book){
      int curSize = blockEntity.getNumBooks();
      boolean isFull = curSize == capacity;
      
      ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.addBook(book);
      
      if(result == ArcaneSingularityBlockEntity.SingularityResult.FULL){
         player.sendSystemMessage(Component.literal("The Singularity is Full").withStyle(ChatFormatting.RED), false);
         return false;
      }
      if(!isFull && blockEntity.getNumBooks() == capacity){
         ArcanaAchievements.grant(player, ArcanaAchievements.ARCANE_QUASAR);
      }
      
      buildPage();
      return result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS;
   }
   
   @Override
   public void buildPage(){
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      items(new ArrayList<>(blockEntity.getBooks()));
      super.buildPage();
      
      GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideDefaultTooltip();
      singularityItem.setName((Component.literal("")
            .append(Component.literal("Arcane Singularity").withStyle(ChatFormatting.LIGHT_PURPLE))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a book in your inventory to insert it").withStyle(ChatFormatting.DARK_PURPLE)))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a book in the singularity to remove it").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(4, singularityItem);
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity || !blockEntity.isAssembled()){
         this.close();
      }
      
      super.onTick();
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
   
   private static class BookFilter extends GuiFilter<ItemStack> {
      public static final List<BookFilter> FILTERS = new ArrayList<>();
      
      public static final BookFilter NONE = new BookFilter("gui.arcananovum.none", ChatFormatting.WHITE.getColor().intValue(),
            (stack) -> true);
      public static final BookFilter SINGLE_ENCHANT = new BookFilter("gui.arcananovum.single_enchant", ChatFormatting.GREEN.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).size() == 1);
      public static final BookFilter MULTIPLE_ENCHANT = new BookFilter("gui.arcananovum.multiple_enchants", ChatFormatting.AQUA.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).size() > 1);
      public static final BookFilter MAX_LEVEL = new BookFilter("gui.arcananovum.max_level", ChatFormatting.LIGHT_PURPLE.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().getMaxLevel() == e.getIntValue()));
      public static final BookFilter SWORDS = new BookFilter("gui.arcananovum.sword_enchants", ChatFormatting.RED.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_SWORD))));
      public static final BookFilter BOWS = new BookFilter("gui.arcananovum.bow_enchants", ChatFormatting.GOLD.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.BOW))));
      public static final BookFilter AXES = new BookFilter("gui.arcananovum.axe_enchants", ChatFormatting.DARK_RED.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_AXE))));
      public static final BookFilter TOOLS = new BookFilter("gui.arcananovum.tool_enchants", ChatFormatting.BLUE.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_PICKAXE))));
      public static final BookFilter CROSSBOWS = new BookFilter("gui.arcananovum.crossbow_enchants", ChatFormatting.GRAY.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.CROSSBOW))));
      public static final BookFilter TRIDENTS = new BookFilter("gui.arcananovum.trident_enchants", ChatFormatting.DARK_AQUA.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.TRIDENT))));
      public static final BookFilter FISHING = new BookFilter("gui.arcananovum.fishing_rod_enchants", ChatFormatting.DARK_GRAY.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.FISHING_ROD))));
      public static final BookFilter ARMOR = new BookFilter("gui.arcananovum.armor_enchants", ChatFormatting.DARK_GREEN.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e ->
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_HELMET)) ||
                        e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_CHESTPLATE)) ||
                        e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_LEGGINGS)) ||
                        e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_BOOTS))));
      public static final BookFilter MACES = new BookFilter("gui.arcananovum.mace_enchants", ChatFormatting.DARK_BLUE.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.MACE))));
      public static final BookFilter SPEARS = new BookFilter("gui.arcananovum.spear_enchants", 0xa0ffec,
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_SPEAR))));
      public static final BookFilter TREASURE = new BookFilter("gui.arcananovum.treasure_enchants", ChatFormatting.YELLOW.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().is(net.minecraft.tags.EnchantmentTags.TREASURE) && !e.getKey().is(net.minecraft.tags.EnchantmentTags.CURSE)));
      public static final BookFilter CURSES = new BookFilter("gui.arcananovum.curses", ChatFormatting.DARK_PURPLE.getColor().intValue(),
            (stack) -> EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().stream().anyMatch(e -> e.getKey().is(net.minecraft.tags.EnchantmentTags.CURSE)));
      
      private BookFilter(String key, int color, java.util.function.Predicate<ItemStack> filter){
         super(key, color, filter);
         FILTERS.add(this);
      }
      
      @Override
      protected List<BookFilter> getList(){
         return FILTERS;
      }
      
      public BookFilter getStaticDefault(){
         return NONE;
      }
   }
   
   private static class BookSort extends GuiSort<ItemStack> {
      public static final List<BookSort> SORTS = new ArrayList<>();
      
      public static final BookSort TOTAL_LEVELS = new BookSort("gui.arcananovum.total_levels", ChatFormatting.LIGHT_PURPLE.getColor().intValue(),
            Comparator.<ItemStack>comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  count += e.getIntValue();
               }
               return -count;
            }));
      public static final BookSort HIGHEST_LEVEL = new BookSort("gui.arcananovum.highest_level", ChatFormatting.AQUA.getColor().intValue(),
            Comparator.<ItemStack>comparingInt(stack -> {
               int highest = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  if(e.getIntValue() > highest) highest = e.getIntValue();
               }
               return -highest;
            }));
      public static final BookSort LEAST_LEVELS = new BookSort("gui.arcananovum.least_levels", ChatFormatting.RED.getColor().intValue(),
            Comparator.<ItemStack>comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  count += e.getIntValue();
               }
               return count;
            }));
      public static final BookSort FIRST_ALPHABETICAL = new BookSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor().intValue(),
            Comparator.comparing(stack -> {
               java.util.Iterator<Holder<Enchantment>> iter = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().iterator();
               if(iter.hasNext()){
                  return Enchantment.getFullname(iter.next(), 1).getString();
               }
               return "";
            }));
      
      private BookSort(String key, int color, Comparator<ItemStack> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<BookSort> getList(){
         return SORTS;
      }
      
      public BookSort getStaticDefault(){
         return FIRST_ALPHABETICAL;
      }
   }
}
