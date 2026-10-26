package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class FarmBlockMixin {
   
   // Only the trample is cancelled, fallOn still has to run so the player takes fall damage
   @Inject(method = "turnToBaseBlock", at = @At(value = "HEAD"), cancellable = true)
   private void arcananovum$wildGrowthStopTrample(@Nullable Entity sourceEntity, BlockState state, Level level, BlockPos pos, CallbackInfo ci){
      if(sourceEntity instanceof ServerPlayer player && ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.WILD_GROWTH_CHARM.getItem())){
         ci.cancel();
      }
   }
}
