package net.borisshoes.arcananovum.recipes;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerShieldItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShieldDecorationRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class ArcanaShieldDecoratorRecipe extends ShieldDecorationRecipe {
   
   public ArcanaShieldDecoratorRecipe(CraftingRecipeCategory craftingRecipeCategory){
      super(CraftingRecipeCategory.EQUIPMENT);
   }
   
   @Override
   public boolean matches(CraftingRecipeInput craftingRecipeInput, World world){
      ItemStack itemStack = ItemStack.EMPTY;
      ItemStack itemStack2 = ItemStack.EMPTY;
      
      for (int i = 0; i < craftingRecipeInput.size(); ++i){
         ItemStack itemStack3 = craftingRecipeInput.getStackInSlot(i);
         if(!itemStack3.isEmpty()){
            if(itemStack3.getItem() instanceof BannerItem){
               if(!itemStack2.isEmpty()){
                  return false;
               }
               
               itemStack2 = itemStack3;
            }else{
               
               if(!(itemStack3.getItem() instanceof ArcanaPolymerShieldItem)){
                  return false;
               }
               
               if(!itemStack.isEmpty()){
                  return false;
               }
               
               BannerPatternsComponent bannerPatternsComponent = (BannerPatternsComponent)itemStack3.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
               if(!bannerPatternsComponent.layers().isEmpty()){
                  return false;
               }
               
               itemStack = itemStack3;
            }
         }
      }
      
      return !itemStack.isEmpty() && !itemStack2.isEmpty();
   }
   
   
   @Override
   public ItemStack craft(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup){
      ItemStack itemStack = ItemStack.EMPTY;
      ItemStack itemStack2 = ItemStack.EMPTY;
      
      for(int i = 0; i < craftingRecipeInput.size(); ++i){
         ItemStack itemStack3 = craftingRecipeInput.getStackInSlot(i);
         if(!itemStack3.isEmpty()){
            if(itemStack3.getItem() instanceof BannerItem){
               itemStack = itemStack3;
            } else if(itemStack3.getItem() instanceof ArcanaPolymerShieldItem theShieldItem){
               itemStack2 = itemStack3.copy();
            }
         }
      }
      
      if(itemStack2.isEmpty()){
         return itemStack2;
      }else{
         itemStack2.set(DataComponentTypes.BANNER_PATTERNS, (BannerPatternsComponent)itemStack.get(DataComponentTypes.BANNER_PATTERNS));
         itemStack2.set(DataComponentTypes.BASE_COLOR, ((BannerItem)itemStack.getItem()).getColor());
         return itemStack2;
      }
   }
   
   public boolean fits(int width, int height){
      return width * height >= 2;
   }
   
   public static class ShieldRecipeSerializer extends SpecialRecipeSerializer implements PolymerObject {
      public ShieldRecipeSerializer(Factory factory){
         super(factory);
      }
   }
   
}
