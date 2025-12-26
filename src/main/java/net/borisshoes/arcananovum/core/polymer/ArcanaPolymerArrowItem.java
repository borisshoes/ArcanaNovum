package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerArrowItem extends ArrowItem implements PolymerItem {
   protected final ArcanaItem arcanaItem;
   public ArcanaPolymerArrowItem(ArcanaItem arcanaItem, Item.Properties settings){
      super(settings.setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public Component getName(ItemStack stack) {
      return arcanaItem.getDisplayName() != null ? arcanaItem.getDisplayName() : super.getName(stack);
   }
   
   @Override
   public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter, @Nullable ItemStack shotFrom){
      return new RunicArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom);
   }
   
   @Override
   public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction){
      return new RunicArrowEntity(world, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1), null);
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context){
      ItemStack superStack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
      PotionContents stackComp = itemStack.get(DataComponents.POTION_CONTENTS);
      if(stackComp != null){
         PotionContents cur = superStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
         superStack.set(DataComponents.POTION_CONTENTS, new PotionContents(cur.potion(), Optional.of(stackComp.getColor()),cur.customEffects(), cur.customName()));
      }
      if(superStack.has(DataComponents.POTION_CONTENTS) && !superStack.has(DataComponents.CUSTOM_NAME)){
         superStack.set(DataComponents.CUSTOM_NAME, TextUtils.removeItalics(this.getName(superStack)));
      }
      return superStack;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return arcanaItem instanceof RunicArrow ? Identifier.fromNamespaceAndPath(MOD_ID,RunicArrow.TXT) : Identifier.fromNamespaceAndPath(MOD_ID,arcanaItem.getId());
      }else{
         return BuiltInRegistries.ITEM.getResourceKey(arcanaItem.getVanillaItem().asItem()).get().identifier();
      }
   }
   
   @Override
   public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context){
      PolymerItem.super.modifyBasePolymerItemStack(out, stack, context);
   }
   
   @Override
   public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context){
      PolymerItem.super.modifyClientTooltip(tooltip, stack, context);
   }
   
   @Override
   public Item getPolymerReplacement(Item item, PacketContext context){
      return PolymerItem.super.getPolymerReplacement(item, context);
   }
   
   @Override
   public boolean shouldStorePolymerItemStackCount(){
      return PolymerItem.super.shouldStorePolymerItemStackCount();
   }
   
   @Override
   public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult){
      return PolymerItem.super.isPolymerBlockInteraction(state, player, hand, stack, world, blockHitResult, actionResult);
   }
   
   @Override
   public boolean isPolymerEntityInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, Entity entity, InteractionResult actionResult){
      return PolymerItem.super.isPolymerEntityInteraction(player, hand, stack, world, entity, actionResult);
   }
   
   @Override
   public boolean isPolymerItemInteraction(ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, InteractionResult actionResult){
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
   public boolean handleMiningOnServer(ItemStack tool, BlockState targetBlock, BlockPos pos, ServerPlayer player){
      return PolymerItem.super.handleMiningOnServer(tool, targetBlock, pos, player);
   }
}

