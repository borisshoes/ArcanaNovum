package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.borisshoes.arcananovum.ArcanaNovum.VIRTUAL_INVENTORY_GUIS;

public class VirtualInventoryGui<I extends Inventory> extends SimpleGui {
   protected I inventory;
   
   public VirtualInventoryGui(ScreenHandlerType<?> type, ServerPlayerEntity player, boolean manipulatePlayerSlots){
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
