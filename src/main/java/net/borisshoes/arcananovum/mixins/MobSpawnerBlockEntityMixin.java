package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.SpawnerInfuser;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

@Mixin(MobSpawnerBlockEntity.class)
public class MobSpawnerBlockEntityMixin {

   @Inject(method="serverTick", at = @At(value="HEAD"), cancellable = true)
   private static void arcananovum_infuseSpawner(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity, CallbackInfo ci){
      try{
         BlockPos infuserPos = blockEntity.getPos().add(0,-2,0);

         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
         for(MagicBlock block : blocks){
            BlockPos magicPos = block.getPos();
            if(infuserPos.equals(magicPos) && block.getData().getString("id").equals(MagicItems.SPAWNER_INFUSER.getId())){
               NbtCompound blockData = block.getData();
               boolean active = blockData.getBoolean("active");
               if(active){
                  SpawnerInfuser.tickActiveInfuser(world,pos,blockData,blockEntity);
                  ci.cancel(); // Cancel to avoid double ticking
               }
               break;
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
