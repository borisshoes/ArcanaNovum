package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public abstract class EnergyItem extends MagicItem{
   protected int maxEnergy;
   
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
      NbtCompound magic = new NbtCompound();
      magic.putString("id",id);
      magic.putInt("Rarity", MagicRarity.getRarityInt(rarity));
      magic.putInt("Version",version);
      magic.putString("UUID", "-");
      magic.putInt("energy",0);
      compound.put("arcananovum",magic);
      return compound;
   }
   
}
