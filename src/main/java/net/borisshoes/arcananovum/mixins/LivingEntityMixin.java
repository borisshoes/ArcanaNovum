package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   
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
            SoundUtils.playSongToPlayer(player,SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1.8f);
         }
         entity.setAbsorptionAmount((curAbs + addedAbs));
      }
   }
   
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
   private void playerDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      if(entity instanceof ServerPlayerEntity player){
         PlayerInventory inv = player.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack item = inv.getStack(i);
            if(item.isEmpty()){
               continue;
            }
      
            boolean isMagic = MagicItemUtils.isMagic(item);
            if(!isMagic)
               continue; // Item not magic, skip
      
            // Cancel all Pearls of Recall
            if(MagicItemUtils.identifyItem(item) instanceof PearlOfRecall pearl){
               NbtCompound itemNbt = item.getNbt();
               NbtCompound magicNbt = itemNbt.getCompound("arcananovum");
               if(magicNbt.getInt("heat") > 0){
                  player.sendMessage(new LiteralText("Your Recall Has Been Disrupted!").formatted(Formatting.RED,Formatting.ITALIC),true);
                  magicNbt.putInt("heat", -1);
               }
            }
         }
   
         // Stall Levitation Harness
         ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
         if(MagicItemUtils.isMagic(chestItem) && player.getAbilities().flying){
            if(MagicItemUtils.identifyItem(chestItem) instanceof LevitationHarness harness){
               harness.setStall(chestItem,10);
               player.sendMessage(new LiteralText("Your Harness Stalls!").formatted(Formatting.YELLOW,Formatting.ITALIC),true);
               SoundUtils.playSound(player.getWorld(),player.getBlockPos(),SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS,1, 0.7f);
               ParticleEffectUtils.harnessStall(player.getWorld(),player.getPos().add(0,0.5,0));
            }
         }
      }
   }
   
   @Inject(method="damage",at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"))
   private void damageDealt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
      LivingEntity entity = (LivingEntity) (Object) this;
      Entity attacker = source.getAttacker();
      if(attacker instanceof ServerPlayerEntity player){
         ItemStack weapon = player.getEquippedStack(EquipmentSlot.MAINHAND);
   
         if(MagicItemUtils.identifyItem(weapon) instanceof ShadowStalkersGlaive glaive){
            int oldEnergy = glaive.getEnergy(weapon);
            glaive.addEnergy(weapon, (int) amount);
            int newEnergy = glaive.getEnergy(weapon);
            if(oldEnergy/20 != newEnergy/20){
               String message = "Glaive Charges: ";
               for(int i=1; i<=5; i++){
                  message += newEnergy >= i*20 ? "✦ " : "✧ ";
               }
               player.sendMessage(new LiteralText(message).formatted(Formatting.BLACK),true);
            }
         }
      }
   }
   
   
   // Mixin for damage mitigation (Wings of Zephyr, Charm of Felidae
   @Inject(method = "applyEnchantmentsToDamage", at = @At("RETURN"), cancellable = true)
   private void kineticDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir){
      float reduced = cir.getReturnValueF();
      float newReturn = reduced;
      LivingEntity entity = (LivingEntity) (Object) this;
      if(source.equals(DamageSource.FALL) || source.equals(DamageSource.FLY_INTO_WALL)){
         ItemStack chestItem = entity.getEquippedStack(EquipmentSlot.CHEST);
         if(MagicItemUtils.isMagic(chestItem)){
            if(MagicItemUtils.identifyItem(chestItem) instanceof WingsOfZephyr wings){
               int energy = wings.getEnergy(chestItem);
               double maxDmgReduction = reduced*.5;
               double dmgReduction = Math.min(energy/100.0,maxDmgReduction);
               if(entity instanceof ServerPlayerEntity player){
                  if(dmgReduction == maxDmgReduction || dmgReduction > 12){
                     player.sendMessage(new LiteralText("Your Armored Wings cushion your fall!").formatted(Formatting.GRAY,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ENDER_DRAGON_FLAP, 1,1.3f);
                     Timer timer = new Timer();
                     timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                           player.sendMessage(new LiteralText("Wing Energy Remaining: "+wings.getEnergy(chestItem)).formatted(Formatting.GRAY),true);
                        }
                     }, 2500);
                  }
                  PLAYER_DATA.get(player).addXP((int)dmgReduction*25); // Add xp
               }
               wings.addEnergy(chestItem,(int)-dmgReduction*100);
               newReturn = (float) (reduced - dmgReduction);
            }
         }
         
         // Felidae Charm
         if(entity instanceof ServerPlayerEntity player && source.equals(DamageSource.FALL)){
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty()){
                  continue;
               }
      
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
      
               if(MagicItemUtils.identifyItem(item) instanceof FelidaeCharm){
                  SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_CAT_PURREOW, 1,1);
                  float oldReturn = newReturn;
                  newReturn = newReturn/2 < 2 ? 0 : newReturn / 2; // Half the damage, if the remaining damage is less than a heart, remove all of it.
                  PLAYER_DATA.get(player).addXP(10*(int)(oldReturn-newReturn)); // Add xp
                  break; // Make it so multiple charms don't stack
               }
            }
         }
      }
      cir.setReturnValue(newReturn);
   }
   
   
   /*@Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"))
   public void swingHand(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
      LivingEntity entity = (LivingEntity) (Object) this;
      System.out.println("This is a left click?");
      if (!entity.world.isClient) {
         if(entity instanceof ServerPlayerEntity player && entity.world instanceof ServerWorld world){
            LeftClickEvent.EVENT.invoker().onPlayerLeftClick(player,world,hand);
         }
      }
   }*/
}
