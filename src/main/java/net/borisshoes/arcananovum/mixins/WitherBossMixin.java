package net.borisshoes.arcananovum.mixins;

import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherBoss.class)
public class WitherBossMixin {
   
   @Redirect(method= "customServerAiStep",at=@At(value="INVOKE",target= "Lnet/minecraft/server/level/ServerLevel;globalLevelEvent(ILnet/minecraft/core/BlockPos;I)V"))
   private void arcananovum$redirectSpawnSound(ServerLevel instance, int eventId, BlockPos pos, int data){
      SoundUtils.playSound(instance,pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE,1,1);
   }
}
