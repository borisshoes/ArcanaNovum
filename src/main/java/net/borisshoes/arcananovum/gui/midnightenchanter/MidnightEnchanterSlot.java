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
      boolean isPaper = stack.isOf(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      boolean enchantableOrEnchanted = EnchantmentHelper.canHaveEnchantments(stack) || EnchantmentHelper.hasEnchantments(stack);
      boolean atMostOneIncoming = stack.getCount() <= 1;
      boolean sameItemAndComponents = ItemStack.areItemsAndComponentsEqual(this.getStack(), stack);
      boolean notMergingOrSlotEndsAtOne = !sameItemAndComponents || stack.getCount() + this.getStack().getCount() <= 1;
      boolean sizeLimit = atMostOneIncoming && notMergingOrSlotEndsAtOne;
      return (enchantableOrEnchanted && sizeLimit) || isPaper;
   }
   
   @Override
   public int getMaxItemCount(){
      return 64;
   }
}
