package net.borisshoes.arcananovum.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrowableItemProjectile.class)
public interface ThrowableItemProjectileAccessor {
   @Accessor
   static EntityDataAccessor<ItemStack> getDATA_ITEM_STACK(){
      throw new UnsupportedOperationException();
   }
}