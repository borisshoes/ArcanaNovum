package net.borisshoes.arcananovum.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.TeamColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
   
   @Shadow
   @Final
   private @Nullable Entity directEntity;
   
   @Inject(method = "getLocalizedDeathMessage", at = @At(value = "RETURN"), cancellable = true)
   private void arcananovum$deathMessage(LivingEntity killed, CallbackInfoReturnable<Component> cir){
      DamageSource source = (DamageSource) (Object) this;
      
      if(source.getMsgId().contains("arcananovum.concentration")){
         Team abstractTeam = killed.getTeam();
         TeamColor playerColor = TeamColor.WHITE;
         if(abstractTeam != null && abstractTeam.getColor().isPresent()){
            playerColor = abstractTeam.getColor().get();
         }
         String[] deathStrings = {
               " lost concentration on their Arcana",
               "'s mind was consumed by their Arcana",
               " was crushed by the power of their Arcana",
               "'s items consumed too much concentration",
               " couldn't channel enough Arcana to their items"
         };
         final Component deathMsg = Component.literal("")
               .append(Component.literal(killed.getScoreboardName()).withColor(playerColor.rgb()))
               .append(Component.literal(deathStrings[killed.getRandom().nextInt(deathStrings.length)]).withStyle(ChatFormatting.WHITE));
         cir.setReturnValue(deathMsg);
      }
   }
}
