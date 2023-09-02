package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
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
         PlayerInventory inv = playerEntity.getInventory();
         for(int i=0; i<inv.size();i++){
            ItemStack invItem = inv.getStack(i);
            MagicItem magicItem = MagicItemUtils.identifyItem(invItem);
            if(magicItem instanceof ShadowStalkersGlaive glaive){ // Check for Shadow Stalkers Glaive
               glaive.entityAttacked(playerEntity,invItem,entity);
            }
         }
         
         return ActionResult.PASS;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
}
