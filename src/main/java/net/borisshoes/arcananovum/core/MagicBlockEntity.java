package net.borisshoes.arcananovum.core;

import net.borisshoes.arcananovum.augments.ArcanaAugment;
import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

public interface MagicBlockEntity {
   TreeMap<ArcanaAugment, Integer> getAugments();
   
   String getCrafterId();
   
   String getUuid();
   
   boolean isSynthetic();
   
   String getCustomArcanaName();
   
   void initialize(TreeMap<ArcanaAugment,Integer> augments, String crafterId, String uuid, boolean synthetic, @Nullable String customName);
   
   MagicItem getMagicItem();
   
   default boolean isAssembled(){
      return true;
   }
}
