package net.borisshoes.arcananovum.gui.arcanistsbelt;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ArcanistsBeltSlot extends Slot {
   public ArcanistsBeltSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return isValidItem(stack);
   }
   
   public static boolean isValidItem(ItemStack stack){
      if(stack.isIn(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_ALLOWED)) return true;
      if(stack.isIn(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_DISALLOWED)) return false;
      return stack.getMaxCount() == 1 && !stack.contains(DataComponentTypes.EQUIPPABLE) && !(stack.getItem() instanceof BlockItem);
   }
}
