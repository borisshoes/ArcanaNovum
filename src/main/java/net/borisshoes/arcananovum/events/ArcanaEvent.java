package net.borisshoes.arcananovum.events;

import java.util.UUID;

public abstract class ArcanaEvent {
   public final int lifespan;
   public final String id;
   private final UUID uuid;
   private int timeAlive;
   private boolean removalMark;
   
   public ArcanaEvent(String id, int lifespan){
      this.lifespan = lifespan;
      this.id = id;
      this.timeAlive = 0;
      this.uuid = UUID.randomUUID();
      this.removalMark = false;
   }
   
   public void tick(){
      timeAlive++;
   }
   
   public boolean isExpired(){
      return timeAlive >= lifespan || removalMark;
   }
   
   public UUID getUuid(){
      return uuid;
   }
   
   public void markForRemoval(){
      this.removalMark = true;
   }
}
