package net.borisshoes.arcananovum.mixins;

import com.mojang.logging.LogUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(DispenseItemBehavior.class)
public interface DispenseItemBehaviorMixin {
   
   @Inject(method = "bootStrap", at = @At("TAIL"))
   private static void arcananovum$dispenserInteractions(CallbackInfo ci){
      
      DispenserBlock.registerBehavior(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               public ItemStack execute(BlockSource pointer, ItemStack stack) {
                  int mode = ArcanaItem.getIntProperty(stack,ArcanaItem.MODE_TAG); // 0 place, 1 remove
                  BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                  Level world = pointer.level();
                  BlockState blockState = world.getBlockState(blockPos);
                  if(mode == 1){
                     BucketPickup fluidDrainable;
                     Block block = blockState.getBlock();
                     if(block instanceof BucketPickup && !(fluidDrainable = (BucketPickup) block).pickupBlock(null, world, blockPos, blockState).isEmpty()){
                        world.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                        fluidDrainable.getPickupSound().ifPresent(sound -> SoundUtils.playSound(world,blockPos,sound, SoundSource.BLOCKS,1,1));
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else if(mode == 0 || mode == 2){
                     AquaticEversource.placeFluid(Fluids.WATER, null, world, blockPos, null, false, false);
                     this.setSuccess(true);
                  }else{
                     this.setSuccess(false);
                  }
                  return stack;
               }
      });
      
      DispenserBlock.registerBehavior(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               public ItemStack execute(BlockSource pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource magmaticEversource)){
                     this.setSuccess(false);
                     return stack;
                  }
                  int mode = ArcanaItem.getIntProperty(stack,ArcanaItem.MODE_TAG); // 0 place, 1 remove
                  int charges = ArcanaItem.getIntProperty(stack,MagmaticEversource.USES_TAG);
                  
                  BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                  Level world = pointer.level();
                  BlockState blockState = world.getBlockState(blockPos);
                  if(mode == 1){
                     BucketPickup fluidDrainable;
                     Block block = blockState.getBlock();
                     if(block instanceof BucketPickup && !(fluidDrainable = (BucketPickup) block).pickupBlock(null, world, blockPos, blockState).isEmpty()){
                        world.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                        fluidDrainable.getPickupSound().ifPresent(sound -> SoundUtils.playSound(world,blockPos,sound, SoundSource.BLOCKS,1,1));
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else if(mode == 0 && charges >= 1){
                     MagmaticEversource.placeFluid(Fluids.LAVA, null, world, blockPos, null, false);
                     ArcanaItem.putProperty(stack,MagmaticEversource.USES_TAG,charges-1);
                     magmaticEversource.buildItemLore(stack, world.getServer());
                     this.setSuccess(true);
                  }else{
                     this.setSuccess(false);
                  }
                  return stack;
               }
      });
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.CINDERS_CHARM.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               protected ItemStack execute(BlockSource pointer, ItemStack stack){
                  ServerLevel serverWorld = pointer.level();
                  this.setSuccess(true);
                  Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                  BlockPos blockPos = pointer.pos().relative(direction);
                  BlockState blockState = serverWorld.getBlockState(blockPos);
                  if(BaseFireBlock.canBePlacedAt(serverWorld, blockPos, direction)){
                     serverWorld.setBlockAndUpdate(blockPos, BaseFireBlock.getState(serverWorld, blockPos));
                     serverWorld.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                  }else if(CampfireBlock.canLight(blockState) || CandleBlock.canLight(blockState) || CandleCakeBlock.canLight(blockState)){
                     serverWorld.setBlockAndUpdate(blockPos, blockState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)));
                     serverWorld.gameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
                  }else if(blockState.getBlock() instanceof TntBlock){
                     TntBlock.prime(serverWorld, blockPos);
                     serverWorld.removeBlock(blockPos, false);
                  }else{
                     this.setSuccess(false);
                  }
                  
                  return stack;
               }
            });
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.WILD_GROWTH_CHARM.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               protected ItemStack execute(BlockSource pointer, ItemStack stack){
                  this.setSuccess(true);
                  Level world = pointer.level();
                  BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                  if (!BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL,64), world, blockPos) && !BoneMealItem.growWaterPlant(new ItemStack(Items.BONE_MEAL,64), world, blockPos, null)) {
                     this.setSuccess(false);
                  } else if (!world.isClientSide()) {
                     world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);
                  }
                  
                  return stack;
               }
            });
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.EVERLASTING_ROCKET.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               public ItemStack execute(BlockSource pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof EverlastingRocket rocket)){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  ServerLevel serverWorld = pointer.level();
                  Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                  ProjectileItem.DispenseConfig projectileSettings = ProjectileItem.DispenseConfig.builder().positionFunction(
                        (point, facing) -> point.center().add((double)facing.getStepX() * 0.5000099999997474, (double)facing.getStepY() * 0.5000099999997474, (double)facing.getStepZ() * 0.5000099999997474))
                        .uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
                  Position position = projectileSettings.positionFunction().getDispensePosition(pointer, direction);
                  Projectile.spawnProjectileUsingShoot(
                        ((FireworkRocketItem)rocket.getFireworkStack(stack).getItem()).asProjectile(serverWorld, position, stack, direction),
                        serverWorld,
                        stack,
                        (double)direction.getStepX(),
                        (double)direction.getStepY(),
                        (double)direction.getStepZ(),
                        projectileSettings.power(),
                        projectileSettings.uncertainty()
                  );
                  this.setSuccess(true);
                  return stack;
               }
               
               @Override
               protected void playSound(BlockSource pointer) {
                  ProjectileItem.DispenseConfig projectileSettings = ProjectileItem.DispenseConfig.builder().positionFunction(
                              (point, facing) -> point.center().add((double)facing.getStepX() * 0.5000099999997474, (double)facing.getStepY() * 0.5000099999997474, (double)facing.getStepZ() * 0.5000099999997474))
                        .uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
                  pointer.level().levelEvent(projectileSettings.overrideDispenseEvent().orElse(1002), pointer.pos(), 0);
               }
            }
      );
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.ESSENCE_EGG.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               public ItemStack execute(BlockSource pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof EssenceEgg egg) || EntityType.byString(EssenceEgg.getType(stack)).isEmpty()){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                  
                  try {
                     int splitLevel = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SOUL_SPLIT);
                     int efficiencyLevel = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.DETERMINED_SPIRIT);
                     if(EssenceEgg.getUses(stack) > 0){
                        ServerLevel serverWorld = pointer.level();
                        Vec3 summonPos = Vec3.atBottomCenterOf(pointer.pos().relative(direction));
                        
                        CompoundTag nbtCompound = new CompoundTag();
                        nbtCompound.putString("id", EssenceEgg.getType(stack));
                        int spawns = Math.random() >= 0.1*splitLevel ? 1 : 2;
                        
                        for(int i = 0; i < spawns; i++){
                           Entity newEntity = EntityType.loadEntityRecursive(nbtCompound, serverWorld, EntitySpawnReason.DISPENSER, entity -> {
                              entity.snapTo(summonPos.x(), summonPos.y(), summonPos.z(), entity.getYRot(), entity.getXRot());
                              return entity;
                           });
                           if(newEntity instanceof Mob mobEntity){
                              mobEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(newEntity.blockPosition()), EntitySpawnReason.DISPENSER, null);
                           }
                           serverWorld.tryAddFreshEntityWithPassengers(newEntity);
                        }
                        
                        if(Math.random() >= 0.1*efficiencyLevel){
                           EssenceEgg.addUses(stack,-1);
                        }
                     }
                  } catch (Exception var6) {
                     LOGGER.error("Error while dispensing essence egg from dispenser at {}", pointer.pos(), var6);
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  pointer.level().gameEvent(null, GameEvent.ENTITY_PLACE, pointer.pos());
                  return stack;
               }
            }
      );
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.CONTAINMENT_CIRCLET.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               public ItemStack execute(BlockSource pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof ContainmentCirclet circlet)){
                     this.setSuccess(false);
                     return stack;
                  }
                  BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                  CompoundTag contents = ArcanaItem.getCompoundProperty(stack, ContainmentCirclet.CONTENTS_TAG);
                  boolean hostiles = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CONFINEMENT) > 0;
                  Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                  
                  if(contents.isEmpty()){
                     List<Mob> list = pointer.level()
                           .getEntitiesOfClass(
                                 Mob.class,
                                 new AABB(blockPos),
                                 entity -> !entity.getType().is(ArcanaRegistry.CONTAINMENT_CIRCLET_DISALLOWED) && entity.isAlive() && (hostiles || (!(entity instanceof Enemy)))
                           );
                     if(!list.isEmpty()) {
                        Mob entity = list.getFirst();
                        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(pointer.blockEntity().problemPath(), LogUtils.getLogger())){
                           TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, pointer.level().registryAccess());
                           entity.saveWithoutId(nbtWriteView);
                           CompoundTag data = nbtWriteView.buildResult();
                           data.putString("id", EntityType.getKey(entity.getType()).toString());
                           ArcanaItem.putProperty(stack,ContainmentCirclet.CONTENTS_TAG,data);
                           ArcanaItem.putProperty(stack,ContainmentCirclet.HP_TAG,entity.getHealth());
                           ArcanaItem.putProperty(stack,ContainmentCirclet.MAX_HP_TAG,entity.getMaxHealth());
                           entity.discard();
                           circlet.buildItemLore(stack,pointer.level().getServer());
                           this.setSuccess(true);
                        }
                     } else {
                        this.setSuccess(false);
                     }
                     return stack;
                  }else{
                     try{
                        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(pointer.blockEntity().problemPath(), LogUtils.getLogger())){
                           ValueInput newNbtReadView = TagValueInput.create(logging, pointer.level().registryAccess(),contents);
                           Optional<Entity> optional = EntityType.create(newNbtReadView,pointer.level(), EntitySpawnReason.DISPENSER);
                           Vec3 summonPos = Vec3.atBottomCenterOf(pointer.pos().relative(direction));
                           
                           if(optional.isPresent()){
                              Entity newEntity = optional.get();
                              newEntity.snapTo(summonPos.x(), summonPos.y(), summonPos.z(), newEntity.getYRot(), newEntity.getXRot());
                              
                              if(newEntity instanceof LivingEntity living){
                                 float hp = ArcanaItem.getFloatProperty(stack,ContainmentCirclet.HP_TAG);
                                 living.setHealth(hp);
                              }
                              
                              pointer.level().addFreshEntity(newEntity);
                              ArcanaItem.putProperty(stack,ContainmentCirclet.CONTENTS_TAG,new CompoundTag());
                              circlet.buildItemLore(stack,pointer.level().getServer());
                              this.setSuccess(true);
                              return stack;
                           }
                        }
                        this.setSuccess(false);
                        return stack;
                     }catch (Exception var6) {
                        LOGGER.error("Error while dispensing essence egg from dispenser at {}", pointer.pos(), var6);
                        this.setSuccess(false);
                        return stack;
                     }
                  }
               }
            }
      );
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.CHEST_TRANSLOCATOR.getItem(),
            new OptionalDispenseItemBehavior() {
               @Override
               protected ItemStack execute(BlockSource pointer, ItemStack stack){
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof ChestTranslocator translocator)){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  Level world = pointer.level();
                  BlockPos blockPos = pointer.pos().relative(pointer.state().getValue(DispenserBlock.FACING));
                  BlockState state = world.getBlockState(blockPos);
                  CompoundTag contents = ArcanaItem.getCompoundProperty(stack,ChestTranslocator.CONTENTS_TAG);
                  CompoundTag stateTag = ArcanaItem.getCompoundProperty(stack,ChestTranslocator.STATE_TAG);
                  Direction direction = pointer.state().getValue(DispenserBlock.FACING);
                  
                  if(contents.isEmpty()){
                     if(state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL)){
                        BlockEntity be = world.getBlockEntity(blockPos);
                        if(be == null){
                           this.setSuccess(false);
                           return stack;
                        }
                        CompoundTag contentData = be.saveWithFullMetadata(BorisLib.SERVER.registryAccess());
                        ArcanaItem.putProperty(stack,ChestTranslocator.CONTENTS_TAG,contentData);
                        ArcanaItem.putProperty(stack,ChestTranslocator.STATE_TAG, NbtUtils.writeBlockState(state));
                        if(be instanceof Clearable clearable) clearable.clearContent();
                        world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        SoundUtils.playSound(world,blockPos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1,1);
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else{
                     if(world.getBlockState(blockPos).isAir()){
                        BlockState blockState = NbtUtils.readBlockState(world.holderLookup(Registries.BLOCK),stateTag);
                        if(blockState.is(Blocks.CHEST) || blockState.is(Blocks.TRAPPED_CHEST)){
                           FluidState fluidState = world.getFluidState(blockPos);
                           if(direction.getAxis() == Direction.Axis.Y){
                              direction = Direction.NORTH;
                           }
                           blockState = blockState.getBlock().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction).setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
                        }else if(blockState.is(Blocks.BARREL)){
                           blockState = blockState.getBlock().defaultBlockState().setValue(BlockStateProperties.FACING, direction);
                        }
                        world.setBlock(blockPos,blockState, Block.UPDATE_ALL);
                        BlockEntity blockEntity = BlockEntity.loadStatic(blockPos,blockState,contents,world.registryAccess());
                        if(blockEntity != null){
                           world.setBlockEntity(blockEntity);
                        }
                        
                        ArcanaItem.putProperty(stack,ChestTranslocator.CONTENTS_TAG,new CompoundTag());
                        ArcanaItem.putProperty(stack,ChestTranslocator.STATE_TAG,new CompoundTag());
                        SoundUtils.playSound(world,blockPos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1,1);
                        
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }
                  return stack;
               }
            });
      
      
   }
}
