package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class EmpoweredArcanePaper extends NormalPolymerItem {
   
   public EmpoweredArcanePaper(String id, Settings settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient c = new ExplainIngredient(Items.CRAFTING_TABLE,1,"",false)
            .withName(Text.literal("Crafting Recipe").formatted(Formatting.YELLOW))
            .withLore(List.of(Text.literal("Use a normal crafting table").formatted(Formatting.GRAY)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER,1,"Mundane Arcane Paper")
            .withName(Text.literal("Mundane Arcane Paper").formatted(Formatting.AQUA));
      ExplainIngredient a = new ExplainIngredient(Items.AIR,1,"",false);
      ExplainIngredient b = new ExplainIngredient(Items.ENDER_EYE,1,"Eye of Ender")
            .withName(Text.literal("Eye of Ender").formatted(Formatting.DARK_AQUA));
      
      ExplainIngredient[][] ingredients = {
            {c,c,c,c,c},
            {c,a,p,a,c},
            {c,p,b,p,c},
            {c,a,p,a,c},
            {c,c,c,c,c}};
      
      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.EMPOWERED_ARCANE_PAPER.getTranslationKey()), new ItemStack(ArcanaRegistry.EMPOWERED_ARCANE_PAPER,4), new ExplainRecipe(ingredients));
   }
}
