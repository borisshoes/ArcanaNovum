package net.borisshoes.arcananovum.gui.levitationharness;

import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LevitationHarnessInventoryListener implements ContainerListener {
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
   public void containerChanged(Container inv){
      if(!updating){
         updating = true;
         
         //Check Soulstone, glowstone, and update item
         validSoulstone(inv);
         ItemStack glowSlot = inv.getItem(1);
         if(glowSlot.is(Items.GLOWSTONE)){
            harness.addGlow(item, glowSlot.getCount() * 4);
            inv.setItem(1, ItemStack.EMPTY);
         }else if(glowSlot.is(Items.GLOWSTONE_DUST)){
            harness.addGlow(item, glowSlot.getCount());
            inv.setItem(1, ItemStack.EMPTY);
         }
         
         harness.recalculateEnergy(item);
         harness.buildGui(item, gui);
         
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void setUpdating(){
      updating = true;
   }
   
   public boolean validSoulstone(Container inv){
      ItemStack item = inv.getItem(0);
      
      if(ArcanaItemUtils.isArcane(item)){
         if(ArcanaItemUtils.identifyItem(item) instanceof Soulstone stone){
            if(Soulstone.getType(item).equals(EntityType.getKey(EntityType.SHULKER).toString())){
               gui.validStone(item);
               return true;
            }
         }
      }
      gui.notValidStone();
      return false;
   }
}