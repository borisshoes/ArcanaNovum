package net.borisshoes.arcananovum.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;

import static net.borisshoes.arcananovum.ArcanaNovum.VIRTUAL_INVENTORY_GUIS;

public interface VirtualInventoryGui<I extends Container> {
   I getInventory();
   
   ServerPlayer getPlayer();
   
   default void onVirtualInventoryOpen(){
      VIRTUAL_INVENTORY_GUIS.put(this, getPlayer());
   }
   
   default void onVirtualInventoryClose(){
      VIRTUAL_INVENTORY_GUIS.remove(this);
   }
}