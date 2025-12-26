package net.borisshoes.arcananovum.gui.greaves;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class GreavesSlot extends Slot {
   public GreavesSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return isValidItem(stack);
   }
   
   public static boolean isValidItem(ItemStack stack){
      return stack.getItem() instanceof BlockItem && stack.getMaxStackSize() > 1;
   }
}
