package net.borisshoes.arcananovum.gui.arcanistsbelt;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ArcanistsBeltSlot extends Slot {
   public ArcanistsBeltSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return isValidItem(stack);
   }
   
   public static boolean isValidItem(ItemStack stack){
      if(stack.is(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_ALLOWED)) return true;
      if(stack.is(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_DISALLOWED)) return false;
      return stack.getMaxStackSize() == 1 && !stack.has(DataComponents.EQUIPPABLE) && !(stack.getItem() instanceof BlockItem);
   }
}
