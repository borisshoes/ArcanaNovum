package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class EntityKilledCallback {
   public static void killedEntity(ServerWorld serverWorld, Entity entity, LivingEntity livingEntity){
      try{
         if(entity instanceof ServerPlayerEntity){
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            String entityTypeId = EntityType.getId(livingEntity.getType()).toString();
            String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
            
            // Check for soulstone then activate
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty())
                  continue;
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               if(magicItem instanceof Soulstone){
                  Soulstone stone = (Soulstone) magicItem;
                  if(Soulstone.getType(item).equals(entityTypeId)){
                     stone.killedEntity(serverWorld,player,livingEntity, item);
                     break; // Only activate one soulstone per kill
                  }
               }
            }
   
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if(MagicItemUtils.identifyItem(heldItem) instanceof ShadowStalkersGlaive glaive){ // Return 3 charges
               int oldEnergy = glaive.getEnergy(heldItem);
               glaive.addEnergy(heldItem, 60);
               int newEnergy = glaive.getEnergy(heldItem);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.translatable(message).formatted(Formatting.BLACK),true);
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
