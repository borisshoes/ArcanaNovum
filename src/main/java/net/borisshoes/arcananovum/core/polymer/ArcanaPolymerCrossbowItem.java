package net.borisshoes.arcananovum.core.polymer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.skins.ArcanaSkin;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ArcanaPolymerCrossbowItem extends CrossbowItem implements PolymerItem {
   protected final ArcanaItem arcanaItem;
   
   public ArcanaPolymerCrossbowItem(ArcanaItem arcanaItem, net.minecraft.world.item.Item.Properties settings){
      super(settings.setId(ResourceKey.create(Registries.ITEM, ArcanaRegistry.arcanaId(arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target){
      super.shootProjectile(shooter, projectile, index, speed, divergence, yaw, target);
      
      if(projectile instanceof RunicArrowEntity runicArrow){
         SoundEvent sound = SoundEvents.TRIDENT_THROW.value();
         float volume = 0.8f;
         
         if(runicArrow.getArrowType() instanceof PhotonicArrows photonArrows){
            sound = SoundEvents.AMETHYST_BLOCK_HIT;
            volume = 1.2f;
            
            if(projectile.level() instanceof ServerLevel serverWorld){
               int alignmentLvl = Math.max(0, runicArrow.getAugment(ArcanaAugments.PRISMATIC_ALIGNMENT));
               photonArrows.shoot(serverWorld, shooter, runicArrow, alignmentLvl);
               projectile.kill(serverWorld);
            }
         }
         
         if(shooter instanceof ServerPlayer player){
            ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_RUNIC_ARROW_SHOOT));
            ArcanaAchievements.progress(player, ArcanaAchievements.JUST_LIKE_ARCHER, 1);
            shooter.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, 1.0F / (shooter.level().getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
         }
      }
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup){
      ItemStack stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context, lookup);
      ChargedProjectiles projs = itemStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
      stack.set(DataComponents.CHARGED_PROJECTILES, projs);
      return stack;
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
   public Component getName(ItemStack stack){
      return arcanaItem.getDisplayName() != null ? arcanaItem.getDisplayName() : super.getName(stack);
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
}
