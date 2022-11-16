package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.items.core.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.*;

import static net.borisshoes.arcananovum.Arcananovum.log;

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
            log("Item Missing Version Data");
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
      magicInvHelper(inv,magicInv,false,false);
      magicInvHelper(eChest,magicInv,true,false);
      return magicInv;
   }
   
   public static void magicInvHelper(Inventory inv, List<MagicInvItem> magicInv, boolean enderChest, boolean shulker){
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
            magicInvHelper(shulkerInv,magicInv,enderChest,true);
         }
         if(!isMagic){
            continue;
         }
         
      
         MagicItem magicItem = identifyItem(item);
         MagicInvItem invItem = new MagicInvItem(magicItem,item.getCount(),enderChest,shulker);
         int contains = MagicInvItem.invContains(magicInv,invItem);
         if(contains >= 0){
            magicInv.get(contains).count += invItem.count;
         }else{
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
         double containerMod = 1;
         if(magicInvItem.eChest) containerMod *= 0.5;
         if(magicInvItem.shulker) containerMod *= 0.5;
         concSum += Math.ceil(magicInvItem.count/(double)prefCount) * Math.round(containerMod*MagicRarity.getConcentration(magicItem.getRarity())+magicItem.getConcMod());
      }
      
      return concSum;
   }
   
   
   public static List<String> getConcBreakdown(ServerPlayerEntity player){
      ArrayList<String> list = new ArrayList<>();
      
      List<MagicInvItem> magicInv = getMagicInventory(player);
      Comparator<MagicInvItem> comparator = (MagicInvItem i1, MagicInvItem i2) -> {
         int r1 = i1.count*MagicRarity.getConcentration(i1.item.getRarity());
         if(i1.shulker) r1 += 50000;
         if(i1.eChest) r1 += 100000;
         int r2 = i2.count*MagicRarity.getConcentration(i2.item.getRarity());
         if(i2.shulker) r2 += 50000;
         if(i2.eChest) r2 += 100000;
         return r1 - r2;
      };
      magicInv.sort(comparator);
      
      for(MagicInvItem magicInvItem : magicInv){
         MagicItem magicItem = magicInvItem.item;
         int prefCount = magicItem.getPrefItem().getCount();
         double containerMod = 1;
         if(magicInvItem.eChest) containerMod *= 0.5;
         if(magicInvItem.shulker) containerMod *= 0.5;
         int multiplier = (int)Math.ceil(magicInvItem.count/(double)prefCount);
         int itemConc = multiplier * (int)Math.round(containerMod*MagicRarity.getConcentration(magicItem.getRarity())+magicItem.getConcMod());
         String multStr = multiplier > 1 ? " x"+multiplier : "";
         String contStr = magicInvItem.eChest && magicInvItem.shulker ? " [Ender Chest + Shulker Box]" : magicInvItem.eChest ? " [Ender Chest]" : magicInvItem.shulker ? " [Shulker Box]" : "";
         //String line = magicItem.getName()+multStr+" ("+itemConc+")"+contStr;
         String line = "[{\"text\":\"- "+magicItem.getName()+"\",\"italic\":false,\"color\":\"dark_aqua\"},{\"text\":\""+multStr+"\",\"color\":\"blue\"},{\"text\":\" ("+itemConc+")\",\"color\":\"dark_green\"},{\"text\":\""+contStr+"\",\"color\":\"dark_purple\"}]";
         list.add(line);
      }
      
      return list;
   }
   
   public static MagicItem getItemFromId(String id){
      return MagicItems.registry.get(id);
   }
   
   public static class MagicInvItem {
      public final boolean eChest;
      public final boolean shulker;
      private int count;
      public final MagicItem item;
      public final String hash;
   
      public MagicInvItem(MagicItem item, int count, boolean eChest, boolean shulker){
         this.eChest = eChest;
         this.shulker = shulker;
         this.count = count;
         this.item = item;
         this.hash = item.getId() + eChest + shulker;
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
   }
}
