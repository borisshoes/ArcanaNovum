package net.borisshoes.arcananovum.gui.midnightenchanter;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class MidnightEnchanterSlot extends Slot {
   
   public MidnightEnchanterSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      boolean isPaper = stack.is(ArcanaRegistry.EMPOWERED_ARCANE_PAPER);
      boolean enchantableOrEnchanted = EnchantmentHelper.canStoreEnchantments(stack) || EnchantmentHelper.hasAnyEnchantments(stack);
      boolean atMostOneIncoming = stack.getCount() <= 1;
      boolean sameItemAndComponents = ItemStack.isSameItemSameComponents(this.getItem(), stack);
      boolean notMergingOrSlotEndsAtOne = !sameItemAndComponents || stack.getCount() + this.getItem().getCount() <= 1;
      boolean sizeLimit = atMostOneIncoming && notMergingOrSlotEndsAtOne;
      return (enchantableOrEnchanted && sizeLimit) || isPaper;
   }
   
   @Override
   public int getMaxStackSize(){
      return 64;
   }
}
