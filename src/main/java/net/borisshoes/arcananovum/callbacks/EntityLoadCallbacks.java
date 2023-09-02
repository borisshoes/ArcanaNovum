package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.entities.StasisPearlEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public class EntityLoadCallbacks {
   
   public static void loadEntity(Entity entity, ServerWorld serverWorld){
   
   }
   
   public static void unloadEntity(Entity entity, ServerWorld serverWorld){
      if(entity instanceof StasisPearlEntity stasisPearl){
         if(entity.getRemovalReason() != Entity.RemovalReason.DISCARDED && entity.getRemovalReason() != Entity.RemovalReason.KILLED){
            stasisPearl.killNextTick();
         }
      }
   }
}
