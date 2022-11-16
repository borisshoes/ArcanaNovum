package net.borisshoes.arcananovum.items.core;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface TickingItem{
   public void onTick(ServerWorld world, ServerPlayerEntity player, ItemStack item);
}
