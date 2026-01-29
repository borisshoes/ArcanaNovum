package net.borisshoes.arcananovum.gui.shulkercore;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public class ShulkerCoreInventoryListener implements ContainerListener {
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
   public void containerChanged(Container inv){
      if(!updating){
         updating = true;

         //Check Soulstone, and update item
         boolean hasStone = validSoulstone(inv);
         
         GuiElementBuilder pane = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.MENU_TOP,hasStone ? ArcanaColors.ARCANA_COLOR : ArcanaColors.DARK_COLOR));
         String paneText = hasStone ? LevelUtils.readableInt(EnergyItem.getEnergy(item)) + " Shulker Souls" : "No Soulstone Inserted";
         ChatFormatting textColor = hasStone ? ChatFormatting.YELLOW : ChatFormatting.RED;
   
         gui.setSlot(0,pane.setName(Component.literal(paneText).withStyle(textColor)));
         gui.setSlot(1,pane.setName(Component.literal(paneText).withStyle(textColor)));
         gui.setSlot(3,pane.setName(Component.literal(paneText).withStyle(textColor)));
         gui.setSlot(4,pane.setName(Component.literal(paneText).withStyle(textColor)));
         
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
   
   public boolean validSoulstone(Container inv){
      ItemStack item = inv.getItem(0);
      
      if(ArcanaItemUtils.isArcane(item)){
         if(ArcanaItemUtils.identifyItem(item) instanceof Soulstone stone){
            if(Soulstone.getType(item).equals(EntityType.getKey(EntityType.SHULKER).toString())){
               gui.validStone(item);
               return true;
            }
         }
      }
      gui.notValid();
      return false;
   }
}
