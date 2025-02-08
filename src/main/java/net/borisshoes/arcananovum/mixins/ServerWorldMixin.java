package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(ServerWorld.class)
public class ServerWorldMixin {
   
   @ModifyReturnValue(method = "isChunkLoaded", at = @At("RETURN"))
   private boolean arcananovum_isChunkLoaded(boolean original, long chunkPos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld, new ChunkPos(chunkPos))){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z", at = @At("RETURN"))
   private boolean arcananovum_shouldTick(boolean original, ChunkPos chunkPos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld, chunkPos)){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "shouldTickEntity(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("RETURN"))
   private boolean arcananovum_shouldTickEntity(boolean original, BlockPos pos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld, serverWorld.getChunk(pos).getPos())){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("RETURN"))
   private boolean arcananovum_shouldTick(boolean original, BlockPos pos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld, serverWorld.getChunk(pos).getPos())){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "shouldTickBlocksInChunk", at = @At("RETURN"))
   private boolean arcananovum_shouldTickBlocksInChunk(boolean original, long chunkPos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld,new ChunkPos(chunkPos))){
         return true;
      }
      return original;
   }
   
   @ModifyExpressionValue(method = "shouldTickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager;shouldTickEntities(J)Z"))
   private boolean arcananovum_injectedChunkManagerCall(boolean original, BlockPos pos){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!original && ContinuumAnchor.isChunkLoaded(serverWorld,new ChunkPos(pos))) return true;
      return original;
   }
   
   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", shift = At.Shift.BEFORE))
   private void arcananovum_resetWorldIdle(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
      ServerWorld serverWorld = (ServerWorld)(Object)this;
      if(!ContinuumAnchor.getLoadedChunks(serverWorld).isEmpty()){
         serverWorld.resetIdleTimeout();
      }
   }
}
