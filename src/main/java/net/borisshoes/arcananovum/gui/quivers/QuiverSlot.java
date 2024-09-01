package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class QuiverSlot extends Slot {
   
   private final boolean runic;
   
   public QuiverSlot(Inventory inventory, boolean runic, int index, int x, int y){
      super(inventory, index, x, y);
      this.runic = runic;
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      if(runic){
         return (stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW) || stack.isOf(Items.ARROW) || ArcanaItemUtils.isRunicArrow(stack));
      }else{
         return (stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW) || stack.isOf(Items.ARROW)) && !(ArcanaItemUtils.isRunicArrow(stack));
      }
   }
}
