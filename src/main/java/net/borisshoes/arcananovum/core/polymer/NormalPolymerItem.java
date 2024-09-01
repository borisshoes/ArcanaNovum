package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class NormalPolymerItem extends Item implements PolymerItem {
   
   public NormalPolymerItem(Settings settings){
      super(settings);
   }
   
   public abstract ArrayList<Pair<Item,String>> getModels();
   
   @Override
   public abstract Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player);
   
   @Override
   public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
      return false;
   }
   
   @Override
   public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack){
      return true;
   }
}
