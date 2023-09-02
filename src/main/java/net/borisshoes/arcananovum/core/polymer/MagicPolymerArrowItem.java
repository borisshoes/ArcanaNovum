package net.borisshoes.arcananovum.core.polymer;

import com.google.common.collect.Multimap;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MagicPolymerArrowItem extends ArrowItem implements PolymerItem {
   protected MagicItem magicItem;
   public MagicPolymerArrowItem(MagicItem magicItem, Settings settings){
      super(settings);
      this.magicItem = magicItem;
   }
   
   @Override
   public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
      RunicArrowEntity arrowEntity = new RunicArrowEntity(world, shooter, stack);
      arrowEntity.initFromStack(stack);
      return arrowEntity;
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      return magicItem.getVanillaItem();
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipContext context, @Nullable ServerPlayerEntity player){
      ItemStack superStack = PolymerItem.super.getPolymerItemStack(itemStack, context, player);
      superStack.getNbt().putInt("CustomPotionColor",itemStack.getNbt().getInt("CustomPotionColor"));
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
   public boolean showDefaultNameInItemFrames(){
      return PolymerItem.super.showDefaultNameInItemFrames();
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
   public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack){
      return false;
   }
   
   @Override
   public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack){
      return true;
   }
   
   @Override
   public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot){
      return super.getAttributeModifiers(stack, slot);
   }
   
   @Override
   public boolean isSuitableFor(ItemStack stack, BlockState state){
      return super.isSuitableFor(stack, state);
   }
   
   @Override
   public ItemStack getRecipeRemainder(ItemStack stack){
      return super.getRecipeRemainder(stack);
   }
}

