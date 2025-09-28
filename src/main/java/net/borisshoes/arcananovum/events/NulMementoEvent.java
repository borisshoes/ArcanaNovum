package net.borisshoes.arcananovum.events;

import net.borisshoes.borislib.events.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulMementoEvent extends Event {
   public static final Identifier ID = Identifier.of(MOD_ID,"nul_memento_event");
   
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

