package net.borisshoes.arcananovum.areaeffects;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public abstract class AreaEffectSource {
   public abstract List<BlockPos> getAffectedBlocks(ServerWorld world);
   public abstract List<Entity> getAffectedEntities(ServerWorld world);
   public abstract int getDuration();
}
