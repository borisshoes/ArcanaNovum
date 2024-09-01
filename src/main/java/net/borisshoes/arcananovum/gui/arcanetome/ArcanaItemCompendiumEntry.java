package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.core.ArcanaItem;

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
   public String getName(){
      return arcanaItem.getNameString();
   }
   
   @Override
   public int getRarityValue(){
      return arcanaItem.getRarity().rarity;
   }
}
