package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredientCompendiumEntry extends CompendiumEntry{
   
   private final ExplainRecipe recipe;
   private final Text name;
   private final Optional<List<List<Text>>> bookLore;
   
   public IngredientCompendiumEntry(Text name, ItemStack displayStack, ExplainRecipe recipe){
      this(name,displayStack,recipe,null);
   }
   
   public IngredientCompendiumEntry(Text name, ItemStack displayStack, ExplainRecipe recipe, List<List<Text>> bookLore){
      super(new TomeGui.TomeFilter[]{TomeGui.TomeFilter.INGREDIENT}, displayStack);
      this.recipe = recipe;
      this.name = name;
      this.bookLore = bookLore == null ? Optional.empty() : Optional.of(bookLore);
      LoreComponent lore = this.displayStack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT);
      if(!lore.lines().isEmpty()){
         List<Text> lines = new ArrayList<>(lore.styledLines());
         lines.add(Text.literal(""));
         lines.add(TextUtils.removeItalics(Text.literal("Arcana Ingredient").formatted(Formatting.DARK_PURPLE)));
         this.displayStack.set(DataComponentTypes.LORE, new LoreComponent(lines,lines));
      }else{
         List<Text> lines = new ArrayList<>();
         lines.add(TextUtils.removeItalics(Text.literal("Arcana Ingredient").formatted(Formatting.DARK_PURPLE)));
         this.displayStack.set(DataComponentTypes.LORE, new LoreComponent(lines,lines));
      }
   }
   
   public ExplainRecipe getRecipe(){
      return recipe;
   }
   
   @Override
   public MutableText getName(){
      return name.copy();
   }
   
   @Override
   public int getRarityValue(){
      return -1;
   }
   
   public boolean hasBookLore(){
      return bookLore.isPresent();
   }
   
   public List<List<Text>> getBookLore(){
      return bookLore.orElse(null);
   }
}
