package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class NulGuardianEntity extends WitherSkeleton implements PolymerEntity {
   
   private boolean mage;
   private NulConstructEntity construct;
   
   public NulGuardianEntity(Level world, NulConstructEntity construct, boolean mage){
      super(ArcanaRegistry.NUL_GUARDIAN_ENTITY, world);
      this.mage = mage;
      this.construct = construct;
   }
   
   public NulGuardianEntity(EntityType<? extends WitherSkeleton> entityType, Level world){
      super(entityType, world);
      this.mage = false;
      this.construct = null;
   }
   
   @Override
   public boolean canAttack(LivingEntity target){
      boolean base = super.canAttack(target);
      if(target.is(ArcanaRegistry.NUL_CONSTRUCT_FRIENDS)){
         return false;
      }
      return base;
   }
   
   @Nullable
   @Override
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData entityData){
      SpawnGroupData entityData2 = super.finalizeSpawn(world, difficulty, spawnReason, entityData);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0);
      this.reassessWeaponGoal();
      this.setDropChance(EquipmentSlot.MAINHAND, 0);
      MobEffectInstance res = new MobEffectInstance(MobEffects.RESISTANCE, -1, 1, false, false, false);
      this.addEffect(res);
      return entityData2;
   }
   
   @Override
   protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty){
      if(mage){
         ItemStack bowStack = new ItemStack(Items.BOW);
         bowStack.set(DataComponents.ITEM_MODEL, ArcanaRegistry.arcanaId("nul_guardian_staff"));
         this.setItemSlot(EquipmentSlot.MAINHAND, bowStack);
      }else{
         ItemStack meleeStack = new ItemStack(Items.NETHERITE_AXE);
         float rand = this.random.nextFloat();
         if(rand < 0.1){
            meleeStack.set(DataComponents.ITEM_MODEL, ArcanaRegistry.arcanaId("nul_guardian_glaive"));
         }else if(rand < 0.5){
            meleeStack.set(DataComponents.ITEM_MODEL, ArcanaRegistry.arcanaId("nul_guardian_sword"));
         }else{
            meleeStack.set(DataComponents.ITEM_MODEL, ArcanaRegistry.arcanaId("nul_guardian_axe"));
         }
         this.setItemSlot(EquipmentSlot.MAINHAND, meleeStack);
      }
   }
   
   public NulConstructEntity getConstruct(){
      return construct;
   }
   
   @Override
   public void die(DamageSource damageSource){
      super.die(damageSource);
      
      if(this.construct != null && this.mage && this.level() instanceof ServerLevel serverWorld){
         ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, this::getEyePosition, this.construct::getEyePosition, 20, 1.0, ParticleTypes.RAID_OMEN, 4, 1, 0, 0, false, 0, 50);
         BorisLib.addTickTimerCallback(new GenericTimer(50, () -> {
            if(this.construct != null && this.construct.isAlive()){
               this.construct.hurtServer(serverWorld, ArcanaDamageTypes.of(this.level(), ArcanaDamageTypes.CONCENTRATION, damageSource.getDirectEntity(), damageSource.getEntity()), 35);
            }
         }));
      }
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(this.construct != null && this.level() instanceof ServerLevel serverWorld){
         if(!this.construct.isAlive() || !this.construct.level().dimension().equals(this.level().dimension())){
            this.kill(serverWorld);
         }else if(this.mage){
            if(this.tickCount % 20 == 0 && this.construct.distanceTo(this) < NulConstructEntity.FIGHT_RANGE){
               this.construct.heal(this.construct.isExalted() ? 2.0f : 5.0f);
               
               List<Entity> entities = serverWorld.getEntities(this, getBoundingBox().inflate(NulConstructEntity.FIGHT_RANGE), e -> !e.isSpectator() && e.distanceTo(this) < 16.0 && (e instanceof NulGuardianEntity));
               Collections.shuffle(entities);
               for(Entity entity : entities){
                  NulGuardianEntity otherGuardian = (NulGuardianEntity) entity;
                  otherGuardian.heal(otherGuardian.mage ? 1f : 2f);
               }
               
               if(this.tickCount % 80 == 0){
                  ParticleOptions dust = new DustParticleOptions(0x9e0945, 0.8f);
                  ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, this::getEyePosition, this.construct::getEyePosition, 12, 0.5, dust, 8, 1, 0, 0, false, 0, 60);
                  
                  if(!entities.isEmpty()){
                     ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, this::getEyePosition, entities.getFirst()::getEyePosition, 12, 0.5, dust, 8, 1, 0, 0, false, 0, 60);
                  }
               }
            }
         }
      }
   }
   
   @Override
   protected float getDamageAfterMagicAbsorb(DamageSource source, float amount){
      float modified = super.getDamageAfterMagicAbsorb(source, amount);
      if(source.isCreativePlayer() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return modified;
      
      if(this.construct != null && this.construct.isExalted()) modified *= 0.75f;
      if(source.is(ArcanaRegistry.NUL_CONSTRUCT_VULNERABLE_TO)) modified *= 2.0f;
      if(source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL) || source.is(ArcanaDamageTypes.NUL))
         modified *= 0.0f;
      if(source.is(DamageTypeTags.IS_EXPLOSION)) modified *= 0.5f;
      if(source.is(DamageTypeTags.BYPASSES_ARMOR)) modified *= 0.85f;
      
      return modified;
   }
   
   @Override
   protected void registerGoals(){
      this.goalSelector.removeAllGoals(g -> true);
      this.targetSelector.removeAllGoals(g -> true);
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
   }
   
   @Override
   public void performRangedAttack(LivingEntity target, float pullProgress){
      if(!this.isSilent()){
         this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
      }
      
      double eyeX = this.getEyePosition().x;
      double eyeY = this.getEyePosition().y;
      double eyeZ = this.getEyePosition().z;
      double g = target.getX() - eyeX;
      double h = target.getY(0.34) - eyeY;
      double i = target.getZ() - eyeZ;
      Vec3 vec3d = new Vec3(g, h, i);
      WitherSkull witherSkullEntity = new WitherSkull(this.level(), this, vec3d.normalize());
      witherSkullEntity.setOwner(this);
      
      if(this.construct != null && construct.isExalted()){
         witherSkullEntity.setDangerous(true);
      }else{
         witherSkullEntity.setDangerous(this.random.nextFloat() < 0.34f);
      }
      
      
      witherSkullEntity.setPosRaw(eyeX, eyeY, eyeZ);
      this.level().addFreshEntity(witherSkullEntity);
   }
   
   public static AttributeSupplier.Builder createGuardianAttributes(){
      return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.MAX_HEALTH, 50.0)
            .add(Attributes.ARMOR, 10)
            .add(Attributes.ARMOR_TOUGHNESS, 4)
            .add(Attributes.ATTACK_DAMAGE, 10)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.WITHER_SKELETON;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putBoolean("mage", mage);
      if(this.construct != null) view.putString("construct", this.construct.getStringUUID());
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      mage = view.getBooleanOr("mage", false);
      
      if(level() instanceof ServerLevel serverWorld){
         if(serverWorld.getEntity(AlgoUtils.getUUID(view.getStringOr("construct", ""))) instanceof NulConstructEntity con){
            this.construct = con;
         }
      }
   }
}
