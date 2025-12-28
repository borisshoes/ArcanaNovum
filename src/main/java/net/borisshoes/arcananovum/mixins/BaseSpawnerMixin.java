package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Credit to xZarex for some of the Chunk Loading mixin code
@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {
   @ModifyReturnValue(method = "isNearPlayer", at = @At(value = "RETURN"))
   private boolean arcananovum$isPlayerInRange(boolean original, Level world, BlockPos pos){
      if(original) return true;
      
      if(world instanceof ServerLevel serverWorld){
         ChunkAccess chunk = world.getChunk(pos);
         if(ContinuumAnchor.isChunkLoaded(serverWorld,chunk.getPos())){
            return true;
         }
   
         BlockPos infuserPos = pos.offset(0,-2,0);
         BlockState state = serverWorld.getBlockState(infuserPos);
         BlockEntity be = world.getBlockEntity(infuserPos);
         if(be instanceof SpawnerInfuserBlockEntity infuser){
            if(infuser.isActive()){
               boolean emulator = ArcanaAugments.getAugmentFromMap(infuser.getAugments(),ArcanaAugments.SPIRIT_EMULATOR.id) >= 1;
               if(emulator){
                  return true;
               }
            }
         }
         
         
      }
      return false;
   }
   
   @Inject(method = "isNearPlayer", at = @At("RETURN"))
   private void arcananovum$forPlayersInRange(Level world, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValue() && world instanceof ServerLevel serverWorld){
         for(ServerPlayer player : serverWorld.getPlayers(player -> player.blockPosition().closerThan(pos, 5.0))){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FIND_SPAWNER, true);
         }
      }
   }
   
   @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
   private void arcananovum$infuserMobCapSet(ServerLevel world, BlockPos pos, CallbackInfo ci, @Local Entity entity){
      if(!(entity instanceof Mob mob)) return;
      BlockPos infuserPos = pos.offset(0,-2,0);
      BlockEntity be = world.getBlockEntity(infuserPos);
      boolean infuserActive = (be instanceof SpawnerInfuserBlockEntity infuser) && infuser.isActive();
      if(infuserActive) mob.addTag("$arcananovum.infused_spawn");
   }
}
