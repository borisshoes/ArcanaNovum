package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
   
   @Shadow @Final private @Nullable Entity source;
   
   @Inject(method="getDeathMessage",at=@At(value="RETURN"), cancellable = true)
   private void arcananovum_deathMessage(LivingEntity killed, CallbackInfoReturnable<Text> cir){
      DamageSource source = (DamageSource) (Object) this;
      
      if(source.getName().contains("arcananovum.concentration")){
         AbstractTeam abstractTeam = killed.getScoreboardTeam();
         Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.LIGHT_PURPLE;
         String[] deathStrings = {
               " lost concentration on their Arcana",
               "'s mind was consumed by their Arcana",
               "'s was crushed by the power of their Arcana",
               "'s items consumed too much concentration",
               " couldn't channel enough Arcana to their items"
         };
         final Text deathMsg = Text.literal("")
               .append(Text.literal(killed.getEntityName()).formatted(playerColor).formatted())
               .append(Text.literal(deathStrings[(int)(Math.random()*deathStrings.length)]).formatted(Formatting.LIGHT_PURPLE));
         cir.setReturnValue(deathMsg);
      }else if(source.getName().contains("arcananovum.detonationarrow")){
         AbstractTeam abstractTeam = killed.getScoreboardTeam();
         Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.WHITE;
         final Text deathMsg;
         if(source.getAttacker() == null){
            deathMsg = Text.literal("")
                  .append(Text.literal(killed.getEntityName()).formatted(playerColor).formatted())
                  .append(Text.literal(" was obliterated by a Detonation Arrow").formatted(Formatting.WHITE));
         }else{
            deathMsg = Text.literal("")
                  .append(Text.literal(killed.getEntityName()).formatted(playerColor).formatted())
                  .append(Text.literal(" was obliterated by ").formatted(Formatting.WHITE))
                  .append(source.getAttacker().getDisplayName())
                  .append(Text.literal("'s Detonation Arrow").formatted(Formatting.WHITE));
         }
         cir.setReturnValue(deathMsg);
      }else if(source.getName().contains("arcananovum.photonic")){
         AbstractTeam abstractTeam = killed.getScoreboardTeam();
         Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.WHITE;
         final Text deathMsg;
         if(source.getAttacker() == null){
            deathMsg = Text.literal("")
                  .append(Text.literal(killed.getEntityName()).formatted(playerColor).formatted())
                  .append(Text.literal(" was ionized by a Photonic Arrow").formatted(Formatting.WHITE));
         }else{
            deathMsg = Text.literal("")
                  .append(Text.literal(killed.getEntityName()).formatted(playerColor).formatted())
                  .append(Text.literal(" was ionized by ").formatted(Formatting.WHITE))
                  .append(source.getAttacker().getDisplayName())
                  .append(Text.literal("'s Photonic Arrow").formatted(Formatting.WHITE));
         }
         cir.setReturnValue(deathMsg);
      }
   }
}
