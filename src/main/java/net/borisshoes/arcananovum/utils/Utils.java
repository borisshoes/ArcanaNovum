package net.borisshoes.arcananovum.utils;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class Utils {
   public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
      player.networkHandler.sendPacket(new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x,player.getPos().y, player.getPos().z, vol, pitch));
   }
}
