package net.borisshoes.arcananovum.blocks;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.ContainerWatcher;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.gui.spawnerinfuser.SpawnerInfuserGui;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.TreeMap;

public class SpawnerInfuserBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, PolymerObject, ContainerWatcher, ArcanaBlockEntity {
   
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private ArcanaSkin skin;
   private String customName;
   private WatchedContainer inventory = new WatchedContainer(getContainerSize());
   private boolean active;
   private ItemStack soulstone;
   private int points;
   private int spentPoints;
   private short minSpawnDelay;
   private short maxSpawnDelay;
   private short spawnRange;
   private short spawnCount;
   private short playerRange;
   private short maxEntities;
   private boolean updating;
   private boolean prevStone;
   private final HashMap<ServerPlayer, SpawnerInfuserGui> watchingPlayers = new HashMap<>();
   
   public SpawnerInfuserBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, blockPos, blockState);
      this.inventory.addWatcher(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, ArcanaSkin skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
      this.active = false;
      this.soulstone = ItemStack.EMPTY;
      this.points = 0;
      this.spentPoints = 0;
      this.minSpawnDelay = 200;
      this.maxSpawnDelay = 800;
      this.spawnCount = 4;
      this.maxEntities = 6;
      this.playerRange = 16;
      this.spawnRange = 4;
   }
   
   public void openGui(ServerPlayer player){
      SpawnerInfuserGui gui = new SpawnerInfuserGui(player, this);
      gui.build();
      gui.open();
      watchingPlayers.put(player, gui);
   }
   
   public void removePlayer(ServerPlayer player){
      watchingPlayers.remove(player);
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof SpawnerInfuserBlockEntity infuser){
         infuser.tick();
      }
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      if(serverWorld.getServer().getTickCount() % 5 == 0){ // Infuser only ticks redstone every quarter second
         // Check for spawner above, match soulstone type, update redstone power, do particles
         boolean prevActive = active;
         boolean hasRedstone = serverWorld.hasNeighborSignal(worldPosition); // Redstone high is ON
         boolean hasSoulstone = !soulstone.isEmpty();
         BlockPos spawnerPos = worldPosition.offset(0, 2, 0);
         BlockEntity blockEntity = serverWorld.getBlockEntity(spawnerPos);
         BlockState spawnerState = serverWorld.getBlockState(spawnerPos);
         boolean hasSpawner = spawnerState.is(Blocks.SPAWNER) && blockEntity instanceof SpawnerBlockEntity;
         
         if(!hasRedstone || !hasSoulstone || !hasSpawner){
            if(prevActive) this.active = false; // Update active status
            if(prevActive != active) setChanged();
            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE, active));
            return;
         }
         
         String stoneType = Soulstone.getType(soulstone);
         SpawnerBlockEntity spawnerEntity = (SpawnerBlockEntity) blockEntity;
         try(ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())){
            TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, this.getLevel().registryAccess());
            spawnerEntity.getSpawner().save(nbtWriteView);
            CompoundTag spawnerData = nbtWriteView.buildResult();
            CompoundTag spawnData = spawnerData.getCompoundOrEmpty("SpawnData");
            if(spawnData.isEmpty() || !spawnData.contains("entity") || !spawnData.getCompoundOrEmpty("entity").contains("id")){
                if(prevActive) this.active = false; // Update active status
                if(prevActive != active) setChanged();
                level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE, active));
                return;
             }
            CompoundTag spawnEntity = spawnData.getCompoundOrEmpty("entity");
            
            boolean correctType = stoneType.equals(spawnEntity.getStringOr("id", ""));
            
            if(correctType){
                if(!prevActive) this.active = true; // Update active status
                if(prevActive != active) setChanged();
                level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE, active));
               ArcanaEffectUtils.spawnerInfuser(serverWorld, worldPosition, 5);
               SoundUtils.soulSounds(serverWorld, worldPosition, 1, 5);
            }
         }
      }
      
      if(serverWorld.getServer().getTickCount() % 20 == 0 && this.active){
         ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
      }
   }
   
   public void tickInfuser(BlockPos spawnerPos, SpawnerBlockEntity spawnerEntity){
      BaseSpawner logic = spawnerEntity.getSpawner();
      try(ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this.problemPath(), LogUtils.getLogger())){
         TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, this.getLevel().registryAccess());
         logic.save(nbtWriteView);
         CompoundTag savedLogic = nbtWriteView.buildResult(); // Save default data
         CompoundTag newLogic = getSpawnerStats().copy(); // Get data from infuser
         
         newLogic.put("SpawnData", savedLogic.get("SpawnData").copy()); // Copy some default data into new data
         newLogic.put("SpawnPotentials", savedLogic.get("SpawnPotentials").copy());
         short oldDelay = savedLogic.getShortOr("Delay", (short) 0);
         short maxDelay = newLogic.getShortOr("MaxSpawnDelay", (short) 0);
         newLogic.putShort("Delay", (short) Math.min(oldDelay, maxDelay));
         
         ValueInput newNbtReadView = TagValueInput.create(logging, this.getLevel().registryAccess(), newLogic);
         logic.load(level, spawnerPos, newNbtReadView); // Inject new data
         if(level instanceof ServerLevel serverWorld) logic.serverTick(serverWorld, spawnerPos); // Tick with new data
         TagValueOutput nbtWriteView2 = TagValueOutput.createWithContext(logging, this.getLevel().registryAccess());
         logic.save(nbtWriteView2);
         short newDelay = nbtWriteView2.buildResult().getShortOr("Delay", (short) 0);
         savedLogic.putShort("Delay", newDelay); // Extract new delay and put in saved data
         ValueInput savedNbtReadView = TagValueInput.create(logging, this.getLevel().registryAccess(), savedLogic);
         logic.load(level, spawnerPos, savedNbtReadView); // Return saved default data with new delay
      }
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
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.SPAWNER_INFUSER;
   }
   
   public boolean isActive(){
      return active;
   }
   
   public ItemStack getSoulstone(){
      return soulstone;
   }
   
   public int getPoints(){
      return points;
   }
   
   public int getSpentPoints(){
      return spentPoints;
   }
   
   public void setSoulstone(ItemStack soulstone){
      this.soulstone = soulstone;
   }
   
   public void setPoints(int points){
      this.points = points;
   }
   
   public void setSpentPoints(int spentPoints){
      this.spentPoints = spentPoints;
   }
   
   public void resetStats(){
      this.minSpawnDelay = 200;
      this.maxSpawnDelay = 800;
      this.spawnCount = 4;
      this.maxEntities = 6;
      this.playerRange = 16;
      this.spawnRange = 4;
   }
   
   @Override
   public void preRemoveSideEffects(BlockPos pos, BlockState oldState){
      super.preRemoveSideEffects(pos, oldState);
      
      if(!(this.level instanceof ServerLevel serverWorld)) return;
      
      NonNullList<ItemStack> drops = NonNullList.create();
      int ratio = (int) Math.pow(2, 3 + ArcanaAugments.getAugmentFromMap(this.getAugments(), ArcanaAugments.AUGMENTED_APPARATUS));
      int points = this.getPoints();
      if(points > 0){
         Item pointsItem = SpawnerInfuser.getPointsItem();
         while(points / ratio > 64){
            ItemStack dropItem = new ItemStack(pointsItem);
            dropItem.setCount(64);
            drops.add(dropItem.copy());
            points -= 64 * ratio;
         }
         ItemStack dropItem = new ItemStack(pointsItem);
         dropItem.setCount(points / ratio);
         drops.add(dropItem.copy());
      }
      
      Containers.dropContents(level, pos, drops);
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = ArcanaSkin.getSkinFromString(view.getStringOr(ArcanaBlockEntity.SKIN_TAG, ""));
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.active = view.getBooleanOr("active", false);
      this.points = view.getIntOr("points", 0);
      this.spentPoints = view.getIntOr("spentPoints", 0);
      this.soulstone = ItemStack.EMPTY;
      view.read("soulstone", ItemStack.CODEC).ifPresent(stack -> {
         this.soulstone = stack;
      });
      
      
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new WatchedContainer(getContainerSize());
      this.inventory.addWatcher(this);
      if(!this.tryLoadLootTable(view)){
         ContainerHelper.loadAllItems(view, this.inventory.getItems());
      }
      
      view.read("spawnerStats", SpawnerStats.CODEC).ifPresent(stats -> {
         setSpawnerStats(stats.toNbt());
      });
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
      view.putInt("points", this.points);
      view.putInt("spentPoints", this.spentPoints);
      view.putBoolean("active", this.active);
      if(!this.trySaveLootTable(view)){
         ContainerHelper.saveAllItems(view, this.inventory.getItems());
      }
      
      if(this.soulstone != null && !this.soulstone.isEmpty()){
         view.storeNullable("soulstone", ItemStack.CODEC, this.soulstone);
      }
      
      view.storeNullable("spawnerStats", SpawnerStats.CODEC, SpawnerStats.fromNbt(getSpawnerStats()));
   }
   
   public CompoundTag getSpawnerStats(){
      CompoundTag stats = new CompoundTag();
      stats.putShort("MinSpawnDelay", this.minSpawnDelay);
      stats.putShort("MaxSpawnDelay", this.maxSpawnDelay);
      stats.putShort("SpawnCount", this.spawnCount);
      stats.putShort("MaxNearbyEntities", this.maxEntities);
      stats.putShort("RequiredPlayerRange", this.playerRange);
      stats.putShort("SpawnRange", this.spawnRange);
      return stats;
   }
   
   public void setSpawnerStats(CompoundTag stats){
      if(stats.contains("MinSpawnDelay")){
         this.minSpawnDelay = stats.getShortOr("MinSpawnDelay", (short) 0);
      }
      if(stats.contains("MaxSpawnDelay")){
         this.maxSpawnDelay = stats.getShortOr("MaxSpawnDelay", (short) 0);
      }
      if(stats.contains("SpawnCount")){
         this.spawnCount = stats.getShortOr("SpawnCount", (short) 0);
      }
      if(stats.contains("MaxNearbyEntities")){
         this.maxEntities = stats.getShortOr("MaxNearbyEntities", (short) 0);
      }
      if(stats.contains("RequiredPlayerRange")){
         this.playerRange = stats.getShortOr("RequiredPlayerRange", (short) 0);
      }
      if(stats.contains("SpawnRange")){
         this.spawnRange = stats.getShortOr("SpawnRange", (short) 0);
      }
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
   protected Component getDefaultName(){
      return Component.literal("Spawner Infuser");
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
      return 2;
   }
   
   public Container getInventory(){
      return this.inventory;
   }
   
   public void refreshGuis(){
      watchingPlayers.values().forEach(SpawnerInfuserGui::build);
   }
   
   @Override
   public void setChanged(){
      super.setChanged();
      this.inventory.setChanged();
   }
   
   @Override
   public void onChanged(WatchedContainer inv){
      if(!updating){
         updating = true;
         
         ItemStack soulstoneSlot = inv.getItem(0);
         ItemStack extraPoints = ItemStack.EMPTY;
         int points = getPoints();
         int bonusCap = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SPAWNER_INFUSER_EXTRA_CAPACITY_PER_LVL).get(ArcanaAugments.getAugmentFromMap(getAugments(), ArcanaAugments.SOUL_RESERVOIR));
         int ratio = (int) Math.pow(2, 3 + ArcanaAugments.getAugmentFromMap(getAugments(), ArcanaAugments.AUGMENTED_APPARATUS));
         
         if(!soulstoneSlot.isEmpty()){
            setSoulstone(soulstoneSlot);
            if(!prevStone){
               watchingPlayers.keySet().forEach(player -> SoundUtils.soulSounds(player, 1, 20));
               if(Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot)) == Soulstone.tiers.length){
                  watchingPlayers.keySet().forEach(player -> ArcanaAchievements.grant(player, ArcanaAchievements.INNOCENT_SOULS));
               }
            }
            
            ItemStack pointsSlot = inv.getItem(1);
            if(!pointsSlot.isEmpty()){
               int maxPoints = SpawnerInfuser.pointsFromTier[Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot))] + bonusCap;
               int toAdd = pointsSlot.getCount() * ratio;
               
               if(maxPoints - points < toAdd){
                  setPoints(maxPoints);
                  extraPoints = pointsSlot.copy();
                  extraPoints.setCount((toAdd - (maxPoints - points)) / ratio);
               }else{
                  setPoints(points + toAdd);
               }
               int curPoints = getPoints();
               if(toAdd != 0 && points < maxPoints){
                  watchingPlayers.keySet().forEach(player -> {
                     SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_CHARGE, 1, (.8f + ((float) curPoints / maxPoints)));
                     if(curPoints == maxPoints)
                        SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1, 2f);
                     if(curPoints >= 512) ArcanaAchievements.grant(player, ArcanaAchievements.ARCHLICH);
                     if(curPoints >= 1024) ArcanaAchievements.grant(player, ArcanaAchievements.POWER_OVERWHELMING);
                  });
               }
            }
            prevStone = true;
         }else{
            setSoulstone(ItemStack.EMPTY);
            points += inv.getItem(1).getCount() * ratio;
            
            NonNullList<ItemStack> drops = NonNullList.create();
            if(points > 0){
               Item pointsItem = SpawnerInfuser.getPointsItem();
               while(points / ratio > 64){
                  ItemStack dropItem = new ItemStack(pointsItem);
                  dropItem.setCount(64);
                  drops.add(dropItem.copy());
                  points -= 64 * ratio;
               }
               ItemStack dropItem = new ItemStack(pointsItem);
               dropItem.setCount(points / ratio);
               drops.add(dropItem.copy());
            }
            
            if(getLevel() != null){
               Containers.dropContents(getLevel(), getBlockPos().above(), drops);
            }
            
            if(prevStone){
               watchingPlayers.keySet().forEach(player -> SoundUtils.playSongToPlayer(player, SoundEvents.RESPAWN_ANCHOR_DEPLETE, 1, .8f));
            }
            
            setPoints(0);
            resetStats();
            setSpentPoints(0);
            
            prevStone = false;
         }
         
         inv.setItem(1, extraPoints);
         setChanged();
         refreshGuis();
         
         updating = false;
      }
   }
   
   public record SpawnerStats(short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities,
                              short requiredPlayerRange, short spawnRange) {
      public static final Codec<SpawnerStats> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.SHORT.fieldOf("MinSpawnDelay").forGetter(s -> s.minSpawnDelay), Codec.SHORT.fieldOf("MaxSpawnDelay").forGetter(s -> s.maxSpawnDelay), Codec.SHORT.fieldOf("SpawnCount").forGetter(s -> s.spawnCount), Codec.SHORT.fieldOf("MaxNearbyEntities").forGetter(s -> s.maxNearbyEntities), Codec.SHORT.fieldOf("RequiredPlayerRange").forGetter(s -> s.requiredPlayerRange), Codec.SHORT.fieldOf("SpawnRange").forGetter(s -> s.spawnRange)).apply(i, SpawnerStats::new));
      
      public CompoundTag toNbt(){
         CompoundTag n = new CompoundTag();
         n.putShort("MinSpawnDelay", this.minSpawnDelay);
         n.putShort("MaxSpawnDelay", this.maxSpawnDelay);
         n.putShort("SpawnCount", this.spawnCount);
         n.putShort("MaxNearbyEntities", this.maxNearbyEntities);
         n.putShort("RequiredPlayerRange", this.requiredPlayerRange);
         n.putShort("SpawnRange", this.spawnRange);
         return n;
      }
      
      public static SpawnerStats fromNbt(CompoundTag n){
         return new SpawnerStats(
               n.getShortOr("MinSpawnDelay", (short) 0),
               n.getShortOr("MaxSpawnDelay", (short) 0),
               n.getShortOr("SpawnCount", (short) 0),
               n.getShortOr("MaxNearbyEntities", (short) 0),
               n.getShortOr("RequiredPlayerRange", (short) 0),
               n.getShortOr("SpawnRange", (short) 0));
      }
   }
}
