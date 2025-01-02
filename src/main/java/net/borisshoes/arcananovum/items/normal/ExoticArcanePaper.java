package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class ExoticArcanePaper extends NormalPolymerItem {
   
   public ExoticArcanePaper(String id, Settings settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.LAPIS_COLOR),1,"",false)
            .withName(Text.literal("In Midnight Enchanter").formatted(Formatting.DARK_AQUA));
      ExplainIngredient m = new ExplainIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem(),1,"",false)
            .withName(Text.literal("Midnight Enchanter").formatted(Formatting.DARK_AQUA, Formatting.BOLD))
            .withLore(List.of(Text.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").formatted(Formatting.BLUE)));
      ExplainIngredient b = new ExplainIngredient(ArcanaRegistry.EMPOWERED_ARCANE_PAPER,1,"Empowered Arcane Paper")
            .withName(Text.literal("Empowered Arcane Paper").formatted(Formatting.BOLD,Formatting.GREEN))
            .withLore(List.of(Text.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").formatted(Formatting.LIGHT_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,a,a,a},
            {a,a,b,a,a},
            {a,a,m,a,a},
            {a,a,a,a,a}};
      
      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.EXOTIC_ARCANE_PAPER.getTranslationKey()), new ItemStack(ArcanaRegistry.EXOTIC_ARCANE_PAPER), new ExplainRecipe(ingredients));
   }
}
