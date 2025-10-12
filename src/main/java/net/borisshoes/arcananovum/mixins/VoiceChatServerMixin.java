package net.borisshoes.arcananovum.mixins;

import de.maxhenkel.voicechat.voice.common.LocationSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerSoundPacket;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.Server;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Collection;
import java.util.UUID;

@Pseudo
@Mixin(Server.class)
public class VoiceChatServerMixin {
   
   @ModifyArgs(method = "processProximityPacket", at = @At(value = "INVOKE", target = "Lde/maxhenkel/voicechat/voice/server/Server;broadcast(Ljava/util/Collection;Lde/maxhenkel/voicechat/voice/common/SoundPacket;Lnet/minecraft/server/network/ServerPlayerEntity;Lde/maxhenkel/voicechat/voice/common/PlayerState;Ljava/util/UUID;Ljava/lang/String;)V"))
   private void arcananovum$modifyBroadcast(Args args){
      //Collection<ServerPlayerEntity> players, SoundPacket<?> packet, @Nullable ServerPlayerEntity sender, @Nullable PlayerState senderState, @Nullable UUID groupId, String source
      Collection<ServerPlayerEntity> players = args.get(0);
      SoundPacket<?> packet = args.get(1);
      ServerPlayerEntity sender = args.get(2);
      PlayerState senderState = args.get(3);
      UUID groupId = args.get(4);
      String source = args.get(5);
      if(sender != null && sender.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT) && packet instanceof PlayerSoundPacket playerSoundPacket){
         args.set(1, new LocationSoundPacket(sender.getUuid(), sender.getUuid(), sender.getEyePos(), packet.getData(), packet.getSequenceNumber(), playerSoundPacket.getDistance(), (String)null));
         args.set(5, "spectator");
      }
   }
}
