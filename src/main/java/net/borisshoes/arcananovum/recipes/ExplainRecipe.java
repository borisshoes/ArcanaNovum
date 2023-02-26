package net.borisshoes.arcananovum.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;

import java.util.HashMap;

public class ExplainRecipe extends MagicItemRecipe{
   
   private final ExplainIngredient[][] explainIngredients;
   
   public ExplainRecipe(ExplainIngredient[][] ingredients){
      super(ingredients);
      this.explainIngredients = ingredients;
   }
   
   @Override
   public boolean satisfiesRecipe(ItemStack[][] items){
      return false;
   }
   
   @Override
   public HashMap<String, Pair<Integer,ItemStack>> getIngredientList(){
      HashMap<String, Pair<Integer,ItemStack>> map = new HashMap<>();
      for(int i = 0; i < explainIngredients.length; i++){
         for(int j = 0; j < explainIngredients[0].length; j++){
            ItemStack stack = explainIngredients[i][j].ingredientAsStack();
            if(!stack.isEmpty() && explainIngredients[i][j].show){
               String ingred = explainIngredients[i][j].getName();
               Pair<Integer,ItemStack> pair;
               if(map.containsKey(ingred)){
                  int oldCount = map.get(ingred).getLeft();
                  pair = new Pair<>(explainIngredients[i][j].count+oldCount,stack);
               }else{
                  pair = new Pair<>(explainIngredients[i][j].count,stack);
               }
               map.put(ingred,pair);
            }
         }
      }
      return map;
   }
}
