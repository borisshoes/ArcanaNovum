package net.borisshoes.arcananovum.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public class DataFixer {
   
   public static ItemContainerContents nbtListToComponent(ListTag list, MinecraftServer server){
      List<ItemStack> stackList = new ArrayList<>();
      for(int i = 0; i < list.size(); i++){
         CompoundTag arrow = list.getCompoundOrEmpty(i);
         int slot = arrow.getByteOr("Slot", (byte) 0);
         ItemStack arrowStack = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, server.registryAccess()),arrow).result().orElse(ItemStack.EMPTY);
         if(arrowStack.getCount() > 0 && !arrowStack.isEmpty())
            stackList.add(arrowStack);
      }
      
      return ItemContainerContents.fromItems(stackList);
   }
}
