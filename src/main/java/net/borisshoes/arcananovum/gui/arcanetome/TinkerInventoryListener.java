package net.borisshoes.arcananovum.gui.arcanetome;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class TinkerInventoryListener implements InventoryChangedListener {
   private final ArcaneTome tome;
   private final TomeGui gui;
   private boolean updating = false;
   private final int[][] dynamicSlots = {{},{3},{1,5},{1,3,5},{0,2,4,6},{1,2,3,4,5},{0,1,2,4,5,6},{0,1,2,3,4,5,6}};
   
   public TinkerInventoryListener(ArcaneTome tome, TomeGui gui){
      this.tome = tome;
      this.gui = gui;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         for(int i = 0; i < inv.size(); i++){
            ItemStack stack = inv.getStack(i);
            if(stack.getCount() != 0){
               //System.out.println("Slot " + i + ": " + stack.getItem().getName().getString() + " (" + stack.getCount() + ")");
            }
         }
         //Update gui
         redraw(inv);
         finishUpdate();
      }
   }
   
   private void redraw(Inventory inv){
      ItemStack item = inv.getStack(0);
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
   
      ItemStack upgradePane;
      if(magicItem == null){
         upgradePane = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      }else{
         upgradePane = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
      }
      NbtCompound tag = upgradePane.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
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
      
      ItemStack itemWindow;
      if(magicItem == null){
         itemWindow = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
      }else{
         itemWindow = new ItemStack(Items.WHITE_STAINED_GLASS_PANE);
   
         int[] upgradeSlots = dynamicSlots[0];
         for(int i = 0; i < upgradeSlots.length; i++){
            gui.clearSlot(28+upgradeSlots[i]);
            gui.clearSlot(37+upgradeSlots[i]);
            gui.clearSlot(46+upgradeSlots[i]);
         }
      }
      
      tag = itemWindow.getOrCreateNbt();
      display = new NbtCompound();
      loreList = new NbtList();
      display.putString("Name","[{\"text\":\"Insert a Magic Item to Tinker with it\",\"italic\":false,\"color\":\"dark_purple\"}]");
      loreList.add(NbtString.of("[{\"text\":\"Apply upgrades or rename your item!\",\"color\":\"light_purple\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.putInt("HideFlags",103);
      gui.setSlot(3, GuiElementBuilder.from(itemWindow));
      gui.setSlot(5,GuiElementBuilder.from(itemWindow));
      gui.setSlot(12,GuiElementBuilder.from(itemWindow));
      gui.setSlot(13,GuiElementBuilder.from(itemWindow));
      gui.setSlot(14,GuiElementBuilder.from(itemWindow));
      
   }
   
   public void finishUpdate(){
      updating = false;
   }
}
