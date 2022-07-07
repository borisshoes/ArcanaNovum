package net.borisshoes.arcananovum.utils;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoundUtils {
   public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
      player.networkHandler.sendPacket(new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x,player.getPos().y, player.getPos().z, vol, pitch));
   }
   
   public static void playSound(World world, BlockPos pos, SoundEvent event, SoundCategory category, float vol, float pitch){
      world.playSound(null,pos.getX(),pos.getY(),pos.getZ(),event, category, vol, pitch);
   }
}
