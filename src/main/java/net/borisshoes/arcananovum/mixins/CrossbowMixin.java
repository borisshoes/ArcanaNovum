package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.EverlastingRocket;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrossbowItem.class)
public class CrossbowMixin {

   @Inject(method="loadProjectile",at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private static void arcananovum_decreaseQuiver(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative, CallbackInfoReturnable<Boolean> cir, boolean bl){
      if(shooter instanceof ServerPlayerEntity player){
         QuiverItem.decreaseQuiver(crossbow,projectile,player);
         EverlastingRocket.decreaseRocket(projectile,player);
      }
   }
}
