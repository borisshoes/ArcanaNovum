package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.CRAFTING_SLOTS;
import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.DYNAMIC_SLOTS;

public class StarlightForgeGui extends SimpleGui {
   private final StarlightForgeBlockEntity blockEntity;
   private final World world;
   private StarlightForgeInventory inv;
   private StarlightForgeInventoryListener listener;
   private static final int[] FORGE_SLOTS = new int[]{1,2,3,10,11,12,19,20,21};
   private static final int[] SKILLED_POINTS = new int[]{0,1,2,3,4,5,6,8,10,12,15};
   private int mode; // 0 - Menu (hopper), 1 - Arcana Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - Compendium (9x6), 5 - Skilled Selection (9x2)
   private final TomeGui.CompendiumSettings settings;
   
   public StarlightForgeGui(ScreenHandlerType<?> type, ServerPlayerEntity player, StarlightForgeBlockEntity blockEntity, World world, int mode, @Nullable TomeGui.CompendiumSettings settings){
      super(type, player, false);
      this.blockEntity = blockEntity;
      this.world = world;
      this.mode = mode;
      if(settings == null){
         int skillLvl = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.SKILLED.id);
         int resourceLvl =  ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.RESOURCEFUL.id);
         this.settings = new TomeGui.CompendiumSettings(skillLvl,resourceLvl);
      }else{
         this.settings = settings;
      }
      this.inv = new StarlightForgeInventory();
      this.listener = new StarlightForgeInventoryListener(this,blockEntity,world,mode);
      inv.addListener(listener);
   }
   
   public int getMode(){
      return mode;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(mode == 0){ // Menu
         if(index == 1){
            blockEntity.openGui(2,player,"",settings);
         }else if(index == 3){
            blockEntity.openGui(1,player,"",settings);
         }
      }else if(mode == 1){ // Arcana Crafting
         if(index == 7){
            // Go to compendium
            MiscUtils.returnItems(inv,player);
            blockEntity.openGui(4,player,"",settings);
         }else if(index == 25){
            ItemStack item = this.getSlot(index).getItemStack();
            if(ArcanaItemUtils.isArcane(item)){
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               ArcanaRecipe recipe = arcanaItem.getRecipe();
               
               if(!ArcanaNovum.data(player).hasResearched(arcanaItem)){
                  player.sendMessage(Text.literal("You must research this item first!").formatted(Formatting.RED),false);
                  return false;
               }
               
               boolean canApplySkilled = getSkilledOptions(arcanaItem,player).entrySet().stream().anyMatch(entry -> entry.getValue() > 0);
               if(canApplySkilled){
                  buildSkilledGui(arcanaItem.getId());
               }else{
                  ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inv, blockEntity),player.getUuidAsString(),false,player.getServer());
                  forgeItem(arcanaItem, newArcanaItem, recipe, null,type == ClickType.MOUSE_LEFT_SHIFT);
               }
            }
         }
      }else if(mode == 2){ // Equipment Forging
         if(index == 15){
            ItemStack stack = listener.getEnhancedStack(inv);
            if(!stack.isEmpty()){
               listener.setUpdating();
               DefaultedList<ItemStack> remainders = listener.getRemainders(inv);
               DefaultedList<ItemStack> ingredients = DefaultedList.of();
               for(int i = 0; i < inv.size(); i++){
                  if(i < 9){
                     ingredients.add(inv.removeStack(i,1)); // Remove 1 from ingredients
                  }else{
                     inv.setStack(i,ItemStack.EMPTY); // Clear other slots
                  }
               }
               
               MiscUtils.returnItems(inv,player);
               
               EnhancedForgingGui efg = new EnhancedForgingGui(player,this.blockEntity,stack,ingredients,remainders);
               efg.buildGui();
               efg.open();
            }
         }else if(index == 17){
            // Guide gui
            BookElementBuilder bookBuilder = getGuideBook();
            LoreGui loreGui = new LoreGui(player,bookBuilder,null,null,null);
            loreGui.open();
            MiscUtils.returnItems(inv,player);
         }
      }else if(mode == 3){ // Recipe
         ItemStack item = this.getSlot(25).getItemStack();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem == null){
            return false;
         }
         if(index == 7){
            blockEntity.openGui(4,player,"",settings);
         }else if(index == 43){
            blockEntity.openGui(1,player, arcanaItem.getId(),settings);
         }else if(index > 9 && index < 36 && (index % 9 == 1 || index % 9 == 2 || index % 9 == 3 || index % 9 == 4 ||index % 9 == 5)){
            ItemStack ingredStack = this.getSlot(index).getItemStack();
            ArcanaItem arcanaItem1 = ArcanaItemUtils.identifyItem(ingredStack);
            if(arcanaItem1 != null){
               blockEntity.openGui(3,player, arcanaItem1.getId(),settings);
            }
         }
      }else if(mode == 4){ // Compendium
         if(index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8){
            ItemStack item = this.getSlot(index).getItemStack();
            if(!item.isEmpty()){
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               if(arcanaItem == null){
                  return false;
               }
               if(ArcanaNovum.data(player).hasResearched(arcanaItem)){
                  if(arcanaItem.getRecipe() != null){
                     blockEntity.openGui(3,player, arcanaItem.getId(),settings);
                  }else{
                     player.sendMessage(Text.literal("You Cannot Craft This Item").formatted(Formatting.RED),false);
                  }
               }else{
                  player.sendMessage(Text.literal("You must research this item first!").formatted(Formatting.RED),false);
               }
            }
         }else if(index == 0){
            boolean backwards = type == ClickType.MOUSE_RIGHT;
            boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
            if(shiftLeft){
               settings.setSortType(TomeGui.TomeSort.RECOMMENDED);
            }else{
               settings.setSortType(TomeGui.TomeSort.cycleSort(settings.getSortType(),backwards));
            }
            
            TomeGui.buildCompendiumGui(this,player,settings);
         }else if(index == 8){
            boolean backwards = type == ClickType.MOUSE_RIGHT;
            boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
            if(shiftLeft){
               settings.setFilterType(TomeGui.TomeFilter.NONE);
            }else{
               settings.setFilterType(TomeGui.TomeFilter.cycleFilter(settings.getFilterType(),backwards));
            }
            
            List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
            int numPages = (int) Math.ceil((float)items.size()/28.0);
            if(settings.getPage() > numPages){
               settings.setPage(numPages);
            }
            TomeGui.buildCompendiumGui(this,player,settings);
         }else if(index == 45){
            if(settings.getPage() > 1){
               settings.setPage(settings.getPage()-1);
               TomeGui.buildCompendiumGui(this,player,settings);
            }
         }else if(index == 53){
            List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
            int numPages = (int) Math.ceil((float)items.size()/28.0);
            if(settings.getPage() < numPages){
               settings.setPage(settings.getPage()+1);
               TomeGui.buildCompendiumGui(this,player,settings);
            }
         }
      }else if(mode == 5){ // Skilled selection
         ItemStack item = this.getSlot(4).getItemStack();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         
         if(index >= 19 && index <= 25 && arcanaItem != null){
            List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
            int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
            ArcanaAugment augment = null;
            for(int i = 0; i < augmentSlots.length; i++){
               if(index == 19 + augmentSlots[i]){
                  augment = augments.get(i);
                  break;
               }
            }
            
            if(augment != null){
               int applicableLevel = getSkilledOptions(arcanaItem,player).getOrDefault(augment,0);
               if(applicableLevel <= 0){
                  player.sendMessage(Text.literal("You cannot apply this Augment!").formatted(Formatting.RED),false);
                  return true;
               }
               
               ArcanaRecipe recipe = arcanaItem.getRecipe();
               ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inv, blockEntity),player.getUuidAsString(),false,player.getServer());
               forgeItem(arcanaItem, newArcanaItem, recipe, new Pair<>(augment,applicableLevel), type == ClickType.MOUSE_LEFT_SHIFT);
               close();
            }
         }else if(index == 40 && arcanaItem != null){
            ArcanaRecipe recipe = arcanaItem.getRecipe();
            ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inv, blockEntity),player.getUuidAsString(),false,player.getServer());
            forgeItem(arcanaItem, newArcanaItem, recipe, null, type == ClickType.MOUSE_LEFT_SHIFT);
         }
      }
   
      return true;
   }
   
   private void forgeItem(ArcanaItem arcanaItem, ItemStack newArcanaItem, ArcanaRecipe recipe, @Nullable Pair<ArcanaAugment, Integer> skillPair, boolean fastAnim){
      if(!(blockEntity.getWorld()instanceof ServerWorld world)) return;
      if(skillPair != null && skillPair.getRight() > 0){
         ArcanaAugments.applyAugment(newArcanaItem, skillPair.getLeft().id, skillPair.getRight(),false);
      }
      
      arcanaItem.buildItemLore(newArcanaItem,player.getServer());
      
      ItemStack[][] ingredients = new ItemStack[5][5];
      for(int i = 0; i < inv.size(); i++){
         ingredients[i/5][i%5] = inv.getStack(i);
      }
      ItemStack[][] remainders = recipe.getRemainders(ingredients, blockEntity, settings.resourceLvl);
      for(int i = 0; i < inv.size(); i++){
         inv.setStack(i,remainders[i/5][i%5]);
      }
      
      ParticleEffectUtils.arcanaCraftingAnim(world,blockEntity.getPos(),newArcanaItem,0,fastAnim ? 1.75 : 1);
      
      ArcanaNovum.addTickTimerCallback(world, new GenericTimer(fastAnim ? (int) (350 / 1.75) : 350, () -> {
         if(!ArcanaNovum.data(player).addCrafted(newArcanaItem) && !(arcanaItem instanceof ArcaneTome)){
            ArcanaNovum.data(player).addXP(ArcanaRarity.getCraftXp(arcanaItem.getRarity()));
         }
         
         if(arcanaItem.getRarity() != ArcanaRarity.MUNDANE){
            ArcanaAchievements.grant(player,ArcanaAchievements.INTRO_ARCANA.id);
            ArcanaAchievements.progress(player,ArcanaAchievements.INTERMEDIATE_ARTIFICE.id,1);
         }
         if(arcanaItem.getRarity() == ArcanaRarity.SOVEREIGN) ArcanaAchievements.grant(player,ArcanaAchievements.ARTIFICIAL_DIVINITY.id);
         if(recipe.getForgeRequirement().needsFletchery()){
            ArcanaAchievements.setCondition(player,ArcanaAchievements.OVERLY_EQUIPPED_ARCHER.id, arcanaItem.getNameString(),true);
         }
         
         Vec3d pos = blockEntity.getPos().toCenterPos().add(0,2,0);
         ItemScatterer.spawn(world,pos.x,pos.y,pos.z,newArcanaItem);
      }));
      
      close();
   }
   
   private HashMap<ArcanaAugment,Integer> getSkilledOptions(ArcanaItem arcanaItem, ServerPlayerEntity player){
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
      HashMap<ArcanaAugment,Integer> options = new HashMap<>();
      if(settings.skillLvl == 0) return options;
      ArcanaRarity maxRarity = ArcanaAugments.SKILLED.getTiers()[settings.skillLvl-1];
      
      for(ArcanaAugment augment : augments){
         ArcanaRarity[] tiers = augment.getTiers();
         int skillPoints = SKILLED_POINTS[settings.skillLvl];
         int applicableLevel = 0;
         int sumCost = 0;
         int unlockedLevel = ArcanaNovum.data(player).getAugmentLevel(augment.id);
         for(ArcanaRarity tier : tiers){
            sumCost += tier.rarity + 1;
            if(sumCost > skillPoints || applicableLevel+1 > unlockedLevel || tier.rarity > maxRarity.rarity){
               break;
            }else{
               applicableLevel++;
            }
         }
         options.put(augment,applicableLevel);
      }
      return options;
   }
   
   public void buildForgeGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         if(i % 9 < 4){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).setName(Text.literal("Place Recipe Here >").formatted(Formatting.DARK_PURPLE)));
         }else if(i % 9 == 4){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).setName(Text.literal("< Place Recipe Here").formatted(Formatting.DARK_PURPLE)));
         }else{
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG,ArcanaColors.DARK_COLOR)).setName(Text.empty()).hideTooltip());
         }
      }
      
      GuiElementBuilder bookItem = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      bookItem.setName((Text.literal("")
            .append(Text.literal("Read About Stardust Infusion").formatted(Formatting.GREEN))));
      setSlot(17,bookItem);
      
      GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
      craftingItem.setName((Text.literal("")
            .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
            .append(Text.literal("to forge an item once a recipe is loaded!").formatted(Formatting.DARK_AQUA)))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("").formatted(Formatting.DARK_AQUA)))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("This slot will show an item once a valid recipe is loaded.").formatted(Formatting.LIGHT_PURPLE)))));
      setSlot(15,craftingItem);
      
      for(int i = 0; i < FORGE_SLOTS.length; i++){
         setSlot(FORGE_SLOTS[i], new GuiElementBuilder(Items.AIR));
      }
      
      for(int i = 0; i< FORGE_SLOTS.length; i++){
         setSlotRedirect(FORGE_SLOTS[i], new Slot(inv,i,0,0));
      }
      
      setTitle(Text.literal("Forge Equipment"));
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,ArcanaColors.ARCANA_COLOR)).setName(Text.literal("Starlight Forge").formatted(Formatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder equipmentItem = new GuiElementBuilder(Items.DIAMOND_CHESTPLATE).hideDefaultTooltip();
      equipmentItem.setName((Text.literal("")
            .append(Text.literal("Forge Equipment").formatted(Formatting.AQUA))));
      setSlot(1,equipmentItem);
      
      GuiElementBuilder arcanaItem = new GuiElementBuilder(Items.END_CRYSTAL);
      arcanaItem.setName((Text.literal("")
            .append(Text.literal("Forge Arcana Items").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(3,arcanaItem);
      
      setTitle(Text.literal("Starlight Forge"));
   }
   
   public void buildCraftingGui(String itemId){
      for(int i = 0; i < getSize(); i++){
         if(i%9 == 0 || i%9 == 6){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 8){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 7){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_HORIZONTAL,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(35,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(15,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(33,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder book = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      book.setName(Text.literal("Forge Item").formatted(Formatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click Here").formatted(Formatting.GREEN))
            .append(Text.literal(" to forge an Arcana Item once a recipe is loaded!").formatted(Formatting.LIGHT_PURPLE))));
      book.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click ").formatted(Formatting.YELLOW))
            .append(Text.literal("to view an Arcana Item Recipe").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(7,book);
      
      GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      table.setName(Text.literal("Forge Item").formatted(Formatting.DARK_PURPLE));
      table.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click Here").formatted(Formatting.GREEN))
            .append(Text.literal(" to forge an Arcana Item once a recipe is loaded!").formatted(Formatting.LIGHT_PURPLE))));
      table.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      table.addLoreLine(TextUtils.removeItalics(Text.literal("This slot will show an Arcana Item once a valid recipe is loaded.").formatted(Formatting.ITALIC,Formatting.AQUA)));
      setSlot(25,table);
      
      for(int i = 0; i < 25; i++){
         setSlot(CRAFTING_SLOTS[i], new GuiElementBuilder(Items.AIR));
      }
      
      boolean collect = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.MYSTIC_COLLECTION.id) >= 1;
      ArrayList<Inventory> inventories = collect ? blockEntity.getIngredientInventories() : new ArrayList<>();
      for(int i = 0; i<25;i++){
         setSlotRedirect(CRAFTING_SLOTS[i], new Slot(inv,i,0,0));
      }
      
      if(itemId != null && !itemId.isEmpty()){
         ArcanaRecipe recipe = ArcanaItemUtils.getItemFromId(itemId).getRecipe();
         ArcanaIngredient[][] ingredients = recipe.getIngredients();
         Inventory playerInventory = player.getInventory();
         
         for(int i = 0; i < 25; i++){
            ArcanaIngredient ingredient = ingredients[i/5][i%5];
            if(ingredient.ingredientAsStack().isEmpty()) continue;
            List<ItemStack> matchingStacks = new ArrayList<>(); // Build a list of matching stacks
            
            // Check player's inventory
            for(int j = 0; j < playerInventory.size(); j++){
               ItemStack invSlot = playerInventory.getStack(j);
               if(invSlot.isEmpty()) continue;
               
               if(ingredient.validStackIgnoreCount(invSlot)){
                  matchingStacks.add(invSlot);
               }
            }
            
            // Check nearby inventories (list is empty without Mystic Collection)
            for(Inventory inventory : inventories){
               for(int j = 0; j < inventory.size(); j++){
                  ItemStack invSlot = inventory.getStack(j);
                  if(invSlot.isEmpty()) continue;
                  
                  if(ingredient.validStackIgnoreCount(invSlot)){
                     matchingStacks.add(invSlot);
                  }
               }
            }
            // Take from smaller stacks first to avoid clutter
            matchingStacks.sort(Comparator.comparingInt(ItemStack::getCount));
            
            boolean found = false;
            ArrayList<ItemStack> neededStacks = new ArrayList<>();
            for(ItemStack outerStack : matchingStacks){ // Find combinable stacks with the sufficient amount
               neededStacks.clear();
               int remaining = ingredient.getCount() - outerStack.getCount();
               neededStacks.add(outerStack);
               
               for(ItemStack innerStack : matchingStacks){
                  if(!ItemStack.areItemsAndComponentsEqual(outerStack, innerStack) || innerStack == outerStack) continue;
                  if(remaining <= 0) break;
                  remaining -= innerStack.getCount();
                  neededStacks.add(innerStack);
               }
               
               if(remaining <= 0){
                  found = true;
                  break;
               }
            }
            
            if(found){ // Take from selected stacks and combine them in the crafting inventory
               int remaining = ingredient.getCount();
               ItemStack totalStack = null;
               for(ItemStack neededStack : neededStacks){
                  int toRemove = Math.min(remaining, neededStack.getCount());
                  if(toRemove <= 0) continue;
                  if(totalStack == null){
                     totalStack = neededStack.split(toRemove);
                     remaining -= totalStack.getCount();
                  }else{
                     ItemStack removed = neededStack.split(toRemove);
                     remaining -= removed.getCount();
                     totalStack.increment(removed.getCount());
                  }
               }
               inv.setStack(i,totalStack);
            }
         }
         
         recipe.getForgeRequirement().forgeMeetsRequirement(blockEntity,true,player);
         
         HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
         GuiElementBuilder recipeList = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
         recipeList.setName(Text.literal("Total Ingredients").formatted(Formatting.DARK_PURPLE));
         recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("-----------------------").formatted(Formatting.LIGHT_PURPLE)));
         for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
            Text ingredStr = TomeGui.getIngredStr(ingred);
            recipeList.addLoreLine(TextUtils.removeItalics(ingredStr));
         }
         recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("")));
         int slotCount = 0;
         for(ArcanaItem item : recipe.getForgeRequirementList()){
            GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
            Text req = Text.literal("")
                  .append(Text.literal("Requires").formatted(Formatting.GREEN))
                  .append(Text.literal(" a ").formatted(Formatting.DARK_PURPLE))
                  .append(item.getTranslatedName().formatted(Formatting.AQUA));
            recipeList.addLoreLine(TextUtils.removeItalics(req));
            reqItem.setName(req);
            setSlot(slotCount,reqItem);
            slotCount += 9;
         }
         if(!recipe.getForgeRequirementList().isEmpty()) recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("")));
         recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("Does not include item data").formatted(Formatting.ITALIC,Formatting.DARK_PURPLE)));
         setSlot(43,recipeList);
         
      }
      
      setTitle(Text.literal("Forge Items"));
   }
   
   public void buildRecipeGui(String id){
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem == null){
         close();
         return;
      }
      
      MiscUtils.outlineGUI(this,ArcanaColors.ARCANA_COLOR,Text.empty());
      
      GuiElementBuilder returnBook = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      returnBook.setName((Text.literal("")
            .append(Text.literal("Arcana Items").formatted(Formatting.DARK_PURPLE))));
      returnBook.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Click ").formatted(Formatting.GREEN))
            .append(Text.literal("to return to the Arcana Items page").formatted(Formatting.LIGHT_PURPLE)))));
      setSlot(7,returnBook);
      
      ArcanaRecipe recipe = arcanaItem.getRecipe();
      
      GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      table.setName(Text.literal("Forge Item").formatted(Formatting.DARK_PURPLE));
      table.addLoreLine(TextUtils.removeItalics(Text.literal("")
            .append(Text.literal("Click Here").formatted(Formatting.GREEN))
            .append(Text.literal(" to forge this item!").formatted(Formatting.LIGHT_PURPLE))));
      if(!(recipe instanceof ExplainRecipe)) setSlot(43,table);
      
      
      setSlot(25,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      ArcanaIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(ArcanaItemUtils.isArcane(ingredient)) craftingElement.glow();
         setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
      
      HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
      GuiElementBuilder recipeList = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
      recipeList.setName(Text.literal("Total Ingredients").formatted(Formatting.DARK_PURPLE));
      recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("-----------------------").formatted(Formatting.LIGHT_PURPLE)));
      for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
         Text ingredStr = TomeGui.getIngredStr(ingred);
         recipeList.addLoreLine(TextUtils.removeItalics(ingredStr));
      }
      recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      int slotCount = 0;
      for(ArcanaItem item : recipe.getForgeRequirementList()){
         GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
         Text req = Text.literal("")
               .append(Text.literal("Requires").formatted(Formatting.GREEN))
               .append(Text.literal(" a ").formatted(Formatting.DARK_PURPLE))
               .append(item.getTranslatedName().formatted(Formatting.AQUA));
         recipeList.addLoreLine(TextUtils.removeItalics(req));
         reqItem.setName(req);
         setSlot(slotCount,reqItem);
         slotCount += 9;
      }
      if(!recipe.getForgeRequirementList().isEmpty()) recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("")));
      recipeList.addLoreLine(TextUtils.removeItalics(Text.literal("Does not include item data").formatted(Formatting.ITALIC,Formatting.DARK_PURPLE)));
      setSlot(26,recipeList);
      
      setTitle(Text.literal("Recipe for ").append(arcanaItem.getTranslatedName()));
   }
   
   public void buildSkilledGui(String id){
      mode = 5;
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(arcanaItem == null){
         close();
         return;
      }
      
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         if(i >= 19 && i <= 25){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,ArcanaColors.LIGHT_COLOR)).setName(Text.literal("")).hideTooltip());
         }else{
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.PAGE_BG,ArcanaColors.ARCANA_COLOR)).setName(Text.literal("Select an Augment to apply it").formatted(Formatting.DARK_PURPLE)));
         }
      }
      MiscUtils.outlineGUI(this,ArcanaColors.ARCANA_COLOR,Text.empty());
      
      setSlot(4,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
      int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
      
      for(int i = 0; i < augmentSlots.length; i++){
         ArcanaAugment augment = augments.get(i);
         int augmentLvl = profile.getAugmentLevel(augment.id);
         
         GuiElementBuilder augmentItem1 = GuiElementBuilder.from(augment.getDisplayItem());
         MutableText name = augment.getTranslatedName().formatted(Formatting.DARK_PURPLE);
         if(augmentLvl > 0){
            name.append(Text.literal("")
                  .append(Text.literal(" (Level ")).formatted(Formatting.BLUE)
                  .append(Text.literal(""+augmentLvl)).formatted(Formatting.DARK_AQUA)
                  .append(Text.literal(")")).formatted(Formatting.BLUE));
         }else{
            name.append(Text.literal("")
                  .append(Text.literal(" (")).formatted(Formatting.BLUE)
                  .append(Text.literal("LOCKED")).formatted(Formatting.DARK_AQUA)
                  .append(Text.literal(")")).formatted(Formatting.BLUE));
         }
         
         augmentItem1.hideDefaultTooltip().setName(name).addLoreLine(TextUtils.removeItalics(augment.getTierDisplay()));
         
         int applicableLevel = getSkilledOptions(arcanaItem,player).getOrDefault(augment,0);
         
         for(String s : augment.getDescription()){
            augmentItem1.addLoreLine(TextUtils.removeItalics(Text.literal(s).formatted(Formatting.GRAY)));
         }
         
         augmentItem1.addLoreLine(Text.literal(""));
         if(applicableLevel > 0){
            augmentItem1.addLoreLine(TextUtils.removeItalics(Text.literal("")
                  .append(Text.literal("Click").formatted(Formatting.AQUA))
                  .append(Text.literal(" to apply ").formatted(Formatting.DARK_PURPLE))
                  .append(augment.getTranslatedName().formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(" at Level ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal(applicableLevel+"").formatted(Formatting.LIGHT_PURPLE))));
         }else{
            augmentItem1.addLoreLine(TextUtils.removeItalics(Text.literal("You cannot apply this Augment").formatted(Formatting.RED)));
         }
         if(augmentLvl > 0) augmentItem1.glow();
         
         setSlot(19+augmentSlots[i], augmentItem1);
      }
      
      GuiElementBuilder cancel = GuiElementBuilder.from(GraphicalItem.with(GraphicItems.CANCEL)).hideDefaultTooltip();
      cancel.setName((Text.literal("")
            .append(Text.literal("Forgo Augmentation").formatted(Formatting.RED))));
      cancel.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("No Augmentations will be applied").formatted(Formatting.DARK_PURPLE)))));
      setSlot(40,cancel);
      
      setTitle(Text.literal("Skilled Augmentation Selection"));
   }
   
   public static BookElementBuilder getGuideBook(){
      BookElementBuilder book = new BookElementBuilder();
      List<Text> pages = new ArrayList<>();
      
      pages.add(Text.literal("   Stardust Infusion\n-------------------\nThe Starlight Forge has the capability to create equipment that is considerably stronger than equipment made in a crafting table. This is done through the use of Stardust, a material I discovered that comes from salvaging enchanted equipment").formatted(Formatting.BLACK));
      pages.add(Text.literal("   Stardust Infusion\n-------------------\nin a Stellar Core. \nWhen attempting to forge a new piece of equipment the Forge displays a vision of celestial bodies in Astral Space. Using Stardust I can manipulate the vision to cause the celestial entities to interact and give rise to new ").formatted(Formatting.BLACK));
      pages.add(Text.literal("   Stardust Infusion\n-------------------\nstars. Each of the objects interacts uniquely with the others, and their layout changes each time I forge something.\nThe more stars that are present at the end of the vision, the more powerful the infusion results. ").formatted(Formatting.BLACK));
      pages.add(Text.literal("   Infused Equipment\n-------------------\nBased on my results, different equipment experiences different types of enhancements.\n\nAll equipment seems to have a significant buff in its durability, taking up to 50% more time before it breaks.").formatted(Formatting.BLACK));
      pages.add(Text.literal("      Infused Tools\n-------------------\nTools that undergo infusion gain an increase to their harvesting capabilities, putting them on par with tools that have already undergone high levels of enchantment. However, enchanting infused tools only gives marginal gains.").formatted(Formatting.BLACK));
      pages.add(Text.literal("    Infused Weapons\n-------------------\nWeapons that undergo infusion become lighter to wield and sharper. The strikes can deal up to 5 additional damage. The quickened strikes only manifests at higher infusion rates, but can increase the rate of attack by up to 0.5 strikes per second.").formatted(Formatting.BLACK));
      pages.add(Text.literal("     Infused Armor\n-------------------\nArmor is where Stardust really seems to shine. I have catalogued up to 4 unique effects of infusion on armor, ranging from increasing its base armor, its toughness, its resistance to knockback and even increasing constitution").formatted(Formatting.BLACK));
      pages.add(Text.literal("     Infused Armor\n-------------------\nArmor can receive up to 5 extra points of protection and toughness as well as up to an additional 20% chance to mitigate knockback.\nAt extremely high levels of infusion, this armor has shown to boost my health by up to 2.5 hearts each.").formatted(Formatting.BLACK));
      
      pages.forEach(book::addPage);
      book.setAuthor("Arcana Novum");
      book.setTitle("Stardust Infusion");
      
      return book;
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
   public void onClose(){
      if(mode == 3){
         blockEntity.openGui(4,player,"",settings);
      }else if(mode == 4){
         blockEntity.openGui(1,player,"",settings);
      }
      
      MiscUtils.returnItems(inv,player);
   }
   
   @Override
   public void close(){
      super.close();
   }
   
}
