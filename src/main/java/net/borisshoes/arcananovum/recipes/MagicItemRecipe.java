package net.borisshoes.arcananovum.recipes;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagicItemRecipe {
   
   private final MagicItemIngredient[][] ingredients;
   
   public MagicItemRecipe(MagicItemIngredient[][] ingredients){
      this.ingredients = ingredients;
   }
   
   public boolean satisfiesRecipe(ItemStack[][] items){
      if(ingredients.length != items.length || ingredients[0].length != items[0].length)
         return false;
      
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            if(!ingredients[i][j].validStack(items[i][j]))
               return false;
         }
      }
      
      return true;
   }
   
   public ItemStack[][] getRemainders(ItemStack[][] items, int resourceLvl){
      if(!satisfiesRecipe(items))
         return null;
      ItemStack[][] remainders = new ItemStack[items.length][items[0].length];
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            remainders[i][j] = ingredients[i][j].getRemainder(items[i][j],resourceLvl);
         }
      }
      return remainders;
   }
   
   public MagicItemIngredient[][] getIngredients(){
      return ingredients;
   }
   
   public HashMap<String, Pair<Integer,ItemStack>> getIngredientList(){
      HashMap<String, Pair<Integer,ItemStack>> map = new HashMap<>();
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            ItemStack stack = ingredients[i][j].ingredientAsStack();
            if(!stack.isEmpty()){
               String ingred = ingredients[i][j].getName();
               Pair<Integer,ItemStack> pair;
               if(map.containsKey(ingred)){
                  int oldCount = map.get(ingred).getLeft();
                  pair = new Pair<>(ingredients[i][j].count+oldCount,stack);
               }else{
                  pair = new Pair<>(ingredients[i][j].count,stack);
               }
               map.put(ingred,pair);
            }
         }
      }
      return map;
   }
}
