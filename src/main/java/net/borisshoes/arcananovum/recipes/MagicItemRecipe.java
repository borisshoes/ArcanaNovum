package net.borisshoes.arcananovum.recipes;

import net.minecraft.item.ItemStack;

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
   
   public ItemStack[][] getRemainders(ItemStack[][] items){
      if(!satisfiesRecipe(items))
         return null;
      ItemStack[][] remainders = new ItemStack[items.length][items[0].length];
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            remainders[i][j] = ingredients[i][j].getRemainder(items[i][j]);
         }
      }
      return remainders;
   }
   
   public MagicItemIngredient[][] getIngredients(){
      return ingredients;
   }
}
