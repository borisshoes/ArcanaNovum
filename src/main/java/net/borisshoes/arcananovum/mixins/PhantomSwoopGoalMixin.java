package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.entities.DragonPhantomEntity;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.charms.FelidaeCharm;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomSweepAttackGoal")
public abstract class PhantomSwoopGoalMixin extends Goal {
   
   @Final
   @Shadow
   Phantom this$0; // Outer class synthetic field
   
   @ModifyReturnValue(method = "canContinueToUse", at = @At(value = "RETURN"))
   private boolean arcananovum$shouldContinue(boolean original){
      if(this$0 instanceof DragonPhantomEntity) return true; // Guardian Phantoms immune to Felidae Charm
      if(original && this$0.getTarget() instanceof ServerPlayer player){
         GeomanticSteleBlockEntity.SteleZone felidaeStele = GeomanticSteleBlockEntity.getZoneAtEntity(player, (item) -> item.is(ArcanaRegistry.FELIDAE_CHARM.getItem()));
         if(felidaeStele != null){
            SoundUtils.playSongToPlayer(player, SoundUtils.getSound("entity.cat.hiss"), .1f, 1);
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_SCARE_PHANTOM)); // Add xp
            felidaeStele.getBlockEntity().giveXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_SCARE_PHANTOM));
            return false;
         }
         
         Inventory inv = player.getInventory();
         for(int i = 0; i < inv.getContainerSize(); i++){
            ItemStack item = inv.getItem(i);
            if(item.isEmpty()){
               continue;
            }
            
            boolean isArcane = ArcanaItemUtils.isArcane(item);
            if(!isArcane)
               continue; // Item not arcane, skip
            
            if(ArcanaItemUtils.identifyItem(item) instanceof FelidaeCharm || ArcanistsBelt.checkBeltAndHasItem(item, ArcanaRegistry.FELIDAE_CHARM.getItem())){
               SoundUtils.playSongToPlayer(player, SoundUtils.getSound("entity.cat.hiss"), .1f, 1);
               ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_FELIDAE_CHARM_SCARE_PHANTOM)); // Add xp
               return false;
            }
         }
      }
      return original;
   }
   
   @Inject(method = "canContinueToUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/feline/Cat;hiss()V"))
   private void arcananovum$phantomScare(CallbackInfoReturnable<Boolean> cir, @Local Cat catEntity){
      if(catEntity.level() instanceof ServerLevel serverWorld){
         for(ServerPlayer player : serverWorld.getPlayers(player -> player.blockPosition().closerThan(catEntity.blockPosition(), 10.0))){
            ArcanaNovum.data(player).setResearchTask(ResearchTasks.CAT_SCARE, true);
         }
      }
   }
}
