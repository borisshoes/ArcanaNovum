package net.borisshoes.arcananovum.gui.stellarcore;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.forge.StellarCoreBlockEntity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class StellarCoreInventoryListener implements InventoryChangedListener {
   private final StellarCoreGui gui;
   private final StellarCoreBlockEntity blockEntity;
   private boolean updating = false;
   
   public StellarCoreInventoryListener(StellarCoreGui gui, StellarCoreBlockEntity blockEntity){
      this.gui = gui;
      this.blockEntity = blockEntity;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         setUpdating();
         
         ItemStack stack = inv.getStack(0);
         List<ItemStack> salvage = blockEntity.salvageItem(stack);
         if(!salvage.isEmpty()){
            salvage = salvage.stream().filter(s -> !s.isEmpty() && s.getCount()>0).toList();
            ArcanaAchievements.progress(gui.getPlayer(),ArcanaAchievements.RECLAMATION.id, stack.getCount());
            if(salvage.stream().anyMatch(s -> s.isOf(Items.NETHERITE_SCRAP))){
               ArcanaAchievements.grant(gui.getPlayer(),ArcanaAchievements.SCRAP_TO_SCRAP.id);
            }
            
            inv.setStack(0,ItemStack.EMPTY);
            SimpleInventory newInv = new SimpleInventory(salvage.toArray(new ItemStack[0]));
            MiscUtils.returnItems(newInv,gui.getPlayer());
            
            if(blockEntity.getWorld() instanceof ServerWorld serverWorld){
               SoundUtils.playSound(serverWorld,blockEntity.getPos(), SoundEvents.ENTITY_BLAZE_DEATH, SoundCategory.BLOCKS, 1, 0.8f);
               SoundUtils.playSound(serverWorld,blockEntity.getPos(), SoundEvents.ENTITY_IRON_GOLEM_HURT, SoundCategory.BLOCKS, 1, 1.2f);
            }
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
