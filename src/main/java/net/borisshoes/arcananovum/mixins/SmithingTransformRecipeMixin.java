package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {
   
   @ModifyExpressionValue(method = "craft(Lnet/minecraft/recipe/input/SmithingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/input/SmithingRecipeInput;base()Lnet/minecraft/item/ItemStack;"))
   private ItemStack arcananovum_modifyBaseItem(ItemStack original){
      ItemStack stack = original;
      if(EnhancedStatUtils.isEnhanced(original)){
         stack = original.copy();
         EnhancedStatUtils.stripEnhancements(stack,false);
      }
      return stack;
   }
   
   @ModifyReturnValue(method = "craft(Lnet/minecraft/recipe/input/SmithingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
   private ItemStack arcananovum_modifyReturnItem(ItemStack original){
      if(EnhancedStatUtils.isEnhanced(original)){
         EnhancedStatUtils.enhanceItem(original,EnhancedStatUtils.getPercentile(original));
      }
      return original;
   }
}
