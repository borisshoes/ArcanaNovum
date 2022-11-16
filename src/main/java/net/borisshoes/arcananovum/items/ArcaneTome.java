package net.borisshoes.arcananovum.items;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.gui.arcanetome.*;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.recipes.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

import static net.borisshoes.arcananovum.Arcananovum.devMode;
import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ArcaneTome extends MagicItem implements UsableItem {
   private IArcanaProfileComponent profile;
   private final int[] craftingSlots = {1,2,3,4,5,10,11,12,13,14,19,20,21,22,23,28,29,30,31,32,37,38,39,40,41};
   
   public ArcaneTome(){
      id = "arcane_tome";
      name = "Tome of Arcana Novum";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
   
      ItemStack item = new ItemStack(Items.KNOWLEDGE_BOOK);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Tome of Arcana Novum\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"The knowledge within shall be your \",\"italic\":false,\"color\":\"green\"},{\"text\":\"guide\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There is so much \",\"italic\":false,\"color\":\"green\"},{\"text\":\"new magic\",\"color\":\"light_purple\"},{\"text\":\" to explore...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"yellow\"},{\"text\":\" to open the tome.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Mundane\",\"italic\":false,\"color\":\"gray\",\"bold\":true},{\"text\":\" Magic Item\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
   
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand){
      openGui(playerEntity,TomeMode.PROFILE);
      return false;
   }
   
   @Override
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result){
      openGui(playerEntity,TomeMode.PROFILE);
      return false;
   }
   
   public void getOrCreateProfile(ServerPlayerEntity player){
      this.profile = PLAYER_DATA.get(player);
      if(profile.getLevel() == 0){
         // Profile needs initialization
         profile.setLevel(1);
         
         // Right now all recipes are unlocked
         for(MagicItem item : MagicItems.registry.values()){
            profile.addRecipe(item.getId());
         }
      }
      // update level from xp just in case levelling changed
      profile.setLevel(LevelUtils.levelFromXp(profile.getXP()));
   }
   
   public void openGui(PlayerEntity playerEntity, TomeMode mode){
      openGui(playerEntity,mode,new TomeGui.CompendiumSettings(),"");
   }
   
   public void openGui(PlayerEntity playerEntity, TomeMode mode, TomeGui.CompendiumSettings settings){
      openGui(playerEntity,mode,settings,"");
   }
   
   public void openGui(PlayerEntity playerEntity, TomeMode mode, TomeGui.CompendiumSettings settings, String data){
      if(!(playerEntity instanceof ServerPlayerEntity))
         return;
      ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
      getOrCreateProfile(player);
      TomeGui gui = null;
      if(mode == TomeMode.PROFILE){ // Profile
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildProfileGui(gui,player);
      }else if(mode == TomeMode.COMPENDIUM){ // Compendium
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildCompendiumGui(gui,player,settings);
      }else if(mode == TomeMode.CRAFTING){ // Crafting
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,mode,this,settings);
         buildCraftingGui(gui,player,data);
      }else if(mode == TomeMode.ITEM){ // Item
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildItemGui(gui,player,data);
      }else if(mode == TomeMode.RECIPE){ // Recipe
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,mode,this,settings);
         buildRecipeGui(gui,player,data);
      }else if(mode == TomeMode.TINKER){ // Tinker
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildTinkerGui(gui,player,null);
      }
      gui.setMode(mode);
      gui.open();
   }
   
   public void openRecipeGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,TomeMode.RECIPE,this,settings);
      buildRecipeGui(gui,player,id);
      gui.setMode(TomeMode.RECIPE);
      gui.open();
   }
   
   public void openItemGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,TomeMode.ITEM,this,settings);
      buildItemGui(gui,player,id);
      gui.setMode(TomeMode.ITEM);
      gui.open();
   }
   
   public void openTinkerGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, ItemStack item){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,TomeMode.TINKER,this,settings);
      buildTinkerGui(gui,player,item);
      gui.setMode(TomeMode.TINKER);
      gui.open();
   }
   
   public void buildProfileGui(TomeGui gui, ServerPlayerEntity player){
      gui.setMode(TomeMode.PROFILE);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GameProfile gameProfile = new GameProfile(player.getUuid(),null);
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal(player.getEntityName()+"'s ").formatted(Formatting.AQUA)).append(Text.literal("Arcane Profile").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Click").formatted(Formatting.YELLOW)).append(Text.literal(" for a brief overview of Arcana Novum!").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(4,head);
      
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to go to the Magic Items Page\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(49,GuiElementBuilder.from(book));
      
      int level = profile.getLevel();
      ItemStack lecturn = new ItemStack(Items.LECTERN);
      tag = lecturn.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Arcana Level\",\"italic\":false,\"color\":\"dark_green\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Arcana Level: "+level+"\",\"italic\":false,\"color\":\"green\"}]"));
      if(level == 100){
         loreList.add(NbtString.of("[{\"text\":\"Total Experience: "+LevelUtils.readableInt(profile.getXP())+"\",\"italic\":false,\"color\":\"green\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      }else{
         loreList.add(NbtString.of("[{\"text\":\"Experience: "+LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()))+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(level))+"\",\"italic\":false,\"color\":\"green\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      }
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"You can increase your arcana by crafting and using magic items!\",\"italic\":false,\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(10,GuiElementBuilder.from(lecturn));
      
      if(level == 100){
         for(int i = 11; i <= 16; i++){
            gui.setSlot(i,new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Text.literal("XP: MAX LEVEL").formatted(Formatting.GREEN)));
         }
      }else{
         int filled = (int)Math.round((double)LevelUtils.getCurLevelXp(profile.getXP())/LevelUtils.nextLevelNewXp(profile.getLevel()) * 6.0);
         for(int i = 11; i <= 16; i++){
            if(i >= filled+11){
               gui.setSlot(i,new GuiElementBuilder(Items.GLASS_BOTTLE).setName(Text.literal("XP: "+LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()))+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).formatted(Formatting.GREEN)));
         
            }else{
               gui.setSlot(i,new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Text.literal("XP: "+LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()))+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).formatted(Formatting.GREEN)));
            }
         }
      }
   
      ItemStack shelf = new ItemStack(Items.BOOKSHELF);
      tag = shelf.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Skill Points (Coming Soon!)\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Total Skill Points: "+0+"\",\"italic\":false,\"color\":\"aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Allocated Points: "+0+"/"+0+"\",\"italic\":false,\"color\":\"aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Points From Levelling: "+0+"\",\"italic\":false,\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Points From Achievements: "+0+"\",\"italic\":false,\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Allocate Skill Points to Upgrade Items!\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Earn Skill Points From Levelling Up or From Achievements!\",\"italic\":false,\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(19,GuiElementBuilder.from(shelf));
   
      int books = 0;
      for(int i = 20; i <= 25; i++){
         if(i >= books+20){
            gui.setSlot(i,new GuiElementBuilder(Items.BOOK).setName(Text.literal("Allocated Skill Points: "+0+"/"+0).formatted(Formatting.DARK_AQUA)));
         
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.ENCHANTED_BOOK).setName(Text.literal("Allocated Skill Points: "+0+"/"+0).formatted(Formatting.DARK_AQUA)));
         }
      }
   
      ItemStack crystal = new ItemStack(Items.END_CRYSTAL);
      tag = crystal.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Arcane Concentration\",\"italic\":false,\"color\":\"blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())+"\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Your max concentration increases with your level!\",\"italic\":false,\"color\":\"light_purple\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      
      List<String> concBreakdown = MagicItemUtils.getConcBreakdown(player);
      if(!concBreakdown.isEmpty()){
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Items Taking Concentration:\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         for(String item : concBreakdown){
            loreList.add(NbtString.of(item));
         }
      }
      
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(37,GuiElementBuilder.from(crystal));
   
      int used = (int)Math.ceil((double)MagicItemUtils.getUsedConcentration(player)/LevelUtils.concFromLevel(profile.getLevel()) * 6.0);
      boolean overConc = MagicItemUtils.getUsedConcentration(player) > LevelUtils.concFromLevel(profile.getLevel());
      for(int i = 38; i <= 43; i++){
         if(overConc){
            gui.setSlot(i,new GuiElementBuilder(Items.FIRE_CHARGE).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())).formatted(Formatting.RED)));
         }else if(i >= used+38){
            gui.setSlot(i,new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())).formatted(Formatting.AQUA)));
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.MAGMA_CREAM).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+LevelUtils.concFromLevel(profile.getLevel())).formatted(Formatting.AQUA)));
         }
         
      }
   
      gui.setTitle(Text.literal("Arcane Profile"));
   }
   
   
   public List<MagicItem> sortedFilteredItemList(TomeGui.CompendiumSettings settings){
      TomeFilter filterType = settings.getFilterType();
      TomeSort sortType = settings.getSortType();
      List<MagicItem> items;
      if(filterType != null){
         items = new ArrayList<>();
         for(MagicItem magicItem : MagicItems.registry.values().stream().toList()){
            if(TomeFilter.matchesFilter(filterType,magicItem)){
               items.add(magicItem);
            }
         }
      }else{
         items = new ArrayList<>(MagicItems.registry.values().stream().toList());
      }
      
      switch(sortType){
         case NAME -> {
            Comparator<MagicItem> nameComparator = Comparator.comparing(MagicItem::getName);
            items.sort(nameComparator);
         }
         case RARITY_DESC -> {
            Comparator<MagicItem> rarityDescComparator = (MagicItem i1, MagicItem i2) -> {
               int rarityCompare = (i2.getRarity().rarity - i1.getRarity().rarity);
               if(rarityCompare == 0){
                  return i1.getName().compareTo(i2.getName());
               }else{
                  return rarityCompare;
               }
            };
            items.sort(rarityDescComparator);
         }
         default -> Collections.sort(items);
      }
      return items;
   }
   
   public List<MagicItem> listToPage(List<MagicItem> items, int page){
      if(page <= 0){
         return items;
      }else if(28*(page-1) >= items.size()){
         return new ArrayList<>();
      }else{
         return items.subList(28*(page-1), Math.min(items.size(), 28*page));
      }
   }
   
   public void buildCompendiumGui(TomeGui gui, ServerPlayerEntity player){
      buildCompendiumGui(gui,player,new TomeGui.CompendiumSettings());
   }
   
   public void buildCompendiumGui(TomeGui gui, ServerPlayerEntity player, TomeGui.CompendiumSettings settings){
      gui.setMode(TomeMode.COMPENDIUM);
      List<MagicItem> items = sortedFilteredItemList(settings);
      List<MagicItem> pageItems = listToPage(items, settings.getPage());
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      GameProfile gameProfile = new GameProfile(player.getUuid(),null);
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal("Magic Items").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Click here").formatted(Formatting.GREEN)).append(Text.literal(" to return to the Profile Page").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(4,head);
   
      ItemStack anvil = new ItemStack(Items.ANVIL);
      NbtCompound tag = anvil.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Tinker With A Magic Item!\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to go to the Tinkering Menu\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(49,GuiElementBuilder.from(anvil));
   
      ItemStack filterItem = new ItemStack(Items.HOPPER);
      tag = filterItem.getOrCreateNbt();
      display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Filter Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(filterItem);
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal(""));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(TomeFilter.getColoredLabel(settings.getFilterType())));
      gui.setSlot(8,filterBuilt);
      
      ItemStack sortItem = new ItemStack(Items.NETHER_STAR);
      tag = sortItem.getOrCreateNbt();
      display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Sort Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(sortItem);
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal(""));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(TomeSort.getColoredLabel(settings.getSortType())));
      gui.setSlot(0,sortBuilt);
   
      ItemStack nextPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = nextPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Next Page ("+settings.getPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Next Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(53,GuiElementBuilder.from(nextPage));
   
      ItemStack prevPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = prevPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Previous Page ("+settings.getPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Previous Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(45,GuiElementBuilder.from(prevPage));
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               gui.setSlot((i*9+10)+j,GuiElementBuilder.from(pageItems.get(k).getPrefItem()).glow());
            }else{
               gui.setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      gui.setTitle(Text.literal("Item Compendium"));
   }
   
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
   public void buildTinkerGui(TomeGui gui, ServerPlayerEntity player, @Nullable ItemStack item){
      gui.setMode(TomeMode.TINKER);
   
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"green\"},{\"text\":\"to return to the Compendium.\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(10,GuiElementBuilder.from(book));
   
      ItemStack itemPage = new ItemStack(Items.ANVIL);
      tag = itemPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Item Page\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"green\"},{\"text\":\"to go to the Item Page and unlock Upgrades!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(16,GuiElementBuilder.from(itemPage));
   
      ItemStack nameItem = new ItemStack(Items.NAME_TAG);
      tag = nameItem.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Name Your Magic Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"to give your Magic Item a name!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(22,GuiElementBuilder.from(nameItem));
   
      ItemStack upgradePane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      tag = upgradePane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Upgrades (Coming Soon):\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Unlocked upgrades can be applied to enhance Magic Items!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
   
      for(int i = 0; i < 7; i++){
         gui.setSlot(28+i,GuiElementBuilder.from(upgradePane));
         gui.setSlot(37+i,GuiElementBuilder.from(upgradePane));
         gui.setSlot(46+i,GuiElementBuilder.from(upgradePane));
      }
   
      ItemStack itemWindow = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      tag = itemWindow.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Insert a Magic Item to Tinker with it\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Apply upgrades or rename your item!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(3,GuiElementBuilder.from(itemWindow));
      gui.setSlot(5,GuiElementBuilder.from(itemWindow));
      gui.setSlot(12,GuiElementBuilder.from(itemWindow));
      gui.setSlot(13,GuiElementBuilder.from(itemWindow));
      gui.setSlot(14,GuiElementBuilder.from(itemWindow));
   
      TinkerInventory inv = new TinkerInventory();
      TinkerInventoryListener listener = new TinkerInventoryListener(this,gui);
      inv.addListener(listener);
      gui.setSlotRedirect(4, new Slot(inv,0,0,0));
      
      if(item != null){
         inv.setStack(0,item);
      }
   
      gui.setTitle(Text.literal("Tinker Items"));
   }
   
   public void buildItemGui(TomeGui gui, ServerPlayerEntity player, String id){
      gui.setMode(TomeMode.ITEM);
      MagicItem magicItem = MagicItemUtils.getItemFromId(id);
      if(magicItem == null){
         gui.close();
         return;
      }
   
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      for(int i = 0; i < 9; i++){
         gui.setSlot(i,new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Item Lore\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to read about this Magic Item.\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(6,GuiElementBuilder.from(book));
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Item Recipe\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"green\"},{\"text\":\"to view this item's recipe!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(2,GuiElementBuilder.from(table));
   
      gui.setSlot(4,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
      
      ItemStack upgradePane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      tag = upgradePane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Upgrades (Coming Soon):\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Unlocked upgrades can be applied to enhance Magic Items!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
   
      int[] upgradeSlots = dynamicSlots[0];
      for(int i = 0; i < 7; i++){
         gui.setSlot(19+i,GuiElementBuilder.from(upgradePane));
         gui.setSlot(28+i,GuiElementBuilder.from(upgradePane));
      }
      for(int i = 0; i < upgradeSlots.length; i++){
         gui.clearSlot(19+upgradeSlots[i]);
         gui.clearSlot(28+upgradeSlots[i]);
      }
   
      ItemStack achievePane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      tag = achievePane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Achievements (Coming Soon):\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Earning Achievements Grants Skill Points!\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
   
      int[] achieveSlots = dynamicSlots[0];
      for(int i = 0; i < 7; i++){
         gui.setSlot(46+i,GuiElementBuilder.from(achievePane));
      }
      for(int i = 0; i < achieveSlots.length; i++){
         gui.clearSlot(46+achieveSlots[i]);
      }
   
      gui.setTitle(Text.literal(magicItem.getName()));
   }
   
   public void buildRecipeGui(TomeGui gui, ServerPlayerEntity player, String id){
      gui.setMode(TomeMode.RECIPE);
      MagicItem magicItem = MagicItemUtils.getItemFromId(id);
      if(magicItem == null){
         gui.close();
         return;
      }
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to return to the Compendium.\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(7,GuiElementBuilder.from(book));
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge this item!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(43,GuiElementBuilder.from(table));
   
      gui.setSlot(25,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
   
      MagicItemRecipe recipe = magicItem.getRecipe();
      MagicItemIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         gui.setSlot(craftingSlots[i], GuiElementBuilder.from(ingredient));
      }
   
      ItemStack recipeList = new ItemStack(Items.PAPER);
      HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
      tag = recipeList.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Total Ingredients\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"-----------------------\",\"italic\":false,\"color\":\"light_purple\"}]"));
      for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
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
         String ingredStr = "[{\"text\":\""+ingred.getKey()+"\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" - \",\"color\":\"dark_purple\"},{\"text\":\""+num+"\",\"color\":\"green\"}"+stackStr+"]";
         
         loreList.add(NbtString.of(ingredStr));
      }
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Does not include NBT Values\",\"italic\":true,\"color\":\"dark_purple\"}]"));
   
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(26,GuiElementBuilder.from(recipeList));
   
      gui.setTitle(Text.literal("Recipe for "+magicItem.getName()));
   }
   
   public void buildCraftingGui(TomeGui gui, ServerPlayerEntity player, String itemId){
      gui.setMode(TomeMode.CRAFTING);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      GameProfile gameProfile = new GameProfile(player.getUuid(),null);
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal(player.getEntityName()+"'s ").formatted(Formatting.AQUA)).append(Text.literal("Arcane Profile").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Click").formatted(Formatting.YELLOW)).append(Text.literal(" to go to your Profile").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(43,head);
   
      ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
      NbtCompound tag = book.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Magic Items\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to view a Magic Item Recipe\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(7,GuiElementBuilder.from(book));
   
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
      gui.setSlot(25,GuiElementBuilder.from(table));
   
      for(int i = 0; i < 25; i++){
         gui.setSlot(craftingSlots[i], new GuiElementBuilder(Items.AIR));
      }
      
      CraftingInventory inv = new CraftingInventory();
      CraftingInventoryListener listener = new CraftingInventoryListener(this,gui);
      inv.addListener(listener);
      for(int i = 0; i<25;i++){
         gui.setSlotRedirect(craftingSlots[i], new Slot(inv,i,0,0));
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
         }
      }
   
      gui.setTitle(Text.literal("Forge Items"));
   }
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nRarity: Empowered\\n\\nStrangely enough, this Tome is incredibly easy to craft compared to most other Magic Items, like it wants to share its knowledge.\\n\\nThe way the Eye of Ender is so naturally \"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nAttracted to the enchantment table is definitely curious.\\n\\nHowever, as a result of its ease of construction, it offers no Crafting XP like other Magic Items do.\\n\\nIt acts as a guide and forge for those who\"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nseek the secrets of Arcana Novum.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      MagicItemIngredient a = new MagicItemIngredient(Items.AIR,1,null);
      MagicItemIngredient t = new MagicItemIngredient(Items.ENCHANTING_TABLE,1,null);
      MagicItemIngredient e = new MagicItemIngredient(Items.ENDER_EYE,1,null);
      
      MagicItemIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a},
            {a,a,a,a,a}};
      return new MagicItemRecipe(ingredients);
   }
   
   public enum TomeMode{
      PROFILE,
      COMPENDIUM,
      CRAFTING,
      ITEM,
      RECIPE,
      NONE,
      TINKER
   }
   
   public enum TomeFilter{
      NONE("None"),
      MUNDANE("Mundane"),
      EMPOWERED("Empowered"),
      EXOTIC("Exotic"),
      LEGENDARY("Legendary"),
      MYTHICAL("Mythical"),
      ITEMS("Items"),
      BLOCKS("Blocks"),
      ARROWS("Arrows"),
      ARMOR("Armor"),
      EQUIPMENT("Equipment"),
      CHARMS("Charms");
   
      public final String label;
   
      TomeFilter(String label){
         this.label = label;
      }
   
      public static Text getColoredLabel(TomeFilter filter){
         MutableText text = Text.literal(filter.label);
      
         return switch(filter){
            case NONE -> text.formatted(Formatting.WHITE);
            case MUNDANE -> text.formatted(Formatting.GRAY);
            case EMPOWERED -> text.formatted(Formatting.GREEN);
            case EXOTIC -> text.formatted(Formatting.AQUA);
            case LEGENDARY -> text.formatted(Formatting.GOLD);
            case MYTHICAL -> text.formatted(Formatting.LIGHT_PURPLE);
            case ITEMS -> text.formatted(Formatting.DARK_AQUA);
            case BLOCKS -> text.formatted(Formatting.DARK_PURPLE);
            case ARROWS -> text.formatted(Formatting.RED);
            case ARMOR -> text.formatted(Formatting.BLUE);
            case EQUIPMENT -> text.formatted(Formatting.DARK_RED);
            case CHARMS -> text.formatted(Formatting.YELLOW);
         };
      }
   
      public static TomeFilter cycleFilter(TomeFilter filter, boolean backwards){
         TomeFilter[] filters = TomeFilter.values();
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
      
      public static boolean matchesFilter(TomeFilter filter, MagicItem item){
         if(filter == TomeFilter.NONE) return true;
         TomeFilter[] cats = item.getCategories();
         if(cats == null){
            log("WARNING!!! No categories found for: "+item.getName());
            return false;
         }
         for(TomeFilter category : cats){
            if(filter == category) return true;
         }
         return false;
      }
   }
   
   public enum TomeSort{
      RARITY_ASC("Rarity Ascending"),
      RARITY_DESC("Rarity Descending"),
      NAME("Alphabetical");
   
      public final String label;
   
      TomeSort(String label){
         this.label = label;
      }
   
      public static Text getColoredLabel(TomeSort sort){
         MutableText text = Text.literal(sort.label);
      
         return switch(sort){
            case RARITY_ASC -> text.formatted(Formatting.LIGHT_PURPLE);
            case RARITY_DESC -> text.formatted(Formatting.DARK_PURPLE);
            case NAME -> text.formatted(Formatting.GREEN);
         };
      }
      
      public static TomeSort cycleSort(TomeSort sort, boolean backwards){
         TomeSort[] sorts = TomeSort.values();
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
