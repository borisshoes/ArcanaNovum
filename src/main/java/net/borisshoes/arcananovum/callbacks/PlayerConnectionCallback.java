package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.login.MaxHealthLoginCallback;
import net.borisshoes.arcananovum.cardinalcomponents.DataFixer;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.gui.VirtualInventoryGui;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.borisshoes.arcananovum.ArcanaNovum.VIRTUAL_INVENTORY_GUIS;

public class PlayerConnectionCallback {
   public static void onPlayerJoin(ServerGamePacketListenerImpl netHandler, PacketSender sender, MinecraftServer server){
      ServerPlayer player = netHandler.player;
      DataFixer.onPlayerJoin(netHandler,sender,server);
   
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      profile.setUsername(player.getGameProfile().name());
      if(profile.getLevel() == 0){ // Profile needs initialization
         profile.setLevel(1);
      }
      profile.setLevel(LevelUtils.levelFromXp(profile.getXP())); // update level from xp just in case leveling changed
   
   
      // Linked Augments
      HashMap<String,ArrayList<ArcanaAugment>> linkedAugments = new HashMap<>();
      HashMap<String,Integer> highestValue = new HashMap<>();
      for(Map.Entry<ArcanaAugment, String> entry : ArcanaAugments.linkedAugments.entrySet()){
         String linkedId = entry.getValue();
         ArcanaAugment augment = entry.getKey();
         
         if(linkedAugments.containsKey(linkedId)){
            linkedAugments.get(linkedId).add(augment);
            int lvl = profile.getAugmentLevel(augment);
            if(lvl > highestValue.get(linkedId)){
               highestValue.put(linkedId,lvl);
            }
         }else{
            ArrayList<ArcanaAugment> augs = new ArrayList<>();
            augs.add(augment);
            highestValue.put(linkedId,profile.getAugmentLevel(augment));
            linkedAugments.put(linkedId,augs);
         }
      }
      
      for(Map.Entry<String,ArrayList<ArcanaAugment>> entry : linkedAugments.entrySet()){
         ArrayList<ArcanaAugment> augs = entry.getValue();
         for(ArcanaAugment aug : augs){
            profile.setAugmentLevel(aug,highestValue.get(entry.getKey()));
         }
      }
   
      for(Map.Entry<ArcanaAugment, Integer> entry : profile.getAugments().entrySet()){
         ArcanaAugment baseAug = ArcanaAugments.registry.get(entry.getKey());
         if(baseAug != null){
            if(entry.getValue() > baseAug.getTiers().length){
               profile.setAugmentLevel(entry.getKey(),baseAug.getTiers().length);
            }
         }
      }
      
      if(profile.getMiscData(QuiverItem.QUIVER_CD_TAG) == null){
         profile.addMiscData(QuiverItem.QUIVER_CD_TAG, IntTag.valueOf(0));
      }
      if(profile.getMiscData(QuiverItem.RUNIC_INV_ID_TAG) == null){
         profile.addMiscData(QuiverItem.RUNIC_INV_ID_TAG, StringTag.valueOf(""));
      }
      if(profile.getMiscData(QuiverItem.ARROW_INV_ID_TAG) == null){
         profile.addMiscData(QuiverItem.ARROW_INV_ID_TAG, StringTag.valueOf(""));
      }
      if(profile.getMiscData(QuiverItem.RUNIC_INV_SLOT_TAG) == null){
         profile.addMiscData(QuiverItem.RUNIC_INV_SLOT_TAG, IntTag.valueOf(0));
      }
      if(profile.getMiscData(QuiverItem.ARROW_INV_SLOT_TAG) == null){
         profile.addMiscData(QuiverItem.ARROW_INV_SLOT_TAG, IntTag.valueOf(0));
      }
      if(profile.getMiscData(ArcanaPlayerData.CONCENTRATION_TICK_TAG) == null){
         profile.addMiscData(ArcanaPlayerData.CONCENTRATION_TICK_TAG, IntTag.valueOf(0));
      }
      if(profile.getMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG) == null){
         profile.addMiscData(ArcanaPlayerData.ADMIN_SKILL_POINTS_TAG, IntTag.valueOf(0));
      }
   }
   
   public static void onPlayerLeave(ServerGamePacketListenerImpl handler, MinecraftServer server){
      ServerPlayer player = handler.player;
      if(player.getMaxHealth() > 20 && player.getHealth() > 20){
         BorisLib.addLoginCallback(new MaxHealthLoginCallback(server,player.getStringUUID(),player.getHealth()));
      }
      
      VIRTUAL_INVENTORY_GUIS.forEach((virtualInventoryGui, p) -> {
         if(player.getStringUUID().equals(p.getStringUUID()) && virtualInventoryGui.getInventory() != null){
            for(ItemStack itemStack : virtualInventoryGui.getInventory()){
               if(!itemStack.isEmpty()){
                  BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(itemStack.copyAndClear(),player,0));
               }
            }
         }
      });
      for(VirtualInventoryGui<?> inv : new ArrayList<>(VIRTUAL_INVENTORY_GUIS.keySet())){
         VIRTUAL_INVENTORY_GUIS.remove(inv);
      }
   }
}
