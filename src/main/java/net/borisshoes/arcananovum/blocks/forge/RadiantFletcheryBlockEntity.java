package net.borisshoes.arcananovum.blocks.forge;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class RadiantFletcheryBlockEntity extends LootableContainerBlockEntity implements SidedInventory, PolymerObject, InventoryChangedListener, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private final Multiblock multiblock;
   private boolean assembled;
   private boolean seenForge;
   private boolean updating;
   private SimpleInventory inventory = new SimpleInventory(size());
   private final int[] efficiency = {24,32,40,48,56,64};
   private final Set<ServerPlayerEntity> watchingPlayers = new HashSet<>();
   
   public RadiantFletcheryBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.RADIANT_FLETCHERY_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.RADIANT_FLETCHERY).getMultiblock();
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof RadiantFletcheryBlockEntity fletchery){
         fletchery.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 10 == 0){
         this.assembled = multiblock.matches(getMultiblockCheck());
         this.seenForge = StarlightForge.findActiveForge(serverWorld,pos) != null;
      }
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.assembled && this.seenForge){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
      
      watchingPlayers.removeIf(player -> player.currentScreenHandler == player.playerScreenHandler);
   }
   
   public int getPotionRatio(){
      int lvl = ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.ALCHEMICAL_EFFICIENCY.id);
      return efficiency[lvl];
   }
   
   public void openGui(ServerPlayerEntity player){
      RadiantFletcheryGui gui = new RadiantFletcheryGui(player,this);
      gui.buildGui();
      gui.open();
      watchingPlayers.add(player);
   }
   
   public void removePlayer(ServerPlayerEntity player){
      watchingPlayers.remove(player);
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld,pos,serverWorld.getBlockState(pos),new BlockPos(((MultiblockCore) ArcanaRegistry.RADIANT_FLETCHERY).getCheckOffset()),null);
   }
   
   public Inventory getInventory(){
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
   
   public boolean isSynthetic(){
      return synthetic;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.RADIANT_FLETCHERY;
   }
   
   @Override
   public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
      super.readNbt(nbt, registryLookup);
      if(nbt.contains("arcanaUuid")){
         this.uuid = nbt.getString("arcanaUuid", "");
      }
      if(nbt.contains("crafterId")){
         this.crafterId = nbt.getString("crafterId", "");
      }
      if(nbt.contains("customName")){
         this.customName = nbt.getString("customName", "");
      }
      if(nbt.contains("synthetic")){
         this.synthetic = nbt.getBoolean("synthetic", false);
      }
      this.inventory = new SimpleInventory(size());
      this.inventory.addListener(this);
      if(!this.readLootTable(nbt) && nbt.contains("Items")){
         Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompoundOrEmpty("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug, augCompound.getInt(key, 0));
         }
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
      super.writeNbt(nbt, registryLookup);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("arcanaAugments",augsCompound);
      }
      if(this.uuid != null){
         nbt.putString("arcanaUuid",this.uuid);
      }
      if(this.crafterId != null){
         nbt.putString("crafterId",this.crafterId);
      }
      if(this.customName != null){
         nbt.putString("customName",this.customName);
      }
      nbt.putBoolean("synthetic",this.synthetic);
      if(!this.writeLootTable(nbt)){
         Inventories.writeNbt(nbt, this.inventory.getHeldStacks(), false, registryLookup);
      }
   }
   
   @Override
   protected DefaultedList<ItemStack> getHeldStacks(){
      return this.inventory.getHeldStacks();
   }
   
   @Override
   protected void setHeldStacks(DefaultedList<ItemStack> list){
      for(int i = 0; i < list.size(); i++){
         this.inventory.setStack(i,list.get(i));
      }
   }
   
   @Override
   protected Text getContainerName(){
      return Text.literal("Radiant Fletchery");
   }
   
   @Override
   protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory){
      return null;
   }
   
   @Override
   public int[] getAvailableSlots(Direction side){
      return new int[0];
   }
   
   @Override
   public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
      return false;
   }
   
   @Override
   public boolean canExtract(int slot, ItemStack stack, Direction dir){
      return false;
   }
   
   @Override
   public int size(){
      return 3;
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         ItemStack arrowStack = inv.getStack(0);
         ItemStack potionStack = inv.getStack(1);
         ItemStack outputStack = inv.getStack(2);
         int potionRatio = getPotionRatio();
         ItemStack resultStack = new ItemStack(Items.TIPPED_ARROW);
         resultStack.set(DataComponentTypes.POTION_CONTENTS,potionStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT));
         resultStack.setCount(potionRatio);
         
         while(arrowStack.getCount() >= potionRatio && potionStack.getCount() >= 1 && (((outputStack.getMaxCount()-outputStack.getCount()) >= potionRatio && ItemStack.areItemsAndComponentsEqual(outputStack,resultStack)) || outputStack.isEmpty())){
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
            
            watchingPlayers.forEach(player -> ArcanaAchievements.grant(player,ArcanaAchievements.FINALLY_USEFUL_2.id));
            watchingPlayers.forEach(player -> ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.RADIANT_FLETCHERY_TIP_ARROWS)));
         }
         
         //Update gui
         markDirty();
         updating = false;
      }
   }
}
