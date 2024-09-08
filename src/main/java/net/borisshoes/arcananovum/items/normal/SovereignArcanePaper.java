package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
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

public class SovereignArcanePaper extends NormalPolymerItem {
   
   private static final String TXT = "item/sovereign_arcane_paper";
   
   public SovereignArcanePaper(Settings settings){
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
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.STARLIGHT_FORGE.getItem(),1,"",false)
            .withName(Text.literal("Starlight Forge").formatted(Formatting.LIGHT_PURPLE))
            .withLore(List.of(
                  Text.literal("Stardust Infusion Recipe").formatted(Formatting.YELLOW),
                  Text.literal(""),
                  Text.literal("A better infusion result yields more paper").formatted(Formatting.GOLD)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.EXOTIC_ARCANE_PAPER,1,"Exotic Arcane Paper")
            .withName(Text.literal("Exotic Arcane Paper").formatted(Formatting.AQUA));
      ExplainIngredient b = new ExplainIngredient(Items.GOLD_INGOT,1,"Gold Ingot")
            .withName(Text.literal("Gold Ingot").formatted(Formatting.BOLD,Formatting.GOLD));
      
      ExplainIngredient[][] ingredients = {
            {c,c,c,c,c},
            {c,p,p,p,c},
            {c,p,b,p,c},
            {c,p,p,p,c},
            {c,c,c,c,c}};
      
      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER.getTranslationKey()), new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER,8), new ExplainRecipe(ingredients));
   }
}
