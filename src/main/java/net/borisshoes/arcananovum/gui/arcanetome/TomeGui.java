package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.recipes.ExplainRecipe;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
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
         }
      }else if(mode == ArcaneTome.TomeMode.COMPENDIUM){
         if(index == 4){
            tome.buildProfileGui(this,player);
         }else if(index == 49){
            tome.openGui(player,ArcaneTome.TomeMode.TINKER,settings);
         }else if(index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8){
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
      }else if(mode == ArcaneTome.TomeMode.CRAFTING){
         if(index == 7){
            //Give Items back
            Inventory inv = getSlotRedirect(1).inventory;
            returnItems(inv);
            tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
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
                  double skillChance = new double[]{0, .25, .5, .75, 1, 2}[settings.skillLvl];
                  List<ArcanaAugment> allAugs = ArcanaAugments.getAugmentsForItem(magicItem);
                  allAugs.removeIf(aug -> profile.getAugmentLevel(aug.id) <= 0);
                  if(Math.random() < skillChance && allAugs.size() > 0){
                     int ind = (int) (Math.random() * allAugs.size());
                     ArcanaAugment aug = allAugs.get(ind);
                     ArcanaAugments.applyAugment(newMagicItem, aug.id, Math.min(settings.skillLvl,(int)(Math.random()*profile.getAugmentLevel(aug.id)+1)));
                     if(settings.skillLvl == 5 && allAugs.size() > 1){
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
               
               
               if(!PLAYER_DATA.get(player).addCrafted(magicItem.getId()) && !(magicItem instanceof ArcaneTome)){
                  PLAYER_DATA.get(player).addXP(MagicRarity.getCraftXp(magicItem.getRarity()));
               }
   
               if(magicItem.getRarity() != MagicRarity.MUNDANE){
                  ArcanaAchievements.grant(player,"intro_arcana");
                  ArcanaAchievements.progress(player,"intermediate_artifice",1);
               }
               if(magicItem.getRarity() == MagicRarity.LEGENDARY) ArcanaAchievements.grant(player,"artificial_divinity");
   
               int resourceLvl = profile.getAugmentLevel("resourceful");
               
               ItemStack[][] ingredients = new ItemStack[5][5];
               for(int i = 0; i < inv.size(); i++){
                  ingredients[i/5][i%5] = inv.getStack(i);
               }
               ItemStack[][] remainders = recipe.getRemainders(ingredients,resourceLvl);
               for(int i = 0; i < inv.size(); i++){
                  inv.setStack(i,remainders[i/5][i%5]);
               }
   
               

               while(true){
                  ItemEntity itemEntity;
                  boolean bl = player.getInventory().insertStack(newMagicItem);
                  if (!bl || !newMagicItem.isEmpty()) {
                     itemEntity = player.dropItem(newMagicItem, false);
                     if (itemEntity == null) break;
                     itemEntity.resetPickupDelay();
                     itemEntity.setOwner(player.getUuid());
                     break;
                  }
                  newMagicItem.setCount(1);
                  itemEntity = player.dropItem(newMagicItem, false);
                  if (itemEntity != null) {
                     itemEntity.setDespawnImmediately();
                  }
                  break;
               }
            }
         }else if(index == 43){
            //Give Items back
            Inventory inv = getSlotRedirect(1).inventory;
            returnItems(inv);
            tome.openGui(player, ArcaneTome.TomeMode.PROFILE,settings);
         }
      }else if(mode == ArcaneTome.TomeMode.RECIPE){
         ItemStack item = this.getSlot(25).getItemStack();
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         if(index == 7){
            tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
         }else if(index == 25){
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
         }else if(index == 43){
            if(!(magicItem.getRecipe() instanceof ExplainRecipe)) tome.openGui(player, ArcaneTome.TomeMode.CRAFTING,settings,magicItem.getId());
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
      }else if(mode == ArcaneTome.TomeMode.TINKER){
         Inventory inv = getSlotRedirect(4).inventory;
         ItemStack item = inv.getStack(0);
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         
         if(index == 10){
            returnItems(inv);
            tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
         }else if(index == 16){
            if(magicItem != null){
               returnItems(inv);
               tome.openGui(player, ArcaneTome.TomeMode.ITEM,settings,magicItem.getId());
            }else{
               player.sendMessage(Text.literal("Insert an Item to Tinker").formatted(Formatting.RED),false);
            }
         }else if(index ==  22){
            if(magicItem != null){
               RenameGui renameGui = new RenameGui(player,tome,settings,item);
               renameGui.setTitle(Text.literal("Rename Magic Item"));
               renameGui.setSlot(0, GuiElementBuilder.from(item));
               renameGui.setSlot(2, GuiElementBuilder.from(item));
               renameGui.open();
            }else{
               player.sendMessage(Text.literal("Insert an Item to Tinker").formatted(Formatting.RED),false);
            }
         }else if(index >= 37 && index <= 44){
            if(magicItem != null){
               List<ArcanaAugment> augments = ArcanaAugments.getAugmentsForItem(magicItem);
               int[] augmentSlots = dynamicSlots[augments.size()];
               ArcanaAugment augment = null;
               for(int i = 0; i < augmentSlots.length; i++){
                  if(index == 37 + augmentSlots[i]){
                     augment = augments.get(i);
                     break;
                  }
               }
               
               if(augment != null){
                  IArcanaProfileComponent profile = PLAYER_DATA.get(player);
                  int augmentLvl = profile.getAugmentLevel(augment.id);
                  MagicRarity[] tiers = augment.getTiers();
                  int curItemLevel = ArcanaAugments.getAugmentOnItem(item, augment.id);
                  if(curItemLevel == -2){
                     Arcananovum.log(3, "Magic item errored in Tinker's Screen: " + magicItem.getId());
                  }else if(curItemLevel == -1) curItemLevel = 0;
   
                  boolean generic = magicItem.getId().equals(MagicItems.ARCANE_TOME.getId()) && !augment.id.equals(ArcanaAugments.SKILL.id);
                  
                  if(generic){
                     player.sendMessage(Text.literal("These augments are active by default").formatted(Formatting.AQUA), false);
                  }else if(curItemLevel >= tiers.length){ // Item Level = max: End Crystal
                     player.sendMessage(Text.literal("You have already maxed this augment").formatted(Formatting.AQUA), false);
                  }else if(augmentLvl == 0 && curItemLevel == 0){ // Item & player lvl = 0: Obsidian
                     player.sendMessage(Text.literal("You must unlock this augment first").formatted(Formatting.RED), false);
                  }else if(curItemLevel >= augmentLvl){ // Item level != max & >= player level: Obsidian
                     player.sendMessage(Text.literal("You must unlock higher levels to augment further").formatted(Formatting.RED), false);
                  }else if(ArcanaAugments.isIncompatible(item, augment.id)){ // Incompatible augment: Structure Void
                     player.sendMessage(Text.literal("This augment is incompatible with existing augments").formatted(Formatting.RED), false);
                  }else{ // Item level = 0 | (Item level != max & < player level): Augment Catalyst
                     if(attemptAugment(item, augment, curItemLevel + 1)){
                        //tome.openGui(player, ArcaneTome.TomeMode.TINKER, settings);
                        SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1, (.5f+((float)(curItemLevel+1)/(tiers.length-1))));
                        inv.setStack(0,item);
                     }
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
      }else if(mode == ArcaneTome.TomeMode.CRAFTING){ // Crafting gui give items back
         //Give Items back
         Inventory inv = getSlotRedirect(1).inventory;
         returnItems(inv);
         tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
      }else if(mode == ArcaneTome.TomeMode.ITEM){ // Item gui to compendium
         tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
      }else if(mode == ArcaneTome.TomeMode.TINKER){ // Give tinker items back
         //Give Items back
         Inventory inv = getSlotRedirect(4).inventory;
         returnItems(inv);
         tome.openGui(player,ArcaneTome.TomeMode.COMPENDIUM,settings);
      }
   }
   
   private void returnItems(Inventory inv){
      for(int i=0; i<inv.size();i++){
         ItemStack stack = inv.getStack(i);
         if(!stack.isEmpty()){
         
            ItemEntity itemEntity;
            boolean bl = player.getInventory().insertStack(stack);
            if (!bl || !stack.isEmpty()) {
               itemEntity = player.dropItem(stack, false);
               if (itemEntity == null) continue;
               itemEntity.resetPickupDelay();
               itemEntity.setOwner(player.getUuid());
               continue;
            }
            stack.setCount(1);
            itemEntity = player.dropItem(stack, false);
            if (itemEntity != null) {
               itemEntity.setDespawnImmediately();
            }
         }
      }
   }
   
   private boolean attemptAugment(ItemStack item, ArcanaAugment augment, int level){
      PlayerInventory playerInv = player.getInventory();
      MagicRarity tier = augment.getTiers()[level-1];
      
      int catalystSlot = -1;
      boolean creative = player.isCreative();
      for(int i=0; i<playerInv.size(); i++){
         ItemStack cata = playerInv.getStack(i);
         MagicItem magicItem = MagicItemUtils.identifyItem(cata);
         if(magicItem != null && magicItem.getId().equals(MagicRarity.getAugmentCatalyst(tier).getId())){
            //Found catalyst
            catalystSlot = i;
            break;
         }
      }
      if(catalystSlot == -1 && !creative){
         player.sendMessage(Text.literal("No Augment Catalyst Found").formatted(Formatting.RED),false);
      }else{
         if(ArcanaAugments.applyAugment(item,augment.id,level)){
            if(!creative) playerInv.removeStack(catalystSlot);
            return true;
         }else{
            Arcananovum.log(3,"Error applying augment "+augment.id+" to "+MagicItemUtils.identifyItem(item).getId());
         }
      }
      
      
      return false;
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
      loreList.add(NbtString.of("{\"text\":\"       Welcome To             Arcana Novum!\\n\\nArcana Novum is a server mod that adds Magic Items that try to stay within the 'feel' of Vanilla Minecraft.\\n\\nYou are probably accessing this guide through your Tome of Arcana Novum.\"}"));
      loreList.add(NbtString.of("{\"text\":\"This Tome is your guide book for the entirety of the mod and will help you discover all of the cool Magic Items you can craft.\\n\\nThe first thing you see when you open the Tome is your profile. Your profile has 2 main components.\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Arcane Level\\n \\nYour level decides how many Magic Items you can carry through Concentration\\n\\nYou gain XP by using and crafting Magic Items. Crafting an item for the first time gives extra XP\\n\\n\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Concentration\\n\\nMagic Items contain powerful Arcana that takes focus to use.\\nEach tier of item takes a certain amount of concentration to hold in your inventory. If you go over your concentration limit your mind starts to collapse and you will die.\"}"));
      loreList.add(NbtString.of("{\"text\":\"      Item Rarities\\n\\nThere are 5 main rarities:\\n\\nMundane, Empowered, Exotic, Legendary and Mythical.\\n\\nEach tier gives a more powerful ability than the last.\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Mundane Items\\n\\nCrafting XP: 1000 / 100 (First / Normal)\\nConcentration: 0\\n\\nMundane Items only faintly eminate Arcana and are mostly used in conjunction with other Magic Items\"}"));
      loreList.add(NbtString.of("{\"text\":\"   Empowered Items\\n\\nCrafting XP: 5000 / 1000\\nConcentration: 1\\n\\nEmpowered Items usually give passive effects that would be considered 'nice to have', nothing crazy strong, and don't take a heavy toll on your mind.\"}"));
      loreList.add(NbtString.of("{\"text\":\"      Exotic Items\\n\\nCrafting XP: 10000 / 5000\\nConcentration: 5\\n\\nExotic Items are where things get interesting. Their abilities are more powerful and are more expensive to craft and use.\"}"));
      loreList.add(NbtString.of("{\"text\":\"   Legendary Items\\n\\nCrafting XP: 25000 / 15000\\nConcentration: 20\\n\\nLegendary Items are Arcanists' best attempts at recreating the power of Mythical Artifacts. However unlike Mythical Items, they lack the elegant design that harmlessly\"}"));
      loreList.add(NbtString.of("{\"text\":\"   Legendary Items\\n\\nchannels Arcana through the user, and as a result take an extraordinary amount of focus to use.\\nWhere the Arcanists succeeded was in replicating the incredible abilities of Mythical Items in a craftable form.\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Mythical Items\\n\\nCrafting XP: - / -\\nConcentration: 0\\n\\nMythical Items are items designed by the Gods to tap into the raw Arcana of the world itself and allow it to be wielded with no effort as if they are  an extension of the user's body. \\n\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Mythical Items\\n\\nMythical Items are unable to be crafted and are in short supply.\\n\\nOnly a few Mythical Items have been discovered and their power compared to all but Legendary Items is on a whole other level.\"}"));
      loreList.add(NbtString.of("{\"text\":\"    Item Compendium\\n\\nNow that you are caught up on the types of Magic Items, you can use your Tome to look through all of the available items and how to use and craft them.\\nThe Compendium is accessed by clicking the book in the Profile section of the Tome.\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Forging Items\\n\\nYou are able to view the recipes of Magic Items in the Compendium menu.\\n \\nYou can forge them by clicking the crafting table in the Compendium menu to access the Forging menu.\\n\"}"));
      loreList.add(NbtString.of("{\"text\":\"     Forging Items\\n\\nSome crafting ingredients require more than just the item. For example an item might require enchantments or a Soulstone with a certain amount of souls inside. Make sure you check all requirements in the Recipe Display.\"}"));
      loreList.add(NbtString.of("{\"text\":\"       Conclusion\\n\\nThats about it for the basics of the Arcana Novum mod!\\n \\nIf you have any questions you can always ask them on the server discord!\\n\\nEnjoy discovering and unleashing your Arcana Novum!\"}"));
      bookLore.put("pages",loreList);
      bookLore.putString("author","Arcana Novum");
      bookLore.putString("filtered_title","arcana_guide");
      bookLore.putString("title","arcana_guide");
      
      return bookLore;
   }
   
   public static class CompendiumSettings{
      private ArcaneTome.TomeSort sortType;
      private ArcaneTome.TomeFilter filterType;
      private int page;
      public final int skillLvl;
      
      public CompendiumSettings(int skillLvl){
         this.sortType = ArcaneTome.TomeSort.RECOMMENDED;
         this.filterType = ArcaneTome.TomeFilter.NONE;
         this.page = 1;
         this.skillLvl = skillLvl;
      }
      
      public CompendiumSettings(ArcaneTome.TomeSort sortType, ArcaneTome.TomeFilter filterType, int page, int skillLvl){
         this.sortType = sortType;
         this.filterType = filterType;
         this.page = page;
         this.skillLvl = skillLvl;
      }
   
      public ArcaneTome.TomeFilter getFilterType(){
         return filterType;
      }
   
      public ArcaneTome.TomeSort getSortType(){
         return sortType;
      }
   
      public int getPage(){
         return page;
      }
   
      public void setPage(int page){
         this.page = page;
      }
   
      public void setFilterType(ArcaneTome.TomeFilter filterType){
         this.filterType = filterType;
      }
   
      public void setSortType(ArcaneTome.TomeSort sortType){
         this.sortType = sortType;
      }
   }
}
