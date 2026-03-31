package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public class FishingHookMixin {
   
   @Inject(method = "pullEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
   private void arcananovum$pullHookedEntity(CallbackInfo ci){
      FishingHook bobber = (FishingHook) (Object) this;
      
      if(bobber.getPlayerOwner() instanceof ServerPlayer player){
         if(bobber.getHookedIn() instanceof ItemEntity){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FISH_ITEM, true);
         }else if(bobber.getHookedIn() instanceof Mob){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.FISH_MOB, true);
         }
      }
   }
}
