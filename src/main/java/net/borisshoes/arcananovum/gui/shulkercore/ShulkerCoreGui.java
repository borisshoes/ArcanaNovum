package net.borisshoes.arcananovum.gui.shulkercore;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ShulkerCoreGui extends SimpleGui {
   private ShulkerCore core;
   private ItemStack item;
   private boolean valid;

   public ShulkerCoreGui(MenuType<?> type, ServerPlayer player, ShulkerCore core, ItemStack item){
      super(type, player, false);
      this.core = core;
      this.item = item;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      return true;
   }
   
   @Override
   public void onClose(){
      if(!valid){
         // Return invalid item
         Container inv = getSlotRedirect(2).container;
         for(int i = 0; i<inv.getContainerSize(); i++){
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
      }
      core.buildItemLore(item,player.level().getServer());
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
