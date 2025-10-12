package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellBlockEntity.class)
public class BellBlockEntityMixin {
   
   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
   private static void arcananovum$resonateBell(World world, BlockPos pos, BlockState state, BellBlockEntity blockEntity, BellBlockEntity.Effect bellEffect, CallbackInfo ci){
      if(world instanceof ServerWorld serverWorld){
         for(ServerPlayerEntity player : serverWorld.getPlayers(player -> player.getBlockPos().isWithinDistance(pos, 25.0))){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.RESONATE_BELL, true);
         }
      }
   }
}
