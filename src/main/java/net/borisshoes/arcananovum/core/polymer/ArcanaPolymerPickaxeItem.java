package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ArcanaPolymerPickaxeItem extends PickaxeItem implements PolymerItem {
   protected ArcanaItem arcanaItem;
   public ArcanaPolymerPickaxeItem(ArcanaItem arcanaItem, ToolMaterial material, int attackDamage, float attackSpeed, Settings settings){
      super(material, attackDamage, attackSpeed, settings);
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return arcanaItem.getVanillaItem();
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player){
      return PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
   }
   
   @Override
   public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return PolymerItem.super.getPolymerCustomModelData(itemStack, player);
   }
   
   @Override
   public int getPolymerArmorColor(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return PolymerItem.super.getPolymerArmorColor(itemStack, player);
   }
   
   @Override
   public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player){
      PolymerItem.super.modifyClientTooltip(tooltip, stack, player);
   }
   
   @Override
   public Item getPolymerReplacement(ServerPlayerEntity player){
      return PolymerItem.super.getPolymerReplacement(player);
   }
   
   @Override
   public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player){
      return PolymerItem.super.canSynchronizeToPolymerClient(player);
   }
   
   @Override
   public boolean canSyncRawToClient(ServerPlayerEntity player){
      return PolymerItem.super.canSyncRawToClient(player);
   }
   
   @Override
   public boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayerEntity player){
      return PolymerItem.super.handleMiningOnServer(tool, targetBlock, pos, player);
   }
   
   @Override
   public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
      return false;
   }
   
   @Override
   public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack){
      return true;
   }
   
   @Override
   public ItemStack getRecipeRemainder(ItemStack stack){
      return super.getRecipeRemainder(stack);
   }
}
