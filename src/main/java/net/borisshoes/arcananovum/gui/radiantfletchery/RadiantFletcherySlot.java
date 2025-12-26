package net.borisshoes.arcananovum.gui.radiantfletchery;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public class RadiantFletcherySlot extends Slot {
   private final int mode; // 0 - arrow, 1 - potion, 2 - output
   
   public RadiantFletcherySlot(Container inventory, int index, int x, int y, int mode){
      super(inventory, index, x, y);
      this.mode = mode;
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      if(mode == 0){
         return stack.is(Items.ARROW);
      }else if(mode == 1){
         PotionContents potionContentsComponent = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         return (potionContentsComponent.hasEffects() && stack.is(ArcanaRegistry.FLETCHERY_POTION_ITEMS));
      }
      return false;
   }
}
