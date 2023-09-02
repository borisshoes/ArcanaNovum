package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
   
   @Shadow
   public ServerPlayerEntity player;
   
   @Inject(method = "onHandSwing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
   private void arcananovum_handSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
      ItemStack bow = player.getStackInHand(Hand.MAIN_HAND);
      boolean arbalest = (MagicItemUtils.identifyItem(bow) instanceof AlchemicalArbalest);
      boolean crossbow = bow.isOf(Items.CROSSBOW) || arbalest;
      boolean runic = (MagicItemUtils.identifyItem(bow) instanceof RunicBow) || (arbalest && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1);
      if(!bow.isOf(Items.BOW) && !runic && !crossbow) return;
      
      // Check for and rotate arrow types in quivers
      PlayerInventory inv = player.getInventory();
      
      // Switch to next arrow slot if quiver is found
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         MagicItem magicItem = MagicItemUtils.identifyItem(item);
         if(magicItem instanceof RunicQuiver || magicItem instanceof OverflowingQuiver){
            // Quiver found allow switching
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            
            int cooldown = ((NbtInt)profile.getMiscData("quiverCD")).intValue();
            if(cooldown <= 0){
               QuiverItem.switchArrowOption(player,runic);
               profile.addMiscData("quiverCD",NbtInt.of(3));
            }
            
            return;
         }
      }
   }
}
