package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.accessors.ServerChunkLoadingManagerAccessor;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.utils.DensityCap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(LocalMobCapCalculator.class)
public class LocalMobCapCalculatorMixin {
   
   @Shadow
   private final ChunkMap chunkMap;
   
   public LocalMobCapCalculatorMixin(){
      chunkMap = null;
   }
   
   private Map<ChunkPos, DensityCap> chunkPosDensityCapMap;
   
   
   @Inject(method = "<init>(Lnet/minecraft/server/level/ChunkMap;)V", at = @At(value = "TAIL"))
   private void SpawnDensityCapper(ChunkMap threadedAnvilChunkStorage, CallbackInfo cir){
      chunkPosDensityCapMap = new HashMap<>();
   }
   
   @Inject(method = "addMob", at = @At(value = "HEAD"), cancellable = true)
   private void increaseDensity(ChunkPos chunkPos, MobCategory spawnGroup, CallbackInfo cir){
      if(ContinuumAnchor.isChunkLoaded(((ServerChunkLoadingManagerAccessor) (chunkMap)).getHookedWorld(), chunkPos)){
         this.chunkPosDensityCapMap.computeIfAbsent(chunkPos, pos -> new DensityCap()).increaseDensity(spawnGroup);
         cir.cancel();
      }
   }
   
   @ModifyReturnValue(method = "canSpawn", at = @At(value = "RETURN"))
   private boolean canSpawn(boolean original, MobCategory spawnGroup, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(((ServerChunkLoadingManagerAccessor) (chunkMap)).getHookedWorld(), chunkPos)){
         DensityCap densityCap = this.chunkPosDensityCapMap.get(chunkPos);
         return densityCap != null && densityCap.canSpawn(spawnGroup);
      }
      return original;
   }
}



