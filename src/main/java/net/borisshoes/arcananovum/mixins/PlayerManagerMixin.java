package net.borisshoes.arcananovum.mixins;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
   
   @Inject(method="respawnPlayer",at=@At(value="INVOKE",target="Lnet/minecraft/server/PlayerManager;sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift= At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private void respawnPlayer(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld, Optional optional, ServerWorld serverWorld2, ServerPlayerEntity newPlayer, boolean bl2, byte b, ServerWorld serverWorld3, WorldProperties worldProperties) {
      if (alive) {
         newPlayer.getEquipmentChanges();
         newPlayer.setHealth(oldPlayer.getHealth());
      }
   }
}
