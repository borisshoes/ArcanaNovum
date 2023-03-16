package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.items.arrows.ArcaneFlakArrows;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.items.arrows.TetherArrows;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.QuiverItem;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.items.RunicBow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

@Mixin(BowItem.class)
public class BowMixin {
   @Inject(method="onStoppedUsing",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_bowFired(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity){
      // itemStack = arrow | stack = bow
      
      if(MagicItemUtils.isMagic(stack) && MagicItemUtils.isMagic(itemStack)){
         MagicItem magicBow = MagicItemUtils.identifyItem(stack);
         MagicItem magicArrow = MagicItemUtils.identifyItem(itemStack);
         if(magicBow instanceof RunicBow && magicArrow instanceof RunicArrow){
            NbtCompound magicTag = itemStack.getNbt().getCompound("arcananovum");
            
            if(playerEntity instanceof ServerPlayerEntity player) ArcanaAchievements.progress(player,"just_like_archer",1);
            if(magicArrow instanceof PhotonicArrows photonArrows){
               int alignmentLvl = 0;
               if(magicTag.contains("augments")){
                  alignmentLvl = Math.max(0, ArcanaAugments.getAugmentFromCompound(magicTag,"prismatic_alignment"));
               }
               photonArrows.shoot(world,user,persistentProjectileEntity,alignmentLvl);
               persistentProjectileEntity.kill();
               SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.BLOCK_AMETHYST_BLOCK_HIT,SoundCategory.PLAYERS,1.2f,1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               PLAYER_DATA.get(playerEntity).addXP(50);
               return;
            }
            
            NbtCompound arrowData = new NbtCompound();
            arrowData.putString("UUID", magicArrow.getUUID(itemStack));
            arrowData.putString("owner", playerEntity.getUuidAsString());
            arrowData.putString("id",magicArrow.getId());
            if(magicTag.contains("augments")) arrowData.put("augments",magicTag.getCompound("augments"));
            putCustomArrowData(arrowData,persistentProjectileEntity,magicArrow);
            MagicEntity arrowEntity = new MagicEntity(persistentProjectileEntity.getUuidAsString(), arrowData);
            MAGIC_ENTITY_LIST.get(world).addEntity(arrowEntity);
   
            SoundUtils.playSound(world,playerEntity.getBlockPos(),SoundEvents.ITEM_TRIDENT_THROW,SoundCategory.PLAYERS,0.8f,1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
            PLAYER_DATA.get(playerEntity).addXP(50);
            
            //System.out.println("Firing magic bow with magic arrow ("+magicArrow.getId()+"): "+persistentProjectileEntity.getUuidAsString());
         }
      }
   }
   
   private void putCustomArrowData(NbtCompound arrowData, PersistentProjectileEntity arrow, MagicItem magicArrow){
      if(magicArrow instanceof ArcaneFlakArrows){
         arrowData.putInt("armTime",ArcaneFlakArrows.armTime);
      }else if(magicArrow instanceof TetherArrows){
         arrowData.putBoolean("severed",false);
      }
   }
   
   @Inject(method="onStoppedUsing",at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
   private void arcananovum_decreaseQuiver(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2){
      NbtCompound tag = itemStack.getNbt();
      if(tag == null || !(playerEntity instanceof ServerPlayerEntity player)) return;
      if(tag.contains("QuiverSlot") && tag.contains("QuiverId")){
         String quiverId = tag.getString("QuiverId");
         int slot = tag.getInt("QuiverSlot");
         
         PlayerInventory inv = playerEntity.getInventory();
         for(int invSlot = 0; invSlot<inv.size(); invSlot++){
            ItemStack item = inv.getStack(invSlot);
            if(item.isEmpty()){
               continue;
            }
            
            MagicItem magicItem = MagicItemUtils.identifyItem(item);
            if(magicItem instanceof QuiverItem quiver){
               if(quiver.getUUID(item).equals(quiverId)){
                  quiver.shootArrow(item,slot,player,stack);
                  return;
               }
            }
         }
      }
   }
   
   @Redirect(method="onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
   private boolean arcananovum_enhancedInfinity(ItemStack arrows, Item item, ItemStack bow, World world, LivingEntity user, int remainingUseTicks){
      if(MagicItemUtils.isMagic(bow)){
         MagicItem magicBow = MagicItemUtils.identifyItem(bow);
         boolean runicArrows = MagicItemUtils.identifyItem(arrows) instanceof RunicArrow;
         if(magicBow instanceof RunicBow){
            boolean enhanced = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,"enhanced_infinity")) >= 1;
            if(enhanced){
               return arrows.isOf(item) || arrows.isOf(Items.SPECTRAL_ARROW) || (arrows.isOf(Items.TIPPED_ARROW) && !runicArrows);
            }
         }
      }
      return arrows.isOf(item);
   }
   
   @Redirect(method="onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V"))
   private void arcananovum_arrowVelocity(PersistentProjectileEntity arrowEntity, Entity player, float pitch, float yaw, float roll, float speed, float divergence, ItemStack bow, World world, LivingEntity user, int remainingUseTicks){
      float newDiv = divergence;
      if(MagicItemUtils.isMagic(bow)){
         MagicItem magicBow = MagicItemUtils.identifyItem(bow);
         if(magicBow instanceof RunicBow){
            int stableLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,"bow_stabilization"));
            final float[] stability = {1f,.75f,.5f,0f};
            newDiv *= stability[stableLvl];
         }
      }
      arrowEntity.setVelocity(player,pitch,yaw,roll,speed,newDiv);
   }
   
   @Redirect(method="onStoppedUsing", at=@At(value = "INVOKE",target="Lnet/minecraft/item/BowItem;getPullProgress(I)F"))
   private float arcananovum_bowAcceleration(int useTicks, ItemStack bow, World world, LivingEntity user, int remainingUseTicks){
      float maxPullTicks = 20f;
      if(MagicItemUtils.isMagic(bow)){
         MagicItem magicBow = MagicItemUtils.identifyItem(bow);
         if(magicBow instanceof RunicBow){
            int accelLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(bow,"bow_acceleration"));
            final float[] accel = {20,18,17,16,15,10};
            maxPullTicks = accel[accelLvl];
         }
      }
      
      float f = (float)useTicks / maxPullTicks;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }
   
      return f;
   }
}
