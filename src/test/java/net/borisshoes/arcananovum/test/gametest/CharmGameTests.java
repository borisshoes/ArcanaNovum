package net.borisshoes.arcananovum.test.gametest;

import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.charms.CindersCharm;
import net.borisshoes.arcananovum.items.charms.CleansingCharm;
import net.borisshoes.arcananovum.items.charms.LightCharm;
import net.borisshoes.arcananovum.items.charms.MagnetismCharm;
import net.borisshoes.arcananovum.items.charms.NegotiationCharm;
import net.borisshoes.arcananovum.items.charms.WildGrowthCharm;
import net.borisshoes.borislib.conditions.Conditions;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.borisshoes.arcananovum.test.gametest.RegressionGameTests.finish;
import static net.borisshoes.arcananovum.test.gametest.TestKit.*;

/** Achievements and augments of the Charms */
public class CharmGameTests {

   // ===== Charm of Cinders =====

   private static CindersCharm cinders(){
      return (CindersCharm) ArcanaRegistry.CINDERS_CHARM;
   }

   private static ItemStack fullCinders(ItemStack stack){
      ArcanaItem.putProperty(stack, ArcanaItem.ACTIVE_TAG, true);
      cinders().setEnergy(stack, cinders().getMaxEnergy(stack));
      return stack;
   }

   @GameTest
   @Covers("cake_day")
   public void lightingACandleCakeGrantsCakeDay(GameTestHelper helper){
      ServerPlayer player = player(helper);
      BlockPos cake = new BlockPos(3, 2, 3);
      helper.setBlock(cake, Blocks.CANDLE_CAKE.defaultBlockState());
      ItemStack charm = fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem());
      player.setItemInHand(InteractionHand.MAIN_HAND, charm);
      cinders().attackBlock(player, helper.getLevel(), InteractionHand.MAIN_HAND, helper.absolutePos(cake), Direction.UP);
      helper.assertTrue(helper.getBlockState(cake).getValue(BlockStateProperties.LIT), "The cake was not lit");
      assertAchieved(helper, player, ArcanaAchievements.CAKE_DAY);
      finish(helper, player);
   }

   @GameTest
   @Covers("firestarter")
   public void firestarterLightsForFree(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.assertTrue(lightCost(helper, player, 0) == 5, "Lighting a campfire without Firestarter should cost 5 cinders");
      helper.assertTrue(lightCost(helper, player, 1) == 0, "Lighting a campfire with Firestarter should be free");
      finish(helper, player);
   }

   private static int lightCost(GameTestHelper helper, ServerPlayer player, int level){
      BlockPos campfire = new BlockPos(3, 2, 3);
      helper.setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(BlockStateProperties.LIT, false));
      ItemStack charm = fullCinders(item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.FIRESTARTER, level));
      player.setItemInHand(InteractionHand.MAIN_HAND, charm);
      int before = cinders().getEnergy(charm);
      cinders().attackBlock(player, helper.getLevel(), InteractionHand.MAIN_HAND, helper.absolutePos(campfire), Direction.UP);
      helper.assertTrue(helper.getBlockState(campfire).getValue(BlockStateProperties.LIT), "The campfire was not lit");
      return before - cinders().getEnergy(charm);
   }

   @GameTest
   @Covers("glassblower")
   public void smeltingGlassGrantsGlassblower(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.GLASSBLOWER, 8);
      ItemStack charm = fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem());
      assertNotAchieved(helper, player, ArcanaAchievements.GLASSBLOWER);
      ItemStack glass = cinders().smelt(charm, player, new ItemStack(Items.SAND, 8));
      helper.assertTrue(glass != null && glass.is(Items.GLASS) && glass.getCount() == 8, "8 sand did not smelt into 8 glass");
      assertAchieved(helper, player, ArcanaAchievements.GLASSBLOWER);
      finish(helper, player);
   }

   @GameTest
   @Covers("supersmelter")
   public void supersmelterUsesFewerCinders(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack plain = fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem());
      ItemStack smelter = fullCinders(item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.SUPERSMELTER, 1));
      int plainBefore = cinders().getEnergy(plain);
      int smelterBefore = cinders().getEnergy(smelter);
      cinders().smelt(plain, player, new ItemStack(Items.RAW_IRON, 32));
      cinders().smelt(smelter, player, new ItemStack(Items.RAW_IRON, 32));
      int plainCost = plainBefore - cinders().getEnergy(plain);
      int smelterCost = smelterBefore - cinders().getEnergy(smelter);
      int expected = (int) Math.ceil(32 / (2.0 * ArcanaNovum.CONFIG.getDouble(ArcanaConfig.CINDERS_CHARM_SUPERSMELTER_MULTIPLIER)));
      helper.assertTrue(plainCost == 16, "Smelting 32 items cost " + plainCost + " cinders instead of 16");
      helper.assertTrue(smelterCost == expected, "Supersmelter cost " + smelterCost + " cinders instead of " + expected);
      finish(helper, player);
   }

   @GameTest
   @Covers("pyromaniac")
   public void igniting12MobsGrantsPyromaniac(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 11; i++) mob(helper, EntityTypes.IRON_GOLEM, 1.5, 2, 3 + i * 0.3);
      use(helper, player, fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem()));
      assertNotAchieved(helper, player, ArcanaAchievements.PYROMANIAC);
      mob(helper, EntityTypes.IRON_GOLEM, 1.5, 2, 6.5);
      use(helper, player, fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem()));
      assertAchieved(helper, player, ArcanaAchievements.PYROMANIAC);
      finish(helper, player);
   }

   @GameTest
   @Covers("pyroblast")
   public void pyroblastSpendsAllCindersOnABlast(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.setBlock(new BlockPos(1, 3, 7), Blocks.STONE); // Stops the ray, so the fireball lands next to the golem
      IronGolem golem = mob(helper, EntityTypes.IRON_GOLEM, 1.5, 2, 6);
      ItemStack charm = fullCinders(item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.PYROBLAST, 1));
      use(helper, player, charm);
      helper.assertTrue(cinders().getEnergy(charm) == 0, "Pyroblast left " + cinders().getEnergy(charm) + " cinders");
      helper.assertTrue(golem.getHealth() < golem.getMaxHealth(), "Pyroblast did no damage where it landed");
      finish(helper, player);
   }

   @GameTest
   @Covers("web_of_fire")
   public void webOfFireHitsItsNumberOfTargets(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int targets = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CINDERS_CHARM_FIREWEB_CREATURES_PER_LVL).get(1);
      List<IronGolem> golems = new ArrayList<>();
      for(int i = 0; i < targets + 2; i++) golems.add(mob(helper, EntityTypes.IRON_GOLEM, 2 + i * 0.1, 2, 2));
      ItemStack charm = fullCinders(item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.WEB_OF_FIRE, 1));
      use(helper, player, charm);
      long hit = golems.stream().filter(golem -> golem.getHealth() < golem.getMaxHealth()).count();
      helper.assertTrue(hit == targets, "Web of Fire 1 hit " + hit + " creatures instead of " + targets);
      finish(helper, player);
   }

   @GameTest
   @Covers("cremation")
   public void cremationHitsHarderAndBurnsCindersToBlockFire(GameTestHelper helper){
      ServerPlayer player = player(helper);
      IronGolem plain = mob(helper, EntityTypes.IRON_GOLEM, 1.5, 2, 4);
      use(helper, player, fullCinders(ArcanaRegistry.CINDERS_CHARM.getNewItem()));
      float plainDamage = plain.getMaxHealth() - plain.getHealth();
      plain.discard();
      IronGolem blue = mob(helper, EntityTypes.IRON_GOLEM, 1.5, 2, 4);
      ItemStack charm = fullCinders(item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.CREMATION, 1));
      use(helper, player, charm);
      float blueDamage = blue.getMaxHealth() - blue.getHealth();
      float mod = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.CINDERS_CHARM_CREMATION_MULTIPLIER);
      helper.assertTrue(Math.abs(blueDamage - plainDamage * mod) < 0.01, "Cremation did " + blueDamage + " damage, expected " + plainDamage * mod);

      int before = cinders().getEnergy(charm);
      player.hurtServer(helper.getLevel(), player.damageSources().inFire(), 4f);
      helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Cinders did not soak the fire damage");
      helper.assertTrue(cinders().getEnergy(charm) < before, "Soaking fire damage used no cinders");
      finish(helper, player);
   }

   @GameTest(maxTicks = 40)
   @Covers("wildfire")
   public void wildfireAddsCindersAndRegeneration(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack plain = ArcanaRegistry.CINDERS_CHARM.getNewItem();
      ItemStack wild = item(ArcanaRegistry.CINDERS_CHARM, ArcanaAugments.WILDFIRE, 5);
      helper.assertTrue(cinders().getMaxEnergy(wild) - cinders().getMaxEnergy(plain) == ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CINDERS_CHARM_WILDFIRE_CINDERS_PER_LVL).get(5), "Wildfire 5 added the wrong capacity");
      cinders().setEnergy(plain, 0);
      cinders().setEnergy(wild, 0);
      tickUntil(helper, player, plain, () -> {
         inventoryTick(helper, player, wild);
         helper.assertTrue(cinders().getEnergy(plain) > 0, "The charm has not regenerated yet");
         int bonus = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CINDERS_CHARM_WILDFIRE_REGENERATION_PER_LVL).get(5);
         helper.assertTrue(cinders().getEnergy(wild) - cinders().getEnergy(plain) == bonus, "Wildfire 5 regenerated " + cinders().getEnergy(wild) + " against " + cinders().getEnergy(plain));
      });
   }

   // ===== Charm of Cleansing =====

   private static CleansingCharm cleansing(){
      return (CleansingCharm) ArcanaRegistry.CLEANSING_CHARM;
   }

   private static ItemStack readyCleansing(ItemStack stack){
      cleansing().setEnergy(stack, 0);
      return stack;
   }

   @GameTest
   @Covers("septic_shock")
   public void cleansingTenAilmentsGrantsSepticShock(GameTestHelper helper){
      ServerPlayer player = player(helper);
      List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> harmful = List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.SLOWNESS, MobEffects.MINING_FATIGUE,
            MobEffects.NAUSEA, MobEffects.BLINDNESS, MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.UNLUCK);
      for(var effect : harmful) player.addEffect(new MobEffectInstance(effect, 200));
      cleansing().cleanseEffect(player, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      assertNotAchieved(helper, player, ArcanaAchievements.SEPTIC_SHOCK);

      player.removeAllEffects();
      for(var effect : harmful) player.addEffect(new MobEffectInstance(effect, 200));
      player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200));
      cleansing().cleanseEffect(player, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      assertAchieved(helper, player, ArcanaAchievements.SEPTIC_SHOCK);
      finish(helper, player);
   }

   @GameTest
   @Covers("food_poisont")
   public void cleansingHungerGrantsFoodPoisont(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      cleansing().cleanseEffect(player, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      assertNotAchieved(helper, player, ArcanaAchievements.FOOD_POISONT);
      player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200));
      cleansing().cleanseEffect(player, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      helper.assertFalse(player.hasEffect(MobEffects.HUNGER), "Hunger was not removed");
      assertAchieved(helper, player, ArcanaAchievements.FOOD_POISONT);
      finish(helper, player);
   }

   @GameTest
   @Covers("chronic_ailment")
   public void gettingACleansedEffectBackGrantsChronicAilment(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      assertNotAchieved(helper, player, ArcanaAchievements.CHRONIC_AILMENT);
      cleansing().cleanseEffect(player, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
      assertNotAchieved(helper, player, ArcanaAchievements.CHRONIC_AILMENT);
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      assertAchieved(helper, player, ArcanaAchievements.CHRONIC_AILMENT);
      finish(helper, player);
   }

   @GameTest
   @Covers("antidote")
   public void antidoteCleansesTwoEffects(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      for(var effect : List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.SLOWNESS)) plain.addEffect(new MobEffectInstance(effect, 200));
      cleansing().cleanseEffect(plain, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      helper.assertTrue(plain.getActiveEffects().size() == 2, "The plain charm left " + plain.getActiveEffects().size() + " of 3 effects");

      ServerPlayer player = player(helper);
      for(var effect : List.of(MobEffects.POISON, MobEffects.WITHER, MobEffects.SLOWNESS)) player.addEffect(new MobEffectInstance(effect, 200));
      cleansing().cleanseEffect(player, readyCleansing(item(ArcanaRegistry.CLEANSING_CHARM, ArcanaAugments.ANTIDOTE, 1)));
      helper.assertTrue(player.getActiveEffects().size() == 1, "Antidote left " + player.getActiveEffects().size() + " of 3 effects");
      finish(helper, plain, player);
   }

   @GameTest
   @Covers("infused_charcoal")
   public void infusedCharcoalShortensTheCooldown(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack charm = readyCleansing(item(ArcanaRegistry.CLEANSING_CHARM, ArcanaAugments.INFUSED_CHARCOAL, 4));
      int expected = ArcanaNovum.CONFIG.getInt(ArcanaConfig.CLEANSING_CHARM_COOLDOWN) - ArcanaNovum.CONFIG.getIntList(ArcanaConfig.CLEANSING_CHARM_CHARCOAL_COOLDOWN_PER_LVL).get(4);
      helper.assertTrue(expected < ArcanaNovum.CONFIG.getInt(ArcanaConfig.CLEANSING_CHARM_COOLDOWN), "Infused Charcoal 4 removes no cooldown");
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      cleansing().cleanseEffect(player, charm);
      helper.assertTrue(cleansing().getEnergy(charm) == expected, "The cooldown was set to " + cleansing().getEnergy(charm) + " instead of " + expected);
      finish(helper, player);
   }

   @GameTest
   @Covers("rejuvenation")
   public void rejuvenationHealsAfterCleansing(GameTestHelper helper){
      ServerPlayer plain = player(helper);
      plain.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      cleansing().cleanseEffect(plain, readyCleansing(ArcanaRegistry.CLEANSING_CHARM.getNewItem()));
      helper.assertTrue(Conditions.getConditionValue(plain, Conditions.REJUVENATION) == 0, "Cleansing without the augment gave Rejuvenation");

      ServerPlayer player = player(helper);
      player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
      cleansing().cleanseEffect(player, readyCleansing(item(ArcanaRegistry.CLEANSING_CHARM, ArcanaAugments.REJUVENATION, 1)));
      helper.assertTrue(Conditions.getConditionValue(player, Conditions.REJUVENATION) > 0, "Rejuvenation gave no healing");
      finish(helper, plain, player);
   }

   // ===== Charm of Feasting =====

   private static void feastTick(GameTestHelper helper, ServerPlayer player, ItemStack charm){
      withConfig(ArcanaConfig.FEASTING_CHARM_ACTIVE_COOLDOWN, 1, () -> withConfig(ArcanaConfig.FEASTING_CHARM_PASSIVE_COOLDOWN, 1000003, () -> inventoryTick(helper, player, charm)));
   }

   @GameTest
   @Covers("tarrare")
   public void eatingEverythingGrantsTarrare(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack charm = ArcanaRegistry.FEASTING_CHARM.getNewItem();
      List<ItemStack> foods = List.of(new ItemStack(Items.POISONOUS_POTATO), new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.SUSPICIOUS_STEW), new ItemStack(Items.CHICKEN), new ItemStack(Items.PUFFERFISH));
      for(int i = 0; i < foods.size(); i++){
         assertNotAchieved(helper, player, ArcanaAchievements.TARRARE);
         player.getInventory().setItem(0, foods.get(i).copy());
         player.getFoodData().setFoodLevel(0);
         feastTick(helper, player, charm);
         helper.assertTrue(player.getFoodData().getFoodLevel() > 0, "The charm did not eat " + foods.get(i).getItem());
      }
      assertAchieved(helper, player, ArcanaAchievements.TARRARE);
      finish(helper, player);
   }

   @GameTest
   @Covers("gluttony")
   public void gluttonyAddsFoodAndSaturation(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int nutrition = new ItemStack(Items.APPLE).get(DataComponents.FOOD).nutrition();
      helper.assertTrue(eatApple(helper, player, 0) == nutrition, "A plain charm should eat exactly the apple");
      int expected = nutrition + ArcanaNovum.CONFIG.getIntList(ArcanaConfig.FEASTING_CHARM_GLUTTONY_BONUS_FOOD_PER_LVL).get(3);
      int gained = eatApple(helper, player, 3);
      helper.assertTrue(gained == expected, "Gluttony 3 gave " + gained + " food instead of " + expected);
      finish(helper, player);
   }

   private static int eatApple(GameTestHelper helper, ServerPlayer player, int gluttony){
      player.getInventory().setItem(0, new ItemStack(Items.APPLE));
      player.getFoodData().setFoodLevel(0);
      feastTick(helper, player, item(ArcanaRegistry.FEASTING_CHARM, ArcanaAugments.GLUTTONY, gluttony));
      return player.getFoodData().getFoodLevel();
   }

   @GameTest
   @Covers("picky_eater")
   public void pickyEaterChoosesTheBestFood(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.getInventory().setItem(0, new ItemStack(Items.DRIED_KELP));
      player.getInventory().setItem(1, new ItemStack(Items.COOKED_BEEF));
      player.getFoodData().setFoodLevel(0);
      feastTick(helper, player, ArcanaRegistry.FEASTING_CHARM.getNewItem());
      helper.assertTrue(player.getInventory().getItem(0).isEmpty() && !player.getInventory().getItem(1).isEmpty(), "The plain charm should eat the first food it finds");

      player.getInventory().setItem(0, new ItemStack(Items.DRIED_KELP));
      player.getFoodData().setFoodLevel(0);
      feastTick(helper, player, item(ArcanaRegistry.FEASTING_CHARM, ArcanaAugments.PICKY_EATER, 1));
      helper.assertTrue(!player.getInventory().getItem(0).isEmpty() && player.getInventory().getItem(1).isEmpty(), "Picky Eater should eat the most nutritious food");
      finish(helper, player);
   }

   @GameTest
   @Covers("enzymes")
   public void enzymesShortenTheEatingInterval(GameTestHelper helper){
      ServerPlayer player = player(helper);
      int reduction = ArcanaNovum.CONFIG.getIntList(ArcanaConfig.FEASTING_CHARM_ENZYMES_COOLDOWN_PER_LVL).get(3);
      helper.assertTrue(reduction > 0, "Enzymes 3 takes nothing off the interval");
      // With this active cooldown level 3 eats every tick, so the only way not to eat at level 0 is the interval
      int cooldown = reduction + 1;
      player.getInventory().setItem(0, new ItemStack(Items.COOKED_BEEF, 64));
      ItemStack enzymes = item(ArcanaRegistry.FEASTING_CHARM, ArcanaAugments.ENZYMES, 3);
      ItemStack plain = ArcanaRegistry.FEASTING_CHARM.getNewItem();
      withConfig(ArcanaConfig.FEASTING_CHARM_PASSIVE_COOLDOWN, 1000003, () -> withConfig(ArcanaConfig.FEASTING_CHARM_ACTIVE_COOLDOWN, cooldown, () -> {
         player.getFoodData().setFoodLevel(0);
         inventoryTick(helper, player, enzymes);
         helper.assertTrue(player.getFoodData().getFoodLevel() > 0, "Enzymes 3 did not eat on this tick");
         if(helper.getLevel().getServer().getTickCount() % cooldown != 0){
            player.getFoodData().setFoodLevel(0);
            inventoryTick(helper, player, plain);
            helper.assertTrue(player.getFoodData().getFoodLevel() == 0, "The plain charm ate between its intervals");
         }
      }));
      finish(helper, player);
   }

   // ===== Charm of Felidae =====

   @GameTest
   @Covers("meow")
   public void usingTheFelidaeCharmGrantsMeow(GameTestHelper helper){
      ServerPlayer player = player(helper);
      use(helper, player, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      assertAchieved(helper, player, ArcanaAchievements.MEOW);
      finish(helper, player);
   }

   @GameTest(maxTicks = 30)
   @Covers("infiltration")
   public void beingAmongCreepersGrantsInfiltration(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 4; i++) mob(helper, EntityTypes.CREEPER, 2.5 + i, 2, 3);
      tickUntil(helper, player, ArcanaRegistry.FELIDAE_CHARM.getNewItem(), () -> assertAchieved(helper, player, ArcanaAchievements.INFILTRATION));
   }

   private static float fallDamage(GameTestHelper helper, ServerPlayer player, int grace){
      player.getInventory().setItem(0, item(ArcanaRegistry.FELIDAE_CHARM, ArcanaAugments.FELINE_GRACE, grace));
      player.setHealth(player.getMaxHealth());
      player.damageCooldownTime = 0;
      player.hurtServer(helper.getLevel(), player.damageSources().fall(), 16f);
      return player.getMaxHealth() - player.getHealth();
   }

   @GameTest
   @Covers("feline_grace")
   public void felineGraceReducesFallDamageUntilItIsGone(GameTestHelper helper){
      ServerPlayer player = player(helper);
      float base = fallDamage(helper, player, 0);
      float level3 = fallDamage(helper, player, 3);
      float level4 = fallDamage(helper, player, 4);
      float baseReduction = ArcanaNovum.CONFIG.getFloat(ArcanaConfig.FELIDAE_CHARM_REDUCTION);
      helper.assertTrue(base == 16 * (1 - baseReduction), "The plain charm took " + base + " from a fall of 16");
      helper.assertTrue(level3 < base, "Feline Grace 3 took " + level3 + ", no less than " + base);
      helper.assertTrue(level4 == 0, "Feline Grace 4 should negate falls, took " + level4);
      finish(helper, player);
   }

   @GameTest
   @Covers("land_on_feet")
   public void survivingALethalFallGrantsLandOnFeet(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.getInventory().setItem(0, ArcanaRegistry.FELIDAE_CHARM.getNewItem());
      player.setHealth(10f);
      player.hurtServer(helper.getLevel(), player.damageSources().fall(), 4f);
      assertNotAchieved(helper, player, ArcanaAchievements.LAND_ON_FEET);
      player.setHealth(10f);
      player.damageCooldownTime = 0;
      player.hurtServer(helper.getLevel(), player.damageSources().fall(), 16f);
      helper.assertTrue(player.isAlive(), "The charm did not save the player");
      assertAchieved(helper, player, ArcanaAchievements.LAND_ON_FEET);
      finish(helper, player);
   }

   // ===== Charm of Leadership =====

   @GameTest(maxTicks = 40)
   @Covers("raid_leader")
   public void leadingSixPlayersGrantsRaidLeader(GameTestHelper helper){
      ServerPlayer player = player(helper);
      List<ServerPlayer> raid = new ArrayList<>();
      for(int i = 0; i < 5; i++) raid.add(player(helper));
      inventoryTick(helper, player, ArcanaRegistry.LEADERSHIP_CHARM.getNewItem());
      assertAchieved(helper, player, ArcanaAchievements.RAID_LEADER);
      raid.add(player);
      finish(helper, raid.toArray(new ServerPlayer[0]));
   }

   @GameTest(padding = 32)
   @Covers("invigoration")
   public void invigorationWidensAndStrengthensTheAura(GameTestHelper helper){
      ServerPlayer leader = player(helper);
      ServerPlayer ally = player(helper);
      double base = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.LEADERSHIP_CHARM_RADIUS);
      double extra = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.LEADERSHIP_CHARM_INVIGORATION_RADIUS_PER_LVL).get(3);
      ally.teleportTo(leader.getX(), leader.getY() + base + extra / 2, leader.getZ());
      inventoryTick(helper, leader, ArcanaRegistry.LEADERSHIP_CHARM.getNewItem());
      helper.assertTrue(Conditions.getConditionValue(ally, Conditions.MIGHT) == 0, "The plain aura reached past its radius");
      float plainMight = Conditions.getConditionValue(leader, Conditions.MIGHT);

      inventoryTick(helper, leader, item(ArcanaRegistry.LEADERSHIP_CHARM, ArcanaAugments.INVIGORATION, 3));
      float might = Conditions.getConditionValue(ally, Conditions.MIGHT);
      helper.assertTrue(might > plainMight, "Invigoration 3 gave " + might + " Might at the wider radius, plain gives " + plainMight);
      finish(helper, leader, ally);
   }

   // ===== Charm of Light =====

   private static LightCharm light(){
      return (LightCharm) ArcanaRegistry.LIGHT_CHARM;
   }

   @GameTest
   @Covers("enlightened")
   public void placingLightsGrantsEnlightened(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.ENLIGHTENED, 1);
      ItemStack charm = ArcanaRegistry.LIGHT_CHARM.getNewItem();
      ArcanaItem.putProperty(charm, ArcanaItem.ACTIVE_TAG, true);
      ArcanaItem.putProperty(charm, LightCharm.THRESHOLD_TAG, 15);
      withConfig(ArcanaConfig.LIGHT_CHARM_PASSIVE_COOLDOWN, 1, () -> inventoryTick(helper, player, charm));
      helper.assertTrue(helper.getLevel().getBlockState(player.blockPosition()).is(Blocks.LIGHT), "The charm placed no light at the player");
      assertAchieved(helper, player, ArcanaAchievements.ENLIGHTENED);
      finish(helper, player);
   }

   private static Set<Integer> lightModes(GameTestHelper helper, ServerPlayer player, ItemStack charm){
      Set<Integer> modes = new HashSet<>();
      player.setShiftKeyDown(true);
      for(int i = 0; i < 8; i++){
         use(helper, player, charm);
         modes.add(ArcanaItem.getIntProperty(charm, ArcanaItem.MODE_TAG));
      }
      player.setShiftKeyDown(false);
      return modes;
   }

   @GameTest
   @Covers({"mood_lighting", "dimmer_switch", "radiance", "selective_placement"})
   public void lightAugmentsUnlockTheirModes(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Set<Integer> plain = lightModes(helper, player, ArcanaRegistry.LIGHT_CHARM.getNewItem());
      helper.assertTrue(plain.equals(Set.of(0, 1)), "A plain charm should only have the toggle and vision modes, has " + plain);
      helper.assertTrue(lightModes(helper, player, item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.MOOD_LIGHTING, 1)).contains(2), "Mood Lighting did not unlock threshold mode");
      helper.assertTrue(lightModes(helper, player, item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.DIMMER_SWITCH, 1)).contains(3), "Dimmer Switch did not unlock brightness mode");
      helper.assertTrue(lightModes(helper, player, item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.RADIANCE, 1)).contains(4), "Radiance did not unlock nova mode");
      helper.assertTrue(lightModes(helper, player, item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.SELECTIVE_PLACEMENT, 1)).contains(5), "Selective Placement did not unlock manual mode");

      ItemStack manual = item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.SELECTIVE_PLACEMENT, 1);
      ArcanaItem.putProperty(manual, ArcanaItem.MODE_TAG, 5);
      helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
      useOn(helper, player, manual, new BlockPos(3, 2, 3), Direction.UP);
      helper.assertBlockPresent(Blocks.LIGHT, new BlockPos(3, 3, 3));

      ItemStack nova = item(ArcanaRegistry.LIGHT_CHARM, ArcanaAugments.RADIANCE, 1);
      ArcanaItem.putProperty(nova, ArcanaItem.MODE_TAG, 4);
      withConfig(ArcanaConfig.LIGHT_CHARM_NOVA_RANGE, 2, () -> use(helper, player, nova));
      helper.assertTrue(ArcanaItem.getIntProperty(nova, LightCharm.NOVA_TAG) > 0, "Radiant Nova started no cooldown");
      finish(helper, player);
   }

   // ===== Charm of Magnetism =====

   private static MagnetismCharm magnet(){
      return (MagnetismCharm) ArcanaRegistry.MAGNETISM_CHARM;
   }

   private static ItemEntity drop(GameTestHelper helper, double x, double y, double z, ItemStack stack){
      Vec3 pos = helper.absoluteVec(new Vec3(x, y, z));
      ItemEntity item = new ItemEntity(helper.getLevel(), pos.x, pos.y, pos.z, stack, 0, 0, 0);
      item.setNoGravity(true);
      helper.getLevel().addFreshEntity(item);
      return item;
   }

   @GameTest
   @Covers("magnets")
   public void pulling25ItemsGrantsMagnets(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 25; i++) drop(helper, 1.5, 3.6, 2.5 + i * 0.2, new ItemStack(Items.STICK));
      magnet().activeUse(player, helper.getLevel(), ArcanaRegistry.MAGNETISM_CHARM.getNewItem());
      assertAchieved(helper, player, ArcanaAchievements.MAGNETS);
      finish(helper, player);
   }

   @GameTest
   @Covers("electromagnet")
   public void electromagnetExtendsTheActiveBeam(GameTestHelper helper){
      ServerPlayer player = player(helper);
      double base = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.MAGNETISM_CHARM_ACTIVE_RANGE);
      double extra = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.MAGNETISM_CHARM_ACTIVE_RANGE_PER_LVL).get(3);
      Vec3 eye = player.getEyePosition().subtract(helper.absoluteVec(Vec3.ZERO));
      double width = ArcanaNovum.CONFIG.getDouble(ArcanaConfig.MAGNETISM_CHARM_ACTIVE_WIDTH);
      ItemEntity item = drop(helper, eye.x, eye.y, eye.z + base + width + extra / 2, new ItemStack(Items.STICK));
      magnet().activeUse(player, helper.getLevel(), ArcanaRegistry.MAGNETISM_CHARM.getNewItem());
      helper.assertTrue(item.getDeltaMovement().length() == 0, "The plain beam pulled an item past its range");
      magnet().activeUse(player, helper.getLevel(), item(ArcanaRegistry.MAGNETISM_CHARM, ArcanaAugments.ELECTROMAGNET, 3));
      helper.assertTrue(item.getDeltaMovement().length() > 0, "Electromagnet 3 did not reach the item");
      finish(helper, player);
   }

   @GameTest(maxTicks = 30, padding = 32)
   @Covers("ferrite_core")
   public void ferriteCoreWidensThePassivePull(GameTestHelper helper){
      ServerPlayer player = player(helper);
      // A small base range keeps the item inside the test's walls, which would push it around
      double base = 2;
      double extra = ArcanaNovum.CONFIG.getDoubleList(ArcanaConfig.MAGNETISM_CHARM_PASSIVE_RANGE_PER_LVL).get(3);
      Vec3 eye = player.getEyePosition().subtract(helper.absoluteVec(Vec3.ZERO));
      ItemEntity item = drop(helper, eye.x, eye.y, eye.z + base + Math.min(extra / 2, 1), new ItemStack(Items.STICK));
      ItemStack plain = ArcanaRegistry.MAGNETISM_CHARM.getNewItem();
      ItemStack ferrite = item(ArcanaRegistry.MAGNETISM_CHARM, ArcanaAugments.FERRITE_CORE, 3);
      ArcanaItem.putProperty(plain, ArcanaItem.MODE_TAG, 1);
      ArcanaItem.putProperty(ferrite, ArcanaItem.MODE_TAG, 1);
      helper.succeedWhen(() -> withConfig(ArcanaConfig.MAGNETISM_CHARM_PASSIVE_RANGE, base, () -> {
         inventoryTick(helper, player, plain);
         helper.assertTrue(item.getDeltaMovement().length() == 0, "The plain pull reached past its range");
         inventoryTick(helper, player, ferrite);
         helper.assertTrue(item.getDeltaMovement().length() > 0, "Waiting for Ferrite Core to pull the item");
         helper.getLevel().getServer().getPlayerList().remove(player);
      }));
   }

   @GameTest(maxTicks = 30, padding = 32)
   @Covers("polarity_reversal")
   public void polarityReversalPushesItemsAway(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack plain = ArcanaRegistry.MAGNETISM_CHARM.getNewItem();
      magnet().toggleMode(player, plain);
      magnet().toggleMode(player, plain);
      helper.assertTrue(ArcanaItem.getIntProperty(plain, ArcanaItem.MODE_TAG) == 0, "A plain charm should only have two modes");

      ItemStack charm = item(ArcanaRegistry.MAGNETISM_CHARM, ArcanaAugments.POLARITY_REVERSAL, 1);
      magnet().toggleMode(player, charm);
      magnet().toggleMode(player, charm);
      helper.assertTrue(ArcanaItem.getIntProperty(charm, ArcanaItem.MODE_TAG) == 2, "Polarity Reversal did not unlock repel mode");
      Vec3 eye = player.getEyePosition().subtract(helper.absoluteVec(Vec3.ZERO));
      ItemEntity item = drop(helper, eye.x, eye.y, eye.z + 3, new ItemStack(Items.STICK));
      helper.succeedWhen(() -> {
         inventoryTick(helper, player, charm);
         helper.assertTrue(item.getDeltaMovement().z > 0, "Waiting for the charm to push the item away");
         helper.getLevel().getServer().getPlayerList().remove(player);
      });
   }

   @GameTest
   @Covers("faraday_cage")
   public void faradayCageFiltersWhatTheBeamPulls(GameTestHelper helper){
      ServerPlayer player = player(helper);
      ItemStack charm = item(ArcanaRegistry.MAGNETISM_CHARM, ArcanaAugments.FARADAY_CAGE, 1);
      player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STICK));
      player.setShiftKeyDown(true);
      use(helper, player, charm);
      player.setShiftKeyDown(false);
      helper.assertTrue(magnet().canAffectItem(charm, Items.STICK), "A whitelisted item should still be pulled");
      helper.assertFalse(magnet().canAffectItem(charm, Items.DIRT), "Whitelisting one item should stop the others");

      ItemStack plain = ArcanaRegistry.MAGNETISM_CHARM.getNewItem();
      int before = ArcanaItem.getIntProperty(plain, ArcanaItem.MODE_TAG);
      player.setShiftKeyDown(true);
      use(helper, player, plain);
      player.setShiftKeyDown(false);
      helper.assertTrue(ArcanaItem.getIntProperty(plain, ArcanaItem.MODE_TAG) != before, "Without Faraday Cage the same click should change the mode");
      helper.assertTrue(magnet().canAffectItem(plain, Items.DIRT), "A plain charm has no filter");
      finish(helper, player);
   }

   @GameTest
   @Covers("neodymium")
   public void neodymiumPullsMetalGearOffMobs(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Zombie zombie = mob(helper, EntityTypes.ZOMBIE, 1.5, 2, 4);
      zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
      zombie.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
      magnet().activeUse(player, helper.getLevel(), ArcanaRegistry.MAGNETISM_CHARM.getNewItem());
      helper.assertTrue(zombie.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET), "The plain charm took a mob's gear");
      magnet().activeUse(player, helper.getLevel(), item(ArcanaRegistry.MAGNETISM_CHARM, ArcanaAugments.NEODYMIUM, 1));
      helper.assertTrue(zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty(), "Neodymium left the iron helmet on");
      helper.assertTrue(zombie.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE), "Neodymium took leather armor");
      finish(helper, player);
   }

   // ===== Charm of Negotiation =====

   @GameTest
   @Covers("renegotiation")
   public void renegotiationRerollsAVillager(GameTestHelper helper){
      ServerPlayer player = player(helper);
      Villager villager = mob(helper, EntityTypes.VILLAGER, 3, 2, 3);
      villager.setVillagerData(villager.getVillagerData().withProfession(helper.getLevel().registryAccess(), VillagerProfession.FARMER).withLevel(2));
      villager.setOffers(new MerchantOffers());
      NegotiationCharm charm = (NegotiationCharm) ArcanaRegistry.NEGOTIATION_CHARM;
      ItemStack plain = charm.getNewItem();
      player.setItemInHand(InteractionHand.MAIN_HAND, plain);
      charm.useOnEntity(player, villager, InteractionHand.MAIN_HAND);
      helper.assertTrue(villager.getOffers().isEmpty(), "The plain charm rerolled trades");

      ItemStack stack = item(charm, ArcanaAugments.RENEGOTIATION, 1);
      player.setItemInHand(InteractionHand.MAIN_HAND, stack);
      charm.useOnEntity(player, villager, InteractionHand.MAIN_HAND);
      helper.assertTrue(villager.getOffers().size() >= 2, "Renegotiation gave a level 2 villager " + villager.getOffers().size() + " trades");
      finish(helper, player);
   }

   // ===== Charm of Wild Growth =====

   private static void passiveTick(GameTestHelper helper, ServerPlayer player, ItemStack charm, BlockPos relative){
      BlockPos pos = helper.absolutePos(relative);
      AABB range = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
      passiveTick(helper, player, charm, pos, range);
   }

   private static void passiveTick(GameTestHelper helper, ServerPlayer player, ItemStack charm, BlockPos pos, AABB range){
      try{
         Method passiveTick = WildGrowthCharm.class.getDeclaredMethod("passiveTick", ItemStack.class, ServerLevel.class, BlockPos.class, AABB.class, ServerPlayer.class, net.borisshoes.arcananovum.blocks.GeomanticSteleBlockEntity.class);
         passiveTick.setAccessible(true);
         passiveTick.invoke(ArcanaRegistry.WILD_GROWTH_CHARM, charm, helper.getLevel(), pos, range, player, null);
      }catch(ReflectiveOperationException e){
         throw new RuntimeException("WildGrowthCharm.passiveTick changed signature, update this test", e);
      }
   }

   @GameTest
   @Covers("they_grow_up_so_fast")
   public void growingFiveBabiesGrantsTheyGrowUpSoFast(GameTestHelper helper){
      ServerPlayer player = player(helper);
      for(int i = 0; i < 5; i++){
         Cow cow = mob(helper, EntityTypes.COW, 3 + i * 0.5, 2, 3);
         cow.setBaby(true);
      }
      BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 3));
      passiveTick(helper, player, ArcanaRegistry.WILD_GROWTH_CHARM.getNewItem(), pos, new AABB(pos).inflate(5));
      assertAchieved(helper, player, ArcanaAchievements.THEY_GROW_UP_SO_FAST);
      finish(helper, player);
   }

   @GameTest
   @Covers("bountiful_harvest")
   public void growingCropsGrantsBountifulHarvest(GameTestHelper helper){
      ServerPlayer player = player(helper);
      almostComplete(player, ArcanaAchievements.BOUNTIFUL_HARVEST, 1);
      BlockPos crop = new BlockPos(3, 3, 3);
      helper.setBlock(crop.below(), Blocks.FARMLAND);
      helper.setBlock(crop, Blocks.WHEAT);
      ItemStack charm = ArcanaRegistry.WILD_GROWTH_CHARM.getNewItem();
      for(int i = 0; i < 30 && !achieved(player, ArcanaAchievements.BOUNTIFUL_HARVEST); i++) passiveTick(helper, player, charm, crop);
      assertAchieved(helper, player, ArcanaAchievements.BOUNTIFUL_HARVEST);
      finish(helper, player);
   }

   @GameTest
   @Covers("reaping")
   public void reapingHarvestsAndThenReplants(GameTestHelper helper){
      ServerPlayer player = player(helper);
      BlockPos crop = new BlockPos(3, 3, 3);
      CropBlock wheat = (CropBlock) Blocks.WHEAT;

      helper.setBlock(crop.below(), Blocks.FARMLAND);
      helper.setBlock(crop, wheat.getStateForAge(wheat.getMaxAge()));
      ItemStack harvest = item(ArcanaRegistry.WILD_GROWTH_CHARM, ArcanaAugments.REAPING, 1);
      ArcanaItem.putProperty(harvest, WildGrowthCharm.HARVEST_TAG, true);
      passiveTick(helper, player, harvest, crop);
      helper.assertBlockNotPresent(Blocks.WHEAT, crop);

      helper.setBlock(crop, wheat.getStateForAge(wheat.getMaxAge()));
      ItemStack replant = item(ArcanaRegistry.WILD_GROWTH_CHARM, ArcanaAugments.REAPING, 2);
      ArcanaItem.putProperty(replant, WildGrowthCharm.HARVEST_TAG, true);
      passiveTick(helper, player, replant, crop);
      helper.assertTrue(helper.getBlockState(crop).is(Blocks.WHEAT) && !wheat.isMaxAge(helper.getBlockState(crop)), "Reaping 2 did not replant the crop");
      finish(helper, player);
   }

   @GameTest
   @Covers("charm_of_blooming")
   public void charmOfBloomingActsAsBoneMeal(GameTestHelper helper){
      ServerPlayer player = player(helper);
      BlockPos crop = new BlockPos(3, 3, 3);
      helper.setBlock(crop.below(), Blocks.FARMLAND);
      helper.setBlock(crop, Blocks.WHEAT);
      useOn(helper, player, ArcanaRegistry.WILD_GROWTH_CHARM.getNewItem(), crop, Direction.UP);
      helper.assertTrue(((CropBlock) Blocks.WHEAT).getAge(helper.getBlockState(crop)) == 0, "The plain charm acted as bone meal");
      useOn(helper, player, item(ArcanaRegistry.WILD_GROWTH_CHARM, ArcanaAugments.CHARM_OF_BLOOMING, 1), crop, Direction.UP);
      helper.assertTrue(((CropBlock) Blocks.WHEAT).getAge(helper.getBlockState(crop)) > 0, "Charm of Blooming did not grow the crop");
      finish(helper, player);
   }

   // ===== Charm of Cetacea =====

   private static ServerPlayer playerInWater(GameTestHelper helper){
      ServerPlayer player = player(helper);
      helper.setBlock(new BlockPos(1, 2, 1), Blocks.WATER);
      helper.setBlock(new BlockPos(1, 3, 1), Blocks.WATER);
      player.setDeltaMovement(Vec3.ZERO);
      player.doTick();
      player.doTick(); // The eye check reads what the previous tick saw
      return player;
   }

   @GameTest
   @Covers({"delphinidae", "gills"})
   public void cetaceaAugmentsImproveSwimming(GameTestHelper helper){
      ServerPlayer player = playerInWater(helper);
      helper.assertTrue(player.isInWater() && player.isUnderWater(), "The test player is not under water");
      inventoryTick(helper, player, ArcanaRegistry.CETACEA_CHARM.getNewItem());
      helper.assertTrue(player.getEffect(MobEffects.DOLPHINS_GRACE).getAmplifier() == 0, "The plain charm gave the wrong Dolphin's Grace");
      helper.assertFalse(player.hasEffect(MobEffects.WATER_BREATHING), "The plain charm gave Water Breathing");

      inventoryTick(helper, player, item(ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.DELPHINIDAE, 1));
      helper.assertTrue(player.getEffect(MobEffects.DOLPHINS_GRACE).getAmplifier() == 1, "Delphinidae did not give Dolphin's Grace II");
      inventoryTick(helper, player, item(ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.GILLS, 1));
      helper.assertTrue(player.hasEffect(MobEffects.WATER_BREATHING), "Gills did not give Water Breathing");
      finish(helper, player);
   }

   @GameTest
   @Covers("mariners_grace")
   public void marinersGraceRemovesTheMidairMiningPenalty(GameTestHelper helper){
      ServerPlayer player = player(helper);
      player.setOnGround(false);
      float plain = player.getDestroySpeed(Blocks.STONE.defaultBlockState());
      ItemStack charm = item(ArcanaRegistry.CETACEA_CHARM, ArcanaAugments.MARINERS_GRACE, 3);
      player.getInventory().setItem(0, charm);
      float graced = player.getDestroySpeed(Blocks.STONE.defaultBlockState());
      helper.assertTrue(graced > plain, "Mariner's Grace 3 mined at " + graced + ", no faster than " + plain);
      finish(helper, player);
   }

   @GameTest
   @Covers("ocean_migration")
   public void swimmingWithTheCharmGrantsOceanMigration(GameTestHelper helper){
      ServerPlayer player = playerInWater(helper);
      player.getInventory().setItem(0, ArcanaRegistry.CETACEA_CHARM.getNewItem());
      player.setSwimming(true);
      almostComplete(player, ArcanaAchievements.OCEAN_MIGRATION, 100);
      player.checkMovementStatistics(1.5, 0, 0);
      assertAchieved(helper, player, ArcanaAchievements.OCEAN_MIGRATION);
      finish(helper, player);
   }

   @GameTest
   @Covers("cephalos_in_a_pod")
   public void swimmingWithDolphinsGrantsCephalosInAPod(GameTestHelper helper){
      ServerPlayer player = playerInWater(helper);
      player.getInventory().setItem(0, ArcanaRegistry.CETACEA_CHARM.getNewItem());
      player.setSwimming(true);
      almostComplete(player, ArcanaAchievements.CEPHALOS_IN_A_POD, 100);
      player.checkMovementStatistics(1.5, 0, 0);
      assertNotAchieved(helper, player, ArcanaAchievements.CEPHALOS_IN_A_POD);
      mob(helper, EntityTypes.DOLPHIN, 3, 2, 3);
      player.checkMovementStatistics(1.5, 0, 0);
      assertAchieved(helper, player, ArcanaAchievements.CEPHALOS_IN_A_POD);
      finish(helper, player);
   }
}
