package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.datastorage.ArcanaPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class ArcanaAchievements {
   public static final HashMap<String, ArcanaAchievement> ARCANA_ACHIEVEMENTS = new HashMap<>();
   public static final List<ArcanaAchievement> EXCLUDED_ACHIEVEMENTS = new ArrayList<>();
   
   // Divine Catalyst
   public static final ArcanaAchievement DOOR_OF_DIVINITY = ArcanaAchievements.register(
         new EventAchievement("door_of_divinity", new ItemStack(Items.AMETHYST_SHARD), ArcanaRegistry.DIVINE_CATALYST, 1000, 1));
   public static final ArcanaAchievement CONSTRUCT_DECONSTRUCTED = ArcanaAchievements.register(
         new EventAchievement("construct_deconstructed", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.DIVINE_CATALYST, 5000, 2));
   
   public static final ArcanaAchievement DIVINE_TRANSMUTATION = ArcanaAchievements.register(
         new EventAchievement("divine_transmutation", new ItemStack(Items.DIAMOND), ArcanaRegistry.DIVINE_CATALYST, 10000, 3));
   
   // Arcane Flak Arrows
   public static final ArcanaAchievement AA_ARTILLERY = ArcanaAchievements.register(
         new EventAchievement("aa_artillery", new ItemStack(Items.FIREWORK_STAR), ArcanaRegistry.ARCANE_FLAK_ARROWS, 5000, 2));
   
   // Blink Arrows
   public static final ArcanaAchievement NOW_YOU_SEE_ME = ArcanaAchievements.register(
         new EventAchievement("now_you_see_me", new ItemStack(Items.FEATHER), ArcanaRegistry.BLINK_ARROWS, 1000, 1));
   
   // Concussion Arrows
   public static final ArcanaAchievement SHOCK_AWE = ArcanaAchievements.register(
         new EventAchievement("shock_awe", new ItemStack(Items.BLACK_DYE), ArcanaRegistry.CONCUSSION_ARROWS, 1000, 1));
   
   // Detonation Arrows
   public static final ArcanaAchievement SAFETY_THIRD = ArcanaAchievements.register(
         new EventAchievement("safety_third", new ItemStack(Items.TNT), ArcanaRegistry.DETONATION_ARROWS, 1000, 1));
   
   // Expulsion Arrows
   public static final ArcanaAchievement JUMP_PAD = ArcanaAchievements.register(
         new EventAchievement("jump_pad", new ItemStack(Items.SLIME_BLOCK), ArcanaRegistry.EXPULSION_ARROWS, 1000, 1));
   
   // Graviton Arrows
   public static final ArcanaAchievement BRING_TOGETHER = ArcanaAchievements.register(
         new EventAchievement("bring_together", new ItemStack(Items.COBWEB), ArcanaRegistry.GRAVITON_ARROWS, 1000, 1));
   
   // Photonic Arrows
   public static final ArcanaAchievement X = ArcanaAchievements.register(
         new EventAchievement("x", new ItemStack(Items.STRUCTURE_VOID), ArcanaRegistry.PHOTONIC_ARROWS, 5000, 2));
   
   // Siphoning Arrows
   public static final ArcanaAchievement CIRCLE_OF_LIFE = ArcanaAchievements.register(
         new EventAchievement("circle_of_life", new ItemStack(Items.APPLE), ArcanaRegistry.SIPHONING_ARROWS, 2000, 1));
   
   // Smoke Arrows
   public static final ArcanaAchievement SMOKE_SCREEN = ArcanaAchievements.register(
         new EventAchievement("smoke_screen", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), ArcanaRegistry.SMOKE_ARROWS, 1000, 1));
   
   // Storm Arrows
   public static final ArcanaAchievement SHOCK_THERAPY = ArcanaAchievements.register(
         new EventAchievement("shock_therapy", new ItemStack(Items.SUSPICIOUS_STEW), ArcanaRegistry.STORM_ARROWS, 2500, 1));
   
   // Tether Arrows
   public static final ArcanaAchievement SPIDERMAN = ArcanaAchievements.register(
         new TimedAchievement("spiderman", new ItemStack(Items.COBWEB), ArcanaRegistry.TETHER_ARROWS, 1000, 1, 5, 300));
   
   // Charm of Cinders
   public static final ArcanaAchievement PYROMANIAC = ArcanaAchievements.register(
         new EventAchievement("pyromaniac", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.CINDERS_CHARM, 1000, 1));
   public static final ArcanaAchievement CAKE_DAY = ArcanaAchievements.register(
         new EventAchievement("cake_day", new ItemStack(Items.CAKE), ArcanaRegistry.CINDERS_CHARM, 1000, 1));
   public static final ArcanaAchievement GLASSBLOWER = ArcanaAchievements.register(
         new ProgressAchievement("glassblower", new ItemStack(Items.GLASS), ArcanaRegistry.CINDERS_CHARM, 10000, 3, 10000));
   
   // Charm of Feasting
   public static final ArcanaAchievement TARRARE = ArcanaAchievements.register(
         new ConditionalsAchievement("tarrare", new ItemStack(Items.SPIDER_EYE), ArcanaRegistry.FEASTING_CHARM, 5000, 2,
               new String[]{"Poisonous Potato", "Spider Eye", "Rotten Flesh", "Suspicious Stew", "Raw Chicken", "Pufferfish"}));
   
   // Charm of Felidae
   public static final ArcanaAchievement LAND_ON_FEET = ArcanaAchievements.register(
         new EventAchievement("land_on_feet", new ItemStack(Items.LEATHER_BOOTS), ArcanaRegistry.FELIDAE_CHARM, 5000, 2));
   public static final ArcanaAchievement INFILTRATION = ArcanaAchievements.register(
         new EventAchievement("infiltration", new ItemStack(Items.CREEPER_HEAD), ArcanaRegistry.FELIDAE_CHARM, 1000, 1));
   
   // Charm of Leadership
   public static final ArcanaAchievement RAID_LEADER = ArcanaAchievements.register(
         new EventAchievement("raid_leader", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.LEADERSHIP_CHARM, 1000, 1));
   
   // Charm of Light
   public static final ArcanaAchievement ENLIGHTENED = ArcanaAchievements.register(
         new ProgressAchievement("enlightened", new ItemStack(Items.LIGHT), ArcanaRegistry.LIGHT_CHARM, 5000, 2, 1000));
   
   // Charm of Magnetism
   public static final ArcanaAchievement MAGNETS = ArcanaAchievements.register(
         new EventAchievement("magnets", new ItemStack(Items.IRON_ORE), ArcanaRegistry.MAGNETISM_CHARM, 1000, 1));
   
   // Ancient Dowsing Rod
   public static final ArcanaAchievement MOTHERLOAD = ArcanaAchievements.register(
         new EventAchievement("motherload", new ItemStack(Items.ANCIENT_DEBRIS), ArcanaRegistry.ANCIENT_DOWSING_ROD, 1000, 1));
   public static final ArcanaAchievement ARCHEOLOGIST = ArcanaAchievements.register(
         new ProgressAchievement("archeologist", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.ANCIENT_DOWSING_ROD, 5000, 2, 1000));
   
   // Arcane Tome / Generic
   public static final ArcanaAchievement CLOSE_CALL = ArcanaAchievements.register(
         new EventAchievement("close_call", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.ARCANE_TOME, 1000, 1));
   public static final ArcanaAchievement GOD_BOON = ArcanaAchievements.register(
         new EventAchievement("god_boon", new ItemStack(Items.DRAGON_EGG), ArcanaRegistry.ARCANE_TOME, 10000, 3));
   public static final ArcanaAchievement ARCANE_ADDICT = ArcanaAchievements.register(
         new EventAchievement("arcane_addict", new ItemStack(Items.KNOWLEDGE_BOOK), ArcanaRegistry.ARCANE_TOME, 50000, 4));
   public static final ArcanaAchievement ALL_ACHIEVEMENTS = ArcanaAchievements.register(
         new EventAchievement("all_achievements", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.ARCANE_TOME, 1000000, 6666));
   
   // Brain in a Jar
   public static final ArcanaAchievement BREAK_BANK = ArcanaAchievements.register(
         new EventAchievement("break_bank", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.BRAIN_JAR, 10000, 3));
   public static final ArcanaAchievement CERTIFIED_REPAIR = ArcanaAchievements.register(
         new ProgressAchievement("certified_repair", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.BRAIN_JAR, 25000, 3, 100000));
   
   // Continuum Anchor
   public static final ArcanaAchievement TIMEY_WIMEY = ArcanaAchievements.register(
         new ProgressAchievement("timey_wimey", new ItemStack(Items.CLOCK), ArcanaRegistry.CONTINUUM_ANCHOR, 100000, 5, 2629744));
   
   // Essence Egg
   public static final ArcanaAchievement SOUL_CONVERSION = ArcanaAchievements.register(
         new EventAchievement("soul_conversion", new ItemStack(Items.SPAWNER), ArcanaRegistry.ESSENCE_EGG, 5000, 2));
   public static final ArcanaAchievement SOUL_FOR_SOUL = ArcanaAchievements.register(
         new ProgressAchievement("soul_for_soul", new ItemStack(Items.CHICKEN_SPAWN_EGG), ArcanaRegistry.ESSENCE_EGG, 10000, 3, 25));
   
   // Fractal Sponge
   public static final ArcanaAchievement OCEAN_CLEANUP = ArcanaAchievements.register(
         new ProgressAchievement("ocean_cleanup", new ItemStack(Items.SEAGRASS), ArcanaRegistry.FRACTAL_SPONGE, 5000, 2, 10000));
   
   // Igneous Collider
   public static final ArcanaAchievement ENDLESS_EXTRUSION = ArcanaAchievements.register(
         new ProgressAchievement("endless_extrusion", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER, 15000, 3, 10000));
   public static final ArcanaAchievement EXPENSIVE_INFUSION = ArcanaAchievements.register(
         new EventAchievement("expensive_infusion", new ItemStack(Items.CRYING_OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER, 5000, 2));
   
   // Levitation Harness
   public static final ArcanaAchievement TO_THE_MOON = ArcanaAchievements.register(
         new EventAchievement("to_the_moon", new ItemStack(Items.END_STONE), ArcanaRegistry.LEVITATION_HARNESS, 2500, 1));
   public static final ArcanaAchievement FREQUENT_FLIER = ArcanaAchievements.register(
         new ProgressAchievement("frequent_flier", new ItemStack(Items.FEATHER), ArcanaRegistry.LEVITATION_HARNESS, 15000, 3, 86400));
   public static final ArcanaAchievement AIR_TRAFFIC_CONTROL = ArcanaAchievements.register(
         new EventAchievement("air_traffic_control", new ItemStack(Items.SPYGLASS), ArcanaRegistry.LEVITATION_HARNESS, 100000, 5));
   
   // Nul Memento
   public static final ArcanaAchievement DIVINE_FAVOR = ArcanaAchievements.register(
         new EventAchievement("divine_favor", new ItemStack(Items.AMETHYST_CLUSTER), ArcanaRegistry.NUL_MEMENTO, 100000, 5));
   public static final ArcanaAchievement AMNESIAC = ArcanaAchievements.register(
         new ProgressAchievement("amnesiac", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.NUL_MEMENTO, 100000, 5, 2));
   public static final ArcanaAchievement DEATHS_DOOR = ArcanaAchievements.register(
         new EventAchievement("deaths_door", new ItemStack(Items.TOTEM_OF_UNDYING), ArcanaRegistry.NUL_MEMENTO, 2500, 1));
   public static final ArcanaAchievement LOST_KNOWLEDGE = ArcanaAchievements.register(
         new EventAchievement("lost_knowledge", new ItemStack(Items.KNOWLEDGE_BOOK), ArcanaRegistry.NUL_MEMENTO, 2500, 1));
   
   // Overflowing Quiver
   public static final ArcanaAchievement SPARE_STOCK = ArcanaAchievements.register(
         new ProgressAchievement("spare_stock", new ItemStack(Items.ARROW), ArcanaRegistry.OVERFLOWING_QUIVER, 5000, 2, 200));
   public static final ArcanaAchievement DIVERSE_ARSENAL = ArcanaAchievements.register(
         new EventAchievement("diverse_arsenal", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.OVERFLOWING_QUIVER, 2000, 1));
   
   // Pearl of Recall
   public static final ArcanaAchievement BACK_TO_HELL = ArcanaAchievements.register(
         new EventAchievement("back_to_hell", new ItemStack(Items.NETHERRACK), ArcanaRegistry.PEARL_OF_RECALL, 1500, 1));
   public static final ArcanaAchievement ASCENDING_TO_HEAVEN = ArcanaAchievements.register(
         new EventAchievement("ascending_to_heaven", new ItemStack(Items.END_STONE), ArcanaRegistry.PEARL_OF_RECALL, 1500, 1));
   
   // Pickaxe of Ceptyus
   public static final ArcanaAchievement BACK_IN_THE_MINE = ArcanaAchievements.register(
         new ProgressAchievement("back_in_the_mine", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 5000, 2, 72000));
   public static final ArcanaAchievement DIGGY_HOLE = ArcanaAchievements.register(
         new ProgressAchievement("diggy_hole", new ItemStack(Items.COBBLESTONE), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 100000, 5, 100000));
   public static final ArcanaAchievement MINE_DIAMONDS = ArcanaAchievements.register(
         new EventAchievement("mine_diamonds", new ItemStack(Items.DIAMOND), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 2500, 1));
   public static final ArcanaAchievement INTERLOPER = ArcanaAchievements.register(
         new EventAchievement("interloper", ArcanaRegistry.PLANESHIFTER.getPrefItemNoLore(), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 100000, 10)).setHidden(true);
   
   // Runic Bow
   public static final ArcanaAchievement JUST_LIKE_ARCHER = ArcanaAchievements.register(
         new ProgressAchievement("just_like_archer", new ItemStack(Items.BOW), ArcanaRegistry.RUNIC_BOW, 1000, 3, 1000));
   public static final ArcanaAchievement AIMBOT = ArcanaAchievements.register(
         new EventAchievement("aimbot", new ItemStack(Items.TARGET), ArcanaRegistry.RUNIC_BOW, 5000, 2));
   
   // Runic Quiver
   public static final ArcanaAchievement UNLIMITED_STOCK = ArcanaAchievements.register(
         new ProgressAchievement("unlimited_stock", new ItemStack(Items.ARROW), ArcanaRegistry.RUNIC_QUIVER, 5000, 2, 500));
   public static final ArcanaAchievement ARROW_FOR_EVERY_FOE = ArcanaAchievements.register(
         new TimedAchievement("arrow_for_every_foe", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RUNIC_QUIVER, 10000, 3, 9, 400));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAchievement OMAE_WA = ArcanaAchievements.register(
         new TimedAchievement("omae_wa", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.SHADOW_STALKERS_GLAIVE, 7500, 2, 1, 20));
   public static final ArcanaAchievement SHADOW_FURY = ArcanaAchievements.register(
         new TimedAchievement("shadow_fury", new ItemStack(Items.WITHER_ROSE), ArcanaRegistry.SHADOW_STALKERS_GLAIVE, 5000, 2, 15, 600));
   
   // Shield of Fortitude
   public static final ArcanaAchievement BUILT_LIKE_TANK = ArcanaAchievements.register(
         new EventAchievement("built_like_tank", new ItemStack(Items.IRON_BLOCK), ArcanaRegistry.SHIELD_OF_FORTITUDE, 5000, 2));
   
   // Shulker Core
   public static final ArcanaAchievement MILE_HIGH = ArcanaAchievements.register(
         new EventAchievement("mile_high", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.SHULKER_CORE, 1500, 1));
   
   // Sojourner's Boots
   public static final ArcanaAchievement RUNNING = ArcanaAchievements.register(
         new TimedAchievement("running", new ItemStack(Items.GOLDEN_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS, 10000, 3, 5900, 6001));
   public static final ArcanaAchievement PHEIDIPPIDES = ArcanaAchievements.register(
         new ProgressAchievement("pheidippides", new ItemStack(Items.NETHERITE_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS, 100000, 5, 24600000));
   
   // Soulstone
   public static final ArcanaAchievement TOOK_A_VILLAGE = ArcanaAchievements.register(
         new EventAchievement("took_a_village", new ItemStack(Items.EMERALD), ArcanaRegistry.SOULSTONE, 5000, 2));
   public static final ArcanaAchievement PHILOSOPHER_STONE = ArcanaAchievements.register(
         new EventAchievement("philosopher_stone", new ItemStack(Items.REDSTONE_BLOCK), ArcanaRegistry.SOULSTONE, 10000, 3));
   public static final ArcanaAchievement PRIME_EVIL = ArcanaAchievements.register(
         new EventAchievement("prime_evil", new ItemStack(Items.BLACKSTONE), ArcanaRegistry.SOULSTONE, 100000, 5));
   
   // Spawner Harness
   public static final ArcanaAchievement FINALLY_USEFUL = ArcanaAchievements.register(
         new EventAchievement("finally_useful", new ItemStack(Items.IRON_NUGGET), ArcanaRegistry.SPAWNER_HARNESS, 1000, 1));
   
   // Spawner Infuser
   public static final ArcanaAchievement HUMBLE_NECROMANCER = ArcanaAchievements.register(
         new EventAchievement("humble_necromancer", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.SPAWNER_INFUSER, 1000, 1));
   public static final ArcanaAchievement SCULK_HUNGERS = ArcanaAchievements.register(
         new EventAchievement("sculk_hungers", new ItemStack(Items.SCULK), ArcanaRegistry.SPAWNER_INFUSER, 10000, 3));
   public static final ArcanaAchievement ARCHLICH = ArcanaAchievements.register(
         new EventAchievement("archlich", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.SPAWNER_INFUSER, 25000, 3));
   public static final ArcanaAchievement INNOCENT_SOULS = ArcanaAchievements.register(
         new EventAchievement("innocent_souls", new ItemStack(Items.SOUL_SAND), ArcanaRegistry.SPAWNER_INFUSER, 50000, 4));
   public static final ArcanaAchievement POWER_OVERWHELMING = ArcanaAchievements.register(
         new EventAchievement("power_overwhelming", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.SPAWNER_INFUSER, 100000, 5));
   
   // Stasis Pearl
   public static final ArcanaAchievement PEARL_HANG = ArcanaAchievements.register(
         new EventAchievement("pearl_hang", new ItemStack(Items.CLOCK), ArcanaRegistry.STASIS_PEARL, 1000, 1));
   public static final ArcanaAchievement INSTANT_TRANSMISSION = ArcanaAchievements.register(
         new EventAchievement("instant_transmission", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.STASIS_PEARL, 1500, 1));
   
   // Telescoping Beacon
   public static final ArcanaAchievement ART_OF_THE_DEAL = ArcanaAchievements.register(
         new EventAchievement("art_of_the_deal", new ItemStack(Items.EMERALD_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 5000, 2));
   public static final ArcanaAchievement ACQUISITION_RULES = ArcanaAchievements.register(
         new EventAchievement("acquisition_rules", new ItemStack(Items.GOLD_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 10000, 3));
   public static final ArcanaAchievement BEJEWELED = ArcanaAchievements.register(
         new EventAchievement("bejeweled", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 50000, 4));
   public static final ArcanaAchievement CLINICALLY_INSANE = ArcanaAchievements.register(
         new EventAchievement("clinically_insane", new ItemStack(Items.NETHERITE_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 100000, 5));
   
   // Wings of Enderia
   public static final ArcanaAchievement SEE_GLASS = ArcanaAchievements.register(
         new EventAchievement("see_glass", new ItemStack(Items.GLASS), ArcanaRegistry.WINGS_OF_ENDERIA, 5000, 2));
   public static final ArcanaAchievement ANGEL_OF_DEATH = ArcanaAchievements.register(
         new EventAchievement("angel_of_death", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.WINGS_OF_ENDERIA, 1000, 1));
   public static final ArcanaAchievement CROW_FATHER = ArcanaAchievements.register(
         new EventAchievement("crow_father", new ItemStack(Items.FEATHER), ArcanaRegistry.WINGS_OF_ENDERIA, 1500, 1));
   
   // Charm of Wild Growth
   public static final ArcanaAchievement THEY_GROW_UP_SO_FAST = ArcanaAchievements.register(
         new EventAchievement("they_grow_up_so_fast", new ItemStack(Items.BEEF), ArcanaRegistry.WILD_GROWTH_CHARM, 1000, 1));
   public static final ArcanaAchievement BOUNTIFUL_HARVEST = ArcanaAchievements.register(
         new ProgressAchievement("bountiful_harvest", new ItemStack(Items.WHEAT), ArcanaRegistry.WILD_GROWTH_CHARM, 5000, 2, 10000));
   
   // Arcanist's Belt
   public static final ArcanaAchievement BELT_CHARMING = ArcanaAchievements.register(
         new EventAchievement("belt_charming", new ItemStack(Items.GOLD_INGOT), ArcanaRegistry.ARCANISTS_BELT, 2500, 2));
   
   // Containment Circlet
   public static final ArcanaAchievement I_CHOOSE_YOU = ArcanaAchievements.register(
         new EventAchievement("i_choose_you", new ItemStack(Items.BONE), ArcanaRegistry.CONTAINMENT_CIRCLET, 1000, 1));
   
   // Alchemical Arbalest
   public static final ArcanaAchievement MANY_BIRDS_MANY_ARROWS = ArcanaAchievements.register(
         new TimedAchievement("many_birds_many_arrows", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.ALCHEMICAL_ARBALEST, 5000, 2, 3, 60));
   public static final ArcanaAchievement SPECTRAL_SUPPORT = ArcanaAchievements.register(
         new TimedAchievement("spectral_support", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.ALCHEMICAL_ARBALEST, 5000, 2, 1000, 200));
   
   // Chest Translocator
   public static final ArcanaAchievement PEAK_LAZINESS = ArcanaAchievements.register(
         new EventAchievement("peak_laziness", new ItemStack(Items.COBWEB), ArcanaRegistry.CHEST_TRANSLOCATOR, 1000, 1));
   public static final ArcanaAchievement STORAGE_RELOCATION = ArcanaAchievements.register(
         new ProgressAchievement("storage_relocation", new ItemStack(Items.CHEST), ArcanaRegistry.CHEST_TRANSLOCATOR, 1000, 1, 100));
   
   // Planeshifter
   public static final ArcanaAchievement PLANE_RIDER = ArcanaAchievements.register(
         new ConditionalsAchievement("plane_rider", new ItemStack(Items.MAP), ArcanaRegistry.PLANESHIFTER, 5000, 2,
               new String[]{"From The Overworld", "To The Overworld", "From The Nether", "To The Nether", "From The End", "To The End"}));
   public static final ArcanaAchievement UNFORTUNATE_MATERIALIZATION = ArcanaAchievements.register(
         new EventAchievement("unfortunate_materialization", new ItemStack(Items.LAVA_BUCKET), ArcanaRegistry.PLANESHIFTER, 2500, 1));
   
   // Everlasting Rocket
   public static final ArcanaAchievement ROCKETMAN = ArcanaAchievements.register(
         new EventAchievement("rocketman", new ItemStack(Items.GUNPOWDER), ArcanaRegistry.EVERLASTING_ROCKET, 1500, 1));
   public static final ArcanaAchievement MISSILE_LAUNCHER = ArcanaAchievements.register(
         new TimedAchievement("missile_launcher", new ItemStack(Items.FIREWORK_ROCKET), ArcanaRegistry.EVERLASTING_ROCKET, 1500, 1, 5, 100));
   
   // Altar of the Stormcaller
   public static final ArcanaAchievement COME_AGAIN_RAIN = ArcanaAchievements.register(
         new EventAchievement("come_again_rain", new ItemStack(Items.WATER_BUCKET), ArcanaRegistry.STORMCALLER_ALTAR, 1000, 1));
   
   // Celestial Altar
   public static final ArcanaAchievement POWER_OF_THE_SUN = ArcanaAchievements.register(
         new EventAchievement("power_of_the_sun", new ItemStack(Items.SHROOMLIGHT), ArcanaRegistry.CELESTIAL_ALTAR, 1000, 1));
   public static final ArcanaAchievement LYCANTHROPE = ArcanaAchievements.register(
         new EventAchievement("lycanthrope", new ItemStack(Items.BONE), ArcanaRegistry.CELESTIAL_ALTAR, 1000, 1));
   
   // Starpath Altar
   public static final ArcanaAchievement ADVENTURING_PARTY = ArcanaAchievements.register(
         new EventAchievement("adventuring_party", new ItemStack(Items.LEAD), ArcanaRegistry.STARPATH_ALTAR, 1000, 1));
   public static final ArcanaAchievement FAR_FROM_HOME = ArcanaAchievements.register(
         new EventAchievement("far_from_home", new ItemStack(Items.FILLED_MAP), ArcanaRegistry.STARPATH_ALTAR, 10000, 3));
   
   // Starlight Forge
   public static final ArcanaAchievement INTRO_ARCANA = ArcanaAchievements.register(
         new EventAchievement("intro_arcana", new ItemStack(Items.BOOK), ArcanaRegistry.STARLIGHT_FORGE, 1000, 1));
   public static final ArcanaAchievement INTERMEDIATE_ARTIFICE = ArcanaAchievements.register(
         new ProgressAchievement("intermediate_artifice", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.STARLIGHT_FORGE, 5000, 2, 10));
   public static final ArcanaAchievement ARTIFICIAL_DIVINITY = ArcanaAchievements.register(
         new EventAchievement("artificial_divinity", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.STARLIGHT_FORGE, 10000, 3));
   public static final ArcanaAchievement MASTER_CRAFTSMAN = ArcanaAchievements.register(
         new EventAchievement("master_craftsman", new ItemStack(Items.CRAFTING_TABLE), ArcanaRegistry.STARLIGHT_FORGE, 25000, 3));
   public static final ArcanaAchievement NIDAVELLIR = ArcanaAchievements.register(
         new EventAchievement("nidavellir", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.STARLIGHT_FORGE, 100000, 5));
   
   // Twilight Anvil
   public static final ArcanaAchievement TOUCH_OF_PERSONALITY = ArcanaAchievements.register(
         new EventAchievement("touch_of_personality", new ItemStack(Items.NAME_TAG), ArcanaRegistry.TWILIGHT_ANVIL, 1000, 1));
   public static final ArcanaAchievement BEYOND_IRONS_LIMIT = ArcanaAchievements.register(
         new EventAchievement("beyond_irons_limit", new ItemStack(Items.NETHERITE_INGOT), ArcanaRegistry.TWILIGHT_ANVIL, 2500, 2));
   public static final ArcanaAchievement TINKER_TO_THE_TOP = ArcanaAchievements.register(
         new EventAchievement("tinker_to_the_top", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.TWILIGHT_ANVIL, 10000, 3));
   
   // Midnight Enchanter
   public static final ArcanaAchievement ENCHANTING_OVERKILL = ArcanaAchievements.register(
         new EventAchievement("enchanting_overkill", new ItemStack(Items.WOODEN_PICKAXE), ArcanaRegistry.MIDNIGHT_ENCHANTER, 1000, 1));
   public static final ArcanaAchievement MASTERPIECE_TO_NOTHING = ArcanaAchievements.register(
         new EventAchievement("masterpiece_to_nothing", new ItemStack(Items.NETHERITE_CHESTPLATE), ArcanaRegistry.MIDNIGHT_ENCHANTER, 5000, 2));
   
   // Arcane Singularity
   public static final ArcanaAchievement ARCANE_QUASAR = ArcanaAchievements.register(
         new EventAchievement("arcane_quasar", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.ARCANE_SINGULARITY, 5000, 2));
   
   // Stellar Core
   public static final ArcanaAchievement SCRAP_TO_SCRAP = ArcanaAchievements.register(
         new EventAchievement("scrap_to_scrap", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.STELLAR_CORE, 1000, 1));
   public static final ArcanaAchievement RECLAMATION = ArcanaAchievements.register(
         new ProgressAchievement("reclamation", new ItemStack(Items.DIAMOND), ArcanaRegistry.STELLAR_CORE, 10000, 3, 1000));
   
   // Radiant Fletchery
   public static final ArcanaAchievement OVERLY_EQUIPPED_ARCHER = ArcanaAchievements.register(
         new ConditionalsAchievement("overly_equipped_archer", new ItemStack(Items.BOW), ArcanaRegistry.RADIANT_FLETCHERY, 50000, 4,
               new String[]{
                     ArcanaRegistry.OVERFLOWING_QUIVER.getNameString(),
                     ArcanaRegistry.RUNIC_BOW.getNameString(),
                     ArcanaRegistry.ALCHEMICAL_ARBALEST.getNameString(),
                     ArcanaRegistry.RUNIC_QUIVER.getNameString(),
                     ArcanaRegistry.ARCANE_FLAK_ARROWS.getNameString(),
                     ArcanaRegistry.BLINK_ARROWS.getNameString(),
                     ArcanaRegistry.DETONATION_ARROWS.getNameString(),
                     ArcanaRegistry.STORM_ARROWS.getNameString(),
                     ArcanaRegistry.CONCUSSION_ARROWS.getNameString(),
                     ArcanaRegistry.EXPULSION_ARROWS.getNameString(),
                     ArcanaRegistry.GRAVITON_ARROWS.getNameString(),
                     ArcanaRegistry.PHOTONIC_ARROWS.getNameString(),
                     ArcanaRegistry.SIPHONING_ARROWS.getNameString(),
                     ArcanaRegistry.TETHER_ARROWS.getNameString(),
                     ArcanaRegistry.SMOKE_ARROWS.getNameString(),
                     ArcanaRegistry.TRACKING_ARROWS.getNameString(),
                     ArcanaRegistry.ENSNAREMENT_ARROWS.getNameString()
               }));
   public static final ArcanaAchievement FINALLY_USEFUL_2 = ArcanaAchievements.register(
         new EventAchievement("finally_useful_2", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RADIANT_FLETCHERY, 1000, 1));
   
   // Totem of Vengeance
   public static final ArcanaAchievement REVENGEANCE = ArcanaAchievements.register(
         new EventAchievement("revengeance", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.TOTEM_OF_VENGEANCE, 5000, 2));
   public static final ArcanaAchievement TOO_ANGRY_TO_DIE = ArcanaAchievements.register(
         new TimedAchievement("too_angry_to_die", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.TOTEM_OF_VENGEANCE, 10000, 3, 1000, 1200));
   
   // Aquatic Eversource
   public static final ArcanaAchievement POCKET_OCEAN = ArcanaAchievements.register(
         new ProgressAchievement("pocket_ocean", new ItemStack(Items.HEART_OF_THE_SEA), ArcanaRegistry.AQUATIC_EVERSOURCE, 1500, 1, 1000));
   
   // Magmatic Eversource
   public static final ArcanaAchievement HELLGATE = ArcanaAchievements.register(
         new ProgressAchievement("hellgate", new ItemStack(Items.CRIMSON_FENCE_GATE), ArcanaRegistry.MAGMATIC_EVERSOURCE, 2500, 2, 1000));
   
   // Altar of Transmutation
   public static final ArcanaAchievement STATE_ALCHEMIST = ArcanaAchievements.register(
         new ProgressAchievement("state_alchemist", new ItemStack(Items.QUARTZ), ArcanaRegistry.TRANSMUTATION_ALTAR, 10000, 3, 10000));
   
   // Aequalis Scientia
   public static final ArcanaAchievement PRICE_OF_KNOWLEDGE = ArcanaAchievements.register(
         new EventAchievement("price_of_knowledge", ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.AEQUALIS_SCIENTIA, 10000, 3));
   public static final ArcanaAchievement QUESTIONABLE_EXCHANGE = ArcanaAchievements.register(
         new EventAchievement("questionable_exchange", new ItemStack(Items.NETHERITE_HOE), ArcanaRegistry.AEQUALIS_SCIENTIA, 10000, 3));
   public static final ArcanaAchievement FRACTAL_ATTUNEMENT = ArcanaAchievements.register(
         new EventAchievement("fractal_attunement", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.AEQUALIS_SCIENTIA, 10000, 3));
   
   // Ensnarement Arrows
   public static final ArcanaAchievement WATERBOARDING = ArcanaAchievements.register(
         new EventAchievement("waterboarding", new ItemStack(Items.WATER_BUCKET), ArcanaRegistry.ENSNAREMENT_ARROWS, 1000, 1));
   public static final ArcanaAchievement SHACKLED = ArcanaAchievements.register(
         new TimedAchievement("shackled", new ItemStack(Items.IRON_CHAIN), ArcanaRegistry.ENSNAREMENT_ARROWS, 1500, 1, 1200, 1211));
   
   // Tracking Arrows
   public static final ArcanaAchievement TARGET_ACQUIRED = ArcanaAchievements.register(
         new EventAchievement("target_acquired", new ItemStack(Items.TARGET), ArcanaRegistry.TRACKING_ARROWS, 1000, 1));
   public static final ArcanaAchievement ACTUAL_AIMBOT = ArcanaAchievements.register(
         new EventAchievement("actual_aimbot", new ItemStack(Items.BOW), ArcanaRegistry.TRACKING_ARROWS, 5000, 2));
   public static final ArcanaAchievement THE_ARROW_KNOWS_WHERE_IT_IS = ArcanaAchievements.register(
         new EventAchievement("the_arrow_knows_where_it_is", new ItemStack(Items.COMPASS), ArcanaRegistry.TRACKING_ARROWS, 2500, 2));
   
   // Binary Blades
   public static final ArcanaAchievement STARBURST_STREAM = ArcanaAchievements.register(
         new TimedAchievement("starburst_stream", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.BINARY_BLADES, 2500, 2, 6000, 6020));
   
   // Graviton Maul
   public static final ArcanaAchievement QUICK_WAY_DOWN = ArcanaAchievements.register(
         new EventAchievement("quick_way_down", new ItemStack(Items.NETHERITE_BOOTS), ArcanaRegistry.GRAVITON_MAUL, 2500, 2));
   public static final ArcanaAchievement RAISE_THE_ROOF = ArcanaAchievements.register(
         new EventAchievement("raise_the_roof", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.GRAVITON_MAUL, 2500, 2));
   public static final ArcanaAchievement BONE_SMASHER = ArcanaAchievements.register(
         new EventAchievement("bone_smasher", new ItemStack(Items.BONE), ArcanaRegistry.GRAVITON_MAUL, 5000, 2));
   
   // Charm of Cetacea
   public static final ArcanaAchievement OCEAN_MIGRATION = ArcanaAchievements.register(
         new ProgressAchievement("ocean_migration", new ItemStack(Items.HEART_OF_THE_SEA), ArcanaRegistry.CETACEA_CHARM, 10000, 3, 1000000));
   public static final ArcanaAchievement CEPHALOS_IN_A_POD = ArcanaAchievements.register(
         new ProgressAchievement("cephalos_in_a_pod", new ItemStack(Items.PRISMARINE_CRYSTALS), ArcanaRegistry.CETACEA_CHARM, 5000, 2, 100000));
   
   // Charm of Cleansing
   public static final ArcanaAchievement SEPTIC_SHOCK = ArcanaAchievements.register(
         new EventAchievement("septic_shock", new ItemStack(Items.PUFFERFISH), ArcanaRegistry.CLEANSING_CHARM, 2500, 2));
   public static final ArcanaAchievement FOOD_POISONT = ArcanaAchievements.register(
         new EventAchievement("food_poisont", new ItemStack(Items.ROTTEN_FLESH), ArcanaRegistry.CLEANSING_CHARM, 1000, 1));
   public static final ArcanaAchievement CHRONIC_AILMENT = ArcanaAchievements.register(
         new TimedAchievement("chronic_ailment", new ItemStack(Items.FERMENTED_SPIDER_EYE), ArcanaRegistry.CLEANSING_CHARM, 1000, 1, 1, 20));
   
   // Greaves of Gaialtus
   public static final ArcanaAchievement TERRAFORMER = ArcanaAchievements.register(
         new ProgressAchievement("terraformer", new ItemStack(Items.GRASS_BLOCK), ArcanaRegistry.GREAVES_OF_GAIALTUS, 10000, 3, 10000));
   public static final ArcanaAchievement MINERS_WALLET = ArcanaAchievements.register(
         new EventAchievement("miners_wallet", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.GREAVES_OF_GAIALTUS, 1000, 1));
   public static final ArcanaAchievement AND_THE_UNIVERSE_SAID = ArcanaAchievements.register(
         new EventAchievement("and_the_universe_said", new ItemStack(Items.END_PORTAL_FRAME), ArcanaRegistry.GREAVES_OF_GAIALTUS, 100000, 10)).setHidden(true);
   
   // Spear of Tenbrous
   public static final ArcanaAchievement KILL_THEM_ALL = ArcanaAchievements.register(
         new ConditionalsAchievement("kill_them_all", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.SPEAR_OF_TENBROUS, 2500, 2,
               new String[]{
                     EntityType.ENDERMAN.getDescription().getString(),
                     EntityType.ENDERMITE.getDescription().getString(),
                     EntityType.SHULKER.getDescription().getString(),
                     EntityType.ENDER_DRAGON.getDescription().getString()
               }));
   public static final ArcanaAchievement HISTORY_CARVED_IN_STONE = ArcanaAchievements.register(
         new EventAchievement("history_carved_in_stone", ArcanaRegistry.SOULSTONE.getPrefItemNoLore(), ArcanaRegistry.SPEAR_OF_TENBROUS, 100000, 5));
   public static final ArcanaAchievement ZERAIYA = ArcanaAchievements.register(
         new EventAchievement("zeraiya", new ItemStack(Items.DRAGON_EGG), ArcanaRegistry.SPEAR_OF_TENBROUS, 100000, 10)).setHidden(true);
   
   // Geomantic Stele
   public static final ArcanaAchievement ARTIFICIAL_VOLCANO = ArcanaAchievements.register(
         new EventAchievement("artificial_volcano", ArcanaRegistry.MAGMATIC_EVERSOURCE.getPrefItemNoLore(), ArcanaRegistry.GEOMANTIC_STELE, 1000, 1));
   public static final ArcanaAchievement ARTIFICIAL_GEYSER = ArcanaAchievements.register(
         new EventAchievement("artificial_geyser", ArcanaRegistry.AQUATIC_EVERSOURCE.getPrefItemNoLore(), ArcanaRegistry.GEOMANTIC_STELE, 1000, 1));
   public static final ArcanaAchievement MONOLITH_OF_FEAR = ArcanaAchievements.register(
         new EventAchievement("monolith_of_fear", ArcanaRegistry.FELIDAE_CHARM.getPrefItemNoLore(), ArcanaRegistry.GEOMANTIC_STELE, 1000, 1));
   public static final ArcanaAchievement DOCTOR_STONE = ArcanaAchievements.register(
         new EventAchievement("doctor_stone", ArcanaRegistry.CLEANSING_CHARM.getPrefItemNoLore(), ArcanaRegistry.GEOMANTIC_STELE, 1000, 1));
   public static final ArcanaAchievement KOKOPELLI = ArcanaAchievements.register(
         new EventAchievement("kokopelli", new ItemStack(Items.WHEAT), ArcanaRegistry.GEOMANTIC_STELE, 1000, 1));
   
   // Interdictor
   public static final ArcanaAchievement UNMOBBED = ArcanaAchievements.register(
         new ProgressAchievement("unmobbed", new ItemStack(Items.GUNPOWDER), ArcanaRegistry.INTERDICTOR, 5000, 2, 10000));
   
   // Ender Crate
   public static final ArcanaAchievement OUT_OF_THE_BOX = ArcanaAchievements.register(
         new EventAchievement("out_of_the_box", new ItemStack(Items.HOPPER), ArcanaRegistry.ENDER_CRATE, 1000, 1));
   public static final ArcanaAchievement ENDERON_PRIME = ArcanaAchievements.register(
         new ProgressAchievement("enderon_prime", new ItemStack(Items.BARREL), ArcanaRegistry.ENDER_CRATE, 7500, 3, 10000));
   public static final ArcanaAchievement SECURITY_RAINBOW = ArcanaAchievements.register(
         new EventAchievement("security_rainbow", new ItemStack(Items.CYAN_DYE), ArcanaRegistry.ENDER_CRATE, 1500, 1));
   
   // Astral Gateway
   public static final ArcanaAchievement FANCIER_STARGATE = ArcanaAchievements.register(
         new EventAchievement("fancier_stargate", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.ASTRAL_GATEWAY, 2500, 2));
   public static final ArcanaAchievement CARRIER_HAS_ARRIVED = ArcanaAchievements.register(
         new EventAchievement("carrier_has_arrived", new ItemStack(Items.PALE_OAK_BOAT), ArcanaRegistry.ASTRAL_GATEWAY, 7500, 3));
   public static final ArcanaAchievement ON_YOUR_LEFT = ArcanaAchievements.register(
         new EventAchievement("on_your_left", new ItemStack(Items.PLAYER_HEAD), ArcanaRegistry.ASTRAL_GATEWAY, 2500, 2));
   public static final ArcanaAchievement DIALING_HELL = ArcanaAchievements.register(
         new EventAchievement("dialing_hell", new ItemStack(Items.NETHERRACK), ArcanaRegistry.ASTRAL_GATEWAY, 2500, 2));
   public static final ArcanaAchievement MODERATELY_INSANE = ArcanaAchievements.register(
         new EventAchievement("moderately_insane", new ItemStack(Items.NETHERITE_BLOCK), ArcanaRegistry.ASTRAL_GATEWAY, 50000, 4));
   
   // Clockwork Multitool
   public static final ArcanaAchievement FIDGET_TOY = ArcanaAchievements.register(
         new ProgressAchievement("fidget_toy", new ItemStack(Items.CLOCK), ArcanaRegistry.CLOCKWORK_MULTITOOL, 2500, 2, 1000));
   
   // Charm of Negotiation
   public static final ArcanaAchievement WOLF_OF_BLOCK_STREET = ArcanaAchievements.register(
         new ProgressAchievement("wolf_of_block_street", new ItemStack(Items.GOLDEN_HELMET), ArcanaRegistry.NEGOTIATION_CHARM, 10000, 3, 1000));
   
   // Itineranteur
   public static final ArcanaAchievement ARCANA_BOULEVARD = ArcanaAchievements.register(
         new ProgressAchievement("arcana_boulevard", new ItemStack(Items.CHERRY_SIGN), ArcanaRegistry.ITINERANTEUR, 50000, 4, 1000000));
   
   
   static{
      EXCLUDED_ACHIEVEMENTS.addAll(getItemAchievements(ArcanaRegistry.PICKAXE_OF_CEPTYUS));
      EXCLUDED_ACHIEVEMENTS.addAll(getItemAchievements(ArcanaRegistry.WINGS_OF_ENDERIA));
      EXCLUDED_ACHIEVEMENTS.addAll(getItemAchievements(ArcanaRegistry.LEADERSHIP_CHARM));
      EXCLUDED_ACHIEVEMENTS.addAll(getItemAchievements(ArcanaRegistry.GREAVES_OF_GAIALTUS));
      EXCLUDED_ACHIEVEMENTS.addAll(getItemAchievements(ArcanaRegistry.SPEAR_OF_TENBROUS));
      EXCLUDED_ACHIEVEMENTS.add(ALL_ACHIEVEMENTS);
   }
   
   
   private static ArcanaAchievement register(ArcanaAchievement achievement){
      String id = achievement.id;
      ARCANA_ACHIEVEMENTS.put(id, achievement);
      return achievement;
   }
   
   public static List<ArcanaAchievement> getItemAchievements(ArcanaItem item){
      ArrayList<ArcanaAchievement> achs = new ArrayList<>();
      for(Map.Entry<String, ArcanaAchievement> entry : ARCANA_ACHIEVEMENTS.entrySet()){
         if(entry.getValue().getArcanaItem().getId().equals(item.getId())) achs.add(entry.getValue());
      }
      return achs;
   }
   
   public static void grant(ServerPlayer player, ArcanaAchievement ach){
      grant(player.getUUID(),ach);
   }
   
   public static void grant(UUID player, ArcanaAchievement ach){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof ProgressAchievement baseAch){
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            ProgressAchievement newAch = baseAch.makeNew();
            newAch.setProgress(baseAch.getGoal());
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.setProgress(baseAch.getGoal());
            profile.setAchievement(achievement);
         }
         baseAch.announceAcquired(player);
      }else if(ach instanceof EventAchievement baseAch){
         EventAchievement achievement = (EventAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            EventAchievement newAch = baseAch.makeNew();
            newAch.setAcquired(true);
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.setAcquired(true);
            profile.setAchievement(achievement);
         }
         baseAch.announceAcquired(player);
      }else if(ach instanceof ConditionalsAchievement baseAch){
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            for(Map.Entry<String, Boolean> entry : newAch.getConditions().entrySet()){
               newAch.setCondition(entry.getKey(), true);
            }
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            for(Map.Entry<String, Boolean> entry : achievement.getConditions().entrySet()){
               achievement.setCondition(entry.getKey(), true);
            }
            profile.setAchievement(achievement);
         }
         baseAch.announceAcquired(player);
      }else if(ach instanceof TimedAchievement baseAch){
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            newAch.addProgress(baseAch.getGoal());
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.addProgress(baseAch.getGoal());
            profile.setAchievement(achievement);
         }
         baseAch.announceAcquired(player);
      }
   }
   
   public static void setCondition(ServerPlayer player, ArcanaAchievement ach, String condition, boolean set){
      setCondition(player.getUUID(),ach,condition,set);
   }
   
   public static void setCondition(UUID player, ArcanaAchievement ach, String condition, boolean set){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof ConditionalsAchievement baseAch){
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(baseAch);
         boolean get;
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            get = newAch.setCondition(condition, set);
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setCondition(condition, set);
            profile.setAchievement(achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static void progress(ServerPlayer player, ArcanaAchievement ach, int toAdd){
      progress(player.getUUID(),ach,toAdd);
   }
   
   public static void progress(UUID player, ArcanaAchievement ach, int toAdd){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof ProgressAchievement baseAch){
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(baseAch);
         boolean get;
         if(achievement == null){
            ProgressAchievement newAch = baseAch.makeNew();
            get = newAch.setProgress(toAdd);
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setProgress(achievement.getProgress() + toAdd);
            profile.setAchievement(achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }else if(ach instanceof TimedAchievement baseAch){
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(baseAch);
         boolean get;
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            get = newAch.addProgress(toAdd);
            profile.setAchievement(newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.addProgress(toAdd);
            profile.setAchievement(achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static int getProgress(ServerPlayer player, ArcanaAchievement ach){
      return getProgress(player.getUUID(),ach);
   }
   
   public static int getProgress(UUID player, ArcanaAchievement ach){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof ProgressAchievement baseAch){
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            return 0;
         }else{
            return achievement.getProgress();
         }
      }else if(ach instanceof TimedAchievement baseAch){
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(baseAch);
         if(achievement == null){
            return 0;
         }else{
            return achievement.getProgress();
         }
      }
      return -1;
   }
   
   public static void revoke(ServerPlayer player, ArcanaAchievement ach){
      revoke(player.getUUID(),ach);
   }
   
   public static void revoke(UUID player, ArcanaAchievement ach){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      profile.removeAchievement(ach);
   }
   
   public static void reset(ServerPlayer player, ArcanaAchievement ach){
      reset(player.getUUID(),ach);
   }
   
   public static void reset(UUID player, ArcanaAchievement ach){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof TimedAchievement baseAch){
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(baseAch);
         if(achievement != null){
            if(achievement.isAcquired()) return;
            achievement.reset();
            profile.setAchievement(achievement);
         }
      }
   }
   
   public static boolean isTimerActive(ServerPlayer player, ArcanaAchievement ach){
      return isTimerActive(player.getUUID(),ach);
   }
   
   public static boolean isTimerActive(UUID player, ArcanaAchievement ach){
      ArcanaPlayerData profile = ArcanaNovum.data(player);
      if(ach instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(baseAch);
         if(achievement != null){
            if(achievement.isAcquired()) return false;
            return achievement.isActive();
         }
      }
      return false;
   }
}
