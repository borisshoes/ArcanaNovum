package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ArcanaServerCommonPacketListenerMixin {
   
   @Shadow
   @Final
   protected MinecraftServer server;
   
   @Inject(method = "handleCustomClickAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/Identifier;Ljava/util/Optional;)V"), cancellable = true)
   public void arcananovum$handleArcanaClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci){
      if(!packet.id().equals(ArcanaNovum.ARCANA_CLICK_ACTION_ID)) return;
      if(!((Object) this instanceof ServerGamePacketListenerImpl game)) return;
      ServerPlayer player = game.player;
      packet.payload().flatMap(Tag::asCompound).ifPresent(root ->
            root.getString(ArcanaNovum.ARCANA_CLICK_KEY).ifPresent(command ->
                  server.getCommands().performPrefixedCommand(player.createCommandSourceStack(), command)
            ));
      ci.cancel();
   }
}
