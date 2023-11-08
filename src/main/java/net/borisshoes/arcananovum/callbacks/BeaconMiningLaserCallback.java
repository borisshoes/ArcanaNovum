package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BeaconMiningLaserCallback extends TickTimerCallback{
   private final ServerWorld world;
   private final BlockPos beaconPos;
   private final BlockPos breakPos;
   
   public BeaconMiningLaserCallback(ServerWorld world, BlockPos beaconPos, BlockPos breakPos){
      super(5, null, null);
      this.world = world;
      this.beaconPos = beaconPos;
      this.breakPos = breakPos;
   }
   
   
   @Override
   public void onTimer(){
      if(world.getBlockState(beaconPos).isOf(Blocks.BEACON)){
         BlockState breakState = world.getBlockState(breakPos);
         if(!(breakState.getOpacity(world, breakPos) < 15 || breakState.isOf(Blocks.BEDROCK))){
            world.breakBlock(breakPos,true);
         }
         if(world.isInBuildLimit(breakPos.up())){
            ArcanaNovum.addTickTimerCallback(world,new BeaconMiningLaserCallback(world,beaconPos,breakPos.up()));
         }
      }
   }
}
