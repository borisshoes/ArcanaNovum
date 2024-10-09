package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
   
   @Shadow
   public ServerPlayerEntity player;
   
   @Inject(method = "onHandSwing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"))
   private void arcananovum_handSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
      ItemStack bow = player.getStackInHand(Hand.MAIN_HAND);
      boolean arbalest = (ArcanaItemUtils.identifyItem(bow) instanceof AlchemicalArbalest);
      boolean crossbow = bow.isOf(Items.CROSSBOW) || arbalest;
      boolean runic = (ArcanaItemUtils.identifyItem(bow) instanceof RunicBow) || (arbalest && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1);
      if(!bow.isOf(Items.BOW) && !runic && !crossbow) return;
      
      // Check for and rotate arrow types in quivers
      PlayerInventory inv = player.getInventory();
      
      // Switch to next arrow slot if quiver is found
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof RunicQuiver || arcanaItem instanceof OverflowingQuiver){
            // Quiver found allow switching
            IArcanaProfileComponent profile = PLAYER_DATA.get(player);
            
            int cooldown = ((NbtInt)profile.getMiscData(QuiverItem.QUIVER_CD_TAG)).intValue();
            if(cooldown <= 0){
               QuiverItem.switchArrowOption(player,runic,true);
               profile.addMiscData(QuiverItem.QUIVER_CD_TAG,NbtInt.of(3));
            }
            
            return;
         }
      }
   }
   
   @Inject(method = "requestTeleport(Lnet/minecraft/entity/player/PlayerPosition;Ljava/util/Set;)V", at = @At("HEAD"), cancellable = true)
   private void arcananovum_ensnarementPlayerTeleport(PlayerPosition pos, Set<PositionFlag> flags, CallbackInfo ci) {
      StatusEffectInstance effect = player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null && effect.getAmplifier() > 0){
         player.sendMessage(Text.literal("Your teleport has been ensnared!").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
         ci.cancel();
      }
   }
   
   @Inject(method = "onPlayerMove", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"))
   private void arcananovum_ensnarementPlayerTest(PlayerMoveC2SPacket packet, CallbackInfo ci) {
      StatusEffectInstance effect = player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         // TODO: This needs to be fixed
//         player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(player.getX(),player.getY(),player.getZ(),0,0,PositionFlag.getFlags(0b11000),0));
         //player.networkHandler.sendPacket(new PlayerPositionLookS2CPacket(player.getX(),player.getY(),player.getZ(),player.getYaw(),player.getPitch(),PositionFlag.getFlags(0b111),0));
      }
   }
   
   @ModifyVariable(method="onPlayerMove",at=@At("STORE"), ordinal = 0)
   private double arcananovum_ensnarementPlayerX(double x){
      if(player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getX();
      }else{
         return x;
      }
      
   }
   
   @ModifyVariable(method="onPlayerMove",at=@At("STORE"), ordinal = 1)
   private double arcananovum_ensnarementPlayerY(double y){
      if(player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getY();
      }else{
         return y;
      }
   }
   
   @ModifyVariable(method="onPlayerMove",at=@At("STORE"), ordinal = 2)
   private double arcananovum_ensnarementPlayerZ(double z){
      if(player.getStatusEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getZ();
      }else{
         return z;
      }
   }
   
}
