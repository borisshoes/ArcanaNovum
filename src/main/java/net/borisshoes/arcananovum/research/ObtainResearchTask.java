package net.borisshoes.arcananovum.research;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ObtainResearchTask extends ResearchTask{
   
   private final Item item;
   
   public ObtainResearchTask(String id, Item item, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.OBTAIN_ITEM, name, description, displayItem);
      this.item = item;
   }
   
   public ObtainResearchTask(String id, Item item, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.OBTAIN_ITEM, name, description, displayItem, prerequisites);
      this.item = item;
   }
   
   public Item getItem(){
      return item;
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      return PLAYER_DATA.get(player).completedResearchTask(id);
   }
}
