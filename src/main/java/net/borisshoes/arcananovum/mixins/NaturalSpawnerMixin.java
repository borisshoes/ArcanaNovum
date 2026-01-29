package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.blocks.InterdictorBlockEntity;
import net.borisshoes.arcananovum.datastorage.InterdictionZones;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
   
   @ModifyExpressionValue(method = "createState", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/entity/Mob;requiresCustomPersistence()Z"))
   private static boolean arcananovum$infuserMobCapGet(boolean original, @Local Mob entity){
      if(original) return true;
      if(entity.getTags().contains("$arcananovum.infused_spawn")){
         return true;
      }else{
         return entity.requiresCustomPersistence();
      }
   }
   
   @ModifyExpressionValue(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isRedstoneConductor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
   private static boolean arcananovum$interdictorCancel(boolean original, MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback){
      if(mobCategory != MobCategory.MONSTER || original) return original;
      InterdictionZones worldZones = DataAccess.getWorld(serverLevel.dimension(), InterdictionZones.KEY);
      if(worldZones.isInAnyBlockingZone(blockPos)){
         InterdictionZones.InterdictionZone zone = worldZones.getBlockingZonesContaining(blockPos).getFirst();
         if(serverLevel.getBlockEntity(zone.getSourcePos()) instanceof InterdictorBlockEntity interdictor){
            interdictor.onSpawn();
            return true;
         }
         return original;
      }
      return original;
   }
   
   @Inject(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/SpawnGroupData;)Lnet/minecraft/world/entity/SpawnGroupData;"))
   private static void arcananovum$interdictorRedirect(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci, @Local Mob mob){
      if(mobCategory != MobCategory.MONSTER) return;
      InterdictionZones worldZones = DataAccess.getWorld(serverLevel.dimension(), InterdictionZones.KEY);
      if(worldZones.isInAnyRedirectZone(mob.position())){
         InterdictionZones.InterdictionZone zone = worldZones.getRedirectZonesContaining(mob.position()).getFirst();
         if(serverLevel.getBlockEntity(zone.getSourcePos()) instanceof InterdictorBlockEntity interdictor){
            interdictor.onSpawn();
            mob.snapTo(zone.getSourcePos().getX()+0.5, zone.getSourcePos().getY() + 3, zone.getSourcePos().getZ()+0.5, serverLevel.random.nextFloat() * 360.0F, 0.0F);
         }
      }
   }
}
