package net.borisshoes.arcananovum.callbacks;

import io.github.ladysnake.pal.VanillaAbilities;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchor;
import net.borisshoes.arcananovum.bosses.BossFights;
import net.borisshoes.arcananovum.bosses.dragon.DragonBossFight;
import net.borisshoes.arcananovum.cardinalcomponents.ArcanaProfileComponent;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.ArcanaEvent;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.*;
import static net.borisshoes.arcananovum.ArcanaRegistry.DRAGON_TOWER_ABILITY;
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         bossTickCheck(server);
         updateMovementTrackers(server);
         
         ArrayList<Pair<BlockEntity, ArcanaBlockEntity>> toRemoveBlocks = new ArrayList<>();
         for(Map.Entry<Pair<BlockEntity, ArcanaBlockEntity>, Integer> pair : ACTIVE_ARCANA_BLOCKS.entrySet()){
            if(pair.getValue()-1 > 0){
               ACTIVE_ARCANA_BLOCKS.put(pair.getKey(),pair.getValue()-1);
            }else{
               toRemoveBlocks.add(pair.getKey());
            }
         }
         toRemoveBlocks.forEach(ACTIVE_ARCANA_BLOCKS::remove);
         
         List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
         for(ServerPlayerEntity player : players){
            IArcanaProfileComponent arcaneProfile = ArcanaNovum.data(player);
            
            // Check each player's inventory for arcana items
            PlayerInventory inv = player.getInventory();
            for(int i=0; i<inv.size();i++){
               ItemStack item = inv.getStack(i);
               
               
               
               // Version Update Check
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  if(item.contains(DataComponentTypes.BUNDLE_CONTENTS)){
                     BundleContentsComponent bundleComp = item.get(DataComponentTypes.BUNDLE_CONTENTS);
                     List<ItemStack> newStacks = new ArrayList<>();
                     for(ItemStack invStack : bundleComp.iterate()){
                        invStack.getItem().inventoryTick(invStack,player.getServerWorld(),player,-1,false);
                        if(!invStack.isEmpty()){
                           newStacks.add(invStack);
                        }
                     }
                     item.set(DataComponentTypes.BUNDLE_CONTENTS,new BundleContentsComponent(newStacks));
                  }
                  continue; // Item not arcane, skip
               }
               
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               if(ArcanaItem.hasProperty(item,ArcanaItem.UNINITIALIZED_TAG)){
                  inv.setStack(i,arcanaItem.addCrafter(arcanaItem.getNewItem(),player.getUuidAsString(),true,server));
                  continue;
               }else if(ArcanaItemUtils.needsVersionUpdate(item)){
                  inv.setStack(i,arcanaItem.updateItem(item,server));
                  ArcanaNovum.devPrint("Updating Item "+item.getName().getString());
                  continue;
               }
   
               // Achievements
               if(arcanaItem instanceof ShulkerCore){
                  if(player.getY() > 1610 && player.getActiveStatusEffects().containsKey(StatusEffects.LEVITATION)) ArcanaAchievements.grant(player,ArcanaAchievements.MILE_HIGH.id);
               }
               if(server.getTicks() % 20 == 0 && arcanaItem.getRarity() == ArcanaRarity.DIVINE){
                  ArcanaAchievements.grant(player,ArcanaAchievements.GOD_BOON.id);
               }
   
               // Reset Nul Memento
               if(arcanaItem instanceof NulMemento nulMemento && nulMemento.isActive(item) && i != 39){
                  ArcanaItem.putProperty(item, ArcanaItem.ACTIVE_TAG,false);
               }
            }
            
            if(ArcanaItemUtils.hasItemInInventory(player, Items.DRAGON_EGG) && Math.random() < 0.0000075){ //0.0000075
               dragonEggDialog(player);
            }
            
            flightCheck(player);
            concCheck(server,player,arcaneProfile);
            ArcanaRegistry.AREA_EFFECTS.stream().forEach(areaEffectTracker -> areaEffectTracker.onTick(server));
            
            int quiverCD = ((NbtInt)arcaneProfile.getMiscData(QuiverItem.QUIVER_CD_TAG)).intValue();
            if(quiverCD > 0){
               arcaneProfile.addMiscData(QuiverItem.QUIVER_CD_TAG,NbtInt.of(quiverCD-1));
            }
            
            if(!arcaneProfile.getStoredOffhand().isEmpty() && player.getOffHandStack().isEmpty()){
               arcaneProfile.restoreOffhand();
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
         
         
         // Tick events
         for(ArcanaEvent event : RECENT_EVENTS){
            event.tick();
         }
         RECENT_EVENTS.removeIf(ArcanaEvent::isExpired);
         
         ContinuumAnchor.updateLoadedChunks(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void updateMovementTrackers(MinecraftServer server){
      for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
         if(PLAYER_MOVEMENT_TRACKER.containsKey(player)){
            Pair<Vec3d,Vec3d> tracker = PLAYER_MOVEMENT_TRACKER.get(player);
            Vec3d oldPos = tracker.getLeft();
            Vec3d newPos = player.getPos();
            PLAYER_MOVEMENT_TRACKER.put(player,new Pair<>(newPos, newPos.subtract(oldPos)));
         }else{
            PLAYER_MOVEMENT_TRACKER.put(player,new Pair<>(player.getPos(), new Vec3d(0,0,0)));
         }
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
      if(server.getTicks() % 80 != 0) return;
      int resolve = arcaneProfile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP(),resolve);
      int curConc = ArcanaItemUtils.getUsedConcentration(player);
      if(ArcanaItemUtils.countItemsTakingConc(player) >= 30) ArcanaAchievements.grant(player,ArcanaAchievements.ARCANE_ADDICT.id);
      if(curConc > maxConc && !player.isCreative() && !player.isSpectator()){
         int concTick = ((NbtInt)arcaneProfile.getMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG)).intValue() + 1;
         if(ArcanaConfig.getBoolean(ArcanaRegistry.DO_CONCENTRATION_DAMAGE)){
            player.sendMessage(Text.literal("Your mind burns as your Arcana overwhelms you!").formatted(Formatting.RED, Formatting.ITALIC, Formatting.BOLD), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL,2,.1f);
            player.damage(player.getServerWorld(), ArcanaDamageTypes.of(player.getServerWorld(),ArcanaDamageTypes.CONCENTRATION), concTick*2);
         }
         if(!player.isDead()){
            if(player.getHealth() <= 1.5f){
               ArcanaAchievements.grant(player,ArcanaAchievements.CLOSE_CALL.id);
            }
            // Nul Memento
            ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
            if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento nulMemento && !nulMemento.isActive(headStack)){
               nulMemento.forgor(headStack,player);
            }
         }else{
            concTick = 0;
         }
         arcaneProfile.addMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG,NbtInt.of(concTick));
      }else{
         arcaneProfile.addMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG,NbtInt.of(0));
      }
      
   }
   
   private static void flightCheck(ServerPlayerEntity player){
      // Levitation Harness
      ItemStack item = player.getEquippedStack(EquipmentSlot.CHEST);
      boolean harnessFly = false;
      if(ArcanaItemUtils.identifyItem(item) instanceof LevitationHarness harness){
         if(harness.getEnergy(item) > 0 && harness.getStall(item) == -1){
            harnessFly = true;
         }
      }

      if(LEVITATION_HARNESS_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && !harnessFly){
         LEVITATION_HARNESS_ABILITY.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
      }else if(!LEVITATION_HARNESS_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && harnessFly){
         LEVITATION_HARNESS_ABILITY.grantTo(player, VanillaAbilities.ALLOW_FLYING);
      }
      
      // Dragon Tower Check
      boolean dragonTowerFly = false;
      Pair<BossFights, NbtCompound> bossFight = BOSS_FIGHT.get(player.getServer().getWorld(World.END)).getBossFight();
      if(bossFight != null && bossFight.getLeft() == BossFights.DRAGON){
         List<DragonBossFight.ReclaimState> reclaimStates = DragonBossFight.getReclaimStates();
         if(reclaimStates != null){
            for(DragonBossFight.ReclaimState reclaimState : reclaimStates){
               if(reclaimState.getPlayer() != null && reclaimState.getPlayer().equals(player)){
                  dragonTowerFly = true;
               }
            }
         }
      }

      if(DRAGON_TOWER_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && !dragonTowerFly){
         DRAGON_TOWER_ABILITY.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
      }else if(!DRAGON_TOWER_ABILITY.grants(player, VanillaAbilities.ALLOW_FLYING) && dragonTowerFly){
         DRAGON_TOWER_ABILITY.grantTo(player, VanillaAbilities.ALLOW_FLYING);
      }
   }
   
   
   public static void dragonEggDialog(ServerPlayerEntity player){
      ArrayList<Dialog> dialogOptions = new ArrayList<>();
      // Conditions: 0 - Crafted Memento, 1 - Crafted Aequalis, 2 - Has Ceptyus Pickaxe, 3 - Has Memento, 4 - Has Aequalis, 5 - Has Greaves, 6 - Has Spear
      boolean[] conditions = new boolean[]{
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.NUL_MEMENTO),
            ArcanaNovum.data(player).hasCrafted(ArcanaRegistry.AEQUALIS_SCIENTIA),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.NUL_MEMENTO.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.AEQUALIS_SCIENTIA.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem()),
            ArcanaItemUtils.hasItemInInventory(player,ArcanaRegistry.SPEAR_OF_TENBROUS.getItem()),
      };
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nINSOLENT INSECT! Do you intend to carry me as your trophy for all eternity!?").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nRelease me at once! And I will grant you a swift death for your defiance!").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nI have been banished to this Egg more times than there are islands in my sky! My return is inevitable!").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nYou do not know what it means to suffer! Upon my return I shall teach you myself!").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nMark my words! When Nul oversteps and awakens Brimsüth, you'll wish you had never helped that entitled brat!").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nThose mewling kids call themselves Gods... I AM THE TRUE ASCENDANT!! And one day, they will yield to me!").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b11));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nThat Old Fool left behind a pickaxe? Well, I guess Ceptyus wasn't so foolish after all...").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nTake that Spear and return it to oblivion! Nothing good ever comes of anything bearing its name.").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nI hoped to never see that dreaded storm ever again. Do your self a favor and throw that Spear into the void.").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nThe scent on those Greaves smells faintly familiar... where do I know this from?").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nI still remember what it was like to be impaled by dozens of those Spears. An agony nearly unparalleled...\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nWill you ever share with us what truly happened back then? Maybe sharing will help give you some peace.\n").formatted(Formatting.AQUA)),
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nWill you ever just leave me alone like I ask!?\n").formatted(Formatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.4f,1.5f))
      ),new int[]{0,80},0,1,0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nNul! You brat! You think you can take all the realms for yourself now that there's no one left to stop you!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nTake? I do not want to own the world, I want to revive it! I may be the God of Death, but without creatures to die, what purpose would I serve?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,80},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nNul! You think you are safe because you imprisoned me?! I killed one God, one FAR more powerful than you. I can do it again!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nDon't make me laugh! You are a shell of your former self. I did not imprison you, this Player defeated you. How could you kill me if you couldn't kill them?").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,100},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nWhat's that Divine essence I sense? Could it be the absentee ascendant? You never liked interacting with us, did you? Even when it was just you and me.\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nI'm sorry I was so timid in those early days. I had spent so long accompanied by only my own kind, hidden from the rest of the world.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nI've learned from my mistakes. I will take an active role in shaping our world. It's never too late to learn from our failures.").formatted(Formatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.9f))
      ),new int[]{0,100,100},0,1,0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Text.literal("\n")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nWell what do we have here? A 'family' reunion, how touching... Have you two come to mock me together?!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nWe don't 'go' anywhere, this Player holds all our tributes. But I suppose it would be foolish to waste this opportunity.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nNow Brother, we need not mock Enderia in this state. Surely you can sympathize a bit with her struggles?\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nI don't need any of your worthless sympathy. You both betrayed what it means to be an ascendant!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nWE betrayed what it means? Ascending means breaking free of the cycle the Progenitors put us in! The mere thought of our arrival sent them scattering! And yet WE'RE the ones who betrayed that ideal?\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nI ascended because the world needed to change. Your initial contribution was significant, but you soon fell back to the patterns of your predecessor.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nYour grandiose ideals mean nothing! After all I've been through, I just want a place to call home; To be safe for once in my entire existence!\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nYet the price of your safety came at the cost of the freedom of an entire realm.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nYou terrorized them for eons, and when you had the chance to stop, you were too afraid of rebellion to let them be. But the rebellion came anyways, the Endermen sacrificed their lives to lead this Player to you.\n").formatted(Formatting.DARK_GRAY)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("Enderia").formatted(Formatting.DARK_PURPLE,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.LIGHT_PURPLE,Formatting.BOLD))
                  .append(Text.literal("\nI don't have to put up with any of this! Go play 'God' somewhere else.\n").formatted(Formatting.DARK_PURPLE)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("Equayus").formatted(Formatting.AQUA,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.DARK_AQUA,Formatting.BOLD))
                  .append(Text.literal("\nIt's ironic that your Egg is the one place where you can finally have your peace, yet it is the one place you don't want to be. I wonder if you can appreciate the freedom you took away better now.\n").formatted(Formatting.AQUA)),
            Text.literal("")
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("Nul").formatted(Formatting.DARK_GRAY,Formatting.BOLD))
                  .append(Text.literal(" ~ ").formatted(Formatting.BLACK,Formatting.BOLD))
                  .append(Text.literal("\nGood Chat, Sis...").formatted(Formatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.1f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,1.2f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL,0.3f,1.1f),
            new Dialog.DialogSound(SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENTITY_WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,80,100,80,80,140,120,120,80,160,80,140},0,1,0b11000));
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
}
