package net.borisshoes.arcananovum.gui;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WatchedContainer extends SimpleContainer {
   
   private final List<ContainerWatcher> watchers = new ArrayList<>();
   
   public WatchedContainer(int size){
      super(size);
   }
   
   public WatchedContainer(final ItemStack... itemstacks){
      super(itemstacks);
   }
   
   public void addWatcher(ContainerWatcher watcher){
      this.watchers.add(watcher);
   }
   
   public boolean removeWatcher(ContainerWatcher watcher){
      return this.watchers.remove(watcher);
   }
   
   public void clearWatchers(){
      this.watchers.clear();
   }
   
   @Override
   public void setChanged(){
      List.copyOf(watchers).forEach(watcher -> watcher.onChanged(this));
      super.setChanged();
   }
}
