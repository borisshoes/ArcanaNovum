package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class BeaconMiningLaserCallback extends TickTimerCallback {
   private final ServerLevel world;
   private final BlockPos beaconPos;
   private final BlockPos breakPos;
   
   public BeaconMiningLaserCallback(ServerLevel world, BlockPos beaconPos, BlockPos breakPos){
      super(5, null, null);
      this.world = world;
      this.beaconPos = beaconPos;
      this.breakPos = breakPos;
   }
   
   
   @Override
   public void onTimer(){
      if(world.getBlockState(beaconPos).is(Blocks.BEACON)){
         BlockState breakState = world.getBlockState(breakPos);
         if(!(breakState.propagatesSkylightDown() || breakState.is(Blocks.BEDROCK))){
            world.destroyBlock(breakPos, true);
         }
         if(world.isInWorldBounds(breakPos.above())){
            BorisLib.addTickTimerCallback(world, new BeaconMiningLaserCallback(world, beaconPos, breakPos.above()));
         }
      }
   }
}
