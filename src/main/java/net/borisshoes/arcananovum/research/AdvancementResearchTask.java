package net.borisshoes.arcananovum.research;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AdvancementResearchTask extends ResearchTask{
   
   private final String advancementId;
   
   public AdvancementResearchTask(String id, String advancementId, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.ADVANCEMENT, name, description, displayItem);
      this.advancementId = advancementId;
   }
   
   public AdvancementResearchTask(String id, String advancementId, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.ADVANCEMENT, name, description, displayItem, prerequisites);
      this.advancementId = advancementId;
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      if(player.getServer() == null) return false;
      AdvancementEntry entry = player.getServer().getAdvancementLoader().get(Identifier.of(advancementId));
      if(entry == null) return false;
      return player.getAdvancementTracker().getProgress(entry).isDone();
   }
}
