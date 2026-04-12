package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
import net.borisshoes.borislib.utils.ItemContainerContentsMutable;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.borisshoes.borislib.utils.SoundUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

public abstract class ArcanaPolymerItem extends Item implements PolymerItem {
   protected final ArcanaItem arcanaItem;
   
   public ArcanaPolymerItem(ArcanaItem arcanaItem){
      this(arcanaItem, arcanaItem.getArcanaItemComponents());
   }
   
   public ArcanaPolymerItem(ArcanaItem arcanaItem, net.minecraft.world.item.Item.Properties settings){
      super(settings.setId(ResourceKey.create(Registries.ITEM, ArcanaRegistry.arcanaId(arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   public Component getName(ItemStack stack){
      return arcanaItem.getDisplayName() != null ? arcanaItem.getDisplayName() : super.getName(stack);
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
      return PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         ArcanaSkin skin = ArcanaItem.getSkin(stack);
         if(skin != null){
            return skin.getModelId();
         }else{
            return ArcanaRegistry.arcanaId(arcanaItem.getId());
         }
      }else{
         return BuiltInRegistries.ITEM.getResourceKey(arcanaItem.getVanillaItem().asItem()).get().identifier();
      }
   }
   
   @Override
   public void modifyBasePolymerItemStack(ItemStack out, ItemStack stack, PacketContext context, HolderLookup.Provider lookup){
      PolymerItem.super.modifyBasePolymerItemStack(out, stack, context, lookup);
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
   
   public boolean arcanaBundleOtherStackedOnMe(final ItemStack self, final ItemStack other, final Slot slot, final ClickAction clickAction, final Player playerEntity, final SlotAccess carriedItem, int maxSlots, BiPredicate<ItemStack, List<ItemStack>> slotPredicate){
      if(playerEntity.level().isClientSide() || !(playerEntity instanceof ServerPlayer player)) return false;
      if(clickAction == ClickAction.PRIMARY && other.isEmpty()){
         return false;
      }else{
         ItemContainerContents initialContents = self.get(DataComponents.CONTAINER);
         if(initialContents == null){
            return false;
         }else{
            ItemContainerContentsMutable contents = ItemContainerContentsMutable.fromComponent(initialContents, maxSlots).setPredicate(slotPredicate);
            if(clickAction == ClickAction.PRIMARY && !other.isEmpty()){
               int originalSize = other.count();
               if(slot.allowModification(player) && contents.tryAddStackToContainerComp(other).getCount() < originalSize){
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
               }else{
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
               }
               
               self.set(DataComponents.CONTAINER, contents.toImmutable());
               arcanaItem.buildItemLore(self, player.level().getServer());
               return true;
            }else if(clickAction == ClickAction.SECONDARY && other.isEmpty()){
               if(slot.allowModification(player)){
                  ItemStack removed = contents.tryRemoveFirstNonEmpty();
                  if(!removed.isEmpty()){
                     SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
                     carriedItem.set(removed);
                  }
               }
               
               self.set(DataComponents.CONTAINER, contents.toImmutable());
               arcanaItem.buildItemLore(self, player.level().getServer());
               return true;
            }else{
               return false;
            }
         }
      }
   }
   
   public boolean arcanaBundleStackedOnOther(final ItemStack self, final Slot slot, final ClickAction clickAction, final Player playerEntity, int maxSlots, BiPredicate<ItemStack, List<ItemStack>> slotPredicate){
      if(playerEntity.level().isClientSide() || !(playerEntity instanceof ServerPlayer player)) return false;
      ItemContainerContents initialContents = self.get(DataComponents.CONTAINER);
      if(initialContents == null){
         return false;
      }else{
         ItemStack other = slot.getItem();
         ItemContainerContentsMutable contents = ItemContainerContentsMutable.fromComponent(initialContents, maxSlots).setPredicate(slotPredicate);
         if(clickAction == ClickAction.PRIMARY && !other.isEmpty()){
            ItemStack toTransfer = slot.safeTake(slot.getItem().getCount(), slot.getItem().getCount(), player);
            int transferCount = toTransfer.getCount();
            ItemStack leftover = contents.tryAddStackToContainerComp(toTransfer);
            int leftoverCount = leftover.getCount();
            ItemStack leftover2 = slot.safeInsert(leftover);
            if(!leftover2.isEmpty()) MinecraftUtils.giveStacks(player, leftover2);
            if(transferCount > leftoverCount){
               SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
            }else{
               SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
            }
            
            self.set(DataComponents.CONTAINER, contents.toImmutable());
            arcanaItem.buildItemLore(self, player.level().getServer());
            return true;
         }else if(clickAction == ClickAction.SECONDARY && other.isEmpty()){
            ItemStack itemStack = contents.tryRemoveFirstNonEmpty();
            if(!itemStack.isEmpty()){
               ItemStack remainder = slot.safeInsert(itemStack);
               if(remainder.getCount() > 0){
                  ItemStack leftover = contents.tryAddStackToContainerComp(remainder);
                  if(!leftover.isEmpty()) MinecraftUtils.giveStacks(player, leftover);
               }else{
                  SoundUtils.playSongToPlayer(player, SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
               }
            }
            
            self.set(DataComponents.CONTAINER, contents.toImmutable());
            arcanaItem.buildItemLore(self, player.level().getServer());
            return true;
         }else{
            return false;
         }
      }
   }
}
