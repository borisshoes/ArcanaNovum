package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

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
         BlockState state = serverWorld.getBlockState(infuserPos);
         BlockEntity be = world.getBlockEntity(infuserPos);
         if(be instanceof SpawnerInfuserBlockEntity infuser){
            if(infuser.isActive()){
               boolean emulator = ArcanaAugments.getAugmentFromMap(infuser.getAugments(),ArcanaAugments.SPIRIT_EMULATOR.id) >= 1;
               if(emulator){
                  cir.setReturnValue(true);
                  cir.cancel();
               }
            }
         }
         
         
      }
   }
   
   @Inject(method = "isPlayerInRange", at = @At("RETURN"))
   private void arcananovum_forPlayersInRange(World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if(cir.getReturnValue() && world instanceof ServerWorld serverWorld){
         for(ServerPlayerEntity player : serverWorld.getPlayers(player -> player.getBlockPos().isWithinDistance(pos, 5.0))){
            PLAYER_DATA.get(player).setResearchTask(ResearchTasks.FIND_SPAWNER, true);
         }
      }
   }
   
   @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"),locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_infuserMobCapSet(ServerWorld world, BlockPos pos, CallbackInfo ci, boolean bl, Random random, MobSpawnerEntry mobSpawnerEntry, int i, NbtCompound nbtCompound, Optional optional, NbtList nbtList, int j, double d, double e, double f, BlockPos blockPos, Entity entity){
      if(!(entity instanceof MobEntity mob)) return;
      BlockPos infuserPos = pos.add(0,-2,0);
      BlockEntity be = world.getBlockEntity(infuserPos);
      boolean infuserActive = (be instanceof SpawnerInfuserBlockEntity infuser) && infuser.isActive();
      if(infuserActive) mob.addCommandTag("$arcananovum.infused_spawn");
   }
}
