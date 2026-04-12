package net.borisshoes.arcananovum.utils;

import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.GenericTimer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class DialogHelper {
   
   private final ArrayList<Dialog> dialogs;
   private boolean[] conditions;
   
   public DialogHelper(){
      dialogs = new ArrayList<>();
      conditions = new boolean[]{};
   }
   
   public DialogHelper(ArrayList<Dialog> dialogs, boolean[] conditions){
      this.dialogs = dialogs;
      this.conditions = conditions;
   }
   
   public void setConditions(boolean[] conditions){
      this.conditions = conditions;
   }
   
   public void addDialog(Dialog dialog){
      dialogs.add(dialog);
   }
   
   public Dialog getWeightedResult(RandomSource random){
      ArrayList<Integer> pool = new ArrayList<>();
      int[] weights = new int[dialogs.size()];
      for(int i = 0; i < dialogs.size(); i++){
         weights[i] = dialogs.get(i).getWeight(conditions);
      }
      
      for(int i = 0; i < weights.length; i++){
         if(weights[i] == -1) return dialogs.get(i);
         for(int j = 0; j < weights[i]; j++){
            pool.add(i);
         }
      }
      
      return dialogs.get(pool.get(random.nextInt(pool.size())));
   }
   
   public static void sendDialog(List<ServerPlayer> players, Dialog dialog, boolean sounds){
      int index = 0;
      int curDelay = 0;
      int[] delay = dialog.delay();
      for(MutableComponent msg : dialog.message()){
         if(index < delay.length){
            curDelay += delay[index];
         }
         if(curDelay != 0){
            int finalIndex = index;
            BorisLib.addTickTimerCallback(new GenericTimer(curDelay, () -> {
               for(ServerPlayer player : players){
                  player.sendSystemMessage(msg, false);
                  if(sounds && finalIndex < dialog.sounds().size()){
                     Dialog.DialogSound soundEvent = dialog.sounds().get(finalIndex);
                     if(soundEvent != null){
                        soundEvent.playSound(player);
                     }
                  }
               }
            }));
         }else{
            for(ServerPlayer player : players){
               player.sendSystemMessage(msg, false);
               if(sounds && index < dialog.sounds().size()){
                  Dialog.DialogSound soundEvent = dialog.sounds().get(index);
                  if(soundEvent != null){
                     soundEvent.playSound(player);
                  }
               }
            }
            
         }
         index++;
      }
   }
}
