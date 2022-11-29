package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.minecraft.inventory.SimpleInventory;

public class QuiverInventory extends SimpleInventory {
   public QuiverInventory(){
      super(QuiverItem.size);
   }
}
