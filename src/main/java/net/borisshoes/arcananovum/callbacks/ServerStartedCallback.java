package net.borisshoes.arcananovum.callbacks;

import com.mojang.authlib.GameProfile;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerStartedCallback {
   
   public static void serverStarted(MinecraftServer server){
      getPlayerStats(server);
   }
   
   public static void getPlayerStats(MinecraftServer server){
      PlayerManager playerManager = server.getPlayerManager();
      UserCache userCache = server.getUserCache();
      List<ServerPlayerEntity> allPlayers = new ArrayList<>();
      List<UserCache.Entry> cacheEntries = userCache.load();
      
      for(UserCache.Entry cacheEntry : cacheEntries){
         GameProfile reqProfile = cacheEntry.getProfile();
         ServerPlayerEntity reqPlayer = playerManager.getPlayer(reqProfile.getName());
         
         if(reqPlayer == null){ // Player Offline
            reqPlayer = playerManager.createPlayer(reqProfile, SyncedClientOptions.createDefault());
            server.getPlayerManager().loadPlayerData(reqPlayer);
         }
         allPlayers.add(reqPlayer);
      }
      
      ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.clear();
      ArcanaNovum.PLAYER_XP_TRACKER.clear();
      for(ServerPlayerEntity player : allPlayers){
         IArcanaProfileComponent profile = ArcanaNovum.data(player);
         
         for(ArcanaAchievement achieve : ArcanaAchievements.registry.values()){
            List<UUID> curList = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.containsKey(achieve.id) ? ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achieve.id) : new ArrayList<>();
            if(profile.hasAcheivement(achieve.getArcanaItem().getId(), achieve.id)){
               curList.add(player.getUuid());
               ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achieve.id,curList);
            }
         }
         ArcanaNovum.PLAYER_XP_TRACKER.put(player.getUuid(),profile.getXP());
      }
      
      for(ArcanaAchievement achieve : ArcanaAchievements.registry.values()){
         if(!ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.containsKey(achieve.id)){
            ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achieve.id,new ArrayList<>());
         }
      }
   }
}
