package net.borisshoes.arcananovum.gui.starlightforge;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.gui.WatchedGui;
import net.borisshoes.arcananovum.gui.arcanetome.LoreGui;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.items.ArcaneTome.CRAFTING_SLOTS;

public class StarlightForgeGui extends SimpleGui implements WatchedGui {
   private final StarlightForgeBlockEntity blockEntity;
   private final World world;
   private StarlightForgeInventory inv;
   private StarlightForgeInventoryListener listener;
   private static final int[] forgeSlots = new int[]{1,2,3,10,11,12,19,20,21,13};
   private final int mode; // 0 - Menu (hopper), 1 - Magic Crafting (9x5), 2 - Equipment Forging (9x3), 3 - Recipe (9x5), 4 - Compendium (9x6)
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
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(mode == 0){ // Menu
         if(index == 1){
            blockEntity.openGui(2,player,"",settings);
         }else if(index == 3){
            blockEntity.openGui(1,player,"",settings);
         }
      }else if(mode == 1){ // Magic Crafting
         if(index == 7){
            // Go to compendium
            MiscUtils.returnItems(inv,player);
            blockEntity.openGui(4,player,"",settings);
         }else if(index == 25){
            ItemStack item = this.getSlot(index).getItemStack();
            if(MagicItemUtils.isMagic(item)){
               IArcanaProfileComponent profile = PLAYER_DATA.get(player);
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               MagicItemRecipe recipe = magicItem.getRecipe();
               Inventory inv = getSlotRedirect(1).inventory;
               
               ItemStack newMagicItem = magicItem.addCrafter(magicItem.forgeItem(inv),player.getUuidAsString(),false,player.getServer());
               if(newMagicItem == null){
                  return false;
               }
               
               if(!(magicItem instanceof ArcaneTome)){
                  double skillChance = new double[]{0, .25, .5, .75, 1, 1.25, 1.5, 1.75, 2}[settings.skillLvl];
                  List<ArcanaAugment> allAugs = ArcanaAugments.getAugmentsForItem(magicItem);
                  allAugs.removeIf(aug -> profile.getAugmentLevel(aug.id) <= 0);
                  if(Math.random() < skillChance && allAugs.size() > 0){
                     int ind = (int) (Math.random() * allAugs.size());
                     ArcanaAugment aug = allAugs.get(ind);
                     ArcanaAugments.applyAugment(newMagicItem, aug.id, Math.min(settings.skillLvl,(int)(Math.random()*profile.getAugmentLevel(aug.id)+1)));
                     if(Math.random() < (skillChance-1) && allAugs.size() > 1){
                        int newInd = (int) (Math.random() * allAugs.size());
                        while(newInd == ind){
                           newInd = (int) (Math.random() * allAugs.size());
                        }
                        ArcanaAugment aug2 = allAugs.get(newInd);
                        ArcanaAugments.applyAugment(newMagicItem, aug2.id, Math.min(settings.skillLvl,(int)(Math.random()*profile.getAugmentLevel(aug2.id)+1)));
                     }
                  }
                  magicItem.redoAugmentLore(newMagicItem);
               }
               
               
               if(!PLAYER_DATA.get(player).addCrafted(newMagicItem) && !(magicItem instanceof ArcaneTome)){
                  PLAYER_DATA.get(player).addXP(MagicRarity.getCraftXp(magicItem.getRarity()));
               }
               
               if(magicItem.getRarity() != MagicRarity.MUNDANE){
                  ArcanaAchievements.grant(player,ArcanaAchievements.INTRO_ARCANA.id);
                  ArcanaAchievements.progress(player,ArcanaAchievements.INTERMEDIATE_ARTIFICE.id,1);
               }
               if(magicItem.getRarity() == MagicRarity.LEGENDARY) ArcanaAchievements.grant(player,ArcanaAchievements.ARTIFICIAL_DIVINITY.id);
               if(recipe.getForgeRequirement().needsFletchery()){
                  ArcanaAchievements.setCondition(player,ArcanaAchievements.OVERLY_EQUIPPED_ARCHER.id,magicItem.getNameString(),true);
               }
               
               ItemStack[][] ingredients = new ItemStack[5][5];
               for(int i = 0; i < inv.size(); i++){
                  ingredients[i/5][i%5] = inv.getStack(i);
               }
               ItemStack[][] remainders = recipe.getRemainders(ingredients, blockEntity, settings.resourceLvl);
               for(int i = 0; i < inv.size(); i++){
                  inv.setStack(i,remainders[i/5][i%5]);
               }
               
               block: {
                  ItemEntity itemEntity;
                  boolean bl = player.getInventory().insertStack(newMagicItem);
                  if (!bl || !newMagicItem.isEmpty()) {
                     itemEntity = player.dropItem(newMagicItem, false);
                     if (itemEntity == null) break block;
                     itemEntity.resetPickupDelay();
                     itemEntity.setOwner(player.getUuid());
                     break block;
                  }
                  newMagicItem.setCount(1);
                  itemEntity = player.dropItem(newMagicItem, false);
                  if (itemEntity != null) {
                     itemEntity.setDespawnImmediately();
                  }
               }
            }
         }
      }else if(mode == 2){ // Equipment Forging
         if(index == 15){
            ItemStack stack = listener.getEnhancedStack(inv);
            if(!stack.isEmpty()){
               listener.setUpdating();
               DefaultedList<ItemStack> remainders = listener.getRemainders(inv);
               int stardustCount = inv.getStack(9).getCount();
               for(int i = 0; i < inv.size(); i++){
                  if(i < 9){
                     inv.removeStack(i,1); // Remove 1 from ingredients
                  }else{
                     inv.setStack(i,ItemStack.EMPTY); // Clear stardust
                  }
               }
               int influence = 0;
               if(ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.MOONLIT_FORGE.id) >= 1){
                  long timeOfDay = world.getTimeOfDay();
                  int day = (int) (timeOfDay/24000L % Integer.MAX_VALUE);
                  int curPhase = day % 8;
                  influence = Math.abs(-curPhase+4);
               }
               
               double percentile = EnhancedStatUtils.generatePercentile(stardustCount,influence);
               if(percentile >= 0.99){
                  ArcanaAchievements.grant(player,ArcanaAchievements.MASTER_CRAFTSMAN.id);
               }
               EnhancedStatUtils.enhanceItem(stack, percentile);
               PLAYER_DATA.get(player).addXP(stardustCount*10);
               
               SimpleInventory returnInv = new SimpleInventory(remainders.size()+1);
               returnInv.addStack(stack);
               for(ItemStack remainder : remainders){
                  returnInv.addStack(remainder);
               }
               MiscUtils.returnItems(returnInv,player);
               listener.finishUpdate();
               listener.onInventoryChanged(inv);
            }
         }else if(index == 17){
            // Guide gui
            ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
            writablebook.setNbt(getGuideBook());
            BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
            LoreGui loreGui = new LoreGui(player,bookBuilder,null,null,null);
            loreGui.open();
            MiscUtils.returnItems(inv,player);
         }
      }else if(mode == 3){ // Recipe
         ItemStack item = this.getSlot(25).getItemStack();
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         if(index == 7){
            blockEntity.openGui(4,player,"",settings);
         }else if(index == 43){
            blockEntity.openGui(1,player,magicItem.getId(),settings);
         }else if(index > 9 && index < 36 && (index % 9 == 1 || index % 9 == 2 || index % 9 == 3 || index % 9 == 4 ||index % 9 == 5)){
            ItemStack ingredStack = this.getSlot(index).getItemStack();
            MagicItem magicItem1 = MagicItemUtils.identifyItem(ingredStack);
            if(magicItem1 != null){
               blockEntity.openGui(3,player,magicItem1.getId(),settings);
            }
         }
      }else if(mode == 4){ // Compendium
         if(index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8){
            ItemStack item = this.getSlot(index).getItemStack();
            if(!item.isEmpty()){
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               if(magicItem.getRarity() == MagicRarity.MYTHICAL){
                  if(magicItem.getRecipe() != null){
                     blockEntity.openGui(3,player,magicItem.getId(),settings);
                  }else{
                     player.sendMessage(Text.literal("You Cannot Craft Mythical Items").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
                  }
               }else{
                  if(magicItem.getRecipe() != null){
                     blockEntity.openGui(3,player,magicItem.getId(),settings);
                  }else{
                     player.sendMessage(Text.literal("You Cannot Craft This Item").formatted(Formatting.RED),false);
                  }
               }
            }
         }else if(index == 0){
            boolean backwards = type == ClickType.MOUSE_RIGHT;
            boolean middle = type == ClickType.MOUSE_MIDDLE;
            if(middle){
               settings.setSortType(ArcaneTome.TomeSort.RECOMMENDED);
            }else{
               settings.setSortType(ArcaneTome.TomeSort.cycleSort(settings.getSortType(),backwards));
            }
            
            ArcaneTome.buildCompendiumGui(this,player,settings);
         }else if(index == 8){
            boolean backwards = type == ClickType.MOUSE_RIGHT;
            boolean middle = type == ClickType.MOUSE_MIDDLE;
            if(middle){
               settings.setFilterType(ArcaneTome.TomeFilter.NONE);
            }else{
               settings.setFilterType(ArcaneTome.TomeFilter.cycleFilter(settings.getFilterType(),backwards));
            }
            
            List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
            int numPages = (int) Math.ceil((float)items.size()/28.0);
            if(settings.getPage() > numPages){
               settings.setPage(numPages);
            }
            ArcaneTome.buildCompendiumGui(this,player,settings);
         }else if(index == 45){
            if(settings.getPage() > 1){
               settings.setPage(settings.getPage()-1);
               ArcaneTome.buildCompendiumGui(this,player,settings);
            }
         }else if(index == 53){
            List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
            int numPages = (int) Math.ceil((float)items.size()/28.0);
            if(settings.getPage() < numPages){
               settings.setPage(settings.getPage()+1);
               ArcaneTome.buildCompendiumGui(this,player,settings);
            }
         }
      }
   
      return true;
   }
   
   public void buildForgeGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         if(i % 9 < 4){
            setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.literal("Place Recipe Here > ").formatted(Formatting.DARK_PURPLE)));
         }else if(i % 9 == 4){
            setSlot(i,new GuiElementBuilder(Items.YELLOW_STAINED_GLASS_PANE).setName(Text.literal("| Place Stardust Here |").formatted(Formatting.GOLD)));
         }else{
            setSlot(i,new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Text.empty()));
         }
      }
      
      GuiElementBuilder bookItem = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      bookItem.setName((Text.literal("")
            .append(Text.literal("Read About Enhanced Forging").formatted(Formatting.GREEN))));
      setSlot(17,bookItem);
      
      GuiElementBuilder craftingItem = new GuiElementBuilder(Items.CRAFTING_TABLE);
      craftingItem.setName((Text.literal("")
            .append(Text.literal("Forge Item").formatted(Formatting.AQUA))));
      craftingItem.addLoreLine((Text.literal("")
            .append(Text.literal("Click Here ").formatted(Formatting.GREEN))
            .append(Text.literal("to forge an item once a recipe is loaded!").formatted(Formatting.DARK_AQUA))));
      craftingItem.addLoreLine((Text.literal("")
            .append(Text.literal("").formatted(Formatting.DARK_AQUA))));
      craftingItem.addLoreLine((Text.literal("")
            .append(Text.literal("This slot will show an item once a valid recipe is loaded.").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(15,craftingItem);
      
      for(int i = 0; i < forgeSlots.length; i++){
         setSlot(forgeSlots[i], new GuiElementBuilder(Items.AIR));
      }
      
      inv = new StarlightForgeInventory();
      listener = new StarlightForgeInventoryListener(this,blockEntity,world,1);
      inv.addListener(listener);
      for(int i = 0; i<forgeSlots.length;i++){
         if(forgeSlots[i] == 13){
            setSlotRedirect(forgeSlots[i], new StardustSlot(inv,i,0,0));
         }else{
            setSlotRedirect(forgeSlots[i], new Slot(inv,i,0,0));
         }
      }
      
      setTitle(Text.literal("Forge Equipment"));
   }
   
   public void buildMenuGui(){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.literal("Starlight Forge").formatted(Formatting.DARK_PURPLE)));
      }
      
      GuiElementBuilder equipmentItem = new GuiElementBuilder(Items.DIAMOND_CHESTPLATE).hideFlags();
      equipmentItem.setName((Text.literal("")
            .append(Text.literal("Forge Equipment").formatted(Formatting.AQUA))));
      setSlot(1,equipmentItem);
      
      GuiElementBuilder magicItem = new GuiElementBuilder(Items.END_CRYSTAL);
      magicItem.setName((Text.literal("")
            .append(Text.literal("Forge Magic Items").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(3,magicItem);
      
      setTitle(Text.literal("Starlight Forge"));
   }
   
   public void buildCraftingGui(String itemId){
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to view a Magic Item Recipe\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(7,GuiElementBuilder.from(book));
      
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge a Magic Item once a recipe is loaded!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"This slot will show a Magic Item once a valid recipe is loaded.\",\"italic\":true,\"color\":\"aqua\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(25,GuiElementBuilder.from(table));
      
      for(int i = 0; i < 25; i++){
         setSlot(CRAFTING_SLOTS[i], new GuiElementBuilder(Items.AIR));
      }
      
      boolean collect = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.MYSTIC_COLLECTION.id) >= 1;
      ArrayList<Inventory> inventories = blockEntity.getIngredientInventories();
      inv = new StarlightForgeInventory();
      listener = new StarlightForgeInventoryListener(this,blockEntity,world,0);
      inv.addListener(listener);
      for(int i = 0; i<25;i++){
         setSlotRedirect(CRAFTING_SLOTS[i], new Slot(inv,i,0,0));
      }
      
      if(itemId != null && !itemId.isEmpty()){
         MagicItemRecipe recipe = MagicItemUtils.getItemFromId(itemId).getRecipe();
         MagicItemIngredient[][] ingredients = recipe.getIngredients();
         Inventory playerInventory = player.getInventory();
         
         for(int i = 0; i < 25; i++){
            MagicItemIngredient ingredient = ingredients[i/5][i%5];
            
            for(int j = 0; j < playerInventory.size(); j++){
               ItemStack invSlot = playerInventory.getStack(j);
               
               if(ingredient.validStack(invSlot)){
                  ItemStack toMove = invSlot.split(ingredient.getCount());
                  if(invSlot.getCount() == 0){
                     invSlot = ItemStack.EMPTY;
                  }
                  inv.setStack(i,toMove);
                  playerInventory.setStack(j,invSlot);
                  break;
               }
            }
            
            if(collect){
               searchBlock: {
                  for(Inventory inventory : inventories){
                     for(int j = 0; j < inventory.size(); j++){
                        ItemStack invSlot = inventory.getStack(j);
                        if(invSlot.isEmpty()) continue;
                        
                        if(ingredient.validStack(invSlot)){
                           ItemStack toMove = invSlot.split(ingredient.getCount());
                           if(invSlot.getCount() == 0){
                              invSlot = ItemStack.EMPTY;
                           }
                           inv.setStack(i,toMove);
                           inventory.setStack(j,invSlot);
                           break searchBlock;
                        }
                     }
                  }
               }
            }
         }
         
         recipe.getForgeRequirement().forgeMeetsRequirement(blockEntity,true,player);
         
         
         ItemStack recipeList = new ItemStack(Items.PAPER);
         HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
         tag = recipeList.getOrCreateNbt();
         display = new NbtCompound();
         loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Total Ingredients\",\"italic\":false,\"color\":\"dark_purple\"}]");
         loreList.add(NbtString.of("[{\"text\":\"-----------------------\",\"italic\":false,\"color\":\"light_purple\"}]"));
         for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
            String ingredStr = getIngredString(ingred);
            
            loreList.add(NbtString.of(ingredStr));
         }
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         int slotCount = 0;
         for(MagicItem item : recipe.getForgeRequirementList()){
            loreList.add(NbtString.of("[{\"text\":\"Requires\",\"color\":\"green\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\""+item.getNameString()+"\",\"italic\":false,\"color\":\"aqua\"}]"));
            GuiElementBuilder reqItem = new GuiElementBuilder(item.getItem()).hideFlags().glow();
            reqItem.setName((Text.literal("")
                  .append(Text.literal("Requires ").formatted(Formatting.GREEN))
                  .append(Text.literal("a ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal(item.getNameString()).formatted(Formatting.AQUA))));
            setSlot(slotCount,reqItem);
            slotCount += 9;
         }
         if(!recipe.getForgeRequirementList().isEmpty()) loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Does not include item data\",\"italic\":true,\"color\":\"dark_purple\"}]"));
         
         display.put("Lore",loreList);
         tag.put("display",display);
         tag.putInt("HideFlags",103);
         setSlot(43,GuiElementBuilder.from(recipeList));
      }
      
      setTitle(Text.literal("Forge Items"));
   }
   
   public void buildRecipeGui(String id){
      MagicItem magicItem = MagicItemUtils.getItemFromId(id);
      if(magicItem == null){
         close();
         return;
      }
      
      for(int i = 0; i < getSize(); i++){
         clearSlot(i);
         setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GuiElementBuilder returnBook = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      returnBook.setName((Text.literal("")
            .append(Text.literal("Magic Items").formatted(Formatting.DARK_PURPLE))));
      returnBook.addLoreLine((Text.literal("")
            .append(Text.literal("Click ").formatted(Formatting.GREEN))
            .append(Text.literal("to return to the Magic Items page").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(7,returnBook);
      
      MagicItemRecipe recipe = magicItem.getRecipe();
      
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      NbtCompound tag = table.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge this item!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      if(!(recipe instanceof ExplainRecipe)){
         setSlot(43,GuiElementBuilder.from(table));
      }
      
      setSlot(25,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
      
      MagicItemIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(MagicItemUtils.isMagic(ingredient)) craftingElement.glow();
         setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
      
      ItemStack recipeList = new ItemStack(Items.PAPER);
      HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
      tag = recipeList.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Total Ingredients\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"-----------------------\",\"italic\":false,\"color\":\"light_purple\"}]"));
      for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
         String ingredStr = getIngredString(ingred);
         
         loreList.add(NbtString.of(ingredStr));
      }
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      int slotCount = 0;
      for(MagicItem item : recipe.getForgeRequirementList()){
         loreList.add(NbtString.of("[{\"text\":\"Requires\",\"color\":\"green\"},{\"text\":\" a \",\"color\":\"dark_purple\"},{\"text\":\""+item.getNameString()+"\",\"italic\":false,\"color\":\"aqua\"}]"));
         GuiElementBuilder reqItem = new GuiElementBuilder(item.getItem()).hideFlags().glow();
         reqItem.setName((Text.literal("")
               .append(Text.literal("Requires ").formatted(Formatting.GREEN))
               .append(Text.literal("a ").formatted(Formatting.DARK_PURPLE))
               .append(Text.literal(item.getNameString()).formatted(Formatting.AQUA))));
         setSlot(slotCount,reqItem);
         slotCount += 9;
      }
      if(!recipe.getForgeRequirementList().isEmpty()) loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Does not include item data\",\"italic\":true,\"color\":\"dark_purple\"}]"));
      
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      setSlot(26,GuiElementBuilder.from(recipeList));
      
      setTitle(Text.literal("Recipe for "+magicItem.getNameString()));
   }
   
   private String getIngredString(Map.Entry<String, Pair<Integer, ItemStack>> ingred){
      ItemStack ingredStack = ingred.getValue().getRight();
      int maxCount = ingredStack.getMaxCount();
      int num = ingred.getValue().getLeft();
      int stacks = num / maxCount;
      int rem = num % maxCount;
      String stackStr = "";
      if(num > maxCount){
         if(rem > 0){
            stackStr = "("+stacks+" Stacks + "+rem+")";
         }else{
            stackStr = "("+stacks+" Stacks)";
         }
         stackStr = ",{\"text\":\" - \",\"color\":\"dark_purple\"},{\"text\":\""+stackStr+"\",\"color\":\"yellow\"}";
      }
      return "[{\"text\":\""+ ingred.getKey()+"\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" - \",\"color\":\"dark_purple\"},{\"text\":\""+num+"\",\"color\":\"green\"}"+stackStr+"]";
   }
   
   public static NbtCompound getGuideBook(){
      NbtCompound bookLore = new NbtCompound();
      NbtList loreList = new NbtList();
      List<String> list = new ArrayList<>();
      list.add("{\"text\":\"   Enhanced Forging\\n-------------------\\nThe Starlight Forge allows you to craft Armor and Melee Weapons of higher quality than in a normal crafting table.\\n\\nThis is done through use of Stardust, a material gained by salvaging enchanted gear in a Stellar Core.\"}");
      list.add("{\"text\":\"   Enhanced Forging\\n-------------------\\nThe quality of the resulting enhancements is largely random, but more Stardust appears to have a positive effect on the enhancement strength.\\nIt also appears possible to influence the enhancements via Augmentation.\"}");
      list.add("{\"text\":\"    Enhanced Armor\\n-------------------\\nEnhanced Armor can receive up to a 50% boost in its raw protection, as well as gain additional toughness and knockback resistance.\\nThe highest quality of enhancements seem to also boost the constitution of the wearer.\"}");
      list.add("{\"text\":\"  Enhanced Weapons\\n-------------------\\nEnhanced Weaponry like Axes and Swords can see improvements in their weight and sharpness, resulting in quicker and more deadly strikes.\\n \\nThe extent of the enhancements also seems to be up to 50% of their base value.\"}");
      
      for(String s : list){
         loreList.add(NbtString.of(s));
      }
      
      bookLore.put("pages",loreList);
      bookLore.putString("author","Arcana Novum");
      bookLore.putString("filtered_title","enhanced_forging");
      bookLore.putString("title","enhanced_forging");
      
      return bookLore;
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
   
   @Override
   public BlockEntity getBlockEntity(){
      return blockEntity;
   }
   
   @Override
   public SimpleGui getGui(){
      return this;
   }
}
