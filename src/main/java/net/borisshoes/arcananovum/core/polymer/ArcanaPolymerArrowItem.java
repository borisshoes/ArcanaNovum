package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public abstract class ArcanaPolymerArrowItem extends ArrowItem implements PolymerItem {
   protected ArcanaItem arcanaItem;
   public ArcanaPolymerArrowItem(ArcanaItem arcanaItem, Item.Settings settings){
      super(settings);
      this.arcanaItem = arcanaItem;
   }
   
   
   @Override
   public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom) {
      return new RunicArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
   }
   
   @Override
   public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
      return new RunicArrowEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1), null);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return arcanaItem.getVanillaItem();
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player){
      ItemStack superStack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, lookup, player);
      PotionContentsComponent stackComp = itemStack.get(DataComponentTypes.POTION_CONTENTS);
      if(stackComp != null){
         PotionContentsComponent cur = superStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
         superStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(cur.potion(), Optional.of(stackComp.getColor()),cur.customEffects(),Optional.empty()));
      }
      return superStack;
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

