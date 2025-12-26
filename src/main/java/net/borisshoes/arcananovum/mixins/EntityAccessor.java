package net.borisshoes.arcananovum.mixins;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
   
   @Invoker
   public void callSetLevel(Level world);
   
   @Accessor
   static EntityDataAccessor<Boolean> getDATA_NO_GRAVITY(){
      throw new UnsupportedOperationException();
   }
}