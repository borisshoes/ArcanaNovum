package net.borisshoes.arcananovum;

import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.items.ShieldOfFortitude;
import net.borisshoes.arcananovum.utils.ConfigUnits;
import net.borisshoes.borislib.config.ConfigSetting;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.config.values.*;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;


public class ArcanaConfig {
   
   public static final String CONFIG_NAME = "ArcanaNovum.properties";
   public static final HashMap<Identifier, ConfigUnits> CONFIG_UNITS = new HashMap<>();
   public static final Registry<IConfigSetting<?>> CONFIG_SETTINGS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID, "config_settings")), Lifecycle.stable());
   
   // Config Settings
   public static final IConfigSetting<?> RECIPE_FOLDER = registerConfigSetting(new ConfigSetting<>(
         new StringConfigValue("recipeFolder", "default", "default", "classic")));
   public static final IConfigSetting<?> DO_CONCENTRATION_DAMAGE = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("doConcentrationDamage", true)));
   public static final IConfigSetting<?> ANNOUNCE_ACHIEVEMENTS = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("announceAchievements", true)));
   public static final IConfigSetting<?> RESEARCH_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("researchEnabled", true)));
   public static final IConfigSetting<?> STARDUST_PARTICLE_RATE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("stardustParticleRate", 0.225, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> DISABLE_ARCANA_CRAFTING = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("disableArcanaCrafting", false)));
   public static final IConfigSetting<?> DISABLE_STARDUST_INFUSION = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("disableStardustInfusion", false)));
   public static final IConfigSetting<?> ALLOW_SIMILAR_BLOCK_CHECKS = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("allowSimilarBlockChecks", true)));
   public static final IConfigSetting<?> LOG_COMMAND_USAGE = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("logCommandUsage", false)));
   
   public static final IConfigSetting<?> CEPTYUS_EVENT_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("ceptyusEventEnabled", true)));
   public static final IConfigSetting<?> GAIALTUS_EVENT_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("gaialtusEventEnabled", true)));
   public static final IConfigSetting<?> ZERAIYA_EVENT_ENABLED = registerConfigSetting(new ConfigSetting<>(
         new BooleanConfigValue("zeraiyaEventEnabled", true)));
   // https://www.desmos.com/calculator/7xkrnk00zl
   public static final IConfigSetting<?> DRAGON_EGG_DIALOG_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("dragonEggDialogChance", 0.0000075, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> MEMENTO_DIALOG_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("mementoDialogChance", 0.0000075, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> AEQUALIS_DIALOG_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("aequalisDialogChance", 0.0000075, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> ZERAIYA_EVENT_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("zeraiyaEventChance", 0.000001, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> GAIALTUS_EVENT_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("gaialtusEventChance", 0.000014, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> CEPTYUS_EVENT_CHANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("ceptyusEventChance", 0.0004, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   
   public static final IConfigSetting<?> INFUSION_MAX_DURABILITY = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxDurability", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_MINING_SPEED = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxMiningSpeed", 1.25, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_ATTACK_SPEED = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxAttackSpeed", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_ATTACK_DAMAGE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxAttackDamage", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_ARMOR = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxArmor", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_ARMOR_TOUGHNESS = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxArmorToughness", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_KNOCKBACK_RESISTANCE = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxKnockbackResistance", 0.15, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> INFUSION_MAX_HEALTH_BOOST = registerConfigSetting(new ConfigSetting<>(
         new DoubleConfigValue("infusionMaxHealthBoost", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   
   // Runic Arcane Flak Arrows
   public static final IConfigSetting<?> FLAK_ARROW_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("flakArrowRange", 4.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> FLAK_ARROW_DAMAGE = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("flakArrowDamage", 3.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> FLAK_ARROW_DAMAGE_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("flakArrowDamageMultiplier", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> FLAK_ARROW_SENSE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("flakArrowSenseRange", 4.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("flakArrowAirburstRangeBuffPerLvl", List.of(0.0, 1.25, 2.5, 4.0), new DoubleConfigValue("flakArrowAirburstRangeBuffPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Runic Blink Arrows
   public static final IConfigSetting<?> BLINK_ARROW_PHASE_IN_DMG_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("blinkArrowPhaseInDmgMultiplier", 0.2, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> BLINK_ARROW_PHASE_IN_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("blinkArrowPhaseInDurationPerLvl", List.of(0, 20, 60, 100), new IntConfigValue("blinkArrowPhaseInDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Runic Concussion Arrows
   public static final IConfigSetting<?> CONCUSSION_ARROW_RANGE_MIN = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("concussionArrowRangeMin", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CONCUSSION_ARROW_RANGE_MAX = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("concussionArrowRangeMax", 6.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CONCUSSION_ARROW_DURATION_MOD = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("concussionArrowDurationMod", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CONCUSSION_ARROW_SHELLSHOCK_BOOST_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("concussionArrowShellshockBoostPerLvl", List.of(0.0, 0.5, 1.0, 2.0), new DoubleConfigValue("concussionArrowShellshockBoostPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Runic Detonation Arrows
   public static final IConfigSetting<?> DETONATION_ARROW_ENTITY_DMG_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("detonationArrowEntityDmgMultiplier", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> DETONATION_ARROW_BLOCK_DMG_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("detonationArrowBlockDmgMultiplier", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> DETONATION_ARROW_PLAYER_DMG_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("detonationArrowPlayerDmgMultiplier", 0.2, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> DETONATION_ARROW_BLAST_MINE_INCREASE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("detonationArrowBlastMineIncreasePerLvl", List.of(0.0, 1.5), new DoubleConfigValue("detonationArrowBlastMineIncreasePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> DETONATION_ARROW_ANTI_PERSONNEL_INCREASE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("detonationArrowAntiPersonnelIncreasePerLvl", List.of(0.0, 0.25, 0.5, 0.75), new DoubleConfigValue("detonationArrowAntiPersonnelIncreasePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Runic Ensnarement Arrows
   public static final IConfigSetting<?> ENSNAREMENT_ARROW_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("ensnarementArrowDuration", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ENSNAREMENT_ARROW_ENTRAPMENT_DURATION_INCREASE_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("ensnarementArrowEntrapmentDurationIncreasePerLvl", List.of(0, 20, 40, 60, 80, 100), new IntConfigValue("ensnarementArrowEntrapmentDurationIncreasePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ENSNAREMENT_ARROW_PLAYER_DURATION_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("ensnarementArrowPlayerDurationMultiplier", 0.2, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Runic Expulsion Arrows
   public static final IConfigSetting<?> EXPULSION_ARROW_DURATION_MIN = registerConfigSetting(ConfigUnits.QUARTER_SECONDS, new ConfigSetting<>(
         new IntConfigValue("expulsionArrowDurationMin", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> EXPULSION_ARROW_DURATION_MAX = registerConfigSetting(ConfigUnits.QUARTER_SECONDS, new ConfigSetting<>(
         new IntConfigValue("expulsionArrowDurationMax", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> EXPULSION_ARROW_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("expulsionArrowRange", 4.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> EXPULSION_ARROW_REPULSION_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("expulsionArrowRepulsionRangePerLvl", List.of(0.0, 1.5, 3.0, 4.5), new DoubleConfigValue("expulsionArrowRepulsionRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> EXPULSION_ARROW_EVICTION_RANGE_MIN = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("expulsionArrowEvictionRangeMin", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> EXPULSION_ARROW_EVICTION_RANGE_MAX = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("expulsionArrowEvictionRangeMax", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> EXPULSION_ARROW_EVICTION_POWER_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("expulsionArrowEvictionPowerPerLvl", List.of(0.0, 1.0), new DoubleConfigValue("expulsionArrowEvictionPowerPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Runic Graviton Arrows
   public static final IConfigSetting<?> GRAVITON_ARROW_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("gravitonArrowRange", 3.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_ARROW_DURATION_MIN = registerConfigSetting(ConfigUnits.QUARTER_SECONDS, new ConfigSetting<>(
         new IntConfigValue("gravitonArrowDurationMin", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> GRAVITON_ARROW_DURATION_MAX = registerConfigSetting(ConfigUnits.QUARTER_SECONDS, new ConfigSetting<>(
         new IntConfigValue("gravitonArrowDurationMax", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> GRAVITON_ARROW_WELL_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("gravitonArrowWellRangePerLvl", List.of(0.0, 1.0, 2.0, 3.0), new DoubleConfigValue("gravitonArrowWellRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Runic Photonic Arrows
   public static final IConfigSetting<?> PHOTONIC_ARROW_DMG_MIN = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowDmgMin", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_DMG_MAX = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowDmgMax", 20.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_DMG_FALLOFF_PER_BLOCK = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowRangeDmgPerBlock", 0.1, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_RANGE_MAX = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowRangeMax", 100.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_PLAYER_DMG_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowPlayerDamageMultiplier", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_PRISMATIC_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("photonicArrowPrismaticPerLvl", List.of(0.0, 1.0, 2.0, 3.0, 4.0, 5.0), new DoubleConfigValue("photonicArrowPrismaticPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_PRISMATIC_FLAT_DMG_INCREASE = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowPrismaticFlatDmgIncrease", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> PHOTONIC_ARROW_PRISMATIC_DMG_MAX = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("photonicArrowPrismaticDmgMax", 25.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Runic Siphoning Arrows
   public static final IConfigSetting<?> SIPHONING_ARROW_MIN_HEAL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("siphoningArrowHealMinHeal", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SIPHONING_ARROW_MAX_HEAL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("siphoningArrowHealMaxHeal", 8.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SIPHONING_ARROW_OVERHEAL_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("siphoningArrowOverhealPerLvl", List.of(0, 4, 10, 20), new IntConfigValue("siphoningArrowOverhealPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Runic Smoke Arrows
   public static final IConfigSetting<?> SMOKE_ARROW_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("smokeArrowDuration", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SMOKE_ARROW_RANGE_MIN = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("smokeArrowRangeMin", 0.3, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SMOKE_ARROW_RANGE_MAX = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("smokeArrowRangeMax", 2.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SMOKE_ARROW_GAS_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("smokeArrowGasDurationPerLvl", List.of(0, 60, 120, 180), new IntConfigValue("smokeArrowGasDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Runic Storm Arrows
   public static final IConfigSetting<?> STORM_ARROW_STRIKE_CHANCE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("stormArrowStrikeChance", List.of(0.1, 0.2, 0.4, 0.6, 0.8, 1.0), new DoubleConfigValue("stormArrowStrikeChance", 0.1, new DoubleConfigValue.DoubleLimits(0.0, 1.0)))));
   public static final IConfigSetting<?> STORM_ARROW_STRIKE_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("stormArrowStrikeDmg", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> STORM_ARROW_CHAIN_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("stormArrowChainDmg", 6.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> STORM_ARROW_CHAIN_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("stormArrowChainRange", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> STORM_ARROW_AFTERSHOCK_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("stormArrowAftershockRangePerLvl", List.of(2.5, 2.75, 3.0, 3.5, 4.0), new DoubleConfigValue("stormArrowAftershockRangePerLvl", 2.5, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> STORM_ARROW_AFTERSHOCK_DMG_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("stormArrowAftershockDmgPerLvl", List.of(2.0, 2.5, 3.0, 3.5, 4.0), new DoubleConfigValue("stormArrowAftershockDmgPerLvl", 2.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> STORM_ARROW_AFTERSHOCK_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("stormArrowAftershockDurationPerLvl", List.of(30, 50, 70, 90, 110), new IntConfigValue("stormArrowAftershockDurationPerLvl", 30, new IntConfigValue.IntLimits(0)))));
   
   // Runic Tracking Arrows
   public static final IConfigSetting<?> TRACKING_ARROW_BROADHEAD_DMG_AMP_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("trackingArrowBroadheadDmgAmpPerLvl", List.of(0.0, 0.5, 0.5, 0.5), new DoubleConfigValue("trackingArrowBroadheadDmgAmpPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> TRACKING_ARROW_BROADHEAD_DMG_AMP_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("trackingArrowBroadheadDmgAmpDurationPerLvl", List.of(0, 100, 200, 300), new IntConfigValue("trackingArrowBroadheadDmgAmpDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> TRACKING_ARROW_DETECTION_WIDTH_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("trackingArrowDetectionWidthPerLvl", List.of(5.0, 6.5, 8.0, 9.5), new DoubleConfigValue("trackingArrowDetectionWidthPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Charm of Cinders
   public static final IConfigSetting<?> CINDERS_CHARM_REGENERATION_RATE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("cindersCharmRegenerationRate", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> CINDERS_CHARM_FIREWEB_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmFirewebRangePerLvl", List.of(0.0, 4.0, 6.0, 8.0, 10.0), new DoubleConfigValue("cindersCharmFirewebRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_FIREWEB_DMG_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmFirewebDmgPerLvl", List.of(0.0, 0.07, 0.085, 0.095, 0.105), new DoubleConfigValue("cindersCharmFirewebDmgPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_FIREWEB_CREATURES_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmFirewebCreaturesPerLvl", List.of(0, 5, 10, 15, 20), new IntConfigValue("cindersCharmFirewebCreaturesPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_FLAME_CONE_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmFlameConeDmg", 2.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CINDERS_CHARM_FLAME_CONE_ANGLE = registerConfigSetting(ConfigUnits.DEGREES, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmFlameConeAngle", 30.0, new DoubleConfigValue.DoubleLimits(0.01, 179.99))));
   public static final IConfigSetting<?> CINDERS_CHARM_FLAME_CONE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmFlameConeRange", 7.0, new DoubleConfigValue.DoubleLimits(0.01))));
   public static final IConfigSetting<?> CINDERS_CHARM_PYROBLAST_TARGET_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmPyroblastTargetRange", 35.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CINDERS_CHARM_PYROBLAST_EXPLOSION_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmPyroblastExplosionRangePerLvl", List.of(0.0, 4.0, 5.0, 6.0, 7.0), new DoubleConfigValue("cindersCharmPyroblastExplosionRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_PYROBLAST_DMG_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmPyroblastDmgPerLvl", List.of(0.0, 0.1, 0.12, 0.14, 0.16), new DoubleConfigValue("cindersCharmPyroblastDmgPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_CREMATION_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmCremationMultiplier", 2.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CINDERS_CHARM_CREMATION_DAMAGE_PER_ENERGY = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("cinderCharmCremationDamagePerEnergy", 0.07, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> CINDERS_CHARM_WILDFIRE_REGENERATION_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmWildfireRegenerationPerLvl", List.of(0, 1, 2, 3, 5, 7), new IntConfigValue("cindersCharmWildfireRegenerationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_WILDFIRE_CINDERS_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("cindersCharmWildfireCindersPerLvl", List.of(0, 20, 40, 60, 80, 100), new IntConfigValue("cindersCharmWildfireCindersPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> CINDERS_CHARM_SUPERSMELTER_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("cindersCharmSupersmelterMultiplier", 4.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Charm of Cleansing
   public static final IConfigSetting<?> CLEANSING_CHARM_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("cleansingCharmCooldown", 30, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> CLEANSING_CHARM_CHARCOAL_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("cleansingCharmCharcoalCooldownPerLvl", List.of(0, 100, 200, 300, 400), new IntConfigValue("cleansingCharmCharcoalCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> CLEANSING_CHARM_REJUVENATION_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("cleansingCharmRejuvenationDuration", 200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> CLEANSING_CHARM_REJUVENATION_HEALTH_PER_TICK = registerConfigSetting(ConfigUnits.HP_PER_TICK, new ConfigSetting<>(
         new DoubleConfigValue("cleansingCharmRejuvenationHealthPerTick", 0.03, new DoubleConfigValue.DoubleLimits(0))));
   
   // Charm of Feasting
   public static final IConfigSetting<?> FEASTING_CHARM_ACTIVE_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("feastingCharmActiveCooldown", 400, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> FEASTING_CHARM_PASSIVE_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("feastingCharmPassiveCooldown", 1200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> FEASTING_CHARM_PASSIVE_HUNGER = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("feastingCharmPassiveHunger", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> FEASTING_CHARM_PASSIVE_SATURATION = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new DoubleConfigValue("feastingCharmPassiveSaturation", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> FEASTING_CHARM_ENZYMES_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("feastingCharmEnzymesCooldownPerLvl", List.of(0, 100, 200, 300), new IntConfigValue("feastingCharmEnzymesCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> FEASTING_CHARM_GLUTTONY_BONUS_FOOD_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("feastingCharmGluttonyBonusFoodPerLvl", List.of(0, 2, 4, 6), new IntConfigValue("feastingCharmGluttonyBonusFoodPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> FEASTING_CHARM_GLUTTONY_BONUS_SATURATION_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("feastingCharmGluttonyBonusSaturationPerLvl", List.of(0.0, 0.5, 1.0, 1.5), new DoubleConfigValue("feastingCharmGluttonyBonusSaturationPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Charm of Felidae
   public static final IConfigSetting<?> FELIDAE_CHARM_REDUCTION = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("felidaeCharmReduction", 0.5, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> FELIDAE_CHARM_GRACE_REDUCTION_PER_LEVEL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("felidaeCharmGraceReductionPerLvl", List.of(0.0, 0.125, 0.25, 0.375, 0.5), new DoubleConfigValue("felidaeCharmGraceReductionPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0, 1.0)))));
   public static final IConfigSetting<?> FELIDAE_CHARM_CREEPER_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("felidaeCharmCreeperRange", 8.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Charm of Leadership
   public static final IConfigSetting<?> LEADERSHIP_CHARM_RADIUS = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("leadershipCharmRadius", 8.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> LEADERSHIP_CHARM_INVIGORATION_RADIUS_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("leadershipCharmInvigorationRadiusPerLvl", List.of(0.0, 1.0, 2.0, 3.0), new DoubleConfigValue("leadershipCharmInvigorationRadiusPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> LEADERSHIP_CHARM_MIGHT_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("leadershipCharmMightPerLvl", List.of(0.25, 0.4, 0.55, 0.7), new DoubleConfigValue("leadershipCharmMightPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> LEADERSHIP_CHARM_FORTITUDE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("leadershipCharmFortitudePerLvl", List.of(0.25, 0.30, 0.35, 0.40), new DoubleConfigValue("leadershipCharmFortitudePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> LEADERSHIP_CHARM_REJUVENATION_PER_LVL = registerConfigSetting(ConfigUnits.HP_PER_TICK, new ConfigSetting<>(
         new ListConfigValue<>("leadershipCharmRejuvenationPerLvl", List.of(0.03, 0.04, 0.05, 0.06), new DoubleConfigValue("leadershipCharmRejuvenationPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Charm of Light
   public static final IConfigSetting<?> LIGHT_CHARM_PASSIVE_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("lightCharmPassiveCooldown", 60, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> LIGHT_CHARM_NOVA_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("lightCharmNovaCooldown", 30, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> LIGHT_CHARM_NOVA_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("lightCharmNovaRange", 32, new IntConfigValue.IntLimits(1, 128))));
   
   // Charm of Magnetism
   public static final IConfigSetting<?> MAGNETISM_CHARM_ACTIVE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("magnetismCharmActiveRange", 25.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> MAGNETISM_CHARM_ACTIVE_WIDTH = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("magnetismCharmActiveWidth", 3.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> MAGNETISM_CHARM_PASSIVE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("magnetismCharmPassiveRange", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> MAGNETISM_CHARM_ACTIVE_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("magnetismCharmActiveRangePerLvl", List.of(0.0, 3.0, 6.0, 9.0), new DoubleConfigValue("magnetismCharmActiveRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> MAGNETISM_CHARM_PASSIVE_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("magnetismCharmPassiveRangePerLvl", List.of(0.0, 1.0, 2.0, 3.0), new DoubleConfigValue("magnetismCharmPassiveRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Charm of Negotiation
   public static final IConfigSetting<?> NEGOTIATION_CHARM_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("negotiationCharmRange", 25.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> NEGOTIATION_CHARM_BARTER_BUFF_MULTIPLIER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("negotiationCharmBarterBuffMultiplier", 2.5, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Charm of Wild Growth
   public static final IConfigSetting<?> WILD_GROWTH_CHARM_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("wildGrowthCharmRange", 4.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> WILD_GROWTH_CHARM_BLOCKS_PER_TICK = registerConfigSetting(ConfigUnits.BLOCKS_PER_TICK, new ConfigSetting<>(
         new IntConfigValue("wildGrowthCharmBlocksPerTick", 3, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> WILD_GROWTH_CHARM_FERTILIZER_INTERVALS = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("wildGrowthCharmFertilizerIntervals", List.of(10, 7, 5, 3, 2), new IntConfigValue("wildGrowthCharmFertilizerIntervals", 1, new IntConfigValue.IntLimits(1)))));
   
   // Aequalis Scientia
   public static final IConfigSetting<?> AEQUALIS_SCIENTIA_BASE_USES = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("aequalisScientiaBaseUses", 3, new IntConfigValue.IntLimits(0))));
   
   // Ancient Dowsing Rod
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("ancientDowsingRodCooldown", 30, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("ancientDowsingRodCooldownPerLvl", List.of(0, 5, 10, 15), new IntConfigValue("ancientDowsingRodCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("ancientDowsingRodRange", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("ancientDowsingRodRangePerLvl", List.of(0, 5, 10, 15), new IntConfigValue("ancientDowsingRodRangePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_EFFECT_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("ancientDowsingRodEffectDuration", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ANCIENT_DOWSING_ROD_EFFECT_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("ancientDowsingRodEffectDurationPerLvl", List.of(0, 50, 100, 150), new IntConfigValue("ancientDowsingRodEffectDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Binary Blades
   public static final IConfigSetting<?> BINARY_BLADES_ENERGY_PER_HIT = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("binaryBladesEnergyPerHit", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BINARY_BLADES_MAX_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("binaryBladesMaxEnergy", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BINARY_BLADES_ENERGY_DECAY_RATE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("binaryBladesEnergyDecayRate", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BINARY_BLADES_ENERGY_GRACE_PERIOD = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("binaryBladesEnergyGracePeriod", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BINARY_BLADES_PULSAR_ENERGY_CONSUMPTION_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("binaryBladesPulsarEnergyConsumptionPerLvl", List.of(0, 50, 25), new IntConfigValue("binaryBladesPulsarEnergyConsumptionPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> BINARY_BLADES_RED_GIANT_DMG_PER_ENERGY = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("binaryBladesRedGiantDmgPerEnergy", 0.1, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> BINARY_BLADES_WHITE_DWARF_DMG_PER_ENERGY_BLOCK = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("binaryBladesWhiteDwarfDmgPerEnergyBlock", 0.2, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> BINARY_BLADES_PULSAR_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("binaryBladesPulsarRange", 25.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> BINARY_BLADES_PULSAR_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("binaryBladesPulsarDmg", 10.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Brain in a Jar
   public static final IConfigSetting<?> BRAIN_JAR_MAX_XP = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("brainJarMaxXp", 1000000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BRAIN_JAR_MAX_XP_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("brainJarMaxXpPerLvl", List.of(0, 1000000, 3000000, 5000000, 7000000, 9000000), new IntConfigValue("brainJarMaxXpPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> BRAIN_JAR_INTEREST_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("brainJarInterestPerLvl", List.of(0.0, .001, .0025, .005), new DoubleConfigValue("brainJarInterestPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> BRAIN_JAR_INTEREST_TICK = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("brainJarInterestTick", 1200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> BRAIN_JAR_REPAIR_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("brainJarRepairPerLvl", List.of(1.0, 1.5, 2.0, 1.5, 2.0, 2.5), new DoubleConfigValue("brainJarRepairPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0)))));
   
   // Chest Translocator
   public static final IConfigSetting<?> CHEST_TRANSLOCATOR_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("chestTranslocatorCooldown", 30, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> CHEST_TRANSLOCATOR_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("chestTranslocatorCooldownPerLvl", List.of(0, 8, 16, 24), new IntConfigValue("chestTranslocatorCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Containment Circlet
   public static final IConfigSetting<?> CONTAINMENT_CIRCLET_HEALING_RATE = registerConfigSetting(ConfigUnits.HP_PER_SECOND, new ConfigSetting<>(
         new DoubleConfigValue("containmentCircletHealingRate", 0.05, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Essence Egg
   public static final IConfigSetting<?> ESSENCE_EGG_SPAWNER_USES = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("essenceEggSpawnerUses", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ESSENCE_EGG_WILLING_CAPTIVE_DECREASE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("essenceEggWillingCaptiveDecrease", List.of(0, 1, 2, 3, 4, 5), new IntConfigValue("essenceEggWillingCaptiveDecrease", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ESSENCE_EGG_SOUL_SPLIT_CHANCE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("essenceEggSoulSplitChance", List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5), new DoubleConfigValue("essenceEggSoulSplitChance", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> ESSENCE_EGG_EFFICIENCY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("essenceEggEfficiencyPerLvl", List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5), new DoubleConfigValue("essenceEggEfficiencyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Everlasting Rocket
   public static final IConfigSetting<?> EVERLASTING_ROCKET_CHARGES = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("everlastingRocketCharges", 16, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> EVERLASTING_ROCKET_CHARGES_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("everlastingRocketChargesPerLvl", List.of(0, 3, 6, 9), new IntConfigValue("everlastingRocketChargesPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> EVERLASTING_ROCKET_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("everlastingRocketCooldown", 600, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> EVERLASTING_ROCKET_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("everlastingRocketCooldownPerLvl", List.of(0, 100, 200, 300), new IntConfigValue("everlastingRocketCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Exotic Matter
   public static final IConfigSetting<?> EXOTIC_MATTER_DURATION = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("exoticMatterDuration", 600000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> EXOTIC_MATTER_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("exoticMatterDurationPerLvl", List.of(0, 298800, 597600, 896400, 1195200, 2390400), new IntConfigValue("exoticMatterDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Graviton Maul
   public static final IConfigSetting<?> GRAVITON_MAUL_UP_SPEED = registerConfigSetting(ConfigUnits.BLOCKS_PER_TICK, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulUpSpeed", 0.75, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_DOWN_SPEED = registerConfigSetting(ConfigUnits.BLOCKS_PER_TICK, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulDownSpeed", -1.0, new DoubleConfigValue.DoubleLimits(Double.MIN_VALUE, 0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_CRUSH_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulCrushDmg", 2.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_VORTEX_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulVortexRange", 5.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_VORTEX_DMG_AMP = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulVortexDmgAmp", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_VORTEX_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulVortexDmg", 1.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_VORTEX_SUCK_POWER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulVortexSuckPower", 0.2, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_VORTEX_FORTITUDE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulVortexFortitude", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_AOE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulAoeRange", 3.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_SLAM_DMG_PER_SPEED = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulSlamDmgPerSpeed", 3.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> GRAVITON_MAUL_SLAM_RANGE_PER_SPEED = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("gravitonMaulSlamRangePerSpeed", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Levitation Harness
   public static final IConfigSetting<?> LEVITATION_HARNESS_GLOWSTONE_PER_HOUR = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("levitationHarnessGlowstonePerHour", 960, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> LEVITATION_HARNESS_SOUL_PER_HOUR = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("levitationHarnessSoulPerHour", 60, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> LEVITATION_HARNESS_DURABILITY_CHANCE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("levitationHarnessDurabilityChance", List.of(0.0, .15, .35, .5), new DoubleConfigValue("levitationHarnessDurabilityChance", 0.0, new DoubleConfigValue.DoubleLimits(0.0, 1.0)))));
   public static final IConfigSetting<?> LEVITATION_HARNESS_REBOOT_SPEED_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("levitationHarnessRebootSpeedPerLvl", List.of(15, 13, 11, 9, 7), new IntConfigValue("levitationHarnessRebootSpeedPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Levitation Harness / Shulker Core
   public static final IConfigSetting<?> HARNESS_CORE_RECYCLER_EFFICIENCY = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("harnessCoreRecyclerEfficiency", List.of(0.0, 0.1, 0.25, 0.5), new DoubleConfigValue("harnessCoreRecyclerEfficiency", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Magmatic Eversource
   public static final IConfigSetting<?> MAGMATIC_EVERSOURCE_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("magmaticEversourceCooldown", 30, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> MAGMATIC_EVERSOURCE_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("magmaticEversourceCooldownPerLvl", List.of(0, 8, 16, 24), new IntConfigValue("magmaticEversourceCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> MAGMATIC_EVERSOURCE_CHARGES_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("magmaticEversourceChargesPerLvl", List.of(1, 3, 5, 10, 25), new IntConfigValue("magmaticEversourceChargesPerLvl", 1, new IntConfigValue.IntLimits(1)))));
   
   // Nul Memento
   public static final IConfigSetting<?> NUL_MEMENTO_WARD_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("nulMementoWardCooldown", 36000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> NUL_MEMENTO_WARD_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("nulMementoWardCooldownPerLvl", List.of(0, 12000, 24000), new IntConfigValue("nulMementoWardCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Overflowing Quiver
   public static final IConfigSetting<?> OVERFLOWING_QUIVER_RESTOCK_TIME = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("overflowingQuiverRestockTime", 3000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> OVERFLOWING_QUIVER_RESTOCK_TIME_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("overflowingQuiverRestockTimePerLvl", List.of(0, 300, 600, 900, 1200, 1800), new IntConfigValue("overflowingQuiverRestockTimePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Runic Quiver
   public static final IConfigSetting<?> RUNIC_QUIVER_RESTOCK_TIME = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("runicQuiverRestockTime", 1200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> RUNIC_QUIVER_RESTOCK_TIME_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("runicQuiverRestockTimePerLvl", List.of(0, 100, 200, 400, 600, 900), new IntConfigValue("runicQuiverRestockTimePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Quivers (Shared)
   public static final IConfigSetting<?> QUIVER_EFFICIENCY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("quiverEfficiencyPerLvl", List.of(0.0, 0.05, 0.1, 0.15, 0.2, 0.3), new DoubleConfigValue("quiverEfficiencyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Pearl of Recall
   public static final IConfigSetting<?> PEARL_OF_RECALL_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("pearlOfRecallCooldown", 600, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PEARL_OF_RECALL_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("pearlOfRecallCooldownPerLvl", List.of(0, 60, 120, 240, 360, 480), new IntConfigValue("pearlOfRecallCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> PEARL_OF_RECALL_WARMUP = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("pearlOfRecallWarmup", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PEARL_OF_RECALL_CANCEL_PERCENT = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("pearlOfRecallCancelPercent", 0.75, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> PEARL_OF_RECALL_PHASE_DEFENSE_CHANCE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("pearlOfRecallPhaseDefenseChance", List.of(0.0, .15, .35, .5), new DoubleConfigValue("pearlOfRecallPhaseDefenseChance", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Ancient Pickaxe of Ceptyus
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_ENERGY_GAIN = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("pickaxeOfCeptyusEnergyGain", 3, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_ENERGY_GAIN_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("pickaxeOfCeptyusEnergyGainPerLvl", List.of(0, 1, 2, 3, 4, 5), new IntConfigValue("pickaxeOfCeptyusEnergyGainPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_MAX_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("pickaxeOfCeptyusMaxEnergy", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_MAX_ENERGY_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("pickaxeOfCeptyusMaxEnergyPerLvl", List.of(0, 100, 200, 300, 400, 500), new IntConfigValue("pickaxeOfCeptyusMaxEnergyPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_ENERGY_GRACE = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("pickaxeOfCeptyusEnergyGrace", 40, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_ENERGY_LOSS = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("pickaxeOfCeptyusEnergyLoss", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_ENERGY_PER_HASTE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new DoubleConfigValue("pickaxeOfCeptyusEnergyPerHaste", 100.0, new DoubleConfigValue.DoubleLimits(0))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_VEIN_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("pickaxeOfCeptyusVeinRangePerLvl", List.of(8, 10, 12, 14, 16, 18), new IntConfigValue("pickaxeOfCeptyusVeinRangePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_VEIN_BLOCKS_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("pickaxeOfCeptyusVeinBlocksPerLvl", List.of(64, 96, 128, 160, 192, 224), new IntConfigValue("pickaxeOfCeptyusVeinBlocksPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> PICKAXE_OF_CEPTYUS_FORTUNE_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("pickaxeOfCeptyusFortunePerLvl", List.of(5, 6, 8, 10), new IntConfigValue("pickaxeOfCeptyusFortunePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Planeshifter
   public static final IConfigSetting<?> PLANESHIFTER_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("planeshifterCooldown", 600, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> PLANESHIFTER_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("planeshifterCooldownPerLvl", List.of(0, 60, 120, 240, 360, 480), new IntConfigValue("planeshifterCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Shadow Stalker's Glaive
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_HIT_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaiveHitEnergy", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_BLINK_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaiveBlinkEnergy", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_BLINK_DISTANCE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("shadowStalkersGlaiveBlinkDistance", 10.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_KILL_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaiveKillEnergy", 85, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_STALK_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaiveStalkEnergy", 80, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_PASSIVE_ENERGY_CAP = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaivePassiveEnergyCap", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_PASSIVE_ENERGY_RATE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shadowStalkersGlaivePassiveEnergyRate", 20, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_INVIS_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("shadowStalkersGlaiveInvisDuration", List.of(0, 20, 40, 100), new IntConfigValue("shadowStalkersGlaiveInvisDuration", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_NEARSIGHT_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("shadowStalkersGlaiveNearsightDuration", List.of(0, 20, 40, 100), new IntConfigValue("shadowStalkersGlaiveNearsightDuration", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> SHADOW_STALKERS_GLAIVE_BLOODLETTER_DAMAGE = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("shadowStalkersGlaiveBloodletterDamage", 2.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Shield of Fortitude
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_DISPLAY_MODE = registerConfigSetting(new ConfigSetting<>(
         new EnumConfigValue<>("shieldOfFortitudeDisplayMode", ShieldOfFortitude.ShieldDisplayMode.HYBRID, ShieldOfFortitude.ShieldDisplayMode.class)));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_BLOCKED_ENERGY_CONVERSION_PERCENT = registerConfigSetting(ConfigUnits.PERCENT, new ConfigSetting<>(
         new DoubleConfigValue("shieldOfFortitudeBlockedEnergyConversionPercent", 0.5, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_HIT_MAX = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("shieldOfFortitudeHitMax", 10.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_HIT_MAX_PER_LVL = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new ListConfigValue<>("shieldOfFortitudeHitMaxPerLvl", List.of(0.0, 2.0, 4.0, 6.0, 8.0, 10.0), new DoubleConfigValue("shieldOfFortitudeHitMaxPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("shieldOfFortitudeDuration", 200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("shieldOfFortitudeDurationPerLvl", List.of(0, 100, 200, 300, 400, 500), new IntConfigValue("shieldOfFortitudeDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("shieldOfFortitudeShieldBashSlowness", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("shieldOfFortitudeShieldBashSlownessDuration", 60, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_PER_ABSORPTION = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("shieldOfFortitudeShieldBashVulnerabilityPerAbsorption", 0.02, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_DURATION_PER_ABSORPTION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new DoubleConfigValue("shieldOfFortitudeShieldBashVulnerabilityDurationPerAbsorption", 5.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Sojourner's Boots
   public static final IConfigSetting<?> SOJOURNER_BOOTS_ENERGY_MAX = registerConfigSetting(ConfigUnits.PERCENT, new ConfigSetting<>(
         new IntConfigValue("sojournerBootsEnergyMax", 250, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SOJOURNER_BOOTS_ENERGY_MAX_PER_LVL = registerConfigSetting(ConfigUnits.PERCENT, new ConfigSetting<>(
         new ListConfigValue<>("sojournerBootsEnergyMaxPerLvl", List.of(0, 50, 100, 150, 200, 250), new IntConfigValue("sojournerBootsEnergyMaxPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> SOJOURNER_BOOTS_RAMP = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("sojournerBootsRamp", 2, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> SOJOURNER_BOOTS_RAMP_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("sojournerBootsRampPerLvl", List.of(0, 2, 4), new IntConfigValue("sojournerBootsRampPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("sojournersBootsJuggernautVulnerability", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("sojournersBootsJuggernautVulnerabilityDuration", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("sojournersBootsJuggernautSlowness", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("sojournersBootsJuggernautSlownessDuration", 60, new IntConfigValue.IntLimits(0))));
   
   // Spawner Harness
   public static final IConfigSetting<?> SPAWNER_HARNESS_BREAK_PERCENT = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("spawnerHarnessBreakPercent", 0.15, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   
   // Spear of Tenbrous
   public static final IConfigSetting<?> SPEAR_OF_TENBROUS_THROW_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("spearOfTenbrousThrowDmg", 11.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SPEAR_OF_TENBROUS_STUN_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("spearOfTenbrousStunDuration", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> SPEAR_OF_TENBROUS_STORM_DMG = registerConfigSetting(ConfigUnits.HP, new ConfigSetting<>(
         new DoubleConfigValue("spearOfTenbrousStormDmg", 6.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SPEAR_OF_TENBROUS_STORM_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("spearOfTenbrousStormRange", 4.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> SPEAR_OF_TENBROUS_THROW_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("spearOfTenbrousThrowCooldownPerLvl", List.of(180, 140, 100, 60, 20), new IntConfigValue("spearOfTenbrousThrowCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Stasis Pearl
   public static final IConfigSetting<?> STASIS_PEARL_COOLDOWN = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("stasisPearlCooldown", 60, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> STASIS_PEARL_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("stasisPearlCooldownPerLvl", List.of(0, 10, 20, 30, 40, 50), new IntConfigValue("stasisPearlCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> STASIS_PEARL_REGEN_PER_LVL = registerConfigSetting(ConfigUnits.HP_PER_TICK, new ConfigSetting<>(
         new ListConfigValue<>("stasisPearlRegenPerLvl", List.of(0.0, 0.05, 0.1, 0.15), new DoubleConfigValue("stasisPearlRegenPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0)))));
   public static final IConfigSetting<?> STASIS_PEARL_FORTITUDE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("stasisPearlFortitudePerLvl", List.of(0.0, 0.1, 0.2, 0.3), new DoubleConfigValue("stasisPearlFortitudePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0)))));
   public static final IConfigSetting<?> STASIS_PEARL_RECONSTRUCT_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("stasisPearlReconstructDuration", 100, new IntConfigValue.IntLimits(0))));
   
   // Totem of Vengeance
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_DURATION = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new IntConfigValue("totemOfVengeanceDuration", 300, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_DURATION_PER_LVL = registerConfigSetting(ConfigUnits.SECONDS, new ConfigSetting<>(
         new ListConfigValue<>("totemOfVengeanceDurationPerLvl", List.of(0, 300, 600, 900), new IntConfigValue("totemOfVengeanceDurationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_DURATION_PERCENT_AGAINST_PLAYER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("totemOfVengeanceDurationPercentAgainstPlayer", 0.5, new DoubleConfigValue.DoubleLimits(0.0, 1.0))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_SPEED = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("totemOfVengeanceSpeed", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_SPEED_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("totemOfVengeanceSpeedPerLvl", List.of(0, 1, 2, 3), new IntConfigValue("totemOfVengeanceSpeedPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_STRENGTH = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("totemOfVengeanceStrength", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> TOTEM_OF_VENGEANCE_STRENGTH_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("totemOfVengeanceStrengthPerLvl", List.of(0, 1, 2, 3), new IntConfigValue("totemOfVengeanceStrengthPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Armored Wings of Enderia
   public static final IConfigSetting<?> WINGS_OF_ENDERIA_MAX_ENERGY = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("wingsOfEnderiaMaxEnergy", 10000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> WINGS_OF_ENDERIA_ENERGY_RATE = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("wingsOfEnderiaEnergyRate", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> WINGS_OF_ENDERIA_ENERGY_PER_DMG = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new DoubleConfigValue("wingsOfEnderiaEnergyPerDmg", 100.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> WINGS_OF_ENDERIA_BUFFET_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new DoubleConfigValue("wingsOfEnderiaBuffetRange", 4.0, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> WINGS_OF_ENDERIA_BUFFET_POWER = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("wingsOfEnderiaBuffetPower", 1.0, new DoubleConfigValue.DoubleLimits(0.0))));
   
   // Celestial Altar
   public static final IConfigSetting<?> CELESTIAL_ALTAR_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("celestialAltarCooldown", 36000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> CELESTIAL_ALTAR_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("celestialAltarCooldownPerLvl", List.of(0, 6000, 12000, 18000, 24000, 30000), new IntConfigValue("celestialAltarCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> CELESTIAL_ALTAR_ITEM = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new StringConfigValue("celestialAltarItem", "minecraft:nether_star")));
   
   // Starpath Altar
   public static final IConfigSetting<?> STARPATH_ALTAR_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("starpathAltarCooldown", 36000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> STARPATH_ALTAR_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("starpathAltarCooldownPerLvl", List.of(0, 6000, 12000, 18000, 24000, 30000), new IntConfigValue("starpathAltarCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> STARPATH_ALTAR_BLOCKS_PER_EYE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("starpathAltarBlocksPerEye", 64, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> STARPATH_ALTAR_ITEM = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new StringConfigValue("starpathAltarItem", "minecraft:ender_eye")));
   
   // Altar of the Stormcaller
   public static final IConfigSetting<?> STORMCALLER_ALTAR_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("stormcallerAltarCooldown", 36000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> STORMCALLER_ALTAR_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("stormcallerAltarCooldownPerLvl", List.of(0, 6000, 12000, 18000, 24000, 30000), new IntConfigValue("stormcallerAltarCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> STORMCALLER_ALTAR_ITEM = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new StringConfigValue("stormcallerAltarItem", "minecraft:diamond_block")));
   
   // Transmutation Altar
   public static final IConfigSetting<?> TRANSMUTATION_ALTAR_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("transmutationAltarCooldown", 13200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> TRANSMUTATION_ALTAR_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("transmutationAltarCooldownPerLvl", List.of(0, 2400, 4800, 7200, 9600, 12000), new IntConfigValue("transmutationAltarCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Astral Gateway
   public static final IConfigSetting<?> ASTRAL_GATEWAY_STARDUST = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("astralGatewayStardust", 64, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ASTRAL_GATEWAY_STARDUST_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("astralGatewayStardustPerLvl", List.of(1.0, 0.9, 0.8, 0.7, 0.5, 0.25, 0.0), new DoubleConfigValue("astralGatewayStardustPerLvl", 1.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Arcane Singularity
   public static final IConfigSetting<?> ARCANE_SINGULARITY_BOOKS = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("arcaneSingularityPages", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ARCANE_SINGULARITY_BOOKS_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("arcaneSingularityPagesPerLvl", List.of(0, 4, 8, 12, 16, 20), new IntConfigValue("arcaneSingularityPagesPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Midnight Enchanter
   public static final IConfigSetting<?> MIDNIGHT_ENCHANTER_ESSENCE_RATE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("midnightEnchanterEssenceRate", 0.30, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> MIDNIGHT_ENCHANTER_ESSENCE_RATE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("midnightEnchanterEssenceRatePerLvl", List.of(0.0, 0.15, 0.3, 0.45), new DoubleConfigValue("midnightEnchanterEssenceRatePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Stellar Core
   public static final IConfigSetting<?> STELLAR_CORE_STARDUST_RATE = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("stellarCoreStardustRate", 0.25, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> STELLAR_CORE_STARDUST_RATE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("stellarCoreStardustRatePerLvl", List.of(0.0, 0.25, 0.5, 0.75), new DoubleConfigValue("stellarCoreStardustRatePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> STELLAR_CORE_SALVAGE_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("stellarCoreSalvagePerLvl", List.of(0.25, 0.5, 0.75, 1.0), new DoubleConfigValue("stellarCoreSalvagePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0, 1.0)))));
   
   // Fractal Sponge
   public static final IConfigSetting<?> FRACTAL_SPONGE_BLOCKS = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("fractalSpongeBlocks", 512, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> FRACTAL_SPONGE_BLOCKS_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("fractalSpongeBlocksPerLvl", List.of(0, 256, 512, 768, 1024, 1280), new IntConfigValue("fractalSpongeBlocksPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> FRACTAL_SPONGE_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("fractalSpongeRange", 16, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> FRACTAL_SPONGE_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("fractalSpongeRangePerLvl", List.of(0, 2, 4, 6, 8, 10), new IntConfigValue("fractalSpongeRangePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> FRACTAL_SPONGE_PULSES = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new IntConfigValue("fractalSpongePulses", 3, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> FRACTAL_SPONGE_PULSE_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("fractalSpongePulseDuration", 50, new IntConfigValue.IntLimits(1))));
   
   // Igneous Collider
   public static final IConfigSetting<?> IGNEOUS_COLLIDER_COOLDOWN = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("igneousColliderCooldown", 300, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> IGNEOUS_COLLIDER_COOLDOWN_PER_LVL = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new ListConfigValue<>("igneousColliderCooldownPerLvl", List.of(0, 40, 80, 120, 160, 200), new IntConfigValue("igneousColliderCooldownPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> IGNEOUS_COLLIDER_EFFICIENCY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("igneousColliderEfficiencyPerLvl", List.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5), new DoubleConfigValue("igneousColliderEfficiencyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Interdictor
   public static final IConfigSetting<?> INTERDICTOR_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("interdictorRange", 32, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> INTERDICTOR_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("interdictorRangePerLvl", List.of(0, 16, 32, 64), new IntConfigValue("interdictorRangePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Itineranteur
   public static final IConfigSetting<?> ITINERANTEUR_RANGE = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("itineranteurRange", 16, new IntConfigValue.IntLimits(1))));
   public static final IConfigSetting<?> ITINERANTEUR_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("itineranteurRangePerLvl", List.of(0, 16, 32, 48, 64), new IntConfigValue("itineranteurRangePerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ITINERANTEUR_BLOCKS = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("itineranteurBlocks", 64, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ITINERANTEUR_BLOCKS_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("itineranteurBlocksPerLvl", List.of(0, 128, 256, 384, 512), new IntConfigValue("itineranteurBlocksPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ITINERANTEUR_SPEED = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new DoubleConfigValue("itineranteurSpeed", 0.5, new DoubleConfigValue.DoubleLimits(0.0))));
   public static final IConfigSetting<?> ITINERANTEUR_SPEED_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("itineranteurSpeedPerLvl", List.of(0.0, 0.5, 1.0, 2.0), new DoubleConfigValue("itineranteurSpeedPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Spawner Infuser
   public static final IConfigSetting<?> SPAWNER_INFUSER_ITEM_ID = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new StringConfigValue("spawnerInfuserItemId", "minecraft:nether_star")));
   public static final IConfigSetting<?> SPAWNER_INFUSER_EXTRA_CAPACITY_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("spawnerInfuserExtraCapacityPerLvl", List.of(0, 64, 128, 192, 256, 352), new IntConfigValue("spawnerInfuserExtraCapacityPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Arcane Tome / Generic
   public static final IConfigSetting<?> RESOLVE_CONCENTRATION_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("resolveConcentrationPerLvl", List.of(0, 10, 20, 30, 40, 50), new IntConfigValue("resolveConcentrationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> ADAPTABILITY_CONCENTRATION_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("adaptabilityConcentrationPerLvl", List.of(0, 1, 2, 3, 4, 5), new IntConfigValue("adaptabilityConcentrationPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Continuum Anchor
   public static final IConfigSetting<?> CONTINUUM_ANCHOR_EFFICIENCY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("continuumAnchorEfficiencyPerLvl", List.of(0.0, .05, .1, .15, .2, .5), new DoubleConfigValue("continuumAnchorEfficiencyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Soulstone
   public static final IConfigSetting<?> SOULSTONE_SOULS_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("soulstoneSoulsPerLvl", List.of(1, 2, 3, 4, 5, 10), new IntConfigValue("soulstoneSoulsPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Alchemical Arbalest
   public static final IConfigSetting<?> ALCHEMICAL_ARBALEST_VULNERABILITY_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("alchemicalArbalestVulnerabilityDuration", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> ALCHEMICAL_ARBALEST_VULNERABILITY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("alchemicalArbalestVulnerabilityPerLvl", List.of(0.5, 0.75, 1.0, 1.5), new DoubleConfigValue("alchemicalArbalestVulnerabilityPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> ALCHEMICAL_ARBALEST_FIELD_RANGE_PER_LVL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new ListConfigValue<>("alchemicalArbalestFieldRangePerLvl", List.of(2.0, 3.0, 4.0, 5.0, 6.0, 7.0), new DoubleConfigValue("alchemicalArbalestFieldRangePerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> ALCHEMICAL_ARBALEST_FIELD_DURATION = registerConfigSetting(ConfigUnits.TICKS, new ConfigSetting<>(
         new IntConfigValue("alchemicalArbalestFieldDuration", 200, new IntConfigValue.IntLimits(0))));
   
   // Starlight Forge
   public static final IConfigSetting<?> STARLIGHT_FORGE_RESOURCEFUL_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("starlightForgeResourcefulPerLvl", List.of(0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35), new DoubleConfigValue("starlightForgeResourcefulPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   public static final IConfigSetting<?> STARLIGHT_FORGE_SKILLED_POINTS_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("starlightForgeSkilledPointsPerLvl", List.of(0, 1, 2, 3, 4, 5, 6, 8, 10, 12, 15), new IntConfigValue("starlightForgeSkilledPointsPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   public static final IConfigSetting<?> STARLIGHT_FORGE_STELLAR_RANGE_VERTICAL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("starlightForgeStellarRangeVertical", 3, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> STARLIGHT_FORGE_STELLAR_RANGE_HORIZONTAL = registerConfigSetting(ConfigUnits.BLOCKS, new ConfigSetting<>(
         new IntConfigValue("starlightForgeStellarRangeHorizontal", 7, new IntConfigValue.IntLimits(0))));
   
   // Twilight Anvil
   public static final IConfigSetting<?> TWILIGHT_ANVIL_INFUSION_BUFF_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("twilightAnvilInfusionBuffPerLvl", List.of(0.0, 0.025, 0.05, 0.075), new DoubleConfigValue("twilightAnvilInfusionBuffPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Radiant Fletchery
   public static final IConfigSetting<?> RADIANT_FLETCHERY_ARROWS_PER_LVL = registerConfigSetting(ConfigUnits.NONE, new ConfigSetting<>(
         new ListConfigValue<>("radiantFletcheryArrowsPerLvl", List.of(24, 32, 40, 48, 56, 64), new IntConfigValue("radiantFletcheryArrowsPerLvl", 0, new IntConfigValue.IntLimits(0)))));
   
   // Runic Bow
   public static final IConfigSetting<?> RUNIC_BOW_ACCURACY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("runicBowAccuracyPerLvl", List.of(1.0, .75, .5, 0.0), new DoubleConfigValue("runicBowAccuracyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Charm of Cetacea
   public static final IConfigSetting<?> CETACEA_CHARM_SWIM_PENALTY_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("cetaceaCharmSwimPenaltyPerLvl", List.of(0.0, 0.33, 0.67, 1.0), new DoubleConfigValue("cetaceaCharmSwimPenaltyPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   // Geomantic Stele
   public static final IConfigSetting<?> GEOMANTIC_STELE_RANGE_MULTIPLIER_PER_LVL = registerConfigSetting(ConfigUnits.MULTIPLIER, new ConfigSetting<>(
         new ListConfigValue<>("geomanticSteleRangeMultiplierPerLvl", List.of(1.0, 1.5, 2.0, 3.0), new DoubleConfigValue("geomanticSteleRangeMultiplierPerLvl", 0.0, new DoubleConfigValue.DoubleLimits(0.0)))));
   
   
   public static final IConfigSetting<?> XP_STORMCALLER_ALTAR_ACTIVATE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStormcallerAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CELESTIAL_ALTAR_ACTIVATE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCelestialAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STARPATH_ALTAR_ACTIVATE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStarpathAltarActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_IGNEOUS_COLLIDER_PRODUCE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpIgneousColliderProduce", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CONTINUUM_ANCHOR_PER_MINUTE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpContinuumAnchorPerMinute", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FRACTAL_SPONGE_ABSORB_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFractalSpongeAbsorbBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_FLY = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaFly", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_RADIANT_FLETCHERY_TIP_ARROWS = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpRadiantFletcheryTipArrows", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STELLAR_CORE_SALVAGE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStellarCoreSalvage", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STELLAR_CORE_SMELT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStellarCoreSmelt", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PICKAXE_OF_CEPTYUS_MINE_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpPickaxeOfCeptyusMineBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PICKAXE_OF_CEPTYUS_VEIN_MINE_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpPickaxeOfCeptyusVeinMineBlock", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_RUNIC_ARROW_SHOOT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpRunicArrowShoot", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHIELD_OF_FORTITUDE_ABSORB_DAMAGE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpShieldOfFortitudeAbsorbDamage", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TOTEM_OF_VENGEANCE_ACTIVATE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTotemOfVengeanceActivate", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TOTEM_OF_VENGEANCE_SURVIVE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTotemOfVengeanceSurvive", 4000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_DAMAGE_AMP_PER_10 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpDamageAmpPer10", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_DAMAGE_AMP_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpDamageAmpCap", 200, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ALCHEMICAL_ARBALEST_SHOOT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAlchemicalArbalestShoot", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TRANSMUTATION_ALTAR_TRANSMUTE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTransmutationAltarTransmute", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TRANSMUTATION_ALTAR_TRANSMUTE_PER_ITEM = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTransmutationAltarTransmutePerItem", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MIDNIGHT_ENCHANTER_DISENCHANT_PER_ESSENCE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpMidnightEnchanterDisenchantPerEssence", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STARDUST_INFUSION_PER_STARDUST = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStardustInfusionPerStardust", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TWILIGHT_ANVIL_PER_10 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTwilightAnvilPer10", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TWILIGHT_ANVIL_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTwilightAnvilCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ANCIENT_DOWSING_ROD_PER_DEBRIS = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAncientDowsingRodPerDebris", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ANCIENT_DOWSING_ROD_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAncientDowsingRodCap", 1500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AQUATIC_EVERSOURCE_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAquaticEversourceUse", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGMATIC_EVERSOURCE_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpMagmaticEversourceUse", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_BRAIN_JAR_MEND_PER_XP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpBrainJarMendPerXp", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CHEST_TRANSLOCATOR_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpChestTranslocatorUse", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CONTAINMENT_CIRCLET_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpContainmentCircletUse", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ESSENCE_EGG_SPAWN = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpEssenceEggSpawn", 2500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ESSENCE_EGG_CONVERT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpEssenceEggConvert", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_EVERLASTING_ROCKET_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpEverlastingRocketUse", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LEVITATION_HARNESS_PER_SECOND = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpLevitationHarnessPerSecond", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NUL_MEMENTO_DEALLOCATE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpNulMementoDeallocate", 50000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NUL_MEMENTO_PROTECT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpNulMementoProtect", 5000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PEARL_OF_RECALL_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpPearlOfRecallUse", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_PLANESHIFTER_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpPlaneshifterUse", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_STASIS_PEARL_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpStasisPearlUse", 250, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_QUIVER_REFILL = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpQuiverRefill", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHADOW_STALKERS_GLAIVE_STALK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpShadowStalkersGlaiveStalk", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHADOW_STALKERS_GLAIVE_BLINK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpShadowStalkersGlaiveBlink", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SHULKER_CORE_PER_SOUL = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpShulkerCorePerSoul", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SOJOURNERS_BOOTS_RUN_PER_SECOND = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpSojournersBootsRunPerSecond", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SOULSTONE_LEVEL_UP_PER_SOUL = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpSoulstoneLevelUpPerSoul", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SPAWNER_HARNESS_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpSpawnerHarnessUse", 20000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_TELESCOPING_BEACON_PER_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpTelescopingBeaconPerBlock", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteBlock", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_TNT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteTnt", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_ENTITY = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteEntity", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_IGNITE_CREEPER = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmIgniteCreeper", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_LIGHT_BLOCK = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmLightBlock", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_SMELT_PER_CINDER = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmSmeltPerCinder", 4, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_CONE_PER_TARGET = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmConePerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_PYROBLAST_PER_TARGET = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmPyroblastPerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CINDERS_CHARM_WEB_PER_TARGET = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCindersCharmWebPerTarget", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FEASTING_CHARM_PER_FOOD_VALUE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFeastingCharmPerFoodValue", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_NOVA_PER_LIGHT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpLightCharmNovaPerLight", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_AUTOMATIC = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpLightCharmAutomatic", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_LIGHT_CHARM_MANUAL = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpLightCharmManual", 15, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGNETISM_CHARM_PER_ITEM = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpMagnetismCharmPerItem", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_MAGNETISM_CHARM_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpMagnetismCharmCap", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PER_MATURE_CROP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPerMatureCrop", 25, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PER_REAPED_CROP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPerReapedCrop", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WILD_GROWTH_CHARM_PASSIVE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWildGrowthCharmPassive", 1, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_FALL = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmFall", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_FALL_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmFallCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_SCARE_PHANTOM = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmScarePhantom", 2, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_FELIDAE_CHARM_SCARE_CREEPER = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpFelidaeCharmScareCreeper", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_CUSHION = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaCushion", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_WINGS_OF_ENDERIA_CUSHION_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpWingsOfEnderiaCushionCap", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_CATALYST_TRANSMUTE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteCatalyst", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_SKILL_TRANSMUTE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteSkill", 1000, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_AEQUALIS_SCIENTIA_ATTUNED_TRANSMUTE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAequalisScientiaTransmuteAttuned", 500, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_BINARY_BLADES_MAX_ENERGY_PER_SECOND = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpBinaryBladesMaxEnergyPerSecond", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CETACEA_CHARM_PER_SECOND = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCetaceaCharmPerSecond", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CLEANSING_CHARM_CLEANSE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpCleansingCharmCleanse", 150, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GRAVITON_MAUL_IMPACT_DAMAGE_PER_10 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpGravitonMaulImpactDamagePer10", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GRAVITON_MAUL_IMPACT_DAMAGE_CAP = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpGravitonMaulImpactDamageCap", 250, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_GREAVES_OF_GAIALTUS_REFILL_BLOCK_PER_10 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpGreavesOfGaialtusRefillBlockPer10", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_SPEAR_OF_TENBROUS_IMPALE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpSpearOfTenbrousImpale", 50, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ITINERANTEUR_BLOCK_TRAVELLED_PER_10 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpItineranteurBlockTravelledPer10", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_INTERDICTOR_MOB_BLOCKED_PER_100 = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpInterdictorMobBlockedPer100", 10, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NEGOTIATION_CHARM_BARTER = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpNegotiationCharmBarter", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_NEGOTIATION_CHARM_INFLUENCE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpNegotiationCharmInfluence", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ASTRAL_GATEWAY_STARDUST_CONSUMED = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAstralGatewayStardustConsumed", 5, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_ASTRAL_GATEWAY_TELEPORT = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpAstralGatewayTeleport", 100, new IntConfigValue.IntLimits(0))));
   public static final IConfigSetting<?> XP_CLOCKWORK_MULTITOOL_USE = registerConfigSetting(ConfigUnits.ARCANA_XP, new ConfigSetting<>(
         new IntConfigValue("xpClockworkMultitoolUse", 10, new IntConfigValue.IntLimits(0))));
   
   public static IConfigSetting<?> registerConfigSetting(IConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID, setting.getId()), setting);
      return setting;
   }
   
   public static IConfigSetting<?> registerConfigSetting(ConfigUnits units, IConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID, setting.getId()), setting);
      ArcanaConfig.CONFIG_UNITS.put(Identifier.fromNamespaceAndPath(MOD_ID, setting.getId()), units);
      return setting;
   }
}
