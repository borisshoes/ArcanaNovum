package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.WingsOfEnderia;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class EntityKilledCallback {
   public static void killedEntity(ServerWorld serverWorld, Entity entity, LivingEntity livingEntity){
      try{
         if(entity instanceof ServerPlayerEntity player){
            String entityTypeId = EntityType.getId(livingEntity.getType()).toString();
            
            // Check for soulstone then activate
            List<Pair<List<ItemStack>,ItemStack>> allItems = new ArrayList<>();
            PlayerInventory playerInv = player.getInventory();
            boolean procdStone = false;
            
            List<ItemStack> invItems = new ArrayList<>();
            for(int i=0; i<playerInv.size();i++){
               ItemStack item = playerInv.getStack(i);
               if(item.isEmpty()){
                  continue;
               }
               
               invItems.add(item);
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               if(magicItem instanceof ArcanistsBelt belt){
                  SimpleInventory beltInv = belt.deserialize(item);
                  ArrayList<ItemStack> beltList = new ArrayList<>();
                  for(int j = 0; j < beltInv.size(); j++){
                     beltList.add(beltInv.getStack(j));
                  }
                  allItems.add(new Pair<>(beltList,item));
               }
            }
            allItems.add(new Pair<>(invItems,ItemStack.EMPTY));
            
            for(int i = 0; i < allItems.size(); i++){
               List<ItemStack> itemList = allItems.get(i).getLeft();
               ItemStack carrier = allItems.get(i).getRight();
               SimpleInventory sinv = new SimpleInventory(itemList.size());
               
               for(int j = 0; j < itemList.size(); j++){
                  ItemStack item = itemList.get(j);
                  
                  boolean isMagic = MagicItemUtils.isMagic(item);
                  if(!isMagic){
                     sinv.setStack(j,item);
                     continue; // Item not magic, skip
                  }
                  MagicItem magicItem = MagicItemUtils.identifyItem(item);
                  
                  if(magicItem instanceof Soulstone stone && !procdStone){
                     if(Soulstone.getType(item).equals(entityTypeId)){
                        stone.killedEntity(serverWorld,player,livingEntity, item);
                        procdStone = true; // Only activate one soulstone per kill
                     }
                  }
                  
                  sinv.setStack(j,item);
               }
               
               if(MagicItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
                  belt.serialize(carrier, sinv);
               }
            }
   
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if(MagicItemUtils.identifyItem(heldItem) instanceof ShadowStalkersGlaive glaive){ // Return 4 charges
               int oldEnergy = glaive.getEnergy(heldItem);
               glaive.addEnergy(heldItem, 80);
               int newEnergy = glaive.getEnergy(heldItem);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.translatable(message).formatted(Formatting.BLACK),true);
               }
   
               if((livingEntity instanceof ServerPlayerEntity || livingEntity instanceof WardenEntity) && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.OMAE_WA.id)){
                  ArcanaAchievements.progress(player,ArcanaAchievements.OMAE_WA.id,1);
               }
               if(livingEntity instanceof MobEntity && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.SHADOW_FURY.id) && ArcanaAchievements.getProgress(player,ArcanaAchievements.SHADOW_FURY.id) % 2 == 0){
                  ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,1);
               }
            }
   
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
            if(MagicItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings && player.isFallFlying() && livingEntity instanceof MobEntity){
               ArcanaAchievements.grant(player,ArcanaAchievements.ANGEL_OF_DEATH.id);
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
