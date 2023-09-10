package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
public abstract class PhantomSwoopGoalMixin extends Goal {
   
   @Final
   @Shadow
   PhantomEntity field_7333; // Outer class synthetic field
   
   @Inject(method = "shouldContinue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSpectator()Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
   private void shouldContinue(CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity){
      if(livingEntity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
         
            boolean isMagic = MagicItemUtils.isMagic(item);
            if(!isMagic)
               continue; // Item not magic, skip
         
            if(MagicItemUtils.identifyItem(item) instanceof FelidaeCharm || ArcanistsBelt.checkBeltAndHasItem(item, ArcanaRegistry.FELIDAE_CHARM.getItem())){
               if(field_7333 instanceof DragonPhantomEntity){
                  return; // Guardian Phantoms immune to Felidae Charm
               }
               
               SoundUtils.playSongToPlayer(player,SoundEvents.ENTITY_CAT_HISS, .1f, 1);
               PLAYER_DATA.get(player).addXP(2); // Add xp
               cir.setReturnValue(false);
            }
         }
      }
   }
}
