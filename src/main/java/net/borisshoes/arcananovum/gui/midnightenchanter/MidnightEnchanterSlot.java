package net.borisshoes.arcananovum.gui.midnightenchanter;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MidnightEnchanterSlot extends Slot {
   
   public MidnightEnchanterSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return stack.isEnchantable() || !EnchantmentHelper.get(stack).isEmpty() || stack.hasEnchantments();
   }
   
   @Override
   public int getMaxItemCount() {
      return 1;
   }
}
