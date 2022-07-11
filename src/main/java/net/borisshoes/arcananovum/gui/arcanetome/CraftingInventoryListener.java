package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class CraftingInventoryListener implements InventoryChangedListener {
   private final ArcaneTome tome;
   private final TomeGui gui;
   private boolean updating = false;
   
   public CraftingInventoryListener(ArcaneTome tome, TomeGui gui){this.tome = tome; this.gui = gui;}
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         for(int i = 0; i < 25; i++){
            ItemStack stack = inv.getStack(i);
            if(stack.getCount() != 0){
               //System.out.println("Slot " + i + ": " + stack.getItem().getName().getString() + " (" + stack.getCount() + ")");
            }
         }
         //Check Valid Recipe, and update gui
         validRecipe(inv);
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void validRecipe(Inventory inv){
      ItemStack[][] curItems = new ItemStack[5][5];
      for(int i = 0; i < 5; i++){
         for(int j = 0; j < 5; j++){
            curItems[i][j] = inv.getStack(i*5+j);
         }
      }
      MagicItem matchedItem = null;
      for(MagicItem item : MagicItems.registry.values()){
         MagicItemRecipe recipe = item.getRecipe();
         if(recipe == null)
            continue;
         if(recipe.satisfiesRecipe(curItems)){
            matchedItem = item;
            break;
         }
      }
      if(matchedItem == null){
         ItemStack table = new ItemStack(Items.CRAFTING_TABLE);
         NbtCompound tag = table.getOrCreateNbt();
         NbtCompound display = new NbtCompound();
         NbtList loreList = new NbtList();
         display.putString("Name","[{\"text\":\"Forge Item\",\"italic\":false,\"color\":\"dark_purple\"}]");
         loreList.add(NbtString.of("[{\"text\":\"Click Here\",\"italic\":false,\"color\":\"green\"},{\"text\":\" to forge a Magic Item once a recipe is loaded!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"This slot will show a Magic Item once a valid recipe is loaded.\",\"italic\":true,\"color\":\"aqua\"}]"));
         display.put("Lore",loreList);
         tag.put("display",display);
         tag.putInt("HideFlags",103);
         gui.setSlot(25, GuiElementBuilder.from(table));
      }else{
         gui.setSlot(25, GuiElementBuilder.from(matchedItem.getPrefItem()).addLoreLine(new LiteralText("")).addLoreLine(new LiteralText("Click to Forge!").formatted(Formatting.AQUA,Formatting.BOLD)).glow());
      }
   }
}
