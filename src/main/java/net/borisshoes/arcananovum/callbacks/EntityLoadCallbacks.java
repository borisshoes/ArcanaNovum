package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.MagicItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import static net.borisshoes.arcananovum.cardinalcomponents.MagicEntityComponentInitializer.MAGIC_ENTITY_LIST;

public class EntityLoadCallbacks {
   
   public static void loadEntity(Entity entity, ServerWorld serverWorld){
   
   }
   
   public static void unloadEntity(Entity entity, ServerWorld serverWorld){
      for(MagicEntity magicEntity : MAGIC_ENTITY_LIST.get(serverWorld).getEntities()){
         if(entity.getUuidAsString().equals(magicEntity.getUuid())){
            NbtCompound magicData = magicEntity.getData();
            if(magicData.getString("id").equals(MagicItems.STASIS_PEARL.getId())){
               if(entity.getRemovalReason() != Entity.RemovalReason.KILLED && entity.getRemovalReason() != Entity.RemovalReason.DISCARDED){
                  MAGIC_ENTITY_LIST.get(serverWorld).removeEntity(magicEntity);
                  entity.kill();
                  System.out.println("Unloading Entity: "+entity.getEntityName()+entity.getRemovalReason());
               }
            }
         }
      }
   }
}
