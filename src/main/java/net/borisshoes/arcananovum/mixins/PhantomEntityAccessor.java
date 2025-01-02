package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PhantomEntity.class)
public interface PhantomEntityAccessor {
   
   @Accessor
   static TrackedData<Integer> getSIZE(){
      throw new UnsupportedOperationException();
   }
}
