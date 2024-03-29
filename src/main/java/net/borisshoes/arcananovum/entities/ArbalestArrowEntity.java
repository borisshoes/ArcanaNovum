package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.effects.DamageAmpEffect;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ArbalestArrowEntity extends ArrowEntity implements PolymerEntity {
   
   private int lvl;
   private static final int[] lvlLookup = new int[]{1,2,3,5};
   private double range;
   
   public ArbalestArrowEntity(EntityType<? extends ArbalestArrowEntity> entityType, World world) {
      super(entityType, world);
      this.lvl = 0;
      this.range = 2;
   }
   
   public ArbalestArrowEntity(World world, LivingEntity owner, int ampLvl, int rangeLvl, ItemStack stack) {
      this(ArcanaRegistry.ARBALEST_ARROW_ENTITY, world);
      this.setOwner(owner);
      this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      this.lvl = ampLvl > 3 ? ampLvl : lvlLookup[ampLvl];
      this.range = 2 + rangeLvl;
      this.stack = stack.copy();
      if(this.stack.hasNbt()){
         this.stack.removeSubNbt("QuiverId");
         this.stack.removeSubNbt("QuiverSlot");
      }
      if (this.stack.hasCustomName()) {
         this.setCustomName(this.stack.getName());
      }
      
      if(owner instanceof ServerPlayerEntity player){
         PLAYER_DATA.get(player).addXP(25); // Add xp
      }
   }
   
   @Override
   public EntityType<?> getPolymerEntityType(ServerPlayerEntity player){
      return PotionUtil.getPotionEffects(asItemStack()).isEmpty() ? EntityType.SPECTRAL_ARROW : EntityType.ARROW;
   }
   
   @Override
   public void tick(){
      super.tick();
   }
   
   @Override
   protected void onEntityHit(EntityHitResult entityHitResult){
      if(getEntityWorld() instanceof ServerWorld world){
         deployAura(world,getPos());
      }
      super.onEntityHit(entityHitResult);
      
      if(entityHitResult.getEntity().getType() == EntityType.PHANTOM && !entityHitResult.getEntity().isAlive()){
         if(getOwner() instanceof ServerPlayerEntity player){
            ArcanaAchievements.progress(player,ArcanaAchievements.MANY_BIRDS_MANY_ARROWS.id, 1);
         }
      }
   }
   
   @Override
   protected void onBlockHit(BlockHitResult blockHitResult){
      if(getEntityWorld() instanceof ServerWorld world){
         deployAura(world,getPos());
      }
      super.onBlockHit(blockHitResult);
      
      if(pickupType != PickupPermission.ALLOWED){
         this.discard();
      }
   }
   
   private void deployAura(ServerWorld serverWorld, Vec3d pos){
      List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(asItemStack());
      if(effects.isEmpty()){
         effects.add(new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT,100,lvl,false,false,false));
         effects.add(new StatusEffectInstance(StatusEffects.GLOWING,100,0,false,true,true));
      }
      
      ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.ALCHEMICAL_ARROW_AREA_EFFECT_TRACKER.getType()).addSource(AlchemicalArrowAreaEffectTracker.source(getOwner(), BlockPos.ofFloored(pos),serverWorld,range,lvl,effects));
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("ampLvl",lvl);
      nbt.putDouble("range",range);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if(nbt.contains("ampLvl")){
         lvl = nbt.getInt("ampLvl");
      }
      if(nbt.contains("range")){
         range = nbt.getDouble("range");
      }
   }
}
