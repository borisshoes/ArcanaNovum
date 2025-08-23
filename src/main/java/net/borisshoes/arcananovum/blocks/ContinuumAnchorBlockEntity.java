package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.AnchorTimeLoginCallback;
import net.borisshoes.arcananovum.callbacks.XPLoginCallback;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class ContinuumAnchorBlockEntity extends LootableContainerBlockEntity implements PolymerObject, ArcanaBlockEntity, SidedInventory, InventoryChangedListener {
   private static final double[] anchorEfficiency = {0,.05,.1,.15,.2,.5};
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private int fuel;
   private boolean active;
   private SimpleInventory inventory = new SimpleInventory(size());
   
   public ContinuumAnchorBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, pos, state);
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
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
   
   public boolean isSynthetic(){
      return synthetic;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ItemStack getFuelStack(){
      return inventory.getStack(0);
   }
   
   public int getFuel(){
      return fuel;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.CONTINUUM_ANCHOR;
   }
   
   public boolean interact(PlayerEntity player, ItemStack stack){
      if(!inventory.isEmpty() && stack.isEmpty()){ // Remove fuel
         fuel = 0;
         ItemStack returnStack = getFuelStack().copy();
         inventory.setStack(0,ItemStack.EMPTY);
         markDirty();
         if(!player.giveItemStack(returnStack)){
            ItemEntity itemEntity = player.dropItem(returnStack, false);
            if(itemEntity == null) return true;
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
         }
         return true;
      }
      
      //Add Fuel
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(inventory.isEmpty() && arcanaItem instanceof ExoticMatter matter){
         fuel = matter.getEnergy(stack);
         inventory.addStack(stack.copy());
         player.getInventory().removeOne(stack);
         markDirty();
         return true;
      }
      return false;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof ContinuumAnchorBlockEntity anchor){
         anchor.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 5 == 0){ // Anchor only ticks redstone and load update every quarter second
         boolean prevActive = active;
         active = !serverWorld.isReceivingRedstonePower(pos) && fuel > 0;
         
         if(fuel <= 0 && !inventory.isEmpty()){
            inventory.clear();
         }
         
         if(active && serverWorld.getServer().getTicks() % 20 == 0){
            int lvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.TEMPORAL_RELATIVITY.id);
            if(Math.random() >= anchorEfficiency[lvl]){
               fuel = Math.max(0, fuel - 1);
               
               if(ArcanaItemUtils.identifyItem(getFuelStack()) instanceof ExoticMatter matter){
                  matter.setFuel(getFuelStack(), fuel);
                  markDirty();
               }
            }
            
            if(crafterId != null && !crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(MiscUtils.getUUID(crafterId));
               if(player == null){
                  ArcanaNovum.addLoginCallback(new AnchorTimeLoginCallback(serverWorld.getServer(),crafterId,1));
                  if(serverWorld.getServer().getTicks() % 1200 == 0) ArcanaNovum.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,ArcanaConfig.getInt(ArcanaRegistry.CONTINUUM_ANCHOR_PER_MINUTE)));
               }else{
                  ArcanaAchievements.progress(player,ArcanaAchievements.TIMEY_WIMEY.id, 1);
                  if(serverWorld.getServer().getTicks() % 1200 == 0) ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.CONTINUUM_ANCHOR_PER_MINUTE));
               }
            }
         }
         int fuelMarks = (int)Math.min(Math.ceil(4.0*fuel/600000.0),4);
         BlockState blockState = world.getBlockState(pos).with(ContinuumAnchor.ContinuumAnchorBlock.ACTIVE,active).with(ContinuumAnchor.ContinuumAnchorBlock.CHARGES,fuelMarks);
         world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
         
         if(prevActive && !active){ // Power Down
            ArcanaNovum.removeActiveAnchor(serverWorld,pos);
            SoundUtils.playSound(serverWorld,pos,SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS,1,1.5f);
         }else if(!prevActive && active){ // Power Up
            ArcanaNovum.addActiveAnchor(serverWorld,pos);
            SoundUtils.playSound(serverWorld,pos,SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS,1,0.7f);
            ContinuumAnchor.loadChunks(serverWorld,new ChunkPos(pos));
         }
         
         this.markDirty();
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.active){
         ContinuumAnchor.loadChunks(serverWorld,new ChunkPos(pos));
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   @Override
   public void onBlockReplaced(BlockPos pos, BlockState oldState){
      super.onBlockReplaced(pos, oldState);
      
      if(!(this.world instanceof ServerWorld serverWorld)) return;
      ArcanaNovum.removeActiveAnchor(serverWorld, pos);
   }
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString("arcanaUuid", "");
      this.crafterId = view.getString("crafterId", "");
      this.customName = view.getString("customName", "");
      this.synthetic = view.getBoolean("synthetic", false);
      this.fuel = view.getInt("fuel", 0);
      this.active = view.getBoolean("active", false);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new SimpleInventory(size());
      if (!this.readLootTable(view)) {
         Inventories.readData(view, this.inventory.getHeldStacks());
      }
   }
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString("arcanaUuid",this.uuid == null ? "" : this.uuid);
      view.putString("crafterId",this.crafterId == null ? "" : this.crafterId);
      view.putString("customName",this.customName == null ? "" : this.customName);
      view.putBoolean("synthetic",this.synthetic);
      view.putInt("fuel",this.fuel);
      view.putBoolean("active",this.active);
      if (!this.writeLootTable(view)) {
         Inventories.writeData(view, this.inventory.getHeldStacks());
      }
   }
   
   public Inventory getInventory(){
      return this.inventory;
   }
   
   @Override
   protected Text getContainerName(){
      return Text.literal("Continuum Anchor");
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
      return 1;
   }
   
   @Override
   public void onInventoryChanged(Inventory sender){
   
   }
}
