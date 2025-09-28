package net.borisshoes.arcananovum.recipes.arcana;

import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExplainIngredient extends ArcanaIngredient {
   
   private final String ingredientName;
   public final boolean show;
   private Text displayName;
   private List<Text> lore;
   private final ItemStack displayOverride;
   
   public ExplainIngredient(ItemStack itemStack, int count, String ingredientName, boolean show){
      super(itemStack.getItem(), count);
      this.ingredientName = ingredientName;
      this.show = show;
      this.displayOverride = itemStack.copy();
   }
   
   public ExplainIngredient(Item item, int count, String ingredientName){
      super(item, count);
      this.ingredientName = ingredientName;
      this.show = true;
      this.displayOverride = null;
   }
   
   public ExplainIngredient(Item item, int count, String ingredientName, boolean show){
      super(item, count);
      this.ingredientName = ingredientName;
      this.show = show;
      this.displayOverride = null;
   }
   
   public ExplainIngredient withName(Text name){
      this.exampleStack.set(DataComponentTypes.ITEM_NAME,name);
      if(displayOverride != null){
         this.displayOverride.set(DataComponentTypes.ITEM_NAME,name);
      }
      this.displayName = name;
      return this;
   }
   
   public ExplainIngredient withLore(List<Text> lore){
      this.lore = lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
      this.exampleStack.set(DataComponentTypes.LORE,new LoreComponent(this.lore));
      if(displayOverride != null){
         this.displayOverride.set(DataComponentTypes.LORE,new LoreComponent(this.lore));
      }
      return this;
   }
   
   @Override
   public ArcanaIngredient copyWithCount(int newCount){
      ExplainIngredient ingredient;
      if(displayOverride != null){
         ingredient = new ExplainIngredient(this.displayOverride,this.count,ingredientName,show);
      }else{
         ingredient = new ExplainIngredient(this.itemType,this.count,ingredientName,show);
      }
      if(displayName != null){
         ingredient = ingredient.withName(this.displayName);
      }
      if(lore != null){
         ingredient = ingredient.withLore(this.lore);
      }
      return ingredient;
   }
   
   @Override
   public boolean validStack(ItemStack stack){
      return false;
   }
   
   @Override
   public boolean validStackIgnoreCount(ItemStack stack){
      return false;
   }
   
   @Override
   public String getName(){
      return ingredientName;
   }
   
   @Override
   public boolean equals(Object other){
      if(other == this) return true;
      if(!(other instanceof ExplainIngredient o)) return false;
      return (this.exampleStack.equals(o.ingredientAsStack()) && ingredientName.equals(o.getName()));
   }
   
   @Override
   public ItemStack ingredientAsStack(){
      if(displayOverride != null){
         return displayOverride.copy();
      }else{
         return super.ingredientAsStack();
      }
   }
}
