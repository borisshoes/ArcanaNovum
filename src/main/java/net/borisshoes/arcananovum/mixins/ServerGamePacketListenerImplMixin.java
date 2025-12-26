package net.borisshoes.arcananovum.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
   
   @Shadow
   public ServerPlayer player;
   
   @Shadow
   private int awaitingTeleport;
   
   @Shadow
   private Vec3 awaitingPositionFromClient;
   
   @ModifyExpressionValue(method = "tryPickItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"))
   private int arcananovum$greavesPickblock(int original, ItemStack stack){
      if(original == -1){
         ItemStack pants = player.getItemBySlot(EquipmentSlot.LEGS);
         if(!(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus greaves) || !ArcanaItem.getBooleanProperty(pants,ArcanaItem.ACTIVE_TAG))
            return original;
         ItemStack refillStack = greaves.getStackOf(pants, stack);
         Inventory inv = player.getInventory();
         int emptySlot = inv.getFreeSlot();
         if(!refillStack.isEmpty() && emptySlot != -1){
            int amtToRefill = Math.min((int) (stack.getMaxStackSize() * 0.67), refillStack.getCount());
            ItemStack insertStack = refillStack.split(amtToRefill);
            inv.add(emptySlot,insertStack);
            player.inventoryMenu.broadcastChanges();
            greaves.buildItemLore(pants, BorisLib.SERVER);
            return emptySlot;
         }
      }
      return original;
   }
   
   @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0))
   private void arcananovum$onHandSwap(ServerboundPlayerActionPacket packet, CallbackInfo ci){
      ItemStack offHand = player.getOffhandItem();
      if(BinaryBlades.isFakeBlade(offHand)){
         player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
         ArcanaNovum.data(player).restoreOffhand();
      }
   }
   
   @Inject(method = "handleAcceptPlayerLoad", at = @At("TAIL"))
   private void arcananovum$onPlayerLoad(ServerboundPlayerLoadedPacket packet, CallbackInfo ci){
      for(UUID uuid : ArcanaNovum.TOTEM_KILL_LIST){
         if(uuid.equals(player.getUUID())){
            player.hurtServer(player.level(), ArcanaDamageTypes.of(player.level(),ArcanaDamageTypes.VENGEANCE_TOTEM,player), player.getMaxHealth()*10);
            break;
         }
      }
      ArcanaNovum.TOTEM_KILL_LIST.removeIf(uuid -> uuid.equals(player.getUUID()));
   }
   
   @Inject(method = "handleAnimate", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"))
   private void arcananovum$handSwing(ServerboundSwingPacket packet, CallbackInfo ci){
      ServerGamePacketListenerImpl networkHandler = (ServerGamePacketListenerImpl) (Object) this;
      
      // Hit through Greater Invisibility
      double range = player.entityInteractionRange();
      Vec3 startPos = player.getEyePosition();
      Vec3 view = player.getForward();
      Vec3 rayEnd = startPos.add(view.scale(range));
      AABB box = player.getBoundingBox().expandTowards(view.scale(range)).inflate(1.0, 1.0, 1.0);
      EntityHitResult hitEntity = ProjectileUtil.getEntityHitResult(player,startPos,rayEnd,box, e -> e instanceof LivingEntity living && !e.isSpectator() && living.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT),range);
      
      if(hitEntity != null && hitEntity.getEntity() != null){
         networkHandler.handleInteract(ServerboundInteractPacket.createAttackPacket(hitEntity.getEntity(),player.isShiftKeyDown()));
      }
      
      // Quiver arrow swap
      ItemStack bow = player.getItemInHand(InteractionHand.MAIN_HAND);
      boolean arbalest = (ArcanaItemUtils.identifyItem(bow) instanceof AlchemicalArbalest);
      boolean crossbow = bow.is(Items.CROSSBOW) || arbalest;
      boolean runic = (ArcanaItemUtils.identifyItem(bow) instanceof RunicBow) || (arbalest && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1);
      if(!bow.is(Items.BOW) && !runic && !crossbow) return;
      
      // Check for and rotate arrow types in quivers
      Inventory inv = player.getInventory();
      
      // Switch to next arrow slot if quiver is found
      for(int i = 0; i<inv.getContainerSize(); i++){
         ItemStack item = inv.getItem(i);
         if(item.isEmpty()){
            continue;
         }
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
         if(arcanaItem instanceof RunicQuiver || arcanaItem instanceof OverflowingQuiver){
            // Quiver found allow switching
            IArcanaProfileComponent profile = ArcanaNovum.data(player);
            
            int cooldown = ((IntTag)profile.getMiscData(QuiverItem.QUIVER_CD_TAG)).intValue();
            if(cooldown <= 0){
               QuiverItem.switchArrowOption(player,runic,true);
               profile.addMiscData(QuiverItem.QUIVER_CD_TAG, IntTag.valueOf(3));
            }
            
            return;
         }
      }
   }
   
   @Inject(method = "teleport(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;)V", at = @At("HEAD"), cancellable = true)
   private void arcananovum$ensnarementPlayerTeleport(PositionMoveRotation pos, Set<Relative> flags, CallbackInfo ci){
      MobEffectInstance effect = player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null && effect.getAmplifier() > 0){
         player.displayClientMessage(Component.literal("Your teleport has been ensnared!").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ILLUSIONER_CAST_SPELL,2,.1f);
         ci.cancel();
      }
   }
   
   @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
   private void arcananovum$ensnarementPlayerOnMove(ServerboundMovePlayerPacket packet, CallbackInfo ci){
      MobEffectInstance effect = player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT);
      if(effect != null){
         if (++awaitingTeleport == Integer.MAX_VALUE) {
            awaitingTeleport = 0;
         }
         awaitingPositionFromClient = player.position();
         player.connection.send(new ClientboundPlayerPositionPacket(awaitingTeleport,new PositionMoveRotation(player.position(), Vec3.ZERO,0,0), Relative.unpack(0b11000)));
      }else{
         ItemStack pants = player.getItemBySlot(EquipmentSlot.LEGS);
         if(ArcanaItemUtils.identifyItem(pants) instanceof GreavesOfGaialtus && ArcanaAugments.getAugmentOnItem(pants,ArcanaAugments.EARTHEN_ASCENT) >= 1){
            if(packet.horizontalCollision() && !player.getAbilities().flying && player.isShiftKeyDown()){
               player.setDeltaMovement(new Vec3(player.getDeltaMovement().x(),0.2,player.getDeltaMovement().z()));
               player.connection.send(new ClientboundSetEntityMotionPacket(player));
               player.connection.aboveGroundTickCount = 0;
            }
         }
      }
   }
   
   @ModifyVariable(method= "handleMovePlayer",at=@At("STORE"), ordinal = 0)
   private double arcananovum$ensnarementPlayerX(double x){
      if(player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getX();
      }else{
         return x;
      }
      
   }
   
   @ModifyVariable(method= "handleMovePlayer",at=@At("STORE"), ordinal = 1)
   private double arcananovum$ensnarementPlayerY(double y){
      if(player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getY();
      }else{
         return y;
      }
   }
   
   @ModifyVariable(method= "handleMovePlayer",at=@At("STORE"), ordinal = 2)
   private double arcananovum$ensnarementPlayerZ(double z){
      if(player.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT) != null){
         return player.getZ();
      }else{
         return z;
      }
   }
   
}
