package net.borisshoes.arcananovum.events;

import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class NulMementoEvent extends Event {
   public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID,"nul_memento_event");
   
   private final ServerPlayer player;
   private final ItemStack memento;
   
   public NulMementoEvent(int interval, ServerPlayer player, ItemStack memento){
      super(ID, interval+10);
      this.player = player;
      this.memento = memento;
   }
   
   public ItemStack getMemento(){
      return memento;
   }
   
   public ServerPlayer getPlayer(){
      return player;
   }
}

