package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.UsableItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ItemUseCallback {
   public static TypedActionResult<ItemStack> useItem(PlayerEntity playerEntity, World world, Hand hand){
      ItemStack item = playerEntity.getStackInHand(hand);
      try{
         if(MagicItemUtils.isUsableItem(item)){
            UsableItem magicItem = MagicItemUtils.identifyUsableItem(item);
            boolean useReturn = magicItem.useItem(playerEntity,world,hand);
            if(playerEntity instanceof ServerPlayerEntity player){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40, item));
            }
            return useReturn ? TypedActionResult.pass(item) : TypedActionResult.success(item);
         }
         return TypedActionResult.pass(item);
      }catch(Exception e){
         e.printStackTrace();
         return TypedActionResult.pass(item);
      }
   }
}
