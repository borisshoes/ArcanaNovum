package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RepairItemRecipe.class)
public class RepairItemRecipeMixin {
   
   @ModifyReturnValue(method = "canCombine", at = @At("RETURN"))
   private static boolean arcananovum$binaryBladesCombine(boolean original, ItemStack itemStack, ItemStack itemStack2){
      if(!original) return false;
      if(!itemStack.is(ArcanaRegistry.BINARY_BLADES.getItem()) && !itemStack2.is(ArcanaRegistry.BINARY_BLADES.getItem())) return true;
      if(ArcanaItem.getBooleanProperty(itemStack, BinaryBlades.FAKE_TAG) || ArcanaItem.getBooleanProperty(itemStack2, BinaryBlades.FAKE_TAG)) return false;
      return true;
   }
}
