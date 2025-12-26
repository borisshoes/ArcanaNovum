package net.borisshoes.arcananovum.gui.arcanesingularity;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.*;

public class ArcaneSingularityGui extends SimpleGui {
   private final ArcaneSingularityBlockEntity blockEntity;
   private int page = 1;
   private final int capacity;
   private ArrayList<ItemStack> filteredBooks;
   private List<ItemStack> books;
   private BookFilter filter;
   private BookSort sort;
   private ItemStack selected = ItemStack.EMPTY;
   
   public ArcaneSingularityGui(ServerPlayer player, ArcaneSingularityBlockEntity blockEntity, int capacity){
      super(MenuType.GENERIC_9x6, player, false);
      this.blockEntity = blockEntity;
      this.filter = BookFilter.NONE;
      this.sort = BookSort.FIRST_ALPHABETICAL;
      this.capacity = capacity;
      loadBooks();
      setTitle(Component.literal("Arcane Singularity"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      boolean accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION.id) >= 1;
      
      if(index >= size && type == ClickType.MOUSE_LEFT_SHIFT){
         int invSlot = index >= 27+size ? index - (27+size) : index-45;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(stack.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.hasAnyEnchantments(stack)){
            if(tryAddBook(stack)){
               player.getInventory().setItem(invSlot, ItemStack.EMPTY);
            }
         }
      }else if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            sort = BookSort.FIRST_ALPHABETICAL;
         }else{
            sort = BookSort.cycleSort(sort,backwards);
         }
         buildGui();
      }else if(index == 8){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            filter = BookFilter.NONE;
         }else{
            filter = BookFilter.cycleFilter(filter,backwards);
            page = 1;
         }
         buildGui();
      }else if(index == 45){
         prevPage();
         buildGui();
      }else if(index == 53){
         nextPage();
         buildGui();
      }else if(indexInCenter){
         int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(page-1);
         if(ind >= filteredBooks.size()) return true;
         ItemStack targetBook = filteredBooks.get(ind);
         for(ItemStack book : this.books){
            if(!ItemStack.isSameItemSameComponents(targetBook,book)) continue;
            
            if(accretion && type == ClickType.MOUSE_LEFT_SHIFT){ // merge book
               if(ItemStack.isSameItemSameComponents(targetBook, selected)){ // deselect book
                  selected = ItemStack.EMPTY;
               }else if(selected != ItemStack.EMPTY){ // find selected and merge
                  for(ItemStack otherBook : this.books){
                     if(ItemStack.isSameItemSameComponents(selected, otherBook)){
                        ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.mergeBooks(book,otherBook);
                        
                        if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                           MinecraftUtils.returnItems(new SimpleContainer(new ItemStack(Items.BOOK)),player);
                           selected = ItemStack.EMPTY;
                        }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                           player.displayClientMessage(Component.literal("Those books are incompatible").withStyle(ChatFormatting.RED),false);
                        }
                        break;
                     }
                  }
               }else{
                  selected = book;
               }
            }else if(accretion && type == ClickType.MOUSE_RIGHT){ // split book
               if(MinecraftUtils.removeItems(player, Items.BOOK,1)){
                  ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.splitBook(book);
                  if(result == ArcaneSingularityBlockEntity.SingularityResult.FULL){
                     player.displayClientMessage(Component.literal("That Singularity is full").withStyle(ChatFormatting.RED),false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                     player.displayClientMessage(Component.literal("That book cannot be split").withStyle(ChatFormatting.RED),false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                     selected = ItemStack.EMPTY;
                  }
               }else{
                  player.displayClientMessage(Component.literal("You need a book to split the enchants to").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,1);
               }
            }else{
               if(ItemStack.isSameItemSameComponents(book,selected)) selected = ItemStack.EMPTY;
               if(blockEntity.removeBook(book) == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                  ArcanaItem.removeProperty(book, ArcaneSingularity.SINGULARITY_TAG);
                  MinecraftUtils.returnItems(new SimpleContainer(book),player);
               }
            }
            break;
         }
         
         buildGui();
      }
      return true;
   }
   
   private boolean tryAddBook(ItemStack book){
      int curSize = blockEntity.getNumBooks();
      boolean isFull = curSize == capacity;
      
      ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.addBook(book);
      
      if(result == ArcaneSingularityBlockEntity.SingularityResult.FULL){
         player.displayClientMessage(Component.literal("The Singularity is Full").withStyle(ChatFormatting.RED),false);
         return false;
      }
      if(!isFull && blockEntity.getNumBooks() == capacity){
         ArcanaAchievements.grant(player,ArcanaAchievements.ARCANE_QUASAR.id);
      }
      
      buildGui();
      return result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS;
   }
   
   public void buildGui(){
      loadBooks();
      boolean accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION.id) >= 1;
      int maxPages = (int) Math.ceil(filteredBooks.size() / 28.0);
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      if(maxPages > 1){
         GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextArrow.setName((Component.literal("")
               .append(Component.literal("Next Page").withStyle(ChatFormatting.GOLD))));
         nextArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+maxPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(53,nextArrow);
         
         GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevArrow.setName((Component.literal("")
               .append(Component.literal("Prev Page").withStyle(ChatFormatting.GOLD))));
         prevArrow.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("("+page+" of "+maxPages+")").withStyle(ChatFormatting.DARK_PURPLE)))));
         setSlot(45,prevArrow);
      }
      
      GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideDefaultTooltip();
      singularityItem.setName((Component.literal("")
            .append(Component.literal("Arcane Singularity").withStyle(ChatFormatting.LIGHT_PURPLE))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Shift Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a book in your inventory to insert it").withStyle(ChatFormatting.DARK_PURPLE)))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" a book in the singularity to remove it").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(4,singularityItem);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Component.literal("Filter Books").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle filter backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Current Filter: ").withStyle(ChatFormatting.AQUA)).append(BookFilter.getColoredLabel(filter))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Books").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(BookSort.getColoredLabel(sort))));
      setSlot(0,sortBuilt);
      
      List<ItemStack> pageItems = getPage();
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               ItemEnchantments comp = EnchantmentHelper.getEnchantmentsForCrafting(pageItems.get(k));
               
               GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow();
               enchantBook.setName((Component.literal("Enchanted Book").withStyle(ChatFormatting.YELLOW)));
               for(Object2IntMap.Entry<Holder<Enchantment>> entry : comp.entrySet()){
                  enchantBook.addLoreLine(TextUtils.removeItalics(Enchantment.getFullname(entry.getKey(),entry.getIntValue())));
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
                  
                  if(ItemStack.isSameItemSameComponents(pageItems.get(k),selected)){
                     enchantBook.addLoreLine(TextUtils.removeItalics(Component.empty()));
                     enchantBook.addLoreLine(TextUtils.removeItalics((Component.literal("<< Selected >>").withStyle(ChatFormatting.BLUE))));
                  }
               }
               
               setSlot((i*9+10)+j,enchantBook);
            }else{
               setSlot((i*9+10)+j,GuiElementBuilder.from(GraphicalItem.with(ArcanaRegistry.GAS)).setName(Component.empty()).hideTooltip());
            }
            k++;
         }
      }
   }
   
   private void loadBooks(){
      this.books = new ArrayList<>(blockEntity.getBooks());
      this.filteredBooks = new ArrayList<>();
      for(ItemStack book : this.books){
         if(BookFilter.matchesFilter(filter, EnchantmentHelper.getEnchantmentsForCrafting(book).entrySet())){
            filteredBooks.add(book.copy());
         }
      }
      
      switch(sort){
         case TOTAL_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  count += e.getIntValue();
               }
               return -count;
            });
            filteredBooks.sort(levelComparator);
         }
         case HIGHEST_LEVEL -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int highest = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  if(e.getIntValue() > highest) highest = e.getIntValue();
               }
               return -highest;
            });
            filteredBooks.sort(levelComparator);
         }
         case LEAST_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()){
                  count += e.getIntValue();
               }
               return count;
            });
            filteredBooks.sort(levelComparator);
         }
         case FIRST_ALPHABETICAL -> {
            Comparator<ItemStack> nameComparator = Comparator.comparing(stack -> {
               Iterator<Holder<Enchantment>> iter = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().iterator();
               if(iter.hasNext()){
                  return Enchantment.getFullname(iter.next(),1).getString();
               }
               return "";
            });
            filteredBooks.sort(nameComparator);
         }
      }
   }
   
   private List<ItemStack> getPage(){
      List<ItemStack> pageItems = new ArrayList<>();
      
      for(int i = (page-1)*28; i < page*28 && i < filteredBooks.size(); i++){
         pageItems.add(filteredBooks.get(i));
      }
      return pageItems;
   }
   
   public void nextPage(){
      int maxPages = (int) Math.ceil(filteredBooks.size() / 28.0);
      if(page < maxPages){
         page++;
      }
   }
   
   public void prevPage(){
      if(page > 1){
         page--;
      }
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
   
   public enum BookFilter {
      NONE("None"),
      SINGLE_ENCHANT("Single Enchant"),
      MAX_LEVEL("Max Level"),
      MULTIPLE_ENCHANT("Multiple Enchants"),
      SWORDS("Sword Enchants"),
      BOWS("Bow Enchants"),
      AXES("Axe Enchants"),
      TOOLS("Tool Enchants"),
      CROSSBOWS("Crossbow Enchants"),
      TRIDENTS("Trident Enchants"),
      FISHING("Fishing Rod Enchants"),
      ARMOR("Armor Enchants"),
      MACES("Mace Enchants"),
      TREASURE("Treasure Enchants"),
      CURSES("Curses");
      
      public final String label;
      
      BookFilter(String label){
         this.label = label;
      }
      
      public static Component getColoredLabel(BookFilter filter){
         MutableComponent text = Component.literal(filter.label);
         
         return switch(filter){ // Only Black for future usage (before repeats)
            case NONE -> text.withStyle(ChatFormatting.WHITE);
            case CROSSBOWS -> text.withStyle(ChatFormatting.GRAY);
            case SINGLE_ENCHANT -> text.withStyle(ChatFormatting.GREEN);
            case MULTIPLE_ENCHANT -> text.withStyle(ChatFormatting.AQUA);
            case BOWS -> text.withStyle(ChatFormatting.GOLD);
            case MAX_LEVEL -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case TRIDENTS -> text.withStyle(ChatFormatting.DARK_AQUA);
            case CURSES -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case ARMOR -> text.withStyle(ChatFormatting.DARK_GREEN);
            case SWORDS -> text.withStyle(ChatFormatting.RED);
            case TOOLS -> text.withStyle(ChatFormatting.BLUE);
            case AXES -> text.withStyle(ChatFormatting.DARK_RED);
            case TREASURE -> text.withStyle(ChatFormatting.YELLOW);
            case FISHING -> text.withStyle(ChatFormatting.DARK_GRAY);
            case MACES -> text.withStyle(ChatFormatting.DARK_BLUE);
         };
      }
      
      public static BookFilter cycleFilter(BookFilter filter, boolean backwards){
         BookFilter[] filters = BookFilter.values();
         int ind = -1;
         for(int i = 0; i < filters.length; i++){
            if(filter == filters[i]){
               ind = i;
            }
         }
         ind += backwards ? -1 : 1;
         if(ind >= filters.length) ind = 0;
         if(ind < 0) ind = filters.length-1;
         return filters[ind];
      }
      
      public static boolean matchesFilter(BookFilter filter, Set<Object2IntMap.Entry<Holder<Enchantment>>> enchantMap){
         if(enchantMap.isEmpty()) return false;
         return switch(filter){
            case NONE -> true;
            case CROSSBOWS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.CROSSBOW)));
            case SINGLE_ENCHANT -> enchantMap.size() == 1;
            case MULTIPLE_ENCHANT -> enchantMap.size() > 1;
            case BOWS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.BOW)));
            case MAX_LEVEL ->  enchantMap.stream().anyMatch(e -> e.getKey().value().getMaxLevel() == e.getIntValue());
            case TRIDENTS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.TRIDENT)));
            case CURSES -> enchantMap.stream().anyMatch(e ->  e.getKey().is(EnchantmentTags.CURSE));
            case ARMOR -> enchantMap.stream().anyMatch(e ->
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_HELMET)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_CHESTPLATE)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_LEGGINGS)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_BOOTS))
            );
            case SWORDS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_SWORD)));
            case TOOLS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_PICKAXE)));
            case AXES -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_AXE)));
            case TREASURE -> enchantMap.stream().anyMatch(e -> e.getKey().is(EnchantmentTags.TREASURE) && ! e.getKey().is(EnchantmentTags.CURSE));
            case FISHING -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.FISHING_ROD)));
            case MACES -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.MACE)));
         };
      }
   }
   
   public enum BookSort {
      TOTAL_LEVELS("Total Levels"),
      HIGHEST_LEVEL("Highest Level"),
      LEAST_LEVELS("Least Levels"),
      FIRST_ALPHABETICAL("Alphabetical");
      
      public final String label;
      
      BookSort(String label){
         this.label = label;
      }
      
      public static Component getColoredLabel(BookSort sort){
         MutableComponent text = Component.literal(sort.label);
         
         return switch(sort){
            case TOTAL_LEVELS -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case HIGHEST_LEVEL -> text.withStyle(ChatFormatting.AQUA);
            case LEAST_LEVELS -> text.withStyle(ChatFormatting.RED);
            case FIRST_ALPHABETICAL -> text.withStyle(ChatFormatting.GREEN);
         };
      }
      
      public static BookSort cycleSort(BookSort sort, boolean backwards){
         BookSort[] sorts = BookSort.values();
         int ind = -1;
         for(int i = 0; i < sorts.length; i++){
            if(sort == sorts[i]){
               ind = i;
            }
         }
         ind += backwards ? -1 : 1;
         if(ind >= sorts.length) ind = 0;
         if(ind < 0) ind = sorts.length-1;
         return sorts[ind];
      }
   }
}
