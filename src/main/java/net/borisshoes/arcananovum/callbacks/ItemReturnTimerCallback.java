package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemReturnTimerCallback extends TickTimerCallback{
   
   final int prefSlot; // TODO Finish pref slot
   
   public ItemReturnTimerCallback(ItemStack item, ServerPlayerEntity player){
      super(1, item, player);
      this.prefSlot = -1;
   }
   
   public ItemReturnTimerCallback(ItemStack item, ServerPlayerEntity player, int prefSlot){
      super(1, item, player);
      this.prefSlot = prefSlot;
   }
   
   @Override
   public void onTimer(){
      ServerPlayerEntity player1 = player.getServer().getPlayerManager().getPlayer(player.getUuid());
      if(player1 == null){
         ArcanaNovum.addLoginCallback(new ItemReturnLoginCallback(player,item));
      }else{ // TODO: This doesn't work for stackable items! Insert returns true if the stack size decreases at all
         if(!player1.isAlive() || !player1.getInventory().insertStack(item)){
            ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(item,player1));
         }
      }
   }
}
