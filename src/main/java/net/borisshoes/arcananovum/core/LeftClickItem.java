package net.borisshoes.arcananovum.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface LeftClickItem {
   public boolean attackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction);
}
