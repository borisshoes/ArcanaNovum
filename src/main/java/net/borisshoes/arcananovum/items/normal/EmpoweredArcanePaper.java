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

public class EmpoweredArcanePaper extends NormalPolymerItem {
   
   private static final String TXT = "item/empowered_arcane_paper";
   
   public EmpoweredArcanePaper(Settings settings){
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
      
      return new IngredientCompendiumEntry("Empowered Arcane Paper", new ItemStack(ArcanaRegistry.EMPOWERED_ARCANE_PAPER,4), new ExplainRecipe(ingredients));
   }
}
