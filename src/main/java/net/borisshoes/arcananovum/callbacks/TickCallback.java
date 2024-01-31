package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.items.WingsOfEnderia;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.SERVER_TIMER_CALLBACKS;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         bossTickCheck(server);
         
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
               
               // Detect un-formatted item
               Identifier id = Registries.ITEM.getId(item.getItem());
               if(id.getNamespace().equals(ArcanaNovum.MOD_ID)){
                  if(!MagicItemUtils.isMagic(item)){
                     MagicItem magicItem = ArcanaRegistry.registry.get(id.getPath());
                     if(magicItem != null){
                        item.setNbt(magicItem.getNewItem().getNbt());
                        //ArcanaNovum.devPrint("Replacing data on: "+id.getPath());
                     }
                  }
               }
               
               
               // Version Update Check
               boolean isMagic = MagicItemUtils.isMagic(item);
               if(!isMagic)
                  continue; // Item not magic, skip
               
               MagicItem magicItem = MagicItemUtils.identifyItem(item);
               if(MagicItemUtils.needsVersionUpdate(item)){
                  magicItem.updateItem(item,server);
               }
   
               // Achievements
               if(magicItem instanceof ShulkerCore){
                  if(player.getY() > 1610 && player.getActiveStatusEffects().containsKey(StatusEffects.LEVITATION)) ArcanaAchievements.grant(player,ArcanaAchievements.MILE_HIGH.id);
               }
               if(server.getTicks() % 20 == 0 && magicItem.getRarity() == MagicRarity.MYTHICAL){
                  ArcanaAchievements.grant(player,ArcanaAchievements.GOD_BOON.id);
               }
   
               // Reset Nul Memento
               if(magicItem instanceof NulMemento nulMemento && nulMemento.isActive(item) && i != 39){
                  item.getNbt().getCompound("arcananovum").putBoolean("active",false);
               }
            }
            
            wingsTick(player);
            flightCheck(player);
            concCheck(server,player,arcaneProfile);
            
            int quiverCD = ((NbtInt)arcaneProfile.getMiscData("quiverCD")).intValue();
            if(quiverCD > 0){
               arcaneProfile.addMiscData("quiverCD",NbtInt.of(quiverCD-1));
            }
         }
         
         // Tick Timer Callbacks
         ArrayList<TickTimerCallback> toRemove = new ArrayList<>();
         HashMap<ServerPlayerEntity,Float> shieldTotals = new HashMap<>();
         for(int i = 0; i < SERVER_TIMER_CALLBACKS.size(); i++){
            TickTimerCallback t = SERVER_TIMER_CALLBACKS.get(i);
            if(t.decreaseTimer() == 0){
               t.onTimer();
               toRemove.add(t);
               continue;
            }
            if(t instanceof ShieldTimerCallback st){
               if(shieldTotals.containsKey(st.player)){
                  shieldTotals.put(st.player,shieldTotals.get(st.player)+st.getHearts());
                  if(shieldTotals.get(st.player) >= 200 && st.player.getAbsorptionAmount() >= 200) ArcanaAchievements.grant(st.player,ArcanaAchievements.BUILT_LIKE_TANK.id);
               }else{
                  shieldTotals.put(st.player,st.getHearts());
               }
            }
         }
         SERVER_TIMER_CALLBACKS.removeIf(toRemove::contains);
         
         ContinuumAnchor.updateLoadedChunks(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void bossTickCheck(MinecraftServer server){
      for(ServerWorld world : server.getWorlds()){
         Pair<BossFights, NbtCompound> fight = BOSS_FIGHT.get(world).getBossFight();
         if(fight != null){
            if(fight.getLeft() == BossFights.DRAGON){
               DragonBossFight.tick(server,fight.getRight());
            }
         }
      }
   }
   
   private static void concCheck(MinecraftServer server, ServerPlayerEntity player, IArcanaProfileComponent arcaneProfile){
      // Check to make sure everyone is under concentration limit
      int resolve = arcaneProfile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP(),resolve);
      int curConc = MagicItemUtils.getUsedConcentration(player);
      if(MagicItemUtils.countItemsTakingConc(player) >= 30) ArcanaAchievements.grant(player,ArcanaAchievements.ARCANE_ADDICT.id);
      if(curConc > maxConc && server.getTicks()%80 == 0 && !player.isCreative() && !player.isSpectator()){
         if((boolean) ArcanaNovum.config.getValue("doConcentrationDamage")){
            player.sendMessage(Text.literal("Your mind burns as your Arcana overwhelms you!").formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
            player.damage(ArcanaDamageTypes.of(player.getServerWorld(),ArcanaDamageTypes.CONCENTRATION), 8);
         }
         if(!player.isDead()){
            if(player.getHealth() <= 1.5f){
               ArcanaAchievements.grant(player,ArcanaAchievements.CLOSE_CALL.id);
            }
            // Nul Memento
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(MagicItemUtils.identifyItem(headStack) instanceof NulMemento nulMemento && !nulMemento.isActive(headStack)){
               nulMemento.forgor(headStack,player);
            }
         }
      }
   }
   
   private static void wingsTick(ServerPlayerEntity player){
      ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
      if(MagicItemUtils.identifyItem(item) instanceof WingsOfEnderia wings){
         if(player.isFallFlying()){ // Wings of Enderia
            wings.addEnergy(item,1); // Add 1 energy for each tick of flying
            if(wings.getEnergy(item) % 1000 == 999)
               player.sendMessage(Text.literal("Wing Energy Stored: "+ (wings.getEnergy(item) + 1)).formatted(Formatting.DARK_PURPLE),true);
            PLAYER_DATA.get(player).addXP(2); // Add xp
         }
         NbtCompound leftShoulder = player.getShoulderEntityLeft();
         NbtCompound rightShoulder = player.getShoulderEntityRight();
         if(leftShoulder != null && rightShoulder != null && leftShoulder.contains("id") && rightShoulder.contains("id")){
            if(leftShoulder.getString("id").equals("minecraft:parrot") && rightShoulder.getString("id").equals("minecraft:parrot")){
               ArcanaAchievements.grant(player, ArcanaAchievements.CROW_FATHER.id);
            }
         }
      }
   }
   
   private static void flightCheck(ServerPlayerEntity player){
      if(player.isCreative() || player.isSpectator())
         return;
      
      // Levitation Harness
      ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
      boolean allowFly = false;
      if(MagicItemUtils.isMagic(item)){
         if(MagicItemUtils.identifyItem(item) instanceof LevitationHarness harness){
            if(harness.getEnergy(item) > 0 && harness.getStall(item) == -1){
               allowFly = true;
            }
         }
      }
      
      // Dragon Tower Check
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(player.getServer().getWorld(World.END)).getBossFight();
      if(bossFight != null && bossFight.getLeft() == BossFights.DRAGON){
         List<DragonBossFight.ReclaimState> reclaimStates = DragonBossFight.getReclaimStates();
         if(reclaimStates != null){
            for(DragonBossFight.ReclaimState reclaimState : reclaimStates){
               if(reclaimState.getPlayer() != null && reclaimState.getPlayer().equals(player)){
                  allowFly = true;
               }
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
