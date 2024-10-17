package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
   @Inject(method="isAcceptableItem",at=@At("HEAD"), cancellable = true)
   private void arcananovum_makeUnenchantable1(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      if(stack.isOf(ArcanaRegistry.LEVITATION_HARNESS.getItem())){
         Enchantment enchant = (Enchantment) (Object) this;
         cir.setReturnValue(false);
      }
   }
   
   @Inject(method="isSupportedItem",at=@At("HEAD"), cancellable = true)
   private void arcananovum_makeUnenchantable2(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      if(stack.isOf(ArcanaRegistry.LEVITATION_HARNESS.getItem())){
         Enchantment enchant = (Enchantment) (Object) this;
         cir.setReturnValue(false);
      }
   }
   
   @Inject(method="applyLocationBasedEffects", at= @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/effect/EnchantmentLocationBasedEffect;apply(Lnet/minecraft/server/world/ServerWorld;ILnet/minecraft/enchantment/EnchantmentEffectContext;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Z)V"))
   private void arcananovum_applyLocationEffects(ServerWorld world, int level, EnchantmentEffectContext context, LivingEntity user, CallbackInfo ci){
      if(user instanceof ServerPlayerEntity player){
         Enchantment enchant = (Enchantment) (Object) this;
         if(enchant.toString().equals(MiscUtils.getEnchantment(Enchantments.SOUL_SPEED).value().toString())){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.USE_SOUL_SPEED, true);
         }
      }
   }
   
   @Inject(method="modifyDamageProtection", at= @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/mutable/MutableFloat;setValue(F)V"))
   private void arcananovum_modifyDamageProtection(ServerWorld world, int level, ItemStack stack, Entity user, DamageSource damageSource, MutableFloat damageProtection, CallbackInfo ci){
      if(user instanceof ServerPlayerEntity player){
         Enchantment enchant = (Enchantment) (Object) this;
         if(enchant.toString().equals(MiscUtils.getEnchantment(Enchantments.FEATHER_FALLING).value().toString())){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FEATHER_FALL, true);
         }
      }
   }
}