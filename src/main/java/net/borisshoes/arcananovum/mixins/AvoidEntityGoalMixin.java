package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvoidEntityGoal.class)
public class AvoidEntityGoalMixin<T extends LivingEntity> {
   
   @Shadow
   @Final
   protected PathfinderMob mob;
   
   @Shadow
   protected T toAvoid;
   
   @Inject(method = "start", at=@At("RETURN"))
   private void arcananovum$fleeEntityStart(CallbackInfo ci){
      if(mob instanceof Creeper && toAvoid instanceof Cat cat){
         if(cat.level() instanceof ServerLevel serverWorld){
            for(ServerPlayer player : serverWorld.getPlayers(player -> player.blockPosition().closerThan(cat.blockPosition(), 10.0))){
               ArcanaNovum.data(player).setResearchTask(ResearchTasks.CAT_SCARE, true);
            }
         }
      }
   }
}
