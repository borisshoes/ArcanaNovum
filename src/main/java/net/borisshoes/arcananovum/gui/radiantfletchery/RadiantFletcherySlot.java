package net.borisshoes.arcananovum.gui.radiantfletchery;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
         PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
         return (potionContentsComponent.hasEffects() && stack.isIn(ArcanaRegistry.FLETCHERY_POTION_ITEMS));
      }
      return false;
   }
}
