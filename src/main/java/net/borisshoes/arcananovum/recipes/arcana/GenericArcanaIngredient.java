package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.item.ItemStack;

public class GenericArcanaIngredient extends ArcanaIngredient {
   
   private final ArcanaItem item;
   
   public GenericArcanaIngredient(ArcanaItem item, int count){
      super(item.getPrefItem().getItem(), count, true);
      this.item = item;
   }
   
   @Override
   public ArcanaIngredient copyWithCount(int newCount){
      return new GenericArcanaIngredient(item,newCount);
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      ArcanaItem stackItem = ArcanaItemUtils.identifyItem(stack);
      return stackItem != null && stackItem.getId().equals(item.getId()) && stack.getCount() >= count;
   }
   
   @Override
   public boolean validStackIgnoreCount(ItemStack stack){
      ArcanaItem stackItem = ArcanaItemUtils.identifyItem(stack);
      return stackItem != null && stackItem.getId().equals(item.getId());
   }
   
   @Override
   public String getName(){
      return item.getNameString();
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return item.getPrefItem().copyWithCount(count);
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof GenericArcanaIngredient o)) return false;
      return (o.item.getId().equals(item.getId()) && o.getCount() == count);
   }
}
