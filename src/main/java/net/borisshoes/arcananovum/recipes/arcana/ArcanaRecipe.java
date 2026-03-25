package net.borisshoes.arcananovum.recipes.arcana;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ArcanaRecipe {
   
   private final ArcanaIngredient[][] trueIngredients;
   private final ForgeRequirement forgeRequirement;
   private final List<Integer> centerpieces = new ArrayList<>();
   private final Identifier itemId;
   
   public ArcanaRecipe(Identifier item, ArcanaIngredient[][] ingredients){
      this(item, ingredients,new ForgeRequirement());
   }
   
   public ArcanaRecipe(ArcanaItem item, ArcanaIngredient[][] ingredients){
      this(item, ingredients,new ForgeRequirement());
   }
   
   public ArcanaRecipe(Item item, ArcanaIngredient[][] ingredients){
      this(BuiltInRegistries.ITEM.getKey(item), ingredients,new ForgeRequirement());
   }
   
   public ArcanaRecipe(ArcanaItem item, ArcanaIngredient[][] ingredients, ForgeRequirement forgeRequirement){
      this.itemId = ArcanaRegistry.arcanaId(item.getId());
      this.trueIngredients = ingredients;
      this.forgeRequirement = forgeRequirement;
   }
   
   public ArcanaRecipe(Identifier item, ArcanaIngredient[][] ingredients, ForgeRequirement forgeRequirement){
      this.itemId = item;
      this.trueIngredients = ingredients;
      this.forgeRequirement = forgeRequirement;
   }
   
   public ArcanaRecipe(Item item, ArcanaIngredient[][] ingredients, ForgeRequirement forgeRequirement){
      this.itemId = BuiltInRegistries.ITEM.getKey(item);
      this.trueIngredients = ingredients;
      this.forgeRequirement = forgeRequirement;
   }
   
   public boolean satisfiesRecipe(ItemStack[][] items, StarlightForgeBlockEntity forge){
      ArcanaIngredient[][] ingredients = getAlteredIngredients();
      if(ingredients.length != items.length || ingredients[0].length != items[0].length)
         return false;
      
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            if(!ingredients[i][j].validStack(items[i][j]))
               return false;
         }
      }
      
      return forgeRequirement.forgeMeetsRequirement(forge, false, null);
   }
   
   public ItemStack[][] getRemainders(ItemStack[][] items, StarlightForgeBlockEntity forge, int resourceLvl){
      if(!satisfiesRecipe(items,forge))
         return null;
      ItemStack[][] remainders = new ItemStack[items.length][items[0].length];
      ArcanaIngredient[][] ingredients = getAlteredIngredients();
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            remainders[i][j] = ingredients[i][j].getRemainder(items[i][j],resourceLvl);
         }
      }
      return remainders;
   }
   
   public ArcanaIngredient[][] getIngredients(){
      return getAlteredIngredients();
   }
   
   public Identifier getOutputId(){
      return itemId;
   }
   
   public ItemStack getDisplayStack(){
      if(!BuiltInRegistries.ITEM.containsKey(getOutputId())) return ItemStack.EMPTY.copy();
      Item item = BuiltInRegistries.ITEM.getValue(getOutputId());
      ItemStack showStack = new ItemStack(item);
      Optional<Holder.Reference<ArcanaItem>> arcanaItem = ArcanaRegistry.ARCANA_ITEMS.get(getOutputId());
      if(arcanaItem.isPresent()){
         showStack = arcanaItem.get().value().getPrefItem();
      }
      return showStack;
   }
   
   public ForgeRequirement getForgeRequirement(){
      return forgeRequirement;
   }
   
   public HashMap<String, Tuple<Integer, ItemStack>> getIngredientList(){
      HashMap<String, Tuple<Integer, ItemStack>> map = new HashMap<>();
      ArcanaIngredient[][] ingredients = getAlteredIngredients();
      for(int i = 0; i < ingredients.length; i++){
         for(int j = 0; j < ingredients[0].length; j++){
            ItemStack stack = ingredients[i][j].ingredientAsStack();
            if(!stack.isEmpty()){
               String ingred = ingredients[i][j].getName();
               Tuple<Integer, ItemStack> pair;
               if(map.containsKey(ingred)){
                  int oldCount = map.get(ingred).getA();
                  pair = new Tuple<>(ingredients[i][j].count+oldCount,stack);
               }else{
                  pair = new Tuple<>(ingredients[i][j].count,stack);
               }
               map.put(ingred,pair);
            }
         }
      }
      return map;
   }
   
   public ArrayList<ArcanaItem> getForgeRequirementList(){
      ArrayList<ArcanaItem> list = new ArrayList<>();
      if(forgeRequirement.needsFletchery()){
         list.add(ArcanaRegistry.RADIANT_FLETCHERY);
      }
      if(forgeRequirement.needsAnvil()){
         list.add(ArcanaRegistry.TWILIGHT_ANVIL);
      }
      if(forgeRequirement.needsCore()){
         list.add(ArcanaRegistry.STELLAR_CORE);
      }
      if(forgeRequirement.needsEnchanter()){
         list.add(ArcanaRegistry.MIDNIGHT_ENCHANTER);
      }
      if(forgeRequirement.needsSingularity()){
         list.add(ArcanaRegistry.ARCANE_SINGULARITY);
      }
      return list;
   }
   
   private ArcanaIngredient[][] getAlteredIngredients(){
      ArcanaIngredient[][] alteredIngredients = new ArcanaIngredient[trueIngredients.length][trueIngredients[0].length];
      for(int i = 0; i < trueIngredients.length; i++){
         for(int j = 0; j < trueIngredients[0].length; j++){
            int newCount = trueIngredients[i][j].getCount();
            alteredIngredients[i][j] = trueIngredients[i][j].copyWithCount(newCount);
         }
      }
      return alteredIngredients;
   }
   
   public ArcanaRecipe addCenterpiece(int index){
      this.centerpieces.add(index);
      return this;
   }
   
   public List<Integer> getCenterpieces(){
      return centerpieces;
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:forging_recipe");
      
      // Build the key map and pattern
      JsonObject keyJson = new JsonObject();
      JsonArray patternArray = new JsonArray();
      Map<ArcanaIngredient, String> ingredientToKey = new HashMap<>();
      char nextKey = 'A';
      
      for(int row = 0; row < 5; row++){
         StringBuilder patternRow = new StringBuilder();
         for(int col = 0; col < 5; col++){
            ArcanaIngredient ingredient = trueIngredients[row][col];
            
            // Check if this ingredient already has a key assigned
            String key = null;
            for(Map.Entry<ArcanaIngredient, String> entry : ingredientToKey.entrySet()){
               if(entry.getKey().equals(ingredient)){
                  key = entry.getValue();
                  break;
               }
            }
            
            if(key == null){
               // Assign a new key
               if(ingredient.equals(ArcanaIngredient.EMPTY)){
                  key = " ";
               }else{
                  key = String.valueOf(nextKey++);
                  keyJson.add(key, ingredient.toJson());
               }
               ingredientToKey.put(ingredient, key);
            }
            
            patternRow.append(key);
         }
         patternArray.add(patternRow.toString());
      }
      
      json.add("key", keyJson);
      json.add("pattern", patternArray);
      
      // Serialize requirements
      JsonArray requirementsArray = new JsonArray();
      if(forgeRequirement.needsEnchanter()){
         requirementsArray.add("arcananovum:midnight_enchanter");
      }
      if(forgeRequirement.needsAnvil()){
         requirementsArray.add("arcananovum:twilight_anvil");
      }
      if(forgeRequirement.needsCore()){
         requirementsArray.add("arcananovum:stellar_core");
      }
      if(forgeRequirement.needsFletchery()){
         requirementsArray.add("arcananovum:radiant_fletchery");
      }
      if(forgeRequirement.needsSingularity()){
         requirementsArray.add("arcananovum:arcane_singularity");
      }
      json.add("requirements", requirementsArray);
      
      // Serialize centerpieces
      JsonArray centerpiecesArray = new JsonArray();
      for(Integer index : centerpieces){
         centerpiecesArray.add(index);
      }
      json.add("centerpieces", centerpiecesArray);
      
      // Serialize result
      json.addProperty("result", itemId.toString());
      
      return json;
   }
   
   public static ArcanaRecipe fromJson(JsonObject json){
      if(!json.get("type").getAsString().equals("arcananovum:forging_recipe")) return null;
      
      // Parse the key map
      JsonObject keyJson = json.getAsJsonObject("key");
      Map<String, ArcanaIngredient> keyToIngredient = new HashMap<>();
      keyToIngredient.put(" ", ArcanaIngredient.EMPTY);
      
      for(Map.Entry<String, JsonElement> entry : keyJson.entrySet()){
         JsonObject ingredientJson = entry.getValue().getAsJsonObject();
         String type = ingredientJson.get("type").getAsString();
         ArcanaIngredient ingredient = switch(type){
            case "arcananovum:generic_ingredient" -> ArcanaIngredient.fromJson(ingredientJson);
            case "arcananovum:arcane_ingredient" -> GenericArcanaIngredient.fromJson(ingredientJson);
            case "arcananovum:waystone_ingredient" -> WaystoneIngredient.fromJson(ingredientJson);
            case "arcananovum:soulstone_ingredient" -> SoulstoneIngredient.fromJson(ingredientJson);
            case "arcananovum:shulker_core_ingredient" -> ShulkerCoreIngredient.fromJson(ingredientJson);
            default -> null;
         };
         if(ingredient != null){
            keyToIngredient.put(entry.getKey(), ingredient);
         }
      }
      
      // Parse the pattern
      JsonArray patternArray = json.getAsJsonArray("pattern");
      ArcanaIngredient[][] ingredients = new ArcanaIngredient[5][5];
      for(int row = 0; row < 5; row++){
         String patternRow = patternArray.get(row).getAsString();
         for(int col = 0; col < 5; col++){
            String key = String.valueOf(patternRow.charAt(col));
            ingredients[row][col] = keyToIngredient.getOrDefault(key, ArcanaIngredient.EMPTY);
         }
      }
      
      // Parse requirements
      ForgeRequirement forgeRequirement = new ForgeRequirement();
      JsonArray requirementsArray = json.getAsJsonArray("requirements");
      for(JsonElement element : requirementsArray){
         String req = element.getAsString();
         switch(req){
            case "arcananovum:midnight_enchanter" -> forgeRequirement.withEnchanter();
            case "arcananovum:twilight_anvil" -> forgeRequirement.withAnvil();
            case "arcananovum:stellar_core" -> forgeRequirement.withCore();
            case "arcananovum:radiant_fletchery" -> forgeRequirement.withFletchery();
            case "arcananovum:arcane_singularity" -> forgeRequirement.withSingularity();
         }
      }
      
      // Parse result
      String resultId = json.get("result").getAsString();
      Identifier resultIdentifier = Identifier.parse(resultId);
      ArcanaItem resultItem = ArcanaRegistry.getArcanaItem(resultIdentifier.getPath());
      
      if(resultItem == null){
         throw new IllegalArgumentException("Unknown Arcana item: " + resultId);
      }
      
      ArcanaRecipe recipe = new ArcanaRecipe(resultItem, ingredients, forgeRequirement);
      
      // Parse centerpieces
      if(json.has("centerpieces")){
         JsonArray centerpiecesArray = json.getAsJsonArray("centerpieces");
         for(JsonElement element : centerpiecesArray){
            recipe.addCenterpiece(element.getAsInt());
         }
      }
      
      return recipe;
   }
}
