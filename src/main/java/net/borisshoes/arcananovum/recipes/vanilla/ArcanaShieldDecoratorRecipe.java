package net.borisshoes.arcananovum.recipes.vanilla;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShieldDecorationRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ArcanaShieldDecoratorRecipe extends ShieldDecorationRecipe {
   
   public ArcanaShieldDecoratorRecipe(CraftingBookCategory craftingRecipeCategory){
      super(CraftingBookCategory.EQUIPMENT);
   }
   
   @Override
   public boolean matches(CraftingInput craftingRecipeInput, Level world){
      ItemStack itemStack = ItemStack.EMPTY;
      ItemStack itemStack2 = ItemStack.EMPTY;
      
      for(int i = 0; i < craftingRecipeInput.size(); ++i){
         ItemStack itemStack3 = craftingRecipeInput.getItem(i);
         if(!itemStack3.isEmpty()){
            if(itemStack3.getItem() instanceof BannerItem){
               if(!itemStack2.isEmpty()){
                  return false;
               }
               
               itemStack2 = itemStack3;
            }else{
               
               if(!(itemStack3.getItem() instanceof ShieldOfFortitude.ShieldOfFortitudeItem)){
                  return false;
               }
               
               if(!itemStack.isEmpty()){
                  return false;
               }
               
               BannerPatternLayers bannerPatternsComponent = (BannerPatternLayers) itemStack3.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
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
   public ItemStack assemble(CraftingInput craftingRecipeInput, HolderLookup.Provider wrapperLookup){
      ItemStack itemStack = ItemStack.EMPTY;
      ItemStack itemStack2 = ItemStack.EMPTY;
      
      for(int i = 0; i < craftingRecipeInput.size(); ++i){
         ItemStack itemStack3 = craftingRecipeInput.getItem(i);
         if(!itemStack3.isEmpty()){
            if(itemStack3.getItem() instanceof BannerItem){
               itemStack = itemStack3;
            }else if(itemStack3.getItem() instanceof ShieldOfFortitude.ShieldOfFortitudeItem theShieldItem){
               itemStack2 = itemStack3.copy();
            }
         }
      }
      
      if(itemStack2.isEmpty()){
         return itemStack2;
      }else{
         itemStack2.set(DataComponents.BANNER_PATTERNS, (BannerPatternLayers) itemStack.get(DataComponents.BANNER_PATTERNS));
         itemStack2.set(DataComponents.BASE_COLOR, ((BannerItem) itemStack.getItem()).getColor());
         return itemStack2;
      }
   }
   
   public boolean fits(int width, int height){
      return width * height >= 2;
   }
   
   public static class ShieldRecipeSerializer extends Serializer implements PolymerObject {
      public ShieldRecipeSerializer(Factory factory){
         super(factory);
      }
   }
   
}
