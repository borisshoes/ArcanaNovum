package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.MagicBlock;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.items.UsableItem;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_BLOCK_LIST;

public class BlockUseCallback {
   public static ActionResult useBlock(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult){
      ItemStack item = playerEntity.getStackInHand(hand);
      //System.out.println(hand+" "+item);
      ActionResult result = ActionResult.PASS;
      try{
         UsableItem magicItem = null;
         if(MagicItemUtils.isUsableItem(item)){
            magicItem = MagicItemUtils.identifyUsableItem(item);
            boolean useReturn = magicItem.useItem(playerEntity,world,hand,blockHitResult);
            result = useReturn ? ActionResult.PASS : ActionResult.SUCCESS;
            if(playerEntity instanceof ServerPlayerEntity player){
               player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, hand == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40, item));
            }
         }
         
         // Magic Block check
         List<MagicBlock> blocks = MAGIC_BLOCK_LIST.get(world).getBlocks();
         for(MagicBlock magicBlock : blocks){
            if(magicBlock.getPos().equals(blockHitResult.getBlockPos())){
               NbtCompound blockData = magicBlock.getData();
               if(blockData.contains("id")){
                  String id = blockData.getString("id");
                  if(id.equals(MagicItems.CONTINUUM_ANCHOR.getId())){ // Continuum Anchor Check
                     int curFuel = blockData.getInt("fuel");
                     if(magicItem instanceof ExoticMatter){ // Try to add fuel
                        blockData.putInt("fuel",curFuel+((ExoticMatter) magicItem).getEnergy(item));
                        item.decrement(item.getCount());
                        item.setNbt(new NbtCompound());
                     }else if(playerEntity.getMainHandStack().isEmpty() && playerEntity.getMainHandStack().isEmpty() && curFuel > 0){ // Remove fuel if both hands are empty
                        blockData.putInt("fuel",0);
                        ItemStack removedFuelItem = MagicItems.EXOTIC_MATTER.getNewItem();
                        ((ExoticMatter)MagicItemUtils.identifyEnergyItem(removedFuelItem)).setFuel(removedFuelItem,curFuel);
                        playerEntity.giveItemStack(removedFuelItem);
                     }
                     result = ActionResult.SUCCESS;
                  }
               }
            }
         }
         
         return result;
      }catch(Exception e){
         e.printStackTrace();
         return ActionResult.PASS;
      }
   }
}
