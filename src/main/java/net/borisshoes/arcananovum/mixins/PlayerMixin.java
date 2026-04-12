package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.borisshoes.borislib.BorisLib.SERVER_TIMER_CALLBACKS;

@Mixin(Player.class)
public class PlayerMixin {
   
   @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
   private void arcananovum$postDamageEntity(Entity target, CallbackInfo ci, @Local(ordinal = 1) float atkPercentage){
      Player player = (Player) (Object) this;
      ItemStack handStack = player.getMainHandItem();
      if(ArcanaItemUtils.identifyItem(handStack) instanceof BinaryBlades blades){
         if(atkPercentage > 0.5){
            int delay = ArcanaNovum.CONFIG.getInt(ArcanaConfig.BINARY_BLADES_ENERGY_GRACE_PERIOD);
            ArcanaItem.putProperty(handStack, BinaryBlades.LAST_HIT_TAG, delay);
         }
         if(atkPercentage > 0.85){
            int perHit = ArcanaNovum.CONFIG.getInt(ArcanaConfig.BINARY_BLADES_ENERGY_PER_HIT);
            blades.addEnergy(handStack, perHit);
            if(player instanceof ServerPlayer serverPlayer)
               BorisLib.addTickTimerCallback(serverPlayer.level(), new GenericTimer(4, () -> {
                  serverPlayer.level().getChunkSource().sendToTrackingPlayersAndSelf(serverPlayer, new ClientboundAnimatePacket(serverPlayer, ClientboundAnimatePacket.SWING_OFF_HAND));
               }));
         }
      }
      if(ArcanaItemUtils.identifyItem(handStack) instanceof ShadowStalkersGlaive glaive && player instanceof ServerPlayer serverPlayer){
         if(atkPercentage > 0.8){
            int toAdd = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SHADOW_STALKERS_GLAIVE_HIT_ENERGY);
            int oldEnergy = EnergyItem.getEnergy(handStack);
            glaive.addEnergy(handStack, toAdd);
            int newEnergy = EnergyItem.getEnergy(handStack);
            glaive.sendEnergyMessage(serverPlayer, oldEnergy, newEnergy, false);
         }
      }
   }
   
   @ModifyReturnValue(method = "onClimbable", at = @At("RETURN"))
   private boolean arcananovum$greavesClimbing(boolean original){
      Player player = (Player) (Object) this;
      if(original) return true;
      ItemStack pants = player.getItemBySlot(EquipmentSlot.LEGS);
      if(!(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus greaves) || ArcanaAugments.getAugmentOnItem(pants, ArcanaAugments.EARTHEN_ASCENT) < 1)
         return original;
      if(player.horizontalCollision){
         return true;
      }
      return original;
   }
   
   @ModifyExpressionValue(method = "getDestroySpeed", at = @At(value = "CONSTANT", args = "floatValue=5.0"))
   private float arcananovum$offGroundBlockBreakingSpeed(float constant){
      Player player = (Player) (Object) this; // This part of the augment currently works even if the player is not in water, not sure if I will leave it like this
      List<ItemStack> stacks = ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
      int level = 0;
      for(ItemStack stack : stacks){
         boolean isActive = ArcanaItem.getBooleanProperty(stack, ArcanaItem.ACTIVE_TAG);
         if(!isActive) continue;
         int lvl = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.MARINERS_GRACE);
         if(lvl > level) level = lvl;
      }
      float marinerBuff = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.CETACEA_CHARM_SWIM_PENALTY_PER_LVL).get(level);
      return level == 0 ? constant : Math.min(constant, Math.max(1, constant - marinerBuff)); // Don't buff below 1, unless it is already below 1
   }
   
   @ModifyExpressionValue(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;getValue()D"))
   private double arcananovum$underwaterBlockBreakingSpeed(double original){
      Player player = (Player) (Object) this;
      int level = 0;
      if(player instanceof ServerPlayer serverPlayer){
         for(int i = ArcanaAugments.MARINERS_GRACE.getTiers().length - 1; i >= 0; i--){
            int finalI = i;
            if(GeomanticSteleBlockEntity.isEntityInZone(serverPlayer, (item) -> item.is(ArcanaRegistry.CETACEA_CHARM.getItem()) && ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.MARINERS_GRACE) > finalI)){
               level = i + 1;
               break;
            }
         }
      }
      List<ItemStack> stacks = ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
      for(ItemStack stack : stacks){
         boolean isActive = ArcanaItem.getBooleanProperty(stack, ArcanaItem.ACTIVE_TAG);
         if(!isActive) continue;
         int lvl = ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.MARINERS_GRACE);
         if(lvl > level) level = lvl;
      }
      double marinerBuff = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.CETACEA_CHARM_SWIM_PENALTY_PER_LVL).get(level);
      return level == 0 ? original : Math.max(original, Math.min(1, original + marinerBuff)); // Don't buff beyond 1, unless it is already above 1
   }
   
   // Remove all absorption callbacks when shield gets disabled
   @Inject(method = "blockUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/BlocksAttacks;disable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/item/ItemStack;)V"))
   private void arcananovum$disableFortitudeShield(ServerLevel world, LivingEntity attacker, CallbackInfo ci, @Local ItemStack shield){
      if(!(shield.getItem() instanceof ShieldOfFortitude.ShieldOfFortitudeItem)) return;
      Player player = (Player) (Object) this;
      ArrayList<ShieldTimerCallback> toRemove = new ArrayList<>();
      for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
         TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
         if(t instanceof ShieldTimerCallback st && st.getPlayer().getStringUUID().equals(player.getStringUUID())){
            toRemove.add(st);
         }
      }
      toRemove.forEach(ShieldTimerCallback::onTimer);
      SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains);
   }
   
   @Inject(method = "getProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles()Ljava/util/function/Predicate;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum$quiverCheck(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      Player player = (Player) (Object) this;
      boolean runicBow = (ArcanaItemUtils.identifyItem(bow) instanceof RunicBow);
      boolean runicArbalest = bow.is(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && ArcanaAugments.getAugmentOnItem(bow, ArcanaAugments.RUNIC_ARBALEST) >= 1;
      if(!bow.is(Items.BOW) && !runicBow && !bow.is(Items.CROSSBOW) && !bow.is(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()))
         return;
      boolean runic = runicBow || runicArbalest;
      
      if(player instanceof ServerPlayer serverPlayer){
         ItemStack arrowStack = QuiverItem.getArrowStack(serverPlayer, runic, false);
         Tuple<String, Integer> option = QuiverItem.getArrowOption(serverPlayer, runic, false);
         if(arrowStack != null && option != null){
            ItemStack returnStack = arrowStack.copy();
            ArcanaItem.putProperty(returnStack, QuiverItem.QUIVER_SLOT_TAG, option.getB());
            ArcanaItem.putProperty(returnStack, QuiverItem.QUIVER_ID_TAG, option.getA());
            cir.setReturnValue(returnStack);
         }else if(runicArbalest){
            Predicate<ItemStack> predicate = ((ProjectileWeaponItem) bow.getItem()).getAllSupportedProjectiles();
            for(int i = 0; i < player.getInventory().getContainerSize(); ++i){
               ItemStack itemStack2 = player.getInventory().getItem(i);
               if(predicate.test(itemStack2) || ArcanaItemUtils.isRunicArrow(itemStack2))
                  cir.setReturnValue(itemStack2);
            }
         }
      }
   }
   
   @Inject(method = "getProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getHeldProjectile(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum$everlastingRocketCheck(ItemStack stack, CallbackInfoReturnable<ItemStack> cir, @Local Predicate<ItemStack> predicate){
      Player player = (Player) (Object) this;
      ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
      ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
      if(ArcanaItemUtils.identifyItem(main) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(main);
         if(EnergyItem.getEnergy(main) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }else if(ArcanaItemUtils.identifyItem(off) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(off);
         if(EnergyItem.getEnergy(off) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }
   }
   
   @Inject(method = "getProjectile", at = @At(value = "RETURN"), cancellable = true)
   private void arcananovum$stopRunicUsage(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      Player player = (Player) (Object) this;
      if(!bow.is(Items.BOW) || bow.is(Items.CROSSBOW) || bow.is(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())) return;
      boolean runicArbalest = ArcanaAugments.getAugmentOnItem(bow, ArcanaAugments.RUNIC_ARBALEST) >= 1;
      ItemStack curReturn = cir.getReturnValue();
      if(ArcanaItemUtils.isRunicArrow(curReturn) && !runicArbalest){
         cir.setReturnValue(player.isCreative() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY);
      }
   }
   
   @Inject(method = "giveExperiencePoints", at = @At(value = "RETURN"))
   private void arcananovum$addExperience(CallbackInfo ci){
      Player player = (Player) (Object) this;
      if(player instanceof ServerPlayer serverPlayer && player.experienceLevel >= 100){
         ArcanaNovum.data(serverPlayer).setResearchTask(ResearchTasks.LEVEL_100, true);
      }
   }
   
   @Inject(method = "startAutoSpinAttack", at = @At(value = "HEAD"))
   private void arcananovum$useRiptide(int riptideTicks, float riptideAttackDamage, ItemStack stack, CallbackInfo ci){
      Player player = (Player) (Object) this;
      if(player instanceof ServerPlayer serverPlayer && stack.is(Items.TRIDENT)){
         ArcanaNovum.data(serverPlayer).setResearchTask(ResearchTasks.RIPTIDE_TRIDENT, true);
      }
   }
}
