package net.borisshoes.arcananovum.core;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public abstract class EnergyItem extends ArcanaItem {
   public static final String ENERGY_TAG = "energy";
   protected int initEnergy = 0;
   
   public abstract int getMaxEnergy(ItemStack item);
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      int energy = getIntProperty(stack,ENERGY_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,ENERGY_TAG, energy);
      return buildItemLore(newStack,server);
   }
   
   public int getEnergy(ItemStack item){
      return getIntProperty(item,ENERGY_TAG);
   }
   
   public void addEnergy(ItemStack item, int e){
      int newE = Math.max(0, Math.min(getMaxEnergy(item), getIntProperty(item,ENERGY_TAG)+e));
      putProperty(item,ENERGY_TAG, newE);
   }
   
   public void setEnergy(ItemStack item, int e){
      int newE = Math.max(0, Math.min(getMaxEnergy(item), e));
      putProperty(item,ENERGY_TAG, newE);
   }
   
   @Override
   public ItemStack initializeArcanaTag(ItemStack stack){
      ItemStack comp = super.initializeArcanaTag(stack);
      putProperty(comp,ENERGY_TAG, initEnergy);
      return stack;
   }
   
}
