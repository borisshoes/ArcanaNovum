package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.bosses.BossFights;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import org.ladysnake.cca.api.v3.component.ComponentV3;

public interface IBossFightComponent extends ComponentV3 {
   boolean setBossFight(BossFights boss, NbtCompound data);
   boolean removeBossFight();
   Pair<BossFights,NbtCompound> getBossFight();
}