package net.borisshoes.arcananovum.core;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public interface MagicBlockEntity {
   TreeMap<ArcanaAugment, Integer> getAugments();
   
   String getCrafterId();
   
   String getUuid();
   
   boolean isSynthetic();
   
   String getCustomArcanaName();
   
   void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName);
   
   MagicItem getMagicItem();
   
   default boolean isAssembled(){
      return true;
   }
   
   default ItemStack getBlockEntityAsItem(BlockEntity be, World world){
      if(!(be instanceof MagicBlockEntity magicBlock)){
         return ItemStack.EMPTY;
      }
      
      String uuid = magicBlock.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      NbtCompound augmentsTag = new NbtCompound();
      if(magicBlock.getAugments() != null){
         for(Map.Entry<ArcanaAugment, Integer> entry : magicBlock.getAugments().entrySet()){
            augmentsTag.putInt(entry.getKey().id, entry.getValue());
         }
      }else{
         augmentsTag = null;
      }
      
      MagicItem magicItem = magicBlock.getMagicItem();
      ItemStack newItem = new ItemStack(magicItem.getItem());
      newItem.setNbt(magicItem.getNewItem().getNbt());
      ItemStack drop = magicItem.addCrafter(newItem, magicBlock.getCrafterId(), magicBlock.isSynthetic(),world.getServer());
      NbtCompound dropNbt = drop.getNbt();
      NbtCompound magicTag = dropNbt.getCompound("arcananovum");
      if(augmentsTag != null) {
         magicTag.put("augments",augmentsTag);
         magicItem.buildItemLore(drop,world.getServer());
      }
      magicTag.putString("UUID",uuid);
      
      if(magicBlock.getCustomArcanaName() != null && !magicBlock.getCustomArcanaName().isEmpty()){
         dropNbt.getCompound("display").putString("Name",magicBlock.getCustomArcanaName());
      }
      
      if(magicBlock instanceof ArcaneSingularityBlockEntity singularity){
         magicTag.put("books",singularity.writeBooks());
      }
      if(magicBlock instanceof StarpathAltarBlockEntity altar){
         magicTag.put("targets",altar.writeTargets());
      }
      
      return drop;
   }
}
