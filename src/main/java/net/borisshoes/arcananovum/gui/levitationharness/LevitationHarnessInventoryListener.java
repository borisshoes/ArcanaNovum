package net.borisshoes.arcananovum.gui.levitationharness;

import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class LevitationHarnessInventoryListener implements InventoryChangedListener {
   private final LevitationHarness harness;
   private final LevitationHarnessGui gui;
   private final ItemStack item;
   private boolean updating = false;
   
   public LevitationHarnessInventoryListener(LevitationHarness harness, LevitationHarnessGui gui, ItemStack item){
      this.harness = harness;
      this.gui = gui;
      this.item = item;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         
         //Check Soulstone, glowstone, and update item
         validSoulstone(inv);
         ItemStack glowSlot = inv.getStack(1);
         if(glowSlot.isOf(Items.GLOWSTONE)){
            harness.addGlow(item,glowSlot.getCount()*4);
            inv.setStack(1,ItemStack.EMPTY);
         }else if(glowSlot.isOf(Items.GLOWSTONE_DUST)){
            harness.addGlow(item,glowSlot.getCount());
            inv.setStack(1,ItemStack.EMPTY);
         }
         
         harness.recalculateEnergy(item);
         harness.buildGui(item,gui);
         
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
   
   public boolean validSoulstone(Inventory inv){
      ItemStack item = inv.getStack(0);
      
      if(MagicItemUtils.isMagic(item)){
         if(MagicItemUtils.identifyItem(item) instanceof Soulstone stone){
            if(Soulstone.getType(item).equals(EntityType.getId(EntityType.SHULKER).toString())){
               gui.validStone(item);
               return true;
            }
         }
      }
      gui.notValidStone();
      return false;
   }
}