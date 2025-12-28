package net.borisshoes.arcananovum.recipes.vanilla;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.AquaticEversource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Map;

import static java.util.Map.entry;

public class AquaticEversourceFillRecipe extends CustomRecipe {
   
   private static final Map<Item, ItemStack> FILLABLE = Map.ofEntries(
         entry(Items.DIRT, Items.MUD.getDefaultInstance()),
         entry(Items.GLASS_BOTTLE, PotionContents.createItemStack(Items.POTION, Potions.WATER)),
         entry(Items.BUCKET, Items.WATER_BUCKET.getDefaultInstance())
   );
   
   public AquaticEversourceFillRecipe(CraftingBookCategory craftingRecipeCategory){
      super(CraftingBookCategory.MISC);
   }
   
   @Override
   public boolean matches(CraftingInput input, Level world){
      boolean hasEversource = false;
      boolean hasFillable = false;
      
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
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
   public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries){
      for (int i = 0; i < input.size(); ++i){
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
      for (int i = 0; i < input.size(); ++i){
         ItemStack rStack = input.getItem(i);
         if(rStack.isEmpty()) continue;
         
         if(rStack.getItem() instanceof AquaticEversource.AquaticEversourceItem){
            stacks.set(i, rStack.copy());
         }
      }
      return stacks;
   }
   
   @Override
   public RecipeSerializer<? extends CustomRecipe> getSerializer(){
      return ArcanaRegistry.AQUATIC_EVERSOURCE_FILL_RECIPE_SERIALIZER;
   }
   
   public static class AquaticEversourceRecipeSerializer extends Serializer implements PolymerObject {
      public AquaticEversourceRecipeSerializer(Factory factory){
         super(factory);
      }
   }
}
