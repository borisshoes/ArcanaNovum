package net.borisshoes.arcananovum.items.core;

import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public abstract class EnergyItem extends MagicItem{
   protected int maxEnergy;
   protected int initEnergy = 0;
   
   @Override
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      int energy = magicTag.getInt("energy");
      NbtCompound newTag = super.updateItem(stack).getNbt();
      newTag.getCompound("arcananovum").putInt("energy",energy);
      stack.setNbt(newTag);
      return stack;
   }
   
   public int getEnergy(ItemStack item){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      return magicNbt.getInt("energy");
   }
   
   public int getMaxEnergy(ItemStack item){
      return maxEnergy;
   }
   
   public void addEnergy(ItemStack item, int e){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int newE = Math.max(0, Math.min(maxEnergy, magicNbt.getInt("energy")+e));
      magicNbt.putInt("energy",newE);
   }
   
   public void setEnergy(ItemStack item, int e){
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
      int newE = Math.max(0, Math.min(maxEnergy, e));
      magicNbt.putInt("energy",newE);
   }
   
   @Override
   protected NbtCompound addMagicNbt(NbtCompound compound){
      NbtCompound comp = super.addMagicNbt(compound);
      comp.getCompound("arcananovum").putInt("energy",initEnergy);
      return comp;
   }
   
}
