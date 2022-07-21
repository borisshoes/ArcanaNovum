package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.Arcananovum.TIMER_CALLBACKS;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         sojournerScoreboardTick(server);
         
         List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
         for(ServerPlayerEntity player : players){
            IArcanaProfileComponent arcaneProfile = PLAYER_DATA.get(player);
            
            // Check each player's inventory for magic items
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               if(item.isEmpty()){
                  if(item.getNbt() != null){
                     item.setNbt(null);
                  }else{
                     continue;
                  }
               }
               
               // Guidebook Craft Check
               if(item.hasNbt()){
                  if(item.getNbt().contains("ArcanaGuideBook")){
                     ItemStack newArcanaTome = MagicItems.ARCANE_TOME.getNewItem();
                     inv.setStack(i,newArcanaTome);
                     arcaneProfile.addCrafted(MagicItems.ARCANE_TOME.getId());
                  }
               }
               
               // Version Update Check
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
               if(MagicItemUtils.needsVersionUpdate(item)){
                  MagicItem magicItem = MagicItemUtils.identifyItem(item);
                  magicItem.updateItem(item);
                  //System.out.println("updated item");
               }
               
               // Ticking Item Check
               boolean needsTick = MagicItemUtils.needsMagicTick(item);
               //System.out.println("Inspecting "+item.getName().asString()+" needs ticking: "+needsTick);
               if(needsTick){
                  TickingItem magicItem = MagicItemUtils.identifyTickingItem(item);
                  magicItem.onTick(player.getWorld(),player,item);
               }
            }
            
            wingsTick(player);
            levitationHarnessCheck(player);
            concCheck(server,player,arcaneProfile);
         }
         
         // Tick Timer Callbacks
         Iterator<TickTimerCallback> itr = TIMER_CALLBACKS.iterator();
         while(itr.hasNext()){
            TickTimerCallback t = itr.next();
            if(t.decreaseTimer() == 0){
               t.onTimer();
               itr.remove();
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void sojournerScoreboardTick(MinecraftServer server){
      ServerScoreboard scoreboard = server.getScoreboard();
      if(scoreboard.getNullableObjective("arcananovum_sojourn_walk") == null){
         ScoreboardCriterion walked = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.walk_one_cm").orElseThrow();
         scoreboard.addObjective("arcananovum_sojourn_walk",walked,Text.translatable("dist_walked_sojourn"),walked.getDefaultRenderType());
      }
      if(scoreboard.getNullableObjective("arcananovum_sojourn_sprint") == null){
         ScoreboardCriterion sprinted = ScoreboardCriterion.getOrCreateStatCriterion("minecraft.custom:minecraft.sprint_one_cm").orElseThrow();
         scoreboard.addObjective("arcananovum_sojourn_sprint",sprinted,Text.translatable("dist_sprinted_sojourn"),sprinted.getDefaultRenderType());
      }
   }
   
   private static void concCheck(MinecraftServer server, ServerPlayerEntity player, IArcanaProfileComponent arcaneProfile){
      // Check to make sure everyone is under concentration limit
      int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP());
      int curConc = MagicItemUtils.getUsedConcentration(player);
      if(curConc > maxConc && server.getTicks()%80 == 0 && !player.isCreative() && !player.isSpectator()){
         player.sendMessage(Text.translatable("Your mind burns as your Arcana overwhelms you!").formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
         SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
         player.damage(DamageSource.OUT_OF_WORLD, 8);
         if(player.isDead()){
            AbstractTeam abstractTeam = player.getScoreboardTeam();
            Formatting playerColor = abstractTeam != null && abstractTeam.getColor() != null ? abstractTeam.getColor() : Formatting.LIGHT_PURPLE;
            String[] deathStrings = {
                  " lost concentration on their Arcana",
                  "'s mind was consumed by their Arcana",
                  "'s was crushed by the power of their Arcana",
                  "'s items consumed too much concentration",
                  " couldn't channel enough Arcana to their items"
            };
            final Text deathMsg = Text.translatable("")
                  .append(Text.translatable(player.getEntityName()).formatted(playerColor).formatted())
                  .append(Text.translatable(deathStrings[(int)(Math.random()*deathStrings.length)]).formatted(Formatting.LIGHT_PURPLE));
            server.getPlayerManager().broadcast(deathMsg, MessageType.SYSTEM);
         }
      }
   }
   
   private static void wingsTick(ServerPlayerEntity player){
      if(player.isFallFlying()){ // Wings of Zephyr
         ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
         if(MagicItemUtils.isMagic(item)){
            if(MagicItemUtils.identifyItem(item) instanceof WingsOfZephyr wings){
               wings.addEnergy(item,1); // Add 1 energy for each tick of flying
               if(wings.getEnergy(item) % 1000 == 999)
                  player.sendMessage(Text.translatable("Wing Energy Stored: "+Integer.toString(wings.getEnergy(item)+1)).formatted(Formatting.GRAY),true);
               PLAYER_DATA.get(player).addXP(2); // Add xp
            }
         }
      }
   }
   
   private static void levitationHarnessCheck(ServerPlayerEntity player){
      if(player.isCreative() || player.isSpectator())
         return;
      ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
      boolean allowFly = false;
      if(MagicItemUtils.isMagic(item)){
         if(MagicItemUtils.identifyItem(item) instanceof LevitationHarness harness){
            if(harness.getEnergy(item) > 0 && harness.getStall(item) == -1){
               allowFly = true;
            }
         }
      }
      if(player.getAbilities().allowFlying != allowFly){
         if(player.getAbilities().flying && !allowFly){
            player.getAbilities().flying = false;
         }
         player.getAbilities().allowFlying = allowFly;
         player.sendAbilitiesUpdate();
      }
      
   }
}
