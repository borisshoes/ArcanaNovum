package net.borisshoes.arcananovum.gui;

/**
 * Interface for GUIs that want button click cooldowns.
 * Implementers must provide storage for the cooldown value via {@link #getClickCooldown()} and {@link #setClickCooldown(int)}.
 * Call {@link #tickClickCooldown()} in the GUI's onTick method.
 */
public interface ClickCooldown {
   
   int getCooldownDuration();
   
   /**
    * Gets the current cooldown value. Implementers must provide a field to back this.
    */
   int getClickCooldown();
   
   /**
    * Sets the current cooldown value. Implementers must provide a field to back this.
    */
   void setClickCooldown(int cooldown);
   
   /**
    * Resets the cooldown to its full duration.
    */
   default void resetClickCooldown(){
      setClickCooldown(getCooldownDuration());
   }
   
   /**
    * Resets the cooldown to a custom duration.
    */
   default void resetClickCooldown(int duration){
      setClickCooldown(duration);
   }
   
   /**
    * Decrements the cooldown. Call this in the GUI's onTick method.
    */
   default void tickClickCooldown(){
      int current = getClickCooldown();
      if(current > 0){
         setClickCooldown(current - 1);
      }
   }
   
   /**
    * Checks if the cooldown is currently active.
    */
   default boolean isOnClickCooldown(){
      return getClickCooldown() > 0;
   }
}

