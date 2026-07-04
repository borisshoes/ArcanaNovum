package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;

public class StatisticResearchTask<T> extends ResearchTask {
   
   private final int threshold;
   private final Either<Identifier, Pair<StatType<T>, T>> data;
   
   public StatisticResearchTask(String id, Either<Identifier, Pair<StatType<T>, T>> data, int threshold, ItemStack displayItem){
      super(id, Type.STATISTIC, displayItem);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   public StatisticResearchTask(String id, Either<Identifier, Pair<StatType<T>, T>> data, int threshold, ItemStack displayItem, ResourceKey<ResearchTask>... prerequisites){
      super(id, Type.STATISTIC, displayItem, prerequisites);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   @Override
   public boolean isAcquired(ServerPlayer player){
      return data.left().map(identifier -> player.getStats().getValue(Stats.CUSTOM.get(identifier)) >= threshold)
            .orElseGet(() -> data.right().filter(statTypeTPair -> player.getStats().getValue(statTypeTPair.getFirst(), statTypeTPair.getSecond()) >= threshold).isPresent());
   }
}
