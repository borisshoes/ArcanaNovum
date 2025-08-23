package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ItemReturnTimerCallback;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.mixins.TridentEntityAccessor;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;

public class SpearOfTenbrousEntity extends PersistentProjectileEntity implements PolymerEntity {
   
   private long chunkTicketExpiryTicks = 0L;
   private float damage;
   private ArrayList<Vec3d> oldPos = new ArrayList<>();
   
   public SpearOfTenbrousEntity(EntityType<? extends SpearOfTenbrousEntity> entityType, World world) {
      super(entityType, world);
      this.pickupType = PickupPermission.CREATIVE_ONLY;
      this.damage = 11.0f;
   }
   
   public SpearOfTenbrousEntity(World world, LivingEntity owner, ItemStack stack) {
      super(ArcanaRegistry.SPEAR_OF_TENBROUS_ENTITY, owner, world, stack, null);
      this.pickupType = PickupPermission.CREATIVE_ONLY;
      float dmg = 11.0f;
      for(AttributeModifiersComponent.Entry modifier : stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()){
         if(modifier.attribute().equals(EntityAttributes.ATTACK_DAMAGE) && modifier.modifier().idMatches(BASE_ATTACK_DAMAGE_MODIFIER_ID)){
            dmg = (float) modifier.modifier().value() + 1.0f;
         }
      }
      this.damage = dmg;
   }
   
   @Override
   protected void writeCustomData(WriteView view){
      super.writeCustomData(view);
      view.putFloat("spearDamage",damage);
   }
   
   @Override
   protected void readCustomData(ReadView view){
      super.readCustomData(view);
      this.damage = view.getFloat("spearDamage", 0.0f);
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.TRIDENT;
   }
   
   @Override
   public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial){
      data.add(new DataTracker.SerializedEntry<>(TridentEntityAccessor.getENCHANTED().id(), TridentEntityAccessor.getENCHANTED().dataType(), true));
   }
   
   @Override
   public void tick() {
      if (this.inGroundTime >= 1) {
         this.discard();
         return;
      }
      int chunkX = ChunkSectionPos.getSectionCoordFloored(this.getPos().getX());
      int chunkZ = ChunkSectionPos.getSectionCoordFloored(this.getPos().getZ());
      super.tick();
      
      if (this.isAlive()) {
         if(getWorld() instanceof ServerWorld serverWorld){
            ParticleEffectUtils.spawnLongParticle(serverWorld,new DustColorTransitionParticleEffect(0x001c08,0x000000,1.25f),getX(),getY(),getZ(),0.125,0.125,0.125,0.02, 6);
            
            int trailSize = 3;
            if(this.age % 3 == 0 && !oldPos.isEmpty()){
               Vec3d endPos = MiscUtils.randomSpherePoint(oldPos.getLast(),1.25, 0.4);
               ParticleEffectUtils.trackedAnimatedLightningBolt(serverWorld, this::getEyePos, () -> endPos, (int)(Math.random()*5+5), 0.5, ParticleTypes.COMPOSTER,
                     12, 1, 0, 1, true, 5, 5);
            }
            oldPos.add(getPos());
            if(oldPos.size() > trailSize){
               oldPos.removeFirst();
            }
         }
         
         BlockPos blockPos = BlockPos.ofFloored(this.getPos());
         ChunkPos chunkPos = this.getChunkPos();
         if ((--this.chunkTicketExpiryTicks <= 0L || chunkX != ChunkSectionPos.getSectionCoord(blockPos.getX()) || chunkZ != ChunkSectionPos.getSectionCoord(blockPos.getZ())) && getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.resetIdleTimeout();
            this.chunkTicketExpiryTicks = ServerPlayerEntity.addEnderPearlTicket(serverWorld, chunkPos) - 1L;
         }
      }
   }
   
   @Nullable
   @Override
   public Entity teleportTo(TeleportTarget teleportTarget) {
      Entity entity = super.teleportTo(teleportTarget);
      if (entity != null) {
         entity.addPortalChunkTicketAt(BlockPos.ofFloored(entity.getPos()));
      }
      return entity;
   }
   
   private void applyImpactEffects(Entity hitEntity, List<LivingEntity> affectedEntities){
      StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS,25,9,false,false,true);
      StatusEffectInstance blind = new StatusEffectInstance(ArcanaRegistry.GREATER_BLINDNESS_EFFECT,25,5,false,false,true);
      boolean blindRage = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.BLINDING_RAGE) > 0;
      boolean voidStorm = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.VOID_STORM) > 0;
      
      if(hitEntity instanceof LivingEntity living){
         living.addStatusEffect(slow);
         if(blindRage){
            living.addStatusEffect(blind);
         }
      }
      
      if(voidStorm && getWorld() instanceof ServerWorld serverWorld){
         SoundUtils.playSound(serverWorld,BlockPos.ofFloored(getPos()), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS,.1f,2f);
         ParticleEffect dust = new DustColorTransitionParticleEffect(0x001c08,0x000000,2f);
         serverWorld.spawnParticles(dust,getX(),getY(),getZ(),150,1,1,1,0.02);
         for(int i = 0; i < 18; i++){
            ParticleEffectUtils.animatedLightningBolt(serverWorld, getEyePos(), MiscUtils.randomSpherePoint(getEyePos(),5,2), (int)(Math.random()*5+5), 0.5, ParticleTypes.COMPOSTER,
                  8, 1, 0, 1, false, 0, 5);
         }
         
         for(LivingEntity affectedEntity : affectedEntities){ // Void Storm
            ParticleEffectUtils.animatedLightningBolt(serverWorld, getEyePos(), affectedEntity.getEyePos(), (int)(Math.random()*5+5), 0.5, ParticleTypes.COMPOSTER,
                  8, 1, 0, 1, false, 4, 5);
            DamageSource source = ArcanaDamageTypes.of(serverWorld,ArcanaDamageTypes.ARCANE_LIGHTNING,this,getOwner() == null ? this : getOwner());
            float damage = 6.0f;
            if(affectedEntity.getType().isIn(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)) damage *= 1.25f;
            affectedEntity.damage(serverWorld,source,damage);
            
            if(blindRage){
               affectedEntity.addStatusEffect(blind);
            }
         }
      }
   }
   
   private List<LivingEntity> getSurroundingEntities(World world, Vec3d pos){
      ArrayList<LivingEntity> entities = new ArrayList<>();
      boolean voidStorm = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.VOID_STORM) > 0;
      if(!voidStorm) return entities;
      
      world.getOtherEntities(this.getOwner(), new Box(BlockPos.ofFloored(pos)).expand(6), entity -> entity.squaredDistanceTo(pos) < (4.5*4.5)).forEach(e -> {
         if(e instanceof LivingEntity living) entities.add(living);
      });
      
      return entities;
   }
   
   @Override
   protected void onEntityHit(EntityHitResult entityHitResult) {
      Entity target = entityHitResult.getEntity();
      float baseDamage = this.damage;
      Entity owner = this.getOwner();
      DamageSource damageSource = ArcanaDamageTypes.of(getWorld(),ArcanaDamageTypes.ARCANE_LIGHTNING,this,owner == null ? this : owner);
      if (this.getWorld() instanceof ServerWorld serverWorld) {
         baseDamage = EnchantmentHelper.getDamage(serverWorld, this.getWeaponStack(), target, damageSource, baseDamage);
      }
      
      int fireAspect = this.getWeaponStack().getEnchantments().getLevel(MiscUtils.getEnchantment(Enchantments.FIRE_ASPECT));
      if(!target.isFireImmune() && fireAspect > 0){
         target.setOnFireFor(fireAspect*4.0f);
      }
      applyImpactEffects(target, getSurroundingEntities(getWorld(),getPos()));
      
      if(target.getType().isIn(ArcanaRegistry.TENBROUS_BONUS_DAMAGE)) baseDamage *= 1.25f;
      
      if (target.sidedDamage(damageSource, baseDamage)) {
         if (this.getWorld() instanceof ServerWorld serverWorld) {
            EnchantmentHelper.onTargetDamaged(serverWorld, target, damageSource, this.getWeaponStack(), item -> this.kill(serverWorld));
         }
         
         if (target instanceof LivingEntity livingEntity) {
            this.knockback(livingEntity, damageSource);
            this.onHit(livingEntity);
            
            if(owner instanceof ServerPlayerEntity player) ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.SPEAR_OF_TENBROUS_IMPALE));
         }
      }
      
      this.deflect(ProjectileDeflection.SIMPLE, target, this.getOwner(), false);
      this.setVelocity(this.getVelocity().multiply(0.02, 0.2, 0.02));
      this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
      this.discard();
   }
   
   @Override
   public void remove(RemovalReason reason){
      super.remove(reason);
      if(this.getOwner() != null && this.getOwner() instanceof ServerPlayerEntity player && getWorld() instanceof ServerWorld serverWorld){
         int cooldownTime = 20 * (9 - 2*Math.max(0, ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.UNENDING_HATRED)));
         if(!player.isInCreativeMode()) ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(stack,player));
         player.getItemCooldownManager().set(stack,cooldownTime);
         
         this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT, 1.0F, 1.0F);
         SoundUtils.playSongToPlayer(player,SoundEvents.ENTITY_PLAYER_TELEPORT, 0.3F, 1.0F);
         
         serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),30,.2,0.2,.2,0.03);
         serverWorld.spawnParticles(ParticleTypes.PORTAL,getX(),getY(),getZ(),30,.2,0.2,.2,1);
      }
   }
   
   @Override
   protected ItemStack getDefaultItemStack(){
      return ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem();
   }
   
   @Override
   protected void onBlockHitEnchantmentEffects(ServerWorld world, BlockHitResult blockHitResult, ItemStack weaponStack) {
      Vec3d vec3d = blockHitResult.getBlockPos().clampToWithin(blockHitResult.getPos());
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
      
      applyImpactEffects(null, getSurroundingEntities(getWorld(),getPos()));
   }
   
   @Override
   public ItemStack getWeaponStack() {
      return this.getItemStack();
   }
   
   @Override
   protected boolean tryPickup(PlayerEntity player) {
      return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
   }
   
   @Override
   protected SoundEvent getHitSound() {
      return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
   }
   
   @Override
   public void onPlayerCollision(PlayerEntity player) {
      if (this.isOwner(player) || this.getOwner() == null) {
         super.onPlayerCollision(player);
      }
   }
   
   @Override
   protected float getDragInWater() {
      return 0.99F;
   }
   
   @Override
   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }
}
