package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.FractalSponge;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
   
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;onItemEntityDestroyed(Lnet/minecraft/entity/ItemEntity;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_onItemStackDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getStack();
      try{
         if(source.isFire()){
            if(MagicItemUtils.identifyItem(stack) instanceof FractalSponge sponge){
               String uuid = sponge.getCrafter(stack);
               ServerPlayerEntity player = itemEntity.getServer().getPlayerManager().getPlayer(UUID.fromString(uuid));
               if(player != null){
                  ArcanaAchievements.grant(player,"burning_despair");
               }
            }
         }
      }catch(Exception e){
         Arcananovum.log(2,"Error in Arcana Novum ItemEntity Mixin");
         e.printStackTrace();
      }
   }
   
   @Inject(method="isFireImmune",at=@At(value="RETURN"),cancellable = true)
   private void arcananovum_fireImmuneItems(CallbackInfoReturnable<Boolean> cir){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getStack();
      if(MagicItemUtils.identifyItem(stack) instanceof FractalSponge sponge){
         boolean fireRes = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,"heat_treatment")) >= 1;
         if(fireRes){
            cir.setReturnValue(true);
         }
      }
   }
}
