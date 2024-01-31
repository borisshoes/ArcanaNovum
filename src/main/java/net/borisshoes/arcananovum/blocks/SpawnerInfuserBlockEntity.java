package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
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
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;

public class SpawnerInfuserBlockEntity extends LootableContainerBlockEntity implements SidedInventory, PolymerObject, MagicBlockEntity {
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private DefaultedList<ItemStack> inventory;
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
   
   public SpawnerInfuserBlockEntity(BlockPos blockPos, BlockState blockState){
      super(ArcanaRegistry.SPAWNER_INFUSER_BLOCK_ENTITY, blockPos, blockState);
      this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
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
         NbtCompound spawnData = spawnerData.getCompound("SpawnData");
         if(spawnData.isEmpty() || !spawnData.contains("entity") || !spawnData.getCompound("entity").contains("id")){
            if(prevActive) this.active = false; // Update active status
            world.setBlockState(pos,world.getBlockState(pos).with(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE,active));
            return;
         }
         NbtCompound spawnEntity = spawnData.getCompound("entity");
         
         boolean correctType = stoneType.equals(spawnEntity.getString("id"));
         
         if(correctType){
            if(!prevActive) this.active = true; // Update active status
            world.setBlockState(pos,world.getBlockState(pos).with(SpawnerInfuser.SpawnerInfuserBlock.ACTIVE,active));
            ParticleEffectUtils.spawnerInfuser(serverWorld,pos,5);
            SoundUtils.soulSounds(serverWorld,pos,1,5);
         }
      }
   }
   
   public void tickInfuser(BlockPos spawnerPos, MobSpawnerBlockEntity spawnerEntity){
      MobSpawnerLogic logic = spawnerEntity.getLogic();
      NbtCompound savedLogic = logic.writeNbt(new NbtCompound()); // Save default data
      NbtCompound newLogic = getSpawnerStats().copy(); // Get data from infuser
      
      newLogic.put("SpawnData",savedLogic.get("SpawnData").copy()); // Copy some default data into new data
      newLogic.put("SpawnPotentials",savedLogic.get("SpawnPotentials").copy());
      short oldDelay = savedLogic.getShort("Delay");
      short maxDelay = newLogic.getShort("MaxSpawnDelay");
      newLogic.putShort("Delay", (short) Math.min(oldDelay,maxDelay));
      
      logic.readNbt(world,spawnerPos,newLogic); // Inject new data
      logic.serverTick((ServerWorld)world, spawnerPos); // Tick with new data
      short newDelay = logic.writeNbt(new NbtCompound()).getShort("Delay");
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
   
   public MagicItem getMagicItem(){
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
   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("arcanaUuid")) {
         this.uuid = nbt.getString("arcanaUuid");
      }
      if (nbt.contains("crafterId")) {
         this.crafterId = nbt.getString("crafterId");
      }
      if (nbt.contains("customName")) {
         this.customName = nbt.getString("customName");
      }
      if (nbt.contains("synthetic")) {
         this.synthetic = nbt.getBoolean("synthetic");
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
      this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
      if (!this.readLootTable(nbt)) {
         Inventories.readNbt(nbt, this.inventory);
      }
      if (nbt.contains("active")) {
         this.active = nbt.getBoolean("active");
      }
      if (nbt.contains("soulstone")) {
         this.soulstone = ItemStack.fromNbt(nbt.getCompound("soulstone"));
      }
      if (nbt.contains("points")) {
         this.points = nbt.getInt("points");
      }
      if (nbt.contains("spentPoints")) {
         this.spentPoints = nbt.getInt("spentPoints");
      }
      
      if (nbt.contains("spawnerStats")) {
         NbtCompound stats = nbt.getCompound("spawnerStats");
         setSpawnerStats(stats);
      }
   }
   
   @Override
   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
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
      if (!this.writeLootTable(nbt)) {
         Inventories.writeNbt(nbt, this.inventory);
      }
      
      nbt.putInt("points",this.points);
      nbt.putInt("spentPoints",this.spentPoints);
      nbt.putBoolean("active",this.active);
      if(this.soulstone != null){
         nbt.put("soulstone",soulstone.writeNbt(new NbtCompound()));
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
         this.minSpawnDelay = stats.getShort("MinSpawnDelay");
      }
      if(stats.contains("MaxSpawnDelay")){
         this.maxSpawnDelay = stats.getShort("MaxSpawnDelay");
      }
      if(stats.contains("SpawnCount")){
         this.spawnCount = stats.getShort("SpawnCount");
      }
      if(stats.contains("MaxNearbyEntities")){
         this.maxEntities = stats.getShort("MaxNearbyEntities");
      }
      if(stats.contains("RequiredPlayerRange")){
         this.playerRange = stats.getShort("RequiredPlayerRange");
      }
      if(stats.contains("SpawnRange")){
         this.spawnRange = stats.getShort("SpawnRange");
      }
   }
   
   protected DefaultedList<ItemStack> getHeldStacks() {
      return this.inventory;
   }
   
   @Override
   protected DefaultedList<ItemStack> method_11282() {
      return this.inventory;
   }
   
   protected void setInvStackList(DefaultedList<ItemStack> list) {
      this.inventory = list;
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
}
