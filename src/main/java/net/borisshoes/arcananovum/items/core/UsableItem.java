package net.borisshoes.arcananovum.items.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public interface UsableItem {
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand);
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result);
}
