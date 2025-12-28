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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class DivineArcanePaper extends NormalPolymerItem {
   
   public DivineArcanePaper(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      ExplainIngredient b = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.DARK_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient w = new ExplainIngredient(GraphicalItem.withColor(GraphicalItem.PAGE_BG, ArcanaColors.LIGHT_COLOR),1,"",false)
            .withName(Component.literal("Transmutation Recipe").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient c = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_CATALYST.getItem(),1,"Sovereign Augment Catalyst")
            .withName(Component.literal("Sovereign Augmentation Catalyst").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      ExplainIngredient t = new ExplainIngredient(ArcanaRegistry.TRANSMUTATION_ALTAR.getItem(),1,"",false)
            .withName(Component.literal("Transmutation Altar").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Use a Transmutation Altar").withStyle(ChatFormatting.DARK_AQUA)));
      ExplainIngredient d = new ExplainIngredient(Items.DIAMOND,16,"Diamonds")
            .withName(Component.literal("Diamonds").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Transmutation Reagent").withStyle(ChatFormatting.LIGHT_PURPLE)));
      ExplainIngredient p = new ExplainIngredient(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER,2,"Sovereign Arcane Paper")
            .withName(Component.literal("Sovereign Arcane Paper").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .withLore(List.of(Component.literal("Infusion Input").withStyle(ChatFormatting.WHITE)));
      
      ExplainIngredient[][] ingredients = {
            {b,b,p,b,b},
            {b,b,b,b,w},
            {c,b,t,w,d},
            {b,w,w,w,w},
            {w,w,w,w,w}};
      
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.DIVINE_ARCANE_PAPER.getDescriptionId()), new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,1), new ExplainRecipe(BuiltInRegistries.ITEM.getKey(ArcanaRegistry.DIVINE_ARCANE_PAPER),ingredients));
   }
}
