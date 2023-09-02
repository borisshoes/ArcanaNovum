package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.TimerTask;

@Mixin(PistonBlock.class)
public class PistonMixin {
   
   @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
   private static void moveBlockCheck(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir){
      if(world instanceof ServerWorld serverWorld){
         if(ArcanaRegistry.BLOCKS.containsValue(state.getBlock())){
            cir.setReturnValue(false);
            
            Arcananovum.addTickTimerCallback(serverWorld, new GenericTimer(4, new TimerTask() {
               @Override
               public void run(){
                  List<ServerPlayerEntity> players = serverWorld.getPlayers(p -> p.squaredDistanceTo(pos.getX(),pos.getY(),pos.getZ()) <= 25000);
                  for(ServerPlayerEntity player : players){
                     player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos,state));
                     
                     for(Direction direction : Direction.values()){
                        BlockPos blockPos2 = pos.offset(direction);
                        BlockState blockState = world.getBlockState(blockPos2);
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos2, blockState));
                     }
                  }
               }
            }));
         }
      }
   }
}
