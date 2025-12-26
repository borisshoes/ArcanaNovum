package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(Mob.class)
public class MobMixin {
   
   // Continuum Anchor Mob Despawn Check
   @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V", ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
   private void checkDespawn(CallbackInfo ci, @Local double distToPlayerSq, @Local(ordinal = 1) int imDespSq){
      Mob mob = (Mob) (Object) this;
      if(mob.level() instanceof ServerLevel world){
         ChunkAccess chunk = world.getChunk(mob.blockPosition());
         if(ContinuumAnchor.isChunkLoaded(world,chunk.getPos())){
            //log("Entity: "+entity.getEntityName()+" ("+entity.getPos().toString()+") distSq: "+distToPlayerSq+" imDespSq: "+imDespSq);
            
            if(distToPlayerSq > (double)imDespSq && mob.removeWhenFarAway(distToPlayerSq)){
               if(mob.getNoActionTime() < 1200 || ((int)(Math.random()*1600)) != 0 || !mob.removeWhenFarAway(distToPlayerSq)){
                  ci.cancel();
               }
            }
         }
      }
   }
}