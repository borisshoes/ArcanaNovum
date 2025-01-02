package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherEntity.class)
public interface WitherEntityAccessor {
   
   @Accessor
   static TrackedData<Integer> getTRACKED_ENTITY_ID_1(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static TrackedData<Integer> getTRACKED_ENTITY_ID_2(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static TrackedData<Integer> getTRACKED_ENTITY_ID_3(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static TrackedData<Integer> getINVUL_TIMER(){
      throw new UnsupportedOperationException();
   }
}
