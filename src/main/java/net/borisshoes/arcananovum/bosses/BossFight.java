package net.borisshoes.arcananovum.bosses;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.Arcananovum.log;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class BossFight {
   public static int cleanBoss(MinecraftServer server){
      for(ServerWorld world : server.getWorlds()){
         if(BOSS_FIGHT.get(world).getBossFight() != null){
            BOSS_FIGHT.get(world).removeBossFight();
         }
         
         List<MagicEntity> entities = MAGIC_ENTITY_LIST.get(world).getEntities();
         Iterator<MagicEntity> iter2 = entities.iterator();
         while(iter2.hasNext()){
            MagicEntity magicEntity = iter2.next();
            NbtCompound magicData = magicEntity.getData();
            String id = magicData.getString("id");
            String uuid = magicEntity.getUuid();
            if(id.startsWith("boss")){
               iter2.remove();
            }
         }
      }
      
      //log("Cleaning Boss");
      return 0;
   }
}
