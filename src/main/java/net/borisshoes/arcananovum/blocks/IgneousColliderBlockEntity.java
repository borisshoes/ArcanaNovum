package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ColliderLoginCallback;
import net.borisshoes.arcananovum.callbacks.XPLoginCallback;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicBlockEntity;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class IgneousColliderBlockEntity extends BlockEntity implements PolymerObject, MagicBlockEntity {
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private int cooldown;
   private String crafterId;
   private String uuid;
   private boolean synthetic;
   private String customName;
   
   public IgneousColliderBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.IGNEOUS_COLLIDER_BLOCK_ENTITY, pos, state);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.synthetic = synthetic;
      this.customName = customName == null ? "" : customName;
      int injectionLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.MAGMATIC_INJECTION.id);
      this.cooldown = IgneousCollider.COOLDOWN-1-2*injectionLvl;
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof IgneousColliderBlockEntity collider){
         collider.tick();
      }
   }
   
   private void tick(){
      if (!(this.world instanceof ServerWorld serverWorld)) {
         return;
      }
      
      if(serverWorld.getServer().getTicks() % 20 == 0){ // Tick the block every second
         if(cooldown-- <= 0){
            // Do the check
            BlockPos hasLava = null;
            BlockPos hasWater = null;
            BlockPos hasInventory = null;
            BlockPos hasNetherite = null;
            Inventory output = null;
            boolean canUseIce = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.CRYOGENIC_COOLING.id) >= 1;
            
            Direction[] dirs = Direction.values();
            int numDirs = dirs.length;
            
            for(int side = 0; side < numDirs; ++side){
               Direction direction = dirs[side];
               BlockPos pos2 = pos.offset(direction);
               BlockState state2 = serverWorld.getBlockState(pos2);
               Block block2 = state2.getBlock();
               
               if(direction.getAxis() != Direction.Axis.Y){ // Check for fluid
                  if(block2 == Blocks.LAVA && state2.getFluidState().isStill()){
                     hasLava = pos2;
                  }else if(block2 == Blocks.WATER  && state2.getFluidState().isStill()){
                     hasWater = pos2;
                  }else if(block2 == Blocks.LAVA_CAULDRON){
                     hasLava = pos2;
                  }else if(block2 == Blocks.WATER_CAULDRON){
                     hasWater = pos2;
                  }else if(canUseIce && block2 == Blocks.BLUE_ICE){
                     hasWater = pos2;
                  }
               }else if(direction.getId() == 1){ // Check for chest
                  if (block2 instanceof InventoryProvider) {
                     output = ((InventoryProvider)block2).getInventory(state2, serverWorld, pos2);
                  } else if (state2.hasBlockEntity()) {
                     BlockEntity blockEntity = serverWorld.getBlockEntity(pos2);
                     if (blockEntity instanceof Inventory) {
                        output = (Inventory)blockEntity;
                        if (output instanceof ChestBlockEntity && block2 instanceof ChestBlock) {
                           output = ChestBlock.getInventory((ChestBlock)block2, state2, serverWorld, pos2, true);
                        }
                     }
                  }
                  if (output != null){
                     hasInventory = pos2;
                  }
               }else if(direction.getId() == 0){ //Check for netherite block
                  if(block2 == Blocks.NETHERITE_BLOCK){
                     hasNetherite = pos2;
                  }
               }
            }
            if(hasLava != null && hasWater != null){ // Produce Obsidian
               ItemStack obby;
               if(hasNetherite == null){
                  obby = new ItemStack(Items.OBSIDIAN);
               }else{
                  obby = new ItemStack(Items.CRYING_OBSIDIAN);
               }
               
               if(hasInventory == null){ // Drop above collider
                  serverWorld.spawnEntity(new ItemEntity(serverWorld,pos.getX()+0.5,pos.getY()+1.25,pos.getZ()+0.5,obby, 0, 0.2, 0));
               }else{ // Put in inventory
                  
                  try{
                     Transaction transaction = Transaction.openOuter();
                     int inserted = (int) StorageUtil.tryInsertStacking(InventoryStorage.of(output,Direction.DOWN), ItemVariant.of(obby),obby.getCount(), transaction);
                     if(inserted < obby.getCount()){
                        obby.setCount(obby.getCount() - inserted);
                        serverWorld.spawnEntity(new ItemEntity(serverWorld,pos.getX()+0.5,pos.getY()+2.5,pos.getZ()+0.5,obby, 0, 0.2, 0));
                     }
                     if(inserted > 0){
                        output.markDirty();
                     }
                     transaction.commit();
                     
                  }catch(Exception e){
                     ArcanaNovum.log(2,"Exception in Igneous Collider inventory insertion at "+this.pos.toShortString());
                     e.printStackTrace();
                  }
               }
               
               // Remove Source Blocks
               int efficiencyLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.THERMAL_EXPANSION.id);
               if(Math.random() >= .1*efficiencyLvl){
                  if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA){
                     serverWorld.setBlockState(hasLava, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                  }else if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA_CAULDRON){
                     serverWorld.setBlockState(hasLava, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                  }
                  if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER){
                     serverWorld.setBlockState(hasWater, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                  }else if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER_CAULDRON){
                     serverWorld.setBlockState(hasWater, Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
                  }
               }
               
               SoundUtils.playSound(serverWorld,pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1, .6f);
               
               if(crafterId != null && !crafterId.isEmpty()){
                  ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(UUID.fromString(crafterId));
                  if(player == null){
                     ArcanaNovum.addLoginCallback(new ColliderLoginCallback(serverWorld.getServer(),crafterId,1));
                     ArcanaNovum.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,10));
                  }else{
                     ArcanaAchievements.progress(player,ArcanaAchievements.ENDLESS_EXTRUSION.id,1);
                     PLAYER_DATA.get(player).addXP(10);
                     if(obby.isOf(Items.CRYING_OBSIDIAN)) ArcanaAchievements.grant(player,ArcanaAchievements.EXPENSIVE_INFUSION.id);
                  }
               }
            }
            
            int injectionLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.MAGMATIC_INJECTION.id);
            cooldown = IgneousCollider.COOLDOWN-1-2*injectionLvl;
         }
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
   
   public MagicItem getMagicItem(){
      return ArcanaRegistry.IGNEOUS_COLLIDER;
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
      if (nbt.contains("cooldown")){
         this.cooldown = nbt.getInt("cooldown");
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
      nbt.putInt("cooldown",this.cooldown);
   }
}