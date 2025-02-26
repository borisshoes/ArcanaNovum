package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Collections;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulGuardianEntity extends WitherSkeletonEntity implements PolymerEntity {
   
   private boolean mage;
   private NulConstructEntity construct;
   
   public NulGuardianEntity(World world, NulConstructEntity construct, boolean mage){
      super(ArcanaRegistry.NUL_GUARDIAN_ENTITY, world);
      this.mage = mage;
      this.construct = construct;
   }
   
   public NulGuardianEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world){
      super(entityType, world);
      this.mage = false;
      this.construct = null;
   }
   
   @Override
   public boolean canTarget(EntityType<?> type){
      boolean base = super.canTarget(type);
      if(type == ArcanaRegistry.NUL_GUARDIAN_ENTITY || type == ArcanaRegistry.NUL_CONSTRUCT_ENTITY){
         
         return false;
      }
      return base;
   }
   
   @Nullable
   @Override
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData){
      EntityData entityData2 = super.initialize(world, difficulty, spawnReason, entityData);
      this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(10.0);
      this.updateAttackType();
      this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
      StatusEffectInstance res = new StatusEffectInstance(StatusEffects.RESISTANCE,-1,1,false,false,false);
      this.addStatusEffect(res);
      return entityData2;
   }
   
   @Override
   protected void initEquipment(Random random, LocalDifficulty localDifficulty){
      if(mage){
         ItemStack bowStack = new ItemStack(Items.BOW);
         bowStack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(MOD_ID,"nul_guardian_staff"));
         this.equipStack(EquipmentSlot.MAINHAND, bowStack);
      }else{
         ItemStack meleeStack = new ItemStack(Items.NETHERITE_AXE);
         float rand = this.random.nextFloat();
         if(rand < 0.1){
            meleeStack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(MOD_ID,"nul_guardian_glaive"));
         }else if(rand < 0.5){
            meleeStack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(MOD_ID,"nul_guardian_sword"));
         }else{
            meleeStack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(MOD_ID,"nul_guardian_axe"));
         }
         this.equipStack(EquipmentSlot.MAINHAND, meleeStack);
      }
   }
   
   @Override
   public void onDeath(DamageSource damageSource){
      super.onDeath(damageSource);
      
      if(this.construct != null && this.mage && this.getWorld() instanceof ServerWorld serverWorld){
         ParticleEffectUtils.trackedAnimatedLightningBolt(serverWorld,this::getEyePos, this.construct::getEyePos,20,1.0, ParticleTypes.RAID_OMEN,4,1,0,0,false,0,50);
         ArcanaNovum.addTickTimerCallback(new GenericTimer(50, () -> {
            if(this.construct != null && this.construct.isAlive()){
               this.construct.damage(serverWorld, ArcanaDamageTypes.of(this.getWorld(),ArcanaDamageTypes.CONCENTRATION,damageSource.getSource(),damageSource.getAttacker()), 35);
            }
         }));
      }
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(this.construct != null && this.getWorld() instanceof ServerWorld serverWorld){
         if(!this.construct.isAlive() || !this.construct.getWorld().getRegistryKey().equals(this.getWorld().getRegistryKey())){
            this.kill(serverWorld);
         }else if(this.mage){
            if(this.age % 20 == 0 && this.construct.distanceTo(this) < NulConstructEntity.FIGHT_RANGE){
               this.construct.heal(this.construct.isExalted() ? 2.0f : 5.0f);
               
               List<Entity> entities = serverWorld.getOtherEntities(this,getBoundingBox().expand(NulConstructEntity.FIGHT_RANGE), e -> !e.isSpectator() && e.distanceTo(this) < 16.0 && (e instanceof NulGuardianEntity));
               Collections.shuffle(entities);
               for(Entity entity : entities){
                  NulGuardianEntity otherGuardian = (NulGuardianEntity) entity;
                  otherGuardian.heal(otherGuardian.mage ? 1f : 2f);
               }
               
               if(this.age % 80 == 0){
                  ParticleEffect dust = new DustParticleEffect(0x9e0945,0.8f);
                  ParticleEffectUtils.trackedAnimatedLightningBolt(serverWorld,this::getEyePos, this.construct::getEyePos,12,0.5, dust,8,1,0,0,false,0,60);
                  
                  if(!entities.isEmpty()){
                     ParticleEffectUtils.trackedAnimatedLightningBolt(serverWorld,this::getEyePos, entities.getFirst()::getEyePos,12,0.5, dust,8,1,0,0,false,0,60);
                  }
               }
            }
         }
      }
   }
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount){
      float modified = super.modifyAppliedDamage(source, amount);
      if(source.isSourceCreativePlayer() || source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return modified;
      
      if(this.construct != null && this.construct.isExalted()) modified *= 0.75f;
      if(source.isIn(ArcanaRegistry.NUL_CONSTRUCT_VULNERABLE_TO)) modified *= 2.0f;
      if(source.isOf(DamageTypes.WITHER) || source.isOf(DamageTypes.WITHER_SKULL) || source.isOf(ArcanaDamageTypes.NUL)) modified *= 0.0f;
      if(source.isIn(DamageTypeTags.IS_EXPLOSION)) modified *= 0.5f;
      if(source.isIn(DamageTypeTags.BYPASSES_ARMOR)) modified *= 0.85f;
      
      return modified;
   }
   
   @Override
   protected void initGoals(){
      this.goalSelector.clear(g -> true);
      this.targetSelector.clear(g -> true);
      this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
      this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.add(6, new LookAroundGoal(this));
      this.targetSelector.add(1, new RevengeGoal(this));
      this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
   }
   
   @Override
   public void shootAt(LivingEntity target, float pullProgress) {
      if(!this.isSilent()){
         this.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      }
      
      double eyeX = this.getEyePos().x;
      double eyeY = this.getEyePos().y;
      double eyeZ = this.getEyePos().z;
      double g = target.getX() - eyeX;
      double h = target.getBodyY(0.34) - eyeY;
      double i = target.getZ() - eyeZ;
      Vec3d vec3d = new Vec3d(g, h, i);
      WitherSkullEntity witherSkullEntity = new WitherSkullEntity(this.getWorld(), this, vec3d.normalize());
      witherSkullEntity.setOwner(this);
      
      if(this.construct != null && construct.isExalted()){
         witherSkullEntity.setCharged(true);
      }else{
         witherSkullEntity.setCharged(this.random.nextFloat() < 0.34f);
      }
      
      
      witherSkullEntity.setPos(eyeX, eyeY, eyeZ);
      this.getWorld().spawnEntity(witherSkullEntity);
   }
   
   public static DefaultAttributeContainer.Builder createGuardianAttributes(){
      return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MOVEMENT_SPEED, 0.35)
            .add(EntityAttributes.FOLLOW_RANGE, 64.0)
            .add(EntityAttributes.MAX_HEALTH, 50.0)
            .add(EntityAttributes.ARMOR, 10)
            .add(EntityAttributes.ARMOR_TOUGHNESS, 4)
            .add(EntityAttributes.ATTACK_DAMAGE, 10)
            .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.WITHER_SKELETON;
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt){
      super.writeCustomDataToNbt(nbt);
      nbt.putBoolean("mage",mage);
      if(this.construct != null) nbt.putString("construct",this.construct.getUuidAsString());
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt){
      super.readCustomDataFromNbt(nbt);
      mage = nbt.getBoolean("mage");
      
      if(nbt.contains("construct")){
         if(getEntityWorld() instanceof ServerWorld serverWorld){
            if(serverWorld.getEntity(MiscUtils.getUUID(nbt.getString("construct"))) instanceof NulConstructEntity construct){
               this.construct = construct;
            }
         }
      }
   }
}
