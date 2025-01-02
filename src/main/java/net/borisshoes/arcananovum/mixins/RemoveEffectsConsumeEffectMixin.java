package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.RemoveEffectsConsumeEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RemoveEffectsConsumeEffect.class)
public class RemoveEffectsConsumeEffectMixin {
   
   @Inject(method = "onConsume", at = @At("RETURN"))
   private void arcananovum_honeyCleanse(World world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> cir){
      if(cir.getReturnValue() && stack.isOf(Items.HONEY_BOTTLE) && user instanceof ServerPlayerEntity player){
         ArcanaNovum.data(player).setResearchTask(ResearchTasks.HONEY_CLEANSE, true);
      }
   }
}
