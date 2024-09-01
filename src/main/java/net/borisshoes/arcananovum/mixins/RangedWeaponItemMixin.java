package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.entities.ArbalestArrowEntity;
import net.borisshoes.arcananovum.items.EverlastingRocket;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
   
   @Inject(method="getProjectile",at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private static void arcananovum_decreaseQuiver(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir, int i, ItemStack itemStack){
      if(shooter instanceof ServerPlayerEntity playerEntity){
         QuiverItem.decreaseQuiver(stack,itemStack,playerEntity);
         EverlastingRocket.decreaseRocket(itemStack,playerEntity);
      }
   }
   
   @Inject(method = "getProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I",shift = At.Shift.BEFORE))
   private static void arcananovum_modifyProjectiles(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 0) LocalIntRef i){
      if(i.get() != 0 && (ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ENHANCED_INFINITY.id) >= 1 && (projectileStack.isOf(Items.SPECTRAL_ARROW) || projectileStack.isOf(Items.TIPPED_ARROW)))){
         i.set(0);
      }else if(i.get() < 5 && ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SCATTERSHOT.id) > 0){
         i.set(5);
      }
   }
   
   @ModifyExpressionValue(method = "createArrowEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"))
   private PersistentProjectileEntity arcananovum_createAlchemicalArrow(PersistentProjectileEntity regularArrow, World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical){
      if(weaponStack.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && (projectileStack.isOf(Items.TIPPED_ARROW) || projectileStack.isOf(Items.SPECTRAL_ARROW))){
         int spectralLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(weaponStack,ArcanaAugments.SPECTRAL_AMPLIFICATION.id));
         int prolificLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(weaponStack,ArcanaAugments.PROLIFIC_POTIONS.id));
         return new ArbalestArrowEntity(world, shooter, spectralLvl,prolificLvl, projectileStack, weaponStack);
      }
      return regularArrow;
   }
}
