package net.borisshoes.arcananovum.cardinalcomponents;

import net.minecraft.util.math.BlockPos;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.List;

public interface IAnchorsComponent extends ComponentV3 {
   List<BlockPos> getAnchors();
   boolean addAnchor(BlockPos anchor);
   boolean removeAnchor(BlockPos anchor);
}
