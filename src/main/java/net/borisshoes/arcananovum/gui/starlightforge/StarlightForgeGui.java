package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.gui.VirtualInventoryGui;
import net.borisshoes.arcananovum.gui.arcanetome.CompendiumEntry;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.CRAFTING_SLOTS;
import static net.borisshoes.arcananovum.gui.arcanetome.TomeGui.DYNAMIC_SLOTS;

public class StarlightForgeGui extends VirtualInventoryGui<SimpleContainer> {
   private final StarlightForgeBlockEntity blockEntity;
   private final Level world;
   private StarlightForgeInventoryListener listener;
   private static final int[] FORGE_SLOTS = new int[]{1,2,3,10,11,12,19,20,21};
   private static final int[] SKILLED_POINTS = new int[]{0,1,2,3,4,5,6,8,10,12,15};
   private int mode; // 0 - Menu (hopper), 1 - Arcana Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - Compendium (9x6), 5 - Skilled Selection (9x2)
   private final TomeGui.CompendiumSettings settings;
   
   public StarlightForgeGui(MenuType<?> type, ServerPlayer player, StarlightForgeBlockEntity blockEntity, Level world, int mode, @Nullable TomeGui.CompendiumSettings settings){
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
      this.inventory = new SimpleContainer(25);
      this.listener = new StarlightForgeInventoryListener(this,blockEntity,world,mode);
      inventory.addListener(listener);
   }
   
   public int getMode(){
      return mode;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(mode == 0){ // Menu
         if(index == 1){
            blockEntity.openGui(2,player,"",settings);
         }else if(index == 3){
            blockEntity.openGui(1,player,"",settings);
         }
      }else if(mode == 1){ // Arcana Crafting
         if(index == 7){
            // Go to compendium
            MinecraftUtils.returnItems(inventory,player);
            blockEntity.openGui(4,player,"",settings);
         }else if(index == 25){
            ItemStack item = this.getSlot(index).getItemStack();
            if(ArcanaItemUtils.isArcane(item)){
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               ArcanaRecipe recipe = arcanaItem.getRecipe();
               
               if(!ArcanaNovum.data(player).hasResearched(arcanaItem)){
                  player.displayClientMessage(Component.literal("You must research this item first!").withStyle(ChatFormatting.RED),false);
                  return false;
               }
               
               boolean canApplySkilled = getSkilledOptions(arcanaItem,player).entrySet().stream().anyMatch(entry -> entry.getValue() > 0);
               if(canApplySkilled){
                  buildSkilledGui(arcanaItem.getId());
               }else{
                  ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inventory, blockEntity),player.getStringUUID(),0,player.level().getServer());
                  forgeItem(arcanaItem, newArcanaItem, recipe, null,type == ClickType.MOUSE_LEFT_SHIFT);
               }
            }
         }
      }else if(mode == 2){ // Equipment Forging
         if(index == 15){
            ItemStack stack = listener.getEnhancedStack(inventory);
            if(!stack.isEmpty()){
               listener.setUpdating();
               NonNullList<ItemStack> remainders = listener.getRemainders(inventory);
               NonNullList<ItemStack> ingredients = NonNullList.create();
               for(int i = 0; i < inventory.getContainerSize(); i++){
                  if(i < 9){
                     ingredients.add(inventory.removeItem(i,1)); // Remove 1 from ingredients
                  }else{
                     inventory.setItem(i, ItemStack.EMPTY); // Clear other slots
                  }
               }
               
               MinecraftUtils.returnItems(inventory,player);
               
               EnhancedForgingGui efg = new EnhancedForgingGui(player,this.blockEntity,stack,ingredients,remainders);
               efg.buildGui();
               efg.open();
            }
         }else if(index == 17){
            // Guide gui
            BookElementBuilder bookBuilder = getGuideBook();
            LoreGui loreGui = new LoreGui(player,bookBuilder,null,null,null);
            loreGui.open();
            MinecraftUtils.returnItems(inventory,player);
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
                     player.displayClientMessage(Component.literal("You Cannot Craft This Item").withStyle(ChatFormatting.RED),false);
                  }
               }else{
                  player.displayClientMessage(Component.literal("You must research this item first!").withStyle(ChatFormatting.RED),false);
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
                  player.displayClientMessage(Component.literal("You cannot apply this Augment!").withStyle(ChatFormatting.RED),false);
                  return true;
               }
               
               ArcanaRecipe recipe = arcanaItem.getRecipe();
               ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inventory, blockEntity),player.getStringUUID(),0,player.level().getServer());
               forgeItem(arcanaItem, newArcanaItem, recipe, new Tuple<>(augment,applicableLevel), type == ClickType.MOUSE_LEFT_SHIFT);
               close();
            }
         }else if(index == 40 && arcanaItem != null){
            ArcanaRecipe recipe = arcanaItem.getRecipe();
            ItemStack newArcanaItem = arcanaItem.addCrafter(arcanaItem.forgeItem(inventory, blockEntity),player.getStringUUID(),0,player.level().getServer());
            forgeItem(arcanaItem, newArcanaItem, recipe, null, type == ClickType.MOUSE_LEFT_SHIFT);
         }
      }
   
      return true;
   }
   
   private void forgeItem(ArcanaItem arcanaItem, ItemStack newArcanaItem, ArcanaRecipe recipe, @Nullable Tuple<ArcanaAugment, Integer> skillPair, boolean fastAnim){
      if(!(blockEntity.getLevel()instanceof ServerLevel world)) return;
      if(skillPair != null && skillPair.getB() > 0){
         ArcanaAugments.applyAugment(newArcanaItem, skillPair.getA().id, skillPair.getB(),false);
      }
      
      arcanaItem.buildItemLore(newArcanaItem,player.level().getServer());
      
      ItemStack[][] ingredients = new ItemStack[5][5];
      for(int i = 0; i < inventory.getContainerSize(); i++){
         ingredients[i/5][i%5] = inventory.getItem(i);
      }
      ItemStack[][] remainders = recipe.getRemainders(ingredients, blockEntity, settings.resourceLvl);
      for(int i = 0; i < inventory.getContainerSize(); i++){
         inventory.setItem(i,remainders[i/5][i%5]);
      }
      
      ArcanaEffectUtils.arcanaCraftingAnim(world,blockEntity.getBlockPos(),newArcanaItem,0,fastAnim ? 1.75 : 1);
      
      BorisLib.addTickTimerCallback(world, new GenericTimer(fastAnim ? (int) (350 / 1.75) : 350, () -> {
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
         
         Vec3 pos = blockEntity.getBlockPos().getCenter().add(0,2,0);
         Containers.dropItemStack(world,pos.x,pos.y,pos.z,newArcanaItem);
      }));
      
      if(fastAnim){
         close();
         blockEntity.openGui(4,player, arcanaItem.getId(), settings);
      }else{
         close();
      }
   }
   
   private HashMap<ArcanaAugment,Integer> getSkilledOptions(ArcanaItem arcanaItem, ServerPlayer player){
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
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).setName(Component.literal("Place Recipe Here >").withStyle(ChatFormatting.DARK_PURPLE)));
         }else if(i % 9 == 4){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).setName(Component.literal("< Place Recipe Here").withStyle(ChatFormatting.DARK_PURPLE)));
         }else{
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.DARK_COLOR)).setName(Component.empty()).hideTooltip());
         }
      }
      
      GuiElementBuilder bookItem = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      bookItem.setName((Component.literal("")
            .append(Component.literal("Read About Stardust Infusion").withStyle(ChatFormatting.GREEN))));
      setSlot(17,bookItem);
      
      GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
      craftingItem.setName((Component.literal("")
            .append(Component.literal("Forge Item").withStyle(ChatFormatting.AQUA))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click Here ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to forge an item once a recipe is loaded!").withStyle(ChatFormatting.DARK_AQUA)))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("").withStyle(ChatFormatting.DARK_AQUA)))));
      craftingItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("This slot will show an item once a valid recipe is loaded.").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      setSlot(15,craftingItem);
      
      for(int i = 0; i < FORGE_SLOTS.length; i++){
         setSlot(FORGE_SLOTS[i], new GuiElementBuilder(Items.AIR));
      }
      
      for(int i = 0; i< FORGE_SLOTS.length; i++){
         setSlotRedirect(FORGE_SLOTS[i], new Slot(inventory,i,0,0));
      }
      
      setTitle(Component.literal("Forge Equipment"));
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.ARCANA_COLOR)).setName(Component.literal("Starlight Forge").withStyle(ChatFormatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder equipmentItem = new GuiElementBuilder(Items.DIAMOND_CHESTPLATE).hideDefaultTooltip();
      equipmentItem.setName((Component.literal("")
            .append(Component.literal("Forge Equipment").withStyle(ChatFormatting.AQUA))));
      setSlot(1,equipmentItem);
      
      GuiElementBuilder arcanaItem = new GuiElementBuilder(Items.END_CRYSTAL);
      arcanaItem.setName((Component.literal("")
            .append(Component.literal("Forge Arcana Items").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(3,arcanaItem);
      
      setTitle(Component.literal("Starlight Forge"));
   }
   
   public void buildCraftingGui(String itemId){
      for(int i = 0; i < getSize(); i++){
         if(i%9 == 0 || i%9 == 6){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 8){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 7){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(35,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(15,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(33,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder book = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      book.setName(Component.literal("Forge Item").withStyle(ChatFormatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to forge an Arcana Item once a recipe is loaded!").withStyle(ChatFormatting.LIGHT_PURPLE))));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to view an Arcana Item Recipe").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(7,book);
      
      GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      table.setName(Component.literal("Forge Item").withStyle(ChatFormatting.DARK_PURPLE));
      table.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" to forge an Arcana Item once a recipe is loaded!").withStyle(ChatFormatting.LIGHT_PURPLE))));
      table.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      table.addLoreLine(TextUtils.removeItalics(Component.literal("This slot will show an Arcana Item once a valid recipe is loaded.").withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA)));
      setSlot(25,table);
      
      for(int i = 0; i < 25; i++){
         setSlot(CRAFTING_SLOTS[i], new GuiElementBuilder(Items.AIR));
      }
      
      boolean collect = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.MYSTIC_COLLECTION.id) >= 1;
      ArrayList<Container> inventories = collect ? blockEntity.getIngredientInventories() : new ArrayList<>();
      for(int i = 0; i<25;i++){
         setSlotRedirect(CRAFTING_SLOTS[i], new Slot(inventory,i,0,0));
      }
      
      if(itemId != null && !itemId.isEmpty()){
         ArcanaRecipe recipe = ArcanaItemUtils.getItemFromId(itemId).getRecipe();
         ArcanaIngredient[][] ingredients = recipe.getIngredients();
         Container playerInventory = player.getInventory();
         
         for(int i = 0; i < 25; i++){
            ArcanaIngredient ingredient = ingredients[i/5][i%5];
            if(ingredient.ingredientAsStack().isEmpty()) continue;
            List<ItemStack> matchingStacks = new ArrayList<>(); // Build a list of matching stacks
            
            // Check player's inventory
            for(int j = 0; j < playerInventory.getContainerSize(); j++){
               ItemStack invSlot = playerInventory.getItem(j);
               if(invSlot.isEmpty()) continue;
               
               if(ingredient.validStackIgnoreCount(invSlot)){
                  matchingStacks.add(invSlot);
               }
            }
            
            // Check nearby inventories (list is empty without Mystic Collection)
            for(Container inventory : inventories){
               for(int j = 0; j < inventory.getContainerSize(); j++){
                  ItemStack invSlot = inventory.getItem(j);
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
                  if(!ItemStack.isSameItemSameComponents(outerStack, innerStack) || innerStack == outerStack) continue;
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
                     totalStack.grow(removed.getCount());
                  }
               }
               inventory.setItem(i,totalStack);
            }
         }
         
         recipe.getForgeRequirement().forgeMeetsRequirement(blockEntity,true,player);
         
         HashMap<String, Tuple<Integer, ItemStack>> ingredList = recipe.getIngredientList();
         GuiElementBuilder recipeList = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
         recipeList.setName(Component.literal("Total Ingredients").withStyle(ChatFormatting.DARK_PURPLE));
         recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
         for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
            Component ingredStr = TomeGui.getIngredStr(ingred);
            recipeList.addLoreLine(TextUtils.removeItalics(ingredStr));
         }
         recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         int slotCount = 0;
         for(ArcanaItem item : recipe.getForgeRequirementList()){
            GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
            Component req = Component.literal("")
                  .append(Component.literal("Requires").withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(item.getTranslatedName().withStyle(ChatFormatting.AQUA));
            recipeList.addLoreLine(TextUtils.removeItalics(req));
            reqItem.setName(req);
            setSlot(slotCount,reqItem);
            slotCount += 9;
         }
         if(!recipe.getForgeRequirementList().isEmpty()) recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("Does not include item data").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE)));
         setSlot(43,recipeList);
         
      }
      
      setTitle(Component.literal("Forge Items"));
   }
   
   public void buildRecipeGui(String id){
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem == null){
         close();
         return;
      }
      
      for(int i = 0; i < getSize(); i++){
         if(i%9 == 0 || i%9 == 6){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 8){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 7){
            setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(35,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(15,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(33,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder returnBook = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      returnBook.setName((Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))));
      returnBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to return to the Arcana Items page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      setSlot(7,returnBook);
      
      ArcanaRecipe recipe = arcanaItem.getRecipe();
      
      setSlot(25,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      ArcanaIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(ArcanaItemUtils.isArcane(ingredient)) craftingElement.glow();
         setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
      
      HashMap<String, Tuple<Integer, ItemStack>> ingredList = recipe.getIngredientList();
      if(!(recipe instanceof ExplainRecipe)){
         boolean collect = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.MYSTIC_COLLECTION.id) >= 1;
         ArrayList<Container> inventories = collect ? blockEntity.getIngredientInventories() : new ArrayList<>();
         Container playerInventory = player.getInventory();
         HashMap<String,Integer> ingredCounts = new HashMap<>();
         
         for(int i = 0; i < 25; i++){
            ArcanaIngredient ingredient = ingredients[i / 5][i % 5];
            if(ingredient.ingredientAsStack().isEmpty() || ingredCounts.containsKey(ingredient.getName())) continue;
            Set<ItemStack> matchingStacks = new HashSet<>(); // Build a list of matching stacks
            
            // Check player's inventory
            for(int j = 0; j < playerInventory.getContainerSize(); j++){
               ItemStack invSlot = playerInventory.getItem(j);
               if(invSlot.isEmpty()) continue;
               
               if(ingredient.validStackIgnoreCount(invSlot)){
                  matchingStacks.add(invSlot);
               }
            }
            
            // Check nearby inventories (list is empty without Mystic Collection)
            for(Container inventory : inventories){
               for(int j = 0; j < inventory.getContainerSize(); j++){
                  ItemStack invSlot = inventory.getItem(j);
                  if(invSlot.isEmpty()) continue;
                  
                  if(ingredient.validStackIgnoreCount(invSlot)){
                     matchingStacks.add(invSlot);
                  }
               }
            }
            
            int totalCount = 0;
            for(ItemStack matchingStack : matchingStacks){
               totalCount += matchingStack.getCount();
            }
            ingredCounts.put(ingredient.getName(),totalCount);
         }
         
         
         GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
         table.setName(Component.literal("Forge Item").withStyle(ChatFormatting.DARK_PURPLE));
         table.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click Here").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" to forge this item!").withStyle(ChatFormatting.LIGHT_PURPLE))));
         table.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         table.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
         for(String key : ingredList.keySet()){ //✔✘
            int foundCount = ingredCounts.get(key);
            int neededCount = ingredList.get(key).getA();
            
            MutableComponent text = Component.literal("")
                  .append(Component.literal(foundCount >= neededCount ? "✔ " : "✘ ").withStyle(foundCount >= neededCount ? ChatFormatting.GREEN : ChatFormatting.RED))
                  .append(Component.literal(key).withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(LevelUtils.readableInt(foundCount)).withStyle(foundCount >= neededCount ? ChatFormatting.GREEN : ChatFormatting.RED))
                  .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(LevelUtils.readableInt(neededCount)).withStyle(foundCount >= neededCount ? ChatFormatting.GREEN : ChatFormatting.RED));
            table.addLoreLine(TextUtils.removeItalics(text));
         }
         
         setSlot(43,table);
      }
      
      GuiElementBuilder recipeList = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
      recipeList.setName(Component.literal("Total Ingredients").withStyle(ChatFormatting.DARK_PURPLE));
      recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
      for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
         Component ingredStr = TomeGui.getIngredStr(ingred);
         recipeList.addLoreLine(TextUtils.removeItalics(ingredStr));
      }
      recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      int slotCount = 0;
      for(ArcanaItem item : recipe.getForgeRequirementList()){
         GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
         Component req = Component.literal("")
               .append(Component.literal("Requires").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(item.getTranslatedName().withStyle(ChatFormatting.AQUA));
         recipeList.addLoreLine(TextUtils.removeItalics(req));
         reqItem.setName(req);
         setSlot(slotCount,reqItem);
         slotCount += 9;
      }
      if(!recipe.getForgeRequirementList().isEmpty()) recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      recipeList.addLoreLine(TextUtils.removeItalics(Component.literal("Does not include item data").withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_PURPLE)));
      setSlot(26,recipeList);
      
      setTitle(Component.literal("Recipe for ").append(arcanaItem.getTranslatedName()));
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
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.LIGHT_COLOR)).setName(Component.literal("")).hideTooltip());
         }else{
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).setName(Component.literal("Select an Augment to apply it").withStyle(ChatFormatting.DARK_PURPLE)));
         }
      }
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      setSlot(4,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
      int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
      
      for(int i = 0; i < augmentSlots.length; i++){
         ArcanaAugment augment = augments.get(i);
         int augmentLvl = profile.getAugmentLevel(augment.id);
         
         GuiElementBuilder augmentItem1 = GuiElementBuilder.from(augment.getDisplayItem());
         MutableComponent name = augment.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE);
         if(augmentLvl > 0){
            name.append(Component.literal("")
                  .append(Component.literal(" (Level ")).withStyle(ChatFormatting.BLUE)
                  .append(Component.literal(""+augmentLvl)).withStyle(ChatFormatting.DARK_AQUA)
                  .append(Component.literal(")")).withStyle(ChatFormatting.BLUE));
         }else{
            name.append(Component.literal("")
                  .append(Component.literal(" (")).withStyle(ChatFormatting.BLUE)
                  .append(Component.literal("LOCKED")).withStyle(ChatFormatting.DARK_AQUA)
                  .append(Component.literal(")")).withStyle(ChatFormatting.BLUE));
         }
         
         augmentItem1.hideDefaultTooltip().setName(name).addLoreLine(TextUtils.removeItalics(augment.getTierDisplay()));
         
         int applicableLevel = getSkilledOptions(arcanaItem,player).getOrDefault(augment,0);
         
         for(String s : augment.getDescription()){
            augmentItem1.addLoreLine(TextUtils.removeItalics(Component.literal(s).withStyle(ChatFormatting.GRAY)));
         }
         
         augmentItem1.addLoreLine(Component.literal(""));
         if(applicableLevel > 0){
            augmentItem1.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to apply ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(augment.getTranslatedName().withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" at Level ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(applicableLevel+"").withStyle(ChatFormatting.LIGHT_PURPLE))));
         }else{
            augmentItem1.addLoreLine(TextUtils.removeItalics(Component.literal("You cannot apply this Augment").withStyle(ChatFormatting.RED)));
         }
         if(augmentLvl > 0) augmentItem1.glow();
         
         setSlot(19+augmentSlots[i], augmentItem1);
      }
      
      GuiElementBuilder cancel = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.CANCEL)).hideDefaultTooltip();
      cancel.setName((Component.literal("")
            .append(Component.literal("Forgo Augmentation").withStyle(ChatFormatting.RED))));
      cancel.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("No Augmentations will be applied").withStyle(ChatFormatting.DARK_PURPLE)))));
      setSlot(40,cancel);
      
      setTitle(Component.literal("Skilled Augmentation Selection"));
   }
   
   public static BookElementBuilder getGuideBook(){
      BookElementBuilder book = new BookElementBuilder();
      List<Component> pages = new ArrayList<>();
      
      pages.add(Component.literal("   Stardust Infusion\n-------------------\nThe Starlight Forge has the capability to create equipment that is considerably stronger than equipment made in a crafting table. This is done through the use of Stardust, a material I discovered that comes from salvaging enchanted equipment").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("   Stardust Infusion\n-------------------\nin a Stellar Core. \nWhen attempting to forge a new piece of equipment the Forge displays a vision of celestial bodies in Astral Space. Using Stardust I can manipulate the vision to cause the celestial entities to interact and give rise to new ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("   Stardust Infusion\n-------------------\nstars. Each of the objects interacts uniquely with the others, and their layout changes each time I forge something.\nThe more stars that are present at the end of the vision, the more powerful the infusion results. ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("   Infused Equipment\n-------------------\nBased on my results, different equipment experiences different types of enhancements.\n\nAll equipment seems to have a significant buff in its durability, taking up to 50% more time before it breaks.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Infused Tools\n-------------------\nTools that undergo infusion gain an increase to their harvesting capabilities, putting them on par with tools that have already undergone high levels of enchantment. However, enchanting infused tools only gives marginal gains.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Infused Weapons\n-------------------\nWeapons that undergo infusion become lighter to wield and sharper. The strikes can deal up to 5 additional damage. The quickened strikes only manifests at higher infusion rates, but can increase the rate of attack by up to 0.5 strikes per second.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Infused Armor\n-------------------\nArmor is where Stardust really seems to shine. I have catalogued up to 4 unique effects of infusion on armor, ranging from increasing its base armor, its toughness, its resistance to knockback and even increasing constitution").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Infused Armor\n-------------------\nArmor can receive up to 5 extra points of protection and toughness as well as up to an additional 20% chance to mitigate knockback.\nAt extremely high levels of infusion, this armor has shown to boost my health by up to 2.5 hearts each.").withStyle(ChatFormatting.BLACK));
      
      pages.forEach(book::addPage);
      book.setAuthor("Arcana Novum");
      book.setTitle("Stardust Infusion");
      
      return book;
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
   public void onClose(){
      if(mode == 3){
         blockEntity.openGui(4,player,"",settings);
      }else if(mode == 4){
         blockEntity.openGui(1,player,"",settings);
      }
      
      MinecraftUtils.returnItems(inventory,player);
      super.onClose();
   }
   
   @Override
   public void close(){
      super.close();
   }
   
}
