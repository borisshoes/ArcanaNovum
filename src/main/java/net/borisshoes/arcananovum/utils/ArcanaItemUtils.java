package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.datastorage.EnderCrateChannels;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;

public class ArcanaItemUtils {
   
   public static boolean isArcane(ItemStack item){
      try{
         if(item == null) return false;
         ArcanaItem arcanaItem = ArcanaRegistry.ARCANA_ITEMS.getValue(ArcanaRegistry.arcanaId(ArcanaItem.getStringProperty(item, ArcanaItem.ID_TAG)));
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
         return ArcanaRegistry.ARCANA_ITEMS.getValue(ArcanaRegistry.arcanaId(ArcanaItem.getStringProperty(item, ArcanaItem.ID_TAG)));
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
   
   public static ServerPlayer findHolder(MinecraftServer server, String uuid){
      for(ServerPlayer player : server.getPlayerList().getPlayers()){
         if(getHolderStack(player, uuid) != null) return player;
      }
      return null;
   }
   
   public static ItemStack getHolderStack(ServerPlayer player, String uuid){
      Inventory inv = player.getInventory();
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcanaItem = identifyItem(item);
         if(arcanaItem != null){
            String itemUuid = ArcanaItem.getUUID(item);
            if(uuid.equals(itemUuid)) return item;
         }
      }
      return null;
   }
   
   public static List<ArcanaInvItem> getActiveArcanaBlocks(ServerPlayer player){
      List<ArcanaInvItem> arcanaInv = new ArrayList<>();
      
      for(Tuple<BlockEntity, ArcanaBlockEntity> pair : ACTIVE_ARCANA_BLOCKS.keySet().stream().filter(pair -> player.getStringUUID().equals(pair.getB().getCrafterId()) && pair.getA().hasLevel() && pair.getA().getLevel().getBlockEntity(pair.getA().getBlockPos()) == pair.getA()).toList()){
         BlockEntity blockEntity = pair.getA();
         ArcanaBlockEntity arcanaBlockEntity = pair.getB();
         
         String dim = blockEntity.getLevel().dimension().identifier().toString();
         String dimensionName = "Unknown";
         if(dim.equals(ServerLevel.OVERWORLD.identifier().toString())){
            dimensionName = "Overworld";
         }else if(dim.equals(ServerLevel.NETHER.identifier().toString())){
            dimensionName = "Nether";
         }else if(dim.equals(ServerLevel.END.identifier().toString())){
            dimensionName = "End";
         }
         BlockPos pos = blockEntity.getBlockPos();
         String posStr = " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
         
         ArcanaItemContainer worldContainer = new ArcanaItemContainer(
               Identifier.withDefaultNamespace("in_world"),
               new SimpleContainer(1), 1, 1000,
               Component.literal("World"),
               Component.literal(dimensionName + posStr),
               0.25);
         ArcanaInvItem invItem = new ArcanaInvItem(arcanaBlockEntity.getArcanaItem(), 1, arcanaBlockEntity.getAugments(), new ArrayList<>(List.of(worldContainer)));
         arcanaInv.add(invItem);
      }
      
      return arcanaInv;
   }
   
   public static List<ArcanaInvItem> getArcanaInventory(ServerPlayer player){
      List<ArcanaInvItem> arcanaInv = new ArrayList<>();
      Inventory inv = player.getInventory();
      PlayerEnderChestContainer eChest = player.getEnderChestInventory();
      ArcanaItemContainer eChestCont = new ArcanaItemContainer(
            Identifier.withDefaultNamespace("ender_chest"),
            player.getEnderChestInventory(), player.getEnderChestInventory().getContainerSize(), 100,
            Component.literal("EC"),
            Items.ENDER_CHEST.getName().copy(),
            0.5);
      arcanaInvHelper(inv, arcanaInv, new ArrayList<>());
      arcanaInvHelper(eChest, arcanaInv, new ArrayList<>(List.of(eChestCont)));
      for(ArcanaItemContainer arcanaItemContainer : DataAccess.getGlobal(EnderCrateChannels.KEY).arcanaInventoriesForPlayer(player.getUUID())){
         arcanaInvHelper(arcanaItemContainer.getInventory(), arcanaInv, new ArrayList<>(List.of(arcanaItemContainer)));
      }
      arcanaInv.addAll(getActiveArcanaBlocks(player));
      return arcanaInv;
   }
   
   public static void arcanaInvHelper(Container inv, List<ArcanaInvItem> arcanaInv, List<ArcanaItemContainer> containers){
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty())
            continue;
         boolean isArcane = ArcanaItemUtils.isArcane(item);
         boolean isBox = item.is(Items.SHULKER_BOX) || item.is(Items.WHITE_SHULKER_BOX) || item.is(Items.BLACK_SHULKER_BOX) || item.is(Items.BLUE_SHULKER_BOX) || item.is(Items.BROWN_SHULKER_BOX) || item.is(Items.CYAN_SHULKER_BOX) || item.is(Items.GRAY_SHULKER_BOX) || item.is(Items.GREEN_SHULKER_BOX) || item.is(Items.LIGHT_BLUE_SHULKER_BOX) || item.is(Items.LIGHT_GRAY_SHULKER_BOX) || item.is(Items.LIME_SHULKER_BOX) || item.is(Items.MAGENTA_SHULKER_BOX) || item.is(Items.ORANGE_SHULKER_BOX) || item.is(Items.PINK_SHULKER_BOX) || item.is(Items.PURPLE_SHULKER_BOX) || item.is(Items.RED_SHULKER_BOX) || item.is(Items.YELLOW_SHULKER_BOX);
         boolean isBundle = item.has(DataComponents.BUNDLE_CONTENTS);
         if(isBundle && !isArcane){
            BundleContents bundleComp = item.get(DataComponents.BUNDLE_CONTENTS);
            SimpleContainer bundleInv = new SimpleContainer(bundleComp.size());
            int index = 0;
            for(ItemStack itemStack : bundleComp.itemsCopy()){
               bundleInv.setItem(index, itemStack);
               index++;
            }
            ArrayList<ArcanaItemContainer> containersCopy = new ArrayList<>(containers);
            containersCopy.add(new ArcanaItemContainer(
                  Identifier.withDefaultNamespace("bundle"),
                  bundleInv, bundleInv.getContainerSize(), 5,
                  Component.literal("BD"),
                  item.getItemName().copy(),
                  1));
            arcanaInvHelper(bundleInv, arcanaInv, containersCopy);
         }else if(isBox && !isArcane){
            ItemContainerContents comp = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            NonNullList<ItemStack> shulkerItems = NonNullList.withSize(27, ItemStack.EMPTY);
            comp.copyInto(shulkerItems);
            SimpleContainer shulkerInv = new SimpleContainer(27);
            for(int j = 0; j < shulkerItems.size(); j++){
               shulkerInv.setItem(j, shulkerItems.get(j));
            }
            ArrayList<ArcanaItemContainer> containersCopy = new ArrayList<>(containers);
            containersCopy.add(new ArcanaItemContainer(
                  Identifier.withDefaultNamespace("shulker_box"),
                  shulkerInv, shulkerInv.getContainerSize(), 50,
                  Component.literal("SB"),
                  item.getItemName().copy(),
                  0.5));
            arcanaInvHelper(shulkerInv, arcanaInv, containersCopy);
         }
         if(!isArcane){
            continue;
         }
         ArcanaItem arcanaItem = identifyItem(item);
         if(!ArcanaItem.hasProperty(item, ArcanaItem.UUID_TAG) || ArcanaItem.getUUID(item).equals(ArcanaNovum.BLANK_UUID))
            continue;
         if(arcanaItem instanceof ArcanaItemContainer.ArcanaItemContainerHaver containerHaver){
            ArrayList<ArcanaItemContainer> containersCopy = new ArrayList<>(containers);
            ArcanaItemContainer arcanaContainer = containerHaver.getArcanaItemContainer(item);
            containersCopy.add(arcanaContainer);
            arcanaInvHelper(arcanaContainer.getInventory(), arcanaInv, containersCopy);
         }
         
         ArcanaInvItem invItem = new ArcanaInvItem(arcanaItem, item.getCount(), ArcanaAugments.getAugmentsOnItem(item), containers);
         int contains = ArcanaInvItem.invContains(arcanaInv, invItem);
         if(contains >= 0){
            arcanaInv.get(contains).count += invItem.count;
            arcanaInv.get(contains).addItem(item);
         }else{
            invItem.addItem(item);
            arcanaInv.add(invItem);
         }
      }
   }
   
   public static int getUsedConcentration(ServerPlayer player){
      int concSum = 0;
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         concSum += (int) (Math.ceil(arcanaInvItem.count / (double) prefCount) * Math.ceil(containerMod * (ArcanaRarity.getConcentration(arcanaItem.getRarity()) + arcanaInvItem.getAugmentConc(player))));
      }
      return concSum;
   }
   
   
   public static List<MutableComponent> getConcBreakdown(ServerPlayer player){
      ArrayList<MutableComponent> list = new ArrayList<>();
      
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      Comparator<ArcanaInvItem> comparator = (ArcanaInvItem i1, ArcanaInvItem i2) -> {
         int r1 = i1.count * ArcanaRarity.getConcentration(i1.item.getRarity());
         r1 += (int) (10000 * i1.getSortMod());
         int r2 = i2.count * ArcanaRarity.getConcentration(i2.item.getRarity());
         r2 += (int) (10000 * i2.getSortMod());
         return r1 - r2;
      };
      arcanaInv.sort(comparator);
      
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         int multiplier = (int) Math.ceil(arcanaInvItem.count / (double) prefCount);
         
         int itemConc = multiplier * (int) Math.ceil(containerMod * (ArcanaRarity.getConcentration(arcanaItem.getRarity()) + arcanaInvItem.getAugmentConc(player)));
         
         String multStr = multiplier > 1 ? " x" + multiplier : "";
         MutableComponent contStr = arcanaInvItem.getContainerString();
         MutableComponent line = Component.literal("")
               .append((Component.literal("- ").append(arcanaItem.getTranslatedName())).withStyle(ChatFormatting.DARK_AQUA))
               .append(Component.literal(multStr).withStyle(ChatFormatting.BLUE))
               .append(Component.literal(" (" + itemConc + ") ").withStyle(ChatFormatting.DARK_GREEN))
               .append(contStr.withStyle(ChatFormatting.DARK_PURPLE));
         list.add(line);
      }
      
      return list;
   }
   
   public static int countItemsTakingConc(ServerPlayer player){
      List<ArcanaInvItem> arcanaInv = getArcanaInventory(player);
      int itemsTakingConc = 0;
      
      for(ArcanaInvItem arcanaInvItem : arcanaInv){
         ArcanaItem arcanaItem = arcanaInvItem.item;
         int prefCount = arcanaItem.getPrefItem().getCount();
         int multiplier = (int) Math.ceil(arcanaInvItem.count / (double) prefCount);
         double containerMod = arcanaInvItem.getFocusedConcMod(player);
         int itemConc = multiplier * (int) Math.ceil(containerMod * (ArcanaRarity.getConcentration(arcanaItem.getRarity()) + arcanaInvItem.getAugmentConc(player)));
         
         if(itemConc > 0) itemsTakingConc += multiplier;
      }
      return itemsTakingConc;
   }
   
   public static boolean hasItemInInventory(Player player, Item itemType){
      List<Tuple<List<ItemStack>, ItemStack>> allItems = ArcanaUtils.getAllItems(player);
      
      for(Tuple<List<ItemStack>, ItemStack> allItem : allItems){
         List<ItemStack> itemList = allItem.getA();
         
         for(ItemStack item : itemList){
            if(item.is(itemType)) return true;
         }
         
      }
      return false;
   }
   
   public static int countRarityInList(List<String> ids, ArcanaRarity rarity, boolean exclude){
      int count = 0;
      for(String id : ids){
         Identifier identifier = id.contains(":") ? Identifier.parse(id) : ArcanaRegistry.arcanaId(id);
         if(!ArcanaRegistry.ARCANA_ITEMS.containsKey(identifier)) continue;
         if(getItemFromId(id).getRarity() == rarity ^ exclude) count++;
      }
      return count;
   }
   
   public static ArcanaItem getItemFromId(String id){
      if(id == null) return null;
      Identifier identifier = id.contains(":") ? Identifier.parse(id) : ArcanaRegistry.arcanaId(id);
      return ArcanaRegistry.ARCANA_ITEMS.getValue(identifier);
   }
   
   public static class ArcanaInvItem {
      private int count;
      public final ArcanaItem item;
      public final String hash;
      private final ArrayList<Tuple<String, ItemStack>> stacks;
      private final TreeMap<ArcanaAugment, Integer> augments;
      private final List<ArcanaItemContainer> containers;
      private double concMod;
      private int sortMod;
      
      public ArcanaInvItem(ArcanaItem item, int count, TreeMap<ArcanaAugment, Integer> augments, List<ArcanaItemContainer> containers){
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
         containers.sort(Comparator.comparing(ArcanaItemContainer::getSortMod));
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
         stacks.add(new Tuple<>(ArcanaItem.getUUID(stack), stack));
      }
      
      public ArrayList<Tuple<String, ItemStack>> getStacks(){
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
      
      public int getAugmentConc(ServerPlayer player){
         ArcanaPlayerData profile = ArcanaNovum.data(player);
         int adaptability = profile.getAugmentLevel(ArcanaAugments.ADAPTABILITY);
         int adaptabilityBonus = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ADAPTABILITY_CONCENTRATION_PER_LVL).get(adaptability);
         int augmentConc = 0;
         
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            ArcanaAugment aug = entry.getKey();
            int itemLvl = entry.getValue();
            int profileLvl = profile.getAugmentLevel(aug);
            augmentConc += Math.max(0, itemLvl - profileLvl);
         }
         return Math.max(0, augmentConc - adaptabilityBonus);
      }
      
      public double getFocusedConcMod(ServerPlayer player){
         ArcanaPlayerData profile = ArcanaNovum.data(player);
         int focus = profile.getAugmentLevel(ArcanaAugments.FOCUS);
         if(focus == 1){
            boolean isEChest = false;
            boolean isShulker = false;
            
            for(ArcanaItemContainer container : containers){
               if(container.getId().equals(Identifier.withDefaultNamespace("ender_chest"))){
                  isEChest = true;
               }else if(container.getId().equals(Identifier.withDefaultNamespace("shulker_box"))){
                  isShulker = true;
               }
            }
            return isShulker && isEChest ? 0 : getConcMod();
         }else if(focus == 2){
            for(ArcanaItemContainer container : containers){
               if(container.getId().equals(Identifier.withDefaultNamespace("ender_chest"))){
                  return 0;
               }
            }
         }else if(focus == 3){
            for(ArcanaItemContainer container : containers){
               if(container.getId().equals(Identifier.withDefaultNamespace("ender_chest"))){
                  return 0;
               }else if(container.getId().equals(Identifier.withDefaultNamespace("shulker_box"))){
                  return 0;
               }
            }
         }
         return getConcMod();
      }
      
      public MutableComponent getShortContainerString(){
         if(containers.isEmpty()) return Component.literal("Inv");
         MutableComponent comp = Component.literal("");
         for(int i = 0; i < containers.size(); i++){
            comp.append(containers.get(i).getConcModStr());
            if(i != containers.size() - 1){
               comp.append("+");
            }
         }
         return comp;
      }
      
      public MutableComponent getContainerString(){
         if(containers.isEmpty()) return Component.literal("");
         MutableComponent comp = Component.literal("[");
         for(int i = 0; i < containers.size(); i++){
            comp.append(containers.get(i).getContainerName());
            if(i != containers.size() - 1){
               comp.append(" + ");
            }
         }
         if(comp.getString().length() > 30 && !(containers.size() == 1 && containers.getFirst().getId().equals(ArcanaRegistry.arcanaId(ArcanaRegistry.ENDER_CRATE.getId())))){
            return Component.literal("[").append(getShortContainerString()).append("]");
         }else{
            return comp.append("]");
         }
      }
      
      public TreeMap<ArcanaAugment, Integer> getAugments(){
         return augments;
      }
   }
}
