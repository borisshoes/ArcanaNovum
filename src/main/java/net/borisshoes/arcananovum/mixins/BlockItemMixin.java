package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.GreavesOfGaialtus;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

   @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrementUnlessCreative(ILnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER))
   private void arcananovum_greavesHandRefill(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir, @Local ItemStack stack){
      int count = stack.getCount();
      int lowerThresh = (int) (stack.getMaxCount() * 0.33);
      int upperThresh = (int) (stack.getMaxCount() * 0.67);
      if(stack.getCount() > lowerThresh) return;
      if(!(context.getPlayer() instanceof ServerPlayerEntity player)) return;
      ItemStack pants = player.getEquippedStack(EquipmentSlot.LEGS);
      if(!(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus greaves) || !ArcanaItem.getBooleanProperty(pants,ArcanaItem.ACTIVE_TAG)) return;
      ItemStack refillStack = greaves.getStackOf(pants, stack);
      if(!refillStack.isEmpty()){
         int amtToRefill = Math.min(upperThresh - count, refillStack.getCount());
         player.getInventory().insertStack(refillStack.split(amtToRefill));
         greaves.buildItemLore(pants, ArcanaNovum.SERVER);
         
         if(stack.isOf(Items.DIAMOND_BLOCK)){
            ArcanaAchievements.grant(player,ArcanaAchievements.MINERS_WALLET);
         }else if(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != null && (blockItem.getBlock().getRegistryEntry().isIn(BlockTags.BASE_STONE_OVERWORLD) || blockItem.getBlock().getRegistryEntry().isIn(BlockTags.DIRT))){
            ArcanaAchievements.progress(player,ArcanaAchievements.TERRAFORMER,amtToRefill);
         }
         
         ArcanaNovum.data(player).addXP(ArcanaConfig.getInt(ArcanaRegistry.GREAVES_OF_GAIALTUS_REFILL_BLOCK_PER_10) * amtToRefill / 10);
      }
   }
}
