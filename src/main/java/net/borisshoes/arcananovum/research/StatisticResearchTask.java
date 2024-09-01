package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class StatisticResearchTask<T> extends ResearchTask{
   
   private final int threshold;
   private final Either<Identifier, Pair<StatType<T>,T>> data;
   
   public StatisticResearchTask(String id, Either<Identifier, Pair<StatType<T>,T>> data, int threshold, Text name, Text[] description, ItemStack displayItem){
      super(id, Type.STATISTIC, name, description, displayItem);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   public StatisticResearchTask(String id, Either<Identifier, Pair<StatType<T>,T>> data, int threshold, Text name, Text[] description, ItemStack displayItem, RegistryKey<ResearchTask>... prerequisites){
      super(id, Type.STATISTIC, name, description, displayItem, prerequisites);
      this.data = data;
      this.threshold = threshold;
      
   }
   
   @Override
   public boolean isAcquired(ServerPlayerEntity player){
      return data.left().map(identifier -> player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(identifier)) > threshold)
            .orElseGet(() -> data.right().filter(statTypeTPair -> player.getStatHandler().getStat(statTypeTPair.getLeft(), statTypeTPair.getRight()) > threshold).isPresent());
   }
}
