package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
   
   @Inject(method="teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", shift= At.Shift.AFTER))
   private void arcananovum_sendAbilitiesAfterDimChange(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
   }
}
