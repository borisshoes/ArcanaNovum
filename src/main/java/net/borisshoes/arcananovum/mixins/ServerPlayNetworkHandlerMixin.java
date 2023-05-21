package net.borisshoes.arcananovum.mixins;

import eu.pb4.sgui.virtual.hotbar.HotbarScreenHandler;
import net.borisshoes.arcananovum.items.OverflowingQuiver;
import net.borisshoes.arcananovum.items.RunicBow;
import net.borisshoes.arcananovum.items.RunicQuiver;
import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
   
   @Shadow
   public ServerPlayerEntity player;
   
   @Inject(method = "onHandSwing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
   private void arcananovum_handSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
      ItemStack bow = player.getStackInHand(Hand.MAIN_HAND);
      if(!bow.isOf(Items.BOW)) return;
      boolean runic = (MagicItemUtils.identifyItem(bow) instanceof RunicBow);
      
      // Check for and rotate arrow types in quivers
      PlayerInventory inv = player.getInventory();
      
      // Switch to next arrow slot if quiver is found
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
      
         if(MagicItemUtils.identifyItem(item) instanceof RunicQuiver && runic){
            // Quiver found allow switching
            QuiverItem.switchArrowOption(player,runic);
            return;
         }else if(MagicItemUtils.identifyItem(item) instanceof OverflowingQuiver){
            // Quiver found allow switching
            QuiverItem.switchArrowOption(player,runic);
            return;
         }
      }
   }
}
