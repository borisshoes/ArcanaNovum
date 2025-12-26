package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ContainmentCirclet;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class EntityUseCallback {
   public static InteractionResult useEntity(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      InteractionResult result = InteractionResult.PASS;
      try{
         
         ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(playerEntity.getItemInHand(hand));
         if(entity instanceof LivingEntity living){
            if(arcanaItem instanceof ContainmentCirclet circlet){
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
