package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

public class MagicItemIngredient {
   protected final Item itemType;
   protected final int count;
   protected final NbtCompound requiredNbt;
   protected final boolean ignoresResourceful;
   
   public static MagicItemIngredient EMPTY = new MagicItemIngredient(Items.AIR,1,null);
   
   public MagicItemIngredient(Item itemType, int count, @Nullable NbtCompound requiredNbt, boolean ignoresResourceful){
      this.count = count;
      this.itemType = itemType;
      this.requiredNbt = requiredNbt;
      this.ignoresResourceful = ignoresResourceful;
   }
   
   public MagicItemIngredient(Item itemType, int count, @Nullable NbtCompound requiredNbt){
      this.count = count;
      this.itemType = itemType;
      this.requiredNbt = requiredNbt;
      this.ignoresResourceful = false;
   }
   
   public MagicItemIngredient copyWithCount(int newCount){
      return new MagicItemIngredient(itemType,newCount,requiredNbt,ignoresResourceful);
   }
   
   public int getCount(){
      return count;
   }
   
   public Item getItemType(){
      return itemType;
   }
   
   public NbtCompound getRequiredNbt(){
      return requiredNbt;
   }
   
   public boolean validStack(ItemStack stack){
      if(itemType == Items.AIR || MagicItemUtils.isMagic(stack)){
         return stack.isEmpty();
      }else if(requiredNbt == null){
         return (stack.isOf(itemType) && stack.getCount() >= count);
      }else{
         if(!stack.hasNbt()){
            return false;
         }else{
            return (stack.isOf(itemType) && stack.getCount() >= count && validNbt(stack.getNbt(),requiredNbt));
         }
      }
   }
   
   public ItemStack getRemainder(ItemStack stack, int resourceLvl){
      int saved = 0;
      if(!ignoresResourceful){
         for(int i = 0; i < count; i++){
            if(Math.random() < 0.05 * resourceLvl) saved++;
         }
      }
      int newCount = MagicItemUtils.isMagic(stack) ? count : count - saved;
      
      if(stack.getCount() <= newCount){
         return ItemStack.EMPTY;
      }else{
         ItemStack stackCopy = stack.copy();
         stackCopy.decrement(newCount);
         return stackCopy;
      }
   }
   
   public ItemStack ingredientAsStack(){
      ItemStack stack = itemType.getDefaultStack().copyWithCount(count);
      if(requiredNbt != null)
         stack.setNbt(requiredNbt);
      return stack;
   }
   
   public String getName(){
      return ingredientAsStack().getName().getString();
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof MagicItemIngredient o)) return false;
      if(requiredNbt != null && o.getRequiredNbt() == null) return false;
      if(requiredNbt == null && o.getRequiredNbt() != null) return false;
      if(requiredNbt == null && o.getRequiredNbt() == null) return o.getCount() == count && o.getItemType().equals(itemType);
      return (o.getCount() == count && o.getItemType().equals(itemType) && o.getRequiredNbt().equals(requiredNbt));
   }
   
   public static NbtCompound getEnchantNbt(Pair<Enchantment,Integer>...enchants){
      NbtCompound nbt = new NbtCompound();
      NbtList enchantList = new NbtList();
      for(Pair<Enchantment,Integer> enchant : enchants){
         enchantList.add(EnchantmentHelper.createNbt(Registries.ENCHANTMENT.getId(enchant.getLeft()),enchant.getRight()));
      }
      nbt.put("Enchantments",enchantList);
      return nbt;
   }
   
   public static boolean validNbt(NbtCompound nbt, NbtCompound required){
      try{
         for(String key : required.getKeys()){
            //log("Looking for key "+key);
            if(nbt.contains(key)){
               if(nbt.getType(key) == required.getType(key)){
                  byte type = nbt.getType(key);
                  if((type >= 1 && type <=6) || type == 8){ // Direct Comparison
                     switch(type){
                        case 1:
                           if(nbt.getByte(key) != required.getByte(key))
                              return false;
                           break;
                        case 2:
                           //log("Comparing Shorts: "+key+" "+nbt.getShort(key)+" "+required.getShort(key));
                           if(nbt.getShort(key) != required.getShort(key)){
                              //log("Returning false on shorts");
                              return false;
                           }
                           
                           break;
                        case 3:
                           if(nbt.getInt(key) != required.getInt(key))
                              return false;
                           break;
                        case 4:
                           if(nbt.getLong(key) != required.getLong(key))
                              return false;
                           break;
                        case 5:
                           if(nbt.getFloat(key) != required.getFloat(key))
                              return false;
                           break;
                        case 6:
                           if(nbt.getDouble(key) != required.getDouble(key))
                              return false;
                           break;
                        case 8:
                           //log("Comparing Strings: "+key+" "+nbt.getString(key)+" "+required.getString(key));
                           if(!nbt.getString(key).equals(required.getString(key))){
                              //log("Returning false on Strings");
                              return false;
                           }
                           break;
                     }
                  }else if(type == 10){
                     // Recursive call on compound
                     if(!validNbt(nbt.getCompound(key),required.getCompound(key)))
                        return false;
                  }else if(type == 7 || type == 11 || type == 12){ // Array comparison
                     // Ignore for now since i've never seen an array used in minecraft nbt
                  }else if(type == 9){ // List comparison
                     NbtList list1 = (NbtList) nbt.get(key);
                     NbtList list2 = (NbtList) required.get(key);
                     //log("Calling List on "+key);
                     if(!validList(list1,list2)){
                        //log("Valid List False");
                        return false;
                     }
                     
                  }
               }
            }else{
               return false;
            }
         }
         return true;
      }catch(Exception e){
         e.printStackTrace();
         return false;
      }
   }
   
   public static boolean validList(NbtList list, NbtList required){
      if(required.size() == 0)
         return true;
      if(required.size() > list.size())
         return false;
      byte type = required.get(0).getType();
      if(type != list.get(0).getType())
         return false;
      
      for(int i = 0; i < required.size(); i++){
         NbtElement reqElem = required.get(i);
         boolean found = false;
         int searched = 0;
         for(int j = 0; j < list.size(); j++){ // Scan list for each element in required
            searched++;
            NbtElement elem = list.get(j);
   
            if(type == 9){
               if(validList(list.getList(j),required.getList(i))){ // Recursive list call
                  found = true;
                  break;
               }
            }else if(type == 10){
               //log("Recursive Call on compound from list");
               if(validNbt(list.getCompound(j),required.getCompound(i))){// Recursive compound call
                  found = true;
                  break;
               }
            }else{
               if(elem.asString().equals(reqElem.asString())) {// Basic element check, order matters
                  //log("Found basic element");
                  found = true;
                  break;
               }
            }
         }
         
         if(!found){
            //log("searched "+searched+" without finding");
            return false;
         }
      }
      return true;
   }
}
