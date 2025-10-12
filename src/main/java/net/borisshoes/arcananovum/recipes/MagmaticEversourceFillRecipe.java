package net.borisshoes.arcananovum.recipes;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.Map;

import static java.util.Map.entry;

public class MagmaticEversourceFillRecipe extends SpecialCraftingRecipe {
   
   private static final Map<Item, ItemStack> FILLABLE = Map.ofEntries(
         entry(Items.BUCKET, Items.LAVA_BUCKET.getDefaultStack())
   );
   
   public MagmaticEversourceFillRecipe(CraftingRecipeCategory craftingRecipeCategory){
      super(CraftingRecipeCategory.MISC);
   }
   
   @Override
   public boolean matches(CraftingRecipeInput input, World world){
      boolean hasEversource = false;
      boolean hasFillable = false;
      int eversourceSlot = -1;
      
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getStackInSlot(i);
         if(rStack.isEmpty()) continue;
         
         if(!hasEversource && rStack.getItem() instanceof MagmaticEversource.MagmaticEversourceItem){
            hasEversource = true;
            eversourceSlot = i;
            continue;
         }else if(!hasFillable && FILLABLE.containsKey(rStack.getItem())){
            hasFillable = true;
            continue;
         }
         return false;
      }
      if(!hasEversource || !hasFillable) return false;
      
      ItemStack eversource = input.getStackInSlot(eversourceSlot);
      int charges = ArcanaItem.getIntProperty(eversource,MagmaticEversource.USES_TAG);
      return charges >= 1;
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
         
         if(rStack.getItem() instanceof MagmaticEversource.MagmaticEversourceItem){
            ItemStack source = rStack.copy();
            ArcanaItem.putProperty(source,MagmaticEversource.USES_TAG,ArcanaItem.getIntProperty(source,MagmaticEversource.USES_TAG)-1);
            stacks.set(i, source);
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends SpecialCraftingRecipe> getSerializer(){
      return ArcanaRegistry.MAGMATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   }
   
   public static class MagmaticEversourceRecipeSerializer extends SpecialRecipeSerializer implements PolymerObject {
      public MagmaticEversourceRecipeSerializer(Factory factory){
         super(factory);
      }
   }
}
