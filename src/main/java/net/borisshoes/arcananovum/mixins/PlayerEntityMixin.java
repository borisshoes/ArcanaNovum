package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z",shift = At.Shift.BEFORE))
   private void arcananovum_onGroundMove(double dx, double dy, double dz, CallbackInfo ci){
      PlayerEntity playerEntity = (PlayerEntity) (Object) this;
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack bootsItem = player.getEquippedStack(EquipmentSlot.FEET);
         if(MagicItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
            boots.attemptStepAssist(bootsItem,player, new Vec3d(dx,dy,dz));
            if(player.isSprinting()){
               int i = Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0f);
               ArcanaAchievements.progress(player, ArcanaAchievements.PHEIDIPPIDES.id, i);
            }
         }
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;getProjectiles()Ljava/util/function/Predicate;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum_quiverCheck(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      boolean runicBow = (MagicItemUtils.identifyItem(bow) instanceof RunicBow);
      boolean runicArbalest = bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem()) && ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1;
      if(!bow.isOf(Items.BOW) && !runicBow && !bow.isOf(Items.CROSSBOW) && !bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())) return;
      boolean runic = runicBow || runicArbalest;
      
      if(player instanceof ServerPlayerEntity serverPlayer){
         ItemStack arrowStack = QuiverItem.getArrowStack(serverPlayer,runic,false);
         Pair<String,Integer> option = QuiverItem.getArrowOption(serverPlayer,runic);
         if(arrowStack != null && option != null){
            ItemStack returnStack = arrowStack.copy();
            NbtCompound tag = returnStack.getOrCreateNbt();
            tag.putInt("QuiverSlot",option.getRight());
            tag.putString("QuiverId",option.getLeft());
            
            cir.setReturnValue(returnStack);
         }else if(runicArbalest){
            Predicate<ItemStack> predicate = ((RangedWeaponItem)bow.getItem()).getProjectiles();
            for (int i = 0; i < player.getInventory().size(); ++i) {
               ItemStack itemStack2 = player.getInventory().getStack(i);
               if (predicate.test(itemStack2) || MagicItemUtils.isRunicArrow(itemStack2)) cir.setReturnValue(itemStack2);
            }
         }
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;getHeldProjectile(Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_everlastingRocketCheck(ItemStack stack, CallbackInfoReturnable<ItemStack> cir, Predicate<ItemStack> predicate){
      PlayerEntity player = (PlayerEntity) (Object) this;
      ItemStack main = player.getStackInHand(Hand.MAIN_HAND);
      ItemStack off = player.getStackInHand(Hand.OFF_HAND);
      if(MagicItemUtils.identifyItem(main) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(main);
         if(rocket.getEnergy(main) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }else if(MagicItemUtils.identifyItem(off) instanceof EverlastingRocket rocket){
         ItemStack fireworkStack = rocket.getFireworkStack(off);
         if(rocket.getEnergy(off) > 0 && predicate.test(fireworkStack)) cir.setReturnValue(fireworkStack);
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value="RETURN"), cancellable = true)
   private void arcananovum_stopRunicUsage(ItemStack bow, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      if(!bow.isOf(Items.BOW) || bow.isOf(Items.CROSSBOW) || bow.isOf(ArcanaRegistry.ALCHEMICAL_ARBALEST.getItem())) return;
      boolean runicArbalest = ArcanaAugments.getAugmentOnItem(bow,ArcanaAugments.RUNIC_ARBALEST.id) >= 1;
      ItemStack curReturn = cir.getReturnValue();
      if(MagicItemUtils.isRunicArrow(curReturn) && !runicArbalest){
         cir.setReturnValue(player.isCreative() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY);
      }
   }
   
   @Redirect(method="checkFallFlying", at=@At(value="INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
   private boolean arcananovum_elytraTick(ItemStack stack, Item item){
      return stack.isOf(item) || MagicItemUtils.identifyItem(stack) instanceof WingsOfEnderia;
   }
}
