package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

@Mixin(SculkShriekerBlockEntity.class)
public class SculkShriekerBlockEntityMixin {
   
   @Inject(method = "shriek(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void arcananovum_cancelShriek(ServerWorld world, Entity entity, CallbackInfo ci) {
      SculkShriekerBlockEntity blockEntity = (SculkShriekerBlockEntity) (Object) this;
      BlockPos pos = blockEntity.getPos();
   
      List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
      for(MagicBlock block : blocks){
         BlockPos magicPos = block.getPos();
         if(pos.equals(magicPos) && block.getData().getString("id").equals(MagicItems.SPAWNER_INFUSER.getId())){
            ci.cancel();
            break;
         }
      }
   }
}
