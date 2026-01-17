package net.borisshoes.arcananovum.research;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ObtainResearchTask extends ResearchTask{
   
   private final Item item;
   
   public ObtainResearchTask(String id, Item item, ItemStack displayItem){
      super(id, Type.OBTAIN_ITEM, displayItem);
      this.item = item;
   }
   
   public ObtainResearchTask(String id, Item item, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.OBTAIN_ITEM, displayItem, prerequisites);
      this.item = item;
   }
   
   public Item getItem(){
      return item;
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return ArcanaNovum.data(player).completedResearchTask(id);
   }
}
