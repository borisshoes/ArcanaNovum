package net.borisshoes.arcananovum.recipes;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Map;

import static java.util.Map.entry;

public class AquaticEversourceFillRecipe extends SpecialCraftingRecipe {
   
   private static final Map<Item, ItemStack> FILLABLE = Map.ofEntries(
         entry(Items.DIRT, Items.MUD.getDefaultStack()),
         entry(Items.GLASS_BOTTLE, PotionContentsComponent.createStack(Items.POTION, Potions.WATER)),
         entry(Items.BUCKET, Items.WATER_BUCKET.getDefaultStack())
   );
   
   public AquaticEversourceFillRecipe(CraftingRecipeCategory craftingRecipeCategory){
      super(CraftingRecipeCategory.MISC);
   }
   
   @Override
   public boolean matches(CraftingRecipeInput input, World world){
      boolean hasEversource = false;
      boolean hasFillable = false;
      
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getStackInSlot(i);
         if(rStack.isEmpty()) continue;
         
         if(!hasEversource && rStack.getItem() instanceof AquaticEversource.AquaticEversourceItem){
            hasEversource = true;
            continue;
         }else if(!hasFillable && FILLABLE.containsKey(rStack.getItem())){
            hasFillable = true;
            continue;
         }
         return false;
      }
      
      return hasEversource && hasFillable;
   }
   
   @Override
   public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup registries){
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getStackInSlot(i);
         if(FILLABLE.containsKey(rStack.getItem())){
            return FILLABLE.get(rStack.getItem()).copyWithCount(1);
         }
      }
      return ItemStack.EMPTY;
   }
   
   
   @Override
   public DefaultedList<ItemStack> getRecipeRemainders(CraftingRecipeInput input){
      DefaultedList<ItemStack> stacks = DefaultedList.ofSize(input.size(),ItemStack.EMPTY);
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getStackInSlot(i);
         if(rStack.isEmpty()) continue;
         
         if(rStack.getItem() instanceof AquaticEversource.AquaticEversourceItem){
            stacks.set(i, rStack.copy());
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer(){
      return ArcanaRegistry.AQUATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   }
   
   public static class AquaticEversourceRecipeSerializer extends SpecialRecipeSerializer implements PolymerObject {
      public AquaticEversourceRecipeSerializer(Factory factory){
         super(factory);
      }
   }
}
