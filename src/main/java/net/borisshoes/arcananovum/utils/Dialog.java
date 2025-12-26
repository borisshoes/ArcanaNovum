package net.borisshoes.arcananovum.utils;

import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public record Dialog(ArrayList<MutableComponent> message, ArrayList<DialogSound> sounds, int[] delay, int weightNoCond, int weightWithCond, int condMask){
   
   public int getWeight(boolean[] args){
      int bitflag = condMask;
      for(int i = 0; bitflag > 0 && i < args.length; i++, bitflag >>= 1){
         if((bitflag & 1) == 1){
            if(!args[i]){
               return weightNoCond;
            }
         }
      }
      return weightWithCond;
   }
   
   public record DialogSound(SoundEvent soundEvent, float volume, float pitch){
      public void playSound(ServerPlayer player){
         SoundUtils.playSongToPlayer(player,soundEvent,volume,pitch);
      }
      
      public void playSound(Level world, BlockPos pos){
         SoundUtils.playSound(world,pos,soundEvent, SoundSource.MASTER,volume,pitch);
      }
      
      public void playSound(Level world, BlockPos pos, SoundSource category){
         SoundUtils.playSound(world,pos,soundEvent,category,volume,pitch);
      }
   }
}


