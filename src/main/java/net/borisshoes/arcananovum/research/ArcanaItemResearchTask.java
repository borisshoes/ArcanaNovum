package net.borisshoes.arcananovum.research;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ArcanaItemResearchTask extends ResearchTask{
   
   private final ArcanaItem arcanaItem;
   
   public ArcanaItemResearchTask(String id, ArcanaItem arcanaItem, Component name, Component[] description, ItemStack displayItem){
      super(id, Type.ARCANA_ITEM_UNLOCK, name, description, displayItem);
      this.arcanaItem = arcanaItem;
   }
   
   public ArcanaItemResearchTask(String id, ArcanaItem arcanaItem, Component name, Component[] description, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.ARCANA_ITEM_UNLOCK, name, description, displayItem, prerequisites);
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return ArcanaNovum.data(player).hasResearched(arcanaItem);
   }
}
