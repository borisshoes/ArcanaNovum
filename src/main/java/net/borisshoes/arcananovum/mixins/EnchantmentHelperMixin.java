package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
   
   @Inject(method = "setEnchantments", at = @At("RETURN"))
   private static void arcananovum$enchantHelperSetItemLore(ItemStack stack, ItemEnchantments enchantments, CallbackInfo ci){
      if(ArcanaItemUtils.isArcane(stack)){
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, BorisLib.SERVER);
      }
   }
   
   @Inject(method = "setEnchantments", at = @At("HEAD"))
   private static void arcananovum$enchantHelperSetHideTooltip(ItemStack stack, ItemEnchantments enchantments, CallbackInfo ci){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
         if(display.shows(DataComponents.ENCHANTMENTS)){
            stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(DataComponents.ENCHANTMENTS, true));
         }
      }
   }
   
   @Inject(method = "updateEnchantments", at = @At("RETURN"))
   private static void arcananovum$enchantHelperSetHideTooltipApplyReturn(ItemStack stack, Consumer<ItemEnchantments.Mutable> applier, CallbackInfoReturnable<ItemEnchantments> cir){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
         if(display.shows(DataComponents.ENCHANTMENTS)){
            stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(DataComponents.ENCHANTMENTS, true));
         }
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, BorisLib.SERVER);
      }
   }
   
   @Inject(method = "updateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE))
   private static void arcananovum$enchantHelperSetHideTooltipApplySet(ItemStack stack, Consumer<ItemEnchantments.Mutable> applier, CallbackInfoReturnable<ItemEnchantments> cir, @Local(ordinal = 1) LocalRef<ItemEnchantments> itemEnchantmentsComponent2){
      if(ArcanaItemUtils.isArcane(stack)){
         TooltipDisplay display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
         if(display.shows(DataComponents.ENCHANTMENTS)){
            stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(DataComponents.ENCHANTMENTS, true));
         }
      }
   }
   
   @Inject(method = "processProjectileCount", at = @At("RETURN"), cancellable = true)
   private static void arcananovum$modifyProjectileCount(ServerLevel world, ItemStack stack, Entity user, int baseProjectileCount, CallbackInfoReturnable<Integer> cir){
      if(ArcanaAugments.getAugmentOnItem(stack, ArcanaAugments.SCATTERSHOT) > 0 && cir.getReturnValueI() < 5){
         cir.setReturnValue(5);
      }
   }
   
   @Inject(method = "getRandomItemWith", at = @At("RETURN"))
   private static void arcananovum$useMendingFallback(DataComponentType<?> dataComponentType, LivingEntity livingEntity, Predicate<ItemStack> predicate, CallbackInfoReturnable<Optional<EnchantedItemInUse>> cir){
      if(dataComponentType != EnchantmentEffectComponents.REPAIR_WITH_XP || cir.getReturnValue().isEmpty() || !(livingEntity instanceof ServerPlayer player))
         return;
      ArcanaNovum.data(player).setResearchTask(ResearchTasks.ACTIVATE_MENDING, true);
   }
}
