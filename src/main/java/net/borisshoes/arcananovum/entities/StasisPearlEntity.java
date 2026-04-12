package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.StasisPearl;
import net.borisshoes.arcananovum.mixins.EntityAccessor;
import net.borisshoes.arcananovum.mixins.ThrowableItemProjectileAccessor;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class StasisPearlEntity extends ThrownEnderpearl implements PolymerEntity {
   
   private final ItemStack pearlStack = ArcanaRegistry.STASIS_PEARL.getPrefItem();
   private String itemStackId;
   private int stasisTime;
   private boolean inStasis;
   private Vec3 savedVelocity;
   private CompoundTag augments;
   
   public StasisPearlEntity(EntityType<? extends StasisPearlEntity> entityType, Level world){
      super(entityType, world);
   }
   
   public StasisPearlEntity(Level world, LivingEntity owner, String itemUuid, CompoundTag augments){
      this(ArcanaRegistry.STASIS_PEARL_ENTITY, world);
      setPos(owner.getX(), owner.getEyeY() - (double) 0.1f, owner.getZ());
      setOwner(owner);
      itemStackId = itemUuid;
      this.augments = augments;
      this.savedVelocity = getDeltaMovement();
   }
   
   @Override
   public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial){
      data.add(new SynchedEntityData.DataValue<>(ThrowableItemProjectileAccessor.getDATA_ITEM_STACK().id(), ThrowableItemProjectileAccessor.getDATA_ITEM_STACK().serializer(), pearlStack.copy()));
      data.add(new SynchedEntityData.DataValue<>(EntityAccessor.getDATA_NO_GRAVITY().id(), EntityAccessor.getDATA_NO_GRAVITY().serializer(), inStasis));
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.ENDER_PEARL;
   }
   
   public void setStasis(boolean stasis){
      inStasis = stasis;
      ArcanaItem.putProperty(pearlStack, StasisPearl.ACTIVE_TAG, stasis); // For visuals only, not related to stack in inventory
      ArcanaItem.putProperty(pearlStack, StasisPearl.PEARL_ID_TAG, stasis ? "-" : "");
      
      if(inStasis){
         this.savedVelocity = getDeltaMovement();
         setDeltaMovement(0, 0, 0);
         setNoGravity(true);
      }else{
         if(stasisTime >= 6000 && getOwner() instanceof ServerPlayer player){
            ArcanaAchievements.grant(player, ArcanaAchievements.PEARL_HANG);
         }
         setDeltaMovement(this.savedVelocity);
         setNoGravity(false);
         stasisTime = 0;
      }
   }
   
   public void killNextTick(){
      stasisTime = Integer.MAX_VALUE - 1;
   }
   
   @Override
   public void tick(){
      super.tick();
      if(stasisTime > 36000){
         this.discard();
      }
      
      if(inStasis){
         stasisTime++;
         
         if(getDeltaMovement().length() > 0.001){
            setDeltaMovement(0, 0, 0);
         }
      }
      
      // Update holder every second
      MinecraftServer server = this.level().getServer();
      if(server != null){
         if(server.getTickCount() % 20 == 0){
            resyncHolder();
         }
         
         if(this.level() instanceof ServerLevel serverWorld){
            ArcanaEffectUtils.stasisPearl(serverWorld, position());
         }
      }
      
   }
   
   public void resyncHolder(){
      ServerPlayer holder = ArcanaItemUtils.findHolder(this.level().getServer(), itemStackId);
      if(holder != null && holder.connection.isAcceptingMessages()){
         setOwner(holder);
         ItemStack stack = ArcanaItemUtils.getHolderStack(holder, itemStackId);
         ArcanaItem.putProperty(stack, StasisPearl.PEARL_ID_TAG, this.getStringUUID());
         ArcanaItem.putProperty(stack, StasisPearl.ACTIVE_TAG, this.inStasis);
      }
      
   }
   
   @Override
   protected void onHit(HitResult hitResult){
      if(inStasis) return; // Stasis'd pearl is immune to collisions
      
      // Find Holder of the item
      ServerPlayer holder = null;
      if(itemStackId != null && level().getServer() != null){
         holder = ArcanaItemUtils.findHolder(level().getServer(), itemStackId);
         if(holder != null && holder.connection.isAcceptingMessages() && holder.level() == this.level() && !holder.isSleeping()){
            setOwner(holder);
            
            if(holder.position().distanceTo(position()) >= 1000){
               ArcanaAchievements.grant(holder, ArcanaAchievements.INSTANT_TRANSMISSION);
            }
            int reconstructLvl = ArcanaAugments.getAugmentFromCompound(augments, ArcanaAugments.STASIS_RECONSTRUCTION);
            if(reconstructLvl > 0){
               int duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.STASIS_PEARL_RECONSTRUCT_DURATION);
               float rejuvRate = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.STASIS_PEARL_REGEN_PER_LVL).get(reconstructLvl);
               float fortMod = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.STASIS_PEARL_FORTITUDE_PER_LVL).get(reconstructLvl);
               ConditionInstance rejuv = new ConditionInstance(Conditions.REJUVENATION, arcanaId(ArcanaRegistry.STASIS_PEARL.getId()), duration, rejuvRate, true, true, false, AttributeModifier.Operation.ADD_VALUE, holder.getUUID());
               ConditionInstance fortitude = new ConditionInstance(Conditions.FORTITUDE, arcanaId(ArcanaRegistry.STASIS_PEARL.getId()), duration, -fortMod, true, true, false, AttributeModifier.Operation.ADD_VALUE, holder.getUUID());
               Conditions.addCondition(holder.level().getServer(), holder, rejuv);
               Conditions.addCondition(holder.level().getServer(), holder, fortitude);
               
               holder.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + holder.getBbHeight() / 2, getZ(), 10 * reconstructLvl, .5, .5, .5, 1);
            }
         }
      }
      super.onHit(hitResult);
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      if(augments != null){
         view.store("augments", CompoundTag.CODEC, augments);
      }
      view.putBoolean("inStasis", inStasis);
      view.putInt("stasisTime", stasisTime);
      view.putString("stackUuid", itemStackId);
      view.putDouble("savedDX", savedVelocity.x);
      view.putDouble("savedDY", savedVelocity.y);
      view.putDouble("savedDZ", savedVelocity.z);
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      augments = view.read("augments", CompoundTag.CODEC).orElse(new CompoundTag());
      inStasis = view.getBooleanOr("inStasis", false);
      stasisTime = view.getIntOr("stasisTime", 0);
      itemStackId = view.getStringOr("stackUuid", "");
      savedVelocity = new Vec3(view.getDoubleOr("savedDX", 0.0), view.getDoubleOr("savedDY", 0.0), view.getDoubleOr("savedDZ", 0.0));
   }
}
