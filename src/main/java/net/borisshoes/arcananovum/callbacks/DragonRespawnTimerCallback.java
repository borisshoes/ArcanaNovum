package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.bosses.BossFight;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

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
         Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(server.getWorld(World.END)).getBossFight();
         if(bossFight != null && bossFight.getLeft() == BossFights.DRAGON){
            NbtCompound data = bossFight.getRight();
            String state = data.getString("State", "");
            ServerPlayerEntity gm = server.getPlayerManager().getPlayer(AlgoUtils.getUUID(data.getString("GameMaster", "")));
            if(DragonBossFight.States.valueOf(state) == DragonBossFight.States.WAITING_RESPAWN){
               //Check for alive Dragon
               if(!server.getWorld(World.END).getAliveEnderDragons().isEmpty()){
                  bossFight.getRight().putString("State",DragonBossFight.States.WAITING_START.name());
                  // Confirm message
                  if(gm != null){
                     gm.sendMessage(Text.literal("Dragon Respawned Successfully"));
                  }
                  return;
               }
            }
            if(gm != null){
               gm.sendMessage(Text.literal("Dragon Respawn Failed, Cleaning Boss Fight"));
            }
         }
         // Clean Boss
         BossFight.cleanBoss(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
