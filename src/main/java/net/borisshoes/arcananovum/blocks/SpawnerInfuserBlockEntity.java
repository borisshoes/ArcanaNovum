package net.borisshoes.arcananovum.blocks;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.utils.SoundUtils;
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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
         try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getReporterContext(), LogUtils.getLogger())){
            NbtWriteView nbtWriteView = NbtWriteView.create(logging, this.getWorld().getRegistryManager());
            spawnerEntity.getLogic().writeData(nbtWriteView);
            NbtCompound spawnerData = nbtWriteView.getNbt();
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
               ArcanaEffectUtils.spawnerInfuser(serverWorld,pos,5);
               SoundUtils.soulSounds(serverWorld,pos,1,5);
            }
         }
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0 && this.active){
         ArcanaNovum.addActiveBlock(new Pair<>(this,this));
      }
   }
   
   public void tickInfuser(BlockPos spawnerPos, MobSpawnerBlockEntity spawnerEntity){
      MobSpawnerLogic logic = spawnerEntity.getLogic();
      try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.getReporterContext(), LogUtils.getLogger())){
         NbtWriteView nbtWriteView = NbtWriteView.create(logging, this.getWorld().getRegistryManager());
         logic.writeData(nbtWriteView);
         NbtCompound savedLogic = nbtWriteView.getNbt(); // Save default data
         NbtCompound newLogic = getSpawnerStats().copy(); // Get data from infuser
         
         newLogic.put("SpawnData",savedLogic.get("SpawnData").copy()); // Copy some default data into new data
         newLogic.put("SpawnPotentials",savedLogic.get("SpawnPotentials").copy());
         short oldDelay = savedLogic.getShort("Delay", (short) 0);
         short maxDelay = newLogic.getShort("MaxSpawnDelay", (short) 0);
         newLogic.putShort("Delay", (short) Math.min(oldDelay,maxDelay));
         
         ReadView newNbtReadView = NbtReadView.create(logging, this.getWorld().getRegistryManager(),newLogic);
         logic.readData(world,spawnerPos,newNbtReadView); // Inject new data
         if(world instanceof ServerWorld serverWorld) logic.serverTick(serverWorld, spawnerPos); // Tick with new data
         NbtWriteView nbtWriteView2 = NbtWriteView.create(logging, this.getWorld().getRegistryManager());
         logic.writeData(nbtWriteView2);
         short newDelay = nbtWriteView2.getNbt().getShort("Delay", (short) 0);
         savedLogic.putShort("Delay",newDelay); // Extract new delay and put in saved data
         ReadView savedNbtReadView = NbtReadView.create(logging, this.getWorld().getRegistryManager(),savedLogic);
         logic.readData(world,spawnerPos,savedNbtReadView); // Return saved default data with new delay
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
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString("arcanaUuid", "");
      this.crafterId = view.getString("crafterId", "");
      this.customName = view.getString("customName", "");
      this.synthetic = view.getBoolean("synthetic", false);
      this.active = view.getBoolean("active", false);
      this.points = view.getInt("points", 0);
      this.spentPoints = view.getInt("spentPoints", 0);
      this.soulstone = ItemStack.EMPTY;
      view.read("soulstone",ItemStack.CODEC).ifPresent(stack -> {
         this.soulstone = stack;
      });
      
      
      
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.inventory = new SimpleInventory(size());
      if (!this.readLootTable(view)) {
         Inventories.readData(view, this.inventory.getHeldStacks());
      }
      
      view.read("spawnerStats",SpawnerStats.CODEC).ifPresent(stats -> {
         setSpawnerStats(stats.toNbt());
      });
   }
   
   
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString("arcanaUuid",this.uuid == null ? "" : this.uuid);
      view.putString("crafterId",this.crafterId == null ? "" : this.crafterId);
      view.putString("customName",this.customName == null ? "" : this.customName);
      view.putBoolean("synthetic",this.synthetic);
      view.putInt("points",this.points);
      view.putInt("spentPoints",this.spentPoints);
      view.putBoolean("active",this.active);
      if (!this.writeLootTable(view)) {
         Inventories.writeData(view, this.inventory.getHeldStacks());
      }
      
      if(this.soulstone != null && !this.soulstone.isEmpty()){
         view.putNullable("soulstone",ItemStack.CODEC,this.soulstone);
      }
      
      view.putNullable("spawnerStats",SpawnerStats.CODEC,SpawnerStats.fromNbt(getSpawnerStats()));
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
   
   public record SpawnerStats(short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange, short spawnRange) {
      public static final Codec<SpawnerStats> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.SHORT.fieldOf("MinSpawnDelay").forGetter(s -> s.minSpawnDelay), Codec.SHORT.fieldOf("MaxSpawnDelay").forGetter(s -> s.maxSpawnDelay), Codec.SHORT.fieldOf("SpawnCount").forGetter(s -> s.spawnCount), Codec.SHORT.fieldOf("MaxNearbyEntities").forGetter(s -> s.maxNearbyEntities), Codec.SHORT.fieldOf("RequiredPlayerRange").forGetter(s -> s.requiredPlayerRange), Codec.SHORT.fieldOf("SpawnRange").forGetter(s -> s.spawnRange)).apply(i, SpawnerStats::new));
      
      public NbtCompound toNbt(){
         NbtCompound n = new NbtCompound();
         n.putShort("MinSpawnDelay", this.minSpawnDelay);
         n.putShort("MaxSpawnDelay", this.maxSpawnDelay);
         n.putShort("SpawnCount", this.spawnCount);
         n.putShort("MaxNearbyEntities", this.maxNearbyEntities);
         n.putShort("RequiredPlayerRange", this.requiredPlayerRange);
         n.putShort("SpawnRange", this.spawnRange);
         return n;
      }
      
      public static SpawnerStats fromNbt(NbtCompound n){
         return new SpawnerStats(
               n.getShort("MinSpawnDelay",(short) 0),
               n.getShort("MaxSpawnDelay",(short) 0),
               n.getShort("SpawnCount",(short) 0),
               n.getShort("MaxNearbyEntities",(short) 0),
               n.getShort("RequiredPlayerRange",(short) 0),
               n.getShort("SpawnRange",(short) 0));
      }
   }
}
