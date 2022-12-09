package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.Arcananovum;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.TimerTask;

public class SoundUtils {
   public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
      player.networkHandler.sendPacket(new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x,player.getPos().y, player.getPos().z, vol, pitch,0));
   }
   
   public static void playSound(World world, BlockPos pos, SoundEvent event, SoundCategory category, float vol, float pitch){
      world.playSound(null,pos.getX(),pos.getY(),pos.getZ(),event, category, vol, pitch);
   }
   
   public static void soulSounds(ServerWorld world, BlockPos pos, int count, int duration){
      for(int i = 0; i < duration; i++){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(2*(i+1), new TimerTask() {
            @Override
            public void run(){
               for(int j = 0; j < count; j++){
                  playSound(world, pos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 1.3f, (float)(Math.random()*1.5+.5));
               }
            }
         }));
      }
   }
   
   public static void soulSounds(ServerPlayerEntity player, int count, int duration){
      for(int i = 0; i < duration; i++){
         Arcananovum.addTickTimerCallback(player.getWorld(), new GenericTimer(2*(i+1), new TimerTask() {
            @Override
            public void run(){
               for(int j = 0; j < count; j++){
                  playSongToPlayer(player, SoundEvents.PARTICLE_SOUL_ESCAPE, 2f, (float)(Math.random()*1.5+.5));
               }
            }
         }));
      }
   }
}
