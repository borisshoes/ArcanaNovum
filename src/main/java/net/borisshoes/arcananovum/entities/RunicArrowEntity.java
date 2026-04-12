package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.events.RunicArrowHitEvent;
import net.borisshoes.arcananovum.items.arrows.ArcaneFlakArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.items.arrows.TetherArrows;
import net.borisshoes.arcananovum.items.arrows.TrackingArrows;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.utils.MathUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RunicArrowEntity extends Arrow implements PolymerEntity {
   
   private RunicArrow arrowType;
   private TreeMap<ArcanaAugment, Integer> augments;
   private CompoundTag data;
   
   public RunicArrowEntity(EntityType<? extends RunicArrowEntity> entityType, Level world){
      super(entityType, world);
   }
   
   public RunicArrowEntity(Level world, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom){
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      setOwner(owner);
      this.setPos(owner.getX(), owner.getEyeY() - (double) 0.1f, owner.getZ());
      data = new CompoundTag();
      initFromStack(stack, shotFrom);
   }
   
   public RunicArrowEntity(Level world, double x, double y, double z, ItemStack stack, @Nullable ItemStack shotFrom){
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      this.setPos(x, y, z);
      data = new CompoundTag();
      initFromStack(stack, shotFrom);
   }
   
   public void initFromStack(ItemStack arrowStack, ItemStack weaponStack){
      augments = ArcanaAugments.getAugmentsOnItem(arrowStack);
      arrowType = ArcanaItemUtils.identifyRunicArrow(arrowStack);
      setPickupItemStack(arrowStack);
      this.pickup = Pickup.CREATIVE_ONLY;
      
      if(this.pickupItemStack.has(DataComponents.CUSTOM_NAME)){
         this.setCustomName(this.pickupItemStack.getHoverName());
      }
      
      if(weaponStack != null){
         this.firedFromWeapon = weaponStack.copy();
         
         if(level() instanceof ServerLevel serverWorld){
            int i = EnchantmentHelper.getPiercingCount(serverWorld, firedFromWeapon, this.pickupItemStack);
            if(i > 0){
               this.setPierceLevel((byte) i);
            }
            
            EnchantmentHelper.onProjectileSpawned(serverWorld, firedFromWeapon, this, item -> this.firedFromWeapon = null);
         }
      }
   }
   
   @Override
   public void setPickupItemStack(ItemStack stack){
      super.setPickupItemStack(stack);
      
      if(arrowType instanceof ArcaneFlakArrows){
         data.putInt("armTime", 5);
      }
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.ARROW;
   }
   
   @Override
   public void tick(){
      super.tick();
      
      if(arrowType instanceof TetherArrows){
         if(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.QUICK_RELEASE) > 0){
            if(getOwner() != null && getOwner().isShiftKeyDown()){
               data.putBoolean("severed", true);
               if(getOwner() instanceof ServerPlayer player){
                  player.sendSystemMessage(Component.literal("Arcane Tethers Severed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
               }
            }
            
         }
      }else if(arrowType instanceof ArcaneFlakArrows){
         int armTime = data.getIntOr("armTime", 0);
         if(armTime > 0){
            armTime--;
            data.putInt("armTime", armTime);
         }
         
         if(armTime == 0){
            double senseRange = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.FLAK_ARROW_SENSE_RANGE);
            List<Entity> triggerTargets = level().getEntities(this, this.getBoundingBox().inflate(senseRange * 2),
                  e -> !e.isSpectator() && e.distanceTo(this) <= senseRange && e instanceof LivingEntity && !e.onGround());
            if(!triggerTargets.isEmpty()){
               double baseR = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.FLAK_ARROW_RANGE);
               double extraR = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL).get(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.AIRBURST));
               double radius = baseR + extraR;
               ArcaneFlakArrows.detonate(this, radius);
            }
         }
      }else if(arrowType instanceof TrackingArrows){
         Vec3 velocityUnit = getDeltaMovement().normalize();
         if(!data.contains("initYaw")){
            data.putFloat("initYaw", Mth.wrapDegrees((float) (Mth.atan2(velocityUnit.z, velocityUnit.x) * 57.2957763671875) - 90.0f));
         }
         if(!data.contains("initPos")){
            data.store("initPos", Vec3.CODEC, this.position());
         }
         
         double viewWidth = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.TRACKING_ARROW_DETECTION_WIDTH_PER_LVL).get(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.RUNIC_GUIDANCE));
         double distance = 15;
         List<LivingEntity> possibleTargets = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(distance), e ->
               !e.isSpectator() && MathUtils.inCone(this.position(), velocityUnit, distance, 1, viewWidth, e.position().add(0, e.getBbHeight() / 2, 0)) && !e.isInvisible()
         );
         LivingEntity closestTarget = null;
         double distFromLine = distance;
         for(LivingEntity possibleTarget : possibleTargets){
            double dist = MathUtils.distToLine(possibleTarget.position().add(0, possibleTarget.getBbHeight() / 2, 0), this.position(), this.position().add(velocityUnit.scale(distance)));
            if(dist < distFromLine){
               distFromLine = dist;
               closestTarget = possibleTarget;
            }
         }
         
         if(closestTarget != null){
            Vec3 newVelocity = closestTarget.position().add(0, closestTarget.getBbHeight() / 2, 0).subtract(this.position()).normalize().scale(this.getDeltaMovement().length());
            this.setDeltaMovement(newVelocity);
            
            if(this.getOwner() instanceof ServerPlayer player){
               if(closestTarget.getUUID().equals(this.getOwner().getUUID())){
                  ArcanaAchievements.grant(player, ArcanaAchievements.TARGET_ACQUIRED);
               }
               Vector2d horizVel = new Vector2d(newVelocity.x, newVelocity.z);
               if(Math.abs(newVelocity.y / horizVel.length()) < 4){
                  float curYaw = Mth.wrapDegrees((float) (Mth.atan2(newVelocity.z, newVelocity.x) * 57.2957763671875) - 90.0f);
                  float yawDiff = curYaw - data.getFloatOr("initYaw", 0.0f);
                  yawDiff += (yawDiff > 180) ? -360 : (yawDiff < -180) ? 360 : 0;
                  Vector2d currentPos = new Vector2d(getX(), getZ());
                  Vec3 initPos3D = data.read("initPos", Vec3.CODEC).orElse(Vec3.ZERO);
                  Vector2d initPos = new Vector2d(initPos3D.x, initPos3D.z);
                  double distFromInitYawPlane = initPos.sub(currentPos).length() * Math.sin(Math.toRadians(Math.abs(yawDiff)));
                  if(Math.abs(yawDiff) >= 90 && distFromInitYawPlane > 10){
                     ArcanaAchievements.grant(player, ArcanaAchievements.THE_ARROW_KNOWS_WHERE_IT_IS);
                  }
               }
            }
            
            if(level() instanceof ServerLevel serverWorld){
               ArcanaEffectUtils.spawnLongParticle(serverWorld, ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0, 0, 1);
            }
         }
      }
   }
   
   
   @Override
   protected void onHitEntity(EntityHitResult entityHitResult){
      if(arrowType != null){
         arrowType.entityHit(this, entityHitResult);
         
         if(this.getOwner() instanceof ServerPlayer player){
            if(player.position().distanceTo(this.position()) >= 100)
               ArcanaAchievements.grant(player, ArcanaAchievements.AIMBOT);
            incArrowForEveryFoe(player);
         }
      }
      super.onHitEntity(entityHitResult);
   }
   
   public void incArrowForEveryFoe(ServerPlayer player){
      Event.addEvent(new RunicArrowHitEvent(player, arrowType));
      if(Event.getEventsOfType(RunicArrowHitEvent.class).stream().filter(event -> event.getPlayer().equals(player)).map(event -> event.getArrowType().getId()).distinct().count() >= ((TimedAchievement) ArcanaAchievements.ARROW_FOR_EVERY_FOE).getGoal()){
         ArcanaAchievements.grant(player, ArcanaAchievements.ARROW_FOR_EVERY_FOE);
      }
   }
   
   public int getAugment(ArcanaAugment augment){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey() == augment) return entry.getValue();
      }
      return 0;
   }
   
   public RunicArrow getArrowType(){
      return arrowType;
   }
   
   @Override
   protected void onHitBlock(BlockHitResult blockHitResult){
      if(arrowType != null){
         arrowType.blockHit(this, blockHitResult);
         this.discard();
      }
      super.onHitBlock(blockHitResult);
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public CompoundTag getData(){
      return data;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.storeNullable("runicAugments", ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC, this.augments);
      view.putString("runicArrowType", arrowType.getId());
      view.storeNullable("runicArrowData", CompoundTag.CODEC, data);
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      this.augments = new TreeMap<>();
      view.read("runicAugments", ArcanaAugments.AugmentData.AUGMENT_MAP_CODEC).ifPresent(data -> {
         this.augments = data;
      });
      ArcanaItem arcanaItem = ArcanaRegistry.getArcanaItem(view.getStringOr("runicArrowType", ""));
      if(arcanaItem instanceof RunicArrow ra) arrowType = ra;
      data = view.read("runicArrowData", CompoundTag.CODEC).orElse(new CompoundTag());
   }
}
