package net.borisshoes.arcananovum.gui.spawnerinfuser;

import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SpawnerInfuserPointsSlot extends Slot {
   public SpawnerInfuserPointsSlot(Inventory inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean canInsert(ItemStack stack){
      return stack.isOf(SpawnerInfuser.pointsItem) && !MagicItemUtils.isMagic(stack);
   }
}
