package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         bossTickCheck(server);
         
         ArrayList<Tuple<BlockEntity, ArcanaBlockEntity>> toRemoveBlocks = new ArrayList<>();
         for(Map.Entry<Tuple<BlockEntity, ArcanaBlockEntity>, Integer> pair : ACTIVE_ARCANA_BLOCKS.entrySet()){
            if(pair.getValue() - 1 > 0){
               ACTIVE_ARCANA_BLOCKS.put(pair.getKey(), pair.getValue() - 1);
            }else{
               toRemoveBlocks.add(pair.getKey());
            }
         }
         toRemoveBlocks.forEach(ACTIVE_ARCANA_BLOCKS::remove);
         
         List<ServerPlayer> players = server.getPlayerList().getPlayers();
         players.forEach(p -> ArcanaNovum.data(p).tick(p));
         
         HashMap<ServerPlayer, Float> shieldTotals = new HashMap<>();
         for(TickTimerCallback callback : BorisLib.SERVER_TIMER_CALLBACKS){
            if(callback instanceof ShieldTimerCallback st){
               if(shieldTotals.containsKey(st.getPlayer())){
                  shieldTotals.put(st.getPlayer(), shieldTotals.get(st.getPlayer()) + st.getHearts());
                  if(shieldTotals.get(st.getPlayer()) >= 200 && st.getPlayer().getAbsorptionAmount() >= 200)
                     ArcanaAchievements.grant(st.getPlayer(), ArcanaAchievements.BUILT_LIKE_TANK);
               }else{
                  shieldTotals.put(st.getPlayer(), st.getHearts());
               }
            }
         }
         
         ContinuumAnchor.updateLoadedChunks(server);
         ItineranteurBlockEntity.tickZones();
         GeomanticSteleBlockEntity.tickZones();
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void bossTickCheck(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         Tuple<BossFights, CompoundTag> fight = DataAccess.getWorld(world.dimension(), BossFightData.KEY).getBossFight();
         if(fight != null){
            if(fight.getA() == BossFights.DRAGON){
               DragonBossFight.tick(server, fight.getB());
            }
         }
      }
   }
}
