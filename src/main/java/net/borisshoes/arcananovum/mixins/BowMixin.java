package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public class BowMixin {
   
   @Inject(method="onStoppedUsing",at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_decreaseQuiver(ItemStack bow, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack arrow, int i, float f, boolean bl2){
      QuiverItem.decreaseQuiver(bow,arrow,playerEntity);
   }
}
