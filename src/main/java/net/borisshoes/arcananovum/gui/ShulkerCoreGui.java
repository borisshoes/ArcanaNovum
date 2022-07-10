package net.borisshoes.arcananovum.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.BrainJar;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShulkerCoreGui extends SimpleGui {
   private ShulkerCore core;
   private ItemStack item;
   private boolean valid;
   /**
    * Constructs a new simple container gui for the supplied player.
    *
    * @param type                        the screen handler that the client should display
    * @param player                      the player to server this gui to
    */
   public ShulkerCoreGui(ScreenHandlerType<?> type, ServerPlayerEntity player, ShulkerCore core, ItemStack item){
      super(type, player, false);
      this.core = core;
      this.item = item;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
      return true;
   }
   
   @Override
   public void onClose(){
      if(!valid){
         // Return invalid item
         Inventory inv = getSlotRedirect(2).inventory;
         for(int i=0; i<inv.size();i++){
            ItemStack stack = inv.getStack(i);
            if(!stack.isEmpty()){
         
               ItemEntity itemEntity;
               boolean bl = player.getInventory().insertStack(stack);
               if (!bl || !stack.isEmpty()) {
                  itemEntity = player.dropItem(stack, false);
                  if (itemEntity == null) continue;
                  itemEntity.resetPickupDelay();
                  itemEntity.setOwner(player.getUuid());
                  continue;
               }
               stack.setCount(1);
               itemEntity = player.dropItem(stack, false);
               if (itemEntity != null) {
                  itemEntity.setDespawnImmediately();
               }
            }
         }
      }
      core.redoLore(item);
   }
   
   public void validStone(ItemStack newStone){
      valid = true;
      core.setStone(item,newStone);
   }
   
   public void notValid(){
      valid = false;
      core.setStone(item,null);
   }
}
