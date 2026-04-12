package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.callbacks.InventoryChangedCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$2")
public class ServerPlayerEntityScreenHandlerListenerMixin {
   
   @Shadow
   @Final
   ServerPlayer this$0;
   
   @Inject(method = "slotChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/InventoryChangeTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/ItemStack;)V"))
   private void arcananovum$inventoryChanged(AbstractContainerMenu handler, int slotId, ItemStack stack, CallbackInfo ci){
      InventoryChangedCallback.onSlotUpdate(this$0, this$0.getInventory(), stack);
   }
}
