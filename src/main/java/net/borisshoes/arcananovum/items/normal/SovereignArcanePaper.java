package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class SovereignArcanePaper extends NormalPolymerItem {
   
   public SovereignArcanePaper(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.STARLIGHT_FORGE.getItem(),1,"",false)
            .withName(Component.literal("Starlight Forge").withStyle(ChatFormatting.LIGHT_PURPLE))
            .withLore(List.of(
                  Component.literal("Stardust Infusion Recipe").withStyle(ChatFormatting.YELLOW),
                  Component.literal(""),
                  Component.literal("A better infusion result yields more paper").withStyle(ChatFormatting.GOLD)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.EXOTIC_ARCANE_PAPER,1,"Exotic Arcane Paper")
            .withName(Component.literal("Exotic Arcane Paper").withStyle(ChatFormatting.AQUA));
      ExplainIngredient b = new ExplainIngredient(Items.GOLD_INGOT,1,"Gold Ingot")
            .withName(Component.literal("Gold Ingot").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
      
      ExplainIngredient[][] ingredients = {
            {c,c,c,c,c},
            {c,p,p,p,c},
            {c,p,b,p,c},
            {c,p,p,p,c},
            {c,c,c,c,c}};
      
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER.getDescriptionId()), new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER,8), new ExplainRecipe(ingredients));
   }
}
