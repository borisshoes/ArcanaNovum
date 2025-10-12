package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.XPLoginCallback;
import net.borisshoes.arcananovum.callbacks.login.ColliderLoginCallback;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public class IgneousColliderBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   
   private TreeMap<ArcanaAugment,Integer> augments;
   private int cooldown;
   private String crafterId;
   private String uuid;
   private int origin;
   private String customName;
   
   public IgneousColliderBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.IGNEOUS_COLLIDER_BLOCK_ENTITY, pos, state);
   }
   
   public void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, int origin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.customName = customName == null ? "" : customName;
      int injectionLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.MAGMATIC_INJECTION.id);
      this.cooldown = 20 * (IgneousCollider.COOLDOWN-1-2*injectionLvl);
   }
   
   public static <E extends BlockEntity> void ticker(World world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof IgneousColliderBlockEntity collider){
         collider.tick();
      }
   }
   
   private void tick(){
      if(!(this.world instanceof ServerWorld serverWorld)){
         return;
      }
      
      if(cooldown > 0) cooldown--;
      
      if(serverWorld.getServer().getTicks() % 20 == 0){ // Block is active
         ArcanaNovum.addActiveBlock(new Pair<>(this, this));
      }
      
      if(cooldown <= 0 && serverWorld.getServer().getTicks() % 2 == 0){
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
            }else if(direction == Direction.UP){ // Check for chest
               if(block2 instanceof InventoryProvider){
                  output = ((InventoryProvider)block2).getInventory(state2, serverWorld, pos2);
               } else if(state2.hasBlockEntity()){
                  BlockEntity blockEntity = serverWorld.getBlockEntity(pos2);
                  if(blockEntity instanceof Inventory){
                     output = (Inventory)blockEntity;
                     if(output instanceof ChestBlockEntity && block2 instanceof ChestBlock){
                        output = ChestBlock.getInventory((ChestBlock)block2, state2, serverWorld, pos2, true);
                     }
                  }
               }
               if(output != null){
                  hasInventory = pos2;
               }
            }else if(direction == Direction.DOWN){ // Check for netherite block
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
            
            if(crafterId != null && !crafterId.isEmpty()){
               ServerPlayerEntity player = serverWorld.getServer().getPlayerManager().getPlayer(AlgoUtils.getUUID(crafterId));
               if(player == null){
                  BorisLib.addLoginCallback(new ColliderLoginCallback(serverWorld.getServer(),crafterId,1));
                  BorisLib.addLoginCallback(new XPLoginCallback(serverWorld.getServer(),crafterId,ArcanaConfig.getInt(ArcanaRegistry.IGNEOUS_COLLIDER_PRODUCE)));
               }else{
                  ArcanaAchievements.progress(player,ArcanaAchievements.ENDLESS_EXTRUSION.id,1);
                  ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.IGNEOUS_COLLIDER_PRODUCE));
                  if(obby.isOf(Items.CRYING_OBSIDIAN)) ArcanaAchievements.grant(player,ArcanaAchievements.EXPENSIVE_INFUSION.id);
               }
            }
            
            SoundUtils.playSound(serverWorld,pos, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 1, .6f);
            world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(getCachedState()));
            int injectionLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.MAGMATIC_INJECTION.id);
            cooldown = 20 * (IgneousCollider.COOLDOWN-1-2*injectionLvl);
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
   
   public int getOrigin(){
      return origin;
   }
   
   public String getCustomArcanaName(){
      return customName;
   }
   
   public ArcanaItem getArcanaItem(){
      return ArcanaRegistry.IGNEOUS_COLLIDER;
   }
   
   @Override
   public void readData(ReadView view){
      super.readData(view);
      this.uuid = view.getString(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getString(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getString(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getInt(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getInt("cooldown", 0);
      this.augments = new TreeMap<>();
      view.read("arcanaAugments",ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void writeData(WriteView view){
      super.writeData(view);
      view.putNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt("cooldown",this.cooldown);
   }
}