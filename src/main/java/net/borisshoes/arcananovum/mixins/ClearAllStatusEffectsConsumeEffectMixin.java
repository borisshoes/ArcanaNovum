package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public class ClearAllStatusEffectsConsumeEffectMixin {

   @Inject(method = "apply", at = @At("RETURN"))
   private void arcananovum$drinkMilk(Level world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir){
      if(stack.is(Items.MILK_BUCKET) && cir.getReturnValue() && user instanceof ServerPlayer player){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.MILK_CLEANSE, true);
      }
   }
}
