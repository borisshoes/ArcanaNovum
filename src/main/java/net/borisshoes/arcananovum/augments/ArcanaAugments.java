package net.borisshoes.arcananovum.augments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.arcananovum.ArcanaConfig;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.ArcanaRarity;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.ConfigUnits;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.*;

import static net.borisshoes.arcananovum.core.ArcanaRarity.*;

@SuppressWarnings("unchecked")
public class ArcanaAugments {
   public static final HashMap<String, ArcanaAugment> registry = new HashMap<>();
   public static final HashMap<ArcanaAugment, String> linkedAugments = new HashMap<>();
   public static final List<List<ArcanaAugment>> exclusiveAugments = new ArrayList<>();
   
   // Arcane Flak Arrows
   public static final ArcanaAugment AIRBURST = ArcanaAugments.register(
         new ArcanaAugment("airburst", new ItemStack(Items.FIREWORK_STAR), ArcanaRegistry.ARCANE_FLAK_ARROWS,
               MUNDANE, EMPOWERED, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FLAK_ARROW_AIRBURST_RANGE_BUFF_PER_LVL, ConfigUnits.BLOCKS)
         ));
   
   // Blink Arrows
   public static final ArcanaAugment PHASE_IN = ArcanaAugments.register(
         new ArcanaAugment("phase_in", new ItemStack(Items.ENDER_PEARL), ArcanaRegistry.BLINK_ARROWS,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BLINK_ARROW_PHASE_IN_DMG_MULTIPLIER, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.BLINK_ARROW_PHASE_IN_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Concussion Arrows
   public static final ArcanaAugment SHELLSHOCK = ArcanaAugments.register(
         new ArcanaAugment("shellshock", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.CONCUSSION_ARROWS,
               MUNDANE, EMPOWERED, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CONCUSSION_ARROW_SHELLSHOCK_BOOST_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Detonation Arrows
   public static final ArcanaAugment ANTI_PERSONNEL = ArcanaAugments.register(
         new ArcanaAugment("anti_personnel", new ItemStack(Items.ROTTEN_FLESH), ArcanaRegistry.DETONATION_ARROWS,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.DETONATION_ARROW_ANTI_PERSONNEL_INCREASE_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment BLAST_MINE = ArcanaAugments.register(
         new ArcanaAugment("blast_mine", new ItemStack(Items.COBBLESTONE), ArcanaRegistry.DETONATION_ARROWS,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.DETONATION_ARROW_BLAST_MINE_INCREASE_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Expulsion Arrows
   public static final ArcanaAugment REPULSION = ArcanaAugments.register(
         new ArcanaAugment("repulsion", new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS), ArcanaRegistry.EXPULSION_ARROWS,
               MUNDANE, EMPOWERED, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.EXPULSION_ARROW_REPULSION_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment EVICTION_BURST = ArcanaAugments.register(
         new ArcanaAugment("eviction_burst", new ItemStack(Items.WIND_CHARGE), ArcanaRegistry.EXPULSION_ARROWS,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MIN, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.EXPULSION_ARROW_EVICTION_RANGE_MAX, ConfigUnits.BLOCKS)
         ));
   
   // Graviton Arrows
   public static final ArcanaAugment GRAVITY_WELL = ArcanaAugments.register(
         new ArcanaAugment("gravity_well", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.GRAVITON_ARROWS,
               MUNDANE, EMPOWERED, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.GRAVITON_ARROW_WELL_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   
   // Photonic Arrows
   public static final ArcanaAugment PRISMATIC_ALIGNMENT = ArcanaAugments.register(
         new ArcanaAugment("prismatic_alignment", new ItemStack(Items.BEACON), ArcanaRegistry.PHOTONIC_ARROWS,
               EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PHOTONIC_ARROW_PRISMATIC_PER_LVL, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.PHOTONIC_ARROW_PRISMATIC_DMG_MAX, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.PHOTONIC_ARROW_PRISMATIC_FLAT_DMG_INCREASE, ConfigUnits.HEARTS)
         ));
   
   // Siphoning Arrows
   public static final ArcanaAugment OVERHEAL = ArcanaAugments.register(
         new ArcanaAugment("overheal", PotionContents.createItemStack(Items.POTION, Potions.HEALING), ArcanaRegistry.SIPHONING_ARROWS,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SIPHONING_ARROW_OVERHEAL_PER_LVL, ConfigUnits.HEARTS)
         ));
   
   // Smoke Arrows
   public static final ArcanaAugment TEAR_GAS = ArcanaAugments.register(
         new ArcanaAugment("tear_gas", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), ArcanaRegistry.SMOKE_ARROWS,
               MUNDANE, EMPOWERED, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SMOKE_ARROW_GAS_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Storm Arrows
   public static final ArcanaAugment STORM_STABILIZATION = ArcanaAugments.register(
         new ArcanaAugment("storm_stabilization", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.STORM_ARROWS,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STORM_ARROW_STRIKE_CHANCE, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment CHAIN_LIGHTNING = ArcanaAugments.register(
         new ArcanaAugment("chain_lightning", new ItemStack(Items.PRISMARINE_CRYSTALS), ArcanaRegistry.STORM_ARROWS,
               EXOTIC, EMPOWERED, EMPOWERED, EXOTIC, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STORM_ARROW_CHAIN_DMG, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.STORM_ARROW_CHAIN_RANGE, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment AFTERSHOCK = ArcanaAugments.register(
         new ArcanaAugment("aftershock", new ItemStack(Items.LIGHTNING_ROD), ArcanaRegistry.STORM_ARROWS,
               EXOTIC, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STORM_ARROW_AFTERSHOCK_RANGE_PER_LVL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.STORM_ARROW_AFTERSHOCK_DMG_PER_LVL, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.STORM_ARROW_AFTERSHOCK_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Tether Arrows
   public static final ArcanaAugment QUICK_RELEASE = ArcanaAugments.register(
         new ArcanaAugment("quick_release", new ItemStack(Items.SHEARS), ArcanaRegistry.TETHER_ARROWS,
               EMPOWERED
         ));
   
   // Charm of Cinders
   public static final ArcanaAugment PYROBLAST = ArcanaAugments.register(
         new ArcanaAugment("pyroblast", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.CINDERS_CHARM,
               DIVINE, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_PYROBLAST_TARGET_RANGE, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_PYROBLAST_EXPLOSION_RANGE_PER_LVL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_PYROBLAST_DMG_PER_LVL, ConfigUnits.HEARTS)
         ));
   public static final ArcanaAugment WEB_OF_FIRE = ArcanaAugments.register(
         new ArcanaAugment("web_of_fire", new ItemStack(Items.FIRE_CORAL), ArcanaRegistry.CINDERS_CHARM,
               DIVINE, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_FIREWEB_RANGE_PER_LVL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_FIREWEB_DMG_PER_LVL, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_FIREWEB_CREATURES_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment CREMATION = ArcanaAugments.register(
         new ArcanaAugment("cremation", new ItemStack(Items.SOUL_CAMPFIRE), ArcanaRegistry.CINDERS_CHARM,
               DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_CREMATION_MULTIPLIER, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_CREMATION_DAMAGE_PER_ENERGY, ConfigUnits.HEARTS)
         ));
   public static final ArcanaAugment FIRESTARTER = ArcanaAugments.register(
         new ArcanaAugment("firestarter", new ItemStack(Items.FLINT_AND_STEEL), ArcanaRegistry.CINDERS_CHARM,
               MUNDANE
         ));
   public static final ArcanaAugment WILDFIRE = ArcanaAugments.register(
         new ArcanaAugment("wildfire", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.CINDERS_CHARM,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_WILDFIRE_REGENERATION_PER_LVL, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_WILDFIRE_CINDERS_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment SUPERSMELTER = ArcanaAugments.register(
         new ArcanaAugment("supersmelter", new ItemStack(Items.BLAST_FURNACE), ArcanaRegistry.CINDERS_CHARM,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CINDERS_CHARM_SUPERSMELTER_MULTIPLIER, ConfigUnits.MULTIPLIER)
         ));
   
   // Charm of Feasting
   public static final ArcanaAugment PICKY_EATER = ArcanaAugments.register(
         new ArcanaAugment("picky_eater", new ItemStack(Items.COOKED_PORKCHOP), ArcanaRegistry.FEASTING_CHARM,
               EMPOWERED
         ));
   public static final ArcanaAugment ENZYMES = ArcanaAugments.register(
         new ArcanaAugment("enzymes", new ItemStack(Items.FROGSPAWN), ArcanaRegistry.FEASTING_CHARM,
               EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FEASTING_CHARM_ENZYMES_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment GLUTTONY = ArcanaAugments.register(
         new ArcanaAugment("gluttony", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.FEASTING_CHARM,
               EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FEASTING_CHARM_GLUTTONY_BONUS_FOOD_PER_LVL, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.FEASTING_CHARM_GLUTTONY_BONUS_SATURATION_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Charm of Felidae
   public static final ArcanaAugment FELINE_GRACE = ArcanaAugments.register(
         new ArcanaAugment("feline_grace", new ItemStack(Items.FEATHER), ArcanaRegistry.FELIDAE_CHARM,
               EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FELIDAE_CHARM_GRACE_REDUCTION_PER_LEVEL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment PANTHERA = ArcanaAugments.register(
         new ArcanaAugment("panthera", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.FELIDAE_CHARM,
               SOVEREIGN
         ));
   
   // Charm of Leadership
   public static final ArcanaAugment INVIGORATION = ArcanaAugments.register(
         new ArcanaAugment("invigoration", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.LEADERSHIP_CHARM,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.LEADERSHIP_CHARM_INVIGORATION_RADIUS_PER_LVL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.LEADERSHIP_CHARM_MIGHT_PER_LVL, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.LEADERSHIP_CHARM_FORTITUDE_PER_LVL, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.LEADERSHIP_CHARM_REJUVENATION_PER_LVL, ConfigUnits.HEARTS_PER_SECOND)
         ));
   
   // Charm of Light
   public static final ArcanaAugment MOOD_LIGHTING = ArcanaAugments.register(
         new ArcanaAugment("mood_lighting", new ItemStack(Items.LANTERN), ArcanaRegistry.LIGHT_CHARM,
               EMPOWERED
         ));
   public static final ArcanaAugment SELECTIVE_PLACEMENT = ArcanaAugments.register(
         new ArcanaAugment("selective_placement", new ItemStack(Items.TORCH), ArcanaRegistry.LIGHT_CHARM,
               EXOTIC
         ));
   public static final ArcanaAugment DIMMER_SWITCH = ArcanaAugments.register(
         new ArcanaAugment("dimmer_switch", new ItemStack(Items.REDSTONE_TORCH), ArcanaRegistry.LIGHT_CHARM,
               EMPOWERED
         ));
   public static final ArcanaAugment RADIANCE = ArcanaAugments.register(
         new ArcanaAugment("radiance", new ItemStack(Items.GLOWSTONE), ArcanaRegistry.LIGHT_CHARM,
               SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.LIGHT_CHARM_NOVA_COOLDOWN, ConfigUnits.SECONDS),
               new Tuple<>(ArcanaConfig.LIGHT_CHARM_NOVA_RANGE, ConfigUnits.BLOCKS)
         ));
   
   // Charm of Magnetism
   public static final ArcanaAugment ELECTROMAGNET = ArcanaAugments.register(
         new ArcanaAugment("electromagnet", new ItemStack(Items.IRON_BLOCK), ArcanaRegistry.MAGNETISM_CHARM,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.MAGNETISM_CHARM_ACTIVE_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment FERRITE_CORE = ArcanaAugments.register(
         new ArcanaAugment("ferrite_core", new ItemStack(Items.RAW_IRON), ArcanaRegistry.MAGNETISM_CHARM,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.MAGNETISM_CHARM_PASSIVE_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment FARADAY_CAGE = ArcanaAugments.register(
         new ArcanaAugment("faraday_cage", new ItemStack(Items.HOPPER), ArcanaRegistry.MAGNETISM_CHARM,
               SOVEREIGN
         ));
   public static final ArcanaAugment POLARITY_REVERSAL = ArcanaAugments.register(
         new ArcanaAugment("polarity_reversal", new ItemStack(Items.GOLD_INGOT), ArcanaRegistry.MAGNETISM_CHARM,
               EXOTIC
         ));
   public static final ArcanaAugment NEODYMIUM = ArcanaAugments.register(
         new ArcanaAugment("neodymium", new ItemStack(Items.NETHERITE_INGOT), ArcanaRegistry.MAGNETISM_CHARM,
               DIVINE
         ));
   
   // Ancient Dowsing Rod
   public static final ArcanaAugment ENHANCED_RESONANCE = ArcanaAugments.register(
         new ArcanaAugment("enhanced_resonance", new ItemStack(Items.BELL), ArcanaRegistry.ANCIENT_DOWSING_ROD,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ANCIENT_DOWSING_ROD_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment HARMONIC_FEEDBACK = ArcanaAugments.register(
         new ArcanaAugment("harmonic_feedback", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.ANCIENT_DOWSING_ROD,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ANCIENT_DOWSING_ROD_EFFECT_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment SONIC_REABSORPTION = ArcanaAugments.register(
         new ArcanaAugment("sonic_reabsorption", new ItemStack(Items.GOAT_HORN), ArcanaRegistry.ANCIENT_DOWSING_ROD,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ANCIENT_DOWSING_ROD_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Arcane Tome / Generic
   public static final ArcanaAugment RESOLVE = ArcanaAugments.register(
         new ArcanaAugment("resolve", new ItemStack(Items.DIAMOND), ArcanaRegistry.ARCANE_TOME,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.RESOLVE_CONCENTRATION_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment FOCUS = ArcanaAugments.register(
         new ArcanaAugment("focus", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.ARCANE_TOME,
               SOVEREIGN, SOVEREIGN, DIVINE
         ));
   public static final ArcanaAugment ADAPTABILITY = ArcanaAugments.register(
         new ArcanaAugment("adaptability", new ItemStack(Items.AMETHYST_SHARD), ArcanaRegistry.ARCANE_TOME,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ADAPTABILITY_CONCENTRATION_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Brain in a Jar
   public static final ArcanaAugment KNOWLEDGE_BANK = ArcanaAugments.register(
         new ArcanaAugment("knowledge_bank", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.BRAIN_JAR,
               EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BRAIN_JAR_INTEREST_PER_LVL, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.BRAIN_JAR_INTEREST_TICK, ConfigUnits.MINUTES)
         ));
   public static final ArcanaAugment TRADE_SCHOOL = ArcanaAugments.register(
         new ArcanaAugment("trade_school", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.BRAIN_JAR,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BRAIN_JAR_REPAIR_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment UNENDING_WISDOM = ArcanaAugments.register(
         new ArcanaAugment("unending_wisdom", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.BRAIN_JAR,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BRAIN_JAR_MAX_XP_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Continuum Anchor
   public static final ArcanaAugment TEMPORAL_RELATIVITY = ArcanaAugments.register(
         new ArcanaAugment("temporal_relativity", new ItemStack(Items.CLOCK), ArcanaRegistry.CONTINUUM_ANCHOR,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CONTINUUM_ANCHOR_EFFICIENCY_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Essence Egg
   public static final ArcanaAugment SOUL_SPLIT = ArcanaAugments.register(
         new ArcanaAugment("soul_split", new ItemStack(Items.EGG), ArcanaRegistry.ESSENCE_EGG,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ESSENCE_EGG_SOUL_SPLIT_CHANCE, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment DETERMINED_SPIRIT = ArcanaAugments.register(
         new ArcanaAugment("determined_spirit", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.ESSENCE_EGG,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ESSENCE_EGG_EFFICIENCY_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment WILLING_CAPTIVE = ArcanaAugments.register(
         new ArcanaAugment("willing_captive", new ItemStack(Items.SPAWNER), ArcanaRegistry.ESSENCE_EGG,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ESSENCE_EGG_WILLING_CAPTIVE_DECREASE, ConfigUnits.NONE)
         ));
   
   // Exotic Matter
   public static final ArcanaAugment TIME_IN_A_BOTTLE = ArcanaAugments.register(
         new ArcanaAugment("time_in_a_bottle", new ItemStack(Items.CLOCK), ArcanaRegistry.EXOTIC_MATTER,
               MUNDANE, MUNDANE, MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.EXOTIC_MATTER_DURATION_PER_LVL, ConfigUnits.HOURS)
         ));
   
   // Fractal Sponge
   public static final ArcanaAugment SIERPINSKI = ArcanaAugments.register(
         new ArcanaAugment("sierpinski", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
               MUNDANE, MUNDANE, EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FRACTAL_SPONGE_BLOCKS_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment MANDELBROT = ArcanaAugments.register(
         new ArcanaAugment("mandelbrot", new ItemStack(Items.GOLD_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
               MUNDANE, MUNDANE, EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FRACTAL_SPONGE_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment CANTOR = ArcanaAugments.register(
         new ArcanaAugment("cantor", new ItemStack(Items.EMERALD_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.FRACTAL_SPONGE_PULSES, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.FRACTAL_SPONGE_PULSE_DURATION, ConfigUnits.SECONDS)
         ));
   
   // Igneous Collider
   public static final ArcanaAugment CRYOGENIC_COOLING = ArcanaAugments.register(
         new ArcanaAugment("cryogenic_cooling", new ItemStack(Items.BLUE_ICE), ArcanaRegistry.IGNEOUS_COLLIDER,
               EXOTIC
         ));
   public static final ArcanaAugment THERMAL_EXPANSION = ArcanaAugments.register(
         new ArcanaAugment("thermal_expansion", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.IGNEOUS_COLLIDER_EFFICIENCY_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment MAGMATIC_INJECTION = ArcanaAugments.register(
         new ArcanaAugment("magmatic_injection", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.IGNEOUS_COLLIDER,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.IGNEOUS_COLLIDER_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Levitation Harness
   public static final ArcanaAugment STURDY_CONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("sturdy_construction", new ItemStack(Items.IRON_CHESTPLATE), ArcanaRegistry.LEVITATION_HARNESS,
               SOVEREIGN, SOVEREIGN, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.LEVITATION_HARNESS_DURABILITY_CHANCE, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment EMERGENCY_PROTOCOL = ArcanaAugments.register(
         new ArcanaAugment("emergency_protocol", new ItemStack(Items.FEATHER), ArcanaRegistry.LEVITATION_HARNESS,
               EXOTIC
         ));
   public static final ArcanaAugment FAST_REBOOT = ArcanaAugments.register(
         new ArcanaAugment("fast_reboot", new ItemStack(Items.EMERALD), ArcanaRegistry.LEVITATION_HARNESS,
               EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.LEVITATION_HARNESS_REBOOT_SPEED_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment HARNESS_RECYCLER = ArcanaAugments.register(
         new ArcanaAugment("harness_recycler", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.LEVITATION_HARNESS,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.HARNESS_CORE_RECYCLER_EFFICIENCY, ConfigUnits.PERCENT)
         ));
   
   // Nul Memento
   public static final ArcanaAugment DEATHS_CHAMPION = ArcanaAugments.register(
         new ArcanaAugment("deaths_champion", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.NUL_MEMENTO,
               SOVEREIGN
         ));
   public static final ArcanaAugment TEMPO_MORTUUS = ArcanaAugments.register(
         new ArcanaAugment("tempo_mortuus", new ItemStack(Items.CLOCK), ArcanaRegistry.NUL_MEMENTO,
               SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.NUL_MEMENTO_WARD_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   
   // Overflowing Quiver
   public static final ArcanaAugment ABUNDANT_AMMO = ArcanaAugments.register(
         new ArcanaAugment("abundant_ammo", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.OVERFLOWING_QUIVER,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.OVERFLOWING_QUIVER_RESTOCK_TIME_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment OVERFLOWING_BOTTOMLESS = ArcanaAugments.register(
         new ArcanaAugment("overflowing_bottomless", new ItemStack(Items.ARROW), ArcanaRegistry.OVERFLOWING_QUIVER,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.QUIVER_EFFICIENCY_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Pearl of Recall
   public static final ArcanaAugment RECALL_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("recall_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.PEARL_OF_RECALL,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PEARL_OF_RECALL_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   public static final ArcanaAugment PHASE_DEFENSE = ArcanaAugments.register(
         new ArcanaAugment("phase_defense", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.PEARL_OF_RECALL,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PEARL_OF_RECALL_PHASE_DEFENSE_CHANCE, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment CHRONO_TEAR = ArcanaAugments.register(
         new ArcanaAugment("chrono_tear", new ItemStack(Items.END_PORTAL_FRAME), ArcanaRegistry.PEARL_OF_RECALL,
               EXOTIC
         ));
   
   // Pickaxe of Ceptyus
   public static final ArcanaAugment WITH_THE_DEPTHS = ArcanaAugments.register(
         new ArcanaAugment("with_the_depths", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
               MUNDANE, MUNDANE, EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PICKAXE_OF_CEPTYUS_VEIN_BLOCKS_PER_LVL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.PICKAXE_OF_CEPTYUS_VEIN_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment GREED = ArcanaAugments.register(
         new ArcanaAugment("greed", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
               EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PICKAXE_OF_CEPTYUS_FORTUNE_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment WARDENS_HASTE = ArcanaAugments.register(
         new ArcanaAugment("wardens_haste", new ItemStack(Items.GOLDEN_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PICKAXE_OF_CEPTYUS_ENERGY_GAIN_PER_LVL, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.PICKAXE_OF_CEPTYUS_MAX_ENERGY_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Runic Bow
   public static final ArcanaAugment BOW_STABILIZATION = ArcanaAugments.register(
         new ArcanaAugment("bow_stabilization", new ItemStack(Items.TARGET), ArcanaRegistry.RUNIC_BOW,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.RUNIC_BOW_ACCURACY_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment BOW_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("bow_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.RUNIC_BOW,
               EXOTIC, EXOTIC, SOVEREIGN, SOVEREIGN, DIVINE
         ));
   public static final ArcanaAugment ENHANCED_INFINITY = ArcanaAugments.register(
         new ArcanaAugment("enhanced_infinity", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.RUNIC_BOW,
               EXOTIC
         ));
   
   // Runic Quiver
   public static final ArcanaAugment QUIVER_DUPLICATION = ArcanaAugments.register(
         new ArcanaAugment("quiver_duplication", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RUNIC_QUIVER,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.RUNIC_QUIVER_RESTOCK_TIME_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment RUNIC_BOTTOMLESS = ArcanaAugments.register(
         new ArcanaAugment("runic_bottomless", new ItemStack(Items.ARROW), ArcanaRegistry.RUNIC_QUIVER,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.QUIVER_EFFICIENCY_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment SHUNT_RUNES = ArcanaAugments.register(
         new ArcanaAugment("shunt_runes", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.RUNIC_QUIVER,
               DIVINE
         ));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAugment SHADOW_STRIDE = ArcanaAugments.register(
         new ArcanaAugment("shadow_stride", new ItemStack(Items.GLASS), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
               SOVEREIGN, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHADOW_STALKERS_GLAIVE_INVIS_DURATION, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment PARANOIA = ArcanaAugments.register(
         new ArcanaAugment("paranoia", new ItemStack(Items.TINTED_GLASS), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
               SOVEREIGN, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHADOW_STALKERS_GLAIVE_NEARSIGHT_DURATION, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment BLOODLETTER = ArcanaAugments.register(
         new ArcanaAugment("bloodletter", new ItemStack(Items.REDSTONE), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
               SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHADOW_STALKERS_GLAIVE_BLOODLETTER_DAMAGE, ConfigUnits.HEARTS)
         ));
   
   // Shield of Fortitude
   public static final ArcanaAugment SHIELD_OF_FAITH = ArcanaAugments.register(
         new ArcanaAugment("shield_of_faith", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_HIT_MAX_PER_LVL, ConfigUnits.HEARTS)
         ));
   public static final ArcanaAugment SHIELD_OF_RESILIENCE = ArcanaAugments.register(
         new ArcanaAugment("shield_of_resilience", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment SHIELD_BASH = ArcanaAugments.register(
         new ArcanaAugment("shield_bash", new ItemStack(Items.IRON_AXE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
               DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_SLOWNESS_DURATION, ConfigUnits.SECONDS),
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_PER_ABSORPTION, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.SHIELD_OF_FORTITUDE_SHIELD_BASH_VULNERABILITY_DURATION_PER_ABSORPTION, ConfigUnits.SECONDS)
         ));
   
   // Shulker Core
   public static final ArcanaAugment LEVITATIVE_REABSORPTION = ArcanaAugments.register(
         new ArcanaAugment("levitative_reabsorption", new ItemStack(Items.FEATHER), ArcanaRegistry.SHULKER_CORE,
               EMPOWERED
         ));
   public static final ArcanaAugment SHULKER_RECYCLER = ArcanaAugments.register(
         new ArcanaAugment("shulker_recycler", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.SHULKER_CORE,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.HARNESS_CORE_RECYCLER_EFFICIENCY, ConfigUnits.PERCENT)
         ));
   
   // Sojourner's Boots
   public static final ArcanaAugment HIKING_BOOTS = ArcanaAugments.register(
         new ArcanaAugment("hiking_boots", new ItemStack(Items.GRAVEL), ArcanaRegistry.SOJOURNER_BOOTS,
               SOVEREIGN
         ));
   public static final ArcanaAugment MARATHON_RUNNER = ArcanaAugments.register(
         new ArcanaAugment("marathon_runner", new ItemStack(Items.FEATHER), ArcanaRegistry.SOJOURNER_BOOTS,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SOJOURNER_BOOTS_ENERGY_MAX_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment SPRINTER = ArcanaAugments.register(
         new ArcanaAugment("sprinter", new ItemStack(Items.GOLDEN_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS,
               EXOTIC, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SOJOURNER_BOOTS_RAMP_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment JUGGERNAUT = ArcanaAugments.register(
         new ArcanaAugment("juggernaut", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.SOJOURNER_BOOTS,
               DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_VULNERABILITY_DURATION, ConfigUnits.SECONDS),
               new Tuple<>(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.SOJOURNERS_BOOTS_JUGGERNAUT_SLOWNESS_DURATION, ConfigUnits.SECONDS)
         ));
   
   // Soulstone
   public static final ArcanaAugment SOUL_REAPER = ArcanaAugments.register(
         new ArcanaAugment("soul_reaper", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.SOULSTONE,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SOULSTONE_SOULS_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment SOUL_ANNIHILATION = ArcanaAugments.register(
         new ArcanaAugment("soul_annihilation", new ItemStack(Items.SOUL_CAMPFIRE), ArcanaRegistry.SOULSTONE,
               EXOTIC
         ));
   
   // Spawner Harness
   public static final ArcanaAugment REINFORCED_CHASSIS = ArcanaAugments.register(
         new ArcanaAugment("reinforced_chassis", new ItemStack(Items.REINFORCED_DEEPSLATE), ArcanaRegistry.SPAWNER_HARNESS,
               DIVINE
         ));
   
   // Spawner Infuser
   public static final ArcanaAugment AUGMENTED_APPARATUS = ArcanaAugments.register(
         new ArcanaAugment("augmented_apparatus", new ItemStack(Items.SCULK_CATALYST), ArcanaRegistry.SPAWNER_INFUSER,
               EXOTIC, SOVEREIGN, SOVEREIGN, DIVINE, DIVINE
         ));
   public static final ArcanaAugment SOUL_RESERVOIR = ArcanaAugments.register(
         new ArcanaAugment("soul_reservoir", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.SPAWNER_INFUSER,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SPAWNER_INFUSER_EXTRA_CAPACITY_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Stasis Pearl
   public static final ArcanaAugment SPATIAL_FOLD = ArcanaAugments.register(
         new ArcanaAugment("spatial_fold", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.STASIS_PEARL,
               EXOTIC
         ));
   public static final ArcanaAugment STASIS_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("stasis_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.STASIS_PEARL,
               MUNDANE, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STASIS_PEARL_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment STASIS_RECONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("stasis_reconstruction", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.STASIS_PEARL,
               EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STASIS_PEARL_RECONSTRUCT_DURATION, ConfigUnits.SECONDS),
               new Tuple<>(ArcanaConfig.STASIS_PEARL_REGEN_PER_LVL, ConfigUnits.HEARTS_PER_SECOND),
               new Tuple<>(ArcanaConfig.STASIS_PEARL_FORTITUDE_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Telescoping Beacon
   public static final ArcanaAugment CAREFUL_RECONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("careful_reconstruction", new ItemStack(Items.IRON_INGOT), ArcanaRegistry.TELESCOPING_BEACON,
               EMPOWERED
         ));
   public static final ArcanaAugment MINING_LASER = ArcanaAugments.register(
         new ArcanaAugment("mining_laser", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.TELESCOPING_BEACON,
               EXOTIC
         ));
   
   // Wings of Enderia
   public static final ArcanaAugment SCALES_OF_THE_CHAMPION = ArcanaAugments.register(
         new ArcanaAugment("scales_of_the_champion", new ItemStack(Items.NETHERITE_CHESTPLATE), ArcanaRegistry.WINGS_OF_ENDERIA,
               SOVEREIGN, DIVINE
         ));
   public static final ArcanaAugment WING_BUFFET = ArcanaAugments.register(
         new ArcanaAugment("wing_buffet", new ItemStack(Items.FEATHER), ArcanaRegistry.WINGS_OF_ENDERIA,
               SOVEREIGN, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.WINGS_OF_ENDERIA_BUFFET_RANGE, ConfigUnits.BLOCKS)
         ));
   
   // Charm of Wild Growth
   public static final ArcanaAugment CHARM_OF_BLOOMING = ArcanaAugments.register(
         new ArcanaAugment("charm_of_blooming", new ItemStack(Items.PEONY), ArcanaRegistry.WILD_GROWTH_CHARM,
               SOVEREIGN
         ));
   public static final ArcanaAugment FERTILIZATION = ArcanaAugments.register(
         new ArcanaAugment("fertilization", new ItemStack(Items.BONE_MEAL), ArcanaRegistry.WILD_GROWTH_CHARM,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.WILD_GROWTH_CHARM_FERTILIZER_INTERVALS, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment REAPING = ArcanaAugments.register(
         new ArcanaAugment("reaping", new ItemStack(Items.DIAMOND_HOE), ArcanaRegistry.WILD_GROWTH_CHARM,
               SOVEREIGN, DIVINE
         ));
   
   // Arcanist's Belt
   public static final ArcanaAugment POUCHES = ArcanaAugments.register(
         new ArcanaAugment("pouches", new ItemStack(Items.CHEST), ArcanaRegistry.ARCANISTS_BELT,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN
         ));
   public static final ArcanaAugment MENTAL_PADDING = ArcanaAugments.register(
         new ArcanaAugment("mental_padding", new ItemStack(Items.LEATHER_HELMET), ArcanaRegistry.ARCANISTS_BELT,
               DIVINE
         ));
   
   // Containment Circlet
   public static final ArcanaAugment CONFINEMENT = ArcanaAugments.register(
         new ArcanaAugment("confinement", new ItemStack(Items.SPAWNER), ArcanaRegistry.CONTAINMENT_CIRCLET,
               SOVEREIGN
         ));
   public static final ArcanaAugment HEALING_CIRCLET = ArcanaAugments.register(
         new ArcanaAugment("healing_circlet", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.CONTAINMENT_CIRCLET,
               EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CONTAINMENT_CIRCLET_HEALING_RATE, ConfigUnits.HEARTS_PER_SECOND)
         ));
   
   // Alchemical Arbalest
   public static final ArcanaAugment RUNIC_ARBALEST = ArcanaAugments.register(
         new ArcanaAugment("runic_arbalest", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.ALCHEMICAL_ARBALEST,
               DIVINE
         ));
   public static final ArcanaAugment SPECTRAL_AMPLIFICATION = ArcanaAugments.register(
         new ArcanaAugment("spectral_amplification", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.ALCHEMICAL_ARBALEST,
               EXOTIC, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ALCHEMICAL_ARBALEST_VULNERABILITY_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment PROLIFIC_POTIONS = ArcanaAugments.register(
         new ArcanaAugment("prolific_potions", new ItemStack(Items.POTION), ArcanaRegistry.ALCHEMICAL_ARBALEST,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ALCHEMICAL_ARBALEST_FIELD_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment SCATTERSHOT = ArcanaAugments.register(
         new ArcanaAugment("scattershot", new ItemStack(Items.MELON_SEEDS), ArcanaRegistry.ALCHEMICAL_ARBALEST,
               DIVINE
         ));
   
   // Chest Translocator
   public static final ArcanaAugment RAPID_TRANSLOCATION = ArcanaAugments.register(
         new ArcanaAugment("rapid_translocation", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.CHEST_TRANSLOCATOR,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CHEST_TRANSLOCATOR_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Planeshifter
   public static final ArcanaAugment PLANAR_FLOW = ArcanaAugments.register(
         new ArcanaAugment("planar_flow", new ItemStack(Items.SCULK), ArcanaRegistry.PLANESHIFTER,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.PLANESHIFTER_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   
   // Everlasting Rocket
   public static final ArcanaAugment ADJUSTABLE_FUSE = ArcanaAugments.register(
         new ArcanaAugment("adjustable_fuse", new ItemStack(Items.STRING), ArcanaRegistry.EVERLASTING_ROCKET,
               EXOTIC
         ));
   public static final ArcanaAugment SULFUR_REPLICATION = ArcanaAugments.register(
         new ArcanaAugment("sulfur_replication", new ItemStack(Items.GUNPOWDER), ArcanaRegistry.EVERLASTING_ROCKET,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.EVERLASTING_ROCKET_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment POWDER_PACKING = ArcanaAugments.register(
         new ArcanaAugment("powder_packing", new ItemStack(Items.TNT), ArcanaRegistry.EVERLASTING_ROCKET,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.EVERLASTING_ROCKET_CHARGES_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Altar of the Stormcaller
   public static final ArcanaAugment CLOUD_SEEDING = ArcanaAugments.register(
         new ArcanaAugment("cloud_seeding", new ItemStack(Items.PUMPKIN_SEEDS), ArcanaRegistry.STORMCALLER_ALTAR,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STORMCALLER_ALTAR_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   public static final ArcanaAugment PERSISTENT_TEMPEST = ArcanaAugments.register(
         new ArcanaAugment("persistent_tempest", new ItemStack(Items.CLOCK), ArcanaRegistry.STORMCALLER_ALTAR,
               DIVINE
         ));
   
   // Celestial Altar
   public static final ArcanaAugment STELLAR_CONTROL = ArcanaAugments.register(
         new ArcanaAugment("stellar_control", new ItemStack(Items.GLOWSTONE), ArcanaRegistry.CELESTIAL_ALTAR,
               DIVINE
         ));
   public static final ArcanaAugment ORBITAL_PERIOD = ArcanaAugments.register(
         new ArcanaAugment("orbital_period", new ItemStack(Items.CLOCK), ArcanaRegistry.CELESTIAL_ALTAR,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CELESTIAL_ALTAR_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   
   // Starpath Altar
   public static final ArcanaAugment ASTRAL_PATHFINDER = ArcanaAugments.register(
         new ArcanaAugment("astral_pathfinder", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.STARPATH_ALTAR,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE
         ));
   public static final ArcanaAugment CONSTELLATION_DRIFT = ArcanaAugments.register(
         new ArcanaAugment("constellation_drift", new ItemStack(Items.SCULK), ArcanaRegistry.STARPATH_ALTAR,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STARPATH_ALTAR_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   public static final ArcanaAugment STAR_CHARTS = ArcanaAugments.register(
         new ArcanaAugment("star_charts", new ItemStack(Items.FILLED_MAP), ArcanaRegistry.STARPATH_ALTAR,
               EMPOWERED
         ));
   public static final ArcanaAugment STARGATE = ArcanaAugments.register(
         new ArcanaAugment("stargate", new ItemStack(Items.END_PORTAL_FRAME), ArcanaRegistry.STARPATH_ALTAR,
               DIVINE
         ));
   
   // Starlight Forge
   public static final ArcanaAugment RESOURCEFUL = ArcanaAugments.register(
         new ArcanaAugment("resourceful", new ItemStack(Items.DIAMOND), ArcanaRegistry.STARLIGHT_FORGE,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN, DIVINE, DIVINE, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STARLIGHT_FORGE_RESOURCEFUL_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment SKILLED = ArcanaAugments.register(
         new ArcanaAugment("skilled", new ItemStack(Items.BOOK), ArcanaRegistry.STARLIGHT_FORGE,
               MUNDANE, MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN, SOVEREIGN, DIVINE, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STARLIGHT_FORGE_SKILLED_POINTS_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment MOONLIT_FORGE = ArcanaAugments.register(
         new ArcanaAugment("moonlit_forge", MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultInstance()), ArcanaRegistry.STARLIGHT_FORGE,
               SOVEREIGN
         ));
   public static final ArcanaAugment MYSTIC_COLLECTION = ArcanaAugments.register(
         new ArcanaAugment("mystic_collection", new ItemStack(Items.CHEST), ArcanaRegistry.STARLIGHT_FORGE,
               EMPOWERED
         ));
   public static final ArcanaAugment STELLAR_RANGE = ArcanaAugments.register(
         new ArcanaAugment("stellar_range", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.STARLIGHT_FORGE,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STARLIGHT_FORGE_STELLAR_RANGE_VERTICAL, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.STARLIGHT_FORGE_STELLAR_RANGE_HORIZONTAL, ConfigUnits.BLOCKS)
         ));
   
   // Twilight Anvil
   public static final ArcanaAugment ENHANCED_ENHANCEMENTS = ArcanaAugments.register(
         new ArcanaAugment("enhanced_enhancements", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.TWILIGHT_ANVIL,
               EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.TWILIGHT_ANVIL_INFUSION_BUFF_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment ANVIL_EXPERTISE = ArcanaAugments.register(
         new ArcanaAugment("anvil_expertise", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.TWILIGHT_ANVIL,
               SOVEREIGN
         ));
   
   // Midnight Enchanter
   public static final ArcanaAugment PRECISION_DISENCHANTING = ArcanaAugments.register(
         new ArcanaAugment("precision_disenchanting", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.MIDNIGHT_ENCHANTER,
               SOVEREIGN
         ));
   public static final ArcanaAugment ENCHANTING_EXPERTISE = ArcanaAugments.register(
         new ArcanaAugment("enchanting_expertise", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.MIDNIGHT_ENCHANTER,
               SOVEREIGN
         ));
   public static final ArcanaAugment ESSENCE_SUPERNOVA = ArcanaAugments.register(
         new ArcanaAugment("essence_supernova", MinecraftUtils.removeLore(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultInstance()), ArcanaRegistry.MIDNIGHT_ENCHANTER,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.MIDNIGHT_ENCHANTER_ESSENCE_RATE_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Arcane Singularity
   public static final ArcanaAugment SUPERMASSIVE = ArcanaAugments.register(
         new ArcanaAugment("supermassive", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.ARCANE_SINGULARITY,
               MUNDANE, EMPOWERED, EMPOWERED, EXOTIC, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ARCANE_SINGULARITY_BOOKS_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment ACCRETION = ArcanaAugments.register(
         new ArcanaAugment("accretion", new ItemStack(Items.CRYING_OBSIDIAN), ArcanaRegistry.ARCANE_SINGULARITY,
               SOVEREIGN
         ));
   
   // Stellar Core
   public static final ArcanaAugment DYSON_SPHERE = ArcanaAugments.register(
         new ArcanaAugment("dyson_sphere", new ItemStack(Items.SPAWNER), ArcanaRegistry.STELLAR_CORE,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STELLAR_CORE_SALVAGE_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment FUSION_INJECTORS = ArcanaAugments.register(
         new ArcanaAugment("fusion_injectors", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.STELLAR_CORE,
               MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.STELLAR_CORE_STARDUST_RATE_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment MOLTEN_CORE = ArcanaAugments.register(
         new ArcanaAugment("molten_core", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.STELLAR_CORE,
               EXOTIC
         ));
   
   // Radiant Fletchery
   public static final ArcanaAugment ALCHEMICAL_EFFICIENCY = ArcanaAugments.register(
         new ArcanaAugment("alchemical_efficiency", new ItemStack(Items.LINGERING_POTION), ArcanaRegistry.RADIANT_FLETCHERY,
               MUNDANE, MUNDANE, EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.RADIANT_FLETCHERY_ARROWS_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Totem of Vengeance
   public static final ArcanaAugment RETALIATIVE_FURY = ArcanaAugments.register(
         new ArcanaAugment("retaliative_fury", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.TOTEM_OF_VENGEANCE,
               MUNDANE, MUNDANE, EMPOWERED).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.TOTEM_OF_VENGEANCE_DURATION_PER_LVL, ConfigUnits.MINUTES),
               new Tuple<>(ArcanaConfig.TOTEM_OF_VENGEANCE_SPEED_PER_LVL, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.TOTEM_OF_VENGEANCE_STRENGTH_PER_LVL, ConfigUnits.NONE)
         ));
   
   // Aquatic Eversource
   public static final ArcanaAugment FLOODGATE = ArcanaAugments.register(
         new ArcanaAugment("floodgate", new ItemStack(Items.WARPED_FENCE_GATE), ArcanaRegistry.AQUATIC_EVERSOURCE,
               EMPOWERED
         ));
   
   // Magmatic Eversource
   public static final ArcanaAugment VOLCANIC_CHAMBER = ArcanaAugments.register(
         new ArcanaAugment("volcanic_chamber", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.MAGMATIC_EVERSOURCE,
               EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.MAGMATIC_EVERSOURCE_CHARGES_PER_LVL, ConfigUnits.NONE)
         ));
   public static final ArcanaAugment ERUPTION = ArcanaAugments.register(
         new ArcanaAugment("eruption", new ItemStack(Items.MAGMA_CREAM), ArcanaRegistry.MAGMATIC_EVERSOURCE,
               EMPOWERED, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.MAGMATIC_EVERSOURCE_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Transmutation Altar
   public static final ArcanaAugment TRADE_AGREEMENT = ArcanaAugments.register(
         new ArcanaAugment("trade_agreement", new ItemStack(Items.DIAMOND), ArcanaRegistry.TRANSMUTATION_ALTAR,
               DIVINE
         ));
   public static final ArcanaAugment HASTY_BARGAIN = ArcanaAugments.register(
         new ArcanaAugment("hasty_bargain", new ItemStack(Items.CLOCK), ArcanaRegistry.TRANSMUTATION_ALTAR,
               EXOTIC, MUNDANE, MUNDANE, EMPOWERED, EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.TRANSMUTATION_ALTAR_COOLDOWN_PER_LVL, ConfigUnits.MINUTES)
         ));
   
   // Aequalis Scientia
   public static final ArcanaAugment EQUIVALENT_EXCHANGE = ArcanaAugments.register(
         new ArcanaAugment("equivalent_exchange", ArcanaRegistry.CATALYTIC_MATRIX::getPrefItemNoLore, ArcanaRegistry.AEQUALIS_SCIENTIA,
               SOVEREIGN
         ));
   public static final ArcanaAugment TIMELESS_WISDOM = ArcanaAugments.register(
         new ArcanaAugment("timeless_wisdom", ArcanaRegistry.DIVINE_CATALYST::getPrefItemNoLore, ArcanaRegistry.AEQUALIS_SCIENTIA,
               DIVINE
         ));
   public static final ArcanaAugment IMPERMANENT_PERMUTATION = ArcanaAugments.register(
         new ArcanaAugment("impermanent_permutation", MinecraftUtils.removeLore(ArcanaRegistry.NEBULOUS_ESSENCE.getDefaultInstance()), ArcanaRegistry.AEQUALIS_SCIENTIA,
               DIVINE
         ));
   
   // Ensnarement Arrows
   public static final ArcanaAugment ETHEREAL_ANCHOR = ArcanaAugments.register(
         new ArcanaAugment("ethereal_anchor", new ItemStack(Items.ENDER_PEARL), ArcanaRegistry.ENSNAREMENT_ARROWS,
               DIVINE
         ));
   public static final ArcanaAugment ENTRAPMENT = ArcanaAugments.register(
         new ArcanaAugment("entrapment", new ItemStack(Items.COBWEB), ArcanaRegistry.ENSNAREMENT_ARROWS,
               EMPOWERED, EXOTIC, EXOTIC, SOVEREIGN, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ENSNAREMENT_ARROW_ENTRAPMENT_DURATION_INCREASE_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Tracking Arrows
   public static final ArcanaAugment RUNIC_GUIDANCE = ArcanaAugments.register(
         new ArcanaAugment("runic_guidance", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.TRACKING_ARROWS,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.TRACKING_ARROW_DETECTION_WIDTH_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment BROADHEADS = ArcanaAugments.register(
         new ArcanaAugment("broadheads", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.TRACKING_ARROWS,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.TRACKING_ARROW_BROADHEAD_DMG_AMP_PER_LVL, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.TRACKING_ARROW_BROADHEAD_DMG_AMP_DURATION_PER_LVL, ConfigUnits.SECONDS)
         ));
   
   // Binary Blades
   public static final ArcanaAugment PULSAR_BLADES = ArcanaAugments.register(
         new ArcanaAugment("pulsar_blades", new ItemStack(Items.PEARLESCENT_FROGLIGHT), ArcanaRegistry.BINARY_BLADES,
               DIVINE, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BINARY_BLADES_PULSAR_ENERGY_CONSUMPTION_PER_LVL, ConfigUnits.NONE),
               new Tuple<>(ArcanaConfig.BINARY_BLADES_PULSAR_RANGE, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.BINARY_BLADES_PULSAR_DMG, ConfigUnits.HEARTS)
         ));
   public static final ArcanaAugment RED_GIANT_BLADES = ArcanaAugments.register(
         new ArcanaAugment("red_giant_blades", new ItemStack(Items.SHROOMLIGHT), ArcanaRegistry.BINARY_BLADES,
               DIVINE, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BINARY_BLADES_RED_GIANT_DMG_PER_ENERGY, ConfigUnits.HEARTS)
         ));
   public static final ArcanaAugment WHITE_DWARF_BLADES = ArcanaAugments.register(
         new ArcanaAugment("white_dwarf_blades", new ItemStack(Items.SEA_LANTERN), ArcanaRegistry.BINARY_BLADES,
               DIVINE, SOVEREIGN, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.BINARY_BLADES_WHITE_DWARF_DMG_PER_ENERGY_BLOCK, ConfigUnits.HEARTS)
         ));
   
   // Graviton Maul
   public static final ArcanaAugment SINGULARITY_MAELSTROM = ArcanaAugments.register(
         new ArcanaAugment("singularity_maelstrom", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.GRAVITON_MAUL,
               DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.GRAVITON_MAUL_VORTEX_RANGE, ConfigUnits.BLOCKS),
               new Tuple<>(ArcanaConfig.GRAVITON_MAUL_VORTEX_DMG_AMP, ConfigUnits.PERCENT),
               new Tuple<>(ArcanaConfig.GRAVITON_MAUL_VORTEX_DMG, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.GRAVITON_MAUL_VORTEX_FORTITUDE, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment GRAVITIC_DOMAIN = ArcanaAugments.register(
         new ArcanaAugment("gravitic_domain", new ItemStack(Items.SLIME_BLOCK), ArcanaRegistry.GRAVITON_MAUL,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.GRAVITON_MAUL_AOE_RANGE, ConfigUnits.BLOCKS)
         ));
   
   // Charm of Cetacea
   public static final ArcanaAugment DELPHINIDAE = ArcanaAugments.register(
         new ArcanaAugment("delphinidae", new ItemStack(Items.TROPICAL_FISH), ArcanaRegistry.CETACEA_CHARM,
               EMPOWERED
         ));
   public static final ArcanaAugment GILLS = ArcanaAugments.register(
         new ArcanaAugment("gills", new ItemStack(Items.TURTLE_SCUTE), ArcanaRegistry.CETACEA_CHARM,
               EXOTIC
         ));
   public static final ArcanaAugment MARINERS_GRACE = ArcanaAugments.register(
         new ArcanaAugment("mariners_grace", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.CETACEA_CHARM,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CETACEA_CHARM_SWIM_PENALTY_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Charm of Cleansing
   public static final ArcanaAugment REJUVENATION = ArcanaAugments.register(
         new ArcanaAugment("rejuvenation", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.CLEANSING_CHARM,
               EXOTIC).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CLEANSING_CHARM_REJUVENATION_HEALTH_PER_TICK, ConfigUnits.HEARTS_PER_SECOND),
               new Tuple<>(ArcanaConfig.CLEANSING_CHARM_REJUVENATION_DURATION, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment INFUSED_CHARCOAL = ArcanaAugments.register(
         new ArcanaAugment("infused_charcoal", new ItemStack(Items.CHARCOAL), ArcanaRegistry.CLEANSING_CHARM,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.CLEANSING_CHARM_CHARCOAL_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment ANTIDOTE = ArcanaAugments.register(
         new ArcanaAugment("antidote", new ItemStack(Items.MILK_BUCKET), ArcanaRegistry.CLEANSING_CHARM,
               EXOTIC
         ));
   
   // Greaves of Gaialtus
   public static final ArcanaAugment PLANETARY_POCKETS = ArcanaAugments.register(
         new ArcanaAugment("planetary_pockets", new ItemStack(Items.CHEST), ArcanaRegistry.GREAVES_OF_GAIALTUS,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN
         ));
   public static final ArcanaAugment CREATORS_TOUCH = ArcanaAugments.register(
         new ArcanaAugment("creators_touch", new ItemStack(Items.CRAFTING_TABLE), ArcanaRegistry.GREAVES_OF_GAIALTUS,
               DIVINE
         ));
   public static final ArcanaAugment NATURES_EMBRACE = ArcanaAugments.register(
         new ArcanaAugment("natures_embrace", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.GREAVES_OF_GAIALTUS,
               SOVEREIGN
         ));
   public static final ArcanaAugment WINDS_GRACE = ArcanaAugments.register(
         new ArcanaAugment("winds_grace", new ItemStack(Items.LIGHT_GRAY_WOOL), ArcanaRegistry.GREAVES_OF_GAIALTUS,
               EXOTIC
         ));
   public static final ArcanaAugment EARTHEN_ASCENT = ArcanaAugments.register(
         new ArcanaAugment("earthen_ascent", new ItemStack(Items.LADDER), ArcanaRegistry.GREAVES_OF_GAIALTUS,
               EXOTIC
         ));
   
   // Spear of Tenbrous
   public static final ArcanaAugment VOID_STORM = ArcanaAugments.register(
         new ArcanaAugment("void_storm", new ItemStack(Items.LIGHTNING_ROD), ArcanaRegistry.SPEAR_OF_TENBROUS,
               DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SPEAR_OF_TENBROUS_STORM_DMG, ConfigUnits.HEARTS),
               new Tuple<>(ArcanaConfig.SPEAR_OF_TENBROUS_STORM_RANGE, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment UNENDING_HATRED = ArcanaAugments.register(
         new ArcanaAugment("unending_hatred", new ItemStack(Items.TRIDENT), ArcanaRegistry.SPEAR_OF_TENBROUS,
               MUNDANE, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.SPEAR_OF_TENBROUS_THROW_COOLDOWN_PER_LVL, ConfigUnits.SECONDS)
         ));
   public static final ArcanaAugment ETERNAL_CRUELTY = ArcanaAugments.register(
         new ArcanaAugment("eternal_cruelty", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.SPEAR_OF_TENBROUS,
               EXOTIC
         ));
   public static final ArcanaAugment BLINDING_RAGE = ArcanaAugments.register(
         new ArcanaAugment("blinding_rage", new ItemStack(Items.BLACK_CONCRETE), ArcanaRegistry.SPEAR_OF_TENBROUS,
               EXOTIC
         ));
   public static final ArcanaAugment STARLESS_DOMAIN = ArcanaAugments.register(
         new ArcanaAugment("starless_domain", MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultInstance()), ArcanaRegistry.SPEAR_OF_TENBROUS,
               SOVEREIGN
         ));
   
   // Geomantic Stele
   public static final ArcanaAugment GEOLITHIC_AMPLIFICATION = ArcanaAugments.register(
         new ArcanaAugment("geolithic_amplification", new ItemStack(Items.DIAMOND), ArcanaRegistry.GEOMANTIC_STELE,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.GEOMANTIC_STELE_RANGE_MULTIPLIER_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment METAMORPHIC_ALIGNMENT = ArcanaAugments.register(
         new ArcanaAugment("metamorphic_alignment", new ItemStack(Items.QUARTZ), ArcanaRegistry.GEOMANTIC_STELE,
               EXOTIC
         ));
   
   // Interdictor
   public static final ArcanaAugment COALESCENCE_REDIRECTION = ArcanaAugments.register(
         new ArcanaAugment("coalescence_redirection", new ItemStack(Items.GREEN_STAINED_GLASS), ArcanaRegistry.INTERDICTOR,
               DIVINE
         ));
   public static final ArcanaAugment NATAL_RIFT = ArcanaAugments.register(
         new ArcanaAugment("natal_rift", new ItemStack(Items.PURPLE_STAINED_GLASS), ArcanaRegistry.INTERDICTOR,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.INTERDICTOR_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment DECOALESCENCE = ArcanaAugments.register(
         new ArcanaAugment("decoalescence", new ItemStack(Items.RED_STAINED_GLASS), ArcanaRegistry.INTERDICTOR,
               SOVEREIGN
         ));
   public static final ArcanaAugment PRECISION_INTERDICTION = ArcanaAugments.register(
         new ArcanaAugment("precision_interdiction", new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS), ArcanaRegistry.INTERDICTOR,
               EXOTIC
         ));
   
   // Ender Crate
   public static final ArcanaAugment BIOMETRIC_CHANNELS = ArcanaAugments.register(
         new ArcanaAugment("biometric_channels", new ItemStack(Items.DIAMOND), ArcanaRegistry.ENDER_CRATE,
               DIVINE
         ));
   public static final ArcanaAugment ENDER_BANDWIDTH = ArcanaAugments.register(
         new ArcanaAugment("ender_bandwidth", new ItemStack(Items.CHEST), ArcanaRegistry.ENDER_CRATE,
               SOVEREIGN, SOVEREIGN, SOVEREIGN
         ));
   
   // Astral Gateway
   public static final ArcanaAugment ASTRAL_STABILIZERS = ArcanaAugments.register(
         new ArcanaAugment("astral_stabilizers", new ItemStack(Items.REINFORCED_DEEPSLATE), ArcanaRegistry.ASTRAL_GATEWAY,
               EXOTIC, EXOTIC
         ));
   public static final ArcanaAugment STARLIGHT_RECYCLERS = ArcanaAugments.register(
         new ArcanaAugment("starlight_recyclers", MinecraftUtils.removeLore(ArcanaRegistry.STARDUST.getDefaultInstance()), ArcanaRegistry.ASTRAL_GATEWAY,
               EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN, SOVEREIGN, DIVINE).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ASTRAL_GATEWAY_STARDUST_PER_LVL, ConfigUnits.PERCENT)
         ));
   public static final ArcanaAugment ASTRAL_STARGATE = ArcanaAugments.register(
         new ArcanaAugment("astral_stargate", ArcanaRegistry.WAYSTONE::getPrefItemNoLore, ArcanaRegistry.ASTRAL_GATEWAY,
               DIVINE
         ));
   
   // Clockwork Multitool
   public static final ArcanaAugment ENCHANTMENT_MECHANISM = ArcanaAugments.register(
         new ArcanaAugment("enchantment_mechanism", new ItemStack(Items.ENCHANTING_TABLE), ArcanaRegistry.CLOCKWORK_MULTITOOL,
               EXOTIC
         ));
   public static final ArcanaAugment REPAIRING_MECHANISM = ArcanaAugments.register(
         new ArcanaAugment("repairing_mechanism", new ItemStack(Items.ANVIL), ArcanaRegistry.CLOCKWORK_MULTITOOL,
               EXOTIC
         ));
   public static final ArcanaAugment ENDER_MECHANISM = ArcanaAugments.register(
         new ArcanaAugment("ender_mechanism", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.CLOCKWORK_MULTITOOL,
               SOVEREIGN, DIVINE, SOVEREIGN, SOVEREIGN, SOVEREIGN
         ));
   
   // Negotiation Charm
   public static final ArcanaAugment EXTORTION = ArcanaAugments.register(
         new ArcanaAugment("extortion", new ItemStack(Items.EMERALD_BLOCK), ArcanaRegistry.NEGOTIATION_CHARM,
               SOVEREIGN
         ));
   
   // Itineranteur
   public static final ArcanaAugment GUIDING_LIGHT = ArcanaAugments.register(
         new ArcanaAugment("guiding_light", new ItemStack(Items.GLOWSTONE), ArcanaRegistry.ITINERANTEUR,
               EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ITINERANTEUR_RANGE_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment THOROUGHFARE = ArcanaAugments.register(
         new ArcanaAugment("thoroughfare", new ItemStack(Items.DIRT_PATH), ArcanaRegistry.ITINERANTEUR,
               EMPOWERED, EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ITINERANTEUR_BLOCKS_PER_LVL, ConfigUnits.BLOCKS)
         ));
   public static final ArcanaAugment ROAD_SNACKS = ArcanaAugments.register(
         new ArcanaAugment("road_snacks", new ItemStack(Items.COOKED_PORKCHOP), ArcanaRegistry.ITINERANTEUR,
               SOVEREIGN
         ));
   public static final ArcanaAugment PAVED_WARMTH = ArcanaAugments.register(
         new ArcanaAugment("paved_warmth", new ItemStack(Items.POLISHED_ANDESITE), ArcanaRegistry.ITINERANTEUR,
               EMPOWERED, EXOTIC, SOVEREIGN).setRelatedConfigs(
               new Tuple<>(ArcanaConfig.ITINERANTEUR_SPEED_PER_LVL, ConfigUnits.PERCENT)
         ));
   
   // Linked and exclusive augments
   static{
      linkedAugments.put(ArcanaAugments.HARNESS_RECYCLER, "harness_shulker_recycler");
      linkedAugments.put(ArcanaAugments.SHULKER_RECYCLER, "harness_shulker_recycler");
      
      linkedAugments.put(ArcanaAugments.OVERFLOWING_BOTTOMLESS, "quiver_efficiency");
      linkedAugments.put(ArcanaAugments.RUNIC_BOTTOMLESS, "quiver_efficiency");
      
      linkedAugments.put(ArcanaAugments.ABUNDANT_AMMO, "quiver_restock");
      linkedAugments.put(ArcanaAugments.QUIVER_DUPLICATION, "quiver_restock");
      
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.WEB_OF_FIRE,
            ArcanaAugments.PYROBLAST)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.ANTI_PERSONNEL,
            ArcanaAugments.BLAST_MINE)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.AFTERSHOCK,
            ArcanaAugments.CHAIN_LIGHTNING)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.RUNIC_ARBALEST,
            ArcanaAugments.SCATTERSHOT)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.RUNIC_ARBALEST,
            ArcanaAugments.PROLIFIC_POTIONS)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.RUNIC_ARBALEST,
            ArcanaAugments.SPECTRAL_AMPLIFICATION)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.REPULSION,
            ArcanaAugments.EVICTION_BURST)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.PULSAR_BLADES,
            ArcanaAugments.RED_GIANT_BLADES,
            ArcanaAugments.WHITE_DWARF_BLADES)));
      exclusiveAugments.add(new ArrayList<>(Arrays.asList(
            ArcanaAugments.DECOALESCENCE,
            ArcanaAugments.COALESCENCE_REDIRECTION)));
   }
   
   private static ArcanaAugment register(ArcanaAugment augment){
      registry.put(augment.id, augment);
      return augment;
   }
   
   public static List<ArcanaAugment> getLinkedAugments(ArcanaAugment augment){
      ArrayList<ArcanaAugment> linked = new ArrayList<>();
      if(!linkedAugments.containsKey(augment)){
         linked.add(augment);
      }else{
         String linkedId = linkedAugments.get(augment);
         for(Map.Entry<ArcanaAugment, String> entry : linkedAugments.entrySet()){
            if(entry.getValue().equals(linkedId)){
               linked.add(entry.getKey());
            }
         }
      }
      return linked;
   }
   
   
   public static List<ArcanaAugment> getAugmentsForItem(ArcanaItem item){
      ArrayList<ArcanaAugment> augments = new ArrayList<>();
      for(Map.Entry<String, ArcanaAugment> entry : registry.entrySet()){
         if(entry.getValue().getArcanaItem().getId().equals(item.getId())) augments.add(entry.getValue());
      }
      return augments;
   }
   
   public static TreeMap<ArcanaAugment, Integer> getAugmentsOnItem(ItemStack stack){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return null;
      TreeMap<ArcanaAugment, Integer> map = new TreeMap<>();
      CompoundTag augmentTag = ArcanaItem.getCompoundProperty(stack, ArcanaItem.AUGMENTS_TAG);
      
      for(String key : augmentTag.keySet()){
         if(registry.containsKey(key)){
            map.put(registry.get(key), augmentTag.getIntOr(key, 0));
         }
      }
      return map;
   }
   
   public static void setAugmentsOnItem(ItemStack stack, TreeMap<ArcanaAugment, Integer> augments){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return;
      CompoundTag augsTag = new CompoundTag();
      
      augments.forEach((augKey, augValue) -> {
         if(registry.containsKey(augKey.id)){
            augsTag.putInt(augKey.id, augValue);
         }
      });
      
      ArcanaItem.putProperty(stack, ArcanaItem.AUGMENTS_TAG, augsTag);
      arcanaItem.buildItemLore(stack, BorisLib.SERVER);
   }
   
   public static boolean isIncompatible(ItemStack item, ArcanaAugment augment){
      TreeMap<ArcanaAugment, Integer> curAugments = getAugmentsOnItem(item);
      if(curAugments == null) return true;
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(item);
      if(arcanaItem == null) return true;
      if(!augment.getArcanaItem().getId().equals(arcanaItem.getId())) return true;
      
      for(Map.Entry<ArcanaAugment, Integer> entry : curAugments.entrySet()){
         ArcanaAugment other = entry.getKey();
         if(other == augment) return false;
         
         for(List<ArcanaAugment> exclusiveList : exclusiveAugments){
            if(exclusiveList.contains(augment) && exclusiveList.contains(other)) return true;
         }
      }
      return false;
   }
   
   public static boolean validAugmentAndItem(ItemStack stack, String id){
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null || !registry.containsKey(id)) return false;
      return registry.get(id).getArcanaItem().getId().equals(arcanaItem.getId());
   }
   
   public static int getAugmentOnItem(ItemStack stack, ArcanaAugment augment){
      return getAugmentOnItem(stack, augment.id);
   }
   
   public static int getAugmentOnItem(ItemStack stack, String id){
      if(!validAugmentAndItem(stack, id)) return 0;
      CompoundTag augmentTag = ArcanaItem.getCompoundProperty(stack, ArcanaItem.AUGMENTS_TAG);
      if(augmentTag.contains(id)){
         return augmentTag.getIntOr(id, 0);
      }
      return 0;
   }
   
   public static int getAugmentFromCompound(CompoundTag compound, ArcanaAugment augment){
      if(!registry.containsKey(augment.id)) return -1;
      CompoundTag augmentTag = compound.contains("augments") ? compound.getCompoundOrEmpty("augments") : compound;
      if(augmentTag.contains(augment.id)){
         return augmentTag.getIntOr(augment.id, 0);
      }
      return 0;
   }
   
   public static int getAugmentFromMap(TreeMap<ArcanaAugment, Integer> augments, ArcanaAugment augment){
      if(augments == null) return 0;
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey() == augment) return entry.getValue();
      }
      return 0;
   }
   
   public static int getAugmentFromMap(TreeMap<ArcanaAugment, Integer> augments, String id){
      if(augments == null) return 0;
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)) return entry.getValue();
      }
      return 0;
   }
   
   // Applies Augment to Item, cannot down-level existing augments
   public static boolean applyAugment(ItemStack stack, ArcanaAugment augment, int level, boolean withCatalyst){
      if(!validAugmentAndItem(stack, augment.id)) return false;
      int curLevel = getAugmentOnItem(stack, augment);
      ArcanaItem arcanaItem = ArcanaItemUtils.identifyItem(stack);
      if(arcanaItem == null) return false;
      if(!augment.getArcanaItem().getId().equals(arcanaItem.getId())) return false;
      if(level > augment.getTiers().length || curLevel >= augment.getTiers().length) return false;
      if(isIncompatible(stack, augment)) return false;
      if(curLevel >= level) return false;
      
      CompoundTag augmentTag = ArcanaItem.getCompoundProperty(stack, ArcanaItem.AUGMENTS_TAG);
      augmentTag.putInt(augment.id, level);
      ArcanaItem.putProperty(stack, ArcanaItem.AUGMENTS_TAG, augmentTag);
      
      if(withCatalyst){
         int clampedCurLevel = Math.max(0, curLevel);
         ListTag catalystsList = ArcanaItem.getListProperty(stack, ArcanaItem.CATALYSTS_TAG);
         for(int lvl = clampedCurLevel + 1; lvl <= level; lvl++){
            CompoundTag cata = new CompoundTag();
            ArcanaRarity rarity = augment.getTiers()[lvl - 1];
            cata.putString("augment", augment.id);
            cata.putInt("level", lvl);
            cata.putInt("rarity", rarity.rarity);
            catalystsList.add(cata);
         }
         ArcanaItem.putProperty(stack, ArcanaItem.CATALYSTS_TAG, catalystsList);
      }
      
      arcanaItem.onAugment(stack, augment, level);
      arcanaItem.buildItemLore(stack, BorisLib.SERVER);
      return true;
   }
   
   public static void copyAugment(ItemStack sourceStack, ItemStack destinationStack, ArcanaAugment sourceAugment, ArcanaAugment destinationAugment){
      ListTag sourceCatas = ArcanaItem.getListProperty(sourceStack, ArcanaItem.CATALYSTS_TAG);
      
      int sourceLvl = ArcanaAugments.getAugmentOnItem(sourceStack, sourceAugment);
      if(sourceLvl <= 0) return;
      
      for(int lvl = 0; lvl <= sourceLvl; lvl++){
         boolean foundCata = false;
         for(Tag sourceCata : sourceCatas){
            CompoundTag cata = (CompoundTag) sourceCata;
            if(cata.getStringOr("augment", "").equals(sourceAugment.id) && cata.getIntOr("level", 0) == lvl){
               ArcanaAugments.applyAugment(destinationStack, destinationAugment, lvl, true);
               foundCata = true;
               break;
            }
         }
         if(!foundCata){
            ArcanaAugments.applyAugment(destinationStack, destinationAugment, lvl, false);
         }
      }
   }
   
   public record AugmentData(TreeMap<ArcanaAugment, Integer> augments) {
      private static final Comparator<ArcanaAugment> BY_ID = Comparator.comparing(a -> a.id);
      
      public AugmentData{
         if(augments == null){
            augments = new TreeMap<>(BY_ID);
         }else if(augments.comparator() != BY_ID){
            TreeMap<ArcanaAugment, Integer> sorted = new TreeMap<>(BY_ID);
            sorted.putAll(augments);
            augments = sorted;
         }
      }
      
      private static DataResult<ArcanaAugment> resolveAugment(String id){
         ArcanaAugment a = ArcanaAugments.registry.get(id);
         return a != null ? DataResult.success(a) : DataResult.error(() -> "Unknown ArcanaAugment id: " + id);
      }
      
      private static String augmentId(ArcanaAugment a){
         return a.id;
      }
      
      public static final Codec<ArcanaAugment> AUGMENT_ID_CODEC = Codec.STRING.comapFlatMap(AugmentData::resolveAugment, AugmentData::augmentId);
      
      public static final Codec<TreeMap<ArcanaAugment, Integer>> AUGMENT_MAP_CODEC =
            Codec.unboundedMap(AUGMENT_ID_CODEC, Codec.INT).xmap(map -> {
               TreeMap<ArcanaAugment, Integer> sorted = new TreeMap<>(BY_ID);
               sorted.putAll(map);
               return sorted;
            }, tm -> tm);
      
      public static final Codec<AugmentData> CODEC =
            RecordCodecBuilder.create(inst -> inst.group(
                  AUGMENT_MAP_CODEC.fieldOf("augments").forGetter(AugmentData::augments)
            ).apply(inst, AugmentData::new));
   }
}
