package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.SojournerBoots;
import net.borisshoes.arcananovum.research.EffectResearchTask;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
   
//   @Inject(method="teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", shift= At.Shift.AFTER))
//   private void arcananovum_sendAbilitiesAfterDimChange(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci){
//      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//      player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
//   }
   // this may have been fixed? Test max HP armor and harness flying
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSprinting()Z",shift = At.Shift.BEFORE))
   private void arcananovum_onGroundMove(double dx, double dy, double dz, CallbackInfo ci){
      PlayerEntity playerEntity = (PlayerEntity) (Object) this;
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack bootsItem = player.getEquippedStack(EquipmentSlot.FEET);
         if(ArcanaItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
            if(player.isSprinting()){
               int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
               ArcanaAchievements.progress(player, ArcanaAchievements.PHEIDIPPIDES.id, i);
            }
         }
      }
   }
   
   @Inject(method = "tick", at = @At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;tickFallStartPos()V", shift = At.Shift.BEFORE))
   private void arcananovum_ensnarementMovement(CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      if(player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         player.move(MovementType.PLAYER,player.getVelocity());
      }
   }
   
   @Inject(method = "onStatusEffectApplied", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/EffectsChangedCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;)V"))
   private void arcananovum_effectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci){
      // Effect Research Task Check
      for(Map.Entry<RegistryKey<ResearchTask>, ResearchTask> entry : ResearchTasks.RESEARCH_TASKS.getEntrySet()){
         ResearchTask task = entry.getValue();
         if(task instanceof EffectResearchTask effectTask){
            if(effect.getEffectType() == effectTask.getEffect()){
               ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
               PLAYER_DATA.get(player).setResearchTask(entry.getKey(), true);
            }
         }
      }
   }
   
   @Inject(method = "teleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getLevelProperties()Lnet/minecraft/world/WorldProperties;"))
   private void arcananovum_teleportDimensionChange(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      PLAYER_DATA.get(player).setResearchTask(ResearchTasks.DIMENSION_TRAVEL, true);
   }
}
