package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class IngredientCompendiumEntry extends CompendiumEntry{
   
   private final ExplainRecipe recipe;
   private final String name;
   
   public IngredientCompendiumEntry(String name, ItemStack displayStack, ExplainRecipe recipe){
      super(new TomeGui.TomeFilter[]{TomeGui.TomeFilter.INGREDIENT}, displayStack);
      this.recipe = recipe;
      this.name = name;
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
   public String getName(){
      return name;
   }
   
   @Override
   public int getRarityValue(){
      return -1;
   }
}
