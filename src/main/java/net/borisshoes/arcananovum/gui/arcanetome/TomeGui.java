package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.*;
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
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TomeGui extends SimpleGui {
   private int mode;
   private ArcaneTome tome;
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    * @param mode                        mode of screen (profile:0 items:1)
    */
   public TomeGui(ScreenHandlerType<?> type, ServerPlayerEntity player, int mode, ArcaneTome tome){
      super(type, player, false);
      this.mode = mode;
      this.tome = tome;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      if(mode == 0){
         if(index == 49){
            tome.buildItemsGui(this,player);
         }
         if(index == 4){
            // Guide gui
            ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
            writablebook.setNbt(getGuideBook());
            BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
            LoreGui loreGui = new LoreGui(player,bookBuilder,tome,0);
            loreGui.open();
         }
      }else if(mode == 1){
         if(index == 4){
            tome.buildProfileGui(this,player);
         }
         if(index == 49){
            tome.openGui(player,2);
         }
         if(index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8){
            ItemStack item = this.getSlot(index).getItemStack();
            if(!item.isEmpty()){
               if(type == ClickType.MOUSE_RIGHT){
                  MagicItem magicItem = MagicItemUtils.identifyItem(item);
                  if(magicItem.getRarity() == MagicRarity.MYTHICAL){
                     player.sendMessage(new LiteralText("You Cannot Craft Mythical Items").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC),false);
                  }else{
                     if(magicItem.getRecipe() != null){
                        tome.openRecipeGui(player, magicItem.getId());
                     }else{
                        player.sendMessage(new LiteralText("You Cannot Craft This Item").formatted(Formatting.RED),false);
                     }
                  }
               }else{
                  NbtCompound loreData = MagicItemUtils.identifyItem(item).getBookLore();
                  if(loreData != null){
                     ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
                     writablebook.setNbt(loreData);
                     BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
                     LoreGui loreGui = new LoreGui(player,bookBuilder,tome,1);
                     loreGui.open();
                  }else{
                     player.sendMessage(new LiteralText("No Lore Found For That Item").formatted(Formatting.RED),false);
                  }
               }
            }
            
         }
      }else if(mode == 2){
         if(index == 7){
            //Give Items back
            Inventory inv = getSlotRedirect(1).inventory;
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
            tome.openGui(player,1);
         }else if(index == 25){
            ItemStack item = this.getSlot(index).getItemStack();
            if(MagicItemUtils.isMagic(item)){
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               MagicItemRecipe recipe = magicItem.getRecipe();
               Inventory inv = getSlotRedirect(1).inventory;
   
               ItemStack newMagicItem;
   
               if(magicItem instanceof EssenceEgg){
                  // Souls n stuff
                  ItemStack soulstoneStack = inv.getStack(12); // Should be the Soulstone
                  if(MagicItemUtils.identifyItem(soulstoneStack) instanceof Soulstone soulstone){
                     int uses = (Soulstone.getSouls(soulstoneStack) / Soulstone.tiers[0]);
                     String essenceType = Soulstone.getType(soulstoneStack);
                     
                     newMagicItem = MagicItems.ESSENCE_EGG.getNewItem();
                     EssenceEgg.setType(newMagicItem,essenceType);
                     EssenceEgg.setUses(newMagicItem,uses);
                  }else{
                     return false;
                  }
               }else{
                  newMagicItem = magicItem.getNewItem();
               }
               if(!PLAYER_DATA.get(player).addCrafted(magicItem.getId()) && !(magicItem instanceof ArcaneTome)){
                  PLAYER_DATA.get(player).addXP(MagicRarity.getCraftXp(magicItem.getRarity()));
               }
               
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
            tome.openGui(player,0);
         }
      }else if(mode == 3){
         if(index == 7){
            tome.openGui(player,1);
         }else if(index == 25){
            ItemStack item = this.getSlot(index).getItemStack();
            NbtCompound loreData = MagicItemUtils.identifyItem(item).getBookLore();
            if(loreData != null){
               ItemStack writablebook = new ItemStack(Items.WRITABLE_BOOK);
               writablebook.setNbt(loreData);
               BookElementBuilder bookBuilder = BookElementBuilder.from(writablebook);
               LoreGui loreGui = new LoreGui(player,bookBuilder,tome,1);
               loreGui.open();
            }else{
               player.sendMessage(new LiteralText("No Lore Found For That Item").formatted(Formatting.RED),false);
            }
         }else if(index == 43){
            tome.openGui(player,2);
         }
      }
      return true;
   }
   
   @Override
   public void onClose(){
      if(mode == 3){ // Recipe gui to compendium
         tome.openGui(player,1);
      }else if(mode == 2){ // Crafting gui give items back
         //Give Items back
         Inventory inv = getSlotRedirect(1).inventory;
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
   }
   
   public int getMode(){
      return mode;
   }
   
   public void setMode(int mode){
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
}
