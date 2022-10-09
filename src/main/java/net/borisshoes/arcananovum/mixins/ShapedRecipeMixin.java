package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
   
   @Inject(method="matches(Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/world/World;)Z", at= @At("HEAD"), cancellable = true)
   public void arcananovum_matches(CraftingInventory craftingInventory, World world, CallbackInfoReturnable<Boolean> cir){
      for(int i = 0; i < craftingInventory.size(); ++i){
         ItemStack item = craftingInventory.getStack(i);
         if(MagicItemUtils.isMagic(item)){
            cir.setReturnValue(false);
         }
      }
   }
   
}
