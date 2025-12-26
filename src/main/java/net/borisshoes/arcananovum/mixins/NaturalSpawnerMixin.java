package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
   
   @ModifyExpressionValue(method = "createState", at = @At(value = "INVOKE",target = "Lnet/minecraft/world/entity/Mob;requiresCustomPersistence()Z"))
   private static boolean arcananovum$infuserMobCapGet(boolean original, @Local Mob entity){
      if(original) return true;
      if(entity.getTags().contains("$arcananovum.infused_spawn")){
         return true;
      }else{
         return entity.requiresCustomPersistence();
      }
   }
}
