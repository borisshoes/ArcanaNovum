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
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.datastorage.InterdictionZones;
import net.borisshoes.arcananovum.entities.NulGuardianEntity;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.borisshoes.borislib.utils.MathUtils;
import net.borisshoes.borislib.utils.ParticleEffectUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;
import java.util.UUID;


public class InterdictorBlockEntity extends BlockEntity implements PolymerObject, ArcanaBlockEntity {
   private TreeMap<ArcanaAugment, Integer> augments;
   private String crafterId;
   private String uuid;
   private int origin;
   private ArcanaSkin skin;
   private String customName;
   private final Multiblock multiblock;
   private int xRange = getMaxRange(0);
   private int yRange = getMaxRange(0);
   private int zRange = getMaxRange(0);
   private boolean decoalescence, redirect, wasActive;
   private AABB interdictionZone;
   
   public InterdictorBlockEntity(BlockPos pos, BlockState state){
      super(ArcanaRegistry.INTERDICTOR_BLOCK_ENTITY, pos, state);
      this.multiblock = ((MultiblockCore) ArcanaRegistry.INTERDICTOR).getMultiblock();
   }
   
   public void initialize(TreeMap<ArcanaAugment, Integer> augments, String crafterId, String uuid, int origin, ArcanaSkin skin, @Nullable String customName){
      this.augments = augments;
      this.crafterId = crafterId;
      this.uuid = uuid;
      this.origin = origin;
      this.skin = skin;
      this.customName = customName == null ? "" : customName;
      
      int riftLvl = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.NATAL_RIFT);
      this.xRange = getMaxRange(riftLvl);
      this.yRange = getMaxRange(riftLvl);
      this.zRange = getMaxRange(riftLvl);
      this.decoalescence = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.DECOALESCENCE) > 0;
      this.redirect = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.COALESCENCE_REDIRECTION) > 0;
      BlockPos here = this.getBlockPos();
      this.interdictionZone = new AABB(here.getX() - xRange, here.getY() - yRange, here.getZ() - zRange, here.getX() + xRange, here.getY() + yRange, here.getZ() + zRange);
   }
   
   public static <E extends BlockEntity> void ticker(Level world, BlockPos blockPos, BlockState blockState, E e){
      if(e instanceof InterdictorBlockEntity interdictor){
         interdictor.tick(world, blockPos, blockState);
      }
   }
   
   public int getMaxRange(int riftLvl){
      int baseRange = ArcanaNovum.CONFIG.getInt(ArcanaConfig.INTERDICTOR_RANGE);
      int extraRange = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.INTERDICTOR_RANGE_PER_LVL).get(riftLvl);
      return baseRange + extraRange;
   }
   
   private void tick(Level world, BlockPos blockPos, BlockState blockState){
      if(!(world instanceof ServerLevel serverWorld)){
         return;
      }
      
      boolean active = blockState.getValue(Interdictor.InterdictorBlock.ACTIVE);
      
      if(serverWorld.getServer().getTickCount() % 20 == 0){
         boolean assembled = isAssembled();
         boolean hasRedstone = serverWorld.hasNeighborSignal(blockPos);
         if(hasRedstone && !active && assembled){
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
            serverWorld.setBlock(blockPos, blockState.setValue(Interdictor.InterdictorBlock.ACTIVE, true), Block.UPDATE_ALL);
            active = true;
         }else if(active && (!hasRedstone || !isAssembled())){
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, worldPosition, GameEvent.Context.of(getBlockState()));
            serverWorld.setBlock(blockPos, blockState.setValue(Interdictor.InterdictorBlock.ACTIVE, false), Block.UPDATE_ALL);
            active = false;
         }
         
         if(active && decoalescence){
            UUID crafterUUID = AlgoUtils.getUUID(this.getCrafterId());
            for(Entity entity : serverWorld.getEntities(null, getInterdictionZone())){
               if(entity instanceof Enemy && entity.isAlive() && !entity.hasCustomName()){
                  if(!entity.is(ArcanaRegistry.INTERDICTOR_IMMUNE)){
                     entity.discard();
                     if(this.getCrafterId() != null && !this.getCrafterId().isEmpty())
                        ArcanaAchievements.progress(crafterUUID, ArcanaAchievements.UNMOBBED, 1);
                  }else if(serverWorld.getServer().getTickCount() % 40 == 0 && entity.getType() == ArcanaRegistry.NUL_GUARDIAN_ENTITY && entity instanceof NulGuardianEntity guardian){
                     guardian.hurtServer(serverWorld, serverWorld.damageSources().magic(), 12.0f);
                  }
                  
               }
            }
         }
         if(active && assembled){
            ArcanaNovum.addActiveBlock(new Tuple<>(this, this));
            DataAccess.getWorld(this.level.dimension(), InterdictionZones.KEY).addOrRefreshZone(getInterdictionZone(), blockPos, 25, this.redirect);
         }
      }
      if(active && serverWorld.getServer().getTickCount() % 70 == 50){
         SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 2, 0.7f);
      }
      if(active && serverWorld.getServer().getTickCount() % 200 == 0){
         ArcanaEffectUtils.interdictionRing(serverWorld, blockPos, 0);
         SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1, 1.5f);
      }
      
      if(active && !wasActive){
         SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1, 1.5f);
         ArcanaEffectUtils.interdictionRing(serverWorld, blockPos, 0);
         wasActive = true;
      }else if(!active && wasActive){
         SoundUtils.playSound(serverWorld, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1, 1.5f);
         wasActive = false;
      }
   }
   
   public void onSpawn(){
      if(this.crafterId != null && !this.crafterId.isEmpty()){
         ArcanaAchievements.progress(AlgoUtils.getUUID(this.crafterId), ArcanaAchievements.UNMOBBED, 1);
         if(this.level instanceof ServerLevel && this.level.getRandom().nextFloat() < 0.01)
            ArcanaNovum.data(AlgoUtils.getUUID(this.crafterId)).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_INTERDICTOR_MOB_BLOCKED_PER_100));
      }
      if(this.level instanceof ServerLevel serverLevel && this.level.getRandom().nextFloat() < 0.005){
         Vec3 p1 = this.getBlockPos().getBottomCenter().add(this.level.getRandom().nextBoolean() ? -1 : 1, 1.85, this.level.getRandom().nextBoolean() ? -1 : 1);
         Vec3 p2 = MathUtils.randomSpherePoint(p1, 5, 3);
         ParticleEffectUtils.animatedLightningBolt(serverLevel, p1, p2,
               this.level.getRandom().nextInt(4, 8), 1.0, ParticleTypes.WITCH,
               8, 1, 0, 0, false, 0, 20);
         SoundUtils.playSound(this.level, worldPosition, SoundEvents.TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundSource.BLOCKS, 0.3f, 0.5f + this.level.getRandom().nextFloat() * 0.3f);
         SoundUtils.playSound(this.level, worldPosition, SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 0.3f, 1.5f + this.level.getRandom().nextFloat() * 0.3f);
      }
   }
   
   public AABB getInterdictionZone(){
      return this.interdictionZone;
   }
   
   public void recalculateZone(){
      BlockPos here = this.getBlockPos();
      this.interdictionZone = new AABB(here.getX() - xRange, here.getY() - yRange, here.getZ() - zRange, here.getX() + xRange, here.getY() + yRange, here.getZ() + zRange);
   }
   
   @Override
   public boolean isAssembled(){
      return multiblock.matches(getMultiblockCheck());
   }
   
   public Multiblock.MultiblockCheck getMultiblockCheck(){
      if(!(this.level instanceof ServerLevel serverWorld)){
         return null;
      }
      return new Multiblock.MultiblockCheck(serverWorld, worldPosition, serverWorld.getBlockState(worldPosition), new BlockPos(((MultiblockCore) ArcanaRegistry.INTERDICTOR).getCheckOffset()), null);
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
      return ArcanaRegistry.INTERDICTOR;
   }
   
   public int getxRange(){
      return xRange;
   }
   
   public int getyRange(){
      return yRange;
   }
   
   public int getzRange(){
      return zRange;
   }
   
   public void setxRange(int xRange){
      int riftLvl = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.NATAL_RIFT);
      this.xRange = Mth.clamp(xRange, 1, getMaxRange(riftLvl));
      recalculateZone();
      setChanged();
   }
   
   public void setyRange(int yRange){
      int riftLvl = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.NATAL_RIFT);
      this.yRange = Mth.clamp(yRange, 1, getMaxRange(riftLvl));
      recalculateZone();
      setChanged();
   }
   
   public void setzRange(int zRange){
      int riftLvl = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.NATAL_RIFT);
      this.zRange = Mth.clamp(zRange, 1, getMaxRange(riftLvl));
      recalculateZone();
      setChanged();
   }
   
   @Override
   public void loadAdditional(ValueInput view){
      super.loadAdditional(view);
      this.uuid = view.getStringOr(ArcanaBlockEntity.ARCANA_UUID_TAG, "");
      this.crafterId = view.getStringOr(ArcanaBlockEntity.CRAFTER_ID_TAG, "");
      this.customName = view.getStringOr(ArcanaBlockEntity.CUSTOM_NAME, "");
      this.skin = ArcanaSkin.getSkinFromString(view.getStringOr(ArcanaBlockEntity.SKIN_TAG, ""));
      this.origin = view.getIntOr(ArcanaBlockEntity.ORIGIN_TAG, 0);
      this.xRange = view.getIntOr("xRange", getMaxRange(0));
      this.yRange = view.getIntOr("yRange", getMaxRange(0));
      this.zRange = view.getIntOr("zRange", getMaxRange(0));
      this.augments = new TreeMap<>();
      view.read(ArcanaBlockEntity.AUGMENT_TAG, ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      this.decoalescence = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.DECOALESCENCE) > 0;
      this.redirect = ArcanaAugments.getAugmentFromMap(this.augments, ArcanaAugments.COALESCENCE_REDIRECTION) > 0;
      
      BlockPos here = this.getBlockPos();
      this.interdictionZone = new AABB(here.getX() - xRange, here.getY() - yRange, here.getZ() - zRange, here.getX() + xRange, here.getY() + yRange, here.getZ() + zRange);
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
      view.putInt("xRange", this.xRange);
      view.putInt("yRange", this.yRange);
      view.putInt("zRange", this.zRange);
   }
}
