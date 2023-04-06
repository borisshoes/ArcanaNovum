package net.borisshoes.arcananovum.items.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface UsableItem {
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand);
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult result);
   public boolean useItem(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult);
}
