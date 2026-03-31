package net.borisshoes.arcananovum.entities;

import com.mojang.datafixers.util.Either;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.areaeffects.AlchemicalArrowAreaEffectTracker;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaRegistry.arcanaId;

public class ArbalestArrowEntity extends Arrow implements PolymerEntity {
   
   private int lvl;
   private double range;
   
   public ArbalestArrowEntity(EntityType<? extends ArbalestArrowEntity> entityType, Level world){
      super(entityType, world);
      this.lvl = 0;
      this.range = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.ALCHEMICAL_ARBALEST_FIELD_RANGE_PER_LVL).getFirst();
   }
   
   public ArbalestArrowEntity(Level world, LivingEntity owner, int ampLvl, int rangeLvl, ItemStack arrowStack, @Nullable ItemStack weaponStack){
      this(ArcanaRegistry.ARBALEST_ARROW_ENTITY, world);
      this.setOwner(owner);
      this.setPos(owner.getX(), owner.getEyeY() - (double) 0.1f, owner.getZ());
      this.lvl = ampLvl;
      this.range = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.ALCHEMICAL_ARBALEST_FIELD_RANGE_PER_LVL).get(rangeLvl);
      initFromStack(arrowStack, weaponStack);
      
      if(owner instanceof ServerPlayer player){
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_ALCHEMICAL_ARBALEST_SHOOT)); // Add xp
      }
   }
   
   public void initFromStack(ItemStack arrowStack, ItemStack weaponStack){
      setPickupItemStack(arrowStack);
      
      Unit unit = pickupItemStack.remove(DataComponents.INTANGIBLE_PROJECTILE);
      if(unit != null){
         this.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
      }
      
      ArcanaItem.removeProperty(this.pickupItemStack, QuiverItem.QUIVER_ID_TAG);
      ArcanaItem.removeProperty(this.pickupItemStack, QuiverItem.QUIVER_SLOT_TAG);
      
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
   public EntityType<?> getPolymerEntityType(PacketContext context){
      return this.pickupItemStack.has(DataComponents.POTION_CONTENTS) ? EntityType.ARROW : EntityType.SPECTRAL_ARROW;
   }
   
   @Override
   public void tick(){
      super.tick();
   }
   
   @Override
   protected void onHitEntity(EntityHitResult entityHitResult){
      if(level() instanceof ServerLevel world){
         deployAura(world, position());
      }
      super.onHitEntity(entityHitResult);
      
      if(entityHitResult.getEntity().getType() == EntityType.PHANTOM && !entityHitResult.getEntity().isAlive()){
         if(getOwner() instanceof ServerPlayer player){
            ArcanaAchievements.progress(player, ArcanaAchievements.MANY_BIRDS_MANY_ARROWS, 1);
         }
      }
   }
   
   @Override
   protected void onHitBlock(BlockHitResult blockHitResult){
      if(level() instanceof ServerLevel world){
         deployAura(world, position());
      }
      super.onHitBlock(blockHitResult);
      
      if(pickup != Pickup.ALLOWED){
         this.discard();
      }
   }
   
   private void deployAura(ServerLevel serverWorld, Vec3 pos){
      List<Either<MobEffectInstance, ConditionInstance>> effects = new ArrayList<>();
      this.pickupItemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effect -> effects.add(Either.left(effect)));
      if(effects.isEmpty()){
         int duration = ArcanaNovum.CONFIG.getInt(ArcanaConfig.ALCHEMICAL_ARBALEST_VULNERABILITY_DURATION);
         float vulnStr = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.ALCHEMICAL_ARBALEST_VULNERABILITY_PER_LVL).get(lvl);
         ConditionInstance vulnerability = new ConditionInstance(Conditions.VULNERABILITY, arcanaId(ArcanaRegistry.ALCHEMICAL_ARBALEST.getId() + "_" + lvl), duration, vulnStr, true, true, true, AttributeModifier.Operation.ADD_VALUE, getOwner() != null ? getOwner().getUUID() : null);
         effects.add(Either.right(vulnerability));
         effects.add(Either.left(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, true, true)));
      }
      
      ArcanaRegistry.AREA_EFFECTS.getValue(ArcanaRegistry.ALCHEMICAL_ARROW_AREA_EFFECT_TRACKER.getId()).addSource(AlchemicalArrowAreaEffectTracker.source(getOwner(), BlockPos.containing(pos), serverWorld, range, lvl, effects));
   }
   
   
   @Override
   protected void addAdditionalSaveData(ValueOutput view){
      super.addAdditionalSaveData(view);
      view.putInt("ampLvl", lvl);
      view.putDouble("range", range);
   }
   
   @Override
   protected void readAdditionalSaveData(ValueInput view){
      super.readAdditionalSaveData(view);
      lvl = view.getIntOr("ampLvl", 0);
      range = view.getDoubleOr("range", 0.0);
   }
}
