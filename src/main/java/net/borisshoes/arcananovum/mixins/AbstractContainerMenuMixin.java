package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
   
   @ModifyReturnValue(method = "stillValid(Lnet/minecraft/world/inventory/ContainerLevelAccess;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/Block;)Z", at = @At("RETURN"))
   private static boolean arcananovum$multitoolOverride(boolean original, ContainerLevelAccess containerLevelAccess, Player player, Block block){
      if(!original && player instanceof ServerPlayer serverPlayer){
         if(serverPlayer.isHolding(ArcanaRegistry.CLOCKWORK_MULTITOOL.getItem())) return true;
      }
      return original;
   }
}
