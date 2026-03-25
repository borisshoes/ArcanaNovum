package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannel;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.stream.IntStream;

public class EnderCrateBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, ContainerListener, PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private EnderCrateChannel channel;
   private int slotCount = 27;
   private int[] slots = IntStream.range(0, slotCount).toArray();
   
   public EnderCrateBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.ENDER_CRATE_BLOCK_ENTITY, blockPos, blockState);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      
      this.slotCount = 27 + 9*ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ENDER_BANDWIDTH);
      this.slots = IntStream.range(0, slotCount).toArray();
   }
   
   public void setChannel(EnderCrateChannel channel){
      this.channel = channel;
      this.setChanged();
   }
   
   public EnderCrateChannel getChannel(){
      return channel;
   }
   
   public int getBandwidth(){
      return ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ENDER_BANDWIDTH);
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public String getCrafterId(){
      return crafterId;
   }
   
   public String getUuid(){
      return uuid;
   }
   
   public int getOrigin(){
      return origin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.ENDER_CRATE;
   }
   
   @Override
   protected Component getDefaultName(){
      return ArcanaRegistry.ENDER_CRATE.getTranslatedName();
   }
   
   @Override
   protected NonNullList<ItemStack> getItems(){
      return this.channel == null ? NonNullList.create() : this.channel.getInventory().getItems();
   }
   
   @Override
   protected void setItems(NonNullList<ItemStack> list){
      if(this.channel == null) return;
      for(int i = 0; i < list.size(); i++){
         this.channel.getInventory().setItem(i,list.get(i));
      }
   }
   
   @Override
   protected AbstractContainerMenu createMenu(int i, Inventory inventory){
      return null;
   }
   
   @Override
   public int[] getSlotsForFace(Direction direction){
      return slots;
   }
   
   @Override
   public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction){
      if(!this.channel.isLocked() && itemStack.is(ArcanaRegistry.ALL_ARCANA_ITEMS)) return false;
      return true;
   }
   
   @Override
   public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction){
      return true;
   }
   
   @Override
   public int getContainerSize(){
      return slotCount;
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState){}
   
   @Override
   public void containerChanged(Container container){
   
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      view.read("channel", EnderCrateChannel.CODEC).ifPresent(data -> {
         // Use the deserialized channel's key (colors + lock) to get the shared channel from global storage
         this.channel = EnderCrateChannels.getChannel(data.getIdLock(), data.getColors());
      });
      
      this.slotCount = 27 + 9*ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.ENDER_BANDWIDTH);
      this.slots = IntStream.range(0, slotCount).toArray();
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.storeNullable("channel", EnderCrateChannel.CODEC, this.channel);
   }
}
