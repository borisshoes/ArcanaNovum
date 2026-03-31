package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnerBlockEntity.class)
public class SpawnerBlockEntityMixin {
   
   @Inject(method = "serverTick", at = @At(value = "HEAD"), cancellable = true)
   private static void arcananovum$infuseSpawner(Level world, BlockPos pos, BlockState state, SpawnerBlockEntity blockEntity, CallbackInfo ci){
      try{
         BlockPos infuserPos = blockEntity.getBlockPos().offset(0, -2, 0);
         
         BlockState infuserState = world.getBlockState(infuserPos);
         BlockEntity be = world.getBlockEntity(infuserPos);
         if(be instanceof SpawnerInfuserBlockEntity infuser){
            if(infuser.isActive()){
               infuser.tickInfuser(pos, blockEntity);
               ci.cancel(); // Cancel to avoid double ticking
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
