package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.OverflowingQuiver;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
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
      tag.getList("crafted", NbtElement.STRING_TYPE).forEach(item -> crafted.add(item.asString()));
      //tag.getList("recipes", NbtElement.STRING_TYPE).forEach(item -> recipes.add(item.asString()));
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
            itemAchs.add(ArcanaAchievements.registry.get(achieveKey).makeNew().fromNbt(achieveKey,achTag));
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
   
   @Override
   public boolean hasCrafted(MagicItem magicItem){
      return crafted.stream().anyMatch(s -> s.equals(magicItem.getId()));
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
   public int getBonusSkillPoints(){
      NbtInt pointsEle = (NbtInt) getMiscData("adminSkillPoints");
      return pointsEle == null ? 0 : pointsEle.intValue();
   }
   
   @Override
   public int getTotalSkillPoints(){
      return getAchievementSkillPoints() + LevelUtils.getLevelSkillPoints(level) + getBonusSkillPoints();
   }
   
   @Override
   public int getSpentSkillPoints(){
      int spent = 0;
      List<ArcanaAugment> counted = new ArrayList<>();
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         ArcanaAugment augment = entry.getKey();
         if(counted.contains(augment)) continue;
         if(ArcanaAugments.linkedAugments.containsKey(augment)){
            counted.addAll(ArcanaAugments.getLinkedAugments(augment.id));
         }
         
         MagicRarity[] tiers = augment.getTiers();
         for(int i = 0; i < entry.getValue(); i++){
            spent += tiers[i].rarity + 1;
         }
      }
      return spent;
   }
   
   @Override
   public boolean addXP(int xp){
      if(!(player instanceof ServerPlayerEntity serverPlayer)) return false;
      if(getLevel() == 100 && this.xp + xp < 1000000000){
         this.xp += xp;
         ArcanaNovum.PLAYER_XP_TRACKER.put(serverPlayer.getUuid(),this.xp);
         return true;
      }
      
      int newLevel = LevelUtils.levelFromXp(this.xp+xp);
      if(getLevel() != newLevel){
         if(getLevel()/5 < newLevel/5){
            MinecraftServer server = player.getServer();
            List<MutableText> msgs = new ArrayList<>();
            
            if(server != null){
               if(newLevel/5 * 5 == 100){
                  MutableText playerName = Text.literal("").append(player.getDisplayName()).formatted(Formatting.BOLD, Formatting.UNDERLINE);
                  
                  msgs.add(Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.DARK_PURPLE));
                  msgs.add(Text.literal("")
                        .append(Text.literal("=== ").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK))
                        .append(playerName)
                        .append(Text.literal(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal("!!!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD, Formatting.UNDERLINE))
                        .append(Text.literal(" ===").formatted(Formatting.OBFUSCATED,Formatting.BOLD,Formatting.BLACK)));
                  msgs.add(Text.literal("=============================================").formatted(Formatting.BOLD,Formatting.DARK_PURPLE));
               }else{
                  MutableText lvlUpMsg = Text.literal("")
                        .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                        .append(Text.literal(" has reached Arcana Level ").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC))
                        .append(Text.literal(Integer.toString(newLevel/5 * 5)).formatted(Formatting.DARK_PURPLE,Formatting.BOLD,Formatting.ITALIC, Formatting.UNDERLINE))
                        .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE,Formatting.ITALIC));
                  server.getPlayerManager().broadcast(lvlUpMsg, false);
               }
               
               if((boolean) ArcanaNovum.config.getValue("announceAchievements")){
                  for(MutableText msg : msgs){
                     server.getPlayerManager().broadcast(msg, false);
                  }
               }else{
                  for(MutableText msg : msgs){
                     player.sendMessage(msg, false);
                  }
               }
            }
         }
         
         SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
         int resolve = getAugmentLevel(ArcanaAugments.RESOLVE.id);
         player.sendMessage(Text.literal(""),false);
         player.sendMessage(Text.literal("Your Arcana has levelled up to level "+newLevel+"!").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD),false);
         player.sendMessage(Text.literal("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel,resolve)+"!").formatted(Formatting.AQUA,Formatting.ITALIC),false);
         player.sendMessage(Text.literal(""),false);
      }
      this.xp += xp;
      this.level = newLevel;
      ArcanaNovum.PLAYER_XP_TRACKER.put(serverPlayer.getUuid(),this.xp);
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
   public boolean addCraftedSilent(ItemStack stack){
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      if(magicItem == null) return false;
      String itemId = magicItem.getId();
      if (crafted.stream().anyMatch(i -> i.equalsIgnoreCase(itemId))) return false;
      addXP(MagicRarity.getFirstCraftXp(magicItem.getRarity()));
      return crafted.add(itemId);
   }
   
   @Override
   public boolean addCrafted(ItemStack stack){
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      if(magicItem == null) return false;
      String itemId = magicItem.getId();
      if (crafted.stream().anyMatch(i -> i.equalsIgnoreCase(itemId))) return false;
      if(player instanceof ServerPlayerEntity){
         MinecraftServer server = player.getServer();
         if(server != null){
            MutableText newCraftMsg = Text.literal("")
                  .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                  .append(Text.literal(" has crafted their first ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC))
                  .append(Text.literal(magicItem.getNameString()).formatted(Formatting.DARK_PURPLE, Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            server.getPlayerManager().broadcast(newCraftMsg.styled(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)))), false);
         }
      }
      addXP(MagicRarity.getFirstCraftXp(magicItem.getRarity()));
      return crafted.add(itemId);
   }
   
   @Override
   public boolean setAchievement(String item, ArcanaAchievement achievement){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         boolean removed = itemAchs.removeIf(itemAch -> itemAch.id.equals(achievement.id));
         if(removed) {
            // Update data and return
            itemAchs.add(achievement);
            List<UUID> curList = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
            curList.removeIf(uuid -> uuid.equals(player.getUuid()));
            if(achievement.isAcquired()){
               curList.add(player.getUuid());
            }
            ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achievement.id,curList);
            return false;
         }
         // Add achievement
         itemAchs.add(achievement);
      }else{
         // Add item and achievement
         List<ArcanaAchievement> itemAchs = new ArrayList<>();
         itemAchs.add(achievement);
         achievements.put(item,itemAchs);
      }
      
      List<UUID> curList = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievement.id);
      curList.removeIf(uuid -> uuid.equals(player.getUuid()));
      if(achievement.isAcquired()){
         curList.add(player.getUuid());
      }
      ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achievement.id,curList);
      
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
      boolean found = false;
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(achievementId)){
               itemAchs.remove(itemAch);
               found = true;
               break;
            }
         }
      }
      
      List<UUID> curList = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achievementId);
      curList.removeIf(uuid -> uuid.equals(player.getUuid()));
      ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achievementId,curList);
      
      return found;
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
   public int totalAcquiredAchievements(){
      int count = 0;
      for(Map.Entry<String, List<ArcanaAchievement>> listEntry : achievements.entrySet()){
         for(ArcanaAchievement ach : listEntry.getValue()){
            if(ach.isAcquired()){
               count++;
            }
         }
      }
      return count;
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
      
      if(ArcanaAugments.linkedAugments.containsKey(baseAugment)){
         return setLinkedAugmentLevel(id,level);
      }
      
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)){
            entry.setValue(level);
            return true;
         }
      }
      augments.put(baseAugment,level);
      return false;
   }
   
   private boolean setLinkedAugmentLevel(String id, int level){
      List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(id);
      if(linkedAugments.isEmpty()) return false;
      
      boolean had = false;
      int[] levels = new int[linkedAugments.size()];
      for(int i = 0; i < linkedAugments.size(); i++){
         ArcanaAugment augment = linkedAugments.get(i);
         levels[i] = getAugmentLevel(augment.id);
         
         if(levels[i] != 0){
            had = true;
            for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
               if(entry.getKey().id.equals(augment.id)){
                  entry.setValue(level);
                  break;
               }
            }
         }else{
            augments.put(augment,level);
         }
      }
      
      return had;
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
