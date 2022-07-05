package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.FelidaeCharm;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
public abstract class PhantomMixin extends Goal {
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
         
            if(MagicItemUtils.identifyItem(item) instanceof FelidaeCharm){
               Utils.playSongToPlayer(player,SoundEvents.ENTITY_CAT_HISS, .1f, 1);
               PLAYER_DATA.get(player).addXP(2); // Add xp
               cir.setReturnValue(false);
            }
         }
      }
   }
}
