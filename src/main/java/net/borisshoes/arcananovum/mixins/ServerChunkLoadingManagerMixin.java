package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DataFixer;
import net.borisshoes.arcananovum.accessors.ServerChunkLoadingManagerAccessor;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
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

import java.util.concurrent.Executor;
import java.util.function.Supplier;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin implements ServerChunkLoadingManagerAccessor {
   private ServerWorld hookedWorld;
   
   @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/structure/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/util/thread/ThreadExecutor;Lnet/minecraft/world/chunk/ChunkProvider;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/chunk/ChunkStatusChangeListener;Ljava/util/function/Supplier;IZ)V", at = @At(value = "TAIL"))
   private void ServerChunkLoadingManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, int viewDistance, boolean dsync, CallbackInfo ci){
      hookedWorld = world;
   }
   
   @Override
   public ServerWorld getHookedWorld(){
      return hookedWorld;
   }
   
   @ModifyReturnValue(method = "shouldTick", at = @At("RETURN"))
   private boolean arcananovum_shouldTick(boolean original, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(hookedWorld, chunkPos)){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "isAnyPlayerTicking", at = @At("RETURN"))
   private boolean arcananovum_isAnyPlayerTicking(boolean original, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(hookedWorld, chunkPos)){
         return true;
      }
      return original;
   }
}
