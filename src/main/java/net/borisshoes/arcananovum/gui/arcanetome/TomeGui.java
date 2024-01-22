package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TomeGui extends SimpleGui {
   private ArcaneTome.TomeMode mode;
   private ArcaneTome tome;
   private CompendiumSettings settings;
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    * @param mode                        mode of screen
    */
   public TomeGui(ScreenHandlerType<?> type, ServerPlayerEntity player, ArcaneTome.TomeMode mode, ArcaneTome tome, CompendiumSettings settings){
      super(type, player, false);
      this.mode = mode;
      this.tome = tome;
      this.settings = settings;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(mode == ArcaneTome.TomeMode.PROFILE){
         if(index == 49){
            tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
         }else if(index == 4){
            // Guide gui
            ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
            writablebook.setNbt(getGuideBook());
            BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
            LoreGui loreGui = new LoreGui(player,bookBuilder,tome,ArcaneTome.TomeMode.PROFILE,settings);
            loreGui.open();
         }else if(index == 19){
            // Achievements View
            tome.openGui(player, ArcaneTome.TomeMode.ACHIEVEMENTS,settings);
         }
      }else{
         boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
         if(mode == ArcaneTome.TomeMode.COMPENDIUM){
            if(index == 4){
               tome.buildProfileGui(this,player);
            }else if(indexInCenter){
               ItemStack item = this.getSlot(index).getItemStack();
               if(!item.isEmpty()){
                  MagicItem magicItem = MagicItemUtils.identifyItem(item);
                  if(type == ClickType.MOUSE_RIGHT){
                     if(magicItem.getRarity() == MagicRarity.MYTHICAL){
                        if(magicItem.getRecipe() != null){
                           tome.openRecipeGui(player,settings, magicItem.getId());
                        }else{
                           player.sendMessage(Text.literal("You Cannot Craft Mythical Items").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
                        }
                     }else{
                        if(magicItem.getRecipe() != null){
                           tome.openRecipeGui(player,settings, magicItem.getId());
                        }else{
                           player.sendMessage(Text.literal("You Cannot Craft This Item").formatted(Formatting.RED),false);
                        }
                     }
                  }else{
                     tome.openItemGui(player,settings, magicItem.getId());
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
               
               tome.buildCompendiumGui(this,player,settings);
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
               tome.buildCompendiumGui(this,player,settings);
            }else if(index == 45){
               if(settings.getPage() > 1){
                  settings.setPage(settings.getPage()-1);
                  tome.buildCompendiumGui(this,player,settings);
               }
            }else if(index == 53){
               List<MagicItem> items = ArcaneTome.sortedFilteredItemList(settings);
               int numPages = (int) Math.ceil((float)items.size()/28.0);
               if(settings.getPage() < numPages){
                  settings.setPage(settings.getPage()+1);
                  tome.buildCompendiumGui(this,player,settings);
               }
            }
         }else if(mode == ArcaneTome.TomeMode.ACHIEVEMENTS){
            if(index == 4){
               if(type == ClickType.MOUSE_RIGHT){
                  tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
               }else{
                  tome.buildProfileGui(this,player);
               }
            }else if(indexInCenter){
               ItemStack item = this.getSlot(index).getItemStack();
               if(!item.isEmpty()){
                  tome.openItemGui(player,settings, item.getNbt().getString("magicItemId"));
               }
            }else if(index == 0){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean middle = type == ClickType.MOUSE_MIDDLE;
               if(middle){
                  settings.setSortType(ArcaneTome.AchievementSort.RECOMMENDED);
               }else{
                  settings.setSortType(ArcaneTome.AchievementSort.cycleSort(settings.getAchSortType(),backwards));
               }
         
               tome.buildAchievementsGui(this,player,settings);
            }else if(index == 8){
               boolean backwards = type == ClickType.MOUSE_RIGHT;
               boolean middle = type == ClickType.MOUSE_MIDDLE;
               if(middle){
                  settings.setFilterType(ArcaneTome.AchievementFilter.NONE);
               }else{
                  settings.setFilterType(ArcaneTome.AchievementFilter.cycleFilter(settings.getAchFilterType(),backwards));
               }
         
               List<ArcanaAchievement> achs = ArcaneTome.sortedFilteredAchievementList(player,settings);
               int numPages = (int) Math.ceil((float)achs.size()/28.0);
               if(settings.getAchPage() > numPages){
                  settings.setAchPage(numPages);
               }
               tome.buildAchievementsGui(this,player,settings);
            }else if(index == 45){
               if(settings.getAchPage() > 1){
                  settings.setAchPage(settings.getAchPage()-1);
                  tome.buildAchievementsGui(this,player,settings);
               }
            }else if(index == 53){
               List<ArcanaAchievement> achs = ArcaneTome.sortedFilteredAchievementList(player,settings);
               int numPages = (int) Math.ceil((float)achs.size()/28.0);
               if(settings.getAchPage() < numPages){
                  settings.setAchPage(settings.getAchPage()+1);
                  tome.buildAchievementsGui(this,player,settings);
               }
            }
         }else if(mode == ArcaneTome.TomeMode.RECIPE){
            ItemStack item = this.getSlot(25).getItemStack();
            MagicItem magicItem = MagicItemUtils.identifyItem(item);
            if(index == 7){
               NbtCompound loreData = magicItem.getBookLore();
               if(loreData != null){
                  ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
                  writablebook.setNbt(loreData);
                  BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
                  LoreGui loreGui = new LoreGui(player,bookBuilder,tome, ArcaneTome.TomeMode.RECIPE,settings, magicItem.getId());
                  loreGui.open();
               }else{
                  player.sendMessage(Text.literal("No Lore Found For That Item").formatted(Formatting.RED),false);
               }
            }else if(index == 25 || index == 26){
               tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
            }else if(index > 9 && index < 36 && (index % 9 == 1 || index % 9 == 2 || index % 9 == 3 || index % 9 == 4 ||index % 9 == 5)){
               ItemStack ingredStack = this.getSlot(index).getItemStack();
               MagicItem magicItem1 = MagicItemUtils.identifyItem(ingredStack);
               if(magicItem1 != null){
                  tome.openRecipeGui(player,settings, magicItem1.getId());
               }
            }
         }else if(mode == ArcaneTome.TomeMode.ITEM){
            ItemStack item = this.getSlot(4).getItemStack();
            MagicItem magicItem = MagicItemUtils.identifyItem(item);
            
            if(index == 2){
               if(magicItem.getRarity() == MagicRarity.MYTHICAL){
                  if(magicItem.getRecipe() != null){
                     tome.openRecipeGui(player,settings, magicItem.getId());
                  }else{
                     player.sendMessage(Text.literal("You Cannot Craft Mythical Items").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC), false);
                  }
               }else{
                  if(magicItem.getRecipe() != null){
                     tome.openRecipeGui(player,settings, magicItem.getId());
                  }else{
                     player.sendMessage(Text.literal("You Cannot Craft This Item").formatted(Formatting.RED),false);
                  }
               }
            }
            if(index == 4){
               tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
            }
            if(index == 6){
               NbtCompound loreData = magicItem.getBookLore();
               if(loreData != null){
                  ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
                  writablebook.setNbt(loreData);
                  BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
                  LoreGui loreGui = new LoreGui(player,bookBuilder,tome, ArcaneTome.TomeMode.ITEM,settings,magicItem.getId());
                  loreGui.open();
               }else{
                  player.sendMessage(Text.literal("No Lore Found For That Item").formatted(Formatting.RED),false);
               }
            }
            if(index >= 28 && index <= 35){ // Unlock augment
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
               int[] augmentSlots = dynamicSlots[augments.size()];
               ArcanaAugment augment = null;
               for(int i = 0; i < augmentSlots.length; i++){
                  if(index == 28+augmentSlots[i]){
                     augment = augments.get(i);
                     break;
                  }
               }
               
               if(augment != null){
                  IArcanaProfileComponent profile = PLAYER_DATA.get(player);
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  MagicRarity[] tiers = augment.getTiers();
                  if(augmentLvl >= tiers.length) return true;
                  int cost = tiers[augmentLvl].rarity+1;
                  int unallocated = profile.getTotalSkillPoints() - profile.getSpentSkillPoints();
                  if(cost <= unallocated){
                     profile.setAugmentLevel(augment.id,augmentLvl+1);
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, (.5f+((float)(augmentLvl+1)/(tiers.length-1))));
                     tome.openItemGui(player,settings, magicItem.getId());
                  }else{
                     player.sendMessage(Text.literal("Not Enough Skill Points").formatted(Formatting.RED),false);
                  }
               }
            }
         }
      }
      return true;
   }
   
   @Override
   public void onClose(){
      if(mode == ArcaneTome.TomeMode.RECIPE){ // Recipe gui to compendium
         ItemStack item = this.getSlot(25).getItemStack();
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
         //tome.openGui(player,ArcaneTome.TomeMode.ITEM,settings,magicItem.getId());
      }else if(mode == ArcaneTome.TomeMode.ITEM){ // Item gui to compendium
         tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
      }
   }
   
   public ArcaneTome.TomeMode getMode(){
      return mode;
   }
   
   public void setMode(ArcaneTome.TomeMode mode){
      this.mode = mode;
   }
   
   public static NbtCompound getGuideBook(){
      NbtCompound bookLore = new NbtCompound();
      NbtList loreList = new NbtList();
      List<String> list = new ArrayList<>();
      list.add("{\"text\":\"       Welcome To             Arcana Novum!\\n\\nArcana Novum is a server mod that adds Magic Items that try to stay within the 'feel' of Vanilla Minecraft.\\n\\nYou are probably accessing this guide through your Tome of Arcana Novum.\"}");
      list.add("{\"text\":\"This Tome is your guide book for the entirety of the mod and will help you discover all of the cool Magic Items you can craft.\\n\\nThe first thing you see when you open the Tome is your profile. Your profile has 3 main sections.\"}");
      list.add("{\"text\":\"     Arcane Level\\n \\nYour level decides how many Magic Items you can carry through Concentration\\nYou gain XP by using and crafting Magic Items. Crafting an item for the first time gives extra XP.\\nAchievements also grant experience.\\n\\n\"}");
      list.add("{\"text\":\"     Concentration\\n\\nMagic Items contain powerful Arcana that takes focus to use.\\nEach tier of item takes a certain amount of concentration to hold in your inventory. If you go over your concentration limit your mind starts to collapse and you will die.\"}");
      list.add("{\"text\":\"       Skill Points\\n\\nYou get 3 skill points per Arcana level.\\nThese points can be used to unlock Augments for items.\\n\\nYou also get skill points by completing achievements with different items.\"}");
      list.add("{\"text\":\"      Item Rarities\\n\\nThere are 5 rarities:\\nMundane, Empowered, Exotic, Legendary and Mythical.\\n\\nAll Magic Items are immensely powerful, but some are more demanding to wield and craft which is reflected by their rarity.\"}");
      list.add("{\"text\":\"     Mundane Items\\n\\nCrafting XP: 1st/2nd+ \\n      5000 / 1000 \\nConcentration: 0\\n\\nMundane Items only faintly eminate Arcana and are mostly used in conjunction with other Magic Items as fuel or ingredients.\"}");
      list.add("{\"text\":\"   Empowered Items\\n\\nCrafting XP: 1st/2nd+ \\n     10000 / 5000\\nConcentration: 1\\n\\nEmpowered Items usually are utility items that offer convienience in common situations. They take a minimal mental toll.\"}");
      list.add("{\"text\":\"      Exotic Items\\n\\nCrafting XP: 1st/2nd+ \\n     50000 / 10000\\nConcentration: 5\\n\\nExotic Items are where things get interesting. Their abilities are more powerful and are more expensive to craft and use.\"}");
      list.add("{\"text\":\"   Legendary Items\\n\\nCrafting XP: 1st/2nd+ \\n    100000 / 50000\\nConcentration: 20\\n\\nLegendary Items are Arcanists' best attempts at recreating the power of Mythical Artifacts. However unlike Mythical Items, they lack the divine construction that\"}");
      list.add("{\"text\":\"   Legendary Items\\n\\nharmlessly channels Arcana through the user, and as a result take an extraordinary amount of focus to wield.\\nWhere the Arcanists succeeded was in replicating the incredible abilities of Mythical Items in a craftable form.\"}");
      list.add("{\"text\":\"     Mythical Items\\n\\nCrafting XP: - / -\\nConcentration: 0\\n\\nMythical Items are items that have divine origins and tap into the raw Arcana of the world itself and allow a user to wield it with no effort. Mythical Items become an extension of the user.\"}");
      list.add("{\"text\":\"     Mythical Items\\n\\nMythical Items are unable to be crafted by normal means.\\n\\nThey can only be obtained by interacting with divine entities, which is a very dangerous door to go knocking on, but the reward could be worth it...\"}");
      list.add("{\"text\":\"    Item Compendium\\n\\nNow that you are caught up on the types of Magic Items, you can use your Tome to look through all of the available items and how to use and craft them.\\nThe Compendium is accessed by clicking the book in the Profile section of the Tome.\"}");
      list.add("{\"text\":\"     Forging Items\\n\\nIn order to craft Magic Items you need a Starlight Forge. \\nA Starlight Forge is made by placing an Enchanted Golden Apple along with your Tome of Arcana Novum upon a Smithing Table during the height of a New Moon.\"}");
      list.add("{\"text\":\"     Forging Items\\n\\nThe Starlight Forge will require a structure beneath it, which is shown by right clicking the forge.\\nOnce completed, the Forge will allow you to craft better gear, and Magic Items by following the recipes in your Tome.\"}");
      list.add("{\"text\":\"     Forging Items\\n\\nSome recipes will require your Forge to be upgraded by making Magic Items that add to the Forge.\\n\\nPlacing them near your Forge will allow you to make new Magic Items, along with providing their own unique abilities.\"}");
      list.add("{\"text\":\"     Forging Items\\n\\nSome crafting ingredients require more than just the item. For example an item might require enchantments or a Soulstone with a certain amount of souls inside. Make sure you check all requirements in the Recipe Display.\"}");
      list.add("{\"text\":\"      Augmentation\\n\\nAugments give your items enhanced capabilities or provide a unique twist on their original purpose.\\n\\nEvery Item has Augments you can unlock with Skill Points.\\nHowever, there are not enough Points to unlock every Augment.\\n\"}");
      list.add("{\"text\":\"      Augmentation\\n\\nAugments follow the same rarity structure as items.\\nRarity defines how many skill points they take to unlock, and the type of catalyst they need to apply.\\nIndividual augments can have multiple tiers of varying rarity.\"}");
      list.add("{\"text\":\"      Augmentation\\n\\nUnlocking an Augment does NOT immediately provide their benefits.\\n\\nAugments must be applied to individual items using an Augmentation Catalyst in the Twilight Anvil Forge Addition.\"}");
      list.add("{\"text\":\"      Augmentation\\n\\nItems that hold Augments that you do not possess pose an additional danger to the user.\\n\\nAugments require additional concentration if you do not have the augment unlocked.\"}");
      list.add("{\"text\":\"       Conclusion\\n\\nThats about it for the basics of the Arcana Novum mod!\\n \\nIf you have any questions you can always ask them on the server discord!\\n\\nEnjoy discovering and unleashing your Arcana Novum!\"}");
      
      for(String s : list){
         loreList.add(NbtString.of(s));
      }
      
      bookLore.put("pages",loreList);
      bookLore.putString("author","Arcana Novum");
      bookLore.putString("filtered_title","arcana_guide");
      bookLore.putString("title","arcana_guide");
      
      return bookLore;
   }
   
   public static class CompendiumSettings{
      private ArcaneTome.TomeSort sortType;
      private ArcaneTome.TomeFilter filterType;
      private ArcaneTome.AchievementSort achSortType;
      private ArcaneTome.AchievementFilter achFilterType;
      private int page;
      private int achPage;
      public final int skillLvl;
      public final int resourceLvl;
      
      public CompendiumSettings(int skillLvl, int resourceLvl){
         this.sortType = ArcaneTome.TomeSort.RECOMMENDED;
         this.filterType = ArcaneTome.TomeFilter.NONE;
         this.achSortType = ArcaneTome.AchievementSort.RECOMMENDED;
         this.achFilterType = ArcaneTome.AchievementFilter.NONE;
         this.page = 1;
         this.achPage = 1;
         this.skillLvl = skillLvl;
         this.resourceLvl = resourceLvl;
      }
   
      public CompendiumSettings(ArcaneTome.TomeSort sortType, ArcaneTome.TomeFilter filterType, ArcaneTome.AchievementSort achSortType, ArcaneTome.AchievementFilter achFilterType, int page, int achPage, int skillLvl, int resourceLvl){
         this.sortType = sortType;
         this.filterType = filterType;
         this.achSortType = achSortType;
         this.achFilterType = achFilterType;
         this.page = page;
         this.achPage = achPage;
         this.skillLvl = skillLvl;
         this.resourceLvl = resourceLvl;
      }
   
      public ArcaneTome.TomeFilter getFilterType(){
         return filterType;
      }
   
      public ArcaneTome.TomeSort getSortType(){
         return sortType;
      }
   
      public ArcaneTome.AchievementSort getAchSortType(){
         return achSortType;
      }
   
      public ArcaneTome.AchievementFilter getAchFilterType(){
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
   
      public void setFilterType(ArcaneTome.TomeFilter filterType){
         this.filterType = filterType;
      }
   
      public void setFilterType(ArcaneTome.AchievementFilter filterType){
         this.achFilterType = filterType;
      }
   
      public void setSortType(ArcaneTome.TomeSort sortType){
         this.sortType = sortType;
      }
      
      public void setSortType(ArcaneTome.AchievementSort sortType){
         this.achSortType = sortType;
      }
   }
}
