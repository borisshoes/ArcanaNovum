package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TomeGui extends SimpleGui {
   private ArcaneTome.TomeMode mode;
   private ArcaneTome tome;
   private CompendiumSettings settings;
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    * @param mode                        mode of screen (profile:0 items:1)
    */
   public TomeGui(ScreenHandlerType<?> type, ServerPlayerEntity player, ArcaneTome.TomeMode mode, ArcaneTome tome){
      super(type, player, false);
      this.mode = mode;
      this.tome = tome;
      this.settings = new CompendiumSettings();
   }
   
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
                  tome.openRecipeGui(player,settings, magicItem.getId());
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
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               MagicItemRecipe recipe = magicItem.getRecipe();
               Inventory inv = getSlotRedirect(1).inventory;
   
               ItemStack newMagicItem = magicItem.addCrafter(magicItem.forgeItem(inv),player.getUuidAsString());
               if(newMagicItem == null){
                  return false;
               }
               
               if(!PLAYER_DATA.get(player).addCrafted(magicItem.getId()) && !(magicItem instanceof ArcaneTome)){
                  PLAYER_DATA.get(player).addXP(MagicRarity.getCraftXp(magicItem.getRarity()));
               }
   
               if(magicItem.getRarity() != MagicRarity.MUNDANE){
                  ArcanaAchievements.grant(player,"intro_arcana");
                  ArcanaAchievements.progress(player,"intermediate_artifice",1);
               }
               if(magicItem.getRarity() == MagicRarity.LEGENDARY) ArcanaAchievements.grant(player,"artificial_divinity");
   
               ItemStack[][] ingredients = new ItemStack[5][5];
               for(int i = 0; i < inv.size(); i++){
                  ingredients[i/5][i%5] = inv.getStack(i);
               }
               ItemStack[][] remainders = recipe.getRemainders(ingredients);
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
               player.sendMessage(Text.translatable("No Lore Found For That Item").formatted(Formatting.RED),false);
            }
         }else if(index == 43){
            tome.openGui(player, ArcaneTome.TomeMode.CRAFTING,settings,magicItem.getId());
         }
      }else if(mode == ArcaneTome.TomeMode.ITEM){
         ItemStack item = this.getSlot(4).getItemStack();
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         
         if(index == 2){
            if(magicItem.getRarity() == MagicRarity.MYTHICAL){
               player.sendMessage(Text.translatable("You Cannot Craft Mythical Items").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),false);
            }else{
               if(magicItem.getRecipe() != null){
                  tome.openRecipeGui(player,settings, magicItem.getId());
               }else{
                  player.sendMessage(Text.translatable("You Cannot Craft This Item").formatted(Formatting.RED),false);
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
               player.sendMessage(Text.translatable("No Lore Found For That Item").formatted(Formatting.RED),false);
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
               player.sendMessage(Text.translatable("Insert an Item to Tinker").formatted(Formatting.RED),false);
            }
         }else if(index ==  22){
            if(magicItem != null){
               RenameGui renameGui = new RenameGui(player,tome,settings,item);
               renameGui.setTitle(Text.translatable("Rename Magic Item"));
               renameGui.setSlot(0, GuiElementBuilder.from(item));
               renameGui.setSlot(2, GuiElementBuilder.from(item));
               renameGui.open();
            }else{
               player.sendMessage(Text.translatable("Insert an Item to Tinker").formatted(Formatting.RED),false);
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
      
      public CompendiumSettings(){
         this.sortType = ArcaneTome.TomeSort.RECOMMENDED;
         this.filterType = ArcaneTome.TomeFilter.NONE;
         this.page = 1;
      }
      
      public CompendiumSettings(ArcaneTome.TomeSort sortType, ArcaneTome.TomeFilter filterType, int page){
         this.sortType = sortType;
         this.filterType = filterType;
         this.page = page;
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
