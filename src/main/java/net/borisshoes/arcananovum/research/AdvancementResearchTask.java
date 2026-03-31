package net.borisshoes.arcananovum.research;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class AdvancementResearchTask extends ResearchTask {
   
   private final String advancementId;
   
   public AdvancementResearchTask(String id, String advancementId, ItemStack displayItem){
      super(id, Type.ADVANCEMENT, displayItem);
      this.advancementId = advancementId;
   }
   
   public AdvancementResearchTask(String id, String advancementId, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.ADVANCEMENT, displayItem, prerequisites);
      this.advancementId = advancementId;
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      AdvancementHolder entry = player.level().getServer().getAdvancements().get(Identifier.parse(advancementId));
      if(entry == null) return false;
      return player.getAdvancements().getOrStartProgress(entry).isDone();
   }
}
