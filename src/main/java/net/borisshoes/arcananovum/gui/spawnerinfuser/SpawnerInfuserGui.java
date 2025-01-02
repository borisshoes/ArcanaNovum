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
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;


public class SpawnerInfuserGui extends SimpleGui {
   private final SpawnerInfuserBlockEntity blockEntity;
   private final World world;
   
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
   
   
   public SpawnerInfuserGui(ServerPlayerEntity player, SpawnerInfuserBlockEntity blockEntity, World world){
      super(ScreenHandlerType.GENERIC_9X5, player, false);
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
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      int[] clickable = {10,11,12,14,15,16,28,29,30,32,33,34};
      if(!Arrays.stream(clickable).boxed().toList().contains(index)) return true;
      if(getSlot(index).getItemStack().isOf(Items.STRUCTURE_VOID)){
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,0.8f);
         return true;
      }
      
      int points = blockEntity.getPoints();
      int spentPoints = blockEntity.getSpentPoints();
      NbtCompound stats = blockEntity.getSpawnerStats();
      
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
   
      int curVal = stats.getShort(nbtKey);
      int ind = indexOf(curVal,lvlArr);
      newInd = increase ? Math.min(ind+1,pointArr.length-1) : Math.max(ind-1,0);
      newVal = lvlArr[newInd];
      pointDif = pointArr[ind] - pointArr[newInd];
   
      if(spentPoints - pointDif > points){
         player.sendMessage(Text.literal("Not Enough Points").formatted(Formatting.RED,Formatting.ITALIC),true);
         SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, 0.8f);
         return true;
      }
   
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1, (.5f+((float)newInd/(lvlArr.length-1))));
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
      NbtCompound stats = blockEntity.getSpawnerStats();
      int spentPoints = blockEntity.getSpentPoints();
      
      boolean hasStone = !blockEntity.getSoulstone().isEmpty();
      boolean spawnerDetected = detectSpawner();
      boolean usable = hasStone && spawnerDetected;
      
      if(usable){
         MiscUtils.outlineGUI(this,0x0a6627,Text.literal("Use the arrows to configure the Infusion").formatted(Formatting.GREEN), List.of(
               TextUtils.removeItalics(Text.literal("")
               .append(Text.literal("Apply a ").formatted(Formatting.DARK_AQUA))
               .append(Text.literal("Redstone signal").formatted(Formatting.RED))
               .append(Text.literal(" to activate the Infuser").formatted(Formatting.DARK_AQUA)))));
      }else if(spawnerDetected){
         MiscUtils.outlineGUI(this,ArcanaColors.DARK_COLOR,Text.literal("Insert a Soulstone in the top slot").formatted(Formatting.RED));
      }else{
         MiscUtils.outlineGUI(this,ArcanaColors.DARK_COLOR,Text.literal("A Spawner must be 2 blocks above the Infuser").formatted(Formatting.RED));
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
            pipItem.setName((Text.literal("")
                  .append(Text.literal("Upgrade Points: ").formatted(Formatting.DARK_GREEN))
                  .append(Text.literal(points+"/"+maxPoints).formatted(Formatting.GREEN))
                  .append(Text.literal(" (Tier "+soulstoneTier+")").formatted(Formatting.AQUA))));
            pipItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Allocated Points: ").formatted(Formatting.DARK_GREEN))
                  .append(Text.literal(spentPoints+"/"+points).formatted(Formatting.GREEN)))));
            pipItem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
            pipItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("Add ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal("Nether Stars").formatted(Formatting.AQUA))
                  .append(Text.literal(" to the bottom slot to add ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal("Upgrade Points").formatted(Formatting.GREEN)))));
            pipItem.addLoreLine(TextUtils.removeItalics((Text.literal("")
                  .append(Text.literal("A higher tier ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal("Soulstone").formatted(Formatting.DARK_RED))
                  .append(Text.literal(" increases your ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal("Max Upgrade Points").formatted(Formatting.GREEN)))));
            setSlot(31-i*9,pipItem);
         }
         
         GuiElementBuilder maxedOut = new GuiElementBuilder(Items.STRUCTURE_VOID);
         maxedOut.setName((Text.literal("")
               .append(Text.literal("Maximum Value").formatted(Formatting.GREEN))));
         maxedOut.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("You cannot increase this stat further").formatted(Formatting.DARK_AQUA)))));
   
         GuiElementBuilder minedOut = new GuiElementBuilder(Items.STRUCTURE_VOID);
         minedOut.setName((Text.literal("")
               .append(Text.literal("Minimum Value").formatted(Formatting.GREEN))));
         minedOut.addLoreLine(TextUtils.removeItalics((Text.literal("")
               .append(Text.literal("You cannot decrease this stat further").formatted(Formatting.DARK_AQUA)))));
         
         int minSpeed = stats.getShort("MinSpawnDelay");
         int minSpeedInd = indexOf(minSpeed,minSpeedLvls);
         int minSpeedPointC = minSpeedPoints[minSpeedInd];
         ItemStack minSpeedArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.SLOWNESS);
         GuiElementBuilder minSpeedIncArrow = GuiElementBuilder.from(minSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(minSpeedIncArrow,true,"Minimum Spawn Time","ticks",minSpeedInd,minSpeedPoints,minSpeedLvls);
         GuiElementBuilder minSpeedDecArrow = GuiElementBuilder.from(minSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(minSpeedDecArrow,false,"Minimum Spawn Time","ticks",minSpeedInd,minSpeedPoints,minSpeedLvls);
         GuiElementBuilder minSpeedItem = new GuiElementBuilder(Items.GUNPOWDER);
         makeMainItem(minSpeedItem,"Minimum Spawn Time","The minimum amount of time between mob spawns","ticks",minSpeed,minSpeedPointC);
         
         setSlot(10,minSpeedInd == minSpeedLvls.length-1 ? maxedOut : minSpeedIncArrow);
         setSlot(19,minSpeedItem);
         setSlot(28,minSpeedInd == 0 ? minedOut : minSpeedDecArrow);
   
         int maxSpeed = stats.getShort("MaxSpawnDelay");
         int maxSpeedInd = indexOf(maxSpeed,maxSpeedLvls);
         int maxSpeedPointC = maxSpeedPoints[maxSpeedInd];
         ItemStack maxSpeedArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.SWIFTNESS);
         GuiElementBuilder maxSpeedIncArrow = GuiElementBuilder.from(maxSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(maxSpeedIncArrow,true,"Maximum Spawn Time","ticks",maxSpeedInd,maxSpeedPoints,maxSpeedLvls);
         GuiElementBuilder maxSpeedDecArrow = GuiElementBuilder.from(maxSpeedArrowStack).hideDefaultTooltip();
         makeArrowItem(maxSpeedDecArrow,false,"Maximum Spawn Time","ticks",maxSpeedInd,maxSpeedPoints,maxSpeedLvls);
         GuiElementBuilder maxSpeedItem = new GuiElementBuilder(Items.SUGAR);
         makeMainItem(maxSpeedItem,"Maximum Spawn Time","The maximum amount of time between mob spawns","ticks",maxSpeed,maxSpeedPointC);
   
         setSlot(11,maxSpeedInd == maxSpeedLvls.length-1 ? maxedOut :maxSpeedIncArrow);
         setSlot(20,maxSpeedItem);
         setSlot(29,maxSpeedInd == 0 ? minedOut : maxSpeedDecArrow);
   
         int spawnRange = stats.getShort("SpawnRange");
         int spawnRangeInd = indexOf(spawnRange,spawnRangeLvls);
         int spawnRangePointC = spawnRangePoints[spawnRangeInd];
         ItemStack spawnRangeArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.SLOW_FALLING);
         GuiElementBuilder spawnRangeIncArrow = GuiElementBuilder.from(spawnRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnRangeIncArrow,true,"Spawn Range","blocks",spawnRangeInd,spawnRangePoints,spawnRangeLvls);
         GuiElementBuilder spawnRangeDecArrow = GuiElementBuilder.from(spawnRangeArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnRangeDecArrow,false,"Spawn Range","blocks",spawnRangeInd,spawnRangePoints,spawnRangeLvls);
         GuiElementBuilder spawnRangeItem = new GuiElementBuilder(Items.NETHER_STAR);
         makeMainItem(spawnRangeItem,"Spawn Range","The range in which mobs can spawn","blocks",spawnRange,spawnRangePointC);
   
         setSlot(12,spawnRangeInd == spawnRangeLvls.length-1 ? maxedOut : spawnRangeIncArrow);
         setSlot(21,spawnRangeItem);
         setSlot(30,spawnRangeInd == 0 ? minedOut : spawnRangeDecArrow);
   
         int spawnCount = stats.getShort("SpawnCount");
         int spawnCountInd = indexOf(spawnCount,spawnCountLvls);
         int spawnCountPointC = spawnCountPoints[spawnCountInd];
         ItemStack spawnCountArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.POISON);
         GuiElementBuilder spawnCountIncArrow = GuiElementBuilder.from(spawnCountArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnCountIncArrow,true,"Spawn Count","mobs",spawnCountInd,spawnCountPoints,spawnCountLvls);
         GuiElementBuilder spawnCountDecArrow = GuiElementBuilder.from(spawnCountArrowStack).hideDefaultTooltip();
         makeArrowItem(spawnCountDecArrow,false,"Spawn Count","mobs",spawnCountInd,spawnCountPoints,spawnCountLvls);
         GuiElementBuilder spawnCountItem = new GuiElementBuilder(Items.ZOMBIE_HEAD);
         makeMainItem(spawnCountItem,"Spawn Count","The amount of spawn attempts each cycle","mobs",spawnCount,spawnCountPointC);
   
         setSlot(14,spawnCountInd == spawnCountLvls.length-1 ? maxedOut : spawnCountIncArrow);
         setSlot(23,spawnCountItem);
         setSlot(32,spawnCountInd == 0 ? minedOut : spawnCountDecArrow);
   
         int maxEntities = stats.getShort("MaxNearbyEntities");
         int maxEntitiesInd = indexOf(maxEntities,maxEntitiesLvls);
         int maxEntitiesPointC = maxEntitiesPoints[maxEntitiesInd];
         ItemStack maxEntitiesArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.WATER_BREATHING);
         GuiElementBuilder maxEntitiesIncArrow = GuiElementBuilder.from(maxEntitiesArrowStack).hideDefaultTooltip();
         makeArrowItem(maxEntitiesIncArrow,true,"Max Entities","mobs",maxEntitiesInd,maxEntitiesPoints,maxEntitiesLvls);
         GuiElementBuilder maxEntitiesDecArrow = GuiElementBuilder.from(maxEntitiesArrowStack).hideDefaultTooltip();
         makeArrowItem(maxEntitiesDecArrow,false,"Max Entities","mobs",maxEntitiesInd,maxEntitiesPoints,maxEntitiesLvls);
         GuiElementBuilder maxEntitiesItem = new GuiElementBuilder(Items.WITHER_SKELETON_SKULL);
         makeMainItem(maxEntitiesItem,"Max Entities","The amount of mobs in range before the spawner is disabled","mobs",maxEntities,maxEntitiesPointC);
   
         setSlot(15,maxEntitiesInd == maxEntitiesLvls.length-1 ? maxedOut : maxEntitiesIncArrow);
         setSlot(24,maxEntitiesItem);
         setSlot(33,maxEntitiesInd == 0 ? minedOut : maxEntitiesDecArrow);
   
         int playerRange = stats.getShort("RequiredPlayerRange");
         int playerRangeInd = indexOf(playerRange,playerRangeLvls);
         int playerRangePointC = playerRangePoints[playerRangeInd];
         ItemStack playerRangeArrowStack = PotionContentsComponent.createStack(Items.TIPPED_ARROW, Potions.TURTLE_MASTER);
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
      BlockPos pos = blockEntity.getPos().add(0,2,0);
      BlockEntity blockEntity = world.getBlockEntity(pos);
      BlockState blockState = world.getBlockState(pos);
      return blockState.isOf(Blocks.SPAWNER) && blockEntity instanceof MobSpawnerBlockEntity;
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
      elem.setName((Text.literal("")
            .append(Text.literal(title+": ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(value+" "+units).formatted(Formatting.GREEN))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal(description).formatted(Formatting.DARK_AQUA)))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal(""))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Current Value: ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(value+" "+units).formatted(Formatting.GREEN)))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Point Allocation: ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal(points+" points").formatted(Formatting.GREEN)))));
   }
   
   private void makeArrowItem(GuiElementBuilder elem, boolean increase, String title, String units, int ind, int[] pointArr, int[] lvlArr){
      int newInd = increase ? Math.min(ind+1,pointArr.length-1) : Math.max(ind-1,0);
      int pointDiff = pointArr[ind] - pointArr[newInd];
      String incDecStr = increase ? "Increase" : "Decrease";
      int newVal = increase ? lvlArr[Math.min(ind+1,lvlArr.length-1)] : lvlArr[Math.max(ind-1,0)];
      
      elem.setName((Text.literal("")
            .append(Text.literal(incDecStr+" "+title).formatted(Formatting.DARK_GREEN))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal(incDecStr+": ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(lvlArr[ind]+" "+units).formatted(Formatting.GREEN))
            .append(Text.literal(" -> ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(newVal+" "+units).formatted(Formatting.GREEN)))));
      elem.addLoreLine(TextUtils.removeItalics((Text.literal("")
            .append(Text.literal("Point Cost: ").formatted(Formatting.DARK_AQUA))
            .append(pointDiff > 0 ? Text.literal("+"+pointDiff+" points").formatted(Formatting.GREEN) : Text.literal(pointDiff+" points").formatted(Formatting.RED)))));
   }
   
   @Override
   public void onTick(){
      World world = blockEntity.getWorld();
      if(world == null || world.getBlockEntity(blockEntity.getPos()) != blockEntity){
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
