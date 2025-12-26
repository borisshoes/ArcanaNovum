package net.borisshoes.arcananovum.bosses;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class BossFight {
   public static int cleanBoss(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         if(BOSS_FIGHT.get(world).getBossFight() != null){
            BOSS_FIGHT.get(world).removeBossFight();
         }
      }
      
      //log("Cleaning Boss");
      return 0;
   }
}
