package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.items.BinaryBlades;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ArcanaPlayerData {
   
   public static final String ADMIN_SKILL_POINTS_TAG = "adminSkillPoints";
   public static final String CONCENTRATION_TICK_TAG = "concentration";
   
   private final UUID playerId;
   private String username = "";
   private final List<String> crafted = new ArrayList<>();
   private final List<String> researchedItems = new ArrayList<>();
   private final List<String> researchTasks = new ArrayList<>();
   private final HashMap<String, Tag> miscData = new HashMap<>();
   private final HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
   private final HashMap<String,List<ArcanaAchievement>> achievements = new HashMap<>();
   private int level;
   private int xp;
   private ItemStack storedOffhand = ItemStack.EMPTY;
   
   // Helper codec for achievements map - stores as CompoundTag matching existing NBT structure
   private static final Codec<HashMap<String, List<ArcanaAchievement>>> ACHIEVEMENTS_CODEC = CompoundTag.CODEC.xmap(
         achievementsTag -> {
            HashMap<String, List<ArcanaAchievement>> achievements = new HashMap<>();
            Set<String> achieveItemKeys = achievementsTag.keySet();
            for(String itemKey : achieveItemKeys){
               List<ArcanaAchievement> itemAchs = new ArrayList<>();
               CompoundTag itemAchsTag = achievementsTag.getCompoundOrEmpty(itemKey);
               
               for(String achieveKey : itemAchsTag.keySet()){
                  CompoundTag achTag = itemAchsTag.getCompoundOrEmpty(achieveKey);
                  ArcanaAchievement ach = ArcanaAchievements.ARCANA_ACHIEVEMENTS.get(achieveKey);
                  if(ach == null) continue;
                  itemAchs.add(ach.makeNew().fromNbt(achieveKey, achTag));
               }
               achievements.put(itemKey, itemAchs);
            }
            return achievements;
         },
         achievements -> {
            CompoundTag achievementsTag = new CompoundTag();
            for(Map.Entry<String, List<ArcanaAchievement>> entry : achievements.entrySet()){
               String item = entry.getKey();
               List<ArcanaAchievement> itemAchs = entry.getValue();
               CompoundTag itemAchsTag = new CompoundTag();
               for(ArcanaAchievement itemAch : itemAchs){
                  itemAchsTag.put(itemAch.id, itemAch.toNbt());
               }
               achievementsTag.put(item, itemAchsTag);
            }
            return achievementsTag;
         }
   );
   
   // Helper codec for augments map - stores as CompoundTag
   private static final Codec<HashMap<ArcanaAugment, Integer>> AUGMENTS_CODEC = CompoundTag.CODEC.xmap(
         tag -> {
            HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
            Set<String> augmentKeys = tag.keySet();
            for(String augmentKey : augmentKeys){
               int augmentLvl = tag.getIntOr(augmentKey, 0);
               if(augmentLvl > 0){
                  ArcanaAugment aug = ArcanaAugments.registry.get(augmentKey);
                  if(aug != null) augments.put(aug, augmentLvl);
               }
            }
            return augments;
         },
         augments -> {
            CompoundTag tag = new CompoundTag();
            for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
               tag.putInt(entry.getKey().id, entry.getValue());
            }
            return tag;
         }
   );
   
   // Helper codec for miscData map - stores as CompoundTag
   private static final Codec<HashMap<String, Tag>> MISC_DATA_CODEC = CompoundTag.CODEC.xmap(
         tag -> {
            HashMap<String, Tag> miscData = new HashMap<>();
            Set<String> keys = tag.keySet();
            keys.forEach(key -> miscData.put(key, tag.get(key)));
            return miscData;
         },
         miscData -> {
            CompoundTag tag = new CompoundTag();
            miscData.forEach(tag::put);
            return tag;
         }
   );
   
   public static final Codec<ArcanaPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         CodecUtils.UUID_CODEC.optionalFieldOf("playerID").forGetter(data -> Optional.ofNullable(data.playerId)),
         Codec.STRING.optionalFieldOf("username","").forGetter(data -> data.username),
         CodecUtils.STRING_LIST.optionalFieldOf("crafted", new ArrayList<>()).forGetter(data -> data.crafted),
         CodecUtils.STRING_LIST.optionalFieldOf("researchedItems", new ArrayList<>()).forGetter(data -> data.researchedItems),
         CodecUtils.STRING_LIST.optionalFieldOf("researchTasks", new ArrayList<>()).forGetter(data -> data.researchTasks),
         MISC_DATA_CODEC.optionalFieldOf("miscData", new HashMap<>()).forGetter(data -> data.miscData),
         ACHIEVEMENTS_CODEC.optionalFieldOf("achievements", new HashMap<>()).forGetter(data -> data.achievements),
         AUGMENTS_CODEC.optionalFieldOf("augments", new HashMap<>()).forGetter(data -> data.augments),
         Codec.INT.optionalFieldOf("level", 0).forGetter(data -> data.level),
         Codec.INT.optionalFieldOf("xp", 0).forGetter(data -> data.xp),
         ItemStack.OPTIONAL_CODEC.optionalFieldOf("storedOffhand", ItemStack.EMPTY).forGetter(data -> data.storedOffhand)
   ).apply(instance, ArcanaPlayerData::fromCodec));
   
   private static ArcanaPlayerData fromCodec(Optional<UUID> playerId, String username, List<String> crafted, List<String> researchedItems, List<String> researchTasks, HashMap<String, Tag> miscData, HashMap<String, List<ArcanaAchievement>> achievements, HashMap<ArcanaAugment, Integer> augments, int level, int xp, ItemStack storedOffhand){
      return new ArcanaPlayerData(playerId.orElse(null), username, crafted, researchedItems, researchTasks, miscData, achievements, augments, level, xp, storedOffhand);
   }
   
   public static final DataKey<ArcanaPlayerData> KEY = DataRegistry.register(DataKey.ofPlayer(Identifier.fromNamespaceAndPath(MOD_ID, "playerdata"), CODEC,ArcanaPlayerData::new));
   
   public ArcanaPlayerData(UUID playerId){
      this.playerId = playerId;
   }
   
   public ArcanaPlayerData(UUID playerId, String username, List<String> crafted, List<String> researchedItems, List<String> researchTasks, HashMap<String, Tag> miscData, HashMap<String, List<ArcanaAchievement>> achievements, HashMap<ArcanaAugment, Integer> augments, int level, int xp, ItemStack storedOffhand){
      this.playerId = playerId;
      this.username = username;
      this.crafted.addAll(crafted);
      this.researchedItems.addAll(researchedItems);
      this.researchTasks.addAll(researchTasks);
      this.miscData.putAll(miscData);
      this.achievements.putAll(achievements);
      this.augments.putAll(augments);
      this.level = level;
      this.xp = xp;
      this.storedOffhand = storedOffhand;
   }
   
   private ServerPlayer findPlayer(){
      if(BorisLib.SERVER == null || playerId == null) return null;
      return BorisLib.SERVER.getPlayerList().getPlayer(playerId);
   }
   
   public UUID getPlayerId(){
      return playerId;
   }
   
   public String getUsername(){
      return this.username;
   }
   
   public void setUsername(String username){
      this.username = username != null ? username : "";
   }
   
   public List<String> getCrafted(){
      return crafted;
   }
   
   public boolean hasCrafted(ArcanaItem arcanaItem){
      return crafted.stream().anyMatch(s -> s.equals(arcanaItem.getId()));
   }
   
   public boolean hasResearched(ArcanaItem arcanaItem){
      if(!ArcanaNovum.CONFIG.getBoolean(ArcanaRegistry.RESEARCH_ENABLED)) return true;
      return researchedItems.stream().anyMatch(s -> s.equals(arcanaItem.getId()));
   }
   
   public List<String> getResearchedItems(){
      return researchedItems;
   }
   
   public boolean completedResearchTask(String id){
      return researchTasks.stream().anyMatch(s -> s.equals(id));
   }
   
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
   
   public Tag getMiscData(String id){
      return miscData.get(id);
   }
   
   public HashMap<String, List<ArcanaAchievement>> getAchievements(){
      return achievements;
   }
   
   public HashMap<ArcanaAugment, Integer> getAugments(){
      return augments;
   }
   
   public int getLevel(){
      return level;
   }
   
   public int getXP(){
      return xp;
   }
   
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
   
   public int getBonusSkillPoints(){
      IntTag pointsEle = (IntTag) getMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG);
      return pointsEle == null ? 0 : pointsEle.intValue();
   }
   
   public int getTotalSkillPoints(){
      return getAchievementSkillPoints() + LevelUtils.getLevelSkillPoints(level) + getBonusSkillPoints();
   }
   
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
   
   public boolean addXP(int xp){
      if(getLevel() == 100 && this.xp + xp < 1000000000){
         this.xp += xp;
         return true;
      }
      
      int newLevel = LevelUtils.levelFromXp(this.xp+xp);
      if(getLevel() != newLevel){
         onLevelUp(newLevel);
      }
      this.xp += xp;
      this.level = newLevel;
      return true;
   }
   
   private void onLevelUp(int newLevel){
      ServerPlayer player = findPlayer();
      if(player == null) return;
      if(getLevel()/5 < newLevel/5){
         MinecraftServer server = player.level().getServer();
         List<MutableComponent> msgs = new ArrayList<>();
         
         if(newLevel / 5 * 5 == 100){
            MutableComponent playerName = Component.literal("").append(player.getDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);
            
            msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));
            msgs.add(Component.literal("")
                  .append(Component.literal("=== ").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK))
                  .append(playerName)
                  .append(Component.literal(" has reached Arcana Level ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                  .append(Component.literal(Integer.toString(newLevel / 5 * 5)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                  .append(Component.literal("!!!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD, ChatFormatting.UNDERLINE))
                  .append(Component.literal(" ===").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.BLACK)));
            msgs.add(Component.literal("=============================================").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_PURPLE));
         }else{
            MutableComponent lvlUpMsg = Component.literal("")
                  .append(player.getDisplayName()).withStyle(ChatFormatting.ITALIC)
                  .append(Component.literal(" has reached Arcana Level ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(Integer.toString(newLevel / 5 * 5)).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            server.getPlayerList().broadcastSystemMessage(lvlUpMsg, false);
         }
         
         if(ArcanaNovum.CONFIG.getBoolean(ArcanaRegistry.ANNOUNCE_ACHIEVEMENTS)){
            for(MutableComponent msg : msgs){
               server.getPlayerList().broadcastSystemMessage(msg, false);
            }
         }else{
            for(MutableComponent msg : msgs){
               player.displayClientMessage(msg, false);
            }
         }
      }
      
      SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f,1.5f);
      int resolve = getAugmentLevel(ArcanaAugments.RESOLVE);
      player.displayClientMessage(Component.literal(""),false);
      player.displayClientMessage(Component.literal("Your Arcana has levelled up to level "+newLevel+"!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),false);
      player.displayClientMessage(Component.literal("Max Concentration increased to "+LevelUtils.concFromLevel(newLevel,resolve)+"!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),false);
      player.displayClientMessage(Component.literal(""),false);
   }
   
   public boolean setXP(int xp){
      this.xp = xp;
      setLevel(LevelUtils.levelFromXp(this.xp));
      return true;
   }
   
   public boolean setLevel(int lvl){
      this.level = lvl;
      return true;
   }
   
   public boolean addCraftedSilent(ItemStack stack){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return false;
      String itemId = arcanaItem.getId();
      if(crafted.stream().anyMatch(i -> i.equalsIgnoreCase(itemId))) return false;
      addXP(ArcanaRarity.getFirstCraftXp(arcanaItem.getRarity()));
      return crafted.add(itemId);
   }
   
   public boolean addCrafted(ItemStack stack){
      boolean added = addCraftedSilent(stack);
      if(added){
         ServerPlayer player = findPlayer();
         if(player != null){
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
            MinecraftServer server = player.level().getServer();
            MutableComponent newCraftMsg = Component.literal("")
                  .append(player.getDisplayName()).withStyle(ChatFormatting.ITALIC)
                  .append(Component.literal(" has crafted their first ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC))
                  .append(arcanaItem.getTranslatedName().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.UNDERLINE))
                  .append(Component.literal("!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            server.getPlayerList().broadcastSystemMessage(newCraftMsg.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowItem(stack))), false);
         }
      }
      return added;
   }
   
   public boolean setAchievement(ArcanaAchievement achievement){
      return setAchievement(achievement.getArcanaItem().getId(),achievement);
   }
   
   private boolean setAchievement(String item, ArcanaAchievement achievement){
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
   
   public boolean addResearchedItem(String item){
      if(researchedItems.stream().anyMatch(i -> i.equalsIgnoreCase(item))) return false;
      return researchedItems.add(item);
   }
   
   public boolean removeCrafted(String item){
      if(crafted.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return crafted.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   public boolean removeAchievement(ArcanaAchievement ach){
      return removeAchievement(ach.getArcanaItem().getId(),ach);
   }
   
   private boolean removeAchievement(String item, ArcanaAchievement ach){
      boolean found = false;
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(ach.id)){
               itemAchs.remove(itemAch);
               found = true;
               break;
            }
         }
      }
      
      return found;
   }
   
   public boolean removeResearchedItem(String item){
      if(researchedItems.stream().noneMatch(i -> i.equalsIgnoreCase(item))) return false;
      return researchedItems.removeIf(i -> i.equalsIgnoreCase(item));
   }
   
   public void addMiscData(String id, Tag data){
      miscData.put(id,data);
   }
   
   public void removeMiscData(String id){
      miscData.remove(id);
   }
   
   public boolean hasAcheivement(ArcanaAchievement ach){
      return hasAcheivement(ach.getArcanaItem().getId(), ach);
   }
   
   private boolean hasAcheivement(String item, ArcanaAchievement ach){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(ach.id)){
               return itemAch.isAcquired();
            }
         }
      }
      return false;
   }
   
   public ArcanaAchievement getAchievement(ArcanaAchievement ach){
      return getAchievement(ach.getArcanaItem().getId(),ach);
   }
   
   private ArcanaAchievement getAchievement(String item, ArcanaAchievement ach){
      if(achievements.containsKey(item)){
         List<ArcanaAchievement> itemAchs = achievements.get(item);
         for(ArcanaAchievement itemAch : itemAchs){
            if(itemAch.id.equals(ach.id)){
               return itemAch;
            }
         }
      }
      return null;
   }
   
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
   
   public int getAugmentLevel(ArcanaAugment augment){
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey() == augment){
            return entry.getValue();
         }
      }
      return 0;
   }
   
   // Returns whether the player already had that augment or not
   public boolean setAugmentLevel(ArcanaAugment augment, int level){
      if(level < 0 || augment.getTiers().length < level) return false;
      
      if(ArcanaAugments.linkedAugments.containsKey(augment)){
         return setLinkedAugmentLevel(augment,level);
      }
      
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey() == augment){
            entry.setValue(level);
            return true;
         }
      }
      augments.put(augment,level);
      return false;
   }
   
   private boolean setLinkedAugmentLevel(ArcanaAugment augment, int level){
      List<ArcanaAugment> linkedAugments = ArcanaAugments.getLinkedAugments(augment);
      if(linkedAugments.isEmpty()) return false;
      
      boolean had = false;
      int[] levels = new int[linkedAugments.size()];
      for(int i = 0; i < linkedAugments.size(); i++){
         ArcanaAugment aug = linkedAugments.get(i);
         levels[i] = getAugmentLevel(aug);
         
         if(levels[i] != 0){
            had = true;
            for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
               if(entry.getKey() == aug){
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
   public boolean removeAugment(ArcanaAugment augment){
      if(augments.entrySet().stream().noneMatch(e -> e.getKey() == augment)) return false;
      return augments.entrySet().removeIf(e -> e.getKey() == augment);
   }
   
   public void removeAllAugments(){
      augments.clear();
   }
   
   public int getArcanePaperRequirement(ArcanaRarity rarity){
      int totalResearched = ArcanaItemUtils.countRarityInList(getResearchedItems(),rarity,false);
      return Math.min(64,(int) (0.025*Math.pow(totalResearched,2.5) + totalResearched + 1));
   }
   
   
   public boolean restoreOffhand(ServerPlayer player){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(storedOffhand.isEmpty()) return false;
      ItemStack offHand = player.getOffhandItem().copy();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND,storedOffhand.copyAndClear());
      if(!offHand.isEmpty() && !BinaryBlades.isFakeBlade(offHand)) {
         player.getInventory().placeItemBackInInventory(offHand);
      }
      return true;
   }
   
   public boolean storeOffhand(ServerPlayer player, ItemStack replacement){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty()) return false;
      storedOffhand = player.getOffhandItem();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND,replacement);
      return true;
   }
   
   public ItemStack getStoredOffhand(){
      return storedOffhand == null ? ItemStack.EMPTY : storedOffhand;
   }
}
