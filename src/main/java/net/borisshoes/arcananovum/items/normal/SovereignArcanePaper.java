package net.borisshoes.arcananovum.items.normal;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.polymer.NormalPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.IngredientCompendiumEntry;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SovereignArcanePaper extends NormalPolymerItem {
   
   public SovereignArcanePaper(String id, net.minecraft.world.item.Item.Properties settings){
      super(id, settings);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return Items.PAPER;
   }
   
   public static IngredientCompendiumEntry getCompendiumEntry(){
      return new IngredientCompendiumEntry(Component.translatable(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER.getDescriptionId()), new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER, 8), ArcanaRegistry.SOVEREIGN_ARCANE_PAPER);
   }
}
