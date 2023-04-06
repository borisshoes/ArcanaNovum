package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.core.UsableItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EntityUseCallback {
   public static ActionResult useEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      try{
         if(MagicItemUtils.isUsableItem(item)){
            UsableItem magicItem = MagicItemUtils.identifyUsableItem(item);
            boolean useReturn = magicItem.useItem(playerEntity,world,hand,entity,entityHitResult);
            if(playerEntity instanceof ServerPlayerEntity player){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : PlayerInventory.OFF_HAND_SLOT, item));
            }
            
            return useReturn ? ActionResult.PASS : ActionResult.SUCCESS;
         }
         return ActionResult.PASS;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
}
