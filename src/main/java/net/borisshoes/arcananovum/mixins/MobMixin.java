package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.entities.NulGuardianEntity;
import net.borisshoes.arcananovum.utils.ArcanaEffectUtils;
import net.borisshoes.arcananovum.utils.SpawnPile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(Mob.class)
public class MobMixin {
   
   // Continuum Anchor Mob Despawn Check
   @ModifyExpressionValue(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
   private double arcananovum$checkDespawn(double original){
      Mob mob = (Mob) (Object) this;
      if(mob.level() instanceof ServerLevel world){
         ChunkAccess chunk = world.getChunk(mob.blockPosition());
         if(ContinuumAnchor.isChunkLoaded(world, chunk.getPos())){
            //log("Entity: "+entity.getEntityName()+" ("+entity.getPos().toString()+") distSq: "+distToPlayerSq+" imDespSq: "+imDespSq);
            return 0.1;
         }
      }
      return original;
   }
   
   @Inject(method = "checkDespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;discard()V"), cancellable = true)
   private void arcananovum$teleportConstructGuardians(CallbackInfo ci){
      Mob mob = (Mob) (Object) this;
      if(mob instanceof NulGuardianEntity guardian){
         if(guardian.getConstruct() != null){
            List<BlockPos> poses = SpawnPile.makeSpawnLocations(1, 16, (ServerLevel) guardian.getConstruct().level(), EntityType.WITHER_SKELETON, guardian.getConstruct().blockPosition());
            if(poses.isEmpty() || poses.getFirst() == null) return;
            ArcanaEffectUtils.shadowGlaiveTp((ServerLevel) guardian.level(), guardian.position());
            guardian.teleport(new TeleportTransition((ServerLevel) guardian.getConstruct().level(), poses.getFirst().getCenter(), Vec3.ZERO, guardian.getYRot(), guardian.getXRot(), TeleportTransition.DO_NOTHING));
            ArcanaEffectUtils.shadowGlaiveTp((ServerLevel) guardian.level(), guardian.position());
            ci.cancel();
         }
      }
   }
}