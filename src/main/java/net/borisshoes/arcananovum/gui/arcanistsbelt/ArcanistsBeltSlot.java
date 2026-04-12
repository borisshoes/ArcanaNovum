package net.borisshoes.arcananovum.gui.arcanistsbelt;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class ArcanistsBeltSlot extends Slot {
   public static final BiPredicate<ItemStack, List<ItemStack>> PREDICATE = (stack, others) -> {
      if(stack.is(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_ALLOWED)) return true;
      if(stack.is(ArcanaRegistry.ARCANISTS_BELT_SPECIAL_DISALLOWED)) return false;
      return stack.getMaxStackSize() == 1 && !stack.has(DataComponents.EQUIPPABLE) && !(stack.getItem() instanceof BlockItem);
   };
   
   public ArcanistsBeltSlot(Container inventory, int index, int x, int y){
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
