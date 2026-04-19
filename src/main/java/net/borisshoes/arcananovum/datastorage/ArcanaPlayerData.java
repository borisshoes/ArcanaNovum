package net.borisshoes.arcananovum.datastorage;

import com.mojang.serialization.Codec;
import io.github.ladysnake.pal.VanillaAbilities;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.NulMementoEvent;
import net.borisshoes.arcananovum.events.special.CeptyusOpenEvent;
import net.borisshoes.arcananovum.events.special.CeptyusStartEvent;
import net.borisshoes.arcananovum.events.special.GaialtusEvent;
import net.borisshoes.arcananovum.events.special.ZeraiyaStartEvent;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.research.ResearchTask;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.Dialog;
import net.borisshoes.arcananovum.utils.DialogHelper;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.GenericTimer;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.storage.ValueInput;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.borisshoes.arcananovum.ArcanaRegistry.DRAGON_TOWER_ABILITY;
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;

public class ArcanaPlayerData implements StorableData {
   
   public static final String ADMIN_SKILL_POINTS_TAG = "adminSkillPoints";
   public static final String CONCENTRATION_TICK_TAG = "concentration";
   
   public static final String ZERAIYA_TIME_TAG = "lastZeraiyaAttempt";
   public static final String CEPTYUS_TIME_TAG = "lastCeptyusAttempt";
   public static final String GAIALTUS_TIME_TAG = "lastGaialtusAttempt";
   public static final String CAN_CEPTYUS_TAG = "canAttemptCeptyus";
   public static final String ZERAIYA_COMPLETED_TAG = "completedZeraiya";
   public static final String CEPTYUS_COMPLETED_TAG = "completedCeptyus";
   public static final String GAIALTUS_COMPLETED_TAG = "completedGialatus";
   
   private final UUID playerId;
   private String username = "";
   private final List<String> crafted = new ArrayList<>();
   private final List<String> researchedItems = new ArrayList<>();
   private final List<String> researchTasks = new ArrayList<>();
   private final HashMap<String, Tag> miscData = new HashMap<>();
   private final HashMap<ArcanaAugment, Integer> augments = new HashMap<>();
   private final HashMap<String, List<ArcanaAchievement>> achievements = new HashMap<>();
   private int level;
   private int xp;
   private ItemStack storedOffhand = ItemStack.EMPTY;
   
   public static final DataKey<ArcanaPlayerData> KEY = DataRegistry.register(DataKey.ofPlayer(ArcanaRegistry.arcanaId("playerdata"), ArcanaPlayerData::new));
   
   public ArcanaPlayerData(UUID playerId){
      this.playerId = playerId;
   }
   
   @Override
   public void read(ValueInput view){
      this.username = view.getStringOr("username", "");
      
      this.crafted.clear();
      view.listOrEmpty("crafted", Codec.STRING).forEach(this.crafted::add);
      
      this.researchedItems.clear();
      view.listOrEmpty("researchedItems", Codec.STRING).forEach(this.researchedItems::add);
      
      this.researchTasks.clear();
      view.listOrEmpty("researchTasks", Codec.STRING).forEach(this.researchTasks::add);
      
      this.miscData.clear();
      view.read("miscData", CompoundTag.CODEC).ifPresent(tag -> {
         tag.keySet().forEach(key -> this.miscData.put(key, tag.get(key)));
      });
      
      this.achievements.clear();
      view.read("achievements", CompoundTag.CODEC).ifPresent(achievementsTag -> {
         for(String itemKey : achievementsTag.keySet()){
            List<ArcanaAchievement> itemAchs = new ArrayList<>();
            CompoundTag itemAchsTag = achievementsTag.getCompoundOrEmpty(itemKey);
            
            for(String achieveKey : itemAchsTag.keySet()){
               CompoundTag achTag = itemAchsTag.getCompoundOrEmpty(achieveKey);
               ArcanaAchievement ach = ArcanaAchievements.ARCANA_ACHIEVEMENTS.get(achieveKey);
               if(ach == null) continue;
               itemAchs.add(ach.makeNew().fromNbt(achieveKey, achTag));
            }
            this.achievements.put(itemKey, itemAchs);
         }
      });
      
      this.augments.clear();
      view.read("augments", CompoundTag.CODEC).ifPresent(tag -> {
         for(String augmentKey : tag.keySet()){
            int augmentLvl = tag.getIntOr(augmentKey, 0);
            if(augmentLvl > 0){
               ArcanaAugment aug = ArcanaAugments.registry.get(augmentKey);
               if(aug != null) this.augments.put(aug, augmentLvl);
            }
         }
      });
      
      this.level = view.getIntOr("level", 0);
      this.xp = view.getIntOr("xp", 0);
      this.storedOffhand = view.read("storedOffhand", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      tag.putString("username", username);
      
      ListTag craftedList = new ListTag();
      for(String s : crafted){
         craftedList.add(StringTag.valueOf(s));
      }
      tag.put("crafted", craftedList);
      
      ListTag researchedItemsList = new ListTag();
      for(String s : researchedItems){
         researchedItemsList.add(StringTag.valueOf(s));
      }
      tag.put("researchedItems", researchedItemsList);
      
      ListTag researchTasksList = new ListTag();
      for(String s : researchTasks){
         researchTasksList.add(StringTag.valueOf(s));
      }
      tag.put("researchTasks", researchTasksList);
      
      CompoundTag miscDataTag = new CompoundTag();
      miscData.forEach(miscDataTag::put);
      tag.put("miscData", miscDataTag);
      
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
      tag.put("achievements", achievementsTag);
      
      CompoundTag augmentsTag = new CompoundTag();
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         augmentsTag.putInt(entry.getKey().id, entry.getValue());
      }
      tag.put("augments", augmentsTag);
      
      tag.putInt("level", level);
      tag.putInt("xp", xp);
      
      if(!storedOffhand.isEmpty()){
         ItemStack.OPTIONAL_CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, BorisLib.SERVER.registryAccess()), storedOffhand).result().ifPresent(nbt -> tag.put("storedOffhand", nbt));
      }
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
      if(!ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.RESEARCH_ENABLED)) return true;
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
   
   public Tag getMiscDataOr(String id, Tag fallback){
      if(miscData.containsKey(id)) return miscData.get(id);
      return fallback;
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
      IntTag pointsEle = (IntTag) getMiscDataOr(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG, IntTag.valueOf(0));
      return pointsEle.intValue();
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
      
      int newLevel = LevelUtils.levelFromXp(this.xp + xp);
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
      if(getLevel() / 5 < newLevel / 5){
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
         
         if(ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.ANNOUNCE_ACHIEVEMENTS)){
            for(MutableComponent msg : msgs){
               server.getPlayerList().broadcastSystemMessage(msg, false);
            }
         }else{
            for(MutableComponent msg : msgs){
               player.sendSystemMessage(msg, false);
            }
         }
      }
      
      SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, .5f, 1.5f);
      int resolve = getAugmentLevel(ArcanaAugments.RESOLVE);
      player.sendSystemMessage(Component.literal(""), false);
      player.sendSystemMessage(Component.literal("Your Arcana has levelled up to level " + newLevel + "!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), false);
      player.sendSystemMessage(Component.literal("Max Concentration increased to " + LevelUtils.concFromLevel(newLevel, resolve) + "!").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
      player.sendSystemMessage(Component.literal(""), false);
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
            server.getPlayerList().broadcastSystemMessage(newCraftMsg.withStyle(s -> s.withHoverEvent(new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack)))), false);
         }
      }
      return added;
   }
   
   public boolean setAchievement(ArcanaAchievement achievement){
      return setAchievement(achievement.getArcanaItem().getId(), achievement);
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
         achievements.put(item, itemAchs);
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
      return removeAchievement(ach.getArcanaItem().getId(), ach);
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
      miscData.put(id, data);
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
      return getAchievement(ach.getArcanaItem().getId(), ach);
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
   
   public int totalAcquiredHiddenAchievements(){
      int count = 0;
      for(Map.Entry<String, List<ArcanaAchievement>> listEntry : achievements.entrySet()){
         for(ArcanaAchievement ach : listEntry.getValue()){
            if(ach.isAcquired() && ach.isHidden()){
               count++;
            }
         }
      }
      return count;
   }
   
   public int totalAcquiredNormalAchievements(){
      int count = 0;
      for(Map.Entry<String, List<ArcanaAchievement>> listEntry : achievements.entrySet()){
         for(ArcanaAchievement ach : listEntry.getValue()){
            if(ach.isAcquired() && !ach.isHidden()){
               count++;
            }
         }
      }
      return count;
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
         return setLinkedAugmentLevel(augment, level);
      }
      
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey() == augment){
            entry.setValue(level);
            return true;
         }
      }
      augments.put(augment, level);
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
            augments.put(aug, level);
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
      int totalResearched = ArcanaItemUtils.countRarityInList(getResearchedItems(), rarity, false);
      return Math.min(64, (int) (0.025 * Math.pow(totalResearched, 2.5) + totalResearched + 1));
   }
   
   
   public boolean restoreOffhand(ServerPlayer player){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(storedOffhand.isEmpty()) return false;
      ItemStack offHand = player.getOffhandItem().copy();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND, storedOffhand.copyAndClear());
      if(!offHand.isEmpty() && !BinaryBlades.isFakeBlade(offHand)){
         player.getInventory().placeItemBackInInventory(offHand);
      }
      return true;
   }
   
   public boolean storeOffhand(ServerPlayer player, ItemStack replacement){
      if(storedOffhand == null) storedOffhand = ItemStack.EMPTY;
      if(!storedOffhand.isEmpty()) return false;
      storedOffhand = player.getOffhandItem();
      player.getInventory().setItem(Inventory.SLOT_OFFHAND, replacement);
      return true;
   }
   
   public ItemStack getStoredOffhand(){
      return storedOffhand == null ? ItemStack.EMPTY : storedOffhand;
   }
   
   public void tick(ServerPlayer player){
      if(!player.getUUID().equals(this.playerId)) return;
      try{
         MinecraftServer server = player.level().getServer();
         
         // Check each player's inventory for arcana items
         Inventory inv = player.getInventory();
         for(int i = 0; i < inv.getContainerSize(); i++){
            ItemStack item = inv.getItem(i);
            
            boolean isArcane = ArcanaItemUtils.isArcane(item);
            if(!isArcane){
               if(item.is(ArcanaRegistry.ALL_ARCANA_ITEMS)){
                  ArcanaItem arcanaItem = ArcanaRegistry.ARCANA_ITEMS.getValue(Identifier.parse(item.getItem().toString()));
                  if(arcanaItem != null){
                     inv.setItem(i, arcanaItem.addCrafter(arcanaItem.getNewItem(), player.getStringUUID(), 1, server));
                     item = inv.getItem(i);
                  }
               }
               
               if(item.has(DataComponents.BUNDLE_CONTENTS)){
                  BundleContents bundleComp = item.get(DataComponents.BUNDLE_CONTENTS);
                  List<ItemStackTemplate> newStacks = new ArrayList<>();
                  for(ItemStackTemplate invStack : bundleComp.items()){
                     ItemStack containedStack = invStack.create();
                     if(containedStack.is(ArcanaRegistry.ALL_ARCANA_ITEMS)){
                        containedStack.inventoryTick(player.level(), player, null);
                     }
                     if(!containedStack.isEmpty()){
                        newStacks.add(ItemStackTemplate.fromNonEmptyStack(containedStack));
                     }
                  }
                  item.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(newStacks));
               }
               continue; // Item not arcane, skip
            }
            
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
            if(ArcanaItem.hasProperty(item, ArcanaItem.UNINITIALIZED_TAG)){
               inv.setItem(i, arcanaItem.addCrafter(arcanaItem.getNewItem(), player.getStringUUID(), 1, server));
               continue;
            }else if(ArcanaItemUtils.needsVersionUpdate(item)){
               inv.setItem(i, arcanaItem.updateItem(item, server));
               ArcanaNovum.devPrint("Updating Item " + item.getHoverName().getString());
               continue;
            }
            
            if(arcanaItem.getOrigin(item) == 2 && arcanaItem.getCrafter(item).isEmpty()){
               arcanaItem.addCrafter(item, player.getStringUUID(), 2, server);
            }
            
            // Achievements
            if(arcanaItem instanceof ShulkerCore){
               if(player.getY() > 1610 && player.getActiveEffectsMap().containsKey(MobEffects.LEVITATION))
                  ArcanaAchievements.grant(player, ArcanaAchievements.MILE_HIGH);
            }
            if(server.getTickCount() % 20 == 0 && arcanaItem.getRarity() == ArcanaRarity.DIVINE){
               ArcanaAchievements.grant(player, ArcanaAchievements.GOD_BOON);
            }
            
            // Reset Nul Memento
            ItemStack finalItem = item;
            if(arcanaItem instanceof NulMemento nulMemento && nulMemento.isActive(item) &&
                  (i != 39 || Event.getEventsOfType(NulMementoEvent.class).stream().noneMatch(event -> event.getPlayer().equals(player) && ArcanaItem.getUUID(event.getMemento()).equals(ArcanaItem.getUUID(finalItem))))){
               ArcanaItem.putProperty(item, ArcanaItem.ACTIVE_TAG, false);
            }
         }
         
         if(ArcanaItemUtils.hasItemInInventory(player, Items.DRAGON_EGG)){
            double eggDialogChance = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.DRAGON_EGG_DIALOG_CHANCE);
            double zeraiyaEventChance = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.ZERAIYA_EVENT_CHANCE);
            if(player.getRandom().nextDouble() < eggDialogChance){
               dragonEggDialog(player);
            }else if(player.getRandom().nextDouble() < zeraiyaEventChance){
               if(!completedZeraiya() && getLastZeraiyaAttempt() <= 0 && player.getRandom().nextInt(10) == 0 && ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.ZERAIYA_EVENT_ENABLED)){
                  startZeraiya(player);
                  setLastZeraiyaAttempt(36000);
                  return;
               }
            }
         }
         double gaialtusEventChance = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GAIALTUS_EVENT_CHANCE);
         if(player.getRandom().nextDouble() < gaialtusEventChance){
            tryStartGaialtus(player);
         }
         
         flightCheck(player);
         concCheck(server, player, this);
         ArcanaRegistry.AREA_EFFECTS.stream().forEach(areaEffectTracker -> areaEffectTracker.onTick(server));
         
         int quiverCD = ((IntTag) getMiscDataOr(QuiverItem.QUIVER_CD_TAG, IntTag.valueOf(0))).intValue();
         if(quiverCD > 0){
            addMiscData(QuiverItem.QUIVER_CD_TAG, IntTag.valueOf(quiverCD - 1));
         }
         
         if(!getStoredOffhand().isEmpty() && player.getOffhandItem().isEmpty()){
            restoreOffhand(player);
         }
         
         if(server.getTickCount() % 100 == 0){
            ListTag biomeList = (ListTag) getMiscDataOr(ResearchTasks.BIOMES_VISITED_TAG, new ListTag());
            Holder<Biome> biome = player.level().getBiome(player.blockPosition());
            StringTag biomeId = StringTag.valueOf(biome.getRegisteredName());
            if(!biomeList.contains(biomeId)){
               biomeList.add(biomeId);
               addMiscData(ResearchTasks.BIOMES_VISITED_TAG, biomeList);
            }else if(biomeList.size() >= 12){
               setResearchTask(ResearchTasks.VISIT_DOZEN_BIOMES, true);
            }
         }
         
         if(server.getTickCount() % 10 == 0){
            ItineranteurBlockEntity.tickPlayer(player);
         }
         
         int lastZeraiayaAttempt = getLastZeraiyaAttempt();
         if(lastZeraiayaAttempt > 0){
            setLastZeraiyaAttempt(lastZeraiayaAttempt - 1);
         }
         
         int lastCeptyusAttempt = getLastCeptyusAttempt();
         if(lastCeptyusAttempt > 0){
            setLastCeptyusAttempt(lastCeptyusAttempt - 1);
         }
         
         int lastGaialtusAttempt = getLastGaialtusAttempt();
         if(lastGaialtusAttempt > 0){
            setLastGaialtusAttempt(lastGaialtusAttempt - 1);
         }
      }catch(Exception e){
         ArcanaNovum.log(2, e.getMessage());
         ArcanaNovum.log(2, e.toString());
      }
   }
   
   private static void concCheck(MinecraftServer server, ServerPlayer player, ArcanaPlayerData arcaneProfile){
      if(server.getTickCount() % 80 != 0) return;
      int resolve = arcaneProfile.getAugmentLevel(ArcanaAugments.RESOLVE);
      int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP(), resolve);
      int curConc = ArcanaItemUtils.getUsedConcentration(player);
      if(ArcanaItemUtils.countItemsTakingConc(player) >= 30)
         ArcanaAchievements.grant(player, ArcanaAchievements.ARCANE_ADDICT);
      if(curConc > maxConc && !player.isCreative() && !player.isSpectator()){
         int concTick = ((IntTag) arcaneProfile.getMiscDataOr(ArcanaPlayerData.CONCENTRATION_TICK_TAG, IntTag.valueOf(0))).intValue() + 1;
         if(ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.DO_CONCENTRATION_DAMAGE)){
            player.sendSystemMessage(Component.literal("Your mind burns as your Arcana overwhelms you!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC, ChatFormatting.BOLD), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ILLUSIONER_CAST_SPELL, 2, .1f);
            player.hurtServer(player.level(), ArcanaDamageTypes.of(player.level(), ArcanaDamageTypes.CONCENTRATION), concTick * 2);
         }
         if(!player.isDeadOrDying()){
            if(player.getHealth() <= 1.5f){
               ArcanaAchievements.grant(player, ArcanaAchievements.CLOSE_CALL);
            }
            // Nul Memento
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento nulMemento && !nulMemento.isActive(headStack)){
               nulMemento.forgor(headStack, player);
            }
         }else{
            concTick = 0;
         }
         arcaneProfile.addMiscData(ArcanaPlayerData.CONCENTRATION_TICK_TAG, IntTag.valueOf(concTick));
      }else{
         arcaneProfile.addMiscData(ArcanaPlayerData.CONCENTRATION_TICK_TAG, IntTag.valueOf(0));
      }
      
   }
   
   private static void flightCheck(ServerPlayer player){
      // Levitation Harness
      ItemStack item = player.getItemBySlot(EquipmentSlot.CHEST);
      boolean harnessFly = false;
      if(ArcanaItemUtils.identifyItem(item) instanceof LevitationHarness harness){
         if(EnergyItem.getEnergy(item) > 0 && harness.getStall(item) == -1){
            harnessFly = true;
         }
      }
      
      if(LEVITATION_HARNESS_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && !harnessFly){
         LEVITATION_HARNESS_ABILITY.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
      }else if(!LEVITATION_HARNESS_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && harnessFly){
         LEVITATION_HARNESS_ABILITY.grantTo(player, VanillaAbilities.ALLOW_FLYING);
      }
      
      // Dragon Tower Check
      boolean dragonTowerFly = false;
      Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
      if(bossFight != null && bossFight.getA() == BossFights.DRAGON){
         List<DragonBossFight.ReclaimState> reclaimStates = DragonBossFight.getReclaimStates();
         if(reclaimStates != null){
            for(DragonBossFight.ReclaimState reclaimState : reclaimStates){
               if(reclaimState.getPlayer() != null && reclaimState.getPlayer().equals(player)){
                  dragonTowerFly = true;
               }
            }
         }
      }
      
      if(DRAGON_TOWER_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && !dragonTowerFly){
         DRAGON_TOWER_ABILITY.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
      }else if(!DRAGON_TOWER_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && dragonTowerFly){
         DRAGON_TOWER_ABILITY.grantTo(player, VanillaAbilities.ALLOW_FLYING);
      }
   }
   
   public void dragonEggDialog(ServerPlayer player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Memento, 1 - Crafted Aequalis, 2 - Has Ceptyus Pickaxe, 3 - Has Memento, 4 - Has Aequalis, 5 - Has Greaves, 6 - Has Spear
      boolean[] conditions = new boolean[]{
            hasCrafted(ArcanaRegistry.NUL_MEMENTO),
            hasCrafted(ArcanaRegistry.AEQUALIS_SCIENTIA),
            ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.NUL_MEMENTO.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nINSOLENT INSECT! Do you intend to carry me as your trophy for all eternity!?").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 1, 1, 0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nRelease me at once! And I will grant you a swift death for your defiance!").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 1, 1, 0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI have been banished to this Egg more times than there are islands in my sky! My return is inevitable!").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 1, 1, 0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou do not know what it means to suffer! Upon my return I shall teach you myself!").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 1, 1, 0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nMark my words! When Nul oversteps and awakens Brimsüth, you'll wish you had never helped that entitled brat!").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThose mewling kids call themselves Gods... I AM THE TRUE ASCENDANT!! And one day, they will yield to me!").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0b11));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThat Old Fool left behind a pickaxe? Well, I guess Ceptyus wasn't so foolish after all...").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nTake that Spear and return it to oblivion! Nothing good ever comes of anything bearing its name.").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI hoped to never see that dreaded storm ever again. Do your self a favor and throw that Spear into the void.").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThe scent on those Greaves smells faintly familiar... where do I know this from?").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f))
      ), new int[]{}, 0, 1, 0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI still remember what it was like to be impaled by dozens of those Spears. An agony nearly unparalleled...\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nWill you ever share with us what truly happened back then? Maybe sharing will help give you some peace.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWill you ever just leave me alone like I ask!?\n").withStyle(ChatFormatting.DARK_PURPLE))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.4f, 1.5f))
      ), new int[]{0, 80}, 0, 1, 0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul! You brat! You think you can take all the realms for yourself now that there's no one left to stop you!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nTake? I do not want to own the world, I want to revive it! I may be the God of Death, but without creatures to die, what purpose would I serve?").withStyle(ChatFormatting.DARK_GRAY))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 1.4f))
      ), new int[]{0, 80}, 0, 1, 0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul! You think you are safe because you imprisoned me?! I killed one God, one FAR more powerful than you. I can do it again!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nDon't make me laugh! You are a shell of your former self. I did not imprison you, this Player defeated you. How could you kill me if you couldn't kill them?").withStyle(ChatFormatting.DARK_GRAY))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 0.7f))
      ), new int[]{0, 100}, 0, 1, 0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWhat's that Divine essence I sense? Could it be the absentee ascendant? You never liked interacting with us, did you? Even when it was just you and me.\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI'm sorry I was so timid in those early days. I had spent so long accompanied by only my own kind, hidden from the rest of the world.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI've learned from my mistakes. I will take an active role in shaping our world. It's never too late to learn from our failures.").withStyle(ChatFormatting.AQUA))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.9f))
      ), new int[]{0, 100, 100}, 0, 1, 0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWell what do we have here? A 'family' reunion, how touching... Have you two come to mock me together?!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nWe don't 'go' anywhere, this Player holds all our tributes. But I suppose it would be foolish to waste this opportunity.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nNow Brother, we need not mock Enderia in this state. Surely you can sympathize a bit with her struggles?\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI don't need any of your worthless sympathy. You both betrayed what it means to be an ascendant!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nWE betrayed what it means? Ascending means breaking free of the cycle the Progenitors put us in! The mere thought of our arrival sent them scattering! And yet WE'RE the ones who betrayed that ideal?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI ascended because the world needed to change. Your initial contribution was significant, but you soon fell back to the patterns of your predecessor.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYour grandiose ideals mean nothing! After all I've been through, I just want a place to call home; To be safe for once in my entire existence!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYet the price of your safety came at the cost of the freedom of an entire realm.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou terrorized them for eons, and when you had the chance to stop, you were too afraid of rebellion to let them be. But the rebellion came anyways, the Endermen sacrificed their lives to lead this Player to you.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI don't have to put up with any of this! Go play 'God' somewhere else.\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nIt's ironic that your Egg is the one place where you can finally have your peace, yet it is the one place you don't want to be. I wonder if you can appreciate the freedom you took away better now.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nGood Chat, Sis...").withStyle(ChatFormatting.DARK_GRAY))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.1f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 1.2f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.3f, 1.1f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT, 0.3f, 0.7f))
      ), new int[]{0, 80, 100, 80, 80, 140, 120, 120, 80, 160, 80, 140}, 0, 1, 0b11000));
      
      DialogHelper helper = new DialogHelper(dialogOptions, conditions);
      DialogHelper.sendDialog(List.of(player), helper.getWeightedResult(player.getRandom()), true);
   }
   
   public void tryStartGaialtus(ServerPlayer player){
      if(!ArcanaNovum.CONFIG.getBoolean(ArcanaConfig.GAIALTUS_EVENT_ENABLED)) return;
      if(completedGaialtus() || getLastGaialtusAttempt() > 0) return;
      if(!player.level().equals(player.level().getServer().overworld())) return;
      if(player.level().getBrightness(LightLayer.SKY, player.blockPosition()) == 0) return;
      List<FishingHook> list = player.level().getEntities(EntityType.FISHING_BOBBER, player.getBoundingBox().inflate(20.0, 8.0, 20.0), (hook) -> player.equals(hook.getPlayerOwner()));
      if(list.isEmpty()) return;
      AtomicInteger mined = new AtomicInteger();
      AtomicInteger placed = new AtomicInteger();
      Object2IntMap<Stat<?>> stats = player.getStats().stats;
      stats.keySet().forEach(key -> {
         if(key.getType() == Stats.BLOCK_MINED){
            mined.addAndGet(stats.getInt(key));
         }else if(key.getType() == Stats.ITEM_USED && key.getValue() instanceof BlockItem){
            placed.addAndGet(stats.getInt(key));
         }
      });
      if(mined.get() < 10000 || placed.get() < 10000) return;
      startGaialtus(player);
      setLastGaialtusAttempt(36000);
   }
   
   public int getLastZeraiyaAttempt(){
      return getMiscDataOr(ZERAIYA_TIME_TAG, IntTag.valueOf(0)).asInt().orElse(0);
   }
   
   public void setLastZeraiyaAttempt(int lastZeraiyaAttempt){
      addMiscData(ZERAIYA_TIME_TAG, IntTag.valueOf(lastZeraiyaAttempt));
   }
   
   public int getLastCeptyusAttempt(){
      return getMiscDataOr(CEPTYUS_TIME_TAG, IntTag.valueOf(0)).asInt().orElse(0);
   }
   
   public void setLastCeptyusAttempt(int lastCeptyusAttempt){
      addMiscData(CEPTYUS_TIME_TAG, IntTag.valueOf(lastCeptyusAttempt));
   }
   
   public int getLastGaialtusAttempt(){
      return getMiscDataOr(GAIALTUS_TIME_TAG, IntTag.valueOf(0)).asInt().orElse(0);
   }
   
   public void setLastGaialtusAttempt(int lastGaialtusAttempt){
      addMiscData(GAIALTUS_TIME_TAG, IntTag.valueOf(lastGaialtusAttempt));
   }
   
   public boolean canAttemptCeptyus(){
      return getMiscDataOr(CAN_CEPTYUS_TAG, ByteTag.valueOf(true)).asBoolean().orElse(true);
   }
   
   public void setCanAttemptCeptyus(boolean canAttemptCeptyus){
      addMiscData(CAN_CEPTYUS_TAG, ByteTag.valueOf(canAttemptCeptyus));
   }
   
   public boolean completedCeptyus(){
      return getMiscDataOr(CEPTYUS_COMPLETED_TAG, ByteTag.valueOf(false)).asBoolean().orElse(false);
   }
   
   public void completeCeptyus(){
      addMiscData(CEPTYUS_COMPLETED_TAG, ByteTag.valueOf(true));
   }
   
   public boolean completedGaialtus(){
      return getMiscDataOr(GAIALTUS_COMPLETED_TAG, ByteTag.valueOf(false)).asBoolean().orElse(false);
   }
   
   public void completeGaialtus(){
      addMiscData(GAIALTUS_COMPLETED_TAG, ByteTag.valueOf(true));
   }
   
   public boolean completedZeraiya(){
      return getMiscDataOr(ZERAIYA_COMPLETED_TAG, ByteTag.valueOf(false)).asBoolean().orElse(false);
   }
   
   public void completeZeraiya(){
      addMiscData(ZERAIYA_COMPLETED_TAG, ByteTag.valueOf(true));
   }
   
   public void startZeraiya(ServerPlayer player){
      // z_no z_yes z_ask | z_no z_listen z_more
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal("As ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                  .append(Component.literal(" quietly dwells in your pocket, you feel a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("wisp of wind").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" blow through your mind.\n").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("")
                  .append(Component.literal("A gentle voice inquires...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("")
                  .append(player.getDisplayName().copy())
                  .append(Component.literal(", would you like to hear a story?").withStyle(ChatFormatting.DARK_GREEN)),
            Component.literal("\n")
                  .append(Component.literal("[No]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_RED).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_no"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[Yes]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_GREEN).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_yes"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[Is it a sad story?]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_ask"))))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
            new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
            new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
            new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1))
      ), new int[]{0, 80, 60, 80}, 1, 1, 0), true);
      Event.addEvent(new ZeraiyaStartEvent(player));
   }
   
   public void startGaialtus(ServerPlayer player){
      // g_hungry g_cat g_relax g_bored | g_creation g_destruction g_balance | g_past g_future g_stars g_screen g_universe
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal("You feel a presence in the ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("warm breeze").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" and hear a gentle voice...\n").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
            Component.literal("")
                  .append(Component.literal("Why have you come to fish, ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(player.getDisplayName().copy())
                  .append(Component.literal(", on this quiet and beautiful day?").withStyle(ChatFormatting.DARK_GREEN)),
            Component.literal("\n")
                  .append(Component.literal("[I am Hungry]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.GOLD).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_hungry"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[I Want to Tame a Cat]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.YELLOW).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_cat"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[I am Enjoying the World]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_relax"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[I am Bored...]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_bored"))))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
            new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
            new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
      )), new int[]{0, 80, 60}, 1, 1, 0), true);
      Event.addEvent(new GaialtusEvent(player));
   }
   
   public void startCeptyus(ServerPlayer player){
      // c_investigate c_tinker c_reach
      Structure structure = player.level().structureManager().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(BuiltinStructures.ANCIENT_CITY);
      StructureStart start = player.level().structureManager().getStructureAt(player.blockPosition(), structure);
      if(!(start.isValid() && start.canBeReferenced())) return;
      
      DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nWhen ").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("Ceptyus").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal(" left our realm behind, many cities like this were buried and sealed. My ").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal("Brother").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal(" convinced me to uncover them once more.").withStyle(ChatFormatting.AQUA))
      )), new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f)
      )), new int[]{}, 1, 1, 0), true);
      Event.addEvent(new CeptyusStartEvent(player));
   }
   
   public boolean canSpecialEventRespond(ServerPlayer player){
      GaialtusEvent gEventFound = Event.getEventsOfType(GaialtusEvent.class).stream().filter(g -> g.getPlayer().equals(player)).findFirst().orElse(null);
      ZeraiyaStartEvent zEventFound = Event.getEventsOfType(ZeraiyaStartEvent.class).stream().filter(z -> z.getPlayer().equals(player)).findFirst().orElse(null);
      CeptyusStartEvent cEventFound = Event.getEventsOfType(CeptyusStartEvent.class).stream().filter(c -> c.getPlayer().equals(player) && c.sentInvestigate()).findFirst().orElse(null);
      CeptyusOpenEvent c2EventFound = Event.getEventsOfType(CeptyusOpenEvent.class).stream().filter(c -> c.getPlayer().equals(player)).findFirst().orElse(null);
      return gEventFound != null || zEventFound != null || cEventFound != null || c2EventFound != null;
   }
   
   public void specialEventResponse(ServerPlayer player, String msg){
      GaialtusEvent gEventFound = Event.getEventsOfType(GaialtusEvent.class).stream().filter(g -> g.getPlayer().equals(player)).findFirst().orElse(null);
      if(gEventFound != null && gEventFound.getStage() == 0){
         if(msg.equalsIgnoreCase("g_hungry")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("Ah! So hungry is the one who ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("creates").withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(", and the one who ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("destroys").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal(". Say, ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(player.getDisplayName())
                  .append(Component.literal(", which one are you?").withStyle(ChatFormatting.DARK_GREEN)), false);
         }else if(msg.equalsIgnoreCase("g_cat")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("A furry companion to keep you company on your long dream? When you have your new friend, would you choose to embark on a new journey of ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("creation").withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(", or one of ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("destruction").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("?").withStyle(ChatFormatting.DARK_GREEN)), false);
         }else if(msg.equalsIgnoreCase("g_relax")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("I am glad. There is so much to enjoy of this dream. Tell me, when you close your eyes, do you see visions of your ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("creations").withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(", or ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("destructions").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("?").withStyle(ChatFormatting.DARK_GREEN)), false);
         }else if(msg.equalsIgnoreCase("g_bored")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("It is wise to sit with your boredom, for it is the spout from which creativity flows. Does your creativity often lead you to acts of ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("creation").withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(", or of ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal("destruction").withStyle(ChatFormatting.DARK_GRAY))
                  .append(Component.literal("?").withStyle(ChatFormatting.DARK_GREEN)), false);
         }else{
            return;
         }
         SoundUtils.playSongToPlayer(player, SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f);
         gEventFound.setStage(1);
         BorisLib.addTickTimerCallback(new GenericTimer(40, () -> {
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("[Creation]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.WHITE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_creation"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[Destruction]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_GRAY).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_destruction"))))
                  .append(Component.literal(" "))
                  .append(Component.literal("[I do what I feel like]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_balance")))), false);
            SoundUtils.playSongToPlayer(player, SoundEvents.SOUL_ESCAPE.value(), 1f, 1);
         }));
      }else if(gEventFound != null && gEventFound.getStage() == 1){
         if(msg.equalsIgnoreCase("g_creation") || msg.equalsIgnoreCase("g_destruction")){
            DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("\n")
                        .append(Component.literal("You've ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("created").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" many things in this world, and have ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("destroyed").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" just as much. One cannot exist without the other, even within ourselves. Perhaps you could learn from ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(", to learn of the balance between all things.").withStyle(ChatFormatting.DARK_GREEN)),
                  Component.literal("\n")
                        .append(Component.literal("The breeze fades...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            )), new ArrayList<>(Arrays.asList(
                  new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                  new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
            )), new int[]{0, 120}, 1, 1, 0), true);
            gEventFound.markForRemoval();
         }else if(msg.equalsIgnoreCase("g_balance")){
            DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                  Component.literal("\n")
                        .append(Component.literal("So ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(player.getDisplayName())
                        .append(Component.literal(" knows of the balance. There can never be ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("one").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" without ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("the other").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(". Perhaps you learned this from ").withStyle(ChatFormatting.DARK_GREEN))
                        .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("?").withStyle(ChatFormatting.DARK_GREEN)),
                  Component.literal("\n")
                        .append(Component.literal("The breeze cools, you look up at the sky, and suddenly it is midnight. The new moon silently hovers over your head.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
                  Component.literal("\n")
                        .append(Component.literal("When you look up at the stars, what do you see?").withStyle(ChatFormatting.DARK_GREEN)),
                  Component.literal("\n")
                        .append(Component.literal("[The Past]").withStyle(s ->
                              s.withBold(true).withColor(ChatFormatting.DARK_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_past"))))
                        .append(Component.literal(" "))
                        .append(Component.literal("[The Future]").withStyle(s ->
                              s.withBold(true).withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_future"))))
                        .append(Component.literal(" "))
                        .append(Component.literal("[Flaming Balls of Gas]").withStyle(s ->
                              s.withBold(true).withColor(ChatFormatting.YELLOW).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_stars"))))
                        .append(Component.literal(" "))
                        .append(Component.literal("[A Computer Screen]").withStyle(s ->
                              s.withBold(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_screen"))))
                        .append(Component.literal(" "))
                        .append(Component.literal("[The Universe]").withStyle(s ->
                              s.withBold(true).withColor(ChatFormatting.WHITE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_universe"))))
            )), new ArrayList<>(Arrays.asList(
                  new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                  new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                  new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
                  new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
            )), new int[]{0, 120, 100, 40}, 1, 1, 0), true);
            gEventFound.setStage(2);
            BorisLib.addTickTimerCallback(new GenericTimer(120, () -> {
               ServerLevel overworld = player.level().getServer().overworld();
               long timeOfDay = overworld.getOverworldClockTime();
               int curTime = (int) (timeOfDay % 24000L);
               int targetTime = 18000;
               int timeDiff = (targetTime - curTime + 24000) % 24000;
               int day = (int) (timeOfDay / 24000L % Integer.MAX_VALUE);
               int curPhase = day % 8;
               int phaseDiff = (4 - curPhase + 8) % 8;
               overworld.clockManager().setTotalTicks(overworld.dimensionType().defaultClock().get(),timeOfDay + phaseDiff * 24000L + timeDiff);
            }));
         }
      }else if(gEventFound != null && gEventFound.getStage() == 2){
         if(msg.equalsIgnoreCase("g_universe")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("And I see you, ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(player.getDisplayName())
                  .append(Component.literal("...").withStyle(ChatFormatting.DARK_GREEN)), false);
            BorisLib.addTickTimerCallback(new GenericTimer(60, () -> {
               player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
               player.sendSystemMessage(Component.literal("\n[Look Around]").withStyle(s ->
                     s.withBold(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action g_wake"))), false);
            }));
            gEventFound.setStage(3);
         }else{
            if(msg.equalsIgnoreCase("g_past")){
               DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n")
                           .append(Component.literal("I suppose you are right, all those stars are as they were billions of years ago. Well, not these stars, they are only ones and zeros in the game you dream of. Keep looking up into the night, and I hope to see you again, ").withStyle(ChatFormatting.DARK_GREEN))
                           .append(player.getDisplayName())
                           .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)),
                     Component.literal("\n")
                           .append(Component.literal("The breeze fades and you are left in the moonless night.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               )), new ArrayList<>(Arrays.asList(
                     new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                     new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
               )), new int[]{0, 180}, 1, 1, 0), true);
            }else if(msg.equalsIgnoreCase("g_future")){
               DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n")
                           .append(Component.literal("So you have your head in the stars? It is true, the possibilities are endless with a creativity like yours. Keep looking up into the night, and I hope to see you again, ").withStyle(ChatFormatting.DARK_GREEN))
                           .append(player.getDisplayName())
                           .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)),
                     Component.literal("\n")
                           .append(Component.literal("The breeze fades and you are left in the moonless night.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               )), new ArrayList<>(Arrays.asList(
                     new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                     new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
               )), new int[]{0, 180}, 1, 1, 0), true);
            }else if(msg.equalsIgnoreCase("g_stars")){
               DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n")
                           .append(Component.literal("I suppose you are right. Well, not for these stars, they are only ones and zeros in the game you dream of. Keep looking up into the night, and I hope to see you again, ").withStyle(ChatFormatting.DARK_GREEN))
                           .append(player.getDisplayName())
                           .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)),
                     Component.literal("\n")
                           .append(Component.literal("The breeze fades and you are left in the moonless night.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               )), new ArrayList<>(Arrays.asList(
                     new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                     new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
               )), new int[]{0, 180}, 1, 1, 0), true);
            }else if(msg.equalsIgnoreCase("g_screen")){
               DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n")
                           .append(Component.literal("So you are aware of your dream? But can you see the meaning in it, the ").withStyle(ChatFormatting.DARK_GREEN))
                           .append(Component.literal("------").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.OBFUSCATED))
                           .append(Component.literal("? Ah, not yet. Keep dreaming, ").withStyle(ChatFormatting.DARK_GREEN))
                           .append(player.getDisplayName())
                           .append(Component.literal(", and I hope to see you again.").withStyle(ChatFormatting.DARK_GREEN)),
                     Component.literal("\n")
                           .append(Component.literal("The breeze fades and you are left in the moonless night.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               )), new ArrayList<>(Arrays.asList(
                     new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
                     new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
               )), new int[]{0, 180}, 1, 1, 0), true);
            }
            gEventFound.markForRemoval();
         }
      }else if(gEventFound != null && gEventFound.getStage() == 3){
         if(msg.equalsIgnoreCase("g_wake")){
            player.sendSystemMessage(Component.literal("\n")
                  .append(Component.literal("After that strange encounter, you look down at your feet and see a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                  .append(Component.literal("pair of pants").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC))
                  .append(Component.literal(" unlike any that you have seen before.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
            ItemStack pants = ArcanaRegistry.GREAVES_OF_GAIALTUS.addCrafter(ArcanaRegistry.GREAVES_OF_GAIALTUS.getNewItem(), player.getStringUUID(), 3, player.level().getServer());
            pants.enchant(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), 1);
            BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(pants, player, 0));
            BorisLib.addTickTimerCallback(new GenericTimer(40, () -> {
               ArcanaAchievements.grant(player, ArcanaAchievements.AND_THE_UNIVERSE_SAID);
               completeGaialtus();
            }));
            gEventFound.markForRemoval();
         }
      }
      
      ZeraiyaStartEvent zEventFound = Event.getEventsOfType(ZeraiyaStartEvent.class).stream().filter(z -> z.getPlayer().equals(player)).findFirst().orElse(null);
      if(zEventFound != null && msg.equalsIgnoreCase("z_yes")){
         Event.RECENT_EVENTS.removeIf(e -> e instanceof ZeraiyaStartEvent z && z.getPlayer().equals(player));
         DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("\n")
                     .append(Component.literal("This is a story of how a ").withStyle(ChatFormatting.DARK_GREEN))
                     .append(Component.literal("young dragon girl").withStyle(ChatFormatting.DARK_PURPLE))
                     .append(Component.literal(" lost her way a long time ago...").withStyle(ChatFormatting.DARK_GREEN)),
               Component.literal("\n")
                     .append(Component.literal("[Listen More]").withStyle(s ->
                           s.withBold(true).withColor(ChatFormatting.DARK_AQUA).withClickEvent(new ClickEvent.OpenUrl(URI.create("https://docs.google.com/document/d/1sLhs7qW2Z4RJ6sVh14ZyCs669U8xbBT-lHxhxWd3a1M/edit?usp=sharing"))))),
               Component.literal("\n")
                     .append(Component.literal("As the story ends you see at your feet a ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                     .append(Component.literal("pitch black spear").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC))
                     .append(Component.literal(".").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
               Component.literal("")
                     .append(Component.literal("No one is born with venom in their veins, darkness is always chosen.").withStyle(ChatFormatting.DARK_GREEN)),
               Component.literal("")
                     .append(Component.literal("The presence fades and ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                     .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal(" stirs...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
         )), new ArrayList<>(Arrays.asList(
               new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
               new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
               new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
               new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1),
               new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL, 0.05f, 1.4f))
         ), new int[]{0, 60, 200, 80, 80}, 1, 1, 0), true);
         ItemStack spear = ArcanaRegistry.SPEAR_OF_TENBROUS.addCrafter(ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem(), player.getStringUUID(), 3, player.level().getServer());
         spear.enchant(MinecraftUtils.getEnchantment(ArcanaRegistry.FATE_ANCHOR), 1);
         BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(spear, player, 265));
         BorisLib.addTickTimerCallback(new GenericTimer(265, () -> {
            ArcanaAchievements.grant(player, ArcanaAchievements.ZERAIYA);
            completeZeraiya();
         }));
      }else if(zEventFound != null && msg.equalsIgnoreCase("z_no")){
         Event.RECENT_EVENTS.removeIf(e -> e instanceof ZeraiyaStartEvent z && z.getPlayer().equals(player));
         player.sendSystemMessage(Component.literal("\n")
               .append(Component.literal("The presence fades and ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
               .append(Component.literal("Enderia's Egg").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
               .append(Component.literal(" stirs...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)), false);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENDER_DRAGON_GROWL, 0.05f, 1.4f);
      }else if(zEventFound != null && msg.equalsIgnoreCase("z_ask")){
         zEventFound.refresh();
         DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("\n")
                     .append(Component.literal("The luminance of life often casts a ").withStyle(ChatFormatting.DARK_GREEN))
                     .append(Component.literal("dark shadow").withStyle(ChatFormatting.DARK_GRAY))
                     .append(Component.literal(". Not everyone has learned to live in the ").withStyle(ChatFormatting.DARK_GREEN))
                     .append(Component.literal("light").withStyle(ChatFormatting.WHITE))
                     .append(Component.literal(".").withStyle(ChatFormatting.DARK_GREEN)),
               Component.literal("\n")
                     .append(Component.literal("[I don't like sad stories]").withStyle(s ->
                           s.withBold(true).withColor(ChatFormatting.DARK_RED).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_no"))))
                     .append(Component.literal(" "))
                     .append(Component.literal("[I'll listen]").withStyle(s ->
                           s.withBold(true).withColor(ChatFormatting.DARK_GREEN).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action z_yes"))))
         )), new ArrayList<>(Arrays.asList(
               new Dialog.DialogSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS.value(), 2f, 2f),
               new Dialog.DialogSound(SoundEvents.SOUL_ESCAPE.value(), 1f, 1)
         )), new int[]{0, 80}, 1, 1, 0), true);
      }
      
      CeptyusStartEvent cEventFound = Event.getEventsOfType(CeptyusStartEvent.class).stream().filter(c -> c.getPlayer().equals(player) && c.sentInvestigate()).findFirst().orElse(null);
      CeptyusOpenEvent c2EventFound = Event.getEventsOfType(CeptyusOpenEvent.class).stream().filter(c -> c.getPlayer().equals(player)).findFirst().orElse(null);
      if(cEventFound != null && msg.equalsIgnoreCase("c_investigate")){
         Structure structure = player.level().structureManager().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(BuiltinStructures.ANCIENT_CITY);
         StructureStart start = player.level().structureManager().getStructureAt(player.blockPosition(), structure);
         if(!(start.isValid() && start.canBeReferenced())){
            player.sendSystemMessage(Component.literal("Return to the portal...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
            return;
         }
         for(StructurePiece piece : start.getPieces()){
            if(!(piece.getBoundingBox().isInside(player.blockPosition()))) continue;
            if(!(piece instanceof PoolElementStructurePiece poolPiece && poolPiece.getElement() instanceof SinglePoolElement elem))
               continue;
            Identifier pieceId = elem.getTemplateLocation();
            String[] split = pieceId.getPath().split("/");
            if(split[split.length - 1].contains("city_center")){
               DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
                     Component.literal("\n")
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                           .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                           .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                           .append(Component.literal("\nAlas, I have never been able to reactivate them. As long as ").withStyle(ChatFormatting.AQUA))
                           .append(Component.literal("Ceptyus").withStyle(ChatFormatting.DARK_AQUA))
                           .append(Component.literal(" wishes to stay isolated, we may never see the other side.").withStyle(ChatFormatting.AQUA))
               )), new ArrayList<>(Arrays.asList(
                     new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 0.5f, 0.7f)
               )), new int[]{}, 1, 1, 0), true);
               
               if(ArcanaItemUtils.hasItemInInventory(player, ArcanaRegistry.PLANESHIFTER.getItem())){
                  player.sendSystemMessage(Component.literal("\n[Tinker with your Planeshifter]").withStyle(s ->
                        s.withBold(true).withColor(ChatFormatting.DARK_PURPLE).withClickEvent(new ClickEvent.RunCommand("/arcana specialEvent action c_tinker"))), false);
                  cEventFound.setSentTinker();
               }else{
                  player.sendSystemMessage(Component.literal("\nYou wonder if there is some way to reactivate it...").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                  cEventFound.markForRemoval();
               }
               return;
            }
         }
         player.sendSystemMessage(Component.literal("Return to the portal...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
      }else if(cEventFound != null && cEventFound.sentTinker() && c2EventFound == null && msg.equalsIgnoreCase("c_tinker")){
         if(!player.getMainHandItem().is(ArcanaRegistry.PLANESHIFTER.getItem()) && !player.getOffhandItem().is(ArcanaRegistry.PLANESHIFTER.getItem())){
            player.sendSystemMessage(Component.literal("Take out your Planeshifter...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
            return;
         }
         
         Structure structure = player.level().structureManager().registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(BuiltinStructures.ANCIENT_CITY);
         StructureStart start = player.level().structureManager().getStructureAt(player.blockPosition(), structure);
         if(!(start.isValid() && start.canBeReferenced())){
            player.sendSystemMessage(Component.literal("Return to the portal...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
            return;
         }
         boolean found = false;
         for(StructurePiece piece : start.getPieces()){
            if(!(piece.getBoundingBox().isInside(player.blockPosition()))) continue;
            if(!(piece instanceof PoolElementStructurePiece poolPiece && poolPiece.getElement() instanceof SinglePoolElement elem))
               continue;
            Identifier pieceId = elem.getTemplateLocation();
            String[] split = pieceId.getPath().split("/");
            if(split[split.length - 1].contains("city_center")){
               found = true;
               break;
            }
         }
         if(!found){
            player.sendSystemMessage(Component.literal("Return to the portal...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
            return;
         }
         
         DialogHelper.sendDialog(List.of(player), new Dialog(new ArrayList<>(Arrays.asList(
               Component.literal("\n")
                     .append(Component.literal("You take your ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                     .append(Component.literal("Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal(" and place it against the portal and expose its rift generator.").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
               Component.literal("\n")
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                     .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                     .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                     .append(Component.literal("\nAn intriguing trinket. What are you planning?").withStyle(ChatFormatting.AQUA)),
               Component.literal("\n")
                     .append(Component.literal("The ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                     .append(Component.literal("Planeshifter").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                     .append(Component.literal(" attunes to the faintest sliver of dimensional energy within the abandoned portal...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)),
               Component.literal("")
                     .append(Component.literal("     ... and syncs with the other side!").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC, ChatFormatting.BOLD)),
               Component.literal("")
                     .append(Component.literal("\nA flickering, highly unstable rift opens. You hear a cacophony of voices, clicks, and pops from the other side.").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC)),
               Component.literal("")
                     .append(Component.literal("You feel millions of eyeless gazes upon you... and then...").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC)),
               Component.literal("")
                     .append(Component.literal("     ...A ROARING BLAST!!").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC, ChatFormatting.BOLD))
         )), new ArrayList<>(Arrays.asList(
               new Dialog.DialogSound(SoundEvents.SPYGLASS_USE, 2f, 0.5f),
               new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 1f, 1.4f),
               new Dialog.DialogSound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 1f, 1.4f),
               new Dialog.DialogSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 1f, 0.7f),
               new Dialog.DialogSound(SoundEvents.PORTAL_AMBIENT, 1f, 0.7f),
               new Dialog.DialogSound(SoundEvents.WARDEN_SONIC_CHARGE, 2f, 0.7f),
               new Dialog.DialogSound(SoundEvents.WARDEN_SONIC_BOOM, 2f, 1.3f)
         )), new int[]{0, 80, 80, 60, 40, 60, 50}, 1, 1, 0), true);
         Event.addEvent(new CeptyusOpenEvent(player));
         cEventFound.markForRemoval();
         setCanAttemptCeptyus(false);
      }else if(c2EventFound != null && c2EventFound.canProceed() && msg.equalsIgnoreCase("c_reach")){
         if(player.distanceToSqr(c2EventFound.getPosition()) < 40){
            c2EventFound.complete();
         }else{
            player.sendSystemMessage(Component.literal("Get closer to the rift...").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 0.5f, 0.7f);
         }
      }
   }
   
   public List<ArcanaSkin> getSkinsForItem(ArcanaItem item){
      ArrayList<ArcanaSkin> skins = new ArrayList<>();
      if(playerId.equals(UUID.fromString("fee11d1a-2536-4891-8757-25f3063a1dc1")) ||
            playerId.equals(UUID.fromString("883d74be-9200-4d06-b629-22a12ef398f5")) ||
            playerId.equals(UUID.fromString("5de15dee-0e50-4440-a19e-1a44da3f79dd")))
         return ArcanaSkin.getAllSkinsForItem(item);
      if(playerId.equals(UUID.fromString("f42869e3-3d93-45ba-be11-7ce76a77b64e"))) skins.add(ArcanaSkin.VESTIGE_WINGS);
      if(playerId.equals(UUID.fromString("66063256-a19f-4fe0-8c29-0faf413c426e"))) skins.add(ArcanaSkin.FEATHER_WINGS);
      if(playerId.equals(UUID.fromString("b25b760d-d167-437b-a948-9f6d0a426388")))
         skins.add(ArcanaSkin.RESPLENDENT_HARNESS);
      if(playerId.equals(UUID.fromString("0797c485-623b-4955-9af3-16c54e03099e")))
         skins.add(ArcanaSkin.COLEOPTERA_WINGS);
      if(playerId.equals(UUID.fromString("ff7289c6-5170-41f7-8195-79df491927d4")))
         skins.add(ArcanaSkin.CATGIRL_MEMENTO);
      if(playerId.equals(UUID.fromString("d134c5a2-1e99-48ac-b8f2-a814d25a1d17"))){
         skins.add(ArcanaSkin.LUNAR_BOW);
         skins.add(ArcanaSkin.LUNAR_QUIVER);
      }
      LocalDate today = LocalDate.now();
      boolean isTDOVOrPride = (today.getMonth() == Month.MARCH && today.getDayOfMonth() == 31) || today.getMonth() == Month.JUNE;
      if(isTDOVOrPride) skins.add(ArcanaSkin.AEQUALIS_RIGHTS);
      return skins.stream().filter(skin -> skin.getArcanaItem().getId().equals(item.getId())).toList();
   }
   
   public List<ArcanaSkin> getAllSkins(){
      List<ArcanaSkin> skins = new ArrayList<>();
      for(ArcanaItem arcanaItem : ArcanaRegistry.ARCANA_ITEMS){
         skins.addAll(getSkinsForItem(arcanaItem));
      }
      return skins;
   }
   
   public boolean hasAnySkin(){
      for(ArcanaItem arcanaItem : ArcanaRegistry.ARCANA_ITEMS){
         if(!getSkinsForItem(arcanaItem).isEmpty()) return true;
      }
      return false;
   }
   
   public boolean hasSkin(ArcanaSkin cataSkin){
      return getSkinsForItem(cataSkin.getArcanaItem()).contains(cataSkin);
   }
}
