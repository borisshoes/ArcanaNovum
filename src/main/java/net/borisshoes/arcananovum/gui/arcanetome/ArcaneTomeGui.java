package net.borisshoes.arcananovum.gui.arcanetome;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarRecipeGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.normal.ArcaneNotesItem;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.gui.*;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.apache.commons.lang3.function.TriConsumer;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.ArcanaRegistry.RECOMMENDED_LIST;

public class ArcaneTomeGui extends PagedMultiGui {
   
   public static final int[][] DYNAMIC_SLOTS = {{}, {3}, {1, 5}, {1, 3, 5}, {0, 2, 4, 6}, {1, 2, 3, 4, 5}, {0, 1, 2, 4, 5, 6}, {0, 1, 2, 3, 4, 5, 6}};
   private static final int[] CRAFTING_SLOTS = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42, 47, 48, 49, 50, 51};
   private static final int[] REQUIREMENT_SLOTS = {9,17,18,26,27,35};
   
   private TomeMode mode = TomeMode.PROFILE;
   private boolean permaCloseFlag = false;
   private boolean hideCompletedResearch = false;
   private boolean forTome = true;
   private boolean forAnvil = false;
   private boolean forCache = false;
   private boolean forForge = false;
   private boolean building = false;
   private int recipeInd;
   private List<ArcanaRecipe> selectedRecipes;
   private ArcanaItem selectedArcanaItem;
   private SimpleGui returnGui;
   
   public ArcaneTomeGui(ServerPlayer player, TomeMode mode, SimpleGui returnGui){
      super(MenuType.GENERIC_9x6, player);
      this.mode = mode;
      this.returnGui = returnGui;
   }
   
   public ArcaneTomeGui(ServerPlayer player, TomeMode mode){
      super(MenuType.GENERIC_9x6, player);
      this.mode = mode;
   }
   
   public ArcaneTomeGui(ServerPlayer player){
      super(MenuType.GENERIC_9x6, player);
   }
   
   public void setReturnGui(SimpleGui returnGui){
      this.returnGui = returnGui;
   }
   
   // 0 - Compendium
   // 1 - Achievement
   // 2 - Leaderboard
   // 3 - Research
   public ArcaneTomeGui addModes(){
      return addModes(getDefaultCompendiumClickHandler(), getDefaultAchievementClickHandler(), getDefaultLeaderboardClickHandler(), getDefaultResearchClickHandler());
   }
   
   public ArcaneTomeGui addModes(TriConsumer<CompendiumEntry, Integer, ClickType> compendiumClickHandler, TriConsumer<ArcanaAchievement, Integer, ClickType> achievementClickHandler, TriConsumer<ArcanaPlayerData, Integer, ClickType> leaderboardClickHandler, TriConsumer<ResearchTask, Integer, ClickType> researchClickHandler){
      addMode(new ArrayList<CompendiumEntry>(),
            (item, index) -> {
               GuiElementBuilder builder = GuiElementBuilder.from(item.getDisplayStack()).glow();
               if(item instanceof IngredientCompendiumEntry ice && ice.hasBookLore() && this instanceof ArcaneTomeGui){
                  builder.addLoreLine(Component.literal(""));
                  builder.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("Click").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(" to read about this Ingredient Item").withStyle(ChatFormatting.DARK_PURPLE))));
               }
               return builder;
            }, compendiumClickHandler,
            TomeSort.RECOMMENDED, TomeFilter.NONE
      );
      
      addMode(new ArrayList<ArcanaAchievement>(),
            (baseAch, index) -> {
               ArcanaPlayerData profile = ArcanaNovum.data(player);
               ArcanaAchievement profileAchievement = profile.getAchievement(baseAch.getArcanaItem().getId(), baseAch.id);
               ArcanaAchievement achievement = profileAchievement != null ? profileAchievement : baseAch;
               
               ItemStack displayItem = achievement.getDisplayItem();
               ArcanaItem.putProperty(displayItem, ArcaneTome.DISPLAY_TAG, achievement.getArcanaItem().getId());
               GuiElementBuilder achievementItem = GuiElementBuilder.from(displayItem);
               achievementItem.hideDefaultTooltip().setName(Component.literal("").withStyle(ChatFormatting.LIGHT_PURPLE).append(achievement.getTranslatedName()).append(" - ").append(achievement.getArcanaItem().getTranslatedName()))
                     .addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal(LevelUtils.readableInt(achievement.xpReward)).withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("" + achievement.pointsReward).withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").withStyle(ChatFormatting.DARK_AQUA))));
               
               List<Component> descLines = achievement.getDescription();
               for(Component descLine : descLines){
                  achievementItem.addLoreLine(TextUtils.removeItalics(descLine.copy().withStyle(ChatFormatting.GRAY)));
               }
               
               MutableComponent[] statusText = achievement.getStatusDisplay(player);
               if(statusText != null){
                  for(MutableComponent mutableText : statusText){
                     achievementItem.addLoreLine(TextUtils.removeItalics(mutableText));
                  }
               }
               
               List<ArcanaPlayerData> allData = DataAccess.allPlayerDataFor(ArcanaPlayerData.KEY).values().stream().filter(p -> p.getXP() > 1).toList();
               int achPlayers = Math.toIntExact(allData.stream().filter(data -> data.hasAcheivement(achievement)).count());
               if(achPlayers == 0){
                  achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("No Arcanists have achieved this").withStyle(ChatFormatting.DARK_PURPLE))));
               }else{
                  int allArcanists = allData.size();
                  DecimalFormat df = new DecimalFormat("#0.00");
                  achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("Acquired by ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(achPlayers + "").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" Arcanists (").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(df.format((100 * (double) achPlayers) / ((double) allArcanists)) + "%").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE))));
               }
               
               if(profile.hasAcheivement(achievement)) achievementItem.glow();
               return achievementItem;
            }, achievementClickHandler,
            AchievementSort.RECOMMENDED, AchievementFilter.NONE
      );
      
      addMode(new ArrayList<ArcanaPlayerData>(),
            (data, index) -> {
               UUID playerId = data.getPlayerId();
               int numAchievements = ArcanaAchievements.registry.size();
               int playerXp = data.getXP();
               int playerLevel = LevelUtils.levelFromXp(playerXp);
               GameProfile playerGameProf;
               GuiElementBuilder playerItem;
               try{
                  playerGameProf = player.level().getServer().services().profileResolver().fetchById(playerId).orElseThrow();
                  playerItem = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(playerGameProf, player.level().getServer());
                  playerItem.setName(Component.literal(playerGameProf.name()).withStyle(ChatFormatting.LIGHT_PURPLE));
               }catch(Exception e){
                  playerItem = new GuiElementBuilder(Items.BARRIER);
                  playerItem.setName(Component.literal("<UNKNOWN>").withStyle(ChatFormatting.LIGHT_PURPLE));
               }
               playerItem.hideDefaultTooltip();
               
               playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Arcana Level: ").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal("" + playerLevel).withStyle(ChatFormatting.LIGHT_PURPLE)))));
               if(playerLevel == 100){
                  playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Total Experience: ").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(LevelUtils.readableInt(playerXp)).withStyle(ChatFormatting.GREEN)))));
               }else{
                  playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Experience: ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(LevelUtils.readableInt(LevelUtils.getCurLevelXp(playerXp))).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("/").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(LevelUtils.readableInt(LevelUtils.nextLevelNewXp(playerLevel))).withStyle(ChatFormatting.GREEN)))));
               }
               int playerAchievements = data.totalAcquiredAchievements();
               playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Achievements: ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(LevelUtils.readableInt(playerAchievements)).withStyle(ChatFormatting.AQUA))
                     .append(Component.literal("/").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(LevelUtils.readableInt(numAchievements)).withStyle(ChatFormatting.AQUA)))));
               return playerItem;
            }, leaderboardClickHandler,
            LeaderboardSort.XP_DESC, LeaderboardFilter.NONE
      );
      
      addMode(new ArrayList<ResearchTask>(),
            (task, index) -> {
               List<ResearchTask> allTasksList = ResearchTasks.getUniqueTasks(selectedArcanaItem.getResearchTasks()).stream().toList();
               boolean allTasksDone = allTasksList.stream().allMatch(t -> t.isAcquired(player));
               GuiElementBuilder bg = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, allTasksDone ? ArcanaColors.ARCANE_PAGE_COLOR : ArcanaColors.PAGE_COLOR)).hideTooltip();
               if(task == null) return bg;
               
               boolean acquired = task.isAcquired(player);
               boolean hasPreReqs = task.satisfiedPreReqs(player);
               boolean hasPrePreReqs = task.satisfiedPrePreReqs(player);
               GuiElementBuilder taskItem = GuiElementBuilder.from(task.getDisplayItem()).hideDefaultTooltip().setCount(1).glow(acquired);
               taskItem.setName(MutableComponent.create(task.getName().getContents()).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
               
               if(acquired){
                  taskItem.addLoreLine(TextUtils.removeItalics(Component.literal("Completed").withStyle(ChatFormatting.AQUA)));
               }else{
                  if(hasPreReqs){
                     List<MutableComponent> descLines = task.getColoredDescription();
                     for(Component descLine : descLines){
                        taskItem.addLoreLine(TextUtils.removeItalics(descLine));
                     }
                     
                  }else{
                     taskItem.setName(Component.literal("???").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
                  }
               }
               if(hasPrePreReqs){
                  return taskItem;
               }
               return bg;
            }, researchClickHandler,
            null, null
      );
      TomeFilter.setPlayer(player);
      AchievementFilter.setPlayer(player);
      return this;
   }
   
   public TriConsumer<CompendiumEntry, Integer, ClickType> getDefaultCompendiumClickHandler(){
      return (entry, index, type) -> {
         if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
            ArcanaItem arcanaItem = arcanaEntry.getArcanaItem();
            if(!ArcanaNovum.data(player).hasResearched(arcanaItem)){
               if(forTome) buildGui(TomeMode.RESEARCH, arcanaItem);
            }else{
               if(type == ClickType.MOUSE_RIGHT){
                  List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(arcanaItem.getItem());
                  if(!recipes.isEmpty()){
                     if(forTome) buildGui(TomeMode.RECIPE, arcanaItem, recipes, 0);
                  }else{
                     player.displayClientMessage(Component.literal("You Cannot Craft This Item").withStyle(ChatFormatting.RED), false);
                  }
               }else{
                  if(forTome) buildGui(TomeMode.ITEM, arcanaItem);
               }
            }
         }else if(entry instanceof IngredientCompendiumEntry ingredientEntry){
            if(type.isLeft && ingredientEntry.hasBookLore()){
               List<List<Component>> loreData = ingredientEntry.getBookLore();
               if(loreData != null){
                  BookElementBuilder bookBuilder = new BookElementBuilder();
                  loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
                  LoreGui loreGui = new LoreGui(player, bookBuilder, this);
                  loreGui.open();
               }else{
                  player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED), false);
               }
            }else{
               List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(ingredientEntry.getItem());
               if(forTome && !recipes.isEmpty()) buildGui(TomeMode.RECIPE, null, recipes, 0);
            }
         }else if(entry instanceof TransmutationRecipesCompendiumEntry){
            TransmutationAltarRecipeGui transmutationGui = new TransmutationAltarRecipeGui(player, this, Optional.empty());
            transmutationGui.buildPage();
            transmutationGui.open();
         }
      };
   }
   
   public TriConsumer<ArcanaAchievement, Integer, ClickType> getDefaultAchievementClickHandler(){
      return (ach, index, clickType) -> {
         ArcanaItem arcanaItem = ach.getArcanaItem();
         if(ArcanaNovum.data(player).hasResearched(arcanaItem)){
            if(forTome) buildGui(TomeMode.ITEM, arcanaItem);
         }else{
            player.displayClientMessage(Component.literal("You Have Not Researched This Item").withStyle(ChatFormatting.RED), false);
         }
      };
   }
   
   public TriConsumer<ArcanaPlayerData, Integer, ClickType> getDefaultLeaderboardClickHandler(){
      return (a, b, c) -> {
      };
   }
   
   public TriConsumer<ResearchTask, Integer, ClickType> getDefaultResearchClickHandler(){
      return (a, b, c) -> {
      };
   }
   
   public void setMode(TomeMode mode){
      this.mode = mode;
   }
   
   public TomeMode getMode(){
      return mode;
   }
   
   @Override
   public void onClose(){
      if(permaCloseFlag) return;
      if(returnGui != null){
         returnGui.open();
         this.returnGui = null;
      }
      if(mode == TomeMode.RECIPE){ // Recipe gui to compendium
         buildGui(TomeMode.COMPENDIUM);
      }else if(mode == TomeMode.ITEM || mode == TomeMode.RESEARCH){ // Item gui to compendium
         buildGui(TomeMode.COMPENDIUM);
      }
   }
   
   public void setGuiFlags(boolean forTome, boolean forAnvil, boolean forCache, boolean forForge){
      this.forTome = forTome;
      this.forAnvil = forAnvil;
      this.forCache = forCache;
      this.forForge = forForge;
   }
   
   public void buildGui(TomeMode mode){
      this.selectedRecipes = new ArrayList<>();
      this.recipeInd = 0;
      this.selectedArcanaItem = null;
      this.mode = mode;
      buildPage();
   }
   
   public void buildGui(TomeMode mode, ArcanaItem item){
      this.selectedRecipes = new ArrayList<>();
      this.recipeInd = 0;
      this.selectedArcanaItem = item;
      this.mode = mode;
      buildPage();
   }
   
   public void buildGui(TomeMode mode, ArcanaItem item, List<ArcanaRecipe> recipes, int index){
      this.selectedRecipes = recipes == null ? new ArrayList<>() : recipes;
      this.recipeInd = index;
      this.selectedArcanaItem = item;
      this.mode = mode;
      buildPage();
   }
   
   public void buildAndOpen(){
      buildPage();
      open();
   }
   
   @Override
   public void buildPage(){
      TomeFilter.setPlayer(player);
      AchievementFilter.setPlayer(player);
      if(!building){
         building = true;
         blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).hideTooltip());
         if(this.mode == TomeMode.RECIPE){
            buildRecipeGui();
         }else if(this.mode == TomeMode.ITEM){
            buildItemGui();
         }else if(this.mode == TomeMode.RESEARCH){
            buildResearchGui();
            switchMode(3);
            super.buildPage();
         }else if(this.mode == TomeMode.ACHIEVEMENTS){
            buildAchievementsGui();
            switchMode(1);
            super.buildPage();
         }else if(this.mode == TomeMode.COMPENDIUM){
            buildCompendiumGui();
            switchMode(0);
            super.buildPage();
         }else if(this.mode == TomeMode.LEADERBOARD){
            buildLeaderboardGui();
            switchMode(2);
            super.buildPage();
         }else if(this.mode == TomeMode.PROFILE){
            buildProfileGui();
         
         }
         building = false;
      }
   }
   
   private void buildProfileGui(){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      for(int i = 1; i < 8; i++){
         setSlot(27+i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      }
      setSlot(27, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(35, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setProfile(player.getUUID()).hideDefaultTooltip();
      head.setName((Component.literal("").append(Component.literal(player.getScoreboardName() + "'s ").withStyle(ChatFormatting.AQUA)).append(Component.literal("Arcane Profile").withStyle(ChatFormatting.DARK_PURPLE))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" for a brief overview of Arcana Novum!").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.setCallback((type) -> {
         if(!forTome) return;
         BookElementBuilder bookBuilder = ArcanaNovum.getGuideBook();
         LoreGui loreGui = new LoreGui(player, bookBuilder, this);
         loreGui.open();
      });
      setSlot(4, head);
      
      GuiElementBuilder book = new GuiElementBuilder(Items.WRITTEN_BOOK);
      book.setName(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to go to the Arcana Items Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
      book.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.COMPENDIUM);
      });
      setSlot(49, book);
      
      
      int level = profile.getLevel();
      String readableXP = LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()));
      GuiElementBuilder lectern = new GuiElementBuilder(Items.LECTERN);
      lectern.setName(Component.literal("Arcana Level").withStyle(ChatFormatting.DARK_GREEN));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Arcana Level: " + level).withStyle(ChatFormatting.GREEN)));
      if(level == 100){
         lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Total Experience: " + LevelUtils.readableInt(profile.getXP())).withStyle(ChatFormatting.GREEN)));
      }else{
         lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Experience: " + readableXP + "/" + LevelUtils.readableInt(LevelUtils.nextLevelNewXp(level))).withStyle(ChatFormatting.GREEN)));
      }
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("You can increase your arcana by crafting and using Arcana items!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to see the Leaderboard").withStyle(ChatFormatting.DARK_AQUA))));
      lectern.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.LEADERBOARD);
      });
      setSlot(10, lectern);
      
      
      if(level == 100){
         for(int i = 11; i <= 16; i++){
            setSlot(i, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Component.literal("XP: " + LevelUtils.readableInt(profile.getXP())).withStyle(ChatFormatting.GREEN)));
         }
      }else{
         int filled = (int) Math.round((double) LevelUtils.getCurLevelXp(profile.getXP()) / LevelUtils.nextLevelNewXp(profile.getLevel()) * 6.0);
         for(int i = 11; i <= 16; i++){
            if(i >= filled + 11){
               setSlot(i, new GuiElementBuilder(Items.GLASS_BOTTLE).setName(Component.literal("XP: " + readableXP + "/" + LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).withStyle(ChatFormatting.GREEN)));
               
            }else{
               setSlot(i, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Component.literal("XP: " + readableXP + "/" + LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).withStyle(ChatFormatting.GREEN)));
            }
         }
      }
      
      int totalSkillPoints = profile.getTotalSkillPoints();
      int spentSkillPoints = profile.getSpentSkillPoints();
      int bonusSkillPoints = profile.getBonusSkillPoints();
      GuiElementBuilder shelf = new GuiElementBuilder(Items.BOOKSHELF);
      shelf.setName(Component.literal("Skill Points").withStyle(ChatFormatting.DARK_AQUA));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Total Skill Points: " + totalSkillPoints).withStyle(ChatFormatting.AQUA)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Available Points: " + (totalSkillPoints - spentSkillPoints)).withStyle(ChatFormatting.AQUA)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Points From Leveling: " + LevelUtils.getLevelSkillPoints(level)).withStyle(ChatFormatting.BLUE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Points From Achievements: " + profile.getAchievementSkillPoints()).withStyle(ChatFormatting.BLUE)));
      if(bonusSkillPoints != 0)
         shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Bonus Skill Points: " + bonusSkillPoints).withStyle(ChatFormatting.BLUE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Allocate Skill Points to Augment Items!").withStyle(ChatFormatting.DARK_PURPLE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Earn Skill Points From Leveling Up or From Achievements!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" to see all Achievements").withStyle(ChatFormatting.LIGHT_PURPLE))));
      shelf.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.ACHIEVEMENTS);
      });
      setSlot(19, shelf);
      
      
      int books = (int) ((double) spentSkillPoints / totalSkillPoints * 6.0);
      for(int i = 20; i <= 25; i++){
         if(i >= books + 20){
            setSlot(i, new GuiElementBuilder(Items.BOOK).setName(Component.literal("Allocated Skill Points: " + spentSkillPoints + "/" + totalSkillPoints).withStyle(ChatFormatting.DARK_AQUA)));
            
         }else{
            setSlot(i, new GuiElementBuilder(Items.ENCHANTED_BOOK).setName(Component.literal("Allocated Skill Points: " + spentSkillPoints + "/" + totalSkillPoints).withStyle(ChatFormatting.DARK_AQUA)));
         }
      }
      
      int resolve = profile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromLevel(profile.getLevel(), resolve);
      GuiElementBuilder crystal = new GuiElementBuilder(Items.END_CRYSTAL);
      crystal.setName(Component.literal("Arcane Concentration").withStyle(ChatFormatting.BLUE));
      crystal.addLoreLine(TextUtils.removeItalics(Component.literal("Concentration: " + ArcanaItemUtils.getUsedConcentration(player) + "/" + maxConc).withStyle(ChatFormatting.AQUA)));
      crystal.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      crystal.addLoreLine(TextUtils.removeItalics(Component.literal("Your max concentration increases with your level!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      List<MutableComponent> concBreakdown = ArcanaItemUtils.getConcBreakdown(player);
      if(!concBreakdown.isEmpty()){
         crystal.addLoreLine(TextUtils.removeItalics(Component.literal("")));
         crystal.addLoreLine(TextUtils.removeItalics(Component.literal("Items Taking Concentration:").withStyle(ChatFormatting.DARK_AQUA)));
         for(MutableComponent item : concBreakdown){
            crystal.addLoreLine(TextUtils.removeItalics(item));
         }
      }
      setSlot(37, crystal);
      
      
      int used = (int) Math.ceil((double) ArcanaItemUtils.getUsedConcentration(player) / maxConc * 6.0);
      boolean overConc = ArcanaItemUtils.getUsedConcentration(player) > maxConc;
      for(int i = 38; i <= 43; i++){
         if(overConc){
            setSlot(i, new GuiElementBuilder(Items.FIRE_CHARGE).setName(Component.literal("Concentration: " + ArcanaItemUtils.getUsedConcentration(player) + "/" + maxConc).withStyle(ChatFormatting.RED)));
         }else if(i >= used + 38){
            setSlot(i, new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("Concentration: " + ArcanaItemUtils.getUsedConcentration(player) + "/" + maxConc).withStyle(ChatFormatting.AQUA)));
         }else{
            setSlot(i, new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal("Concentration: " + ArcanaItemUtils.getUsedConcentration(player) + "/" + maxConc).withStyle(ChatFormatting.AQUA)));
         }
         
      }
      
      setTitle(Component.literal("Arcane Profile"));
   }
   
   private void buildCompendiumGui(){
      GuiMode<CompendiumEntry> config = getMode(0);
      List<CompendiumEntry> list = new ArrayList<>(RECOMMENDED_LIST);
      if(forForge){
         list.removeIf(entry -> {
            if(!(entry instanceof ArcanaItemCompendiumEntry arcanaEntry)) return true;
            List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(arcanaEntry.getArcanaItem().getItem());
            return recipes.stream().allMatch(recipe -> recipe instanceof ExplainRecipe);
         });
      }else if(forCache){
         list.removeIf(entry -> !(entry instanceof IngredientCompendiumEntry || entry instanceof ArcanaItemCompendiumEntry));
      }
      config.setItems(list);
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setProfile(player.getUUID()).hideDefaultTooltip();
      head.setName((Component.literal("").append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))));
      if(forTome){
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click here").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to return to the Profile Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to view its page or research").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Right Click an item").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to see its recipe or research").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }else{
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to see its recipe").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }
      head.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.PROFILE);
      });
      setSlot(4, head);
      
      setTitle(Component.literal("Item Compendium"));
   }
   
   private void buildAchievementsGui(){
      GuiMode<ArcanaAchievement> config = getMode(1);
      config.setItems(new ArrayList<>(ArcanaAchievements.registry.values().stream().toList()));
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setProfile(player.getUUID()).hideDefaultTooltip();
      head.setName((Component.literal("").append(Component.literal("Arcana Achievements").withStyle(ChatFormatting.DARK_PURPLE))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click here").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to return to the Profile Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Right Click here").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to go to the Items Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to view its page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.setCallback((type) -> {
         if(!forTome) return;
         if(type.isRight){
            buildGui(TomeMode.COMPENDIUM);
         }else{
            buildGui(TomeMode.PROFILE);
         }
      });
      setSlot(4, head);
      
      setTitle(Component.literal("All Arcana Achievements"));
   }
   
   private void buildLeaderboardGui(){
      GuiMode<ArcanaPlayerData> config = getMode(2);
      config.setItems(DataAccess.allPlayerDataFor(ArcanaPlayerData.KEY).values().stream().toList());
      
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      int numAchievements = ArcanaAchievements.registry.size();
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setProfile(player.getUUID()).hideDefaultTooltip();
      head.setName((Component.literal("").append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.AQUA))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Arcana Level: ").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal("" + profile.getLevel()).withStyle(ChatFormatting.LIGHT_PURPLE)))));
      if(profile.getLevel() == 100){
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Total Experience: ").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(LevelUtils.readableInt(profile.getXP())).withStyle(ChatFormatting.GREEN)))));
      }else{
         head.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Experience: ").withStyle(ChatFormatting.DARK_GREEN))
               .append(Component.literal(LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()))).withStyle(ChatFormatting.GREEN))
               .append(Component.literal("/").withStyle(ChatFormatting.DARK_GREEN))
               .append(Component.literal(LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).withStyle(ChatFormatting.GREEN)))));
      }
      head.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Achievements: ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(LevelUtils.readableInt(profile.totalAcquiredAchievements())).withStyle(ChatFormatting.AQUA))
            .append(Component.literal("/").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(LevelUtils.readableInt(numAchievements)).withStyle(ChatFormatting.AQUA)))));
      head.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to return to the profile page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.PROFILE);
      });
      setSlot(4, head);
      
      setTitle(Component.literal("Arcana Leaderboard"));
   }
   
   private void buildItemGui(){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(selectedArcanaItem == null){
         close();
         return;
      }
      
      for(int i = 0; i < getSize(); i++){
         if(i / 9 == 1){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i % 9 == 0){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i % 9 == 8){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i / 9 == 4){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      setSlot(9, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_LEFT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(17, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_RIGHT, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(36, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(44, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      for(int i = 0; i < 9; i++){
         setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).setName(Component.empty()).hideTooltip());
      }
      
      GuiElementBuilder structure = getMultiblockItem();
      if(structure != null && !forAnvil) setSlot(0, structure);
      
      GuiElementBuilder book = new GuiElementBuilder(Items.WRITTEN_BOOK).hideDefaultTooltip();
      book.setName(Component.literal("Item Lore").withStyle(ChatFormatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to read about this Arcana Item").withStyle(ChatFormatting.LIGHT_PURPLE))));
      if(selectedArcanaItem.getAttributions().length > 0){
         book.addLoreLine(Component.literal(""));
         for(Tuple<MutableComponent, MutableComponent> attribution : selectedArcanaItem.getAttributions()){
            book.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(attribution.getA().withStyle(ChatFormatting.DARK_PURPLE))
                  .append(attribution.getB().withStyle(ChatFormatting.LIGHT_PURPLE))));
         }
      }
      book.setCallback((type) -> {
         if(!forTome) return;
         List<List<Component>> loreData = selectedArcanaItem.getBookLore();
         if(loreData != null){
            BookElementBuilder bookBuilder = new BookElementBuilder();
            loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
            LoreGui loreGui = new LoreGui(player, bookBuilder, this);
            loreGui.open();
         }else{
            player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED), false);
         }
      });
      if(!forAnvil) setSlot(6, book);
      
      GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      table.setName(Component.literal("Item Recipe").withStyle(ChatFormatting.DARK_PURPLE));
      table.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to view this item's recipe!").withStyle(ChatFormatting.LIGHT_PURPLE))));
      table.setCallback((type) -> {
         if(!forTome) return;
         List<ArcanaRecipe> recipes = RecipeManager.getRecipesFor(selectedArcanaItem.getItem());
         if(!recipes.isEmpty()){
            buildGui(TomeMode.RECIPE, selectedArcanaItem, recipes, 0);
         }else{
            player.displayClientMessage(Component.literal("You Cannot Craft This Item").withStyle(ChatFormatting.RED), false);
         }
      });
      if(!forAnvil) setSlot(2, table);
      
      GuiElementBuilder item = GuiElementBuilder.from(selectedArcanaItem.getPrefItem()).glow();
      item.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.COMPENDIUM);
      });
      setSlot(4, item);
      
      GuiElementBuilder notes = getNotesItem();
      if(notes != null && forTome) setSlot(8, notes);
      
      GuiElementBuilder augmentPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).hideDefaultTooltip();
      augmentPane.setName(Component.literal("Augments:").withStyle(ChatFormatting.DARK_PURPLE));
      augmentPane.addLoreLine(TextUtils.removeItalics(Component.literal("Unlocked augments can be applied to enhance Arcana Items!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(selectedArcanaItem);
      int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
      for(int i = 0; i < 7; i++){
         setSlot(19 + i, augmentPane);
         setSlot(28 + i, augmentPane);
      }
      for(int i = 0; i < augmentSlots.length; i++){
         ArcanaAugment augment = augments.get(i);
         clearSlot(19 + augmentSlots[i]);
         clearSlot(28 + augmentSlots[i]);
         
         int augmentLvl = profile.getAugmentLevel(augment.id);
         
         GuiElementBuilder augmentItem1 = GuiElementBuilder.from(augment.getDisplayItem());
         augmentItem1.hideDefaultTooltip().setName(augment.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(TextUtils.removeItalics(augment.getTierDisplay()));
         
         List<Component> descLines = augment.getDescription();
         for(Component descLine : descLines){
            augmentItem1.addLoreLine(TextUtils.removeItalics(descLine.copy().withStyle(ChatFormatting.GRAY)));
         }
         if(augmentLvl > 0) augmentItem1.glow();
         
         int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
         MutableComponent titleText = augmentLvl == 0 ? Component.literal("Unlock Level 1").withStyle(ChatFormatting.LIGHT_PURPLE) : Component.literal("Current Level: ").withStyle(ChatFormatting.DARK_PURPLE).append(Component.literal("" + augmentLvl).withStyle(ChatFormatting.LIGHT_PURPLE));
         ArcanaRarity[] tiers = augment.getTiers();
         Item concrete = augmentLvl == tiers.length ? Items.WHITE_CONCRETE : ArcanaRarity.getColoredConcrete(tiers[augmentLvl]);
         
         GuiElementBuilder augmentItem2 = new GuiElementBuilder(concrete);
         
         if(augmentLvl == tiers.length){
            augmentItem2.hideDefaultTooltip().setName(
                  Component.literal("Level ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal("" + augmentLvl).withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" Unlocked").withStyle(ChatFormatting.DARK_PURPLE)));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Max Level").withStyle(ChatFormatting.AQUA))));
            augmentItem2.glow();
         }else{
            augmentItem2.hideDefaultTooltip().setName(titleText);
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Next Level: ").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal((augmentLvl + 1) + "").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" (").withStyle(ChatFormatting.BLUE))
                  .append(ArcanaRarity.getColoredLabel(tiers[augmentLvl], false))
                  .append(Component.literal(")").withStyle(ChatFormatting.BLUE))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Skill Point Cost: ").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal((tiers[augmentLvl].rarity + 1) + "").withStyle(ChatFormatting.DARK_AQUA))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("(").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal(unallocated + "").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" Unallocated Points)").withStyle(ChatFormatting.BLUE))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Click To Unlock").withStyle(ChatFormatting.AQUA))));
            augmentItem2.setCallback((click) -> {
               int cost = tiers[augmentLvl].rarity + 1;
               if(cost <= unallocated){
                  profile.setAugmentLevel(augment.id, augmentLvl + 1);
                  SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, 1, (.5f + ((float) (augmentLvl + 1) / (tiers.length - 1))));
                  buildPage();
               }else{
                  player.displayClientMessage(Component.literal("Not Enough Skill Points").withStyle(ChatFormatting.RED), false);
               }
            });
         }
         
         setSlot(19 + augmentSlots[i], augmentItem1);
         setSlot(28 + augmentSlots[i], augmentItem2);
      }
      
      GuiElementBuilder achievePane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.PAGE_COLOR)).hideDefaultTooltip();
      achievePane.setName(Component.literal("Achievements:").withStyle(ChatFormatting.DARK_PURPLE));
      achievePane.addLoreLine(TextUtils.removeItalics(Component.literal("Earning Achievements Grants Skill Points and XP!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      List<ArcanaPlayerData> allData = DataAccess.allPlayerDataFor(ArcanaPlayerData.KEY).values().stream().filter(p -> p.getXP() > 1).toList();
      List<ArcanaAchievement> achievements = ArcanaAchievements.getItemAchievements(selectedArcanaItem);
      int[] achieveSlots = DYNAMIC_SLOTS[achievements.size()];
      for(int i = 0; i < 7; i++){
         setSlot(46 + i, achievePane);
      }
      for(int i = 0; i < achievements.size(); i++){
         ArcanaAchievement achievement = achievements.get(i);
         clearSlot(46 + achieveSlots[i]);
         
         GuiElementBuilder achievementItem = GuiElementBuilder.from(achievement.getDisplayItem());
         achievementItem.hideDefaultTooltip().setName(achievement.getTranslatedName().withStyle(ChatFormatting.LIGHT_PURPLE))
               .addLoreLine(TextUtils.removeItalics(Component.literal("")
                    .append(Component.literal(LevelUtils.readableInt(achievement.xpReward)).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("" + achievement.pointsReward).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").withStyle(ChatFormatting.DARK_AQUA))));
         
         List<Component> descLines = achievement.getDescription();
         for(Component descLine : descLines){
            achievementItem.addLoreLine(TextUtils.removeItalics(descLine.copy().withStyle(ChatFormatting.GRAY)));
         }
         
         MutableComponent[] statusText = achievement.getStatusDisplay(player);
         if(statusText != null){
            for(MutableComponent mutableText : statusText){
               achievementItem.addLoreLine(TextUtils.removeItalics(mutableText));
            }
         }
         
         int achPlayers = Math.toIntExact(allData.stream().filter(data -> data.hasAcheivement(achievement)).count());
         if(achPlayers == 0){
            achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("No Arcanists have achieved this").withStyle(ChatFormatting.DARK_PURPLE))));
         }else{
            int allArcanists = allData.size();
            DecimalFormat df = new DecimalFormat("#0.00");
            achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Acquired by ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(achPlayers + "").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(" Arcanists (").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(df.format((100 * (double) achPlayers) / ((double) allArcanists)) + "%").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE))));
         }
         
         if(profile.hasAcheivement(selectedArcanaItem.getId(), achievement.id)) achievementItem.glow();
         
         setSlot(46 + achieveSlots[i], achievementItem);
      }
      
      setTitle(selectedArcanaItem.getTranslatedName());
   }
   
   private void buildRecipeGui(){
      if(selectedRecipes.isEmpty()) return;
      ArcanaRecipe selectedRecipe = selectRecipe();
      if(selectedRecipe == null) return;
      Holder<Item> holder = BuiltInRegistries.ITEM.get(selectedRecipe.getOutputId()).orElse(null);
      if(holder == null) return;
      
      for(int i = 0; i < size; i++){
         if(i < 9){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i % 9 == 1){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i % 9 == 7){
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_VERTICAL, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else{
            setSlot(i, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      setSlot(1, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(7, GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_CONNECTOR, ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      ItemStack output = selectedRecipe.getDisplayStack();
      ArcanaItem arcanaItem = ArcanaRegistry.ARCANA_ITEMS.getValue(selectedRecipe.getOutputId());
      if(arcanaItem != null){
         this.selectedArcanaItem = arcanaItem;
         GuiElementBuilder book = new GuiElementBuilder(Items.WRITTEN_BOOK).hideDefaultTooltip();
         book.setName(Component.literal("Item Lore").withStyle(ChatFormatting.DARK_PURPLE));
         book.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal("to read about this Arcana Item.").withStyle(ChatFormatting.LIGHT_PURPLE))));
         if(arcanaItem.getAttributions().length > 0){
            book.addLoreLine(Component.literal(""));
            for(Tuple<MutableComponent, MutableComponent> attribution : arcanaItem.getAttributions()){
               book.addLoreLine(TextUtils.removeItalics(Component.literal("")
                     .append(attribution.getA().withStyle(ChatFormatting.DARK_PURPLE))
                     .append(attribution.getB().withStyle(ChatFormatting.LIGHT_PURPLE))));
            }
         }
         book.setCallback((type) -> {
            if(!forTome) return;
            List<List<Component>> loreData = arcanaItem.getBookLore();
            if(loreData != null){
               BookElementBuilder bookBuilder = new BookElementBuilder();
               loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
               LoreGui loreGui = new LoreGui(player, bookBuilder, this);
               loreGui.open();
            }else{
               player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED), false);
            }
         });
         setSlot(2, book);
      }
      
      GuiElementBuilder outputElem = GuiElementBuilder.from(output);
      outputElem.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.COMPENDIUM);
      });
      setSlot(4, outputElem);
      
      GuiElementBuilder structure = getMultiblockItem();
      if(structure != null && !forAnvil) setSlot(0, structure);
      
      GuiElementBuilder notes = getNotesItem();
      if(notes != null && forTome) setSlot(8, notes);
      
      ArcanaIngredient[][] ingredients = selectedRecipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i / 5][i % 5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(ingredients[i / 5][i % 5] instanceof ExplainIngredient){
            craftingElement.hideDefaultTooltip();
         }
         ArcanaItem arcaneIngredient = ArcanaItemUtils.identifyItem(ingredient);
         if(arcaneIngredient != null){
            craftingElement.glow();
         }
         List<ArcanaRecipe> otherRecipes = RecipeManager.getRecipesFor(ingredient.getItem());
         if(!otherRecipes.isEmpty()){
            craftingElement.setCallback((type) -> {
               buildGui(TomeMode.RECIPE, null, otherRecipes, 0);
            });
         }
         setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
      HashMap<String, Tuple<Integer, ItemStack>> ingredList = selectedRecipe.getIngredientList();
      recipeItem.setName(Component.literal("Total Ingredients").withStyle(ChatFormatting.DARK_PURPLE));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
      for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
         recipeItem.addLoreLine(TextUtils.removeItalics(getIngredStr(ingred)));
      }
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      int slotCount = 0;
      for(ArcanaItem item : selectedRecipe.getForgeRequirementList()){
         GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
         MutableComponent requiresText = Component.literal("")
               .append(Component.literal("Requires").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(item.getTranslatedName().withStyle(ChatFormatting.AQUA));
         recipeItem.addLoreLine(TextUtils.removeItalics(requiresText));
         reqItem.setName(requiresText);
         setSlot(REQUIREMENT_SLOTS[slotCount], reqItem);
         slotCount++;
      }
      if(!selectedRecipe.getForgeRequirementList().isEmpty())
         recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("Does not include item data").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
      
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to copy the materials list to your clipboard").withStyle(ChatFormatting.LIGHT_PURPLE))));
      recipeItem.setCallback((type) -> {
         if(!forTome) return;
         StringBuilder copyString = new StringBuilder();
         for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
            copyString.append(getIngredStr(ingred).getString()).append("\n");
         }
         player.sendSystemMessage(Component.translatable("text.arcananovum.materials_copy_message").withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.arcananovum.materials_copy_message"))).withClickEvent(new ClickEvent.CopyToClipboard(copyString.toString()))));
         permaCloseFlag = true;
         close();
      });
      setSlot(6, recipeItem);
      
      int size = selectedRecipes.size();
      if(size > 1){
         GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW));
         nextPage.setName(Component.translatable("gui.arcananovum.next_recipe", recipeInd+1, size).withColor(this.primaryTextColor));
         nextPage.addLoreLine(Component.translatable("text.borislib.two_elements", Component.translatable("gui.borislib.click").withColor(this.action1TextColor), Component.translatable("gui.arcananovum.next_recipe_sub").withColor(this.secondaryTextColor)));
         nextPage.setCallback((type) -> {
            recipeInd = (recipeInd+1) % size;
            buildRecipeGui();
         });
         setSlot(53,nextPage);
         
         GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW));
         prevPage.setName(Component.translatable("gui.arcananovum.prev_recipe", recipeInd+1, size).withColor(this.primaryTextColor));
         prevPage.addLoreLine(Component.translatable("text.borislib.two_elements", Component.translatable("gui.borislib.click").withColor(this.action1TextColor), Component.translatable("gui.arcananovum.prev_recipe_sub").withColor(this.secondaryTextColor)));
         prevPage.setCallback((type) -> {
            recipeInd = (recipeInd-1) % size;
            if(recipeInd < 0) recipeInd = size-1;
            buildRecipeGui();
         });
         setSlot(45,prevPage);
      }
      
      setTitle(Component.literal("Recipe for ").append(output.getItemName().copy().withStyle(s -> s.withItalic(false).withBold(false).withColor(ChatFormatting.BLACK))));
   }
   
   private void buildResearchGui(){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(selectedArcanaItem == null){
         close();
         return;
      }
      
      List<ResearchTask> allTasks = ResearchTasks.getUniqueTasks(selectedArcanaItem.getResearchTasks()).stream().toList();
      List<ResearchTask> uncompletedOnly = allTasks.stream().filter(task -> !task.isAcquired(player)).toList();
      boolean allAcquired = allTasks.stream().allMatch(task -> task.isAcquired(player));
      
      int allTaskPages = (int) (Math.ceil(allTasks.size() / (double) pageSize()));
      int uncompletedPages = (int) (Math.ceil(uncompletedOnly.size() / (double) pageSize()));
      
      List<Tuple<ResearchTask, Integer>> taskPair;
      List<ResearchTask> tasks = hideCompletedResearch ? uncompletedOnly : allTasks;
      int numPages = Math.max(1,(int) (Math.ceil(tasks.size() / (double) pageSize())));
      if(allTaskPages == uncompletedPages){
         taskPair = AlgoUtils.randomlySpace(allTasks, allTaskPages * pageSize(), WorldOptions.parseSeed(selectedArcanaItem.getId()).orElse(WorldOptions.randomSeed()));
         if(hideCompletedResearch){
            taskPair = taskPair.stream().filter(pair -> !pair.getA().isAcquired(player)).toList();
         }
      }else{
         taskPair = AlgoUtils.randomlySpace(tasks, numPages * pageSize(), WorldOptions.parseSeed(selectedArcanaItem.getId()).orElse(WorldOptions.randomSeed()));
      }
      List<ResearchTask> paddedTasks = new ArrayList<>(numPages * pageSize());
      for(int i = 0; i < numPages * pageSize(); i++){
         paddedTasks.add(i,null);
      }
      for(Tuple<ResearchTask, Integer> pair : taskPair){
         paddedTasks.set(pair.getB(),pair.getA());
      }
      GuiMode<ResearchTask> config = getMode(3);
      config.setItems(paddedTasks);
      
      ArcanaRarity rarity = selectedArcanaItem.getRarity();
      int paperCost = profile.getArcanePaperRequirement(rarity);
      Item paperType = ArcanaRarity.getArcanePaper(rarity);
      
      GuiHelper.outlineGUI(this, ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GuiElementBuilder itemElem = GuiElementBuilder.from(selectedArcanaItem.getPrefItem()).glow();
      itemElem.setCallback((type) -> {
         if(forTome) buildGui(TomeMode.COMPENDIUM);
      });
      setSlot(4, itemElem);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      String filterStr = hideCompletedResearch ? " to show all research." : " to hide completed research.";
      filterBuilt.setName(Component.literal("Filter Research Tasks").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(filterStr).withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.setCallback((type) -> {
         hideCompletedResearch = !hideCompletedResearch;
         buildPage();
      });
      setSlot(8, filterBuilt);
      
      GuiElementBuilder notes = new GuiElementBuilder(ArcanaRegistry.ARCANE_NOTES).glow(allAcquired).hideDefaultTooltip();
      notes.setName((Component.literal("")
            .append(Component.literal("Scribe Notes").withStyle(ChatFormatting.DARK_PURPLE))));
      if(allAcquired){
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal("to complete your research").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         notes.addLoreLine(Component.literal(""));
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Costs ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(paperCost + " ").withStyle(ChatFormatting.YELLOW))
               .append(Component.translatable(paperType.getDescriptionId()).withStyle(ArcanaRarity.getColor(rarity))))));
         notes.setCallback((type) -> {
            if(MinecraftUtils.removeItems(player, paperType, paperCost)){
               ItemStack newNotes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.UNLOCK_ID_TAG, selectedArcanaItem.getId());
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.COST_TAG, paperCost);
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.AUTHOR_TAG, player.getGameProfile().name());
               ArcaneNotesItem.buildLore(newNotes);
               MinecraftUtils.returnItems(new SimpleContainer(newNotes), player);
               
               SoundUtils.playSongToPlayer(player, SoundEvents.BOOK_PAGE_TURN, 2, 0.75f);
               SoundUtils.playSongToPlayer(player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 2, 0.9f);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 2);
            }else{
               player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                     .append(Component.translatable(paperType.getDescriptionId()).withStyle(ChatFormatting.ITALIC, ArcanaRarity.getColor(rarity))), false);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
            }
         });
      }else{
         notes.addLoreLine(TextUtils.removeItalics(Component.literal("Complete all research tasks to unlock this item.").withStyle(ChatFormatting.LIGHT_PURPLE)));
      }
      setSlot(49, notes);
      
      setTitle(selectedArcanaItem.getTranslatedName().append(Component.literal(" Research")));
   }
   
   private GuiElementBuilder getNotesItem(){
      if(this.selectedArcanaItem == null) return null;
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      int paperCost = profile.getArcanePaperRequirement(selectedArcanaItem.getRarity());
      GuiElementBuilder notes = new GuiElementBuilder(ArcanaRegistry.ARCANE_NOTES).glow().hideDefaultTooltip();
      notes.setName((Component.literal("")
            .append(Component.literal("Research Notes").withStyle(ChatFormatting.DARK_PURPLE))));
      if(profile.hasResearched(selectedArcanaItem)){
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Left Click ").withStyle(ChatFormatting.AQUA))
               .append(Component.literal("to scribe a spare set of Arcane Notes").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("(Costs ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(Component.literal(paperCost + " ").withStyle(ChatFormatting.YELLOW))
               .append(Component.translatable(ArcanaRarity.getArcanePaper(selectedArcanaItem.getRarity()).getDescriptionId()).withStyle(ArcanaRarity.getColor(selectedArcanaItem.getRarity())))
               .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE)))));
         notes.addLoreLine(Component.literal(""));
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Right Click ").withStyle(ChatFormatting.GREEN))
               .append(Component.literal("to view the research for this item").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }else{
         notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
               .append(Component.literal("to view the research for this item").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }
      
      notes.setCallback((type) -> {
         if(!forTome) return;
         if(type == ClickType.MOUSE_RIGHT || !profile.hasResearched(selectedArcanaItem)){
            buildGui(TomeMode.RESEARCH, selectedArcanaItem);
         }else{
            ArcanaRarity rarity = selectedArcanaItem.getRarity();
            Item paperType = ArcanaRarity.getArcanePaper(rarity);
            int cost = profile.getArcanePaperRequirement(rarity);
            if(MinecraftUtils.removeItems(player, paperType, cost)){
               ItemStack newNotes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.UNLOCK_ID_TAG, selectedArcanaItem.getId());
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.COST_TAG, cost);
               ArcanaItem.putProperty(newNotes, ArcaneNotesItem.AUTHOR_TAG, player.getGameProfile().name());
               ArcaneNotesItem.buildLore(newNotes);
               MinecraftUtils.returnItems(new SimpleContainer(newNotes), player);
               
               SoundUtils.playSongToPlayer(player, SoundEvents.BOOK_PAGE_TURN, 2, 0.75f);
               SoundUtils.playSongToPlayer(player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 2, 0.9f);
               SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 2);
            }else{
               player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                     .append(Component.translatable(paperType.getDescriptionId()).withStyle(ChatFormatting.ITALIC, ArcanaRarity.getColor(rarity))), false);
               SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, .5f);
            }
         }
      });
      return notes;
   }
   
   private GuiElementBuilder getMultiblockItem(){
      if(selectedArcanaItem instanceof MultiblockCore multicore){
         LinkedHashMap<Item, Integer> mbMats = new LinkedHashMap<>();
         multicore.getMultiblock().getMaterialList().entrySet().stream()
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .forEachOrdered(entry -> mbMats.put(entry.getKey(), entry.getValue()));
         
         GuiElementBuilder structure = new GuiElementBuilder(Items.BRICKS).hideDefaultTooltip();
         structure.setName(Component.literal("Multi-Block Structure").withStyle(ChatFormatting.DARK_PURPLE));
         structure.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
               .append(Component.literal("to view this block's Structure").withStyle(ChatFormatting.LIGHT_PURPLE))));
         structure.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Right Click ").withStyle(ChatFormatting.GREEN))
               .append(Component.literal("to copy the materials list to your clipboard").withStyle(ChatFormatting.LIGHT_PURPLE))));
         structure.addLoreLine(Component.literal(""));
         structure.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Materials ").withStyle(ChatFormatting.DARK_PURPLE))));
         structure.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
         
         for(Map.Entry<Item, Integer> entry : mbMats.entrySet()){
            Item matItem = entry.getKey();
            int num = entry.getValue();
            int stacks = num / matItem.getDefaultMaxStackSize();
            int rem = num % matItem.getDefaultMaxStackSize();
            
            MutableComponent text = Component.literal("")
                  .append(Component.translatable(matItem.getDescriptionId()).withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(num + "").withStyle(ChatFormatting.GREEN));
            if(num > matItem.getDefaultMaxStackSize()){
               text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE));
               if(rem > 0){
                  text.append("(" + stacks + " Stacks + " + rem + ")").withStyle(ChatFormatting.YELLOW);
               }else{
                  text.append("(" + stacks + " Stacks)").withStyle(ChatFormatting.YELLOW);
               }
            }
            structure.addLoreLine(TextUtils.removeItalics(text));
         }
         structure.setCallback((type) -> {
            if(!forTome) return;
            if(type == ClickType.MOUSE_RIGHT){
               StringBuilder copyString = new StringBuilder();
               multicore.getMultiblock().getMaterialList().entrySet().stream()
                     .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                     .forEachOrdered(entry -> mbMats.put(entry.getKey(), entry.getValue()));
               
               for(Map.Entry<Item, Integer> entry : mbMats.entrySet()){
                  Item matItem = entry.getKey();
                  int num = entry.getValue();
                  int stacks = num / matItem.getDefaultMaxStackSize();
                  int rem = num % matItem.getDefaultMaxStackSize();
                  
                  copyString.append(matItem.getName().getString()).append(" - ").append(num);
                  
                  if(num > matItem.getDefaultMaxStackSize()){
                     copyString.append(" - ");
                     if(rem > 0){
                        copyString.append("(").append(stacks).append(" Stacks + ").append(rem).append(")");
                     }else{
                        copyString.append("(").append(stacks).append(" Stacks)");
                     }
                  }
                  copyString.append("\n");
               }
               
               player.sendSystemMessage(Component.translatable("text.arcananovum.materials_copy_message").withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.arcananovum.materials_copy_message"))).withClickEvent(new ClickEvent.CopyToClipboard(copyString.toString()))));
               permaCloseFlag = true;
               close();
            }else{
               multicore.getMultiblock().displayStructure(new Multiblock.MultiblockCheck(player.level(), player.blockPosition(), player.level().getBlockState(player.blockPosition()), new BlockPos(multicore.getCheckOffset()), null), player);
               permaCloseFlag = true;
               close();
            }
         });
         return structure;
      }
      return null;
   }
   
   private ArcanaRecipe selectRecipe(){
      ArcanaRecipe found = selectRecipe(recipeInd);
      this.recipeInd = selectedRecipes.indexOf(found);
      return found;
   }
   
   private ArcanaRecipe selectRecipe(int index){
      if(selectedRecipes.isEmpty()) return null;
      if(index < 0 || index >= selectedRecipes.size()){
         return selectedRecipes.getFirst();
      }
      return selectedRecipes.get(index);
   }
   
   public static MutableComponent getIngredStr(Map.Entry<String, Tuple<Integer, ItemStack>> ingred){
      ItemStack ingredStack = ingred.getValue().getB();
      int maxCount = ingredStack.getMaxStackSize();
      int num = ingred.getValue().getA();
      int stacks = num / maxCount;
      int rem = num % maxCount;
      MutableComponent text = Component.literal("")
            .append(Component.literal(ingred.getKey()).withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(num + "").withStyle(ChatFormatting.GREEN));
      if(num > maxCount){
         text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE));
         if(rem > 0){
            text.append("(" + stacks + " Stacks + " + rem + ")").withStyle(ChatFormatting.YELLOW);
         }else{
            text.append("(" + stacks + " Stacks)").withStyle(ChatFormatting.YELLOW);
         }
      }
      return text;
   }
   
   public enum TomeMode {
      PROFILE,
      COMPENDIUM,
      ITEM,
      RECIPE,
      NONE,
      ACHIEVEMENTS,
      LEADERBOARD,
      RESEARCH
   }
   
   // ========== Tome Filters and Sorts ==========
   
   @SuppressWarnings("DataFlowIssue")
   public static class TomeFilter extends GuiFilter<CompendiumEntry> {
      private static ServerPlayer player;
      
      public static final List<TomeFilter> FILTERS = new ArrayList<>();
      
      public static final TomeFilter NONE = new TomeFilter("gui.arcananovum.none", ChatFormatting.WHITE.getColor(),
            (entry) -> true);
      public static final TomeFilter RESEARCHED = new TomeFilter("gui.arcananovum.researched", ArcanaColors.ARCANE_PAGE_COLOR,
            (entry) -> entry instanceof ArcanaItemCompendiumEntry arcanaEntry && ArcanaNovum.data(getPlayer()).hasResearched(arcanaEntry.getArcanaItem()));
      public static final TomeFilter NOT_RESEARCHED = new TomeFilter("gui.arcananovum.not_researched", ArcanaColors.STARLIGHT_FORGE_COLOR,
            (entry) -> entry instanceof ArcanaItemCompendiumEntry arcanaEntry && !ArcanaNovum.data(getPlayer()).hasResearched(arcanaEntry.getArcanaItem()));
      public static final TomeFilter MUNDANE = new TomeFilter("gui.arcananovum.mundane", ChatFormatting.GRAY.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.MUNDANE));
      public static final TomeFilter EMPOWERED = new TomeFilter("gui.arcananovum.empowered", ChatFormatting.GREEN.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.EMPOWERED));
      public static final TomeFilter EXOTIC = new TomeFilter("gui.arcananovum.exotic", ChatFormatting.AQUA.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.EXOTIC));
      public static final TomeFilter SOVEREIGN = new TomeFilter("gui.arcananovum.sovereign", ChatFormatting.GOLD.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.SOVEREIGN));
      public static final TomeFilter DIVINE = new TomeFilter("gui.arcananovum.divine", ChatFormatting.LIGHT_PURPLE.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.DIVINE));
      public static final TomeFilter ITEMS = new TomeFilter("gui.arcananovum.items", ChatFormatting.DARK_AQUA.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.ITEMS));
      public static final TomeFilter BLOCKS = new TomeFilter("gui.arcananovum.blocks", ChatFormatting.DARK_PURPLE.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.BLOCKS));
      public static final TomeFilter FORGE = new TomeFilter("gui.arcananovum.forge", ChatFormatting.DARK_GREEN.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.FORGE));
      public static final TomeFilter ARROWS = new TomeFilter("gui.arcananovum.arrows", ChatFormatting.RED.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.ARROWS));
      public static final TomeFilter ALTARS = new TomeFilter("gui.arcananovum.altars", ChatFormatting.BLUE.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.ALTARS));
      public static final TomeFilter EQUIPMENT = new TomeFilter("gui.arcananovum.equipment", ChatFormatting.DARK_RED.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.EQUIPMENT));
      public static final TomeFilter CHARMS = new TomeFilter("gui.arcananovum.charms", ChatFormatting.YELLOW.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.CHARMS));
      public static final TomeFilter CATALYSTS = new TomeFilter("gui.arcananovum.catalysts", ChatFormatting.DARK_BLUE.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.CATALYSTS));
      public static final TomeFilter INGREDIENT = new TomeFilter("gui.arcananovum.ingredient", ChatFormatting.DARK_GRAY.getColor(),
            (entry) -> hasCategory(entry, TomeFilter.INGREDIENT));
      
      private TomeFilter(String key, int color, java.util.function.Predicate<CompendiumEntry> filter){
         super(key, color, filter);
         FILTERS.add(this);
      }
      
      private static boolean hasCategory(CompendiumEntry entry, TomeFilter category){
         TomeFilter[] cats = entry.getCategories();
         if(cats == null) return false;
         for(TomeFilter cat : cats){
            if(cat == category) return true;
         }
         return false;
      }
      
      public static void setPlayer(ServerPlayer player){
         TomeFilter.player = player;
      }
      
      public static ServerPlayer getPlayer(){
         return player;
      }
      
      @Override
      protected List<TomeFilter> getList(){
         return FILTERS;
      }
      
      @SuppressWarnings("unchecked")
      public TomeFilter getStaticDefault(){
         return NONE;
      }
   }
   
   @SuppressWarnings("DataFlowIssue")
   public static class TomeSort extends GuiSort<CompendiumEntry> {
      public static final List<TomeSort> SORTS = new ArrayList<>();
      
      public static final TomeSort RECOMMENDED = new TomeSort("gui.arcananovum.recommended", ChatFormatting.YELLOW.getColor(),
            Comparator.<CompendiumEntry>comparingInt(entry -> {
               int index = RECOMMENDED_LIST.indexOf(entry);
               return index == -1 ? Integer.MAX_VALUE : index;
            }));
      public static final TomeSort RARITY_ASC = new TomeSort("gui.arcananovum.rarity_ascending", ChatFormatting.LIGHT_PURPLE.getColor(),
            Comparator.<CompendiumEntry>comparingInt(CompendiumEntry::getRarityValue).thenComparing((entry) -> entry.getName().getString()));
      public static final TomeSort RARITY_DESC = new TomeSort("gui.arcananovum.rarity_descending", ChatFormatting.DARK_PURPLE.getColor(),
            Comparator.<CompendiumEntry>comparingInt((entry) -> -entry.getRarityValue()).thenComparing((entry) -> entry.getName().getString()));
      public static final TomeSort ALPHABETICAL = new TomeSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor(),
            Comparator.comparing((entry) -> entry.getName().getString()));
      
      private TomeSort(String key, int color, Comparator<CompendiumEntry> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<TomeSort> getList(){
         return SORTS;
      }
      
      @SuppressWarnings("unchecked")
      public TomeSort getStaticDefault(){
         return RECOMMENDED;
      }
   }
   
   // ========== Achievement Filters and Sorts ==========
   
   @SuppressWarnings("DataFlowIssue")
   public static class AchievementFilter extends GuiFilter<ArcanaAchievement> {
      public static final List<AchievementFilter> FILTERS = new ArrayList<>();
      private static ServerPlayer player;
      
      public static final AchievementFilter NONE = new AchievementFilter("gui.arcananovum.none", ChatFormatting.WHITE.getColor(),
            (ach) -> true);
      public static final AchievementFilter ACQUIRED = new AchievementFilter("gui.arcananovum.acquired", ChatFormatting.AQUA.getColor(),
            (ach) -> ArcanaNovum.data(getPlayer()).hasAcheivement(ach));
      public static final AchievementFilter NOT_ACQUIRED = new AchievementFilter("gui.arcananovum.not_acquired", ChatFormatting.RED.getColor(),
            (ach) -> !ArcanaNovum.data(getPlayer()).hasAcheivement(ach));
      
      private AchievementFilter(String key, int color, java.util.function.Predicate<ArcanaAchievement> filter){
         super(key, color, filter);
         FILTERS.add(this);
      }
      
      public static void setPlayer(ServerPlayer player){
         AchievementFilter.player = player;
      }
      
      public static ServerPlayer getPlayer(){
         return player;
      }
      
      @Override
      protected List<AchievementFilter> getList(){
         return FILTERS;
      }
      
      @SuppressWarnings("unchecked")
      public AchievementFilter getStaticDefault(){
         return NONE;
      }
   }
   
   @SuppressWarnings("DataFlowIssue")
   public static class AchievementSort extends GuiSort<ArcanaAchievement> {
      public static final List<AchievementSort> SORTS = new ArrayList<>();
      
      public static final AchievementSort RECOMMENDED = new AchievementSort("gui.arcananovum.item_recommended", ChatFormatting.YELLOW.getColor(),
            Comparator.comparingInt(ach ->
                  (RECOMMENDED_LIST.stream().map(entry -> entry instanceof ArcanaItemCompendiumEntry arcanaEntry ? arcanaEntry.getArcanaItem() : null).toList())
                        .indexOf(ach.getArcanaItem())));
      public static final AchievementSort XP_ASC = new AchievementSort("gui.arcananovum.xp_ascending", ChatFormatting.LIGHT_PURPLE.getColor(),
            Comparator.<ArcanaAchievement>comparingInt(ach -> ach.pointsReward).thenComparingInt(ach -> ach.xpReward));
      public static final AchievementSort XP_DESC = new AchievementSort("gui.arcananovum.xp_descending", ChatFormatting.DARK_PURPLE.getColor(),
            Comparator.<ArcanaAchievement>comparingInt(ach -> -ach.pointsReward).thenComparingInt(ach -> -ach.xpReward));
      public static final AchievementSort ALPHABETICAL = new AchievementSort("gui.borislib.alphabetical", ChatFormatting.GREEN.getColor(),
            Comparator.comparing(ach -> ach.getTranslatedName().getString()));
      
      private AchievementSort(String key, int color, Comparator<ArcanaAchievement> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<AchievementSort> getList(){
         return SORTS;
      }
      
      @SuppressWarnings("unchecked")
      public AchievementSort getStaticDefault(){
         return RECOMMENDED;
      }
   }
   
   // ========== Leaderboard Filters and Sorts ==========
   
   @SuppressWarnings("DataFlowIssue")
   public static class LeaderboardFilter extends GuiFilter<ArcanaPlayerData> {
      public static final List<LeaderboardFilter> FILTERS = new ArrayList<>();
      
      public static final LeaderboardFilter NONE = new LeaderboardFilter("gui.arcananovum.none", ChatFormatting.WHITE.getColor(),
            (data) -> true);
      public static final LeaderboardFilter ARCANIST = new LeaderboardFilter("gui.arcananovum.arcanist", ChatFormatting.LIGHT_PURPLE.getColor(),
            (data) -> data.getXP() > 1);
      public static final LeaderboardFilter MAX_LVL = new LeaderboardFilter("gui.arcananovum.max_level_player", ChatFormatting.GREEN.getColor(),
            (data) -> LevelUtils.levelFromXp(data.getXP()) >= 100);
      public static final LeaderboardFilter ABYSS = new LeaderboardFilter("gui.arcananovum.abyssal_arcanist", ChatFormatting.DARK_PURPLE.getColor(),
            (data) -> data.hasAcheivement(ArcanaAchievements.ALL_ACHIEVEMENTS));
      
      private LeaderboardFilter(String key, int color, Predicate<ArcanaPlayerData> filter){
         super(key, color, filter);
         FILTERS.add(this);
      }
      
      @Override
      protected List<LeaderboardFilter> getList(){
         return FILTERS;
      }
      
      @SuppressWarnings("unchecked")
      public LeaderboardFilter getStaticDefault(){
         return NONE;
      }
   }
   
   @SuppressWarnings("DataFlowIssue")
   public static class LeaderboardSort extends GuiSort<ArcanaPlayerData> {
      public static final List<LeaderboardSort> SORTS = new ArrayList<>();
      
      public static final LeaderboardSort XP_DESC = new LeaderboardSort("gui.arcananovum.xp_descending_recommended", ChatFormatting.LIGHT_PURPLE.getColor(),
            Comparator.<ArcanaPlayerData>comparingInt(data -> -data.getXP()));
      public static final LeaderboardSort XP_ASC = new LeaderboardSort("gui.arcananovum.xp_ascending", ChatFormatting.DARK_PURPLE.getColor(),
            Comparator.<ArcanaPlayerData>comparingInt(ArcanaPlayerData::getXP));
      public static final LeaderboardSort ACHIEVES_DESC = new LeaderboardSort("gui.arcananovum.achievements_descending", ChatFormatting.GREEN.getColor(),
            Comparator.<ArcanaPlayerData>comparingInt(data -> -data.totalAcquiredAchievements()));
      public static final LeaderboardSort SKILL_POINTS_DESC = new LeaderboardSort("gui.arcananovum.skill_points_descending", ChatFormatting.DARK_GREEN.getColor(),
            Comparator.<ArcanaPlayerData>comparingInt(data -> -data.getTotalSkillPoints()));
      public static final LeaderboardSort ALPHABETICAL = new LeaderboardSort("gui.borislib.alphabetical", ChatFormatting.AQUA.getColor(),
            Comparator.comparing(ArcanaPlayerData::getUsername));
      
      private LeaderboardSort(String key, int color, Comparator<ArcanaPlayerData> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<LeaderboardSort> getList(){
         return SORTS;
      }
      
      @SuppressWarnings("unchecked")
      public LeaderboardSort getStaticDefault(){
         return XP_DESC;
      }
   }
}
