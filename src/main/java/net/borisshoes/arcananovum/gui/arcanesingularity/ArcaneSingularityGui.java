package net.borisshoes.arcananovum.gui.arcanesingularity;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ArcaneSingularityGui extends SimpleGui implements WatchedGui {
   private final ArcaneSingularityBlockEntity blockEntity;
   private int page = 1;
   private final int capacity;
   private ArrayList<ItemStack> books;
   private BookFilter filter;
   private BookSort sort;
   private ItemStack selected = ItemStack.EMPTY;
   
   public ArcaneSingularityGui(ServerPlayerEntity player, ArcaneSingularityBlockEntity blockEntity, int capacity){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      this.blockEntity = blockEntity;
      this.filter = BookFilter.NONE;
      this.sort = BookSort.FIRST_ALPHABETICAL;
      this.capacity = capacity;
      loadBooks();
      setTitle(Text.literal("Arcane Singularity"));
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      boolean accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION.id) >= 1;
      
      if(index >= size && type == ClickType.MOUSE_LEFT_SHIFT){
         int invSlot = index >= 27+size ? index - (27+size) : index-45;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(stack.isOf(Items.ENCHANTED_BOOK) && !EnchantmentHelper.get(stack).isEmpty()){
            if(tryAddBook(stack)){
               player.getInventory().setStack(invSlot,ItemStack.EMPTY);
            }
         }
      }else if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean middle = type == ClickType.MOUSE_MIDDLE;
         if(middle){
            sort = BookSort.FIRST_ALPHABETICAL;
         }else{
            sort = BookSort.cycleSort(sort,backwards);
         }
         buildGui();
      }else if(index == 8){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean middle = type == ClickType.MOUSE_MIDDLE;
         if(middle){
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
         if(ind >= books.size()) return true;
         ItemStack book = books.get(ind);
         for(int i = 0; i < blockEntity.getBooks().size(); i++){
            if(ItemStack.canCombine(book,blockEntity.getBooks().get(i))){
               if(accretion && type == ClickType.MOUSE_LEFT_SHIFT){ // merge book
                  if(ItemStack.canCombine(book, selected)){ // deselect book
                     selected = ItemStack.EMPTY;
                  }else if(selected != ItemStack.EMPTY){ // find selected and merge
                     for(int j = 0; j < blockEntity.getBooks().size(); j++){
                        if(ItemStack.canCombine(selected, blockEntity.getBooks().get(j))){
                           if(tryMergeBooks(i,j)){
                              selected = ItemStack.EMPTY;
                           }else{
                              player.sendMessage(Text.literal("Those books are incompatible").formatted(Formatting.RED),false);
                           }
                           break;
                        }
                     }
                  }else{
                     selected = blockEntity.getBooks().get(i);
                  }
               }else if(accretion && type == ClickType.MOUSE_RIGHT){ // split book
                  if(!trySplitBook(i)){
                     player.sendMessage(Text.literal("That book cannot be split, or the singularity is full").formatted(Formatting.RED),false);
                  }
               }else{
                  if(ItemStack.canCombine(blockEntity.getBooks().get(i),selected)) selected = ItemStack.EMPTY;
                  book.removeSubNbt("singularityId");
                  blockEntity.getBooks().remove(i);
                  MiscUtils.returnItems(new SimpleInventory(book),player);
               }
               break;
            }
         }
         
         buildGui();
      }
      return true;
   }
   
   private boolean trySplitBook(int ind){
      ItemStack book = blockEntity.getBooks().get(ind);
      Map<Enchantment,Integer> enchants = EnchantmentHelper.get(book);
      if(blockEntity.getBooks().size() == capacity) return false;
      
      Iterator<Map.Entry<Enchantment, Integer>> iter = enchants.entrySet().iterator();
      Map.Entry<Enchantment, Integer> entry = iter.next();
      if(enchants.size() == 1){ // Split enchantment level
         if(entry.getValue() <= entry.getKey().getMinLevel()) return false;
         if(ItemStack.canCombine(book,selected)) selected = ItemStack.EMPTY;
         
         blockEntity.getBooks().remove(book);
         ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(newBook,new EnchantmentLevelEntry(entry.getKey(), entry.getValue()-1));
         newBook.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
         blockEntity.getBooks().add(newBook.copy());
         newBook.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
         blockEntity.getBooks().add(newBook.copy());
      }else{ // Remove top enchant
         if(ItemStack.canCombine(book,selected)) selected = ItemStack.EMPTY;
         iter.remove();
         
         blockEntity.getBooks().remove(book);
         ItemStack newBook1 = new ItemStack(Items.ENCHANTED_BOOK);
         ItemStack newBook2 = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(newBook1,new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
         newBook1.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
         blockEntity.getBooks().add(newBook1);
         EnchantmentHelper.set(enchants,newBook2);
         newBook2.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
         blockEntity.getBooks().add(newBook2);
      }
      blockEntity.markDirty();
      return true;
   }
   
   private boolean tryMergeBooks(int ind1, int ind2){
      ItemStack book1 = blockEntity.getBooks().get(ind1);
      ItemStack book2 = blockEntity.getBooks().get(ind2);
      Map<Enchantment,Integer> enchants1 = EnchantmentHelper.get(book1);
      Map<Enchantment,Integer> enchants2 = EnchantmentHelper.get(book2);
      Map<Enchantment,Integer> outputEnchants = new HashMap<>(enchants2);
      
      boolean hasCompatibleEnchant = false;
      boolean hasIncompatibleEnchant = false;
      for(Map.Entry<Enchantment, Integer> entry1 : enchants1.entrySet()){
         int combinedLvl = entry1.getValue();
         boolean canCombine = true;
         for(Map.Entry<Enchantment, Integer> entry2 : enchants2.entrySet()){
            if(entry1.getKey() == entry2.getKey()){
               combinedLvl = entry1.getValue().equals(entry2.getValue()) ? combinedLvl+1 : Math.max(entry1.getValue(), entry2.getValue());
            }
            if (entry1.getKey() == entry2.getKey() || entry1.getKey().canCombine(entry2.getKey())) continue;
            canCombine = false;
         }
         if (!canCombine) {
            hasIncompatibleEnchant = true;
            continue;
         }
         hasCompatibleEnchant = true;
         if (combinedLvl > entry1.getKey().getMaxLevel()) {
            combinedLvl = entry1.getKey().getMaxLevel();
         }
         outputEnchants.put(entry1.getKey(), combinedLvl);
      }
      if (hasIncompatibleEnchant && !hasCompatibleEnchant) {
         return false;
      }
      blockEntity.getBooks().remove(book1);
      blockEntity.getBooks().remove(book2);
      ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
      EnchantmentHelper.set(outputEnchants,newBook);
      newBook.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
      blockEntity.getBooks().add(newBook);
      blockEntity.markDirty();
      return true;
   }
   
   private boolean tryAddBook(ItemStack book){
      int curSize = blockEntity.getBooks().size();
      if(curSize == capacity){
         player.sendMessage(Text.literal("The Singularity is Full").formatted(Formatting.RED),false);
         return false;
      }else if(curSize == capacity-1){
         ArcanaAchievements.grant(player,ArcanaAchievements.ARCANE_QUASAR.id);
      }
      book.setSubNbt("singularityId", NbtString.of(UUID.randomUUID().toString()));
      blockEntity.getBooks().add(book);
      buildGui();
      blockEntity.markDirty();
      return true;
   }
   
   public void buildGui(){
      loadBooks();
      boolean accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION.id) >= 1;
      int maxPages = (int) Math.ceil(books.size() / 28.0);
      
      GuiElementBuilder borderPane = new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).hideFlags();
      borderPane.setName(Text.empty());
      for(int i = 0; i < size; i++){
         setSlot(i, borderPane);
      }
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      nextArrow.setName((Text.literal("")
            .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideFlags();
      prevArrow.setName((Text.literal("")
            .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine((Text.literal("")
            .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE))));
      setSlot(45,prevArrow);
      
      GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideFlags();
      singularityItem.setName((Text.literal("")
            .append(Text.literal("Arcane Singularity").formatted(Formatting.LIGHT_PURPLE))));
      singularityItem.addLoreLine((Text.literal("")
            .append(Text.literal("Shift Click").formatted(Formatting.AQUA))
            .append(Text.literal(" a book in your inventory to insert it").formatted(Formatting.DARK_PURPLE))));
      singularityItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" a book in the singularity to remove it").formatted(Formatting.DARK_PURPLE))));
      setSlot(4,singularityItem);
      
      GuiElementBuilder filterBuilt = new GuiElementBuilder(Items.HOPPER).hideFlags();
      filterBuilt.setName(Text.literal("Filter Books").formatted(Formatting.DARK_PURPLE));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal(""));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(BookFilter.getColoredLabel(filter)));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = new GuiElementBuilder(Items.NETHER_STAR).hideFlags();
      sortBuilt.setName(Text.literal("Sort Books").formatted(Formatting.DARK_PURPLE));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal(""));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(BookSort.getColoredLabel(sort)));
      setSlot(0,sortBuilt);
      
      List<ItemStack> pageItems = getPage();
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               Map<Enchantment,Integer> enchants = EnchantmentHelper.get(pageItems.get(k));
               GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow();
               enchantBook.setName((Text.literal("Enchanted Book").formatted(Formatting.YELLOW)));
               for(Map.Entry<Enchantment, Integer> entry : enchants.entrySet()){
                  enchantBook.addLoreLine(entry.getKey().getName(entry.getValue()));
               }
               enchantBook.addLoreLine(Text.empty());
               enchantBook.addLoreLine((Text.literal("")
                     .append(Text.literal("Click").formatted(Formatting.AQUA))
                     .append(Text.literal(" to take the book").formatted(Formatting.LIGHT_PURPLE))));
               
               if(accretion){
                  enchantBook.addLoreLine((Text.literal("")
                        .append(Text.literal("Shift Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to select the book for merging").formatted(Formatting.LIGHT_PURPLE))));
                  enchantBook.addLoreLine((Text.literal("")
                        .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to split the book").formatted(Formatting.LIGHT_PURPLE))));
                  
                  if(ItemStack.canCombine(pageItems.get(k),selected)){
                     enchantBook.addLoreLine(Text.empty());
                     enchantBook.addLoreLine((Text.literal("<< Selected >>").formatted(Formatting.BLUE)));
                  }
               }
               
               setSlot((i*9+10)+j,enchantBook);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).setName(Text.empty()));
            }
            k++;
         }
      }
   }
   
   private void loadBooks(){
      this.books = new ArrayList<>();
      for(ItemStack book : blockEntity.getBooks()){
         if(BookFilter.matchesFilter(filter, EnchantmentHelper.get(book))){
            books.add(book.copy());
         }
      }
      
      switch(sort){
         case TOTAL_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Map.Entry<Enchantment, Integer> e : EnchantmentHelper.get(stack).entrySet()){
                  count += e.getValue();
               }
               return -count;
            });
            books.sort(levelComparator);
         }
         case HIGHEST_LEVEL -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int highest = 0;
               for(Map.Entry<Enchantment, Integer> e : EnchantmentHelper.get(stack).entrySet()){
                  if(e.getValue() > highest) highest = e.getValue();
               }
               return -highest;
            });
            books.sort(levelComparator);
         }
         case BEST_RARITY -> {
            Comparator<ItemStack> rarityComparator = Comparator.comparingInt(stack -> {
               int highest = 0;
               for(Map.Entry<Enchantment, Integer> e : EnchantmentHelper.get(stack).entrySet()){
                  int invWeight = Integer.MAX_VALUE - e.getKey().getRarity().getWeight();
                  if(invWeight > highest) highest = invWeight;
               }
               return -highest;
            });
            books.sort(rarityComparator);
         }
         case LEAST_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Map.Entry<Enchantment, Integer> e : EnchantmentHelper.get(stack).entrySet()){
                  count += e.getValue();
               }
               return count;
            });
            books.sort(levelComparator);
         }
         case FIRST_ALPHABETICAL -> {
            Comparator<ItemStack> nameComparator = Comparator.comparing(stack -> {
               Iterator<Map.Entry<Enchantment, Integer>> iter = EnchantmentHelper.get(stack).entrySet().iterator();
               if(iter.hasNext()){
                  return iter.next().getKey().getTranslationKey();
               }
               return "";
            });
            books.sort(nameComparator);
         }
      }
   }
   
   private List<ItemStack> getPage(){
      List<ItemStack> pageItems = new ArrayList<>();
      
      for(int i = (page-1)*28; i < page*28 && i < books.size(); i++){
         pageItems.add(books.get(i));
      }
      return pageItems;
   }
   
   public void nextPage(){
      int maxPages = (int) Math.ceil(books.size() / 28.0);
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
   public void close(){
      super.close();
   }
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
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
      TREASURE("Treasure Enchants"),
      CURSES("Curses");
      
      public final String label;
      
      BookFilter(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(BookFilter filter){
         MutableText text = Text.literal(filter.label);
         
         return switch(filter){ // Only Black and Dark Blue left for future usage (before repeats)
            case NONE -> text.formatted(Formatting.WHITE);
            case CROSSBOWS -> text.formatted(Formatting.GRAY);
            case SINGLE_ENCHANT -> text.formatted(Formatting.GREEN);
            case MULTIPLE_ENCHANT -> text.formatted(Formatting.AQUA);
            case BOWS -> text.formatted(Formatting.GOLD);
            case MAX_LEVEL -> text.formatted(Formatting.LIGHT_PURPLE);
            case TRIDENTS -> text.formatted(Formatting.DARK_AQUA);
            case CURSES -> text.formatted(Formatting.DARK_PURPLE);
            case ARMOR -> text.formatted(Formatting.DARK_GREEN);
            case SWORDS -> text.formatted(Formatting.RED);
            case TOOLS -> text.formatted(Formatting.BLUE);
            case AXES -> text.formatted(Formatting.DARK_RED);
            case TREASURE -> text.formatted(Formatting.YELLOW);
            case FISHING -> text.formatted(Formatting.DARK_GRAY);
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
      
      public static boolean matchesFilter(BookFilter filter, Map<Enchantment,Integer> enchantMap){
         if(enchantMap.isEmpty()) return false;
         return switch(filter){
            case NONE -> true;
            case CROSSBOWS -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.CROSSBOW)));
            case SINGLE_ENCHANT -> enchantMap.size() == 1;
            case MULTIPLE_ENCHANT -> enchantMap.size() > 1;
            case BOWS -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.BOW)));
            case MAX_LEVEL ->  enchantMap.entrySet().stream().anyMatch(e -> e.getKey().getMaxLevel() == e.getValue());
            case TRIDENTS -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.TRIDENT)));
            case CURSES -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isCursed());
            case ARMOR -> enchantMap.entrySet().stream().anyMatch(e ->
                  e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_HELMET)) ||
                  e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_CHESTPLATE)) ||
                  e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_LEGGINGS)) ||
                  e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_BOOTS))
            );
            case SWORDS -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_SWORD)));
            case TOOLS -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_PICKAXE)));
            case AXES -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.GOLDEN_AXE)));
            case TREASURE -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isTreasure() && !e.getKey().isCursed());
            case FISHING -> enchantMap.entrySet().stream().anyMatch(e -> e.getKey().isAcceptableItem(new ItemStack(Items.FISHING_ROD)));
         };
      }
   }
   
   public enum BookSort {
      TOTAL_LEVELS("Total Levels"),
      HIGHEST_LEVEL("Highest Level"),
      BEST_RARITY("Highest Rarity"),
      LEAST_LEVELS("Least Levels"),
      FIRST_ALPHABETICAL("Alphabetical");
      
      public final String label;
      
      BookSort(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(BookSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case TOTAL_LEVELS -> text.formatted(Formatting.LIGHT_PURPLE);
            case HIGHEST_LEVEL -> text.formatted(Formatting.AQUA);
            case BEST_RARITY -> text.formatted(Formatting.GOLD);
            case LEAST_LEVELS -> text.formatted(Formatting.RED);
            case FIRST_ALPHABETICAL -> text.formatted(Formatting.GREEN);
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
