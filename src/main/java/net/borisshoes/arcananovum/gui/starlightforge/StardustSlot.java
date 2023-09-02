package net.borisshoes.arcananovum.gui.starlightforge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class StardustSlot  extends Slot {
   public StardustSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return stack.isOf(ArcanaRegistry.STARDUST);
   }
}
