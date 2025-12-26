package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;

public class StatisticResearchTask<T> extends ResearchTask{
   
   private final int threshold;
   private final Either<Identifier, Tuple<StatType<T>,T>> data;
   
   public StatisticResearchTask(String id, Either<Identifier, Tuple<StatType<T>,T>> data, int threshold, Component name, Component[] description, ItemStack displayItem){
      super(id, Type.STATISTIC, name, description, displayItem);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   public StatisticResearchTask(String id, Either<Identifier, Tuple<StatType<T>,T>> data, int threshold, Component name, Component[] description, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.STATISTIC, name, description, displayItem, prerequisites);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return data.left().map(identifier -> player.getStats().getValue(Stats.CUSTOM.get(identifier)) >= threshold)
            .orElseGet(() -> data.right().filter(statTypeTPair -> player.getStats().getValue(statTypeTPair.getA(), statTypeTPair.getB()) >= threshold).isPresent());
   }
}
