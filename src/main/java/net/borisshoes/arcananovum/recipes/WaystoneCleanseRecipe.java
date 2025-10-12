package net.borisshoes.arcananovum.recipes;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.RegistryWrapper;

public class WaystoneCleanseRecipe extends CampfireCookingRecipe {
   public WaystoneCleanseRecipe(String string, CookingRecipeCategory cookingRecipeCategory, Ingredient ingredient, ItemStack itemStack, float f, int i){
      super(string, cookingRecipeCategory, ingredient, itemStack, f, i);
   }
   
   @Override
   public ItemStack craft(SingleStackRecipeInput singleStackRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup){
      ItemStack input = singleStackRecipeInput.item().copy();
      if(input.isOf(ArcanaRegistry.WAYSTONE.getItem()) && ArcanaItemUtils.identifyItem(input) instanceof Waystone waystone){
         Waystone.setUnattuned(input);
         waystone.buildItemLore(input, BorisLib.SERVER);
      }
      return input;
   }
   
   public static class WaystoneCleanseRecipeSerializer <T extends AbstractCookingRecipe> extends AbstractCookingRecipe.Serializer<T> implements PolymerObject {
      public WaystoneCleanseRecipeSerializer(RecipeFactory<T> factory){
         super(factory, 6000);
      }
   }
}
