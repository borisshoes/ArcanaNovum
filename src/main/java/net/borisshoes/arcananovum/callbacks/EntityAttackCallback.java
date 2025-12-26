package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class EntityAttackCallback {
   public static InteractionResult attackEntity(Player playerEntity, Level world, InteractionHand hand, Entity entity, @Nullable EntityHitResult entityHitResult){
      ItemStack item = playerEntity.getItemInHand(hand);
      try{
         Inventory inv = playerEntity.getInventory();
         for(int i = 0; i<inv.getContainerSize(); i++){
            ItemStack invItem = inv.getItem(i);
            ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(invItem);
            if(arcanaItem instanceof ShadowStalkersGlaive glaive){ // Check for Shadow Stalkers Glaive
               glaive.entityAttacked(playerEntity,invItem,entity);
            }
         }
         
         return InteractionResult.PASS;
      }catch(Exception e){
         e.printStackTrace();
         return InteractionResult.PASS;
      }
   }
}
