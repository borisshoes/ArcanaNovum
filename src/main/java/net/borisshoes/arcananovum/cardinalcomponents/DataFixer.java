package net.borisshoes.arcananovum.cardinalcomponents;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.datastorage.AnchorData;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.ACTIVE_ANCHORS;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class DataFixer {
   public static void onServerStarted(MinecraftServer server){
      boolean anyDataMoved = false;
      StringBuilder movedWorlds = new StringBuilder();
      
      for(ServerLevel world : server.getAllLevels()){
         ResourceKey<Level> key = world.dimension();
         BossFightData bfData = DataAccess.getWorld(key, BossFightData.KEY);
         AnchorData anchorData = DataAccess.getWorld(key, AnchorData.KEY);
         IBossFightComponent oldBfData = BOSS_FIGHT.get(world);
         IAnchorsComponent oldAnchorData = ACTIVE_ANCHORS.get(world);
         
         boolean worldHadData = false;
         if(migrateBossFightData(oldBfData, bfData)){
            worldHadData = true;
         }
         if(migrateAnchorData(oldAnchorData, anchorData)){
            worldHadData = true;
         }
         if(worldHadData){
            anyDataMoved = true;
            if(!movedWorlds.isEmpty()){
               movedWorlds.append(", ");
            }
            movedWorlds.append(key);
         }
      }
      
      if(anyDataMoved){
         ArcanaNovum.log(0,"Migrated world data for dimensions: " + movedWorlds);
         ArcanaNovum.log(0,"This migration should not happen again...");
      }
   }
   
   private static boolean migrateBossFightData(IBossFightComponent oldData, BossFightData newData){
      Tuple<BossFights, CompoundTag> bossFight = oldData.getBossFight();
      if(bossFight != null && newData.getBossFight() == null){
         newData.setBossFight(bossFight.getA(), bossFight.getB());
         oldData.removeBossFight();
         return true;
      }
      return false;
   }
   
   private static boolean migrateAnchorData(IAnchorsComponent oldData, AnchorData newData){
      List<BlockPos> anchors = oldData.getAnchors();
      if(anchors != null && !anchors.isEmpty() && newData.getAnchors().isEmpty()){
         for(BlockPos anchor : anchors){
            newData.addAnchor(anchor);
         }
         anchors.clear();
         return true;
      }
      return false;
   }
   
   public static void onPlayerJoin(ServerGamePacketListenerImpl handler, PacketSender packetSender, MinecraftServer server){
      ServerPlayer player = handler.getPlayer();
      IArcanaProfileComponent oldData = PLAYER_DATA.get(player);
      if(oldData.getXP() < 0){
         return;
      }
      ArcanaPlayerData newData = DataAccess.getPlayer(player.getUUID(),ArcanaPlayerData.KEY);
      migratePlayerData(oldData, newData);
      clearOldData(oldData);
      ArcanaNovum.log(0,"Migrated data for "+player.getScoreboardName()+"...");
   }
   
   private static void migratePlayerData(IArcanaProfileComponent oldData, ArcanaPlayerData newData){
      for(String craftedItem : oldData.getCrafted()){
         if(newData.getCrafted().stream().noneMatch(s -> s.equals(craftedItem))){
            newData.getCrafted().add(craftedItem);
         }
      }
      
      for(String researchedItem : oldData.getResearchedItems()){
         if(newData.getResearchedItems().stream().noneMatch(s -> s.equals(researchedItem))){
            newData.getResearchedItems().add(researchedItem);
         }
      }
      
      HashMap<String, Tag> oldMiscData = oldData.getMiscDataMap();
      for(Map.Entry<String, Tag> entry : oldMiscData.entrySet()){
         if(newData.getMiscData(entry.getKey()) == null){
            newData.addMiscData(entry.getKey(), entry.getValue());
         }
      }
      
      HashMap<String, List<ArcanaAchievement>> oldAchievements = oldData.getAchievements();
      for(Map.Entry<String, List<ArcanaAchievement>> entry : oldAchievements.entrySet()){
         for(ArcanaAchievement achievement : entry.getValue()){
            newData.setAchievement(entry.getKey(), achievement);
         }
      }
      
      HashMap<ArcanaAugment, Integer> oldAugments = oldData.getAugments();
      for(Map.Entry<ArcanaAugment, Integer> entry : oldAugments.entrySet()){
         if(newData.getAugmentLevel(entry.getKey().id) == 0){
            newData.setAugmentLevel(entry.getKey().id, entry.getValue());
         }
      }
      
      if(newData.getLevel() == 0 && newData.getXP() == 0){
         newData.setLevel(oldData.getLevel());
         newData.setXP(oldData.getXP());
      }
   }
   
   private static void clearOldData(IArcanaProfileComponent oldData){
      oldData.getCrafted().clear();
      oldData.getResearchedItems().clear();
      oldData.getAchievements().clear();
      oldData.removeAllAugments();
      oldData.getMiscDataMap().clear();
      oldData.setLevel(0);
      oldData.setXP(-1);
   }
}
