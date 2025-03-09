package net.borisshoes.arcananovum.events;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class NulMementoEvent extends ArcanaEvent{
   public static final String ID = "nul_memento_event";
   
   private final ServerPlayerEntity player;
   private final ItemStack memento;
   
   public NulMementoEvent(int interval, ServerPlayerEntity player, ItemStack memento){
      super(ID, interval+10);
      this.player = player;
      this.memento = memento;
   }
   
   public ItemStack getMemento(){
      return memento;
   }
   
   public ServerPlayerEntity getPlayer(){
      return player;
   }
}

