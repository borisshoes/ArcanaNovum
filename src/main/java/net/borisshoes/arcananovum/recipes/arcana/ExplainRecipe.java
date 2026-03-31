package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class ExplainRecipe extends ArcanaRecipe {
   
   private final ExplainIngredient[][] explainIngredients;
   
   public ExplainRecipe(ArcanaItem item, ExplainIngredient[][] ingredients){
      super(item, ingredients);
      this.explainIngredients = ingredients;
   }
   
   public ExplainRecipe(Identifier item, ExplainIngredient[][] ingredients){
      super(item, ingredients);
      this.explainIngredients = ingredients;
   }
   
   public ExplainRecipe(Item item, ExplainIngredient[][] ingredients){
      super(item, ingredients);
      this.explainIngredients = ingredients;
   }
   
   @Override
   public boolean satisfiesRecipe(ItemStack[][] items, StarlightForgeBlockEntity forge){
      return false;
   }
   
   @Override
   public HashMap<String, Tuple<Integer, ItemStack>> getIngredientList(){
      HashMap<String, Tuple<Integer, ItemStack>> map = new HashMap<>();
      for(int i = 0; i < explainIngredients.length; i++){
         for(int j = 0; j < explainIngredients[0].length; j++){
            ItemStack stack = explainIngredients[i][j].ingredientAsStack();
            if(!stack.isEmpty() && explainIngredients[i][j].show){
               String ingred = explainIngredients[i][j].getName();
               Tuple<Integer, ItemStack> pair;
               if(map.containsKey(ingred)){
                  int oldCount = map.get(ingred).getA();
                  pair = new Tuple<>(explainIngredients[i][j].count + oldCount, stack);
               }else{
                  pair = new Tuple<>(explainIngredients[i][j].count, stack);
               }
               map.put(ingred, pair);
            }
         }
      }
      return map;
   }
   
   @Override
   public ArcanaIngredient[][] getIngredients(){
      return explainIngredients;
   }
}
