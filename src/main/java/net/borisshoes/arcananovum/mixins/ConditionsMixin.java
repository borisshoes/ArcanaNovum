package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.events.CleansingCharmEvent;
import net.borisshoes.borislib.conditions.ConditionInstance;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.events.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Conditions.class)
public class ConditionsMixin {
   
   @Inject(method = "addCondition", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"))
   private static void arcananovum$onConditionAdd(MinecraftServer server, LivingEntity entity, ConditionInstance instance, CallbackInfoReturnable<Boolean> cir){
      if(entity instanceof ServerPlayer player && Event.getEventsOfType(CleansingCharmEvent.class).stream().anyMatch(event -> event.getPlayer().equals(player) &&
            event.isCondition() && event.getCondition().getId().equals(instance.getId()) && event.getCondition().getCondition().equals(instance.getCondition()))){
         ArcanaAchievements.grant(player, ArcanaAchievements.CHRONIC_AILMENT);
      }
   }
   
   
}
