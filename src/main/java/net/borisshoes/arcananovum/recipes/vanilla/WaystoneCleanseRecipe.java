package net.borisshoes.arcananovum.recipes.vanilla;

import com.mojang.serialization.MapCodec;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.Waystone;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class WaystoneCleanseRecipe extends CampfireCookingRecipe {
   public static final MapCodec<WaystoneCleanseRecipe> MAP_CODEC = cookingMapCodec(WaystoneCleanseRecipe::new, 1200);
   public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneCleanseRecipe> STREAM_CODEC = cookingStreamCodec(WaystoneCleanseRecipe::new);
   public static final RecipeSerializer<WaystoneCleanseRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);
   
   public WaystoneCleanseRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate itemStack, float f, int i){
      super(commonInfo, bookInfo, ingredient, itemStack, f, i);
   }
   
   @Override
   public ItemStack assemble(SingleRecipeInput singleStackRecipeInput){
      ItemStack input = singleStackRecipeInput.item().copy();
      if(input.is(ArcanaRegistry.WAYSTONE.getItem()) && ArcanaItemUtils.identifyItem(input) instanceof Waystone waystone){
         Waystone.setUnattuned(input);
         waystone.buildItemLore(input, BorisLib.SERVER);
      }
      return input;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public RecipeSerializer<CampfireCookingRecipe> getSerializer(){
      return (RecipeSerializer<CampfireCookingRecipe>) (RecipeSerializer<?>) SERIALIZER;
   }
}
