package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.entities.ArbalestArrowEntity;
import net.borisshoes.arcananovum.items.EverlastingRocket;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
   
   @Inject(method = "useAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", shift = At.Shift.BEFORE))
   private static void arcananovum$decreaseQuiver(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 2) ItemStack itemStack){
      if(shooter instanceof ServerPlayer playerEntity){
         QuiverItem.decreaseQuiver(stack,itemStack,playerEntity);
         EverlastingRocket.decreaseRocket(itemStack,playerEntity);
      }
   }
   
   @Inject(method = "useAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I",shift = At.Shift.BEFORE))
   private static void arcananovum$modifyProjectiles(ItemStack stack, ItemStack projectileStack, LivingEntity shooter, boolean multishot, CallbackInfoReturnable<ItemStack> cir, @Local(ordinal = 0) LocalIntRef i){
      if(i.get() != 0 &&
            EnchantmentHelper.getItemEnchantmentLevel(MinecraftUtils.getEnchantment(Enchantments.INFINITY),stack) >= 1 &&
            ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.ENHANCED_INFINITY) >= 1 &&
            (projectileStack.is(Items.SPECTRAL_ARROW) || projectileStack.is(Items.TIPPED_ARROW))){
         i.set(0);
      }
   }
   
   @ModifyExpressionValue(method = "createProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArrowItem;createArrow(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;"))
   private AbstractArrow arcananovum$createAlchemicalArrow(AbstractArrow regularArrow, Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical){
      if(weaponStack.is(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && (projectileStack.is(Items.TIPPED_ARROW) || projectileStack.is(Items.SPECTRAL_ARROW))){
         int spectralLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(weaponStack,ArcanaAugments.SPECTRAL_AMPLIFICATION));
         int prolificLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(weaponStack,ArcanaAugments.PROLIFIC_POTIONS));
         return new ArbalestArrowEntity(world, shooter, spectralLvl,prolificLvl, projectileStack, weaponStack);
      }
      return regularArrow;
   }
}
