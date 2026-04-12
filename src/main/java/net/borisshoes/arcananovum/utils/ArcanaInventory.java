package net.borisshoes.arcananovum.utils;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltSlot;
import net.borisshoes.arcananovum.gui.greaves.GreavesSlot;
import net.borisshoes.arcananovum.gui.quivers.QuiverSlot;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.borisshoes.arcananovum.items.OverflowingQuiver;
import net.borisshoes.arcananovum.items.RunicQuiver;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.ItemContainerContentsMutable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.borisshoes.arcananovum.items.ArcanistsBelt.ArcanistsBeltItem.BELT_SLOT_COUNT;
import static net.borisshoes.arcananovum.items.GreavesOfGaialtus.GreavesOfGaialtusItem.GREAVES_SLOT_COUNT;

public class ArcanaInventory {
   
   private final List<Entry> items;
   
   private ArcanaInventory(List<Entry> items){
      this.items = items;
   }
   
   public static ArcanaInventory getPlayerItems(ServerPlayer player){
      Inventory playerInv = player.getInventory();
      List<Entry> invItems = new ArrayList<>();
      for(int i = 0; i < playerInv.getContainerSize(); i++){
         ItemStack item = playerInv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem mitem = ArcanaItemUtils.identifyItem(item);
         ItemContainerContentsMutable container = null;
         ItemContainerContents containerComp = item.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
         if(mitem instanceof ArcanistsBelt belt){
            container = ItemContainerContentsMutable.fromComponent(containerComp,BELT_SLOT_COUNT[ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.POUCHES)])
                  .setPredicate(ArcanistsBeltSlot.PREDICATE);
         }else if(mitem instanceof RunicQuiver quiver){
            container = ItemContainerContentsMutable.fromComponent(containerComp,9)
                  .setPredicate(QuiverSlot.RUNIC_PREDICATE);
         }else if(mitem instanceof OverflowingQuiver quiver){
            container = ItemContainerContentsMutable.fromComponent(containerComp,9)
                  .setPredicate(QuiverSlot.NON_RUNIC_PREDICATE);
         }else if(mitem instanceof GreavesOfGaialtus greaves){
            container = ItemContainerContentsMutable.fromComponent(containerComp,GREAVES_SLOT_COUNT[ArcanaAugments.getAugmentOnItem(item, ArcanaAugments.PLANETARY_POCKETS)])
                  .setPredicate(GreavesSlot.PREDICATE);
         }
         invItems.add(new Entry(item,ItemStack.EMPTY,null));
         if(container != null){
            for(ItemStack containerItem : container.getNonEmpty()){
               invItems.add(new Entry(containerItem,item,container));
            }
         }
      }
      return new ArcanaInventory(invItems);
   }
   
   public List<Entry> getArcanaItems(ArcanaItem arcanaItem){
      return this.items.stream().filter(entry -> {
         ArcanaItem mitem = ArcanaItemUtils.identifyItem(entry.getStack());
         return mitem != null && mitem.getId().equals(arcanaItem.getId());
      }).toList();
   }
   
   public List<Entry> getMatchingItems(Predicate<ItemStack> predicate){
      return this.items.stream().filter(entry -> predicate.test(entry.getStack())).toList();
   }
   
   public List<Entry> getMatchingEntries(Predicate<Entry> predicate){
      return this.items.stream().filter(predicate).toList();
   }
   
   public boolean anyMatch(Predicate<ItemStack> predicate){
      return this.items.stream().anyMatch(entry -> predicate.test(entry.getStack()));
   }
   
   public void close(){
      HashMap<ItemContainerContentsMutable,ItemStack> changedContainers = new HashMap<>();
      for(Entry item : this.items){
         if(item.modified && item.mutableContainer != null){
            changedContainers.put(item.mutableContainer, item.containerItem);
         }
      }
      
      for(Map.Entry<ItemContainerContentsMutable, ItemStack> entry : changedContainers.entrySet()){
         ItemStack stack = entry.getValue();
         ItemContainerContentsMutable container = entry.getKey();
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
         stack.set(DataComponents.CONTAINER,container.toImmutable());
         if(arcanaItem != null){
            arcanaItem.buildItemLore(stack, BorisLib.SERVER);
         }
      }
   }
   
   
   
   public static class Entry{
      private final ItemStack stack;
      private final ItemStack containerItem;
      private final ItemContainerContentsMutable mutableContainer;
      private boolean modified = false;
      
      public Entry(ItemStack stack, ItemStack containerItem, ItemContainerContentsMutable mutableContainer){
         this.stack = stack;
         this.containerItem = containerItem;
         this.mutableContainer = mutableContainer;
      }
      
      public ItemStack getStack(){
         return stack;
      }
      
      public ItemStack getContainerItem(){
         return containerItem;
      }
      
      public void setModified(){
         this.modified = true;
      }
   }
}
