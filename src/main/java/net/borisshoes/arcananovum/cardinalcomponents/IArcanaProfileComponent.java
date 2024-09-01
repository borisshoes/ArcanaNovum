package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.HashMap;
import java.util.List;

public interface IArcanaProfileComponent extends ComponentV3 {
   List<String> getCrafted();
   boolean hasCrafted(ArcanaItem arcanaItem);
   boolean hasResearched(ArcanaItem arcanaItem);
   boolean completedResearchTask(String id);
   List<String> getResearchedItems();
   NbtElement getMiscData(String id);
   HashMap<String,List<ArcanaAchievement>> getAchievements();
   HashMap<ArcanaAugment,Integer> getAugments();
   int getLevel();
   int getXP();
   int getAchievementSkillPoints();
   int getTotalSkillPoints();
   int getSpentSkillPoints();
   int getBonusSkillPoints();
   
   boolean setXP(int xp);
   boolean addXP(int xp);
   boolean setLevel(int lvl);
   boolean addCrafted(ItemStack stack);
   boolean addCraftedSilent(ItemStack stack);
   boolean setAchievement(String item, ArcanaAchievement achievement);
   boolean addResearchedItem(String item);
   boolean removeCrafted(String item);
   boolean removeAchievement(String item, String achievementId);
   boolean removeResearchedItem(String item);
   void addMiscData(String id, NbtElement data);
   void removeMiscData(String id);
   void setResearchTask(RegistryKey<ResearchTask> key, boolean acquired);
   boolean hasAcheivement(String item, String achievementId);
   ArcanaAchievement getAchievement(String item, String achievementId);
   int totalAcquiredAchievements();
   int getAugmentLevel(String id);
   boolean setAugmentLevel(String id, int level);
   boolean removeAugment(String id);
   void removeAllAugments();
   int getArcanePaperRequirement(ArcanaRarity rarity);
}
