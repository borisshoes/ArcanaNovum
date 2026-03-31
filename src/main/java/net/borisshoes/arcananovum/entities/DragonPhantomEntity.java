package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import xyz.nucleoid.packettweaker.PacketContext;

public class DragonPhantomEntity extends Phantom implements PolymerEntity {
   
   private int numPlayers;
   
   public DragonPhantomEntity(EntityType<? extends DragonPhantomEntity> entityType, Level world){
      super(entityType, world);
      this.numPlayers = 5;
      getAttributes().getInstance(Attributes.SCALE).addPermanentModifier(new AttributeModifier(ArcanaRegistry.arcanaId("phantom_scale"), 4.0, AttributeModifier.Operation.ADD_VALUE));
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return EntityType.PHANTOM;
   }
   
   public void setNumPlayers(int numPlayers){
      this.numPlayers = numPlayers;
   }
   
   public static AttributeSupplier.Builder createPhantomAttributes(){
      return Monster.createMonsterAttributes()
            .add(Attributes.SCALE, 1.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.MAX_HEALTH, 512.0)
            .add(Attributes.ARMOR, 10)
            .add(Attributes.ARMOR_TOUGHNESS, 10)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
   }
   
   @Override
   public void tick(){
      super.tick();
      try{
         setPersistenceRequired();
         
         if(level() instanceof ServerLevel serverWorld && serverWorld.getServer().getTickCount() % 2 == 0){
            serverWorld.sendParticles(new DustParticleOptions(0xFF00D4, 3f), getX(), getY(), getZ(), 1, 1.5, 1, 1.5, 0);
            
            
            if(serverWorld.dimension().equals(Level.END) && this.anchorPoint.getY() > 120){
               this.anchorPoint = new BlockPos(this.anchorPoint.getX(), 110, this.anchorPoint.getZ());
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   protected float getDamageAfterMagicAbsorb(DamageSource source, float amount){
      float scale = numPlayers > 0 ? 2f / numPlayers : 1;
      scale = Math.max(scale, 0.1f);
      if(source.getEntity() instanceof EnderDragon) amount = 0;
      if(source.is(DamageTypeTags.BYPASSES_ARMOR))
         amount *= 0.25f; // Reduce damage from magic sources and immune to the dragon
      if(amount > getMaxHealth() / 0.1) amount = getMaxHealth() / 0.1f;
      amount *= scale;
      return amount;
   }
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putInt("numPlayers", numPlayers);
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      numPlayers = view.getIntOr("numPlayers", 0);
   }
}
