package net.borisshoes.arcananovum.core;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ArcanaItemContainer {
   
   private final Identifier id;
   private final Container inventory;
   private final int size, sortMod;
   private final MutableComponent concModStr, name;
   private final double concMod;
   
   public ArcanaItemContainer(Identifier id, Container inventory, int size, int sortMod, MutableComponent concModStr, MutableComponent name, double concMod){
      this.id = id;
      this.inventory = inventory;
      this.size = size;
      this.sortMod = sortMod;
      this.concModStr = concModStr;
      this.name = name;
      this.concMod = concMod;
   }
   
   public Container getInventory(){
      return inventory;
   }
   
   public int getSize(){
      return size;
   }
   
   public int getSortMod(){
      return sortMod;
   }
   
   public Identifier getId(){
      return id;
   }
   
   public MutableComponent getConcModStr(){
      return concModStr;
   }
   
   public MutableComponent getContainerName(){
      return name;
   }
   
   public double getConcMod(){
      return concMod;
   }
   
   public interface ArcanaItemContainerHaver {
      ArcanaItemContainer getArcanaItemContainer(ItemStack item);
   }
}
