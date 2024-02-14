package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.ThornsEnchantment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
   @Inject(method="isAcceptableItem",at=@At("HEAD"), cancellable = true)
   private void arcananovum_makeUnenchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir){
      if(stack.isOf(ArcanaRegistry.LEVITATION_HARNESS.getItem())){
         Enchantment enchant = (Enchantment) (Object) this;
         cir.setReturnValue(false);
      }
   }
}
