package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class FamlandBlockMixin {
   
   @Inject(method = "setToDirt",at=@At(value = "HEAD"),cancellable = true)
   private static void arcananovum_wildGrowthStopTrample(Entity entity, BlockState state, World world, BlockPos pos, CallbackInfo ci){
      if(entity instanceof ServerPlayerEntity player && ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.WILD_GROWTH_CHARM.getItem())){
         ci.cancel();
      }
   }
}
