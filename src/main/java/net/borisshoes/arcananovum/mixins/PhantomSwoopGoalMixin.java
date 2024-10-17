package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
public abstract class PhantomSwoopGoalMixin extends Goal {
   
   @Final
   @Shadow
   PhantomEntity field_7333; // Outer class synthetic field
   
   @Inject(method = "shouldContinue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSpectator()Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_shouldContinue(CallbackInfoReturnable<Boolean> cir, LivingEntity livingEntity){
      if(livingEntity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
         
            boolean isArcane = ArcanaItemUtils.isArcane(item);
            if(!isArcane)
               continue; // Item not arcane, skip
         
            if(ArcanaItemUtils.identifyItem(item) instanceof FelidaeCharm || ArcanistsBelt.checkBeltAndHasItem(item, ArcanaRegistry.FELIDAE_CHARM.getItem())){
               if(field_7333 instanceof DragonPhantomEntity){
                  return; // Guardian Phantoms immune to Felidae Charm
               }
               
               SoundUtils.playSongToPlayer(player,SoundEvents.ENTITY_CAT_HISS, .1f, 1);
               ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.FELIDAE_CHARM_SCARE_PHANTOM)); // Add xp
               cir.setReturnValue(false);
            }
         }
      }
   }
   
   @Inject(method = "shouldContinue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/CatEntity;hiss()V"), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_phantomScare(CallbackInfoReturnable<Boolean> cir, @Local CatEntity catEntity){
      if(catEntity.getWorld() instanceof ServerWorld serverWorld){
         for(ServerPlayerEntity player : serverWorld.getPlayers(player -> player.getBlockPos().isWithinDistance(catEntity.getBlockPos(), 10.0))){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.CAT_SCARE, true);
         }
      }
   }
}
