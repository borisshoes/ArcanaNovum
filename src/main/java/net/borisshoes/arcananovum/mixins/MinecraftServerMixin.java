package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
   @Inject(at = @At("TAIL"), method = "prepareStartRegion")
   private void prepareStartRegion(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo info){
      ContinuumAnchor.initLoadedChunks((MinecraftServer) (Object) this);
   }
}
