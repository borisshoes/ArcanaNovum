package net.borisshoes.arcananovum.research;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class CustomResearchTask extends ResearchTask {
   
   public CustomResearchTask(String id, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem);
   }
   
   public CustomResearchTask(String id, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.CUSTOM_EVENT, name, description, displayItem, prerequisites);
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      return PLAYER_DATA.get(player).completedResearchTask(id);
   }
}
