package net.borisshoes.arcananovum.mixins;

import com.mojang.datafixers.DataFixer;
import net.borisshoes.arcananovum.accessors.ThreadedAnvilChunkStorageAccessor;
import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements ThreadedAnvilChunkStorageAccessor {
   private ServerWorld hookedWorld;
   
   @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/structure/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/util/thread/ThreadExecutor;Lnet/minecraft/world/chunk/ChunkProvider;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/chunk/ChunkStatusChangeListener;Ljava/util/function/Supplier;IZ)V", at = @At(value = "TAIL"))
   private void ThreadedAnvilChunkStorage(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, int viewDistance, boolean dsync, CallbackInfo ci) {
      hookedWorld = world;
   }
   
   @Override
   public ServerWorld getHookedWorld() {
      return hookedWorld;
   }
   
   @Inject(method = "shouldTick", at = @At("HEAD"), cancellable = true)
   private void shouldTick(ChunkPos chunkPos, CallbackInfoReturnable<Boolean> cir) {
      if (ContinuumAnchor.isChunkLoaded(hookedWorld, chunkPos)) {
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
}
