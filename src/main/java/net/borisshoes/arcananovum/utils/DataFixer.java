package net.borisshoes.arcananovum.utils;

import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class DataFixer {
   
   public static ContainerComponent nbtListToComponent(NbtList list, MinecraftServer server){
      List<ItemStack> stackList = new ArrayList<>();
      for(int i = 0; i < list.size(); i++){
         NbtCompound arrow = list.getCompound(i);
         int slot = arrow.getByte("Slot");
         ItemStack arrowStack = ItemStack.fromNbt(server.getRegistryManager(),arrow).orElse(ItemStack.EMPTY);
         if(arrowStack.getCount() > 0 && !arrowStack.isEmpty())
            stackList.add(arrowStack);
      }
      
      return ContainerComponent.fromStacks(stackList);
   }
}
