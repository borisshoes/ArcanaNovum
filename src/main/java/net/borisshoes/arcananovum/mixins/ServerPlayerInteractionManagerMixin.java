package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.PickaxeOfCeptyus;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
   
   @Shadow
   private boolean mining;
   
   @Final
   @Shadow
   protected ServerPlayerEntity player;
   
   @Inject(method = "continueMining", at = @At("HEAD"))
   private void arcananovum_continueMining(BlockState state, BlockPos pos, int failedStartMiningTime, CallbackInfoReturnable<Float> cir) {
      if(player != null && mining){
         ItemStack mineStack = player.getMainHandStack();
         if(MagicItemUtils.identifyItem(mineStack) instanceof PickaxeOfCeptyus pick){
            pick.mining(player,mineStack);
         }
      }
   }
}
