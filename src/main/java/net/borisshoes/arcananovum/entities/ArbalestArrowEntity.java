package net.borisshoes.arcananovum.entities;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public class ArbalestArrowEntity extends ArrowEntity implements PolymerEntity {
   
   private int lvl;
   private static final int[] lvlLookup = new int[]{1,2,3,5};
   private double range;
   
   public ArbalestArrowEntity(EntityType<? extends ArbalestArrowEntity> entityType, World world){
      super(entityType, world);
      this.lvl = 0;
      this.range = 2;
   }
   
   public ArbalestArrowEntity(World world, LivingEntity owner, int ampLvl, int rangeLvl, ItemStack arrowStack, @Nullable ItemStack weaponStack){
      this(ArcanaRegistry.ARBALEST_ARROW_ENTITY, world);
      this.setOwner(owner);
      this.setPosition(owner.getX(), owner.getEyeY() - (double)0.1f, owner.getZ());
      this.lvl = ampLvl > 3 ? ampLvl : lvlLookup[ampLvl];
      this.range = 2 + rangeLvl;
      initFromStack(arrowStack, weaponStack);
      
      if(owner instanceof ServerPlayerEntity player){
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.ALCHEMICAL_ARBALEST_SHOOT)); // Add xp
      }
   }
   
   public void initFromStack(ItemStack arrowStack, ItemStack weaponStack){
      setStack(arrowStack);
      
      Unit unit = stack.remove(DataComponentTypes.INTANGIBLE_PROJECTILE);
      if(unit != null){
         this.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
      }
      
      ArcanaItem.removeProperty(this.stack, QuiverItem.QUIVER_ID_TAG);
      ArcanaItem.removeProperty(this.stack, QuiverItem.QUIVER_SLOT_TAG);
      
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
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return this.stack.contains(DataComponentTypes.POTION_CONTENTS) ? EntityType.ARROW : EntityType.SPECTRAL_ARROW;
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
      List<StatusEffectInstance> effects = new ArrayList<>();
      this.stack.getOrDefault(DataComponentTypes.POTION_CONTENTS,PotionContentsComponent.DEFAULT).getEffects().forEach(effects::add);
      if(effects.isEmpty()){
         effects.add(new StatusEffectInstance(ArcanaRegistry.DAMAGE_AMP_EFFECT,100,lvl,false,false,false));
         effects.add(new StatusEffectInstance(StatusEffects.GLOWING,100,0,false,true,true));
      }
      
      ArcanaRegistry.AREA_EFFECTS.get(ArcanaRegistry.ALCHEMICAL_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(AlchemicalArrowAreaEffectTracker.source(getOwner(), BlockPos.ofFloored(pos),serverWorld,range,lvl,effects));
   }
   
   @Override
   public void writeCustomDataToNbt(NbtCompound nbt){
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("ampLvl",lvl);
      nbt.putDouble("range",range);
   }
   
   @Override
   public void readCustomDataFromNbt(NbtCompound nbt){
      super.readCustomDataFromNbt(nbt);
      if(nbt.contains("ampLvl")){
         lvl = nbt.getInt("ampLvl", 0);
      }
      if(nbt.contains("range")){
         range = nbt.getDouble("range", 0.0);
      }
   }
}
