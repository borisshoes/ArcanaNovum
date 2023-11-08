package net.borisshoes.arcananovum.core;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

public abstract class EnergyItem extends MagicItem{
   protected int initEnergy = 0;
   
   public abstract int getMaxEnergy(ItemStack item);
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int energy = magicTag.getInt("energy");
      NbtCompound newTag = super.updateItem(stack,server).getNbt();
      newTag.getCompound("arcananovum").putInt("energy",energy);
      stack.setNbt(newTag);
      return buildItemLore(stack,server);
   }
   
   public int getEnergy(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      return magicNbt.getInt("energy");
   }
   
   public void addEnergy(ItemStack item, int e){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int newE = Math.max(0, Math.min(getMaxEnergy(item), magicNbt.getInt("energy")+e));
      magicNbt.putInt("energy",newE);
   }
   
   public void setEnergy(ItemStack item, int e){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int newE = Math.max(0, Math.min(getMaxEnergy(item), e));
      magicNbt.putInt("energy",newE);
   }
   
   @Override
   protected NbtCompound addMagicNbt(NbtCompound compound){
      NbtCompound comp = super.addMagicNbt(compound);
      comp.getCompound("arcananovum").putInt("energy",initEnergy);
      return comp;
   }
   
}
