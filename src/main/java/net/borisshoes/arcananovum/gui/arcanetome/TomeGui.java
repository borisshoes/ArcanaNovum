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
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.altars.TransmutationAltarRecipeGui;
import net.borisshoes.arcananovum.gui.twilightanvil.TwilightAnvilGui;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.normal.ArcaneNotesItem;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.WorldOptions;

import java.text.DecimalFormat;
import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.log;
import static net.borisshoes.arcananovum.ArcanaRegistry.RECOMMENDED_LIST;

public class TomeGui extends SimpleGui {
   private TomeMode mode;
   private ArcaneTome tome;
   private CompendiumSettings settings;
   public static final int[][] DYNAMIC_SLOTS = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   public static final int[] CRAFTING_SLOTS = {1,2,3,4,5,10,11,12,13,14,19,20,21,22,23,28,29,30,31,32,37,38,39,40,41};
   private ArcanaRecipe selectedRecipe;
   private boolean permaCloseFlag = false;
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    * @param mode                        mode of screen
    */
   public TomeGui(MenuType<?> type, ServerPlayer player, TomeMode mode, ArcaneTome tome, CompendiumSettings settings){
      super(type, player, false);
      this.mode = mode;
      this.tome = tome;
      this.settings = settings;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(mode == TomeMode.PROFILE){
         if(index == 49){
            tome.openGui(player,TomeMode.COMPENDIUM,settings);
         }else if(index == 4){
            // Guide gui
            BookElementBuilder bookBuilder = getGuideBook();
            LoreGui loreGui = new LoreGui(player,bookBuilder,tome,TomeMode.PROFILE,settings);
            loreGui.open();
         }else if(index == 10){
            // Leaderboard View
            tome.openGui(player, TomeMode.LEADERBOARD,settings);
         }else if(index == 19){
            // Achievements View
            tome.openGui(player, TomeMode.ACHIEVEMENTS,settings);
         }
      }else{
         boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
         if(mode == TomeMode.COMPENDIUM){
            if(index == 4){
               buildProfileGui(player);
            }else if(indexInCenter){
               List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
               int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(settings.getPage()-1);
               if(ind >= items.size()) return true;
               CompendiumEntry entry = items.get(ind);
               
               if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
                  ArcanaItem arcanaItem = arcanaEntry.getArcanaItem();
                  if(!ArcanaNovum.data(player).hasResearched(arcanaItem)){
                     tome.openResearchGui(player,settings,arcanaItem.getId());
                  }else{
                     if(type == ClickType.MOUSE_RIGHT){
                        if(arcanaItem.getRecipe() != null){
                           tome.openRecipeGui(player,settings, arcanaItem);
                        }else{
                           player.displayClientMessage(Component.literal("You Cannot Craft This Item").withStyle(ChatFormatting.RED),false);
                        }
                     }else{
                        tome.openItemGui(player,settings, arcanaItem.getId());
                     }
                  }
               }else if(entry instanceof IngredientCompendiumEntry ingredientEntry){
                  if(type.isLeft && ingredientEntry.hasBookLore()){
                     List<List<Component>> loreData = ingredientEntry.getBookLore();
                     if(loreData != null){
                        BookElementBuilder bookBuilder = new BookElementBuilder();
                        loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
                        LoreGui loreGui = new LoreGui(player,bookBuilder,tome,TomeMode.COMPENDIUM,settings,null);
                        loreGui.open();
                     }else{
                        player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED),false);
                     }
                  }else{
                     tome.openRecipeGui(player,settings, ingredientEntry.getName(), ingredientEntry.getRecipe(), ingredientEntry.getDisplayStack());
                  }
               }else if(entry instanceof TransmutationRecipesCompendiumEntry){
                  TransmutationAltarRecipeGui transmutationGui = new TransmutationAltarRecipeGui(player,this,Optional.empty());
                  transmutationGui.buildRecipeListGui();
                  transmutationGui.open();
               }
            }else if(index == 0){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setSortType(TomeSort.RECOMMENDED);
               }else{
                  settings.setSortType(TomeSort.cycleSort(settings.getSortType(),backwards));
               }
               
               buildCompendiumGui(this,player,settings);
            }else if(index == 8){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setFilterType(TomeFilter.NONE);
               }else{
                  settings.setFilterType(TomeFilter.cycleFilter(settings.getFilterType(),backwards));
               }
               
               List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
               int numPages = (int) Math.ceil((float)items.size()/28.0);
               if(settings.getPage() > numPages){
                  settings.setPage(numPages);
               }
               buildCompendiumGui(this,player,settings);
            }else if(index == 45){
               if(settings.getPage() > 1){
                  settings.setPage(settings.getPage()-1);
                  buildCompendiumGui(this,player,settings);
               }
            }else if(index == 53){
               List<CompendiumEntry> items = TomeGui.sortedFilteredEntryList(settings, player);
               int numPages = (int) Math.ceil((float)items.size()/28.0);
               if(settings.getPage() < numPages){
                  settings.setPage(settings.getPage()+1);
                  buildCompendiumGui(this,player,settings);
               }
            }
         }else if(mode == TomeMode.ACHIEVEMENTS){
            if(index == 4){
               if(type == ClickType.MOUSE_RIGHT){
                  tome.openGui(player,TomeMode.COMPENDIUM,settings);
               }else{
                  buildProfileGui(player);
               }
            }else if(indexInCenter){
               ItemStack item = this.getSlot(index).getItemStack();
               if(!item.isEmpty()){
                  String itemId = ArcanaItem.getStringProperty(item,ArcaneTome.DISPLAY_TAG);
                  ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(itemId);
                  if(arcanaItem != null){
                     if(ArcanaNovum.data(player).hasResearched(arcanaItem)){
                        tome.openItemGui(player,settings, itemId);
                     }else{
                        player.displayClientMessage(Component.literal("You Have Not Researched This Item").withStyle(ChatFormatting.RED),false);
                     }
                  }
                  
               }
            }else if(index == 0){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setSortType(AchievementSort.RECOMMENDED);
               }else{
                  settings.setSortType(AchievementSort.cycleSort(settings.getAchSortType(),backwards));
               }
         
               buildAchievementsGui(player,settings);
            }else if(index == 8){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setFilterType(AchievementFilter.NONE);
               }else{
                  settings.setFilterType(AchievementFilter.cycleFilter(settings.getAchFilterType(),backwards));
               }
         
               List<ArcanaAchievement> achs = TomeGui.sortedFilteredAchievementList(player,settings);
               int numPages = (int) Math.ceil((float)achs.size()/28.0);
               if(settings.getAchPage() > numPages){
                  settings.setAchPage(numPages);
               }
               buildAchievementsGui(player,settings);
            }else if(index == 45){
               if(settings.getAchPage() > 1){
                  settings.setAchPage(settings.getAchPage()-1);
                  buildAchievementsGui(player,settings);
               }
            }else if(index == 53){
               List<ArcanaAchievement> achs = TomeGui.sortedFilteredAchievementList(player,settings);
               int numPages = (int) Math.ceil((float)achs.size()/28.0);
               if(settings.getAchPage() < numPages){
                  settings.setAchPage(settings.getAchPage()+1);
                  buildAchievementsGui(player,settings);
               }
            }
         }else if(mode == TomeMode.LEADERBOARD){
            if(index == 4){
               buildProfileGui(player);
            }else if(index == 0){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setLeaderSortType(LeaderboardSort.XP_DESC);
               }else{
                  settings.setLeaderSortType(LeaderboardSort.cycleSort(settings.getLeaderSortType(),backwards));
               }
               
               buildLeaderboardGui(player,settings);
            }else if(index == 8){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
               if(shiftLeft){
                  settings.setLeaderFilterType(LeaderboardFilter.NONE);
               }else{
                  settings.setLeaderFilterType(LeaderboardFilter.cycleFilter(settings.getLeaderFilterType(),backwards));
               }
               
               List<UUID> players = TomeGui.sortedFilteredLeaderboardList(settings);
               int numPages = (int) Math.ceil((float)players.size()/28.0);
               if(settings.getLeaderboardPage() > numPages){
                  settings.setLeaderboardPage(Math.max(1,numPages));
               }
               buildLeaderboardGui(player,settings);
            }else if(index == 45){
               if(settings.getLeaderboardPage() > 1){
                  settings.setLeaderboardPage(settings.getLeaderboardPage()-1);
                  buildLeaderboardGui(player,settings);
               }
            }else if(index == 53){
               List<UUID> players = TomeGui.sortedFilteredLeaderboardList(settings);
               int numPages = (int) Math.ceil((float)players.size()/28.0);
               if(settings.getLeaderboardPage() < numPages){
                  settings.setLeaderboardPage(settings.getLeaderboardPage()+1);
                  buildLeaderboardGui(player,settings);
               }
            }
         }else if(mode == TomeMode.RECIPE){
            ItemStack item = this.getSlot(25).getItemStack();
            
            if(index == 7){
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               if(arcanaItem != null){
                  List<List<Component>> loreData = arcanaItem.getBookLore();
                  if(loreData != null){
                     BookElementBuilder bookBuilder = new BookElementBuilder();
                     loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
                     LoreGui loreGui = new LoreGui(player,bookBuilder,tome,TomeMode.RECIPE,settings,arcanaItem.getId());
                     loreGui.open();
                  }else{
                     player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED),false);
                  }
               }
            }else if(index == 25 || index == 26){
               tome.openGui(player,TomeMode.COMPENDIUM,settings);
            }else if(index == 43){
               if(selectedRecipe != null){
                  StringBuilder copyString = new StringBuilder();
                  HashMap<String, Tuple<Integer, ItemStack>> ingredList = selectedRecipe.getIngredientList();
                  for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
                     copyString.append(getIngredStr(ingred).getString()).append("\n");
                  }
                  
                  player.sendSystemMessage(Component.translatable("text.arcananovum.materials_copy_message").withStyle(s -> s.withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.arcananovum.materials_copy_message"))).withClickEvent(new ClickEvent.CopyToClipboard(copyString.toString()))));
                  permaCloseFlag = true;
                  close();
               }
            }else if(index > 9 && index < 36 && (index % 9 == 1 || index % 9 == 2 || index % 9 == 3 || index % 9 == 4 ||index % 9 == 5)){
               ItemStack ingredStack = this.getSlot(index).getItemStack();
               ArcanaItem arcanaItem1 = ArcanaItemUtils.identifyItem(ingredStack);
               if(arcanaItem1 != null){
                  tome.openRecipeGui(player,settings, arcanaItem1);
               }
            }
         }else if(mode == TomeMode.ITEM){
            ItemStack item = this.getSlot(4).getItemStack();
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
            
            if(index == 0){
               if(arcanaItem instanceof MultiblockCore multicore){
                  if(type == ClickType.MOUSE_RIGHT){
                     LinkedHashMap<Item, Integer> mbMats = new LinkedHashMap<>();
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
                     multicore.getMultiblock().displayStructure(new Multiblock.MultiblockCheck(player.level(),player.blockPosition(),player.level().getBlockState(player.blockPosition()),new BlockPos(multicore.getCheckOffset()),null),player);
                     permaCloseFlag = true;
                     close();
                  }
               }
            }else if(index == 2){
               if(arcanaItem.getRecipe() != null){
                  tome.openRecipeGui(player,settings, arcanaItem);
               }else{
                  player.displayClientMessage(Component.literal("You Cannot Craft This Item").withStyle(ChatFormatting.RED),false);
               }
            }else if(index == 4){
               tome.openGui(player,TomeMode.COMPENDIUM,settings);
            }else if(index == 6){
               List<List<Component>> loreData = arcanaItem.getBookLore();
               if(loreData != null){
                  BookElementBuilder bookBuilder = new BookElementBuilder();
                  loreData.forEach(list -> bookBuilder.addPage(list.toArray(new Component[0])));
                  LoreGui loreGui = new LoreGui(player,bookBuilder,tome, TomeMode.ITEM,settings, arcanaItem.getId());
                  loreGui.open();
               }else{
                  player.displayClientMessage(Component.literal("No Lore Found For That Item").withStyle(ChatFormatting.RED),false);
               }
            }else if(index == 8){
               if(type == ClickType.MOUSE_RIGHT){
                  tome.openResearchGui(player,settings,arcanaItem.getId());
               }else{
                  IArcanaProfileComponent profile = ArcanaNovum.data(player);
                  ArcanaRarity rarity = arcanaItem.getRarity();
                  Item paperType = ArcanaRarity.getArcanePaper(rarity);
                  int cost = profile.getArcanePaperRequirement(rarity);
                  if(MinecraftUtils.removeItems(player,paperType,cost)){
                     ItemStack newNotes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.UNLOCK_ID_TAG,arcanaItem.getId());
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.COST_TAG,cost);
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.AUTHOR_TAG,player.getGameProfile().name());
                     ArcaneNotesItem.buildLore(newNotes);
                     MinecraftUtils.returnItems(new SimpleContainer(newNotes),player);
                     
                     SoundUtils.playSongToPlayer(player, SoundEvents.BOOK_PAGE_TURN, 2, 0.75f);
                     SoundUtils.playSongToPlayer(player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 2, 0.9f);
                     SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 2);
                  }else{
                     player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                           .append(Component.translatable(paperType.getDescriptionId()).withStyle(ChatFormatting.ITALIC,ArcanaRarity.getColor(rarity))),false);
                     SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }else if(index >= 28 && index <= 35){ // Unlock augment
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
               int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
               ArcanaAugment augment = null;
               for(int i = 0; i < augmentSlots.length; i++){
                  if(index == 28+augmentSlots[i]){
                     augment = augments.get(i);
                     break;
                  }
               }
               
               if(augment != null){
                  IArcanaProfileComponent profile = ArcanaNovum.data(player);
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  ArcanaRarity[] tiers = augment.getTiers();
                  if(augmentLvl >= tiers.length) return true;
                  int cost = tiers[augmentLvl].rarity+1;
                  int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
                  if(cost <= unallocated){
                     profile.setAugmentLevel(augment.id,augmentLvl+1);
                     SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_PLING, 1, (.5f+((float)(augmentLvl+1)/(tiers.length-1))));
                     tome.openItemGui(player,settings, arcanaItem.getId());
                  }else{
                     player.displayClientMessage(Component.literal("Not Enough Skill Points").withStyle(ChatFormatting.RED),false);
                  }
               }
            }
         }else if(mode == TomeMode.RESEARCH){
            ItemStack item = this.getSlot(4).getItemStack();
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
            
            if(index == 4){
               tome.openGui(player,TomeMode.COMPENDIUM,settings);
            }else if(index == 8){
               settings.setHideCompletedResearch(!settings.hideCompletedResearch());
               List<ResearchTask> tasks = ResearchTasks.getUniqueTasks(arcanaItem.getResearchTasks()).stream().toList();
               if(settings.hideCompletedResearch()){
                  tasks = tasks.stream().filter(task -> !task.isAcquired(player)).toList();
               }
               int numPages = Math.max(1,(int) (Math.ceil(tasks.size() / 28.0)));
               if(settings.getResearchPage() > numPages){
                  settings.setResearchPage(numPages);
               }
               buildResearchGui(player,settings,arcanaItem.getId());
            }else if(index == 45){
               if(settings.getResearchPage() > 1){
                  settings.setResearchPage(settings.getResearchPage()-1);
                  buildResearchGui(player,settings,arcanaItem.getId());
               }
            }else if(index == 53){
               List<ResearchTask> tasks = ResearchTasks.getUniqueTasks(arcanaItem.getResearchTasks()).stream().toList();
               if(settings.hideCompletedResearch()){
                  tasks = tasks.stream().filter(task -> !task.isAcquired(player)).toList();
               }
               int numPages = (int) (Math.ceil(tasks.size() / 28.0));
               if(settings.getResearchPage() < numPages){
                  settings.setResearchPage(settings.getResearchPage()+1);
                  buildResearchGui(player,settings,arcanaItem.getId());
               }
            }else if(index == 49){
               List<ResearchTask> tasks = ResearchTasks.getUniqueTasks(arcanaItem.getResearchTasks()).stream().toList();
               IArcanaProfileComponent profile = ArcanaNovum.data(player);
               ArcanaRarity rarity = arcanaItem.getRarity();
               boolean allAcquired = tasks.stream().allMatch(task -> task.isAcquired(player));
               Item paperType = ArcanaRarity.getArcanePaper(rarity);
               int cost = profile.getArcanePaperRequirement(rarity);
               
               if(allAcquired){
                  if(MinecraftUtils.removeItems(player,paperType,cost)){
                     ItemStack newNotes = new ItemStack(ArcanaRegistry.ARCANE_NOTES);
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.UNLOCK_ID_TAG,arcanaItem.getId());
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.COST_TAG,cost);
                     ArcanaItem.putProperty(newNotes,ArcaneNotesItem.AUTHOR_TAG,player.getGameProfile().name());
                     ArcaneNotesItem.buildLore(newNotes);
                     MinecraftUtils.returnItems(new SimpleContainer(newNotes),player);
                     
                     SoundUtils.playSongToPlayer(player, SoundEvents.BOOK_PAGE_TURN, 2, 0.75f);
                     SoundUtils.playSongToPlayer(player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 2, 0.9f);
                     SoundUtils.playSongToPlayer(player, SoundEvents.ENCHANTMENT_TABLE_USE, 1, 2);
                  }else{
                     player.displayClientMessage(Component.literal("You do not have enough ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
                           .append(Component.translatable(paperType.getDescriptionId()).withStyle(ChatFormatting.ITALIC,ArcanaRarity.getColor(rarity))),false);
                     SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH,1,.5f);
                  }
               }
            }
         }
      }
      return true;
   }
   
   public void buildProfileGui(ServerPlayer player){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      setMode(TomeMode.PROFILE);
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      setSlot(27,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      setSlot(35,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.level().getServer());
      head.setName((Component.literal("").append(Component.literal(player.getScoreboardName()+"'s ").withStyle(ChatFormatting.AQUA)).append(Component.literal("Arcane Profile").withStyle(ChatFormatting.DARK_PURPLE))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" for a brief overview of Arcana Novum!").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      setSlot(4,head);
      
      GuiElementBuilder book = new GuiElementBuilder(Items.WRITTEN_BOOK);
      book.setName(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to go to the Arcana Items Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(49,book);
      
      
      int level = profile.getLevel();
      String readableXP = LevelUtils.readableInt(LevelUtils.getCurLevelXp(profile.getXP()));
      GuiElementBuilder lectern = new GuiElementBuilder(Items.LECTERN);
      lectern.setName(Component.literal("Arcana Level").withStyle(ChatFormatting.DARK_GREEN));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Arcana Level: "+level).withStyle(ChatFormatting.GREEN)));
      if(level == 100){
         lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Total Experience: "+ LevelUtils.readableInt(profile.getXP())).withStyle(ChatFormatting.GREEN)));
      }else{
         lectern.addLoreLine(TextUtils.removeItalics(Component.literal("Experience: "+readableXP+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(level))).withStyle(ChatFormatting.GREEN)));
      }
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("You can increase your arcana by crafting and using Arcana items!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      lectern.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(" to see the Leaderboard").withStyle(ChatFormatting.DARK_AQUA))));
      setSlot(10,lectern);
      
      
      if(level == 100){
         for(int i = 11; i <= 16; i++){
            setSlot(i,new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Component.literal("XP: "+LevelUtils.readableInt(profile.getXP())).withStyle(ChatFormatting.GREEN)));
         }
      }else{
         int filled = (int)Math.round((double)LevelUtils.getCurLevelXp(profile.getXP())/LevelUtils.nextLevelNewXp(profile.getLevel()) * 6.0);
         for(int i = 11; i <= 16; i++){
            if(i >= filled+11){
               setSlot(i,new GuiElementBuilder(Items.GLASS_BOTTLE).setName(Component.literal("XP: "+readableXP+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).withStyle(ChatFormatting.GREEN)));
               
            }else{
               setSlot(i,new GuiElementBuilder(Items.EXPERIENCE_BOTTLE).setName(Component.literal("XP: "+readableXP+"/"+LevelUtils.readableInt(LevelUtils.nextLevelNewXp(profile.getLevel()))).withStyle(ChatFormatting.GREEN)));
            }
         }
      }
      
      int totalSkillPoints = profile.getTotalSkillPoints();
      int spentSkillPoints = profile.getSpentSkillPoints();
      int bonusSkillPoints = profile.getBonusSkillPoints();
      GuiElementBuilder shelf = new GuiElementBuilder(Items.BOOKSHELF);
      shelf.setName(Component.literal("Skill Points").withStyle(ChatFormatting.DARK_AQUA));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Total Skill Points: "+totalSkillPoints).withStyle(ChatFormatting.AQUA)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Available Points: "+(totalSkillPoints-spentSkillPoints)).withStyle(ChatFormatting.AQUA)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Points From Leveling: "+LevelUtils.getLevelSkillPoints(level)).withStyle(ChatFormatting.BLUE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Points From Achievements: "+profile.getAchievementSkillPoints()).withStyle(ChatFormatting.BLUE)));
      if(bonusSkillPoints != 0) shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Bonus Skill Points: "+bonusSkillPoints).withStyle(ChatFormatting.BLUE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Allocate Skill Points to Augment Items!").withStyle(ChatFormatting.DARK_PURPLE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("Earn Skill Points From Leveling Up or From Achievements!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      shelf.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click Here").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(" to see all Achievements").withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(19,shelf);
      
      
      int books = (int)((double)spentSkillPoints/totalSkillPoints * 6.0);
      for(int i = 20; i <= 25; i++){
         if(i >= books+20){
            setSlot(i,new GuiElementBuilder(Items.BOOK).setName(Component.literal("Allocated Skill Points: "+spentSkillPoints+"/"+totalSkillPoints).withStyle(ChatFormatting.DARK_AQUA)));
            
         }else{
            setSlot(i,new GuiElementBuilder(Items.ENCHANTED_BOOK).setName(Component.literal("Allocated Skill Points: "+spentSkillPoints+"/"+totalSkillPoints).withStyle(ChatFormatting.DARK_AQUA)));
         }
      }
      
      int resolve = profile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromLevel(profile.getLevel(),resolve);
      GuiElementBuilder crystal = new GuiElementBuilder(Items.END_CRYSTAL);
      crystal.setName(Component.literal("Arcane Concentration").withStyle(ChatFormatting.BLUE));
      crystal.addLoreLine(TextUtils.removeItalics(Component.literal("Concentration: "+ ArcanaItemUtils.getUsedConcentration(player)+"/"+maxConc).withStyle(ChatFormatting.AQUA)));
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
      setSlot(37,crystal);
      
      
      int used = (int)Math.ceil((double) ArcanaItemUtils.getUsedConcentration(player)/maxConc * 6.0);
      boolean overConc = ArcanaItemUtils.getUsedConcentration(player) > maxConc;
      for(int i = 38; i <= 43; i++){
         if(overConc){
            setSlot(i,new GuiElementBuilder(Items.FIRE_CHARGE).setName(Component.literal("Concentration: "+ ArcanaItemUtils.getUsedConcentration(player)+"/"+maxConc).withStyle(ChatFormatting.RED)));
         }else if(i >= used+38){
            setSlot(i,new GuiElementBuilder(Items.SLIME_BALL).setName(Component.literal("Concentration: "+ ArcanaItemUtils.getUsedConcentration(player)+"/"+maxConc).withStyle(ChatFormatting.AQUA)));
         }else{
            setSlot(i,new GuiElementBuilder(Items.MAGMA_CREAM).setName(Component.literal("Concentration: "+ ArcanaItemUtils.getUsedConcentration(player)+"/"+maxConc).withStyle(ChatFormatting.AQUA)));
         }
         
      }
      
      setTitle(Component.literal("Arcane Profile"));
   }
   
   public static void buildCompendiumGui(SimpleGui gui, ServerPlayer player, TomeGui.CompendiumSettings settings){
      List<CompendiumEntry> items = sortedFilteredEntryList(settings, player);
      if(gui instanceof TomeGui tomeGui){
         tomeGui.setMode(TomeMode.COMPENDIUM);
      }else{
         items = items.stream().filter(entry -> entry instanceof ArcanaItemCompendiumEntry).toList();
      }
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      settings.setPage(Mth.clamp(settings.getPage(),1,Math.max(1,numPages)));
      List<CompendiumEntry> pageItems = AlgoUtils.listToPage(items, settings.getPage(),28);
      
      GuiHelper.outlineGUI(gui,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.level().getServer());
      head.setName((Component.literal("").append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))));
      if(gui instanceof TomeGui){
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click here").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to return to the Profile Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to view its page or research").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Right Click an item").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to see its recipe or research").withStyle(ChatFormatting.LIGHT_PURPLE)))));
         
      }else{
         head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to see its recipe").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      }
      gui.setSlot(4,head);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Component.literal("Filter Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle filter backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Current Filter: ").withStyle(ChatFormatting.AQUA)).append(TomeFilter.getColoredLabel(settings.getFilterType()))));
      gui.setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Arcana Items").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(TomeSort.getColoredLabel(settings.getSortType()))));
      gui.setSlot(0,sortBuilt);
      
      if(numPages > 1){
         GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextPage.setName(Component.literal("Next Page (" + settings.getPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         nextPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         gui.setSlot(53, nextPage);
         
         GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevPage.setName(Component.literal("Previous Page (" + settings.getPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         prevPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         gui.setSlot(45, prevPage);
      }
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               GuiElementBuilder builder = GuiElementBuilder.from(pageItems.get(k).getDisplayStack()).glow();
               if(pageItems.get(k) instanceof IngredientCompendiumEntry ice && ice.hasBookLore() && gui instanceof TomeGui){
                  builder.addLoreLine(Component.literal(""));
                  builder.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("Click").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(" to read about this Ingredient Item").withStyle(ChatFormatting.DARK_PURPLE))));
               }
               gui.setSlot((i*9+10)+j,builder);
            }else{
               gui.setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      gui.setTitle(Component.literal("Item Compendium"));
   }
   
   public void buildAchievementsGui(ServerPlayer player, TomeGui.CompendiumSettings settings){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      setMode(TomeMode.ACHIEVEMENTS);
      List<ArcanaAchievement> items = sortedFilteredAchievementList(player,settings);
      List<ArcanaAchievement> pageItems = AlgoUtils.listToPage(items, settings.getAchPage(),28);
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.level().getServer());
      head.setName((Component.literal("").append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click here").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to return to the Profile Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Right Click here").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to go to the Items Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Click an item").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to view its page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      setSlot(4,head);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Component.literal("Filter Achievements").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle filter backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Current Filter: ").withStyle(ChatFormatting.AQUA)).append(AchievementFilter.getColoredLabel(settings.getAchFilterType()))));
      setSlot(8,filterBuilt);
      
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Achievements").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(AchievementSort.getColoredLabel(settings.getAchSortType()))));
      setSlot(0,sortBuilt);
      
      if(numPages > 1){
         GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW)).hideDefaultTooltip();
         nextPage.setName(Component.literal("Next Page (" + settings.getAchPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         nextPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(53, nextPage);
         
         GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW)).hideDefaultTooltip();
         prevPage.setName(Component.literal("Previous Page (" + settings.getAchPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         prevPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(45, prevPage);
      }
      
      int k = 0;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < pageItems.size()){
               ArcanaAchievement baseAch = pageItems.get(k);
               ArcanaAchievement profileAchievement = profile.getAchievement(baseAch.getArcanaItem().getId(),baseAch.id);
               ArcanaAchievement achievement = profileAchievement != null ? profileAchievement : baseAch;
               
               ItemStack displayItem = achievement.getDisplayItem();
               ArcanaItem.putProperty(displayItem,ArcaneTome.DISPLAY_TAG,achievement.getArcanaItem().getId());
               GuiElementBuilder achievementItem = GuiElementBuilder.from(displayItem);
               achievementItem.hideDefaultTooltip().setName(Component.literal("").withStyle(ChatFormatting.LIGHT_PURPLE).append(achievement.getTranslatedName()).append(" - ").append(achievement.getArcanaItem().getTranslatedName()))
                     .addLoreLine(TextUtils.removeItalics(Component.literal("")
                           .append(Component.literal(LevelUtils.readableInt(achievement.xpReward)).withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal(""+achievement.pointsReward).withStyle(ChatFormatting.AQUA))
                           .append(Component.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").withStyle(ChatFormatting.DARK_AQUA))));
               
               for(String s : achievement.getDescription()){
                  achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal(s).withStyle(ChatFormatting.GRAY)));
               }
               
               MutableComponent[] statusText = achievement.getStatusDisplay(player);
               if(statusText != null){
                  for(MutableComponent mutableText : statusText){
                     achievementItem.addLoreLine(TextUtils.removeItalics(mutableText));
                  }
               }
               
               List<UUID> achPlayers = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
               if(achPlayers == null || achPlayers.isEmpty()){
                  achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("No Arcanists have achieved this").withStyle(ChatFormatting.DARK_PURPLE))));
               }else{
                  int allArcanists = (int) ArcanaNovum.PLAYER_XP_TRACKER.values().stream().filter(xp -> xp > 1).count();
                  int acquiredCount = achPlayers.size();
                  DecimalFormat df = new DecimalFormat("#0.00");
                  achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                        .append(Component.literal("Acquired by ").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(acquiredCount+"").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" Arcanists (").withStyle(ChatFormatting.DARK_PURPLE))
                        .append(Component.literal(df.format((100*(double)acquiredCount)/((double)allArcanists))+"%").withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE))));
               }
               
               if(profile.hasAcheivement(achievement.getArcanaItem().getId(),achievement.id)) achievementItem.glow();
               
               setSlot((i*9+10)+j,achievementItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      setTitle(Component.literal("All Arcana Achievements"));
   }
   
   public void buildLeaderboardGui(ServerPlayer player, TomeGui.CompendiumSettings settings){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      setMode(TomeMode.LEADERBOARD);
      List<UUID> items = sortedFilteredLeaderboardList(settings);
      List<UUID> pageItems = AlgoUtils.listToPage(items, settings.getLeaderboardPage(),28);
      HashMap<UUID,List<String>> achievementMap = ArcanaAchievements.getInvertedTracker();
      int numPages = (int) Math.ceil((float)items.size()/28.0);
      int numAchievements = ArcanaAchievements.registry.size();
      
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      GameProfile gameProfile = player.getGameProfile();
      GuiElementBuilder head = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(gameProfile,player.level().getServer());
      head.setName((Component.literal("").append(Component.literal(player.getScoreboardName()).withStyle(ChatFormatting.AQUA))));
      head.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Arcana Level: ").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal(""+profile.getLevel()).withStyle(ChatFormatting.LIGHT_PURPLE)))));
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
      setSlot(4,head);
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      filterBuilt.setName(Component.literal("Filter Arcanists").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle filter backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset filter.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Current Filter: ").withStyle(ChatFormatting.AQUA)).append(LeaderboardFilter.getColoredLabel(settings.getLeaderFilterType()))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.SORT)).hideDefaultTooltip();
      sortBuilt.setName(Component.literal("Sort Arcanists").withStyle(ChatFormatting.DARK_PURPLE));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(" to change current sort type.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Right Click").withStyle(ChatFormatting.GREEN)).append(Component.literal(" to cycle sort backwards.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Shift Left Click").withStyle(ChatFormatting.YELLOW)).append(Component.literal(" to reset sort.").withStyle(ChatFormatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      sortBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Sorting By: ").withStyle(ChatFormatting.AQUA)).append(LeaderboardSort.getColoredLabel(settings.getLeaderSortType()))));
      setSlot(0,sortBuilt);
      
      if(numPages > 1){
         GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW));
         nextPage.setName(Component.literal("Next Page (" + settings.getLeaderboardPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         nextPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(53, nextPage);
         
         GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW));
         prevPage.setName(Component.literal("Previous Page (" + settings.getLeaderboardPage() + "/" + numPages + ")").withStyle(ChatFormatting.DARK_PURPLE));
         prevPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(45, prevPage);
      }
      
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
                  playerGameProf = player.level().getServer().services().profileResolver().fetchById(playerId).orElseThrow();
                  playerItem = new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(playerGameProf,player.level().getServer());
                  playerItem.setName(Component.literal(playerGameProf.name()).withStyle(ChatFormatting.LIGHT_PURPLE));
               }catch(Exception e){
                  playerItem = new GuiElementBuilder(Items.BARRIER);
                  playerItem.setName(Component.literal("<UNKNOWN>").withStyle(ChatFormatting.LIGHT_PURPLE));
               }
               playerItem.hideDefaultTooltip();
               
               playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Arcana Level: ").withStyle(ChatFormatting.DARK_PURPLE)).append(Component.literal(""+playerLevel).withStyle(ChatFormatting.LIGHT_PURPLE)))));
               if(playerLevel == 100){
                  playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("").append(Component.literal("Total Experience: ").withStyle(ChatFormatting.DARK_GREEN)).append(Component.literal(LevelUtils.readableInt(playerXp)).withStyle(ChatFormatting.GREEN)))));
               }else{
                  playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                        .append(Component.literal("Experience: ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(LevelUtils.readableInt(LevelUtils.getCurLevelXp(playerXp))).withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("/").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal(LevelUtils.readableInt(LevelUtils.nextLevelNewXp(playerLevel))).withStyle(ChatFormatting.GREEN)))));
               }
               int playerAchievements = achievementMap.containsKey(playerId) ? achievementMap.get(playerId).size() : 0;
               playerItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                     .append(Component.literal("Achievements: ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(LevelUtils.readableInt(playerAchievements)).withStyle(ChatFormatting.AQUA))
                     .append(Component.literal("/").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(LevelUtils.readableInt(numAchievements)).withStyle(ChatFormatting.AQUA)))));
               
               setSlot((i*9+10)+j,playerItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
      
      setTitle(Component.literal("Arcana Leaderboard"));
   }
   
   public static void buildItemGui(SimpleGui gui, ServerPlayer player, String id){
      if(gui instanceof TomeGui tomeGui) tomeGui.setMode(TomeMode.ITEM);
      boolean isTwilightAnvil = gui instanceof TwilightAnvilGui;
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem == null){
         gui.close();
         return;
      }
      
      for(int i = 0; i < gui.getSize(); i++){
         if(i/9 == 1){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 0){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 8){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i/9 == 4){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      gui.setSlot(9,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(36,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(44,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      for(int i = 0; i < 9; i++){
         gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.PAGE_COLOR)).setName(Component.empty()).hideTooltip());
      }
      
      if(arcanaItem instanceof MultiblockCore multicore){
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
                  .append(Component.literal(num+"").withStyle(ChatFormatting.GREEN));
            if(num > matItem.getDefaultMaxStackSize()){
               text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE));
               if(rem > 0){
                  text.append("("+stacks+" Stacks + "+rem+")").withStyle(ChatFormatting.YELLOW);
               }else{
                  text.append("("+stacks+" Stacks)").withStyle(ChatFormatting.YELLOW);
               }
            }
            structure.addLoreLine(TextUtils.removeItalics(text));
         }
         if(!isTwilightAnvil) gui.setSlot(0,structure);
      }
      
      GuiElementBuilder book = new GuiElementBuilder(Items.WRITTEN_BOOK).hideDefaultTooltip();
      book.setName(Component.literal("Item Lore").withStyle(ChatFormatting.DARK_PURPLE));
      book.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal("to read about this Arcana Item").withStyle(ChatFormatting.LIGHT_PURPLE))));
      if(arcanaItem.getAttributions().length > 0){
         book.addLoreLine(Component.literal(""));
         for(Tuple<MutableComponent, MutableComponent> attribution : arcanaItem.getAttributions()){
            book.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(attribution.getA().withStyle(ChatFormatting.DARK_PURPLE))
                  .append(attribution.getB().withStyle(ChatFormatting.LIGHT_PURPLE))));
         }
      }
      if(!isTwilightAnvil) gui.setSlot(6,book);
      
      GuiElementBuilder table = new GuiElementBuilder(Items.CRAFTING_TABLE).hideDefaultTooltip();
      table.setName(Component.literal("Item Recipe").withStyle(ChatFormatting.DARK_PURPLE));
      table.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to view this item's recipe!").withStyle(ChatFormatting.LIGHT_PURPLE))));
      if(!isTwilightAnvil) gui.setSlot(2, table);
      
      gui.setSlot(4,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      int paperCost = profile.getArcanePaperRequirement(arcanaItem.getRarity());
      GuiElementBuilder notes = new GuiElementBuilder(ArcanaRegistry.ARCANE_NOTES).glow().hideDefaultTooltip();
      notes.setName((Component.literal("")
            .append(Component.literal("Research Notes").withStyle(ChatFormatting.DARK_PURPLE))));
      notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Left Click ").withStyle(ChatFormatting.AQUA))
            .append(Component.literal("to scribe a spare set of Arcane Notes").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("(Costs ").withStyle(ChatFormatting.DARK_PURPLE))
            .append(Component.literal(paperCost+" ").withStyle(ChatFormatting.YELLOW))
            .append(Component.translatable(ArcanaRarity.getArcanePaper(arcanaItem.getRarity()).getDescriptionId()).withStyle(ArcanaRarity.getColor(arcanaItem.getRarity())))
            .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE)))));
      notes.addLoreLine(Component.literal(""));
      notes.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Right Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to view the research for this item").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      gui.setSlot(8,notes);
      
      GuiElementBuilder augmentPane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.PAGE_COLOR)).hideDefaultTooltip();
      augmentPane.setName(Component.literal("Augments:").withStyle(ChatFormatting.DARK_PURPLE));
      augmentPane.addLoreLine(TextUtils.removeItalics(Component.literal("Unlocked augments can be applied to enhance Arcana Items!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(arcanaItem);
      int[] augmentSlots = DYNAMIC_SLOTS[augments.size()];
      for(int i = 0; i < 7; i++){
         gui.setSlot(19+i,augmentPane);
         gui.setSlot(28+i,augmentPane);
      }
      for(int i = 0; i < augmentSlots.length; i++){
         ArcanaAugment augment = augments.get(i);
         gui.clearSlot(19+augmentSlots[i]);
         gui.clearSlot(28+augmentSlots[i]);
         
         int augmentLvl = profile.getAugmentLevel(augment.id);
         
         GuiElementBuilder augmentItem1 = GuiElementBuilder.from(augment.getDisplayItem());
         augmentItem1.hideDefaultTooltip().setName(augment.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE)).addLoreLine(TextUtils.removeItalics(augment.getTierDisplay()));
         
         for(String s : augment.getDescription()){
            augmentItem1.addLoreLine(TextUtils.removeItalics(Component.literal(s).withStyle(ChatFormatting.GRAY)));
         }
         if(augmentLvl > 0) augmentItem1.glow();
         
         int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
         MutableComponent titleText = augmentLvl == 0 ? Component.literal("Unlock Level 1").withStyle(ChatFormatting.LIGHT_PURPLE) : Component.literal("Current Level: ").withStyle(ChatFormatting.DARK_PURPLE).append(Component.literal(""+augmentLvl).withStyle(ChatFormatting.LIGHT_PURPLE));
         ArcanaRarity[] tiers = augment.getTiers();
         Item concrete = augmentLvl == tiers.length ? Items.WHITE_CONCRETE : ArcanaRarity.getColoredConcrete(tiers[augmentLvl]);
         
         GuiElementBuilder augmentItem2 = new GuiElementBuilder(concrete);
         
         
         if(augmentLvl == tiers.length){
            augmentItem2.hideDefaultTooltip().setName(
                  Component.literal("Level ").withStyle(ChatFormatting.DARK_PURPLE)
                        .append(Component.literal(""+augmentLvl).withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" Unlocked").withStyle(ChatFormatting.DARK_PURPLE)));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Max Level").withStyle(ChatFormatting.AQUA))));
            augmentItem2.glow();
         }else{
            augmentItem2.hideDefaultTooltip().setName(titleText);
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Next Level: ").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal((augmentLvl+1)+"").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" (").withStyle(ChatFormatting.BLUE))
                  .append(ArcanaRarity.getColoredLabel(tiers[augmentLvl],false))
                  .append(Component.literal(")").withStyle(ChatFormatting.BLUE))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Skill Point Cost: ").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal((tiers[augmentLvl].rarity+1)+"").withStyle(ChatFormatting.DARK_AQUA))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("(").withStyle(ChatFormatting.BLUE))
                  .append(Component.literal(unallocated+"").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" Unallocated Points)").withStyle(ChatFormatting.BLUE))));
            augmentItem2.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Click To Unlock").withStyle(ChatFormatting.AQUA))));
         }
         
         gui.setSlot(19+augmentSlots[i], augmentItem1);
         gui.setSlot(28+augmentSlots[i], augmentItem2);
      }
      
      GuiElementBuilder achievePane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.PAGE_COLOR)).hideDefaultTooltip();
      achievePane.setName(Component.literal("Achievements:").withStyle(ChatFormatting.DARK_PURPLE));
      achievePane.addLoreLine(TextUtils.removeItalics(Component.literal("Earning Achievements Grants Skill Points and XP!").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      List<ArcanaAchievement> achievements = ArcanaAchievements.getItemAchievements(arcanaItem);
      int[] achieveSlots = DYNAMIC_SLOTS[achievements.size()];
      for(int i = 0; i < 7; i++){
         gui.setSlot(46+i,achievePane);
      }
      for(int i = 0; i < achievements.size(); i++){
         ArcanaAchievement achievement = achievements.get(i);
         gui.clearSlot(46+achieveSlots[i]);
         
         GuiElementBuilder achievementItem = GuiElementBuilder.from(achievement.getDisplayItem());
         achievementItem.hideDefaultTooltip().setName(achievement.getTranslatedName().withStyle(ChatFormatting.LIGHT_PURPLE))
               .addLoreLine(TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal(LevelUtils.readableInt(achievement.xpReward)).withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(" XP").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal(""+achievement.pointsReward).withStyle(ChatFormatting.AQUA))
                     .append(Component.literal(achievement.pointsReward != 1 ? " Skill Points" : " Skill Point").withStyle(ChatFormatting.DARK_AQUA))));
         
         for(String s : achievement.getDescription()){
            achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal(s).withStyle(ChatFormatting.GRAY)));
         }
         
         MutableComponent[] statusText = achievement.getStatusDisplay(player);
         if(statusText != null){
            for(MutableComponent mutableText : statusText){
               achievementItem.addLoreLine(TextUtils.removeItalics(mutableText));
            }
         }
         
         List<UUID> achPlayers = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
         if(achPlayers == null || achPlayers.isEmpty()){
            achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("No Arcanists have achieved this").withStyle(ChatFormatting.DARK_PURPLE))));
         }else{
            int allArcanists = (int) ArcanaNovum.PLAYER_XP_TRACKER.values().stream().filter(xp -> xp > 1).count();
            int acquiredCount = achPlayers.size();
            DecimalFormat df = new DecimalFormat("#0.00");
            achievementItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Acquired by ").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(acquiredCount+"").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(" Arcanists (").withStyle(ChatFormatting.DARK_PURPLE))
                  .append(Component.literal(df.format((100*(double)acquiredCount)/((double)allArcanists))+"%").withStyle(ChatFormatting.LIGHT_PURPLE))
                  .append(Component.literal(")").withStyle(ChatFormatting.DARK_PURPLE))));
         }
         
         if(profile.hasAcheivement(arcanaItem.getId(),achievement.id)) achievementItem.glow();
         
         gui.setSlot(46+achieveSlots[i], achievementItem);
      }
      
      gui.setTitle(arcanaItem.getTranslatedName());
   }
   
   public void buildRecipeGui(SimpleGui gui, Component name, ArcanaRecipe recipe, ItemStack output){
      if(gui instanceof TomeGui tomeGui){
         tomeGui.setMode(TomeMode.RECIPE);
         selectedRecipe = recipe;
      }
      
      for(int i = 0; i < gui.getSize(); i++){
         if(i%9 == 0 || i%9 == 6){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 8){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }else if(i%9 == 7){
            gui.setSlot(i,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_HORIZONTAL,ArcanaColors.ARCANA_COLOR)).hideTooltip());
         }
      }
      gui.setSlot(17,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(35,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_RIGHT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(15,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(33,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_LEFT_CONNECTOR,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(7,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      gui.setSlot(43,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,ArcanaColors.ARCANA_COLOR)).hideTooltip());
      
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(output);
      if(arcanaItem != null){
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
         gui.setSlot(7,book);
      }
      
      gui.setSlot(25,GuiElementBuilder.from(output));
      
      GuiElementBuilder returnBook = new GuiElementBuilder(Items.KNOWLEDGE_BOOK);
      returnBook.setName((Component.literal("")
            .append(Component.literal("Arcana Items").withStyle(ChatFormatting.DARK_PURPLE))));
      returnBook.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to return to the Arcana Items Page").withStyle(ChatFormatting.LIGHT_PURPLE)))));
      gui.setSlot(26,returnBook);
      
      
      ArcanaIngredient[][] ingredients = recipe.getIngredients();
      for(int i = 0; i < 25; i++){
         ItemStack ingredient = ingredients[i/5][i%5].ingredientAsStack();
         GuiElementBuilder craftingElement = GuiElementBuilder.from(ingredient);
         if(ingredients[i/5][i%5] instanceof ExplainIngredient){
            craftingElement.hideDefaultTooltip();
         }
         if(ArcanaItemUtils.isArcane(ingredient)) craftingElement.glow();
         gui.setSlot(CRAFTING_SLOTS[i], craftingElement);
      }
      
      
      GuiElementBuilder recipeItem = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip();
      HashMap<String, Tuple<Integer, ItemStack>> ingredList = recipe.getIngredientList();
      recipeItem.setName(Component.literal("Total Ingredients").withStyle(ChatFormatting.DARK_PURPLE));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("-----------------------").withStyle(ChatFormatting.LIGHT_PURPLE)));
      for(Map.Entry<String, Tuple<Integer, ItemStack>> ingred : ingredList.entrySet()){
         recipeItem.addLoreLine(TextUtils.removeItalics(getIngredStr(ingred)));
      }
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      int slotCount = 0;
      for(ArcanaItem item : recipe.getForgeRequirementList()){
         GuiElementBuilder reqItem = GuiElementBuilder.from(item.getPrefItemNoLore()).hideDefaultTooltip().glow();
         MutableComponent requiresText = Component.literal("")
               .append(Component.literal("Requires").withStyle(ChatFormatting.GREEN))
               .append(Component.literal(" a ").withStyle(ChatFormatting.DARK_PURPLE))
               .append(item.getTranslatedName().withStyle(ChatFormatting.AQUA));
         recipeItem.addLoreLine(TextUtils.removeItalics(requiresText));
         reqItem.setName(requiresText);
         gui.setSlot(slotCount,reqItem);
         slotCount += 9;
      }
      if(!recipe.getForgeRequirementList().isEmpty()) recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("Does not include item data").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
      
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      recipeItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Click ").withStyle(ChatFormatting.GREEN))
            .append(Component.literal("to copy the materials list to your clipboard").withStyle(ChatFormatting.LIGHT_PURPLE))));
      
      gui.setSlot(43,recipeItem);
      
      gui.setTitle(Component.literal("Recipe for ").append(name));
   }
   
   public void buildResearchGui(ServerPlayer player, CompendiumSettings settings, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      setMode(TomeMode.RESEARCH);
      
      ArcanaItem arcanaItem = ArcanaItemUtils.getItemFromId(id);
      if(arcanaItem == null){
         close();
         return;
      }
      
      List<ResearchTask> allTasks = ResearchTasks.getUniqueTasks(arcanaItem.getResearchTasks()).stream().toList();
      List<ResearchTask> uncompletedOnly = allTasks.stream().filter(task -> !task.isAcquired(player)).toList();
      boolean allAcquired = allTasks.stream().allMatch(task -> task.isAcquired(player));
      
      int allTaskPages = (int) (Math.ceil(allTasks.size() / 28.0));
      int uncompletedPages = (int) (Math.ceil(uncompletedOnly.size() / 28.0));
      
      List<Tuple<ResearchTask,Integer>> taskPair;
      List<ResearchTask> tasks = settings.hideCompletedResearch() ? uncompletedOnly : allTasks;
      int numPages = (int) (Math.ceil(tasks.size() / 28.0));
      if(allTaskPages == uncompletedPages){
         taskPair = AlgoUtils.randomlySpace(allTasks,allTaskPages*28, WorldOptions.parseSeed(arcanaItem.getId()).orElse(WorldOptions.randomSeed()));
         if(settings.hideCompletedResearch()){
            taskPair = taskPair.stream().filter(pair -> !pair.getA().isAcquired(player)).toList();
         }
      }else{
         taskPair = AlgoUtils.randomlySpace(tasks,numPages*28, WorldOptions.parseSeed(arcanaItem.getId()).orElse(WorldOptions.randomSeed()));
      }
      
      int paperCost = profile.getArcanePaperRequirement(arcanaItem.getRarity());
      
      GuiHelper.outlineGUI(this,ArcanaColors.ARCANA_COLOR, Component.empty());
      
      setSlot(4,GuiElementBuilder.from(arcanaItem.getPrefItem()).glow());
      
      GuiElementBuilder filterBuilt = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.FILTER)).hideDefaultTooltip();
      String filterStr = settings.hideCompletedResearch() ? " to show all research." : " to hide completed research.";
      filterBuilt.setName(Component.literal("Filter Research Tasks").withStyle(ChatFormatting.DARK_PURPLE));
      filterBuilt.addLoreLine(TextUtils.removeItalics(Component.literal("").append(Component.literal("Click").withStyle(ChatFormatting.AQUA)).append(Component.literal(filterStr).withStyle(ChatFormatting.LIGHT_PURPLE))));
      setSlot(8,filterBuilt);
      
      if(numPages > 1){
         GuiElementBuilder nextPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.RIGHT_ARROW));
         nextPage.setName(Component.literal("Next Page ("+settings.getResearchPage()+"/"+numPages+")").withStyle(ChatFormatting.DARK_PURPLE));
         nextPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Next Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(53,nextPage);
         
         GuiElementBuilder prevPage = GuiElementBuilder.from(GraphicalItem.with(GraphicalItem.LEFT_ARROW));
         prevPage.setName(Component.literal("Previous Page ("+settings.getResearchPage()+"/"+numPages+")").withStyle(ChatFormatting.DARK_PURPLE));
         prevPage.addLoreLine(TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Click").withStyle(ChatFormatting.AQUA))
               .append(Component.literal(" to go to the Previous Page").withStyle(ChatFormatting.LIGHT_PURPLE))));
         setSlot(45,prevPage);
      }
      
      int pageLower = (settings.getResearchPage()-1) * 28;
      int pageUpper = settings.getResearchPage() * 28;
      
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            setSlot((i*9+10)+j,GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG,allAcquired ? ArcanaColors.ARCANE_PAGE_COLOR : ArcanaColors.PAGE_COLOR)).hideTooltip());
         }
      }
      
      for(Tuple<ResearchTask, Integer> pair : taskPair){
         int index = pair.getB();
         ResearchTask task = pair.getA();
         if(index >= pageUpper || index < pageLower) continue;
         boolean acquired = task.isAcquired(player);
         boolean hasPreReqs = task.satisfiedPreReqs(player);
         boolean hasPrePreReqs = task.satisfiedPrePreReqs(player);
         int guiIndex = (index % 28) + 10 + 2 * ((index % 28) / 7);
         GuiElementBuilder taskItem = GuiElementBuilder.from(task.getDisplayItem()).hideDefaultTooltip().setCount(1).glow(acquired);
         taskItem.setName(MutableComponent.create(task.getName().getContents()).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
         
         
         if(acquired){
            taskItem.addLoreLine(TextUtils.removeItalics(Component.literal("Completed").withStyle(ChatFormatting.AQUA)));
         }else{
            if(hasPreReqs){
               boolean colorSwitch = false;
               for(Component text : task.getDescription()){
                  if(!text.getString().isEmpty() && text.getString().charAt(0) != ' ') colorSwitch = !colorSwitch;
                  taskItem.addLoreLine(TextUtils.removeItalics(MutableComponent.create(text.getContents())).withColor(colorSwitch ? 0xe6d9bc : 0xb5a684));
               }
            }else{
               taskItem.setName(Component.literal("???").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            }
         }
         if(hasPrePreReqs){
            setSlot(guiIndex,taskItem);
         }
      }
      
      
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
               .append(Component.literal(paperCost+" ").withStyle(ChatFormatting.YELLOW))
               .append(Component.translatable(ArcanaRarity.getArcanePaper(arcanaItem.getRarity()).getDescriptionId()).withStyle(ArcanaRarity.getColor(arcanaItem.getRarity()))))));
      }else{
         notes.addLoreLine(TextUtils.removeItalics(Component.literal("Complete all research tasks to unlock this item.").withStyle(ChatFormatting.LIGHT_PURPLE)));
      }
      setSlot(49,notes);
      
      setTitle(arcanaItem.getTranslatedName().append(Component.literal(" Research")));
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
            .append(Component.literal(num+"").withStyle(ChatFormatting.GREEN));
      if(num > maxCount){
         text.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_PURPLE));
         if(rem > 0){
            text.append("("+stacks+" Stacks + "+rem+")").withStyle(ChatFormatting.YELLOW);
         }else{
            text.append("("+stacks+" Stacks)").withStyle(ChatFormatting.YELLOW);
         }
      }
      return text;
   }
   
   @Override
   public void onClose(){
      if(permaCloseFlag) return;
      if(mode == TomeMode.RECIPE){ // Recipe gui to compendium
         tome.openGui(player,TomeMode.COMPENDIUM,settings);
      }else if(mode == TomeMode.ITEM || mode == TomeMode.RESEARCH){ // Item gui to compendium
         tome.openGui(player,TomeMode.COMPENDIUM,settings);
      }
   }
   
   public TomeMode getMode(){
      return mode;
   }
   
   public void setMode(TomeMode mode){
      this.mode = mode;
   }
   
   public static BookElementBuilder getGuideBook(){
      BookElementBuilder book = new BookElementBuilder();
      List<Component> pages = new ArrayList<>();
      
      pages.add(Component.literal("       Welcome to\n     Arcana Novum!\n\nArcana Novum is a server-sided fabric Magic mod that adds various power Arcana Items to the game. It also includes new game mechanics and multiblocks!").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Introduction\n\nYou are probably reading this in your Tome, which will be your guidebook for the entirety of the mod.\n\nThe first page of the tome is your profile.\nThe profile has 3 main sections to it.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Arcane Level\n\nYour level decides how many Arcana Items you can carry through Concentration\n\nYou gain XP by using and crafting items.\nCrafting an item for the first time gives additional XP.\nArcana Achievements also grant XP.   ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Concentration\n\nArcana Items each take a certain amount of focus to channel Arcana into. Each rarity tier of item takes a different amount of concentration to use.\nIf you go over your concentration limit, your mind will collapse and you will die.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Concentration\n\nItems take full concentration while in your inventory, but half concentration in your Ender Chest or Shulker Boxes.\n\nBlocks that are placed down take a quarter of the concentration when loaded in the world.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Skill Points\n\nYou get 3 skill points per Arcana level.\nYou also earn skill points by completing Arcana Achievements.\n\nYou can use these points to unlock Augments for items, which can be applied to enhance or change their abilities.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Item Rarities\n\nThere are 5 rarities:\nMundane, Empowered, Exotic, Sovereign, and Divine.\n\nAll Arcana Items above Mundane are immensely powerful, but some are more demanding to wield, which is reflected by their rarity.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Mundane Items\n\nConcentration: 0\n\nMundane Items only faintly emit Arcana and are mostly used in conjunction with other Arcana Items, such as being an ingredient in more powerful items, or as a fuel source.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("   Empowered Items\n\nConcentration: 1\n\nEmpowered Items are mostly utility items that offer conveniences in common situations.\n\nThey take a minimal toll to keep in your inventory so feel free to stock up on them!").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Exotic Items\n\nConcentration: 5\n\nExotic Items are more powerful items that can offer unique abilities not gained elsewhere, or provide a significant advantage in troubling situations. They are much more demanding to use.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Sovereign Items\n\nConcentration: 20\n\nSovereign Items are Arcanists' best attempts at recreating the power found in Divine Artifacts. However, unlike Divine Items, they lack the presence of Divine Arcana that makes wielding them trivial.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Sovereign Items\n\nAs a result, these items take an extraordinary amount of focus to wield.\n\nFortunately, they successfully replicate the incredible abilities of Divine Items in a form craftable by mere mortals.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Divine Items\n\nConcentration: 0\n\nDivine Items are made by godlike entities, such as the Aspect of Death. As previously mentioned, they use Divine Arcana, which uses the raw energy of the world itself to power them with no effort by the user.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Divine Items\n\nThere is no way to craft them in your forge, which means the only way of getting them is by interacting with these powerful entities.\nThis can be a very dangerous door to be knocking on, but the reward could be worth the risk...").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("    Item Compendium\n\nNow that you are caught up on the types of Arcana Items, you can use your Tome to look through all of the available items and how to use and craft them.\nThe Compendium is accessed by clicking the book in the Profile").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("  Researching Items\n\nBefore crafting an item you need to research it. This is done by completing various tasks, which can be in the form of obtaining an item, or  an Advancement.\n\nCompleting research requires Arcane Paper.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nIn order to craft Arcana Items you need a Starlight Forge. The recipe for which is viewable in the Compendium after researching it.\n\nThe Starlight Forge will require a structure beneath it which is shown by").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nright clicking the Forge.\n\nOnce completed, the Forge will allow you to craft better gear, and Arcana Items, by researching and following the recipes in your Tome.\n\nSome recipes may\n").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("     Forging Items\n\nrequire your Forge to be upgraded by crafting Forge Additions and placing them around your Forge.\n\nEach Forge Addon will unlock new recipes along with providing their own unique functionalities. ").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nAugments give your items enhanced capabilities or provide their own unique twist on their original purpose.\n\nEvery item has its own Augments you can unlock with Skill Points.\nHowever, there are not enough Skill Points").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nto unlock every augment so you much choose carefully.\n\nAugments follow the same rarity as items.\nRarity defines how many skill points they take to unlock and the type of catalyst needed to apply them.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nUnlocking an Augment does NOT immediately provide their benefits.\n\nAugments must be applied to an individual item by using an Augmentation Catalyst in the Twilight Anvil Forge Addition.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("      Augmentation\n\nItems that possess augments that you do not have unlocked will cost additional concentration to wield, so borrowing items from other players may cause you to use more concentration than expected.").withStyle(ChatFormatting.BLACK));
      pages.add(Component.literal("       Conclusion\n\nThose are the basics of the Arcana Novum mod!\n\nIf you have any questions, ideas, or find any bugs with the mod, please make an issue on the GitHub!\n\nEnjoy unlocking the secrets of Arcana!").withStyle(ChatFormatting.BLACK));
      
      pages.forEach(book::addPage);
      book.setAuthor("Arcana Novum");
      book.setTitle("Arcana Guide");
      
      return book;
   }
   
   public static List<CompendiumEntry> sortedFilteredEntryList(CompendiumSettings settings, ServerPlayer player){
      TomeFilter filterType = settings.getFilterType();
      TomeSort sortType = settings.getSortType();
      List<CompendiumEntry> items;
      if(filterType != null){
         items = new ArrayList<>();
         for(CompendiumEntry entry : RECOMMENDED_LIST){
            if(TomeFilter.matchesFilter(filterType, entry, player)){
               items.add(entry);
            }
         }
      }else{
         items = RECOMMENDED_LIST;
      }
      
      switch(sortType){
         case RECOMMENDED -> {
            items.sort(Comparator.comparingInt(RECOMMENDED_LIST::indexOf));
         }
         case NAME -> {
            Comparator<CompendiumEntry> nameComparator = Comparator.comparing(entry -> entry.getName().getString());
            items.sort(nameComparator);
         }
         case RARITY_DESC -> {
            Comparator<CompendiumEntry> rarityDescComparator = (CompendiumEntry i1, CompendiumEntry i2) -> {
               int rarityCompare = (i2.getRarityValue() - i1.getRarityValue());
               if(rarityCompare == 0){
                  return i1.getName().getString().compareTo(i2.getName().getString());
               }else{
                  return rarityCompare;
               }
            };
            items.sort(rarityDescComparator);
         }
         default -> {
            Comparator<CompendiumEntry> rarityAscComparator = (CompendiumEntry i1, CompendiumEntry i2) -> {
               int rarityCompare = (i1.getRarityValue() - i2.getRarityValue());
               if(rarityCompare == 0){
                  return i1.getName().getString().compareTo(i2.getName().getString());
               }else{
                  return rarityCompare;
               }
            };
            items.sort(rarityAscComparator);
         }
      }
      return items;
   }
   
   public static List<ArcanaAchievement> sortedFilteredAchievementList(ServerPlayer player, CompendiumSettings settings){
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
            achs.sort(Comparator.comparingInt(ach -> (RECOMMENDED_LIST.stream().map(entry -> entry instanceof ArcanaItemCompendiumEntry arcanaEntry ? arcanaEntry.getArcanaItem() : null).toList()).indexOf(ach.getArcanaItem())));
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
   
   public static List<UUID> sortedFilteredLeaderboardList(CompendiumSettings settings){
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
               GameProfile profile = BorisLib.SERVER.services().profileResolver().fetchById(playerID).orElse(null);
               return profile == null ? "" : profile.name();
            });
            filteredSortedPlayers.sort(nameComparator);
         }
      }
      return filteredSortedPlayers;
   }
   
   public static class CompendiumSettings{
      private TomeSort sortType;
      private TomeFilter filterType;
      private AchievementSort achSortType;
      private AchievementFilter achFilterType;
      private LeaderboardSort leaderSortType;
      private LeaderboardFilter leaderFilterType;
      private int page;
      private int achPage;
      private int leaderboardPage;
      private int researchPage;
      private boolean hideCompletedResearch;
      public final int skillLvl;
      public final int resourceLvl;
      
      public CompendiumSettings(int skillLvl, int resourceLvl){
         this.sortType = TomeSort.RECOMMENDED;
         this.filterType = TomeFilter.NONE;
         this.achSortType = AchievementSort.RECOMMENDED;
         this.achFilterType = AchievementFilter.NONE;
         this.leaderSortType = LeaderboardSort.XP_DESC;
         this.leaderFilterType = LeaderboardFilter.NONE;
         this.page = 1;
         this.achPage = 1;
         this.leaderboardPage = 1;
         this.researchPage = 1;
         this.hideCompletedResearch = false;
         this.skillLvl = skillLvl;
         this.resourceLvl = resourceLvl;
      }
      
      public TomeFilter getFilterType(){
         return filterType;
      }
   
      public TomeSort getSortType(){
         return sortType;
      }
   
      public AchievementSort getAchSortType(){
         return achSortType;
      }
   
      public AchievementFilter getAchFilterType(){
         return achFilterType;
      }
   
      public int getPage(){
         return page;
      }
   
      public int getAchPage(){
         return achPage;
      }
      
      public void setPage(int page){
         this.page = page;
      }
   
      public void setAchPage(int achPage){
         this.achPage = achPage;
      }
   
      public void setFilterType(TomeFilter filterType){
         this.filterType = filterType;
      }
   
      public void setFilterType(AchievementFilter filterType){
         this.achFilterType = filterType;
      }
   
      public void setSortType(TomeSort sortType){
         this.sortType = sortType;
      }
      
      public void setSortType(AchievementSort sortType){
         this.achSortType = sortType;
      }
      
      public int getLeaderboardPage(){
         return leaderboardPage;
      }
      
      public void setLeaderboardPage(int leaderboardPage){
         this.leaderboardPage = leaderboardPage;
      }
      
      public LeaderboardSort getLeaderSortType(){
         return leaderSortType;
      }
      
      public void setLeaderSortType(LeaderboardSort leaderSortType){
         this.leaderSortType = leaderSortType;
      }
      
      public LeaderboardFilter getLeaderFilterType(){
         return leaderFilterType;
      }
      
      public void setLeaderFilterType(LeaderboardFilter leaderFilterType){
         this.leaderFilterType = leaderFilterType;
      }
      
      public boolean hideCompletedResearch(){
         return hideCompletedResearch;
      }
      
      public void setHideCompletedResearch(boolean hideCompletedResearch){
         this.hideCompletedResearch = hideCompletedResearch;
      }
      
      public int getResearchPage(){
         return researchPage;
      }
      
      public void setResearchPage(int researchPage){
         this.researchPage = researchPage;
      }
   }
   
   public enum TomeMode{
      PROFILE,
      COMPENDIUM,
      ITEM,
      RECIPE,
      NONE,
      ACHIEVEMENTS,
      LEADERBOARD,
      RESEARCH
   }
   
   // TODO: Refactor filters and sorts
   public enum TomeFilter{
      NONE("None"),
      RESEARCHED("Researched"),
      NOT_RESEARCHED("Not Researched"),
      MUNDANE("Mundane"),
      EMPOWERED("Empowered"),
      EXOTIC("Exotic"),
      SOVEREIGN("Sovereign"),
      DIVINE("Divine"),
      ITEMS("Items"),
      BLOCKS("Blocks"),
      FORGE("Forge"),
      ARROWS("Arrows"),
      ALTARS("Altars"),
      EQUIPMENT("Equipment"),
      CHARMS("Charms"),
      CATALYSTS("Catalysts"),
      INGREDIENT("Ingredient");
      
      public final String label;
      
      TomeFilter(String label){
         this.label = label;
      }
      
      public static Component getColoredLabel(TomeFilter filter){
         MutableComponent text = Component.literal(filter.label);
         
         return switch(filter){ // Only Black left for future usage (before repeats)
            case NONE -> text.withStyle(ChatFormatting.WHITE);
            case RESEARCHED -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case NOT_RESEARCHED -> text.withStyle(ChatFormatting.DARK_GRAY);
            case MUNDANE -> text.withStyle(ChatFormatting.GRAY);
            case EMPOWERED -> text.withStyle(ChatFormatting.GREEN);
            case EXOTIC -> text.withStyle(ChatFormatting.AQUA);
            case SOVEREIGN -> text.withStyle(ChatFormatting.GOLD);
            case DIVINE -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case ITEMS -> text.withStyle(ChatFormatting.DARK_AQUA);
            case BLOCKS -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case FORGE -> text.withStyle(ChatFormatting.DARK_GREEN);
            case ARROWS -> text.withStyle(ChatFormatting.RED);
            case ALTARS -> text.withStyle(ChatFormatting.BLUE);
            case EQUIPMENT -> text.withStyle(ChatFormatting.DARK_RED);
            case CHARMS -> text.withStyle(ChatFormatting.YELLOW);
            case CATALYSTS -> text.withStyle(ChatFormatting.DARK_BLUE);
            case INGREDIENT -> text.withStyle(ChatFormatting.DARK_GRAY);
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
      
      public static boolean matchesFilter(TomeFilter filter, CompendiumEntry entry, ServerPlayer player){
         if(filter == TomeFilter.NONE) return true;
         if(filter == RESEARCHED || filter == NOT_RESEARCHED){
            if(entry instanceof ArcanaItemCompendiumEntry arcanaEntry){
               if(filter == RESEARCHED){
                  return ArcanaNovum.data(player).hasResearched(arcanaEntry.getArcanaItem());
               }else{
                  return !ArcanaNovum.data(player).hasResearched(arcanaEntry.getArcanaItem());
               }
            }
            return false;
         }
         TomeFilter[] cats = entry.getCategories();
         if(cats == null){
            log(2,"No categories found for: "+entry.getName());
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
      
      public static Component getColoredLabel(TomeSort sort){
         MutableComponent text = Component.literal(sort.label);
         
         return switch(sort){
            case RARITY_ASC -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case RARITY_DESC -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case NAME -> text.withStyle(ChatFormatting.GREEN);
            case RECOMMENDED -> text.withStyle(ChatFormatting.YELLOW);
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
      
      public static Component getColoredLabel(AchievementSort sort){
         MutableComponent text = Component.literal(sort.label);
         
         return switch(sort){
            case RARITY_ASC -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case RARITY_DESC -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case NAME -> text.withStyle(ChatFormatting.GREEN);
            case RECOMMENDED -> text.withStyle(ChatFormatting.YELLOW);
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
      
      public static Component getColoredLabel(AchievementFilter filter){
         MutableComponent text = Component.literal(filter.label);
         
         return switch(filter){
            case NONE -> text.withStyle(ChatFormatting.WHITE);
            case ACQUIRED -> text.withStyle(ChatFormatting.AQUA);
            case NOT_ACQUIRED -> text.withStyle(ChatFormatting.RED);
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
      
      public static boolean matchesFilter(ServerPlayer player, AchievementFilter filter, ArcanaAchievement ach){
         if(filter == AchievementFilter.NONE) return true;
         IArcanaProfileComponent profile = ArcanaNovum.data(player);
         boolean acquired = profile.hasAcheivement(ach.getArcanaItem().getId(),ach.id);
         
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
      
      public static Component getColoredLabel(LeaderboardSort sort){
         MutableComponent text = Component.literal(sort.label);
         
         return switch(sort){
            case XP_DESC -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case XP_ASC -> text.withStyle(ChatFormatting.DARK_PURPLE);
            case ACHIEVES_DESC -> text.withStyle(ChatFormatting.GREEN);
            case SKILL_POINTS_DESC -> text.withStyle(ChatFormatting.DARK_GREEN);
            case NAME -> text.withStyle(ChatFormatting.AQUA);
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
      
      public static Component getColoredLabel(LeaderboardFilter filter){
         MutableComponent text = Component.literal(filter.label);
         
         return switch(filter){
            case NONE -> text.withStyle(ChatFormatting.WHITE);
            case ARCANIST -> text.withStyle(ChatFormatting.LIGHT_PURPLE);
            case MAX_LVL -> text.withStyle(ChatFormatting.GREEN);
            case ABYSS -> text.withStyle(ChatFormatting.DARK_PURPLE);
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
}
