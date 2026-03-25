package net.borisshoes.arcananovum.utils;

/**
 * Utility class to share damage tracking data between mixin classes with different priorities.
 */
public class SharedValues {
   public static final ThreadLocal<Float> VULNERABILITY_TRACKER_DAMAGE = new ThreadLocal<>();
}

