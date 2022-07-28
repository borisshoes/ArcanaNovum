package net.borisshoes.arcananovum.recipes;

import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ShulkerCoreIngredient extends MagicItemIngredient{
   
   private final boolean needsStone;
   private final int minSouls;
   
   public ShulkerCoreIngredient(boolean needsStone, int minSouls){
      super(MagicItems.SHULKER_CORE.getPrefItem().getItem(), 1, null);
      this.needsStone = needsStone;
      this.minSouls = minSouls;
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      if(MagicItemUtils.identifyItem(stack) instanceof ShulkerCore core){
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
      String name = MagicItems.SHULKER_CORE.getName();
      if(needsStone){
         name += " ("+minSouls+"+ Souls)";
      }
      return name;
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return MagicItems.SHULKER_CORE.getPrefItem();
   }
}
