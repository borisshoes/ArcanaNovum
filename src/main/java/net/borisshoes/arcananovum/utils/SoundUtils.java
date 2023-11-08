package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SoundUtils {
   public static void playSongToPlayer(ServerPlayerEntity player, RegistryEntry.Reference<SoundEvent> event, float vol, float pitch) {
      player.networkHandler.sendPacket(new PlaySoundS2CPacket(event, SoundCategory.PLAYERS, player.getPos().x,player.getPos().y, player.getPos().z, vol, pitch,0));
   }
   
   public static void playSongToPlayer(ServerPlayerEntity player, SoundEvent event, float vol, float pitch) {
      player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(event), SoundCategory.PLAYERS, player.getPos().x,player.getPos().y, player.getPos().z, vol, pitch,0));
   }
   
   public static void playSound(World world, BlockPos pos, SoundEvent event, SoundCategory category, float vol, float pitch){
      try{
         world.playSound(null,pos.getX(),pos.getY(),pos.getZ(),event, category, vol, pitch);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public static void playSound(World world, BlockPos pos, RegistryEntry.Reference<SoundEvent> event, SoundCategory category, float vol, float pitch){
      try{
         world.playSound(null,pos.getX(),pos.getY(),pos.getZ(),event,category,vol,pitch,0L);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   public static void soulSounds(ServerWorld world, BlockPos pos, int count, int duration){
      for(int i = 0; i < duration; i++){
         ArcanaNovum.addTickTimerCallback(world, new GenericTimer(2*(i+1), () -> {
            for(int j = 0; j < count; j++){
               playSound(world, pos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, 1.3f, (float)(Math.random()*1.5+.5));
            }
         }));
      }
   }
   
   public static void soulSounds(ServerPlayerEntity player, int count, int duration){
      for(int i = 0; i < duration; i++){
         ArcanaNovum.addTickTimerCallback(player.getServerWorld(), new GenericTimer(2*(i+1), () -> {
            for(int j = 0; j < count; j++){
               playSongToPlayer(player, SoundEvents.PARTICLE_SOUL_ESCAPE, 2f, (float)(Math.random()*1.5+.5));
            }
         }));
      }
   }
}
