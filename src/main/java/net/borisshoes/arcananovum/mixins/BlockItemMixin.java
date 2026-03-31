package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
   
   @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER))
   private void arcananovum$greavesHandRefill(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack stack){
      int count = stack.getCount();
      int lowerThresh = (int) (stack.getMaxStackSize() * 0.33);
      int upperThresh = (int) (stack.getMaxStackSize() * 0.67);
      if(stack.getCount() > lowerThresh) return;
      if(!(context.getPlayer() instanceof ServerPlayer player)) return;
      ItemStack pants = player.getItemBySlot(EquipmentSlot.LEGS);
      if(!(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus greaves) || !ArcanaItem.getBooleanProperty(pants, ArcanaItem.ACTIVE_TAG))
         return;
      ItemStack refillStack = greaves.getStackOf(pants, stack);
      if(!refillStack.isEmpty()){
         int amtToRefill = Math.min(upperThresh - count, refillStack.getCount());
         player.getInventory().add(refillStack.split(amtToRefill));
         greaves.buildItemLore(pants, BorisLib.SERVER);
         
         if(stack.is(Items.DIAMOND_BLOCK)){
            ArcanaAchievements.grant(player, ArcanaAchievements.MINERS_WALLET);
         }else if(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != null && (blockItem.getBlock().builtInRegistryHolder().is(BlockTags.BASE_STONE_OVERWORLD) || blockItem.getBlock().builtInRegistryHolder().is(BlockTags.DIRT))){
            ArcanaAchievements.progress(player, ArcanaAchievements.TERRAFORMER, amtToRefill);
         }
         
         ArcanaNovum.data(player).addXP(ArcanaNovum.CONFIG.getInt(ArcanaConfig.XP_GREAVES_OF_GAIALTUS_REFILL_BLOCK_PER_10) * amtToRefill / 10);
      }
   }
}
