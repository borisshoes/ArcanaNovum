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
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

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
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      boolean accretion = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.ACCRETION.id) >= 1;
      
      if(index >= size && type == ClickType.MOUSE_LEFT_SHIFT){
         int invSlot = index >= 27+size ? index - (27+size) : index-45;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(stack.isOf(Items.ENCHANTED_BOOK) && EnchantmentHelper.hasEnchantments(stack)){
            if(tryAddBook(stack)){
               player.getInventory().setStack(invSlot,ItemStack.EMPTY);
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
            if(!ItemStack.areItemsAndComponentsEqual(targetBook,book)) continue;
            
            if(accretion && type == ClickType.MOUSE_LEFT_SHIFT){ // merge book
               if(ItemStack.areItemsAndComponentsEqual(targetBook, selected)){ // deselect book
                  selected = ItemStack.EMPTY;
               }else if(selected != ItemStack.EMPTY){ // find selected and merge
                  for(ItemStack otherBook : this.books){
                     if(ItemStack.areItemsAndComponentsEqual(selected, otherBook)){
                        ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.mergeBooks(book,otherBook);
                        
                        if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                           MinecraftUtils.returnItems(new SimpleInventory(new ItemStack(Items.BOOK)),player);
                           selected = ItemStack.EMPTY;
                        }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                           player.sendMessage(Text.literal("Those books are incompatible").formatted(Formatting.RED),false);
                        }
                        break;
                     }
                  }
               }else{
                  selected = book;
               }
            }else if(accretion && type == ClickType.MOUSE_RIGHT){ // split book
               if(MinecraftUtils.removeItems(player,Items.BOOK,1)){
                  ArcaneSingularityBlockEntity.SingularityResult result = blockEntity.splitBook(book);
                  if(result == ArcaneSingularityBlockEntity.SingularityResult.FULL){
                     player.sendMessage(Text.literal("That Singularity is full").formatted(Formatting.RED),false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.FAIL){
                     player.sendMessage(Text.literal("That book cannot be split").formatted(Formatting.RED),false);
                  }else if(result == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                     selected = ItemStack.EMPTY;
                  }
               }else{
                  player.sendMessage(Text.literal("You need a book to split the enchants to").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else{
               if(ItemStack.areItemsAndComponentsEqual(book,selected)) selected = ItemStack.EMPTY;
               if(blockEntity.removeBook(book) == ArcaneSingularityBlockEntity.SingularityResult.SUCCESS){
                  ArcanaItem.removeProperty(book, ArcaneSingularity.SINGULARITY_TAG);
                  MinecraftUtils.returnItems(new SimpleInventory(book),player);
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
         player.sendMessage(Text.literal("The Singularity is Full").formatted(Formatting.RED),false);
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
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR,Text.empty());
      
      if(maxPages > 1){
         GuiElementBuilder nextArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextArrow.setName((Text.literal("")
               .append(Text.literal("Next Page").formatted(Formatting.GOLD))));
         nextArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE)))));
         setSlot(53,nextArrow);
         
         GuiElementBuilder prevArrow = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevArrow.setName((Text.literal("")
               .append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
         prevArrow.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("("+page+" of "+maxPages+")").formatted(Formatting.DARK_PURPLE)))));
         setSlot(45,prevArrow);
      }
      
      GuiElementBuilder singularityItem = new GuiElementBuilder(Items.LECTERN).hideDefaultTooltip();
      singularityItem.setName((Text.literal("")
            .append(Text.literal("Arcane Singularity").formatted(Formatting.LIGHT_PURPLE))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Shift Click").formatted(Formatting.AQUA))
            .append(Text.literal(" a book in your inventory to insert it").formatted(Formatting.DARK_PURPLE)))));
      singularityItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click").formatted(Formatting.AQUA))
            .append(Text.literal(" a book in the singularity to remove it").formatted(Formatting.DARK_PURPLE)))));
      setSlot(4,singularityItem);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Text.literal("Filter Books").formatted(Formatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(BookFilter.getColoredLabel(filter))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Text.literal("Sort Books").formatted(Formatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(BookSort.getColoredLabel(sort))));
      setSlot(0,sortBuilt);
      
      List<ItemStack> pageItems = getPage();
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(pageItems.get(k));
               
               GuiElementBuilder enchantBook = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow();
               enchantBook.setName((Text.literal("Enchanted Book").formatted(Formatting.YELLOW)));
               for(Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : comp.getEnchantmentEntries()){
                  enchantBook.addLoreLine(TextUtils.removeItalics(Enchantment.getName(entry.getKey(),entry.getIntValue())));
               }
               enchantBook.addLoreLine(TextUtils.removeItalics(Text.empty()));
               enchantBook.addLoreLine(TextUtils.removeItalics((Text.literal("")
                     .append(Text.literal("Click").formatted(Formatting.AQUA))
                     .append(Text.literal(" to take the book").formatted(Formatting.LIGHT_PURPLE)))));
               
               if(accretion){
                  enchantBook.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Shift Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to select the book for merging").formatted(Formatting.LIGHT_PURPLE)))));
                  enchantBook.addLoreLine(TextUtils.removeItalics((Text.literal("")
                        .append(Text.literal("Right Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to split the book").formatted(Formatting.LIGHT_PURPLE)))));
                  
                  if(ItemStack.areItemsAndComponentsEqual(pageItems.get(k),selected)){
                     enchantBook.addLoreLine(TextUtils.removeItalics(Text.empty()));
                     enchantBook.addLoreLine(TextUtils.removeItalics((Text.literal("<< Selected >>").formatted(Formatting.BLUE))));
                  }
               }
               
               setSlot((i*9+10)+j,enchantBook);
            }else{
               setSlot((i*9+10)+j,GuiElementBuilder.from(GraphicalItem.with(ArcanaRegistry.GAS)).setName(Text.empty()).hideTooltip());
            }
            k++;
         }
      }
   }
   
   private void loadBooks(){
      this.books = new ArrayList<>(blockEntity.getBooks());
      this.filteredBooks = new ArrayList<>();
      for(ItemStack book : this.books){
         if(BookFilter.matchesFilter(filter, EnchantmentHelper.getEnchantments(book).getEnchantmentEntries())){
            filteredBooks.add(book.copy());
         }
      }
      
      switch(sort){
         case TOTAL_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<RegistryEntry<Enchantment>> e : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()){
                  count += e.getIntValue();
               }
               return -count;
            });
            filteredBooks.sort(levelComparator);
         }
         case HIGHEST_LEVEL -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int highest = 0;
               for(Object2IntMap.Entry<RegistryEntry<Enchantment>> e : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()){
                  if(e.getIntValue() > highest) highest = e.getIntValue();
               }
               return -highest;
            });
            filteredBooks.sort(levelComparator);
         }
         case LEAST_LEVELS -> {
            Comparator<ItemStack> levelComparator = Comparator.comparingInt(stack -> {
               int count = 0;
               for(Object2IntMap.Entry<RegistryEntry<Enchantment>> e : EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()){
                  count += e.getIntValue();
               }
               return count;
            });
            filteredBooks.sort(levelComparator);
         }
         case FIRST_ALPHABETICAL -> {
            Comparator<ItemStack> nameComparator = Comparator.comparing(stack -> {
               Iterator<RegistryEntry<Enchantment>> iter = EnchantmentHelper.getEnchantments(stack).getEnchantments().iterator();
               if(iter.hasNext()){
                  return Enchantment.getName(iter.next(),1).getString();
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
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity || !blockEntity.isAssembled()){
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
      
      public static Text getColoredLabel(BookFilter filter){
         MutableText text = Text.literal(filter.label);
         
         return switch(filter){ // Only Black for future usage (before repeats)
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
            case MACES -> text.formatted(Formatting.DARK_BLUE);
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
      
      public static boolean matchesFilter(BookFilter filter, Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchantMap){
         if(enchantMap.isEmpty()) return false;
         return switch(filter){
            case NONE -> true;
            case CROSSBOWS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.CROSSBOW)));
            case SINGLE_ENCHANT -> enchantMap.size() == 1;
            case MULTIPLE_ENCHANT -> enchantMap.size() > 1;
            case BOWS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.BOW)));
            case MAX_LEVEL ->  enchantMap.stream().anyMatch(e -> e.getKey().value().getMaxLevel() == e.getIntValue());
            case TRIDENTS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.TRIDENT)));
            case CURSES -> enchantMap.stream().anyMatch(e ->  e.getKey().isIn(EnchantmentTags.CURSE));
            case ARMOR -> enchantMap.stream().anyMatch(e ->
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_HELMET)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_CHESTPLATE)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_LEGGINGS)) ||
                  e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_BOOTS))
            );
            case SWORDS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_SWORD)));
            case TOOLS -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_PICKAXE)));
            case AXES -> enchantMap.stream().anyMatch(e -> e.getKey().value().isSupportedItem(new ItemStack(Items.GOLDEN_AXE)));
            case TREASURE -> enchantMap.stream().anyMatch(e -> e.getKey().isIn(EnchantmentTags.TREASURE) && ! e.getKey().isIn(EnchantmentTags.CURSE));
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
      
      public static Text getColoredLabel(BookSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case TOTAL_LEVELS -> text.formatted(Formatting.LIGHT_PURPLE);
            case HIGHEST_LEVEL -> text.formatted(Formatting.AQUA);
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
