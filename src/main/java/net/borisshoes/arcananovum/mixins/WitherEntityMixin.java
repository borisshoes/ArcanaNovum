package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherEntity.class)
public class WitherEntityMixin {
   
   @Redirect(method="mobTick",at=@At(value="INVOKE",target="Lnet/minecraft/server/world/ServerWorld;syncGlobalEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
   private void arcananovum_redirectSpawnSound(ServerWorld instance, int eventId, BlockPos pos, int data){
      SoundUtils.playSound(instance,pos, SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE,1,1);
   }
}
