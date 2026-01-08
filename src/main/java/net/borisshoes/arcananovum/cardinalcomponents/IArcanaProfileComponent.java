package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.ladysnake.cca.api.v3.component.ComponentV3;

import java.util.HashMap;
import java.util.List;

public interface IArcanaProfileComponent extends ComponentV3 {
   List<String> getCrafted();
   boolean hasCrafted(ArcanaItem arcanaItem);
   boolean hasResearched(ArcanaItem arcanaItem);
   boolean completedResearchTask(String id);
   List<String> getResearchedItems();
   Tag getMiscData(String id);
   HashMap<String,Tag> getMiscDataMap();
   HashMap<String,List<ArcanaAchievement>> getAchievements();
   HashMap<ArcanaAugment,Integer> getAugments();
   int getLevel();
   int getXP();
   int getAchievementSkillPoints();
   int getTotalSkillPoints();
   int getSpentSkillPoints();
   int getBonusSkillPoints();
   ItemStack getStoredOffhand();
   
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
   void addMiscData(String id, Tag data);
   void removeMiscData(String id);
   void setResearchTask(ResourceKey<ResearchTask> key, boolean acquired);
   boolean hasAcheivement(String item, String achievementId);
   ArcanaAchievement getAchievement(String item, String achievementId);
   int totalAcquiredAchievements();
   int getAugmentLevel(String id);
   boolean setAugmentLevel(String id, int level);
   boolean removeAugment(String id);
   void removeAllAugments();
   int getArcanePaperRequirement(ArcanaRarity rarity);
   boolean storeOffhand(ItemStack replacement);
   boolean restoreOffhand();
}
