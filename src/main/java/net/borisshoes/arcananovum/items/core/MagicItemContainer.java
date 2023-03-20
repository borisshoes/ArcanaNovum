package net.borisshoes.arcananovum.items.core;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface MagicItemContainer {
   Inventory getItems(ItemStack item);
   double getConcMod();
   String getConcModStr();
   String getContainerName();
   int getSize();
   int getSortMod();
}
