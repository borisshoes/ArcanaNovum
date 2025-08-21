package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.spawnerinfuser.SpawnerInfuserGui;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SpawnerInfuserBlockEntity extends LootableContainerBlockEntity implements SidedInventory, PolymerObject, InventoryChangedListener, ArcanaBlockEntity {
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private SimpleInventory inventory = new SimpleInventory(size());
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
   private final HashMap<ServerPlayerEntity, SpawnerInfuserGui> watchingPlayers = new HashMap<>();
   
   public SpawnerInfuserBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, blockPos, blockState);
      this.inventory.addListener(this);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
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
   
   public void openGui(ServerPlayerEntity player){
      SpawnerInfuserGui gui = new SpawnerInfuserGui(player,this, getWorld());
      gui.build();
      gui.open();
      watchingPlayers.put(player,gui);
   }
   
   public void removePlayer(ServerPlayerEntity player){
      watchingPlayers.remove(player);
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof SpawnerInfuserBlockEntity infuser){
         infuser.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      if(serverWorld.getServer().getTicks() % 5 == 0){ // Infuser only ticks redstone every quarter second
         // Check for spawner above, match soulstone type, update redstone power, do particles
         boolean prevActive = active;
         boolean hasRedstone = serverWorld.isReceivingRedstonePower(pos); // Redstone high is ON
         boolean hasSoulstone = !soulstone.isEmpty();
         BlockPos spawnerPos = pos.add(0,2,0);
         BlockEntity blockEntity = serverWorld.getBlockEntity(spawnerPos);
         BlockState spawnerState = serverWorld.getBlockState(spawnerPos);
         boolean hasSpawner = spawnerState.isOf(Blocks.SPAWNER) && blockEntity instanceof MobSpawnerBlockEntity;
         
         if(!hasRedstone || !hasSoulstone || !hasSpawner){
            if(prevActive) this.active = false; // Update active status
            world.setBlockState(pos,world.getBlockState(pos).with(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE,active));
            return;
         }
         
         String stoneType = Soulstone.getType(soulstone);
         MobSpawnerBlockEntity spawnerEntity = (MobSpawnerBlockEntity) blockEntity;
         NbtCompound spawnerData = spawnerEntity.getLogic().writeNbt(new NbtCompound());
         NbtCompound spawnData = spawnerData.getCompoundOrEmpty("SpawnData");
         if(spawnData.isEmpty() || !spawnData.contains("entity") || !spawnData.getCompoundOrEmpty("entity").contains("id")){
            if(prevActive) this.active = false; // Update active status
            world.setBlockState(pos,world.getBlockState(pos).with(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE,active));
            return;
         }
         NbtCompound spawnEntity = spawnData.getCompoundOrEmpty("entity");
         
         boolean correctType = stoneType.equals(spawnEntity.getString("id", ""));
         
         if(correctType){
            if(!prevActive) this.active = true; // Update active status
            world.setBlockState(pos,world.getBlockState(pos).with(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE,active));
            ParticleEffectUtils.spawnerInfuser(serverWorld,pos,5);
            SoundUtils.soulSounds(serverWorld,pos,1,5);
         }
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.active){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public void tickInfuser(BlockPos spawnerPos, MobSpawnerBlockEntity spawnerEntity){
      MobSpawnerLogic logic = spawnerEntity.getLogic();
      NbtCompound savedLogic = logic.writeNbt(new NbtCompound()); // Save default data
      NbtCompound newLogic = getSpawnerStats().copy(); // Get data from infuser
      
      newLogic.put("SpawnData",savedLogic.get("SpawnData").copy()); // Copy some default data into new data
      newLogic.put("SpawnPotentials",savedLogic.get("SpawnPotentials").copy());
      short oldDelay = savedLogic.getShort("Delay", (short) 0);
      short maxDelay = newLogic.getShort("MaxSpawnDelay", (short) 0);
      newLogic.putShort("Delay", (short) Math.min(oldDelay,maxDelay));
      
      logic.readNbt(world,spawnerPos,newLogic); // Inject new data
      if(world instanceof ServerWorld serverWorld) logic.serverTick(serverWorld, spawnerPos); // Tick with new data
      short newDelay = logic.writeNbt(new NbtCompound()).getShort("Delay", (short) 0);
      savedLogic.putShort("Delay",newDelay); // Extract new delay and put in saved data
      logic.readNbt(world,spawnerPos,savedLogic); // Return saved default data with new delay
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
   public void onBlockReplaced(BlockPos pos, BlockState oldState){
      super.onBlockReplaced(pos, oldState);
      
      if(!(this.world instanceof ServerWorld serverWorld)) return;
      
      DefaultedList<ItemStack> drops = DefaultedList.of();
      int ratio = (int) Math.pow(2,3+ArcanaAugments.getAugmentFromMap(this.getAugments(),ArcanaAugments.AUGMENTED_APPARATUS.id));
      int points = this.getPoints();
      if(points > 0){
         while(points/ratio > 64){
            ItemStack dropItem = new ItemStack(SpawnerInfuser.POINTS_ITEM);
            dropItem.setCount(64);
            drops.add(dropItem.copy());
            points -= 64*ratio;
         }
         ItemStack dropItem = new ItemStack(SpawnerInfuser.POINTS_ITEM);
         dropItem.setCount(points/ratio);
         drops.add(dropItem.copy());
      }
      
      ItemScatterer.spawn(world, pos, drops);
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
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompoundOrEmpty("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug, augCompound.getInt(key, 0));
         }
      }
      this.inventory = new SimpleInventory(size());
      this.inventory.addListener(this);
      if(!this.readLootTable(nbt) && nbt.contains("Items")){
         Inventories.readNbt(nbt, this.inventory.getHeldStacks(), registryLookup);
      }
      if(nbt.contains("active")){
         this.active = nbt.getBoolean("active", false);
      }
      if(nbt.contains("soulstone")){
         this.soulstone = ItemStack.fromNbt(registryLookup, nbt.getCompoundOrEmpty("soulstone")).orElse(ItemStack.EMPTY);
      }
      if(nbt.contains("points")){
         this.points = nbt.getInt("points", 0);
      }
      if(nbt.contains("spentPoints")){
         this.spentPoints = nbt.getInt("spentPoints", 0);
      }
      
      if(nbt.contains("spawnerStats")){
         NbtCompound stats = nbt.getCompoundOrEmpty("spawnerStats");
         setSpawnerStats(stats);
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
      
      nbt.putInt("points",this.points);
      nbt.putInt("spentPoints",this.spentPoints);
      nbt.putBoolean("active",this.active);
      if(this.soulstone != null && !this.soulstone.isEmpty()){
         nbt.put("soulstone",soulstone.toNbt(registryLookup));
      }
      
      nbt.put("spawnerStats",getSpawnerStats());
   }
   
   public NbtCompound getSpawnerStats(){
      NbtCompound stats = new NbtCompound();
      stats.putShort("MinSpawnDelay", this.minSpawnDelay);
      stats.putShort("MaxSpawnDelay", this.maxSpawnDelay);
      stats.putShort("SpawnCount", this.spawnCount);
      stats.putShort("MaxNearbyEntities", this.maxEntities);
      stats.putShort("RequiredPlayerRange", this.playerRange);
      stats.putShort("SpawnRange", this.spawnRange);
      return stats;
   }
   
   public void setSpawnerStats(NbtCompound stats){
      if(stats.contains("MinSpawnDelay")){
         this.minSpawnDelay = stats.getShort("MinSpawnDelay", (short) 0);
      }
      if(stats.contains("MaxSpawnDelay")){
         this.maxSpawnDelay = stats.getShort("MaxSpawnDelay", (short) 0);
      }
      if(stats.contains("SpawnCount")){
         this.spawnCount = stats.getShort("SpawnCount", (short) 0);
      }
      if(stats.contains("MaxNearbyEntities")){
         this.maxEntities = stats.getShort("MaxNearbyEntities", (short) 0);
      }
      if(stats.contains("RequiredPlayerRange")){
         this.playerRange = stats.getShort("RequiredPlayerRange", (short) 0);
      }
      if(stats.contains("SpawnRange")){
         this.spawnRange = stats.getShort("SpawnRange", (short) 0);
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
      return Text.literal("Spawner Infuser");
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
      return 2;
   }
   
   public Inventory getInventory(){
      return this.inventory;
   }
   
   public void refreshGuis(){
      watchingPlayers.values().forEach(SpawnerInfuserGui::build);
   }
   
   @Override
   public void onInventoryChanged(Inventory inv){
      if(!updating){
         updating = true;
         
         ItemStack soulstoneSlot = inv.getStack(0);
         ItemStack extraPoints = ItemStack.EMPTY;
         int points = getPoints();
         int bonusCap = new int[]{0,64,128,192,256,352}[ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.SOUL_RESERVOIR.id)];
         int ratio = (int) Math.pow(2,3+ArcanaAugments.getAugmentFromMap(getAugments(),ArcanaAugments.AUGMENTED_APPARATUS.id));
         
         if(!soulstoneSlot.isEmpty()){
            setSoulstone(soulstoneSlot);
            if(!prevStone){
               watchingPlayers.keySet().forEach(player -> SoundUtils.soulSounds(player,1,20));
               if(Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot)) == Soulstone.tiers.length){
                  watchingPlayers.keySet().forEach(player -> ArcanaAchievements.grant(player, ArcanaAchievements.INNOCENT_SOULS.id));
               }
            }
            
            ItemStack pointsSlot = inv.getStack(1);
            if(!pointsSlot.isEmpty()){
               int maxPoints = SpawnerInfuser.pointsFromTier[Soulstone.soulsToTier(Soulstone.getSouls(soulstoneSlot))] + bonusCap;
               int toAdd = pointsSlot.getCount() * ratio;
               
               if(maxPoints-points < toAdd){
                  setPoints(maxPoints);
                  extraPoints = pointsSlot.copy();
                  extraPoints.setCount((toAdd-(maxPoints-points))/ratio);
               }else{
                  setPoints(points+toAdd);
               }
               int curPoints = getPoints();
               if(toAdd != 0 && points < maxPoints){
                  watchingPlayers.keySet().forEach(player -> {
                     SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, (.8f+((float)curPoints/maxPoints)));
                     if(curPoints == maxPoints)SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 2f);
                     if(curPoints >= 512) ArcanaAchievements.grant(player,ArcanaAchievements.ARCHLICH.id);
                     if(curPoints >= 1024) ArcanaAchievements.grant(player,ArcanaAchievements.POWER_OVERWHELMING.id);
                  });
               }
            }
            prevStone = true;
         }else{
            setSoulstone(ItemStack.EMPTY);
            points += inv.getStack(1).getCount() * ratio;
            
            DefaultedList<ItemStack> drops = DefaultedList.of();
            if(points > 0){
               while(points/ratio > 64){
                  ItemStack dropItem = new ItemStack(SpawnerInfuser.POINTS_ITEM);
                  dropItem.setCount(64);
                  drops.add(dropItem.copy());
                  points -= 64*ratio;
               }
               ItemStack dropItem = new ItemStack(SpawnerInfuser.POINTS_ITEM);
               dropItem.setCount(points/ratio);
               drops.add(dropItem.copy());
            }
            
            if(getWorld() != null){
               ItemScatterer.spawn(getWorld(),getPos().up(),drops);
            }
            
            if(prevStone){
               watchingPlayers.keySet().forEach(player -> SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1, .8f));
            }
            
            setPoints(0);
            resetStats();
            setSpentPoints(0);
            
            prevStone = false;
         }
         
         inv.setStack(1,extraPoints);
         markDirty();
         refreshGuis();
         
         updating = false;
      }
   }
}
