package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
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
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RunicArrowEntity extends ArrowEntity implements PolymerEntity {
   
   private RunicArrow arrowType;
   private TreeMap<ArcanaAugment,Integer> augments;
   private NbtCompound data;
   
   public RunicArrowEntity(EntityType<? extends RunicArrowEntity> entityType, World world){
      super(entityType, world);
   }
   
   public RunicArrowEntity(World world, LivingEntity owner, ItemStack stack, @Nullable ItemStack shotFrom){
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      setOwner(owner);
      this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      data = new NbtCompound();
      initFromStack(stack,shotFrom);
   }
   
   public RunicArrowEntity(World world, double x, double y, double z, ItemStack stack, @Nullable ItemStack shotFrom){
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      this.setPosition(x,y,z);
      data = new NbtCompound();
      initFromStack(stack,shotFrom);
   }
   
   public void initFromStack(ItemStack arrowStack, ItemStack weaponStack){
      augments = ArcanaAugments.getAugmentsOnItem(arrowStack);
      arrowType = ArcanaItemUtils.identifyRunicArrow(arrowStack);
      setStack(arrowStack);
      this.pickupType = PickupPermission.CREATIVE_ONLY;
      
      if(this.stack.contains(DataComponentTypes.CUSTOM_NAME)){
         this.setCustomName(this.stack.getName());
      }
      
      if(weaponStack != null){
         this.weapon = weaponStack.copy();
         
         if(getWorld() instanceof ServerWorld serverWorld){
            int i = EnchantmentHelper.getProjectilePiercing(serverWorld, weapon, this.stack);
            if(i > 0){
               this.setPierceLevel((byte)i);
            }
            
            EnchantmentHelper.onProjectileSpawned(serverWorld, weapon, this, item -> this.weapon = null);
         }
      }
   }
   
   @Override
   public void setStack(ItemStack stack){
      super.setStack(stack);
      
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
         if(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.QUICK_RELEASE.id) > 0){
            if(getOwner() != null && getOwner().isSneaking()){
               data.putBoolean("severed",true);
               if(getOwner() instanceof ServerPlayerEntity player){
                  player.sendMessage(Text.literal("Arcane Tethers Severed").formatted(Formatting.GRAY, Formatting.ITALIC), true);
               }
            }
            
         }
      }else if(arrowType instanceof ArcaneFlakArrows){
         int armTime = data.getInt("armTime", 0);
         if(armTime > 0){
            armTime--;
            data.putInt("armTime",armTime);
         }
         
         if(armTime == 0){
            double senseRange = 4;
            List<Entity> triggerTargets = getWorld().getOtherEntities(this,this.getBoundingBox().expand(senseRange*2),
                  e -> !e.isSpectator() && e.distanceTo(this) <= senseRange && e instanceof LivingEntity && !e.isOnGround());
            if(!triggerTargets.isEmpty()){
               double radius = 4 + 1.25*ArcanaAugments.getAugmentFromMap(augments,ArcanaAugments.AIRBURST.id);
               ArcaneFlakArrows.detonate(this,radius);
            }
         }
      }else if(arrowType instanceof TrackingArrows){
         Vec3d velocityUnit = getVelocity().normalize();
         if(!data.contains("initYaw")){
            data.putFloat("initYaw",MathHelper.wrapDegrees((float)(MathHelper.atan2(velocityUnit.z, velocityUnit.x) * 57.2957763671875) - 90.0f));
         }
         if(!data.contains("initPos")){
            data.put("initPos", Vec3d.CODEC, this.getPos());
         }
         
         double viewWidth = 1.5*(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.RUNIC_GUIDANCE.id))+5;
         double distance = 15;
         List<LivingEntity> possibleTargets = getWorld().getEntitiesByClass(LivingEntity.class,getBoundingBox().expand(distance), e ->
               !e.isSpectator() && MiscUtils.inCone(this.getPos(),velocityUnit,distance,1,viewWidth,e.getPos().add(0,e.getHeight()/2,0)) && !e.isInvisible()
         );
         LivingEntity closestTarget = null;
         double distFromLine = distance;
         for(LivingEntity possibleTarget : possibleTargets){
            double dist = MiscUtils.distToLine(possibleTarget.getPos().add(0,possibleTarget.getHeight()/2,0),this.getPos(),this.getPos().add(velocityUnit.multiply(distance)));
            if(dist < distFromLine){
               distFromLine = dist;
               closestTarget = possibleTarget;
            }
         }
         
         if(closestTarget != null){
            Vec3d newVelocity = closestTarget.getPos().add(0,closestTarget.getHeight()/2,0).subtract(this.getPos()).normalize().multiply(this.getVelocity().length());
            this.setVelocity(newVelocity);
            
            if(this.getOwner() instanceof ServerPlayerEntity player){
               if(closestTarget.getUuid().equals(this.getOwner().getUuid())){
                  ArcanaAchievements.grant(player,ArcanaAchievements.TARGET_ACQUIRED.id);
               }
               Vector2d horizVel = new Vector2d(newVelocity.x, newVelocity.z);
               if(Math.abs(newVelocity.y / horizVel.length()) < 4){
                  float curYaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(newVelocity.z, newVelocity.x) * 57.2957763671875) - 90.0f);
                  float yawDiff = curYaw - data.getFloat("initYaw", 0.0f);
                  yawDiff += (yawDiff>180) ? -360 : (yawDiff<-180) ? 360 : 0;
                  Vector2d currentPos = new Vector2d(getX(),getZ());
                  Vec3d initPos3D = data.get("initPos", Vec3d.CODEC).orElse(Vec3d.ZERO);
                  Vector2d initPos = new Vector2d(initPos3D.x,initPos3D.z);
                  double distFromInitYawPlane = initPos.sub(currentPos).length() * Math.sin(Math.toRadians(Math.abs(yawDiff)));
                  if(Math.abs(yawDiff) >= 90 && distFromInitYawPlane > 10){
                     ArcanaAchievements.grant(player,ArcanaAchievements.THE_ARROW_KNOWS_WHERE_IT_IS.id);
                  }
               }
            }
            
            if(getWorld() instanceof ServerWorld serverWorld){
               ParticleEffectUtils.spawnLongParticle(serverWorld, ParticleTypes.END_ROD,getX(),getY(),getZ(),0,0,0,0,1);
            }
         }
      }
   }
   
   
   @Override
   protected void onEntityHit(EntityHitResult entityHitResult){
      if(arrowType != null){
         arrowType.entityHit(this,entityHitResult);
         
         if(this.getOwner() instanceof ServerPlayerEntity player){
            if(player.getPos().distanceTo(this.getPos()) >= 100) ArcanaAchievements.grant(player, ArcanaAchievements.AIMBOT.id);
            incArrowForEveryFoe(player);
         }
      }
      super.onEntityHit(entityHitResult);
   }
   
   public void incArrowForEveryFoe(ServerPlayerEntity player){
      ArcanaNovum.addArcanaEvent(new RunicArrowHitEvent(player,arrowType));
      if(ArcanaNovum.getEventsOfType(RunicArrowHitEvent.class).stream().filter(event -> event.getPlayer().equals(player)).map(event -> event.getArrowType().getId()).distinct().count() >= ((TimedAchievement) ArcanaAchievements.ARROW_FOR_EVERY_FOE).getGoal()){
         ArcanaAchievements.grant(player,ArcanaAchievements.ARROW_FOR_EVERY_FOE);
      }
   }
   
   public int getAugment(String id){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)) return entry.getValue();
      }
      return 0;
   }
   
   public RunicArrow getArrowType(){
      return arrowType;
   }
   
   @Override
   protected void onBlockHit(BlockHitResult blockHitResult){
      if(arrowType != null){
         arrowType.blockHit(this,blockHitResult);
         this.discard();
      }
      super.onBlockHit(blockHitResult);
   }
   
   public TreeMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public NbtCompound getData(){
      return data;
   }
   
   @Override
   protected void writeCustomData(WriteView view){
      super.writeCustomData(view);
      if(augments != null){
         NbtCompound augsCompound = new NbtCompound();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            augsCompound.putInt(entry.getKey().id,entry.getValue());
         }
         nbt.put("runicAugments",augsCompound);
      }
      if(arrowType != null){
         nbt.putString("runicArrowType",arrowType.getId());
      }
      if(data != null){
         nbt.put("runicArrowData",data);
      }
   }
   
   @Override
   protected void readCustomData(ReadView view){
      super.readCustomData(view);
      augments = new TreeMap<>();
      if(nbt.contains("runicAugments")){
         NbtCompound augCompound = nbt.getCompoundOrEmpty("runicAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug, augCompound.getInt(key, 0));
         }
      }
      if(nbt.contains("runicArrowType")){
         ArcanaItem arcanaItem = ArcanaRegistry.getArcanaItem(nbt.getString("runicArrowType", ""));
         if(arcanaItem instanceof RunicArrow ra) arrowType = ra;
      }
      if(nbt.contains("runicArrowData")){
         data = nbt.getCompoundOrEmpty("runicArrowData");
      }
   }
}
