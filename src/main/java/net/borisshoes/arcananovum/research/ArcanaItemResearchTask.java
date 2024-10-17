package net.borisshoes.arcananovum.research;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ArcanaItemResearchTask extends ResearchTask{
   
   private final ArcanaItem arcanaItem;
   
   public ArcanaItemResearchTask(String id, ArcanaItem arcanaItem, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.ARCANA_ITEM_UNLOCK, name, description, displayItem);
      this.arcanaItem = arcanaItem;
   }
   
   public ArcanaItemResearchTask(String id, ArcanaItem arcanaItem, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.ARCANA_ITEM_UNLOCK, name, description, displayItem, prerequisites);
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      return ArcanaNovum.data(player).hasResearched(arcanaItem);
   }
}
