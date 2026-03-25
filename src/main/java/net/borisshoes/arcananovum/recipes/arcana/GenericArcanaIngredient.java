package net.borisshoes.arcananovum.recipes.arcana;

import com.google.gson.JsonObject;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

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
   
   @Override
   public String getCodeString(char character){
      StringBuilder builder = new StringBuilder(character + " = new GenericArcanaIngredient(ArcanaRegistry.");
      String id = item.getId().toUpperCase(Locale.ROOT);
      builder.append(id).append(", ").append(this.exampleStack.getCount());
      builder.append(");");
      return builder.toString();
   }
   
   public JsonObject toJson(){
      JsonObject json = new JsonObject();
      json.addProperty("type", "arcananovum:arcane_ingredient");
      json.addProperty("item", item.getId());
      json.addProperty("count", count);
      return json;
   }
   
   public static GenericArcanaIngredient fromJson(JsonObject json){
      if(!json.get("type").getAsString().equals("arcananovum:arcane_ingredient")) return null;
      String itemId = json.get("item").getAsString();
      int count = json.get("count").getAsInt();
      ArcanaItem arcanaItem = ArcanaRegistry.getArcanaItem(itemId);
      if(arcanaItem == null){
         throw new IllegalArgumentException("Unknown Arcana item: " + itemId);
      }
      return new GenericArcanaIngredient(arcanaItem, count);
   }
}
