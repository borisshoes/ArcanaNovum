package net.borisshoes.arcananovum.core;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltar;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public interface ArcanaBlockEntity {
   String AUGMENT_TAG = "arcanaAugments";
   String ARCANA_UUID_TAG = "arcanaUuid";
   String CRAFTER_ID_TAG = "crafterId";
   String CUSTOM_NAME = "customName";
   String ORIGIN_TAG = "synthetic";
   
   TreeMap<ArcanaAugment, Integer> getAugments();
   
   String getCrafterId();
   
   String getUuid();
   
   int getOrigin();
   
   String getCustomArcanaName();
   
   void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName);
   
   ArcanaItem getArcanaItem();
   
   default boolean isAssembled(){
      return true;
   }
   
   static ItemStack getBlockEntityAsItem(ArcanaBlockEntity arcanaBlockEntity, World world){
      return getBlockEntityAsItem(arcanaBlockEntity,world,null);
   }
   
   static ItemStack getBlockEntityAsItem(ArcanaBlockEntity arcanaBlockEntity, World world, @Nullable ItemStack stack){
      String uuid = arcanaBlockEntity.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      NbtCompound augmentsTag = new NbtCompound();
      if(arcanaBlockEntity.getAugments() != null){
         for(Map.Entry<ArcanaAugment, Integer> entry : arcanaBlockEntity.getAugments().entrySet()){
            augmentsTag.putInt(entry.getKey().id, entry.getValue());
         }
      }else{
         augmentsTag = null;
      }
      
      ArcanaItem arcanaItem = arcanaBlockEntity.getArcanaItem();
      
      if(stack != null){
         arcanaItem.initializeArcanaTag(stack,false);
      }else{
         stack = arcanaItem.getNewItem();
      }
      
      stack = arcanaItem.addCrafter(stack, arcanaBlockEntity.getCrafterId(), arcanaBlockEntity.getOrigin(),world.getServer());
      
      if(augmentsTag != null){
         ArcanaItem.putProperty(stack, ArcanaItem.AUGMENTS_TAG,augmentsTag);
      }
      
      ArcanaItem.putProperty(stack, ArcanaItem.UUID_TAG,uuid);
      
      if(arcanaBlockEntity.getCustomArcanaName() != null && !arcanaBlockEntity.getCustomArcanaName().isEmpty()){
         stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(arcanaBlockEntity.getCustomArcanaName()));
      }
      
      if(arcanaBlockEntity instanceof ArcaneSingularityBlockEntity singularity){
         ArcanaItem.putProperty(stack, ArcaneSingularity.BOOKS_TAG,singularity.saveBooks(world.getRegistryManager()));
      }
      if(arcanaBlockEntity instanceof StarpathAltarBlockEntity altar){
         ArcanaItem.putProperty(stack, StarpathAltar.TARGETS_TAG,altar.writeTargets());
      }
      if(arcanaBlockEntity instanceof StarlightForgeBlockEntity forge){
         ArcanaItem.putProperty(stack, StarlightForge.SEED_USES_TAG,forge.getSeedUses());
      }
      
      arcanaItem.buildItemLore(stack,world.getServer());
      
      return stack;
   }
}
