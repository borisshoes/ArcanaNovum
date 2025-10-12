package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {
   
   @Inject(method="addEnchantment",at=@At("RETURN"))
   private void arcananovum$enchantItemLore(RegistryEntry<Enchantment> enchantment, int level, CallbackInfo ci){
      ItemStack stack = (ItemStack) (Object) this;
      if(ArcanaItemUtils.isArcane(stack)){
         ArcanaItemUtils.identifyItem(stack).buildItemLore(stack, BorisLib.SERVER);
      }
   }
}
