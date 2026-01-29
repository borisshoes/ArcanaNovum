package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.gui.radiantfletchery.RadiantFletcheryGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class RadiantFletcheryBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, PolymerObject, ContainerListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   private boolean updating;
   private SimpleContainer inventory = new SimpleContainer(getContainerSize());
   private final int[] efficiency = {24,32,40,48,56,64};
   private final Set<ServerPlayer> watchingPlayers = new HashSet<>();
   
   public RadiantFletcheryBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.RADIANT_FLETCHERY).getMultiblock();
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof RadiantFletcheryBlockEntity fletchery){
         fletchery.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTickCount() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld, worldPosition) != null;
      }
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Tuple<>(this,this));
      }
      
      watchingPlayers.removeIf(player -> player.containerMenu == player.inventoryMenu);
   }
   
   public int getPotionRatio(){
      int lvl = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.ALCHEMICAL_EFFICIENCY);
      return efficiency[lvl];
   }
   
   public void openGui(ServerPlayer player){
      RadiantFletcheryGui gui = new RadiantFletcheryGui(player,this);
      gui.buildGui();
      gui.open();
      watchingPlayers.add(player);
   }
   
   public void removePlayer(ServerPlayer player){
      watchingPlayers.remove(player);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.RADIANT_FLETCHERY).getCheckOffset()),null);
   }
   
   public Container getInventory(){
      return this.inventory;
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
      return ArcanaRegistry.RADIANT_FLETCHERY;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.inventory = new SimpleContainer(getContainerSize());
      this.inventory.addListener(this);
      if (!this.tryLoadLootTable(view)) {
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      if (!this.trySaveLootTable(view)) {
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
   }
   
   @Override
   protected NonNullList<ItemStack> getItems(){
      return this.inventory.getItems();
   }
   
   @Override
   protected void setItems(NonNullList<ItemStack> list){
      for(int i = 0; i < list.size(); i++){
         this.inventory.setItem(i,list.get(i));
      }
   }
   
   @Override
   protected Component getDefaultName(){
      return Component.literal("Radiant Fletchery");
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
      return 3;
   }
   
   @Override
   public void containerChanged(Container inv){
      if(!updating){
         updating = true;
         ItemStack arrowStack = inv.getItem(0);
         ItemStack potionStack = inv.getItem(1);
         ItemStack outputStack = inv.getItem(2);
         int potionRatio = getPotionRatio();
         ItemStack resultStack = new ItemStack(Items.TIPPED_ARROW);
         resultStack.set(DataComponents.POTION_CONTENTS,potionStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
         resultStack.setCount(potionRatio);
         
         while(arrowStack.getCount() >= potionRatio && potionStack.getCount() >= 1 && (((outputStack.getMaxStackSize()-outputStack.getCount()) >= potionRatio && ItemStack.isSameItemSameComponents(outputStack,resultStack)) || outputStack.isEmpty())){
            arrowStack.shrink(potionRatio);
            inv.setItem(0,arrowStack.isEmpty() ? ItemStack.EMPTY : arrowStack);
            
            potionStack.shrink(1);
            inv.setItem(1,potionStack.isEmpty() ? ItemStack.EMPTY : potionStack);
            
            if(outputStack.isEmpty()){
               outputStack = resultStack;
               inv.setItem(2,resultStack);
            }else{
               outputStack.grow(potionRatio);
               inv.setItem(2,outputStack);
            }
            
            watchingPlayers.forEach(player -> ArcanaAchievements.grant(player,ArcanaAchievements.FINALLY_USEFUL_2));
            watchingPlayers.forEach(player -> ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaRegistry.XP_RADIANT_FLETCHERY_TIP_ARROWS)));
         }
         
         //Update gui
         setChanged();
         updating = false;
      }
   }
}
