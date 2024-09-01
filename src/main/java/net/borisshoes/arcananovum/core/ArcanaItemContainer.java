package net.borisshoes.arcananovum.core;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ArcanaItemContainer {
   
   private final Inventory inventory;
   private final int size, sortMod;
   private final String concModStr, name;
   private final double concMod;
   
   public ArcanaItemContainer(Inventory inventory, int size, int sortMod, String concModStr, String name, double concMod){
      this.inventory = inventory;
      this.size = size;
      this.sortMod = sortMod;
      this.concModStr = concModStr;
      this.name = name;
      this.concMod = concMod;
   }
   
   public Inventory getInventory(){
      return inventory;
   }
   
   public int getSize(){
      return size;
   }
   
   public int getSortMod(){
      return sortMod;
   }
   
   public String getConcModStr(){
      return concModStr;
   }
   
   public String getContainerName(){
      return name;
   }
   
   public double getConcMod(){
      return concMod;
   }
   
   public interface ArcanaItemContainerHaver {
      ArcanaItemContainer getArcanaItemContainer(ItemStack item);
   }
}
