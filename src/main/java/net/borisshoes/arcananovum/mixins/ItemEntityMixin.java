package net.borisshoes.arcananovum.mixins;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.FractalSponge;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
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
      MagicItem magicItem = MagicItemUtils.identifyItem(stack);
      if(magicItem instanceof FractalSponge sponge){
         boolean fireRes = Math.max(0, ArcanaAugments.getAugmentOnItem(stack,"heat_treatment")) >= 1;
         if(fireRes){
            cir.setReturnValue(true);
         }
      }else if(magicItem instanceof CindersCharm){
         cir.setReturnValue(true);
      }
   }
   
   @Inject(method="tick",at=@At("TAIL"))
   private void arcananovum_makeCryingObsidian(CallbackInfo ci){
      ItemEntity itemEntity = (ItemEntity) (Object) this;
      ItemStack stack = itemEntity.getStack();
      World world = itemEntity.getWorld();
   
      if(!MagicItemUtils.isMagic(stack) && stack.isOf(Items.OBSIDIAN) && itemEntity.isTouchingWater()){
         List<ItemEntity> otherEntities = world.getEntitiesByType(EntityType.ITEM,itemEntity.getBoundingBox().expand(1.25),e -> (!e.getUuid().equals(itemEntity.getUuid()) && e.isTouchingWater() && !MagicItemUtils.isMagic(e.getStack())));
   
         boolean create = false;
         for(ItemEntity other : otherEntities){
            ItemStack otherStack = other.getStack();
            int count = otherStack.getCount();
            if(otherStack.isOf(Items.GLOWSTONE_DUST) && count >= 4){
               if(count == 4){
                  other.discard();
               }else{
                  otherStack.decrement(4);
                  other.setStack(otherStack);
               }
               
               create = true;
               break;
            }else if(otherStack.isOf(Items.REDSTONE) && count >= 16){
               if(count == 16){
                  other.discard();
               }else{
                  otherStack.decrement(16);
                  other.setStack(otherStack);
               }
               
               create = true;
               break;
            }
         }
         
         if(create){
            ItemStack cObby = new ItemStack(Items.CRYING_OBSIDIAN);
            cObby.setCount(1);
            ItemEntity cryingObby = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), cObby);
            cryingObby.setPickupDelay(40);
   
            float f = world.random.nextFloat() * 0.1F;
            float g = world.random.nextFloat() * 6.2831855F;
            cryingObby.setVelocity((double)(-MathHelper.sin(g) * f), 0.20000000298023224, (double)(MathHelper.cos(g) * f));
            world.spawnEntity(cryingObby);
            
            if(stack.getCount() == 1){
               itemEntity.discard();
            }else{
               stack.decrement(1);
               itemEntity.setStack(stack);
            }
         }
         
      }
   }
}
