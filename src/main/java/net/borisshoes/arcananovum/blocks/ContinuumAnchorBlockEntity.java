package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.AnchorTimeLoginCallback;
import net.borisshoes.arcananovum.callbacks.XPLoginCallback;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ContinuumAnchorBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   private static final double[] anchorEfficiency = {0,.05,.1,.15,.2,.5};
   public static final int RANGE = 2;
   private TreeMap<ArcanaAugment,Integer> augments;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   private int fuel;
   private boolean active;
   private ItemStack fuelStack;
   
   public ContinuumAnchorBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.CONTINUUM_ANCHOR_BLOCK_ENTITY, pos, state);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      this.fuel = 0;
      this.active = false;
      this.fuelStack = ItemStack.EMPTY.copy();
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
      return fuelStack;
   }
   
   public int getFuel(){
      return fuel;
   }
   
   public MagicItem getMagicItem(){
      return ArcanaRegistry.CONTINUUM_ANCHOR;
   }
   
   public boolean interact(PlayerEntity player, ItemStack stack){
      if(fuel > 0 && stack.isEmpty()){ // Remove fuel
         fuel = 0;
         ItemStack returnStack = fuelStack.copy();
         fuelStack = ItemStack.EMPTY;
         markDirty();
         if(!player.giveItemStack(returnStack)){
            ItemEntity itemEntity = player.dropItem(returnStack, false);
            if (itemEntity == null) return true;
            itemEntity.resetPickupDelay();
            itemEntity.setOwner(player.getUuid());
         }
         return true;
      }
      
      //Add Fuel
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      if(fuel == 0 && magicItem instanceof ExoticMatter matter){
         fuel = matter.getEnergy(stack);
         fuelStack = stack.copy();
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
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 5 == 0){ // Anchor only ticks redstone and load update every quarter second
         ChunkPos chunkPos = new ChunkPos(pos);
         boolean prevActive = active;
         active = !serverWorld.isReceivingRedstonePower(pos) && fuel > 0;
         
         
         if(active && serverWorld.getServer().getTicks() % 20 == 0){
            int lvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.TEMPORAL_RELATIVITY.id);
            if(Math.random() >= anchorEfficiency[lvl]){
               fuel = Math.max(0, fuel - 1);
               
               if(MagicItemUtils.identifyItem(fuelStack) instanceof ExoticMatter matter){
                  matter.setFuel(fuelStack, fuel);
                  markDirty();
               }
            }
            
            if(crafterId != null && !crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(crafterId));
               if(player == null){
                  ArcanaNovum.addLoginCallback(new AnchorTimeLoginCallback(serverWorld.getServer(),crafterId,1));
                  if(serverWorld.getServer().getTicks() % 100 == 0) ArcanaNovum.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,1));
               }else{
                  ArcanaAchievements.progress(player,ArcanaAchievements.TIMEY_WIMEY.id, 1);
                  if(serverWorld.getServer().getTicks() % 100 == 0) PLAYER_DATA.get(player).addXP(1);
               }
            }
         }
         int fuelMarks = (int)Math.min(Math.ceil(4.0*fuel/600000.0),4);
         BlockState blockState = world.getBlockState(pos).with(ContinuumAnchor.ContinuumAnchorBlock.ACTIVE,active).with(ContinuumAnchor.ContinuumAnchorBlock.CHARGES,fuelMarks);
         world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
         
         // Do the chunk loading thing
         if(prevActive && !active){ // Power Down
            ArcanaNovum.removeActiveAnchor(serverWorld,pos);
            for(int i = -RANGE; i <= RANGE; i++){
               for(int j = -RANGE; j <= RANGE; j++){
                  ContinuumAnchor.removeChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
            SoundUtils.playSound(serverWorld,pos,SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS,1,1.5f);
         }else if(!prevActive && active){ // Power Up
            ArcanaNovum.addActiveAnchor(serverWorld,pos);
            for(int i = -RANGE; i <= RANGE; i++){
               for(int j = -RANGE; j <= RANGE; j++){
                  ContinuumAnchor.addChunk(serverWorld,new ChunkPos(chunkPos.x+i,chunkPos.z+j));
               }
            }
            SoundUtils.playSound(serverWorld,pos,SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS,1,0.7f);
         }
         
         this.markDirty();
      }
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
      if (nbt.contains("fuel")) {
         this.fuel = nbt.getInt("fuel");
      }
      if (nbt.contains("active")) {
         this.active = nbt.getBoolean("active");
      }
      if (nbt.contains("fuelStack")) {
         this.fuelStack = ItemStack.fromNbt(nbt.getCompound("fuelStack"));
      }
      augments = new TreeMap<>();
      if(nbt.contains("arcanaAugments")){
         NbtCompound augCompound = nbt.getCompound("arcanaAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
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
      nbt.putInt("fuel",this.fuel);
      nbt.putBoolean("active",this.active);
      if(this.fuelStack != null){
         nbt.put("fuelStack",fuelStack.writeNbt(new NbtCompound()));
      }
   }
}
