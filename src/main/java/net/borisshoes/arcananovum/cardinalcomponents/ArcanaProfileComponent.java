package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class ArcanaProfileComponent implements IArcanaProfileComponent{
   public static final String ADMIN_SKILL_POINTS_TAG = "adminSkillPoints";
   public static final String CONCENTRATION_TICK_TAG = "concentration";
   
   private final PlayerEntity player;
   private final List<String> crafted = new ArrayList<>();
   private final List<String> researchedItems = new ArrayList<>();
   private final List<String> researchTasks = new ArrayList<>();
   private final HashMap<String, NbtElement> miscData = new HashMap<>();
   private final HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
   private final HashMap<String,List<ArcanaAchievement>> achievements = new HashMap<>();
   private int level;
   private int xp;
   private ItemStack storedOffhand = ItemStack.EMPTY;
   
   public ArcanaProfileComponent(PlayerEntity player){
      this.player = player;
   }
   
   @Override
   public void readData(ReadView readView){
      crafted.clear();
      miscData.clear();
      achievements.clear();
      augments.clear();
      researchedItems.clear();
      researchTasks.clear();
      readView.read("crafted", CodecUtils.STRING_LIST).ifPresent(crafted::addAll);
      readView.read("researchedItems", CodecUtils.STRING_LIST).ifPresent(researchedItems::addAll);
      readView.read("researchTasks", CodecUtils.STRING_LIST).ifPresent(researchTasks::addAll);
      NbtCompound miscDataTag = readView.read("miscData",NbtCompound.CODEC).orElse(new NbtCompound());
      Set<String> keys = miscDataTag.getKeys();
      keys.forEach(key ->{
         miscData.put(key,miscDataTag.get(key));
      });
      level = readView.getInt("level", 0);
      xp = readView.getInt("xp", 0);
      
      NbtCompound achievementsTag = readView.read("achievements",NbtCompound.CODEC).orElse(new NbtCompound());
      Set<String> achieveItemKeys = achievementsTag.getKeys();
      for(String itemKey : achieveItemKeys){
         List<ArcanaAchievement> itemAchs = new ArrayList<>();
         NbtCompound itemAchsTag = achievementsTag.getCompoundOrEmpty(itemKey);
   
         for(String achieveKey : itemAchsTag.getKeys()){
            NbtCompound achTag = itemAchsTag.getCompoundOrEmpty(achieveKey);
            ArcanaAchievement ach = ArcanaAchievements.registry.get(achieveKey);
            if(ach == null) continue;
            itemAchs.add(ach.makeNew().fromNbt(achieveKey,achTag));
         }
         achievements.put(itemKey,itemAchs);
      }
      
      NbtCompound augmentsTag = readView.read("augments",NbtCompound.CODEC).orElse(new NbtCompound());
      Set<String> augmentKeys = augmentsTag.getKeys();
      for(String augmentKey : augmentKeys){
         int augmentLvl = augmentsTag.getInt(augmentKey, 0);
         if(augmentLvl > 0){
            ArcanaAugment aug = ArcanaAugments.registry.get(augmentKey);
            if(aug == null) continue;
            augments.put(ArcanaAugments.registry.get(augmentKey), augmentLvl);
         }
      }
      
      storedOffhand = readView.read("storedOffhand",ItemStack.CODEC).orElse(ItemStack.EMPTY);
   }
   
   @Override
   public void writeData(WriteView writeView){
      NbtCompound miscDataTag = new NbtCompound();
      miscData.forEach(miscDataTag::put);
      writeView.put("crafted",CodecUtils.STRING_LIST,crafted);
      writeView.put("researchedItems",CodecUtils.STRING_LIST,researchedItems);
      writeView.put("researchTasks",CodecUtils.STRING_LIST,researchTasks);
      writeView.put("miscData",NbtCompound.CODEC,miscDataTag);
      writeView.putInt("level",level);
      writeView.putInt("xp",xp);
      
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
      writeView.put("achievements",NbtCompound.CODEC,achievementsTag);
   
      NbtCompound augmentsTag = new NbtCompound();
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         augmentsTag.putInt(entry.getKey().id, entry.getValue());
      }
      writeView.put("augments",NbtCompound.CODEC,augmentsTag);
      if(this.storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty())
         writeView.put("storedOffhand",ItemStack.CODEC,storedOffhand);
   }
   
   @Override
   public List<String> getCrafted(){
      return crafted;
   }
   
   @Override
   public boolean hasCrafted(ArcanaItem arcanaItem){
      return crafted.stream().anyMatch(s -> s.equals(arcanaItem.getId()));
   }
   
   @Override
   public boolean hasResearched(ArcanaItem arcanaItem){
      return researchedItems.stream().anyMatch(s -> s.equals(arcanaItem.getId()));
   }
   
   @Override
   public List<String> getResearchedItems(){
      return researchedItems;
   }
   
   @Override
   public boolean completedResearchTask(String id){
      return researchTasks.stream().anyMatch(s -> s.equals(id));
   }
   
   @Override
   public void setResearchTask(RegistryKey<ResearchTask> key, boolean acquired){
      ResearchTask task = ResearchTasks.RESEARCH_TASKS.get(key);
      if(task == null) return;
      String id = task.getId();
      
      if(acquired){
         if(researchTasks.stream().noneMatch(s -> s.equals(id)))
            researchTasks.add(id);
      }else{
         researchTasks.removeIf(s -> s.equals(id));
      }
   }
   
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
      NbtInt pointsEle = (NbtInt) getMiscData(ArcanaProfileComponent.ADMIN_SKILL_POINTS_TAG);
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
         
         ArcanaRarity[] tiers = augment.getTiers();
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
               
               if(ArcanaConfig.getBoolean(ArcanaRegistry.ANNOUNCE_ACHIEVEMENTS)){
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
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return false;
      String itemId = arcanaItem.getId();
      if(crafted.stream().anyMatch(i -> i.equalsIgnoreCase(itemId))) return false;
      addXP(ArcanaRarity.getFirstCraftXp(arcanaItem.getRarity()));
      return crafted.add(itemId);
   }
   
   @Override
   public boolean addCrafted(ItemStack stack){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return false;
      String itemId = arcanaItem.getId();
      if(crafted.stream().anyMatch(i -> i.equalsIgnoreCase(itemId))) return false;
      if(player instanceof ServerPlayerEntity){
         MinecraftServer server = player.getServer();
         if(server != null){
            MutableText newCraftMsg = Text.literal("")
                  .append(player.getDisplayName()).formatted(Formatting.ITALIC)
                  .append(Text.literal(" has crafted their first ").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC))
                  .append(arcanaItem.getTranslatedName().formatted(Formatting.DARK_PURPLE, Formatting.BOLD, Formatting.ITALIC, Formatting.UNDERLINE))
                  .append(Text.literal("!").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
            server.getPlayerManager().broadcast(newCraftMsg.styled(s -> s.withHoverEvent(new HoverEvent.ShowItem(stack))), false);
         }
      }
      addXP(ArcanaRarity.getFirstCraftXp(arcanaItem.getRarity()));
      return crafted.add(itemId);
   }
   
   @Override
   public boolean setAchievement(String item, ArcanaAchievement achievement){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         boolean removed = itemAchs.removeIf(itemAch -> itemAch.id.equals(achievement.id));
         if(removed){
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

   @Override
   public boolean addResearchedItem(String item){
      if(researchedItems.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
      return researchedItems.add(item);
   }
   
   @Override
   public boolean removeCrafted(String item){
      if(crafted.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
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

   @Override
   public boolean removeResearchedItem(String item){
      if(researchedItems.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return researchedItems.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
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
      if(augments.entrySet().stream().noneMatch(e -> e.getKey().id.equals(id))) return false;
      return augments.entrySet().removeIf(e -> e.getKey().id.equals(id));
   }
   
   @Override
   public void removeAllAugments(){
      augments.clear();
   }
   
   @Override
   public int getArcanePaperRequirement(ArcanaRarity rarity){
      int totalResearched = ArcanaItemUtils.countRarityInList(getResearchedItems(),rarity,false);
      return Math.min(64,(int) (0.025*Math.pow(totalResearched,2.5) + totalResearched + 1));
   }
   
   
   @Override
   public boolean restoreOffhand(){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(storedOffhand.isEmpty()) return false;
      ItemStack offHand = player.getOffHandStack().copy();
      player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT,storedOffhand.copyAndEmpty());
      if(!offHand.isEmpty() && !BinaryBlades.isFakeBlade(offHand)) {
         player.getInventory().offerOrDrop(offHand);
      }
      return true;
   }
   
   @Override
   public boolean storeOffhand(ItemStack replacement){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty()) return false;
      storedOffhand = player.getOffHandStack();
      player.getInventory().setStack(PlayerInventory.OFF_HAND_SLOT,replacement);
      return true;
   }
   
   @Override
   public ItemStack getStoredOffhand(){
      return storedOffhand == null ? ItemStack.EMPTY : storedOffhand;
   }
}
