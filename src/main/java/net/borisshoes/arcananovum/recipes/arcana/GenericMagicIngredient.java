package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.item.ItemStack;

public class GenericMagicIngredient extends MagicItemIngredient{
   
   private final MagicItem item;
   
   public GenericMagicIngredient(MagicItem item, int count){
      super(item.getPrefItem().getItem(), count, null);
      this.item = item;
   }
   
   @Override
   public MagicItemIngredient copyWithCount(int newCount){
      return new GenericMagicIngredient(item,newCount);
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      MagicItem stackItem = MagicItemUtils.identifyItem(stack);
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
      if(!(other instanceof GenericMagicIngredient o)) return false;
      return (o.item.getId().equals(item.getId()) && o.getCount() == count);
   }
}
