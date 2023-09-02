package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.mixins.EntityAccessor;
import net.borisshoes.arcananovum.mixins.ThrownItemEntityAccessor;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StasisPearlEntity extends EnderPearlEntity implements PolymerEntity {
   
   private final ItemStack pearlStack = new ItemStack(Items.ENDER_PEARL);
   private String itemStackId;
   private int stasisTime;
   private boolean inStasis;
   private Vec3d savedVelocity;
   private NbtCompound augments;
   
   public StasisPearlEntity(EntityType<? extends StasisPearlEntity> entityType, World world){
      super(entityType, world);
      pearlStack.addEnchantment(Enchantments.MENDING,1);
   }
   
   public StasisPearlEntity(World world, LivingEntity owner, String itemUuid, NbtCompound augments){
      this(ArcanaRegistry.STASIS_PEARL_ENTITY,world);
      setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      setOwner(owner);
      itemStackId = itemUuid;
      this.augments = augments;
      this.savedVelocity = getVelocity();
   }
   
   @Override
   public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial){
      data.add(new DataTracker.SerializedEntry<>(ThrownItemEntityAccessor.getITEM().getId(),ThrownItemEntityAccessor.getITEM().getType(), pearlStack.copy()));
      data.add(new DataTracker.SerializedEntry<>(EntityAccessor.getNO_GRAVITY().getId(), EntityAccessor.getNO_GRAVITY().getType(), inStasis));
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
      return EntityType.ENDER_PEARL;
   }
   
   public void setStasis(boolean stasis){
      inStasis = stasis;
      
      if(inStasis){
         this.savedVelocity = getVelocity();
         setVelocity(0,0,0);
         setNoGravity(true);
      }else{
         if(stasisTime >= 6000 && getOwner() instanceof ServerPlayerEntity player){
            ArcanaAchievements.grant(player,ArcanaAchievements.PEARL_HANG.id);
         }
         setVelocity(this.savedVelocity);
         setNoGravity(false);
         stasisTime = 0;
      }
   }
   
   public void killNextTick(){
      stasisTime = Integer.MAX_VALUE-1;
   }
   
   @Override
   public void tick(){
      super.tick();
      if(stasisTime > 36000){
         this.discard();
      }
      
      if(inStasis){
         stasisTime++;
         
         if(getVelocity().length() > 0.001){
            setVelocity(0,0,0);
         }
      }
      
      MinecraftServer server = this.getServer();
      if(server != null){
         // Update holder every second
         if(server.getTicks() % 20 == 0){
            ServerPlayerEntity holder = MagicItemUtils.findHolder(server,itemStackId);
            if(holder != null && holder.networkHandler.isConnectionOpen()){
               setOwner(holder);
            }
         }
         
         if(this.getWorld() instanceof ServerWorld serverWorld){
            ParticleEffectUtils.stasisPearl(serverWorld,getPos());
         }
      }
   }
   
   @Override
   protected void onCollision(HitResult hitResult) {
      if(inStasis) return; // Stasis'd pearl is immune to collisions
      
      // Find Holder of the item
      ServerPlayerEntity holder = null;
      if(itemStackId != null && getServer() != null){
         holder = MagicItemUtils.findHolder(getServer(),itemStackId);
         if(holder != null && holder.networkHandler.isConnectionOpen() && holder.getWorld() == this.getWorld() && !holder.isSleeping()) {
            setOwner(holder);
            
            if(holder.getPos().distanceTo(getPos()) >= 1000){
               ArcanaAchievements.grant(holder, ArcanaAchievements.INSTANT_TRANSMISSION.id);
            }
            int reconstructLvl = augments.getInt(ArcanaAugments.STASIS_RECONSTRUCTION.id);
            if(reconstructLvl > 0){
               StatusEffectInstance regen = new StatusEffectInstance(StatusEffects.REGENERATION, 100, reconstructLvl, false, true, true);
               StatusEffectInstance resist = new StatusEffectInstance(StatusEffects.RESISTANCE, 60, reconstructLvl-1, false, true, true);
               holder.addStatusEffect(regen);
               holder.addStatusEffect(resist);
               
               holder.getServerWorld().spawnParticles(ParticleTypes.HAPPY_VILLAGER,getX(),getY()+holder.getHeight()/2,getZ(),10*reconstructLvl, .5,.5,.5,1);
            }
         }
      }
      super.onCollision(hitResult);
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if(augments != null){
         nbt.put("augments",augments);
      }
      nbt.putBoolean("inStasis",inStasis);
      nbt.putInt("stasisTime",stasisTime);
      nbt.putString("stackUuid",itemStackId);
      nbt.putDouble("savedDX",savedVelocity.x);
      nbt.putDouble("savedDY",savedVelocity.y);
      nbt.putDouble("savedDZ",savedVelocity.z);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if(nbt.contains("augments")){
         augments = nbt.getCompound("augments");
      }
      inStasis = nbt.getBoolean("inStasis");
      stasisTime = nbt.getInt("stasisTime");
      itemStackId = nbt.getString("stackUuid");
      savedVelocity = new Vec3d(nbt.getDouble("savedDX"),nbt.getDouble("savedDY"),nbt.getDouble("savedDZ"));
   }
   
   
   @Override
   @Nullable
   public Entity moveToWorld(ServerWorld destination) {
      Entity entity = this.getOwner();
      if (entity != null && entity.getWorld().getRegistryKey() != destination.getRegistryKey()) {
         this.discard();
      }
      return super.moveToWorld(destination);
   }
}
