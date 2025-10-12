package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
   
   @ModifyExpressionValue(method = "setupSpawn", at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/mob/MobEntity;cannotDespawn()Z"))
   private static boolean arcananovum$infuserMobCapGet(boolean original, @Local MobEntity entity){
      if(original) return true;
      if(entity.getCommandTags().contains("$arcananovum.infused_spawn")){
         return true;
      }else{
         return entity.cannotDespawn();
      }
   }
}
