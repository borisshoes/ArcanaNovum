package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FleeEntityGoal.class)
public class FleeEntityGoalMixin<T extends LivingEntity> {
   
   @Shadow
   @Final
   protected PathAwareEntity mob;
   
   @Shadow
   protected T targetEntity;
   
   @Inject(method = "start", at=@At("RETURN"))
   private void arcananovum_fleeEntityStart(CallbackInfo ci){
      if(mob instanceof CreeperEntity && targetEntity instanceof CatEntity cat){
         if(cat.getWorld() instanceof ServerWorld serverWorld){
            for(ServerPlayerEntity player : serverWorld.getPlayers(player -> player.getBlockPos().isWithinDistance(cat.getBlockPos(), 10.0))){
               ArcanaNovum.data(player).setResearchTask(ResearchTasks.CAT_SCARE, true);
            }
         }
      }
   }
}
