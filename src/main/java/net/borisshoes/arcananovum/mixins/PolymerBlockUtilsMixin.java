package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PolymerBlockUtils.class)
public class PolymerBlockUtilsMixin {
   
   @ModifyReturnValue(method = "shouldMineServerSide", at = @At("RETURN"))
   private static boolean arcananovum$overrideServerMining(boolean original, ServerPlayer player, BlockPos pos, BlockState state){
      if(!original){
         List<ItemStack> stacks = ArcanaUtils.getArcanaItemsWithAug(player, ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 1);
         for(ItemStack stack : stacks){
            boolean isActive = ArcanaItem.getBooleanProperty(stack,ArcanaItem.ACTIVE_TAG);
            if(!isActive) continue;
            return true;
         }
      }
      return original;
   }
}
