package net.borisshoes.arcananovum.core;

import net.minecraft.core.Vec3i;

public interface MultiblockCore {
   void loadMultiblock();
   
   Multiblock getMultiblock();
   
   Vec3i getCheckOffset();
}
