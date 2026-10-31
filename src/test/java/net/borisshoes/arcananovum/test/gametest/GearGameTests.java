package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.callbacks.EntityKilledCallback;
import net.borisshoes.arcananovum.callbacks.ShieldTimerCallback;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.entities.ArbalestArrowEntity;
import net.borisshoes.arcananovum.items.*;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.conditions.Conditions;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.List;

import static net.borisshoes.arcananovum.test.gametest.RegressionGameTests.finish;
import static net.borisshoes.arcananovum.test.gametest.TestKit.*;

/** Achievements and augments of travel, ranged and worn Arcana Items */
public class GearGameTests {

   private static void assertMaxEnergyChange(GameTestHelper helper, ArcanaItem item, ArcanaAugment augment, int level, int expectedChange){
      EnergyItem energyItem = (EnergyItem) item;
      int plain = energyItem.getMaxEnergy(item.getNewItem());
      int augmented = energyItem.getMaxEnergy(item(item, augment, level));
      helper.assertTrue(expectedChange != 0, augment.id + " " + level + " changes nothing in its config");
      helper.assertTrue(augmented - plain == expectedChange, augment.id + " " + level + " changed the maximum by " + (augmented - plain) + " instead of " + expectedChange);
   }

   // ===== Pearl of Recall =====

   private static ItemStack pearlTo(ResourceKeyLevel dim, ServerPlayer player){
      ItemStack stack = ArcanaRegistry.PEARL_OF_RECALL.getNewItem();
      CompoundTag location = new CompoundTag();
      location.putString("dim", dim.id);
      location.putDouble("x", player.getX());
      location.putDouble("y", 100);
      location.putDouble("z", player.getZ());
      ArcanaItem.putProperty(stack, PearlOfRecall.LOCATION_TAG, location);
      return stack;
   }

   private record ResourceKeyLevel(String id){
      static final ResourceKeyLevel NETHER = new ResourceKeyLevel("minecraft:the_nether");
      static final ResourceKeyLevel END = new ResourceKeyLevel("minecraft:the_end");
   }

   @GameTest
   @Covers("back_to_hell")
   public void recallingToTheNetherGrantsBackToHell(GameTestHelper helper){
      ServerPlayer player = player(helper);
      call(ArcanaRegistry.PEARL_OF_RECALL, "teleport", pearlTo(ResourceKeyLevel.NETHER, player), player);
      helper.assertTrue(player.level().dimension() == Level.NETHER, "The Pearl did not reach the Nether");
      assertAchieved(helper, player, ArcanaAchievements.BACK_TO_HELL);
      finish(helper, player);
   }

   @GameTest
   @Covers("ascending_to_heaven")
   public void recallingToTheEndGrantsAscendingToHeaven(GameTestHelper helper){
      ServerPlayer player = player(helper);
      call(ArcanaRegistry.PEARL_OF_RECALL, "teleport", pearlTo(ResourceKeyLevel.END, player), player);
      helper.assertTrue(player.level().dimension() == Level.END, "The Pearl did not reach the End");
      assertAchieved(helper, player, ArcanaAchievements.ASCENDING_TO_HEAVEN);
      finish(helper, player);
   }

   @GameTest
   @Covers("recall_acceleration")
   public void recallAccelerationShortensTheCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.PEARL_OF_RECALL, ArcanaAugments.RECALL_ACCELERATION, 5, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PEARL_OF_RECALL_COOLDOWN_PER_LVL).get(5));
      helper.succeed();
   }

   @GameTest
   @Covers("chrono_tear")
   public void chronoTearClearsTheSavedLocation(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setShiftKeyDown(true);
      ItemStack plain = pearlTo(ResourceKeyLevel.END, player);
      use(helper, player, plain);
      helper.assertTrue(ArcanaItem.getCompoundProperty(plain, PearlOfRecall.LOCATION_TAG).getStringOr("dim", "").equals(ResourceKeyLevel.END.id), "A plain Pearl lost its location");

      ItemStack stack = pearlTo(ResourceKeyLevel.END, player);
      ArcanaAugments.applyAugment(stack, ArcanaAugments.CHRONO_TEAR, 1, false);
      use(helper, player, stack);
      helper.assertTrue(ArcanaItem.getCompoundProperty(stack, PearlOfRecall.LOCATION_TAG).getStringOr("dim", "").equals("unattuned"), "Chrono Tear did not clear the location");
      finish(helper, player);
   }

   @GameTest
   @Covers("phase_defense")
   public void phaseDefenseKeepsTheRecallAndBlocksTheHit(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      ItemStack plainPearl = ArcanaRegistry.PEARL_OF_RECALL.getNewItem();
      ArcanaItem.putProperty(plainPearl, PearlOfRecall.HEAT_TAG, 5);
      plain.getInventory().setItem(0, plainPearl);
      withConfig(ArcanaConfig.PEARL_OF_RECALL_PHASE_DEFENSE_CHANCE, List.of(0.0, 0.0, 0.0, 1.0), () -> plain.hurtServer(helper.getLevel(), plain.damageSources().generic(), 4f));
      helper.assertTrue(ArcanaItem.getIntProperty(plain.getInventory().getItem(0), PearlOfRecall.HEAT_TAG) == -1, "A hit did not cancel the plain Pearl's recall");
      helper.assertTrue(plain.getHealth() < plain.getMaxHealth(), "The plain Pearl blocked the hit");

      ServerPlayer player = player(helper);
      ItemStack pearl = item(ArcanaRegistry.PEARL_OF_RECALL, ArcanaAugments.PHASE_DEFENSE, 3);
      ArcanaItem.putProperty(pearl, PearlOfRecall.HEAT_TAG, 5);
      player.getInventory().setItem(0, pearl);
      withConfig(ArcanaConfig.PEARL_OF_RECALL_PHASE_DEFENSE_CHANCE, List.of(0.0, 0.0, 0.0, 1.0), () -> player.hurtServer(helper.getLevel(), player.damageSources().generic(), 4f));
      helper.assertTrue(ArcanaItem.getIntProperty(player.getInventory().getItem(0), PearlOfRecall.HEAT_TAG) == 5, "Phase Defense at chance 1 lost the recall");
      helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Phase Defense did not negate the hit");
      finish(helper, plain, player);
   }

   // ===== Planeshifter =====

   @GameTest
   @Covers("planar_flow")
   public void planarFlowShortensTheCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.PLANESHIFTER, ArcanaAugments.PLANAR_FLOW, 5, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PLANESHIFTER_COOLDOWN_PER_LVL).get(5));
      helper.succeed();
   }

   @GameTest
   @Covers("unfortunate_materialization")
   public void shiftingAboveLavaGrantsUnfortunateMaterialization(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ServerLevel end = helper.getLevel().getServer().getLevel(Level.END);
      BlockPos lava = player.blockPosition().below(2);
      BlockPos spot = new BlockPos(lava.getX(), 90, lava.getZ());
      player.teleportTo(spot.getX() + 0.5, 95, spot.getZ() + 0.5);
      end.setBlockAndUpdate(spot, Blocks.LAVA.defaultBlockState());
      call(ArcanaRegistry.PLANESHIFTER, "directTeleport", player, end);
      end.setBlockAndUpdate(spot, Blocks.AIR.defaultBlockState());
      assertAchieved(helper, player, ArcanaAchievements.UNFORTUNATE_MATERIALIZATION);
      finish(helper, player);
   }

   // ===== Stasis Pearl =====

   @GameTest
   @Covers("stasis_acceleration")
   public void stasisAccelerationShortensTheCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.STASIS_PEARL, ArcanaAugments.STASIS_ACCELERATION, 5, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.STASIS_PEARL_COOLDOWN_PER_LVL).get(5));
      helper.succeed();
   }

   @GameTest
   @Covers("spatial_fold")
   public void spatialFoldDestroysTheThrownPearl(GameTestHelper helper){
      ServerPlayer player = player(helper);
      StasisPearl pearl = (StasisPearl) ArcanaRegistry.STASIS_PEARL;
      ItemStack plain = ArcanaRegistry.STASIS_PEARL.getNewItem();
      pearl.setEnergy(plain, pearl.getMaxEnergy(plain));
      use(helper, player, plain);
      String plainId = ArcanaItem.getStringProperty(plain, StasisPearl.PEARL_ID_TAG);
      helper.assertFalse(plainId.isEmpty(), "Using the Pearl threw nothing");
      player.setShiftKeyDown(true);
      use(helper, player, plain);
      helper.assertFalse(ArcanaItem.getStringProperty(plain, StasisPearl.PEARL_ID_TAG).isEmpty(), "A plain Pearl was cancelled");
      player.setShiftKeyDown(false);

      ItemStack stack = item(ArcanaRegistry.STASIS_PEARL, ArcanaAugments.SPATIAL_FOLD, 1);
      pearl.setEnergy(stack, pearl.getMaxEnergy(stack));
      use(helper, player, stack);
      String id = ArcanaItem.getStringProperty(stack, StasisPearl.PEARL_ID_TAG);
      var thrown = helper.getLevel().getEntity(java.util.UUID.fromString(id));
      helper.assertTrue(thrown != null, "The thrown pearl is missing");
      player.setShiftKeyDown(true);
      use(helper, player, stack);
      helper.assertTrue(ArcanaItem.getStringProperty(stack, StasisPearl.PEARL_ID_TAG).isEmpty(), "Spatial Fold did not reset the Pearl");
      helper.assertTrue(thrown.isRemoved(), "Spatial Fold did not destroy the thrown pearl");
      finish(helper, player);
   }

   // ===== Soulstone =====

   private static Soulstone soulstone(){
      return (Soulstone) ArcanaRegistry.SOULSTONE;
   }

   private static ItemStack stoneOneKillFromTier(int tier, EntityType<?> type){
      ItemStack stone = Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), type);
      return Soulstone.setSouls(stone, Soulstone.tiers[tier - 1] - 1);
   }

   private static void kill(GameTestHelper helper, ServerPlayer player, EntityType<? extends LivingEntity> type, ItemStack stone, ItemStack weapon){
      LivingEntity dead = type.create(helper.getLevel(), EntitySpawnReason.TRIGGERED);
      soulstone().killedEntity(helper.getLevel(), player, dead, stone, weapon);
   }

   @GameTest
   @Covers("took_a_village")
   public void reachingTierThreeOnVillagersGrantsTookAVillage(GameTestHelper helper){
      ServerPlayer player = player(helper);
      kill(helper, player, EntityTypes.ZOMBIE, stoneOneKillFromTier(3, EntityTypes.ZOMBIE), ItemStack.EMPTY);
      assertNotAchieved(helper, player, ArcanaAchievements.TOOK_A_VILLAGE);
      kill(helper, player, EntityTypes.VILLAGER, stoneOneKillFromTier(3, EntityTypes.VILLAGER), ItemStack.EMPTY);
      assertAchieved(helper, player, ArcanaAchievements.TOOK_A_VILLAGE);
      finish(helper, player);
   }

   @GameTest
   @Covers("philosopher_stone")
   public void reachingTierFiveGrantsPhilosopherStone(GameTestHelper helper){
      ServerPlayer player = player(helper);
      kill(helper, player, EntityTypes.ZOMBIE, stoneOneKillFromTier(5, EntityTypes.ZOMBIE), ItemStack.EMPTY);
      assertAchieved(helper, player, ArcanaAchievements.PHILOSOPHER_STONE);
      finish(helper, player);
   }

   @GameTest
   @Covers({"prime_evil", "history_carved_in_stone"})
   public void reachingTierSevenGrantsPrimeEvilAndOnSpearKillsHistoryCarvedInStone(GameTestHelper helper){
      ServerPlayer player = player(helper);
      kill(helper, player, EntityTypes.ZOMBIE, stoneOneKillFromTier(7, EntityTypes.ZOMBIE), ItemStack.EMPTY);
      assertAchieved(helper, player, ArcanaAchievements.PRIME_EVIL);
      assertNotAchieved(helper, player, ArcanaAchievements.HISTORY_CARVED_IN_STONE);

      ItemStack stone = stoneOneKillFromTier(7, EntityTypes.ZOMBIE);
      ArcanaItem.putProperty(stone, Soulstone.SOULS_FROM_SPEAR_TAG, Soulstone.tiers[6] - 1);
      kill(helper, player, EntityTypes.ZOMBIE, stone, ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem());
      assertAchieved(helper, player, ArcanaAchievements.HISTORY_CARVED_IN_STONE);
      finish(helper, player);
   }

   private static int soulsFromKill(GameTestHelper helper, ServerPlayer player, ItemStack stone, ItemStack weapon){
      ItemStack typed = Soulstone.setType(stone, EntityTypes.ZOMBIE);
      int before = Soulstone.getSouls(typed);
      kill(helper, player, EntityTypes.ZOMBIE, typed, weapon);
      return Soulstone.getSouls(typed) - before;
   }

   @GameTest
   @Covers("soul_reaper")
   public void soulReaperAddsSoulsPerKill(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.assertTrue(soulsFromKill(helper, player, ArcanaRegistry.SOULSTONE.getNewItem(), ItemStack.EMPTY) == 1, "A plain stone should take one soul");
      int expected = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SOULSTONE_SOULS_PER_LVL).get(5);
      int souls = soulsFromKill(helper, player, item(ArcanaRegistry.SOULSTONE, ArcanaAugments.SOUL_REAPER, 5), ItemStack.EMPTY);
      helper.assertTrue(souls == expected && expected > 1, "Soul Reaper 5 took " + souls + " souls instead of " + expected);
      finish(helper, player);
   }

   @GameTest
   @Covers("eternal_cruelty")
   public void eternalCrueltyDoublesSoulsFromTheSpear(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.assertTrue(soulsFromKill(helper, player, ArcanaRegistry.SOULSTONE.getNewItem(), ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem()) == 1, "A plain Spear should not double souls");
      int souls = soulsFromKill(helper, player, ArcanaRegistry.SOULSTONE.getNewItem(), item(ArcanaRegistry.SPEAR_OF_TENBROUS, ArcanaAugments.ETERNAL_CRUELTY, 1));
      helper.assertTrue(souls == 2, "Eternal Cruelty took " + souls + " souls instead of 2");
      finish(helper, player);
   }

   // ===== Runic Bow =====

   private static ServerPlayer archer(GameTestHelper helper, ItemStack bow, ItemStack arrows){
      ServerPlayer player = player(helper);
      player.setItemInHand(InteractionHand.MAIN_HAND, bow);
      player.getInventory().setItem(10, arrows);
      return player;
   }

   private static void release(GameTestHelper helper, ServerPlayer player, ItemStack bow){
      bow.getItem().releaseUsing(bow, helper.getLevel(), player, 0);
   }

   @GameTest
   @Covers("just_like_archer")
   public void shootingTheRunicBowGrantsJustLikeArcher(GameTestHelper helper){
      ItemStack bow = ArcanaRegistry.RUNIC_BOW.getNewItem();
      ItemStack arrows = ArcanaRegistry.SIPHONING_ARROWS.getNewItem();
      arrows.setCount(8);
      ServerPlayer player = archer(helper, bow, arrows);
      almostComplete(player, ArcanaAchievements.JUST_LIKE_ARCHER, 1);
      release(helper, player, bow);
      assertAchieved(helper, player, ArcanaAchievements.JUST_LIKE_ARCHER);
      finish(helper, player);
   }

   @GameTest
   @Covers("bow_acceleration")
   public void bowAccelerationReachesFullDrawSooner(GameTestHelper helper){
      RunicBow bow = (RunicBow) ArcanaRegistry.RUNIC_BOW;
      ItemStack plain = bow.getNewItem();
      ItemStack fast = item(bow, ArcanaAugments.BOW_ACCELERATION, 5);
      Object plainPull = call(plain.getItem(), "getPullProgress", 10, plain);
      Object fastPull = call(fast.getItem(), "getPullProgress", 10, fast);
      helper.assertTrue((float) plainPull < 1f, "A plain bow should not be fully drawn after 10 ticks");
      helper.assertTrue((float) fastPull == 1f, "Bow Acceleration 5 should be fully drawn after 10 ticks, was at " + fastPull);
      helper.succeed();
   }

   @GameTest
   @Covers("enhanced_infinity")
   public void enhancedInfinitySavesTippedArrows(GameTestHelper helper){
      ItemStack plainBow = ArcanaRegistry.RUNIC_BOW.getNewItem();
      plainBow.enchant(MinecraftUtils.getEnchantment(Enchantments.INFINITY), 1);
      ItemStack plainArrows = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.POISON);
      plainArrows.setCount(4);
      ServerPlayer plain = archer(helper, plainBow, plainArrows);
      release(helper, plain, plainBow);
      helper.assertTrue(plain.getInventory().getItem(10).getCount() == 3, "Plain Infinity should still use a tipped arrow");

      ItemStack bow = item(ArcanaRegistry.RUNIC_BOW, ArcanaAugments.ENHANCED_INFINITY, 1);
      bow.enchant(MinecraftUtils.getEnchantment(Enchantments.INFINITY), 1);
      ItemStack arrows = PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.POISON);
      arrows.setCount(4);
      ServerPlayer player = archer(helper, bow, arrows);
      release(helper, player, bow);
      helper.assertTrue(player.getInventory().getItem(10).getCount() == 4, "Enhanced Infinity used a tipped arrow");
      finish(helper, plain, player);
   }

   @GameTest(padding = 32)
   @Covers("bow_stabilization")
   public void bowStabilizationNarrowsTheSpread(GameTestHelper helper){
      // A huge base spread and none at level 3, so a straight shot can only come from reading the level's slot
      withConfig(ArcanaConfig.RUNIC_BOW_ACCURACY_PER_LVL, List.of(100.0, 100.0, 100.0, 0.0), () -> {
         ItemStack bow = item(ArcanaRegistry.RUNIC_BOW, ArcanaAugments.BOW_STABILIZATION, 3);
         ServerPlayer player = archer(helper, bow, new ItemStack(Items.ARROW, 4));
         release(helper, player, bow);
         var arrows = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.arrow.AbstractArrow.class, player.getBoundingBox().inflate(5));
         helper.assertFalse(arrows.isEmpty(), "The bow fired nothing");
         Vec3 direction = arrows.getFirst().getDeltaMovement().normalize();
         helper.assertTrue(direction.dot(player.getLookAngle()) > 0.999, "Stabilization 3 with no spread fired off line");
         finish(helper, player);
      });
   }

   // ===== Quivers =====

   private static int quiverContents(ItemStack quiver){
      return quiver.getOrDefault(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY).nonEmptyItemCopyStream().mapToInt(ItemStack::getCount).sum();
   }

   private static ItemStack stockedQuiver(ArcanaItem type){
      ItemStack quiver = type.getNewItem();
      quiver.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(new ItemStack(Items.ARROW, 4))));
      return quiver;
   }

   @GameTest
   @Covers("spare_stock")
   public void refillingTheOverflowingQuiverGrantsSpareStock(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.SPARE_STOCK, 1);
      ItemStack quiver = stockedQuiver(ArcanaRegistry.OVERFLOWING_QUIVER);
      call(ArcanaRegistry.OVERFLOWING_QUIVER, "refillArrow", helper.getLevel().getServer(), player.getUUID(), quiver);
      assertAchieved(helper, player, ArcanaAchievements.SPARE_STOCK);
      helper.assertTrue(ArcanaAchievements.getProgress(player, ArcanaAchievements.UNLIMITED_STOCK) == 0, "The Overflowing Quiver counted toward the Runic Quiver's achievement");
      finish(helper, player);
   }

   @GameTest
   @Covers("unlimited_stock")
   public void refillingTheRunicQuiverGrantsUnlimitedStock(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.UNLIMITED_STOCK, 1);
      ItemStack quiver = stockedQuiver(ArcanaRegistry.RUNIC_QUIVER);
      call(ArcanaRegistry.RUNIC_QUIVER, "refillArrow", helper.getLevel().getServer(), player.getUUID(), quiver);
      assertAchieved(helper, player, ArcanaAchievements.UNLIMITED_STOCK);
      finish(helper, player);
   }

   @GameTest
   @Covers({"abundant_ammo", "quiver_duplication"})
   public void restockAugmentsShortenTheRefillTime(GameTestHelper helper){
      for(var quiver : List.of(List.of(ArcanaRegistry.OVERFLOWING_QUIVER, ArcanaAugments.ABUNDANT_AMMO), List.of(ArcanaRegistry.RUNIC_QUIVER, ArcanaAugments.QUIVER_DUPLICATION))){
         ArcanaItem type = (ArcanaItem) quiver.get(0);
         ArcanaAugment augment = (ArcanaAugment) quiver.get(1);
         int plain = (int) call(type, "getRefillMod", type.getNewItem());
         int boosted = (int) call(type, "getRefillMod", item(type, augment, 5));
         helper.assertTrue(boosted < plain, augment.id + " 5 restocks every " + boosted + " ticks against " + plain);
      }
      helper.succeed();
   }

   @GameTest
   @Covers({"overflowing_bottomless", "runic_bottomless"})
   public void bottomlessAugmentsSaveArrows(GameTestHelper helper){
      double expected = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.QUIVER_EFFICIENCY_PER_LVL).get(5);
      helper.assertTrue(expected > 0, "The bottomless config at level 5 saves nothing");
      for(var quiver : List.of(List.of(ArcanaRegistry.OVERFLOWING_QUIVER, ArcanaAugments.OVERFLOWING_BOTTOMLESS), List.of(ArcanaRegistry.RUNIC_QUIVER, ArcanaAugments.RUNIC_BOTTOMLESS))){
         ArcanaItem type = (ArcanaItem) quiver.get(0);
         ArcanaAugment augment = (ArcanaAugment) quiver.get(1);
         double chance = (double) call(type, "getEfficiencyMod", item(type, augment, 5));
         helper.assertTrue(chance == expected, augment.id + " 5 has the wrong chance: " + chance);
      }
      helper.succeed();
   }

   @GameTest
   @Covers("shunt_runes")
   public void shuntRunesLowerArrowConcentration(GameTestHelper helper){
      RunicQuiver quiver = (RunicQuiver) ArcanaRegistry.RUNIC_QUIVER;
      double plain = quiver.getArcanaItemContainer(stockedQuiver(quiver)).getConcMod();
      ItemStack shunted = stockedQuiver(quiver);
      ArcanaAugments.applyAugment(shunted, ArcanaAugments.SHUNT_RUNES, 1, false);
      double shunt = quiver.getArcanaItemContainer(shunted).getConcMod();
      helper.assertTrue(plain == 0.5 && shunt == 0.25, "Shunt Runes changed the multiplier from " + plain + " to " + shunt);
      helper.succeed();
   }

   // ===== Alchemical Arbalest =====

   private static void arbalestHit(ArbalestArrowEntity arrow, EntityHitResult hit){
      try{
         Method onHitEntity = ArbalestArrowEntity.class.getDeclaredMethod("onHitEntity", EntityHitResult.class);
         onHitEntity.setAccessible(true);
         onHitEntity.invoke(arrow, hit);
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("ArbalestArrowEntity.onHitEntity changed signature, update this test", e);
      }
   }

   @GameTest
   @Covers("many_birds_many_arrows")
   public void killingPhantomsWithTheArbalestGrantsManyBirds(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 3; i++){
         assertNotAchieved(helper, player, ArcanaAchievements.MANY_BIRDS_MANY_ARROWS);
         Phantom phantom = mobWithHealth(helper, EntityTypes.PHANTOM, 4, 4, 4, 0.5f);
         ArbalestArrowEntity arrow = new ArbalestArrowEntity(helper.getLevel(), player, 0, 0, new ItemStack(Items.SPECTRAL_ARROW), null);
         arrow.snapTo(phantom.getX(), phantom.getY(), phantom.getZ(), 0, 0);
         arrow.setDeltaMovement(0, 0, 3);
         arbalestHit(arrow, new EntityHitResult(phantom));
      }
      assertAchieved(helper, player, ArcanaAchievements.MANY_BIRDS_MANY_ARROWS);
      finish(helper, player);
   }

   @GameTest
   @Covers("prolific_potions")
   public void prolificPotionsWidensTheField(GameTestHelper helper){
      ServerPlayer player = player(helper);
      double plain = arbalestRange(helper, player, 0);
      double prolific = arbalestRange(helper, player, 3);
      double expected = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.ALCHEMICAL_ARBALEST_FIELD_RANGE_PER_LVL).get(3);
      helper.assertTrue(prolific == expected && prolific > plain, "Prolific Potions 3 has a range of " + prolific + " against " + plain);
      finish(helper, player);
   }

   private static double arbalestRange(GameTestHelper helper, ServerPlayer player, int level){
      ArbalestArrowEntity arrow = new ArbalestArrowEntity(helper.getLevel(), player, 0, level, new ItemStack(Items.SPECTRAL_ARROW), null);
      try{
         var field = ArbalestArrowEntity.class.getDeclaredField("range");
         field.setAccessible(true);
         return ((Number) field.get(arrow)).doubleValue();
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("ArbalestArrowEntity.range changed, update this test", e);
      }
   }

   // ===== Shulker Core =====

   @GameTest
   @Covers("levitative_reabsorption")
   public void levitativeReabsorptionUnlocksTheCleanseSpeed(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.assertTrue(highestCoreSpeed(helper, player, ArcanaRegistry.SHULKER_CORE.getNewItem()) == 9, "A plain Core's top speed should be 9");
      helper.assertTrue(highestCoreSpeed(helper, player, item(ArcanaRegistry.SHULKER_CORE, ArcanaAugments.LEVITATIVE_REABSORPTION, 1)) == 11, "Levitative Reabsorption should add the speed above 9");
      finish(helper, player);
   }

   private static int highestCoreSpeed(GameTestHelper helper, ServerPlayer player, ItemStack core){
      int highest = 0;
      player.setItemInHand(InteractionHand.MAIN_HAND, core);
      for(int i = 0; i < 8; i++){
         ArcanaItem.putProperty(core, ShulkerCore.SPEED_CD_TAG, 0);
         call(ArcanaRegistry.SHULKER_CORE, "changeSpeed", player, helper.getLevel(), InteractionHand.MAIN_HAND);
         highest = Math.max(highest, ArcanaItem.getIntProperty(core, ShulkerCore.SPEED_TAG));
      }
      return highest;
   }

   // ===== Shadow Stalker's Glaive =====

   private static ItemStack tetheredGlaive(ArcanaAugment augment, int level, LivingEntity target){
      ItemStack stack = augment == null ? ArcanaRegistry.SHADOW_STALKERS_GLAIVE.getNewItem() : item(ArcanaRegistry.SHADOW_STALKERS_GLAIVE, augment, level);
      ShadowStalkersGlaive glaive = (ShadowStalkersGlaive) ArcanaRegistry.SHADOW_STALKERS_GLAIVE;
      glaive.setEnergy(stack, glaive.getMaxEnergy(stack));
      ArcanaItem.putProperty(stack, ShadowStalkersGlaive.TETHER_TARGET_TAG, target.getStringUUID());
      ArcanaItem.putProperty(stack, ShadowStalkersGlaive.TETHER_TIME_TAG, 10);
      return stack;
   }

   private static void killWithHeld(GameTestHelper helper, ServerPlayer player, LivingEntity target){
      EntityKilledCallback.killedEntity(helper.getLevel(), player.damageSources().playerAttack(player), player, target);
   }

   @GameTest(maxTicks = 40)
   @Covers("omae_wa")
   public void stalkingAndKillingAWardenGrantsOmaeWa(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Warden warden = mob(helper, EntityTypes.WARDEN, 4, 2, 4);
      use(helper, player, tetheredGlaive(null, 0, warden));
      helper.assertTrue(ArcanaAchievements.isTimerActive(player, ArcanaAchievements.OMAE_WA), "Stalking a Warden did not open the timer");
      killWithHeld(helper, player, warden);
      assertAchieved(helper, player, ArcanaAchievements.OMAE_WA);
      finish(helper, player);
   }

   @GameTest
   @Covers("shadow_fury")
   public void stalkKillChainsGrantShadowFury(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 4, 2, 4);
      // The window counts stalks and kills in turn, so a player needs 15 of them. This skips to the last pair
      use(helper, player, tetheredGlaive(null, 0, zombie));
      helper.assertTrue(ArcanaAchievements.isTimerActive(player, ArcanaAchievements.SHADOW_FURY), "Stalking did not open the timer");
      ArcanaAchievements.progress(player, ArcanaAchievements.SHADOW_FURY, 13);
      assertNotAchieved(helper, player, ArcanaAchievements.SHADOW_FURY);
      helper.assertTrue(ArcanaAchievements.getProgress(player, ArcanaAchievements.SHADOW_FURY) == 13, "Expected 13 steps");
      use(helper, player, tetheredGlaive(null, 0, zombie));
      helper.assertTrue(ArcanaAchievements.getProgress(player, ArcanaAchievements.SHADOW_FURY) == 14, "The stalk did not count");
      killWithHeld(helper, player, zombie);
      assertAchieved(helper, player, ArcanaAchievements.SHADOW_FURY);
      finish(helper, player);
   }

   @GameTest
   @Covers({"paranoia", "shadow_stride"})
   public void stalkAugmentsAffectTargetAndWielder(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      Zombie plainTarget = mob(helper, EntityTypes.ZOMBIE, 4, 2, 4);
      use(helper, plain, tetheredGlaive(null, 0, plainTarget));
      helper.assertTrue(Conditions.getConditionValue(plainTarget, Conditions.NEARSIGHT) == 0, "A plain stalk gave Nearsight");
      helper.assertFalse(plain.hasEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT), "A plain stalk gave Greater Invisibility");

      ServerPlayer paranoid = player(helper);
      Zombie target = mob(helper, EntityTypes.ZOMBIE, 6, 2, 4);
      use(helper, paranoid, tetheredGlaive(ArcanaAugments.PARANOIA, 3, target));
      helper.assertTrue(!Conditions.getConditionInstancesOf(target.getUUID(), Conditions.NEARSIGHT).isEmpty(), "Paranoia gave the target no Nearsight");

      ServerPlayer strider = player(helper);
      Zombie other = mob(helper, EntityTypes.ZOMBIE, 2, 2, 6);
      use(helper, strider, tetheredGlaive(ArcanaAugments.SHADOW_STRIDE, 3, other));
      helper.assertTrue(strider.getEffect(ArcanaRegistry.GREATER_INVISIBILITY_EFFECT).getDuration() == ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SHADOW_STALKERS_GLAIVE_INVIS_DURATION).get(3), "Shadow Stride 3 gave the wrong invisibility");
      finish(helper, plain, paranoid, strider);
   }

   // Passes on a tick where the Glaive recharges, which is every 100 ticks
   @GameTest(maxTicks = 120)
   @Covers("bloodletter")
   public void bloodletterTradesHealthForCharge(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ShadowStalkersGlaive glaive = (ShadowStalkersGlaive) ArcanaRegistry.SHADOW_STALKERS_GLAIVE;
      ItemStack stack = item(glaive, ArcanaAugments.BLOODLETTER, 1);
      int start = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SHADOW_STALKERS_GLAIVE_PASSIVE_ENERGY_CAP);
      glaive.setEnergy(stack, start);
      ItemStack plain = ArcanaRegistry.SHADOW_STALKERS_GLAIVE.getNewItem();
      glaive.setEnergy(plain, start);
      tickUntil(helper, player, stack, () -> {
         helper.assertTrue(glaive.getEnergy(stack) > start, "Waiting for Bloodletter to recharge past the passive cap");
         helper.assertTrue(player.getHealth() < player.getMaxHealth(), "Bloodletter recharged for free");
      });
   }

   // ===== Shield of Fortitude =====

   @GameTest
   @Covers({"shield_of_faith", "shield_of_resilience"})
   public void shieldAugmentsRaiseAbsorption(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      ShieldOfFortitude shield = (ShieldOfFortitude) ArcanaRegistry.SHIELD_OF_FORTITUDE;
      shield.shieldBlock(plain, ArcanaRegistry.SHIELD_OF_FORTITUDE.getNewItem(), 10000f);
      float plainAbsorb = plain.getAbsorptionAmount();
      helper.assertTrue(plainAbsorb == ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SHIELD_OF_FORTITUDE_HIT_MAX), "A huge blocked hit should give the base maximum");

      ServerPlayer faithful = player(helper);
      shield.shieldBlock(faithful, item(shield, ArcanaAugments.SHIELD_OF_FAITH, 5), 10000f);
      float expected = plainAbsorb + ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.SHIELD_OF_FORTITUDE_HIT_MAX_PER_LVL).get(5);
      helper.assertTrue(faithful.getAbsorptionAmount() == expected, "Shield of Faith 5 gave " + faithful.getAbsorptionAmount() + " instead of " + expected);

      ServerPlayer resilient = player(helper);
      shield.shieldBlock(resilient, item(shield, ArcanaAugments.SHIELD_OF_RESILIENCE, 5), 10f);
      int plainDuration = shieldTimer(plain);
      int duration = shieldTimer(resilient);
      int extra = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION_PER_LVL).get(5);
      helper.assertTrue(duration - plainDuration == extra && extra > 0, "Shield of Resilience 5 added " + (duration - plainDuration) + " ticks instead of " + extra);
      finish(helper, plain, faithful, resilient);
   }

   private static int shieldTimer(ServerPlayer player){
      return BorisLib.SERVER_TIMER_CALLBACKS.stream().filter(callback -> callback instanceof ShieldTimerCallback timer && timer.getPlayer() == player).mapToInt(callback -> callback.getTimer()).max().orElse(-1);
   }

   @GameTest(maxTicks = 20)
   @Covers("built_like_tank")
   public void stackingAHundredHeartsOfAbsorptionGrantsBuiltLikeTank(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ShieldOfFortitude shield = (ShieldOfFortitude) ArcanaRegistry.SHIELD_OF_FORTITUDE;
      ItemStack stack = item(shield, ArcanaAugments.SHIELD_OF_FAITH, 5);
      for(int i = 0; i < 50 && player.getAbsorptionAmount() < 200; i++) shield.shieldBlock(player, stack, 10000f);
      helper.assertTrue(player.getAbsorptionAmount() >= 200, "50 blocks gave " + player.getAbsorptionAmount() + " absorption");
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.BUILT_LIKE_TANK);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest
   @Covers("shield_bash")
   public void shieldBashSpendsAbsorptionOnAVulnerableHit(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ShieldOfFortitude shield = (ShieldOfFortitude) ArcanaRegistry.SHIELD_OF_FORTITUDE;
      IronGolem plainTarget = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      ItemStack plain = ArcanaRegistry.SHIELD_OF_FORTITUDE.getNewItem();
      player.setItemSlot(EquipmentSlot.OFFHAND, plain);
      for(int i = 0; i < 5; i++) shield.shieldBlock(player, plain, 10000f);
      plainTarget.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1f);
      helper.assertTrue(Conditions.getConditionValue(plainTarget, Conditions.VULNERABILITY) == 0, "A hit without Shield Bash made the target vulnerable");

      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 6);
      ItemStack basher = item(shield, ArcanaAugments.SHIELD_BASH, 1);
      player.setItemSlot(EquipmentSlot.OFFHAND, basher);
      target.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1f);
      helper.assertTrue(Conditions.getConditionValue(target, Conditions.VULNERABILITY) > 0, "Shield Bash did not make the target vulnerable");
      helper.assertTrue(player.getCooldowns().isOnCooldown(basher), "Shield Bash started no cooldown");
      finish(helper, player);
   }

   // ===== Sojourner's Boots =====

   private static SojournerBoots boots(){
      return (SojournerBoots) ArcanaRegistry.SOJOURNER_BOOTS;
   }

   private static ServerPlayer runner(GameTestHelper helper, ItemStack stack){
      ServerPlayer player = player(helper);
      player.setItemSlot(EquipmentSlot.FEET, stack);
      player.setSprinting(true);
      player.setOnGround(true);
      return player;
   }

   @GameTest
   @Covers("pheidippides")
   public void sprintingInTheBootsGrantsPheidippides(GameTestHelper helper){
      ServerPlayer player = runner(helper, ArcanaRegistry.SOJOURNER_BOOTS.getNewItem());
      almostComplete(player, ArcanaAchievements.PHEIDIPPIDES, 100);
      player.checkMovementStatistics(1.5, 0, 0);
      assertAchieved(helper, player, ArcanaAchievements.PHEIDIPPIDES);
      finish(helper, player);
   }

   @GameTest
   @Covers("marathon_runner")
   public void marathonRunnerRaisesTheSpeedCap(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.SOJOURNER_BOOTS, ArcanaAugments.MARATHON_RUNNER, 5, ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SOJOURNER_BOOTS_ENERGY_MAX_PER_LVL).get(5));
      helper.succeed();
   }

   @GameTest
   @Covers("sprinter")
   public void sprinterRampsSpeedFaster(GameTestHelper helper){
      ItemStack plain = ArcanaRegistry.SOJOURNER_BOOTS.getNewItem();
      ServerPlayer plainPlayer = runner(helper, plain);
      inventoryTick(helper, plainPlayer, plainPlayer.getItemBySlot(EquipmentSlot.FEET));
      int base = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SOJOURNER_BOOTS_RAMP) + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SOJOURNER_BOOTS_RAMP_PER_LVL).get(0);
      ItemStack fast = item(boots(), ArcanaAugments.SPRINTER, 2);
      ServerPlayer fastPlayer = runner(helper, fast);
      inventoryTick(helper, fastPlayer, fastPlayer.getItemBySlot(EquipmentSlot.FEET));
      int expected = ArcanaNovum.CONFIG.getInt(ArcanaConfig.SOJOURNER_BOOTS_RAMP) + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SOJOURNER_BOOTS_RAMP_PER_LVL).get(2);
      helper.assertTrue(boots().getEnergy(plainPlayer.getItemBySlot(EquipmentSlot.FEET)) == base, "The plain boots gained " + boots().getEnergy(plainPlayer.getItemBySlot(EquipmentSlot.FEET)) + " instead of " + base);
      helper.assertTrue(boots().getEnergy(fastPlayer.getItemBySlot(EquipmentSlot.FEET)) == expected && expected > base, "Sprinter 2 gained the wrong energy");
      finish(helper, plainPlayer, fastPlayer);
   }

   @GameTest
   @Covers("hiking_boots")
   public void hikingBootsRaiseStepHeight(GameTestHelper helper){
      ItemStack plain = ArcanaRegistry.SOJOURNER_BOOTS.getNewItem();
      ItemStack hiking = item(boots(), ArcanaAugments.HIKING_BOOTS, 1);
      ArcanaItem.putProperty(plain, ArcanaItem.ACTIVE_TAG, true);
      ArcanaItem.putProperty(hiking, ArcanaItem.ACTIVE_TAG, true);
      boots().rebuildAttributes(plain);
      boots().rebuildAttributes(hiking);
      ServerPlayer plainPlayer = player(helper);
      plainPlayer.setItemSlot(EquipmentSlot.FEET, plain);
      ServerPlayer hiker = player(helper);
      hiker.setItemSlot(EquipmentSlot.FEET, hiking);
      double plainStep = stepHeight(plain);
      double hikingStep = stepHeight(hiking);
      helper.assertTrue(Math.abs(hikingStep - plainStep - 1.0) < 0.001, "Hiking Boots changed the step bonus from " + plainStep + " to " + hikingStep);
      finish(helper, plainPlayer, hiker);
   }

   private static double stepHeight(ItemStack boots){
      double[] total = {0};
      boots.forEachModifier(EquipmentSlot.FEET, (attribute, modifier) -> {
         if(attribute.equals(Attributes.STEP_HEIGHT)) total[0] += modifier.amount();
      });
      return total[0];
   }

   @GameTest
   @Covers("juggernaut")
   public void juggernautSpendsSpeedOnAVulnerableHit(GameTestHelper helper){
      ItemStack stack = item(boots(), ArcanaAugments.JUGGERNAUT, 1);
      ServerPlayer player = runner(helper, stack);
      ItemStack worn = player.getItemBySlot(EquipmentSlot.FEET);
      boots().setEnergy(worn, 199);
      IronGolem slow = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      slow.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1f);
      helper.assertTrue(Conditions.getConditionValue(slow, Conditions.VULNERABILITY) == 0, "Juggernaut worked below 200 energy");

      boots().setEnergy(worn, 200);
      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 6);
      target.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1f);
      helper.assertTrue(Conditions.getConditionValue(target, Conditions.VULNERABILITY) > 0, "Juggernaut did not make the target vulnerable");
      helper.assertTrue(boots().getEnergy(worn) == 0, "Juggernaut did not spend the speed");
      finish(helper, player);
   }

   // ===== Totem of Vengeance =====

   @GameTest
   @Covers({"revengeance", "retaliative_fury"})
   public void vengeanceTotemTriggersAndFuryStrengthensIt(GameTestHelper helper){
      ServerPlayer player = player(helper);
      TotemOfVengeance totem = (TotemOfVengeance) ArcanaRegistry.TOTEM_OF_VENGEANCE;
      ItemStack stack = ArcanaRegistry.TOTEM_OF_VENGEANCE.getNewItem();
      totem.triggerTotem(stack, player, player.damageSources().generic());
      assertNotAchieved(helper, player, ArcanaAchievements.REVENGEANCE);
      float plainMight = Conditions.getConditionValue(player, Conditions.MIGHT);
      helper.assertTrue(plainMight > 0, "The plain Totem gave no damage boost");

      ServerPlayer furious = player(helper);
      totem.triggerTotem(item(totem, ArcanaAugments.RETALIATIVE_FURY, 3), furious, net.borisshoes.arcananovum.damage.ArcanaDamageTypes.of(helper.getLevel(), net.borisshoes.arcananovum.damage.ArcanaDamageTypes.VENGEANCE_TOTEM));
      helper.assertTrue(Conditions.getConditionValue(furious, Conditions.MIGHT) > plainMight, "Retaliative Fury 3 did not raise the damage boost");
      assertAchieved(helper, furious, ArcanaAchievements.REVENGEANCE);
      finish(helper, player, furious);
   }

   @GameTest
   @Covers("too_angry_to_die")
   public void survivingDamageOnTheDeathWardGrantsTooAngryToDie(GameTestHelper helper){
      ServerPlayer player = player(helper);
      TotemOfVengeance totem = (TotemOfVengeance) ArcanaRegistry.TOTEM_OF_VENGEANCE;
      totem.triggerTotem(ArcanaRegistry.TOTEM_OF_VENGEANCE.getNewItem(), player, player.damageSources().generic());
      helper.assertTrue(ArcanaAchievements.isTimerActive(player, ArcanaAchievements.TOO_ANGRY_TO_DIE), "The Totem did not start the timer");
      player.addEffect(new MobEffectInstance(ArcanaRegistry.DEATH_WARD_EFFECT, 200));
      player.setHealth(2f);
      player.hurtServer(helper.getLevel(), player.damageSources().generic(), 5000f);
      helper.assertTrue(player.isAlive(), "The Death Ward did not keep the player alive");
      assertAchieved(helper, player, ArcanaAchievements.TOO_ANGRY_TO_DIE);
      finish(helper, player);
   }

   // ===== Pickaxe of Ceptyus =====

   private static PickaxeOfCeptyus pick(){
      return (PickaxeOfCeptyus) ArcanaRegistry.PICKAXE_OF_CEPTYUS;
   }

   private static void oreGrid(GameTestHelper helper, int count){
      for(int i = 0; i < count; i++) helper.setBlock(new BlockPos(2 + i % 4, 2 + i / 4, 4), Blocks.DIAMOND_ORE);
   }

   private static int oresLeft(GameTestHelper helper, int count){
      int left = 0;
      for(int i = 0; i < count; i++) if(helper.getBlockState(new BlockPos(2 + i % 4, 2 + i / 4, 4)).is(Blocks.DIAMOND_ORE)) left++;
      return left;
   }

   @GameTest
   @Covers("mine_diamonds")
   public void veinMiningTwelveDiamondsGrantsMineDiamonds(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack stack = ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem();
      oreGrid(helper, 16);
      pick().veinMine(helper.getLevel(), player, stack, helper.absolutePos(new BlockPos(2, 2, 4)));
      assertAchieved(helper, player, ArcanaAchievements.MINE_DIAMONDS);
      finish(helper, player);
   }

   @GameTest
   @Covers("with_the_depths")
   public void withTheDepthsMinesBiggerVeins(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.PICKAXE_OF_CEPTYUS_VEIN_BLOCKS_PER_LVL, List.of(3, 3, 3, 3, 3, 9), () -> {
         oreGrid(helper, 16);
         pick().veinMine(helper.getLevel(), player, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem(), helper.absolutePos(new BlockPos(2, 2, 4)));
         int plainLeft = oresLeft(helper, 16);
         oreGrid(helper, 16);
         pick().veinMine(helper.getLevel(), player, item(pick(), ArcanaAugments.WITH_THE_DEPTHS, 5), helper.absolutePos(new BlockPos(2, 2, 4)));
         int deepLeft = oresLeft(helper, 16);
         helper.assertTrue(deepLeft < plainLeft, "With the Depths 5 left " + deepLeft + " of 16 ores against " + plainLeft);
      });
      finish(helper, player);
   }

   @GameTest
   @Covers("greed")
   public void greedMultipliesVeinDrops(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.PICKAXE_OF_CEPTYUS_FORTUNE_PER_LVL, List.of(0, 0, 0, 200), () -> {
         int plain = veinDrops(helper, player, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem());
         int greedy = veinDrops(helper, player, item(pick(), ArcanaAugments.GREED, 3));
         helper.assertTrue(greedy > plain, "Greed 3 at Fortune 200 dropped " + greedy + " diamonds against " + plain);
      });
      finish(helper, player);
   }

   private static int veinDrops(GameTestHelper helper, ServerPlayer player, ItemStack pickaxe){
      helper.setBlock(new BlockPos(2, 2, 4), Blocks.DIAMOND_ORE);
      helper.setBlock(new BlockPos(3, 2, 4), Blocks.DIAMOND_ORE);
      pick().veinMine(helper.getLevel(), player, pickaxe, helper.absolutePos(new BlockPos(2, 2, 4)));
      int count = 0;
      for(var drop : helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(helper.absolutePos(new BlockPos(2, 2, 4))).inflate(3))){
         if(drop.getItem().is(Items.DIAMOND)){
            count += drop.getItem().getCount();
            drop.discard();
         }
      }
      return count;
   }

   private static int ceptyusEnergy(ServerPlayer player){
      var tag = (net.minecraft.nbt.IntTag) ArcanaNovum.data(player).getMiscData(PickaxeOfCeptyus.CEPTYUS_ENERGY);
      return tag == null ? 0 : tag.intValue();
   }

   @GameTest
   @Covers("wardens_haste")
   public void wardensHasteRaisesEnergyGainAndCap(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      ServerPlayer hasty = player(helper);
      ItemStack hastyPick = item(pick(), ArcanaAugments.WARDENS_HASTE, 5);
      // The first swing only starts the counter
      pick().mining(plain, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem());
      pick().mining(hasty, hastyPick);
      pick().mining(plain, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem());
      pick().mining(hasty, hastyPick);
      helper.assertTrue(ceptyusEnergy(hasty) > ceptyusEnergy(plain), "Warden's Haste 5 gained " + ceptyusEnergy(hasty) + " against " + ceptyusEnergy(plain));
      for(int i = 0; i < 2000; i++){
         pick().mining(plain, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem());
         pick().mining(hasty, hastyPick);
      }
      int expected = ArcanaNovum.CONFIG.getInt(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY) + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY_PER_LVL).get(5);
      helper.assertTrue(ceptyusEnergy(hasty) == expected && ceptyusEnergy(plain) < expected, "Warden's Haste 5 capped at " + ceptyusEnergy(hasty) + " instead of " + expected);
      finish(helper, plain, hasty);
   }

   @GameTest
   @Covers("back_in_the_mine")
   public void miningAtFullHasteGrantsBackInTheMine(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.BACK_IN_THE_MINE, 1);
      ItemStack stack = ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem();
      pick().mining(player, stack);
      int energy = (int) (10 * ArcanaNovum.CONFIG.getDouble(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_PER_HASTE));
      ArcanaNovum.data(player).addMiscData(PickaxeOfCeptyus.CEPTYUS_ENERGY, net.minecraft.nbt.IntTag.valueOf(energy));
      inventoryTick(helper, player, stack);
      assertAchieved(helper, player, ArcanaAchievements.BACK_IN_THE_MINE);
      finish(helper, player);
   }

   @GameTest
   @Covers("diggy_hole")
   public void breakingStoneWithThePickGrantsDiggyHole(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.DIGGY_HOLE, 1);
      player.setItemInHand(InteractionHand.MAIN_HAND, ArcanaRegistry.PICKAXE_OF_CEPTYUS.getNewItem());
      BlockPos stone = new BlockPos(3, 2, 3);
      helper.setBlock(stone, Blocks.STONE);
      player.gameMode.destroyBlock(helper.absolutePos(stone));
      helper.assertBlockNotPresent(Blocks.STONE, stone);
      assertAchieved(helper, player, ArcanaAchievements.DIGGY_HOLE);
      finish(helper, player);
   }

   // ===== Arcanist's Belt =====

   @GameTest
   @Covers({"belt_charming", "pouches"})
   public void fillingTheBeltWithCharmsGrantsBeltCharming(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int[] slots = ArcanistsBelt.ArcanistsBeltItem.BELT_SLOT_COUNT;
      helper.assertTrue(slots[1] - slots[0] == 1 && slots[2] - slots[0] == 2 && slots[3] - slots[0] == 4 && slots[4] - slots[0] == 6, "Pouches should add 1/2/4/6 slots");
      ItemStack belt = item(ArcanaRegistry.ARCANISTS_BELT, ArcanaAugments.POUCHES, 1);
      net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui gui = new net.borisshoes.arcananovum.gui.arcanistsbelt.ArcanistsBeltGui(player, (ArcanistsBelt) ArcanaRegistry.ARCANISTS_BELT, belt, slots[1]);
      gui.build();
      net.minecraft.world.SimpleContainer inv = (net.minecraft.world.SimpleContainer) field(gui, "inv");
      for(int i = 0; i < slots[1] - 1; i++) inv.setItem(i, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      gui.afterRemoval();
      assertNotAchieved(helper, player, ArcanaAchievements.BELT_CHARMING);
      inv.setItem(slots[1] - 1, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      gui.afterRemoval();
      assertAchieved(helper, player, ArcanaAchievements.BELT_CHARMING);
      finish(helper, player);
   }

   @GameTest
   @Covers("mental_padding")
   public void mentalPaddingLowersBeltConcentration(GameTestHelper helper){
      ArcanistsBelt belt = (ArcanistsBelt) ArcanaRegistry.ARCANISTS_BELT;
      double plain = belt.getArcanaItemContainer(belt.getNewItem()).getConcMod();
      double padded = belt.getArcanaItemContainer(item(belt, ArcanaAugments.MENTAL_PADDING, 1)).getConcMod();
      helper.assertTrue(padded == 0.25 && padded < plain, "Mental Padding changed the multiplier from " + plain + " to " + padded);
      helper.succeed();
   }

   // ===== Telescoping Beacon =====

   private static void placeBeacon(GameTestHelper helper, ServerPlayer player, net.minecraft.world.level.block.Block block){
      net.minecraft.nbt.ListTag types = new net.minecraft.nbt.ListTag();
      CompoundTag type = new CompoundTag();
      type.putString("id", net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString());
      type.putInt("count", 164);
      types.add(type);
      call(ArcanaRegistry.TELESCOPING_BEACON, "placeBeacon", player, helper.getLevel(), helper.absolutePos(new BlockPos(4, 6, 4)), 4, types, null, false);
   }

   @GameTest
   @Covers({"bejeweled", "art_of_the_deal", "acquisition_rules", "clinically_insane"})
   public void fullBeaconsOfEachMetalGrantTheirAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      placeBeacon(helper, player, Blocks.IRON_BLOCK);
      for(ArcanaAchievement achievement : List.of(ArcanaAchievements.BEJEWELED, ArcanaAchievements.ART_OF_THE_DEAL, ArcanaAchievements.ACQUISITION_RULES, ArcanaAchievements.CLINICALLY_INSANE)){
         assertNotAchieved(helper, player, achievement);
      }
      placeBeacon(helper, player, Blocks.DIAMOND_BLOCK);
      assertAchieved(helper, player, ArcanaAchievements.BEJEWELED);
      placeBeacon(helper, player, Blocks.EMERALD_BLOCK);
      assertAchieved(helper, player, ArcanaAchievements.ART_OF_THE_DEAL);
      placeBeacon(helper, player, Blocks.GOLD_BLOCK);
      assertAchieved(helper, player, ArcanaAchievements.ACQUISITION_RULES);
      placeBeacon(helper, player, Blocks.NETHERITE_BLOCK);
      assertAchieved(helper, player, ArcanaAchievements.CLINICALLY_INSANE);
      finish(helper, player);
   }

   // ===== Wings of Enderia =====

   private static ServerPlayer wearingWings(GameTestHelper helper, ArcanaAugment augment, int level){
      ServerPlayer player = player(helper);
      ItemStack wings = augment == null ? ArcanaRegistry.WINGS_OF_ENDERIA.getNewItem() : item(ArcanaRegistry.WINGS_OF_ENDERIA, augment, level);
      WingsOfEnderia type = (WingsOfEnderia) ArcanaRegistry.WINGS_OF_ENDERIA;
      type.setEnergy(wings, type.getMaxEnergy(wings));
      player.setItemSlot(EquipmentSlot.CHEST, wings);
      return player;
   }

   @GameTest
   @Covers("see_glass")
   public void survivingALethalCrashGrantsSeeGlass(GameTestHelper helper){
      ServerPlayer player = wearingWings(helper, null, 0);
      player.setHealth(10f);
      player.hurtServer(helper.getLevel(), player.damageSources().flyIntoWall(), 15f);
      helper.assertTrue(player.isAlive(), "The Wings did not cushion the crash");
      assertAchieved(helper, player, ArcanaAchievements.SEE_GLASS);
      finish(helper, player);
   }

   @GameTest
   @Covers("angel_of_death")
   public void killingWhileGlidingGrantsAngelOfDeath(GameTestHelper helper){
      ServerPlayer player = wearingWings(helper, null, 0);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 4, 2, 4);
      killWithHeld(helper, player, zombie);
      assertNotAchieved(helper, player, ArcanaAchievements.ANGEL_OF_DEATH);
      player.startFallFlying();
      killWithHeld(helper, player, zombie);
      assertAchieved(helper, player, ArcanaAchievements.ANGEL_OF_DEATH);
      finish(helper, player);
   }

   @GameTest
   @Covers("crow_father")
   public void wearingParrotsGrantsCrowFather(GameTestHelper helper){
      ServerPlayer player = wearingWings(helper, null, 0);
      CompoundTag parrot = new CompoundTag();
      parrot.putString("id", "minecraft:parrot");
      call(player, "setShoulderEntityLeft", parrot);
      inventoryTick(helper, player, player.getItemBySlot(EquipmentSlot.CHEST));
      assertNotAchieved(helper, player, ArcanaAchievements.CROW_FATHER);
      call(player, "setShoulderEntityRight", parrot.copy());
      inventoryTick(helper, player, player.getItemBySlot(EquipmentSlot.CHEST));
      assertAchieved(helper, player, ArcanaAchievements.CROW_FATHER);
      finish(helper, player);
   }

   @GameTest
   @Covers("scales_of_the_champion")
   public void scalesOfTheChampionCushionAllDamage(GameTestHelper helper){
      ServerPlayer plain = wearingWings(helper, null, 0);
      plain.hurtServer(helper.getLevel(), plain.damageSources().generic(), 8f);
      WingsOfEnderia wings = (WingsOfEnderia) ArcanaRegistry.WINGS_OF_ENDERIA;
      helper.assertTrue(wings.getEnergy(plain.getItemBySlot(EquipmentSlot.CHEST)) == wings.getMaxEnergy(plain.getItemBySlot(EquipmentSlot.CHEST)), "Plain Wings should only cushion falls and crashes");

      ServerPlayer scaled = wearingWings(helper, ArcanaAugments.SCALES_OF_THE_CHAMPION, 2);
      helper.assertTrue(net.borisshoes.arcananovum.utils.EnhancedStatUtils.isEnhanced(scaled.getItemBySlot(EquipmentSlot.CHEST)), "Level 1 should infuse the Wings with Stardust");
      scaled.hurtServer(helper.getLevel(), scaled.damageSources().generic(), 8f);
      helper.assertTrue(wings.getEnergy(scaled.getItemBySlot(EquipmentSlot.CHEST)) < wings.getMaxEnergy(scaled.getItemBySlot(EquipmentSlot.CHEST)), "Scales 2 did not cushion ordinary damage");
      finish(helper, plain, scaled);
   }

   @GameTest
   @Covers("wing_buffet")
   public void wingBuffetPushesMobsAway(GameTestHelper helper){
      ServerPlayer plain = wearingWings(helper, null, 0);
      Zombie near = mob(helper, EntityTypes.ZOMBIE, 2.5, 2, 2.5);
      plain.hurtServer(helper.getLevel(), plain.damageSources().generic(), 1f);
      helper.assertTrue(near.getDeltaMovement().length() == 0, "Plain Wings pushed a mob");

      ServerPlayer buffeting = wearingWings(helper, ArcanaAugments.WING_BUFFET, 1);
      buffeting.hurtServer(helper.getLevel(), buffeting.damageSources().generic(), 1f);
      helper.assertTrue(near.getDeltaMovement().length() > 0, "Wing Buffet did not push the mob");
      finish(helper, plain, buffeting);
   }

   // The Wing Buffet search only kept Mobs, so its branch for players (level 2) could never run
   @GameTest
   public void wingBuffetLevelTwoPushesPlayers(GameTestHelper helper){
      ServerPlayer buffeting = wearingWings(helper, ArcanaAugments.WING_BUFFET, 2);
      ServerPlayer other = player(helper);
      other.teleportTo(other.getX() + 1, other.getY(), other.getZ());
      other.setDeltaMovement(Vec3.ZERO);
      buffeting.hurtServer(helper.getLevel(), buffeting.damageSources().generic(), 1f);
      helper.assertTrue(other.getDeltaMovement().length() > 0, "Wing Buffet 2 did not push the other player");
      finish(helper, buffeting, other);
   }

   // ===== Graviton Maul =====

   @GameTest
   @Covers("quick_way_down")
   public void plummetingThreeHundredBlocksGrantsQuickWayDown(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack maul = ArcanaRegistry.GRAVITON_MAUL.getNewItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, maul);
      player.setOnGround(true);
      ArcanaItem.putProperty(maul, ArcanaItem.MODE_TAG, 1);
      ArcanaItem.putProperty(maul, GravitonMaul.FALL_START_HEIGHT_TAG, player.getY() + 310);
      call(ArcanaRegistry.GRAVITON_MAUL, "gravityEffects", player, maul);
      assertAchieved(helper, player, ArcanaAchievements.QUICK_WAY_DOWN);
      finish(helper, player);
   }

   @GameTest
   @Covers("raise_the_roof")
   public void surfingACrushWithTheMaulGrantsRaiseTheRoof(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack maul = ArcanaRegistry.GRAVITON_MAUL.getNewItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, maul);
      ArcanaItem.putProperty(maul, ArcanaItem.MODE_TAG, 0);
      player.setHealth(ArcanaNovum.CONFIG.getFloat(ArcanaConfig.GRAVITON_MAUL_CRUSH_DMG) + 1);
      player.verticalCollision = true;
      player.verticalCollisionBelow = false;
      call(ArcanaRegistry.GRAVITON_MAUL, "gravityEffects", player, maul);
      assertAchieved(helper, player, ArcanaAchievements.RAISE_THE_ROOF);
      finish(helper, player);
   }

   @GameTest
   @Covers("gravitic_domain")
   public void graviticDomainLiftsNearbyCreatures(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 2.5, 2, 2.5);
      player.setOnGround(true);
      use(helper, player, ArcanaRegistry.GRAVITON_MAUL.getNewItem());
      helper.assertTrue(zombie.getDeltaMovement().y == 0, "A plain Maul lifted a nearby mob");
      player.setOnGround(true);
      use(helper, player, item(ArcanaRegistry.GRAVITON_MAUL, ArcanaAugments.GRAVITIC_DOMAIN, 1));
      helper.assertTrue(zombie.getDeltaMovement().y > 0, "Gravitic Domain did not lift the mob");
      finish(helper, player);
   }

   @GameTest
   @Covers("singularity_maelstrom")
   public void singularityMaelstromIsASneakUseMode(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setOnGround(true);
      player.setShiftKeyDown(true);
      ItemStack plain = ArcanaRegistry.GRAVITON_MAUL.getNewItem();
      use(helper, player, plain);
      helper.assertTrue(ArcanaItem.getIntProperty(plain, ArcanaItem.MODE_TAG) == 0, "A plain Maul entered maelstrom mode");
      ItemStack maelstrom = item(ArcanaRegistry.GRAVITON_MAUL, ArcanaAugments.SINGULARITY_MAELSTROM, 1);
      player.setOnGround(true);
      use(helper, player, maelstrom);
      helper.assertTrue(ArcanaItem.getIntProperty(maelstrom, ArcanaItem.MODE_TAG) == 2, "Sneak using the augmented Maul did not enter maelstrom mode");
      finish(helper, player);
   }

   // ===== Eversources =====

   private static ServerPlayer lookingDown(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
      Vec3 pos = player.position();
      player.teleportTo(helper.getLevel(), pos.x, pos.y, pos.z, java.util.Set.of(), 0, 90, true);
      return player;
   }

   @GameTest
   @Covers("pocket_ocean")
   public void placingWaterGrantsPocketOcean(GameTestHelper helper){
      ServerPlayer player = lookingDown(helper);
      almostComplete(player, ArcanaAchievements.POCKET_OCEAN, 1);
      use(helper, player, ArcanaRegistry.AQUATIC_EVERSOURCE.getNewItem());
      helper.assertBlockPresent(Blocks.WATER, new BlockPos(1, 2, 1));
      assertAchieved(helper, player, ArcanaAchievements.POCKET_OCEAN);
      finish(helper, player);
   }

   @GameTest
   @Covers("floodgate")
   public void floodgatePlacesAThreeByThree(GameTestHelper helper){
      ServerPlayer player = lookingDown(helper);
      player.setShiftKeyDown(true);
      ItemStack plain = ArcanaRegistry.AQUATIC_EVERSOURCE.getNewItem();
      use(helper, player, plain);
      use(helper, player, plain);
      helper.assertTrue(ArcanaItem.getIntProperty(plain, ArcanaItem.MODE_TAG) == 0, "A plain Eversource has a third mode");

      ItemStack flood = item(ArcanaRegistry.AQUATIC_EVERSOURCE, ArcanaAugments.FLOODGATE, 1);
      use(helper, player, flood);
      use(helper, player, flood);
      helper.assertTrue(ArcanaItem.getIntProperty(flood, ArcanaItem.MODE_TAG) == 2, "Floodgate unlocked no third mode");
      player.setShiftKeyDown(false);
      for(int x = 0; x <= 2; x++) for(int z = 0; z <= 2; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
      use(helper, player, flood);
      helper.assertBlockPresent(Blocks.WATER, new BlockPos(0, 2, 0));
      helper.assertBlockPresent(Blocks.WATER, new BlockPos(2, 2, 2));
      finish(helper, player);
   }

   @GameTest
   @Covers("hellgate")
   public void placingLavaGrantsHellgate(GameTestHelper helper){
      ServerPlayer player = lookingDown(helper);
      almostComplete(player, ArcanaAchievements.HELLGATE, 1);
      ItemStack stack = ArcanaRegistry.MAGMATIC_EVERSOURCE.getNewItem();
      ArcanaItem.putProperty(stack, MagmaticEversource.USES_TAG, 1);
      use(helper, player, stack);
      helper.assertBlockPresent(Blocks.LAVA, new BlockPos(1, 2, 1));
      helper.assertTrue(ArcanaItem.getIntProperty(stack, MagmaticEversource.USES_TAG) == 0, "Placing lava should use a charge");
      assertAchieved(helper, player, ArcanaAchievements.HELLGATE);
      finish(helper, player);
   }

   @GameTest
   @Covers({"eruption", "volcanic_chamber"})
   public void magmaticAugmentsChangeCooldownAndCharges(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.MAGMATIC_EVERSOURCE, ArcanaAugments.ERUPTION, 3, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.MAGMATIC_EVERSOURCE_COOLDOWN_PER_LVL).get(3));
      MagmaticEversource source = (MagmaticEversource) ArcanaRegistry.MAGMATIC_EVERSOURCE;
      int expected = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.MAGMATIC_EVERSOURCE_CHARGES_PER_LVL).get(4);
      helper.assertTrue(source.getMaxCharges(item(source, ArcanaAugments.VOLCANIC_CHAMBER, 4)) == expected && expected > source.getMaxCharges(source.getNewItem()), "Volcanic Chamber 4 has the wrong charge count");
      helper.succeed();
   }

   // ===== Spawner Harness =====

   private static void harnessSpawner(GameTestHelper helper, ServerPlayer player, ItemStack harness, EntityType<?> type){
      BlockPos spawner = new BlockPos(3, 2, 3);
      helper.setBlock(spawner, Blocks.SPAWNER);
      helper.getBlockEntity(spawner, net.minecraft.world.level.block.entity.SpawnerBlockEntity.class).setEntityId(type, helper.getLevel().getRandom());
      useOn(helper, player, harness, spawner, net.minecraft.core.Direction.UP);
   }

   @GameTest
   @Covers("finally_useful")
   public void capturingASilverfishSpawnerGrantsFinallyUseful(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.SPAWNER_HARNESS_BREAK_PERCENT, 0.0, () -> {
         harnessSpawner(helper, player, ArcanaRegistry.SPAWNER_HARNESS.getNewItem(), EntityTypes.ZOMBIE);
         assertNotAchieved(helper, player, ArcanaAchievements.FINALLY_USEFUL);
         harnessSpawner(helper, player, ArcanaRegistry.SPAWNER_HARNESS.getNewItem(), EntityTypes.SILVERFISH);
      });
      assertAchieved(helper, player, ArcanaAchievements.FINALLY_USEFUL);
      finish(helper, player);
   }

   @GameTest
   @Covers("reinforced_chassis")
   public void reinforcedChassisNeverBreaks(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.SPAWNER_HARNESS_BREAK_PERCENT, 1.0, () -> {
         // The first click picks the spawner up, the second puts it down, and that is when the Harness can break
         helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
         ItemStack plain = ArcanaRegistry.SPAWNER_HARNESS.getNewItem();
         harnessSpawner(helper, player, plain, EntityTypes.ZOMBIE);
         useOn(helper, player, plain, new BlockPos(5, 2, 5), net.minecraft.core.Direction.UP);
         helper.assertTrue(plain.isEmpty(), "A plain Harness at break chance 1 survived");
         helper.setBlock(new BlockPos(5, 3, 5), Blocks.AIR);
         ItemStack reinforced = item(ArcanaRegistry.SPAWNER_HARNESS, ArcanaAugments.REINFORCED_CHASSIS, 1);
         harnessSpawner(helper, player, reinforced, EntityTypes.ZOMBIE);
         useOn(helper, player, reinforced, new BlockPos(5, 2, 5), net.minecraft.core.Direction.UP);
         helper.assertFalse(reinforced.isEmpty(), "Reinforced Chassis broke");
      });
      finish(helper, player);
   }

   // ===== Greaves of Gaialtus =====

   private static ServerPlayer wearingGreaves(GameTestHelper helper, ItemStack contents, ArcanaAugment augment, int level){
      ServerPlayer player = player(helper);
      ItemStack greaves = augment == null ? ArcanaRegistry.GREAVES_OF_GAIALTUS.getNewItem() : item(ArcanaRegistry.GREAVES_OF_GAIALTUS, augment, level);
      ArcanaItem.putProperty(greaves, ArcanaItem.ACTIVE_TAG, true);
      greaves.set(net.minecraft.core.component.DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(contents)));
      player.setItemSlot(EquipmentSlot.LEGS, greaves);
      return player;
   }

   private static void placeFromHand(GameTestHelper helper, ServerPlayer player, ItemStack hand){
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
      useOn(helper, player, hand, new BlockPos(3, 2, 3), net.minecraft.core.Direction.UP);
   }

   @GameTest
   @Covers("miners_wallet")
   public void refillingDiamondBlocksGrantsMinersWallet(GameTestHelper helper){
      ServerPlayer player = wearingGreaves(helper, new ItemStack(Items.DIAMOND_BLOCK, 64), null, 0);
      placeFromHand(helper, player, new ItemStack(Items.DIAMOND_BLOCK, 2));
      assertAchieved(helper, player, ArcanaAchievements.MINERS_WALLET);
      helper.assertTrue(player.getInventory().countItem(Items.DIAMOND_BLOCK) > 1, "The Greaves did not refill the hand");
      finish(helper, player);
   }

   @GameTest
   @Covers("terraformer")
   public void refillingDirtGrantsTerraformer(GameTestHelper helper){
      ServerPlayer player = wearingGreaves(helper, new ItemStack(Items.DIRT, 64), null, 0);
      almostComplete(player, ArcanaAchievements.TERRAFORMER, 1);
      placeFromHand(helper, player, new ItemStack(Items.DIRT, 2));
      assertAchieved(helper, player, ArcanaAchievements.TERRAFORMER);
      finish(helper, player);
   }

   @GameTest
   @Covers("planetary_pockets")
   public void planetaryPocketsAddNineSlotsPerLevel(GameTestHelper helper){
      int[] slots = GreavesOfGaialtus.GreavesOfGaialtusItem.GREAVES_SLOT_COUNT;
      for(int i = 1; i < slots.length; i++) helper.assertTrue(slots[i] - slots[i - 1] == 9, "Level " + i + " should add 9 slots");
      helper.succeed();
   }

   @GameTest
   @Covers({"natures_embrace", "creators_touch", "starless_domain", "deaths_champion"})
   public void infusionAugmentsFullyEnhanceTheirItems(GameTestHelper helper){
      for(var entry : List.of(List.of(ArcanaRegistry.GREAVES_OF_GAIALTUS, ArcanaAugments.NATURES_EMBRACE), List.of(ArcanaRegistry.SPEAR_OF_TENBROUS, ArcanaAugments.STARLESS_DOMAIN), List.of(ArcanaRegistry.NUL_MEMENTO, ArcanaAugments.DEATHS_CHAMPION))){
         ItemStack stack = item((ArcanaItem) entry.get(0), (ArcanaAugment) entry.get(1), 1);
         helper.assertTrue(net.borisshoes.arcananovum.utils.EnhancedStatUtils.isEnhanced(stack) && ArcanaItem.getDoubleProperty(stack, net.borisshoes.arcananovum.utils.EnhancedStatUtils.ENHANCED_STAT_TAG) >= 1, ((ArcanaAugment) entry.get(1)).id + " did not fully infuse the item");
      }
      ItemStack touch = item(ArcanaRegistry.GREAVES_OF_GAIALTUS, ArcanaAugments.CREATORS_TOUCH, 1);
      boolean[] reach = {false};
      touch.forEachModifier(EquipmentSlot.LEGS, (attribute, modifier) -> {
         if(attribute.equals(Attributes.BLOCK_INTERACTION_RANGE) && modifier.amount() > 0) reach[0] = true;
      });
      helper.assertTrue(reach[0], "Creator's Touch added no block reach");
      helper.succeed();
   }

   @GameTest
   @Covers("earthen_ascent")
   public void earthenAscentClimbsWalls(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setShiftKeyDown(true);
      player.horizontalCollision = true;
      player.setItemSlot(EquipmentSlot.LEGS, ArcanaRegistry.GREAVES_OF_GAIALTUS.getNewItem());
      helper.assertFalse(player.onClimbable(), "Plain Greaves climbed a wall");
      player.setItemSlot(EquipmentSlot.LEGS, item(ArcanaRegistry.GREAVES_OF_GAIALTUS, ArcanaAugments.EARTHEN_ASCENT, 1));
      helper.assertTrue(player.onClimbable(), "Earthen Ascent did not climb the wall");
      finish(helper, player);
   }

   @GameTest
   @Covers("winds_grace")
   public void windsGraceStepsLightly(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setItemSlot(EquipmentSlot.LEGS, ArcanaRegistry.GREAVES_OF_GAIALTUS.getNewItem());
      helper.assertFalse(player.isSteppingCarefully(), "Plain Greaves step carefully");
      player.setItemSlot(EquipmentSlot.LEGS, item(ArcanaRegistry.GREAVES_OF_GAIALTUS, ArcanaAugments.WINDS_GRACE, 1));
      helper.assertTrue(player.isSteppingCarefully(), "Wind's Grace does not step carefully");
      finish(helper, player);
   }

   // ===== Spear of Tenbrous =====

   @GameTest
   @Covers("kill_them_all")
   public void killingEndCreaturesWithTheSpearGrantsKillThemAll(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setItemInHand(InteractionHand.MAIN_HAND, ArcanaRegistry.SPEAR_OF_TENBROUS.getNewItem());
      for(EntityType<? extends LivingEntity> type : List.of(EntityTypes.ENDERMAN, EntityTypes.ENDERMITE, EntityTypes.SHULKER)){
         killWithHeld(helper, player, mob(helper, (EntityType<? extends net.minecraft.world.entity.Mob>) type, 4, 2, 4));
      }
      assertNotAchieved(helper, player, ArcanaAchievements.KILL_THEM_ALL);
      killWithHeld(helper, player, EntityTypes.ENDER_DRAGON.create(helper.getLevel(), EntitySpawnReason.TRIGGERED));
      assertAchieved(helper, player, ArcanaAchievements.KILL_THEM_ALL);
      finish(helper, player);
   }

   // ===== Nul Memento =====

   @GameTest
   @Covers("deaths_door")
   public void wardingOffDeathGrantsDeathsDoor(GameTestHelper helper){
      ServerPlayer player = player(helper);
      NulMemento memento = (NulMemento) ArcanaRegistry.NUL_MEMENTO;
      ItemStack stack = ArcanaRegistry.NUL_MEMENTO.getNewItem();
      ArcanaItem.putProperty(stack, ArcanaItem.ACTIVE_TAG, true);
      memento.protectFromDeath(stack, player, player.damageSources().generic(), false);
      assertAchieved(helper, player, ArcanaAchievements.DEATHS_DOOR);
      finish(helper, player);
   }

   @GameTest
   @Covers("tempo_mortuus")
   public void tempoMortuusShortensTheWardCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.NUL_MEMENTO, ArcanaAugments.TEMPO_MORTUUS, 2, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.NUL_MEMENTO_WARD_COOLDOWN_PER_LVL).get(2));
      helper.succeed();
   }

   // ===== Levitation Harness =====

   private static LevitationHarness harness(){
      return (LevitationHarness) ArcanaRegistry.LEVITATION_HARNESS;
   }

   private static ServerPlayer flyingInHarness(GameTestHelper helper, ArcanaAugment augment, int level){
      ServerPlayer player = player(helper);
      ItemStack stack = augment == null ? ArcanaRegistry.LEVITATION_HARNESS.getNewItem() : item(ArcanaRegistry.LEVITATION_HARNESS, augment, level);
      player.setItemSlot(EquipmentSlot.CHEST, stack);
      player.getAbilities().flying = true;
      return player;
   }

   private static void hit(GameTestHelper helper, ServerPlayer player){
      player.hurtServer(helper.getLevel(), player.damageSources().generic(), 1f);
   }

   @GameTest
   @Covers("sturdy_construction")
   public void sturdyConstructionResistsStalling(GameTestHelper helper){
      withConfig(ArcanaConfig.LEVITATION_HARNESS_DURABILITY_CHANCE, List.of(0.0, 0.0, 0.0, 1.0), () -> {
         ServerPlayer plain = flyingInHarness(helper, null, 0);
         hit(helper, plain);
         helper.assertTrue(harness().getStall(plain.getItemBySlot(EquipmentSlot.CHEST)) > 0, "A hit did not stall the plain Harness");
         ServerPlayer sturdy = flyingInHarness(helper, ArcanaAugments.STURDY_CONSTRUCTION, 3);
         hit(helper, sturdy);
         helper.assertTrue(harness().getStall(sturdy.getItemBySlot(EquipmentSlot.CHEST)) <= 0, "Sturdy Construction 3 at chance 1 stalled");
         finish(helper, plain, sturdy);
      });
   }

   @GameTest
   @Covers("fast_reboot")
   public void fastRebootShortensTheStall(GameTestHelper helper){
      ServerPlayer plain = flyingInHarness(helper, null, 0);
      ServerPlayer fast = flyingInHarness(helper, ArcanaAugments.FAST_REBOOT, 4);
      withConfig(ArcanaConfig.LEVITATION_HARNESS_DURABILITY_CHANCE, List.of(0.0, 0.0, 0.0, 0.0), () -> {
         hit(helper, plain);
         hit(helper, fast);
      });
      int plainStall = harness().getStall(plain.getItemBySlot(EquipmentSlot.CHEST));
      int fastStall = harness().getStall(fast.getItemBySlot(EquipmentSlot.CHEST));
      helper.assertTrue(fastStall == ArcanaNovum.CONFIG.getIntList(ArcanaConfig.LEVITATION_HARNESS_REBOOT_SPEED_PER_LVL).get(4) && fastStall < plainStall, "Fast Reboot 4 stalled for " + fastStall + " against " + plainStall);
      finish(helper, plain, fast);
   }

   @GameTest
   @Covers("emergency_protocol")
   public void emergencyProtocolLevitatesOnStall(GameTestHelper helper){
      ServerPlayer plain = flyingInHarness(helper, null, 0);
      ServerPlayer emergency = flyingInHarness(helper, ArcanaAugments.EMERGENCY_PROTOCOL, 1);
      withConfig(ArcanaConfig.LEVITATION_HARNESS_DURABILITY_CHANCE, List.of(0.0, 0.0, 0.0, 0.0), () -> {
         hit(helper, plain);
         hit(helper, emergency);
      });
      helper.assertFalse(plain.hasEffect(MobEffects.LEVITATION), "A plain Harness levitated on a stall");
      helper.assertTrue(emergency.hasEffect(MobEffects.LEVITATION), "Emergency Protocol gave no Levitation");
      finish(helper, plain, emergency);
   }

   // ===== Shulker Core =====

   @GameTest
   @Covers("shulker_recycler")
   public void shulkerRecyclerSavesSoulsAtItsChance(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ShulkerCore core = (ShulkerCore) ArcanaRegistry.SHULKER_CORE;
      withConfig(ArcanaConfig.HARNESS_CORE_RECYCLER_EFFICIENCY, List.of(0.0, 0.0, 0.0, 1.0), () -> {
         ItemStack plain = ArcanaRegistry.SHULKER_CORE.getNewItem();
         core.setEnergy(plain, 1000);
         player.setItemInHand(InteractionHand.MAIN_HAND, plain);
         call(core, "levitate", player, helper.getLevel(), InteractionHand.MAIN_HAND);
         helper.assertTrue(core.getEnergy(plain) < 1000, "A plain Core used no souls");

         player.removeEffect(MobEffects.LEVITATION);
         ItemStack recycling = item(core, ArcanaAugments.SHULKER_RECYCLER, 3);
         core.setEnergy(recycling, 1000);
         player.setItemInHand(InteractionHand.MAIN_HAND, recycling);
         call(core, "levitate", player, helper.getLevel(), InteractionHand.MAIN_HAND);
         helper.assertTrue(player.hasEffect(MobEffects.LEVITATION), "The Core did not levitate");
         helper.assertTrue(core.getEnergy(recycling) == 1000, "Shulker Recycler 3 at chance 1 used souls");
      });
      finish(helper, player);
   }

   // ===== Stasis Pearl entity =====

   private static net.borisshoes.arcananovum.entities.StasisPearlEntity thrownPearl(GameTestHelper helper, ServerPlayer player, ItemStack stack){
      player.getInventory().setItem(0, stack);
      var pearl = new net.borisshoes.arcananovum.entities.StasisPearlEntity(helper.getLevel(), player, ArcanaItem.getUUID(stack), ArcanaItem.getCompoundProperty(stack, ArcanaItem.AUGMENTS_TAG));
      helper.getLevel().addFreshEntity(pearl);
      return pearl;
   }

   @GameTest
   @Covers("pearl_hang")
   public void holdingAPearlInStasisGrantsPearlHang(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var pearl = thrownPearl(helper, player, ArcanaRegistry.STASIS_PEARL.getNewItem());
      pearl.setStasis(true);
      setField(pearl, "stasisTime", 5999);
      pearl.setStasis(false);
      assertNotAchieved(helper, player, ArcanaAchievements.PEARL_HANG);
      pearl.setStasis(true);
      setField(pearl, "stasisTime", 6000);
      pearl.setStasis(false);
      assertAchieved(helper, player, ArcanaAchievements.PEARL_HANG);
      pearl.discard();
      finish(helper, player);
   }

   @GameTest
   @Covers({"instant_transmission", "stasis_reconstruction"})
   public void teleportingFarGrantsInstantTransmission(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var pearl = thrownPearl(helper, player, item(ArcanaRegistry.STASIS_PEARL, ArcanaAugments.STASIS_RECONSTRUCTION, 3));
      pearl.snapTo(player.getX(), player.getY(), player.getZ() + 1200, 0, 0);
      call(pearl, "onHit", new net.minecraft.world.phys.BlockHitResult(pearl.position(), net.minecraft.core.Direction.UP, pearl.blockPosition(), false));
      assertAchieved(helper, player, ArcanaAchievements.INSTANT_TRANSMISSION);
      helper.assertTrue(Conditions.getConditionValue(player, Conditions.FORTITUDE) > 0, "Stasis Reconstruction gave no damage reduction");
      helper.assertTrue(Conditions.getConditionValue(player, Conditions.REJUVENATION) > 0, "Stasis Reconstruction gave no healing");
      finish(helper, player);
   }

   // ===== Sojourner's Boots achievement =====

   @GameTest
   @Covers("running")
   public void runningAtFullSpeedForFiveMinutesGrantsRunning(GameTestHelper helper){
      ItemStack stack = ArcanaRegistry.SOJOURNER_BOOTS.getNewItem();
      ServerPlayer player = runner(helper, stack);
      ItemStack worn = player.getItemBySlot(EquipmentSlot.FEET);
      boots().setEnergy(worn, boots().getMaxEnergy(worn));
      // One event per tick at top speed, so this skips to the last tick of the run
      int goal = ((net.borisshoes.arcananovum.achievements.TimedAchievement) ArcanaAchievements.RUNNING).getGoal();
      for(int i = 0; i < goal - 1; i++) net.borisshoes.borislib.events.Event.addEvent(new net.borisshoes.arcananovum.events.SojournersMaxRunEvent(player));
      assertNotAchieved(helper, player, ArcanaAchievements.RUNNING);
      inventoryTick(helper, player, worn);
      assertAchieved(helper, player, ArcanaAchievements.RUNNING);
      finish(helper, player);
   }

   // ===== Soulstone unattuning =====

   @GameTest(maxTicks = 30)
   @Covers("soul_annihilation")
   public void soulAnnihilationUnattunesInSoulFire(GameTestHelper helper){
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.SOUL_SAND);
      helper.setBlock(new BlockPos(5, 2, 3), Blocks.SOUL_SAND);
      helper.setBlock(new BlockPos(3, 3, 3), Blocks.SOUL_FIRE);
      helper.setBlock(new BlockPos(5, 3, 3), Blocks.SOUL_FIRE);
      Vec3 plainPos = helper.absoluteVec(new Vec3(5.5, 3.2, 3.5));
      Vec3 pos = helper.absoluteVec(new Vec3(3.5, 3.2, 3.5));
      var plain = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(), plainPos.x, plainPos.y, plainPos.z, Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityTypes.ZOMBIE), 0, 0, 0);
      var annihilating = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, Soulstone.setType(item(ArcanaRegistry.SOULSTONE, ArcanaAugments.SOUL_ANNIHILATION, 1), EntityTypes.ZOMBIE), 0, 0, 0);
      plain.setNoGravity(true);
      annihilating.setNoGravity(true);
      helper.getLevel().addFreshEntity(plain);
      helper.getLevel().addFreshEntity(annihilating);
      helper.succeedWhen(() -> {
         helper.assertTrue(Soulstone.getType(annihilating.getItem()).equals("unattuned"), "Waiting for Soul Annihilation to unattune the stone");
         helper.assertFalse(Soulstone.getType(plain.getItem()).equals("unattuned"), "A plain stone was unattuned");
         plain.discard();
         annihilating.discard();
      });
   }

   // ===== Negotiation Charm mixins =====

   @GameTest
   @Covers("wolf_of_block_street")
   public void barteringWithTheCharmGrantsWolfOfBlockStreet(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.getInventory().setItem(0, ArcanaRegistry.NEGOTIATION_CHARM.getNewItem());
      almostComplete(player, ArcanaAchievements.WOLF_OF_BLOCK_STREET, 1);
      var piglin = mob(helper, EntityTypes.PIGLIN, 2.5, 2, 2.5);
      withConfig(ArcanaConfig.NEGOTIATION_CHARM_BARTER_BUFF_MULTIPLIER, 50.0, () -> {
         for(int i = 0; i < 40 && !achieved(player, ArcanaAchievements.WOLF_OF_BLOCK_STREET); i++){
            callStatic(net.minecraft.world.entity.monster.piglin.PiglinAi.class, "getBarterResponseItems", piglin);
         }
      });
      assertAchieved(helper, player, ArcanaAchievements.WOLF_OF_BLOCK_STREET);
      finish(helper, player);
   }

   private static int tradeUses(GameTestHelper helper, ServerPlayer player){
      Villager villager = mob(helper, EntityTypes.VILLAGER, 3, 2, 3);
      var offer = new net.minecraft.world.item.trading.MerchantOffer(new net.minecraft.world.item.trading.ItemCost(Items.EMERALD, 1), new ItemStack(Items.STICK), 10, 1, 0f);
      var offers = new net.minecraft.world.item.trading.MerchantOffers();
      offers.add(offer);
      villager.setOffers(offers);
      villager.setTradingPlayer(player);
      var container = new net.minecraft.world.inventory.MerchantContainer(villager);
      container.setItem(0, new ItemStack(Items.EMERALD, 8));
      var slot = new net.minecraft.world.inventory.MerchantResultSlot(player, villager, container, 2, 0, 0);
      slot.onTake(player, container.getItem(2));
      int uses = offer.getUses();
      villager.discard();
      return uses;
   }

   @GameTest
   @Covers("extortion")
   public void extortionKeepsTradesInStock(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      helper.assertTrue(tradeUses(helper, plain) == 1, "A trade should use up stock without the augment");
      ServerPlayer extorting = player(helper);
      extorting.getInventory().setItem(0, item(ArcanaRegistry.NEGOTIATION_CHARM, ArcanaAugments.EXTORTION, 1));
      helper.assertTrue(tradeUses(helper, extorting) == 0, "Extortion let the trade use up stock");
      finish(helper, plain, extorting);
   }

   // ===== Felidae Charm mixin =====

   @GameTest
   @Covers("panthera")
   public void pantheraHidesItsHolderFromPhantoms(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var spawner = new net.minecraft.world.level.levelgen.PhantomSpawner();
      player.getInventory().setItem(0, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      helper.assertFalse((boolean) callMixin(spawner, "arcananovum$fuckPhantoms", false, player), "A plain charm hid its holder from phantoms");
      player.getInventory().setItem(0, item(ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.PANTHERA, 1));
      helper.assertTrue((boolean) callMixin(spawner, "arcananovum$fuckPhantoms", false, player), "Panthera did not hide its holder from phantoms");
      finish(helper, player);
   }

   // ===== Arcane Tome =====

   @GameTest
   @Covers("resolve")
   public void resolveRaisesConcentration(GameTestHelper helper){
      int plain = net.borisshoes.arcananovum.utils.LevelUtils.concFromXp(100000, 0);
      int resolved = net.borisshoes.arcananovum.utils.LevelUtils.concFromXp(100000, 5);
      helper.assertTrue(resolved > plain, "Resolve 5 gave " + resolved + " concentration against " + plain);
      helper.succeed();
   }

   // ===== Regressions =====

   // passiveTick called player.getRandom() in its Blooming branch, and a Stele passes no player. The catch-all swallowed the crash and skipped the rest of the tick
   @GameTest
   public void charmOfBloomingFinishesItsTickInAStele(GameTestHelper helper){
      net.minecraft.world.entity.animal.cow.Cow cow = mob(helper, EntityTypes.COW, 3.5, 2, 3.5);
      cow.setBaby(true);
      int ageBefore = cow.getAge();
      BlockPos center = helper.absolutePos(new BlockPos(3, 2, 3));
      net.minecraft.world.phys.AABB range = new net.minecraft.world.phys.AABB(center).inflate(2);
      ItemStack charm = item(ArcanaRegistry.WILD_GROWTH_CHARM, ArcanaAugments.CHARM_OF_BLOOMING, 1);
      call(ArcanaRegistry.WILD_GROWTH_CHARM, "passiveTick", charm, helper.getLevel(), center, range, null, null);
      helper.assertTrue(cow.getAge() > ageBefore, "A Stele's Charm of Blooming stopped before it reached the baby animal");
      helper.succeed();
   }

   // The bash put its 10 hearts back with 200 + 100 * level ticks instead of the duration settings every other block uses
   @GameTest
   public void shieldBashUsesTheDurationConfig(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ShieldOfFortitude shield = (ShieldOfFortitude) ArcanaRegistry.SHIELD_OF_FORTITUDE;
      ItemStack basher = item(shield, ArcanaAugments.SHIELD_BASH, 1);
      ArcanaAugments.applyAugment(basher, ArcanaAugments.SHIELD_OF_RESILIENCE, 2, false);
      player.setItemSlot(EquipmentSlot.OFFHAND, basher);
      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      for(int i = 0; i < 5; i++) shield.shieldBlock(player, basher, 10000f);
      int expected = 123 + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION_PER_LVL).get(2);
      withConfig(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION, 123, () -> target.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1f));
      helper.assertTrue(player.getCooldowns().isOnCooldown(basher), "The Shield Bash did not happen");
      int duration = shieldTimer(player);
      helper.assertTrue(duration == expected, "The bash's absorption lasts " + duration + " ticks instead of " + expected);
      finish(helper, player);
   }

   // decreaseRocket rebuilt lore on the firework stack it was handed, so the Rocket kept showing its old charge count
   @GameTest
   public void firingARocketUpdatesItsLore(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack stack = ArcanaRegistry.EVERLASTING_ROCKET.getNewItem();
      ((EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET).setEnergy(stack, ((EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET).getMaxEnergy(stack));
      ((EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET).buildItemLore(stack, helper.getLevel().getServer());
      player.getInventory().setItem(0, stack);
      EverlastingRocket.decreaseRocket(((EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET).getFireworkStack(stack), player);
      ItemStack fresh = stack.copy();
      ((EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET).buildItemLore(fresh, helper.getLevel().getServer());
      helper.assertTrue(java.util.Objects.equals(stack.get(net.minecraft.core.component.DataComponents.LORE), fresh.get(net.minecraft.core.component.DataComponents.LORE)), "The Rocket's lore still shows the old charge count");
      finish(helper, player);
   }
}
