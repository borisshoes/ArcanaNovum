package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {
   
   @ModifyExpressionValue(method= "tick",at=@At(value="INVOKE",target= "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"))
   private boolean arcananovum$fuckPhantoms(boolean original, @Local ServerPlayer player){
      if(original) return true;
      if(!ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.PANTHERA, 1).isEmpty()) return true;
      if(GeomanticSteleBlockEntity.getZoneAtEntity(player,(item) -> item.is(ArcanaRegistry.FELIDAE_CHARM.getItem()) && ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PANTHERA) > 0) != null) return true;
      return false;
   }
}
