package net.borisshoes.arcananovum.core;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.blocks.EnderCrate;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.blocks.Itineranteur;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltar;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGateway;
import net.borisshoes.arcananovum.blocks.astralgateway.AstralGatewayBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForge;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
   
   static ItemStack getBlockEntityAsItem(ArcanaBlockEntity arcanaBlockEntity, Level world){
      return getBlockEntityAsItem(arcanaBlockEntity,world,null);
   }
   
   static ItemStack getBlockEntityAsItem(ArcanaBlockEntity arcanaBlockEntity, Level world, @Nullable ItemStack stack){
      String uuid = arcanaBlockEntity.getUuid();
      if(uuid == null) uuid = UUID.randomUUID().toString();
      CompoundTag augmentsTag = new CompoundTag();
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
         stack.set(DataComponents.CUSTOM_NAME, Component.literal(arcanaBlockEntity.getCustomArcanaName()));
      }
      
      if(arcanaBlockEntity instanceof ArcaneSingularityBlockEntity singularity){
         ArcanaItem.putProperty(stack, ArcaneSingularity.BOOKS_TAG,singularity.saveBooks(world.registryAccess()));
      }
      if(arcanaBlockEntity instanceof StarpathAltarBlockEntity altar){
         ArcanaItem.putProperty(stack, StarpathAltar.TARGETS_TAG,altar.writeTargets());
      }
      if(arcanaBlockEntity instanceof StarlightForgeBlockEntity forge){
         ArcanaItem.putProperty(stack, StarlightForge.SEED_USES_TAG,forge.getSeedUses());
      }
      if(arcanaBlockEntity instanceof AstralGatewayBlockEntity gateway){
         ArcanaItem.putProperty(stack, AstralGateway.WAYSTONES_TAG,gateway.saveStones(world.registryAccess()));
         ArcanaItem.putProperty(stack, AstralGateway.STARDUST_TAG,gateway.getStardust());
      }
      if(arcanaBlockEntity instanceof EnderCrateBlockEntity crate){
         EnderCrateChannel channel = crate.getChannel();
         ArcanaItem.putProperty(stack, EnderCrate.CHANNEL_TAG, EnderCrate.colorsToTag(channel.getColors()));
         ArcanaItem.putProperty(stack, EnderCrate.LOCK_TAG, channel.isLocked() ? channel.getIdLock().toString() : "");
      }
      if(arcanaBlockEntity instanceof ItineranteurBlockEntity itineranteur){
         ArcanaItem.putProperty(stack, Itineranteur.COLOR_TAG,itineranteur.getBlockState().getValue(Itineranteur.ItineranteurBlock.TYPE).getId());
      }
      
      arcanaItem.buildItemLore(stack,world.getServer());
      
      return stack;
   }
}
