package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public class MerchantResultSlotMixin {
   
   @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V", shift = At.Shift.AFTER))
   private void arcananovum$extortionCharm(Player player, ItemStack carried, CallbackInfo ci, @Local(name = "offer") MerchantOffer offer){
      if(!ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.NEGOTIATION_CHARM, ArcanaAugments.EXTORTION, 1).isEmpty()){
         offer.resetUses();
      }
   }
}
