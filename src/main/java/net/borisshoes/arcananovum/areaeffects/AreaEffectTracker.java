package net.borisshoes.arcananovum.areaeffects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class AreaEffectTracker {
   
   protected final String type;
   protected final List<AreaEffectSource> sources;
   
   public AreaEffectTracker(String type){
      this.type = type;
      this.sources = new ArrayList<>();
   }
   
   public abstract void onTick(MinecraftServer server);
   
   public abstract void addSource(AreaEffectSource source);
   
   public String getType(){
      return type;
   }
}
