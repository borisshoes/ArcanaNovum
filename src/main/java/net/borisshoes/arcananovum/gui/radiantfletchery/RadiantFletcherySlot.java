package net.borisshoes.arcananovum.gui.radiantfletchery;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.slot.Slot;

public class RadiantFletcherySlot extends Slot {
   private final int mode; // 0 - arrow, 1 - potion, 2 - output
   
   public RadiantFletcherySlot(Inventory inventory, int index, int x, int y, int mode){
      super(inventory, index, x, y);
      this.mode = mode;
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      if(mode == 0){
         return stack.isOf(Items.ARROW);
      }else if(mode == 1){
         return PotionUtil.getPotion(stack) != Potions.EMPTY || !PotionUtil.getCustomPotionEffects(stack).isEmpty();
      }
      return false;
   }
}
