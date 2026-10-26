package net.borisshoes.arcananovum.test.mixins;

import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * BorisLib sets up DataAccess from mixins on DedicatedServer and IntegratedServer only, so on a GameTestServer it is
 * still null when Arcana loads world data during level loading. Mirrors BorisLib's DedicatedServerMixin.
 * Can be removed once BorisLib handles GameTestServer itself.
 */
@Mixin(GameTestServer.class)
public class GameTestServerMixin {
   
   @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/GameTestServer;setPlayerList(Lnet/minecraft/server/players/PlayerList;)V"))
   private void arcananovumtest$initializeBorisLib(CallbackInfoReturnable<Boolean> cir){
      MinecraftServer server = (MinecraftServer) (Object) this;
      BorisLib.SERVER = server;
      DataAccess.onServerStarted(server);
   }
}
