package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
   
   @ModifyExpressionValue(method="spawn",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
   private boolean arcananovum$fuckPhantoms(boolean original, @Local ServerPlayerEntity player){
      if(original) return true;
      return !ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.PANTHERA, 1).isEmpty();
   }
}
