package net.borisshoes.arcananovum.gui.spawnerinfuser;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.SpawnerInfuser;
import net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity;
import net.borisshoes.arcananovum.gui.SoulstoneSlot;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GuiHelper;
import net.borisshoes.borislib.utils.SoundUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;


public class SpawnerInfuserGui extends SimpleGui {
   private final SpawnerInfuserBlockEntity blockEntity;
   private final Level world;
   
   private final int[] minSpeedLvls = {20,30,40,50,75,100,125,150,175,200,300,400,500,1000,2000,3000,4000,5000,10000,25000};
   private final int[] minSpeedPoints = {256,128,96,64,32,16,8,4,2,0,1,2,4,6,8,10,12,14,16,20};
   
   private final int[] maxSpeedLvls = {30,40,50,75,100,125,150,175,200,300,400,500,800,1000,2000,3000,4000,5000,10000,25000};
   private final int[] maxSpeedPoints = {256,192,128,96,64,48,32,16,8,4,2,1,0,1,2,4,6,8,10,12};
   
   private final int[] spawnRangeLvls = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
   private final int[] spawnRangePoints = {8,4,2,0,1,2,4,8,12,16,24,32,48,64,96};
   
   private final int[] playerRangeLvls;
   private final int[] playerRangePoints = {16,8,6,4,2,1,0,1,2,4,8,16,24,32,48,64,96,128,192,256};
   
   private final int[] spawnCountLvls = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
   private final int[] spawnCountPoints = {8,4,2,0,1,2,4,8,16,24,32,64,96,128,192,256};
   
   private final int[] maxEntitiesLvls = {1,2,3,4,5,6,7,8,9,10,12,15,20,25,30,40,50};
   private final int[] maxEntitiesPoints = {16,8,4,2,1,0,1,2,4,8,16,32,64,96,128,192,256};
   
   
   public SpawnerInfuserGui(ServerPlayer player, SpawnerInfuserBlockEntity blockEntity, Level world){
      super(MenuType.GENERIC_9x5, player, false);
      this.blockEntity = blockEntity;
      this.world = world;
      
      boolean emulator = ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.SPIRIT_EMULATOR.id) >= 1;
      if(emulator){
         this.playerRangeLvls = new int[]{2, 5, 8, 10, 12, 14, 16, 18, 20, 22, 25, 30, 35, 40, 45, 50, 60, 75, 90, 9999};
      }else{
         this.playerRangeLvls = new int[]{2, 5, 8, 10, 12, 14, 16, 18, 20, 22, 25, 30, 35, 40, 45, 50, 60, 75, 90, 100};
      }
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      int[] clickable = {10,11,12,14,15,16,28,29,30,32,33,34};
      if(!Arrays.stream(clickable).boxed().toList().contains(index)) return true;
      if(getSlot(index).getItemStack().is(Items.STRUCTURE_VOID)){
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1,0.8f);
         return true;
      }
      
      int points = blockEntity.getPoints();
      int spentPoints = blockEntity.getSpentPoints();
      CompoundTag stats = blockEntity.getSpawnerStats();
      
      int pointDif; // Negative if costs points, positive if refunds points
      String nbtKey;
      int newVal;
      int newInd;

      boolean increase = true;
      int[] lvlArr;
      int[] pointArr;
      
      if(index == 10){
         nbtKey = "MinSpawnDelay";
         lvlArr = minSpeedLvls;
         pointArr = minSpeedPoints;
      }else if(index == 11){
         nbtKey = "MaxSpawnDelay";
         lvlArr = maxSpeedLvls;
         pointArr = maxSpeedPoints;
      }else if(index == 12){
         nbtKey = "SpawnRange";
         lvlArr = spawnRangeLvls;
         pointArr = spawnRangePoints;
      }else if(index == 14){
         nbtKey = "SpawnCount";
         lvlArr = spawnCountLvls;
         pointArr = spawnCountPoints;
      }else if(index == 15){
         nbtKey = "MaxNearbyEntities";
         lvlArr = maxEntitiesLvls;
         pointArr = maxEntitiesPoints;
      }else if(index == 16){
         nbtKey = "RequiredPlayerRange";
         lvlArr = playerRangeLvls;
         pointArr = playerRangePoints;
      }else if(index == 28){
         nbtKey = "MinSpawnDelay";
         lvlArr = minSpeedLvls;
         pointArr = minSpeedPoints;
         increase = false;
      }else if(index == 29){
         nbtKey = "MaxSpawnDelay";
         lvlArr = maxSpeedLvls;
         pointArr = maxSpeedPoints;
         increase = false;
      }else if(index == 30){
         nbtKey = "SpawnRange";
         lvlArr = spawnRangeLvls;
         pointArr = spawnRangePoints;
         increase = false;
      }else if(index == 32){
         nbtKey = "SpawnCount";
         lvlArr = spawnCountLvls;
         pointArr = spawnCountPoints;
         increase = false;
      }else if(index == 33){
         nbtKey = "MaxNearbyEntities";
         lvlArr = maxEntitiesLvls;
         pointArr = maxEntitiesPoints;
         increase = false;
      }else if(index == 34){
         nbtKey = "RequiredPlayerRange";
         lvlArr = playerRangeLvls;
         pointArr = playerRangePoints;
         increase = false;
      }else{
         return true;
      }
      
      int curVal = stats.getShortOr(nbtKey, (short) 0);
      int ind = indexOf(curVal,lvlArr);
      newInd = increase ? Math.min(ind+1,pointArr.length-1) : Math.max(ind-1,0);
      newVal = lvlArr[newInd];
      pointDif = pointArr[ind] - pointArr[newInd];
   
      if(spentPoints - pointDif > points){
         player.displayClientMessage(Component.literal("Not Enough Points").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.FIRE_EXTINGUISH, 1, 0.8f);
         return true;
      }
   
      SoundUtils.playSongToPlayer(player, SoundEvents.NOTE_BLOCK_CHIME, 1, (.5f+((float)newInd/(lvlArr.length-1))));
      blockEntity.setSpentPoints(spentPoints-pointDif);
      stats.putShort(nbtKey, (short) newVal);
      blockEntity.setSpawnerStats(stats);
      if(spentPoints-pointDif >= 1) ArcanaAchievements.grant(player,ArcanaAchievements.HUMBLE_NECROMANCER.id);
      if(pointArr[newInd] >= 256) ArcanaAchievements.grant(player,ArcanaAchievements.SCULK_HUNGERS.id);
      
      blockEntity.refreshGuis();
   
      return true;
   }
   
   
   public void build(){
      int points = blockEntity.getPoints();
      CompoundTag stats = blockEntity.getSpawnerStats();
      int spentPoints = blockEntity.getSpentPoints();
      
      boolean hasStone = !blockEntity.getSoulstone().isEmpty();
      boolean spawnerDetected = detectSpawner();
      boolean usable = hasStone && spawnerDetected;
      
      if(usable){
         GuiHelper.outlineGUI(this,0x0a6627, Component.literal("Use the arrows to configure the Infusion").withStyle(ChatFormatting.GREEN), List.of(
               TextUtils.removeItalics(Component.literal("")
               .append(Component.literal("Apply a ").withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal("Redstone signal").withStyle(ChatFormatting.RED))
               .append(Component.literal(" to activate the Infuser").withStyle(ChatFormatting.DARK_AQUA)))));
      }else if(spawnerDetected){
         GuiHelper.outlineGUI(this,ArcanaColors.DARK_COLOR, Component.literal("Insert a Soulstone in the top slot").withStyle(ChatFormatting.RED));
      }else{
         GuiHelper.outlineGUI(this,ArcanaColors.DARK_COLOR, Component.literal("A Spawner must be 2 blocks above the Infuser").withStyle(ChatFormatting.RED));
      }
      
      clearSlot(4);
      setSlotRedirect(4, new SoulstoneSlot(blockEntity.getInventory(),0,0,0,true,false,null)); // Soulstone slot
   
      for(int i = 1; i <= 7; i++){
         clearSlot(i+9);
         clearSlot(i+18);
         clearSlot(i+27);
      }
   
      ItemStack stone = blockEntity.getSoulstone();
      
      if(usable){
         setSlotRedirect(40, new SpawnerInfuserPointsSlot(blockEntity.getInventory(),1,0,0)); // Points item slot
   
         int bonusCap = new int[]{0,64,128,192,256,352}[ArcanaAugments.getAugmentFromMap(blockEntity.getAugments(),ArcanaAugments.SOUL_RESERVOIR.id)];
         int soulstoneTier = Soulstone.soulsToTier(Soulstone.getSouls(stone));
         int maxPoints = SpawnerInfuser.pointsFromTier[soulstoneTier] + bonusCap;
         int pips = (int)Math.ceil((double)points*3.0/(double)maxPoints);
   
         for(int i = 0; i < 3; i++){
            GuiElementBuilder pipItem = points == maxPoints ? new GuiElementBuilder(Items.SEA_LANTERN) : pips > i ? new GuiElementBuilder(Items.VERDANT_FROGLIGHT) : new GuiElementBuilder(Items.OBSIDIAN);
            pipItem.setName((Component.literal("")
                  .append(Component.literal("Upgrade Points: ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal(points+"/"+maxPoints).withStyle(ChatFormatting.GREEN))
                  .append(Component.literal(" (Tier "+soulstoneTier+")").withStyle(ChatFormatting.AQUA))));
            pipItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Allocated Points: ").withStyle(ChatFormatting.DARK_GREEN))
                  .append(Component.literal(spentPoints+"/"+points).withStyle(ChatFormatting.GREEN)))));
            pipItem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
            pipItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("Add ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Nether Stars").withStyle(ChatFormatting.AQUA))
                  .append(Component.literal(" to the bottom slot to add ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Upgrade Points").withStyle(ChatFormatting.GREEN)))));
            pipItem.addLoreLine(TextUtils.removeItalics((Component.literal("")
                  .append(Component.literal("A higher tier ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Soulstone").withStyle(ChatFormatting.DARK_RED))
                  .append(Component.literal(" increases your ").withStyle(ChatFormatting.DARK_AQUA))
                  .append(Component.literal("Max Upgrade Points").withStyle(ChatFormatting.GREEN)))));
            setSlot(31-i*9,pipItem);
         }
         
         GuiElementBuilder maxedOut = new GuiElementBuilder(Items.STRUCTURE_VOID);
         maxedOut.setName((Component.literal("")
               .append(Component.literal("Maximum Value").withStyle(ChatFormatting.GREEN))));
         maxedOut.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("You cannot increase this stat further").withStyle(ChatFormatting.DARK_AQUA)))));
   
         GuiElementBuilder minedOut = new GuiElementBuilder(Items.STRUCTURE_VOID);
         minedOut.setName((Component.literal("")
               .append(Component.literal("Minimum Value").withStyle(ChatFormatting.GREEN))));
         minedOut.addLoreLine(TextUtils.removeItalics((Component.literal("")
               .append(Component.literal("You cannot decrease this stat further").withStyle(ChatFormatting.DARK_AQUA)))));
         
         int minSpeed = stats.getShortOr("MinSpawnDelay", (short) 0);
         int minSpeedInd = indexOf(minSpeed,minSpeedLvls);
         int minSpeedPointC = minSpeedPoints[minSpeedInd];
         ItemStack minSpeedArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.SLOWNESS);
         GuiElementBuilder minSpeedIncArrow = GuiElementBuilder.from(minSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(minSpeedIncArrow,true,"Minimum Spawn Time","ticks",minSpeedInd,minSpeedPoints,minSpeedLvls);
         GuiElementBuilder minSpeedDecArrow = GuiElementBuilder.from(minSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(minSpeedDecArrow,false,"Minimum Spawn Time","ticks",minSpeedInd,minSpeedPoints,minSpeedLvls);
         GuiElementBuilder minSpeedItem = new GuiElementBuilder(Items.GUNPOWDER);
         makeMainItem(minSpeedItem,"Minimum Spawn Time","The minimum amount of time between mob spawns","ticks",minSpeed,minSpeedPointC);
         
         setSlot(10,minSpeedInd == minSpeedLvls.length-1 ? maxedOut : minSpeedIncArrow);
         setSlot(19,minSpeedItem);
         setSlot(28,minSpeedInd == 0 ? minedOut : minSpeedDecArrow);
         
         int maxSpeed = stats.getShortOr("MaxSpawnDelay", (short) 0);
         int maxSpeedInd = indexOf(maxSpeed,maxSpeedLvls);
         int maxSpeedPointC = maxSpeedPoints[maxSpeedInd];
         ItemStack maxSpeedArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.SWIFTNESS);
         GuiElementBuilder maxSpeedIncArrow = GuiElementBuilder.from(maxSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(maxSpeedIncArrow,true,"Maximum Spawn Time","ticks",maxSpeedInd,maxSpeedPoints,maxSpeedLvls);
         GuiElementBuilder maxSpeedDecArrow = GuiElementBuilder.from(maxSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(maxSpeedDecArrow,false,"Maximum Spawn Time","ticks",maxSpeedInd,maxSpeedPoints,maxSpeedLvls);
         GuiElementBuilder maxSpeedItem = new GuiElementBuilder(Items.SUGAR);
         makeMainItem(maxSpeedItem,"Maximum Spawn Time","The maximum amount of time between mob spawns","ticks",maxSpeed,maxSpeedPointC);
   
         setSlot(11,maxSpeedInd == maxSpeedLvls.length-1 ? maxedOut :maxSpeedIncArrow);
         setSlot(20,maxSpeedItem);
         setSlot(29,maxSpeedInd == 0 ? minedOut : maxSpeedDecArrow);
         
         int spawnRange = stats.getShortOr("SpawnRange", (short) 0);
         int spawnRangeInd = indexOf(spawnRange,spawnRangeLvls);
         int spawnRangePointC = spawnRangePoints[spawnRangeInd];
         ItemStack spawnRangeArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.SLOW_FALLING);
         GuiElementBuilder spawnRangeIncArrow = GuiElementBuilder.from(spawnRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnRangeIncArrow,true,"Spawn Range","blocks",spawnRangeInd,spawnRangePoints,spawnRangeLvls);
         GuiElementBuilder spawnRangeDecArrow = GuiElementBuilder.from(spawnRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnRangeDecArrow,false,"Spawn Range","blocks",spawnRangeInd,spawnRangePoints,spawnRangeLvls);
         GuiElementBuilder spawnRangeItem = new GuiElementBuilder(Items.NETHER_STAR);
         makeMainItem(spawnRangeItem,"Spawn Range","The range in which mobs can spawn","blocks",spawnRange,spawnRangePointC);
   
         setSlot(12,spawnRangeInd == spawnRangeLvls.length-1 ? maxedOut : spawnRangeIncArrow);
         setSlot(21,spawnRangeItem);
         setSlot(30,spawnRangeInd == 0 ? minedOut : spawnRangeDecArrow);
         
         int spawnCount = stats.getShortOr("SpawnCount", (short) 0);
         int spawnCountInd = indexOf(spawnCount,spawnCountLvls);
         int spawnCountPointC = spawnCountPoints[spawnCountInd];
         ItemStack spawnCountArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.POISON);
         GuiElementBuilder spawnCountIncArrow = GuiElementBuilder.from(spawnCountArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnCountIncArrow,true,"Spawn Count","mobs",spawnCountInd,spawnCountPoints,spawnCountLvls);
         GuiElementBuilder spawnCountDecArrow = GuiElementBuilder.from(spawnCountArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnCountDecArrow,false,"Spawn Count","mobs",spawnCountInd,spawnCountPoints,spawnCountLvls);
         GuiElementBuilder spawnCountItem = new GuiElementBuilder(Items.ZOMBIE_HEAD);
         makeMainItem(spawnCountItem,"Spawn Count","The amount of spawn attempts each cycle","mobs",spawnCount,spawnCountPointC);
   
         setSlot(14,spawnCountInd == spawnCountLvls.length-1 ? maxedOut : spawnCountIncArrow);
         setSlot(23,spawnCountItem);
         setSlot(32,spawnCountInd == 0 ? minedOut : spawnCountDecArrow);
         
         int maxEntities = stats.getShortOr("MaxNearbyEntities", (short) 0);
         int maxEntitiesInd = indexOf(maxEntities,maxEntitiesLvls);
         int maxEntitiesPointC = maxEntitiesPoints[maxEntitiesInd];
         ItemStack maxEntitiesArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.WATER_BREATHING);
         GuiElementBuilder maxEntitiesIncArrow = GuiElementBuilder.from(maxEntitiesArrowStack).hideDefaultTooltip();
         makeArrowItem(maxEntitiesIncArrow,true,"Max Entities","mobs",maxEntitiesInd,maxEntitiesPoints,maxEntitiesLvls);
         GuiElementBuilder maxEntitiesDecArrow = GuiElementBuilder.from(maxEntitiesArrowStack).hideDefaultTooltip();
         makeArrowItem(maxEntitiesDecArrow,false,"Max Entities","mobs",maxEntitiesInd,maxEntitiesPoints,maxEntitiesLvls);
         GuiElementBuilder maxEntitiesItem = new GuiElementBuilder(Items.WITHER_SKELETON_SKULL);
         makeMainItem(maxEntitiesItem,"Max Entities","The amount of mobs in range before the spawner is disabled","mobs",maxEntities,maxEntitiesPointC);
   
         setSlot(15,maxEntitiesInd == maxEntitiesLvls.length-1 ? maxedOut : maxEntitiesIncArrow);
         setSlot(24,maxEntitiesItem);
         setSlot(33,maxEntitiesInd == 0 ? minedOut : maxEntitiesDecArrow);
         
         int playerRange = stats.getShortOr("RequiredPlayerRange", (short) 0);
         int playerRangeInd = indexOf(playerRange,playerRangeLvls);
         int playerRangePointC = playerRangePoints[playerRangeInd];
         ItemStack playerRangeArrowStack = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.TURTLE_MASTER);
         GuiElementBuilder playerRangeIncArrow = GuiElementBuilder.from(playerRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(playerRangeIncArrow,true,"Player Range","blocks",playerRangeInd,playerRangePoints,playerRangeLvls);
         GuiElementBuilder playerRangeDecArrow = GuiElementBuilder.from(playerRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(playerRangeDecArrow,false,"Player Range","blocks",playerRangeInd,playerRangePoints,playerRangeLvls);
         GuiElementBuilder playerRangeItem = new GuiElementBuilder(Items.PLAYER_HEAD);
         makeMainItem(playerRangeItem,"Player Range","How close a player has to be for the spawner to work","blocks",playerRange,playerRangePointC);
   
         setSlot(16,playerRangeInd == playerRangeLvls.length-1 ? maxedOut : playerRangeIncArrow);
         setSlot(25,playerRangeItem);
         setSlot(34,playerRangeInd == 0 ? minedOut : playerRangeDecArrow);
      }
      
      setTitle(ArcanaRegistry.SPAWNER_INFUSER.getTranslatedName());
   }
   
   private boolean detectSpawner(){
      BlockPos pos = blockEntity.getBlockPos().offset(0,2,0);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      BlockState blockState = world.getBlockState(pos);
      return blockState.is(Blocks.SPAWNER) && blockEntity instanceof SpawnerBlockEntity;
   }
   
   private int indexOf(int value, int[] arr){
      for(int i = 1; i < arr.length; i++){
         int cur = arr[i];
         if(cur == value){
            return i;
         }else if(cur > value){
            return i-1;
         }
      }
      return arr.length-1;
   }
   
   private void makeMainItem(GuiElementBuilder elem, String title, String description, String units, int value, int points){
      elem.setName((Component.literal("")
            .append(Component.literal(title+": ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(value+" "+units).withStyle(ChatFormatting.GREEN))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal(description).withStyle(ChatFormatting.DARK_AQUA)))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal(""))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Current Value: ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(value+" "+units).withStyle(ChatFormatting.GREEN)))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Point Allocation: ").withStyle(ChatFormatting.DARK_GREEN))
            .append(Component.literal(points+" points").withStyle(ChatFormatting.GREEN)))));
   }
   
   private void makeArrowItem(GuiElementBuilder elem, boolean increase, String title, String units, int ind, int[] pointArr, int[] lvlArr){
      int newInd = increase ? Math.min(ind+1,pointArr.length-1) : Math.max(ind-1,0);
      int pointDiff = pointArr[ind] - pointArr[newInd];
      String incDecStr = increase ? "Increase" : "Decrease";
      int newVal = increase ? lvlArr[Math.min(ind+1,lvlArr.length-1)] : lvlArr[Math.max(ind-1,0)];
      
      elem.setName((Component.literal("")
            .append(Component.literal(incDecStr+" "+title).withStyle(ChatFormatting.DARK_GREEN))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal(incDecStr+": ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(lvlArr[ind]+" "+units).withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" -> ").withStyle(ChatFormatting.DARK_AQUA))
            .append(Component.literal(newVal+" "+units).withStyle(ChatFormatting.GREEN)))));
      elem.addLoreLine(TextUtils.removeItalics((Component.literal("")
            .append(Component.literal("Point Cost: ").withStyle(ChatFormatting.DARK_AQUA))
            .append(pointDiff > 0 ? Component.literal("+"+pointDiff+" points").withStyle(ChatFormatting.GREEN) : Component.literal(pointDiff+" points").withStyle(ChatFormatting.RED)))));
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
