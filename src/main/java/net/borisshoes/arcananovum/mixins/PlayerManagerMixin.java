package net.borisshoes.arcananovum.mixins;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
   
//   @Inject(method="respawnPlayer",at=@At(value="INVOKE",target="Lnet/minecraft/server/PlayerManager;sendWorldInfo(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/world/ServerWorld;)V", shift= At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
//   private void respawnPlayer(ServerPlayerEntity oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir, TeleportTarget teleportTarget, ServerWorld serverWorld, ServerPlayerEntity newPlayer, Vec3d vec3d, byte b, ServerWorld serverWorld2, WorldProperties worldProperties) {
//      if (alive) {
//         newPlayer.getEquipmentChanges();
//         newPlayer.setHealth(oldPlayer.getHealth());
//      }
//   }
}

// I think this was fixed
