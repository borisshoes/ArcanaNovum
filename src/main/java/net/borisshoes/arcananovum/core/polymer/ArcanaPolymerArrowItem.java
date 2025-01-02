package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerArrowItem extends ArrowItem implements PolymerItem {
   protected final ArcanaItem arcanaItem;
   public ArcanaPolymerArrowItem(ArcanaItem arcanaItem, Item.Settings settings){
      super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public Text getName(ItemStack stack) {
      return arcanaItem.getDisplayName() != null ? arcanaItem.getDisplayName() : super.getName(stack);
   }
   
   @Override
   public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom){
      return new RunicArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
   }
   
   @Override
   public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction){
      return new RunicArrowEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1), null);
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
      ItemStack superStack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
      PotionContentsComponent stackComp = itemStack.get(DataComponentTypes.POTION_CONTENTS);
      if(stackComp != null){
         PotionContentsComponent cur = superStack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
         superStack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(cur.potion(), Optional.of(stackComp.getColor()),cur.customEffects(), cur.customName()));
      }
      if(superStack.contains(DataComponentTypes.POTION_CONTENTS) && !superStack.contains(DataComponentTypes.CUSTOM_NAME)){
         superStack.set(DataComponentTypes.CUSTOM_NAME, TextUtils.removeItalics(this.getName(superStack)));
      }
      return superStack;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return arcanaItem instanceof RunicArrow ? Identifier.of(MOD_ID,RunicArrow.TXT) : Identifier.of(MOD_ID,arcanaItem.getId());
      }else{
         return Registries.ITEM.getKey(arcanaItem.getVanillaItem().asItem()).get().getValue();
      }
   }
   
   @Override
   public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context){
      PolymerItem.super.modifyBasePolymerItemStack(out, stack, context);
   }
   
   @Override
   public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context){
      PolymerItem.super.modifyClientTooltip(tooltip, stack, context);
   }
   
   @Override
   public Item getPolymerReplacement(PacketContext context){
      return PolymerItem.super.getPolymerReplacement(context);
   }
   
   @Override
   public boolean shouldStorePolymerItemStackCount(){
      return PolymerItem.super.shouldStorePolymerItemStackCount();
   }
   
   @Override
   public boolean isPolymerBlockInteraction(BlockState state, ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, BlockHitResult blockHitResult, ActionResult actionResult){
      return PolymerItem.super.isPolymerBlockInteraction(state, player, hand, stack, world, blockHitResult, actionResult);
   }
   
   @Override
   public boolean isPolymerEntityInteraction(ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, Entity entity, ActionResult actionResult){
      return PolymerItem.super.isPolymerEntityInteraction(player, hand, stack, world, entity, actionResult);
   }
   
   @Override
   public boolean isPolymerItemInteraction(ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, ActionResult actionResult){
      return PolymerItem.super.isPolymerItemInteraction(player, hand, stack, world, actionResult);
   }
   
   @Override
   public Item getPolymerItem(ItemStack itemStack, PacketContext context){
      return arcanaItem.getVanillaItem();
   }
   
   @Override
   public boolean canSynchronizeToPolymerClient(PacketContext context){
      return PolymerItem.super.canSynchronizeToPolymerClient(context);
   }
   
   @Override
   public boolean canSyncRawToClient(PacketContext context){
      return PolymerItem.super.canSyncRawToClient(context);
   }
   
   @Override
   public boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayerEntity player){
      return PolymerItem.super.handleMiningOnServer(tool, targetBlock, pos, player);
   }
}

