package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(ServerWorld.class)
public class ServerWorldMixin {
   @Inject(method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("HEAD"), cancellable = true)
   private void shouldTick(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(ContinuumAnchor.isChunkLoaded(serverWorld, chunkPos)) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
   
   @Inject(method = "shouldTickEntity(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
   private void shouldTickEntity(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(ContinuumAnchor.isChunkLoaded(serverWorld, serverWorld.getChunk(pos).getPos())) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
   
   @Inject(method = "shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
   private void shouldTick(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(ContinuumAnchor.isChunkLoaded(serverWorld, serverWorld.getChunk(pos).getPos())) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
   
   @Inject(method = "shouldTickBlocksInChunk", at = @At("HEAD"), cancellable = true)
   private void shouldTickBlocksInChunk(long chunkPos, CallbackInfoReturnable<Boolean> cir) {
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(ContinuumAnchor.isChunkLoaded(serverWorld,new ChunkPos(chunkPos))) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
   
   @Redirect(method = "method_31420(Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;shouldTickEntities(J)Z"))
   private boolean injectedChunkManagerCall(ChunkTicketManager chunkTicketManager, long chunkPos) {
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(ContinuumAnchor.isChunkLoaded(serverWorld,new ChunkPos(chunkPos))) return true;
      return chunkTicketManager.shouldTickEntities(chunkPos);
   }
}
