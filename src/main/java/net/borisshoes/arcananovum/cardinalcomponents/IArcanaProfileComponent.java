package net.borisshoes.arcananovum.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.minecraft.nbt.NbtElement;

import java.util.HashMap;
import java.util.List;

public interface IArcanaProfileComponent extends ComponentV3 {
   List<String> getCrafted();
   //List<String> getRecipes();
   NbtElement getMiscData(String id);
   HashMap<String,List<ArcanaAchievement>> getAchievements();
   HashMap<ArcanaAugment,Integer> getAugments();
   int getLevel();
   int getXP();
   int getAchievementSkillPoints();
   int getTotalSkillPoints();
   int getSpentSkillPoints();
   
   boolean setXP(int xp);
   boolean addXP(int xp);
   boolean setLevel(int lvl);
   boolean addCrafted(String item);
   boolean setAchievement(String item, ArcanaAchievement achievement);
   //boolean addRecipe(String item);
   boolean removeCrafted(String item);
   boolean removeAchievement(String item, String achievementId);
   //boolean removeRecipe(String item);
   void addMiscData(String id, NbtElement data);
   void removeMiscData(String id);
   boolean hasAcheivement(String item, String achievementId);
   ArcanaAchievement getAchievement(String item, String achievementId);
   int getAugmentLevel(String id);
   boolean setAugmentLevel(String id, int level);
   boolean removeAugment(String id);
}
