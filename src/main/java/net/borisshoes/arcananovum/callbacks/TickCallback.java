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
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.damage.ArcanaDamageTypes;
import net.borisshoes.arcananovum.events.NulMementoEvent;
import net.borisshoes.arcananovum.items.LevitationHarness;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.items.QuiverItem;
import net.borisshoes.arcananovum.items.ShulkerCore;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.Dialog;
import net.borisshoes.arcananovum.utils.DialogHelper;
import net.borisshoes.arcananovum.utils.LevelUtils;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.events.Event;
import net.borisshoes.borislib.timers.TickTimerCallback;
import net.borisshoes.borislib.utils.SoundUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

import static net.borisshoes.arcananovum.ArcanaNovum.ACTIVE_ARCANA_BLOCKS;
import static net.borisshoes.arcananovum.ArcanaRegistry.DRAGON_TOWER_ABILITY;
import static net.borisshoes.arcananovum.ArcanaRegistry.LEVITATION_HARNESS_ABILITY;
import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.BOSS_FIGHT;

public class TickCallback {
   public static void onTick(MinecraftServer server){
      try{
         bossTickCheck(server);
         
         ArrayList<Tuple<BlockEntity, ArcanaBlockEntity>> toRemoveBlocks = new ArrayList<>();
         for(Map.Entry<Tuple<BlockEntity, ArcanaBlockEntity>, Integer> pair : ACTIVE_ARCANA_BLOCKS.entrySet()){
            if(pair.getValue()-1 > 0){
               ACTIVE_ARCANA_BLOCKS.put(pair.getKey(),pair.getValue()-1);
            }else{
               toRemoveBlocks.add(pair.getKey());
            }
         }
         toRemoveBlocks.forEach(ACTIVE_ARCANA_BLOCKS::remove);
         
         List<ServerPlayer> players = server.getPlayerList().getPlayers();
         for(ServerPlayer player : players){
            IArcanaProfileComponent arcaneProfile = ArcanaNovum.data(player);
            
            // Check each player's inventory for arcana items
            Inventory inv = player.getInventory();
            for(int i = 0; i<inv.getContainerSize(); i++){
               ItemStack item = inv.getItem(i);
               
               
               
               boolean isArcane = ArcanaItemUtils.isArcane(item);
               if(!isArcane){
                  if(item.is(ArcanaRegistry.ALL_ARCANA_ITEMS)){
                     ArcanaItem arcanaItem = ArcanaRegistry.ARCANA_ITEMS.getValue(Identifier.parse(item.getItem().toString()));
                     if(arcanaItem != null){
                        inv.setItem(i,arcanaItem.addCrafter(arcanaItem.getNewItem(),player.getStringUUID(),1,server));
                        item = inv.getItem(i);
                     }
                  }
                  
                  if(item.has(DataComponents.BUNDLE_CONTENTS)){
                     BundleContents bundleComp = item.get(DataComponents.BUNDLE_CONTENTS);
                     List<ItemStack> newStacks = new ArrayList<>();
                     for(ItemStack invStack : bundleComp.items()){
                        invStack.getItem().inventoryTick(invStack,player.level(),player,null);
                        if(!invStack.isEmpty()){
                           newStacks.add(invStack);
                        }
                     }
                     item.set(DataComponents.BUNDLE_CONTENTS,new BundleContents(newStacks));
                  }
                  continue; // Item not arcane, skip
               }
               
               ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
               if(ArcanaItem.hasProperty(item,ArcanaItem.UNINITIALIZED_TAG)){
                  inv.setItem(i,arcanaItem.addCrafter(arcanaItem.getNewItem(),player.getStringUUID(),1,server));
                  continue;
               }else if(ArcanaItemUtils.needsVersionUpdate(item)){
                  inv.setItem(i,arcanaItem.updateItem(item,server));
                  ArcanaNovum.devPrint("Updating Item "+item.getHoverName().getString());
                  continue;
               }
               
               if(arcanaItem.getOrigin(item) == 2 && arcanaItem.getCrafter(item).isEmpty()){
                  arcanaItem.addCrafter(item,player.getStringUUID(),2,server);
               }
   
               // Achievements
               if(arcanaItem instanceof ShulkerCore){
                  if(player.getY() > 1610 && player.getActiveEffectsMap().containsKey(MobEffects.LEVITATION)) ArcanaAchievements.grant(player,ArcanaAchievements.MILE_HIGH.id);
               }
               if(server.getTickCount() % 20 == 0 && arcanaItem.getRarity() == ArcanaRarity.DIVINE){
                  ArcanaAchievements.grant(player,ArcanaAchievements.GOD_BOON.id);
               }
   
               // Reset Nul Memento
               ItemStack finalItem = item;
               if(arcanaItem instanceof NulMemento nulMemento && nulMemento.isActive(item) &&
                     (i != 39 || Event.getEventsOfType(NulMementoEvent.class).stream().noneMatch(event -> event.getPlayer().equals(player) && ArcanaItem.getUUID(event.getMemento()).equals(ArcanaItem.getUUID(finalItem))))){
                  ArcanaItem.putProperty(item, ArcanaItem.ACTIVE_TAG,false);
               }
            }
            
            if(ArcanaItemUtils.hasItemInInventory(player, Items.DRAGON_EGG) && Math.random() < 0.0000075){ //0.0000075
               dragonEggDialog(player);
            }
            
            flightCheck(player);
            concCheck(server,player,arcaneProfile);
            ArcanaRegistry.AREA_EFFECTS.stream().forEach(areaEffectTracker -> areaEffectTracker.onTick(server));
            
            int quiverCD = ((IntTag)arcaneProfile.getMiscData(QuiverItem.QUIVER_CD_TAG)).intValue();
            if(quiverCD > 0){
               arcaneProfile.addMiscData(QuiverItem.QUIVER_CD_TAG, IntTag.valueOf(quiverCD-1));
            }
            
            if(!arcaneProfile.getStoredOffhand().isEmpty() && player.getOffhandItem().isEmpty()){
               arcaneProfile.restoreOffhand();
            }
         }
         
         HashMap<ServerPlayer,Float> shieldTotals = new HashMap<>();
         for(TickTimerCallback callback : BorisLib.SERVER_TIMER_CALLBACKS){
            if(callback instanceof ShieldTimerCallback st){
               if(shieldTotals.containsKey(st.getPlayer())){
                  shieldTotals.put(st.getPlayer(),shieldTotals.get(st.getPlayer())+st.getHearts());
                  if(shieldTotals.get(st.getPlayer()) >= 200 && st.getPlayer().getAbsorptionAmount() >= 200) ArcanaAchievements.grant(st.getPlayer(),ArcanaAchievements.BUILT_LIKE_TANK.id);
               }else{
                  shieldTotals.put(st.getPlayer(),st.getHearts());
               }
            }
         }
         
         ContinuumAnchor.updateLoadedChunks(server);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   private static void bossTickCheck(MinecraftServer server){
      for(ServerLevel world : server.getAllLevels()){
         Tuple<BossFights, CompoundTag> fight = BOSS_FIGHT.get(world).getBossFight();
         if(fight != null){
            if(fight.getA() == BossFights.DRAGON){
               DragonBossFight.tick(server,fight.getB());
            }
         }
      }
   }
   
   private static void concCheck(MinecraftServer server, ServerPlayer player, IArcanaProfileComponent arcaneProfile){
      // Check to make sure everyone is under concentration limit
      if(server.getTickCount() % 80 != 0) return;
      int resolve = arcaneProfile.getAugmentLevel(ArcanaAugments.RESOLVE.id);
      int maxConc = LevelUtils.concFromXp(arcaneProfile.getXP(),resolve);
      int curConc = ArcanaItemUtils.getUsedConcentration(player);
      if(ArcanaItemUtils.countItemsTakingConc(player) >= 30) ArcanaAchievements.grant(player,ArcanaAchievements.ARCANE_ADDICT.id);
      if(curConc > maxConc && !player.isCreative() && !player.isSpectator()){
         int concTick = ((IntTag)arcaneProfile.getMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG)).intValue() + 1;
         if(ArcanaConfig.getBoolean(ArcanaRegistry.DO_CONCENTRATION_DAMAGE)){
            player.displayClientMessage(Component.literal("Your mind burns as your Arcana overwhelms you!").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC, ChatFormatting.BOLD), true);
            SoundUtils.playSongToPlayer(player, SoundEvents.ILLUSIONER_CAST_SPELL,2,.1f);
            player.hurtServer(player.level(), ArcanaDamageTypes.of(player.level(),ArcanaDamageTypes.CONCENTRATION), concTick*2);
         }
         if(!player.isDeadOrDying()){
            if(player.getHealth() <= 1.5f){
               ArcanaAchievements.grant(player,ArcanaAchievements.CLOSE_CALL.id);
            }
            // Nul Memento
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if(ArcanaItemUtils.identifyItem(headStack) instanceof NulMemento nulMemento && !nulMemento.isActive(headStack)){
               nulMemento.forgor(headStack,player);
            }
         }else{
            concTick = 0;
         }
         arcaneProfile.addMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG, IntTag.valueOf(concTick));
      }else{
         arcaneProfile.addMiscData(ArcanaProfileComponent.CONCENTRATION_TICK_TAG, IntTag.valueOf(0));
      }
      
   }
   
   private static void flightCheck(ServerPlayer player){
      // Levitation Harness
      ItemStack item = player.getItemBySlot(EquipmentSlot.CHEST);
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
      Tuple<BossFights, CompoundTag> bossFight = BOSS_FIGHT.get(player.level().getServer().getLevel(Level.END)).getBossFight();
      if(bossFight != null && bossFight.getA() == BossFights.DRAGON){
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
   
   
   public static void dragonEggDialog(ServerPlayer player){
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
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nINSOLENT INSECT! Do you intend to carry me as your trophy for all eternity!?").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nRelease me at once! And I will grant you a swift death for your defiance!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI have been banished to this Egg more times than there are islands in my sky! My return is inevitable!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou do not know what it means to suffer! Upon my return I shall teach you myself!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},1,1,0b0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nMark my words! When Nul oversteps and awakens Brimsüth, you'll wish you had never helped that entitled brat!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThose mewling kids call themselves Gods... I AM THE TRUE ASCENDANT!! And one day, they will yield to me!").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b11));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThat Old Fool left behind a pickaxe? Well, I guess Ceptyus wasn't so foolish after all...").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b100));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nTake that Spear and return it to oblivion! Nothing good ever comes of anything bearing its name.").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI hoped to never see that dreaded storm ever again. Do your self a favor and throw that Spear into the void.").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b1000000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nThe scent on those Greaves smells faintly familiar... where do I know this from?").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f))
      ),new int[]{},0,1,0b100000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI still remember what it was like to be impaled by dozens of those Spears. An agony nearly unparalleled...\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nWill you ever share with us what truly happened back then? Maybe sharing will help give you some peace.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWill you ever just leave me alone like I ask!?\n").withStyle(ChatFormatting.DARK_PURPLE))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.4f,1.5f))
      ),new int[]{0,80},0,1,0b1010000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul! You brat! You think you can take all the realms for yourself now that there's no one left to stop you!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nTake? I do not want to own the world, I want to revive it! I may be the God of Death, but without creatures to die, what purpose would I serve?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.4f))
      ),new int[]{0,80},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nNul! You think you are safe because you imprisoned me?! I killed one God, one FAR more powerful than you. I can do it again!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nDon't make me laugh! You are a shell of your former self. I did not imprison you, this Player defeated you. How could you kill me if you couldn't kill them?").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,100},0,1,0b1000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWhat's that Divine essence I sense? Could it be the absentee ascendant? You never liked interacting with us, did you? Even when it was just you and me.\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI'm sorry I was so timid in those early days. I had spent so long accompanied by only my own kind, hidden from the rest of the world.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI've learned from my mistakes. I will take an active role in shaping our world. It's never too late to learn from our failures.").withStyle(ChatFormatting.AQUA))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.9f))
      ),new int[]{0,100,100},0,1,0b10000));
      
      dialogOptions.add(new Dialog(new ArrayList<>(Arrays.asList(
            Component.literal("\n")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nWell what do we have here? A 'family' reunion, how touching... Have you two come to mock me together?!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nWe don't 'go' anywhere, this Player holds all our tributes. But I suppose it would be foolish to waste this opportunity.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nNow Brother, we need not mock Enderia in this state. Surely you can sympathize a bit with her struggles?\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI don't need any of your worthless sympathy. You both betrayed what it means to be an ascendant!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nWE betrayed what it means? Ascending means breaking free of the cycle the Progenitors put us in! The mere thought of our arrival sent them scattering! And yet WE'RE the ones who betrayed that ideal?\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nI ascended because the world needed to change. Your initial contribution was significant, but you soon fell back to the patterns of your predecessor.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nYour grandiose ideals mean nothing! After all I've been through, I just want a place to call home; To be safe for once in my entire existence!\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nYet the price of your safety came at the cost of the freedom of an entire realm.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nYou terrorized them for eons, and when you had the chance to stop, you were too afraid of rebellion to let them be. But the rebellion came anyways, the Endermen sacrificed their lives to lead this Player to you.\n").withStyle(ChatFormatting.DARK_GRAY)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("Enderia").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                  .append(Component.literal("\nI don't have to put up with any of this! Go play 'God' somewhere else.\n").withStyle(ChatFormatting.DARK_PURPLE)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("Equayus").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
                  .append(Component.literal("\nIt's ironic that your Egg is the one place where you can finally have your peace, yet it is the one place you don't want to be. I wonder if you can appreciate the freedom you took away better now.\n").withStyle(ChatFormatting.AQUA)),
            Component.literal("")
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("Nul").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.BOLD))
                  .append(Component.literal(" ~ ").withStyle(ChatFormatting.BLACK, ChatFormatting.BOLD))
                  .append(Component.literal("\nGood Chat, Sis...").withStyle(ChatFormatting.DARK_GRAY))
      )),new ArrayList<>(Arrays.asList(
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.1f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.4f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,1.2f),
            new Dialog.DialogSound(SoundEvents.ENDER_DRAGON_GROWL,0.3f,1.1f),
            new Dialog.DialogSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM,0.5f,0.7f),
            new Dialog.DialogSound(SoundEvents.WITHER_AMBIENT,0.3f,0.7f))
      ),new int[]{0,80,100,80,80,140,120,120,80,160,80,140},0,1,0b11000));
      
      DialogHelper helper = new DialogHelper(dialogOptions,conditions);
      helper.sendDialog(List.of(player),helper.getWeightedResult(),true);
   }
}
