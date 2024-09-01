package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.PickaxeOfCeptyus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
         if(ArcanaItemUtils.identifyItem(mineStack) instanceof PickaxeOfCeptyus pick){
            pick.mining(player,mineStack);
         }
      }
   }
   
   @Inject(at = @At("HEAD"), method = "interactItem", cancellable = true)
   public void arcananovum_interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> info) {
      ItemStack mainStack = player.getStackInHand(Hand.MAIN_HAND);
      ItemStack offStack = player.getStackInHand(Hand.OFF_HAND);
      ArcanaItem arcanaItem1 = ArcanaItemUtils.identifyItem(mainStack);
      ArcanaItem arcanaItem2 = ArcanaItemUtils.identifyItem(offStack);
      if(arcanaItem1 != null && arcanaItem1.blocksHandInteractions(mainStack) && hand == Hand.OFF_HAND){
         info.setReturnValue(ActionResult.PASS);
         info.cancel();
         return;
      }
   }
}
