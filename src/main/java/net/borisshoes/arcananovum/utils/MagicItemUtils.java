package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.core.*;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

import static net.borisshoes.arcananovum.Arcananovum.log;
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
            MagicItem magicItem = MagicItems.registry.get(id);
            if(magicItem == null)
               return false;
            //System.out.println("Magic Item Name: "+magicItem.getName());
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
   
   public static boolean needsMagicTick(ItemStack item){
      try{
         if(isMagic(item)){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicTag = itemNbt.getCompound("arcananovum");
            // We know that the item is magic, get its class from its id
            String id = magicTag.getString("id");
            if(id==null)
               return false;
            //System.out.println("Found magic id: "+id);
            MagicItem magicItem = MagicItems.registry.get(id);
            //System.out.println("Magic Item Name: "+magicItem.getName());
            return (magicItem instanceof TickingItem);
         }
         
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isUsableItem(ItemStack item){
      try{
         if(isMagic(item)){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicTag = itemNbt.getCompound("arcananovum");
            // We know that the item is magic, get its class from its id
            String id = magicTag.getString("id");
            if(id==null)
               return false;
            //System.out.println("Found magic id: "+id);
            MagicItem magicItem = MagicItems.registry.get(id);
            //System.out.println("Magic Item Name: "+magicItem.getName());
            return (magicItem instanceof UsableItem);
         }
      
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean isAttackingItem(ItemStack item){
      try{
         if(isMagic(item)){
            NbtCompound itemNbt = item.getNbt();
            NbtCompound magicTag = itemNbt.getCompound("arcananovum");
            // We know that the item is magic, get its class from its id
            String id = magicTag.getString("id");
            if(id==null)
               return false;
            //System.out.println("Found magic id: "+id);
            MagicItem magicItem = MagicItems.registry.get(id);
            //System.out.println("Magic Item Name: "+magicItem.getName());
            return (magicItem instanceof AttackingItem);
         }
         
         return false;
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
            MagicItem magicItem = MagicItems.registry.get(id);
            //System.out.println("Magic Item Name: "+magicItem.getName());
            return (magicItem instanceof LeftClickItem);
         }
         
         return false;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static TickingItem identifyTickingItem(ItemStack item){
      if(needsMagicTick(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return (TickingItem) MagicItems.registry.get(id);
      }
      return null;
   }
   
   public static UsableItem identifyUsableItem(ItemStack item){
      if(isUsableItem(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return (UsableItem) MagicItems.registry.get(id);
      }
      return null;
   }
   
   public static AttackingItem identifyAttackingItem(ItemStack item){
      if(isAttackingItem(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return (AttackingItem) MagicItems.registry.get(id);
      }
      return null;
   }
   
   public static LeftClickItem identifyLeftClickItem(ItemStack item){
      if(isLeftClickItem(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return (LeftClickItem) MagicItems.registry.get(id);
      }
      return null;
   }
   
   public static MagicItem identifyItem(ItemStack item){
      if(isMagic(item)){
         NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
         String id = magicTag.getString("id");
         return MagicItems.registry.get(id);
      }
      return null;
   }
   
   public static EnergyItem identifyEnergyItem(ItemStack item){
      if(isEnergyItem(item)){
         return (EnergyItem) identifyItem(item);
      }
      return null;
   }
   
   public static List<MagicInvItem> getMagicInventory(ServerPlayerEntity player){
      List<MagicInvItem> magicInv = new ArrayList<>();
      PlayerInventory inv = player.getInventory();
      EnderChestInventory eChest = player.getEnderChestInventory();
      ArrayList<MagicItemContainer> eChestContainerList = new ArrayList<>();
      eChestContainerList.add(new EnderChestMagicContainer(player));
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
            containersCopy.add(new ShulkerBoxMagicContainer(item));
            magicInvHelper(shulkerInv,magicInv,containersCopy);
         }
         if(!isMagic){
            continue;
         }
         MagicItem magicItem = identifyItem(item);
         if(magicItem instanceof MagicItemContainer magicContainer){
            ArrayList<MagicItemContainer> containersCopy = new ArrayList<>(containers);
            containersCopy.add(magicContainer);
            magicInvHelper(magicContainer.getItems(item),magicInv,containersCopy);
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
         concSum += Math.ceil(magicInvItem.count/(double)prefCount) * Math.ceil(containerMod*(MagicRarity.getConcentration(magicItem.getRarity())+magicInvItem.getAugmentConc(player)));
      }
      
      return concSum;
   }
   
   
   public static List<String> getConcBreakdown(ServerPlayerEntity player){
      ArrayList<String> list = new ArrayList<>();
      
      List<MagicInvItem> magicInv = getMagicInventory(player);
      Comparator<MagicInvItem> comparator = (MagicInvItem i1, MagicInvItem i2) -> {
         int r1 = i1.count*MagicRarity.getConcentration(i1.item.getRarity());
         r1 += 10000*i1.getSortMod();
         int r2 = i2.count*MagicRarity.getConcentration(i2.item.getRarity());
         r2 += 10000*i2.getSortMod();
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
         String line = "[{\"text\":\"- "+magicItem.getName()+"\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+multStr+"\",\"color\":\"blue\"},{\"text\":\" ("+itemConc+")\",\"color\":\"dark_green\"},{\"text\":\" "+contStr+"\",\"color\":\"dark_purple\"}]";
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
         if(!MagicItems.registry.containsKey("id")) continue;
         if(getItemFromId(id).getRarity() == rarity ^ exclude) count++;
      }
      return count;
   }
   
   public static MagicItem getItemFromId(String id){
      return MagicItems.registry.get(id);
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
            ArcanaAugments.applyAugment(stack,aug.id,itemLvl);
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
         stacks.add(new Pair<>(item.getUUID(stack),stack));
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
         int adaptability = profile.getAugmentLevel("adaptability");
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
         int focus = profile.getAugmentLevel("focus");
         if(focus == 1){
            boolean isEChest = false;
            boolean isShulker = false;
   
            for(MagicItemContainer container : containers){
               if(container instanceof EnderChestMagicContainer){
                  isEChest = true;
               }else if(container instanceof ShulkerBoxMagicContainer){
                  isShulker = true;
               }
            }
            return isShulker && isEChest ? 0 : getConcMod();
         }else if(focus == 2){
            for(MagicItemContainer container : containers){
               if(container instanceof EnderChestMagicContainer){
                  return 0;
               }
            }
         }else if(focus == 3){
            for(MagicItemContainer container : containers){
               if(container instanceof EnderChestMagicContainer){
                  return 0;
               }else if(container instanceof ShulkerBoxMagicContainer){
                  return 0;
               }
            }
         }
         return getConcMod();
      }
   
      public String getShortContainerString(){
         if(containers.size() == 0) return "Inv";
         StringBuilder str = new StringBuilder();
         for(MagicItemContainer container : containers){
            str.append(container.getConcModStr()).append("+");
         }
         return (str.substring(0,str.length()-1));
      }
      
      public String getContainerString(){
         if(containers.size() == 0) return "";
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
   
   static class EnderChestMagicContainer implements MagicItemContainer{
      
      private final EnderChestInventory inv;
      
      public EnderChestMagicContainer(ServerPlayerEntity player){
         this.inv = player.getEnderChestInventory();
      }
      
      @Override
      public Inventory getItems(ItemStack item){
         return inv;
      }
      
      @Override
      public double getConcMod(){
         return 0.5;
      }
      
      @Override
      public String getConcModStr(){
         return "EC";
      }
      
      @Override
      public String getContainerName(){
         return "Ender Chest";
      }
      
      @Override
      public int getSize(){
         return inv.size();
      }
      
      @Override
      public int getSortMod(){
         return 100;
      }
   }
   
   static class ShulkerBoxMagicContainer implements MagicItemContainer{
   
      private final SimpleInventory inv;
   
      public ShulkerBoxMagicContainer(ItemStack item){
         NbtCompound tag = item.getNbt();
         NbtCompound bet = tag.getCompound("BlockEntityTag");
         DefaultedList<ItemStack> shulkerItems = DefaultedList.ofSize(27, ItemStack.EMPTY);
         Inventories.readNbt(bet, shulkerItems);
         this.inv = new SimpleInventory(27);
         for(int j = 0; j < shulkerItems.size(); j++){
            inv.setStack(j,shulkerItems.get(j));
         }
      }
   
      @Override
      public Inventory getItems(ItemStack item){
         return inv;
      }
   
      @Override
      public double getConcMod(){
         return 0.5;
      }
   
      @Override
      public String getConcModStr(){
         return "SB";
      }
   
      @Override
      public String getContainerName(){
         return "Shulker Box";
      }
   
      @Override
      public int getSize(){
         return inv.size();
      }
   
      @Override
      public int getSortMod(){
         return 10;
      }
   }
}
