package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PolymerItemUtils.class)
public class PolymerItemUtilsMixin {
   
   @ModifyReturnValue(method = "createItemStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/tooltip/TooltipType;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
   private static ItemStack arcananovum$modifyPlayerHeads(ItemStack returned, ItemStack itemStack, TooltipType tooltipContext, PacketContext context){
      if(itemStack.isOf(Items.PLAYER_HEAD) && returned.isOf(Items.PLAYER_HEAD) && !returned.contains(DataComponentTypes.PROFILE) && itemStack.contains(DataComponentTypes.PROFILE)){
         returned.set(DataComponentTypes.PROFILE,itemStack.get(DataComponentTypes.PROFILE));
      }
      return returned;
   }
}
