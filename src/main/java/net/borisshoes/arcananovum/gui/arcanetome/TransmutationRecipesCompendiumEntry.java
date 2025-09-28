package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TransmutationRecipesCompendiumEntry extends CompendiumEntry{
   
   public TransmutationRecipesCompendiumEntry(){
      super(new TomeGui.TomeFilter[]{}, GraphicalItem.with(ArcanaRegistry.TRANSMUTATION_BOOK));
      
      displayStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Transmutation Recipes").formatted(Formatting.AQUA));
      List<Text> loreText = new ArrayList<>();
      loreText.add(Text.literal("")
            .append(TextUtils.removeItalics(Text.literal("Click").formatted(Formatting.GREEN)))
            .append(TextUtils.removeItalics(Text.literal(" to view all Transmutation Recipes")).formatted(Formatting.LIGHT_PURPLE)));
      displayStack.set(DataComponentTypes.LORE, new LoreComponent(loreText,loreText));
   }
   
   @Override
   public MutableText getName(){
      return Text.literal("Transmutation Recipes");
   }
   
   @Override
   public int getRarityValue(){
      return -2;
   }
}
