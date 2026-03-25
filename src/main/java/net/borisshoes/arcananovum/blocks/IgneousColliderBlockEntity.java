package net.borisshoes.arcananovum.blocks;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.UUID;

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
      this.cooldown = getBaseCooldown();
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof IgneousColliderBlockEntity collider){
         collider.tick();
      }
   }
   
   private int getBaseCooldown(){
      int injectionLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.MAGMATIC_INJECTION);
      int baseCooldown = ArcanaNovum.CONFIG.getInt(ArcanaConfig.IGNEOUS_COLLIDER_COOLDOWN);
      int cooldownReduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.IGNEOUS_COLLIDER_COOLDOWN_PER_LVL).get(injectionLvl);
      return Math.max(1, baseCooldown - cooldownReduction);
   }
   
   private void tick(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return;
      }
      
      if(cooldown > 0) cooldown--;
      
      if(serverWorld.getServer().getTickCount() % 20 == 0){ // Block is active
         ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
      }
      
      if(cooldown <= 0 && serverWorld.getServer().getTickCount() % 2 == 0){
         // Do the check
         BlockPos hasLava = null;
         BlockPos hasWater = null;
         BlockPos hasInventory = null;
         BlockPos hasNetherite = null;
         Container output = null;
         boolean canUseIce = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.CRYOGENIC_COOLING) >= 1;
         
         Direction[] dirs = Direction.values();
         int numDirs = dirs.length;
         
         for(int side = 0; side < numDirs; ++side){
            Direction direction = dirs[side];
            BlockPos pos2 = worldPosition.relative(direction);
            BlockState state2 = serverWorld.getBlockState(pos2);
            Block block2 = state2.getBlock();
            
            if(direction.getAxis() != Direction.Axis.Y){ // Check for fluid
               if(block2 == Blocks.LAVA && state2.getFluidState().isSource()){
                  hasLava = pos2;
               }else if(block2 == Blocks.WATER  && state2.getFluidState().isSource()){
                  hasWater = pos2;
               }else if(block2 == Blocks.LAVA_CAULDRON){
                  hasLava = pos2;
               }else if(block2 == Blocks.WATER_CAULDRON){
                  hasWater = pos2;
               }else if(canUseIce && block2 == Blocks.BLUE_ICE){
                  hasWater = pos2;
               }
            }else if(direction == Direction.UP){ // Check for chest
               if(block2 instanceof WorldlyContainerHolder){
                  output = ((WorldlyContainerHolder)block2).getContainer(state2, serverWorld, pos2);
               } else if(state2.hasBlockEntity()){
                  BlockEntity blockEntity = serverWorld.getBlockEntity(pos2);
                  if(blockEntity instanceof Container){
                     output = (Container)blockEntity;
                     if(output instanceof ChestBlockEntity && block2 instanceof ChestBlock){
                        output = ChestBlock.getContainer((ChestBlock)block2, state2, serverWorld, pos2, true);
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
               serverWorld.addFreshEntity(new ItemEntity(serverWorld, worldPosition.getX()+0.5, worldPosition.getY()+1.25, worldPosition.getZ()+0.5,obby, 0, 0.2, 0));
            }else{ // Put in inventory
               
               try{
                  Transaction transaction = Transaction.openOuter();
                  int inserted = (int) StorageUtil.tryInsertStacking(InventoryStorage.of(output, Direction.DOWN), ItemVariant.of(obby),obby.getCount(), transaction);
                  if(inserted < obby.getCount()){
                     obby.setCount(obby.getCount() - inserted);
                     serverWorld.addFreshEntity(new ItemEntity(serverWorld, worldPosition.getX()+0.5, worldPosition.getY()+2.5, worldPosition.getZ()+0.5,obby, 0, 0.2, 0));
                  }
                  if(inserted > 0){
                     output.setChanged();
                  }
                  transaction.commit();
                  
               }catch(Exception e){
                  ArcanaNovum.log(2,"Exception in Igneous Collider inventory insertion at "+this.worldPosition.toShortString());
                  e.printStackTrace();
               }
            }
            
            // Remove Source Blocks
            int efficiencyLvl = ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.THERMAL_EXPANSION);
            double efficiency = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.IGNEOUS_COLLIDER_EFFICIENCY_PER_LVL).get(efficiencyLvl);
            if(Math.random() >= efficiency){
               if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA){
                  serverWorld.setBlock(hasLava, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
               }else if(serverWorld.getBlockState(hasLava).getBlock() == Blocks.LAVA_CAULDRON){
                  serverWorld.setBlock(hasLava, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
               }
               if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER){
                  serverWorld.setBlock(hasWater, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
               }else if(serverWorld.getBlockState(hasWater).getBlock() == Blocks.WATER_CAULDRON){
                  serverWorld.setBlock(hasWater, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
               }
            }
            
            if(crafterId != null && !crafterId.isEmpty()){
               UUID parsedId = AlgoUtils.getUUID(crafterId);
               ArcanaPlayerData profile = ArcanaNovum.data(parsedId);
               ArcanaAchievements.progress(parsedId,ArcanaAchievements.ENDLESS_EXTRUSION,1);
               profile.addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_IGNEOUS_COLLIDER_PRODUCE));
               if(obby.is(Items.CRYING_OBSIDIAN)) ArcanaAchievements.grant(parsedId,ArcanaAchievements.EXPENSIVE_INFUSION);
            }
            
            SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.BLOCKS, 1, .6f);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
            this.cooldown = getBaseCooldown();
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
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.cooldown = view.getIntOr("cooldown", 0);
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
   }
   
   @Override
   protected void saveAdditional(ValueOutput view){
      super.saveAdditional(view);
      view.storeNullable(ArcanaBlockEntity.AUGMENT_TAG,ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC,this.augments);
      view.putString(ArcanaBlockEntity.ARCANA_UUID_TAG,this.uuid == null ? "" : this.uuid);
      view.putString(ArcanaBlockEntity.CRAFTER_ID_TAG,this.crafterId == null ? "" : this.crafterId);
      view.putString(ArcanaBlockEntity.CUSTOM_NAME,this.customName == null ? "" : this.customName);
      view.putInt(ArcanaBlockEntity.ORIGIN_TAG,this.origin);
      view.putInt("cooldown",this.cooldown);
   }
}