package net.borisshoes.arcananovum.areaeffects;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class AreaEffectSource {
   public abstract List<BlockPos> getAffectedBlocks(ServerLevel world);
   
   public abstract List<Entity> getAffectedEntities(ServerLevel world);
   
   public abstract int getDuration();
}
