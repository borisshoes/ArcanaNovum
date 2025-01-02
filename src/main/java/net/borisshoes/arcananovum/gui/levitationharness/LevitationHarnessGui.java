package net.borisshoes.arcananovum.gui.levitationharness;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class LevitationHarnessGui extends SimpleGui {
   private LevitationHarness harness;
   private ItemStack item;
   private boolean validStone;
   
   public LevitationHarnessGui(ScreenHandlerType<?> type, ServerPlayerEntity player, LevitationHarness harness, ItemStack item){
      super(type, player, false);
      this.harness = harness;
      this.item = item;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      return true;
   }
   
   @Override
   public void onClose(){
      // Return invalid items
      Inventory inv = getSlotRedirect(1).inventory;
      for(int i=0; i<inv.size();i++){
         if(validStone && i == 0)
            continue;
         ItemStack stack = inv.getStack(i);
         if(!stack.isEmpty()){
         
            ItemEntity itemEntity;
            boolean bl = player.getInventory().insertStack(stack);
            if(!bl || !stack.isEmpty()){
               itemEntity = player.dropItem(stack, false);
               if(itemEntity == null) continue;
               itemEntity.resetPickupDelay();
               itemEntity.setOwner(player.getUuid());
               continue;
            }
            stack.setCount(1);
            itemEntity = player.dropItem(stack, false);
            if(itemEntity != null){
               itemEntity.setDespawnImmediately();
            }
         }
      }
      
      harness.recalculateEnergy(item);
   }
   
   public void validStone(ItemStack newStone){
      validStone = true;
      harness.setStone(item,newStone);
   }
   
   public void notValidStone(){
      validStone = false;
      harness.setStone(item,null);
   }
}