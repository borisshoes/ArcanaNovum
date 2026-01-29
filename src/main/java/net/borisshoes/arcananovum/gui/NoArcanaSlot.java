package net.borisshoes.arcananovum.gui;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NoArcanaSlot extends Slot {
   public NoArcanaSlot(Container container, int index, int x, int y){
      super(container, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack itemStack){
      if(itemStack.is(ArcanaRegistry.ALL_ARCANA_ITEMS)) return false;
      return super.mayPlace(itemStack);
   }
}
