package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public interface IMagicBlockComponent extends ComponentV3 {
   List<MagicBlock> getBlocks();
   boolean addBlock(MagicBlock block);
   boolean removeBlock(MagicBlock block);
}
