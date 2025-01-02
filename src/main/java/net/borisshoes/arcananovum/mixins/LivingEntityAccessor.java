package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
   @Invoker
   public void invokeDamageShield(float amount);
   
   @Accessor
   static TrackedData<Float> getHEALTH(){
      throw new UnsupportedOperationException();
   }
}
