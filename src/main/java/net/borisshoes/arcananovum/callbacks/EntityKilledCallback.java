package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.achievements.ConditionalsAchievement;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.SpearOfTenbrousEntity;
import net.borisshoes.arcananovum.items.ArcanistsBelt;
import net.borisshoes.arcananovum.items.ShadowStalkersGlaive;
import net.borisshoes.arcananovum.items.Soulstone;
import net.borisshoes.arcananovum.items.WingsOfEnderia;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ArcanaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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

import java.util.List;

public class EntityKilledCallback {
   public static void killedEntity(ServerWorld serverWorld, DamageSource damageSource, Entity attacker, LivingEntity killed){
      try{
         ServerPlayerEntity player = null;
         SpearOfTenbrousEntity spear = null;
         if(damageSource.getSource() instanceof SpearOfTenbrousEntity spearEntity && spearEntity.getOwner() instanceof ServerPlayerEntity){
            player = (ServerPlayerEntity) spearEntity.getOwner();
            spear = spearEntity;
         }else if(attacker instanceof ServerPlayerEntity){
            player = (ServerPlayerEntity) attacker;
         }
         
         if(player != null){
            String entityTypeId = EntityType.getId(killed.getType()).toString();
            
            // Check for soulstone then activate
            List<Pair<List<ItemStack>,ItemStack>> allItems = ArcanaUtils.getAllItems(player);
            PlayerInventory playerInv = player.getInventory();
            boolean procdStone = false;
            
            for(int i = 0; i < allItems.size(); i++){
               List<ItemStack> itemList = allItems.get(i).getLeft();
               ItemStack carrier = allItems.get(i).getRight();
               SimpleInventory sinv = new SimpleInventory(itemList.size());
               
               for(int j = 0; j < itemList.size(); j++){
                  ItemStack item = itemList.get(j);
                  
                  boolean isArcane = ArcanaItemUtils.isArcane(item);
                  if(!isArcane){
                     sinv.setStack(j,item);
                     continue; // Item not arcane, skip
                  }
                  ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
                  
                  if(arcanaItem instanceof Soulstone stone && !procdStone){
                     if(Soulstone.getType(item).equals(entityTypeId)){
                        stone.killedEntity(serverWorld,player,killed,item,spear != null ? spear.getWeaponStack() : player.getWeaponStack());
                        procdStone = true; // Only activate one soulstone per kill
                     }
                  }
                  
                  sinv.setStack(j,item);
               }
               
               if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
                  belt.buildItemLore(carrier, ArcanaNovum.SERVER);
               }
            }
            
            ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
            if(ArcanaItemUtils.identifyItem(heldItem) instanceof ShadowStalkersGlaive glaive){ // Return 4 charges
               int oldEnergy = glaive.getEnergy(heldItem);
               glaive.addEnergy(heldItem, 80);
               int newEnergy = glaive.getEnergy(heldItem);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.sendMessage(Text.literal(message).formatted(Formatting.BLACK),true);
               }
               
               if((killed instanceof ServerPlayerEntity || killed instanceof WardenEntity) && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.OMAE_WA.id)){
                  ArcanaAchievements.progress(player,ArcanaAchievements.OMAE_WA.id,1);
               }
               if(killed instanceof MobEntity && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.SHADOW_FURY.id) && ArcanaAchievements.getProgress(player,ArcanaAchievements.SHADOW_FURY.id) % 2 == 0){
                  ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,1);
               }
            }
            
            ItemStack chestItem = player.getEquippedStack(EquipmentSlot.CHEST);
            if(ArcanaItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings && player.isGliding() && killed instanceof MobEntity){
               ArcanaAchievements.grant(player,ArcanaAchievements.ANGEL_OF_DEATH.id);
            }
            
            if(player.getWeaponStack().isOf(ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()) || spear != null){
               if(((ConditionalsAchievement)ArcanaAchievements.KILL_THEM_ALL).getConditions().containsKey(killed.getType().getName().getString())){
                  ArcanaAchievements.setCondition(player,ArcanaAchievements.KILL_THEM_ALL.id,killed.getType().getName().getString(),true);
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
