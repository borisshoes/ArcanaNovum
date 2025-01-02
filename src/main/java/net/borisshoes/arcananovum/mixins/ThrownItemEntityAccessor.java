package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrownItemEntity.class)
public interface ThrownItemEntityAccessor {
   @Accessor
   static TrackedData<ItemStack> getITEM(){
      throw new UnsupportedOperationException();
   }
}