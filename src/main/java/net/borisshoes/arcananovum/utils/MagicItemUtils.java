package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.*;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class MagicItemUtils {
   
   public static boolean isMagic(ItemStack item){
      try{
         NbtCompound itemNbt = item.getNbt();
         if(itemNbt == null)
            return false;
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         if(magicTag != null){
            // We know that the item is magic, get its class from its id
            String id = magicTag.getString("id");
            if(id==null)
               return false;
            //System.out.println("Found magic id: "+id);
            MagicItem magicItem = ArcanaRegistry.registry.get(id);
            if(magicItem == null)
               return false;
            //System.out.println("Magic Item Name: "+magicItem.getNameString());
            return true;
         }
      
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isEnergyItem(ItemStack item){
      if(isMagic(item)){
         return identifyItem(item) instanceof EnergyItem;
      }
      return false;
   }
   
   public static boolean isRunicArrow(ItemStack item){
      if(isMagic(item)){
         return identifyItem(item) instanceof RunicArrow;
      }
      return false;
   }
   
   public static boolean needsVersionUpdate(ItemStack item){
      MagicItem magicItem = identifyItem(item);
      if(!isMagic(item) || magicItem == null)
         return false;
      try{
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         if(!magicTag.contains("Version",NbtCompound.INT_TYPE)){
            log(1,"Item Missing Version Data");
            return true; // Version tag missing, needs update
         }
         int version = magicTag.getInt("Version");
         return version != MagicItem.version + magicItem.getItemVersion(); // Version mismatch, needs update
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isLeftClickItem(ItemStack item){
      try{
         if(isMagic(item)){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicTag = itemNbt.getCompound("arcananovum");
            // We know that the item is magic, get its class from its id
            String id = magicTag.getString("id");
            if(id==null)
               return false;
            //System.out.println("Found magic id: "+id);
            MagicItem magicItem = ArcanaRegistry.registry.get(id);
            //System.out.println("Magic Item Name: "+magicItem.getName());
            return (magicItem instanceof LeftClickItem);
         }
         
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static LeftClickItem identifyLeftClickItem(ItemStack item){
      if(isLeftClickItem(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return (LeftClickItem) ArcanaRegistry.registry.get(id);
      }
      return null;
   }
   
   public static MagicItem identifyItem(ItemStack item){
      if(isMagic(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return ArcanaRegistry.registry.get(id);
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
            
            MagicItem magicItem = identifyItem(item);
            if(magicItem != null){
               String itemUuid = magicItem.getUUID(item);
               if(uuid.equals(itemUuid)) return player;
            }
         }
      }
      return null;
   }
   
   public static List<MagicInvItem> getMagicInventory(ServerPlayerEntity player){
      List<MagicInvItem> magicInv = new ArrayList<>();
      PlayerInventory inv = player.getInventory();
      EnderChestInventory eChest = player.getEnderChestInventory();
      ArrayList<MagicItemContainer> eChestContainerList = new ArrayList<>();
      eChestContainerList.add(new MagicItemContainer(player.getEnderChestInventory(),player.getEnderChestInventory().size(),100,"EC","Ender Chest",0.5));
      magicInvHelper(inv,magicInv,new ArrayList<>());
      magicInvHelper(eChest,magicInv,eChestContainerList);
      return magicInv;
   }
   
   public static void magicInvHelper(Inventory inv, List<MagicInvItem> magicInv, List<MagicItemContainer> containers){
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty())
            continue;
         boolean isMagic = MagicItemUtils.isMagic(item);
         boolean isBox = item.isOf(Items.SHULKER_BOX) || item.isOf(Items.WHITE_SHULKER_BOX) || item.isOf(Items.BLACK_SHULKER_BOX) || item.isOf(Items.BLUE_SHULKER_BOX) || item.isOf(Items.BROWN_SHULKER_BOX) || item.isOf(Items.CYAN_SHULKER_BOX) || item.isOf(Items.GRAY_SHULKER_BOX) || item.isOf(Items.GREEN_SHULKER_BOX) || item.isOf(Items.LIGHT_BLUE_SHULKER_BOX) || item.isOf(Items.LIGHT_GRAY_SHULKER_BOX) || item.isOf(Items.LIME_SHULKER_BOX) || item.isOf(Items.MAGENTA_SHULKER_BOX) || item.isOf(Items.ORANGE_SHULKER_BOX) || item.isOf(Items.PINK_SHULKER_BOX) || item.isOf(Items.PURPLE_SHULKER_BOX) || item.isOf(Items.RED_SHULKER_BOX) || item.isOf(Items.YELLOW_SHULKER_BOX);
         if(isBox && !isMagic){
            NbtCompound tag = item.getNbt();
            if(tag == null) continue;
            NbtCompound bet = tag.getCompound("BlockEntityTag");
            if(bet == null || !bet.contains("Items",9)) continue;
            
            DefaultedList<ItemStack> shulkerItems = DefaultedList.ofSize(27, ItemStack.EMPTY);
            Inventories.readNbt(bet, shulkerItems);
            SimpleInventory shulkerInv = new SimpleInventory(27);
            for(int j = 0; j < shulkerItems.size(); j++){
               shulkerInv.setStack(j,shulkerItems.get(j));
            }
            ArrayList<MagicItemContainer> containersCopy = new ArrayList<>(containers);
            containersCopy.add(new MagicItemContainer(shulkerInv,shulkerInv.size(),10,"SB","Shulker Box",0.5));
            magicInvHelper(shulkerInv,magicInv,containersCopy);
         }
         if(!isMagic){
            continue;
         }
         MagicItem magicItem = identifyItem(item);
         if(magicItem instanceof MagicItemContainer.MagicItemContainerHaver containerHaver){
            ArrayList<MagicItemContainer> containersCopy = new ArrayList<>(containers);
            MagicItemContainer magicContainer = containerHaver.getMagicItemContainer(item);
            containersCopy.add(magicContainer);
            magicInvHelper(magicContainer.getInventory(),magicInv,containersCopy);
         }
         
         MagicInvItem invItem = new MagicInvItem(magicItem,item.getCount(),ArcanaAugments.getAugmentsOnItem(item),containers);
         int contains = MagicInvItem.invContains(magicInv,invItem);
         if(contains >= 0){
            magicInv.get(contains).count += invItem.count;
            magicInv.get(contains).addItem(item);
         }else{
            invItem.addItem(item);
            magicInv.add(invItem);
         }
      }
   }
   
   public static int getUsedConcentration(ServerPlayerEntity player){
      int concSum = 0;
      List<MagicInvItem> magicInv = getMagicInventory(player);
      for(MagicInvItem magicInvItem : magicInv){
         MagicItem magicItem = magicInvItem.item;
         int prefCount = magicItem.getPrefItem().getCount();
         double containerMod = magicInvItem.getFocusedConcMod(player);
         concSum += (int) (Math.ceil(magicInvItem.count/(double)prefCount) * Math.ceil(containerMod*(MagicRarity.getConcentration(magicItem.getRarity())+magicInvItem.getAugmentConc(player))));
      }
      
      return concSum;
   }
   
   
   public static List<String> getConcBreakdown(ServerPlayerEntity player){
      ArrayList<String> list = new ArrayList<>();
      
      List<MagicInvItem> magicInv = getMagicInventory(player);
      Comparator<MagicInvItem> comparator = (MagicInvItem i1, MagicInvItem i2) -> {
         int r1 = i1.count*MagicRarity.getConcentration(i1.item.getRarity());
         r1 += (int) (10000*i1.getSortMod());
         int r2 = i2.count*MagicRarity.getConcentration(i2.item.getRarity());
         r2 += (int) (10000*i2.getSortMod());
         return r1 - r2;
      };
      magicInv.sort(comparator);
      
      for(MagicInvItem magicInvItem : magicInv){
         MagicItem magicItem = magicInvItem.item;
         int prefCount = magicItem.getPrefItem().getCount();
         double containerMod = magicInvItem.getFocusedConcMod(player);
         int multiplier = (int)Math.ceil(magicInvItem.count/(double)prefCount);
   
         int itemConc = multiplier * (int)Math.ceil(containerMod*(MagicRarity.getConcentration(magicItem.getRarity())+magicInvItem.getAugmentConc(player)));
         
         String multStr = multiplier > 1 ? " x"+multiplier : "";
         String contStr = magicInvItem.getContainerString();
         String line = "[{\"text\":\"- "+magicItem.getNameString()+"\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+multStr+"\",\"color\":\"blue\"},{\"text\":\" ("+itemConc+")\",\"color\":\"dark_green\"},{\"text\":\" "+contStr+"\",\"color\":\"dark_purple\"}]";
         list.add(line);
      }
      
      return list;
   }
   
   public static int countItemsTakingConc(ServerPlayerEntity player){
      List<MagicInvItem> magicInv = getMagicInventory(player);
      int itemsTakingConc = 0;
      
      for(MagicInvItem magicInvItem : magicInv){
         MagicItem magicItem = magicInvItem.item;
         int prefCount = magicItem.getPrefItem().getCount();
         int multiplier = (int)Math.ceil(magicInvItem.count/(double)prefCount);
         double containerMod = magicInvItem.getFocusedConcMod(player);
         int itemConc = multiplier * (int)Math.ceil(containerMod*(MagicRarity.getConcentration(magicItem.getRarity())+magicInvItem.getAugmentConc(player)));

         if(itemConc > 0) itemsTakingConc += multiplier;
      }
      return itemsTakingConc;
   }
   
   public static int countRarityInList(List<String> ids, MagicRarity rarity, boolean exclude){
      int count = 0;
      for(String id : ids){
         if(!ArcanaRegistry.registry.containsKey("id")) continue;
         if(getItemFromId(id).getRarity() == rarity ^ exclude) count++;
      }
      return count;
   }
   
   public static MagicItem getItemFromId(String id){
      return ArcanaRegistry.registry.get(id);
   }
   
   public static class MagicInvItem {
      private int count;
      public final MagicItem item;
      public final String hash;
      private final ArrayList<Pair<String,ItemStack>> stacks;
      private final TreeMap<ArcanaAugment,Integer> augments;
      private final List<MagicItemContainer> containers;
      private double concMod;
      private int sortMod;
      
      public MagicInvItem(MagicItem item, int count, TreeMap<ArcanaAugment,Integer> augments, List<MagicItemContainer> containers){
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
         containers.sort(Comparator.comparing(MagicItemContainer::getConcModStr));
         StringBuilder contHash = new StringBuilder();
         for(MagicItemContainer container : containers){
            contHash.append(container.getConcModStr());
            concMod *= container.getConcMod();
            sortMod += container.getSortMod();
         }
         
         this.hash = item.getId() + augHash + contHash;
      }
      
      public ItemStack getTemplateItem(){
         ItemStack stack = item.getPrefItem();
         for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
            ArcanaAugment aug = entry.getKey();
            int itemLvl = entry.getValue();
            ArcanaAugments.applyAugment(stack,aug.id,itemLvl,false);
         }
         NbtCompound itemNbt = stack.getNbt();
         NbtList loreList = itemNbt.getCompound("display").getList("Lore", NbtElement.STRING_TYPE);
         loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":true,\"color\":\"light_purple\"}]"));
         loreList.add(NbtString.of("[{\"text\":\"Not Actual Item\",\"italic\":true,\"color\":\"red\"}]"));
         
         return stack;
      }
   
      public static int invContains(List<MagicInvItem> inv, MagicInvItem invItem){
         for(int i = 0; i < inv.size(); i++){
            MagicInvItem magicInvItem = inv.get(i);
            if(magicInvItem.hash.equals(invItem.hash)){
               return i;
            }
         }
         return -1;
      }
      
      public void addItem(ItemStack stack){
         stacks.add(new Pair<>(MagicItem.getUUID(stack),stack));
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
   
            for(MagicItemContainer container : containers){
               if(container.getContainerName().equals("Ender Chest")){
                  isEChest = true;
               }else if(container.getContainerName().equals("Shulker Box")){
                  isShulker = true;
               }
            }
            return isShulker && isEChest ? 0 : getConcMod();
         }else if(focus == 2){
            for(MagicItemContainer container : containers){
               if(container.getContainerName().equals("Ender Chest")){
                  return 0;
               }
            }
         }else if(focus == 3){
            for(MagicItemContainer container : containers){
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
         for(MagicItemContainer container : containers){
            str.append(container.getConcModStr()).append("+");
         }
         return (str.substring(0,str.length()-1));
      }
      
      public String getContainerString(){
         if(containers.isEmpty()) return "";
         StringBuilder str = new StringBuilder("[");
         for(MagicItemContainer container : containers){
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
