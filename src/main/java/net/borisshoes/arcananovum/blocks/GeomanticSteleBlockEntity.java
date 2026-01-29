package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.gui.geomanticstele.GeomanticSteleGui;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.borisshoes.arcananovum.items.charms.CleansingCharm;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.items.charms.WildGrowthCharm;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class GeomanticSteleBlockEntity extends RandomizableContainerBlockEntity implements PolymerObject, WorldlyContainer, ArcanaBlockEntity {
   
   private static final Map<ResourceKey<Level>, Map<Long, Set<SteleZone>>> ACTIVE_STELES = new HashMap<>();
   private static final int KEEP_ALIVE = 21;
   private static final double[] RANGE_MODS = new double[]{1.0,1.5,2.0,3.0};
   
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   private final Multiblock multiblock;
   private double rangeMod = 1;
   private double rangeX, rangeY, rangeZ;
   private boolean assembled, wasActive;
   private ElementHolder hologram;
   private HolderAttachment attachment;
   private int interactCooldown = 0;
   private SteleZone currentZone;
   private SimpleContainer inventory = new SimpleContainer(getContainerSize());
   
   public GeomanticSteleBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.GEOMANTIC_STELE_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.GEOMANTIC_STELE).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      this.rangeMod = RANGE_MODS[ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.GEOLITHIC_AMPLIFICATION)];
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof GeomanticSteleBlockEntity stele){
         stele.tick(world, blockPos, blockState);
      }
   }
   
   private void tick(Level world, BlockPos blockPos, BlockState blockState){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      boolean active = blockState.getValue(GeomanticStele.GeomanticSteleBlock.ACTIVE);
      
      if(serverWorld.getServer().getTickCount() % 20 == 0){
         assembled = multiblock.matches(getMultiblockCheck());
         boolean hasRedstone = serverWorld.hasNeighborSignal(blockPos);
         boolean hasItem = !getItem().isEmpty();
         boolean shouldBeActive = assembled && hasItem && hasRedstone;
         
         if(active ^ shouldBeActive){
            serverWorld.setBlock(blockPos, blockState.setValue(GeomanticStele.GeomanticSteleBlock.ACTIVE, shouldBeActive), Block.UPDATE_ALL);
            active = shouldBeActive;
         }
         
         if(active){
            ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
            Set<BlockPos> blocks = calculateBlocks();
            if(!blocks.isEmpty()){
               if(currentZone != null){
                  // Refresh existing zone
                  currentZone.refresh();
               }else{
                  // Register new zone
                  currentZone = new SteleZone(blocks, serverWorld.dimension(), this);
                  registerZone(currentZone);
               }
            }else if(currentZone != null){ // No blocks, unregister zone
               unregisterZone(currentZone);
               currentZone = null;
            }
         }
         
         if(assembled && hasItem){
            Vec3 pos = getHologramPos();
            Player player = serverWorld.getNearestPlayer(pos.x(),pos.y(),pos.z(), 32, entity -> !entity.isSpectator());
            if(player != null && hologram == null){
               hologram = getNewHologram(serverWorld);
               attachment = ChunkAttachment.ofTicking(this.hologram,serverWorld,pos);
            }else if(player == null && hologram != null){
               hologram.destroy();
               hologram = null;
            }
         }
      }
      
      if(active){
         ItemStack item = getItem();
         if(ArcanaItemUtils.identifyItem(item) instanceof GeomanticStele.Interaction interaction){
            interaction.steleTick(serverWorld,this,item,getRange());
         }
      }
      
      if(active && !wasActive){
         //SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS,1,1.5f);
         wasActive = true;
      }else if(!active && wasActive){
         //SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS,1,1.5f);
         wasActive = false;
      }
   }
   
   private Vec3 getHologramPos(){
      return this.getBlockPos().getCenter();
   }
   
   private ElementHolder getNewHologram(ServerLevel world){
      ItemDisplayElement icon = new ItemDisplayElement(getItem());
      InteractionElement click = new InteractionElement(new VirtualElement.InteractionHandler(){
         public void click(ServerPlayer player, ItemStack stack){
            if(interactCooldown == 0){
               GeomanticSteleBlockEntity.this.interact(player,stack);
               interactCooldown = 5;
            }
         }
         
         @Override
         public void interact(ServerPlayer player, InteractionHand hand){
            click(player,player.getItemInHand(hand));
         }
         
         @Override
         public void interactAt(ServerPlayer player, InteractionHand hand, Vec3 pos){
            click(player,player.getItemInHand(hand));
         }
      });
      click.setSize(0.75f,0.75f);
      
      ElementHolder holder = new ElementHolder() {
         ServerLevel serverLevel = world;
         private final ItemDisplayElement iconElem = icon;
         private final InteractionElement clickElem = click;
         private int tickCount = 0;
         
         @Override
         protected void onTick(){
            super.onTick();
            
            if(!GeomanticSteleBlockEntity.this.assembled || GeomanticSteleBlockEntity.this.getItem().isEmpty()){
               GeomanticSteleBlockEntity.this.hologram = null;
               destroy();
               return;
            }
            
            tickCount++;
            if(interactCooldown > 0) interactCooldown--;
            
            if(GeomanticSteleBlockEntity.this.wasActive){
               double f = Math.PI*2 / 80;
               iconElem.setRotation(0,iconElem.getYaw()+4);
               iconElem.setOffset(new Vec3(0,1+1.2*f*Math.cos(f*tickCount),0));
               icon.setBrightness(Brightness.FULL_BRIGHT);
               if(tickCount % 2 == 0){
                  iconElem.setItem(Items.GLASS_PANE.getDefaultInstance());
                  iconElem.setItem(GeomanticSteleBlockEntity.this.getItem());
               }
            }else{
               iconElem.setOffset(new Vec3(0,1,0));
               icon.setBrightness(new Brightness(5,5));
            }
         }
      };
      
      click.setOffset(new Vec3(0,0.625,0));
      icon.setOffset(new Vec3(0,1,0));
      icon.setScale(new Vector3f(0.75f));
      holder.addElement(icon);
      holder.addElement(click);
      return holder;
   }
   
   public boolean interact(ServerPlayer player, ItemStack stack){
      player.getCooldowns().addCooldown(player.getMainHandItem(),1);
      player.getCooldowns().addCooldown(player.getOffhandItem(),1);
      if(!assembled){
         player.sendSystemMessage(Component.literal("Multiblock not constructed."));
         multiblock.displayStructure(getMultiblockCheck(),player);
         return false;
      }
      
      if(player.isShiftKeyDown() && ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.METAMORPHIC_ALIGNMENT) > 0){
         GeomanticSteleGui gui = new GeomanticSteleGui(player,this);
         gui.open();
         return true;
      }
      
      if(!getItem().isEmpty() && stack.isEmpty()){ // Remove Item
         ItemStack returnStack = getItem().copy();
         setItem(0, ItemStack.EMPTY);
         this.level.setBlock(getBlockPos(),getBlockState().setValue(GeomanticStele.GeomanticSteleBlock.ACTIVE,false),Block.UPDATE_ALL);
         setChanged();
         BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(returnStack,player,0));
         return true;
      }
      
      //Add Item
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(getItem().isEmpty() && arcanaItem instanceof GeomanticStele.Interaction interaction){
         if(arcanaItem instanceof CleansingCharm){
            ArcanaAchievements.grant(player,ArcanaAchievements.DOCTOR_STONE);
         }
         if(arcanaItem instanceof AquaticEversource){
            ArcanaAchievements.grant(player,ArcanaAchievements.ARTIFICIAL_GEYSER);
         }
         if(arcanaItem instanceof MagmaticEversource){
            ArcanaAchievements.grant(player,ArcanaAchievements.ARTIFICIAL_VOLCANO);
         }
         if(arcanaItem instanceof FelidaeCharm){
            ArcanaAchievements.grant(player,ArcanaAchievements.MONOLITH_OF_FEAR);
         }
         if(arcanaItem instanceof WildGrowthCharm){
            ArcanaAchievements.grant(player,ArcanaAchievements.KOKOPELLI);
         }
         setItem(stack.copy());
         player.getInventory().removeItem(stack);
         setChanged();
         boolean hasRedstone = this.level.hasNeighborSignal(getBlockPos());
         if(hasRedstone){
            this.level.setBlock(getBlockPos(),getBlockState().setValue(GeomanticStele.GeomanticSteleBlock.ACTIVE,true),Block.UPDATE_ALL);
         }
         return true;
      }
      return false;
   }
   
   public Set<BlockPos> calculateBlocks(){
      HashSet<BlockPos> blocks = new HashSet<>();
      Vec3 range = getRange();
      if((range.x + range.y + range.z) == 0) return blocks;
      BlockPos thisPos = getBlockPos();
      int rangeX = (int)Math.ceil(range.x);
      int rangeY = (int)Math.ceil(range.y);
      int rangeZ = (int)Math.ceil(range.z);
      for(BlockPos blockPos : BlockPos.betweenClosed(thisPos.getX() - rangeX, thisPos.getY() - rangeY, thisPos.getZ() - rangeZ, thisPos.getX() + rangeX, thisPos.getY() + rangeY, thisPos.getZ() + rangeZ)){
         blocks.add(blockPos.immutable());
      }
      return blocks;
   }
   
   public void setRange(Vec3 range){
      Vec3 maxRange = getMaxRange();
      this.rangeX = Mth.clamp(range.x,0,maxRange.x);
      this.rangeY = Mth.clamp(range.y,0,maxRange.y);
      this.rangeZ = Mth.clamp(range.z,0,maxRange.z);
   }
   
   public Vec3 getRange(){
      Vec3 maxRange = getMaxRange();
      this.rangeX = Mth.clamp(this.rangeX,0,maxRange.x);
      this.rangeY = Mth.clamp(this.rangeY,0,maxRange.y);
      this.rangeZ = Mth.clamp(this.rangeZ,0,maxRange.z);
      return new Vec3(rangeX,rangeY,rangeZ);
   }
   
   public Vec3 getMaxRange(){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(getItem());
      if(arcanaItem instanceof GeomanticStele.Interaction interaction){
         Vec3 baseRange = interaction.getBaseRange();
         return baseRange.scale(rangeMod);
      }
      return Vec3.ZERO;
   }
   
   public boolean isAssembled(){
      return assembled;
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition,serverWorld.getBlockState(worldPosition),new BlockPos(((MultiblockCore) ArcanaRegistry.GEOMANTIC_STELE).getCheckOffset()),null);
   }
   
   public void setItem(ItemStack stack){
      this.inventory.setItem(0,stack);
      setChanged();
      if(!stack.isEmpty()) setRange(getMaxRange());
   }
   
   public ItemStack getItem(){
      return this.inventory.getItem(0);
   }
   
   public void giveXP(int xp){
      String crafterId = getCrafterId();
      if(crafterId != null && !crafterId.isEmpty()){
         ArcanaNovum.data(AlgoUtils.getUUID(crafterId)).addXP(xp);
      }
   }
   
   @Override
   protected Component getDefaultName(){
      return getArcanaItem().getTranslatedName();
   }
   
   public Container getInventory(){
      return this.inventory;
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
      return ArcanaRegistry.GEOMANTIC_STELE;
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new SimpleContainer(getContainerSize());
      if (!this.tryLoadLootTable(view)) {
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
      this.rangeMod = RANGE_MODS[ArcanaAugments.getAugmentFromMap(this.augments,ArcanaAugments.GEOLITHIC_AMPLIFICATION)];
      Vec3 maxRange = getMaxRange();
      this.rangeX = view.getDoubleOr("rangeX", maxRange.x);
      this.rangeY = view.getDoubleOr("rangeY", maxRange.y);
      this.rangeZ = view.getDoubleOr("rangeZ", maxRange.z);
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC, this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG, this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG, this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME, this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG, this.origin);
      if (!this.trySaveLootTable(view)) {
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
      view.putDouble("rangeX", this.rangeX);
      view.putDouble("rangeY", this.rangeY);
      view.putDouble("rangeZ", this.rangeZ);
   }
   
   public static class SteleZone {
      private final Set<BlockPos> blocks;
      private final Set<Long> chunks;
      private final ResourceKey<Level> dimension;
      private final GeomanticSteleBlockEntity blockEntity;
      private int keepAlive;
      
      private SteleZone(Set<BlockPos> blocks, ResourceKey<Level> dimension, GeomanticSteleBlockEntity blockEntity){
         this.blocks = new HashSet<>(blocks);
         this.dimension = dimension;
         this.blockEntity = blockEntity;
         this.keepAlive = KEEP_ALIVE;
         this.chunks = new HashSet<>();
         for(BlockPos pos : blocks){
            chunks.add(getChunkKey(pos));
         }
      }
      
      public boolean containsBlock(BlockPos pos){
         return blocks.contains(pos);
      }
      
      public Set<Long> getChunks(){
         return chunks;
      }
      
      public void decrementKeepAlive(){
         keepAlive--;
      }
      
      public boolean checkExpired(){
         if(keepAlive <= 0){
            // Clear the block entity's reference so it can re-register
            if(blockEntity.currentZone == this){
               blockEntity.currentZone = null;
            }
            return true;
         }
         return false;
      }
      
      public void refresh(){
         keepAlive = KEEP_ALIVE;
      }
      
      public BlockPos getSource(){
         return blockEntity.getBlockPos();
      }
      
      public GeomanticSteleBlockEntity getBlockEntity(){
         return blockEntity;
      }
      
      @Override
      public int hashCode(){
         return blockEntity.getBlockPos().hashCode();
      }
      
      @Override
      public boolean equals(Object obj){
         if(this == obj) return true;
         if(!(obj instanceof SteleZone other)) return false;
         return this.getSource().equals(other.getSource()) && this.dimension.equals(other.dimension);
      }
   }
   
   public static @Nullable GeomanticSteleBlockEntity.SteleZone getZoneAtEntity(Entity entity, Predicate<ItemStack> predicate){
      return getZoneAtPos(entity.level(), entity.blockPosition(), predicate);
   }
   
   public static @Nullable GeomanticSteleBlockEntity.SteleZone getZoneAtPos(Level level, BlockPos pos, Predicate<ItemStack> predicate){
      Map<Long, Set<GeomanticSteleBlockEntity.SteleZone>> dimensionZones = ACTIVE_STELES.get(level.dimension());
      if(dimensionZones == null) return null;
      
      long chunkKey = getChunkKey(pos);
      Set<GeomanticSteleBlockEntity.SteleZone> chunkZones = dimensionZones.get(chunkKey);
      if(chunkZones == null) return null;
      
      for(GeomanticSteleBlockEntity.SteleZone zone : chunkZones){
         if(predicate != null && !predicate.test(zone.getBlockEntity().getItem())) continue;
         if(zone.containsBlock(pos)){
            return zone;
         }
      }
      return null;
   }
   
   public static List<GeomanticSteleBlockEntity.SteleZone> getZonesAtEntity(Entity entity, Predicate<ItemStack> predicate){
      return getZonesAtPos(entity.level(), entity.blockPosition(), predicate);
   }
   
   public static List<GeomanticSteleBlockEntity.SteleZone> getZonesAtPos(Level level, BlockPos pos, Predicate<ItemStack> predicate){
      Map<Long, Set<GeomanticSteleBlockEntity.SteleZone>> dimensionZones = ACTIVE_STELES.get(level.dimension());
      List<GeomanticSteleBlockEntity.SteleZone> zones = new ArrayList<>();
      if(dimensionZones == null) return zones;
      
      long chunkKey = getChunkKey(pos);
      Set<GeomanticSteleBlockEntity.SteleZone> chunkZones = dimensionZones.get(chunkKey);
      if(chunkZones == null) return zones;
      
      for(GeomanticSteleBlockEntity.SteleZone zone : chunkZones){
         if(predicate != null && !predicate.test(zone.getBlockEntity().getItem())) continue;
         if(zone.containsBlock(pos)){
            zones.add(zone);
         }
      }
      return zones;
   }
   
   public static boolean isEntityInZone(Entity entity, Predicate<ItemStack> predicate){
      return getZoneAtEntity(entity, predicate) != null;
   }
   
   private static long getChunkKey(BlockPos pos){
      return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
   }
   
   private static void registerZone(GeomanticSteleBlockEntity.SteleZone zone){
      Map<Long, Set<GeomanticSteleBlockEntity.SteleZone>> dimensionZones = ACTIVE_STELES.computeIfAbsent(zone.dimension, k -> new HashMap<>());
      for(long chunkKey : zone.getChunks()){
         dimensionZones.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(zone);
      }
   }
   
   private static void unregisterZone(GeomanticSteleBlockEntity.SteleZone zone){
      Map<Long, Set<GeomanticSteleBlockEntity.SteleZone>> dimensionZones = ACTIVE_STELES.get(zone.dimension);
      if(dimensionZones == null) return;
      
      for(long chunkKey : zone.getChunks()){
         Set<GeomanticSteleBlockEntity.SteleZone> chunkZones = dimensionZones.get(chunkKey);
         if(chunkZones != null){
            chunkZones.remove(zone);
            if(chunkZones.isEmpty()){
               dimensionZones.remove(chunkKey);
            }
         }
      }
      
      if(dimensionZones.isEmpty()){
         ACTIVE_STELES.remove(zone.dimension);
      }
   }
   
   public static void tickZones(){
      // Collect all unique zones first
      Set<GeomanticSteleBlockEntity.SteleZone> allZones = new HashSet<>();
      for(Map<Long, Set<GeomanticSteleBlockEntity.SteleZone>> dimensionZones : ACTIVE_STELES.values()){
         for(Set<GeomanticSteleBlockEntity.SteleZone> chunkZones : dimensionZones.values()){
            allZones.addAll(chunkZones);
         }
      }
      
      // Decrement keepalive for each unique zone once
      for(GeomanticSteleBlockEntity.SteleZone zone : allZones){
         zone.decrementKeepAlive();
      }
      
      // Find expired zones
      Set<GeomanticSteleBlockEntity.SteleZone> expiredZones = new HashSet<>();
      for(GeomanticSteleBlockEntity.SteleZone zone : allZones){
         if(zone.checkExpired()){
            expiredZones.add(zone);
         }
      }
      
      // Remove expired zones from all chunk sets
      for(GeomanticSteleBlockEntity.SteleZone expired : expiredZones){
         unregisterZone(expired);
      }
   }
}
