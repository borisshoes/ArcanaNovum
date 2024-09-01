package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(MobEntity.class)
public class MobEntityMixin {
   
   // Continuum Anchor Mob Despawn Check
   @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;discard()V", ordinal = 1, shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
   private void checkDespawn(CallbackInfo ci, Entity entity, double distToPlayerSq, int imDespRange, int imDespSq) {
      MobEntity mob = (MobEntity) (Object) this;
      if(mob.getWorld() instanceof ServerWorld world){
         Chunk chunk = world.getChunk(mob.getBlockPos());
         if(ContinuumAnchor.isChunkLoaded(world,chunk.getPos())){
            //log("Entity: "+entity.getEntityName()+" ("+entity.getPos().toString()+") distSq: "+distToPlayerSq+" imDespSq: "+imDespSq);
            
            if (distToPlayerSq > (double)imDespSq && mob.canImmediatelyDespawn(distToPlayerSq)) {
               if (mob.getDespawnCounter() < 600 || ((int)(Math.random()*800)) != 0 || !mob.canImmediatelyDespawn(distToPlayerSq)) {
                  ci.cancel();
               }
            }
         }
      }
   }
}