package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.UUID;

public class ContinuumAnchorBlockEntity extends RandomizableContainerBlockEntity implements PolymerObject, ArcanaBlockEntity, WorldlyContainer {
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private ArcanaSkin skin;
   private String customName;
   private int fuel;
   private boolean active;
   private WatchedContainer inventory = new WatchedContainer(getContainerSize());
   
   public ContinuumAnchorBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, pos, state);
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, ArcanaSkin skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
      this.fuel = 0;
      this.active = false;
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
   
   public ArcanaSkin getSkin(){
      return skin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ItemStack getFuelStack(){
      return inventory.getItem(0);
   }
   
   public int getFuel(){
      return fuel;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.CONTINUUM_ANCHOR;
   }
   
   public boolean interact(Player player, ItemStack stack){
      if(!inventory.isEmpty() && stack.isEmpty()){ // Remove fuel
         fuel = 0;
         ItemStack returnStack = getFuelStack().copy();
         inventory.setItem(0, ItemStack.EMPTY);
         setChanged();
         if(!player.addItem(returnStack)){
            ItemEntity itemEntity = player.drop(returnStack, false);
            if(itemEntity == null) return true;
            itemEntity.setNoPickUpDelay();
            itemEntity.setTarget(player.getUUID());
         }
         return true;
      }
      
      //Add Fuel
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(inventory.isEmpty() && arcanaItem instanceof ExoticMatter matter){
         fuel = EnergyItem.getEnergy(stack);
         inventory.addItem(stack.copy());
         player.getInventory().removeItem(stack);
         setChanged();
         return true;
      }
      return false;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ContinuumAnchorBlockEntity anchor){
         anchor.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTickCount() % 5 == 0){ // Anchor only ticks redstone and load update every quarter second
         boolean prevActive = active;
         active = !serverWorld.hasNeighborSignal(worldPosition) && fuel > 0;
         
         if(fuel <= 0 && !inventory.isEmpty()){
            inventory.clearContent();
         }
         
         if(active && serverWorld.getServer().getTickCount() % 20 == 0){
            int lvl = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.TEMPORAL_RELATIVITY);
            double efficiency = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.CONTINUUM_ANCHOR_EFFICIENCY_PER_LVL).get(lvl);
            if(serverWorld.getRandom().nextFloat() >= efficiency){
               fuel = Math.max(0, fuel - 1);
               
               if(ArcanaItemUtils.identifyItem(getFuelStack()) instanceof ExoticMatter matter){
                  matter.setFuel(getFuelStack(), fuel);
                  setChanged();
               }
            }
            
            if(crafterId != null && !crafterId.isEmpty()){
               UUID parsedId = AlgoUtils.getUUID(crafterId);
               ArcanaPlayerData profile = ArcanaNovum.data(parsedId);
               ArcanaAchievements.progress(parsedId, ArcanaAchievements.TIMEY_WIMEY, 1);
               if(serverWorld.getServer().getTickCount() % 1200 == 0)
                  profile.addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_CONTINUUM_ANCHOR_PER_MINUTE));
            }
         }
         int fuelMarks = (int) Math.min(Math.ceil(4.0 * fuel / 600000.0), 4);
         BlockState oldState = level.getBlockState(worldPosition);
         BlockState newState = oldState.setValue(ContinuumAnchor.ContinuumAnchorBlock.ACTIVE, active).setValue(ContinuumAnchor.ContinuumAnchorBlock.CHARGES, fuelMarks);
         if(oldState != newState){
            level.setBlock(worldPosition, newState, Block.UPDATE_ALL);
         }
         
         if(prevActive && !active){ // Power Down
            ArcanaNovum.removeActiveAnchor(serverWorld, worldPosition);
            SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 1, 1.5f);
         }else if(!prevActive && active){ // Power Up
            ArcanaNovum.addActiveAnchor(serverWorld, worldPosition);
            SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 0.7f);
            ContinuumAnchor.loadChunks(serverWorld, ChunkPos.containing(worldPosition));
         }
         
         if(prevActive != active || oldState != newState){
            this.setChanged();
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.active){
         ContinuumAnchor.loadChunks(serverWorld, ChunkPos.containing(worldPosition));
         ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
      }
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState oldState){
      super.preRemoveSideEffects(pos, oldState);
      
      if(!(this.level instanceof ServerLevel serverWorld)) return;
      ArcanaNovum.removeActiveAnchor(serverWorld, pos);
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = ArcanaSkin.getSkinFromString(view.getStringOr(ArcanaBlockEntity.SKIN_TAG, ""));
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.fuel = view.getIntOr("fuel", 0);
      this.active = view.getBooleanOr("active", false);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new WatchedContainer(getContainerSize());
      if(!this.tryLoadLootTable(view)){
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC, this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG, this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG, this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME, this.customName == null ? "" : this.customName);
      view.putString(ArcanaBlockEntity.SKIN_TAG, this.skin == null ? "" : this.skin.getSerializedName());
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG, this.origin);
      view.putInt("fuel", this.fuel);
      view.putBoolean("active", this.active);
      if(!this.trySaveLootTable(view)){
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
   }
   
   public Container getInventory(){
      return this.inventory;
   }
   
   @Override
   protected Component getDefaultName(){
      return Component.literal("Continuum Anchor");
   }
   
   @Override
   protected NonNullList<ItemStack> getItems(){
      return this.inventory.getItems();
   }
   
   @Override
   protected void setItems(NonNullList<ItemStack> list){
      for(int i = 0; i < list.size(); i++){
         this.inventory.setItem(i, list.get(i));
      }
   }
   
   @Override
   protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory){
      return null;
   }
   
   @Override
   public int[] getSlotsForFace(Direction side){
      return new int[0];
   }
   
   @Override
   public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir){
      return false;
   }
   
   @Override
   public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir){
      return false;
   }
   
   @Override
   public int getContainerSize(){
      return 1;
   }
   
   @Override
   public void setChanged(){
      super.setChanged();
      this.inventory.setChanged();
   }
}
