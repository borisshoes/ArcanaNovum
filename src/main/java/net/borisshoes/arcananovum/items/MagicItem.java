package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class MagicItem implements Comparable<MagicItem>{
   protected String name;
   protected String id;
   protected MagicRarity rarity;
   protected ItemStack prefItem;
   protected NbtCompound prefNBT;
   protected int concentrationModifier;
   protected MagicItemRecipe recipe;
   protected NbtCompound bookLore;
   public static int version = 7;
   
   public String getName(){
      return name;
   }
   
   public String getId(){
      return id;
   }
   
   public MagicRarity getRarity(){
      return rarity;
   }
   
   public int getConcMod(){ return concentrationModifier;}
   
   public NbtCompound getBookLore(){ return bookLore; }
   
   public MagicItemRecipe getRecipe(){ return recipe; }
   
   // Returns item stack with preferred attributes but without a unique UUID
   public ItemStack getPrefItem(){
      return prefItem;
   }
   
   // Returns item stack with preferred attributes and a unique UUID
   public ItemStack getNewItem(){
      ItemStack stack = prefItem.copy();
      NbtCompound tag = prefNBT.copy();
      tag.put("arcananovum",addUUID(tag.getCompound("arcananovum")));
      stack.setNbt(tag);
      return stack;
   }
   
   public NbtCompound getPrefNBT(){
      return prefNBT;
   }
   
   public ItemStack updateItem(ItemStack stack){
      NbtCompound itemNbt = stack.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      // For default just replace everything but UUID
      NbtCompound newTag = prefNBT.copy();
      newTag.getCompound("arcananovum").putString("UUID",magicTag.getString("UUID"));
      stack.setNbt(newTag);
   
      return stack;
   }
   
   protected NbtCompound addMagicNbt(NbtCompound compound){
      NbtCompound magic = new NbtCompound();
      magic.putString("id",id);
      magic.putInt("Rarity",MagicRarity.getRarityInt(rarity));
      magic.putInt("Version",version);
      magic.putString("UUID", "-");
      compound.put("arcananovum",magic);
      return compound;
   }
   
   public NbtCompound addUUID(NbtCompound compound){
      compound.putString("UUID", UUID.randomUUID().toString());
      return compound;
   }
   
   public String getUUID(ItemStack item){
      if(!MagicItemUtils.isMagic(item))
         return null;
      NbtCompound itemNbt = item.getNbt();
      NbtCompound magicTag = itemNbt.getCompound("arcananovum");
      return magicTag.getString("UUID");
   }
   
   public int compareTo(@NotNull MagicItem otherItem){
      int rarityCompare = (this.rarity.rarity - otherItem.rarity.rarity);
      if(rarityCompare == 0){
         return this.name.compareTo(otherItem.name);
      }else{
         return rarityCompare;
      }
   }
   
   protected void setBookLore(List<String> lines){
      bookLore = new NbtCompound();
      NbtList loreList = new NbtList();
      for(String e : lines){
         loreList.add(NbtString.of(e));
      }
      bookLore.put("pages",loreList);
      bookLore.putString("author","Arcana Novum");
      bookLore.putString("filtered_title",id);
      bookLore.putString("title",id);
   }
   
   protected void setRecipe(MagicItemRecipe recipe){
      this.recipe = recipe;
   }
}
