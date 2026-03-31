package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
   
   @Inject(method = "tryTakeInItemFromSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setChanged()V"))
   private static void arcananovum$enderCrateTake(Hopper hopper, Container container, int i, Direction direction, CallbackInfoReturnable<Boolean> cir){
      if(container instanceof EnderCrateBlockEntity crate){
         if(crate.getCrafterId() != null && !crate.getCrafterId().isBlank()){
            ArcanaAchievements.progress(AlgoUtils.getUUID(crate.getCrafterId()), ArcanaAchievements.ENDERON_PRIME, 1);
            ArcanaAchievements.grant(AlgoUtils.getUUID(crate.getCrafterId()), ArcanaAchievements.OUT_OF_THE_BOX);
         }
      }
   }
}
