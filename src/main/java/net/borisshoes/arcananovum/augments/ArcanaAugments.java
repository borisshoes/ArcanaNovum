package net.borisshoes.arcananovum.augments;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.MagicItem;
import net.borisshoes.arcananovum.items.AlchemicalArbalest;
import net.borisshoes.arcananovum.items.ExoticMatter;
import net.borisshoes.arcananovum.items.NulMemento;
import net.borisshoes.arcananovum.items.WingsOfEnderia;
import net.borisshoes.arcananovum.utils.EnhancedStatUtils;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;

import java.util.*;

import static net.borisshoes.arcananovum.utils.MagicRarity.*;

public class ArcanaAugments {
   public static final HashMap<String, ArcanaAugment> registry = new HashMap<>();
   public static final HashMap<ArcanaAugment,String> linkedAugments = new HashMap<>();
   
   // Arcane Flak Arrows
   public static final ArcanaAugment AIRBURST = ArcanaAugments.register(
         new ArcanaAugment("Airburst", "airburst", new ItemStack(Items.FIREWORK_STAR), ArcanaRegistry.ARCANE_FLAK_ARROWS,
         new String[]{"Grants an extra 1.25 blocks burst radius per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Blink Arrows
   public static final ArcanaAugment PHASE_IN = ArcanaAugments.register(
         new ArcanaAugment("Phase In", "phase_in", new ItemStack(Items.ENDER_PEARL), ArcanaRegistry.BLINK_ARROWS,
         new String[]{"Grants brief resistance on teleport","Duration per level: 1/3/5 seconds"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Concussion Arrows
   public static final ArcanaAugment SHELLSHOCK = ArcanaAugments.register(
         new ArcanaAugment("Shellshock", "shellshock", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.CONCUSSION_ARROWS,
         new String[]{"Increases effect strength and duration per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Detonation Arrows
   public static final ArcanaAugment ANTI_PERSONNEL = ArcanaAugments.register(
         new ArcanaAugment("Anti-Personnel", "anti_personnel", new ItemStack(Items.ROTTEN_FLESH), ArcanaRegistry.DETONATION_ARROWS,
         new String[]{"Increases damage to creatures per level","Final level grants no terrain damage","Mutually Exclusive with Blast Mine"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment BLAST_MINE = ArcanaAugments.register(
         new ArcanaAugment("Blast Mine", "blast_mine", new ItemStack(Items.COBBLESTONE), ArcanaRegistry.DETONATION_ARROWS,
         new String[]{"Increases terrain and lowers mob damage","Final level grants no creature damage","Mutually Exclusive with Anti-Personnel"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Expulsion Arrows
   public static final ArcanaAugment REPULSION = ArcanaAugments.register(
         new ArcanaAugment("Repulsion", "repulsion", new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS), ArcanaRegistry.EXPULSION_ARROWS,
         new String[]{"Increases expulsion range by 1.5 blocks per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Graviton Arrows
   public static final ArcanaAugment GRAVITY_WELL = ArcanaAugments.register(
         new ArcanaAugment("Gravity Well", "gravity_well", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.GRAVITON_ARROWS,
         new String[]{"Increases attraction range by 1 block per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Photonic Arrows
   public static final ArcanaAugment PRISMATIC_ALIGNMENT = ArcanaAugments.register(
         new ArcanaAugment("Prismatic Alignment", "prismatic_alignment", new ItemStack(Items.BEACON), ArcanaRegistry.PHOTONIC_ARROWS,
         new String[]{"Increases damage for each enemy pierced","Higher levels increase damage more","Final level gives extra base damage"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Siphoning Arrows
   public static final ArcanaAugment OVERHEAL = ArcanaAugments.register(
         new ArcanaAugment("Overheal", "overheal", PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.HEALING), ArcanaRegistry.SIPHONING_ARROWS,
         new String[]{"Health siphoned above max becomes absorption","Max Overheal per Level: 2.5/5/10 hearts"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Smoke Arrows
   public static final ArcanaAugment TEAR_GAS = ArcanaAugments.register(
         new ArcanaAugment("Tear Gas", "tear_gas", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), ArcanaRegistry.SMOKE_ARROWS,
         new String[]{"Amplifies debilitating effects","Higher levels increase effect duration"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Storm Arrows
   public static final ArcanaAugment STORM_STABILIZATION = ArcanaAugments.register(
         new ArcanaAugment("Runic Stabilization", "storm_stabilization", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.STORM_ARROWS,
         new String[]{"Increases lightning chance per level","Final level grants guaranteed success"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment CHAIN_LIGHTNING = ArcanaAugments.register(
         new ArcanaAugment("Chain Lightning", "chain_lightning", new ItemStack(Items.PRISMARINE_CRYSTALS), ArcanaRegistry.STORM_ARROWS,
         new String[]{"Striking an enemy chains a shock to","an additional creature for each level","Effect occurs independent of lightning","Mutually Exclusive with Aftershock"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   public static final ArcanaAugment AFTERSHOCK = ArcanaAugments.register(
         new ArcanaAugment("Aftershock", "aftershock", new ItemStack(Items.LIGHTNING_ROD), ArcanaRegistry.STORM_ARROWS,
         new String[]{"Lightning strikes charge the ground","Higher levels increase charge duration","Final Level increases damage and size","Mutually Exclusive with Chain Lightning"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Tether Arrows
   public static final ArcanaAugment QUICK_RELEASE = ArcanaAugments.register(
         new ArcanaAugment("Quick Release", "quick_release", new ItemStack(Items.SHEARS), ArcanaRegistry.TETHER_ARROWS,
         new String[]{"Sneaking cancels all in-flight Tether Arrows"," from activating the tether on landing"},
         new MagicRarity[]{EMPOWERED}
   ));
   
   // Charm of Cinders
   public static final ArcanaAugment PYROBLAST = ArcanaAugments.register(
         new ArcanaAugment("Pyroblast", "pyroblast", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.CINDERS_CHARM,
         new String[]{"Cone of Flame becomes a powerful Fireball","The fireball does not damage terrain","Higher levels cause higher radius and damage","Mutually Exclusive with Web of Fire"},
         new MagicRarity[]{MYTHICAL,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment WEB_OF_FIRE = ArcanaAugments.register(
         new ArcanaAugment("Web of Fire", "web_of_fire", new ItemStack(Items.FIRE_CORAL), ArcanaRegistry.CINDERS_CHARM,
         new String[]{"Cone of Flame becomes an AoE precision"," strike on creatures around you.","Higher levels adds more targets and damage","Mutually Exclusive with Pyroblast"},
         new MagicRarity[]{MYTHICAL,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment CREMATION = ArcanaAugments.register(
         new ArcanaAugment("Cremation", "cremation", new ItemStack(Items.SOUL_CAMPFIRE), ArcanaRegistry.CINDERS_CHARM,
         new String[]{"The Charm's Flames become blue soul-flame","Soul-flame does double damage to creatures","The Charm now uses cinders to negate fire damage"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment FIRESTARTER = ArcanaAugments.register(
         new ArcanaAugment("Firestarter", "firestarter", new ItemStack(Items.FLINT_AND_STEEL), ArcanaRegistry.CINDERS_CHARM,
         new String[]{"The Charm's Left Click abilities no","longer consumes cinders on use"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment WILDFIRE = ArcanaAugments.register(
         new ArcanaAugment("Charm of Wildfire", "wildfire", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.CINDERS_CHARM,
         new String[]{"Adds an extra cinder for each level","Final Level increases cinder recharge rate"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Charm of Feasting
   public static final ArcanaAugment PICKY_EATER = ArcanaAugments.register(
         new ArcanaAugment("Picky Eater", "picky_eater", new ItemStack(Items.COOKED_PORKCHOP), ArcanaRegistry.FEASTING_CHARM,
         new String[]{"Makes the Charm select the most nutritious food"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment ENZYMES = ArcanaAugments.register(
         new ArcanaAugment("Digestive Enzymes", "enzymes", new ItemStack(Items.FROGSPAWN), ArcanaRegistry.FEASTING_CHARM,
         new String[]{"Reduces cooldown between eating by 5 seconds per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment GLUTTONY = ArcanaAugments.register(
         new ArcanaAugment("Charm of Gluttony", "gluttony", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.FEASTING_CHARM,
         new String[]{"Food consumed gives better stats than normal","Higher levels gives a bigger boost"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Charm of Felidae
   public static final ArcanaAugment FELINE_GRACE = ArcanaAugments.register(
         new ArcanaAugment("Feline's Grace", "feline_grace", new ItemStack(Items.FEATHER), ArcanaRegistry.FELIDAE_CHARM,
         new String[]{"Stronger fall damage reduction for each level","Final Level negates all fall damage"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PANTHERA = ArcanaAugments.register(
         new ArcanaAugment("Charm of Panthera", "panthera", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.FELIDAE_CHARM,
         new String[]{"The Charm's holder no longer spawns phantoms"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Charm of Leadership
   public static final ArcanaAugment INVIGORATION = ArcanaAugments.register(
         new ArcanaAugment("Charm of Invigoration", "invigoration", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.LEADERSHIP_CHARM,
         new String[]{"Increases the Charm's buffs and radius for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Charm of Light
   public static final ArcanaAugment MOOD_LIGHTING = ArcanaAugments.register(
         new ArcanaAugment("Mood Lighting", "mood_lighting", new ItemStack(Items.LANTERN), ArcanaRegistry.LIGHT_CHARM,
         new String[]{"Unlocks the ability to change the","threshold for light placement","Sneak Right Click to change modes","Right Click in threshold mode to configure"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment SELECTIVE_PLACEMENT = ArcanaAugments.register(
         new ArcanaAugment("Selective Placement", "selective_placement", new ItemStack(Items.TORCH), ArcanaRegistry.LIGHT_CHARM,
         new String[]{"Unlocks manual placement mode","Sneak Right Click to change modes","Right Click in manual mode to place"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment DIMMER_SWITCH = ArcanaAugments.register(
         new ArcanaAugment("Dimmer Switch", "dimmer_switch", new ItemStack(Items.REDSTONE_TORCH), ArcanaRegistry.LIGHT_CHARM,
         new String[]{"Unlocks the ability to change the","brightness of the lights being placed","Sneak Right Click to change modes","Right Click in brightness mode to configure"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment RADIANCE = ArcanaAugments.register(
         new ArcanaAugment("Charm of Radiance", "radiance", new ItemStack(Items.GLOWSTONE), ArcanaRegistry.LIGHT_CHARM,
         new String[]{"Unlocks the Radiant Nova ability","The ability lights up a large radius","Sneak Right Click to change modes","Right Click in Radiant Nova mode to use"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Charm of Magnetism
   public static final ArcanaAugment ELECTROMAGNET = ArcanaAugments.register(
         new ArcanaAugment("Electromagnet", "electromagnet", new ItemStack(Items.IRON_BLOCK), ArcanaRegistry.MAGNETISM_CHARM,
         new String[]{"Grants 3 blocks extra range on the","Charm's Active Ability for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment FERRITE_CORE = ArcanaAugments.register(
         new ArcanaAugment("Ferrite Core", "ferrite_core", new ItemStack(Items.RAW_IRON), ArcanaRegistry.MAGNETISM_CHARM,
         new String[]{"Grants 1 block extra range on the","Charm's Passive Ability for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment FARADAY_CAGE = ArcanaAugments.register(
         new ArcanaAugment("Faraday Cage", "faraday_cage", new ItemStack(Items.HOPPER), ArcanaRegistry.MAGNETISM_CHARM,
         new String[]{"Allows you to filter items attracted","Sneak Right Click with an item in"," your offhand to add it to the filter", "Sneak Right Click with the Charm in", " your offhand to reset the filter"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment POLARITY_REVERSAL = ArcanaAugments.register(
         new ArcanaAugment("Polarity Reversal", "polarity_reversal", new ItemStack(Items.GOLD_INGOT), ArcanaRegistry.MAGNETISM_CHARM,
         new String[]{"Unlocks a mode to repel items when"," the Charm's Passive Ability is active","Sneak Right Click to switch modes"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment NEODYMIUM = ArcanaAugments.register(
         new ArcanaAugment("Charm of Neodymium", "neodymium", new ItemStack(Items.NETHERITE_INGOT), ArcanaRegistry.MAGNETISM_CHARM,
         new String[]{"The Charm's Active Ability now pulls"," Metal Armor and Tools off of mobs"," and disables all equipped shields briefly"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Ancient Dowsing Rod
   public static final ArcanaAugment ENHANCED_RESONANCE = ArcanaAugments.register(
         new ArcanaAugment("Enhanced Resonance", "enhanced_resonance", new ItemStack(Items.BELL), ArcanaRegistry.ANCIENT_DOWSING_ROD,
         new String[]{"Increases the range of the Ancient","Dowsing Rod by 5 blocks for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment HARMONIC_FEEDBACK = ArcanaAugments.register(
         new ArcanaAugment("Harmonic Feedback", "harmonic_feedback", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.ANCIENT_DOWSING_ROD,
         new String[]{"Increases the particle duration of the","glowing debris by 5 seconds for each level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   public static final ArcanaAugment SONIC_REABSORPTION = ArcanaAugments.register(
         new ArcanaAugment("Sonic Reabsorption", "sonic_reabsorption", new ItemStack(Items.GOAT_HORN), ArcanaRegistry.ANCIENT_DOWSING_ROD,
         new String[]{"Reduces the cooldown by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Arcane Tome / Generic
   public static final ArcanaAugment RESOLVE = ArcanaAugments.register(
         new ArcanaAugment("Resolve", "resolve", new ItemStack(Items.DIAMOND), ArcanaRegistry.ARCANE_TOME,
         new String[]{"Grants 10 extra concentration for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment FOCUS = ArcanaAugments.register(
         new ArcanaAugment("Focus", "focus", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.ARCANE_TOME,
         new String[]{"Level 1 grants no concentration for items"," in Shulker Boxes in your Ender Chest","Level 2 extends to your whole Ender Chest","Level 3 extends to Shulker Boxes in your inventory"},
         new MagicRarity[]{LEGENDARY,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment ADAPTABILITY = ArcanaAugments.register(
         new ArcanaAugment("Adaptability", "adaptability", new ItemStack(Items.AMETHYST_SHARD), ArcanaRegistry.ARCANE_TOME,
         new String[]{"Negates 1 point of the concentration penalty","from carrying items with augments beyond","your capacity per item for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Brain in a Jar
   public static final ArcanaAugment KNOWLEDGE_BANK = ArcanaAugments.register(
         new ArcanaAugment("Bank of Knowledge", "knowledge_bank", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.BRAIN_JAR,
         new String[]{"Gain interest on XP stored over time","Interest Rate per level: 0.2%/0.4%/0.6% per minute"},
         new MagicRarity[]{EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment TRADE_SCHOOL = ArcanaAugments.register(
         new ArcanaAugment("Trade School", "trade_school", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.BRAIN_JAR,
         new String[]{"Increases repair efficiency for each level","Efficiency per level: 1.5x/2x/2.5x/3x/3.5x"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   public static final ArcanaAugment UNENDING_WISDOM = ArcanaAugments.register(
         new ArcanaAugment("Unending Wisdom", "unending_wisdom", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.BRAIN_JAR,
         new String[]{"Increases the max XP capacity for each level","Increase per level: 2x/4x/6x/8x/10x"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   
   // Continuum Anchor
   public static final ArcanaAugment TEMPORAL_RELATIVITY = ArcanaAugments.register(
         new ArcanaAugment("Temporal Relativity", "temporal_relativity", new ItemStack(Items.CLOCK), ArcanaRegistry.CONTINUUM_ANCHOR,
         new String[]{"Gives a chance to not consume fuel each second","Chance Per Level: 5%/10%/15%/20%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Essence Egg
   public static final ArcanaAugment SOUL_SPLIT = ArcanaAugments.register(
         new ArcanaAugment("Soul Split", "soul_split", new ItemStack(Items.EGG), ArcanaRegistry.ESSENCE_EGG,
         new String[]{"Chance to spawn two mobs per use","Chance per level: 10%/20%/30%/40%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment DETERMINED_SPIRIT = ArcanaAugments.register(
         new ArcanaAugment("Determined Spirit", "determined_spirit", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.ESSENCE_EGG,
         new String[]{"Chance to not consume a use","Chance per level: 10%/20%/30%/40%/50%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment WILLING_CAPTIVE = ArcanaAugments.register(
         new ArcanaAugment("Willing Captive", "willing_captive", new ItemStack(Items.SPAWNER), ArcanaRegistry.ESSENCE_EGG,
         new String[]{"Converting a spawner type consumes","one less use per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Exotic Matter
   public static final ArcanaAugment TIME_IN_A_BOTTLE = ArcanaAugments.register(
         new ArcanaAugment("Time in a Bottle", "time_in_a_bottle", new ItemStack(Items.CLOCK), ArcanaRegistry.EXOTIC_MATTER,
         new String[]{"Augment to increase fuel content and refuel","Extra Fuel Per Level: 1.5x/2x/2.5x/3x/5x"},
         new MagicRarity[]{MUNDANE,MUNDANE,MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Fractal Sponge
   public static final ArcanaAugment SIERPINSKI = ArcanaAugments.register(
         new ArcanaAugment("Sierpinski's Sponge", "sierpinski", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
         new String[]{"Increases the amount of fluid absorbed","by an additional 50% per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment MANDELBROT = ArcanaAugments.register(
         new ArcanaAugment("Mandelbrot's Sponge", "mandelbrot", new ItemStack(Items.GOLD_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
         new String[]{"Increases the range of the Fractal Sponge","by an additional 2 blocks per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment CANTOR = ArcanaAugments.register(
         new ArcanaAugment("Cantor's Sponge", "cantor", new ItemStack(Items.EMERALD_BLOCK), ArcanaRegistry.FRACTAL_SPONGE,
         new String[]{"Causes the Fractal Sponge to pulse three times","Each pulse re-drains the area"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Igneous Collider
   public static final ArcanaAugment CRYOGENIC_COOLING = ArcanaAugments.register(
         new ArcanaAugment("Cryogenic Cooling", "cryogenic_cooling", new ItemStack(Items.BLUE_ICE), ArcanaRegistry.IGNEOUS_COLLIDER,
         new String[]{"Allows the use of Blue Ice instead of water","The Blue Ice will not be consumed"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment THERMAL_EXPANSION = ArcanaAugments.register(
         new ArcanaAugment("Thermal Expansion", "thermal_expansion", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER,
         new String[]{"Grants a 10% chance to not consume fluid per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment MAGMATIC_INJECTION = ArcanaAugments.register(
         new ArcanaAugment("Magmatic Injection", "magmatic_injection", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.IGNEOUS_COLLIDER,
         new String[]{"Decreases the cooldown by 2 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   
   // Levitation Harness
   public static final ArcanaAugment STURDY_CONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("Sturdy Construction", "sturdy_construction", new ItemStack(Items.IRON_CHESTPLATE), ArcanaRegistry.LEVITATION_HARNESS,
         new String[]{"Grants a chance to not stall when taking damage","Chance per level: 15%/35%/50%"},
         new MagicRarity[]{LEGENDARY,LEGENDARY,LEGENDARY}
   ));
   public static final ArcanaAugment EMERGENCY_PROTOCOL = ArcanaAugments.register(
         new ArcanaAugment("Emergency Protocol", "emergency_protocol", new ItemStack(Items.FEATHER), ArcanaRegistry.LEVITATION_HARNESS,
         new String[]{"Gives levitation when the Harness stalls"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment FAST_REBOOT = ArcanaAugments.register(
         new ArcanaAugment("Fast Reboot", "fast_reboot", new ItemStack(Items.EMERALD), ArcanaRegistry.LEVITATION_HARNESS,
         new String[]{"Reduces Harness reboot time by 2 seconds per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment HARNESS_RECYCLER = ArcanaAugments.register(
         new ArcanaAugment("Soul Recycler", "harness_recycler", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.LEVITATION_HARNESS,
         new String[]{"Grants a chance to not consume fuel","Chance per level: 10%/25%/50%","Linked with the Shulker Core's Soul Recycler"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Nul Memento
   public static final ArcanaAugment DEATHS_CHAMPION = ArcanaAugments.register(
         new ArcanaAugment("Death's Champion", "deaths_champion", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.NUL_MEMENTO,
         new String[]{"Gives the Nul Memento max enhanced stats"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment TEMPO_MORTUUS = ArcanaAugments.register(
         new ArcanaAugment("Tempo Mortuus", "tempo_mortuus", new ItemStack(Items.CLOCK), ArcanaRegistry.NUL_MEMENTO,
         new String[]{"Reduces the cooldown of the Memento's "," Death Ward by 10 minutes per level"},
         new MagicRarity[]{LEGENDARY,MYTHICAL}
   ));
   
   // Overflowing Quiver
   public static final ArcanaAugment ABUNDANT_AMMO = ArcanaAugments.register(
         new ArcanaAugment("Abundant Ammo", "abundant_ammo", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.OVERFLOWING_QUIVER,
         new String[]{"Decreases time between arrow restocks","Decrease per level: 15/30/45/60/90 seconds","Linked with Runic Quiver's Duplication Runes"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment OVERFLOWING_BOTTOMLESS = ArcanaAugments.register(
         new ArcanaAugment("Bottomless Quiver", "overflowing_bottomless", new ItemStack(Items.ARROW), ArcanaRegistry.OVERFLOWING_QUIVER,
         new String[]{"Grants a chance to not consume an arrow","Chance per level: 5%/10%/15%/20%/30%","Linked with Runic Quiver's Bottomless Quiver"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Pearl of Recall
   public static final ArcanaAugment RECALL_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("Dimensional Acceleration", "recall_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.PEARL_OF_RECALL,
         new String[]{"Decreases the Pearl's cooldown time","Decrease per level: 1/2/4/6/8 minutes"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PHASE_DEFENSE = ArcanaAugments.register(
         new ArcanaAugment("Phase Defense", "phase_defense", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.PEARL_OF_RECALL,
         new String[]{"Grants a chance to not cancel when taking damage","while teleporting and negate the damage taken","Chance per level: 15%/35%/50%"},
         new MagicRarity[]{EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment CHRONO_TEAR = ArcanaAugments.register(
         new ArcanaAugment("Chrono-Tear", "chrono_tear", new ItemStack(Items.END_PORTAL_FRAME), ArcanaRegistry.PEARL_OF_RECALL,
         new String[]{"Unlocks the ability to clear the saved location","Sneak Right Click to clear the saved destination"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Pickaxe of Ceptyus
   public static final ArcanaAugment WITH_THE_DEPTHS = ArcanaAugments.register(
         new ArcanaAugment("One With The Depths", "with_the_depths", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
         new String[]{"Increases vein mine range and max blocks","Gives +2 range and +32 blocks per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment GREED = ArcanaAugments.register(
         new ArcanaAugment("Greed of the Deep Dark", "greed", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
         new String[]{"Increases Fortune Level on vein mine","Fortune increase per level: +1/+3/+5"},
         new MagicRarity[]{EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment WARDENS_HASTE = ArcanaAugments.register(
         new ArcanaAugment("Warden's Haste", "wardens_haste", new ItemStack(Items.GOLDEN_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS,
         new String[]{"Increases haste max and ramp speed","+1 Haste Maximum per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Runic Bow
   public static final ArcanaAugment BOW_STABILIZATION = ArcanaAugments.register(
         new ArcanaAugment("Stabilization Runes", "bow_stabilization", new ItemStack(Items.TARGET), ArcanaRegistry.RUNIC_BOW,
         new String[]{"Decreases the Runic Bow's firing randomness","Final level removes randomness entirely"},
         new MagicRarity[]{EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment BOW_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("Acceleration Runes", "bow_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.RUNIC_BOW,
         new String[]{"Runic Bow reaches max charge at lower draw strength","Max charge % per level: 90%/85%/80%/75%/50%"},
         new MagicRarity[]{EXOTIC,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment ENHANCED_INFINITY = ArcanaAugments.register(
         new ArcanaAugment("Enhanced Infinity", "enhanced_infinity", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.RUNIC_BOW,
         new String[]{"Applies infinity to tipped and spectral arrows"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Runic Quiver
   public static final ArcanaAugment QUIVER_DUPLICATION = ArcanaAugments.register(
         new ArcanaAugment("Duplication Runes", "quiver_duplication", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RUNIC_QUIVER,
         new String[]{"Decreases time between arrow restocks","Decrease per level: 5/10/20/30/45 seconds","Linked with Overflowing Quiver's Abundant Ammo"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment RUNIC_BOTTOMLESS = ArcanaAugments.register(
         new ArcanaAugment("Bottomless Quiver", "runic_bottomless", new ItemStack(Items.ARROW), ArcanaRegistry.RUNIC_QUIVER,
         new String[]{"Grants a chance to not consume an arrow","Chance per level: 5%/10%/15%/20%/30%","Linked with Overflowing Quiver's Bottomless Quiver"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SHUNT_RUNES = ArcanaAugments.register(
         new ArcanaAugment("Shunt Runes", "shunt_runes", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.RUNIC_QUIVER,
         new String[]{"Arrows in the Quiver take 1/4 concentration"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAugment SHADOW_STRIDE = ArcanaAugments.register(
         new ArcanaAugment("Shadow Stride", "shadow_stride", new ItemStack(Items.GLASS), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
         new String[]{"Gives the wielder Greater Invisibility"," when using the stalk or teleport abilities","Duration per level: 1/2/5 seconds"},
         new MagicRarity[]{LEGENDARY,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PARANOIA = ArcanaAugments.register(
         new ArcanaAugment("Paranoia", "paranoia", new ItemStack(Items.TINTED_GLASS), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
         new String[]{"Gives target Greater Blindness when", " using the stalk ability","Duration per level: 1/2/5 seconds"},
         new MagicRarity[]{LEGENDARY,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment BLOODLETTER = ArcanaAugments.register(
         new ArcanaAugment("Blood-letter", "bloodletter", new ItemStack(Items.REDSTONE), ArcanaRegistry.SHADOW_STALKERS_GLAIVE,
         new String[]{"The Glaive consumes a heart of your health"," to periodically self-recharge above 1 charge"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Shield of Fortitude
   public static final ArcanaAugment SHIELD_OF_FAITH = ArcanaAugments.register(
         new ArcanaAugment("Shield of Faith", "shield_of_faith", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
         new String[]{"Increases max absorption given per hit","Adds 1 heart to the max per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SHIELD_OF_RESILIENCE = ArcanaAugments.register(
         new ArcanaAugment("Shield of Resilience", "shield_of_resilience", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
         new String[]{"Increases absorption duration by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SHIELD_BASH = ArcanaAugments.register(
         new ArcanaAugment("Shield Bash", "shield_bash", new ItemStack(Items.IRON_AXE), ArcanaRegistry.SHIELD_OF_FORTITUDE,
         new String[]{"Any attack made with more than 10 hearts of",
               " absorption slows the target and causes them",
               " to take increased damage for 5 seconds.",
               "This consumes all but 10 absorption hearts",
               " and places the Shield on a brief cooldown.",
               "More absorption causes more damage amplification",
               " and a longer amplification duration."},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Shulker Core
   public static final ArcanaAugment LEVITATIVE_REABSORPTION = ArcanaAugments.register(
         new ArcanaAugment("Levitative Reabsorption", "levitative_reabsorption", new ItemStack(Items.FEATHER), ArcanaRegistry.SHULKER_CORE,
         new String[]{"Unlocks the Cleanse Levitation ability","This can be activated through speed selection"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment SHULKER_RECYCLER = ArcanaAugments.register(
         new ArcanaAugment("Soul Recycler", "shulker_recycler", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.SHULKER_CORE,
         new String[]{"Grants a chance to not consume souls","Chance per level: 10%/25%/50%","Linked with the Levitation Harness's Soul Recycler"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Sojourner's Boots
   public static final ArcanaAugment HIKING_BOOTS = ArcanaAugments.register(
         new ArcanaAugment("Hiking Boots", "hiking_boots", new ItemStack(Items.GRAVEL), ArcanaRegistry.SOJOURNER_BOOTS,
         new String[]{"Increases step assist height to two blocks"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment MARATHON_RUNNER = ArcanaAugments.register(
         new ArcanaAugment("Marathon Runner", "marathon_runner", new ItemStack(Items.FEATHER), ArcanaRegistry.SOJOURNER_BOOTS,
         new String[]{"Increases speed boost max by +50% per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SPRINTER = ArcanaAugments.register(
         new ArcanaAugment("Sprinter", "sprinter", new ItemStack(Items.GOLDEN_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS,
         new String[]{"Increases energy gain rate when sprinting","Increase per level: 2x/3x"},
         new MagicRarity[]{EMPOWERED,LEGENDARY}
   ));
   public static final ArcanaAugment JUGGERNAUT = ArcanaAugments.register(
         new ArcanaAugment("Juggernaut", "juggernaut", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.SOJOURNER_BOOTS,
         new String[]{"Hitting an enemy with at least 200 energy",
               " slows the target and causes them to take",
               " increased damage for 5 seconds.",
               "This consumes your speed boost"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Soulstone
   public static final ArcanaAugment SOUL_REAPER = ArcanaAugments.register(
         new ArcanaAugment("Soul Reaper", "soul_reaper", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.SOULSTONE,
         new String[]{"Gain an additional soul per kill per level","Final level gives an extra 5 souls per kill"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SOUL_ANNIHILATION = ArcanaAugments.register(
         new ArcanaAugment("Soul Annihilation", "soul_annihilation", new ItemStack(Items.SOUL_CAMPFIRE), ArcanaRegistry.SOULSTONE,
         new String[]{"Allows a Soulstone to be unattuned by"," throwing the Soulstone into Soul Fire"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Spawner Harness
   public static final ArcanaAugment REINFORCED_CHASSIS = ArcanaAugments.register(
         new ArcanaAugment("Reinforced Chassis", "reinforced_chassis", new ItemStack(Items.REINFORCED_DEEPSLATE), ArcanaRegistry.SPAWNER_HARNESS,
         new String[]{"Decreases Harness break chance by 2% per level","Final level grants zero chance of breaking"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SALVAGEABLE_FRAME = ArcanaAugments.register(
         new ArcanaAugment("Salvageable Frame", "salvageable_frame", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.SPAWNER_HARNESS,
         new String[]{"Gives 8 Netherite Scrap back when the Spawner Harness breaks"},
         new MagicRarity[]{MUNDANE}
   ));
   
   // Spawner Infuser
   public static final ArcanaAugment AUGMENTED_APPARATUS = ArcanaAugments.register(
         new ArcanaAugment("Augmented Apparatus", "augmented_apparatus", new ItemStack(Items.SCULK_CATALYST), ArcanaRegistry.SPAWNER_INFUSER,
         new String[]{"Increases item to point conversion ratio","Ratio per level: 2/4/8/16/32"},
         new MagicRarity[]{EXOTIC,LEGENDARY,LEGENDARY,MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment SOUL_RESERVOIR = ArcanaAugments.register(
         new ArcanaAugment("Soul Reservoir", "soul_reservoir", new ItemStack(Items.SOUL_LANTERN), ArcanaRegistry.SPAWNER_INFUSER,
         new String[]{"Gives the Spawner Infuser extra point capacity","Extra capacity per level: 64/128/192/256/352"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment SPIRIT_EMULATOR = ArcanaAugments.register(
         new ArcanaAugment("Spirit Emulator", "spirit_emulator", new ItemStack(Items.PLAYER_HEAD), ArcanaRegistry.SPAWNER_INFUSER,
         new String[]{"The highest level of player range now","lets the spawner function without a player"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Stasis Pearl
   public static final ArcanaAugment SPATIAL_FOLD = ArcanaAugments.register(
         new ArcanaAugment("Spatial Fold", "spatial_fold", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.STASIS_PEARL,
         new String[]{"Unlocks the ability to cancel a Stasis Pearl","Sneak Right Click to destroy the thrown pearl"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment STASIS_ACCELERATION = ArcanaAugments.register(
         new ArcanaAugment("Temporal Acceleration", "stasis_acceleration", new ItemStack(Items.CLOCK), ArcanaRegistry.STASIS_PEARL,
         new String[]{"Decreases the cooldown by 10 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment STASIS_RECONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("Reconstructive Teleport", "stasis_reconstruction", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.STASIS_PEARL,
         new String[]{"Grants regeneration and resistance on teleport","Higher levels increase the effect strength"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Telescoping Beacon
   public static final ArcanaAugment CAREFUL_RECONSTRUCTION = ArcanaAugments.register(
         new ArcanaAugment("Careful Reconstruction", "careful_reconstruction", new ItemStack(Items.IRON_INGOT), ArcanaRegistry.TELESCOPING_BEACON,
         new String[]{"Allows the Beacon to remember its active effects"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment MINING_LASER = ArcanaAugments.register(
         new ArcanaAugment("Mining Laser", "mining_laser", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.TELESCOPING_BEACON,
         new String[]{"The Beacon slowly drills a hole to the surface"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Wings of Enderia
   public static final ArcanaAugment SCALES_OF_THE_CHAMPION = ArcanaAugments.register(
         new ArcanaAugment("Scales of the Champion", "scales_of_the_champion", new ItemStack(Items.NETHERITE_CHESTPLATE), ArcanaRegistry.WINGS_OF_ENDERIA,
         new String[]{"First Level: Gives the Wings max enhanced stats","Second Level: Unlocks the ability for the Wings to"," use stored energy to mitigate all damage types"},
         new MagicRarity[]{MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment WING_BUFFET = ArcanaAugments.register(
         new ArcanaAugment("Wing Buffet", "wing_buffet", new ItemStack(Items.FEATHER), ArcanaRegistry.WINGS_OF_ENDERIA,
         new String[]{"Gives a chance to blow mobs back when hit","This ability costs energy for each enemy","Chance per level: 10%/20%/30%/40%/100%"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Charm of Wild Growth
   public static final ArcanaAugment CHARM_OF_BLOOMING = ArcanaAugments.register(
         new ArcanaAugment("Charm of Blooming", "charm_of_blooming", new ItemStack(Items.PEONY), ArcanaRegistry.WILD_GROWTH_CHARM,
         new String[]{"Grass and plants grow around you periodically", "The Charm can now be used as unlimited bone meal."},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment FERTILIZATION = ArcanaAugments.register(
         new ArcanaAugment("Fertilization", "fertilization", new ItemStack(Items.BONE_MEAL), ArcanaRegistry.WILD_GROWTH_CHARM,
         new String[]{"Increases rate of The Charm's effect","Rate per level: 1.5x/2x/3x/5x"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment REAPING = ArcanaAugments.register(
         new ArcanaAugment("Reaping", "reaping", new ItemStack(Items.DIAMOND_HOE), ArcanaRegistry.WILD_GROWTH_CHARM,
         new String[]{"Fully grown crops get auto-harvested", "Second level replants harvested crops", "Sneak Right Click in off-hand to toggle"},
         new MagicRarity[]{LEGENDARY,MYTHICAL}
   ));
   
   // Arcanist's Belt
   public static final ArcanaAugment POUCHES = ArcanaAugments.register(
         new ArcanaAugment("Pouches", "pouches", new ItemStack(Items.CHEST), ArcanaRegistry.ARCANISTS_BELT,
         new String[]{"Adds extra slots to The Belt", "Extra Slots per level: 1/2/4/6"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment MENTAL_PADDING = ArcanaAugments.register(
         new ArcanaAugment("Mental Padding", "mental_padding", new ItemStack(Items.LEATHER_HELMET), ArcanaRegistry.ARCANISTS_BELT,
         new String[]{"Magic Items take 1/4 concentration when in the belt"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Containment Circlet
   public static final ArcanaAugment CONFINEMENT = ArcanaAugments.register(
         new ArcanaAugment("Confinement", "confinement", new ItemStack(Items.SPAWNER), ArcanaRegistry.CONTAINMENT_CIRCLET,
         new String[]{"Allows the Circlet to capture hostile mobs"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment HEALING_CIRCLET = ArcanaAugments.register(
         new ArcanaAugment("Healing Circlet", "healing_circlet", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.CONTAINMENT_CIRCLET,
         new String[]{"Slowly heals the contained creature"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Alchemical Arbalest
   public static final ArcanaAugment RUNIC_ARBALEST = ArcanaAugments.register(
         new ArcanaAugment("Runic Reconfiguration", "runic_arbalest", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.ALCHEMICAL_ARBALEST,
         new String[]{"Arbalest loses multishot but can activate Runic Arrows","Mutually exclusive with all other Augments"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment SPECTRAL_AMPLIFICATION = ArcanaAugments.register(
         new ArcanaAugment("Spectral Amplification", "spectral_amplification", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.ALCHEMICAL_ARBALEST,
         new String[]{"Increases damage amplification from Spectral Arrows","Increase per level: 25%/50%/100%","Mutually exclusive with Runic Reconfiguration"},
         new MagicRarity[]{EXOTIC,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment PROLIFIC_POTIONS = ArcanaAugments.register(
         new ArcanaAugment("Prolific Potions", "prolific_potions", new ItemStack(Items.POTION), ArcanaRegistry.ALCHEMICAL_ARBALEST,
         new String[]{"Increases potion radius by 1 block per level","Mutually exclusive with Runic Reconfiguration"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment SCATTERSHOT = ArcanaAugments.register(
         new ArcanaAugment("Scattershot", "scattershot", new ItemStack(Items.MELON_SEEDS), ArcanaRegistry.ALCHEMICAL_ARBALEST,
         new String[]{"Increases multishot shots from 3 to 5","Mutually exclusive with Runic Reconfiguration"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Chest Translocator
   public static final ArcanaAugment RAPID_TRANSLOCATION = ArcanaAugments.register(
         new ArcanaAugment("Rapid Translocation", "rapid_translocation", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.CHEST_TRANSLOCATOR,
         new String[]{"Decreases Translocator cooldown by 8 seconds per level"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Planeshifter
   public static final ArcanaAugment PLANAR_FLOW = ArcanaAugments.register(
         new ArcanaAugment("Planar Flow", "planar_flow", new ItemStack(Items.SCULK), ArcanaRegistry.PLANESHIFTER,
         new String[]{"Decreases the Shifter's cooldown time","Decrease per level: 1/2/4/6/8 minutes"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   // Everlasting Rocket
   public static final ArcanaAugment ADJUSTABLE_FUSE = ArcanaAugments.register(
         new ArcanaAugment("Adjustable Fuse", "adjustable_fuse", new ItemStack(Items.STRING), ArcanaRegistry.EVERLASTING_ROCKET,
         new String[]{"Enables changing the Rocket fuse by sneak right clicking"},
         new MagicRarity[]{EXOTIC}
   ));
   public static final ArcanaAugment SULFUR_REPLICATION = ArcanaAugments.register(
         new ArcanaAugment("Sulfur Replication", "sulfur_replication", new ItemStack(Items.GUNPOWDER), ArcanaRegistry.EVERLASTING_ROCKET,
         new String[]{"Decreases charge cooldown by 5 seconds per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment POWDER_PACKING = ArcanaAugments.register(
         new ArcanaAugment("Powder Packing", "powder_packing", new ItemStack(Items.TNT), ArcanaRegistry.EVERLASTING_ROCKET,
         new String[]{"Increases max charges by 3 per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Altar of the Stormcaller
   public static final ArcanaAugment CLOUD_SEEDING = ArcanaAugments.register(
         new ArcanaAugment("Cloud Seeding", "cloud_seeding", new ItemStack(Items.PUMPKIN_SEEDS), ArcanaRegistry.STORMCALLER_ALTAR,
         new String[]{"Decreases Altar cooldown by 5 minutes per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment PERSISTENT_TEMPEST = ArcanaAugments.register(
         new ArcanaAugment("Persistent Tempest", "persistent_tempest", new ItemStack(Items.CLOCK), ArcanaRegistry.STORMCALLER_ALTAR,
         new String[]{"Unlocks duration control of weather manipulation"},
         new MagicRarity[]{MYTHICAL}
   ));
   
   // Celestial Altar
   public static final ArcanaAugment STELLAR_CONTROL = ArcanaAugments.register(
         new ArcanaAugment("Stellar Control", "stellar_control", new ItemStack(Items.GLOWSTONE), ArcanaRegistry.CELESTIAL_ALTAR,
         new String[]{"Unlocks more precise phase and time manipulation"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment ORBITAL_PERIOD = ArcanaAugments.register(
         new ArcanaAugment("Orbital Period", "orbital_period", new ItemStack(Items.CLOCK), ArcanaRegistry.CELESTIAL_ALTAR,
         new String[]{"Decreases Altar cooldown by 5 minutes per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   
   // Starpath Altar
   public static final ArcanaAugment ASTRAL_PATHFINDER = ArcanaAugments.register(
         new ArcanaAugment("Astral Pathfinder", "astral_pathfinder", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.STARPATH_ALTAR,
         new String[]{"Doubles the fuel efficiency of The Altar for each level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment CONSTELLATION_DRIFT = ArcanaAugments.register(
         new ArcanaAugment("Constellation Drift", "constellation_drift", new ItemStack(Items.SCULK), ArcanaRegistry.STARPATH_ALTAR,
         new String[]{"Decreases Altar cooldown by 5 minutes per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,MYTHICAL}
   ));
   public static final ArcanaAugment STAR_CHARTS = ArcanaAugments.register(
         new ArcanaAugment("Star Charts", "star_charts", new ItemStack(Items.FILLED_MAP), ArcanaRegistry.STARPATH_ALTAR,
         new String[]{"Target locations can be saved and loaded"," using the Target Input menu buttons"},
         new MagicRarity[]{EMPOWERED}
   ));
   
   // Starlight Forge
   public static final ArcanaAugment RESOURCEFUL = ArcanaAugments.register(
         new ArcanaAugment("Resourceful", "resourceful", new ItemStack(Items.DIAMOND), ArcanaRegistry.STARLIGHT_FORGE,
         new String[]{"Chance to not consume ingredient when crafting","5% chance per level per individual item","Does not apply to Centerpiece or Magical Ingredients"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY,LEGENDARY,MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment SKILLED = ArcanaAugments.register(
         new ArcanaAugment("Skilled", "skilled", new ItemStack(Items.BOOK), ArcanaRegistry.STARLIGHT_FORGE,
         new String[]{"Forging a Magic Item automatically applies augments"," valued within a set amount of skill points","Skill Points Per Level: 1/2/3/5/7/9/12/15","Augment selection is random from unlocked augments"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,EXOTIC,LEGENDARY,LEGENDARY,MYTHICAL,MYTHICAL}
   ));
   public static final ArcanaAugment MOONLIT_FORGE = ArcanaAugments.register(
         new ArcanaAugment("Moonlit Forge", "moonlit_forge", new ItemStack(Items.PEARLESCENT_FROGLIGHT), ArcanaRegistry.STARLIGHT_FORGE,
         new String[]{"Enhanced Stats Randomness becomes influenced by the lunar phase"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment MYSTIC_COLLECTION = ArcanaAugments.register(
         new ArcanaAugment("Mystic Collection", "mystic_collection", new ItemStack(Items.CHEST), ArcanaRegistry.STARLIGHT_FORGE,
         new String[]{"The Forge can grab ingredients from nearby chests,"," barrels, and shulker boxes for crafting Magic Items"},
         new MagicRarity[]{EMPOWERED}
   ));
   public static final ArcanaAugment STELLAR_RANGE = ArcanaAugments.register(
         new ArcanaAugment("Stellar Range", "stellar_range", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.STARLIGHT_FORGE,
         new String[]{"Doubles the horizontal range of the Forge","Also increases the vertical range by 3 blocks"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Twilight Anvil
   public static final ArcanaAugment ENHANCED_ENHANCEMENTS = ArcanaAugments.register(
         new ArcanaAugment("Enhanced Enhancements", "enhanced_enhancements", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.TWILIGHT_ANVIL,
         new String[]{"Enhanced stats are improved when being combined","Grants an additional 2.5% stat boost per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment ANVIL_EXPERTISE = ArcanaAugments.register(
         new ArcanaAugment("Anvil Expertise", "anvil_expertise", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.TWILIGHT_ANVIL,
         new String[]{"Anvil uses raw XP points instead of levels"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Midnight Enchanter
   public static final ArcanaAugment PRECISION_DISENCHANTING = ArcanaAugments.register(
         new ArcanaAugment("Precision Disenchanting", "precision_disenchanting", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.MIDNIGHT_ENCHANTER,
         new String[]{"Unlocks the ability to disenchant items and put the","enchant on a book or save it to a singularity"},
         new MagicRarity[]{LEGENDARY}
   ));
   public static final ArcanaAugment ESSENCE_SUPERNOVA = ArcanaAugments.register(
         new ArcanaAugment("Essence Supernova", "essence_supernova", new ItemStack(Items.SCULK_VEIN), ArcanaRegistry.MIDNIGHT_ENCHANTER,
         new String[]{"The Enchanter gives an additional 15% Essence per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   
   // Arcane Singularity
   public static final ArcanaAugment SUPERMASSIVE = ArcanaAugments.register(
         new ArcanaAugment("Supermassive", "supermassive", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.ARCANE_SINGULARITY,
         new String[]{"Increases Enchantment storage by 1x per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC,LEGENDARY,LEGENDARY}
   ));
   public static final ArcanaAugment ACCRETION = ArcanaAugments.register(
         new ArcanaAugment("Accretion", "accretion", new ItemStack(Items.CRYING_OBSIDIAN), ArcanaRegistry.ARCANE_SINGULARITY,
         new String[]{"Enables combining enchantments in the singularity"},
         new MagicRarity[]{LEGENDARY}
   ));
   
   // Stellar Core
   public static final ArcanaAugment DYSON_SPHERE = ArcanaAugments.register(
         new ArcanaAugment("Dyson Sphere", "dyson_sphere", new ItemStack(Items.SPAWNER), ArcanaRegistry.STELLAR_CORE,
         new String[]{"Melting down items gives an additional 25% back per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment FUSION_INJECTORS = ArcanaAugments.register(
         new ArcanaAugment("Fusion Injectors", "fusion_injectors", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.STELLAR_CORE,
         new String[]{"The Stellar Core gives an additional 15% Stardust per level"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EXOTIC}
   ));
   public static final ArcanaAugment MOLTEN_CORE = ArcanaAugments.register(
         new ArcanaAugment("Molten Core", "molten_core", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.STELLAR_CORE,
         new String[]{"The Core can now smelt raw ores and raw ore blocks"," giving two ingots or metal blocks per raw ore smelted"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Radiant Fletchery
   public static final ArcanaAugment ALCHEMICAL_EFFICIENCY = ArcanaAugments.register(
         new ArcanaAugment("Alchemical Efficiency", "alchemical_efficiency", new ItemStack(Items.LINGERING_POTION), ArcanaRegistry.RADIANT_FLETCHERY,
         new String[]{"The Fletchery makes additional arrows per potion","Extra arrows per level: 8/16/24/32/40"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED,EMPOWERED,EXOTIC}
   ));
   
   // Totem of Vengeance
   public static final ArcanaAugment RETALIATIVE_FURY = ArcanaAugments.register(
         new ArcanaAugment("Retaliative Fury", "retaliative_fury", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.TOTEM_OF_VENGEANCE,
         new String[]{"Increases the Totem's duration and effect strength","Increases effect strength by 1 per level","Duration increase per level: 15/30/45"},
         new MagicRarity[]{MUNDANE,MUNDANE,EMPOWERED}
   ));
   
   // Aquatic Eversource
   public static final ArcanaAugment FLOODGATE = ArcanaAugments.register(
         new ArcanaAugment("Floodgate", "floodgate", new ItemStack(Items.WARPED_FENCE_GATE), ArcanaRegistry.AQUATIC_EVERSOURCE,
         new String[]{"Unlocks a mode that places water in a flat 3x3 area","Sneak Right Click to toggle modes"},
         new MagicRarity[]{EXOTIC}
   ));
   
   // Magmatic Eversource
   public static final ArcanaAugment VOLCANIC_CHAMBER = ArcanaAugments.register(
         new ArcanaAugment("Volcanic Chamber", "volcanic_chamber", new ItemStack(Items.MAGMA_BLOCK), ArcanaRegistry.MAGMATIC_EVERSOURCE,
         new String[]{"Adds charges to the Eversource","Charges per level: 3/5/10/25"},
         new MagicRarity[]{EMPOWERED,EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment ERUPTION = ArcanaAugments.register(
         new ArcanaAugment("Eruption", "eruption", new ItemStack(Items.MAGMA_CREAM), ArcanaRegistry.MAGMATIC_EVERSOURCE,
         new String[]{"Decreases Eversource cooldown by 8 seconds per level"},
         new MagicRarity[]{EMPOWERED,EMPOWERED,EXOTIC}
   ));
   
   // Transmutation Altar
   public static final ArcanaAugment TRADE_AGREEMENT = ArcanaAugments.register(
         new ArcanaAugment("Trade Agreement", "trade_agreement", new ItemStack(Items.DIAMOND), ArcanaRegistry.TRANSMUTATION_ALTAR,
         new String[]{"The Altar will reactivate automatically"," after a successful transmutation"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment HASTY_BARGAIN = ArcanaAugments.register(
         new ArcanaAugment("Hasty Bargain", "hasty_bargain", new ItemStack(Items.CLOCK), ArcanaRegistry.TRANSMUTATION_ALTAR,
         new String[]{"Decreases Altar cooldown by 5 minutes per level","First Level decreases transmutation time by 50%","First Level increases reagent consumption by 50%,","Reagent consumption decreased by 10% per extra level","Reagent consumption cannot go beyond 1 stack"},
         new MagicRarity[]{MUNDANE,EMPOWERED,EMPOWERED,EXOTIC,EXOTIC}
   ));
   
   // Aequalis Scientia
   public static final ArcanaAugment EQUIVALENT_EXCHANGE = ArcanaAugments.register(
         new ArcanaAugment("Equivalent Exchange", "equivalent_exchange", new ItemStack(ArcanaRegistry.CATALYTIC_MATRIX.getItem()), ArcanaRegistry.AEQUALIS_SCIENTIA,
         new String[]{"Unlocks a transmutation recipe that lets"," you reclaim catalysts from augmented items"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment TIMELESS_WISDOM = ArcanaAugments.register(
         new ArcanaAugment("Timeless Wisdom", "timeless_wisdom", new ItemStack(ArcanaRegistry.MYTHICAL_CATALYST.getItem()), ArcanaRegistry.AEQUALIS_SCIENTIA,
         new String[]{"Gives a 20% chance to not consume the Aequalis Scientia"},
         new MagicRarity[]{MYTHICAL,MYTHICAL,MYTHICAL,MYTHICAL,MYTHICAL}
   ));
   
   // Ensnarement Arrows
   public static final ArcanaAugment ETHEREAL_ANCHOR = ArcanaAugments.register(
         new ArcanaAugment("Ethereal Anchor", "ethereal_anchor", new ItemStack(Items.ENDER_PEARL), ArcanaRegistry.ENSNAREMENT_ARROWS,
         new String[]{"Ensnared players become unable to teleport"},
         new MagicRarity[]{MYTHICAL}
   ));
   public static final ArcanaAugment ENTRAPMENT = ArcanaAugments.register(
         new ArcanaAugment("Entrapment", "entrapment", new ItemStack(Items.COBWEB), ArcanaRegistry.ENSNAREMENT_ARROWS,
         new String[]{"Increases effect duration by 5 seconds per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,EXOTIC,LEGENDARY,LEGENDARY}
   ));
   
   // Tracking Arrows
   public static final ArcanaAugment RUNIC_GUIDANCE = ArcanaAugments.register(
         new ArcanaAugment("Runic Guidance", "runic_guidance", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.TRACKING_ARROWS,
         new String[]{"Increases the tracking angle of the arrows per level"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   public static final ArcanaAugment BROADHEADS = ArcanaAugments.register(
         new ArcanaAugment("Broadheads", "broadheads", new ItemStack(Items.SPECTRAL_ARROW), ArcanaRegistry.TRACKING_ARROWS,
         new String[]{"Hit entities receive 50% extra damage","Duration per level: 5/10/15 seconds"},
         new MagicRarity[]{EMPOWERED,EXOTIC,LEGENDARY}
   ));
   
   
   // Linked augments:
   static{
      linkedAugments.put(ArcanaAugments.HARNESS_RECYCLER,"harness_shulker_recycler");
      linkedAugments.put(ArcanaAugments.SHULKER_RECYCLER,"harness_shulker_recycler");
      
      linkedAugments.put(ArcanaAugments.OVERFLOWING_BOTTOMLESS,"quiver_efficiency");
      linkedAugments.put(ArcanaAugments.RUNIC_BOTTOMLESS,"quiver_efficiency");
      
      linkedAugments.put(ArcanaAugments.ABUNDANT_AMMO,"quiver_restock");
      linkedAugments.put(ArcanaAugments.QUIVER_DUPLICATION,"quiver_restock");
   }
   
   private static ArcanaAugment register(ArcanaAugment augment){
      registry.put(augment.id,augment);
      return augment;
   }
   
   public static List<ArcanaAugment> getLinkedAugments(String id){
      if(!registry.containsKey(id)) return new ArrayList<>();
      ArcanaAugment augment = registry.get(id);
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
   
   public static void setAugmentsOnItem(ItemStack item, TreeMap<ArcanaAugment,Integer> augments){
      MagicItem magicItem = MagicItemUtils.identifyItem(item);
      if(magicItem == null) return;
      NbtCompound magicTag = item.getNbt().getCompound("arcananovum");
      NbtCompound augsTag = new NbtCompound();
      
      augments.forEach((augKey,augValue) -> {
         if(registry.containsKey(augKey.id)){
            augsTag.putInt(augKey.id,augValue);
         }
      });
      
      magicTag.put("augments",augsTag);
      magicItem.buildItemLore(item,ArcanaNovum.SERVER);
   }
   
   public static boolean isIncompatible(ItemStack item, String id){ // TODO: Better incompat check for mutually exclusive augments
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
         
         if(id.equals(ArcanaAugments.RUNIC_ARBALEST.id) && other.id.equals(ArcanaAugments.SCATTERSHOT.id)) return true;
         if(id.equals(ArcanaAugments.SCATTERSHOT.id) && other.id.equals(ArcanaAugments.RUNIC_ARBALEST.id)) return true;
         
         if(id.equals(ArcanaAugments.RUNIC_ARBALEST.id) && other.id.equals(ArcanaAugments.PROLIFIC_POTIONS.id)) return true;
         if(id.equals(ArcanaAugments.PROLIFIC_POTIONS.id) && other.id.equals(ArcanaAugments.RUNIC_ARBALEST.id)) return true;
         
         if(id.equals(ArcanaAugments.RUNIC_ARBALEST.id) && other.id.equals(ArcanaAugments.SPECTRAL_AMPLIFICATION.id)) return true;
         if(id.equals(ArcanaAugments.SPECTRAL_AMPLIFICATION.id) && other.id.equals(ArcanaAugments.RUNIC_ARBALEST.id)) return true;
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
   
   public static int getAugmentFromMap(TreeMap<ArcanaAugment,Integer> augments, String id){
      if(augments == null) return 0;
      for(Map.Entry<ArcanaAugment, Integer> entry : augments.entrySet()){
         if(entry.getKey().id.equals(id)) return entry.getValue();
      }
      return 0;
   }
   
   // Applies Augment to Item, cannot down-level existing augments
   public static boolean applyAugment(ItemStack item, String id, int level, boolean withCatalyst){
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
      
      if(withCatalyst){
         int clampedCurLevel = Math.max(0,curLevel);
         NbtList catalystsList = magicTag.contains("catalysts") ? magicTag.getList("catalysts",NbtElement.COMPOUND_TYPE) : new NbtList();
         for(int lvl = clampedCurLevel+1; lvl <= level; lvl++){
            NbtCompound cata = new NbtCompound();
            MagicRarity rarity = augment.getTiers()[lvl-1];
            cata.putString("augment", augment.id);
            cata.putInt("level",lvl);
            cata.putInt("rarity",rarity.rarity);
            catalystsList.add(cata);
         }
         magicTag.put("catalysts",catalystsList);
      }
   
      if(magicItem instanceof ExoticMatter matter && id.equals(TIME_IN_A_BOTTLE.id)){
         matter.setFuel(item,matter.getMaxEnergy(item));
      }
      if(magicItem instanceof AlchemicalArbalest && id.equals(RUNIC_ARBALEST.id)){
         if(item.hasEnchantments()){
            NbtList enchants = item.getEnchantments();
            for(NbtElement enchant : enchants){
               Identifier enchantId = EnchantmentHelper.getIdFromNbt((NbtCompound) enchant);
               if(enchantId != null && enchantId.equals(EnchantmentHelper.getEnchantmentId(Enchantments.MULTISHOT))){
                  enchants.remove(enchant);
                  break;
               }
            }
            item.getNbt().put("Enchantments",enchants);
         }
      }
      if(magicItem instanceof WingsOfEnderia && id.equals(SCALES_OF_THE_CHAMPION.id) && level >= 1){
         item.removeSubNbt("AttributeModifiers");
         EnhancedStatUtils.enhanceItem(item,1);
      }
      
      if(magicItem instanceof NulMemento && id.equals(DEATHS_CHAMPION.id) && level >= 1){
         item.removeSubNbt("AttributeModifiers");
         EnhancedStatUtils.enhanceItem(item,1);
      }
      
      magicItem.buildItemLore(item, ArcanaNovum.SERVER);
      return true;
   }
   
   public static void copyAugment(ItemStack sourceStack, ItemStack destinationStack, String sourceAugment, String destinationAugment){
      NbtCompound sourceMagicTag = sourceStack.getNbt().getCompound("arcananovum");
      
      NbtList sourceCatas = sourceMagicTag.contains("catalysts") ? sourceMagicTag.getList("catalysts",NbtElement.COMPOUND_TYPE) : new NbtList();
      
      int sourceLvl = ArcanaAugments.getAugmentOnItem(sourceStack, sourceAugment);
      if(sourceLvl <= 0) return;
      
      for(int lvl = 0; lvl <= sourceLvl; lvl++){
         boolean foundCata = false;
         for(NbtElement sourceCata : sourceCatas){
            NbtCompound cata = (NbtCompound) sourceCata;
            if(cata.getString("augment").equals(sourceAugment) && cata.getInt("level") == lvl){
               ArcanaAugments.applyAugment(destinationStack,destinationAugment,lvl,true);
               foundCata = true;
               break;
            }
         }
         if(!foundCata){
            ArcanaAugments.applyAugment(destinationStack,destinationAugment,lvl,false);
         }
      }
   }
}
