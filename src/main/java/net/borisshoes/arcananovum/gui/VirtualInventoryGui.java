package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;

import static net.borisshoes.arcananovum.ArcanaNovum.VIRTUAL_INVENTORY_GUIS;

public class VirtualInventoryGui<I extends Container> extends SimpleGui {
   protected I inventory;
   
   public VirtualInventoryGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots){
      super(type, player, manipulatePlayerSlots);
   }
   
   public I getInventory(){
      return inventory;
   }
   
   @Override
   public void onOpen(){
      VIRTUAL_INVENTORY_GUIS.put(this,this.player);
      super.onOpen();
   }
   
   @Override
   public void onClose(){
      VIRTUAL_INVENTORY_GUIS.remove(this);
      super.onClose();
   }
}
