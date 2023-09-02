package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobSpawnerBlockEntity.class)
public class MobSpawnerBlockEntityMixin {

   @Inject(method="serverTick", at = @At(value="HEAD"), cancellable = true)
   private static void arcananovum_infuseSpawner(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity, CallbackInfo ci){
      try{
         BlockPos infuserPos = blockEntity.getPos().add(0,-2,0);
         
         BlockState infuserState = world.getBlockState(infuserPos);
         BlockEntity be = world.getBlockEntity(infuserPos);
         if(be instanceof SpawnerInfuserBlockEntity infuser){
            if(infuser.isActive()){
               infuser.tickInfuser(pos,blockEntity);
               ci.cancel(); // Cancel to avoid double ticking
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
