package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSaveHandler.class)
public class PlayerSaveMixin {
   @Inject(method = "savePlayerData", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
   private void arcananovum_logPlayerSaveError(PlayerEntity player, CallbackInfo ci, @Local Exception exception){
      exception.printStackTrace();
   }
}
