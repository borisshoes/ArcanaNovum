package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugment;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.gui.brainjar.BrainJarGui;
import net.borisshoes.arcananovum.items.AncientDowsingRod;
import net.borisshoes.arcananovum.items.BrainJar;
import net.borisshoes.arcananovum.items.ChestTranslocator;
import net.borisshoes.arcananovum.items.ClockworkMultitool;
import net.borisshoes.arcananovum.items.ContainmentCirclet;
import net.borisshoes.arcananovum.items.EssenceEgg;
import net.borisshoes.arcananovum.items.EverlastingRocket;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.borisshoes.arcananovum.test.gametest.RegressionGameTests.finish;
import static net.borisshoes.arcananovum.test.gametest.TestKit.*;

/** Achievements and augments of the handheld Arcana Items */
public class ItemGameTests {

   /** Checks an energy item whose capacity is a cooldown or charge count that one augment changes by a config list */
   private static void assertMaxEnergyChange(GameTestHelper helper, ArcanaItem item, ArcanaAugment augment, int level, int expectedChange){
      EnergyItem energyItem = (EnergyItem) item;
      int plain = energyItem.getMaxEnergy(item.getNewItem());
      int augmented = energyItem.getMaxEnergy(item(item, augment, level));
      helper.assertTrue(expectedChange != 0, augment.id + " " + level + " changes nothing in its config");
      helper.assertTrue(augmented - plain == expectedChange, augment.id + " " + level + " changed the maximum by " + (augmented - plain) + " instead of " + expectedChange);
   }

   private static ItemStack charged(ItemStack stack){
      EnergyItem item = (EnergyItem) net.borisshoes.arcananovum.utils.ArcanaItemUtils.identifyItem(stack);
      item.setEnergy(stack, item.getMaxEnergy(stack));
      return stack;
   }

   // ===== Ancient Dowsing Rod =====

   @GameTest(maxTicks = 170, padding = 32)
   @Covers({"motherload", "archeologist"})
   public void findingDebrisGrantsDowsingAchievements(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.ARCHEOLOGIST, 10);
      for(int i = 0; i < 10; i++) helper.setBlock(new BlockPos(1 + i % 5, 1, 3 + i / 5), Blocks.ANCIENT_DEBRIS);
      use(helper, player, charged(ArcanaRegistry.ANCIENT_DOWSING_ROD.getNewItem()));
      assertNotAchieved(helper, player, ArcanaAchievements.MOTHERLOAD);
      helper.runAfterDelay(145, () -> {
         assertAchieved(helper, player, ArcanaAchievements.MOTHERLOAD);
         assertAchieved(helper, player, ArcanaAchievements.ARCHEOLOGIST);
         finish(helper, player);
      });
   }

   @GameTest(maxTicks = 170, padding = 32)
   @Covers("enhanced_resonance")
   public void enhancedResonanceScansFarther(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      ServerPlayer tuned = player(helper);
      int extra = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ANCIENT_DOWSING_ROD_RANGE_PER_LVL).get(3);
      helper.assertTrue(extra > 0, "Enhanced Resonance 3 adds no range");
      int distance = 2 + Math.min(extra, 4);
      helper.setBlock(new BlockPos(1, 2, 1 + distance), Blocks.ANCIENT_DEBRIS);
      withConfig(ArcanaConfig.ANCIENT_DOWSING_ROD_RANGE, 2, () -> {
         use(helper, plain, charged(ArcanaRegistry.ANCIENT_DOWSING_ROD.getNewItem()));
         use(helper, tuned, charged(item(ArcanaRegistry.ANCIENT_DOWSING_ROD, ArcanaAugments.ENHANCED_RESONANCE, 3)));
      });
      helper.runAfterDelay(145, () -> {
         helper.assertTrue(ArcanaAchievements.getProgress(plain, ArcanaAchievements.ARCHEOLOGIST) == 0, "The plain rod found debris out of its range");
         helper.assertTrue(ArcanaAchievements.getProgress(tuned, ArcanaAchievements.ARCHEOLOGIST) == 1, "Enhanced Resonance 3 did not find the debris");
         finish(helper, plain, tuned);
      });
   }

   @GameTest
   @Covers("sonic_reabsorption")
   public void sonicReabsorptionShortensTheRodCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.ANCIENT_DOWSING_ROD, ArcanaAugments.SONIC_REABSORPTION, 3, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ANCIENT_DOWSING_ROD_COOLDOWN_PER_LVL).get(3));
      helper.succeed();
   }

   // ===== Brain in a Jar =====

   private static BrainJar jar(){
      return (BrainJar) ArcanaRegistry.BRAIN_JAR;
   }

   private static ItemStack mendingPick(GameTestHelper helper){
      ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
      pick.enchant(MinecraftUtils.getEnchantment(Enchantments.MENDING), 1);
      pick.setDamageValue(1000);
      return pick;
   }

   @GameTest
   @Covers("break_bank")
   public void fillingTheJarGrantsBreakBank(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack stack = ArcanaRegistry.BRAIN_JAR.getNewItem();
      jar().setEnergy(stack, jar().getMaxEnergy(stack) - 5);
      player.giveExperiencePoints(3);
      BrainJarGui gui = new BrainJarGui(MenuType.HOPPER, player, jar(), stack);
      jar().depositXP(player, stack, false, gui);
      assertNotAchieved(helper, player, ArcanaAchievements.BREAK_BANK);
      player.giveExperiencePoints(10);
      jar().depositXP(player, stack, false, gui);
      helper.assertTrue(jar().getEnergy(stack) == jar().getMaxEnergy(stack), "The jar should be full");
      helper.assertTrue(player.totalExperience == 8, "Only what fits should leave the player, who has " + player.totalExperience + " left");
      assertAchieved(helper, player, ArcanaAchievements.BREAK_BANK);
      finish(helper, player);
   }

   @GameTest
   @Covers("certified_repair")
   public void mendingFromTheJarGrantsCertifiedRepair(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.CERTIFIED_REPAIR, 1);
      ItemStack pick = mendingPick(helper);
      player.getInventory().setItem(0, pick);
      ItemStack stack = ArcanaRegistry.BRAIN_JAR.getNewItem();
      jar().setEnergy(stack, 100);
      ArcanaItem.putProperty(stack, ArcanaItem.ACTIVE_TAG, true);
      inventoryTick(helper, player, stack);
      helper.assertTrue(pick.getDamageValue() < 1000, "The jar did not mend the tool");
      helper.assertTrue(jar().getEnergy(stack) == 99, "Mending should use one stored XP");
      assertAchieved(helper, player, ArcanaAchievements.CERTIFIED_REPAIR);
      finish(helper, player);
   }

   private static int repairedBy(GameTestHelper helper, ServerPlayer player, int level){
      ItemStack pick = mendingPick(helper);
      player.getInventory().setItem(0, pick);
      ItemStack stack = item(ArcanaRegistry.BRAIN_JAR, ArcanaAugments.TRADE_SCHOOL, level);
      jar().setEnergy(stack, 100);
      ArcanaItem.putProperty(stack, ArcanaItem.ACTIVE_TAG, true);
      inventoryTick(helper, player, stack);
      return 1000 - pick.getDamageValue();
   }

   @GameTest
   @Covers("trade_school")
   public void tradeSchoolRepairsMorePerXp(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int plain = repairedBy(helper, player, 0);
      int school = repairedBy(helper, player, 5);
      helper.assertTrue(plain > 0 && school > plain, "Trade School 5 repaired " + school + " durability against " + plain);
      finish(helper, player);
   }

   @GameTest
   @Covers("knowledge_bank")
   public void knowledgeBankPaysInterest(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack plain = ArcanaRegistry.BRAIN_JAR.getNewItem();
      ItemStack bank = item(ArcanaRegistry.BRAIN_JAR, ArcanaAugments.KNOWLEDGE_BANK, 3);
      jar().setEnergy(plain, 10000);
      jar().setEnergy(bank, 10000);
      ArcanaItem.putProperty(plain, ArcanaItem.ACTIVE_TAG, true);
      ArcanaItem.putProperty(bank, ArcanaItem.ACTIVE_TAG, true);
      withConfig(ArcanaConfig.BRAIN_JAR_INTEREST_TICK, 1, () -> {
         inventoryTick(helper, player, plain);
         inventoryTick(helper, player, bank);
      });
      helper.assertTrue(jar().getEnergy(plain) == 10000, "A plain jar earned interest");
      int expected = 10000 + (int) (ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.BRAIN_JAR_INTEREST_PER_LVL).get(3) * 10000);
      helper.assertTrue(jar().getEnergy(bank) == expected, "Knowledge Bank 3 holds " + jar().getEnergy(bank) + " instead of " + expected);
      finish(helper, player);
   }

   @GameTest
   @Covers("unending_wisdom")
   public void unendingWisdomRaisesTheJarCapacity(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.BRAIN_JAR, ArcanaAugments.UNENDING_WISDOM, 5, ArcanaNovum.CONFIG.getIntList(ArcanaConfig.BRAIN_JAR_MAX_XP_PER_LVL).get(5));
      helper.succeed();
   }

   // ===== Chest Translocator =====

   private static ItemStack translocate(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos chest){
      ((ChestTranslocator) ArcanaRegistry.CHEST_TRANSLOCATOR).setEnergy(stack, 0);
      useOn(helper, player, stack, chest, Direction.UP);
      helper.assertBlockNotPresent(Blocks.CHEST, chest);
      return stack;
   }

   @GameTest
   @Covers("peak_laziness")
   public void movingAnEmptyChestGrantsPeakLaziness(GameTestHelper helper){
      ServerPlayer player = player(helper);
      BlockPos chest = new BlockPos(3, 2, 3);
      helper.setBlock(chest, Blocks.CHEST);
      ItemStack stack = translocate(helper, player, ArcanaRegistry.CHEST_TRANSLOCATOR.getNewItem(), chest);
      helper.setBlock(chest, Blocks.STONE);
      useOn(helper, player, stack, chest, Direction.UP);
      helper.assertBlockPresent(Blocks.CHEST, chest.above());
      assertAchieved(helper, player, ArcanaAchievements.PEAK_LAZINESS);
      finish(helper, player);
   }

   @GameTest
   @Covers("storage_relocation")
   public void movingAFullChestGrantsStorageRelocation(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.STORAGE_RELOCATION, 1);
      BlockPos chest = new BlockPos(3, 2, 3);
      helper.setBlock(chest, Blocks.CHEST);
      ChestBlockEntity entity = helper.getBlockEntity(chest, ChestBlockEntity.class);
      for(int i = 0; i < entity.getContainerSize(); i++) entity.setItem(i, new ItemStack(Items.DIRT));
      ItemStack stack = translocate(helper, player, ArcanaRegistry.CHEST_TRANSLOCATOR.getNewItem(), chest);
      helper.setBlock(chest, Blocks.STONE);
      useOn(helper, player, stack, chest, Direction.UP);
      ChestBlockEntity moved = helper.getBlockEntity(chest.above(), ChestBlockEntity.class);
      helper.assertTrue(moved.getItem(26).is(Items.DIRT), "The chest's contents were lost in the move");
      assertAchieved(helper, player, ArcanaAchievements.STORAGE_RELOCATION);
      finish(helper, player);
   }

   @GameTest
   @Covers("rapid_translocation")
   public void rapidTranslocationShortensTheCooldown(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.CHEST_TRANSLOCATOR, ArcanaAugments.RAPID_TRANSLOCATION, 3, -ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CHEST_TRANSLOCATOR_COOLDOWN_PER_LVL).get(3));
      helper.succeed();
   }

   // ===== Clockwork Multitool =====

   private static Set<String> multitoolModes(ServerPlayer player, ItemStack stack){
      Set<String> modes = new HashSet<>();
      for(int i = 0; i < 12; i++){
         charged(stack);
         ClockworkMultitool.ClockworkMultitoolItem.cycleMode(player, stack, false);
         modes.add(ArcanaItem.getStringProperty(stack, ArcanaItem.MODE_TAG));
      }
      return modes;
   }

   @GameTest
   @Covers("fidget_toy")
   public void cyclingTheMultitoolGrantsFidgetToy(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.FIDGET_TOY, 1);
      ItemStack stack = ArcanaRegistry.CLOCKWORK_MULTITOOL.getNewItem();
      ((ClockworkMultitool) ArcanaRegistry.CLOCKWORK_MULTITOOL).setEnergy(stack, 0);
      ClockworkMultitool.ClockworkMultitoolItem.cycleMode(player, stack, false);
      assertNotAchieved(helper, player, ArcanaAchievements.FIDGET_TOY);
      ClockworkMultitool.ClockworkMultitoolItem.cycleMode(player, charged(stack), false);
      assertAchieved(helper, player, ArcanaAchievements.FIDGET_TOY);
      finish(helper, player);
   }

   @GameTest
   @Covers({"repairing_mechanism", "enchantment_mechanism", "ender_mechanism"})
   public void mechanismsAddTheirStationsToTheMultitool(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Set<String> plain = multitoolModes(player, ArcanaRegistry.CLOCKWORK_MULTITOOL.getNewItem());
      helper.assertTrue(!plain.contains("anvil") && !plain.contains("enchanting") && !plain.contains("enderchest"), "A plain Multitool has extra stations: " + plain);
      helper.assertTrue(multitoolModes(player, item(ArcanaRegistry.CLOCKWORK_MULTITOOL, ArcanaAugments.REPAIRING_MECHANISM, 1)).contains("anvil"), "Repairing Mechanism added no anvil");
      helper.assertTrue(multitoolModes(player, item(ArcanaRegistry.CLOCKWORK_MULTITOOL, ArcanaAugments.ENCHANTMENT_MECHANISM, 1)).contains("enchanting"), "Enchantment Mechanism added no table");
      helper.assertTrue(multitoolModes(player, item(ArcanaRegistry.CLOCKWORK_MULTITOOL, ArcanaAugments.ENDER_MECHANISM, 1)).contains("enderchest"), "Ender Mechanism added no ender chest");
      finish(helper, player);
   }

   // ===== Containment Circlet =====

   private static ContainmentCirclet circlet(){
      return (ContainmentCirclet) ArcanaRegistry.CONTAINMENT_CIRCLET;
   }

   @GameTest
   @Covers("i_choose_you")
   public void releasingYourPetGrantsIChooseYou(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Wolf wolf = mob(helper, EntityTypes.WOLF, 3, 2, 3);
      wolf.tame(player);
      ItemStack stack = ArcanaRegistry.CONTAINMENT_CIRCLET.getNewItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      circlet().useOnEntity(player, wolf, InteractionHand.MAIN_HAND);
      helper.assertTrue(wolf.isRemoved(), "The Circlet did not capture the wolf");
      helper.setBlock(new BlockPos(4, 2, 4), Blocks.STONE);
      useOn(helper, player, stack, new BlockPos(4, 2, 4), Direction.UP);
      assertAchieved(helper, player, ArcanaAchievements.I_CHOOSE_YOU);
      finish(helper, player);
   }

   @GameTest
   @Covers("confinement")
   public void confinementCapturesHostileMobs(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 3, 2, 3);
      ItemStack plain = ArcanaRegistry.CONTAINMENT_CIRCLET.getNewItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, plain);
      circlet().useOnEntity(player, zombie, InteractionHand.MAIN_HAND);
      helper.assertFalse(zombie.isRemoved(), "A plain Circlet captured a hostile mob");

      ItemStack confining = item(ArcanaRegistry.CONTAINMENT_CIRCLET, ArcanaAugments.CONFINEMENT, 1);
      player.setItemInHand(InteractionHand.MAIN_HAND, confining);
      circlet().useOnEntity(player, zombie, InteractionHand.MAIN_HAND);
      helper.assertTrue(zombie.isRemoved(), "Confinement did not capture the zombie");
      finish(helper, player);
   }

   @GameTest(maxTicks = 30)
   @Covers("healing_circlet")
   public void healingCircletHealsItsCaptive(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack plain = ArcanaRegistry.CONTAINMENT_CIRCLET.getNewItem();
      ItemStack healing = item(ArcanaRegistry.CONTAINMENT_CIRCLET, ArcanaAugments.HEALING_CIRCLET, 1);
      for(ItemStack stack : List.of(plain, healing)){
         ArcanaItem.putProperty(stack, ContainmentCirclet.HP_TAG, 2f);
         ArcanaItem.putProperty(stack, ContainmentCirclet.MAX_HP_TAG, 20f);
      }
      tickUntil(helper, player, healing, () -> {
         inventoryTick(helper, player, plain);
         helper.assertTrue(ArcanaItem.getFloatProperty(plain, ContainmentCirclet.HP_TAG) == 2f, "A plain Circlet healed its captive");
         helper.assertTrue(ArcanaItem.getFloatProperty(healing, ContainmentCirclet.HP_TAG) > 2f, "Waiting for the Circlet to heal its captive");
      });
   }

   // ===== Essence Egg =====

   private static ItemStack egg(ArcanaAugment augment, int level, int uses){
      ItemStack stack = augment == null ? ArcanaRegistry.ESSENCE_EGG.getNewItem() : item(ArcanaRegistry.ESSENCE_EGG, augment, level);
      EssenceEgg.setType(stack, "minecraft:pig");
      EssenceEgg.setUses(stack, uses);
      return stack;
   }

   private static void convertSpawner(GameTestHelper helper, ServerPlayer player, ItemStack stack){
      BlockPos spawner = new BlockPos(3, 2, 3);
      helper.setBlock(spawner, Blocks.SPAWNER);
      useOn(helper, player, stack, spawner, Direction.UP);
   }

   @GameTest
   @Covers({"soul_conversion", "willing_captive"})
   public void convertingASpawnerGrantsSoulConversionAndWillingCaptiveSavesUses(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int cost = ArcanaNovum.CONFIG.getInt(ArcanaConfig.ESSENCE_EGG_SPAWNER_USES);
      ItemStack plain = egg(null, 0, cost + 2);
      convertSpawner(helper, player, plain);
      helper.assertTrue(EssenceEgg.getUses(plain) == 2, "Converting a spawner left " + EssenceEgg.getUses(plain) + " uses instead of 2");
      assertAchieved(helper, player, ArcanaAchievements.SOUL_CONVERSION);

      int decrease = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.ESSENCE_EGG_WILLING_CAPTIVE_DECREASE).get(5);
      helper.assertTrue(decrease > 0, "Willing Captive 5 saves nothing");
      ItemStack captive = egg(ArcanaAugments.WILLING_CAPTIVE, 5, cost + 2);
      convertSpawner(helper, player, captive);
      int expected = 2 + Math.min(decrease, cost);
      helper.assertTrue(EssenceEgg.getUses(captive) == expected, "Willing Captive 5 left " + EssenceEgg.getUses(captive) + " uses instead of " + expected);
      finish(helper, player);
   }

   // Math.min(0, xp) in EssenceEgg could never add anything, so converting a spawner gave no XP
   @GameTest
   public void convertingASpawnerGivesXp(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int before = ArcanaNovum.data(player).getXP();
      convertSpawner(helper, player, egg(null, 0, ArcanaNovum.CONFIG.getInt(ArcanaConfig.ESSENCE_EGG_SPAWNER_USES)));
      int gained = ArcanaNovum.data(player).getXP() - before - ArcanaAchievements.SOUL_CONVERSION.xpReward;
      helper.assertTrue(gained > 0, "Converting a spawner gave no XP");
      finish(helper, player);
   }

   private static int pigsNear(GameTestHelper helper, BlockPos relative){
      BlockPos pos = helper.absolutePos(relative);
      return helper.getLevel().getEntitiesOfClass(Pig.class, new AABB(pos).inflate(3)).size();
   }

   @GameTest
   @Covers("soul_for_soul")
   public void spawningFromTheEggGrantsSoulForSoul(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.SOUL_FOR_SOUL, 1);
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
      ItemStack stack = egg(null, 0, 3);
      useOn(helper, player, stack, new BlockPos(3, 2, 3), Direction.UP);
      helper.assertTrue(pigsNear(helper, new BlockPos(3, 3, 3)) == 1, "The egg did not spawn one pig");
      helper.assertTrue(EssenceEgg.getUses(stack) == 2, "Spawning should use one charge");
      assertAchieved(helper, player, ArcanaAchievements.SOUL_FOR_SOUL);
      finish(helper, player);
   }

   @GameTest
   @Covers("soul_split")
   public void soulSplitSpawnsTwoMobsAtItsChance(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
      withConfig(ArcanaConfig.ESSENCE_EGG_SOUL_SPLIT_CHANCE, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 1.0), () -> {
         useOn(helper, player, egg(ArcanaAugments.SOUL_SPLIT, 4, 3), new BlockPos(3, 2, 3), Direction.UP);
         helper.assertTrue(pigsNear(helper, new BlockPos(3, 3, 3)) == 1, "The chance for level 4 was not read from its slot");
         useOn(helper, player, egg(ArcanaAugments.SOUL_SPLIT, 5, 3), new BlockPos(3, 2, 3), Direction.UP);
         helper.assertTrue(pigsNear(helper, new BlockPos(3, 3, 3)) == 3, "Soul Split 5 at chance 1 did not spawn two mobs");
      });
      finish(helper, player);
   }

   @GameTest
   @Covers("determined_spirit")
   public void determinedSpiritKeepsTheUseAtItsChance(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
      withConfig(ArcanaConfig.ESSENCE_EGG_EFFICIENCY_PER_LVL, List.of(0.0, 0.0, 0.0, 0.0, 0.0, 1.0), () -> {
         ItemStack plain = egg(ArcanaAugments.DETERMINED_SPIRIT, 4, 3);
         useOn(helper, player, plain, new BlockPos(3, 2, 3), Direction.UP);
         helper.assertTrue(EssenceEgg.getUses(plain) == 2, "The chance for level 4 was not read from its slot");
         ItemStack determined = egg(ArcanaAugments.DETERMINED_SPIRIT, 5, 3);
         useOn(helper, player, determined, new BlockPos(3, 2, 3), Direction.UP);
         helper.assertTrue(EssenceEgg.getUses(determined) == 3, "Determined Spirit 5 at chance 1 used a charge");
      });
      finish(helper, player);
   }

   // ===== Everlasting Rocket =====

   private static EverlastingRocket rocket(){
      return (EverlastingRocket) ArcanaRegistry.EVERLASTING_ROCKET;
   }

   @GameTest
   @Covers("rocketman")
   public void boostingHighUpGrantsRocketman(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.startFallFlying();
      ItemStack stack = charged(ArcanaRegistry.EVERLASTING_ROCKET.getNewItem());
      int before = rocket().getEnergy(stack);
      use(helper, player, stack);
      helper.assertTrue(rocket().getEnergy(stack) == before - 1, "Boosting should use one charge");
      assertNotAchieved(helper, player, ArcanaAchievements.ROCKETMAN);
      player.teleportTo(player.getX(), 510, player.getZ());
      player.startFallFlying();
      use(helper, player, stack);
      assertAchieved(helper, player, ArcanaAchievements.ROCKETMAN);
      finish(helper, player);
   }

   @GameTest
   @Covers("missile_launcher")
   public void firingFiveRocketsFromACrossbowGrantsMissileLauncher(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack stack = charged(ArcanaRegistry.EVERLASTING_ROCKET.getNewItem());
      player.getInventory().setItem(0, stack);
      int before = rocket().getEnergy(stack);
      ItemStack firework = rocket().getFireworkStack(stack);
      for(int i = 0; i < 4; i++) EverlastingRocket.decreaseRocket(firework, player);
      helper.assertTrue(rocket().getEnergy(stack) == before - 4, "Firing should use the rocket's charges");
      assertNotAchieved(helper, player, ArcanaAchievements.MISSILE_LAUNCHER);
      EverlastingRocket.decreaseRocket(firework, player);
      assertAchieved(helper, player, ArcanaAchievements.MISSILE_LAUNCHER);
      finish(helper, player);
   }

   @GameTest
   @Covers("adjustable_fuse")
   public void adjustableFuseCyclesTheFlightDuration(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setShiftKeyDown(true);
      ItemStack plain = ArcanaRegistry.EVERLASTING_ROCKET.getNewItem();
      int plainFlight = plain.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1, List.of())).flightDuration();
      use(helper, player, plain);
      helper.assertTrue(plain.getOrDefault(DataComponents.FIREWORKS, new Fireworks(1, List.of())).flightDuration() == plainFlight, "A plain rocket changed its fuse");

      ItemStack stack = item(ArcanaRegistry.EVERLASTING_ROCKET, ArcanaAugments.ADJUSTABLE_FUSE, 1);
      List<Integer> flights = new ArrayList<>();
      for(int i = 0; i < 3; i++){
         use(helper, player, stack);
         flights.add(stack.get(DataComponents.FIREWORKS).flightDuration());
      }
      helper.assertTrue(flights.equals(List.of(2, 3, 1)), "Adjustable Fuse cycled through " + flights);
      finish(helper, player);
   }

   @GameTest
   @Covers("powder_packing")
   public void powderPackingAddsCharges(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.EVERLASTING_ROCKET, ArcanaAugments.POWDER_PACKING, 3, ArcanaNovum.CONFIG.getIntList(ArcanaConfig.EVERLASTING_ROCKET_CHARGES_PER_LVL).get(3));
      helper.succeed();
   }

   @GameTest
   @Covers("sulfur_replication")
   public void sulfurReplicationRechargesFaster(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int reduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.EVERLASTING_ROCKET_COOLDOWN_PER_LVL).get(3);
      helper.assertTrue(reduction > 0, "Sulfur Replication 3 removes no cooldown");
      int cooldown = reduction + 1; // Level 3 recharges every tick with this, level 0 only every few
      ItemStack plain = ArcanaRegistry.EVERLASTING_ROCKET.getNewItem();
      ItemStack sulfur = item(ArcanaRegistry.EVERLASTING_ROCKET, ArcanaAugments.SULFUR_REPLICATION, 3);
      rocket().setEnergy(plain, 0);
      rocket().setEnergy(sulfur, 0);
      withConfig(ArcanaConfig.EVERLASTING_ROCKET_COOLDOWN, cooldown, () -> {
         inventoryTick(helper, player, plain);
         inventoryTick(helper, player, sulfur);
      });
      helper.assertTrue(rocket().getEnergy(sulfur) == 1, "Sulfur Replication 3 should recharge on every tick here");
      helper.assertTrue(rocket().getEnergy(plain) == (helper.getLevel().getServer().getTickCount() % cooldown == 0 ? 1 : 0), "The plain rocket recharged off its interval");
      finish(helper, player);
   }

   // ===== Exotic Matter =====

   @GameTest
   @Covers("time_in_a_bottle")
   public void timeInABottleAddsFuelAndRefills(GameTestHelper helper){
      assertMaxEnergyChange(helper, ArcanaRegistry.EXOTIC_MATTER, ArcanaAugments.TIME_IN_A_BOTTLE, 5, ArcanaNovum.CONFIG.getIntList(ArcanaConfig.EXOTIC_MATTER_DURATION_PER_LVL).get(5));
      ExoticMatter matter = (ExoticMatter) ArcanaRegistry.EXOTIC_MATTER;
      ItemStack stack = ArcanaRegistry.EXOTIC_MATTER.getNewItem();
      matter.useFuel(stack, 1000);
      ArcanaAugments.applyAugment(stack, ArcanaAugments.TIME_IN_A_BOTTLE, 1, false);
      helper.assertTrue(matter.getEnergy(stack) == matter.getMaxEnergy(stack), "Augmenting did not refill the fuel");
      helper.succeed();
   }
}
