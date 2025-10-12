package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClearAllEffectsConsumeEffect.class)
public class ClearAllEffectsConsumeEffectMixin {

   @Inject(method = "onConsume", at = @At("RETURN"))
   private void arcananovum$drinkMilk(World world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir){
      if(stack.isOf(Items.MILK_BUCKET) && cir.getReturnValue() && user instanceof ServerPlayerEntity player){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.MILK_CLEANSE, true);
      }
   }
}
