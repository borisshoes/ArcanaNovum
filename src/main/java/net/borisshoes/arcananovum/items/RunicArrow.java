package net.borisshoes.arcananovum.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public interface RunicArrow {
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult);
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult);
}
