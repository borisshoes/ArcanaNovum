package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.DataFixer;
import net.borisshoes.arcananovum.accessors.ServerChunkLoadingManagerAccessor;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(ChunkMap.class)
public class ChunkMapMixin implements ServerChunkLoadingManagerAccessor {
   private ServerLevel hookedWorld;
   
   @Inject(method = "<init>", at = @At(value = "TAIL"))
   private void ServerChunkLoadingManager(ServerLevel world, LevelStorageSource.LevelStorageAccess session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, BlockableEventLoop mainThreadExecutor, LightChunkGetter chunkProvider, ChunkGenerator chunkGenerator, ChunkStatusUpdateListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, TicketStorage ticketManager, int viewDistance, boolean dsync, CallbackInfo ci){
      hookedWorld = world;
   }
   
   @Override
   public ServerLevel getHookedWorld(){
      return hookedWorld;
   }
   
   @ModifyReturnValue(method = "anyPlayerCloseEnoughForSpawning", at = @At("RETURN"))
   private boolean arcananovum$shouldTick(boolean original, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(hookedWorld, chunkPos)){
         return true;
      }
      return original;
   }
   
   @ModifyReturnValue(method = "anyPlayerCloseEnoughForSpawningInternal", at = @At("RETURN"))
   private boolean arcananovum$isAnyPlayerTicking(boolean original, ChunkPos chunkPos){
      if(!original && ContinuumAnchor.isChunkLoaded(hookedWorld, chunkPos)){
         return true;
      }
      return original;
   }
}
