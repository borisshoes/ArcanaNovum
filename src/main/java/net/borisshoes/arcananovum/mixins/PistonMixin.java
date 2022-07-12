package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicBlocksComponentInitializer.MAGIC_BLOCK_LIST;

@Mixin(PistonBlock.class)
public class PistonMixin {
   
   @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
   private static void moveBlockCheck(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir){
      if(world instanceof ServerWorld serverWorld){
         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(serverWorld).getBlocks();
         for(MagicBlock block : blocks){
            BlockPos magicPos = block.getPos();
            if(pos.equals(magicPos)){
               cir.setReturnValue(false);
               Timer timer = new Timer();
               timer.schedule(new TimerTask() {
                  @Override
                  public void run(){
                     List<ServerPlayerEntity> players = serverWorld.getPlayers(p -> p.squaredDistanceTo(pos.getX(),pos.getY(),pos.getZ()) <= 25000);
                     for(ServerPlayerEntity player : players){
                        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos,state));
                     }
                  }
               }, 151);
               break;
            }
         }
      }
   }
}
