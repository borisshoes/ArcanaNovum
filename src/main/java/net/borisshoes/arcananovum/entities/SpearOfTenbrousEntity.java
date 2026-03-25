package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.mixins.ThrownTridentAccessor;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.MathUtils;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;
import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;

public class SpearOfTenbrousEntity extends AbstractArrow implements PolymerEntity {
   
   private long chunkTicketExpiryTicks = 0L;
   private float damage;
   private int slot = -1;
   private ArrayList<Vec3> oldPos = new ArrayList<>();
   
   public SpearOfTenbrousEntity(EntityType<? extends SpearOfTenbrousEntity> entityType, Level world) {
      super(entityType, world);
      this.pickup = Pickup.CREATIVE_ONLY;
      this.damage = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SPEAR_OF_TENBROUS_THROW_DMG);
   }
   
   public SpearOfTenbrousEntity(Level world, LivingEntity owner, ItemStack stack) {
      super(ArcanaRegistry.SPEAR_OF_TENBROUS_ENTITY, owner, world, stack, null);
      this.pickup = Pickup.CREATIVE_ONLY;
      float dmg = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SPEAR_OF_TENBROUS_THROW_DMG);
      for(ItemAttributeModifiers.Entry modifier : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()){
         if(modifier.attribute().equals(Attributes.ATTACK_DAMAGE) && modifier.modifier().is(BASE_ATTACK_DAMAGE_ID)){
            dmg = (float) modifier.modifier().amount() + 1.0f;
         }
      }
      this.damage = dmg;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putFloat("spearDamage",damage);
      view.putInt("prefSlot",slot);
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      this.damage = view.getFloatOr("spearDamage", 0.0f);
      this.slot = view.getIntOr("prefSlot",-1);
   }
   
   public int getSlot(){
      return slot;
   }
   
   public void setSlot(int slot){
      this.slot = slot;
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.TRIDENT;
   }
   
   @Override
   public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial){
      data.add(new SynchedEntityData.DataValue<>(ThrownTridentAccessor.getID_FOIL().id(), ThrownTridentAccessor.getID_FOIL().serializer(), true));
   }
   
   @Override
   public void tick() {
      if (this.inGroundTime >= 1) {
         this.discard();
         return;
      }
      int chunkX = SectionPos.blockToSectionCoord(this.position().x());
      int chunkZ = SectionPos.blockToSectionCoord(this.position().z());
      super.tick();
      
      if (this.isAlive()) {
         if(level() instanceof ServerLevel serverWorld){
            ParticleOptions particles = ParticleTypes.COMPOSTER;
            int particleFadeColor = 0x001c08;
            if(ArcanaItem.getSkin(pickupItemStack) == ArcanaSkin.ZEPHOS_LANCE){
               particles = SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT,0x53D1FF,0.0f);
               particleFadeColor = 0x001C32;
            }
            
            ArcanaEffectUtils.spawnLongParticle(serverWorld,new DustColorTransitionOptions(particleFadeColor,0x000000,1.25f),getX(),getY(),getZ(),0.125,0.125,0.125,0.02, 6);
            
            int trailSize = 3;
            if(this.tickCount % 3 == 0 && !oldPos.isEmpty()){
               Vec3 endPos = MathUtils.randomSpherePoint(oldPos.getLast(),1.25, 0.4);
               ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, this::getEyePosition, () -> endPos, (int)(Math.random()*5+5), 0.5, particles,
                     12, 1, 0, 1, true, 5, 5);
            }
            oldPos.add(position());
            if(oldPos.size() > trailSize){
               oldPos.removeFirst();
            }
         }
         
         BlockPos blockPos = BlockPos.containing(this.position());
         ChunkPos chunkPos = this.chunkPosition();
         if ((--this.chunkTicketExpiryTicks <= 0L || chunkX != SectionPos.blockToSectionCoord(blockPos.getX()) || chunkZ != SectionPos.blockToSectionCoord(blockPos.getZ())) && level() instanceof ServerLevel serverWorld) {
            serverWorld.resetEmptyTime();
            this.chunkTicketExpiryTicks = ServerPlayer.placeEnderPearlTicket(serverWorld, chunkPos) - 1L;
         }
      }
   }
   
   @Nullable
   @Override
   public Entity teleport(TeleportTransition teleportTarget) {
      Entity entity = super.teleport(teleportTarget);
      if (entity != null) {
         entity.placePortalTicket(BlockPos.containing(entity.position()));
      }
      return entity;
   }
   
   private void applyImpactEffects(Entity hitEntity, List<LivingEntity> affectedEntities){
      ParticleOptions particles = ParticleTypes.COMPOSTER;
      int particleFadeColor = 0x001c08;
      if(ArcanaItem.getSkin(pickupItemStack) == ArcanaSkin.ZEPHOS_LANCE){
         particles = SpellParticleOption.create(ParticleTypes.INSTANT_EFFECT,0x53D1FF,0.0f);
         particleFadeColor = 0x001C32;
      }
      
      int stunDuration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SPEAR_OF_TENBROUS_STUN_DURATION);
      MobEffectInstance slow = new MobEffectInstance(MobEffects.SLOWNESS,stunDuration,9,false,false,true);
      ConditionInstance nearsight = new ConditionInstance(Conditions.NEARSIGHT,arcanaId(ArcanaRegistry.SPEAR_OF_TENBROUS.getId()),stunDuration,5.0f,false,true,false, AttributeModifier.Operation.ADD_VALUE,getOwner() != null ? getOwner().getUUID() : null);
      
      boolean blindRage = ArcanaAugments.getAugmentOnItem(pickupItemStack,ArcanaAugments.BLINDING_RAGE) > 0;
      boolean voidStorm = ArcanaAugments.getAugmentOnItem(pickupItemStack,ArcanaAugments.VOID_STORM) > 0;
      
      if(hitEntity instanceof LivingEntity living){
         living.addEffect(slow);
         if(blindRage){
            Conditions.addCondition(living.level().getServer(),living,nearsight);
         }
         
         if(living.level() instanceof ServerLevel serverWorld){
            ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, hitEntity::position, () -> hitEntity.position().add(0,hitEntity.getEyeHeight(),0), (int)(5 + 2*hitEntity.getEyeHeight()), hitEntity.getBbWidth(), particles,
                  8, 1, 0, 1, false, 0, 5);
            ArcanaEffectUtils.trackedAnimatedLightningBolt(serverWorld, () -> hitEntity.position().add(0,hitEntity.getEyeHeight(),0), hitEntity::position, (int)(5 + 2*hitEntity.getEyeHeight()), hitEntity.getBbWidth(), particles,
                  8, 1, 0, 1, false, 0, 5);
         }
      }
      
      if(voidStorm && level() instanceof ServerLevel serverWorld){
         float stormDmg = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SPEAR_OF_TENBROUS_STORM_DMG);
         SoundUtils.playSound(serverWorld, BlockPos.containing(position()), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS,.1f,2f);
         ParticleOptions dust = new DustColorTransitionOptions(particleFadeColor,0x000000,2f);
         serverWorld.sendParticles(dust,getX(),getY(),getZ(),150,1,1,1,0.02);
         for(int i = 0; i < 18; i++){
            ArcanaEffectUtils.animatedLightningBolt(serverWorld, getEyePosition(), MathUtils.randomSpherePoint(getEyePosition(),5,2), (int)(Math.random()*5+5), 0.5, particles,
                  8, 1, 0, 1, false, 0, 5);
         }
         
         for(LivingEntity affectedEntity : affectedEntities){ // Void Storm
            ArcanaEffectUtils.animatedLightningBolt(serverWorld, getEyePosition(), affectedEntity.getEyePosition(), (int)(Math.random()*5+5), 0.5, particles,
                  8, 1, 0, 1, false, 4, 5);
            DamageSource source = ArcanaDamageTypes.of(serverWorld,ArcanaDamageTypes.ARCANE_LIGHTNING,this,getOwner() == null ? this : getOwner());
            float damage = stormDmg;
            if(affectedEntity.getType().is(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)) damage *= 1.25f;
            affectedEntity.hurtServer(serverWorld,source,damage);
            
            if(blindRage){
               Conditions.addCondition(affectedEntity.level().getServer(),affectedEntity,nearsight);
            }
         }
      }
   }
   
   private List<LivingEntity> getSurroundingEntities(Level world, Vec3 pos){
      ArrayList<LivingEntity> entities = new ArrayList<>();
      boolean voidStorm = ArcanaAugments.getAugmentOnItem(pickupItemStack,ArcanaAugments.VOID_STORM) > 0;
      double stormRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SPEAR_OF_TENBROUS_STORM_RANGE);
      if(!voidStorm) return entities;
      
      world.getEntities(this.getOwner(), new AABB(BlockPos.containing(pos)).inflate(6), entity -> entity.distanceToSqr(pos) < (stormRange*stormRange)).forEach(e -> {
         if(e instanceof LivingEntity living) entities.add(living);
      });
      
      return entities;
   }
   
   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity target = entityHitResult.getEntity();
      float baseDamage = this.damage;
      Entity owner = this.getOwner();
      DamageSource damageSource = ArcanaDamageTypes.of(level(),ArcanaDamageTypes.ARCANE_LIGHTNING,this,owner == null ? this : owner);
      if (this.level() instanceof ServerLevel serverWorld) {
         baseDamage = EnchantmentHelper.modifyDamage(serverWorld, this.getWeaponItem(), target, damageSource, baseDamage);
      }
      
      int fireAspect = this.getWeaponItem().getEnchantments().getLevel(MinecraftUtils.getEnchantment(Enchantments.FIRE_ASPECT));
      if(!target.fireImmune() && fireAspect > 0){
         target.igniteForSeconds(fireAspect*4.0f);
      }
      applyImpactEffects(target, getSurroundingEntities(level(), position()));
      
      if(target.getType().is(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)) baseDamage *= 1.25f;
      
      if (target.hurtOrSimulate(damageSource, baseDamage)) {
         if (this.level() instanceof ServerLevel serverWorld) {
            EnchantmentHelper.doPostAttackEffectsWithItemSourceOnBreak(serverWorld, target, damageSource, this.getWeaponItem(), item -> this.kill(serverWorld));
         }
         
         if (target instanceof LivingEntity livingEntity) {
            this.doKnockback(livingEntity, damageSource);
            this.doPostHurtEffects(livingEntity);
            
            if(owner instanceof ServerPlayer player) ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_SPEAR_OF_TENBROUS_IMPALE));
         }
      }
      
      this.deflect(ProjectileDeflection.REVERSE, target, EntityReference.of(this.getOwner()), false);
      this.setDeltaMovement(this.getDeltaMovement().multiply(0.02, 0.2, 0.02));
      this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
      this.discard();
   }
   
   @Override
   public void remove(RemovalReason reason){
      super.remove(reason);
      if(this.getOwner() != null && this.getOwner() instanceof ServerPlayer player && level() instanceof ServerLevel serverWorld){
         int cooldownTime = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SPEAR_OF_TENBROUS_THROW_COOLDOWN_PER_LVL).get(ArcanaAugments.getAugmentOnItem(pickupItemStack,ArcanaAugments.UNENDING_HATRED));
         if(!player.hasInfiniteMaterials()) BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(pickupItemStack,player,0, slot));
         player.getCooldowns().addCooldown(pickupItemStack,cooldownTime);
         
         this.playSound(SoundEvents.PLAYER_TELEPORT, 1.0F, 1.0F);
         SoundUtils.playSongToPlayer(player, SoundEvents.PLAYER_TELEPORT, 0.3F, 1.0F);
         
         serverWorld.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),30,.2,0.2,.2,0.03);
         serverWorld.sendParticles(ParticleTypes.PORTAL,getX(),getY(),getZ(),30,.2,0.2,.2,1);
      }
   }
   
   @Override
   protected ItemStack getDefaultPickupItem(){
      return ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem();
   }
   
   @Override
   protected void hitBlockEnchantmentEffects(ServerLevel world, BlockHitResult blockHitResult, ItemStack weaponStack) {
      Vec3 vec3d = blockHitResult.getBlockPos().clampLocationWithin(blockHitResult.getLocation());
      EnchantmentHelper.onHitBlock(
            world,
            weaponStack,
            this.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null,
            this,
            null,
            vec3d,
            world.getBlockState(blockHitResult.getBlockPos()),
            item -> this.kill(world)
      );
      
      applyImpactEffects(null, getSurroundingEntities(level(), position()));
   }
   
   @Override
   public ItemStack getWeaponItem() {
      return this.getPickupItemStackOrigin();
   }
   
   @Override
   protected boolean tryPickup(Player player) {
      return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
   }
   
   @Override
   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.TRIDENT_HIT_GROUND;
   }
   
   @Override
   public void playerTouch(Player player) {
      if (this.ownedBy(player) || this.getOwner() == null) {
         super.playerTouch(player);
      }
   }
   
   @Override
   protected float getWaterInertia() {
      return 0.99F;
   }
   
   @Override
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }
}
