package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.borisshoes.arcananovum.recipes.arcana.ExplainIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ExplainRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class EmpoweredArcanePaper extends NormalPolymerItem {
   
   public EmpoweredArcanePaper(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient c = new ExplainIngredient(Items.CRAFTING_TABLE,1,"",false)
            .withName(Component.literal("Crafting Recipe").withStyle(ChatFormatting.YELLOW))
            .withLore(List.of(Component.literal("Use a normal crafting table").withStyle(ChatFormatting.GRAY)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.MUNDANE_ARCANE_PAPER,1,"Mundane Arcane Paper")
            .withName(Component.literal("Mundane Arcane Paper").withStyle(ChatFormatting.AQUA));
      ExplainIngredient a = new ExplainIngredient(Items.AIR,1,"",false);
      ExplainIngredient b = new ExplainIngredient(Items.ENDER_EYE,1,"Eye of Ender")
            .withName(Component.literal("Eye of Ender").withStyle(ChatFormatting.DARK_AQUA));
      
      ExplainIngredient[][] ingredients = {
            {c,c,c,c,c},
            {c,a,p,a,c},
            {c,p,b,p,c},
            {c,a,p,a,c},
            {c,c,c,c,c}};
      
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.EMPOWERED_ARCANE_PAPER.getDescriptionId()), new ItemStack(ArcanaRegistry.EMPOWERED_ARCANE_PAPER,4), new ExplainRecipe(BuiltInRegistries.ITEM.getKey(ArcanaRegistry.EMPOWERED_ARCANE_PAPER),ingredients));
   }
}
