package net.borisshoes.arcananovum.gui.greaves;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class GreavesSlot extends Slot {
   public GreavesSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return isValidItem(stack);
   }
   
   public static boolean isValidItem(ItemStack stack){
      return stack.getItem() instanceof BlockItem && stack.getMaxCount() > 1;
   }
}
