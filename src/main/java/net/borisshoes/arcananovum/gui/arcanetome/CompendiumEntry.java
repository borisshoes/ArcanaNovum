package net.borisshoes.arcananovum.gui.arcanetome;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class CompendiumEntry {
   
   protected final ArcaneTomeGui.TomeFilter[] categories;
   protected final ItemStack displayStack;
   
   protected CompendiumEntry(ArcaneTomeGui.TomeFilter[] categories, ItemStack displayStack){
      this.categories = categories;
      this.displayStack = displayStack;
   }
   
   public ArcaneTomeGui.TomeFilter[] getCategories(){
      return categories;
   }
   
   public ItemStack getDisplayStack(){
      return displayStack;
   }
   
   public abstract MutableComponent getName();
   
   public abstract int getRarityValue();
}
