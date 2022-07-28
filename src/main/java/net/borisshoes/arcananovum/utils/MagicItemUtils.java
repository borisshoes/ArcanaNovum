package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.items.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      if(!isMagic(item))
         return false;
      try{
         NbtCompound itemNbt = item.getNbt();
         NbtCompound magicTag = itemNbt.getCompound("arcananovum");
         if(!magicTag.contains("Version",NbtCompound.INT_TYPE)){
            System.out.println("Missing Version Data");
            return true; // Version tag missing, needs update
         }
         int version = magicTag.getInt("Version");
         return version != MagicItem.version; // Version mismatch, needs update
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
   
   public static HashMap<MagicItem,Integer> getMagicInventory(ServerPlayerEntity player){
      HashMap<MagicItem,Integer> magicInv = new HashMap<>();
      PlayerInventory inv = player.getInventory();
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty())
            continue;
         boolean isMagic = MagicItemUtils.isMagic(item);
         if(!isMagic)
            continue;
         
         MagicItem magicItem = identifyItem(item);
         if(magicInv.containsKey(magicItem)){
            magicInv.put(magicItem,magicInv.get(magicItem)+item.getCount());
         }else{
            magicInv.put(magicItem,item.getCount());
         }
      }
      return magicInv;
   }
   
   public static int getUsedConcentration(ServerPlayerEntity player){
      int concSum = 0;
      HashMap<MagicItem,Integer> magicInv = getMagicInventory(player);
      for(Map.Entry<MagicItem, Integer> entry : magicInv.entrySet()){
         MagicItem magicItem = entry.getKey();
         int prefCount = magicItem.getPrefItem().getCount();
         concSum += Math.ceil(entry.getValue()/(double)prefCount) * MagicRarity.getConcentration(magicItem.getRarity())+magicItem.getConcMod();
      }
      return concSum;
   }
   
   public static List<String> getConcBreakdown(ServerPlayerEntity player){
      ArrayList<String> list = new ArrayList<>();
   
      HashMap<MagicItem,Integer> magicInv = getMagicInventory(player);
      for(Map.Entry<MagicItem, Integer> entry : magicInv.entrySet()){
         MagicItem magicItem = entry.getKey();
         int prefCount = magicItem.getPrefItem().getCount();
         int multiplier = (int)Math.ceil(entry.getValue()/(double)prefCount);
         int itemConc = multiplier * MagicRarity.getConcentration(magicItem.getRarity())+magicItem.getConcMod();
         String multStr = multiplier > 1 ? " x"+multiplier : "";
         String line = magicItem.getName()+multStr+" ("+itemConc+")";
         list.add(line);
      }
      return list;
   }
   
   public static MagicItem getItemFromId(String id){
      return MagicItems.registry.get(id);
   }
}
