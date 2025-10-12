package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {
   
   @Inject(method = "pullHookedEntity", at = @At(value="INVOKE",target="Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
   private void arcananovum$pullHookedEntity(CallbackInfo ci){
      FishingBobberEntity bobber = (FishingBobberEntity) (Object) this;
      
      if(bobber.getPlayerOwner() instanceof ServerPlayerEntity player){
         if(bobber.getHookedEntity() instanceof ItemEntity){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FISH_ITEM, true);
         }else if(bobber.getHookedEntity() instanceof MobEntity){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FISH_MOB, true);
         }
      }
   }
}
