package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
   
   @Final
   @Shadow
   private Entity entity;
   
   @Inject(method = "startTracking", at= @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.BEFORE))
   private void arcananovum_stopInvisTracking(ServerPlayerEntity player, CallbackInfo ci, @Local List<?> list){
      if(entity instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT)){
         list.clear();
      }
   }
}
