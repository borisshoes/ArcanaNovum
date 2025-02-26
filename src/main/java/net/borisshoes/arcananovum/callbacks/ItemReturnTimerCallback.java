package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemReturnTimerCallback extends TickTimerCallback{
   
   public ItemReturnTimerCallback(ItemStack item, ServerPlayerEntity player){
      super(1, item, player);
   }
   
   @Override
   public void onTimer(){
      ServerPlayerEntity player1 = player.getServer().getPlayerManager().getPlayer(player.getUuid());
      if(player1 == null){
         ArcanaNovum.addLoginCallback(new ItemReturnLoginCallback(player,item));
      }else{
         if(!player1.isAlive() || !player1.getInventory().insertStack(item)){
            ArcanaNovum.addTickTimerCallback(new ItemReturnTimerCallback(item,player1));
         }
      }
   }
}
