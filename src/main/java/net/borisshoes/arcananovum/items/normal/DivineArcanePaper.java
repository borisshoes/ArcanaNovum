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

public class DivineArcanePaper extends NormalPolymerItem {
   
   private static final String TXT = "item/divine_arcane_paper";
   
   public DivineArcanePaper(Settings settings){
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
      ExplainIngredient b = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicItems.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Text.literal("Transmutation Recipe").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_CATALYST.getItem(),1,"Sovereign Augment Catalyst")
            .withName(Text.literal("Sovereign Augmentation Catalyst").formatted(Formatting.GOLD,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.LIGHT_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Text.literal("Transmutation Altar").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Use a Transmutation Altar").formatted(Formatting.DARK_AQUA)));
      ExplainIngredient d = new ExplainIngredient(Items.DIAMOND,16,"Diamonds")
            .withName(Text.literal("Diamonds").formatted(Formatting.AQUA,Formatting.BOLD))
            .withLore(List.of(Text.literal("Transmutation Reagent").formatted(Formatting.LIGHT_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER,2,"Sovereign Arcane Paper")
            .withName(Text.literal("Sovereign Arcane Paper").formatted(Formatting.GOLD,Formatting.BOLD))
            .withLore(List.of(Text.literal("Infusion Input").formatted(Formatting.WHITE)));
      
      ExplainIngredient[][] ingredients = {
            {b,b,p,b,b},
            {b,b,b,b,w},
            {c,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      
      return new IngredientCompendiumEntry(Text.translatable(ArcanaRegistry.DIVINE_ARCANE_PAPER.getTranslationKey()), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,1), new ExplainRecipe(ingredients));
   }
}
