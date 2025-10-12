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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.world.TeleportTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
   
   @Inject(method = "readEnderPearl", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;addEnderPearlTicket(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/ChunkPos;)J"))
   private void arcananovum$readStasisPearl(ReadView view, CallbackInfo ci, @Local Entity entity){
      if(entity instanceof StasisPearlEntity stasisPearl){
         stasisPearl.resyncHolder();
      }
   }
   
   @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onKilledBy(Lnet/minecraft/entity/LivingEntity;)V"))
   private void arcananovum$onEntityKilledOther(DamageSource damageSource, CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      EntityKilledCallback.killedEntity(player.getWorld(),damageSource, damageSource.getAttacker(),player);
   }
   
   @Inject(method="onDeath",at=@At("HEAD"))
   private void arcananovum$restoreOffhandOnDeath(CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      ArcanaNovum.data(player).restoreOffhand();
   }
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V", ordinal = 0))
   private void arcananovum$swimStats(double deltaX, double deltaY, double deltaZ, CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      boolean hasCetacea = ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, null, 0).stream().anyMatch(stack -> ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG));
      if(hasCetacea){
         int i = Math.round((float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 100.0F);
         if(i > 0){
            ArcanaAchievements.progress(player, ArcanaAchievements.OCEAN_MIGRATION.id, i);
            LivingEntity entity = player.getWorld().getClosestEntity(DolphinEntity.class, TargetPredicate.createNonAttackable().setBaseMaxDistance(10.0).ignoreVisibility(), player, player.getX(), player.getY(), player.getZ(), player.getBoundingBox().expand(20.0));
            if(entity != null) ArcanaAchievements.progress(player, ArcanaAchievements.CEPHALOS_IN_A_POD.id, i);
         }
      }
   }
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSprinting()Z",shift = At.Shift.BEFORE))
   private void arcananovum$onGroundMove(double dx, double dy, double dz, CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      ItemStack bootsItem = player.getEquippedStack(EquipmentSlot.FEET);
      if(ArcanaItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
         if(player.isSprinting()){
            int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
            ArcanaAchievements.progress(player, ArcanaAchievements.PHEIDIPPIDES.id, i);
         }
      }
   }
   
   @Inject(method = "tick", at = @At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;tickFallStartPos()V", shift = At.Shift.BEFORE))
   private void arcananovum$ensnarementMovement(CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      if(player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         player.move(MovementType.PLAYER,player.getVelocity());
         player.networkHandler.requestTeleport(new PlayerPosition(player.getPos(), player.getVelocity(), 0, 0), PositionFlag.getFlags(0b11000));
      }
   }
   
   @Inject(method = "onStatusEffectApplied", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/EffectsChangedCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;)V"))
   private void arcananovum$effectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci){
      // Effect Research Task Check
      for(Map.Entry<RegistryKey<ResearchTask>, ResearchTask> entry : ResearchTasks.RESEARCH_TASKS.getEntrySet()){
         ResearchTask task = entry.getValue();
         if(task instanceof EffectResearchTask effectTask){
            if(effect.getEffectType() == effectTask.getEffect()){
               ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
               ArcanaNovum.data(player).setResearchTask(entry.getKey(), true);
            }
         }
      }
   }
   
   @Inject(method = "teleportTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getLevelProperties()Lnet/minecraft/world/WorldProperties;"))
   private void arcananovum$teleportDimensionChange(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      ArcanaNovum.data(player).setResearchTask(ResearchTasks.DIMENSION_TRAVEL, true);
   }
}
