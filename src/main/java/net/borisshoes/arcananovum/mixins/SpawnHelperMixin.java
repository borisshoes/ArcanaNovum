package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {
   
   @Redirect(method = "setupSpawn", at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/mob/MobEntity;cannotDespawn()Z"))
   private static boolean arcananovum_infuserMobCapGet(MobEntity instance){
      if(instance.getCommandTags().contains("$arcananovum.infused_spawn")){
         return true;
      }else{
         return instance.cannotDespawn();
      }
   }
}
