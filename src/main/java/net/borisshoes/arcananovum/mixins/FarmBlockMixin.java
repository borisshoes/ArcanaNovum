package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class FarmBlockMixin {
   
   @Inject(method = "turnToDirt", at = @At(value = "HEAD"), cancellable = true)
   private static void arcananovum$wildGrowthStopTrample(Entity entity, BlockState state, Level world, BlockPos pos, CallbackInfo ci){
      if(entity instanceof ServerPlayer player && ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.WILD_GROWTH_CHARM.getItem())){
         ci.cancel();
      }
   }
}
