package net.borisshoes.arcananovum.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherBoss.class)
public interface WitherBossAccessor {
   
   @Accessor
   static EntityDataAccessor<Integer> getDATA_TARGET_A(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static EntityDataAccessor<Integer> getDATA_TARGET_B(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static EntityDataAccessor<Integer> getDATA_TARGET_C(){
      throw new UnsupportedOperationException();
   }
   
   @Accessor
   static EntityDataAccessor<Integer> getDATA_ID_INV(){
      throw new UnsupportedOperationException();
   }
}
