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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExoticArcanePaper extends NormalPolymerItem {
   
   private static final String TXT = "item/exotic_arcane_paper";
   
   public ExoticArcanePaper(Settings settings){
      super(settings);
   }
   
   @Override
   public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return ArcanaRegistry.getModelData(TXT).value();
   }
   
   @Override
   public ArrayList<Pair<Item, String>> getModels(){
      ArrayList<Pair<Item, String>> models = new ArrayList<>();
      models.add(new Pair<>(Items.PAPER,TXT));
      return models;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
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
      
      return new IngredientCompendiumEntry("Exotic Arcane Paper", new ItemStack(ArcanaRegistry.EXOTIC_ARCANE_PAPER), new ExplainRecipe(ingredients));
   }
}
