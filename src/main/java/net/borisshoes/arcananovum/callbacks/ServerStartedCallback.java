package net.borisshoes.arcananovum.callbacks;

import com.mojang.authlib.GameProfile;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.borisshoes.arcananovum.ArcanaNovum.log;

public class ServerStartedCallback {
   
   public static void serverStarted(MinecraftServer server){
      ArcanaRegistry.onServerStarted(server);
      getPlayerStats(server);
   }
   
   public static void getPlayerStats(MinecraftServer server){
      try{
         ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.clear();
         ArcanaNovum.PLAYER_XP_TRACKER.clear();
         UserNameToIdResolver baseCache = server.services().nameToIdCache();
         List<ServerPlayer> allPlayers = new ArrayList<>();
         if(baseCache instanceof CachedUserNameToIdResolver userCache){
            List<CachedUserNameToIdResolver.GameProfileInfo> cacheEntries = userCache.load();
            
            for(CachedUserNameToIdResolver.GameProfileInfo cacheEntry : cacheEntries){
               Optional<GameProfile> opt = server.services().profileResolver().fetchById(cacheEntry.nameAndId().id());;
               if(opt.isEmpty()) continue;
               GameProfile reqProfile = opt.get();
               ServerPlayer reqPlayer = MinecraftUtils.getRequestedPlayer(server, new NameAndId(reqProfile));
               allPlayers.add(reqPlayer);
            }
         }else{
            log(2,"Was unable to pull player data");
         }
         
         for(ServerPlayer player : allPlayers){
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
            
            for(ArcanaAchievement achieve : ArcanaAchievements.registry.values()){
               List<UUID> curList = ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.containsKey(achieve.id) ? ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.get(achieve.id) : new ArrayList<>();
               if(profile.hasAcheivement(achieve.getArcanaItem().getId(), achieve.id)){
                  curList.add(player.getUUID());
                  ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achieve.id,curList);
               }
            }
            ArcanaNovum.PLAYER_XP_TRACKER.put(player.getUUID(),profile.getXP());
         }
         
         for(ArcanaAchievement achieve : ArcanaAchievements.registry.values()){
            if(!ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.containsKey(achieve.id)){
               ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.put(achieve.id,new ArrayList<>());
            }
         }
      }catch(Exception e){
         log(2,e.toString());
      }
   }
}
