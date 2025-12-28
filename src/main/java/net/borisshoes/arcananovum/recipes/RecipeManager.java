package net.borisshoes.arcananovum.recipes;

import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipes;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class RecipeManager {
   
   public static Registry<ArcanaRecipe> ARCANA_RECIPES = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"arcana_recipes")), Lifecycle.stable());
   public static Registry<TransmutationRecipes> TRANSMUTATION_RECIPES = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"transmutation_recipes")), Lifecycle.stable());
   
   public static void refreshRecipes(MinecraftServer server){
      ARCANA_RECIPES = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"arcana_recipes")), Lifecycle.stable());
      TRANSMUTATION_RECIPES = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"transmutation_recipes")), Lifecycle.stable());
      
      
   }
}
