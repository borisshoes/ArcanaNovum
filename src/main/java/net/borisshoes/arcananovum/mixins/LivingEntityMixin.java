package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
   
   // Mixin for Shield of Fortitude giving absorption hearts
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
   private void damage1(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      ItemStack main = entity.getEquippedStack(EquipmentSlot.MAINHAND);
      ItemStack off = entity.getEquippedStack(EquipmentSlot.OFFHAND);
      MagicItem magic;
      ItemStack item = null;
      if(MagicItemUtils.isMagic(main)){
         magic = MagicItemUtils.identifyItem(main);
         item = main;
      }else if(MagicItemUtils.isMagic(off) && main.getItem() != Items.SHIELD){
         magic = MagicItemUtils.identifyItem(off);
         item = off;
      }else{
         return;
      }
      if(magic instanceof ShieldOfFortitude shield){
         float curAbs = entity.getAbsorptionAmount();
         float addedAbs = (float) Math.min(10,amount*.5);
         if(entity instanceof ServerPlayerEntity player){
            Arcananovum.addTickTimerCallback(new ShieldTimerCallback(200,item,player,addedAbs));
            Utils.playSongToPlayer(player,SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
         }
         entity.setAbsorptionAmount((curAbs + addedAbs));
      }
   }
   
}
