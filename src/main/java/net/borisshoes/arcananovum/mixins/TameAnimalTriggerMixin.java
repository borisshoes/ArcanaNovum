package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.advancements.criterion.TameAnimalTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TameAnimalTrigger.class)
public class TameAnimalTriggerMixin {
   
   @Inject(method = "trigger", at = @At("HEAD"))
   private void arcananovum$tameAnimal(ServerPlayer serverPlayer, Animal animal, CallbackInfo ci){
      if(animal.getType() == EntityType.CAT){
         ArcanaNovum.data(serverPlayer).setResearchTask(ResearchTasks.TAME_CAT,true);
      }
   }
}
