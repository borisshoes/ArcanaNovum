package net.borisshoes.arcananovum.datagen;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.recipes.vanilla.AquaticEversourceFillRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.ArcanaShieldDecoratorRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.MagmaticEversourceFillRecipe;
import net.borisshoes.arcananovum.recipes.vanilla.WaystoneCleanseRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RecipesProvider extends FabricRecipeProvider {
   
   public RecipesProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
      super(output, registriesFuture);
   }
   
   @Override
   protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter){
      return new RecipeProvider(registryLookup, exporter) {
         @Override
         public void buildRecipes(){
            SpecialRecipeBuilder.special(() -> new ArcanaShieldDecoratorRecipe(this.tag(ItemTags.BANNERS), Ingredient.of(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem()), new ItemStackTemplate(ArcanaRegistry.SHIELD_OF_FORTITUDE.getItem())))
                  .save(this.output, recipeKey("arcana_shield_decoration"));
            
            for(Map.Entry<Item, ItemStackTemplate> entry : AquaticEversourceFillRecipe.FILLABLE.entrySet()){
               SpecialRecipeBuilder.special(() -> new AquaticEversourceFillRecipe(Ingredient.of(entry.getKey()), Ingredient.of(ArcanaRegistry.AQUATIC_EVERSOURCE.getItem()), entry.getValue()))
                     .save(this.output, recipeKey("aquatic_eversource_fill_"+getItemName(entry.getKey())));
            }
            
            for(Map.Entry<Item, ItemStackTemplate> entry : MagmaticEversourceFillRecipe.FILLABLE.entrySet()){
               SpecialRecipeBuilder.special(() -> new MagmaticEversourceFillRecipe(Ingredient.of(entry.getKey()), Ingredient.of(ArcanaRegistry.MAGMATIC_EVERSOURCE.getItem()), entry.getValue()))
                     .save(this.output, recipeKey("magmatic_eversource_fill_"+getItemName(entry.getKey())));
            }
            
            SimpleCookingRecipeBuilder.generic(Ingredient.of(ArcanaRegistry.WAYSTONE.getItem()), RecipeCategory.MISC, CookingBookCategory.MISC, ArcanaRegistry.WAYSTONE.getItem(), 10f, 1200, WaystoneCleanseRecipe::new)
                  .unlockedBy(getHasName(ArcanaRegistry.WAYSTONE.getItem()), this.has(ArcanaRegistry.WAYSTONE.getItem()))
                  .save(this.output, recipeKey("waystone_cleanse"));
         }
         
         private ResourceKey<Recipe<?>> recipeKey(String name){
            return ResourceKey.create(Registries.RECIPE, ArcanaRegistry.arcanaId(name));
         }
      };
   }
   
   @Override
   public String getName(){
      return "Arcana Novum - Recipe Generator";
   }
}
