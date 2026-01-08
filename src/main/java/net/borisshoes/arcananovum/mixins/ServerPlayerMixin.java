package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.callbacks.EntityKilledCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.StasisPearlEntity;
import net.borisshoes.arcananovum.items.SojournerBoots;
import net.borisshoes.arcananovum.research.EffectResearchTask;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
   
   @Inject(method = "loadAndSpawnEnderPearl", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;placeEnderPearlTicket(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;)J"))
   private void arcananovum$readStasisPearl(ValueInput view, CallbackInfo ci, @Local Entity entity){
      if(entity instanceof StasisPearlEntity stasisPearl){
         stasisPearl.resyncHolder();
      }
   }
   
   @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;createWitherRose(Lnet/minecraft/world/entity/LivingEntity;)V"))
   private void arcananovum$onEntityKilledOther(DamageSource damageSource, CallbackInfo ci){
      ServerPlayer player = (ServerPlayer) (Object) this;
      EntityKilledCallback.killedEntity(player.level(),damageSource, damageSource.getEntity(),player);
   }
   
   @Inject(method= "die",at=@At("HEAD"))
   private void arcananovum$restoreOffhandOnDeath(CallbackInfo ci){
      ServerPlayer player = (ServerPlayer) (Object) this;
      ArcanaNovum.data(player).restoreOffhand(player);
   }
   
   @Inject(method= "checkMovementStatistics", at = @At(value="INVOKE",target = "Lnet/minecraft/server/level/ServerPlayer;awardStat(Lnet/minecraft/resources/Identifier;I)V", ordinal = 0))
   private void arcananovum$swimStats(double deltaX, double deltaY, double deltaZ, CallbackInfo ci){
      ServerPlayer player = (ServerPlayer) (Object) this;
      boolean hasCetacea = ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, null, 0).stream().anyMatch(stack -> ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG));
      if(hasCetacea){
         int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0F);
         if(i > 0){
            ArcanaAchievements.progress(player, ArcanaAchievements.OCEAN_MIGRATION.id, i);
            LivingEntity entity = player.level().getNearestEntity(Dolphin.class, TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight(), player, player.getX(), player.getY(), player.getZ(), player.getBoundingBox().inflate(20.0));
            if(entity != null) ArcanaAchievements.progress(player, ArcanaAchievements.CEPHALOS_IN_A_POD.id, i);
         }
      }
   }
   
   @Inject(method= "checkMovementStatistics", at = @At(value="INVOKE",target = "Lnet/minecraft/server/level/ServerPlayer;isSprinting()Z",shift = At.Shift.BEFORE))
   private void arcananovum$onGroundMove(double dx, double dy, double dz, CallbackInfo ci){
      ServerPlayer player = (ServerPlayer) (Object) this;
      ItemStack bootsItem = player.getItemBySlot(EquipmentSlot.FEET);
      if(ArcanaItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
         if(player.isSprinting()){
            int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
            ArcanaAchievements.progress(player, ArcanaAchievements.PHEIDIPPIDES.id, i);
         }
      }
   }
   
   @Inject(method = "tick", at = @At(value="INVOKE",target= "Lnet/minecraft/server/level/ServerPlayer;trackStartFallingPosition()V", shift = At.Shift.BEFORE))
   private void arcananovum$ensnarementMovement(CallbackInfo ci){
      ServerPlayer player = (ServerPlayer) (Object) this;
      if(player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         player.move(MoverType.PLAYER,player.getDeltaMovement());
         player.connection.teleport(new PositionMoveRotation(player.position(), player.getDeltaMovement(), 0, 0), Relative.unpack(0b11000));
      }
   }
   
   @Inject(method = "onEffectAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/EffectsChangedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;)V"))
   private void arcananovum$effectApplied(MobEffectInstance effect, Entity source, CallbackInfo ci){
      // Effect Research Task Check
      for(Map.Entry<ResourceKey<ResearchTask>, ResearchTask> entry : ResearchTasks.RESEARCH_TASKS.entrySet()){
         ResearchTask task = entry.getValue();
         if(task instanceof EffectResearchTask effectTask){
            if(effect.getEffect() == effectTask.getEffect()){
               ServerPlayer player = (ServerPlayer) (Object) this;
               ArcanaNovum.data(player).setResearchTask(entry.getKey(), true);
            }
         }
      }
   }
   
   @Inject(method = "teleport", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"))
   private void arcananovum$teleportDimensionChange(TeleportTransition teleportTarget, CallbackInfoReturnable<Entity> cir){
      ServerPlayer player = (ServerPlayer) (Object) this;
      ArcanaNovum.data(player).setResearchTask(ResearchTasks.DIMENSION_TRAVEL, true);
   }
}
