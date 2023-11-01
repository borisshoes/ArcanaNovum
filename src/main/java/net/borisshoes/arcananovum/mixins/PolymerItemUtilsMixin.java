package net.borisshoes.arcananovum.mixins;

import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static eu.pb4.polymer.core.api.item.PolymerItemUtils.getPolymerItemStack;

@Mixin(PolymerItemUtils.class)
public abstract class PolymerItemUtilsMixin {
   
   @Shadow
   private static boolean shouldPolymerConvert(ItemStack itemStack, @Nullable ServerPlayerEntity player){
      throw new LinkageError();
   }
   
   @Inject(method= "createItemStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/item/TooltipContext;Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/item/ItemStack;"
         , locals = LocalCapture.CAPTURE_FAILHARD , at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getNbt()Lnet/minecraft/nbt/NbtCompound;", ordinal = 16, shift = At.Shift.BEFORE))
   private static void arcananovum_shulkerPreviewFix(ItemStack itemStack, TooltipContext tooltipContext, ServerPlayerEntity player, CallbackInfoReturnable<ItemStack> cir, Item item, int cmd, int color, ItemStack out, NbtList lore){
      try{
         if (itemStack.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
            var bet = itemStack.getNbt().getCompound("BlockEntityTag");
            if(bet.contains("Items", NbtElement.LIST_TYPE)){
               var outList = new NbtList();
               
               for (var itemNbt : bet.getList("Items", NbtElement.COMPOUND_TYPE)) {
                  var base = new NbtCompound();
                  var slot = ((NbtCompound) itemNbt).get("Slot");
                  if (slot != null) {
                     base.put("Slot", slot);
                  }
                  outList.add(getPolymerItemStack(ItemStack.fromNbt((NbtCompound) itemNbt), tooltipContext, player).writeNbt(base));
               }
               
               out.getNbt().getCompound("BlockEntityTag").put("Items", outList);
            }
         }
      } catch (Throwable e) {
         if (PolymerImpl.LOG_MORE_ERRORS) {
            e.printStackTrace();
         }
      }
   }
   
   @Inject(method= "shouldPolymerConvert", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getNbt()Lnet/minecraft/nbt/NbtCompound;", ordinal = 2, shift = At.Shift.BEFORE), cancellable = true)
   private static void arcananovum_shouldConvertShulker(ItemStack itemStack, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir){
      if (itemStack.getNbt().contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
         var bet = itemStack.getNbt().getCompound("BlockEntityTag");
         if(bet.contains("Items", NbtElement.LIST_TYPE)){
            for (var itemNbt : bet.getList("Items", NbtElement.COMPOUND_TYPE)) {
               if (shouldPolymerConvert(ItemStack.fromNbt((NbtCompound) itemNbt), player)) {
                  cir.setReturnValue(true);
                  return;
               }
            }
         }
      }
   }
}
