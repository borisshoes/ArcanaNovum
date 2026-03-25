package net.borisshoes.arcananovum.gui.spawnerinfuser;

import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpawnerInfuserPointsSlot extends Slot {
   public SpawnerInfuserPointsSlot(Container inventory, int index, int x, int y){
      super(inventory, index, x, y);
   }
   
   @Override
   public boolean mayPlace(ItemStack stack){
      return stack.is(SpawnerInfuser.getPointsItem()) && !ArcanaItemUtils.isArcane(stack);
   }
}
