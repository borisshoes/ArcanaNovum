package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.items.SojournerBoots;
import net.borisshoes.arcananovum.items.core.AttackingItem;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EntityAttackCallback {
   public static ActionResult attackEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      try{
         if(MagicItemUtils.isAttackingItem(item)){
            AttackingItem magicItem = MagicItemUtils.identifyAttackingItem(item);
            boolean attackReturn = magicItem.attackEntity(playerEntity,world,hand,entity,entityHitResult);
            return attackReturn ? ActionResult.PASS : ActionResult.SUCCESS;
         }
         
         
         PlayerInventory inv = playerEntity.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack invItem = inv.getStack(i);
            MagicItem magicItem = MagicItemUtils.identifyItem(invItem);
            if(magicItem instanceof ShadowStalkersGlaive glaive){ // Check for Shadow Stalkers Glaive
               glaive.entityAttacked(playerEntity,invItem,entity);
               return ActionResult.PASS;
            }else{
               continue;
            }
         }
         
         ItemStack boots = playerEntity.getEquippedStack(EquipmentSlot.FEET);
         if(MagicItemUtils.identifyItem(boots) instanceof SojournerBoots sojournerBoots){
            boolean juggernaut = Math.max(0, ArcanaAugments.getAugmentOnItem(item,"juggernaut")) >= 1;
            if(juggernaut && entity instanceof LivingEntity living){
               float dmg = sojournerBoots.getEnergy(boots)/100.0f;
               if(dmg > 1){
                  StatusEffectInstance slow = new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(dmg*10), (int)dmg, false, false, true);
                  living.addStatusEffect(slow);
                  playerEntity.addStatusEffect(slow);
                  living.damage(DamageSource.player(playerEntity),dmg);
                  sojournerBoots.setEnergy(boots,0);
               }
            }
         }
         
         return ActionResult.PASS;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
}
