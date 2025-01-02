package net.borisshoes.arcananovum.mixins;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PolymerBlockUtils.class)
public class PolymerBlockUtilsMixin {
   
   @Inject(method = "shouldMineServerSide", at = @At("RETURN"), cancellable = true)
   private static void arcananovum_overrideServerMining(ServerPlayerEntity player, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
      if(!cir.getReturnValueZ()){
         List<ItemStack> stacks = MiscUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
         for(ItemStack stack : stacks){
            boolean isActive = ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG);
            if(!isActive) continue;
            cir.setReturnValue(true);
            return;
         }
      }
   }
}
