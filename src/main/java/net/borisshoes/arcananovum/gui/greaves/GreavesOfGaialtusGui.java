package net.borisshoes.arcananovum.gui.greaves;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.concurrent.atomic.AtomicInteger;

public class GreavesOfGaialtusGui extends SimpleGui {
   
   private final GreavesOfGaialtus greaves;
   private final ItemStack greavesStack;
   private SimpleInventory inv;
   private final int slotCount;
   
   public GreavesOfGaialtusGui(ServerPlayerEntity player, GreavesOfGaialtus greaves, ItemStack greavesStack, int slotCount){
      super(getScreenType(slotCount), player, false);
      this.greaves = greaves;
      this.greavesStack = greavesStack;
      this.slotCount = slotCount;
   }
   
   private static ScreenHandlerType<?> getScreenType(int slotCount){
      if(slotCount > 45){
         return ScreenHandlerType.GENERIC_9X6;
      }else if(slotCount > 36){
         return ScreenHandlerType.GENERIC_9X5;
      }else if(slotCount > 27){
         return ScreenHandlerType.GENERIC_9X4;
      }else if(slotCount > 18){
         return ScreenHandlerType.GENERIC_9X3;
      }else if(slotCount > 9){
         return ScreenHandlerType.GENERIC_9X2;
      }else{
         return ScreenHandlerType.GENERIC_9X1;
      }
   }
   
   public void build(){
      inv = new SimpleInventory(54);
      for(int i = 0; i < inv.size(); i++){
         if(i < slotCount){
            setSlotRedirect(i, new GreavesSlot(inv,i,i,0));
         }
      }
      
      ContainerComponent beltItems = greavesStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
      AtomicInteger i = new AtomicInteger();
      beltItems.stream().forEachOrdered(stack -> {
         inv.setStack(i.get(),stack);
         i.getAndIncrement();
      });
      
      setTitle(greaves.getTranslatedName());
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      if(type == ClickType.OFFHAND_SWAP || action == SlotActionType.SWAP){
         close();
      }else if(index > slotCount){
         int invSlot = index >= 27+slotCount ? index - (27+slotCount) : index;
         ItemStack stack = player.getInventory().getStack(invSlot);
         if(ItemStack.areItemsAndComponentsEqual(greavesStack,stack)){
            close();
            return false;
         }
      }
      
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void onClose(){
      DefaultedList<ItemStack> items = DefaultedList.ofSize(54,ItemStack.EMPTY);
      for(int i = 0; i < inv.size(); i++){
         ItemStack itemStack = inv.getStack(i);
         if(itemStack.isEmpty()) continue;
         items.set(i, itemStack);
      }
      greavesStack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(items));
      greaves.buildItemLore(greavesStack,player.getServer());
   }
}