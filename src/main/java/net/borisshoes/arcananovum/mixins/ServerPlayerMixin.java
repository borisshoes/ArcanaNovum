package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.SojournerBoots;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {
   
   @Inject(method="teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/server/network/ServerPlayerEntity;worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", shift= At.Shift.AFTER))
   private void arcananovum_sendAbilitiesAfterDimChange(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci){
      ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
      player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
   }
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSprinting()Z",shift = At.Shift.BEFORE))
   private void arcananovum_onGroundMove(double dx, double dy, double dz, CallbackInfo ci){
      PlayerEntity playerEntity = (PlayerEntity) (Object) this;
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack bootsItem = player.getEquippedStack(EquipmentSlot.FEET);
         if(MagicItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
            boots.attemptStepAssist(bootsItem,player, new Vec3d(dx,dy,dz));
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
}
