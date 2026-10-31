package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.entities.RunicArrowEntity;
import net.borisshoes.arcananovum.items.arrows.PhotonicArrows;
import net.borisshoes.arcananovum.items.arrows.RunicArrow;
import net.borisshoes.borislib.conditions.Conditions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.borisshoes.arcananovum.test.gametest.RegressionGameTests.finish;
import static net.borisshoes.arcananovum.test.gametest.TestKit.*;

/** Achievements and augments of the Runic Arrows, driven through the arrow's real hit methods */
public class ArrowGameTests {

   private static RunicArrow type(ArcanaItem item){
      return (RunicArrow) item;
   }

   private static void clear(ServerPlayer player){
      player.setDeltaMovement(Vec3.ZERO);
   }

   // ===== Arcane Flak Arrows =====

   @GameTest
   @Covers("aa_artillery")
   public void flakArrowKillingFivePhantomsGrantsArtillery(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 4; i++) mobWithHealth(helper, EntityTypes.PHANTOM, 3 + i * 0.3, 4, 3, 0.5f);
      RunicArrowEntity tooFew = arrow(helper, player, ArcanaRegistry.ARCANE_FLAK_ARROWS, null, 0, 3.5, 4, 3);
      tooFew.getArrowType().blockHit(tooFew, blockHit(helper, 3.5, 4, 3));
      assertNotAchieved(helper, player, ArcanaAchievements.AA_ARTILLERY);

      for(int i = 0; i < 5; i++) mobWithHealth(helper, EntityTypes.PHANTOM, 3 + i * 0.3, 4, 3, 0.5f);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.ARCANE_FLAK_ARROWS, null, 0, 3.5, 4, 3);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 3.5, 4, 3));
      assertAchieved(helper, player, ArcanaAchievements.AA_ARTILLERY);
      finish(helper, player);
   }

   @GameTest
   @Covers("airburst")
   public void airburstWidensTheFlakBlast(GameTestHelper helper){
      ServerPlayer player = player(helper);
      List<Double> buffs = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL);
      double base = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.FLAK_ARROW_RANGE);
      double distance = base + buffs.get(3) - 0.5;
      helper.assertTrue(distance > base, "Airburst level 3 adds no range");

      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 2 + distance, 4, 2);
      RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.ARCANE_FLAK_ARROWS, null, 0, 2, 4, 2);
      plain.getArrowType().blockHit(plain, blockHit(helper, 2, 4, 2));
      helper.assertTrue(target.getHealth() == target.getMaxHealth(), "A plain flak arrow reached past its range");

      RunicArrowEntity airburst = arrow(helper, player, ArcanaRegistry.ARCANE_FLAK_ARROWS, ArcanaAugments.AIRBURST, 3, 2, 4, 2);
      airburst.getArrowType().blockHit(airburst, blockHit(helper, 2, 4, 2));
      helper.assertTrue(target.getHealth() < target.getMaxHealth(), "Airburst 3 did not reach " + distance + " blocks");
      finish(helper, player);
   }

   // ===== Blink Arrows =====

   @GameTest
   @Covers("now_you_see_me")
   public void blinkingAHundredBlocksGrantsNowYouSeeMe(GameTestHelper helper){
      ServerPlayer player = player(helper);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.BLINK_ARROWS, null, 0, 1, 3, 1);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 11, 3, 1));
      assertNotAchieved(helper, player, ArcanaAchievements.NOW_YOU_SEE_ME);
      helper.assertTrue(player.position().distanceTo(helper.absoluteVec(new Vec3(11, 4.25, 1))) < 0.01, "The Blink Arrow did not teleport its owner");

      arrow.getArrowType().blockHit(arrow, blockHit(helper, 11, 3, 115));
      assertAchieved(helper, player, ArcanaAchievements.NOW_YOU_SEE_ME);
      finish(helper, player);
   }

   @GameTest
   @Covers("phase_in")
   public void phaseInReducesDamageAfterBlinking(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      RunicArrowEntity plainArrow = arrow(helper, plain, ArcanaRegistry.BLINK_ARROWS, null, 0, 1, 3, 1);
      plainArrow.getArrowType().blockHit(plainArrow, blockHit(helper, 3, 3, 1));
      helper.assertTrue(Conditions.getConditionValue(plain, Conditions.FORTITUDE) == 0, "Blinking without Phase In gave Fortitude");

      ServerPlayer phased = player(helper);
      RunicArrowEntity phasedArrow = arrow(helper, phased, ArcanaRegistry.BLINK_ARROWS, ArcanaAugments.PHASE_IN, 3, 1, 3, 1);
      phasedArrow.getArrowType().blockHit(phasedArrow, blockHit(helper, 3, 3, 1));
      helper.assertTrue(Conditions.getConditionValue(phased, Conditions.FORTITUDE) > 0, "Phase In gave no damage reduction");
      int duration = Conditions.getConditionInstancesOf(phased.getUUID(), Conditions.FORTITUDE).getFirst().getDuration();
      helper.assertTrue(duration == ArcanaNovum.CONFIG.getIntList(ArcanaConfig.BLINK_ARROW_PHASE_IN_DURATION_PER_LVL).get(3), "Phase In 3 lasted " + duration + " ticks");
      finish(helper, plain, phased);
   }

   // ===== Concussion Arrows =====

   @GameTest
   @Covers("shock_awe")
   public void concussingTenMobsGrantsShockAwe(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 10; i++) mob(helper, EntityTypes.ZOMBIE, 3 + (i % 3), 2, 3 + i / 3.0);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.CONCUSSION_ARROWS, null, 0, 4, 2, 4);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 4, 2, 4));
      assertAchieved(helper, player, ArcanaAchievements.SHOCK_AWE);
      finish(helper, player);
   }

   @GameTest
   @Covers("shellshock")
   public void shellshockLengthensConcussionEffects(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie plain = mob(helper, EntityTypes.ZOMBIE, 2, 2, 2);
      RunicArrowEntity plainArrow = arrow(helper, player, ArcanaRegistry.CONCUSSION_ARROWS, null, 0, 2, 2, 2);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(plain));
      int plainDuration = plain.getEffect(MobEffects.NAUSEA).getDuration();
      plain.discard();

      Zombie shocked = mob(helper, EntityTypes.ZOMBIE, 2, 2, 2);
      RunicArrowEntity shellshock = arrow(helper, player, ArcanaRegistry.CONCUSSION_ARROWS, ArcanaAugments.SHELLSHOCK, 3, 2, 2, 2);
      shellshock.getArrowType().entityHit(shellshock, entityHit(shocked));
      int duration = shocked.getEffect(MobEffects.NAUSEA).getDuration();
      double expected = plainDuration * (1 + ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.CONCUSSION_ARROW_SHELLSHOCK_BOOST_PER_LVL).get(3));
      helper.assertTrue(Math.abs(duration - expected) <= 1, "Shellshock 3 nausea lasted " + duration + " ticks, expected about " + expected);
      finish(helper, player);
   }

   // ===== Detonation Arrows =====

   // The first explosion measures the damage, the second leaves the shooter on half a heart. Anti-Personnel 3 keeps the terrain out of it
   @GameTest
   @Covers("safety_third")
   public void survivingYourOwnDetonationGrantsSafetyThird(GameTestHelper helper){
      ServerPlayer measure = player(helper);
      RunicArrowEntity first = arrow(helper, measure, ArcanaRegistry.DETONATION_ARROWS, ArcanaAugments.ANTI_PERSONNEL, 3, 3, 3, 1);
      first.setDeltaMovement(0, 0, 1.5);
      first.getArrowType().blockHit(first, blockHit(helper, 1, 2.5, 3));
      float damage = measure.getMaxHealth() - measure.getHealth();
      helper.assertTrue(damage > 0 && damage < 19, "The test explosion did " + damage + " damage, which leaves no room to survive it");
      assertNotAchieved(helper, measure, ArcanaAchievements.SAFETY_THIRD);
      helper.getLevel().getServer().getPlayerList().remove(measure);

      ServerPlayer player = player(helper);
      player.setHealth(damage + 1);
      RunicArrowEntity second = arrow(helper, player, ArcanaRegistry.DETONATION_ARROWS, ArcanaAugments.ANTI_PERSONNEL, 3, 3, 3, 1);
      second.setDeltaMovement(0, 0, 1.5);
      second.getArrowType().blockHit(second, blockHit(helper, 1, 2.5, 3));
      assertAchieved(helper, player, ArcanaAchievements.SAFETY_THIRD);
      finish(helper, player);
   }

   private static float detonationDamage(GameTestHelper helper, ServerPlayer player, ArcanaAugment augment, int level){
      IronGolem golem = mob(helper, EntityTypes.IRON_GOLEM, 3, 3, 3);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.DETONATION_ARROWS, augment, level, 3, 3, 1);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 3, 4, 1.5));
      float damage = golem.getMaxHealth() - golem.getHealth();
      golem.discard();
      return damage;
   }

   @GameTest
   @Covers("anti_personnel")
   public void antiPersonnelHitsHarderAndStopsTerrainDamageAtFullLevel(GameTestHelper helper){
      ServerPlayer player = player(helper);
      float plain = detonationDamage(helper, player, null, 0);
      helper.assertTrue(plain > 0, "A plain Detonation Arrow did no damage");
      float boosted = detonationDamage(helper, player, ArcanaAugments.ANTI_PERSONNEL, 2);
      helper.assertTrue(boosted > plain, "Anti-Personnel 2 did " + boosted + " damage, no more than the plain " + plain);

      BlockPos floor = new BlockPos(5, 3, 5);
      helper.setBlock(floor, Blocks.STONE);
      RunicArrowEntity full = arrow(helper, player, ArcanaRegistry.DETONATION_ARROWS, ArcanaAugments.ANTI_PERSONNEL, 3, 5, 4, 5);
      full.getArrowType().blockHit(full, blockHit(helper, 5.5, 4, 5.5));
      helper.assertBlockPresent(Blocks.STONE, floor);
      finish(helper, player);
   }

   @GameTest
   @Covers("blast_mine")
   public void blastMineBreaksBlocksWithoutHurtingCreatures(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.assertTrue(detonationDamage(helper, player, ArcanaAugments.BLAST_MINE, 1) == 0, "Blast Mine hurt a creature");
      BlockPos block = new BlockPos(5, 3, 5);
      helper.setBlock(block, Blocks.STONE);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.DETONATION_ARROWS, ArcanaAugments.BLAST_MINE, 1, 5, 4, 5);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 5.5, 4, 5.5));
      helper.assertBlockNotPresent(Blocks.STONE, block);
      finish(helper, player);
   }

   // ===== Ensnarement Arrows =====

   @GameTest
   @Covers("waterboarding")
   public void ensnaringADrowningMobGrantsWaterboarding(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie breathing = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.ENSNAREMENT_ARROWS, null, 0, 3, 2, 3);
      arrow.getArrowType().entityHit(arrow, entityHit(breathing));
      assertNotAchieved(helper, player, ArcanaAchievements.WATERBOARDING);

      Zombie drowning = mob(helper, EntityTypes.ZOMBIE, 3, 2, 5);
      drowning.setAirSupply(0);
      arrow.getArrowType().entityHit(arrow, entityHit(drowning));
      assertAchieved(helper, player, ArcanaAchievements.WATERBOARDING);
      finish(helper, player);
   }

   // The window is a minute of game time, so the test skips most of it and keeps the last two 10 tick checks real
   @GameTest(maxTicks = 80)
   @Covers("shackled")
   public void keepingATargetEnsnaredGrantsShackled(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie target = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.ENSNAREMENT_ARROWS, ArcanaAugments.ENTRAPMENT, 5, 3, 2, 3);
      arrow.getArrowType().entityHit(arrow, entityHit(target));
      helper.assertTrue(ArcanaAchievements.isTimerActive(player, ArcanaAchievements.SHACKLED), "The Shackled timer did not start");
      ArcanaAchievements.progress(player, ArcanaAchievements.SHACKLED, 1170);
      assertNotAchieved(helper, player, ArcanaAchievements.SHACKLED);
      helper.runAfterDelay(40, () -> {
         assertAchieved(helper, player, ArcanaAchievements.SHACKLED);
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("entrapment")
   public void entrapmentLengthensEnsnarement(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie plain = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      RunicArrowEntity plainArrow = arrow(helper, player, ArcanaRegistry.ENSNAREMENT_ARROWS, null, 0, 3, 2, 3);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(plain));
      helper.assertTrue(plain.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT).getDuration() == ArcanaNovum.CONFIG.getInt(ArcanaConfig.ENSNAREMENT_ARROW_DURATION), "Wrong base Ensnarement duration");

      Zombie trapped = mob(helper, EntityTypes.ZOMBIE, 3, 2, 5);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.ENSNAREMENT_ARROWS, ArcanaAugments.ENTRAPMENT, 5, 3, 2, 5);
      arrow.getArrowType().entityHit(arrow, entityHit(trapped));
      int expected = ArcanaNovum.CONFIG.getInt(ArcanaConfig.ENSNAREMENT_ARROW_DURATION) + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ENSNAREMENT_ARROW_ENTRAPMENT_DURATION_INCREASE_PER_LVL).get(5);
      helper.assertTrue(trapped.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT).getDuration() == expected, "Entrapment 5 did not last " + expected + " ticks");
      finish(helper, player);
   }

   @GameTest
   @Covers("ethereal_anchor")
   public void etherealAnchorStopsEnsnaredPlayersTeleporting(GameTestHelper helper){
      ServerPlayer shooter = player(helper);
      ServerPlayer plain = player(helper);
      RunicArrowEntity plainArrow = arrow(helper, shooter, ArcanaRegistry.ENSNAREMENT_ARROWS, null, 0, 1, 2, 1);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(plain));
      Vec3 plainStart = plain.position();
      plain.teleportTo(plainStart.x + 3, plainStart.y, plainStart.z);
      helper.assertTrue(plain.position().distanceTo(plainStart) > 2.5, "Plain Ensnarement stopped a teleport");

      ServerPlayer anchored = player(helper);
      RunicArrowEntity arrow = arrow(helper, shooter, ArcanaRegistry.ENSNAREMENT_ARROWS, ArcanaAugments.ETHEREAL_ANCHOR, 1, 1, 2, 1);
      arrow.getArrowType().entityHit(arrow, entityHit(anchored));
      helper.assertTrue(anchored.getEffect(ArcanaRegistry.ENSNAREMENT_EFFECT).getAmplifier() == 1, "Ethereal Anchor did not raise the amplifier");
      Vec3 start = anchored.position();
      anchored.teleportTo(start.x + 3, start.y, start.z);
      helper.assertTrue(anchored.position().distanceTo(start) < 0.5, "An anchored player teleported");
      finish(helper, shooter, plain, anchored);
   }

   // ===== Expulsion Arrows =====

   @GameTest(maxTicks = 40)
   @Covers({"jump_pad", "eviction_burst"})
   public void evictionBurstLaunchesOnceAndGrantsJumpPad(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Vec3 below = player.position().subtract(helper.absoluteVec(Vec3.ZERO)).subtract(0, 0.5, 0);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.EXPULSION_ARROWS, ArcanaAugments.EVICTION_BURST, 1, below.x, below.y, below.z);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, below.x, below.y, below.z));
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, below.x + 2, below.y + 0.5, below.z);
      helper.runAfterDelay(3, () -> {
         assertAchieved(helper, player, ArcanaAchievements.JUMP_PAD);
         helper.assertTrue(zombie.getDeltaMovement().length() > 0, "The burst did not push a nearby mob");
         zombie.setDeltaMovement(Vec3.ZERO);
         helper.runAfterDelay(10, () -> {
            helper.assertTrue(zombie.getDeltaMovement().length() == 0, "Eviction Burst pulsed more than once");
            finish(helper, player);
         });
      });
   }

   @GameTest
   @Covers("repulsion")
   public void repulsionWidensExpulsionRange(GameTestHelper helper){
      ServerPlayer player = player(helper);
      double base = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.EXPULSION_ARROW_RANGE);
      double extra = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.EXPULSION_ARROW_REPULSION_RANGE_PER_LVL).get(3);
      double distance = Math.min(7.5, Math.sqrt(1.5) * base + 0.5);
      helper.assertTrue(distance < Math.sqrt(1.5) * (base + extra), "Repulsion 3 no longer reaches the test distance");
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 1 + distance, 3, 1);

      RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.EXPULSION_ARROWS, null, 0, 1, 3, 1);
      plain.setDeltaMovement(0, 0, 0.1); // Weak draw, so its pulses end quickly
      plain.getArrowType().blockHit(plain, blockHit(helper, 1, 3, 1));
      helper.assertTrue(zombie.getDeltaMovement().length() == 0, "A plain Expulsion Arrow reached " + distance + " blocks");

      RunicArrowEntity repulsion = arrow(helper, player, ArcanaRegistry.EXPULSION_ARROWS, ArcanaAugments.REPULSION, 3, 1, 3, 1);
      repulsion.setDeltaMovement(0, 0, 0.1);
      repulsion.getArrowType().blockHit(repulsion, blockHit(helper, 1, 3, 1));
      helper.assertTrue(zombie.getDeltaMovement().length() > 0, "Repulsion 3 did not reach " + distance + " blocks");
      finish(helper, player);
   }

   // ===== Graviton Arrows =====

   @GameTest
   @Covers("bring_together")
   public void gatheringTenMobsGrantsBringTogether(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 10; i++) mob(helper, EntityTypes.ZOMBIE, 3 + (i % 3) * 0.5, 2, 3 + i / 5.0);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.GRAVITON_ARROWS, null, 0, 4, 2, 4);
      arrow.setDeltaMovement(0, 0, 0.1);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 4, 2, 4));
      assertAchieved(helper, player, ArcanaAchievements.BRING_TOGETHER);
      finish(helper, player);
   }

   @GameTest
   @Covers("gravity_well")
   public void gravityWellWidensGravitonRange(GameTestHelper helper){
      ServerPlayer player = player(helper);
      double base = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.GRAVITON_ARROW_RANGE);
      double extra = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.GRAVITON_ARROW_WELL_RANGE_PER_LVL).get(3);
      double distance = Math.sqrt(2) * base + 0.5;
      helper.assertTrue(distance < Math.sqrt(2) * (base + extra), "Gravity Well 3 no longer reaches the test distance");
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 1 + distance, 3, 1);

      RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.GRAVITON_ARROWS, null, 0, 1, 3, 1);
      plain.setDeltaMovement(0, 0, 0.1);
      plain.getArrowType().blockHit(plain, blockHit(helper, 1, 3, 1));
      helper.assertTrue(zombie.getDeltaMovement().length() == 0, "A plain Graviton Arrow reached " + distance + " blocks");

      RunicArrowEntity well = arrow(helper, player, ArcanaRegistry.GRAVITON_ARROWS, ArcanaAugments.GRAVITY_WELL, 3, 1, 3, 1);
      well.setDeltaMovement(0, 0, 0.1);
      well.getArrowType().blockHit(well, blockHit(helper, 1, 3, 1));
      helper.assertTrue(zombie.getDeltaMovement().x < 0, "Gravity Well 3 did not pull the mob in");
      finish(helper, player);
   }

   // ===== Photonic Arrows =====

   private static void shootPhotonic(GameTestHelper helper, ServerPlayer player, int alignment){
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.PHOTONIC_ARROWS, null, 0, 1, 3, 1);
      ((PhotonicArrows) ArcanaRegistry.PHOTONIC_ARROWS).shoot(helper.getLevel(), player, arrow, alignment);
   }

   @GameTest
   @Covers("x")
   public void photonicLaserKillingTenMobsGrantsX(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 10; i++) mobWithHealth(helper, EntityTypes.ZOMBIE, 1.5, 2, 2.5 + i * 0.5, 1f);
      shootPhotonic(helper, player, 0);
      assertAchieved(helper, player, ArcanaAchievements.X);
      finish(helper, player);
   }

   @GameTest
   @Covers("prismatic_alignment")
   public void prismaticAlignmentRampsDamagePerPierce(GameTestHelper helper){
      ServerPlayer player = player(helper);
      float[] plain = photonicDamage(helper, player, 0);
      float[] aligned = photonicDamage(helper, player, 1);
      float[] full = photonicDamage(helper, player, 5);
      double perMob = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.PHOTONIC_ARROW_PRISMATIC_PER_LVL).get(1);
      double flat = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.PHOTONIC_ARROW_PRISMATIC_FLAT_DMG_INCREASE);
      helper.assertTrue(aligned[0] == plain[0], "Alignment 1 changed the damage to the first target");
      helper.assertTrue(Math.abs(aligned[1] - plain[1] - perMob) < 0.01, "Alignment 1 added " + (aligned[1] - plain[1]) + " to the next target, expected " + perMob);
      helper.assertTrue(Math.abs(full[0] - plain[0] - flat) < 0.01, "Alignment 5 added " + (full[0] - plain[0]) + " to the first target, expected " + flat);
      finish(helper, player);
   }

   private static float[] photonicDamage(GameTestHelper helper, ServerPlayer player, int alignment){
      IronGolem first = mob(helper, EntityTypes.IRON_GOLEM, 1, 2, 4);
      IronGolem second = mob(helper, EntityTypes.IRON_GOLEM, 1, 2, 6);
      shootPhotonic(helper, player, alignment);
      float[] damage = {first.getMaxHealth() - first.getHealth(), second.getMaxHealth() - second.getHealth()};
      first.discard();
      second.discard();
      return damage;
   }

   // ===== Siphoning Arrows =====

   @GameTest(maxTicks = 40)
   @Covers("circle_of_life")
   public void killingWithASiphonOnLastHeartGrantsCircleOfLife(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setHealth(1f);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.SIPHONING_ARROWS, null, 0, 3, 2, 3);
      arrow.getArrowType().entityHit(arrow, entityHit(zombie));
      zombie.hurtServer(helper.getLevel(), player.damageSources().playerAttack(player), 1000f);
      helper.runAfterDelay(4, () -> {
         assertAchieved(helper, player, ArcanaAchievements.CIRCLE_OF_LIFE);
         finish(helper, player);
      });
   }

   @GameTest
   @Covers("overheal")
   public void overhealTurnsExcessHealingIntoAbsorption(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      RunicArrowEntity plainArrow = arrow(helper, plain, ArcanaRegistry.SIPHONING_ARROWS, null, 0, 3, 2, 3);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(zombie));
      helper.assertTrue(plain.getAbsorptionAmount() == 0, "Healing at full health gave absorption without Overheal");

      ServerPlayer overhealed = player(helper);
      RunicArrowEntity arrow = arrow(helper, overhealed, ArcanaRegistry.SIPHONING_ARROWS, ArcanaAugments.OVERHEAL, 1, 3, 2, 3);
      arrow.getArrowType().entityHit(arrow, entityHit(zombie));
      float expected = Math.min(ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.SIPHONING_ARROW_OVERHEAL_PER_LVL).get(1), ArcanaNovum.CONFIG.getFloat(ArcanaConfig.SIPHONING_ARROW_MAX_HEAL));
      helper.assertTrue(overhealed.getAbsorptionAmount() == expected, "Overheal 1 gave " + overhealed.getAbsorptionAmount() + " absorption, expected " + expected);
      finish(helper, plain, overhealed);
   }

   // ===== Smoke Arrows =====

   @GameTest(maxTicks = 40)
   @Covers("smoke_screen")
   public void hidingInSmokeWithThreeMonstersGrantsSmokeScreen(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Vec3 at = player.position().subtract(helper.absoluteVec(Vec3.ZERO));
      for(int i = 0; i < 3; i++) mob(helper, EntityTypes.ZOMBIE, at.x + 0.5, at.y, at.z + i * 0.3);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.SMOKE_ARROWS, null, 0, at.x, at.y, at.z);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, at.x, at.y, at.z));
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.SMOKE_SCREEN);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest(maxTicks = 40)
   @Covers("tear_gas")
   public void tearGasStrengthensAndLengthensSmokeOnPlayers(GameTestHelper helper){
      ServerPlayer shooter = player(helper);
      ServerPlayer plain = player(helper);
      ServerPlayer gassed = player(helper);
      Vec3 at = plain.position().subtract(helper.absoluteVec(Vec3.ZERO));
      RunicArrowEntity plainArrow = arrow(helper, shooter, ArcanaRegistry.SMOKE_ARROWS, null, 0, at.x, at.y, at.z);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(plain));
      gassed.teleportTo(gassed.getX() + 8, gassed.getY(), gassed.getZ());
      RunicArrowEntity gas = arrow(helper, shooter, ArcanaRegistry.SMOKE_ARROWS, ArcanaAugments.TEAR_GAS, 3, at.x + 8, at.y, at.z);
      gas.getArrowType().entityHit(gas, entityHit(gassed));
      helper.runAfterDelay(8, () -> {
         float plainFeeble = Conditions.getConditionValue(plain, Conditions.FEEBLE);
         float gasFeeble = Conditions.getConditionValue(gassed, Conditions.FEEBLE);
         helper.assertTrue(gasFeeble > plainFeeble && plainFeeble > 0, "Tear Gas 3 gave Feeble " + gasFeeble + " against " + plainFeeble + " for plain smoke");
         int plainDuration = Conditions.getConditionInstancesOf(plain.getUUID(), Conditions.FEEBLE).getFirst().getDuration();
         int gasDuration = Conditions.getConditionInstancesOf(gassed.getUUID(), Conditions.FEEBLE).getFirst().getDuration();
         helper.assertTrue(gasDuration > plainDuration, "Tear Gas 3 lasted " + gasDuration + " ticks against " + plainDuration + " for plain smoke");
         finish(helper, shooter, plain, gassed);
      });
   }

   // The min and max range were swapped in SmokeArrows, so every draw strength got the same cloud
   @GameTest(maxTicks = 40)
   public void smokeCloudShrinksWithAWeakDraw(GameTestHelper helper){
      ServerPlayer player = player(helper);
      double maxReach = 2 * ArcanaNovum.CONFIG.getDouble(ArcanaConfig.SMOKE_ARROW_RANGE_MAX);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 2 + maxReach - 0.5, 3, 2);
      RunicArrowEntity weak = arrow(helper, player, ArcanaRegistry.SMOKE_ARROWS, null, 0, 2, 3, 2);
      weak.setDeltaMovement(0, 0, 0.1);
      weak.getArrowType().blockHit(weak, blockHit(helper, 2, 3, 2));
      helper.runAfterDelay(8, () -> {
         helper.assertTrue(Conditions.getConditionValue(zombie, Conditions.FEEBLE) == 0, "A barely drawn Smoke Arrow reached the full cloud range");
         finish(helper, player);
      });
   }

   // ===== Storm Arrows =====

   @GameTest(maxTicks = 40)
   @Covers("shock_therapy")
   public void strikingAMooshroomGrantsShockTherapy(GameTestHelper helper){
      ServerPlayer player = player(helper);
      MushroomCow cow = mob(helper, EntityTypes.MOOSHROOM, 5, 2, 5);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, ArcanaAugments.STORM_STABILIZATION, 5, 5, 2, 5);
      arrow.setCritArrow(true);
      Vec3 pos = cow.position().subtract(helper.absoluteVec(Vec3.ZERO));
      arrow.getArrowType().blockHit(arrow, blockHit(helper, pos.x, pos.y, pos.z));
      helper.succeedWhen(() -> {
         assertAchieved(helper, player, ArcanaAchievements.SHOCK_THERAPY);
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   private static boolean lightningNear(GameTestHelper helper, double x, double y, double z){
      Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
      return !helper.getLevel().getEntitiesOfClass(LightningBolt.class, new net.minecraft.world.phys.AABB(pos, pos).inflate(1)).isEmpty();
   }

   @GameTest
   @Covers("storm_stabilization")
   public void stormStabilizationPicksTheStrikeChanceForItsLevel(GameTestHelper helper){
      helper.getLevel().getServer().setWeatherParameters(1000, 0, false, false);
      ServerPlayer player = player(helper);
      withConfig(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 1.0), () -> {
         RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, null, 0, 3, 2, 3);
         plain.setCritArrow(true);
         plain.getArrowType().blockHit(plain, blockHit(helper, 3, 2, 3));
         helper.assertFalse(lightningNear(helper, 3, 2, 3), "Chance 0 at level 0 still struck");

         RunicArrowEntity stable = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, ArcanaAugments.STORM_STABILIZATION, 5, 3, 2, 3);
         stable.getArrowType().blockHit(stable, blockHit(helper, 3, 2, 3));
         helper.assertFalse(lightningNear(helper, 3, 2, 3), "An arrow that is not critical struck");
         stable.setCritArrow(true);
         stable.getArrowType().blockHit(stable, blockHit(helper, 3, 2, 3));
         helper.assertTrue(lightningNear(helper, 3, 2, 3), "Stabilization 5 with chance 1 did not strike");
      });
      finish(helper, player);
   }

   // StormArrows reset the damage cooldown with Entity.setInvulnerableTime(1). Since 26.x that is a separate timer
   // that blocks all damage (the cooldown moved to LivingEntity.damageCooldownTime), so the chain hurt nothing.
   // Stops the lightning with chance 0 so only the chain does damage
   @GameTest
   @Covers("chain_lightning")
   public void chainLightningJumpsToNearbyCreatures(GameTestHelper helper){
      helper.getLevel().getServer().setWeatherParameters(1000, 0, false, false);
      ServerPlayer player = player(helper);
      player.teleportTo(player.getX(), player.getY() + 40, player.getZ());
      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      List<IronGolem> others = new ArrayList<>();
      for(int i = 0; i < 3; i++) others.add(mob(helper, EntityTypes.IRON_GOLEM, 2 + i * 2, 2, 6));
      withConfig(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), () -> {
         RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, null, 0, 4, 2, 4);
         plain.getArrowType().entityHit(plain, entityHit(target));
         helper.assertTrue(others.stream().allMatch(golem -> golem.getHealth() == golem.getMaxHealth()), "A plain Storm Arrow chained");

         RunicArrowEntity chain = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, ArcanaAugments.CHAIN_LIGHTNING, 5, 4, 2, 4);
         chain.getArrowType().entityHit(chain, entityHit(target));
         long hit = others.stream().filter(golem -> golem.getHealth() < golem.getMaxHealth()).count();
         helper.assertTrue(hit == 3, "Chain Lightning 5 reached " + hit + " of 3 nearby creatures");
      });
      finish(helper, player);
   }

   // Same setInvulnerableTime(1) as above: the arrow's own hit runs after entityHit, so a Storm Arrow did no direct damage
   @GameTest
   public void stormArrowsStillDoTheirArrowDamage(GameTestHelper helper){
      helper.getLevel().getServer().setWeatherParameters(1000, 0, false, false);
      ServerPlayer player = player(helper);
      IronGolem golem = mob(helper, EntityTypes.IRON_GOLEM, 5, 2, 5);
      withConfig(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), () -> hitThroughTheEntity(arrow(helper, player, ArcanaRegistry.STORM_ARROWS, null, 0, 5, 3, 3), golem));
      helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "The Storm Arrow did no damage to what it hit");
      finish(helper, player);
   }
   
   // The search also found the arrow's target, so level 1 could spend its one jump on the target itself
   @GameTest
   public void chainLightningLevelOneReachesAnotherCreature(GameTestHelper helper){
      helper.getLevel().getServer().setWeatherParameters(1000, 0, false, false);
      ServerPlayer player = player(helper);
      player.teleportTo(player.getX(), player.getY() + 40, player.getZ());
      IronGolem target = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      IronGolem other = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 6);
      withConfig(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0), () -> {
         RunicArrowEntity chain = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, ArcanaAugments.CHAIN_LIGHTNING, 1, 4, 2, 4);
         chain.getArrowType().entityHit(chain, entityHit(target));
      });
      helper.assertTrue(other.getHealth() < other.getMaxHealth(), "Chain Lightning 1 did not reach the other creature");
      helper.assertTrue(target.getHealth() == target.getMaxHealth(), "Chain Lightning 1 hit the arrow's target instead");
      finish(helper, player);
   }

   @GameTest(maxTicks = 120)
   @Covers("aftershock")
   public void aftershockKeepsHurtingAfterTheStrike(GameTestHelper helper){
      helper.getLevel().getServer().setWeatherParameters(1000, 0, false, false);
      ServerPlayer player = player(helper);
      player.teleportTo(player.getX(), player.getY() + 40, player.getZ());
      // Past the bolt's own 3 block reach, so anything it takes comes from the aftershock
      IronGolem golem = mob(helper, EntityTypes.IRON_GOLEM, 5.8, 2, 2);
      withConfig(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, List.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), () -> {
         RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.STORM_ARROWS, ArcanaAugments.AFTERSHOCK, 4, 2, 2, 2);
         arrow.setCritArrow(true);
         arrow.getArrowType().blockHit(arrow, blockHit(helper, 2, 2, 2));
      });
      helper.succeedWhen(() -> {
         helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "Aftershock did no damage");
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   // ===== Tether Arrows =====

   @GameTest
   @Covers("spiderman")
   public void tetheringUpwardFiveTimesGrantsSpiderman(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.SPIDERMAN, 1);
      BlockPos block = new BlockPos(1, 16, 1);
      helper.setBlock(block, Blocks.STONE);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.TETHER_ARROWS, null, 0, 1, 3, 1);
      arrow.getArrowType().blockHit(arrow, blockHit(helper, 1.5, 17, 1.5));
      helper.setBlock(block, Blocks.AIR);
      assertAchieved(helper, player, ArcanaAchievements.SPIDERMAN);
      finish(helper, player);
   }

   @GameTest
   @Covers("quick_release")
   public void quickReleaseSeversArrowsWhenTheOwnerSneaks(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setShiftKeyDown(true);
      RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.TETHER_ARROWS, null, 0, 3, 8, 3);
      plain.tick();
      helper.assertFalse(plain.getData().getBooleanOr("severed", false), "An arrow without Quick Release was severed");

      RunicArrowEntity quick = arrow(helper, player, ArcanaRegistry.TETHER_ARROWS, ArcanaAugments.QUICK_RELEASE, 1, 3, 8, 3);
      quick.tick();
      helper.assertTrue(quick.getData().getBooleanOr("severed", false), "Sneaking did not sever the arrow");
      clear(player);
      Vec3 before = player.getDeltaMovement();
      quick.getArrowType().blockHit(quick, blockHit(helper, 3, 2, 3));
      helper.assertTrue(player.getDeltaMovement().equals(before), "A severed arrow still launched its owner");
      finish(helper, player);
   }

   // ===== Tracking Arrows =====

   @GameTest(padding = 32)
   @Covers("target_acquired")
   public void trackingArrowLockingOntoItsOwnerGrantsTargetAcquired(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Vec3 at = player.position().subtract(helper.absoluteVec(Vec3.ZERO));
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, null, 0, at.x, at.y + 0.9, at.z + 5);
      arrow.setDeltaMovement(0, 0, -0.5);
      arrow.tick();
      assertAchieved(helper, player, ArcanaAchievements.TARGET_ACQUIRED);
      finish(helper, player);
   }

   @GameTest(padding = 32)
   @Covers("the_arrow_knows_where_it_is")
   public void trackingArrowTurningBackGrantsArrowKnows(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Pig pig = mob(helper, EntityTypes.PIG, 4, 2, 7);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, null, 0, 4, 2.45, 2);
      arrow.setDeltaMovement(0, 0, 0.5);
      arrow.getData().putFloat("initYaw", 120f);
      arrow.getData().store("initPos", Vec3.CODEC, arrow.position().add(25, 0, 0));
      arrow.tick();
      helper.assertTrue(pig.isAlive(), "The arrow should still be flying");
      assertAchieved(helper, player, ArcanaAchievements.THE_ARROW_KNOWS_WHERE_IT_IS);
      finish(helper, player);
   }

   @GameTest
   @Covers("actual_aimbot")
   public void trackingArrowHittingFarAwayGrantsActualAimbot(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Pig pig = mob(helper, EntityTypes.PIG, 4, 2, 7);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, null, 0, 4, 2.45, 7);
      arrow.getData().store("initPos", Vec3.CODEC, arrow.position().add(200, 0, 0));
      arrow.getArrowType().entityHit(arrow, entityHit(pig));
      assertNotAchieved(helper, player, ArcanaAchievements.ACTUAL_AIMBOT);
      arrow.getData().store("initPos", Vec3.CODEC, arrow.position().add(260, 0, 0));
      arrow.getArrowType().entityHit(arrow, entityHit(pig));
      assertAchieved(helper, player, ArcanaAchievements.ACTUAL_AIMBOT);
      finish(helper, player);
   }

   @GameTest
   @Covers("broadheads")
   public void broadheadsMakeTheTargetVulnerable(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Pig plain = mob(helper, EntityTypes.PIG, 4, 2, 4);
      RunicArrowEntity plainArrow = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, null, 0, 4, 2.45, 4);
      plainArrow.getArrowType().entityHit(plainArrow, entityHit(plain));
      helper.assertTrue(Conditions.getConditionValue(plain, Conditions.VULNERABILITY) == 0, "A plain Tracking Arrow gave Vulnerability");

      Pig pig = mob(helper, EntityTypes.PIG, 4, 2, 6);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, ArcanaAugments.BROADHEADS, 3, 4, 2.45, 6);
      arrow.getArrowType().entityHit(arrow, entityHit(pig));
      float expected = ArcanaNovum.CONFIG.getFloatList(ArcanaConfig.TRACKING_ARROW_BROADHEAD_DMG_AMP_PER_LVL).get(3);
      helper.assertTrue(Conditions.getConditionValue(pig, Conditions.VULNERABILITY) == expected, "Broadheads 3 gave the wrong Vulnerability");
      finish(helper, player);
   }

   @GameTest(padding = 32)
   @Covers("runic_guidance")
   public void runicGuidanceWidensTheTrackingCone(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.teleportTo(player.getX(), player.getY() + 40, player.getZ());
      mob(helper, EntityTypes.PIG, 7, 2, 13); // 3 blocks off the line, 12 ahead: inside the cone at the highest level only

      RunicArrowEntity plain = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, null, 0, 4, 2.45, 1);
      plain.setDeltaMovement(0, 0, 0.5);
      plain.tick();
      helper.assertTrue(plain.getDeltaMovement().x == 0, "A plain Tracking Arrow turned toward a target outside its cone");

      RunicArrowEntity guided = arrow(helper, player, ArcanaRegistry.TRACKING_ARROWS, ArcanaAugments.RUNIC_GUIDANCE, 3, 4, 2.45, 1);
      guided.setDeltaMovement(0, 0, 0.5);
      guided.tick();
      helper.assertTrue(guided.getDeltaMovement().x > 0, "Runic Guidance 3 did not turn toward the target");
      finish(helper, player);
   }

   // ===== Any Runic Arrow =====

   private static void hitThroughTheEntity(RunicArrowEntity arrow, net.minecraft.world.entity.Entity target){
      try{
         Method onHitEntity = RunicArrowEntity.class.getDeclaredMethod("onHitEntity", net.minecraft.world.phys.EntityHitResult.class);
         onHitEntity.setAccessible(true);
         onHitEntity.invoke(arrow, entityHit(target));
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("RunicArrowEntity.onHitEntity changed signature, update this test", e);
      }
   }

   @GameTest
   @Covers("aimbot")
   public void hittingAMobFromAHundredBlocksGrantsAimbot(GameTestHelper helper){
      ServerPlayer player = player(helper);
      IronGolem golem = mob(helper, EntityTypes.IRON_GOLEM, 4, 2, 4);
      RunicArrowEntity arrow = arrow(helper, player, ArcanaRegistry.SIPHONING_ARROWS, null, 0, 4, 3, 4);
      hitThroughTheEntity(arrow, golem);
      assertNotAchieved(helper, player, ArcanaAchievements.AIMBOT);

      player.teleportTo(player.getX(), player.getY(), player.getZ() + 120);
      RunicArrowEntity far = arrow(helper, player, ArcanaRegistry.SIPHONING_ARROWS, null, 0, 4, 3, 4);
      hitThroughTheEntity(far, golem);
      assertAchieved(helper, player, ArcanaAchievements.AIMBOT);
      finish(helper, player);
   }

   @GameTest
   @Covers("arrow_for_every_foe")
   public void hittingWithNineArrowTypesGrantsArrowForEveryFoe(GameTestHelper helper){
      ServerPlayer player = player(helper);
      List<ArcanaItem> types = List.of(ArcanaRegistry.ARCANE_FLAK_ARROWS, ArcanaRegistry.BLINK_ARROWS, ArcanaRegistry.CONCUSSION_ARROWS, ArcanaRegistry.DETONATION_ARROWS,
            ArcanaRegistry.EXPULSION_ARROWS, ArcanaRegistry.GRAVITON_ARROWS, ArcanaRegistry.PHOTONIC_ARROWS, ArcanaRegistry.SIPHONING_ARROWS, ArcanaRegistry.SMOKE_ARROWS);
      for(int i = 0; i < types.size(); i++){
         assertNotAchieved(helper, player, ArcanaAchievements.ARROW_FOR_EVERY_FOE);
         arrow(helper, player, types.get(i), null, 0, 3, 3, 3).incArrowForEveryFoe(player);
         arrow(helper, player, types.get(i), null, 0, 3, 3, 3).incArrowForEveryFoe(player); // Repeats of one type count once
      }
      assertAchieved(helper, player, ArcanaAchievements.ARROW_FOR_EVERY_FOE);
      finish(helper, player);
   }
}
