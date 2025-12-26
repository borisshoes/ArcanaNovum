package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellBlockEntity.class)
public class BellBlockEntityMixin {
   
   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
   private static void arcananovum$resonateBell(Level world, BlockPos pos, BlockState state, BellBlockEntity blockEntity, BellBlockEntity.ResonationEndAction bellEffect, CallbackInfo ci){
      if(world instanceof ServerLevel serverWorld){
         for(ServerPlayer player : serverWorld.getPlayers(player -> player.blockPosition().closerThan(pos, 25.0))){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.RESONATE_BELL, true);
         }
      }
   }
}
