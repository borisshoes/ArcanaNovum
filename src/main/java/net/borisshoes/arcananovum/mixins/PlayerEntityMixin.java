package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.items.OverflowingQuiver;
import net.borisshoes.arcananovum.items.PearlOfRecall;
import net.borisshoes.arcananovum.items.RunicBow;
import net.borisshoes.arcananovum.items.RunicQuiver;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
   
   @Inject(method = "getArrowType", at = @At(value="INVOKE",target="Lnet/minecraft/item/RangedWeaponItem;getProjectiles()Ljava/util/function/Predicate;", shift = At.Shift.BEFORE), cancellable = true)
   private void arcananovum_quiverCheck(ItemStack stack, CallbackInfoReturnable<ItemStack> cir){
      PlayerEntity player = (PlayerEntity) (Object) this;
      // stack = bow
      ItemStack bow = player.getStackInHand(Hand.MAIN_HAND);
      if(!bow.isOf(Items.BOW)) return;
      boolean runic = (MagicItemUtils.identifyItem(bow) instanceof RunicBow);
   
      PlayerInventory inv = player.getInventory();
   
      // Switch to next arrow slot, runic quivers get priority
      if(runic){
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
   
            if(MagicItemUtils.identifyItem(item) instanceof RunicQuiver quiver){
               ItemStack arrow = quiver.getArrow(item);
               if(arrow != null){
                  cir.setReturnValue(arrow);
                  cir.cancel();
                  return;
               }
            }
         }
      }
      
      for(int i=0; i<inv.size();i++){
         ItemStack item = inv.getStack(i);
         if(item.isEmpty()){
            continue;
         }
   
         if(MagicItemUtils.identifyItem(item) instanceof OverflowingQuiver quiver){
            ItemStack arrow = quiver.getArrow(item);
            if(arrow != null){
               cir.setReturnValue(arrow);
               cir.cancel();
               return;
            };
         }
      }
      
   }
}