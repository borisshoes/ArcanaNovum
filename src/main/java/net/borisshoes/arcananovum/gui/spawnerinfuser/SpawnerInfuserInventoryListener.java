package net.borisshoes.arcananovum.gui.spawnerinfuser;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SpawnerInfuserInventoryListener implements InventoryChangedListener {
   //private final SpawnerInfuser infuser;
   private final SpawnerInfuserGui gui;
   private final SpawnerInfuserBlockEntity blockEntity;
   private final World world;
   private boolean updating = false;
   private boolean prevStone = false;
   
   public SpawnerInfuserInventoryListener(SpawnerInfuserGui gui, SpawnerInfuserBlockEntity blockEntity, World world){
      //this.infuser = infuser;
      this.gui = gui;
      this.blockEntity = blockEntity;
      this.world = world;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         
         ItemStack soulstoneSlot = inv.getStack(0);
         ItemStack extraPoints = ItemStack.EMPTY;
         int points = blockEntity.getPoints();
         int bonusCap = new int[]{0,64,128,192,256,352}[ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.SOUL_RESERVOIR.id)];
         int ratio = (int) Math.pow(2,ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.AUGMENTED_APPARATUS.id));
         
         if(!soulstoneSlot.isEmpty()){
            blockEntity.setSoulstone(soulstoneSlot);
            if(!prevStone){
               SoundUtils.soulSounds(gui.getPlayer(),1,20);
               if(Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot)) == Soulstone.tiers.length) ArcanaAchievements.grant(gui.getPlayer(),ArcanaAchievements.INNOCENT_SOULS.id);
            }
   
            ItemStack pointsSlot = inv.getStack(1);
            if(!pointsSlot.isEmpty()){
               int maxPoints = SpawnerInfuser.pointsFromTier[Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot))] + bonusCap;
               int toAdd = pointsSlot.getCount() * ratio;
               
               if(maxPoints-points < toAdd){
                  blockEntity.setPoints(maxPoints);
                  extraPoints = pointsSlot.copy();
                  extraPoints.setCount((toAdd-(maxPoints-points))/ratio);
               }else{
                  blockEntity.setPoints(points+toAdd);
               }
               int curPoints = blockEntity.getPoints();
               if(toAdd != 0 && points < maxPoints){
                  SoundUtils.playSongToPlayer(gui.getPlayer(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, (.8f+((float)curPoints/maxPoints)));
                  if(curPoints == maxPoints){
                     SoundUtils.playSongToPlayer(gui.getPlayer(), SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 2f);
                  }
                  if(curPoints >= 512) ArcanaAchievements.grant(gui.getPlayer(),ArcanaAchievements.ARCHLICH.id);
                  if(curPoints >= 1024) ArcanaAchievements.grant(gui.getPlayer(),ArcanaAchievements.POWER_OVERWHELMING.id);
               }
            }
            prevStone = true;
         }else{
            blockEntity.setSoulstone(ItemStack.EMPTY);
            points += inv.getStack(1).getCount() * ratio;
   
            List<ItemStack> drops = new ArrayList<>();
            if(points > 0){
               while(points/ratio > 64){
                  ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
                  dropItem.setCount(64);
                  drops.add(dropItem.copy());
                  points -= 64*ratio;
               }
               ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
               dropItem.setCount(points/ratio);
               drops.add(dropItem.copy());
            }
   
            for(ItemStack stack : drops){
               if(!stack.isEmpty()){
         
                  ItemEntity itemEntity;
                  boolean bl = gui.getPlayer().getInventory().insertStack(stack);
                  if(!bl || !stack.isEmpty()){
                     itemEntity = gui.getPlayer().dropItem(stack, false);
                     if(itemEntity == null) continue;
                     itemEntity.resetPickupDelay();
                     itemEntity.setOwner(gui.getPlayer().getUuid());
                     continue;
                  }
                  stack.setCount(1);
                  itemEntity = gui.getPlayer().dropItem(stack, false);
                  if(itemEntity != null){
                     itemEntity.setDespawnImmediately();
                  }
               }
            }
            
            if(prevStone){
               SoundUtils.playSongToPlayer(gui.getPlayer(), SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, .8f);
            }
            
            blockEntity.setPoints(0);
            blockEntity.resetStats();
            blockEntity.setSpentPoints(0);
   
            prevStone = false;
         }
         
         gui.build();
         setUpdating();
         inv.setStack(1,extraPoints);
         blockEntity.markDirty();
         
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
