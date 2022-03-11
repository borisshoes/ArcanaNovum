package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;

import java.util.List;

public interface IArcanaProfileComponent extends ComponentV3 {
   List<String> getCrafted();
   List<String> getRecipes();
   int getLevel();
   int getXP();
   
   boolean setXP(int xp);
   boolean addXP(int xp);
   boolean setLevel(int lvl);
   boolean addCrafted(String item);
   boolean addRecipe(String item);
   boolean removeCrafted(String item);
   boolean removeRecipe(String item);
}
