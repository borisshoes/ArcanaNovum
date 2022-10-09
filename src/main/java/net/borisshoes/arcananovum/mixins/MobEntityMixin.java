package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.ContinuumAnchor;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(MobEntity.class)
public class MobEntityMixin {
   
   // Continuum Anchor Mob Despawn Check
   @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D", shift = At.Shift.AFTER), cancellable = true)
   private void checkDespawn(CallbackInfo cir) {
      MobEntity mob = (MobEntity) (Object) this;
      if(mob.getWorld() instanceof ServerWorld world){
         Chunk chunk = world.getChunk(mob.getBlockPos());
         if(ContinuumAnchor.isChunkLoaded(world,chunk.getPos())) cir.cancel();
      }
   }
}