package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
   
   @Inject(method="repairPlayerGears", at= @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setDamage(I)V"))
   private void arcananovum$mendingActivated(ServerPlayerEntity player, int amount, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 1) int j){
      if(j > 0) ArcanaNovum.data(player).setResearchTask(ResearchTasks.ACTIVATE_MENDING, true);
   }
}
