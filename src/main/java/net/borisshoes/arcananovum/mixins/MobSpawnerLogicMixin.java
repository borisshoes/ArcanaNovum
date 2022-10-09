package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {
   @Inject(method = "isPlayerInRange", at = @At(value = "HEAD"), cancellable = true)
   private void isPlayerInRange(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
   
      if(world instanceof ServerWorld serverWorld){
         Chunk chunk = world.getChunk(pos);
         if(ContinuumAnchor.isChunkLoaded(serverWorld,chunk.getPos())){
            cir.setReturnValue(true);
            cir.cancel();
         }
      }
   }
}
