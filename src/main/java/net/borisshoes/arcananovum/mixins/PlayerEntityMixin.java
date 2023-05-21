package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
   
   @Inject(method="increaseTravelMotionStats", at = @At(value="INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V", ordinal = 4))
   private void arcananovum_sprintStat(double dx, double dy, double dz, CallbackInfo ci){
      PlayerEntity playerEntity = (PlayerEntity) (Object) this;
      if(playerEntity instanceof ServerPlayerEntity player){
         ItemStack bootsItem = player.getEquippedStack(EquipmentSlot.FEET);
         if(MagicItemUtils.identifyItem(bootsItem) instanceof SojournerBoots boots){
            if(boots.getEnergy(bootsItem) == boots.getMaxEnergy(bootsItem)){
               ArcanaAchievements.progress(player, "pheidippides", Math.round((float)Math.sqrt(dx * dx + dz * dz) * 100.0F));
            }
         }
      }
   }
   
   @Inject(method = "getProjectileType", at = @At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;getProjectiles()Ljava/util/function/Predicate;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum_quiverCheck(ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      // stack = bow
      ItemStack bow = player.getStackInHand(Hand.MAIN_HAND);
      if(!bow.isOf(Items.BOW)) return;
      boolean runic = (MagicItemUtils.identifyItem(bow) instanceof RunicBow);
      
      if(player instanceof ServerPlayerEntity serverPlayer){
         ItemStack arrowStack = QuiverItem.getArrowStack(serverPlayer,runic,false);
         Pair<String,Integer> option = QuiverItem.getArrowOption(serverPlayer,runic);
         if(arrowStack != null && option != null){
            ItemStack returnStack = arrowStack.copy();
            NbtCompound tag = returnStack.getOrCreateNbt();
            tag.putInt("QuiverSlot",option.getRight());
            tag.putString("QuiverId",option.getLeft());
            
            cir.setReturnValue(returnStack);
            cir.cancel();
         }
      }
      
   }
}
