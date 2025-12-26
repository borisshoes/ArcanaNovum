package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(PolymerItemUtils.class)
public class PolymerItemUtilsMixin {
   
   @ModifyReturnValue(method = "createItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/TooltipFlag;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
   private static ItemStack arcananovum$modifyPlayerHeads(ItemStack returned, ItemStack itemStack, TooltipFlag tooltipContext, PacketContext context){
      if(itemStack.is(Items.PLAYER_HEAD) && returned.is(Items.PLAYER_HEAD) && !returned.has(DataComponents.PROFILE) && itemStack.has(DataComponents.PROFILE)){
         //returned.set(DataComponentTypes.PROFILE,itemStack.get(DataComponentTypes.PROFILE));
      }
      return returned;
   }
}
