package net.borisshoes.arcananovum.gui.spawnerinfuser;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.gui.shulkercore.ShulkerCoreGui;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.SpawnerInfuser;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SpawnerInfuserInventoryListener implements InventoryChangedListener {
   //private final SpawnerInfuser infuser;
   private final SpawnerInfuserGui gui;
   private final MagicBlock block;
   private final World world;
   private boolean updating = false;
   private boolean prevStone = false;
   
   public SpawnerInfuserInventoryListener(SpawnerInfuserGui gui, MagicBlock block, World world){
      //this.infuser = infuser;
      this.gui = gui;
      this.block = block;
      this.world = world;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         NbtCompound blockData = block.getData();
         
         ItemStack soulstoneSlot = inv.getStack(0);
         ItemStack extraPoints = ItemStack.EMPTY;
         int points = blockData.getInt("points");
         
         if(!soulstoneSlot.isEmpty()){
            blockData.put("soulstone", soulstoneSlot.writeNbt(new NbtCompound()));
            if(!prevStone){
               SoundUtils.soulSounds(gui.getPlayer(),1,20);
            }
   
            ItemStack pointsSlot = inv.getStack(1);
            if(!pointsSlot.isEmpty()){
               int maxPoints = SpawnerInfuser.pointsFromTier[Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot))];
               int toAdd = pointsSlot.getCount();
               
               if(maxPoints-points < toAdd){
                  blockData.putInt("points",maxPoints);
                  extraPoints = pointsSlot.copy();
                  extraPoints.setCount(toAdd-(maxPoints-points));
               }else{
                  blockData.putInt("points",points+toAdd);
               }
               if(toAdd != 0 && points < maxPoints){
                  SoundUtils.playSongToPlayer(gui.getPlayer(), SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, (.8f+((float)blockData.getInt("points")/maxPoints)));
                  if(blockData.getInt("points") == maxPoints){
                     SoundUtils.playSongToPlayer(gui.getPlayer(), SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 2f);
                  }
               }
            }
            prevStone = true;
         }else{
            blockData.put("soulstone", new NbtCompound());
            points += inv.getStack(1).getCount();
   
            List<ItemStack> drops = new ArrayList<>();
            if(points > 0){
               while(points > 64){
                  ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
                  dropItem.setCount(64);
                  drops.add(dropItem.copy());
                  points -= 64;
               }
               ItemStack dropItem = new ItemStack(SpawnerInfuser.pointsItem);
               dropItem.setCount(points);
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
            
            blockData.putInt("points",0);
            NbtCompound stats = new NbtCompound();
            stats.putShort("MinSpawnDelay", (short)200);
            stats.putShort("MaxSpawnDelay", (short)800);
            stats.putShort("SpawnCount", (short)4);
            stats.putShort("MaxNearbyEntities", (short)6);
            stats.putShort("RequiredPlayerRange", (short)16);
            stats.putShort("SpawnRange", (short)4);
            blockData.put("stats",stats);
            blockData.putInt("SpentPoints",0);
   
            prevStone = false;
         }
         
         gui.build();
         setUpdating();
         inv.setStack(1,extraPoints);
         
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
