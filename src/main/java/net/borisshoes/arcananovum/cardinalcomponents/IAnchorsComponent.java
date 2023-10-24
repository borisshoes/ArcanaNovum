package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IAnchorsComponent extends ComponentV3 {
   List<BlockPos> getAnchors();
   boolean addAnchor(BlockPos anchor);
   boolean removeAnchor(BlockPos anchor);
}
