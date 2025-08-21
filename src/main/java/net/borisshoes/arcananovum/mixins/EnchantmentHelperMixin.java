package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
   
   @Inject(method = "set", at = @At("HEAD"))
   private static void arcananovum_enchantHelperSetHideTooltip(ItemStack stack, ItemEnchantmentsComponent enchantments, CallbackInfo ci){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplayComponent display = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY,TooltipDisplayComponent.DEFAULT);
         if(display.shouldDisplay(DataComponentTypes.ENCHANTMENTS)){
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY,display.with(DataComponentTypes.ENCHANTMENTS,true));
         }
      }
   }
   
   @Inject(method="apply",at=@At("RETURN"))
   private static void arcananovum_enchantHelperSetHideTooltipApplyReturn(ItemStack stack, Consumer<ItemEnchantmentsComponent.Builder> applier, CallbackInfoReturnable<ItemEnchantmentsComponent> cir){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplayComponent display = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY,TooltipDisplayComponent.DEFAULT);
         if(display.shouldDisplay(DataComponentTypes.ENCHANTMENTS)){
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY,display.with(DataComponentTypes.ENCHANTMENTS,true));
         }
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
   }
   
   @Inject(method="apply",at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
   private static void arcananovum_enchantHelperSetHideTooltipApplySet(ItemStack stack, Consumer<ItemEnchantmentsComponent.Builder> applier, CallbackInfoReturnable<ItemEnchantmentsComponent> cir, @Local(ordinal=1) LocalRef<ItemEnchantmentsComponent> itemEnchantmentsComponent2){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplayComponent display = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY,TooltipDisplayComponent.DEFAULT);
         if(display.shouldDisplay(DataComponentTypes.ENCHANTMENTS)){
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY,display.with(DataComponentTypes.ENCHANTMENTS,true));
         }
      }
   }
   
   @Inject(method = "getProjectileCount",at=@At("RETURN"), cancellable = true)
   private static void arcananovum_modifyProjectileCount(ServerWorld world, ItemStack stack, Entity user, int baseProjectileCount, CallbackInfoReturnable<Integer> cir){
      if(ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SCATTERSHOT.id) > 0 && cir.getReturnValueI() < 5){
         cir.setReturnValue(5);
      }
   }
}
