package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.borisshoes.arcananovum.items.SpawnerInfuser;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(MobSpawnerLogic.class)
public class MobSpawnerLogicMixin {
   @Inject(method = "isPlayerInRange", at = @At(value = "HEAD"), cancellable = true)
   private void arcananovum_isPlayerInRange(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if(world instanceof ServerWorld serverWorld){
         Chunk chunk = world.getChunk(pos);
         if(ContinuumAnchor.isChunkLoaded(serverWorld,chunk.getPos())){
            cir.setReturnValue(true);
            cir.cancel();
         }
   
         BlockPos infuserPos = pos.add(0,-2,0);
         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
         for(MagicBlock block : blocks){
            BlockPos magicPos = block.getPos();
            if(infuserPos.equals(magicPos) && block.getData().getString("id").equals(MagicItems.SPAWNER_INFUSER.getId())){
               NbtCompound blockData = block.getData();
               boolean active = blockData.getBoolean("active");
               if(active){
                  boolean emulator = ArcanaAugments.getAugmentFromCompound(block.getData(),"spirit_emulator") >= 1;
                  if(emulator){
                     cir.setReturnValue(true);
                     cir.cancel();
                  }
               }
               break;
            }
         }
      }
   }
}
