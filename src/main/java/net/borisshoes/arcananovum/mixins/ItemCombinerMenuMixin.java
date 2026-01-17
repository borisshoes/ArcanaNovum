package net.borisshoes.arcananovum.mixins;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemCombinerMenu.class)
public class ItemCombinerMenuMixin {
   
   @ModifyReturnValue(method = "stillValid(Lnet/minecraft/world/entity/player/Player;)Z", at = @At("RETURN"))
   private static boolean arcananovum$multitoolOverride(boolean original, Player player){
      if(!original && player instanceof ServerPlayer serverPlayer){
         if(serverPlayer.isHolding(ArcanaRegistry.CLOCKWORK_MULTITOOL.getItem())) return true;
      }
      return original;
   }
}
