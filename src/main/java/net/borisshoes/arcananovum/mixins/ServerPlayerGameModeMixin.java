package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.PickaxeOfCeptyus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
   
   @Shadow
   private boolean isDestroyingBlock;
   
   @Final
   @Shadow
   protected ServerPlayer player;
   
   @Inject(method = "incrementDestroyProgress", at = @At("HEAD"))
   private void arcananovum$continueMining(BlockState state, BlockPos pos, int failedStartMiningTime, CallbackInfoReturnable<Float> cir){
      if(player != null && isDestroyingBlock){
         ItemStack mineStack = player.getMainHandItem();
         if(ArcanaItemUtils.identifyItem(mineStack) instanceof PickaxeOfCeptyus pick && mineStack.has(DataComponents.TOOL)){
            if(mineStack.get(DataComponents.TOOL).getMiningSpeed(state) > mineStack.get(DataComponents.TOOL).defaultMiningSpeed()){
               pick.mining(player, mineStack);
            }
         }
      }
   }
   
   @Inject(at = @At("HEAD"), method = "useItem", cancellable = true)
   public void arcananovum$interactItem(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info){
      ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
      ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
      ArcanaItem arcanaItem1 = ArcanaItemUtils.identifyItem(mainStack);
      ArcanaItem arcanaItem2 = ArcanaItemUtils.identifyItem(offStack);
      if(arcanaItem1 != null && arcanaItem1.blocksHandInteractions(mainStack) && hand == InteractionHand.OFF_HAND){
         info.setReturnValue(InteractionResult.PASS);
         info.cancel();
         return;
      }
   }
}
