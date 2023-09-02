package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.ContainmentCirclet;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EntityUseCallback {
   public static ActionResult useEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      ActionResult result = ActionResult.PASS;
      try{
         
         MagicItem magicItem = MagicItemUtils.identifyItem(playerEntity.getStackInHand(hand));
         if(entity instanceof LivingEntity living){
            if(magicItem instanceof ContainmentCirclet circlet){
               return circlet.useOnEntity(playerEntity,living,hand);
            }
         }
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return result;
      }
   }
}
