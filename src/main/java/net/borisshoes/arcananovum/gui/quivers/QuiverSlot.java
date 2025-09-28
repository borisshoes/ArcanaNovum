package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;

public class QuiverSlot extends Slot {
   
   private final boolean runic;
   
   public QuiverSlot(Inventory inventory, boolean runic, int index, int x, int y){
      super(inventory, index, x, y);
      this.runic = runic;
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return isValidItem(stack,runic);
   }
   
   public static boolean isValidItem(ItemStack stack, boolean isRunic){
      if(isRunic){
         return stack.isIn(ItemTags.ARROWS) || ArcanaItemUtils.isRunicArrow(stack);
      }else{
         return stack.isIn(ItemTags.ARROWS) && !(ArcanaItemUtils.isRunicArrow(stack));
      }
   }
}
