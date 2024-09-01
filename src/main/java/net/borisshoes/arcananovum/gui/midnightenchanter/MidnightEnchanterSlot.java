package net.borisshoes.arcananovum.gui.midnightenchanter;

import net.borisshoes.arcananovum.ArcanaRegistry;
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
      boolean sizeLimit = stack.getCount() <= 1 && (!ItemStack.areItemsAndComponentsEqual(this.getStack(), stack) || stack.getCount() + this.getStack().getCount() <= 1);
      return ((EnchantmentHelper.canHaveEnchantments(stack) || EnchantmentHelper.hasEnchantments(stack)) && (sizeLimit) || stack.isOf(ArcanaRegistry.EMPOWERED_ARCANE_PAPER));
   }
   
   @Override
   public int getMaxItemCount() {
      return 64;
   }
}
