package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(DispenserBehavior.class)
public interface DispenserBehaviorMixin {
   
   @Inject(method = "registerDefaults", at = @At("TAIL"))
   private static void arcananovum_dispenserInteractions(CallbackInfo ci){
      
      DispenserBlock.registerBehavior(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                  AquaticEversource.AquaticEversourceItem eversource = (AquaticEversource.AquaticEversourceItem) stack.getItem();
                  int mode = ArcanaItem.getIntProperty(stack,ArcanaItem.MODE_TAG); // 0 place, 1 remove
                  BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                  World world = pointer.world();
                  BlockState blockState = world.getBlockState(blockPos);
                  if(mode == 1){
                     FluidDrainable fluidDrainable;
                     Block block = blockState.getBlock();
                     if(block instanceof FluidDrainable && !(fluidDrainable = (FluidDrainable) block).tryDrainFluid(null, world, blockPos, blockState).isEmpty()){
                        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                        fluidDrainable.getBucketFillSound().ifPresent(sound -> SoundUtils.playSound(world,blockPos,sound,SoundCategory.BLOCKS,1,1));
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else if(mode == 0 || mode == 2){
                     eversource.placeFluid(Fluids.WATER, null, world, blockPos, null, false);
                     this.setSuccess(true);
                  }else{
                     this.setSuccess(false);
                  }
                  return stack;
               }
      });
      
      DispenserBlock.registerBehavior(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof MagmaticEversource magmaticEversource)){
                     this.setSuccess(false);
                     return stack;
                  }
                  MagmaticEversource.MagmaticEversourceItem eversource = (MagmaticEversource.MagmaticEversourceItem) stack.getItem();
                  int mode = ArcanaItem.getIntProperty(stack,ArcanaItem.MODE_TAG); // 0 place, 1 remove
                  int charges = ArcanaItem.getIntProperty(stack,MagmaticEversource.USES_TAG);
                  
                  BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                  World world = pointer.world();
                  BlockState blockState = world.getBlockState(blockPos);
                  if(mode == 1){
                     FluidDrainable fluidDrainable;
                     Block block = blockState.getBlock();
                     if(block instanceof FluidDrainable && !(fluidDrainable = (FluidDrainable) block).tryDrainFluid(null, world, blockPos, blockState).isEmpty()){
                        world.emitGameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
                        fluidDrainable.getBucketFillSound().ifPresent(sound -> SoundUtils.playSound(world,blockPos,sound,SoundCategory.BLOCKS,1,1));
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else if(mode == 0 && charges >= 1){
                     eversource.placeFluid(Fluids.LAVA, null, world, blockPos, null);
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
            new FallibleItemDispenserBehavior() {
               @Override
               protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack){
                  ServerWorld serverWorld = pointer.world();
                  this.setSuccess(true);
                  Direction direction = pointer.state().get(DispenserBlock.FACING);
                  BlockPos blockPos = pointer.pos().offset(direction);
                  BlockState blockState = serverWorld.getBlockState(blockPos);
                  if(AbstractFireBlock.canPlaceAt(serverWorld, blockPos, direction)){
                     serverWorld.setBlockState(blockPos, AbstractFireBlock.getState(serverWorld, blockPos));
                     serverWorld.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                  }else if(CampfireBlock.canBeLit(blockState) || CandleBlock.canBeLit(blockState) || CandleCakeBlock.canBeLit(blockState)){
                     serverWorld.setBlockState(blockPos, blockState.with(Properties.LIT, Boolean.valueOf(true)));
                     serverWorld.emitGameEvent(null, GameEvent.BLOCK_CHANGE, blockPos);
                  }else if(blockState.getBlock() instanceof TntBlock){
                     TntBlock.primeTnt(serverWorld, blockPos);
                     serverWorld.removeBlock(blockPos, false);
                  }else{
                     this.setSuccess(false);
                  }
                  
                  return stack;
               }
            });
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.WILD_GROWTH_CHARM.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack){
                  this.setSuccess(true);
                  World world = pointer.world();
                  BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                  if (!BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL,64), world, blockPos) && !BoneMealItem.useOnGround(new ItemStack(Items.BONE_MEAL,64), world, blockPos, null)) {
                     this.setSuccess(false);
                  } else if (!world.isClient) {
                     world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, blockPos, 15);
                  }
                  
                  return stack;
               }
            });
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.EVERLASTING_ROCKET.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof EverlastingRocket rocket)){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  ServerWorld serverWorld = pointer.world();
                  Direction direction = pointer.state().get(DispenserBlock.FACING);
                  ProjectileItem.Settings projectileSettings = ProjectileItem.Settings.builder().positionFunction(
                        (point, facing) -> point.centerPos().add((double)facing.getOffsetX() * 0.5000099999997474, (double)facing.getOffsetY() * 0.5000099999997474, (double)facing.getOffsetZ() * 0.5000099999997474))
                        .uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
                  Position position = projectileSettings.positionFunction().getDispensePosition(pointer, direction);
                  ProjectileEntity.spawnWithVelocity(
                        ((FireworkRocketItem)rocket.getFireworkStack(stack).getItem()).createEntity(serverWorld, position, stack, direction),
                        serverWorld,
                        stack,
                        (double)direction.getOffsetX(),
                        (double)direction.getOffsetY(),
                        (double)direction.getOffsetZ(),
                        projectileSettings.power(),
                        projectileSettings.uncertainty()
                  );
                  this.setSuccess(true);
                  return stack;
               }
               
               @Override
               protected void playSound(BlockPointer pointer) {
                  ProjectileItem.Settings projectileSettings = ProjectileItem.Settings.builder().positionFunction(
                              (point, facing) -> point.centerPos().add((double)facing.getOffsetX() * 0.5000099999997474, (double)facing.getOffsetY() * 0.5000099999997474, (double)facing.getOffsetZ() * 0.5000099999997474))
                        .uncertainty(1.0F).power(0.5F).overrideDispenseEvent(1004).build();
                  pointer.world().syncWorldEvent(projectileSettings.overrideDispenseEvent().orElse(1002), pointer.pos(), 0);
               }
            }
      );
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.ESSENCE_EGG.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof EssenceEgg egg) || EntityType.get(EssenceEgg.getType(stack)).isEmpty()){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  Direction direction = pointer.state().get(DispenserBlock.FACING);
                  
                  try {
                     int splitLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SOUL_SPLIT.id));
                     int efficiencyLevel = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.DETERMINED_SPIRIT.id));
                     if(EssenceEgg.getUses(stack) > 0){
                        ServerWorld serverWorld = pointer.world();
                        Vec3d summonPos = Vec3d.ofBottomCenter(pointer.pos().offset(direction));
                        
                        NbtCompound nbtCompound = new NbtCompound();
                        nbtCompound.putString("id", EssenceEgg.getType(stack));
                        int spawns = Math.random() >= 0.1*splitLevel ? 1 : 2;
                        
                        for(int i = 0; i < spawns; i++){
                           Entity newEntity = EntityType.loadEntityWithPassengers(nbtCompound, serverWorld, SpawnReason.DISPENSER, entity -> {
                              entity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), entity.getYaw(), entity.getPitch());
                              return entity;
                           });
                           if(newEntity instanceof MobEntity mobEntity){
                              mobEntity.initialize(serverWorld, serverWorld.getLocalDifficulty(newEntity.getBlockPos()), SpawnReason.DISPENSER, null);
                           }
                           serverWorld.spawnNewEntityAndPassengers(newEntity);
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
                  
                  pointer.world().emitGameEvent(null, GameEvent.ENTITY_PLACE, pointer.pos());
                  return stack;
               }
            }
      );
      
      DispenserBlock.registerBehavior(
            ArcanaRegistry.CONTAINMENT_CIRCLET.getItem(),
            new FallibleItemDispenserBehavior() {
               @Override
               public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof ContainmentCirclet circlet)){
                     this.setSuccess(false);
                     return stack;
                  }
                  BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                  NbtCompound contents = ArcanaItem.getCompoundProperty(stack, ContainmentCirclet.CONTENTS_TAG);
                  boolean hostiles = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.CONFINEMENT.id) > 0;
                  Direction direction = pointer.state().get(DispenserBlock.FACING);
                  
                  if(contents.isEmpty()){
                     List<MobEntity> list = pointer.world()
                           .getEntitiesByClass(
                                 MobEntity.class,
                                 new Box(blockPos),
                                 entity -> !entity.getType().isIn(ArcanaRegistry.CONTAINMENT_CIRCLET_DISALLOWED) && entity.isAlive() && (hostiles || (!(entity instanceof Monster)))
                           );
                     if(!list.isEmpty()) {
                        MobEntity entity = list.getFirst();
                        NbtCompound data = entity.writeNbt(new NbtCompound());
                        data.putString("id", EntityType.getId(entity.getType()).toString());
                        ArcanaItem.putProperty(stack,ContainmentCirclet.CONTENTS_TAG,data);
                        ArcanaItem.putProperty(stack,ContainmentCirclet.HP_TAG,entity.getHealth());
                        ArcanaItem.putProperty(stack,ContainmentCirclet.MAX_HP_TAG,entity.getMaxHealth());
                        entity.discard();
                        circlet.buildItemLore(stack,pointer.world().getServer());
                        this.setSuccess(true);
                     } else {
                        this.setSuccess(false);
                     }
                     return stack;
                  }else{
                     try{
                        float hp = ArcanaItem.getFloatProperty(stack,ContainmentCirclet.HP_TAG);
                        Optional<Entity> optional = EntityType.getEntityFromNbt(contents,pointer.world(), SpawnReason.DISPENSER);
                        Vec3d summonPos = Vec3d.ofBottomCenter(pointer.pos().offset(direction));
                        
                        if(optional.isPresent()){
                           Entity newEntity = optional.get();
                           newEntity.refreshPositionAndAngles(summonPos.getX(), summonPos.getY(), summonPos.getZ(), newEntity.getYaw(), newEntity.getPitch());
                           
                           if(newEntity instanceof LivingEntity living){
                              living.setHealth(hp);
                           }
                           
                           pointer.world().spawnEntity(newEntity);
                           ArcanaItem.putProperty(stack,ContainmentCirclet.CONTENTS_TAG,new NbtCompound());
                           circlet.buildItemLore(stack,pointer.world().getServer());
                           this.setSuccess(true);
                           return stack;
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
            new FallibleItemDispenserBehavior() {
               @Override
               protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack){
                  if(!(ArcanaItemUtils.identifyItem(stack) instanceof ChestTranslocator translocator)){
                     this.setSuccess(false);
                     return stack;
                  }
                  
                  World world = pointer.world();
                  BlockPos blockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
                  BlockState state = world.getBlockState(blockPos);
                  NbtCompound contents = ArcanaItem.getCompoundProperty(stack,ChestTranslocator.CONTENTS_TAG);
                  NbtCompound stateTag = ArcanaItem.getCompoundProperty(stack,ChestTranslocator.STATE_TAG);
                  Direction direction = pointer.state().get(DispenserBlock.FACING);
                  
                  if(contents.isEmpty()){
                     if(state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST) || state.isOf(Blocks.BARREL)){
                        BlockEntity be = world.getBlockEntity(blockPos);
                        if(be == null){
                           this.setSuccess(false);
                           return stack;
                        }
                        NbtCompound contentData = be.createNbtWithId(ArcanaNovum.SERVER.getRegistryManager());
                        ArcanaItem.putProperty(stack,ChestTranslocator.CONTENTS_TAG,contentData);
                        ArcanaItem.putProperty(stack,ChestTranslocator.STATE_TAG, NbtHelper.fromBlockState(state));
                        if(be instanceof Clearable clearable) clearable.clear();
                        world.setBlockState(blockPos,Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                        SoundUtils.playSound(world,blockPos,SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.BLOCKS, 1,1);
                        this.setSuccess(true);
                     }else{
                        this.setSuccess(false);
                     }
                  }else{
                     if(world.getBlockState(blockPos).isAir()){
                        BlockState blockState = NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK),stateTag);
                        if(blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST)){
                           FluidState fluidState = world.getFluidState(blockPos);
                           if(direction.getAxis() == Direction.Axis.Y){
                              direction = Direction.NORTH;
                           }
                           blockState = blockState.getBlock().getDefaultState().with(HorizontalFacingBlock.FACING, direction).with(Properties.CHEST_TYPE, ChestType.SINGLE).with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
                        }else if(blockState.isOf(Blocks.BARREL)){
                           blockState = blockState.getBlock().getDefaultState().with(Properties.FACING, direction);
                        }
                        world.setBlockState(blockPos,blockState,Block.NOTIFY_ALL);
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if(blockEntity != null){
                           blockEntity.read(contents,ArcanaNovum.SERVER.getRegistryManager());
                        }
                        
                        ArcanaItem.putProperty(stack,ChestTranslocator.CONTENTS_TAG,new NbtCompound());
                        ArcanaItem.putProperty(stack,ChestTranslocator.STATE_TAG,new NbtCompound());
                        SoundUtils.playSound(world,blockPos,SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1,1);
                        
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
