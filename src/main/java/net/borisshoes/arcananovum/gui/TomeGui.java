package net.borisshoes.arcananovum.gui;

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
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.lwjgl.system.CallbackI;

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
                  System.out.println("Displaying Crafting GUI for: "+item.getName().getString());
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
                     LoreGui loreGui = new LoreGui(player,bookBuilder,tome);
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
               System.out.println("Crafting Magic Item: "+magicItem.getName());
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
               LoreGui loreGui = new LoreGui(player,bookBuilder,tome);
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
}
