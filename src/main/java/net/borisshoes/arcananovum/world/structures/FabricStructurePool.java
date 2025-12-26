package net.borisshoes.arcananovum.world.structures;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public interface FabricStructurePool {
   /**
    * Adds a new {@link StructurePoolElement} to the {@link StructureTemplatePool}.
    * See the alternative {@link #addStructurePoolElement(StructurePoolElement, int)} for details.
    *
    * @param element the element to add
    */
   void addStructurePoolElement(StructurePoolElement element);
   
   /**
    * Adds a new {@link StructurePoolElement} to the {@link StructureTemplatePool}.
    *
    * @param element the element to add
    * @param weight  Minecraft handles weight by adding it that amount of times into a list.}
    */
   void addStructurePoolElement(StructurePoolElement element, int weight);
   
   /**
    * Gets the underlying structure pool.
    */
   StructureTemplatePool getUnderlyingPool();
   
   /**
    * Gets the identifier for the pool.
    */
   Identifier getId();
}