package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;
import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ArcanaItemUtils {
   
   public static boolean isArcane(ItemStack item){
      try{
         if(item == null) return false;
         ArcanaItem arcanaItem = ArcanaRegistry.ARCANA_ITEMS.get(Identifier.of(MOD_ID, ArcanaItem.getStringProperty(item, ArcanaItem.ID_TAG)));
         return arcanaItem != null;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isEnergyItem(ItemStack item){
      if(isArcane(item)){
         return identifyItem(item) instanceof EnergyItem;
      }
      return false;
   }
   
   public static boolean isRunicArrow(ItemStack item){
      if(isArcane(item)){
         return identifyItem(item) instanceof RunicArrow;
      }
      return false;
   }
   
   public static boolean needsVersionUpdate(ItemStack item){
      ArcanaItem arcanaItem = identifyItem(item);
      if(!isArcane(item) || arcanaItem == null)
         return false;
      try{
         int version = ArcanaItem.getIntProperty(item, ArcanaItem.VERSION_TAG);
         return version != ArcanaItem.VERSION + arcanaItem.getItemVersion(); // Version mismatch, needs update
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isLeftClickItem(ItemStack item){
      try{
         if(isArcane(item)){
            ArcanaItem arcanaItem = identifyItem(item);
            return (arcanaItem instanceof LeftClickItem);
         }
         
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static LeftClickItem identifyLeftClickItem(ItemStack item){
      if(isLeftClickItem(item)){
         return (LeftClickItem) identifyItem(item);
      }
      return null;
   }
   
   public static ArcanaItem identifyItem(ItemStack item){
      if(isArcane(item)){
         return ArcanaRegistry.ARCANA_ITEMS.get(Identifier.of(MOD_ID, ArcanaItem.getStringProperty(item, ArcanaItem.ID_TAG)));
      }
      return null;
   }
   
   public static RunicArrow identifyRunicArrow(ItemStack item){
      if(isRunicArrow(item)){
         return (RunicArrow) identifyItem(item);
      }
      return null;
   }
   
   public static EnergyItem identifyEnergyItem(ItemStack item){
      if(isEnergyItem(item)){
         return (EnergyItem) identifyItem(item);
      }
      return null;
   }
   
   public static ServerPlayerEntity findHolder(MinecraftServer server, String uuid){
      for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
            
            ArcanaItem arcanaItem = identifyItem(item);
            if(arcanaItem != null){
               String itemUuid = ArcanaItem.getUUID(item);
               if(uuid.equals(itemUuid)) return player;
            }
         }
      }
      return null;
   }
   
   public static List<ArcanaInvItem> getActiveArcanaBlocks(ServerPlayerEntity player){
      List<ArcanaInvItem> arcanaInv = new ArrayList<>();
      
      for(Pair<BlockEntity, ArcanaBlockEntity> pair : ACTIVE_ARCANA_BLOCKS.keySet().stream().filter(pair -> player.getUuidAsString().equals(pair.getRight().getCrafterId()) && pair.getLeft().hasWorld() && pair.getLeft().getWorld().getBlockEntity(pair.getLeft().getPos()) == pair.getLeft()).toList()){
         BlockEntity blockEntity = pair.getLeft();
         ArcanaBlockEntity arcanaBlockEntity = pair.getRight();
         
         String dim = blockEntity.getWorld().getRegistryKey().getValue().toString();
         String dimensionName = "Unknown";
         if(dim.equals(ServerWorld.OVERWORLD.getValue().toString())){
            dimensionName = "Overworld";
         }else if(dim.equals(ServerWorld.NETHER.getValue().toString())){
            dimensionName = "Nether";
         }else if(dim.equals(ServerWorld.END.getValue().toString())){
            dimensionName = "End";
         }
         BlockPos pos = blockEntity.getPos();
         String posStr = " ("+pos.getX()+","+pos.getY()+","+pos.getZ()+")";
         
         ArcanaItemContainer worldContainer = new ArcanaItemContainer(new SimpleInventory(1),1,1000,"World",dimensionName+posStr,0.25);
         ArcanaInvItem invItem = new ArcanaInvItem(arcanaBlockEntity.getArcanaItem(),1,arcanaBlockEntity.getAugments(),new ArrayList<>(List.of(worldContainer)));
         arcanaInv.add(invItem);
      }
      
      return arcanaInv;
   }
   
   public static List<ArcanaInvItem> getArcanaInventory(ServerPlayerEntity player){
      List<ArcanaInvItem> arcanaInv = new ArrayList<>();
      PlayerInventory inv = player.getInventory();
      EnderChestInventory eChest = player.getEnderChestInventory();
      arcanaInvHelper(inv,arcanaInv,new ArrayList<>());
      arcanaInvHelper(eChest,arcanaInv,new ArrayList<>(List.of(new ArcanaItemContainer(player.getEnderChestInventory(),player.getEnderChestInventory().size(),100,"EC","Ender Chest",0.5))));
      arcanaInv.addAll(getActiveArcanaBlocks(player));
      return arcanaInv;
   }
   
   public static void arcanaInvHelper(Inventory inv, List<ArcanaInvItem> arcanaInv, List<ArcanaItemContainer> containers){
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty())
            continue;
         boolean isArcane = ArcanaItemUtils.isArcane(item);
         boolean isBox = item.isOf(Items.SHULKER_BOX) || item.isOf(Items.WHITE_SHULKER_BOX) || item.isOf(Items.BLACK_SHULKER_BOX) || item.isOf(Items.BLUE_SHULKER_BOX) || item.isOf(Items.BROWN_SHULKER_BOX) || item.isOf(Items.CYAN_SHULKER_BOX) || item.isOf(Items.GRAY_SHULKER_BOX) || item.isOf(Items.GREEN_SHULKER_BOX) || item.isOf(Items.LIGHT_BLUE_SHULKER_BOX) || item.isOf(Items.LIGHT_GRAY_SHULKER_BOX) || item.isOf(Items.LIME_SHULKER_BOX) || item.isOf(Items.MAGENTA_SHULKER_BOX) || item.isOf(Items.ORANGE_SHULKER_BOX) || item.isOf(Items.PINK_SHULKER_BOX) || item.isOf(Items.PURPLE_SHULKER_BOX) || item.isOf(Items.RED_SHULKER_BOX) || item.isOf(Items.YELLOW_SHULKER_BOX);
         if(isBox && !isArcane){
            ContainerComponent comp = item.getOrDefault(DataComponentTypes.CONTAINER,ContainerComponent.DEFAULT);
            DefaultedList<ItemStack> shulkerItems = DefaultedList.ofSize(27, ItemStack.EMPTY);
            comp.copyTo(shulkerItems);
            SimpleInventory shulkerInv = new SimpleInventory(27);
            for(int j = 0; j < shulkerItems.size(); j++){
               shulkerInv.setStack(j,shulkerItems.get(j));
            }
            ArrayList<ArcanaItemContainer> containersCopy = new ArrayList<>(containers);
            containersCopy.add(new ArcanaItemContainer(shulkerInv,shulkerInv.size(),10,"SB","Shulker Box",0.5));
            arcanaInvHelper(shulkerInv,arcanaInv,containersCopy);
         }
         if(!isArcane){
            continue;
         }
         ArcanaItem arcanaItem = identifyItem(item);
         if(arcanaItem instanceof ArcanaItemContainer.ArcanaItemContainerHaver containerHaver){
            ArrayList<ArcanaItemContainer> containersCopy = new ArrayList<>(containers);
            ArcanaItemContainer arcanaContainer = containerHaver.getArcanaItemContainer(item);
            containersCopy.add(arcanaContainer);
            arcanaInvHelper(arcanaContainer.getInventory(),arcanaInv,containersCopy);
         }
         
         ArcanaInvItem invItem = new ArcanaInvItem(arcanaItem,item.getCount(),ArcanaAugments.getAugmentsOnItem(item),containers);
         int contains = ArcanaInvItem.invContains(arcanaInv,invItem);
         if(contains >= 0){
            arcanaInv.get(contains).count += invItem.count;
            arcanaInv.get(contains).addItem(item);
         }else{
            invItem.addItem(item);
            arcanaInv.add(invItem);
         }
      }
   }
   
   public static int getUsedConcentration(ServerPlayerEntity player){
      int concSum = 0;
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         concSum += (int) (Math.ceil(arcanaInvItem.count/(double)prefCount) * Math.ceil(containerMod*(ArcanaRarity.getConcentration(arcanaItem.getRarity())+ arcanaInvItem.getAugmentConc(player))));
      }
      
      return concSum;
   }
   
   
   public static List<MutableText> getConcBreakdown(ServerPlayerEntity player){
      ArrayList<MutableText> list = new ArrayList<>();
      
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      Comparator<ArcanaInvItem> comparator = (ArcanaInvItem i1, ArcanaInvItem i2) -> {
         int r1 = i1.count* ArcanaRarity.getConcentration(i1.item.getRarity());
         r1 += (int) (10000*i1.getSortMod());
         int r2 = i2.count* ArcanaRarity.getConcentration(i2.item.getRarity());
         r2 += (int) (10000*i2.getSortMod());
         return r1 - r2;
      };
      arcanaInv.sort(comparator);
      
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         int multiplier = (int)Math.ceil(arcanaInvItem.count/(double)prefCount);
   
         int itemConc = multiplier * (int)Math.ceil(containerMod*(ArcanaRarity.getConcentration(arcanaItem.getRarity())+ arcanaInvItem.getAugmentConc(player)));
         
         String multStr = multiplier > 1 ? " x"+multiplier : "";
         String contStr = arcanaInvItem.getContainerString();
         MutableText line = Text.literal("")
               .append(Text.literal("- "+ arcanaItem.getNameString()).formatted(Formatting.DARK_AQUA))
               .append(Text.literal(multStr).formatted(Formatting.BLUE))
               .append(Text.literal(" ("+itemConc+")").formatted(Formatting.DARK_GREEN))
               .append(Text.literal(" "+contStr).formatted(Formatting.DARK_PURPLE));
         list.add(line);
      }
      
      return list;
   }
   
   public static int countItemsTakingConc(ServerPlayerEntity player){
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      int itemsTakingConc = 0;
      
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         int multiplier = (int)Math.ceil(arcanaInvItem.count/(double)prefCount);
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         int itemConc = multiplier * (int)Math.ceil(containerMod*(ArcanaRarity.getConcentration(arcanaItem.getRarity())+ arcanaInvItem.getAugmentConc(player)));

         if(itemConc > 0) itemsTakingConc += multiplier;
      }
      return itemsTakingConc;
   }
   
   public static boolean hasItemInInventory(PlayerEntity player, Item itemType){
      List<Pair<List<ItemStack>,ItemStack>> allItems = new ArrayList<>();
      PlayerInventory playerInv = player.getInventory();
      
      List<ItemStack> invItems = new ArrayList<>();
      for(int i=0; i<playerInv.size();i++){
         ItemStack item = playerInv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         invItems.add(item);
         ArcanaItem mitem = ArcanaItemUtils.identifyItem(item);
         if(mitem instanceof ArcanistsBelt belt){
            SimpleInventory beltInv = belt.deserialize(item);
            ArrayList<ItemStack> beltList = new ArrayList<>();
            for(int j = 0; j < beltInv.size(); j++){
               beltList.add(beltInv.getStack(j));
            }
            allItems.add(new Pair<>(beltList,item));
         }
      }
      allItems.add(new Pair<>(invItems,ItemStack.EMPTY));
      
      for(Pair<List<ItemStack>, ItemStack> allItem : allItems){
         List<ItemStack> itemList = allItem.getLeft();
         
         for(ItemStack item : itemList){
            if(item.isOf(itemType)) return true;
         }
         
      }
      return false;
   }
   
   public static int countRarityInList(List<String> ids, ArcanaRarity rarity, boolean exclude){
      int count = 0;
      for(String id : ids){
         Identifier identifier = id.contains(":") ? Identifier.of(id) : Identifier.of(MOD_ID,id);
         if(!ArcanaRegistry.ARCANA_ITEMS.containsId(identifier)) continue;
         if(getItemFromId(id).getRarity() == rarity ^ exclude) count++;
      }
      return count;
   }
   
   public static ArcanaItem getItemFromId(String id){
      if(id == null) return null;
      Identifier identifier = id.contains(":") ? Identifier.of(id) : Identifier.of(MOD_ID,id);
      return ArcanaRegistry.ARCANA_ITEMS.get(identifier);
   }
   
   public static class ArcanaInvItem {
      private int count;
      public final ArcanaItem item;
      public final String hash;
      private final ArrayList<Pair<String,ItemStack>> stacks;
      private final TreeMap<ArcanaAugment,Integer> augments;
      private final List<ArcanaItemContainer> containers;
      private double concMod;
      private int sortMod;
      
      public ArcanaInvItem(ArcanaItem item, int count, TreeMap<ArcanaAugment,Integer> augments, List<ArcanaItemContainer> containers){
         this.count = count;
         this.item = item;
         this.stacks = new ArrayList<>();
         this.augments = augments;
         this.containers = containers;
         
         StringBuilder augHash = new StringBuilder();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            ArcanaAugment aug = entry.getKey();
            int itemLvl = entry.getValue();
            augHash.append(aug.id).append(itemLvl);
         }
         
         concMod = 1;
         sortMod = 0;
         containers.sort(Comparator.comparing(ArcanaItemContainer::getConcModStr));
         StringBuilder contHash = new StringBuilder();
         for(ArcanaItemContainer container : containers){
            contHash.append(container.getConcModStr());
            concMod *= container.getConcMod();
            sortMod += container.getSortMod();
         }
         
         this.hash = item.getId() + augHash + contHash;
      }
   
      public static int invContains(List<ArcanaInvItem> inv, ArcanaInvItem invItem){
         for(int i = 0; i < inv.size(); i++){
            ArcanaInvItem arcanaInvItem = inv.get(i);
            if(arcanaInvItem.hash.equals(invItem.hash)){
               return i;
            }
         }
         return -1;
      }
      
      public void addItem(ItemStack stack){
         stacks.add(new Pair<>(ArcanaItem.getUUID(stack),stack));
      }
      
      public ArrayList<Pair<String,ItemStack>> getStacks(){
         return stacks;
      }
   
      public int getCount(){
         return count;
      }
      
      public double getConcMod(){
         return concMod;
      }
   
      public double getSortMod(){
         return sortMod;
      }
      
      public int getAugmentConc(ServerPlayerEntity player){
         IArcanaProfileComponent profile = PLAYER_DATA.get(player);
         int adaptability = profile.getAugmentLevel(ArcanaAugments.ADAPTABILITY.id);
         int augmentConc = 0;
         
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            ArcanaAugment aug = entry.getKey();
            int itemLvl = entry.getValue();
            int profileLvl = profile.getAugmentLevel(aug.id);
            augmentConc += Math.max(0,itemLvl-profileLvl);
         }
         return Math.max(0,augmentConc-adaptability);
      }
      
      public double getFocusedConcMod(ServerPlayerEntity player){
         IArcanaProfileComponent profile = PLAYER_DATA.get(player);
         int focus = profile.getAugmentLevel(ArcanaAugments.FOCUS.id);
         if(focus == 1){
            boolean isEChest = false;
            boolean isShulker = false;
   
            for(ArcanaItemContainer container : containers){
               if(container.getContainerName().equals("Ender Chest")){
                  isEChest = true;
               }else if(container.getContainerName().equals("Shulker Box")){
                  isShulker = true;
               }
            }
            return isShulker && isEChest ? 0 : getConcMod();
         }else if(focus == 2){
            for(ArcanaItemContainer container : containers){
               if(container.getContainerName().equals("Ender Chest")){
                  return 0;
               }
            }
         }else if(focus == 3){
            for(ArcanaItemContainer container : containers){
               if(container.getContainerName().equals("Ender Chest")){
                  return 0;
               }else if(container.getContainerName().equals("Shulker Box")){
                  return 0;
               }
            }
         }
         return getConcMod();
      }
   
      public String getShortContainerString(){
         if(containers.isEmpty()) return "Inv";
         StringBuilder str = new StringBuilder();
         for(ArcanaItemContainer container : containers){
            str.append(container.getConcModStr()).append("+");
         }
         return (str.substring(0,str.length()-1));
      }
      
      public String getContainerString(){
         if(containers.isEmpty()) return "";
         StringBuilder str = new StringBuilder("[");
         for(ArcanaItemContainer container : containers){
            str.append(container.getContainerName()).append(" + ");
         }
         if(str.length() > 30){
            return "[" + getShortContainerString() + "]";
         }else{
            return (str.substring(0,str.length()-3) + "]");
         }
      }
      
      public TreeMap<ArcanaAugment,Integer> getAugments(){
         return augments;
      }
   }
}
