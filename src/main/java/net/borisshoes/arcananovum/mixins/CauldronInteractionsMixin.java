package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.recipes.ArcanaCauldronInteractions;
import net.minecraft.core.cauldron.CauldronInteractions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CauldronInteractions.class)
public class CauldronInteractionsMixin {
   
   @Inject(method = "bootStrap", at = @At("TAIL"))
   private static void arcananovum$cauldronInteractions(CallbackInfo ci){
      ArcanaCauldronInteractions.registerArcanaCauldronInteractions(
            (CauldronInteractionDispatcherAccessor) CauldronInteractions.WATER,
            (CauldronInteractionDispatcherAccessor) CauldronInteractions.LAVA,
            (CauldronInteractionDispatcherAccessor) CauldronInteractions.EMPTY,
            (CauldronInteractionDispatcherAccessor) CauldronInteractions.POWDER_SNOW);
   }
}
