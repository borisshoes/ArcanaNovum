package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {
   @Accessor
   static TrackedData<Boolean> getENCHANTED(){
      throw new UnsupportedOperationException();
   }
}
