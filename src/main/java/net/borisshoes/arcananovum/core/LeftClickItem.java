package net.borisshoes.arcananovum.core;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface LeftClickItem {
   public boolean attackBlock(Player playerEntity, Level world, InteractionHand hand, BlockPos blockPos, Direction direction);
}
