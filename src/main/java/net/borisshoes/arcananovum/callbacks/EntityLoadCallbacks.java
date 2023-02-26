package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.bosses.nulconstruct.NulConstructFight;
import net.borisshoes.arcananovum.cardinalcomponents.IMagicEntityComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class EntityLoadCallbacks {
   
   public static void loadEntity(Entity entity, ServerWorld serverWorld){
   
   }
   
   public static void unloadEntity(Entity entity, ServerWorld serverWorld){
      try{
         IMagicEntityComponent entityComponent = MAGIC_ENTITY_LIST.get(serverWorld);
         List<MagicEntity> entities = entityComponent.getEntities();
         Iterator<MagicEntity> iter = entities.iterator();
         while(iter.hasNext()){
            MagicEntity magicEntity = iter.next();
            if(entity.getUuidAsString().equals(magicEntity.getUuid())){
               NbtCompound magicData = magicEntity.getData();
               String id = magicData.getString("id");
               if(id.equals(MagicItems.STASIS_PEARL.getId())){
                  if(entity.getRemovalReason() != Entity.RemovalReason.KILLED && entity.getRemovalReason() != Entity.RemovalReason.DISCARDED){
                     iter.remove();
                     entity.kill();
                  }
               }else if(MagicItemUtils.getItemFromId(id) instanceof RunicArrow){
                  if(entity.getRemovalReason() == Entity.RemovalReason.KILLED || entity.getRemovalReason() == Entity.RemovalReason.DISCARDED || entity.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION){
                     iter.remove();
                  }
               }else if(id.equals("boss_dragon_phantom")){
                  if(entity.getRemovalReason() == Entity.RemovalReason.KILLED){
                     magicData.putBoolean("dead",true);
                  }
               }else if(id.equals("boss_dragon_wizard")){
                  if(entity.getRemovalReason() == Entity.RemovalReason.KILLED){
                     magicData.putBoolean("dead",true);
                  }
               }else if(id.equals("nul_construct")){
                  if(entity.getRemovalReason() == Entity.RemovalReason.KILLED){
                     magicData.putBoolean("dead",true);
                     NulConstructFight.onDeath((WitherEntity)entity,magicEntity);
                  }
               }
         
               //log("Unloading magic entity ("+id+"): "+entity.getUuidAsString()+" "+entity.getRemovalReason());
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
