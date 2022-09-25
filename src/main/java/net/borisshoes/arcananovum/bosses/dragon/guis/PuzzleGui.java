package net.borisshoes.arcananovum.bosses.dragon.guis;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.*;

public class PuzzleGui extends SimpleGui {

   private final int[] borders = {0,1,2,3,4,5,14,15,17,23,32,41,45,46,47,48,49,50};
   private final int[] optionSlots = {9,10,11,12,13,18,19,20,21,22,27,28,29,30,31,36,37,38,39,40};
   private final int[] ingredSlots = {24,25,26,33,34,35,42,43,44};
   private Puzzle curPuzzle;
   private int curSlot = 0;
   private int successes = 0;
   private boolean complete = false;
   private DragonBossFight.ReclaimState reclaimState;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    */
   public PuzzleGui(ScreenHandlerType<?> type, ServerPlayerEntity player, DragonBossFight.ReclaimState reclaimState){
      super(type, player, false);
      this.reclaimState = reclaimState;
   }
   
   public void buildPuzzle(){
      curPuzzle = puzzles[(int)(Math.random()*puzzles.length)].generatePuzzle();
      ArrayList<Item> itemOptions = curPuzzle.getItemOptions();
      
      curSlot = 0;
      
      for(int i = 0; i < size; i++){
         this.clearSlot(i);
      }
      
      for(int i = 0; i < borders.length; i++){
         this.setSlot(borders[i],new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      for(int i = 0; i < 20; i++){
         if(itemOptions.get(i).getDefaultStack().isOf(Items.AIR)){
            this.setSlot(optionSlots[i],GuiElementBuilder.from(Items.BARRIER.getDefaultStack()).setName(Text.literal("Air / Empty")));
         }else{
            this.setSlot(optionSlots[i],GuiElementBuilder.from(itemOptions.get(i).getDefaultStack()));
         }
      }
   
      GuiElementBuilder craftItem = GuiElementBuilder.from(curPuzzle.getCraftedItem().getDefaultStack());
      //String name = Text.translatable(curPuzzle.getCraftedItem().getTranslationKey()).getString();
      craftItem.setName(Text.literal("Craft a "+curPuzzle.getName()).formatted(Formatting.BOLD,Formatting.GOLD));
      this.setSlot(7,craftItem);
   
      GuiElementBuilder submitItem = GuiElementBuilder.from(Items.LIME_TERRACOTTA.getDefaultStack());
      submitItem.setName(Text.literal("Submit").formatted(Formatting.BOLD,Formatting.GREEN));
      this.setSlot(16,submitItem);
      
      if(curPuzzle.getRest1() != null){
         PuzzleRestriction r1 = curPuzzle.getRest1();
         GuiElementBuilder restItem = GuiElementBuilder.from(r1.getItem().getDefaultStack());
         restItem.setName(r1.getLabel());
         this.setSlot(6,restItem);
      }else{
         this.setSlot(6,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      if(curPuzzle.getRest2() != null){
         PuzzleRestriction r2 = curPuzzle.getRest2();
         GuiElementBuilder restItem = GuiElementBuilder.from(r2.getItem().getDefaultStack());
         restItem.setName(r2.getLabel());
         this.setSlot(8,restItem);
      }else{
         this.setSlot(8,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      for(int i = 0; i < 3; i++){
         GuiElementBuilder progress = successes > i ? GuiElementBuilder.from(Items.GREEN_STAINED_GLASS.getDefaultStack()) : GuiElementBuilder.from(Items.WHITE_STAINED_GLASS.getDefaultStack());
         progress.setName(Text.literal(successes+"/4 Completed").formatted(Formatting.GREEN));
         this.setSlot(51+i, progress);
      }
      
      GuiElementBuilder slotSelector = GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack());
      slotSelector.setName(Text.literal("Selected Slot").formatted(Formatting.DARK_AQUA));
      this.setSlot(ingredSlots[curSlot], slotSelector);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(type == ClickType.MOUSE_LEFT){
         int slotType = 0; // 0 - unimportant, 1 - ingredient click, 2 - slot select
         for(int i = 0; i < optionSlots.length; i++){
            if(index == optionSlots[i]){
               slotType = 1;
               this.setSlot(ingredSlots[curSlot], curPuzzle.getItemOptions().get(i).getDefaultStack());
               curSlot++;
               if(curSlot == 9){ // Check puzzle and close
                  if(checkPuzzle()){
                     if(successes < 4){
                        buildPuzzle();
                     }else{
                        complete = true;
                        close();
                     }
                  }
                  break;
               }else{ // Switch to next empty slot
                  while(this.getSlot(ingredSlots[curSlot])!=null){
                     curSlot++;
                     if(curSlot == 9){ // No empty slots left. Check puzzle and close
                        if(checkPuzzle()){
                           if(successes < 4){
                              buildPuzzle();
                           }else{
                              complete = true;
                              close();
                           }
                        }
                        break;
                     }
                  }
                  GuiElementBuilder slotSelector = GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack());
                  slotSelector.setName(Text.literal("Selected Slot").formatted(Formatting.DARK_AQUA));
                  this.setSlot(ingredSlots[curSlot], slotSelector);
               }
               break;
            }
         }
         for(int i = 0; i < ingredSlots.length; i++){
            if(index == ingredSlots[i]){
               slotType = 2;
               this.clearSlot(ingredSlots[curSlot]);
               curSlot = i;
               GuiElementBuilder slotSelector = GuiElementBuilder.from(Items.STRUCTURE_VOID.getDefaultStack());
               slotSelector.setName(Text.literal("Selected Slot").formatted(Formatting.DARK_AQUA));
               this.setSlot(ingredSlots[curSlot], slotSelector);
               break;
            }
         }
         if(index == 16){
            if(checkPuzzle()){
               if(successes < 4){
                  buildPuzzle();
               }else{
                  complete = true;
                  close();
                  
               }
            }
         }
      }
      return true;
   }
   
   @Override
   public void onClose(){
      if(reclaimState != null){
         if(complete){
            reclaimState.playerSolved();
            player.sendMessage(Text.literal("The Tower's Arcana Surges Through You!").formatted(Formatting.BOLD,Formatting.DARK_AQUA),true);
         }else{
            reclaimState.setPlayer(null);
            player.sendMessage(Text.literal("You Fail To Channel The Tower's Magic").formatted(Formatting.ITALIC,Formatting.RED),true);
         }
      }else{
         if(complete){
            player.sendMessage(Text.literal("Success!").formatted(Formatting.BOLD,Formatting.DARK_AQUA));
         }else{
            player.sendMessage(Text.literal("Fail!").formatted(Formatting.ITALIC,Formatting.RED));
         }
      }
   }
   
   private boolean checkPuzzle(){
      if(curPuzzle == null) {
         close();
         return false;
      }
      Item[] solution = new Item[9];
      for(int i = 0; i < ingredSlots.length; i++){
         GuiElementInterface slot = this.getSlot(ingredSlots[i]);
         if(slot == null || slot.getItemStack().getItem().getDefaultStack().isOf(Items.STRUCTURE_VOID)){
            solution[i] = Items.AIR;
         }else{
            solution[i] = slot.getItemStack().getItem();
         }
      }
      
      if(curPuzzle.isValid(solution)){
         successes++;
         return true;
      }else{
         complete = false;
         close();
         return false;
      }
   }
   
   private static final Puzzle[] puzzles = {
         new Puzzle(Items.PISTON,"Piston",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.COBBLESTONE,Items.IRON_INGOT,Items.COBBLESTONE,Items.COBBLESTONE,Items.REDSTONE,Items.COBBLESTONE),
         new Puzzle(Items.FURNACE,"Furnace",Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.AIR,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE),
         new Puzzle(Items.ENDER_CHEST,"Ender Chest",Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN,Items.ENDER_EYE,Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN),
         new Puzzle(Items.END_CRYSTAL,"End Crystal",Items.GLASS,Items.GLASS,Items.GLASS,Items.GLASS,Items.ENDER_EYE,Items.GLASS,Items.GLASS,Items.GHAST_TEAR,Items.GLASS),
         new Puzzle(Items.BARREL,"Barrel",Items.OAK_PLANKS,Items.OAK_SLAB,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.AIR,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_SLAB,Items.OAK_PLANKS),
         new Puzzle(Items.COMPOSTER,"Composter",Items.OAK_SLAB,Items.AIR,Items.OAK_SLAB,Items.OAK_SLAB,Items.AIR,Items.OAK_SLAB,Items.OAK_SLAB,Items.OAK_SLAB,Items.OAK_SLAB),
         new Puzzle(Items.CHEST,"Chest",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.AIR,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.JUKEBOX,"Jukebox",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.DIAMOND,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.NOTE_BLOCK,"Note Block",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.REDSTONE,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.IRON_CHESTPLATE,"Metal Chestplate",Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT),
         new Puzzle(Items.IRON_LEGGINGS,"Metal Leggings",Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT),
         new Puzzle(Items.COMPASS,"Compass",Items.AIR,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.REDSTONE,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.AIR),
         new Puzzle(Items.IRON_PICKAXE,"Metal Pickaxe",Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.STICK,Items.AIR,Items.AIR,Items.STICK,Items.AIR),
         new Puzzle(Items.STONE_PICKAXE,"Stone Pickaxe",Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.AIR,Items.STICK,Items.AIR,Items.AIR,Items.STICK,Items.AIR),
         new Puzzle(Items.WOODEN_PICKAXE,"Wood Pickaxe",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.AIR,Items.STICK,Items.AIR,Items.AIR,Items.STICK,Items.AIR),
         new Puzzle(Items.BEEHIVE,"Beehive",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.HONEYCOMB,Items.HONEYCOMB,Items.HONEYCOMB,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.IRON_BLOCK,"Metal Block",Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT),
         new Puzzle(Items.PRISMARINE_STAIRS,"Prismarine Stairs",Items.PRISMARINE,Items.AIR,Items.AIR,Items.PRISMARINE,Items.PRISMARINE,Items.AIR,Items.PRISMARINE,Items.PRISMARINE,Items.PRISMARINE),
         new Puzzle(Items.OAK_STAIRS,"Wood Stairs",Items.OAK_PLANKS,Items.AIR,Items.AIR,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.AIR,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.COBBLESTONE_STAIRS,"Stone Stairs",Items.COBBLESTONE,Items.AIR,Items.AIR,Items.COBBLESTONE,Items.COBBLESTONE,Items.AIR,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE),
         new Puzzle(Items.BEACON,"Beacon",Items.GLASS,Items.GLASS,Items.GLASS,Items.GLASS,Items.NETHER_STAR,Items.GLASS,Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN),
         new Puzzle(Items.ENCHANTING_TABLE,"Enchanting Table",Items.AIR,Items.BOOK,Items.AIR,Items.DIAMOND,Items.OBSIDIAN,Items.DIAMOND,Items.OBSIDIAN,Items.OBSIDIAN,Items.OBSIDIAN),
         new Puzzle(Items.OBSERVER,"Observer",Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.REDSTONE,Items.REDSTONE,Items.QUARTZ,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE),
         new Puzzle(Items.DROPPER,"Dropper",Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.COBBLESTONE,Items.AIR,Items.COBBLESTONE,Items.COBBLESTONE,Items.REDSTONE,Items.COBBLESTONE),
         new Puzzle(Items.BOOKSHELF,"Bookshelf",Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.BOOK,Items.BOOK,Items.BOOK,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS),
         new Puzzle(Items.PAINTING,"Painting",Items.STICK,Items.STICK,Items.STICK,Items.STICK,Items.WHITE_WOOL,Items.STICK,Items.STICK,Items.STICK,Items.STICK),
         new Puzzle(Items.WHITE_BANNER,"Banner",Items.WHITE_WOOL,Items.WHITE_WOOL,Items.WHITE_WOOL,Items.WHITE_WOOL,Items.WHITE_WOOL,Items.WHITE_WOOL,Items.AIR,Items.STICK,Items.AIR),
         new Puzzle(Items.DAYLIGHT_DETECTOR,"Daylight Detector",Items.GLASS,Items.GLASS,Items.GLASS,Items.QUARTZ,Items.QUARTZ,Items.QUARTZ,Items.OAK_SLAB,Items.OAK_SLAB,Items.OAK_SLAB),
         new Puzzle(Items.HOPPER,"Hopper",Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.CHEST,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.AIR),
         new Puzzle(Items.SHIELD,"Shield",Items.OAK_PLANKS,Items.IRON_INGOT,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.OAK_PLANKS,Items.AIR,Items.OAK_PLANKS,Items.AIR),
         new Puzzle(Items.RAIL,"Metal Rail",Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.STICK,Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT),
         new Puzzle(Items.CAULDRON,"Metal Cauldron",Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.AIR,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT,Items.IRON_INGOT)
   };
   
   private static class Puzzle{
      private Item[] ingredients;
      private String name;
      private Item craftedItem;
      private PuzzleRestriction rest1;
      private PuzzleRestriction rest2;
      private ArrayList<Item> itemOptions;
      
      public Puzzle(Item item, String name, Item... ingredients){
         this.craftedItem = item;
         this.name = name;
         this.ingredients = ingredients;
      }
   
      public String getName(){
         return name;
      }
   
      public Puzzle generatePuzzle(){
         Puzzle puzzle = new Puzzle(this.craftedItem,this.name,this.ingredients);
         List<Pair<Item[],Integer>> cats = puzzle.quantizeCategories();
         if(cats.size() == 1){ // Only 1 restriction
            int num = (int) (Math.random() * cats.get(0).getRight());
            Random random = new Random();
            int type = random.nextInt(3) - 1;
            
            puzzle.setRest1(new PuzzleRestriction(cats.get(0).getLeft(),num,type));
         }else{
            int r1 = (int) (Math.random() * cats.size());
            int r2;
            do{
               r2 = (int) (Math.random() * cats.size());
            }while(r2 == r1);
            Random random = new Random();
            int num1 = (int) (Math.random() * cats.get(r1).getRight());
            int type1 = random.nextInt(3) - 1;
            int num2 = (int) (Math.random() * cats.get(r2).getRight());
            int type2 = random.nextInt(3) - 1;
            puzzle.setRest1(new PuzzleRestriction(cats.get(r1).getLeft(),num1,type1));
            puzzle.setRest2(new PuzzleRestriction(cats.get(r2).getLeft(),num2,type2));
         }
   
         puzzle.itemOptions = new ArrayList<>();
   
         puzzle.itemOptions.add(Items.AIR);
   
         for(Pair<Item[], Integer> cat : cats){
            Item[] catList = cat.getLeft();
            if(catList.length < 4){
               for(int i = 0; i < catList.length; i++){
                  Item item = catList[i];
                  if(!puzzle.itemOptions.contains(item)){
                     puzzle.itemOptions.add(item);
                  }
               }
            }else{
               int added = 0;
               do{
                  Item item = catList[(int)(Math.random()*catList.length)];
                  if(!puzzle.itemOptions.contains(item)){
                     puzzle.itemOptions.add(item);
                     added++;
                  }
               }while(added < 4);
            }
         }
         
         for(Item ingredient : ingredients){
            if(!puzzle.itemOptions.contains(ingredient) && getCategory(ingredient) == null){
               puzzle.itemOptions.add(ingredient);
            }
         }
         if(puzzle.rest1 != null){
            if(!puzzle.itemOptions.contains(puzzle.rest1.getItem())){
               puzzle.itemOptions.add(puzzle.rest1.getItem());
            }
         }
         if(puzzle.rest2 != null){
            if(!puzzle.itemOptions.contains(puzzle.rest2.getItem())){
               puzzle.itemOptions.add(puzzle.rest2.getItem());
            }
         }
         
         do{
            Item item = masterItemList[(int)(Math.random()*masterItemList.length)];
            if(!puzzle.itemOptions.contains(item)){
               puzzle.itemOptions.add(item);
            }
         }while(puzzle.itemOptions.size() < 20);
         
         Collections.shuffle(puzzle.itemOptions);
         
         return puzzle;
      }
   
      public ArrayList<Item> getItemOptions(){
         return itemOptions;
      }
   
      public Item getCraftedItem(){
         return craftedItem;
      }
   
      public boolean isValid(Item[] items){
         int rest1Count = 0;
         int rest2Count = 0;
   
         for(int i = 0; i < ingredients.length; i++){
            Item item = items[i];
            Item[] cat = getCategory(ingredients[i]);
            
            if(item.getDefaultStack().isOf(rest1.getItem())) rest1Count++;
            if(rest2 != null && item.getDefaultStack().isOf(rest2.getItem())) rest2Count++;
            
            if(cat == null){
               if(!item.getDefaultStack().isOf(ingredients[i])) {
                  //log(item+" is not of "+ingredients[i].toString());
                  return false;
               }
            }else{
               boolean found = false;
               for(Item catItem : cat){
                  if(!item.getDefaultStack().isOf(catItem)) found = true;
               }
               if(!found) return false;
            }
         }
   
         // -1 less than, 0 exactly, 1 at least
         if(rest1.getType() == -1){
            rest1Count = rest1.getNum() == 0 ? rest1Count-1 : rest1Count;
            if(rest1Count >= rest1.getNum()) return false;
         }else if(rest1.getType() == 0){
            if(rest1Count != rest1.getNum()) return false;
         }else if(rest1.getType() == 1){
            if(rest1Count < rest1.getNum()) return false;
         }
         if(rest2 != null){
            if(rest2.getType() == -1){
               rest2Count = rest2.getNum() == 0 ? rest2Count-1 : rest2Count;
               if(rest2Count >= rest2.getNum()) return false;
            }else if(rest2.getType() == 0){
               if(rest2Count != rest2.getNum()) return false;
            }else if(rest2.getType() == 1){
               if(rest2Count < rest2.getNum()) return false;
            }
         }
         
         return true;
      }
      
      private Item[] getCategory(Item item){
         for(Item[] category : categories){
            for(Item catItem : category){
               if(item.getDefaultStack().isOf(catItem)){
                  return category;
               }
            }
         }
         return null;
      }
      
      private List<Pair<Item[],Integer>> quantizeCategories(){
         HashMap<Item[],Integer> cats = new HashMap<>();
         for(Item[] category : categories){
            cats.put(category,0);
         }
   
         for(Item ingredient : ingredients){
            Item[] category = getCategory(ingredient);
            if(category != null){
               cats.put(category,cats.get(category)+1);
            }
         }
         List<Pair<Item[],Integer>> catList = new ArrayList<>();
         for(Map.Entry<Item[], Integer> entry : cats.entrySet()){
            if(entry.getValue() > 0)
               catList.add(new Pair<>(entry.getKey(), entry.getValue()));
         }
         return catList;
      }
      
      private void setRest1(PuzzleRestriction r1){
         rest1 = r1;
      }
   
      private void setRest2(PuzzleRestriction r2){
         rest2 = r2;
      }
      
      public PuzzleRestriction getRest1(){
         return rest1;
      }
   
      public PuzzleRestriction getRest2(){
         return rest2;
      }
   }
   
   private static class PuzzleRestriction{
      private Item[] list;
      private int num;
      private int type; // -1 less than, 0 exactly, 1 at least
      private Item item;
      
      public PuzzleRestriction(Item[] list, int num, int type){
         this.list = list;
         this.num = num;
         this.type = type;
         item = list[(int)(Math.random() * list.length)];
      }
      
      public Item[] getList(){return list;}
      public int getNum(){return num;}
      public int getType(){return type;}
      public Item getItem(){return item;}
      
      public Text getLabel(){
         String comp = "";
         switch(type){
            case -1 -> comp = "Less Than";
            case 0 -> comp = "Exactly";
            case 1 -> comp = "At Least";
         }
         String name = Text.translatable(item.getTranslationKey()).getString();
         return Text.literal("").append(Text.literal(comp).formatted(Formatting.YELLOW)).append(Text.literal(" "+num).formatted(Formatting.AQUA)).append(Text.literal(" "+name).formatted(Formatting.LIGHT_PURPLE));
      }
   }
   
   private static final Item[] cobble = {Items.COBBLESTONE,Items.COBBLED_DEEPSLATE,Items.BLACKSTONE};
   private static final Item[] glass = {Items.GLASS,Items.WHITE_STAINED_GLASS,Items.ORANGE_STAINED_GLASS,Items.MAGENTA_STAINED_GLASS,Items.LIGHT_BLUE_STAINED_GLASS,Items.YELLOW_STAINED_GLASS,Items.LIME_STAINED_GLASS,Items.PINK_STAINED_GLASS,Items.GRAY_STAINED_GLASS,Items.LIGHT_GRAY_STAINED_GLASS,Items.CYAN_STAINED_GLASS,Items.PURPLE_STAINED_GLASS,Items.BLUE_STAINED_GLASS,Items.BROWN_STAINED_GLASS,Items.GREEN_STAINED_GLASS,Items.RED_STAINED_GLASS,Items.BLACK_STAINED_GLASS};
   //private static final Item[] pane = {Items.GLASS_PANE,Items.WHITE_STAINED_GLASS_PANE,Items.ORANGE_STAINED_GLASS_PANE,Items.MAGENTA_STAINED_GLASS_PANE,Items.LIGHT_BLUE_STAINED_GLASS_PANE,Items.YELLOW_STAINED_GLASS_PANE,Items.LIME_STAINED_GLASS_PANE,Items.PINK_STAINED_GLASS_PANE,Items.GRAY_STAINED_GLASS_PANE,Items.LIGHT_GRAY_STAINED_GLASS_PANE,Items.CYAN_STAINED_GLASS_PANE,Items.PURPLE_STAINED_GLASS_PANE,Items.BLUE_STAINED_GLASS_PANE,Items.BROWN_STAINED_GLASS_PANE,Items.GREEN_STAINED_GLASS_PANE,Items.RED_STAINED_GLASS_PANE,Items.BLACK_STAINED_GLASS_PANE};
   private static final Item[] wood = {Items.OAK_PLANKS,Items.BIRCH_PLANKS,Items.SPRUCE_PLANKS,Items.JUNGLE_PLANKS,Items.ACACIA_PLANKS,Items.DARK_OAK_PLANKS,Items.MANGROVE_PLANKS,Items.WARPED_PLANKS,Items.CRIMSON_PLANKS};
   private static final Item[] slab = {Items.OAK_SLAB,Items.BIRCH_SLAB,Items.SPRUCE_SLAB,Items.JUNGLE_SLAB,Items.ACACIA_SLAB,Items.DARK_OAK_SLAB,Items.MANGROVE_SLAB,Items.WARPED_SLAB,Items.CRIMSON_SLAB};
   private static final Item[] log = {Items.OAK_LOG,Items.BIRCH_LOG,Items.SPRUCE_LOG,Items.JUNGLE_LOG,Items.ACACIA_LOG,Items.DARK_OAK_LOG,Items.MANGROVE_LOG,Items.WARPED_STEM,Items.CRIMSON_STEM};
   private static final Item[] prismarine = {Items.PRISMARINE,Items.PRISMARINE_BRICKS,Items.DARK_PRISMARINE};
   private static final Item[] wool = {Items.WHITE_WOOL,Items.ORANGE_WOOL,Items.MAGENTA_WOOL,Items.LIGHT_BLUE_WOOL,Items.YELLOW_WOOL,Items.LIME_WOOL,Items.PINK_WOOL,Items.GRAY_WOOL,Items.LIGHT_GRAY_WOOL,Items.CYAN_WOOL,Items.PURPLE_WOOL,Items.BLUE_WOOL,Items.BROWN_WOOL,Items.GREEN_WOOL,Items.RED_WOOL,Items.BLACK_WOOL};
   private static final Item[] metal = {Items.IRON_INGOT,Items.COPPER_INGOT,Items.GOLD_INGOT,Items.NETHERITE_INGOT};
   private static final Item[] obsidian = {Items.OBSIDIAN,Items.CRYING_OBSIDIAN};
   private static final Item[][] categories = {cobble,glass,wood,slab,log,prismarine,wool,metal,obsidian};
   
   private static final Item[] masterItemList = {
         Items.COBBLESTONE,Items.COBBLED_DEEPSLATE,Items.BLACKSTONE,
         Items.GLASS,Items.WHITE_STAINED_GLASS,Items.ORANGE_STAINED_GLASS,Items.MAGENTA_STAINED_GLASS,Items.LIGHT_BLUE_STAINED_GLASS,Items.YELLOW_STAINED_GLASS,Items.LIME_STAINED_GLASS,Items.PINK_STAINED_GLASS,Items.GRAY_STAINED_GLASS,Items.LIGHT_GRAY_STAINED_GLASS,Items.CYAN_STAINED_GLASS,Items.PURPLE_STAINED_GLASS,Items.BLUE_STAINED_GLASS,Items.BROWN_STAINED_GLASS,Items.GREEN_STAINED_GLASS,Items.RED_STAINED_GLASS,Items.BLACK_STAINED_GLASS,
         Items.OAK_PLANKS,Items.BIRCH_PLANKS,Items.SPRUCE_PLANKS,Items.JUNGLE_PLANKS,Items.ACACIA_PLANKS,Items.DARK_OAK_PLANKS,Items.MANGROVE_PLANKS,Items.WARPED_PLANKS,Items.CRIMSON_PLANKS,
         Items.OAK_SLAB,Items.BIRCH_SLAB,Items.SPRUCE_SLAB,Items.JUNGLE_SLAB,Items.ACACIA_SLAB,Items.DARK_OAK_SLAB,Items.MANGROVE_SLAB,Items.WARPED_SLAB,Items.CRIMSON_SLAB,
         Items.OAK_LOG,Items.BIRCH_LOG,Items.SPRUCE_LOG,Items.JUNGLE_LOG,Items.ACACIA_LOG,Items.DARK_OAK_LOG,Items.MANGROVE_LOG,Items.WARPED_STEM,Items.CRIMSON_STEM,
         Items.PRISMARINE,Items.PRISMARINE_BRICKS,Items.DARK_PRISMARINE,
         Items.WHITE_WOOL,Items.ORANGE_WOOL,Items.MAGENTA_WOOL,Items.LIGHT_BLUE_WOOL,Items.YELLOW_WOOL,Items.LIME_WOOL,Items.PINK_WOOL,Items.GRAY_WOOL,Items.LIGHT_GRAY_WOOL,Items.CYAN_WOOL,Items.PURPLE_WOOL,Items.BLUE_WOOL,Items.BROWN_WOOL,Items.GREEN_WOOL,Items.RED_WOOL,Items.BLACK_WOOL,
         Items.IRON_INGOT,Items.COPPER_INGOT,Items.GOLD_INGOT,Items.NETHERITE_INGOT,
         Items.OBSIDIAN,Items.CRYING_OBSIDIAN,
         Items.AIR,Items.REDSTONE,Items.ENDER_EYE,Items.DIAMOND,Items.BOOK,Items.STICK,Items.HONEYCOMB,Items.NETHER_STAR,Items.CHEST,Items.QUARTZ
   };
}

