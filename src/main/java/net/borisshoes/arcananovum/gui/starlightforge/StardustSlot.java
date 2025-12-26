package net.borisshoes.arcananovum.gui.starlightforge;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StardustSlot  extends Slot {
   public StardustSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return stack.is(ArcanaRegistry.STARDUST);
   }
}
