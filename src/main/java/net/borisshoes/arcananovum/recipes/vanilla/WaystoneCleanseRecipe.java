package net.borisshoes.arcananovum.recipes.vanilla;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class WaystoneCleanseRecipe extends CampfireCookingRecipe {
   public WaystoneCleanseRecipe(String string, CookingBookCategory cookingRecipeCategory, Ingredient ingredient, ItemStack itemStack, float f, int i){
      super(string, cookingRecipeCategory, ingredient, itemStack, f, i);
   }
   
   @Override
   public ItemStack assemble(SingleRecipeInput singleStackRecipeInput, HolderLookup.Provider wrapperLookup){
      ItemStack input = singleStackRecipeInput.item().copy();
      if(input.is(ArcanaRegistry.WAYSTONE.getItem()) && ArcanaItemUtils.identifyItem(input) instanceof Waystone waystone){
         Waystone.setUnattuned(input);
         waystone.buildItemLore(input, BorisLib.SERVER);
      }
      return input;
   }
   
   public static class WaystoneCleanseRecipeSerializer<T extends AbstractCookingRecipe> extends AbstractCookingRecipe.Serializer<T> implements PolymerObject {
      public WaystoneCleanseRecipeSerializer(net.minecraft.world.item.crafting.AbstractCookingRecipe.Factory<T> factory){
         super(factory, 1200);
      }
   }
}
