package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
   
   @ModifyExpressionValue(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/SmithingRecipeInput;base()Lnet/minecraft/world/item/ItemStack;"))
   private ItemStack arcananovum$modifyBaseItem(ItemStack original){
      ItemStack stack = original;
      if(EnhancedStatUtils.isEnhanced(original)){
         stack = original.copy();
         EnhancedStatUtils.stripEnhancements(stack,false);
      }
      return stack;
   }
   
   @ModifyReturnValue(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
   private ItemStack arcananovum$modifyReturnItem(ItemStack original){
      if(EnhancedStatUtils.isEnhanced(original)){
         EnhancedStatUtils.enhanceItem(original,EnhancedStatUtils.getPercentile(original));
      }
      return original;
   }
}
