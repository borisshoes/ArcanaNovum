package net.borisshoes.arcananovum.recipes.vanilla;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.MagmaticEversource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Map;

import static java.util.Map.entry;

public class MagmaticEversourceFillRecipe extends CustomRecipe {
   
   private static final Map<Item, ItemStack> FILLABLE = Map.ofEntries(
         entry(Items.BUCKET, Items.LAVA_BUCKET.getDefaultInstance())
   );
   
   public MagmaticEversourceFillRecipe(CraftingBookCategory craftingRecipeCategory){
      super(CraftingBookCategory.MISC);
   }
   
   @Override
   public boolean matches(CraftingInput input, Level world){
      boolean hasEversource = false;
      boolean hasFillable = false;
      int eversourceSlot = -1;
      
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
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
      
      ItemStack eversource = input.getItem(eversourceSlot);
      int charges = ArcanaItem.getIntProperty(eversource, MagmaticEversource.USES_TAG);
      return charges >= 1;
   }
   
   @Override
   public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries){
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(FILLABLE.containsKey(rStack.getItem())){
            return FILLABLE.get(rStack.getItem()).copyWithCount(1);
         }
      }
      return ItemStack.EMPTY;
   }
   
   
   @Override
   public NonNullList<ItemStack> getRemainingItems(CraftingInput input){
      NonNullList<ItemStack> stacks = NonNullList.withSize(input.size(), ItemStack.EMPTY);
      for(int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(rStack.isEmpty()) continue;
         
         if(rStack.getItem() instanceof MagmaticEversource.MagmaticEversourceItem){
            ItemStack source = rStack.copy();
            ArcanaItem.putProperty(source, MagmaticEversource.USES_TAG, ArcanaItem.getIntProperty(source, MagmaticEversource.USES_TAG) - 1);
            stacks.set(i, source);
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends CustomRecipe> getSerializer(){
      return ArcanaRegistry.MAGMATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   }
   
   public static class MagmaticEversourceRecipeSerializer extends Serializer implements PolymerObject {
      public MagmaticEversourceRecipeSerializer(Factory factory){
         super(factory);
      }
   }
}
