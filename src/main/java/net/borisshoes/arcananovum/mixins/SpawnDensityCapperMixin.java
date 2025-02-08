package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.accessors.ServerChunkLoadingManagerAccessor;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.utils.DensityCap;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.SpawnDensityCapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(SpawnDensityCapper.class)
public class SpawnDensityCapperMixin {
   
   @Shadow
   private final ServerChunkLoadingManager chunkLoadingManager;
   
   public SpawnDensityCapperMixin()
   {
      chunkLoadingManager = null;
   }
   
   private Map<ChunkPos, DensityCap> chunkPosDensityCapMap;
   
   
   @Inject(method = "<init>(Lnet/minecraft/server/world/ServerChunkLoadingManager;)V", at = @At(value = "TAIL"))
   private void SpawnDensityCapper(ServerChunkLoadingManager threadedAnvilChunkStorage, CallbackInfo cir){
      chunkPosDensityCapMap = new HashMap<>();
   }
   
   @Inject(method = "increaseDensity", at = @At(value = "HEAD"), cancellable = true)
   private void increaseDensity(ChunkPos chunkPos, SpawnGroup spawnGroup, CallbackInfo cir){
      if(ContinuumAnchor.isChunkLoaded(((ServerChunkLoadingManagerAccessor)(chunkLoadingManager)).getHookedWorld(), chunkPos)){
         this.chunkPosDensityCapMap.computeIfAbsent(chunkPos, pos -> new DensityCap()).increaseDensity(spawnGroup);
         cir.cancel();
      }
   }
   
   @ModifyReturnValue(method = "canSpawn", at = @At(value = "RETURN"))
   private boolean canSpawn(boolean original, SpawnGroup spawnGroup, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(((ServerChunkLoadingManagerAccessor)(chunkLoadingManager)).getHookedWorld(), chunkPos)){
         DensityCap densityCap = this.chunkPosDensityCapMap.get(chunkPos);
         return densityCap != null && densityCap.canSpawn(spawnGroup);
      }
      return original;
   }
}



