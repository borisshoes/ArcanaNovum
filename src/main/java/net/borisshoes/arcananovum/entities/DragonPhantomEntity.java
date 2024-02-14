package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.mixins.PhantomEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class DragonPhantomEntity extends PhantomEntity implements PolymerEntity {
   
   private int numPlayers;
   
   public DragonPhantomEntity(EntityType<? extends DragonPhantomEntity> entityType, World world){
      super(entityType, world);
      this.numPlayers = 5;
      setPhantomSize(20);
   }
   
   @Override
   public void modifyRawTrackedData(List<DataTracker.SerializedEntry<?>> data, ServerPlayerEntity player, boolean initial){
      data.add(new DataTracker.SerializedEntry<>(PhantomEntityAccessor.getSIZE().getId(), PhantomEntityAccessor.getSIZE().getType(), getPhantomSize()));
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
      return EntityType.PHANTOM;
   }
   
   public void setNumPlayers(int numPlayers){
      this.numPlayers = numPlayers;
   }
   
   public static DefaultAttributeContainer.Builder createPhantomAttributes() {
      return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0)
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 512.0)
            .add(EntityAttributes.GENERIC_ARMOR, 10)
            .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public void tick(){
      super.tick();
      try{
         setPersistent();
         
         if(getEntityWorld() instanceof ServerWorld serverWorld){
            serverWorld.spawnParticles(new DustParticleEffect(Vec3d.unpackRgb(16711892).toVector3f(),3f),getX(),getY(),getZ(),1,1.5,1,1.5,0);
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   protected float modifyAppliedDamage(DamageSource source, float amount) {
      float scale = numPlayers > 0 ? 2f/numPlayers : 1;
      scale = Math.max(scale,0.1f);
      if(source.getAttacker() instanceof EnderDragonEntity) amount = 0;
      if(source.isIn(DamageTypeTags.BYPASSES_ARMOR)) amount *= 0.25f; // Reduce damage from magic sources and immune to the dragon
      amount *= scale;
      return amount;
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("numPlayers",numPlayers);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt){
      super.readCustomDataFromNbt(nbt);
      numPlayers = nbt.getInt("numPlayers");
   }
}
