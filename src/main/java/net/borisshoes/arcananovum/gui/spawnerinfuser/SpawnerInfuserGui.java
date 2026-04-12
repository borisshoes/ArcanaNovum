package net.borisshoes.arcananovum.gui.spawnerinfuser;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.gui.SoulstoneSlot;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;


public class SpawnerInfuserGui extends SimpleGui {
   private final SpawnerInfuserBlockEntity blockEntity;
   
   private final int[] minDelayLvls = {20, 30, 40, 50, 75, 100, 125, 150, 175, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000, 10000, 25000};
   private final int[] minDelayPoints = {256, 128, 96, 64, 32, 16, 8, 4, 2, 0, 1, 2, 4, 6, 8, 10, 12, 14, 16, 20};
   
   private final int[] maxDelayLvls = {30, 40, 50, 75, 100, 125, 150, 175, 200, 300, 400, 500, 800, 1000, 2000, 3000, 4000, 5000, 10000, 25000};
   private final int[] maxDelayPoints = {256, 192, 128, 96, 64, 48, 32, 16, 8, 4, 2, 1, 0, 1, 2, 4, 6, 8, 10, 12};
   
   private final int[] spawnRangeLvls = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
   private final int[] spawnRangePoints = {8, 4, 2, 0, 1, 2, 4, 8, 12, 16, 24, 32, 48, 64, 96};
   
   private final int[] playerRangeLvls = {2, 5, 8, 10, 12, 14, 16, 18, 20, 22, 25, 30, 35, 40, 45, 50, 60, 75, 90, 100};
   private final int[] playerRangePoints = {16, 8, 6, 4, 2, 1, 0, 1, 2, 4, 8, 16, 24, 32, 48, 64, 96, 128, 192, 256};
   
   private final int[] spawnCountLvls = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
   private final int[] spawnCountPoints = {8, 4, 2, 0, 1, 2, 4, 8, 16, 24, 32, 64, 96, 128, 192, 256};
   
   private final int[] maxEntitiesLvls = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 20, 25, 30, 40, 50};
   private final int[] maxEntitiesPoints = {16, 8, 4, 2, 1, 0, 1, 2, 4, 8, 16, 32, 64, 96, 128, 192, 256};
   
   
   public SpawnerInfuserGui(ServerPlayer player, SpawnerInfuserBlockEntity blockEntity){
      super(MenuType.GENERIC_9x5, player, false);
      this.blockEntity = blockEntity;
   }
   
   private void handleClick(String nbtKey, int[] lvlArr, int[] pointArr, boolean increase){
      CompoundTag stats = blockEntity.getSpawnerStats();
      if(isMaxedOrMinedOut(stats, nbtKey, lvlArr, increase)){
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 0.8f);
         return;
      }
      
      int points = blockEntity.getPoints();
      int spentPoints = blockEntity.getSpentPoints();
      int curVal = stats.getShortOr(nbtKey, (short) 0);
      int ind = indexOf(curVal, lvlArr);
      int newInd = increase ? Math.min(ind + 1, pointArr.length - 1) : Math.max(ind - 1, 0);
      int newVal = lvlArr[newInd];
      int pointDif = pointArr[ind] - pointArr[newInd];
      
      if(spentPoints - pointDif > points){
         player.sendSystemMessage(Component.literal("Not Enough Points").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 0.8f);
         return;
      }
      
      SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_CHIME, 1, (.5f + ((float) newInd / (lvlArr.length - 1))));
      blockEntity.setSpentPoints(spentPoints - pointDif);
      stats.putShort(nbtKey, (short) newVal);
      blockEntity.setSpawnerStats(stats);
      if(spentPoints - pointDif >= 1) ArcanaAchievements.grant(player, ArcanaAchievements.HUMBLE_NECROMANCER);
      if(pointArr[newInd] >= 256) ArcanaAchievements.grant(player, ArcanaAchievements.SCULK_HUNGERS);
      blockEntity.setChanged();
      blockEntity.refreshGuis();
   }
   
   public void build(){
      int points = blockEntity.getPoints();
      CompoundTag stats = blockEntity.getSpawnerStats();
      int spentPoints = blockEntity.getSpentPoints();
      
      ItemStack stone = blockEntity.getSoulstone();
      boolean hasStone = !stone.isEmpty();
      boolean spawnerDetected = detectSpawner();
      boolean usable = hasStone && spawnerDetected;
      
      if(usable){
         GuiHelper.outlineGUI(this, 0x0a6627, Component.literal("Use the arrows to configure the Infusion").withStyle(ChatFormatting.GREEN), List.of(
               TextUtils.removeItalics(Component.literal("")
                     .append(Component.literal("Apply a ").withStyle(ChatFormatting.DARK_AQUA))
                     .append(Component.literal("Redstone signal").withStyle(ChatFormatting.RED))
                     .append(Component.literal(" to activate the Infuser").withStyle(ChatFormatting.DARK_AQUA)))));
      }else if(spawnerDetected){
         GuiHelper.outlineGUI(this, ArcanaColors.DARK_COLOR, Component.literal("Insert a Soulstone in the top slot").withStyle(ChatFormatting.RED));
      }else{
         GuiHelper.outlineGUI(this, ArcanaColors.DARK_COLOR, Component.literal("A Spawner must be 2 blocks above the Infuser").withStyle(ChatFormatting.RED));
      }
      
      clearSlot(4);
      setSlot(4, new SoulstoneSlot(blockEntity.getInventory(), 0, 0, 0, true, false, null)); // Soulstone slot
      
      // Clear all interior slots unconditionally to prevent ghost slots and stale callbacks
      for(int i = 1; i <= 7; i++){
         clearSlot(i + 9);
         clearSlot(i + 18);
         clearSlot(i + 27);
      }
      clearSlot(40);
      
      if(usable){
         setSlot(40, new SpawnerInfuserPointsSlot(blockEntity.getInventory(), 1, 0, 0)); // Points item slot
         
         int bonusCap = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SPAWNER_INFUSER_EXTRA_CAPACITY_PER_LVL).get(ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(), ArcanaAugments.SOUL_RESERVOIR));
         int souls = Soulstone.getSouls(stone);
         int soulstoneTier = Soulstone.soulsToTier(Math.max(souls, 0));
         int maxPoints = SpawnerInfuser.pointsFromTier[soulstoneTier] + bonusCap;
         int pips = maxPoints > 0 ? (int) Math.ceil((double) points * 3.0 / (double) maxPoints) : 0;
         
         for(int i = 0; i < 3; i++){
            GuiElementBuilder pipItem = points == maxPoints ? new GuiElementBuilder(Items.SEA_LANTERN) : pips > i ? new GuiElementBuilder(Items.VERDANT_FROGLIGHT) : new GuiElementBuilder(Items.OBSIDIAN);
            pipItem.setName(Component.literal("")
                  .append(Component.literal("Upgrade Points: ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal(points + "/" + maxPoints).withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" (Tier " + soulstoneTier + ")").withStyle(ChatFormatting.AQUA)));
            pipItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Allocated Points: ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal(spentPoints + "/" + points).withStyle(ChatFormatting.GREEN))));
            pipItem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
            pipItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("Add ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Nether Stars").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to the bottom slot to add ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Upgrade Points").withStyle(ChatFormatting.GREEN))));
            pipItem.addLoreLine(TextUtils.removeItalics(Component.literal("")
                  .append(Component.literal("A higher tier ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Soulstone").withStyle(ChatFormatting.DARK_RED))
                  .append(Component.literal(" increases your ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Max Upgrade Points").withStyle(ChatFormatting.GREEN))));
            setSlot(31 - i * 9, pipItem);
         }
         
         buildStatColumn(stats, 10, "MinSpawnDelay", "Minimum Spawn Time", "ticks",
               Items.GUNPOWDER, "The minimum amount of time between mob spawns",
               MobEffects.SLOWNESS.value().getColor(), minDelayLvls, minDelayPoints);
         
         buildStatColumn(stats, 11, "MaxSpawnDelay", "Maximum Spawn Time", "ticks",
               Items.SUGAR, "The maximum amount of time between mob spawns",
               MobEffects.SPEED.value().getColor(), maxDelayLvls, maxDelayPoints);
         
         buildStatColumn(stats, 12, "SpawnRange", "Spawn Range", "blocks",
               Items.NETHER_STAR, "The range in which mobs can spawn",
               MobEffects.SLOW_FALLING.value().getColor(), spawnRangeLvls, spawnRangePoints);
         
         buildStatColumn(stats, 14, "SpawnCount", "Spawn Count", "mobs",
               Items.ZOMBIE_HEAD, "The amount of spawn attempts each cycle",
               MobEffects.POISON.value().getColor(), spawnCountLvls, spawnCountPoints);
         
         buildStatColumn(stats, 15, "MaxNearbyEntities", "Max Entities", "mobs",
               Items.WITHER_SKELETON_SKULL, "The amount of mobs in range before the spawner is disabled",
               MobEffects.WATER_BREATHING.value().getColor(), maxEntitiesLvls, maxEntitiesPoints);
         
         buildStatColumn(stats, 16, "RequiredPlayerRange", "Player Range", "blocks",
               Items.PLAYER_HEAD, "How close a player has to be for the spawner to work",
               MobEffects.RESISTANCE.value().getColor(), playerRangeLvls, playerRangePoints);
      }
      
      setTitle(ArcanaRegistry.SPAWNER_INFUSER.getTranslatedName());
   }
   
   private void buildStatColumn(CompoundTag stats, int baseSlot, String nbtKey, String title, String units, Item icon, String description, int color, int[] lvlArr, int[] pointArr){
      int value = stats.getShortOr(nbtKey, (short) 0);
      int ind = indexOf(value, lvlArr);
      int pointC = pointArr[ind];
      
      GuiElementBuilder incArrow = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_UP, color)).hideDefaultTooltip();
      makeArrowItem(incArrow, true, title, units, ind, pointArr, lvlArr);
      incArrow.setCallback(clickType -> handleClick(nbtKey, lvlArr, pointArr, true));
      GuiElementBuilder decArrow = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.ARROW_DOWN, color)).hideDefaultTooltip();
      makeArrowItem(decArrow, false, title, units, ind, pointArr, lvlArr);
      decArrow.setCallback(clickType -> handleClick(nbtKey, lvlArr, pointArr, false));
      GuiElementBuilder mainItem = new GuiElementBuilder(icon);
      makeMainItem(mainItem, title, description, units, value, pointC);
      GuiElementBuilder maxedOut = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.CANCEL_COLOR, color)).hideDefaultTooltip();
      makeMaxedOutItem(maxedOut);
      GuiElementBuilder minedOut = GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.CANCEL_COLOR, color)).hideDefaultTooltip();
      makeMinedOutItem(minedOut);
      
      setSlot(baseSlot, ind == lvlArr.length - 1 ? maxedOut : incArrow);
      setSlot(baseSlot + 9, mainItem);
      setSlot(baseSlot + 18, ind == 0 ? minedOut : decArrow);
   }
   
   private boolean detectSpawner(){
      Level world = blockEntity.getLevel();
      if(world == null) return false;
      BlockPos pos = blockEntity.getBlockPos().offset(0, 2, 0);
      BlockEntity targetEntity = world.getBlockEntity(pos);
      BlockState blockState = world.getBlockState(pos);
      return blockState.is(Blocks.SPAWNER) && targetEntity instanceof SpawnerBlockEntity;
   }
   
   private int indexOf(int value, int[] arr){
      if(arr.length == 0) return 0;
      if(value <= arr[0]) return 0;
      for(int i = 1; i < arr.length; i++){
         int cur = arr[i];
         if(cur == value){
            return i;
         }else if(cur > value){
            return i - 1;
         }
      }
      return arr.length - 1;
   }
   
   private void makeMainItem(GuiElementBuilder elem, String title, String description, String units, int value, int points){
      elem.setName(Component.literal("")
            .append(Component.literal(title + ": ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(value + " " + units).withStyle(ChatFormatting.GREEN)));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal(description).withStyle(ChatFormatting.DARK_AQUA))));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Current Value: ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(value + " " + units).withStyle(ChatFormatting.GREEN))));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Point Allocation: ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(points + " points").withStyle(ChatFormatting.GREEN))));
   }
   
   private void makeArrowItem(GuiElementBuilder elem, boolean increase, String title, String units, int ind, int[] pointArr, int[] lvlArr){
      int newInd = increase ? Math.min(ind + 1, pointArr.length - 1) : Math.max(ind - 1, 0);
      int pointDiff = pointArr[ind] - pointArr[newInd];
      String incDecStr = increase ? "Increase" : "Decrease";
      int newVal = increase ? lvlArr[Math.min(ind + 1, lvlArr.length - 1)] : lvlArr[Math.max(ind - 1, 0)];
      
      elem.setName(Component.literal("")
            .append(Component.literal(incDecStr + " " + title).withStyle(ChatFormatting.DARK_GREEN)));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal(incDecStr + ": ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(lvlArr[ind] + " " + units).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(newVal + " " + units).withStyle(ChatFormatting.GREEN))));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("Point Cost: ").withStyle(ChatFormatting.DARK_AQUA))
            .append(pointDiff > 0 ? Component.literal("+" + pointDiff + " points").withStyle(ChatFormatting.GREEN) : Component.literal(pointDiff + " points").withStyle(ChatFormatting.RED))));
   }
   
   private void makeMaxedOutItem(GuiElementBuilder elem){
      elem.setName(Component.literal("")
            .append(Component.literal("Maximum Value").withStyle(ChatFormatting.GREEN)));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("You cannot increase this stat further").withStyle(ChatFormatting.DARK_AQUA))));
   }
   
   private void makeMinedOutItem(GuiElementBuilder elem){
      elem.setName(Component.literal("")
            .append(Component.literal("Minimum Value").withStyle(ChatFormatting.GREEN)));
      elem.addLoreLine(TextUtils.removeItalics(Component.literal("")
            .append(Component.literal("You cannot decrease this stat further").withStyle(ChatFormatting.DARK_AQUA))));
   }
   
   private boolean isMaxedOrMinedOut(CompoundTag stats, String key, int[] arr, boolean increase){
      int ind = indexOf(stats.getShortOr(key, (short) 0), arr);
      return increase ? ind == arr.length - 1 : ind == 0;
   }
   
   @Override
   public void onTick(){
      Level world = blockEntity.getLevel();
      if(world == null || world.getBlockEntity(blockEntity.getBlockPos()) != blockEntity){
         this.close();
      }
      super.onTick();
   }
   
   @Override
   public void close(){
      blockEntity.removePlayer(player);
      super.close();
   }
}
