package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.network.chat.MutableComponent;

public class ArcanaItemCompendiumEntry extends CompendiumEntry{
   
   private final ArcanaItem arcanaItem;
   
   public ArcanaItemCompendiumEntry(ArcanaItem arcanaItem){
      super(arcanaItem.getCategories(), arcanaItem.getPrefItem());
      this.arcanaItem = arcanaItem;
   }
   
   public ArcanaItem getArcanaItem(){
      return arcanaItem;
   }
   
   
   @Override
   public MutableComponent getName(){
      return arcanaItem.getTranslatedName();
   }
   
   @Override
   public int getRarityValue(){
      return arcanaItem.getRarity().rarity;
   }
}
