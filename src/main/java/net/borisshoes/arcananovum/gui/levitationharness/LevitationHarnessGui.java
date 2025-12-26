package net.borisshoes.arcananovum.gui.levitationharness;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class LevitationHarnessGui extends SimpleGui {
   private LevitationHarness harness;
   private ItemStack item;
   private boolean validStone;
   
   public LevitationHarnessGui(MenuType<?> type, ServerPlayer player, LevitationHarness harness, ItemStack item){
      super(type, player, false);
      this.harness = harness;
      this.item = item;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      return true;
   }
   
   @Override
   public void onClose(){
      // Return invalid items
      Container inv = getSlotRedirect(1).container;
      for(int i = 0; i<inv.getContainerSize(); i++){
         if(validStone && i == 0)
            continue;
         ItemStack stack = inv.getItem(i);
         if(!stack.isEmpty()){
         
            ItemEntity itemEntity;
            boolean bl = player.getInventory().add(stack);
            if(!bl || !stack.isEmpty()){
               itemEntity = player.drop(stack, false);
               if(itemEntity == null) continue;
               itemEntity.setNoPickUpDelay();
               itemEntity.setTarget(player.getUUID());
               continue;
            }
            stack.setCount(1);
            itemEntity = player.drop(stack, false);
            if(itemEntity != null){
               itemEntity.makeFakeItem();
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