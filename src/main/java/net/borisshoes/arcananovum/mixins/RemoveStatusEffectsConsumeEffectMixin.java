package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemoveStatusEffectsConsumeEffect.class)
public class RemoveStatusEffectsConsumeEffectMixin {
   
   @Inject(method = "apply", at = @At("RETURN"))
   private void arcananovum$honeyCleanse(Level world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValue() && stack.is(Items.HONEY_BOTTLE) && user instanceof ServerPlayer player){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.HONEY_CLEANSE, true);
      }
   }
}
