package net.borisshoes.arcananovum.items;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.core.polymer.MagicPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.gui.twilightanvil.TwilightAnvilGui;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemIngredient;
import net.borisshoes.arcananovum.recipes.arcana.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.borisshoes.arcananovum.ArcanaNovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.ArcanaRegistry.RECOMMENDED_LIST;

public class ArcaneTome extends MagicItem {
   public static final int[] CRAFTING_SLOTS = {1,2,3,4,5,10,11,12,13,14,19,20,21,22,23,28,29,30,31,32,37,38,39,40,41};
   private static final String TXT = "item/arcane_tome";
   
   public ArcaneTome(){
      id = "arcane_tome";
      name = "Tome of Arcana Novum";
      rarity = MagicRarity.MUNDANE;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.MUNDANE, ArcaneTome.TomeFilter.ITEMS};
      itemVersion = 1;
      vanillaItem = Items.KNOWLEDGE_BOOK;
      item = new ArcaneTomeItem(new FabricItemSettings().maxCount(1).fireproof());
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT));
      
      ItemStack stack = new ItemStack(item);
      NbtCompound tag = stack.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Tome of Arcana Novum\",\"italic\":false,\"bold\":true,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      
      setBookLore(makeLore());
      setRecipe(makeRecipe());
      stack.setNbt(addMagicNbt(tag));
      setPrefStack(stack);
   }
   
   @Override
   public NbtList getItemLore(@Nullable ItemStack itemStack){
      NbtList loreList = new NbtList();
      loreList.add(NbtString.of("[{\"text\":\"The knowledge within shall be your \",\"italic\":false,\"color\":\"green\"},{\"text\":\"guide\",\"color\":\"light_purple\"},{\"text\":\".\",\"color\":\"green\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"There is so much \",\"italic\":false,\"color\":\"green\"},{\"text\":\"new magic\",\"color\":\"light_purple\"},{\"text\":\" to explore...\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Right click\",\"italic\":false,\"color\":\"yellow\"},{\"text\":\" to open the tome.\",\"color\":\"green\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      return loreList;
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   public void openGui(PlayerEntity playerEntity, TomeMode mode, TomeGui.CompendiumSettings settings){
      openGui(playerEntity,mode,settings,"");
   }
   
   public void openGui(PlayerEntity playerEntity, TomeMode mode, TomeGui.CompendiumSettings settings, String data){
      if(!(playerEntity instanceof ServerPlayerEntity))
         return;
      ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
      TomeGui gui = null;
      if(mode == TomeMode.PROFILE){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildProfileGui(gui,player);
      }else if(mode == TomeMode.COMPENDIUM){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildCompendiumGui(gui,player,settings);
      }else if(mode == TomeMode.ITEM){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildItemGui(gui,player,data);
      }else if(mode == TomeMode.RECIPE){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,mode,this,settings);
         buildRecipeGui(gui,data);
      }else if(mode == TomeMode.ACHIEVEMENTS){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildAchievementsGui(gui,player,settings);
      }else if(mode == TomeMode.LEADERBOARD){
         gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,mode,this,settings);
         buildLeaderboardGui(gui,player,settings);
      }
      gui.setMode(mode);
      gui.open();
   }
   
   public void openRecipeGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X5,player,TomeMode.RECIPE,this,settings);
      buildRecipeGui(gui,id);
      gui.setMode(TomeMode.RECIPE);
      gui.open();
   }
   
   public void openItemGui(ServerPlayerEntity player, TomeGui.CompendiumSettings settings, String id){
      TomeGui gui = new TomeGui(ScreenHandlerType.GENERIC_9X6,player,TomeMode.ITEM,this,settings);
      buildItemGui(gui,player,id);
      gui.setMode(TomeMode.ITEM);
      gui.open();
   }
   
   public void buildProfileGui(TomeGui gui, ServerPlayerEntity player){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      gui.setMode(TomeMode.PROFILE);
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal(player.getNameForScoreboard()+"'s ").formatted(Formatting.AQUA)).append(Text.literal("Arcane Profile").formatted(Formatting.DARK_PURPLE))));
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
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"aqua\",\"bold\":false},{\"text\":\" \",\"bold\":false},{\"text\":\"to see the Leaderboard\",\"bold\":false,\"color\":\"dark_aqua\"}]"));
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
      int totalSkillPoints = profile.getTotalSkillPoints();
      int spentSkillPoints = profile.getSpentSkillPoints();
      int bonusSkillPoints = profile.getBonusSkillPoints();
      display.putString("Name","[{\"text\":\"Skill Points\",\"italic\":false,\"color\":\"dark_aqua\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Total Skill Points: "+totalSkillPoints+"\",\"italic\":false,\"color\":\"aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Available Points: "+(totalSkillPoints-spentSkillPoints)+"\",\"italic\":false,\"color\":\"aqua\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Points From Leveling: "+LevelUtils.getLevelSkillPoints(level)+"\",\"italic\":false,\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Points From Achievements: "+profile.getAchievementSkillPoints()+"\",\"italic\":false,\"color\":\"blue\"}]"));
      if(bonusSkillPoints != 0) loreList.add(NbtString.of("[{\"text\":\"Bonus Skill Points: "+bonusSkillPoints+"\",\"italic\":false,\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Allocate Skill Points to Augment Items!\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Earn Skill Points From Leveling Up or From Achievements!\",\"italic\":false,\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"light_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false},{\"text\":\" \",\"bold\":false},{\"text\":\"to see all Achievements\",\"bold\":false,\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(19,GuiElementBuilder.from(shelf));
   
      int books = (int)((double)spentSkillPoints/totalSkillPoints * 6.0);
      for(int i = 20; i <= 25; i++){
         if(i >= books+20){
            gui.setSlot(i,new GuiElementBuilder(Items.BOOK).setName(Text.literal("Allocated Skill Points: "+spentSkillPoints+"/"+totalSkillPoints).formatted(Formatting.DARK_AQUA)));
         
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.ENCHANTED_BOOK).setName(Text.literal("Allocated Skill Points: "+spentSkillPoints+"/"+totalSkillPoints).formatted(Formatting.DARK_AQUA)));
         }
      }
   
      ItemStack crystal = new ItemStack(Items.END_CRYSTAL);
      tag = crystal.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      int resolve = profile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromLevel(profile.getLevel(),resolve);
      display.putString("Name","[{\"text\":\"Arcane Concentration\",\"italic\":false,\"color\":\"blue\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+maxConc+"\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
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
   
      int used = (int)Math.ceil((double)MagicItemUtils.getUsedConcentration(player)/maxConc * 6.0);
      boolean overConc = MagicItemUtils.getUsedConcentration(player) > maxConc;
      for(int i = 38; i <= 43; i++){
         if(overConc){
            gui.setSlot(i,new GuiElementBuilder(Items.FIRE_CHARGE).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+maxConc).formatted(Formatting.RED)));
         }else if(i >= used+38){
            gui.setSlot(i,new GuiElementBuilder(Items.SLIME_BALL).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+maxConc).formatted(Formatting.AQUA)));
         }else{
            gui.setSlot(i,new GuiElementBuilder(Items.MAGMA_CREAM).setName(Text.literal("Concentration: "+MagicItemUtils.getUsedConcentration(player)+"/"+maxConc).formatted(Formatting.AQUA)));
         }
         
      }
   
      gui.setTitle(Text.literal("Arcane Profile"));
   }
   
   
   public static List<MagicItem> sortedFilteredItemList(TomeGui.CompendiumSettings settings){
      TomeFilter filterType = settings.getFilterType();
      TomeSort sortType = settings.getSortType();
      List<MagicItem> items;
      if(filterType != null){
         items = new ArrayList<>();
         for(MagicItem magicItem : ArcanaRegistry.MAGIC_ITEMS.values().stream().toList()){
            if(TomeFilter.matchesFilter(filterType,magicItem)){
               items.add(magicItem);
            }
         }
      }else{
         items = new ArrayList<>(ArcanaRegistry.MAGIC_ITEMS.values().stream().toList());
      }
      
      switch(sortType){
         case RECOMMENDED -> {
            items.sort(Comparator.comparingInt(RECOMMENDED_LIST::indexOf));
         }
         case NAME -> {
            Comparator<MagicItem> nameComparator = Comparator.comparing(MagicItem::getNameString);
            items.sort(nameComparator);
         }
         case RARITY_DESC -> {
            Comparator<MagicItem> rarityDescComparator = (MagicItem i1, MagicItem i2) -> {
               int rarityCompare = (i2.getRarity().rarity - i1.getRarity().rarity);
               if(rarityCompare == 0){
                  return i1.getNameString().compareTo(i2.getNameString());
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
   
   public static List<ArcanaAchievement> sortedFilteredAchievementList(ServerPlayerEntity player, TomeGui.CompendiumSettings settings){
      AchievementFilter filterType = settings.getAchFilterType();
      AchievementSort sortType = settings.getAchSortType();
      List<ArcanaAchievement> achs;
      if(filterType != null){
         achs = new ArrayList<>();
         for(ArcanaAchievement achievement : ArcanaAchievements.registry.values().stream().toList()){
            if(AchievementFilter.matchesFilter(player, filterType,achievement)){
               achs.add(achievement);
            }
         }
      }else{
         achs = new ArrayList<>(ArcanaAchievements.registry.values().stream().toList());
      }
      
      switch(sortType){
         case RECOMMENDED -> {
            achs.sort(Comparator.comparingInt(ach -> RECOMMENDED_LIST.indexOf(ach.getMagicItem())));
         }
         case NAME -> {
            Comparator<ArcanaAchievement> nameComparator = Comparator.comparing(ArcanaAchievement::getName);
            achs.sort(nameComparator);
         }
         case RARITY_DESC -> {
            Comparator<ArcanaAchievement> rarityDescComparator = (ArcanaAchievement i1, ArcanaAchievement i2) -> {
               int rarityCompare = (i2.xpReward - i1.xpReward);
               if(rarityCompare == 0){
                  return i1.getName().compareTo(i2.getName());
               }else{
                  return rarityCompare;
               }
            };
            achs.sort(rarityDescComparator);
         }
         case RARITY_ASC -> {
            Comparator<ArcanaAchievement> rarityAscComparator = (ArcanaAchievement i1, ArcanaAchievement i2) -> {
               int rarityCompare = (i1.xpReward - i2.xpReward);
               if(rarityCompare == 0){
                  return i1.getName().compareTo(i2.getName());
               }else{
                  return rarityCompare;
               }
            };
            achs.sort(rarityAscComparator);
         }
      }
      return achs;
   }
   
   public static List<UUID> sortedFilteredLeaderboardList(TomeGui.CompendiumSettings settings){
      LeaderboardFilter filterType = settings.getLeaderFilterType();
      LeaderboardSort sortType = settings.getLeaderSortType();
      List<UUID> players = ArcanaNovum.PLAYER_XP_TRACKER.keySet().stream().toList();
      List<UUID> filteredSortedPlayers = new ArrayList<>();
      if(filterType != null){
         for(UUID player : players){
            if(LeaderboardFilter.matchesFilter(player, filterType)){
               filteredSortedPlayers.add(player);
            }
         }
         
      }else{
         filteredSortedPlayers = new ArrayList<>(players.stream().toList());
      }
      HashMap<UUID,List<String>> invertedAchList = ArcanaAchievements.getInvertedTracker();
      
      switch(sortType){
         case XP_DESC -> {
            filteredSortedPlayers.sort(Comparator.comparingInt(id -> -ArcanaNovum.PLAYER_XP_TRACKER.get(id)));
         }
         case XP_ASC -> {
            filteredSortedPlayers.sort(Comparator.comparingInt(ArcanaNovum.PLAYER_XP_TRACKER::get));
         }
         case ACHIEVES_DESC -> {
            Comparator<UUID> achieveCountComparator = (UUID i1, UUID i2) -> {
               int achCount1 = invertedAchList.containsKey(i1) ? invertedAchList.get(i1).size() : 0;
               int achCount2 = invertedAchList.containsKey(i2) ? invertedAchList.get(i2).size() : 0;
               int countCompare = (achCount2 - achCount1);
               if(countCompare == 0){
                  return ArcanaNovum.PLAYER_XP_TRACKER.get(i2) -  ArcanaNovum.PLAYER_XP_TRACKER.get(i1);
               }else{
                  return countCompare;
               }
            };
            filteredSortedPlayers.sort(achieveCountComparator);
         }
         case SKILL_POINTS_DESC -> {
            Comparator<UUID> achieveCountComparator = (UUID i1, UUID i2) -> {
               int skillCount1 = 0; int skillCount2 = 0;
               if(invertedAchList.containsKey(i1)){
                  for(String achId : invertedAchList.get(i1)){
                     ArcanaAchievement ach = ArcanaAchievements.registry.get(achId);
                     skillCount1 += ach == null ? 0 : ach.pointsReward;
                  }
               }
               if(invertedAchList.containsKey(i2)){
                  for(String achId : invertedAchList.get(i2)){
                     ArcanaAchievement ach = ArcanaAchievements.registry.get(achId);
                     skillCount2 += ach == null ? 0 : ach.pointsReward;
                  }
               }
               
               int countCompare = (skillCount2 - skillCount1);
               if(countCompare == 0){
                  return ArcanaNovum.PLAYER_XP_TRACKER.get(i2) -  ArcanaNovum.PLAYER_XP_TRACKER.get(i1);
               }else{
                  return countCompare;
               }
            };
            filteredSortedPlayers.sort(achieveCountComparator);
         }
         case NAME -> {
            Comparator<UUID> nameComparator = Comparator.comparing(playerID -> {
               GameProfile profile = ArcanaNovum.SERVER.getUserCache().getByUuid(playerID).orElse(null);
               return profile == null ? "" : profile.getName();
            });
            filteredSortedPlayers.sort(nameComparator);
         }
      }
      return filteredSortedPlayers;
   }
   
   public static <T> List<T> listToPage(List<T> items, int page){
      if(page <= 0){
         return items;
      }else if(28*(page-1) >= items.size()){
         return new ArrayList<>();
      }else{
         return items.subList(28*(page-1), Math.min(items.size(), 28*page));
      }
   }
   
   public static void buildCompendiumGui(SimpleGui gui, ServerPlayerEntity player, TomeGui.CompendiumSettings settings){
      if(gui instanceof TomeGui tomeGui) tomeGui.setMode(TomeMode.COMPENDIUM);
      List<MagicItem> items = sortedFilteredItemList(settings);
      List<MagicItem> pageItems = listToPage(items, settings.getPage());
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
   
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal("Magic Items").formatted(Formatting.DARK_PURPLE))));
      if(gui instanceof TomeGui){
         head.addLoreLine((Text.literal("").append(Text.literal("Click here").formatted(Formatting.AQUA)).append(Text.literal(" to return to the Profile Page").formatted(Formatting.LIGHT_PURPLE))));
         head.addLoreLine((Text.literal("").append(Text.literal("Click an item").formatted(Formatting.GREEN)).append(Text.literal(" to view its page").formatted(Formatting.LIGHT_PURPLE))));
         head.addLoreLine((Text.literal("").append(Text.literal("Right Click an item").formatted(Formatting.YELLOW)).append(Text.literal(" to see its recipe").formatted(Formatting.LIGHT_PURPLE))));
         
      }else{
         head.addLoreLine((Text.literal("").append(Text.literal("Click an item").formatted(Formatting.AQUA)).append(Text.literal(" to see its recipe").formatted(Formatting.LIGHT_PURPLE))));
      }
      gui.setSlot(4,head);
   
      ItemStack filterItem = new ItemStack(Items.HOPPER);
      NbtCompound tag = filterItem.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
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
      NbtList loreList = new NbtList();
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
   
   public void buildAchievementsGui(TomeGui gui, ServerPlayerEntity player, TomeGui.CompendiumSettings settings){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      gui.setMode(TomeMode.ACHIEVEMENTS);
      List<ArcanaAchievement> items = sortedFilteredAchievementList(player,settings);
      List<ArcanaAchievement> pageItems = listToPage(items, settings.getAchPage());
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal("Magic Items").formatted(Formatting.DARK_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Click here").formatted(Formatting.AQUA)).append(Text.literal(" to return to the Profile Page").formatted(Formatting.LIGHT_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Right Click here").formatted(Formatting.GREEN)).append(Text.literal(" to go to the Items Page").formatted(Formatting.LIGHT_PURPLE))));
      head.addLoreLine((Text.literal("").append(Text.literal("Click an item").formatted(Formatting.YELLOW)).append(Text.literal(" to view its page").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(4,head);
      
      ItemStack filterItem = new ItemStack(Items.HOPPER);
      NbtCompound tag = filterItem.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Filter Achievements\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(filterItem);
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal(""));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(AchievementFilter.getColoredLabel(settings.getAchFilterType())));
      gui.setSlot(8,filterBuilt);
      
      ItemStack sortItem = new ItemStack(Items.NETHER_STAR);
      tag = sortItem.getOrCreateNbt();
      display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Sort Achievements\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(sortItem);
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal(""));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(AchievementSort.getColoredLabel(settings.getAchSortType())));
      gui.setSlot(0,sortBuilt);
      
      ItemStack nextPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = nextPage.getOrCreateNbt();
      display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Next Page ("+settings.getAchPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Next Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(53,GuiElementBuilder.from(nextPage));
      
      ItemStack prevPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = prevPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Previous Page ("+settings.getAchPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Previous Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(45,GuiElementBuilder.from(prevPage));
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               ArcanaAchievement baseAch = pageItems.get(k);
               ArcanaAchievement profileAchievement = profile.getAchievement(baseAch.getMagicItem().getId(),baseAch.id);
               ArcanaAchievement achievement = profileAchievement != null ? profileAchievement : baseAch;
               
               ItemStack displayItem = achievement.getDisplayItem();
               NbtCompound nbt = displayItem.getOrCreateNbt();
               nbt.putString("magicItemId",achievement.getMagicItem().getId());
               GuiElementBuilder achievementItem = GuiElementBuilder.from(displayItem);
               achievementItem.hideFlags().setName(Text.literal(achievement.name+" - "+achievement.getMagicItem().getNameString()).formatted(Formatting.LIGHT_PURPLE))
                     .addLoreLine(Text.literal("")
                           .append(Text.literal(""+achievement.xpReward).formatted(Formatting.AQUA))
                           .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                           .append(Text.literal("  |  ").formatted(Formatting.DARK_AQUA))
                           .append(Text.literal(""+achievement.pointsReward).formatted(Formatting.AQUA))
                           .append(Text.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").formatted(Formatting.DARK_AQUA)));
   
               for(String s : achievement.getDescription()){
                  achievementItem.addLoreLine(Text.literal(s).formatted(Formatting.GRAY));
               }
   
               MutableText[] statusText = achievement.getStatusDisplay(player);
               if(statusText != null){
                  for(MutableText mutableText : statusText){
                     achievementItem.addLoreLine(mutableText);
                  }
               }
               
               List<UUID> achPlayers = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
               if(achPlayers == null || achPlayers.isEmpty()){
                  achievementItem.addLoreLine(Text.literal("")
                        .append(Text.literal("No Arcanists have achieved this.").formatted(Formatting.DARK_PURPLE)));
               }else{
                  int allArcanists = (int) ArcanaNovum.PLAYER_XP_TRACKER.values().stream().filter(xp -> xp > 1).count();
                  int acquiredCount = achPlayers.size();
                  DecimalFormat df = new DecimalFormat("#0.00");
                  achievementItem.addLoreLine(Text.literal("")
                        .append(Text.literal("Acquired by ").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal(acquiredCount+"").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(" Arcanists (").formatted(Formatting.DARK_PURPLE))
                        .append(Text.literal(df.format((100*(double)acquiredCount)/((double)allArcanists))+"%").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(")").formatted(Formatting.DARK_PURPLE)));
               }
               
               if(profile.hasAcheivement(achievement.getMagicItem().getId(),achievement.id)) achievementItem.glow();
               
               gui.setSlot((i*9+10)+j,achievementItem);
            }else{
               gui.setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      gui.setTitle(Text.literal("All Arcana Achievements"));
   }
   
   public void buildLeaderboardGui(TomeGui gui, ServerPlayerEntity player, TomeGui.CompendiumSettings settings){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      gui.setMode(TomeMode.LEADERBOARD);
      List<UUID> items = sortedFilteredLeaderboardList(settings);
      List<UUID> pageItems = listToPage(items, settings.getLeaderboardPage());
      HashMap<UUID,List<String>> achievementMap = ArcanaAchievements.getInvertedTracker();
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      int numAchievements = ArcanaAchievements.registry.size();
      
      for(int i = 0; i < gui.getSize(); i++){
         gui.clearSlot(i);
         gui.setSlot(i,new GuiElementBuilder(Items.PURPLE_STAINED_GLASS_PANE).setName(Text.empty()));
      }
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.server);
      head.setName((Text.literal("").append(Text.literal(player.getNameForScoreboard()).formatted(Formatting.AQUA))));
      head.addLoreLine((Text.literal("").append(Text.literal("Arcana Level: ").formatted(Formatting.DARK_PURPLE)).append(Text.literal(""+profile.getLevel()).formatted(Formatting.LIGHT_PURPLE))));
      if(profile.getLevel() == 100){
         head.addLoreLine((Text.literal("").append(Text.literal("Total Experience: ").formatted(Formatting.DARK_GREEN)).append(Text.literal(LevelUtils.readableInt(profile.getXP())).formatted(Formatting.GREEN))));
      }else{
         head.addLoreLine((Text.literal("")
               .append(Text.literal("Experience: ").formatted(Formatting.DARK_GREEN))
               .append(Text.literal(LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()))).formatted(Formatting.GREEN))
               .append(Text.literal("/").formatted(Formatting.DARK_GREEN))
               .append(Text.literal(LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).formatted(Formatting.GREEN))));
      }
      head.addLoreLine((Text.literal("")
            .append(Text.literal("Achievements: ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(LevelUtils.readableInt(profile.totalAcquiredAchievements())).formatted(Formatting.AQUA))
            .append(Text.literal("/").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(LevelUtils.readableInt(numAchievements)).formatted(Formatting.AQUA))));
      head.addLoreLine(Text.literal(""));
      head.addLoreLine((Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to return to the profile page").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(4,head);
      
      ItemStack filterItem = new ItemStack(Items.HOPPER);
      NbtCompound tag = filterItem.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Filter Achievements\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(filterItem);
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE)));
      filterBuilt.addLoreLine(Text.literal(""));
      filterBuilt.addLoreLine(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(LeaderboardFilter.getColoredLabel(settings.getLeaderFilterType())));
      gui.setSlot(8,filterBuilt);
      
      ItemStack sortItem = new ItemStack(Items.NETHER_STAR);
      tag = sortItem.getOrCreateNbt();
      display = new NbtCompound();
      display.putString("Name","[{\"text\":\"Sort Achievements\",\"italic\":false,\"color\":\"dark_purple\"}]");
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(sortItem);
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Middle Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE)));
      sortBuilt.addLoreLine(Text.literal(""));
      sortBuilt.addLoreLine(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(LeaderboardSort.getColoredLabel(settings.getLeaderSortType())));
      gui.setSlot(0,sortBuilt);
      
      ItemStack nextPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = nextPage.getOrCreateNbt();
      display = new NbtCompound();
      NbtList loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Next Page ("+settings.getLeaderboardPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Next Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(53,GuiElementBuilder.from(nextPage));
      
      ItemStack prevPage = new ItemStack(Items.SPECTRAL_ARROW);
      tag = prevPage.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Previous Page ("+settings.getLeaderboardPage()+"/"+numPages+")\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click\",\"italic\":false,\"color\":\"aqua\"},{\"text\":\" to go to the Previous Page\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(45,GuiElementBuilder.from(prevPage));
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               UUID playerId = pageItems.get(k);
               int playerXp = ArcanaNovum.PLAYER_XP_TRACKER.get(playerId);
               int playerLevel = LevelUtils.levelFromXp(playerXp);
               GameProfile playerGameProf;
               GuiElementBuilder playerItem;
               try{
                  playerGameProf = player.getServer().getUserCache().getByUuid(playerId).orElseThrow();
                  playerItem = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(playerGameProf,player.server);
                  playerItem.setName(Text.literal(playerGameProf.getName()).formatted(Formatting.LIGHT_PURPLE));
               }catch(Exception e){
                  playerItem = new GuiElementBuilder(Items.BARRIER);
                  playerItem.setName(Text.literal("<UNKNOWN>").formatted(Formatting.LIGHT_PURPLE));
               }
               playerItem.hideFlags();
               
               playerItem.addLoreLine((Text.literal("").append(Text.literal("Arcana Level: ").formatted(Formatting.DARK_PURPLE)).append(Text.literal(""+playerLevel).formatted(Formatting.LIGHT_PURPLE))));
               if(playerLevel == 100){
                  playerItem.addLoreLine((Text.literal("").append(Text.literal("Total Experience: ").formatted(Formatting.DARK_GREEN)).append(Text.literal(LevelUtils.readableInt(playerXp)).formatted(Formatting.GREEN))));
               }else{
                  playerItem.addLoreLine((Text.literal("")
                        .append(Text.literal("Experience: ").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(LevelUtils.readableInt(LevelUtils.getCurLevelXp(playerXp))).formatted(Formatting.GREEN))
                        .append(Text.literal("/").formatted(Formatting.DARK_GREEN))
                        .append(Text.literal(LevelUtils.readableInt(LevelUtils.nextLevelNewXp(playerLevel))).formatted(Formatting.GREEN))));
               }
               int playerAchievements = achievementMap.containsKey(playerId) ? achievementMap.get(playerId).size() : 0;
               playerItem.addLoreLine((Text.literal("")
                     .append(Text.literal("Achievements: ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(LevelUtils.readableInt(playerAchievements)).formatted(Formatting.AQUA))
                     .append(Text.literal("/").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(LevelUtils.readableInt(numAchievements)).formatted(Formatting.AQUA))));
               
               gui.setSlot((i*9+10)+j,playerItem);
            }else{
               gui.setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      gui.setTitle(Text.literal("Arcana Leaderboard"));
   }
   
   private static final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
   public static void buildItemGui(SimpleGui gui, ServerPlayerEntity player, String id){
      if(gui instanceof TomeGui tomeGui) tomeGui.setMode(TomeMode.ITEM);
      boolean isTwilightAnvil = gui instanceof TwilightAnvilGui;
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
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
      if(!isTwilightAnvil){
         gui.setSlot(6,GuiElementBuilder.from(book));
      }
   
      ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
      tag = table.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Item Recipe\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"green\"},{\"text\":\"to view this item's recipe!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      if(!isTwilightAnvil){
         gui.setSlot(2, GuiElementBuilder.from(table));
      }
   
      gui.setSlot(4,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
      
      ItemStack augmentPane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      tag = augmentPane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Augments:\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Unlocked augments can be applied to enhance Magic Items!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
      int[] augmentSlots = dynamicSlots[augments.size()];
      for(int i = 0; i < 7; i++){
         gui.setSlot(19+i,GuiElementBuilder.from(augmentPane));
         gui.setSlot(28+i,GuiElementBuilder.from(augmentPane));
      }
      for(int i = 0; i < augmentSlots.length; i++){
         ArcanaAugment augment = augments.get(i);
         gui.clearSlot(19+augmentSlots[i]);
         gui.clearSlot(28+augmentSlots[i]);
   
         int augmentLvl = profile.getAugmentLevel(augment.id);
         
         GuiElementBuilder augmentItem1 = new GuiElementBuilder(augment.getDisplayItem().getItem());
         augmentItem1.hideFlags().setName(Text.literal(augment.name).formatted(Formatting.DARK_PURPLE)).addLoreLine(augment.getTierDisplay());
   
         for(String s : augment.getDescription()){
            augmentItem1.addLoreLine(Text.literal(s).formatted(Formatting.GRAY));
         }
         if(augmentLvl > 0) augmentItem1.glow();
         
         int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
         MutableText titleText = augmentLvl == 0 ? Text.literal("Unlock Level 1").formatted(Formatting.LIGHT_PURPLE) : Text.literal("Current Level: ").formatted(Formatting.DARK_PURPLE).append(Text.literal(""+augmentLvl).formatted(Formatting.LIGHT_PURPLE));
         MagicRarity[] tiers = augment.getTiers();
         Item concrete = augmentLvl == tiers.length ? Items.WHITE_CONCRETE : MagicRarity.getColoredConcrete(tiers[augmentLvl]);
         
         GuiElementBuilder augmentItem2 = new GuiElementBuilder(concrete);
         
   
         if(augmentLvl == tiers.length){
            augmentItem2.hideFlags().setName(
                  Text.literal("Level ").formatted(Formatting.DARK_PURPLE)
                        .append(Text.literal(""+augmentLvl).formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(" Unlocked").formatted(Formatting.DARK_PURPLE)));
            augmentItem2.addLoreLine(Text.literal("")
                  .append(Text.literal("Max Level").formatted(Formatting.AQUA)));
            augmentItem2.glow();
         }else{
            augmentItem2.hideFlags().setName(titleText);
            augmentItem2.addLoreLine(Text.literal("")
                  .append(Text.literal("Next Level: ").formatted(Formatting.BLUE))
                  .append(Text.literal((augmentLvl+1)+"").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(" (").formatted(Formatting.BLUE))
                  .append(MagicRarity.getColoredLabel(tiers[augmentLvl],false))
                  .append(Text.literal(")").formatted(Formatting.BLUE)));
            augmentItem2.addLoreLine(Text.literal("")
                  .append(Text.literal("Skill Point Cost: ").formatted(Formatting.BLUE))
                  .append(Text.literal((tiers[augmentLvl].rarity+1)+"").formatted(Formatting.DARK_AQUA)));
            augmentItem2.addLoreLine(Text.literal(""));
            augmentItem2.addLoreLine(Text.literal("")
                  .append(Text.literal("(").formatted(Formatting.BLUE))
                  .append(Text.literal(unallocated+"").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(" Unallocated Points)").formatted(Formatting.BLUE)));
            augmentItem2.addLoreLine(Text.literal("")
                  .append(Text.literal("Click To Unlock").formatted(Formatting.AQUA)));
         }
   
         gui.setSlot(19+augmentSlots[i], augmentItem1);
         gui.setSlot(28+augmentSlots[i], augmentItem2);
      }
   
      ItemStack achievePane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      tag = achievePane.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Achievements:\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Earning Achievements Grants Skill Points and XP!\",\"color\":\"light_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
   
      List<ArcanaAchievement> achievements = ArcanaAchievements.getItemAchievements(magicItem);
      int[] achieveSlots = dynamicSlots[achievements.size()];
      for(int i = 0; i < 7; i++){
         gui.setSlot(46+i,GuiElementBuilder.from(achievePane));
      }
      for(int i = 0; i < achievements.size(); i++){
         ArcanaAchievement achievement = achievements.get(i);
         gui.clearSlot(46+achieveSlots[i]);
         
         GuiElementBuilder achievementItem = new GuiElementBuilder(achievement.getDisplayItem().getItem());
         achievementItem.hideFlags().setName(Text.literal(achievement.name).formatted(Formatting.LIGHT_PURPLE))
               .addLoreLine(Text.literal("")
                     .append(Text.literal(""+achievement.xpReward).formatted(Formatting.AQUA))
                     .append(Text.literal(" XP").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal("  |  ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(""+achievement.pointsReward).formatted(Formatting.AQUA))
                     .append(Text.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").formatted(Formatting.DARK_AQUA)));
   
         for(String s : achievement.getDescription()){
            achievementItem.addLoreLine(Text.literal(s).formatted(Formatting.GRAY));
         }
         
         MutableText[] statusText = achievement.getStatusDisplay(player);
         if(statusText != null){
            for(MutableText mutableText : statusText){
               achievementItem.addLoreLine(mutableText);
            }
         }
         
         List<UUID> achPlayers = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
         if(achPlayers == null || achPlayers.isEmpty()){
            achievementItem.addLoreLine(Text.literal("")
                  .append(Text.literal("No Arcanists have achieved this.").formatted(Formatting.DARK_PURPLE)));
         }else{
            int allArcanists = (int) ArcanaNovum.PLAYER_XP_TRACKER.values().stream().filter(xp -> xp > 1).count();
            int acquiredCount = achPlayers.size();
            DecimalFormat df = new DecimalFormat("#0.00");
            achievementItem.addLoreLine(Text.literal("")
                  .append(Text.literal("Acquired by ").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal(acquiredCount+"").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(" Arcanists (").formatted(Formatting.DARK_PURPLE))
                  .append(Text.literal(df.format((100*(double)acquiredCount)/((double)allArcanists))+"%").formatted(Formatting.LIGHT_PURPLE))
                  .append(Text.literal(")").formatted(Formatting.DARK_PURPLE)));
         }
         
         if(profile.hasAcheivement(magicItem.getId(),achievement.id)) achievementItem.glow();
   
         gui.setSlot(46+achieveSlots[i], achievementItem);
      }
   
      gui.setTitle(Text.literal(magicItem.getNameString()));
   }
   
   public void buildRecipeGui(SimpleGui gui, String id){
      if(gui instanceof TomeGui tomeGui){
         tomeGui.setMode(TomeMode.RECIPE);
      }
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
      display.putString("Name","[{\"text\":\"Item Lore\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Click \",\"italic\":false,\"color\":\"yellow\"},{\"text\":\"to read about this Magic Item.\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(7,GuiElementBuilder.from(book));
      
      gui.setSlot(25,GuiElementBuilder.from(magicItem.getPrefItem()).glow());
      
      GuiElementBuilder returnBook = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      returnBook.setName((Text.literal("")
            .append(Text.literal("Magic Items").formatted(Formatting.DARK_PURPLE))));
      returnBook.addLoreLine((Text.literal("")
            .append(Text.literal("Click ").formatted(Formatting.GREEN))
            .append(Text.literal("to return to the Magic Items Page").formatted(Formatting.LIGHT_PURPLE))));
      gui.setSlot(26,returnBook);
      
      
      MagicItemRecipe recipe = magicItem.getRecipe();
      MagicItemIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(MagicItemUtils.isMagic(ingredient)) craftingElement.glow();
         gui.setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
   
      ItemStack recipeList = new ItemStack(Items.PAPER);
      HashMap<String, Pair<Integer,ItemStack>> ingredList = recipe.getIngredientList();
      tag = recipeList.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Total Ingredients\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"-----------------------\",\"italic\":false,\"color\":\"light_purple\"}]"));
      for(Map.Entry<String, Pair<Integer,ItemStack>> ingred : ingredList.entrySet()){
         loreList.add(NbtString.of(getIngredStr(ingred)));
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
         gui.setSlot(slotCount,reqItem);
         slotCount += 9;
      }
      if(!recipe.getForgeRequirementList().isEmpty()) loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Does not include item data\",\"italic\":true,\"color\":\"dark_purple\"}]"));
   
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(43,GuiElementBuilder.from(recipeList));
   
      gui.setTitle(Text.literal("Recipe for "+magicItem.getNameString()));
   }
   
   private String getIngredStr(Map.Entry<String, Pair<Integer, ItemStack>> ingred){
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
   
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nRarity: Empowered\\n\\nStrangely enough, this Tome is incredibly easy to craft compared to most other Magic Items, like it wants to share its knowledge.\\n\\nThe way the Eye of Ender is so naturally \"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nAttracted to the enchantment table is definitely curious.\\n\\nHowever, as a result of its ease of construction, it offers no Crafting XP like other Magic Items do.\\n\\nIt acts as a guide and aid for those who\"}");
      list.add("{\"text\":\"Tome of Arcana Novum\\n\\nseek the secrets of Arcana Novum.\"}");
      return list;
   }
   
   private MagicItemRecipe makeRecipe(){
      ItemStack pane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE).setCustomName(Text.literal("In World Recipe").formatted(Formatting.BLUE,Formatting.BOLD));
      MiscUtils.addLoreLine(pane,Text.literal("Do this in the World").formatted(Formatting.DARK_PURPLE));
      
      ItemStack table = new ItemStack(Items.ENCHANTING_TABLE).setCustomName(Text.literal("Enchanting Table").formatted(Formatting.DARK_AQUA,Formatting.BOLD));
      MiscUtils.addLoreLine(table,Text.literal("Place an Enchanting Table in the World").formatted(Formatting.DARK_PURPLE));
      
      ItemStack eye = new ItemStack(Items.ENDER_EYE).setCustomName(Text.literal("Eye of Ender").formatted(Formatting.GREEN,Formatting.BOLD));
      MiscUtils.addLoreLine(eye,Text.literal("Place an Eye of Ender onto the Enchanting Table").formatted(Formatting.DARK_PURPLE));
      
      ItemStack book = new ItemStack(Items.BOOK).setCustomName(Text.literal("Book").formatted(Formatting.AQUA,Formatting.BOLD));
      MiscUtils.addLoreLine(book,Text.literal("Place a Book onto the Enchanting Table").formatted(Formatting.DARK_PURPLE));
      
      ExplainIngredient a = new ExplainIngredient(pane,"",false);
      ExplainIngredient t = new ExplainIngredient(table,"Enchanting Table");
      ExplainIngredient b = new ExplainIngredient(book,"Book");
      ExplainIngredient e = new ExplainIngredient(eye,"Eye of Ender");
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,e,a,a},
            {a,a,b,a,a},
            {a,a,t,a,a},
            {a,a,a,a,a}};
      return new ExplainRecipe(ingredients);
   }
   
   public enum TomeMode{
      PROFILE,
      COMPENDIUM,
      ITEM,
      RECIPE,
      NONE,
      ACHIEVEMENTS,
      LEADERBOARD
   }
   
   // TODO: Refactor filters and sorts
   public enum TomeFilter{
      NONE("None"),
      MUNDANE("Mundane"),
      EMPOWERED("Empowered"),
      EXOTIC("Exotic"),
      LEGENDARY("Legendary"),
      MYTHICAL("Mythical"),
      ITEMS("Items"),
      BLOCKS("Blocks"),
      FORGE("Forge"),
      ARROWS("Arrows"),
      ALTARS("Altars"),
      EQUIPMENT("Equipment"),
      CHARMS("Charms"),
      CATALYSTS("Catalysts");
   
      public final String label;
   
      TomeFilter(String label){
         this.label = label;
      }
   
      public static Text getColoredLabel(TomeFilter filter){
         MutableText text = Text.literal(filter.label);
      
         return switch(filter){ // Only Black and Dark Blue left for future usage (before repeats)
            case NONE -> text.formatted(Formatting.WHITE);
            case MUNDANE -> text.formatted(Formatting.GRAY);
            case EMPOWERED -> text.formatted(Formatting.GREEN);
            case EXOTIC -> text.formatted(Formatting.AQUA);
            case LEGENDARY -> text.formatted(Formatting.GOLD);
            case MYTHICAL -> text.formatted(Formatting.LIGHT_PURPLE);
            case ITEMS -> text.formatted(Formatting.DARK_AQUA);
            case BLOCKS -> text.formatted(Formatting.DARK_PURPLE);
            case FORGE -> text.formatted(Formatting.DARK_GREEN);
            case ARROWS -> text.formatted(Formatting.RED);
            case ALTARS -> text.formatted(Formatting.BLUE);
            case EQUIPMENT -> text.formatted(Formatting.DARK_RED);
            case CHARMS -> text.formatted(Formatting.YELLOW);
            case CATALYSTS -> text.formatted(Formatting.DARK_GRAY);
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
            log(2,"No categories found for: "+item.getNameString());
            return false;
         }
         for(TomeFilter category : cats){
            if(filter == category) return true;
         }
         return false;
      }
   }
   
   public enum TomeSort{
      RECOMMENDED("Recommended"),
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
            case RECOMMENDED -> text.formatted(Formatting.YELLOW);
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
   
   public enum AchievementSort{
      RECOMMENDED("Item (Recommended)"),
      RARITY_ASC("XP/SP Ascending"),
      RARITY_DESC("XP/SP Descending"),
      NAME("Alphabetical");
      
      public final String label;
   
      AchievementSort(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(AchievementSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case RARITY_ASC -> text.formatted(Formatting.LIGHT_PURPLE);
            case RARITY_DESC -> text.formatted(Formatting.DARK_PURPLE);
            case NAME -> text.formatted(Formatting.GREEN);
            case RECOMMENDED -> text.formatted(Formatting.YELLOW);
         };
      }
      
      public static AchievementSort cycleSort(AchievementSort sort, boolean backwards){
         AchievementSort[] sorts = AchievementSort.values();
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
   
   public enum AchievementFilter{
      NONE("None"),
      ACQUIRED("Acquired"),
      NOT_ACQUIRED("Not Acquired");
      
      public final String label;
   
      AchievementFilter(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(AchievementFilter filter){
         MutableText text = Text.literal(filter.label);
         
         return switch(filter){
            case NONE -> text.formatted(Formatting.WHITE);
            case ACQUIRED -> text.formatted(Formatting.AQUA);
            case NOT_ACQUIRED -> text.formatted(Formatting.RED);
         };
      }
      
      public static AchievementFilter cycleFilter(AchievementFilter filter, boolean backwards){
         AchievementFilter[] filters = AchievementFilter.values();
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
      
      public static boolean matchesFilter(ServerPlayerEntity player, AchievementFilter filter, ArcanaAchievement ach){
         if(filter == AchievementFilter.NONE) return true;
         IArcanaProfileComponent profile = PLAYER_DATA.get(player);
         boolean acquired = profile.hasAcheivement(ach.getMagicItem().getId(),ach.id);
         
         if(filter == AchievementFilter.ACQUIRED) return acquired;
         if(filter == AchievementFilter.NOT_ACQUIRED) return !acquired;
         return false;
      }
   }
   
   public enum LeaderboardSort{
      XP_DESC("XP Descending (Recommended)"),
      XP_ASC("XP Ascending"),
      ACHIEVES_DESC("Achievements Descending"),
      SKILL_POINTS_DESC("Skill Points Descending"),
      NAME("Alphabetical");
      
      public final String label;
      
      LeaderboardSort(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(LeaderboardSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case XP_DESC -> text.formatted(Formatting.LIGHT_PURPLE);
            case XP_ASC -> text.formatted(Formatting.DARK_PURPLE);
            case ACHIEVES_DESC -> text.formatted(Formatting.GREEN);
            case SKILL_POINTS_DESC -> text.formatted(Formatting.DARK_GREEN);
            case NAME -> text.formatted(Formatting.AQUA);
         };
      }
      
      public static LeaderboardSort cycleSort(LeaderboardSort sort, boolean backwards){
         LeaderboardSort[] sorts = LeaderboardSort.values();
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
   
   public enum LeaderboardFilter{
      NONE("None"),
      ARCANIST("Arcanist"),
      MAX_LVL("Max Level"),
      ABYSS("Abyssal Arcanist");
      
      public final String label;
      
      LeaderboardFilter(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(LeaderboardFilter filter){
         MutableText text = Text.literal(filter.label);
         
         return switch(filter){
            case NONE -> text.formatted(Formatting.WHITE);
            case ARCANIST -> text.formatted(Formatting.LIGHT_PURPLE);
            case MAX_LVL -> text.formatted(Formatting.GREEN);
            case ABYSS -> text.formatted(Formatting.DARK_PURPLE);
         };
      }
      
      public static LeaderboardFilter cycleFilter(LeaderboardFilter filter, boolean backwards){
         LeaderboardFilter[] filters = LeaderboardFilter.values();
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
      
      public static boolean matchesFilter(UUID playerID, LeaderboardFilter filter){
         if(filter == LeaderboardFilter.NONE) return true;
         int xp = ArcanaNovum.PLAYER_XP_TRACKER.get(playerID);
         int level = LevelUtils.levelFromXp(xp);
         
         if(filter == LeaderboardFilter.ARCANIST) return xp > 1;
         if(filter == LeaderboardFilter.MAX_LVL) return level >= 100;
         if(filter == LeaderboardFilter.ABYSS) return ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(ArcanaAchievements.ALL_ACHIEVEMENTS.id).contains(playerID);
         return false;
      }
   }
   
   public class ArcaneTomeItem extends MagicPolymerItem {
      public ArcaneTomeItem(Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         return ArcanaRegistry.MODELS.get(TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
         openGui(playerEntity,TomeMode.PROFILE,new TomeGui.CompendiumSettings(0,0));
         return TypedActionResult.success(playerEntity.getStackInHand(hand));
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!MagicItemUtils.isMagic(stack)) return;
         
         NbtCompound itemNbt = stack.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         if(magicTag.contains("forgeCraftTick")){
            magicTag.remove("forgeCraftTick");
         }
      }
   }
}
