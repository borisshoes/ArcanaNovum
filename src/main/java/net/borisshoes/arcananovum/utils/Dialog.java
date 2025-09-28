package net.borisshoes.arcananovum.utils;

import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public record Dialog(ArrayList<MutableText> message, ArrayList<DialogSound> sounds, int[] delay, int weightNoCond, int weightWithCond, int condMask){
   
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
      public void playSound(ServerPlayerEntity player){
         SoundUtils.playSongToPlayer(player,soundEvent,volume,pitch);
      }
      
      public void playSound(World world, BlockPos pos){
         SoundUtils.playSound(world,pos,soundEvent,SoundCategory.MASTER,volume,pitch);
      }
      
      public void playSound(World world, BlockPos pos, SoundCategory category){
         SoundUtils.playSound(world,pos,soundEvent,category,volume,pitch);
      }
   }
}


