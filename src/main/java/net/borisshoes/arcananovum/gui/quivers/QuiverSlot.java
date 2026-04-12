package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class QuiverSlot extends Slot {
   public static final BiPredicate<ItemStack, List<ItemStack>> RUNIC_PREDICATE = (stack, others) ->
         stack.is(ItemTags.ARROWS) || ArcanaItemUtils.isRunicArrow(stack);
   public static final BiPredicate<ItemStack, List<ItemStack>> NON_RUNIC_PREDICATE = (stack, others) ->
         stack.is(ItemTags.ARROWS) && !(ArcanaItemUtils.isRunicArrow(stack));
   
   private final boolean runic;
   
   public QuiverSlot(Container inventory, boolean runic, int index, int x, int y){
      super(inventory, index, x, y);
      this.runic = runic;
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return isValidItem(stack, runic);
   }
   
   public static boolean isValidItem(ItemStack stack, boolean isRunic){
      if(isRunic){
         return RUNIC_PREDICATE.test(stack,new ArrayList<>());
      }else{
         return NON_RUNIC_PREDICATE.test(stack,new ArrayList<>());
      }
   }
}
