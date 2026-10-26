package net.borisshoes.arcananovum.test.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.recipes.RecipeManager;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.CommutativeTransmutationRecipe;
import net.borisshoes.arcananovum.recipes.transmutation.TransmutationRecipe;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Recipe checks that need the loaded registries and tags. Vanilla moving items between tags is the usual way these break
 * after an update: a recipe quietly matches nothing, or two recipes start covering the same items.
 */
public class RecipeIntegrityGameTests {
   
   private static final int[] COST_MODES = {-1, 0, 1, 2, 3, 4, 5};
   
   @GameTest
   public void commutativeRecipesDoNotOverlap(GameTestHelper helper){
      Map<Item, List<String>> owners = new LinkedHashMap<>();
      for(CommutativeTransmutationRecipe recipe : commutativeRecipes()){
         for(Item item : BuiltInRegistries.ITEM){
            if(recipe.validCommunalInput(item.getDefaultInstance())) owners.computeIfAbsent(item, k -> new ArrayList<>()).add(recipe.getId());
         }
      }
      
      Map<String, List<String>> overlaps = new TreeMap<>(); // recipe pair to the items both of them accept
      for(Map.Entry<Item, List<String>> entry : owners.entrySet()){
         if(entry.getValue().size() > 1){
            overlaps.computeIfAbsent(String.join(" + ", entry.getValue()), k -> new ArrayList<>()).add(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString());
         }
      }
      List<String> lines = new ArrayList<>();
      overlaps.forEach((pair, items) -> lines.add(pair + ": " + items));
      helper.assertTrue(overlaps.isEmpty(), "Items accepted by more than one commutative transmutation:\n" + String.join("\n", lines));
      helper.succeed();
   }
   
   @GameTest
   public void commutativeRecipesStillMatchItems(GameTestHelper helper){
      List<String> problems = new ArrayList<>();
      for(CommutativeTransmutationRecipe recipe : commutativeRecipes()){
         List<Either<Item, TagKey<Item>>> entries = recipe.getCommunalInputs();
         entries.addAll(recipe.getExcludedInputs());
         for(Either<Item, TagKey<Item>> input : entries){
            if(input.right().isPresent()){
               TagKey<Item> tag = input.right().get();
               if(BuiltInRegistries.ITEM.get(tag).map(set -> set.size() == 0).orElse(true)){
                  problems.add(recipe.getId() + " uses tag #" + tag.location() + " which is missing or empty");
               }
            }
         }
         long count = BuiltInRegistries.ITEM.stream().filter(item -> recipe.validCommunalInput(item.getDefaultInstance())).count();
         if(count < 2) problems.add(recipe.getId() + " only matches " + count + " item(s)");
      }
      helper.assertTrue(problems.isEmpty(), String.join("\n", problems));
      helper.succeed();
   }
   
   @GameTest
   public void recipeStacksAreValid(GameTestHelper helper){
      List<String> problems = new ArrayList<>();
      for(ArcanaRecipe recipe : RecipeManager.ARCANA_RECIPES){
         ArcanaIngredient[][] ingredients = recipe.getIngredients();
         for(int i = 0; i < ingredients.length; i++){
            for(int j = 0; j < ingredients[i].length; j++){
               check(problems, "Recipe for " + recipe.getOutputId() + " slot " + i + "," + j, ingredients[i][j].ingredientAsStack());
            }
         }
      }
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         check(problems, "Transmutation " + recipe.getId() + " view stack", recipe.getViewStack());
         for(int mode : COST_MODES){
            ItemStack example1 = recipe.getExampleReagent1();
            ItemStack example2 = recipe.getExampleReagent2();
            if(example1 != null) check(problems, "Transmutation " + recipe.getId() + " reagent 1 at bargain " + mode, recipe.getComputedReagent1(example1, mode));
            if(example2 != null) check(problems, "Transmutation " + recipe.getId() + " reagent 2 at bargain " + mode, recipe.getComputedReagent2(example2, mode));
         }
      }
      helper.assertTrue(problems.isEmpty(), "Invalid recipe display stacks:\n" + String.join("\n", problems));
      helper.succeed();
   }
   
   @GameTest
   public void transmutationRecipesHaveNames(GameTestHelper helper){
      Set<String> keys = langKeys();
      List<String> missing = new ArrayList<>();
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         String key = "transmutation.arcananovum." + recipe.getId();
         if(!keys.contains(key)) missing.add(key);
      }
      helper.assertTrue(missing.isEmpty(), "Transmutation recipes without a name in en_us.json: " + missing);
      helper.succeed();
   }
   
   @GameTest
   public void reagentCostsFollowHastyBargain(GameTestHelper helper){
      // Index 0 is no augment, 1-5 are Hasty Bargain levels, -1 is the GUI's half cost mode.
      // Level 1 doubles the cost, each further level takes off 20%, and a cost never goes past one stack.
      TestRecipe recipe = new TestRecipe(new ItemStack(Items.EMERALD, 10), new ItemStack(Items.ENDER_PEARL, 12));
      int[] expectedEmeralds = {5, 10, 20, 18, 16, 14, 12};
      int[] expectedPearls = {6, 12, 16, 16, 16, 16, 15};
      for(int i = 0; i < COST_MODES.length; i++){
         int mode = COST_MODES[i];
         ItemStack emeralds = recipe.getComputedReagent1(new ItemStack(Items.EMERALD, 64), mode);
         ItemStack pearls = recipe.getComputedReagent2(new ItemStack(Items.ENDER_PEARL, 16), mode);
         helper.assertValueEqual(emeralds.getCount(), expectedEmeralds[i], "Emerald cost at bargain " + mode);
         helper.assertValueEqual(pearls.getCount(), expectedPearls[i], "Ender Pearl cost at bargain " + mode);
         helper.assertTrue(recipe.validReagent2(new ItemStack(Items.ENDER_PEARL, 16), mode), "A full stack of pearls should pay any cost (bargain " + mode + ")");
      }
      helper.assertTrue(recipe.getComputedReagent1(new ItemStack(Items.DIAMOND, 64), 0) == null, "Diamonds were accepted as an emerald reagent");
      helper.assertFalse(recipe.validReagent1(new ItemStack(Items.ENDER_PEARL, 16), 0), "Pearls were accepted in the emerald slot");
      helper.assertFalse(recipe.validReagent1(ItemStack.EMPTY, 0), "An empty slot was accepted as a reagent");
      helper.succeed();
   }
   
   private static class TestRecipe extends TransmutationRecipe {
      TestRecipe(ItemStack reagent1, ItemStack reagent2){
         super("test", reagent1, reagent2);
      }
      
      @Override
      public List<ItemStack> doTransmutation(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, ServerPlayer player){
         return List.of();
      }
      
      @Override
      public List<Pair<ItemStack, String>> doTransmutation(ItemEntity positiveInput, ItemEntity negativeInput, ItemEntity reagent1, ItemEntity reagent2, ItemEntity aequalisInput, TransmutationAltarBlockEntity altar, ServerPlayer player){
         return List.of();
      }
      
      @Override
      public boolean canTransmute(ItemStack positiveInput, ItemStack negativeInput, ItemStack reagent1, ItemStack reagent2, ItemStack aequalisInput, TransmutationAltarBlockEntity altar){
         return false;
      }
      
      @Override
      public ItemStack getViewStack(){
         return ItemStack.EMPTY;
      }
   }
   
   private static List<CommutativeTransmutationRecipe> commutativeRecipes(){
      List<CommutativeTransmutationRecipe> list = new ArrayList<>();
      for(TransmutationRecipe recipe : RecipeManager.TRANSMUTATION_RECIPES){
         if(recipe instanceof CommutativeTransmutationRecipe commutative) list.add(commutative);
      }
      return list;
   }
   
   private static void check(List<String> problems, String label, ItemStack stack){
      if(stack == null){
         problems.add(label + ": null");
         return;
      }
      ItemStack.validateStrict(stack).error().ifPresent(error -> problems.add(label + ": " + error.message()));
   }
   
   static Set<String> langKeys(){
      try(InputStream in = RecipeIntegrityGameTests.class.getResourceAsStream("/data/arcananovum/lang/en_us.json")){
         if(in == null) return Set.of();
         JsonObject json = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
         return json.keySet();
      }catch(IOException e){
         throw new RuntimeException(e);
      }
   }
}
