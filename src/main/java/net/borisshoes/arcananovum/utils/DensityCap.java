package net.borisshoes.arcananovum.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.SpawnGroup;

public class DensityCap {
   private final Object2IntMap<SpawnGroup> spawnGroupsToDensity = new Object2IntOpenHashMap(SpawnGroup.values().length);
   
   public DensityCap(){
   }
   
   public void increaseDensity(SpawnGroup spawnGroup){
      this.spawnGroupsToDensity.computeInt(spawnGroup, (group, density) -> {
         return density == null ? 1 : density + 1;
      });
   }
   
   public boolean canSpawn(SpawnGroup spawnGroup){
      return this.spawnGroupsToDensity.getOrDefault(spawnGroup, 0) < spawnGroup.getCapacity();
   }
}