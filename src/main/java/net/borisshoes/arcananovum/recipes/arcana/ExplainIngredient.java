package net.borisshoes.arcananovum.recipes.arcana;

import net.minecraft.item.ItemStack;

public class ExplainIngredient extends MagicItemIngredient{
   
   private final ItemStack stack;
   private final String name;
   public final boolean show;
   
   public ExplainIngredient(ItemStack stack, String name){
      super(stack.getItem(), stack.getCount(), stack.getNbt());
      this.stack = stack;
      this.name = name;
      this.show = true;
   }
   
   public ExplainIngredient(ItemStack stack, String name, boolean show){
      super(stack.getItem(), stack.getCount(), stack.getNbt());
      this.stack = stack;
      this.name = name;
      this.show = show;
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      return false;
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      return stack;
   }
   
   @Override
   public String getName(){
      return name;
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof ExplainIngredient o)) return false;
      return (stack.equals(o.ingredientAsStack()) && name.equals(o.getName()));
   }
}
