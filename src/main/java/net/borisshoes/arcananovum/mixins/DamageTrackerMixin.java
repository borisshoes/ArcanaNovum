package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(DamageTracker.class)
public class DamageTrackerMixin {

   @Inject(method="getDeathMessage",at=@At(value="INVOKE",target="Lnet/minecraft/entity/damage/DamageRecord;getAttackerName()Lnet/minecraft/text/Text;"),cancellable = true,locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_deathMessage(CallbackInfoReturnable<Text> cir, DamageRecord damageRecord, DamageRecord damageRecord2){
      if(damageRecord2.getDamageSource().name.contains("ArcanaNovum.Concentration")){
         DamageTracker tracker = (DamageTracker)(Object)this;
         AbstractTeam abstractTeam = tracker.getEntity().getScoreboardTeam();
         Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.LIGHT_PURPLE;
         String[] deathStrings = {
               " lost concentration on their Arcana",
               "'s mind was consumed by their Arcana",
               "'s was crushed by the power of their Arcana",
               "'s items consumed too much concentration",
               " couldn't channel enough Arcana to their items"
         };
         final Text deathMsg = Text.literal("")
               .append(Text.literal(tracker.getEntity().getEntityName()).formatted(playerColor).formatted())
               .append(Text.literal(deathStrings[(int)(Math.random()*deathStrings.length)]).formatted(Formatting.LIGHT_PURPLE));
         cir.setReturnValue(deathMsg);
      }
   }
}
