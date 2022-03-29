package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.items.MagicItem;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.items.WingsOfZephyr;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.Utils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Timer;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
   
   // Mixin for Shield of Fortitude giving absorption hearts
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
   private void shieldAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
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
   
   
   // Mixin for Wings of Zephyr damage mitigation
   @Inject(method = "applyEnchantmentsToDamage", at = @At("RETURN"), cancellable = true)
   private void wingsFallDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir){
      float reduced = cir.getReturnValueF();
      LivingEntity entity = (LivingEntity) (Object) this;
      if(source.equals(DamageSource.FALL) || source.equals(DamageSource.FLY_INTO_WALL)){
         ItemStack item = entity.getEquippedStack(EquipmentSlot.CHEST);
         if(MagicItemUtils.isMagic(item)){
            if(MagicItemUtils.identifyItem(item) instanceof WingsOfZephyr){
               WingsOfZephyr wings = (WingsOfZephyr) MagicItemUtils.identifyItem(item);
               int energy = wings.getEnergy(item);
               double maxDmgReduction = reduced*.5;
               double dmgReduction = Math.min(energy/100.0,maxDmgReduction);
               if(entity instanceof ServerPlayerEntity player){
                  if(dmgReduction == maxDmgReduction || dmgReduction > 12){
                     player.sendMessage(new LiteralText("Your Armored Wings cushion your fall!").formatted(Formatting.GRAY,Formatting.ITALIC),true);
                     Utils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1,1.3f);
                     Timer timer = new Timer();
                     timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                           player.sendMessage(new LiteralText("Wing Energy Remaining: "+wings.getEnergy(item)).formatted(Formatting.GRAY),true);
                        }
                     }, 2500);
                  }
                  PLAYER_DATA.get(player).addXP((int)dmgReduction*25); // Add xp
               }
               wings.addEnergy(item,(int)-dmgReduction*100);
               cir.setReturnValue((float) (reduced - dmgReduction));
            }
         }
      }
   }
}
