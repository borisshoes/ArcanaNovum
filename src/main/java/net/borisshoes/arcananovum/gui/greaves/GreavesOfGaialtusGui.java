package net.borisshoes.arcananovum.gui.greaves;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.concurrent.atomic.AtomicInteger;

public class GreavesOfGaialtusGui extends SimpleGui {
   
   private final GreavesOfGaialtus greaves;
   private final ItemStack greavesStack;
   private SimpleContainer inv;
   private final int slotCount;
   
   public GreavesOfGaialtusGui(ServerPlayer player, GreavesOfGaialtus greaves, ItemStack greavesStack, int slotCount){
      super(getScreenType(slotCount), player, false);
      this.greaves = greaves;
      this.greavesStack = greavesStack;
      this.slotCount = slotCount;
   }
   
   private static MenuType<?> getScreenType(int slotCount){
      if(slotCount > 45){
         return MenuType.GENERIC_9x6;
      }else if(slotCount > 36){
         return MenuType.GENERIC_9x5;
      }else if(slotCount > 27){
         return MenuType.GENERIC_9x4;
      }else if(slotCount > 18){
         return MenuType.GENERIC_9x3;
      }else if(slotCount > 9){
         return MenuType.GENERIC_9x2;
      }else{
         return MenuType.GENERIC_9x1;
      }
   }
   
   public void build(){
      inv = new SimpleContainer(54);
      for(int i = 0; i < inv.getContainerSize(); i++){
         if(i < slotCount){
            setSlotRedirect(i, new GreavesSlot(inv, i, i, 0));
         }
      }
      
      ItemContainerContents beltItems = greavesStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
      AtomicInteger i = new AtomicInteger();
      beltItems.stream().forEachOrdered(stack -> {
         inv.setItem(i.get(), stack);
         i.getAndIncrement();
      });
      
      setTitle(greaves.getTranslatedName());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(type == ClickType.OFFHAND_SWAP || action == net.minecraft.world.inventory.ClickType.SWAP){
         close();
      }else if(index > slotCount){
         int invSlot = index >= 27 + slotCount ? index - (27 + slotCount) : index;
         ItemStack stack = player.getInventory().getItem(invSlot);
         if(ItemStack.isSameItemSameComponents(greavesStack, stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
      for(int i = 0; i < inv.getContainerSize(); i++){
         ItemStack itemStack = inv.getItem(i);
         if(itemStack.isEmpty()) continue;
         items.set(i, itemStack);
      }
      greavesStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
      greaves.buildItemLore(greavesStack, player.level().getServer());
   }
}