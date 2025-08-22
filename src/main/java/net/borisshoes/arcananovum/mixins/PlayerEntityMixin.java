package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.callbacks.TickTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.ArcanaNovum.SERVER_TIMER_CALLBACKS;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
   
   @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttackKnockbackAgainst(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;)F"))
   private void arcananovum_postDamageEntity(Entity target, CallbackInfo ci, @Local(ordinal = 2) float atkPercentage){
      PlayerEntity player = (PlayerEntity)(Object) this;
      ItemStack handStack = player.getMainHandStack();
      if(ArcanaItemUtils.identifyItem(handStack) instanceof BinaryBlades blades && atkPercentage > 0.85){
         ArcanaItem.putProperty(handStack,BinaryBlades.LAST_HIT_TAG,12);
         blades.addEnergy(handStack,10);
         if(player instanceof ServerPlayerEntity serverPlayer) ArcanaNovum.addTickTimerCallback(serverPlayer.getWorld(), new GenericTimer(4, () -> {
            serverPlayer.getWorld().getChunkManager().sendToNearbyPlayers(serverPlayer, new EntityAnimationS2CPacket(serverPlayer, EntityAnimationS2CPacket.SWING_OFF_HAND));
         }));
      }
   }
   
   @ModifyReturnValue(method = "isClimbing", at = @At("RETURN"))
   private boolean arcananovum_greavesClimbing(boolean original){
      PlayerEntity player = (PlayerEntity)(Object) this;
      if(original) return true;
      ItemStack pants = player.getEquippedStack(EquipmentSlot.LEGS);
      if(!(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus greaves) || ArcanaAugments.getAugmentOnItem(pants,ArcanaAugments.EARTHEN_ASCENT) < 1) return original;
      if(player.horizontalCollision){
         return true;
      }
      return original;
   }
   
   @ModifyExpressionValue(method = "getBlockBreakingSpeed", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
   private float arcananovum_offGroundBlockBreakingSpeed(float constant){
      PlayerEntity player = (PlayerEntity) (Object) this; // This part of the augment currently works even if the player is not in water, not sure if I will leave it like this
      List<ItemStack> stacks = MiscUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
      int level = 0;
      for(ItemStack stack : stacks){
         boolean isActive = ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG);
         if(!isActive) continue;
         int lvl = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.MARINERS_GRACE);
         if(lvl > level) level = lvl;
      }
      return level == 0 ? constant : Math.min(constant, Math.max(1, constant - level*1.34f)); // Don't buff below 1, unless it is already below 1
   }
   
   @ModifyExpressionValue(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/EntityAttributeInstance;getValue()D"))
   private double arcananovum_underwaterBlockBreakingSpeed(double original){
      PlayerEntity player = (PlayerEntity) (Object) this;
      List<ItemStack> stacks = MiscUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
      int level = 0;
      for(ItemStack stack : stacks){
         boolean isActive = ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG);
         if(!isActive) continue;
         int lvl = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.MARINERS_GRACE);
         if(lvl > level) level = lvl;
      }
      return level == 0 ? original : Math.max(original, Math.min(1,original + level*0.35)); // Don't buff beyond 1, unless it is already above 1
   }
   
   // Remove all absorption callbacks when shield gets disabled
   @Inject(method = "takeShieldHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/BlocksAttacksComponent;applyShieldCooldown(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/item/ItemStack;)V"))
   private void arcananovum_disableFortitudeShield(ServerWorld world, LivingEntity attacker, CallbackInfo ci, @Local ItemStack shield){
      if(!(shield.getItem() instanceof ShieldOfFortitude.ShieldOfFortitudeItem)) return;
      PlayerEntity player = (PlayerEntity) (Object) this;
      ArrayList<ShieldTimerCallback> toRemove = new ArrayList<>();
      for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
         TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
         if(t instanceof ShieldTimerCallback st && st.getPlayer().getUuidAsString().equals(player.getUuidAsString())){
            toRemove.add(st);
         }
      }
      toRemove.forEach(ShieldTimerCallback::onTimer);
      SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains);
   }
   
   @Inject(method = "getProjectileType", at = @At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;getProjectiles()Ljava/util/function/Predicate;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum_quiverCheck(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      boolean runicBow = (ArcanaItemUtils.identifyItem(bow) instanceof RunicBow);
      boolean runicArbalest = bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1;
      if(!bow.isOf(Items.BOW) && !runicBow && !bow.isOf(Items.CROSSBOW) && !bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())) return;
      boolean runic = runicBow || runicArbalest;
      
      if(player instanceof ServerPlayerEntity serverPlayer){
         ItemStack arrowStack = QuiverItem.getArrowStack(serverPlayer,runic,false);
         Pair<String,Integer> option = QuiverItem.getArrowOption(serverPlayer,runic,false);
         if(arrowStack != null && option != null){
            ItemStack returnStack = arrowStack.copy();
            ArcanaItem.putProperty(returnStack, QuiverItem.QUIVER_SLOT_TAG, option.getRight());
            ArcanaItem.putProperty(returnStack, QuiverItem.QUIVER_ID_TAG, option.getLeft());
            cir.setReturnValue(returnStack);
         }else if(runicArbalest){
            Predicate<ItemStack> predicate = ((RangedWeaponItem)bow.getItem()).getProjectiles();
            for (int i = 0; i < player.getInventory().size(); ++i){
               ItemStack itemStack2 = player.getInventory().getStack(i);
               if(predicate.test(itemStack2) || ArcanaItemUtils.isRunicArrow(itemStack2)) cir.setReturnValue(itemStack2);
            }
         }
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectile(Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum_everlastingRocketCheck(ItemStack stack, CallbackInfoReturnable<ItemStack> cir, @Local Predicate<ItemStack> predicate){
      PlayerEntity player = (PlayerEntity) (Object) this;
      ItemStack main = player.getStackInHand(Hand.MAIN_HAND);
      ItemStack off = player.getStackInHand(Hand.OFF_HAND);
      if(ArcanaItemUtils.identifyItem(main) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(main);
         if(rocket.getEnergy(main) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }else if(ArcanaItemUtils.identifyItem(off) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(off);
         if(rocket.getEnergy(off) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value="RETURN"), cancellable = true)
   private void arcananovum_stopRunicUsage(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      if(!bow.isOf(Items.BOW) || bow.isOf(Items.CROSSBOW) || bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())) return;
      boolean runicArbalest = ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1;
      ItemStack curReturn = cir.getReturnValue();
      if(ArcanaItemUtils.isRunicArrow(curReturn) && !runicArbalest){
         cir.setReturnValue(player.isCreative() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY);
      }
   }
   
   @Inject(method = "addExperience", at = @At(value= "RETURN"))
   private void arcananovum_addExperience(CallbackInfo ci){
      PlayerEntity player = (PlayerEntity) (Object) this;
      if(player instanceof ServerPlayerEntity serverPlayer && player.experienceLevel >= 100){
         ArcanaNovum.data(serverPlayer).setResearchTask(ResearchTasks.LEVEL_100, true);
      }
   }
   
   @Inject(method = "useRiptide", at = @At(value= "HEAD"))
   private void arcananovum_useRiptide(int riptideTicks, float riptideAttackDamage, ItemStack stack, CallbackInfo ci){
      PlayerEntity player = (PlayerEntity) (Object) this;
      if(player instanceof ServerPlayerEntity serverPlayer && stack.isOf(Items.TRIDENT)){
         ArcanaNovum.data(serverPlayer).setResearchTask(ResearchTasks.RIPTIDE_TRIDENT, true);
      }
   }
}
