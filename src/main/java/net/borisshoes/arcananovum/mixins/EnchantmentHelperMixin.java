package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
   
   @Inject(method="set",at=@At("RETURN"))
   private static void arcananovum_enchantHelperSetItemLore(ItemStack stack, ItemEnchantmentsComponent enchantments, CallbackInfo ci){
      if(ArcanaItemUtils.isArcane(stack)){
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
   }
   
   @ModifyVariable(method = "set", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private static ItemEnchantmentsComponent arcananovum_enchantHelperSetHideTooltip(ItemEnchantmentsComponent enchantments, ItemStack stack){
      return ArcanaItemUtils.isArcane(stack) ? enchantments.withShowInTooltip(false) : enchantments;
   }
   
   @Inject(method="apply",at=@At("RETURN"), cancellable = true)
   private static void arcananovum_enchantHelperSetHideTooltipApplyReturn(ItemStack stack, Consumer<ItemEnchantmentsComponent.Builder> applier, CallbackInfoReturnable<ItemEnchantmentsComponent> cir){
      if(ArcanaItemUtils.isArcane(stack)){
         cir.setReturnValue(cir.getReturnValue().withShowInTooltip(false));
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
   }
   
   @Inject(method="apply",at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
   private static void arcananovum_enchantHelperSetHideTooltipApplySet(ItemStack stack, Consumer<ItemEnchantmentsComponent.Builder> applier, CallbackInfoReturnable<ItemEnchantmentsComponent> cir, @Local(ordinal=1) LocalRef<ItemEnchantmentsComponent> itemEnchantmentsComponent2){
      if(ArcanaItemUtils.isArcane(stack)){
         itemEnchantmentsComponent2.set(itemEnchantmentsComponent2.get().withShowInTooltip(false));
      }
   }
   
}
