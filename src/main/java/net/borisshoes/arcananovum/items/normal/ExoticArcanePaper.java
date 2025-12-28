package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.borisshoes.arcananovum.utils.ArcanaColors;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class ExoticArcanePaper extends NormalPolymerItem {
   
   public ExoticArcanePaper(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient a = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LAPIS_COLOR),1,"",false)
            .withName(Component.literal("In Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA));
      ExplainIngredient m = new ExplainIngredient(ArcanaRegistry.MIDNIGHT_ENCHANTER.getItem(),1,"",false)
            .withName(Component.literal("Midnight Enchanter").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").withStyle(ChatFormatting.BLUE)));
      ExplainIngredient b = new ExplainIngredient(ArcanaRegistry.EMPOWERED_ARCANE_PAPER,1,"Empowered Arcane Paper")
            .withName(Component.literal("Empowered Arcane Paper").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN))
            .withLore(List.of(Component.literal("Enchant Empowered Arcane Paper in the Midnight Enchanter").withStyle(ChatFormatting.LIGHT_PURPLE)));
      
      ExplainIngredient[][] ingredients = {
            {a,a,a,a,a},
            {a,a,a,a,a},
            {a,a,b,a,a},
            {a,a,m,a,a},
            {a,a,a,a,a}};
      
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.EXOTIC_ARCANE_PAPER.getDescriptionId()), new ItemStack(ArcanaRegistry.EXOTIC_ARCANE_PAPER), new ExplainRecipe(BuiltInRegistries.ITEM.getKey(ArcanaRegistry.EXOTIC_ARCANE_PAPER),ingredients));
   }
}
