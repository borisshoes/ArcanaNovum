package net.borisshoes.arcananovum.bosses;

import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class BossFight {
   public static int cleanBoss(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         if(DataAccess.getWorld(world.dimension(), BossFightData.KEY).getBossFight() != null){
            DataAccess.getWorld(world.dimension(), BossFightData.KEY).removeBossFight();
         }
      }
      
      //log("Cleaning Boss");
      return 0;
   }
}
