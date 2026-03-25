package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class ArcanaProfileComponent implements IArcanaProfileComponent{
   private final Player player;
   private final List<String> crafted = new ArrayList<>();
   private final List<String> researchedItems = new ArrayList<>();
   private final List<String> researchTasks = new ArrayList<>();
   private final HashMap<String, Tag> miscData = new HashMap<>();
   private final HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
   private final HashMap<String,List<ArcanaAchievement>> achievements = new HashMap<>();
   private int level;
   private int xp;
   private ItemStack storedOffhand = ItemStack.EMPTY;
   
   public ArcanaProfileComponent(Player player){
      this.player = player;
   }
   
   @Override
   public void readData(ValueInput readView){
      crafted.clear();
      miscData.clear();
      achievements.clear();
      augments.clear();
      researchedItems.clear();
      researchTasks.clear();
      readView.read("crafted", CodecUtils.STRING_LIST).ifPresent(crafted::addAll);
      readView.read("researchedItems", CodecUtils.STRING_LIST).ifPresent(researchedItems::addAll);
      readView.read("researchTasks", CodecUtils.STRING_LIST).ifPresent(researchTasks::addAll);
      CompoundTag miscDataTag = readView.read("miscData", CompoundTag.CODEC).orElse(new CompoundTag());
      Set<String> keys = miscDataTag.keySet();
      keys.forEach(key ->{
         miscData.put(key,miscDataTag.get(key));
      });
      level = readView.getIntOr("level", 0);
      xp = readView.getIntOr("xp", 0);
      
      CompoundTag achievementsTag = readView.read("achievements", CompoundTag.CODEC).orElse(new CompoundTag());
      Set<String> achieveItemKeys = achievementsTag.keySet();
      for(String itemKey : achieveItemKeys){
         List<ArcanaAchievement> itemAchs = new ArrayList<>();
         CompoundTag itemAchsTag = achievementsTag.getCompoundOrEmpty(itemKey);
   
         for(String achieveKey : itemAchsTag.keySet()){
            CompoundTag achTag = itemAchsTag.getCompoundOrEmpty(achieveKey);
            ArcanaAchievement ach = ArcanaAchievements.ARCANA_ACHIEVEMENTS.get(achieveKey);
            if(ach == null) continue;
            itemAchs.add(ach.makeNew().fromNbt(achieveKey,achTag));
         }
         achievements.put(itemKey,itemAchs);
      }
      
      CompoundTag augmentsTag = readView.read("augments", CompoundTag.CODEC).orElse(new CompoundTag());
      Set<String> augmentKeys = augmentsTag.keySet();
      for(String augmentKey : augmentKeys){
         int augmentLvl = augmentsTag.getIntOr(augmentKey, 0);
         if(augmentLvl > 0){
            ArcanaAugment aug = ArcanaAugments.registry.get(augmentKey);
            if(aug == null) continue;
            augments.put(ArcanaAugments.registry.get(augmentKey), augmentLvl);
         }
      }
      
      storedOffhand = readView.read("storedOffhand", ItemStack.CODEC).orElse(ItemStack.EMPTY);
   }
   
   @Override
   public void writeData(ValueOutput writeView){
      CompoundTag miscDataTag = new CompoundTag();
      miscData.forEach(miscDataTag::put);
      writeView.store("crafted",CodecUtils.STRING_LIST,crafted);
      writeView.store("researchedItems",CodecUtils.STRING_LIST,researchedItems);
      writeView.store("researchTasks",CodecUtils.STRING_LIST,researchTasks);
      writeView.store("miscData", CompoundTag.CODEC,miscDataTag);
      writeView.putInt("level",level);
      writeView.putInt("xp",xp);
      
      CompoundTag achievementsTag = new CompoundTag();
      for(Map.Entry<String, List<ArcanaAchievement>> entry : achievements.entrySet()){
         String item = entry.getKey();
         List<ArcanaAchievement> itemAchs = entry.getValue();
         CompoundTag itemAchsTag = new CompoundTag();
         for(ArcanaAchievement itemAch : itemAchs){
            itemAchsTag.put(itemAch.id,itemAch.toNbt());
         }
         achievementsTag.put(item,itemAchsTag);
      }
      writeView.store("achievements", CompoundTag.CODEC,achievementsTag);
   
      CompoundTag augmentsTag = new CompoundTag();
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         augmentsTag.putInt(entry.getKey().id, entry.getValue());
      }
      writeView.store("augments", CompoundTag.CODEC,augmentsTag);
      if(this.storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty())
         writeView.store("storedOffhand", ItemStack.CODEC,storedOffhand);
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
   public void setResearchTask(ResourceKey<ResearchTask> key, boolean acquired){
      ResearchTask task = ResearchTasks.RESEARCH_TASKS.getValue(key);
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
   public Tag getMiscData(String id){
      return miscData.get(id);
   }
   
   @Override
   public HashMap<String, Tag> getMiscDataMap(){
      return miscData;
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
      IntTag pointsEle = (IntTag) getMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG);
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
            counted.addAll(ArcanaAugments.getLinkedAugments(augment));
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
      if(!(player instanceof ServerPlayer serverPlayer)) return false;
      if(getLevel() == 100 && this.xp + xp < 1000000000){
         this.xp += xp;
         return true;
      }
      
      int newLevel = LevelUtils.levelFromXp(this.xp+xp);
      if(getLevel() != newLevel){
         if(getLevel()/5 < newLevel/5){
            MinecraftServer server = player.level().getServer();
            List<MutableComponent> msgs = new ArrayList<>();
            
            if(server != null){
               if(newLevel/5 * 5 == 100){
                  MutableComponent playerName = Component.literal("").append(player.getDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);
                  
                  msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));
                  msgs.add(Component.literal("")
                        .append(Component.literal("=== ").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK))
                        .append(playerName)
                        .append(Component.literal(" has reached Arcana Level ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                        .append(Component.literal(Integer.toString(newLevel/5 * 5)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                        .append(Component.literal("!!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                        .append(Component.literal(" ===").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK)));
                  msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));
               }else{
                  MutableComponent lvlUpMsg = Component.literal("")
                        .append(player.getDisplayName()).withStyle(ChatFormatting.ITALIC)
                        .append(Component.literal(" has reached Arcana Level ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                        .append(Component.literal(Integer.toString(newLevel/5 * 5)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                        .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                  server.getPlayerList().broadcastSystemMessage(lvlUpMsg, false);
               }
               
               if(ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.ANNOUNCE_ACHIEVEMENTS)){
                  for(MutableComponent msg : msgs){
                     server.getPlayerList().broadcastSystemMessage(msg, false);
                  }
               }else{
                  for(MutableComponent msg : msgs){
                     player.displayClientMessage(msg, false);
                  }
               }
            }
         }
         
         SoundUtils.playSongToPlayer(serverPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
         int resolve = getAugmentLevel(ArcanaAugments.RESOLVE.id);
         player.displayClientMessage(Component.literal(""),false);
         player.displayClientMessage(Component.literal("Your Arcana has levelled up to level "+newLevel+"!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),false);
         player.displayClientMessage(Component.literal("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel,resolve)+"!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),false);
         player.displayClientMessage(Component.literal(""),false);
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
      if(player instanceof ServerPlayer){
         MinecraftServer server = player.level().getServer();
         if(server != null){
            MutableComponent newCraftMsg = Component.literal("")
                  .append(player.getDisplayName()).withStyle(ChatFormatting.ITALIC)
                  .append(Component.literal(" has crafted their first ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(arcanaItem.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            server.getPlayerList().broadcastSystemMessage(newCraftMsg.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowItem(stack))), false);
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
      return found;
   }

   @Override
   public boolean removeResearchedItem(String item){
      if(researchedItems.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return researchedItems.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   @Override
   public void addMiscData(String id, Tag data){
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
         return setLinkedAugmentLevel(baseAugment,level);
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
   
   private boolean setLinkedAugmentLevel(ArcanaAugment aug, int level){
      List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(aug);
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
      ItemStack offHand = player.getOffhandItem().copy();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND,storedOffhand.copyAndClear());
      if(!offHand.isEmpty() && !BinaryBlades.isFakeBlade(offHand)) {
         player.getInventory().placeItemBackInInventory(offHand);
      }
      return true;
   }
   
   @Override
   public boolean storeOffhand(ItemStack replacement){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty()) return false;
      storedOffhand = player.getOffhandItem();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND,replacement);
      return true;
   }
   
   @Override
   public ItemStack getStoredOffhand(){
      return storedOffhand == null ? ItemStack.EMPTY : storedOffhand;
   }
}
