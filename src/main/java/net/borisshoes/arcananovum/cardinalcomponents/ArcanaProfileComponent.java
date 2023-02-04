package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ArcanaProfileComponent implements IArcanaProfileComponent{
   private final PlayerEntity player;
   private final List<String> crafted = new ArrayList<>();
   //private final List<String> recipes = new ArrayList<>();
   private final HashMap<String, NbtElement> miscData = new HashMap<>();
   private final HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
   private final HashMap<String,List<ArcanaAchievement>> achievements = new HashMap<>();
   private int level;
   private int xp;
   
   public ArcanaProfileComponent(PlayerEntity player){
      this.player = player;
   }
   
   @Override
   public void readFromNbt(NbtCompound tag){
      crafted.clear();
      miscData.clear();
      achievements.clear();
      augments.clear();
      //recipes.clear();
      tag.getList("crafted", NbtType.STRING).forEach(item -> crafted.add(item.asString()));
      //tag.getList("recipes", NbtType.STRING).forEach(item -> recipes.add(item.asString()));
      NbtCompound miscDataTag = tag.getCompound("miscData");
      Set<String> keys = miscDataTag.getKeys();
      keys.forEach(key ->{
         miscData.put(key,miscDataTag.get(key));
      });
      level = tag.getInt("level");
      xp = tag.getInt("xp");
      
      NbtCompound achievementsTag = tag.getCompound("achievements");
      Set<String> achieveItemKeys = achievementsTag.getKeys();
      for(String itemKey : achieveItemKeys){
         List<ArcanaAchievement> itemAchs = new ArrayList<>();
         NbtCompound itemAchsTag = achievementsTag.getCompound(itemKey);
   
         for(String achieveKey : itemAchsTag.getKeys()){
            NbtCompound achTag = itemAchsTag.getCompound(achieveKey);
            itemAchs.add(ArcanaAchievements.registry.get(achieveKey).fromNbt(achieveKey,achTag));
         }
         achievements.put(itemKey,itemAchs);
      }
      
      NbtCompound augmentsTag = tag.getCompound("augments");
      Set<String> augmentKeys = augmentsTag.getKeys();
      for(String augmentKey : augmentKeys){
         int augmentLvl = augmentsTag.getInt(augmentKey);
         if(augmentLvl > 0) augments.put(ArcanaAugments.registry.get(augmentKey),augmentLvl);
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      NbtList craftedTag = new NbtList();
      NbtList recipesTag = new NbtList();
      NbtCompound miscDataTag = new NbtCompound();
      crafted.forEach(item -> {
         craftedTag.add(NbtString.of(item));
      });
//      recipes.forEach(item -> {
//         recipesTag.add(NbtString.of(item));
//      });
      miscData.forEach(miscDataTag::put);
      tag.put("crafted",craftedTag);
      tag.put("recipes",recipesTag);
      tag.put("miscData",miscDataTag);
      tag.putInt("level",level);
      tag.putInt("xp",xp);
      
      NbtCompound achievementsTag = new NbtCompound();
      for(Map.Entry<String, List<ArcanaAchievement>> entry : achievements.entrySet()){
         String item = entry.getKey();
         List<ArcanaAchievement> itemAchs = entry.getValue();
         NbtCompound itemAchsTag = new NbtCompound();
         for(ArcanaAchievement itemAch : itemAchs){
            itemAchsTag.put(itemAch.id,itemAch.toNbt());
         }
         achievementsTag.put(item,itemAchsTag);
      }
      tag.put("achievements",achievementsTag);
   
      NbtCompound augmentsTag = new NbtCompound();
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         augmentsTag.putInt(entry.getKey().id, entry.getValue());
      }
      tag.put("augments",augmentsTag);
   }
   
   @Override
   public List<String> getCrafted(){
      return crafted;
   }
   
//   @Override
//   public List<String> getRecipes(){
//      return recipes;
//   }
   
   @Override
   public NbtElement getMiscData(String id){
      return miscData.get(id);
   }
   
   @Override
   public HashMap<String, List<ArcanaAchievement>> getAchievements(){
      return achievements;
   }
   
   @Override
   public HashMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   @Override
   public int getLevel(){
      return level;
   }
   
   @Override
   public int getXP(){
      return xp;
   }
   
   @Override
   public int getAchievementSkillPoints(){
      int points = 0;
      for(Map.Entry<String, List<ArcanaAchievement>> entry : achievements.entrySet()){
         List<ArcanaAchievement> achieves = entry.getValue();
         for(ArcanaAchievement achieve : achieves){
            if(achieve.isAcquired()) points += achieve.pointsReward;
         }
      }
      return points;
   }
   
   @Override
   public int getTotalSkillPoints(){
      return getAchievementSkillPoints() + LevelUtils.getLevelSkillPoints(level);
   }
   
   @Override
   public int getSpentSkillPoints(){
      int spent = 0;
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         ArcanaAugment augment = entry.getKey();
         MagicRarity[] tiers = augment.getTiers();
         for(int i = 0; i < entry.getValue(); i++){
            spent += tiers[i].rarity + 1;
         }
      }
      return spent;
   }
   
   @Override
   public boolean addXP(int xp){
      if(getLevel() == 100 && this.xp + xp < 1000000000){
         this.xp += xp;
         return true;
      }
      
      int newLevel = LevelUtils.levelFromXp(this.xp+xp);
      if(player instanceof ServerPlayerEntity && getLevel() != newLevel){
         if(getLevel()/5 < newLevel/5){
            MinecraftServer server = player.getServer();
            
            if(server != null){
               if(newLevel/5 * 5 == 100){
                  MutableText playerName = Text.literal("").append(player.getDisplayName()).formatted(Formatting.BOLD, Formatting.UNDERLINE);
                  
                  MutableText lvlUpMsg = Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.DARK_PURPLE);
                  server.getPlayerManager().broadcast(lvlUpMsg, false);
                  
                  lvlUpMsg = Text.literal("")
                        .append(Text.literal("=== ").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK))
                        .append(playerName)
                        .append(Text.literal(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal("!!!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal(" ===").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK));
                  server.getPlayerManager().broadcast(lvlUpMsg, false);
   
                  lvlUpMsg = Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.DARK_PURPLE);
                  server.getPlayerManager().broadcast(lvlUpMsg, false);
               }else{
                  MutableText lvlUpMsg = Text.literal("")
                        .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                        .append(Text.literal(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                        .append(Text.literal(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC, Formatting.UNDERLINE))
                        .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC));
                  server.getPlayerManager().broadcast(lvlUpMsg, false);
               }
            }
         }
         SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
         player.sendMessage(Text.literal(""),false);
         player.sendMessage(Text.literal("Your Arcana has levelled up to level "+newLevel+"!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),false);
         player.sendMessage(Text.literal("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel)+"!").formatted(Formatting.AQUA,Formatting.ITALIC),false);
         player.sendMessage(Text.literal(""),false);
      }
      this.xp += xp;
      this.level = newLevel;
      return true;
   }
   
   @Override
   public boolean setXP(int xp){
      this.xp = xp;
      setLevel(LevelUtils.levelFromXp(this.xp));
      return true;
   }
   
   @Override
   public boolean setLevel(int lvl){
      this.level = lvl;
      return true;
   }
   
   @Override
   public boolean addCrafted(String item){
      if (crafted.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
      MagicItem magicItem = MagicItemUtils.getItemFromId(item);
      if(player instanceof ServerPlayerEntity){
         MinecraftServer server = player.getServer();
         if(server != null){
            MutableText newCraftMsg = Text.literal("")
                  .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                  .append(Text.literal(" has crafted their first ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC))
                  .append(Text.literal(magicItem.getName()).formatted(Formatting.DARK_PURPLE, Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            server.getPlayerManager().broadcast(newCraftMsg, false);
         }
      }
      addXP(MagicRarity.getFirstCraftXp(magicItem.getRarity()));
      return crafted.add(item);
   }
   
   @Override
   public boolean setAchievement(String item, ArcanaAchievement achievement){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(achievement.id)){
               // Update data
               itemAchs.remove(itemAch);
               itemAchs.add(achievement);
               return false;
            }
         }
         // Add achievement
         itemAchs.add(achievement);
      }else{
         // Add item and achievement
         List<ArcanaAchievement> itemAchs = new ArrayList<>();
         itemAchs.add(achievement);
         achievements.put(item,itemAchs);
      }
      return true;
   }

//   @Override
//   public boolean addRecipe(String item){
//      if (recipes.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
//      return recipes.add(item);
//   }
   
   @Override
   public boolean removeCrafted(String item){
      if (crafted.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return crafted.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   @Override
   public boolean removeAchievement(String item, String achievementId){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(achievementId)){
               itemAchs.remove(itemAch);
               return true;
            }
         }
      }
      return false;
   }

//   @Override
//   public boolean removeRecipe(String item){
//      if (recipes.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
//      return recipes.removeIf(i -> i.equalsIgnoreCase(item));
//   }
   
   @Override
   public void addMiscData(String id, NbtElement data){
      miscData.put(id,data);
   }
   
   @Override
   public void removeMiscData(String id){
      miscData.remove(id);
   }
   
   @Override
   public boolean hasAcheivement(String item, String achievementId){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(achievementId)){
               return itemAch.isAcquired();
            }
         }
      }
      return false;
   }
   
   @Override
   public ArcanaAchievement getAchievement(String item, String achievementId){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(achievementId)){
               return itemAch;
            }
         }
      }
      return null;
   }
   
   @Override
   public int getAugmentLevel(String id){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)){
            return entry.getValue();
         }
      }
      return 0;
   }
   
   // Returns whether the player already had that augment or not
   @Override
   public boolean setAugmentLevel(String id, int level){
      ArcanaAugment baseAugment = ArcanaAugments.registry.get(id);
      if(baseAugment == null) return false;
      if(level < 0 || baseAugment.getTiers().length < level) return false;
      
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)){
            entry.setValue(level);
            return true;
         }
      }
      augments.put(ArcanaAugments.registry.get(id),level);
      return false;
   }
   
   // Returns if the operation was successful or not
   @Override
   public boolean removeAugment(String id){
      if (augments.entrySet().stream().noneMatch(e -> e.getKey().id.equals(id))) return false;
      return augments.entrySet().removeIf(e -> e.getKey().id.equals(id));
   }
   
   @Override
   public void removeAllAugments(){
      augments.clear();
   }
   
}
