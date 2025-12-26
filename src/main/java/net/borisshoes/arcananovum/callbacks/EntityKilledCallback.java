package net.borisshoes.arcananovum.callbacks;

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
import net.borisshoes.borislib.BorisLib;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EntityKilledCallback {
   public static void killedEntity(ServerLevel serverWorld, DamageSource damageSource, Entity attacker, LivingEntity killed){
      try{
         ServerPlayer player = null;
         SpearOfTenbrousEntity spear = null;
         if(damageSource.getDirectEntity() instanceof SpearOfTenbrousEntity spearEntity && spearEntity.getOwner() instanceof ServerPlayer){
            player = (ServerPlayer) spearEntity.getOwner();
            spear = spearEntity;
         }else if(attacker instanceof ServerPlayer){
            player = (ServerPlayer) attacker;
         }
         
         if(player != null){
            String entityTypeId = EntityType.getKey(killed.getType()).toString();
            
            // Check for soulstone then activate
            List<Tuple<List<ItemStack>, ItemStack>> allItems = ArcanaUtils.getAllItems(player);
            Inventory playerInv = player.getInventory();
            boolean procdStone = false;
            
            for(int i = 0; i < allItems.size(); i++){
               List<ItemStack> itemList = allItems.get(i).getA();
               ItemStack carrier = allItems.get(i).getB();
               SimpleContainer sinv = new SimpleContainer(itemList.size());
               
               for(int j = 0; j < itemList.size(); j++){
                  ItemStack item = itemList.get(j);
                  
                  boolean isArcane = ArcanaItemUtils.isArcane(item);
                  if(!isArcane){
                     sinv.setItem(j,item);
                     continue; // Item not arcane, skip
                  }
                  ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
                  
                  if(arcanaItem instanceof Soulstone stone && !procdStone){
                     if(Soulstone.getType(item).equals(entityTypeId)){
                        stone.killedEntity(serverWorld,player,killed,item,spear != null ? spear.getWeaponItem() : player.getWeaponItem());
                        procdStone = true; // Only activate one soulstone per kill
                     }
                  }
                  
                  sinv.setItem(j,item);
               }
               
               if(ArcanaItemUtils.identifyItem(carrier) instanceof ArcanistsBelt belt){
                  belt.buildItemLore(carrier, BorisLib.SERVER);
               }
            }
            
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
            if(ArcanaItemUtils.identifyItem(heldItem) instanceof ShadowStalkersGlaive glaive){ // Return 4 charges
               int oldEnergy = glaive.getEnergy(heldItem);
               glaive.addEnergy(heldItem, 80);
               int newEnergy = glaive.getEnergy(heldItem);
               if(oldEnergy/20 != newEnergy/20){
                  String message = "Glaive Charges: ";
                  for(int i=1; i<=5; i++){
                     message += newEnergy >= i*20 ? "✦ " : "✧ ";
                  }
                  player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.BLACK),true);
               }
               
               if((killed instanceof ServerPlayer || killed instanceof Warden) && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.OMAE_WA.id)){
                  ArcanaAchievements.progress(player,ArcanaAchievements.OMAE_WA.id,1);
               }
               if(killed instanceof Mob && ArcanaAchievements.isTimerActive(player,ArcanaAchievements.SHADOW_FURY.id) && ArcanaAchievements.getProgress(player,ArcanaAchievements.SHADOW_FURY.id) % 2 == 0){
                  ArcanaAchievements.progress(player,ArcanaAchievements.SHADOW_FURY.id,1);
               }
            }
            
            ItemStack chestItem = player.getItemBySlot(EquipmentSlot.CHEST);
            if(ArcanaItemUtils.identifyItem(chestItem) instanceof WingsOfEnderia wings && player.isFallFlying() && killed instanceof Mob){
               ArcanaAchievements.grant(player,ArcanaAchievements.ANGEL_OF_DEATH.id);
            }
            
            if(player.getWeaponItem().is(ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()) || spear != null){
               if(((ConditionalsAchievement)ArcanaAchievements.KILL_THEM_ALL).getConditions().containsKey(killed.getType().getDescription().getString())){
                  ArcanaAchievements.setCondition(player,ArcanaAchievements.KILL_THEM_ALL.id,killed.getType().getDescription().getString(),true);
               }
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
