package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class BlockUseCallback {
   
   
   public static InteractionResult useBlock(Player player, Level level, InteractionHand interactionHand, BlockHitResult blockHitResult){
      if(!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
      if(interactionHand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
      ItineranteurBlockEntity iter = ArcanaNovum.ITINERANTEUR_USERS.get(serverPlayer);
      if(iter == null) return InteractionResult.PASS;
      if(level.getBlockEntity(iter.getBlockPos()) != iter){
         ArcanaNovum.ITINERANTEUR_USERS.remove(serverPlayer);
         return InteractionResult.PASS;
      }else if(!iter.getBlockPos().equals(blockHitResult.getBlockPos())){
         iter.setSelectedPos(player.isShiftKeyDown() ? null : blockHitResult.getBlockPos());
         return InteractionResult.SUCCESS_SERVER;
      }
      return InteractionResult.PASS;
   }
}
