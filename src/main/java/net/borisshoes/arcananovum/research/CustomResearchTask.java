package net.borisshoes.arcananovum.research;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CustomResearchTask extends ResearchTask {
   
   public CustomResearchTask(String id, Component name, Component[] description, ItemStack displayItem){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem);
   }
   
   public CustomResearchTask(String id, Component name, Component[] description, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem, prerequisites);
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return ArcanaNovum.data(player).completedResearchTask(id);
   }
}
