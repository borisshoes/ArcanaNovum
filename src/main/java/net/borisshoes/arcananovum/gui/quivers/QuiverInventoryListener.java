package net.borisshoes.arcananovum.gui.quivers;

import net.borisshoes.arcananovum.gui.ContainerWatcher;
import net.borisshoes.arcananovum.gui.WatchedContainer;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.minecraft.world.item.ItemStack;

public class QuiverInventoryListener implements ContainerWatcher {
   
   private final QuiverItem quiver;
   private final QuiverGui gui;
   private final ItemStack item;
   private boolean updating = false;
   
   public QuiverInventoryListener(QuiverItem quiver, QuiverGui gui, ItemStack item){
      this.quiver = quiver;
      this.gui = gui;
      this.item = item;
   }
   
   @Override
   public void onChanged(WatchedContainer inv){
   
   }
   
   public void finishUpdate(){
      updating = false;
   }
   
   public void setUpdating(){
      updating = true;
   }
}
