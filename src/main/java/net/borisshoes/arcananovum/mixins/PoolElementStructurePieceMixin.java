package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.world.structures.FabricStructurePoolRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PoolElementStructurePiece.class)
public class PoolElementStructurePieceMixin {
   
   @Shadow
   @Final
   protected StructurePoolElement element;
   
   @Inject(method = "addAdditionalSaveData", at = @At(value = "TAIL"))
   private void fixPoolElement(StructurePieceSerializationContext context, CompoundTag nbt, CallbackInfo ci){
      if(!nbt.contains("pool_element")){
         CompoundTag nbtElement = new CompoundTag();
         String poolId = element.toString();
         String poolId2 = poolId.substring(0, poolId.length() - 2);
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