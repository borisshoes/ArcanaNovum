package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
   
   @Inject(method="set",at=@At("RETURN"))
   private static void arcananovum_enchantHelperSetItemLore(Map<Enchantment, Integer> enchantments, ItemStack stack, CallbackInfo ci){
      if(MagicItemUtils.isMagic(stack)){
         MagicItemUtils.identifyItem(stack).buildItemLore(stack, ArcanaNovum.SERVER);
      }
   }
}
