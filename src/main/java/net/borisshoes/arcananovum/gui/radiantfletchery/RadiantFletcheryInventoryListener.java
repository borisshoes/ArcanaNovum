package net.borisshoes.arcananovum.gui.radiantfletchery;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.RadiantFletcheryBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;

public class RadiantFletcheryInventoryListener implements InventoryChangedListener {
   private final RadiantFletcheryGui gui;
   private final RadiantFletcheryBlockEntity blockEntity;
   private boolean updating = false;
   
   public RadiantFletcheryInventoryListener(RadiantFletcheryGui gui, RadiantFletcheryBlockEntity blockEntity){
      this.gui = gui;
      this.blockEntity = blockEntity;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         setUpdating();
         ItemStack arrowStack = inv.getStack(0);
         ItemStack potionStack = inv.getStack(1);
         ItemStack outputStack = inv.getStack(2);
         int potionRatio = blockEntity.getPotionRatio();
         ItemStack resultStack = new ItemStack(Items.TIPPED_ARROW);
         PotionUtil.setPotion(resultStack, PotionUtil.getPotion(potionStack));
         PotionUtil.setCustomPotionEffects(resultStack, PotionUtil.getCustomPotionEffects(potionStack));
         resultStack.setCount(potionRatio);
         
         while(arrowStack.getCount() >= potionRatio && potionStack.getCount() >= 1 && (((outputStack.getMaxCount()-outputStack.getCount()) >= potionRatio && ItemStack.canCombine(outputStack,resultStack)) || outputStack.isEmpty())){
            arrowStack.decrement(potionRatio);
            inv.setStack(0,arrowStack.isEmpty() ? ItemStack.EMPTY : arrowStack);
            
            potionStack.decrement(1);
            inv.setStack(1,potionStack.isEmpty() ? ItemStack.EMPTY : potionStack);
            
            if(outputStack.isEmpty()){
               outputStack = resultStack;
               inv.setStack(2,resultStack);
            }else{
               outputStack.increment(potionRatio);
               inv.setStack(2,outputStack);
            }
            
            ArcanaAchievements.grant(gui.getPlayer(),ArcanaAchievements.FINALLY_USEFUL_2.id);
         }
         
         // Save back to block entity
         for(int i = 0; i < inv.size(); i++){
            ItemStack stack = inv.getStack(i);
            blockEntity.getInventory().set(i,stack);
         }
         
         //Update gui
         finishUpdate();
      }
   }
   
   public void finishUpdate(){
      updating = false;
   }
   public void setUpdating(){
      updating = true;
   }
}
