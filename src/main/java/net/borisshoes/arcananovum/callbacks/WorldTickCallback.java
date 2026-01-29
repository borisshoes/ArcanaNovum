package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.datastorage.InterdictionZones;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.minecraft.server.level.ServerLevel;

public class WorldTickCallback {
   
   public static void onWorldTick(ServerLevel serverWorld){
      try{
         DataAccess.getWorld(serverWorld.dimension(), InterdictionZones.KEY).tick();
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
