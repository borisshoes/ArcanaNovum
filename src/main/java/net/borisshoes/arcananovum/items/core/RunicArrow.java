package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public interface RunicArrow {
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult, MagicEntity magicEntity);
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult, MagicEntity magicEntity);
}
