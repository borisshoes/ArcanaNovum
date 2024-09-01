package net.borisshoes.arcananovum.mixins;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructurePool.class)
public interface StructurePoolAccessor {
   @Accessor(value = "elements")
   ObjectArrayList<StructurePoolElement> getElements();
   
   @Accessor(value = "elementCounts")
   List<Pair<StructurePoolElement, Integer>> getElementCounts();
   
   @Mutable
   @Accessor(value = "elementCounts")
   void setElementCounts(List<Pair<StructurePoolElement, Integer>> list);
}