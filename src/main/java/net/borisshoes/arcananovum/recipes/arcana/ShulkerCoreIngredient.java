package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.item.ItemStack;

public class ShulkerCoreIngredient extends ArcanaIngredient {
   
   private final boolean needsStone;
   private final int minSouls;
   
   public ShulkerCoreIngredient(boolean needsStone, int minSouls){
      super(ArcanaRegistry.SHULKER_CORE.getPrefItem().getItem(), 1, true);
      this.needsStone = needsStone;
      this.minSouls = minSouls;
   }
   
   @Override
   public ArcanaIngredient copyWithCount(int newCount){
      return new ShulkerCoreIngredient(needsStone,minSouls);
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(ArcanaItemUtils.identifyItem(stack) instanceof ShulkerCore core){
         if(needsStone){
            boolean hasStone = core.hasStone(stack);
            if(hasStone){
               return core.getEnergy(stack) >= minSouls;
            }else{
               return false;
            }
         }else{
            return true;
         }
      }else{
         return false;
      }
   }
   
   @Override
   public String getName(){
      String name = ArcanaRegistry.SHULKER_CORE.getNameString();
      if(needsStone){
         name += " ("+minSouls+"+ Souls)";
      }
      return name;
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return ArcanaRegistry.SHULKER_CORE.getPrefItem();
   }
}
