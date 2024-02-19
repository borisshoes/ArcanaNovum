package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.TimedAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.arrows.ArcaneFlakArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.items.arrows.TetherArrows;
import net.borisshoes.arcananovum.items.arrows.TrackingArrows;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2d;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class RunicArrowEntity extends ArrowEntity implements PolymerEntity {
   
   private RunicArrow arrowType;
   private TreeMap<ArcanaAugment,Integer> augments;
   private NbtCompound data;
   
   public RunicArrowEntity(EntityType<? extends RunicArrowEntity> entityType, World world) {
      super(entityType, world);
   }
   
   public RunicArrowEntity(World world, LivingEntity owner, ItemStack stack) {
      this(ArcanaRegistry.RUNIC_ARROW_ENTITY, world);
      setOwner(owner);
      this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      this.pickupType = PickupPermission.CREATIVE_ONLY;
      
      augments = ArcanaAugments.getAugmentsOnItem(stack);
      arrowType = MagicItemUtils.identifyRunicArrow(stack);
      data = new NbtCompound();
   }
   
   @Override
   public void initFromStack(ItemStack stack){
      super.initFromStack(stack);
      
      if(arrowType instanceof ArcaneFlakArrows){
         data.putInt("armTime", 5);
      }
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
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
         int armTime = data.getInt("armTime");
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
            data.put("initPos", this.toNbtList(this.getX(), this.getY(), this.getZ()));
         }
         
         double viewWidth = 1.5*(ArcanaAugments.getAugmentFromMap(augments, ArcanaAugments.RUNIC_GUIDANCE.id))+5;
         double distance = 15;
         List<LivingEntity> possibleTargets = getEntityWorld().getEntitiesByClass(LivingEntity.class,getBoundingBox().expand(distance), e -> !e.isSpectator() && MiscUtils.inCone(this.getPos(),velocityUnit,distance,1,viewWidth,e.getPos().add(0,e.getHeight()/2,0)));
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
                  float yawDiff = curYaw-data.getFloat("initYaw");
                  yawDiff += (yawDiff>180) ? -360 : (yawDiff<-180) ? 360 : 0;
                  Vector2d currentPos = new Vector2d(getX(),getZ());
                  NbtList posList = data.getList("initPos", NbtElement.DOUBLE_TYPE);
                  Vector2d initPos = new Vector2d(posList.getDouble(0),posList.getDouble(2));
                  double distFromInitYawPlane = initPos.sub(currentPos).length() * Math.sin(Math.toRadians(Math.abs(yawDiff)));
                  if(Math.abs(yawDiff) >= 90 && distFromInitYawPlane > 10){
                     ArcanaAchievements.grant(player,ArcanaAchievements.THE_ARROW_KNOWS_WHERE_IT_IS.id);
                  }
               }
            }
            
            if(getEntityWorld() instanceof ServerWorld serverWorld){
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
      // Do this bit manually so extra data can be saved
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(ArcanaAchievements.ARROW_FOR_EVERY_FOE instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            NbtCompound comp = new NbtCompound();
            comp.putBoolean(arrowType.getId(),true);
            newAch.setData(comp);
            profile.setAchievement(itemId, newAch);
            ArcanaAchievements.progress(player, ArcanaAchievements.ARROW_FOR_EVERY_FOE.id,1);
         }else if(!achievement.isAcquired()){
            NbtCompound comp = achievement.getData();
            if(!comp.contains(arrowType.getId())){
               comp.putBoolean(arrowType.getId(), true);
               achievement.setData(comp);
               profile.setAchievement(itemId, achievement);
               ArcanaAchievements.progress(player, ArcanaAchievements.ARROW_FOR_EVERY_FOE.id, 1);
            }
         }
      }
   }
   
   public int getAugment(String id){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)) return entry.getValue();
      }
      return 0;
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
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
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
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      augments = new TreeMap<>();
      if(nbt.contains("runicAugments")){
         NbtCompound augCompound = nbt.getCompound("runicAugments");
         for(String key : augCompound.getKeys()){
            ArcanaAugment aug = ArcanaAugments.registry.get(key);
            if(aug != null) augments.put(aug,augCompound.getInt(key));
         }
      }
      if(nbt.contains("runicArrowType")){
         MagicItem magicItem = ArcanaRegistry.MAGIC_ITEMS.get(nbt.getString("runicArrowType"));
         if(magicItem instanceof RunicArrow ra) arrowType = ra;
      }
      if(nbt.contains("runicArrowData")){
         data = nbt.getCompound("runicArrowData");
      }
   }
}
