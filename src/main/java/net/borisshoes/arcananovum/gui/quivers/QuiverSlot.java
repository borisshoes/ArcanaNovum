package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class QuiverSlot extends Slot {
   
   private final boolean runic;
   
   public QuiverSlot(Container inventory, boolean runic, int index, int x, int y){
      super(inventory, index, x, y);
      this.runic = runic;
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return isValidItem(stack,runic);
   }
   
   public static boolean isValidItem(ItemStack stack, boolean isRunic){
      if(isRunic){
         return stack.is(ItemTags.ARROWS) || ArcanaItemUtils.isRunicArrow(stack);
      }else{
         return stack.is(ItemTags.ARROWS) && !(ArcanaItemUtils.isRunicArrow(stack));
      }
   }
}
