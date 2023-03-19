package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.items.charms.LightCharm;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import java.util.*;

import static net.borisshoes.arcananovum.utils.MagicRarity.MUNDANE;
import static net.borisshoes.arcananovum.utils.MagicRarity.EMPOWERED;
import static net.borisshoes.arcananovum.utils.MagicRarity.EXOTIC;
import static net.borisshoes.arcananovum.utils.MagicRarity.LEGENDARY;
import static net.borisshoes.arcananovum.utils.MagicRarity.MYTHICAL;

public class ArcanaAugments {
   public static final HashMap<String, ArcanaAugment> registry = new HashMap<>();
   
   // Arcane Flak Arrows
   public static final ArcanaAugment AIRBURST = ArcanaAugments.register("airburst",
         new ArcanaAugment("Airburst", "airburst", new ItemStack(Items.FIREWORK_STAR), MagicItems.ARCANE_FLAK_ARROWS,
         new String[]{"Grants an extra 1.25 blocks burst radius per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Blink Arrows
   public static final ArcanaAugment PHASE_IN = ArcanaAugments.register("phase_in",
         new ArcanaAugment("Phase In", "phase_in", new ItemStack(Items.ENDER_PEARL), MagicItems.BLINK_ARROWS,
         new String[]{"Grants brief invulnerability on teleport","Duration per level: 1/3/5 seconds"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Concussion Arrows
   public static final ArcanaAugment SHELLSHOCK = ArcanaAugments.register("shellshock",
         new ArcanaAugment("Shellshock", "shellshock", new ItemStack(Items.GLOWSTONE_DUST), MagicItems.CONCUSSION_ARROWS,
         new String[]{"Increases effect strength and duration per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Detonation Arrows
   public static final ArcanaAugment ANTI_PERSONNEL = ArcanaAugments.register("anti_personnel",
         new ArcanaAugment("Anti-Personnel", "anti_personnel", new ItemStack(Items.ROTTEN_FLESH), MagicItems.DETONATION_ARROWS,
         new String[]{"Increases damage to creatures per level","Final level grants no terrain damage","Mutually Exclusive with Blast Mine"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment BLAST_MINE = ArcanaAugments.register("blast_mine",
         new ArcanaAugment("Blast Mine", "blast_mine", new ItemStack(Items.COBBLESTONE), MagicItems.DETONATION_ARROWS,
         new String[]{"Increases terrain and lowers mob damage","Higher levels amplify these effects","Mutually Exclusive with Anti-Personnel"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Expulsion Arrows
   public static final ArcanaAugment REPULSION = ArcanaAugments.register("repulsion",
         new ArcanaAugment("Repulsion", "repulsion", new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS), MagicItems.EXPULSION_ARROWS,
         new String[]{"Increases expulsion range by 1.5 blocks per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Graviton Arrows
   public static final ArcanaAugment GRAVITY_WELL = ArcanaAugments.register("gravity_well",
         new ArcanaAugment("Gravity Well", "gravity_well", new ItemStack(Items.OBSIDIAN), MagicItems.GRAVITON_ARROWS,
         new String[]{"Increases attraction range by 1 block per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Photonic Arrows
   public static final ArcanaAugment PRISMATIC_ALIGNMENT = ArcanaAugments.register("prismatic_alignment",
         new ArcanaAugment("Prismatic Alignment", "prismatic_alignment", new ItemStack(Items.BEACON), MagicItems.PHOTONIC_ARROWS,
         new String[]{"Increases damage for each enemy pierced","Higher levels increase damage more","Final level gives extra base damage"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Siphoning Arrows
   public static final ArcanaAugment OVERHEAL = ArcanaAugments.register("overheal",
         new ArcanaAugment("Overheal", "overheal", PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.HEALING), MagicItems.SIPHONING_ARROWS,
         new String[]{"Health siphoned above max becomes absorption","Max Overheal per Level: 2.5/5/10 hearts"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Smoke Arrows
   public static final ArcanaAugment TEAR_GAS = ArcanaAugments.register("tear_gas",
         new ArcanaAugment("Tear Gas", "tear_gas", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), MagicItems.SMOKE_ARROWS,
         new String[]{"Amplifies debilitating effects","Higher levels increase effect duration"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Storm Arrows
   public static final ArcanaAugment STORM_STABILIZATION = ArcanaAugments.register("storm_stabilization",
         new ArcanaAugment("Runic Stabilization", "storm_stabilization", new ItemStack(Items.END_CRYSTAL), MagicItems.STORM_ARROWS,
         new String[]{"Increases lightning chance per level","Final level grants guaranteed success"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment CHAIN_LIGHTNING = ArcanaAugments.register("chain_lightning",
         new ArcanaAugment("Chain Lightning", "chain_lightning", new ItemStack(Items.PRISMARINE_CRYSTALS), MagicItems.STORM_ARROWS,
         new String[]{"Striking an enemy chains a shock to","an additional creature for each level","Effect occurs independent of lightning","Mutually Exclusive with Aftershock"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   public static final ArcanaAugment AFTERSHOCK = ArcanaAugments.register("aftershock",
         new ArcanaAugment("Aftershock", "aftershock", new ItemStack(Items.LIGHTNING_ROD), MagicItems.STORM_ARROWS,
         new String[]{"Lightning strikes charge the ground","Higher levels increase charge duration","Final Level increases damage and size","Mutually Exclusive with Chain Lightning"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Tether Arrows
   public static final ArcanaAugment QUICK_RELEASE = ArcanaAugments.register("quick_release",
         new ArcanaAugment("Quick Release", "quick_release", new ItemStack(Items.SHEARS), MagicItems.TETHER_ARROWS,
         new String[]{"Sneaking cancels all in-flight Tether Arrows"," from activating the tether on landing"},
         new MagicRarity[]{EMPOWERED}
   ));
   
   // Charm of Cinders
   public static final ArcanaAugment PYROBLAST = ArcanaAugments.register("pyroblast",
         new ArcanaAugment("Pyroblast", "pyroblast", new ItemStack(Items.FIRE_CHARGE), MagicItems.CINDERS_CHARM,
         new String[]{"Cone of Flame becomes a powerful Fireball","The fireball does not damage terrain","Higher levels cause higher radius and damage","Mutually Exclusive with Web of Fire"},
         new MagicRarity[]{MYTHICAL,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment WEB_OF_FIRE = ArcanaAugments.register("web_of_fire",
         new ArcanaAugment("Web of Fire", "web_of_fire", new ItemStack(Items.FIRE_CORAL), MagicItems.CINDERS_CHARM,
         new String[]{"Cone of Flame becomes an AoE precision"," strike on creatures around you.","Higher levels adds more targets and damage","Mutually Exclusive with Pyroblast"},
         new MagicRarity[]{MYTHICAL,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment CREMATION = ArcanaAugments.register("cremation",
         new ArcanaAugment("Cremation", "cremation", new ItemStack(Items.SOUL_CAMPFIRE), MagicItems.CINDERS_CHARM,
         new String[]{"The Charm's Flames become blue soul-flame","Soul-flame does double damage to creatures","The Charm now uses cinders to negate fire damage"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment FIRESTARTER = ArcanaAugments.register("firestarter",
         new ArcanaAugment("Firestarter", "firestarter", new ItemStack(Items.FLINT_AND_STEEL), MagicItems.CINDERS_CHARM,
         new String[]{"The Charm's Left Click abilities no","longer consumes cinders on use"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment WILDFIRE = ArcanaAugments.register("wildfire",
         new ArcanaAugment("Charm of Wildfire", "wildfire", new ItemStack(Items.BLAZE_POWDER), MagicItems.CINDERS_CHARM,
         new String[]{"Adds an extra cinder for each level","Final Level increases cinder recharge rate"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Charm of Feasting
   public static final ArcanaAugment PICKY_EATER = ArcanaAugments.register("picky_eater",
         new ArcanaAugment("Picky Eater", "picky_eater", new ItemStack(Items.COOKED_PORKCHOP), MagicItems.FEASTING_CHARM,
         new String[]{"Makes the Charm select the most nutritious food"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment ENZYMES = ArcanaAugments.register("enzymes",
         new ArcanaAugment("Digestive Enzymes", "enzymes", new ItemStack(Items.FROGSPAWN), MagicItems.FEASTING_CHARM,
         new String[]{"Reduces cooldown between eating by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment GLUTTONY = ArcanaAugments.register("gluttony",
         new ArcanaAugment("Charm of Gluttony", "gluttony", new ItemStack(Items.GOLDEN_APPLE), MagicItems.FEASTING_CHARM,
         new String[]{"Food consumed gives better stats than normal","Higher levels gives a bigger boost"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Charm of Felidae
   public static final ArcanaAugment FELINE_GRACE = ArcanaAugments.register("feline_grace",
         new ArcanaAugment("Feline's Grace", "feline_grace", new ItemStack(Items.FEATHER), MagicItems.FELIDAE_CHARM,
         new String[]{"Stronger fall damage reduction for each level","Final Level negates all fall damage"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PANTHERA = ArcanaAugments.register("panthera",
         new ArcanaAugment("Charm of Panthera", "panthera", new ItemStack(Items.PHANTOM_MEMBRANE), MagicItems.FELIDAE_CHARM,
         new String[]{"The Charm's holder no longer spawns phantoms"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Charm of Leadership
   public static final ArcanaAugment INVIGORATION = ArcanaAugments.register("invigoration",
         new ArcanaAugment("Charm of Invigoration", "invigoration", new ItemStack(Items.DIAMOND_SWORD), MagicItems.LEADERSHIP_CHARM,
         new String[]{"Increases the Charm's buffs and radius for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Charm of Light
   public static final ArcanaAugment MOOD_LIGHTING = ArcanaAugments.register("mood_lighting",
         new ArcanaAugment("Mood Lighting", "mood_lighting", new ItemStack(Items.LANTERN), MagicItems.LIGHT_CHARM,
         new String[]{"Unlocks the ability to change the","threshold for light placement","Sneak Right Click to change modes","Right Click in threshold mode to configure"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment SELECTIVE_PLACEMENT = ArcanaAugments.register("selective_placement",
         new ArcanaAugment("Selective Placement", "selective_placement", new ItemStack(Items.TORCH), MagicItems.LIGHT_CHARM,
         new String[]{"Unlocks manual placement mode","Sneak Right Click to change modes","Right Click in manual mode to place"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment DIMMER_SWITCH = ArcanaAugments.register("dimmer_switch",
         new ArcanaAugment("Dimmer Switch", "dimmer_switch", new ItemStack(Items.REDSTONE_TORCH), MagicItems.LIGHT_CHARM,
         new String[]{"Unlocks the ability to change the","brightness of the lights being placed","Sneak Right Click to change modes","Right Click in brightness mode to configure"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment RADIANCE = ArcanaAugments.register("radiance",
         new ArcanaAugment("Charm of Radiance", "radiance", new ItemStack(Items.GLOWSTONE), MagicItems.LIGHT_CHARM,
         new String[]{"Unlocks the Radiant Nova ability","The ability lights up a large radius","Sneak Right Click to change modes","Right Click in Radiant Nova mode to use"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Charm of Magnetism
   public static final ArcanaAugment ELECTROMAGNET = ArcanaAugments.register("electromagnet",
         new ArcanaAugment("Electromagnet", "electromagnet", new ItemStack(Items.IRON_BLOCK), MagicItems.MAGNETISM_CHARM,
         new String[]{"Grants 3 blocks extra range on the","Charm's active ability for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment FERRITE_CORE = ArcanaAugments.register("ferrite_core",
         new ArcanaAugment("Ferrite Core", "ferrite_core", new ItemStack(Items.RAW_IRON), MagicItems.MAGNETISM_CHARM,
         new String[]{"Grants 1 block extra range on the","Charm's passive ability for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment NEODYMIUM = ArcanaAugments.register("neodymium",
         new ArcanaAugment("Charm of Neodymium", "neodymium", new ItemStack(Items.NETHERITE_INGOT), MagicItems.MAGNETISM_CHARM,
         new String[]{"The Charm's active ability now pulls","Metal Armor and Tools off of mobs","and disables all equipped shields briefly"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Ancient Dowsing Rod
   public static final ArcanaAugment ENHANCED_RESONANCE = ArcanaAugments.register("enhanced_resonance",
         new ArcanaAugment("Enhanced Resonance", "enhanced_resonance", new ItemStack(Items.BELL), MagicItems.ANCIENT_DOWSING_ROD,
         new String[]{"Increases the range of the Ancient","Dowsing Rod by 5 blocks for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment HARMONIC_FEEDBACK = ArcanaAugments.register("harmonic_feedback",
         new ArcanaAugment("Harmonic Feedback", "harmonic_feedback", new ItemStack(Items.BLAZE_POWDER), MagicItems.ANCIENT_DOWSING_ROD,
         new String[]{"Increases the particle duration of the","glowing debris by 5 seconds for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment SONIC_REABSORPTION = ArcanaAugments.register("sonic_reabsorption",
         new ArcanaAugment("Sonic Reabsorption", "sonic_reabsorption", new ItemStack(Items.GOAT_HORN), MagicItems.ANCIENT_DOWSING_ROD,
         new String[]{"Reduces the cooldown by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Arcane Tome / Generic
   public static final ArcanaAugment RESOURCEFUL = ArcanaAugments.register("resourceful",
         new ArcanaAugment("Resourceful", "resourceful", new ItemStack(Items.QUARTZ), MagicItems.ARCANE_TOME,
         new String[]{"Chance to not consume ingredient when crafting","5% chance per level per individual item","Does not apply to Magical Ingredients"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY,LEGENDARY,MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment RESOLVE = ArcanaAugments.register("resolve",
         new ArcanaAugment("Resolve", "resolve", new ItemStack(Items.DIAMOND), MagicItems.ARCANE_TOME,
         new String[]{"Grants 10 extra concentration for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment FOCUS = ArcanaAugments.register("focus",
         new ArcanaAugment("Focus", "focus", new ItemStack(Items.ENDER_EYE), MagicItems.ARCANE_TOME,
         new String[]{"Level 1 grants no concentration for items"," in Shulker Boxes in your Ender Chest","Level 2 extends to your whole Ender Chest","Level 3 extends to Shulker Boxes in your inventory"},
         new MagicRarity[]{LEGENDARY,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SKILL = ArcanaAugments.register("skill",
         new ArcanaAugment("Skill", "skill", new ItemStack(Items.BOOK), MagicItems.ARCANE_TOME,
         new String[]{"Chance to forge an item with some"," unlocked augments already applied","Chance Per Level: 25%/50%/75%/100%/200%","Final level attempts to add a second augment"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment ADAPTABILITY = ArcanaAugments.register("adaptability",
         new ArcanaAugment("Adaptability", "adaptability", new ItemStack(Items.AMETHYST_SHARD), MagicItems.ARCANE_TOME,
         new String[]{"Negates 1 point of the concentration penalty","from carrying items with augments beyond","your capacity per item for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Brain in a Jar
   public static final ArcanaAugment KNOWLEDGE_BANK = ArcanaAugments.register("knowledge_bank",
         new ArcanaAugment("Bank of Knowledge", "knowledge_bank", new ItemStack(Items.EXPERIENCE_BOTTLE), MagicItems.BRAIN_JAR,
         new String[]{"Gain interest on XP stored over time","Interest Rate per level: 0.2%/0.4%/0.6% per minute"},
         new MagicRarity[]{EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment TRADE_SCHOOL = ArcanaAugments.register("trade_school",
         new ArcanaAugment("Trade School", "trade_school", new ItemStack(Items.DIAMOND_PICKAXE), MagicItems.BRAIN_JAR,
         new String[]{"Increases repair efficiency for each level","Efficiency per level: 1.5x/2x/2.5x/3x/3.5x"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   public static final ArcanaAugment UNENDING_WISDOM = ArcanaAugments.register("unending_wisdom",
         new ArcanaAugment("Unending Wisdom", "unending_wisdom", new ItemStack(Items.ENCHANTED_BOOK), MagicItems.BRAIN_JAR,
         new String[]{"Increases the max XP capacity for each level","Increase per level: 2x/4x/6x/8x/10x"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   
   // Continuum Anchor
   public static final ArcanaAugment TEMPORAL_RELATIVITY = ArcanaAugments.register("temporal_relativity",
         new ArcanaAugment("Temporal Relativity", "temporal_relativity", new ItemStack(Items.CLOCK), MagicItems.CONTINUUM_ANCHOR,
         new String[]{"Gives a chance to not consume fuel each second","Chance Per Level: 5%/10%/15%/20%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Essence Egg
   public static final ArcanaAugment SOUL_SPLIT = ArcanaAugments.register("soul_split",
         new ArcanaAugment("Soul Split", "soul_split", new ItemStack(Items.EGG), MagicItems.ESSENCE_EGG,
         new String[]{"Chance to spawn two mobs per use","Chance per level: 10%/20%/30%/40%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment DETERMINED_SPIRIT = ArcanaAugments.register("determined_spirit",
         new ArcanaAugment("Determined Spirit", "determined_spirit", new ItemStack(Items.SOUL_LANTERN), MagicItems.ESSENCE_EGG,
         new String[]{"Chance to not consume a use","Chance per level: 10%/20%/30%/40%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment WILLING_CAPTIVE = ArcanaAugments.register("willing_captive",
         new ArcanaAugment("Willing Captive", "willing_captive", new ItemStack(Items.SPAWNER), MagicItems.ESSENCE_EGG,
         new String[]{"Converting a spawner type consumes","one less use per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Exotic Matter
   public static final ArcanaAugment TIME_IN_A_BOTTLE = ArcanaAugments.register("time_in_a_bottle",
         new ArcanaAugment("Time in a Bottle", "time_in_a_bottle", new ItemStack(Items.CLOCK), MagicItems.EXOTIC_MATTER,
         new String[]{"Augment to increase fuel content and refuel","Extra Fuel Per Level: 1.5x/2x/2.5x/3x/5x"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   
   // Fractal Sponge
   public static final ArcanaAugment SIERPINSKI = ArcanaAugments.register("sierpinski",
         new ArcanaAugment("Sierpinski's Sponge", "sierpinski", new ItemStack(Items.DIAMOND_BLOCK), MagicItems.FRACTAL_SPONGE,
         new String[]{"Increases the amount of fluid absorbed","by an additional 50% per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment MANDELBROT = ArcanaAugments.register("mandelbrot",
         new ArcanaAugment("Mandelbrot's Sponge", "mandelbrot", new ItemStack(Items.GOLD_BLOCK), MagicItems.FRACTAL_SPONGE,
         new String[]{"Increases the range of the Fractal Sponge","by an additional 2 blocks per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment CANTOR = ArcanaAugments.register("cantor",
         new ArcanaAugment("Cantor's Sponge", "cantor", new ItemStack(Items.EMERALD_BLOCK), MagicItems.FRACTAL_SPONGE,
         new String[]{"Causes the Fractal Sponge to pulse three times","Each pulse re-drains the area"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment HEAT_TREATMENT = ArcanaAugments.register("heat_treatment",
         new ArcanaAugment("Heat Treatment", "heat_treatment", new ItemStack(Items.LAVA_BUCKET), MagicItems.FRACTAL_SPONGE,
         new String[]{"Makes the Fractal Sponge no longer burn in lava or fire"},
         new MagicRarity[]{EMPOWERED}
   ));
   
   // Igneous Collider
   public static final ArcanaAugment CRYOGENIC_COOLING = ArcanaAugments.register("cryogenic_cooling",
         new ArcanaAugment("Cryogenic Cooling", "cryogenic_cooling", new ItemStack(Items.BLUE_ICE), MagicItems.IGNEOUS_COLLIDER,
         new String[]{"Allows the use of Blue Ice instead of water","The Blue Ice will not be consumed"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment THERMAL_EXPANSION = ArcanaAugments.register("thermal_expansion",
         new ArcanaAugment("Thermal Expansion", "thermal_expansion", new ItemStack(Items.OBSIDIAN), MagicItems.IGNEOUS_COLLIDER,
         new String[]{"Grants a 10% chance to not consume fluid per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment MAGMATIC_INJECTION = ArcanaAugments.register("magmatic_injection",
         new ArcanaAugment("Magmatic Injection", "magmatic_injection", new ItemStack(Items.MAGMA_BLOCK), MagicItems.IGNEOUS_COLLIDER,
         new String[]{"Decreases the cooldown by 2 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   
   // Levitation Harness
   public static final ArcanaAugment STURDY_CONSTRUCTION = ArcanaAugments.register("sturdy_construction",
         new ArcanaAugment("Sturdy Construction", "sturdy_construction", new ItemStack(Items.IRON_CHESTPLATE), MagicItems.LEVITATION_HARNESS,
         new String[]{"Grants a chance to not stall when taking damage","Chance per level: 15%/35%/50%"},
         new MagicRarity[]{LEGENDARY,LEGENDARY,LEGENDARY}
   ));
   public static final ArcanaAugment EMERGENCY_PROTOCOL = ArcanaAugments.register("emergency_protocol",
         new ArcanaAugment("Emergency Protocol", "emergency_protocol", new ItemStack(Items.FEATHER), MagicItems.LEVITATION_HARNESS,
         new String[]{"Gives levitation when the Harness stalls"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment FAST_REBOOT = ArcanaAugments.register("fast_reboot",
         new ArcanaAugment("Fast Reboot", "fast_reboot", new ItemStack(Items.EMERALD), MagicItems.LEVITATION_HARNESS,
         new String[]{"Reduces Harness reboot time by 2 seconds per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment HARNESS_RECYCLER = ArcanaAugments.register("harness_recycler",
         new ArcanaAugment("Soul Recycler", "harness_recycler", new ItemStack(Items.FIRE_CHARGE), MagicItems.LEVITATION_HARNESS,
         new String[]{"Grants a chance to not consume fuel","Chance per level: 5%/10%/15%/25%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Overflowing Quiver
   public static final ArcanaAugment ABUNDANT_AMMO = ArcanaAugments.register("abundant_ammo",
         new ArcanaAugment("Abundant Ammo", "abundant_ammo", new ItemStack(Items.SPECTRAL_ARROW), MagicItems.OVERFLOWING_QUIVER,
         new String[]{"Decreases time between arrow restocks","Decrease per level: 15/30/45/60/90 seconds","Linked with Runic Quiver's Duplication Runes"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment OVERFLOWING_BOTTOMLESS = ArcanaAugments.register("overflowing_bottomless",
         new ArcanaAugment("Bottomless Quiver", "overflowing_bottomless", new ItemStack(Items.ARROW), MagicItems.OVERFLOWING_QUIVER,
         new String[]{"Grants a chance to not consume an arrow","Chance per level: 5%/10%/15%/20%/30%","Linked with Runic Quiver's Bottomless Quiver"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Pearl of Recall
   public static final ArcanaAugment RECALL_ACCELERATION = ArcanaAugments.register("recall_acceleration",
         new ArcanaAugment("Dimensional Acceleration", "recall_acceleration", new ItemStack(Items.CLOCK), MagicItems.PEARL_OF_RECALL,
         new String[]{"Decreases the Pearl's cooldown time","Decrease per level: 1/2/4/6/8 minutes"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PHASE_DEFENSE = ArcanaAugments.register("phase_defense",
         new ArcanaAugment("Phase Defense", "phase_defense", new ItemStack(Items.DIAMOND_CHESTPLATE), MagicItems.PEARL_OF_RECALL,
         new String[]{"Grants a chance to not cancel when taking damage","while teleporting and negate the damage taken","Chance per level: 15%/35%/50%"},
         new MagicRarity[]{EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment CHRONO_TEAR = ArcanaAugments.register("chrono_tear",
         new ArcanaAugment("Chrono-Tear", "chrono_tear", new ItemStack(Items.END_PORTAL_FRAME), MagicItems.PEARL_OF_RECALL,
         new String[]{"Unlocks the ability to clear the saved location","Sneak Right Click to clear the saved destination"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Pickaxe of Pluto
   public static final ArcanaAugment HADES_REACH = ArcanaAugments.register("hades_reach",
         new ArcanaAugment("Hades's Reach", "hades_reach", new ItemStack(Items.DIAMOND_PICKAXE), MagicItems.PICKAXE_OF_PLUTO,
         new String[]{"Increases vein mine range and max blocks","Gives +2 range and +32 blocks per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment GREED = ArcanaAugments.register("greed",
         new ArcanaAugment("Greed of the Deep Dark", "greed", new ItemStack(Items.DIAMOND_BLOCK), MagicItems.PICKAXE_OF_PLUTO,
         new String[]{"Increases Fortune Level on vein mine","Fortune increase per level: +1/+3/+5"},
         new MagicRarity[]{EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment WARDENS_HASTE = ArcanaAugments.register("wardens_haste",
         new ArcanaAugment("Warden's Haste", "wardens_haste", new ItemStack(Items.GOLDEN_PICKAXE), MagicItems.PICKAXE_OF_PLUTO,
         new String[]{"Increases haste max and ramp speed","+1 Haste Maximum per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Runic Bow
   public static final ArcanaAugment BOW_STABILIZATION = ArcanaAugments.register("bow_stabilization",
         new ArcanaAugment("Stabilization Runes", "bow_stabilization", new ItemStack(Items.TARGET), MagicItems.RUNIC_BOW,
         new String[]{"Decreases the Runic Bow's firing randomness","Final level removes randomness entirely"},
         new MagicRarity[]{EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment BOW_ACCELERATION = ArcanaAugments.register("bow_acceleration",
         new ArcanaAugment("Acceleration Runes", "bow_acceleration", new ItemStack(Items.CLOCK), MagicItems.RUNIC_BOW,
         new String[]{"Runic Bow reaches max charge at lower draw strength","Max charge % per level: 90%/85%/80%/75%/50%"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment ENHANCED_INFINITY = ArcanaAugments.register("enhanced_infinity",
         new ArcanaAugment("Enhanced Infinity", "enhanced_infinity", new ItemStack(Items.SPECTRAL_ARROW), MagicItems.RUNIC_BOW,
         new String[]{"Applies infinity to tipped and spectral arrows"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Runic Quiver
   public static final ArcanaAugment QUIVER_DUPLICATION = ArcanaAugments.register("quiver_duplication",
         new ArcanaAugment("Duplication Runes", "quiver_duplication", new ItemStack(Items.TIPPED_ARROW), MagicItems.RUNIC_QUIVER,
         new String[]{"Decreases time between arrow restocks","Decrease per level: 5/10/20/30/45 seconds","Linked with Overflowing Quiver's Abundant Ammo"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment RUNIC_BOTTOMLESS = ArcanaAugments.register("runic_bottomless",
         new ArcanaAugment("Bottomless Quiver", "runic_bottomless", new ItemStack(Items.ARROW), MagicItems.RUNIC_QUIVER,
         new String[]{"Grants a chance to not consume an arrow","Chance per level: 5%/10%/15%/20%/30%","Linked with Overflowing Quiver's Bottomless Quiver"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAugment SHADOW_STRIDE = ArcanaAugments.register("shadow_stride",
         new ArcanaAugment("Shadow Stride", "shadow_stride", new ItemStack(Items.GLASS), MagicItems.SHADOW_STALKERS_GLAIVE,
         new String[]{"Gives the wielder invisibility when using"," the stalk or teleport abilities","Duration per level: 1/5/10 seconds"},
         new MagicRarity[]{LEGENDARY,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment PARANOIA = ArcanaAugments.register("paranoia",
         new ArcanaAugment("Paranoia", "paranoia", new ItemStack(Items.TINTED_GLASS), MagicItems.SHADOW_STALKERS_GLAIVE,
         new String[]{"Gives target blindness when using stalk ability","Duration per level: 1/2/5 seconds"},
         new MagicRarity[]{LEGENDARY,EXOTIC,LEGENDARY}
   ));
   
   // Shield of Fortitude
   public static final ArcanaAugment SHIELD_OF_FAITH = ArcanaAugments.register("shield_of_faith",
         new ArcanaAugment("Shield of Faith", "shield_of_faith", new ItemStack(Items.DIAMOND_CHESTPLATE), MagicItems.SHIELD_OF_FORTITUDE,
         new String[]{"Increases max absorption given per hit","Adds 1 heart to the max per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SHIELD_OF_RESILIENCE = ArcanaAugments.register("shield_of_resilience",
         new ArcanaAugment("Shield of Resilience", "shield_of_resilience", new ItemStack(Items.GOLDEN_APPLE), MagicItems.SHIELD_OF_FORTITUDE,
         new String[]{"Increases absorption duration by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SHIELD_BASH = ArcanaAugments.register("shield_bash",
         new ArcanaAugment("Shield Bash", "shield_bash", new ItemStack(Items.IRON_AXE), MagicItems.SHIELD_OF_FORTITUDE,
         new String[]{"Unlocks the Shield Bash ability","Attack with the Shield to consume absorption"," hearts and deal damage per heart consumed","This places the Shield on a brief cooldown"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Shulker Core
   public static final ArcanaAugment LEVITATIVE_REABSORPTION = ArcanaAugments.register("levitative_reabsorption",
         new ArcanaAugment("Levitative Reabsorption", "levitative_reabsorption", new ItemStack(Items.FEATHER), MagicItems.SHULKER_CORE,
         new String[]{"Unlocks the Cleanse Levitation ability","This can be activated through speed selection"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment SHULKER_RECYCLER = ArcanaAugments.register("shulker_recycler",
         new ArcanaAugment("Soul Recycler", "shulker_recycler", new ItemStack(Items.FIRE_CHARGE), MagicItems.SHULKER_CORE,
         new String[]{"Grants a 10% chance to not consume souls per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Sojourner's Boots
   public static final ArcanaAugment HIKING_BOOTS = ArcanaAugments.register("hiking_boots",
         new ArcanaAugment("Hiking Boots", "hiking_boots", new ItemStack(Items.GRAVEL), MagicItems.SOJOURNER_BOOTS,
         new String[]{"Increases step assist height to two blocks"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment MARATHON_RUNNER = ArcanaAugments.register("marathon_runner",
         new ArcanaAugment("Marathon Runner", "marathon_runner", new ItemStack(Items.FEATHER), MagicItems.SOJOURNER_BOOTS,
         new String[]{"Increases speed boost max by +50% per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SPRINTER = ArcanaAugments.register("sprinter",
         new ArcanaAugment("Sprinter", "sprinter", new ItemStack(Items.GOLDEN_BOOTS), MagicItems.SOJOURNER_BOOTS,
         new String[]{"Increases energy gain rate when sprinting","Increase per level: 2x/3x/4x/5x"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment JUGGERNAUT = ArcanaAugments.register("juggernaut",
         new ArcanaAugment("Juggernaut", "juggernaut", new ItemStack(Items.NETHERITE_HELMET), MagicItems.SOJOURNER_BOOTS,
         new String[]{"Hitting an enemy converts your speed boost to","extra damage and severely slows you and the target"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Soulstone
   public static final ArcanaAugment SOUL_REAPER = ArcanaAugments.register("soul_reaper",
         new ArcanaAugment("Soul Reaper", "soul_reaper", new ItemStack(Items.SOUL_LANTERN), MagicItems.SOULSTONE,
         new String[]{"Gain an additional soul per kill per level","Final level gives an extra 5 souls per kill"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SOUL_ANNIHILATION = ArcanaAugments.register("soul_annihilation",
         new ArcanaAugment("Soul Annihilation", "soul_annihilation", new ItemStack(Items.SOUL_CAMPFIRE), MagicItems.SOULSTONE,
         new String[]{"Allows a Soulstone to be unattuned","Throw the Soulstone into Soul Fire to un-attune it"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Spawner Harness
   public static final ArcanaAugment REINFORCED_CHASSIS = ArcanaAugments.register("reinforced_chassis",
         new ArcanaAugment("Reinforced Chassis", "reinforced_chassis", new ItemStack(Items.REINFORCED_DEEPSLATE), MagicItems.SPAWNER_HARNESS,
         new String[]{"Decreases Harness break chance by 2% per level","Final level grants zero chance of breaking"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SALVAGEABLE_FRAME = ArcanaAugments.register("salvageable_frame",
         new ArcanaAugment("Salvageable Frame", "salvageable_frame", new ItemStack(Items.NETHERITE_SCRAP), MagicItems.SPAWNER_HARNESS,
         new String[]{"Gives 1 to 4 Netherite Scrap per level","back when the Spawner Harness breaks"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Spawner Infuser
   public static final ArcanaAugment AUGMENTED_APPARATUS = ArcanaAugments.register("augmented_apparatus",
         new ArcanaAugment("Augmented Apparatus", "augmented_apparatus", new ItemStack(Items.SCULK_CATALYST), MagicItems.SPAWNER_INFUSER,
         new String[]{"Increases item to point conversion ratio","Ratio per level: 2/4/8/16/32"},
         new MagicRarity[]{EXOTIC,LEGENDARY,LEGENDARY,MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment SOUL_RESERVOIR = ArcanaAugments.register("soul_reservoir",
         new ArcanaAugment("Soul Reservoir", "soul_reservoir", new ItemStack(Items.SOUL_LANTERN), MagicItems.SPAWNER_INFUSER,
         new String[]{"Gives the Spawner Infuser extra point capacity","Extra capacity per level: 64/128/192/256/352"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SPIRIT_EMULATOR = ArcanaAugments.register("spirit_emulator",
         new ArcanaAugment("Spirit Emulator", "spirit_emulator", new ItemStack(Items.PLAYER_HEAD), MagicItems.SPAWNER_INFUSER,
         new String[]{"The highest level of player range now","lets the spawner function without a player"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Stasis Pearl
   public static final ArcanaAugment SPATIAL_FOLD = ArcanaAugments.register("spatial_fold",
         new ArcanaAugment("Spatial Fold", "spatial_fold", new ItemStack(Items.OBSIDIAN), MagicItems.STASIS_PEARL,
         new String[]{"Unlocks the ability to cancel a Stasis Pearl","Sneak Right Click to destroy the thrown pearl"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment STASIS_ACCELERATION = ArcanaAugments.register("stasis_acceleration",
         new ArcanaAugment("Temporal Acceleration", "stasis_acceleration", new ItemStack(Items.CLOCK), MagicItems.STASIS_PEARL,
         new String[]{"Decreases the cooldown by 10 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment STASIS_RECONSTRUCTION = ArcanaAugments.register("stasis_reconstruction",
         new ArcanaAugment("Reconstructive Teleport", "stasis_reconstruction", new ItemStack(Items.GOLDEN_APPLE), MagicItems.STASIS_PEARL,
         new String[]{"Grants regeneration and resistance on teleport","Higher levels increase the effect strength"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Telescoping Beacon
   public static final ArcanaAugment CAREFUL_RECONSTRUCTION = ArcanaAugments.register("careful_reconstruction",
         new ArcanaAugment("Careful Reconstruction", "careful_reconstruction", new ItemStack(Items.IRON_INGOT), MagicItems.TELESCOPING_BEACON,
         new String[]{"Allows the Beacon to remember its active effects"},
         new MagicRarity[]{EMPOWERED}
   ));
   
   // Wings of Zephyr
   public static final ArcanaAugment SCALES_OF_ENDERIA = ArcanaAugments.register("scales_of_enderia",
         new ArcanaAugment("Scales of Enderia", "scales_of_enderia", new ItemStack(Items.NETHERITE_CHESTPLATE), MagicItems.WINGS_OF_ZEPHYR,
         new String[]{"Unlocks the ability for the Wings to use","stored energy to mitigate all damage types"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment WING_BUFFET = ArcanaAugments.register("wing_buffet",
         new ArcanaAugment("Wing Buffet", "wing_buffet", new ItemStack(Items.FEATHER), MagicItems.WINGS_OF_ZEPHYR,
         new String[]{"Gives a chance to blow mobs back when hit","This ability costs energy for each enemy","Chance per level: 10%/20%/30%/40%/100%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // ~867 points for all augments, 325 total augment levels
   
   private static ArcanaAugment register(String id, ArcanaAugment augment){
      registry.put(id,augment);
      return augment;
   }
   
   public static List<ArcanaAugment> getAugmentsForItem(MagicItem item){
      ArrayList<ArcanaAugment> augments = new ArrayList<>();
      for(Map.Entry<String, ArcanaAugment> entry : registry.entrySet()){
         if(entry.getValue().getMagicItem().getId().equals(item.getId())) augments.add(entry.getValue());
      }
      return augments;
   }
   
   public static TreeMap<ArcanaAugment,Integer> getAugmentsOnItem(ItemStack item){
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      if(magicItem == null) return null;
      TreeMap<ArcanaAugment,Integer> map = new TreeMap<>();
      NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
      if(!magicTag.contains("augments")) return map;
      NbtCompound augmentTag = magicTag.getCompound("augments");
      
      for(String key : augmentTag.getKeys()){
         if(registry.containsKey(key)){
            map.put(registry.get(key),augmentTag.getInt(key));
         }
      }
      return map;
   }
   
   public static boolean isIncompatible(ItemStack item, String id){
      TreeMap<ArcanaAugment,Integer> curAugments = getAugmentsOnItem(item);
      if(curAugments == null) return true;
      if(!registry.containsKey(id)) return true;
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      ArcanaAugment augment = registry.get(id);
      if(!augment.getMagicItem().getId().equals(magicItem.getId())) return true;
      
      for(Map.Entry<ArcanaAugment, Integer> entry : curAugments.entrySet()){
         ArcanaAugment other = entry.getKey();
         if(id.equals(ArcanaAugments.WEB_OF_FIRE.id) && other.id.equals(ArcanaAugments.PYROBLAST.id)) return true;
         if(id.equals(ArcanaAugments.PYROBLAST.id) && other.id.equals(ArcanaAugments.WEB_OF_FIRE.id)) return true;
         
         if(id.equals(ArcanaAugments.ANTI_PERSONNEL.id) && other.id.equals(ArcanaAugments.BLAST_MINE.id)) return true;
         if(id.equals(ArcanaAugments.BLAST_MINE.id) && other.id.equals(ArcanaAugments.ANTI_PERSONNEL.id)) return true;
   
         if(id.equals(ArcanaAugments.AFTERSHOCK.id) && other.id.equals(ArcanaAugments.CHAIN_LIGHTNING.id)) return true;
         if(id.equals(ArcanaAugments.CHAIN_LIGHTNING.id) && other.id.equals(ArcanaAugments.AFTERSHOCK.id)) return true;
      }
      return false;
   }
   
   public static int getAugmentOnItem(ItemStack item, String id){
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      if(magicItem == null || !registry.containsKey(id)) return -2;
      NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
      if(!magicTag.contains("augments")) return -1;
      NbtCompound augmentTag = magicTag.getCompound("augments");
      if(augmentTag.contains(id)){
         return augmentTag.getInt(id);
      }
      return 0;
   }
   
   public static int getAugmentFromCompound(NbtCompound compound, String id){
      if(!registry.containsKey(id)) return -2;
      if(!compound.contains("augments")) return -1;
      NbtCompound augmentTag = compound.getCompound("augments");
      if(augmentTag.contains(id)){
         return augmentTag.getInt(id);
      }
      return 0;
   }
   
   // Applies Augment to Item, cannot down-level existing augments
   public static boolean applyAugment(ItemStack item, String id, int level){
      int curLevel = getAugmentOnItem(item,id);
      if(curLevel == -2) return false;
      if(!registry.containsKey(id)) return false;
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      ArcanaAugment augment = registry.get(id);
      if(!augment.getMagicItem().getId().equals(magicItem.getId())) return false;
      if(level > augment.getTiers().length || curLevel >= augment.getTiers().length) return false;
      if(isIncompatible(item,id)) return false;
      if(curLevel >= level) return false;
      
      NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
      if(curLevel == -1){
         magicTag.put("augments",new NbtCompound());
      }
      NbtCompound augmentTag = magicTag.getCompound("augments");
      augmentTag.putInt(id,level);
   
      if(magicItem instanceof ExoticMatter matter){
         matter.setFuel(item,matter.getMaxEnergy(item));
      }
      
      magicItem.redoAugmentLore(item);
      return true;
   }
}
