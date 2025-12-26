package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
   
   @Final
   @Shadow
   private Entity entity;
   
   @Inject(method = "addPairing", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.BEFORE))
   private void arcananovum$stopInvisTracking(ServerPlayer player, CallbackInfo ci, @Local List<?> list){
      if(entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
         list.clear();
      }
   }
}
