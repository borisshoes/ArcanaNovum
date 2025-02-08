package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
   
   @Final
   @Shadow
   private List<WorldChunk> chunks;
   
   @Unique
   private final Set<ChunkPos> chunkSet = new HashSet<>();
   
   @Inject(method = "addChunksToTick", at=@At("TAIL"))
   private void arcananovum_test(List<WorldChunk> chunks, CallbackInfo ci){
      this.chunks.forEach(worldChunk -> chunkSet.add(worldChunk.getPos()));
      
      ServerChunkManager manager = (ServerChunkManager) (Object) this;
      if(manager.getWorld() instanceof ServerWorld serverWorld){
         ContinuumAnchor.getLoadedChunks(serverWorld).forEach(chunkPos -> {
            if(!chunkSet.contains(chunkPos)){
               this.chunks.add(serverWorld.getChunk(chunkPos.x,chunkPos.z));
            }
         });
      }
      
      this.chunkSet.clear();
   }
}
