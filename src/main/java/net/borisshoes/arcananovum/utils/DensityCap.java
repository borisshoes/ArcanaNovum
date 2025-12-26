package net.borisshoes.arcananovum.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.MobCategory;

public class DensityCap {
   private final Object2IntMap<MobCategory> spawnGroupsToDensity = new Object2IntOpenHashMap(MobCategory.values().length);
   
   public DensityCap(){
   }
   
   public void increaseDensity(MobCategory spawnGroup){
      this.spawnGroupsToDensity.computeInt(spawnGroup, (group, density) -> {
         return density == null ? 1 : density + 1;
      });
   }
   
   public boolean canSpawn(MobCategory spawnGroup){
      return this.spawnGroupsToDensity.getOrDefault(spawnGroup, 0) < spawnGroup.getMaxInstancesPerChunk();
   }
}