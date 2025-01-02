package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.pool.StructurePoolElement;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoolStructurePiece.class)
public class PoolStructurePieceMixin {
   
   @Shadow
   @Final
   protected StructurePoolElement poolElement;
   
   @Inject(method = "writeNbt", at = @At(value = "TAIL"))
   private void fixPoolElement(StructureContext context, NbtCompound nbt, CallbackInfo ci){
      if(!nbt.contains("pool_element")){
         NbtCompound nbtElement = new NbtCompound();
         String poolId = poolElement.toString();
         String poolId2 = poolId.substring(0,poolId.length()-2);
         String split = "\\[";
         String[] poolIdArray = poolId2.split(split);
         String poolLocation = poolIdArray[poolIdArray.length - 1];
         Triple<String, String, String> info = FabricStructurePoolRegistry.getPoolStructureElementInfo(poolLocation);
         if(info != null){
            nbtElement.putString("element_type", info.getLeft());
            nbtElement.putString("location", poolLocation);
            nbtElement.putString("processors", info.getMiddle());
            nbtElement.putString("projection", info.getRight());
            nbt.put("pool_element", nbtElement);
         }
      }
   }
   
}