package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.datastorage.BossFightData;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class DragonRespawnTimerCallback extends TickTimerCallback {
   private MinecraftServer server;
   
   public DragonRespawnTimerCallback(MinecraftServer server){
      super(900,null,null);
      this.server = server;
   }
   
   @Override
   public void onTimer(){
      try{
         Tuple<BossFights, CompoundTag> bossFight = DataAccess.getWorld(Level.END, BossFightData.KEY).getBossFight();
         if(bossFight != null && bossFight.getA() == BossFights.DRAGON){
            CompoundTag data = bossFight.getB();
            String state = data.getStringOr("State", "");
            ServerPlayer gm = server.getPlayerList().getPlayer(AlgoUtils.getUUID(data.getStringOr("GameMaster", "")));
            if(DragonBossFight.States.valueOf(state) == DragonBossFight.States.WAITING_RESPAWN){
               //Check for alive Dragon
               if(!server.getLevel(Level.END).getDragons().isEmpty()){
                  bossFight.getB().putString("State",DragonBossFight.States.WAITING_START.name());
                  // Confirm message
                  if(gm != null){
                     gm.sendSystemMessage(Component.literal("Dragon Respawned Successfully"));
                  }
                  return;
               }
            }
            if(gm != null){
               gm.sendSystemMessage(Component.literal("Dragon Respawn Failed, Cleaning Boss Fight"));
            }
         }
         // Clean Boss
         BossFight.cleanBoss(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
