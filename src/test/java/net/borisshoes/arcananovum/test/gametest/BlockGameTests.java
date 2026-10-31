package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievement;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.blocks.ContinuumAnchorBlockEntity;
import net.borisshoes.arcananovum.blocks.EnderCrateBlockEntity;
import net.borisshoes.arcananovum.blocks.FractalSponge;
import net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity;
import net.borisshoes.arcananovum.blocks.IgneousColliderBlockEntity;
import net.borisshoes.arcananovum.blocks.InterdictorBlockEntity;
import net.borisshoes.arcananovum.blocks.ItineranteurBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.CelestialAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.StarpathAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.StormcallerAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.ArcaneSingularityBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.RadiantFletcheryBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StarlightForgeBlockEntity;
import net.borisshoes.arcananovum.blocks.forge.StellarCoreBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import static net.borisshoes.arcananovum.test.gametest.RegressionGameTests.finish;
import static net.borisshoes.arcananovum.test.gametest.TestKit.*;

/** Achievements and augments of Arcana blocks. Multiblock checks are skipped by setting the assembled flag */
@SuppressWarnings("unchecked")
public class BlockGameTests {

   /** Places an Arcana block and initializes it the way placing its item does */
   private static <T extends BlockEntity> T place(GameTestHelper helper, ArcanaItem item, BlockPos relative, ServerPlayer crafter, ArcanaAugment augment, int level){
      helper.setBlock(relative, Blocks.AIR); // A block placed over itself keeps its old block entity
      helper.setBlock(relative, ((ArcanaBlock) item).getBlock().defaultBlockState());
      T entity = (T) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
      TreeMap<ArcanaAugment, Integer> augments = new TreeMap<>();
      if(augment != null && level > 0) augments.put(augment, level);
      call(entity, "initialize", augments, crafter == null ? "" : crafter.getStringUUID(), UUID.randomUUID().toString(), 0, "", null);
      return entity;
   }

   // ===== Igneous Collider =====

   private static IgneousColliderBlockEntity collider(GameTestHelper helper, ServerPlayer player, ArcanaAugment augment, int level, boolean ice){
      BlockPos pos = new BlockPos(3, 2, 3);
      helper.setBlock(pos.north(), Blocks.LAVA);
      helper.setBlock(pos.south(), ice ? Blocks.BLUE_ICE : Blocks.WATER);
      helper.setBlock(pos.above(), Blocks.CHEST);
      return place(helper, ArcanaRegistry.IGNEOUS_COLLIDER, pos, player, augment, level);
   }

   /** The Collider only checks on even ticks, so a test that drives it has to start on one */
   private static void onEvenTick(GameTestHelper helper, Runnable test){
      if(helper.getLevel().getServer().getTickCount() % 2 == 0) test.run();
      else helper.runAfterDelay(1, test);
   }
   
   private static int collide(GameTestHelper helper, IgneousColliderBlockEntity collider){
      BlockPos chest = new BlockPos(3, 2, 3).above();
      setField(collider, "cooldown", 0);
      call(collider, "tick");
      ChestBlockEntity output = helper.getBlockEntity(chest, ChestBlockEntity.class);
      int count = 0;
      for(int i = 0; i < output.getContainerSize(); i++) count += output.getItem(i).getCount();
      return count;
   }

   @GameTest
   @Covers("endless_extrusion")
   public void producingObsidianGrantsEndlessExtrusion(GameTestHelper helper){
      onEvenTick(helper, () -> {
         ServerPlayer player = player(helper);
         almostComplete(player, ArcanaAchievements.ENDLESS_EXTRUSION, 1);
         helper.assertTrue(collide(helper, collider(helper, player, null, 0, false)) > 0, "The Collider made nothing");
         assertAchieved(helper, player, ArcanaAchievements.ENDLESS_EXTRUSION);
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("expensive_infusion")
   public void infusingCryingObsidianGrantsExpensiveInfusion(GameTestHelper helper){
      onEvenTick(helper, () -> {
         ServerPlayer player = player(helper);
         IgneousColliderBlockEntity collider = collider(helper, player, null, 0, false);
         helper.setBlock(new BlockPos(3, 2, 3).below(), Blocks.NETHERITE_BLOCK);
         collide(helper, collider);
         assertAchieved(helper, player, ArcanaAchievements.EXPENSIVE_INFUSION);
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("cryogenic_cooling")
   public void cryogenicCoolingUsesBlueIce(GameTestHelper helper){
      onEvenTick(helper, () -> {
         ServerPlayer player = player(helper);
         helper.assertTrue(collide(helper, collider(helper, player, null, 0, true)) == 0, "A plain Collider worked with Blue Ice");
         helper.assertTrue(collide(helper, collider(helper, player, ArcanaAugments.CRYOGENIC_COOLING, 1, true)) > 0, "Cryogenic Cooling did not work with Blue Ice");
         helper.assertBlockPresent(Blocks.BLUE_ICE, new BlockPos(3, 2, 3).south());
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("thermal_expansion")
   public void thermalExpansionKeepsTheFluid(GameTestHelper helper){
      onEvenTick(helper, () -> {
         ServerPlayer player = player(helper);
         withConfig(ArcanaConfig.IGNEOUS_COLLIDER_EFFICIENCY_PER_LVL, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 1.0), () -> {
            collide(helper, collider(helper, player, ArcanaAugments.THERMAL_EXPANSION, 4, false));
            helper.assertBlockNotPresent(Blocks.LAVA, new BlockPos(3, 2, 3).north());
            collide(helper, collider(helper, player, ArcanaAugments.THERMAL_EXPANSION, 5, false));
            helper.assertBlockPresent(Blocks.LAVA, new BlockPos(3, 2, 3).north());
         });
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("magmatic_injection")
   public void magmaticInjectionShortensTheCooldown(GameTestHelper helper){
      IgneousColliderBlockEntity plain = place(helper, ArcanaRegistry.IGNEOUS_COLLIDER, new BlockPos(2, 2, 2), null, null, 0);
      IgneousColliderBlockEntity injected = place(helper, ArcanaRegistry.IGNEOUS_COLLIDER, new BlockPos(4, 2, 4), null, ArcanaAugments.MAGMATIC_INJECTION, 5);
      int expected = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.IGNEOUS_COLLIDER_COOLDOWN_PER_LVL).get(5);
      int actual = (int) call(plain, "getBaseCooldown") - (int) call(injected, "getBaseCooldown");
      helper.assertTrue(actual == expected && expected > 0, "Magmatic Injection 5 took " + actual + " off the cooldown instead of " + expected);
      helper.succeed();
   }

   // ===== Continuum Anchor =====

   private static ContinuumAnchorBlockEntity fueledAnchor(GameTestHelper helper, ServerPlayer player, ArcanaAugment augment, int level, BlockPos pos){
      ContinuumAnchorBlockEntity anchor = place(helper, ArcanaRegistry.CONTINUUM_ANCHOR, pos, player, augment, level);
      setField(anchor, "fuel", 1000);
      return anchor;
   }

   @GameTest(maxTicks = 30)
   @Covers("timey_wimey")
   public void keepingAnAnchorFueledGrantsTimeyWimey(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.TIMEY_WIMEY, 1);
      fueledAnchor(helper, player, null, 0, new BlockPos(3, 2, 3));
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.TIMEY_WIMEY);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest(maxTicks = 30)
   @Covers("temporal_relativity")
   public void temporalRelativitySavesFuelAtItsChance(GameTestHelper helper){
      // Their block entities are not in the level, so only this test ticks them and every tick sees the chance set here
      ContinuumAnchorBlockEntity plain = unplacedAnchor(helper, null, 0, new BlockPos(2, 2, 2));
      ContinuumAnchorBlockEntity relative = unplacedAnchor(helper, ArcanaAugments.TEMPORAL_RELATIVITY, 5, new BlockPos(4, 2, 4));
      helper.succeedWhen(() -> withConfig(ArcanaConfig.CONTINUUM_ANCHOR_EFFICIENCY_PER_LVL, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 1.0), () -> {
         call(plain, "tick");
         call(relative, "tick");
         helper.assertTrue(plain.getFuel() < 1000, "Waiting for the plain Anchor to burn fuel");
         helper.assertTrue(relative.getFuel() == 1000, "Temporal Relativity 5 at chance 1 burned fuel");
      }));
   }
   
   private static ContinuumAnchorBlockEntity unplacedAnchor(GameTestHelper helper, ArcanaAugment augment, int level, BlockPos relative){
      // The block is there for its state, and the block entity the level ticks for it has no fuel
      helper.setBlock(relative, ((ArcanaBlock) ArcanaRegistry.CONTINUUM_ANCHOR).getBlock().defaultBlockState());
      ContinuumAnchorBlockEntity anchor = new ContinuumAnchorBlockEntity(helper.absolutePos(relative), helper.getBlockState(relative));
      anchor.setLevel(helper.getLevel());
      TreeMap<ArcanaAugment, Integer> augments = new TreeMap<>();
      if(augment != null) augments.put(augment, level);
      anchor.initialize(augments, "", UUID.randomUUID().toString(), 0, "", null);
      setField(anchor, "fuel", 1000);
      return anchor;
   }
   
   // ===== Interdictor =====

   @GameTest
   @Covers("unmobbed")
   public void blockingSpawnsGrantsUnmobbed(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.UNMOBBED, 1);
      InterdictorBlockEntity interdictor = place(helper, ArcanaRegistry.INTERDICTOR, new BlockPos(3, 2, 3), player, null, 0);
      interdictor.onSpawn();
      assertAchieved(helper, player, ArcanaAchievements.UNMOBBED);
      finish(helper, player);
   }

   @GameTest
   @Covers({"natal_rift", "coalescence_redirection"})
   public void interdictorAugmentsSetItsRangeAndRedirect(GameTestHelper helper){
      InterdictorBlockEntity plain = place(helper, ArcanaRegistry.INTERDICTOR, new BlockPos(2, 2, 2), null, null, 0);
      InterdictorBlockEntity rift = place(helper, ArcanaRegistry.INTERDICTOR, new BlockPos(4, 2, 2), null, ArcanaAugments.NATAL_RIFT, 3);
      InterdictorBlockEntity redirect = place(helper, ArcanaRegistry.INTERDICTOR, new BlockPos(6, 2, 2), null, ArcanaAugments.COALESCENCE_REDIRECTION, 1);
      helper.assertTrue(rift.getxRange() > plain.getxRange(), "Natal Rift 3 did not widen the zone");
      rift.setxRange(100000);
      helper.assertTrue(rift.getxRange() == rift.getMaxRange(3), "The zone was not capped at its Natal Rift range");
      helper.assertTrue(!(boolean) field(plain, "redirect") && (boolean) field(redirect, "redirect"), "Coalescence Redirection did not turn on redirecting");
      helper.succeed();
   }

   // ===== Fractal Sponge =====

   private static void fillWater(GameTestHelper helper, int radius){
      for(int x = 3 - radius; x <= 3 + radius; x++) for(int z = 3 - radius; z <= 3 + radius; z++) if(x != 3 || z != 3) helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
   }

   private static int absorb(GameTestHelper helper, ServerPlayer player, ItemStack sponge){
      return (int) call(ArcanaRegistry.FRACTAL_SPONGE, "absorbHelper", player, helper.getLevel(), sponge, helper.absolutePos(new BlockPos(3, 2, 3)), false);
   }

   @GameTest
   @Covers("ocean_cleanup")
   public void absorbingWaterGrantsOceanCleanup(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.OCEAN_CLEANUP, 1);
      fillWater(helper, 1);
      helper.assertTrue(absorb(helper, player, ArcanaRegistry.FRACTAL_SPONGE.getNewItem()) > 0, "The sponge absorbed nothing");
      assertAchieved(helper, player, ArcanaAchievements.OCEAN_CLEANUP);
      finish(helper, player);
   }

   @GameTest
   @Covers("sierpinski")
   public void sierpinskiAbsorbsMoreBlocks(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.FRACTAL_SPONGE_BLOCKS, 2, () -> withConfig(ArcanaConfig.FRACTAL_SPONGE_BLOCKS_PER_LVL, List.of(0, 0, 0, 0, 0, 3), () -> {
         fillWater(helper, 1);
         int plain = absorb(helper, player, ArcanaRegistry.FRACTAL_SPONGE.getNewItem());
         fillWater(helper, 1);
         int fractal = absorb(helper, player, item(ArcanaRegistry.FRACTAL_SPONGE, ArcanaAugments.SIERPINSKI, 5));
         helper.assertTrue(fractal > plain, "Sierpinski 5 absorbed " + fractal + " against " + plain);
      }));
      finish(helper, player);
   }

   @GameTest
   @Covers("mandelbrot")
   public void mandelbrotReachesFarther(GameTestHelper helper){
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.FRACTAL_SPONGE_RANGE, 1, () -> withConfig(ArcanaConfig.FRACTAL_SPONGE_RANGE_PER_LVL, List.of(0, 0, 0, 0, 0, 2), () -> {
         fillWater(helper, 2);
         int plain = absorb(helper, player, ArcanaRegistry.FRACTAL_SPONGE.getNewItem());
         fillWater(helper, 2);
         int fractal = absorb(helper, player, item(ArcanaRegistry.FRACTAL_SPONGE, ArcanaAugments.MANDELBROT, 5));
         helper.assertTrue(fractal > plain, "Mandelbrot 5 absorbed " + fractal + " against " + plain);
      }));
      finish(helper, player);
   }

   // ===== Ender Crate =====

   @GameTest
   @Covers({"out_of_the_box", "enderon_prime"})
   public void hopperingFromACrateGrantsItsAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.ENDERON_PRIME, 1);
      BlockPos cratePos = new BlockPos(3, 3, 3);
      EnderCrateBlockEntity crate = place(helper, ArcanaRegistry.ENDER_CRATE, cratePos, player, null, 0);
      crate.setItem(0, new ItemStack(Items.DIRT, 4));
      helper.setBlock(cratePos.below(), Blocks.HOPPER);
      HopperBlockEntity hopper = helper.getBlockEntity(cratePos.below(), HopperBlockEntity.class);
      HopperBlockEntity.suckInItems(helper.getLevel(), hopper);
      helper.assertTrue(hopper.getItem(0).is(Items.DIRT), "The hopper pulled nothing from the crate");
      assertAchieved(helper, player, ArcanaAchievements.OUT_OF_THE_BOX);
      assertAchieved(helper, player, ArcanaAchievements.ENDERON_PRIME);
      crate.clearContent();
      finish(helper, player);
   }

   @GameTest
   @Covers("ender_bandwidth")
   public void enderBandwidthAddsNineSlotsPerLevel(GameTestHelper helper){
      EnderCrateBlockEntity plain = place(helper, ArcanaRegistry.ENDER_CRATE, new BlockPos(2, 2, 2), null, null, 0);
      EnderCrateBlockEntity wide = place(helper, ArcanaRegistry.ENDER_CRATE, new BlockPos(4, 2, 4), null, ArcanaAugments.ENDER_BANDWIDTH, 3);
      helper.assertTrue(plain.getContainerSize() == 27 && wide.getContainerSize() == 54, "The crates have " + plain.getContainerSize() + " and " + wide.getContainerSize() + " slots");
      helper.succeed();
   }

   // ===== Itineranteur =====

   @GameTest
   @Covers({"guiding_light", "thoroughfare", "paved_warmth", "road_snacks"})
   public void itineranteurAugmentsSetItsStats(GameTestHelper helper){
      ItineranteurBlockEntity plain = place(helper, ArcanaRegistry.ITINERANTEUR, new BlockPos(2, 2, 2), null, null, 0);
      helper.assertTrue(place0(helper, ArcanaAugments.GUIDING_LIGHT, 4).getMaxRange() > plain.getMaxRange(), "Guiding Light 4 did not raise the range");
      helper.assertTrue(place0(helper, ArcanaAugments.THOROUGHFARE, 4).getMaxBlocks() > plain.getMaxBlocks(), "Thoroughfare 4 did not raise the blocks");
      helper.assertTrue(place0(helper, ArcanaAugments.PAVED_WARMTH, 3).getSpeedBoost() > plain.getSpeedBoost(), "Paved Warmth 3 did not raise the speed");
      helper.assertTrue(!(boolean) field(plain, "keepFed") && (boolean) field(place0(helper, ArcanaAugments.ROAD_SNACKS, 1), "keepFed"), "Road Snacks did not keep players fed");
      helper.succeed();
   }

   private static ItineranteurBlockEntity place0(GameTestHelper helper, ArcanaAugment augment, int level){
      return place(helper, ArcanaRegistry.ITINERANTEUR, new BlockPos(4, 2, 4), null, augment, level);
   }

   @GameTest
   @Covers("arcana_boulevard")
   public void travelingOnARoadGrantsItsCrafterArcanaBoulevard(GameTestHelper helper){
      ServerPlayer crafter = player(helper);
      ServerPlayer traveler = player(helper);
      almostComplete(crafter, ArcanaAchievements.ARCANA_BOULEVARD, 100);
      ArcanaNovum.data(traveler).addMiscData(ItineranteurBlockEntity.CRAFTER_TAG, StringTag.valueOf(crafter.getStringUUID()));
      traveler.setSprinting(true);
      traveler.setOnGround(true);
      traveler.checkMovementStatistics(1.5, 0, 0);
      assertAchieved(helper, crafter, ArcanaAchievements.ARCANA_BOULEVARD);
      finish(helper, crafter, traveler);
   }

   // ===== Geomantic Stele =====

   @GameTest
   @Covers({"artificial_geyser", "artificial_volcano", "monolith_of_fear", "doctor_stone", "kokopelli"})
   public void slottingCharmsIntoASteleGrantsItsAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      List<List<Object>> entries = List.of(
            List.of(ArcanaRegistry.AQUATIC_EVERSOURCE, ArcanaAchievements.ARTIFICIAL_GEYSER), List.of(ArcanaRegistry.MAGMATIC_EVERSOURCE, ArcanaAchievements.ARTIFICIAL_VOLCANO),
            List.of(ArcanaRegistry.FELIDAE_CHARM, ArcanaAchievements.MONOLITH_OF_FEAR), List.of(ArcanaRegistry.CLEANSING_CHARM, ArcanaAchievements.DOCTOR_STONE),
            List.of(ArcanaRegistry.WILD_GROWTH_CHARM, ArcanaAchievements.KOKOPELLI));
      for(List<Object> entry : entries){
         GeomanticSteleBlockEntity stele = place(helper, ArcanaRegistry.GEOMANTIC_STELE, new BlockPos(3, 2, 3), player, null, 0);
         setField(stele, "assembled", true);
         ArcanaAchievement achievement = (ArcanaAchievement) entry.get(1);
         assertNotAchieved(helper, player, achievement);
         ItemStack stack = ((ArcanaItem) entry.get(0)).getNewItem();
         player.getInventory().setItem(0, stack);
         stele.interact(player, stack);
         assertAchieved(helper, player, achievement);
      }
      finish(helper, player);
   }

   @GameTest
   @Covers({"geolithic_amplification", "metamorphic_alignment"})
   public void steleAugmentsSetItsRangeAndControls(GameTestHelper helper){
      ServerPlayer player = player(helper);
      GeomanticSteleBlockEntity plain = place(helper, ArcanaRegistry.GEOMANTIC_STELE, new BlockPos(2, 2, 2), null, null, 0);
      plain.setItem(ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      GeomanticSteleBlockEntity amplified = place(helper, ArcanaRegistry.GEOMANTIC_STELE, new BlockPos(4, 2, 2), null, ArcanaAugments.GEOLITHIC_AMPLIFICATION, 3);
      amplified.setItem(ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      helper.assertTrue(amplified.getMaxRange().x > plain.getMaxRange().x, "Geolithic Amplification 3 did not widen the Stele");

      player.setShiftKeyDown(true);
      setField(plain, "assembled", true);
      helper.assertFalse(plain.interact(player, ItemStack.EMPTY) && player.containerMenu != player.inventoryMenu, "Sneak clicking a plain Stele opened its range menu");
      GeomanticSteleBlockEntity aligned = place(helper, ArcanaRegistry.GEOMANTIC_STELE, new BlockPos(6, 2, 2), null, ArcanaAugments.METAMORPHIC_ALIGNMENT, 1);
      setField(aligned, "assembled", true);
      helper.assertTrue(aligned.interact(player, ItemStack.EMPTY) && player.containerMenu != player.inventoryMenu, "Metamorphic Alignment did not open the range menu");
      finish(helper, player);
   }

   // ===== Altars =====

   @GameTest
   @Covers({"power_of_the_sun", "lycanthrope", "orbital_period"})
   public void celestialAltarGrantsItsAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var server = helper.getLevel().getServer();
      var clock = server.overworld().dimensionType().defaultClock().orElseThrow();
      long before = server.clockManager().getInstance(clock).totalTicks();
      CelestialAltarBlockEntity altar = place(helper, ArcanaRegistry.CELESTIAL_ALTAR, new BlockPos(3, 2, 3), null, null, 0);
      altar.setMode(0);
      call(altar, "changeTime", player);
      assertAchieved(helper, player, ArcanaAchievements.POWER_OF_THE_SUN);
      altar.setMode(1);
      altar.setPhase(1);
      call(altar, "changeTime", player);
      assertNotAchieved(helper, player, ArcanaAchievements.LYCANTHROPE);
      altar.setPhase(0);
      call(altar, "changeTime", player);
      assertAchieved(helper, player, ArcanaAchievements.LYCANTHROPE);
      server.clockManager().setTotalTicks(clock, before);

      altar.resetCooldown();
      CelestialAltarBlockEntity fast = place(helper, ArcanaRegistry.CELESTIAL_ALTAR, new BlockPos(5, 2, 3), null, ArcanaAugments.ORBITAL_PERIOD, 5);
      fast.resetCooldown();
      helper.assertTrue(fast.getCooldown() < altar.getCooldown(), "Orbital Period 5 did not shorten the cooldown");
      finish(helper, player);
   }

   @GameTest
   @Covers({"come_again_rain", "cloud_seeding"})
   public void stormcallerAltarGrantsComeAgainRain(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var server = helper.getLevel().getServer();
      float rain = helper.getLevel().getRainLevel(1f);
      helper.getLevel().setRainLevel(1f);
      StormcallerAltarBlockEntity altar = place(helper, ArcanaRegistry.STORMCALLER_ALTAR, new BlockPos(3, 2, 3), null, null, 0);
      altar.setMode(0);
      call(altar, "changeWeather", player);
      helper.getLevel().setRainLevel(rain);
      altar.resetCooldown();
      StormcallerAltarBlockEntity fast = place(helper, ArcanaRegistry.STORMCALLER_ALTAR, new BlockPos(5, 2, 3), null, ArcanaAugments.CLOUD_SEEDING, 5);
      fast.resetCooldown();
      helper.assertTrue(fast.getCooldown() < altar.getCooldown(), "Cloud Seeding 5 did not shorten the cooldown");
      server.setWeatherParameters(6000, 0, false, false);
      assertAchieved(helper, player, ArcanaAchievements.COME_AGAIN_RAIN);
      finish(helper, player);
   }

   @GameTest
   @Covers({"constellation_drift", "astral_pathfinder", "stargate"})
   public void starpathAltarAugmentsChangeCostAndCooldown(GameTestHelper helper){
      StarpathAltarBlockEntity plain = place(helper, ArcanaRegistry.STARPATH_ALTAR, new BlockPos(2, 2, 2), null, null, 0);
      StarpathAltarBlockEntity drift = place(helper, ArcanaRegistry.STARPATH_ALTAR, new BlockPos(4, 2, 2), null, ArcanaAugments.CONSTELLATION_DRIFT, 5);
      plain.resetCooldown();
      drift.resetCooldown();
      helper.assertTrue(drift.getCooldown() < plain.getCooldown(), "Constellation Drift 5 did not shorten the cooldown");

      BlockPos far = helper.absolutePos(new BlockPos(2, 2, 2)).offset(100000, 0, 0);
      plain.setTarget(far);
      StarpathAltarBlockEntity pathfinder = place(helper, ArcanaRegistry.STARPATH_ALTAR, new BlockPos(6, 2, 2), null, ArcanaAugments.ASTRAL_PATHFINDER, 2);
      pathfinder.setTarget(far);
      helper.assertTrue(Math.abs(plain.calculateCost() / 4 - pathfinder.calculateCost()) <= 1, "Astral Pathfinder 2 should halve the cost twice, " + plain.calculateCost() + " became " + pathfinder.calculateCost());

      // Stands in reach of both altars, which send whoever is above them
      ServerPlayer player = player(helper);
      StarpathAltarBlockEntity.TargetEntry nether = new StarpathAltarBlockEntity.TargetEntry("Nether", "minecraft:the_nether", 0, 100, 0);
      plain.setTarget(nether);
      call(plain, "teleport", (Object) null);
      helper.assertTrue(player.level() == helper.getLevel(), "A plain Altar crossed dimensions");
      StarpathAltarBlockEntity stargate = place(helper, ArcanaRegistry.STARPATH_ALTAR, new BlockPos(2, 2, 4), null, ArcanaAugments.STARGATE, 1);
      stargate.setTarget(nether);
      call(stargate, "teleport", (Object) null);
      helper.assertTrue(player.level().dimension() == net.minecraft.world.level.Level.NETHER, "Stargate did not cross to the Nether");
      finish(helper, player);
   }

   @GameTest
   @Covers({"far_from_home", "adventuring_party"})
   public void starpathTripsGrantTheirAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ServerPlayer friend = player(helper);
      StarpathAltarBlockEntity altar = place(helper, ArcanaRegistry.STARPATH_ALTAR, new BlockPos(1, 1, 1), null, null, 0);
      BlockPos near = helper.absolutePos(new BlockPos(1, 2, 7));
      altar.setTarget(near);
      call(altar, "teleport", player);
      assertAchieved(helper, player, ArcanaAchievements.ADVENTURING_PARTY);
      assertNotAchieved(helper, player, ArcanaAchievements.FAR_FROM_HOME);

      player.teleportTo(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)).x, helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)).y, helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2, 1.5)).z);
      altar.setTarget(helper.absolutePos(new BlockPos(1, 2, 7)).offset(110000, 0, 0));
      call(altar, "teleport", (Object) null);
      assertAchieved(helper, player, ArcanaAchievements.FAR_FROM_HOME);
      finish(helper, player, friend);
   }

   @GameTest
   @Covers("hasty_bargain")
   public void hastyBargainShortensTheAltarCooldown(GameTestHelper helper){
      TransmutationAltarBlockEntity plain = place(helper, ArcanaRegistry.TRANSMUTATION_ALTAR, new BlockPos(2, 2, 2), null, null, 0);
      TransmutationAltarBlockEntity hasty = place(helper, ArcanaRegistry.TRANSMUTATION_ALTAR, new BlockPos(4, 2, 2), null, ArcanaAugments.HASTY_BARGAIN, 5);
      plain.resetCooldown();
      hasty.resetCooldown();
      helper.assertTrue(hasty.getCooldown() < plain.getCooldown(), "Hasty Bargain 5 did not shorten the cooldown");
      helper.succeed();
   }

   // ===== Forge =====

   @GameTest
   @Covers("supermassive")
   public void supermassiveAddsSingularityPages(GameTestHelper helper){
      ArcaneSingularityBlockEntity plain = place(helper, ArcanaRegistry.ARCANE_SINGULARITY, new BlockPos(2, 2, 2), null, null, 0);
      ArcaneSingularityBlockEntity big = place(helper, ArcanaRegistry.ARCANE_SINGULARITY, new BlockPos(4, 2, 2), null, ArcanaAugments.SUPERMASSIVE, 5);
      int expected = 28 * ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ARCANE_SINGULARITY_BOOKS_PER_LVL).get(5);
      helper.assertTrue(big.getCapacity() - plain.getCapacity() == expected && expected > 0, "Supermassive 5 added " + (big.getCapacity() - plain.getCapacity()) + " books instead of " + expected);
      helper.succeed();
   }

   @GameTest
   @Covers({"finally_useful_2", "alchemical_efficiency"})
   public void tippingArrowsInTheFletcheryGrantsFinallyUseful(GameTestHelper helper){
      ServerPlayer player = player(helper);
      RadiantFletcheryBlockEntity plain = place(helper, ArcanaRegistry.RADIANT_FLETCHERY, new BlockPos(2, 2, 2), null, null, 0);
      RadiantFletcheryBlockEntity efficient = place(helper, ArcanaRegistry.RADIANT_FLETCHERY, new BlockPos(4, 2, 2), null, ArcanaAugments.ALCHEMICAL_EFFICIENCY, 5);
      helper.assertTrue(efficient.getPotionRatio() > plain.getPotionRatio(), "Alchemical Efficiency 5 did not raise the arrows per potion");

      plain.openGui(player);
      var inv = plain.getInventory();
      inv.setItem(1, net.minecraft.world.item.alchemy.PotionContents.createItemStack(Items.POTION, net.minecraft.world.item.alchemy.Potions.POISON));
      inv.setItem(0, new ItemStack(Items.ARROW, 64));
      plain.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) inv);
      helper.assertTrue(inv.getItem(2).is(Items.TIPPED_ARROW), "The Fletchery tipped no arrows");
      assertAchieved(helper, player, ArcanaAchievements.FINALLY_USEFUL_2);
      player.closeContainer();
      finish(helper, player);
   }

   @GameTest
   @Covers("stellar_range")
   public void stellarRangeWidensTheForge(GameTestHelper helper){
      StarlightForgeBlockEntity plain = place(helper, ArcanaRegistry.STARLIGHT_FORGE, new BlockPos(2, 2, 2), null, null, 0);
      StarlightForgeBlockEntity wide = place(helper, ArcanaRegistry.STARLIGHT_FORGE, new BlockPos(4, 2, 2), null, ArcanaAugments.STELLAR_RANGE, 1);
      helper.assertTrue(wide.getForgeRange().getX() > plain.getForgeRange().getX() && wide.getForgeRange().getY() > plain.getForgeRange().getY(), "Stellar Range did not widen the Forge");
      helper.succeed();
   }

   @GameTest
   @Covers({"intro_arcana", "intermediate_artifice", "artificial_divinity", "master_craftsman", "overly_equipped_archer"})
   public void finishingCraftsGrantsForgeAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.INTERMEDIATE_ARTIFICE, 1);
      StarlightForgeBlockEntity forge = place(helper, ArcanaRegistry.STARLIGHT_FORGE, new BlockPos(3, 2, 3), null, null, 0);
      forge.addPendingCraft(ArcanaRegistry.FELIDAE_CHARM.getNewItem(), 0, player.getUUID(), false);
      call(forge, "tick");
      assertAchieved(helper, player, ArcanaAchievements.INTRO_ARCANA);
      assertAchieved(helper, player, ArcanaAchievements.INTERMEDIATE_ARTIFICE);
      assertNotAchieved(helper, player, ArcanaAchievements.ARTIFICIAL_DIVINITY);

      forge.addPendingCraft(ArcanaRegistry.ENSNAREMENT_ARROWS.getNewItem(), 0, player.getUUID(), false);
      call(forge, "tick");
      assertAchieved(helper, player, ArcanaAchievements.ARTIFICIAL_DIVINITY);

      forge.addPendingInfusion(new ItemStack(Items.DIAMOND_SWORD), 0, player.getUUID(), 0, true);
      call(forge, "tick");
      assertAchieved(helper, player, ArcanaAchievements.MASTER_CRAFTSMAN);

      for(ArcanaItem arrow : List.of(ArcanaRegistry.OVERFLOWING_QUIVER, ArcanaRegistry.RUNIC_BOW, ArcanaRegistry.ALCHEMICAL_ARBALEST, ArcanaRegistry.RUNIC_QUIVER, ArcanaRegistry.ARCANE_FLAK_ARROWS, ArcanaRegistry.BLINK_ARROWS, ArcanaRegistry.DETONATION_ARROWS, ArcanaRegistry.STORM_ARROWS, ArcanaRegistry.CONCUSSION_ARROWS, ArcanaRegistry.EXPULSION_ARROWS, ArcanaRegistry.GRAVITON_ARROWS, ArcanaRegistry.PHOTONIC_ARROWS, ArcanaRegistry.SIPHONING_ARROWS, ArcanaRegistry.TETHER_ARROWS, ArcanaRegistry.SMOKE_ARROWS, ArcanaRegistry.TRACKING_ARROWS, ArcanaRegistry.ENSNAREMENT_ARROWS)){
         assertNotAchieved(helper, player, ArcanaAchievements.OVERLY_EQUIPPED_ARCHER);
         forge.addPendingCraft(arrow.getNewItem(), 0, player.getUUID(), true);
         call(forge, "tick");
      }
      assertAchieved(helper, player, ArcanaAchievements.OVERLY_EQUIPPED_ARCHER);
      finish(helper, player);
   }

   // ===== Stellar Core =====

   @GameTest
   @Covers({"reclamation", "scrap_to_scrap"})
   public void meltingGearGrantsCoreAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.RECLAMATION, 1);
      StellarCoreBlockEntity core = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(3, 2, 3), null, null, 0);
      core.openGui(player);
      var inv = (net.borisshoes.arcananovum.gui.WatchedContainer) core.getInventory();
      inv.setItem(0, new ItemStack(Items.NETHERITE_SWORD));
      core.onChanged(inv);
      assertAchieved(helper, player, ArcanaAchievements.RECLAMATION);
      assertAchieved(helper, player, ArcanaAchievements.SCRAP_TO_SCRAP);
      player.closeContainer();
      finish(helper, player);
   }

   private static int salvageCount(StellarCoreBlockEntity core, GameTestHelper helper, ItemStack stack, net.minecraft.world.item.Item what){
      return core.salvageItem(stack, helper.getLevel().getServer()).stream().filter(s -> s.is(what)).mapToInt(ItemStack::getCount).sum();
   }

   @GameTest
   @Covers("dyson_sphere")
   public void dysonSphereSalvagesMore(GameTestHelper helper){
      StellarCoreBlockEntity plain = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(2, 2, 2), null, null, 0);
      StellarCoreBlockEntity dyson = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(4, 2, 2), null, ArcanaAugments.DYSON_SPHERE, 3);
      int base = salvageCount(plain, helper, new ItemStack(Items.DIAMOND_CHESTPLATE), Items.DIAMOND);
      int more = salvageCount(dyson, helper, new ItemStack(Items.DIAMOND_CHESTPLATE), Items.DIAMOND);
      helper.assertTrue(more > base, "Dyson Sphere 3 salvaged " + more + " diamonds against " + base);
      helper.succeed();
   }

   @GameTest
   @Covers("fusion_injectors")
   public void fusionInjectorsGiveMoreStardust(GameTestHelper helper){
      StellarCoreBlockEntity plain = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(2, 2, 2), null, null, 0);
      StellarCoreBlockEntity fusion = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(4, 2, 2), null, ArcanaAugments.FUSION_INJECTORS, 3);
      ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
      sword.enchant(net.borisshoes.borislib.utils.MinecraftUtils.getEnchantment(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS), 5);
      sword.enchant(net.borisshoes.borislib.utils.MinecraftUtils.getEnchantment(net.minecraft.world.item.enchantment.Enchantments.LOOTING), 3);
      int base = salvageCount(plain, helper, sword, ArcanaRegistry.STARDUST);
      int more = salvageCount(fusion, helper, sword, ArcanaRegistry.STARDUST);
      helper.assertTrue(more > base, "Fusion Injectors 3 gave " + more + " stardust against " + base);
      helper.succeed();
   }

   @GameTest
   @Covers("molten_core")
   public void moltenCoreSmeltsRawOreDoubled(GameTestHelper helper){
      ServerPlayer player = player(helper);
      StellarCoreBlockEntity plain = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(2, 2, 2), null, null, 0);
      var plainInv = (net.borisshoes.arcananovum.gui.WatchedContainer) plain.getInventory();
      plainInv.setItem(0, new ItemStack(Items.RAW_IRON, 4));
      plain.onChanged(plainInv);
      helper.assertTrue(plainInv.getItem(0).is(Items.RAW_IRON), "A plain Core smelted raw iron");
      plainInv.setItem(0, ItemStack.EMPTY);

      StellarCoreBlockEntity molten = place(helper, ArcanaRegistry.STELLAR_CORE, new BlockPos(5, 2, 2), null, ArcanaAugments.MOLTEN_CORE, 1);
      var inv = (net.borisshoes.arcananovum.gui.WatchedContainer) molten.getInventory();
      inv.setItem(0, new ItemStack(Items.RAW_IRON, 4));
      molten.onChanged(inv);
      helper.assertTrue(inv.getItem(0).isEmpty(), "Molten Core did not take the raw iron");
      int ingots = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(5, 2, 2))).inflate(3), e -> e.getItem().is(Items.IRON_INGOT)).stream().mapToInt(e -> e.getItem().getCount()).sum();
      helper.assertTrue(ingots == 8, "Molten Core made " + ingots + " ingots from 4 raw iron instead of 8");
      finish(helper, player);
   }

   // ===== Spawner Infuser =====

   private static ItemStack maxedStone(){
      ItemStack stone = net.borisshoes.arcananovum.items.Soulstone.setType(ArcanaRegistry.SOULSTONE.getNewItem(), EntityTypes.ZOMBIE);
      return net.borisshoes.arcananovum.items.Soulstone.setSouls(stone, net.borisshoes.arcananovum.items.Soulstone.tiers[6]);
   }

   private static net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity infuser(GameTestHelper helper, ServerPlayer player, ArcanaAugment augment, int level, BlockPos pos){
      var infuser = (net.borisshoes.arcananovum.blocks.SpawnerInfuserBlockEntity) place(helper, ArcanaRegistry.SPAWNER_INFUSER, pos, null, augment, level);
      if(player != null) infuser.openGui(player);
      return infuser;
   }

   @GameTest
   @Covers({"innocent_souls", "archlich", "power_overwhelming"})
   public void fillingAnInfuserGrantsItsAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var infuser = infuser(helper, player, ArcanaAugments.SOUL_RESERVOIR, 5, new BlockPos(3, 2, 3));
      infuser.getInventory().setItem(0, maxedStone());
      infuser.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) infuser.getInventory());
      assertAchieved(helper, player, ArcanaAchievements.INNOCENT_SOULS);
      infuser.getInventory().setItem(1, new ItemStack(net.borisshoes.arcananovum.blocks.SpawnerInfuser.getPointsItem(), 64));
      infuser.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) infuser.getInventory());
      assertAchieved(helper, player, ArcanaAchievements.ARCHLICH);
      infuser.getInventory().setItem(1, new ItemStack(net.borisshoes.arcananovum.blocks.SpawnerInfuser.getPointsItem(), 64));
      infuser.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) infuser.getInventory());
      assertAchieved(helper, player, ArcanaAchievements.POWER_OVERWHELMING);
      finish(helper, player);
   }

   @GameTest
   @Covers({"augmented_apparatus", "soul_reservoir"})
   public void infuserAugmentsChangeConversionAndCapacity(GameTestHelper helper){
      var plain = infuser(helper, null, null, 0, new BlockPos(2, 2, 2));
      plain.getInventory().setItem(0, maxedStone());
      plain.getInventory().setItem(1, new ItemStack(net.borisshoes.arcananovum.blocks.SpawnerInfuser.getPointsItem(), 1));
      plain.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) plain.getInventory());
      helper.assertTrue(plain.getPoints() == 8, "One item gave a plain Infuser " + plain.getPoints() + " points instead of 8");

      var apparatus = infuser(helper, null, ArcanaAugments.AUGMENTED_APPARATUS, 2, new BlockPos(5, 2, 2));
      apparatus.getInventory().setItem(0, maxedStone());
      apparatus.getInventory().setItem(1, new ItemStack(net.borisshoes.arcananovum.blocks.SpawnerInfuser.getPointsItem(), 1));
      apparatus.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) apparatus.getInventory());
      helper.assertTrue(apparatus.getPoints() == 32, "Augmented Apparatus 2 should double the ratio twice, gave " + apparatus.getPoints());

      var small = infuser(helper, null, null, 0, new BlockPos(2, 2, 5));
      var big = infuser(helper, null, ArcanaAugments.SOUL_RESERVOIR, 5, new BlockPos(5, 2, 5));
      for(var entity : List.of(small, big)){
         entity.getInventory().setItem(0, maxedStone());
         for(int i = 0; i < 6; i++){
            entity.getInventory().setItem(1, new ItemStack(net.borisshoes.arcananovum.blocks.SpawnerInfuser.getPointsItem(), 64));
            entity.onChanged((net.borisshoes.arcananovum.gui.WatchedContainer) entity.getInventory());
         }
      }
      int extra = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.SPAWNER_INFUSER_EXTRA_CAPACITY_PER_LVL).get(5);
      helper.assertTrue(big.getPoints() - small.getPoints() == extra, "Soul Reservoir 5 added " + (big.getPoints() - small.getPoints()) + " capacity instead of " + extra);
      helper.succeed();
   }

   // ===== Twilight Anvil =====

   @GameTest
   @Covers("enhanced_enhancements")
   public void enhancedEnhancementsBoostCombinedInfusion(GameTestHelper helper){
      var plain = (net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity) place(helper, ArcanaRegistry.TWILIGHT_ANVIL, new BlockPos(2, 2, 2), null, null, 0);
      var boosted = (net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity) place(helper, ArcanaRegistry.TWILIGHT_ANVIL, new BlockPos(5, 2, 2), null, ArcanaAugments.ENHANCED_ENHANCEMENTS, 3);
      double plainStat = combinedInfusion(plain);
      double boostedStat = combinedInfusion(boosted);
      helper.assertTrue(boostedStat > plainStat, "Enhanced Enhancements 3 combined to " + boostedStat + " against " + plainStat);
      helper.succeed();
   }

   private static double combinedInfusion(net.borisshoes.arcananovum.blocks.forge.TwilightAnvilBlockEntity anvil){
      ItemStack first = new ItemStack(Items.DIAMOND_SWORD);
      ItemStack second = new ItemStack(Items.DIAMOND_SWORD);
      net.borisshoes.arcananovum.utils.EnhancedStatUtils.enhanceItem(first, 0.2);
      net.borisshoes.arcananovum.utils.EnhancedStatUtils.enhanceItem(second, 0.2);
      return ArcanaItem.getDoubleProperty(anvil.calculateOutput(first, second).output(), net.borisshoes.arcananovum.utils.EnhancedStatUtils.ENHANCED_STAT_TAG);
   }

   // ===== Player tick =====

   @GameTest(maxTicks = 30)
   @Covers("mile_high")
   public void levitatingAboveTheCloudsWithACoreGrantsMileHigh(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.getInventory().setItem(0, ArcanaRegistry.SHULKER_CORE.getNewItem());
      player.teleportTo(player.getX(), 1620, player.getZ());
      player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION, 100));
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.MILE_HIGH);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest(maxTicks = 40)
   @Covers("god_boon")
   public void holdingADivineItemGrantsGodBoon(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.getInventory().setItem(0, ArcanaRegistry.DIVINE_CATALYST.getNewItem());
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.GOD_BOON);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest(maxTicks = 100)
   @Covers("arcane_addict")
   public void carryingThirtyArcanaItemsGrantsArcaneAddict(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 30; i++) player.getInventory().setItem(i, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.ARCANE_ADDICT);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   // ===== Meta achievements =====

   @GameTest(maxTicks = 40)
   @Covers({"most_achievements", "all_achievements"})
   public void earningTheRestGrantsTheMetaAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      var most = ArcanaAchievements.getAcolyteExcludedAchievements(player.getUUID());
      var all = ArcanaAchievements.getOwtAExcludedAchievements(player.getUUID());
      for(ArcanaAchievement achievement : ArcanaAchievements.ARCANA_ACHIEVEMENTS.values()){
         if(!most.contains(achievement)) ArcanaAchievements.grant(player, achievement);
      }
      assertAchieved(helper, player, ArcanaAchievements.MOST_ACHIEVEMENTS);
      assertNotAchieved(helper, player, ArcanaAchievements.ALL_ACHIEVEMENTS);
      for(ArcanaAchievement achievement : ArcanaAchievements.ARCANA_ACHIEVEMENTS.values()){
         if(!all.contains(achievement)) ArcanaAchievements.grant(player, achievement);
      }
      assertAchieved(helper, player, ArcanaAchievements.ALL_ACHIEVEMENTS);
      finish(helper, player);
   }
}
