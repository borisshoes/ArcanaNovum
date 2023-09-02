package net.borisshoes.arcananovum.gui.midnightenchanter;

import net.borisshoes.arcananovum.blocks.forge.MidnightEnchanterBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;

public class MidnightEnchanterInventoryListener  implements InventoryChangedListener {
   private final MidnightEnchanterGui gui;
   private final MidnightEnchanterBlockEntity blockEntity;
   private boolean updating = false;
   
   public MidnightEnchanterInventoryListener(MidnightEnchanterGui gui, MidnightEnchanterBlockEntity blockEntity){
      this.gui = gui;
      this.blockEntity = blockEntity;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         setUpdating();
         gui.buildGui();
         gui.setItem(inv.getStack(0));
         //Update gui
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
}
