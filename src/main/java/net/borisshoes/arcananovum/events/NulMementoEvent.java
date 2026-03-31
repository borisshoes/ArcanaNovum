package net.borisshoes.arcananovum.events;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.events.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class NulMementoEvent extends Event {
   public static final Identifier ID = ArcanaRegistry.arcanaId("nul_memento_event");
   
   private final ServerPlayer player;
   private final ItemStack memento;
   
   public NulMementoEvent(int interval, ServerPlayer player, ItemStack memento){
      super(ID, interval + 10);
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

