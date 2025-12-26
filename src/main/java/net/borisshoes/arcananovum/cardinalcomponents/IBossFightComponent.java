package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.bosses.BossFights;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tuple;
import org.ladysnake.cca.api.v3.component.ComponentV3;

public interface IBossFightComponent extends ComponentV3 {
   boolean setBossFight(BossFights boss, CompoundTag data);
   boolean removeBossFight();
   Tuple<BossFights, CompoundTag> getBossFight();
}