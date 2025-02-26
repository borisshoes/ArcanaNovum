package net.borisshoes.arcananovum.gui.shulkercore;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.normal.GraphicItems;
import net.borisshoes.arcananovum.items.normal.GraphicalItem;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ShulkerCoreInventoryListener implements InventoryChangedListener {
   private final ShulkerCore core;
   private final ShulkerCoreGui gui;
   private final ItemStack item;
   private boolean updating = false;
   
   public ShulkerCoreInventoryListener(ShulkerCore core, ShulkerCoreGui gui, ItemStack item){
      this.core = core;
      this.gui = gui;
      this.item = item;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;

         //Check Soulstone, and update item
         boolean hasStone = validSoulstone(inv);
         
         GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicItems.MENU_TOP,hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
         String paneText = hasStone ? LevelUtils.readableInt(core.getEnergy(item)) + " Shulker Souls" : "No Soulstone Inserted";
         Formatting textColor = hasStone ? Formatting.YELLOW : Formatting.RED;
   
         gui.setSlot(0,pane.setName(Text.literal(paneText).formatted(textColor)));
         gui.setSlot(1,pane.setName(Text.literal(paneText).formatted(textColor)));
         gui.setSlot(3,pane.setName(Text.literal(paneText).formatted(textColor)));
         gui.setSlot(4,pane.setName(Text.literal(paneText).formatted(textColor)));
         
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
   
   public boolean validSoulstone(Inventory inv){
      ItemStack item = inv.getStack(0);
      
      if(ArcanaItemUtils.isArcane(item)){
         if(ArcanaItemUtils.identifyItem(item) instanceof Soulstone stone){
            if(Soulstone.getType(item).equals(EntityType.getId(EntityType.SHULKER).toString())){
               gui.validStone(item);
               return true;
            }
         }
      }
      gui.notValid();
      return false;
   }
}
