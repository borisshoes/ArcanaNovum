package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;

import java.util.List;

public interface IBossFightComponent extends ComponentV3 {
   boolean setBossFight(BossFights boss, NbtCompound data);
   boolean removeBossFight();
   Pair<BossFights,NbtCompound> getBossFight();
}