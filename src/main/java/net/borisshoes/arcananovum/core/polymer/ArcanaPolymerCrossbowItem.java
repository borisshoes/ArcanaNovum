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
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public abstract class ArcanaPolymerCrossbowItem extends CrossbowItem implements PolymerItem {
   protected final ArcanaItem arcanaItem;
   public ArcanaPolymerCrossbowItem(ArcanaItem arcanaItem, Settings settings){
      super(settings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID,arcanaItem.getId()))));
      this.arcanaItem = arcanaItem;
   }
   
   @Override
   protected void shoot(LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target){
      super.shoot(shooter, projectile, index, speed, divergence, yaw, target);
      
      if(projectile instanceof RunicArrowEntity runicArrow){
         SoundEvent sound = SoundEvents.ITEM_TRIDENT_THROW.value();
         float volume = 0.8f;
         
         if(runicArrow.getArrowType() instanceof PhotonicArrows photonArrows){
            sound = SoundEvents.BLOCK_AMETHYST_BLOCK_HIT;
            volume = 1.2f;
            
            if(projectile.getWorld() instanceof ServerWorld serverWorld){
               int alignmentLvl = Math.max(0, runicArrow.getAugment(ArcanaAugments.PRISMATIC_ALIGNMENT.id));
               photonArrows.shoot(serverWorld, shooter, projectile, alignmentLvl);
               projectile.kill(serverWorld);
            }
         }
         
         if(shooter instanceof ServerPlayerEntity player){
            ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.RUNIC_ARROW_SHOOT));
            ArcanaAchievements.progress(player,ArcanaAchievements.JUST_LIKE_ARCHER.id, 1);
            shooter.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundCategory.PLAYERS,volume, 1.0F / (shooter.getWorld().getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);
         }
      }
   }
   
   @Override
   public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipType tooltipType, PacketContext context){
      ItemStack stack = PolymerItem.super.getPolymerItemStack(itemStack, tooltipType, context);
      ChargedProjectilesComponent projs = itemStack.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
      stack.set(DataComponentTypes.CHARGED_PROJECTILES,projs);
      return stack;
   }
   
   @Override
   public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context){
      if(PolymerResourcePackUtils.hasMainPack(context)){
         return Identifier.of(MOD_ID,arcanaItem.getId());
      }else{
         return Registries.ITEM.getKey(arcanaItem.getVanillaItem().asItem()).get().getValue();
      }
   }
   
   @Override
   public Text getName(ItemStack stack) {
      return arcanaItem.getDisplayName() != null ? arcanaItem.getDisplayName() : super.getName(stack);
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
