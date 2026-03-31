package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
   
   @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
   private void arcananovum$makeUnenchantable1(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      Enchantment enchant = (Enchantment) (Object) this;
      boolean isFateAnchor = enchant.description().toString().contains(Util.makeDescriptionId("enchantment", ArcanaRegistry.FATE_ANCHOR.identifier()));
      if(isFateAnchor && !stack.is(ArcanaRegistry.FATE_ANCHOR_UNENCHANTABLE)){
         cir.setReturnValue(true);
      }else if(stack.is(ArcanaRegistry.LEVITATION_HARNESS.getItem())){
         cir.setReturnValue(false);
      }
   }
   
   @Inject(method = "isSupportedItem", at = @At("HEAD"), cancellable = true)
   private void arcananovum$makeUnenchantable2(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      Enchantment enchant = (Enchantment) (Object) this;
      boolean isFateAnchor = enchant.description().toString().contains(Util.makeDescriptionId("enchantment", ArcanaRegistry.FATE_ANCHOR.identifier()));
      if(isFateAnchor && !stack.is(ArcanaRegistry.FATE_ANCHOR_UNENCHANTABLE)){
         cir.setReturnValue(true);
      }else if(stack.is(ArcanaRegistry.LEVITATION_HARNESS.getItem())){
         cir.setReturnValue(false);
      }
   }
   
   @Inject(method = "runLocationChangedEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/effects/EnchantmentLocationBasedEffect;onChangedBlock(Lnet/minecraft/server/level/ServerLevel;ILnet/minecraft/world/item/enchantment/EnchantedItemInUse;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Z)V"))
   private void arcananovum$applyLocationEffects(ServerLevel world, int level, EnchantedItemInUse context, LivingEntity user, CallbackInfo ci){
      if(user instanceof ServerPlayer player){
         Enchantment enchant = (Enchantment) (Object) this;
         if(enchant.toString().equals(MinecraftUtils.getEnchantment(Enchantments.SOUL_SPEED).value().toString())){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.USE_SOUL_SPEED, true);
         }
      }
   }
   
   @Inject(method = "modifyDamageProtection", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableFloat;setValue(F)V"))
   private void arcananovum$modifyDamageProtection(ServerLevel world, int level, ItemStack stack, Entity user, DamageSource damageSource, MutableFloat damageProtection, CallbackInfo ci){
      if(user instanceof ServerPlayer player){
         Enchantment enchant = (Enchantment) (Object) this;
         if(enchant.toString().equals(MinecraftUtils.getEnchantment(Enchantments.FEATHER_FALLING).value().toString())){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FEATHER_FALL, true);
         }
      }
   }
}