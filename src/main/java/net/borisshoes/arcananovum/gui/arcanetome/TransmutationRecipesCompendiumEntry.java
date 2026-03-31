package net.borisshoes.arcananovum.gui.arcanetome;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.borislib.gui.GraphicalItem;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class TransmutationRecipesCompendiumEntry extends CompendiumEntry {
   
   public TransmutationRecipesCompendiumEntry(){
      super(new ArcaneTomeGui.TomeFilter[]{}, GraphicalItem.with(ArcanaRegistry.TRANSMUTATION_BOOK));
      
      displayStack.set(DataComponents.ITEM_NAME, Component.literal("Transmutation Recipes").withStyle(ChatFormatting.AQUA));
      List<Component> loreText = new ArrayList<>();
      loreText.add(Component.literal("")
            .append(TextUtils.removeItalics(Component.literal("Click").withStyle(ChatFormatting.GREEN)))
            .append(TextUtils.removeItalics(Component.literal(" to view all Transmutation Recipes")).withStyle(ChatFormatting.LIGHT_PURPLE)));
      displayStack.set(DataComponents.LORE, new ItemLore(loreText, loreText));
   }
   
   @Override
   public MutableComponent getName(){
      return Component.literal("Transmutation Recipes");
   }
   
   @Override
   public int getRarityValue(){
      return -2;
   }
}
