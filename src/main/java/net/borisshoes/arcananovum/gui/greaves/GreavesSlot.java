package net.borisshoes.arcananovum.gui.greaves;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class GreavesSlot extends Slot {
   public static final BiPredicate<ItemStack, List<ItemStack>> PREDICATE = (stack, others) ->
         stack.getItem() instanceof BlockItem && stack.getMaxStackSize() > 1;
   
   public GreavesSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return isValidItem(stack);
   }
   
   public static boolean isValidItem(ItemStack stack){
      return PREDICATE.test(stack,new ArrayList<>());
   }
}
