package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

public class ArcanaAchievements {
   public static final HashMap<String, ArcanaAchievement> registry = new HashMap<>();
   public static final List<ArcanaAchievement> excludedAchievements = new ArrayList<>();
   
   // Divine Catalyst
   public static final ArcanaAchievement DOOR_OF_DIVINITY = ArcanaAchievements.register(
         new EventAchievement("Door of Divinity", "door_of_divinity", new ItemStack(Items.AMETHYST_SHARD), ArcanaRegistry.DIVINE_CATALYST, 1000, 1,
               new String[]{"Summon a Nul Construct"}
         ));
   public static final ArcanaAchievement CONSTRUCT_DECONSTRUCTED = ArcanaAchievements.register(
         new EventAchievement("Construct Deconstructed", "construct_deconstructed", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.DIVINE_CATALYST, 5000, 2,
               new String[]{"Successfully Defeat a Nul Construct"}
         ));
   
   public static final ArcanaAchievement DIVINE_TRANSMUTATION = ArcanaAchievements.register(
         new EventAchievement("A Divine Transmutation", "divine_transmutation", new ItemStack(Items.DIAMOND), ArcanaRegistry.DIVINE_CATALYST, 10000, 3,
               new String[]{"Use an Altar of Transmutation to get a Divine Catalyst"}
         ));
   
   // Arcane Flak Arrows
   public static final ArcanaAchievement AA_ARTILLERY = ArcanaAchievements.register(
         new EventAchievement("Aerial Bane", "aa_artillery", new ItemStack(Items.FIREWORK_STAR), ArcanaRegistry.ARCANE_FLAK_ARROWS, 5000, 2,
               new String[]{"Kill 5 phantoms with a single Arcane Flak Arrow"}
         ));
   
   // Blink Arrows
   public static final ArcanaAchievement NOW_YOU_SEE_ME = ArcanaAchievements.register(
         new EventAchievement("Now You See Me", "now_you_see_me", new ItemStack(Items.FEATHER), ArcanaRegistry.BLINK_ARROWS, 1000, 1,
               new String[]{"Teleport over 100 blocks with a Blink Arrow"}
         ));
   
   // Concussion Arrows
   public static final ArcanaAchievement SHOCK_AWE = ArcanaAchievements.register(
         new EventAchievement("Shock and Awe", "shock_awe", new ItemStack(Items.BLACK_DYE), ArcanaRegistry.CONCUSSION_ARROWS, 1000, 1,
               new String[]{"Stun 10 mobs with a single Concussion Arrow"}
         ));
   
   // Detonation Arrows
   public static final ArcanaAchievement SAFETY_THIRD = ArcanaAchievements.register(
         new EventAchievement("Safety Third", "safety_third", new ItemStack(Items.TNT), ArcanaRegistry.DETONATION_ARROWS, 1000, 1,
               new String[]{"Almost kill yourself with your own Detonation Arrow"}
         ));
   
   // Expulsion Arrows
   public static final ArcanaAchievement JUMP_PAD = ArcanaAchievements.register(
         new EventAchievement("Jump Pad", "jump_pad", new ItemStack(Items.SLIME_BLOCK), ArcanaRegistry.EXPULSION_ARROWS, 1000, 1,
               new String[]{"Launch yourself in the air with an Expulsion Arrow"}
         ));
   
   // Graviton Arrows
   public static final ArcanaAchievement BRING_TOGETHER = ArcanaAchievements.register(
         new EventAchievement("Bringing People Together", "bring_together", new ItemStack(Items.COBWEB), ArcanaRegistry.GRAVITON_ARROWS, 1000, 1,
               new String[]{"Attract 10 mobs with a single Graviton Arrow"}
         ));
   
   // Photonic Arrows
   public static final ArcanaAchievement X = ArcanaAchievements.register(
         new EventAchievement("X", "x", new ItemStack(Items.STRUCTURE_VOID), ArcanaRegistry.PHOTONIC_ARROWS, 5000, 2,
               new String[]{"Kill 10 mobs with a single Photonic Arrow"}
         ));
   
   // Siphoning Arrows
   public static final ArcanaAchievement CIRCLE_OF_LIFE = ArcanaAchievements.register(
         new EventAchievement("Circle of Life", "circle_of_life", new ItemStack(Items.APPLE), ArcanaRegistry.SIPHONING_ARROWS, 2000, 1,
               new String[]{"Kill a mob with a Siphoning Arrow", "when you are at half a heart"}
         ));
   
   // Smoke Arrows
   public static final ArcanaAchievement SMOKE_SCREEN = ArcanaAchievements.register(
         new EventAchievement("Smoke Screen", "smoke_screen", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), ArcanaRegistry.SMOKE_ARROWS, 1000, 1,
               new String[]{"Hide amongst your own smoke along with 3 hostile mobs"}
         ));
   
   // Storm Arrows
   public static final ArcanaAchievement SHOCK_THERAPY = ArcanaAchievements.register(
         new EventAchievement("Shock Conversion Therapy", "shock_therapy", new ItemStack(Items.SUSPICIOUS_STEW), ArcanaRegistry.STORM_ARROWS, 2500, 1,
               new String[]{"Use a Storm Arrow to convert a red", "mooshroom to a brown mooshroom"}
         ));
   
   // Tether Arrows
   public static final ArcanaAchievement SPIDERMAN = ArcanaAchievements.register(
         new TimedAchievement("Friendly Neighborhood Menace", "spiderman", new ItemStack(Items.COBWEB), ArcanaRegistry.TETHER_ARROWS, 1000, 1,
               new String[]{"Swing between 5 tall structures in under 15 seconds"}, 5, 300
         ));
   
   // Charm of Cinders
   public static final ArcanaAchievement PYROMANIAC = ArcanaAchievements.register(
         new EventAchievement("Pyromaniac", "pyromaniac", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.CINDERS_CHARM, 1000, 1,
               new String[]{"Set a dozen creatures on fire with a single", "use of the Charm of Cinder's Active Ability"}
         ));
   public static final ArcanaAchievement CAKE_DAY = ArcanaAchievements.register(
         new EventAchievement("Happy Cake Day", "cake_day", new ItemStack(Items.CAKE), ArcanaRegistry.CINDERS_CHARM, 1000, 1,
               new String[]{"Use the Charm of Cinders to light a birthday candle"}
         ));
   public static final ArcanaAchievement GLASSBLOWER = ArcanaAchievements.register(
         new ProgressAchievement("Glassblower", "glassblower", new ItemStack(Items.GLASS), ArcanaRegistry.CINDERS_CHARM, 10000, 3,
               new String[]{"Smelt over 10,000 glass with the", "Charm of Cinder's auto-smelt ability"}, 10000
         ));
   
   // Charm of Feasting
   public static final ArcanaAchievement TARRARE = ArcanaAchievements.register(
         new ConditionalsAchievement("Tarrare", "tarrare", new ItemStack(Items.SPIDER_EYE), ArcanaRegistry.FEASTING_CHARM, 5000, 2,
               new String[]{"Have the Feasting Charm feed you a", "Poisonous Potato, Spider Eye, Rotten Flesh", "Suspicious Stew, Raw Chicken and Pufferfish"},
               new String[]{"Poisonous Potato", "Spider Eye", "Rotten Flesh", "Suspicious Stew", "Raw Chicken", "Pufferfish"}
         ));
   
   // Charm of Felidae
   public static final ArcanaAchievement LAND_ON_FEET = ArcanaAchievements.register(
         new EventAchievement("Always on Your Feet", "land_on_feet", new ItemStack(Items.LEATHER_BOOTS), ArcanaRegistry.FELIDAE_CHARM, 5000, 2,
               new String[]{"Have the Felidae Charm save you from a lethal fall"}
         ));
   public static final ArcanaAchievement INFILTRATION = ArcanaAchievements.register(
         new EventAchievement("Infiltration", "infiltration", new ItemStack(Items.CREEPER_HEAD), ArcanaRegistry.FELIDAE_CHARM, 1000, 1,
               new String[]{"Walk amongst a group of creepers"}
         ));
   
   // Charm of Leadership
   public static final ArcanaAchievement RAID_LEADER = ArcanaAchievements.register(
         new EventAchievement("Raid Leader", "raid_leader", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.LEADERSHIP_CHARM, 1000, 1,
               new String[]{"Have at least 5 players in the", "Charm of Leadership's radius"}
         ));
   
   // Charm of Light
   public static final ArcanaAchievement ENLIGHTENED = ArcanaAchievements.register(
         new ProgressAchievement("Enlightened", "enlightened", new ItemStack(Items.LIGHT), ArcanaRegistry.LIGHT_CHARM, 5000, 2,
               new String[]{"Place 1000 lights with the Charm of Light"}, 1000
         ));
   
   // Charm of Magnetism
   public static final ArcanaAchievement MAGNETS = ArcanaAchievements.register(
         new EventAchievement("That's Not How Magnets Work", "magnets", new ItemStack(Items.IRON_ORE), ArcanaRegistry.MAGNETISM_CHARM, 1000, 1,
               new String[]{"Pull at least 25 different items with", "the Charm of Magnetism's active ability"}
         ));
   
   // Ancient Dowsing Rod
   public static final ArcanaAchievement MOTHERLOAD = ArcanaAchievements.register(
         new EventAchievement("Found the Motherload", "motherload", new ItemStack(Items.ANCIENT_DEBRIS), ArcanaRegistry.ANCIENT_DOWSING_ROD, 1000, 1,
               new String[]{"Reveal at least 10 debris with a single", "use of the Ancient Dowsing Rod"}
         ));
   public static final ArcanaAchievement ARCHEOLOGIST = ArcanaAchievements.register(
         new ProgressAchievement("Ancient Archeologist", "archeologist", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.ANCIENT_DOWSING_ROD, 5000, 2,
               new String[]{"Reveal 1,000 total debris with", "the Ancient Dowsing Rod"}, 1000
         ));
   
   // Arcane Tome / Generic
   public static final ArcanaAchievement CLOSE_CALL = ArcanaAchievements.register(
         new EventAchievement("Close Call", "close_call", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.ARCANE_TOME, 1000, 1,
               new String[]{"Survive concentration damage with half a heart"}
         ));
   public static final ArcanaAchievement GOD_BOON = ArcanaAchievements.register(
         new EventAchievement("Boon of the True Gods", "god_boon", new ItemStack(Items.DRAGON_EGG), ArcanaRegistry.ARCANE_TOME, 10000, 3,
               new String[]{"Obtain a Divine Arcana Item"}
         ));
   public static final ArcanaAchievement ARCANE_ADDICT = ArcanaAchievements.register(
         new EventAchievement("Arcane Addict", "arcane_addict", new ItemStack(Items.KNOWLEDGE_BOOK), ArcanaRegistry.ARCANE_TOME, 50000, 4,
               new String[]{"Have 30 Arcana Items taking concentration"}
         ));
   public static final ArcanaAchievement ALL_ACHIEVEMENTS = ArcanaAchievements.register(
         new EventAchievement("One With the Abyss", "all_achievements", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.ARCANE_TOME, 1000000, 666,
               new String[]{"Unlock all Achievements for currently obtainable items", "(Grants Unobtainable Achievements honorarily)"}
         ));
   
   // Brain in a Jar
   public static final ArcanaAchievement BREAK_BANK = ArcanaAchievements.register(
         new EventAchievement("Break the Bank", "break_bank", new ItemStack(Items.ENDER_CHEST), ArcanaRegistry.BRAIN_JAR, 10000, 3,
               new String[]{"Completely fill the Brain in a Jar"}
         ));
   public static final ArcanaAchievement CERTIFIED_REPAIR = ArcanaAchievements.register(
         new ProgressAchievement("Certified Repairman", "certified_repair", new ItemStack(Items.EXPERIENCE_BOTTLE), ArcanaRegistry.BRAIN_JAR, 25000, 3,
               new String[]{"Repair 100,000 damage with the", "Brain in a Jar's Mending ability"}, 100000
         ));
   
   // Continuum Anchor
   public static final ArcanaAchievement TIMEY_WIMEY = ArcanaAchievements.register(
         new ProgressAchievement("Timey Wimey", "timey_wimey", new ItemStack(Items.CLOCK), ArcanaRegistry.CONTINUUM_ANCHOR, 100000, 5,
               new String[]{"Have your Continuum Anchors keep", "chunks loaded for a total of a month"}, 2629744
         ));
   
   // Essence Egg
   public static final ArcanaAchievement SOUL_CONVERSION = ArcanaAchievements.register(
         new EventAchievement("Soul Conversion", "soul_conversion", new ItemStack(Items.SPAWNER), ArcanaRegistry.ESSENCE_EGG, 5000, 2,
               new String[]{"Use the Essence Egg to switch a spawner type"}
         ));
   public static final ArcanaAchievement SOUL_FOR_SOUL = ArcanaAchievements.register(
         new ProgressAchievement("A Soul for a Soul", "soul_for_soul", new ItemStack(Items.CHICKEN_SPAWN_EGG), ArcanaRegistry.ESSENCE_EGG, 10000, 3,
               new String[]{"Use the Essence Egg to spawn 25 new creatures"}, 25
         ));
   
   // Fractal Sponge
   public static final ArcanaAchievement OCEAN_CLEANUP = ArcanaAchievements.register(
         new ProgressAchievement("Ocean Cleanup", "ocean_cleanup", new ItemStack(Items.SEAGRASS), ArcanaRegistry.FRACTAL_SPONGE, 5000, 2,
               new String[]{"Remove a total of 10,000 blocks of", "water or lava with the Fractal Sponge"}, 10000
         ));
   
   // Igneous Collider
   public static final ArcanaAchievement ENDLESS_EXTRUSION = ArcanaAchievements.register(
         new ProgressAchievement("Endless Extrusion", "endless_extrusion", new ItemStack(Items.OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER, 15000, 3,
               new String[]{"Make over 10,000 blocks of obsidian or", "crying obsidian with your Igneous Colliders"}, 10000
         ));
   public static final ArcanaAchievement EXPENSIVE_INFUSION = ArcanaAchievements.register(
         new EventAchievement("Expensive Infusion", "expensive_infusion", new ItemStack(Items.CRYING_OBSIDIAN), ArcanaRegistry.IGNEOUS_COLLIDER, 5000, 2,
               new String[]{"Make a piece of crying obsidian with", "one of your Igneous Colliders"}
         ));
   
   // Levitation Harness
   public static final ArcanaAchievement TO_THE_MOON = ArcanaAchievements.register(
         new EventAchievement("Fly Me to the Moon", "to_the_moon", new ItemStack(Items.END_STONE), ArcanaRegistry.LEVITATION_HARNESS, 2500, 1,
               new String[]{"Reach a height of over 1000 blocks", "with the Levitation Harness"}
         ));
   public static final ArcanaAchievement FREQUENT_FLIER = ArcanaAchievements.register(
         new ProgressAchievement("Frequent Flier", "frequent_flier", new ItemStack(Items.FEATHER), ArcanaRegistry.LEVITATION_HARNESS, 15000, 3,
               new String[]{"Use the Levitation Harness for 24 Hours"}, 86400
         ));
   
   // Nul Memento
   public static final ArcanaAchievement DIVINE_FAVOR = ArcanaAchievements.register(
         new EventAchievement("Divine Favor", "divine_favor", new ItemStack(Items.AMETHYST_CLUSTER), ArcanaRegistry.NUL_MEMENTO, 100000, 5,
               new String[]{"Receive a Nul Memento from a Nul Construct"}
         ));
   public static final ArcanaAchievement AMNESIAC = ArcanaAchievements.register(
         new ProgressAchievement("Amnesiac", "amnesiac", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.NUL_MEMENTO, 100000, 5,
               new String[]{"Use a Nul Memento twice"}, 2
         ));
   public static final ArcanaAchievement DEATHS_DOOR = ArcanaAchievements.register(
         new EventAchievement("Death's Door", "deaths_door", new ItemStack(Items.TOTEM_OF_UNDYING), ArcanaRegistry.NUL_MEMENTO, 2500, 1,
               new String[]{"'Die' while activating a Nul Memento"}
         ));
   public static final ArcanaAchievement LOST_KNOWLEDGE = ArcanaAchievements.register(
         new EventAchievement("Lost Knowledge", "lost_knowledge", new ItemStack(Items.KNOWLEDGE_BOOK), ArcanaRegistry.NUL_MEMENTO, 2500, 1,
               new String[]{"Use a Nul Memento"}
         ));
   
   // Overflowing Quiver
   public static final ArcanaAchievement SPARE_STOCK = ArcanaAchievements.register(
         new ProgressAchievement("Spare Stock", "spare_stock", new ItemStack(Items.ARROW), ArcanaRegistry.OVERFLOWING_QUIVER, 5000, 2,
               new String[]{"Have the Overflowing Quiver restock 200 arrows"}, 200
         ));
   public static final ArcanaAchievement DIVERSE_ARSENAL = ArcanaAchievements.register(
         new EventAchievement("Diverse Arsenal", "diverse_arsenal", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.OVERFLOWING_QUIVER, 2000, 1,
               new String[]{"Put 9 different Tipped Arrows in your quiver"}
         ));
   
   // Pearl of Recall
   public static final ArcanaAchievement BACK_TO_HELL = ArcanaAchievements.register(
         new EventAchievement("Back to Hell", "back_to_hell", new ItemStack(Items.NETHERRACK), ArcanaRegistry.PEARL_OF_RECALL, 1500, 1,
               new String[]{"Use a Pearl of Recall with the", "destination in The Nether"}
         ));
   public static final ArcanaAchievement ASCENDING_TO_HEAVEN = ArcanaAchievements.register(
         new EventAchievement("Ascending to Heaven", "ascending_to_heaven", new ItemStack(Items.END_STONE), ArcanaRegistry.PEARL_OF_RECALL, 1500, 1,
               new String[]{"Use a Pearl of Recall with", "the destination in The End"}
         ));
   
   // Pickaxe of Ceptyus
   public static final ArcanaAchievement BACK_IN_THE_MINE = ArcanaAchievements.register(
         new ProgressAchievement("So We Back in the Mine", "back_in_the_mine", new ItemStack(Items.DIAMOND_PICKAXE), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 5000, 2,
               new String[]{"Spend one hour at full haste"}, 72000
         ));
   public static final ArcanaAchievement DIGGY_HOLE = ArcanaAchievements.register(
         new ProgressAchievement("Diggy Diggy Hole", "diggy_hole", new ItemStack(Items.COBBLESTONE), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 100000, 5,
               new String[]{"Mine 1,000,000 stone with the Pickaxe of Ceptyus"}, 1000000
         ));
   public static final ArcanaAchievement MINE_DIAMONDS = ArcanaAchievements.register(
         new EventAchievement("Mine Diamonds", "mine_diamonds", new ItemStack(Items.DIAMOND), ArcanaRegistry.PICKAXE_OF_CEPTYUS, 2500, 1,
               new String[]{"Mine at least 12 diamond ore with a single use", "of the Pickaxe of Ceptyus's vein mine ability"}
         ));
   
   // Runic Bow
   public static final ArcanaAchievement JUST_LIKE_ARCHER = ArcanaAchievements.register(
         new ProgressAchievement("Just Like <Famous Fictional Archer>", "just_like_archer", new ItemStack(Items.BOW), ArcanaRegistry.RUNIC_BOW, 1000, 3,
               new String[]{"Shoot 1,000 Runic Arrows"}, 1000
         ));
   public static final ArcanaAchievement AIMBOT = ArcanaAchievements.register(
         new EventAchievement("Aimbot", "aimbot", new ItemStack(Items.TARGET), ArcanaRegistry.RUNIC_BOW, 5000, 2,
               new String[]{"Hit a creature from over 100", "blocks away with a Runic Arrow"}
         ));
   
   // Runic Quiver
   public static final ArcanaAchievement UNLIMITED_STOCK = ArcanaAchievements.register(
         new ProgressAchievement("Unlimited Stock", "unlimited_stock", new ItemStack(Items.ARROW), ArcanaRegistry.RUNIC_QUIVER, 5000, 2,
               new String[]{"Have the Runic Quiver restock 500 arrows"}, 500
         ));
   public static final ArcanaAchievement ARROW_FOR_EVERY_FOE = ArcanaAchievements.register(
         new TimedAchievement("An Arrow for Every Foe", "arrow_for_every_foe", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RUNIC_QUIVER, 10000, 3,
               new String[]{"Shoot and hit 9 different Runic", "Arrows in under 20 seconds"}, 9, 400
         ));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAchievement OMAE_WA = ArcanaAchievements.register(
         new TimedAchievement("Omae Wa Mou Shindeiru", "omae_wa", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.SHADOW_STALKERS_GLAIVE, 7500, 2,
               new String[]{"Kill a player or warden within a", "second of stalking behind them"}, 1, 20
         ));
   public static final ArcanaAchievement SHADOW_FURY = ArcanaAchievements.register(
         new TimedAchievement("Shadow Fury", "shadow_fury", new ItemStack(Items.WITHER_ROSE), ArcanaRegistry.SHADOW_STALKERS_GLAIVE, 5000, 2,
               new String[]{"Stalk behind, and then kill", "8 mobs within 30 seconds"}, 15, 600
         ));
   
   // Shield of Fortitude
   public static final ArcanaAchievement BUILT_LIKE_TANK = ArcanaAchievements.register(
         new EventAchievement("Built Like a Tank", "built_like_tank", new ItemStack(Items.IRON_BLOCK), ArcanaRegistry.SHIELD_OF_FORTITUDE, 5000, 2,
               new String[]{"Have over 100 absorption hearts at", "once from the Shield of Fortitude"}
         ));
   
   // Shulker Core
   public static final ArcanaAchievement MILE_HIGH = ArcanaAchievements.register(
         new EventAchievement("Mile High Club", "mile_high", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.SHULKER_CORE, 1500, 1,
               new String[]{"Use the Shulker Core to reach a", "height of one mile (1610 blocks)"}
         ));
   
   // Sojourner's Boots
   public static final ArcanaAchievement RUNNING = ArcanaAchievements.register(
         new TimedAchievement("Running in the 90s", "running", new ItemStack(Items.GOLDEN_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS, 10000, 3,
               new String[]{"Spend 5 consecutive minutes at full speed"}, 5900, 6001
         ));
   public static final ArcanaAchievement PHEIDIPPIDES = ArcanaAchievements.register(
         new ProgressAchievement("Pheidippides", "pheidippides", new ItemStack(Items.NETHERITE_BOOTS), ArcanaRegistry.SOJOURNER_BOOTS, 100000, 5,
               new String[]{"Run a total of 246 kilometers with the Sojourner's Boots"}, 24600000
         ));
   
   // Soulstone
   public static final ArcanaAchievement TOOK_A_VILLAGE = ArcanaAchievements.register(
         new EventAchievement("It Took a Village", "took_a_village", new ItemStack(Items.EMERALD), ArcanaRegistry.SOULSTONE, 5000, 2,
               new String[]{"Obtain a tier 3 Villager Soulstone"}
         ));
   public static final ArcanaAchievement PHILOSOPHER_STONE = ArcanaAchievements.register(
         new EventAchievement("Philosopher's Stone", "philosopher_stone", new ItemStack(Items.REDSTONE_BLOCK), ArcanaRegistry.SOULSTONE, 10000, 3,
               new String[]{"Obtain a tier 5 Soulstone"}
         ));
   public static final ArcanaAchievement PRIME_EVIL = ArcanaAchievements.register(
         new EventAchievement("Prime Evil", "prime_evil", new ItemStack(Items.BLACKSTONE), ArcanaRegistry.SOULSTONE, 100000, 5,
               new String[]{"Obtain a max tier Soulstone"}
         ));
   
   // Spawner Harness
   public static final ArcanaAchievement FINALLY_USEFUL = ArcanaAchievements.register(
         new EventAchievement("Finally Useful", "finally_useful", new ItemStack(Items.IRON_NUGGET), ArcanaRegistry.SPAWNER_HARNESS, 1000, 1,
               new String[]{"Use the Spawner Harness on a", "silverfish spawner from a stronghold"}
         ));
   
   // Spawner Infuser
   public static final ArcanaAchievement HUMBLE_NECROMANCER = ArcanaAchievements.register(
         new EventAchievement("A Humble Necromancer", "humble_necromancer", new ItemStack(Items.SKELETON_SKULL), ArcanaRegistry.SPAWNER_INFUSER, 1000, 1,
               new String[]{"Upgrade a single stat in a Spawner Infuser"}
         ));
   public static final ArcanaAchievement SCULK_HUNGERS = ArcanaAchievements.register(
         new EventAchievement("The Sculk Hungers", "sculk_hungers", new ItemStack(Items.SCULK), ArcanaRegistry.SPAWNER_INFUSER, 10000, 3,
               new String[]{"Put 4 stacks of points into a single", "stat in a Spawner Infuser"}
         ));
   public static final ArcanaAchievement ARCHLICH = ArcanaAchievements.register(
         new EventAchievement("Archlich", "archlich", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.SPAWNER_INFUSER, 25000, 3,
               new String[]{"Put 8 stacks of points into a Spawner Infuser"}
         ));
   public static final ArcanaAchievement INNOCENT_SOULS = ArcanaAchievements.register(
         new EventAchievement("Souls of the Innocent", "innocent_souls", new ItemStack(Items.SOUL_SAND), ArcanaRegistry.SPAWNER_INFUSER, 50000, 4,
               new String[]{"Use a max tier Soulstone in the Spawner Infuser"}
         ));
   public static final ArcanaAchievement POWER_OVERWHELMING = ArcanaAchievements.register(
         new EventAchievement("POWER OVERWHELMING!", "power_overwhelming", new ItemStack(Items.END_CRYSTAL), ArcanaRegistry.SPAWNER_INFUSER, 100000, 5,
               new String[]{"Reach the full potential of the Spawner Infuser"}
         ));
   
   // Stasis Pearl
   public static final ArcanaAchievement PEARL_HANG = ArcanaAchievements.register(
         new EventAchievement("Pearl Hang", "pearl_hang", new ItemStack(Items.CLOCK), ArcanaRegistry.STASIS_PEARL, 1000, 1,
               new String[]{"Leave a pearl in stasis for", "5 minutes before re-activating it"}
         ));
   public static final ArcanaAchievement INSTANT_TRANSMISSION = ArcanaAchievements.register(
         new EventAchievement("Instant Transmission", "instant_transmission", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.STASIS_PEARL, 1500, 1,
               new String[]{"Use a Stasis Pearl to teleport a", "distance of over 1000 blocks"}
         ));
   
   // Telescoping Beacon
   public static final ArcanaAchievement ART_OF_THE_DEAL = ArcanaAchievements.register(
         new EventAchievement("The Art of the Deal", "art_of_the_deal", new ItemStack(Items.EMERALD_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 5000, 2,
               new String[]{"Use the Telescoping Beacon to", "deploy a tier 4 Emerald Beacon"}
         ));
   public static final ArcanaAchievement ACQUISITION_RULES = ArcanaAchievements.register(
         new EventAchievement("The Rules of Acquisition", "acquisition_rules", new ItemStack(Items.GOLD_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 10000, 3,
               new String[]{"Use the Telescoping Beacon to", "deploy a tier 4 Gold Beacon"}
         ));
   public static final ArcanaAchievement BEJEWELED = ArcanaAchievements.register(
         new EventAchievement("Bejeweled", "bejeweled", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 50000, 4,
               new String[]{"Use the Telescoping Beacon to", "deploy a tier 4 Diamond Beacon"}
         ));
   public static final ArcanaAchievement CLINICALLY_INSANE = ArcanaAchievements.register(
         new EventAchievement("Clinically Insane", "clinically_insane", new ItemStack(Items.NETHERITE_BLOCK), ArcanaRegistry.TELESCOPING_BEACON, 100000, 5,
               new String[]{"Use the Telescoping Beacon to", "deploy a tier 4 Netherite Beacon"}
         ));
   
   // Wings of Enderia
   public static final ArcanaAchievement SEE_GLASS = ArcanaAchievements.register(
         new EventAchievement("Can't See Glass", "see_glass", new ItemStack(Items.GLASS), ArcanaRegistry.WINGS_OF_ENDERIA, 5000, 2,
               new String[]{"Have The Armored Wings of Enderia save you", "from lethal kinetic damage from ramming into a block"}
         ));
   public static final ArcanaAchievement ANGEL_OF_DEATH = ArcanaAchievements.register(
         new EventAchievement("Angel of Death", "angel_of_death", new ItemStack(Items.WITHER_SKELETON_SKULL), ArcanaRegistry.WINGS_OF_ENDERIA, 1000, 1,
               new String[]{"Kill a mob while flying with The Armored Wings of Enderia"}
         ));
   public static final ArcanaAchievement CROW_FATHER = ArcanaAchievements.register(
         new EventAchievement("Crow Father", "crow_father", new ItemStack(Items.FEATHER), ArcanaRegistry.WINGS_OF_ENDERIA, 1500, 1,
               new String[]{"Have two parrots on your shoulders while", "wearing The Armored Wings of Enderia"}
         ));
   
   // Charm of Wild Growth
   public static final ArcanaAchievement THEY_GROW_UP_SO_FAST = ArcanaAchievements.register(
         new EventAchievement("They Grow Up So Fast", "they_grow_up_so_fast", new ItemStack(Items.BEEF), ArcanaRegistry.WILD_GROWTH_CHARM, 1000, 1,
               new String[]{"Have The Charm affect 5 baby animals at once"}
         ));
   public static final ArcanaAchievement BOUNTIFUL_HARVEST = ArcanaAchievements.register(
         new ProgressAchievement("Bountiful Harvest", "bountiful_harvest", new ItemStack(Items.WHEAT), ArcanaRegistry.WILD_GROWTH_CHARM, 5000, 2,
               new String[]{"Use The Charm to grow 10,000 crops to maturity"}, 10000
         ));
   
   // Arcanist's Belt
   public static final ArcanaAchievement BELT_CHARMING = ArcanaAchievements.register(
         new EventAchievement("Charming", "belt_charming", new ItemStack(Items.GOLD_INGOT), ArcanaRegistry.ARCANISTS_BELT, 2500, 2,
               new String[]{"Fill the Arcanist's Belt with Charms"}
         ));
   
   // Containment Circlet
   public static final ArcanaAchievement I_CHOOSE_YOU = ArcanaAchievements.register(
         new EventAchievement("I Choose You", "i_choose_you", new ItemStack(Items.BONE), ArcanaRegistry.CONTAINMENT_CIRCLET, 1000, 1,
               new String[]{"Use the Circlet on an animal you tamed"}
         ));
   
   // Alchemical Arbalest
   public static final ArcanaAchievement MANY_BIRDS_MANY_ARROWS = ArcanaAchievements.register(
         new TimedAchievement("Many Birds, Many Arrows", "many_birds_many_arrows", new ItemStack(Items.PHANTOM_MEMBRANE), ArcanaRegistry.ALCHEMICAL_ARBALEST, 5000, 2,
               new String[]{"Kill 3 phantoms within 3 seconds", "using the Arbalest's Alchemical Arrows"}, 3, 60
         ));
   public static final ArcanaAchievement SPECTRAL_SUPPORT = ArcanaAchievements.register(
         new TimedAchievement("Spectral Support", "spectral_support", new ItemStack(Items.GLOWSTONE_DUST), ArcanaRegistry.ALCHEMICAL_ARBALEST, 5000, 2,
               new String[]{"Amplify 1000 damage within 10 seconds"}, 1000, 200
         ));
   
   // Chest Translocator
   public static final ArcanaAchievement PEAK_LAZINESS = ArcanaAchievements.register(
         new EventAchievement("Peak Laziness", "peak_laziness", new ItemStack(Items.COBWEB), ArcanaRegistry.CHEST_TRANSLOCATOR, 1000, 1,
               new String[]{"Use the Translocator on an empty chest"}
         ));
   public static final ArcanaAchievement STORAGE_RELOCATION = ArcanaAchievements.register(
         new ProgressAchievement("Storage Relocation", "storage_relocation", new ItemStack(Items.CHEST), ArcanaRegistry.CHEST_TRANSLOCATOR, 1000, 1,
               new String[]{"Move 100 full chests"}, 100
         ));
   
   // Planeshifter
   public static final ArcanaAchievement PLANE_RIDER = ArcanaAchievements.register(
         new ConditionalsAchievement("Plane Rider", "plane_rider", new ItemStack(Items.MAP), ArcanaRegistry.PLANESHIFTER, 5000, 2,
               new String[]{"Travel to and from all dimensions"},
               new String[]{"From The Overworld", "To The Overworld", "From The Nether", "To The Nether", "From The End", "To The End"}
         ));
   public static final ArcanaAchievement UNFORTUNATE_MATERIALIZATION = ArcanaAchievements.register(
         new EventAchievement("Unfortunate Materialization", "unfortunate_materialization", new ItemStack(Items.LAVA_BUCKET), ArcanaRegistry.PLANESHIFTER, 2500, 1,
               new String[]{"Have the Planeshifter put you", "directly above a lava pit"}
         ));
   
   // Everlasting Rocket
   public static final ArcanaAchievement ROCKETMAN = ArcanaAchievements.register(
         new EventAchievement("Rocketman", "rocketman", new ItemStack(Items.GUNPOWDER), ArcanaRegistry.EVERLASTING_ROCKET, 1500, 1,
               new String[]{"Use the Everlasting Rocket with Elytra", "to boost you up to a height of 500 blocks"}
         ));
   public static final ArcanaAchievement MISSILE_LAUNCHER = ArcanaAchievements.register(
         new TimedAchievement("Missile Launcher", "missile_launcher", new ItemStack(Items.FIREWORK_ROCKET), ArcanaRegistry.EVERLASTING_ROCKET, 1500, 1,
               new String[]{"Use 5 charges of the everlasting rocket in", "a crossbow within 5 seconds"}, 5, 100
         ));
   
   // Altar of the Stormcaller
   public static final ArcanaAchievement COME_AGAIN_RAIN = ArcanaAchievements.register(
         new EventAchievement("Come Again Another Day", "come_again_rain", new ItemStack(Items.WATER_BUCKET), ArcanaRegistry.STORMCALLER_ALTAR, 1000, 1,
               new String[]{"Use The Altar to get rid of rain"}
         ));
   
   // Celestial Altar
   public static final ArcanaAchievement POWER_OF_THE_SUN = ArcanaAchievements.register(
         new EventAchievement("The Power of the Sun in the Palm of My Hand", "power_of_the_sun", new ItemStack(Items.SHROOMLIGHT), ArcanaRegistry.CELESTIAL_ALTAR, 1000, 1,
               new String[]{"Use the Celestial Altar to change the time of day"}
         ));
   public static final ArcanaAchievement LYCANTHROPE = ArcanaAchievements.register(
         new EventAchievement("Lycanthrope", "lycanthrope", new ItemStack(Items.BONE), ArcanaRegistry.CELESTIAL_ALTAR, 1000, 1,
               new String[]{"Set the moon phase to full"}
         ));
   
   // Starpath Altar
   public static final ArcanaAchievement ADVENTURING_PARTY = ArcanaAchievements.register(
         new EventAchievement("Adventuring Party", "adventuring_party", new ItemStack(Items.LEAD), ArcanaRegistry.STARPATH_ALTAR, 1000, 1,
               new String[]{"Teleport yourself along with another", "player or one of your tamed animals"}
         ));
   public static final ArcanaAchievement FAR_FROM_HOME = ArcanaAchievements.register(
         new EventAchievement("Far From Home", "far_from_home", new ItemStack(Items.FILLED_MAP), ArcanaRegistry.STARPATH_ALTAR, 10000, 3,
               new String[]{"Teleport over 100,000 blocks from your current location"}
         ));
   
   // Starlight Forge
   public static final ArcanaAchievement INTRO_ARCANA = ArcanaAchievements.register(
         new EventAchievement("Intro to Arcana", "intro_arcana", new ItemStack(Items.BOOK), ArcanaRegistry.STARLIGHT_FORGE, 1000, 1,
               new String[]{"Craft your first Non-Mundane Arcana Item"}
         ));
   public static final ArcanaAchievement INTERMEDIATE_ARTIFICE = ArcanaAchievements.register(
         new ProgressAchievement("Intermediate Artifice", "intermediate_artifice", new ItemStack(Items.ENCHANTED_BOOK), ArcanaRegistry.STARLIGHT_FORGE, 5000, 2,
               new String[]{"Craft 10 Non-Mundane Arcana Items"}, 10
         ));
   public static final ArcanaAchievement ARTIFICIAL_DIVINITY = ArcanaAchievements.register(
         new EventAchievement("Artificial Divinity", "artificial_divinity", new ItemStack(Items.GOLDEN_APPLE), ArcanaRegistry.STARLIGHT_FORGE, 10000, 3,
               new String[]{"Create a Sovereign Arcana Item"}
         ));
   public static final ArcanaAchievement MASTER_CRAFTSMAN = ArcanaAchievements.register(
         new EventAchievement("Master Craftsman", "master_craftsman", new ItemStack(Items.CRAFTING_TABLE), ArcanaRegistry.STARLIGHT_FORGE, 25000, 3,
               new String[]{"Make a piece of enhanced equipment", "with its stats in the top 1%"}
         ));
   public static final ArcanaAchievement NIDAVELLIR = ArcanaAchievements.register(
         new EventAchievement("Niðavellir", "nidavellir", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.STARLIGHT_FORGE, 100000, 5,
               new String[]{"Add all additions to your Forge"}
         ));
   
   // Twilight Anvil
   public static final ArcanaAchievement TOUCH_OF_PERSONALITY = ArcanaAchievements.register(
         new EventAchievement("A Touch of Personality", "touch_of_personality", new ItemStack(Items.NAME_TAG), ArcanaRegistry.TWILIGHT_ANVIL, 1000, 1,
               new String[]{"Use The Anvil to Rename a Arcana Item"}
         ));
   public static final ArcanaAchievement BEYOND_IRONS_LIMIT = ArcanaAchievements.register(
         new EventAchievement("Beyond Iron's Limit", "beyond_irons_limit", new ItemStack(Items.NETHERITE_INGOT), ArcanaRegistry.TWILIGHT_ANVIL, 2500, 2,
               new String[]{"Use The Anvil when the XP cost is", "too expensive for a normal anvil"}
         ));
   public static final ArcanaAchievement TINKER_TO_THE_TOP = ArcanaAchievements.register(
         new EventAchievement("Tinker to the Top", "tinker_to_the_top", new ItemStack(Items.DIAMOND_CHESTPLATE), ArcanaRegistry.TWILIGHT_ANVIL, 10000, 3,
               new String[]{"Max out the enhanced stats of a piece of equipment"}
         ));
   
   // Midnight Enchanter
   public static final ArcanaAchievement ENCHANTING_OVERKILL = ArcanaAchievements.register(
         new EventAchievement("Enchanting Overkill", "enchanting_overkill", new ItemStack(Items.WOODEN_PICKAXE), ArcanaRegistry.MIDNIGHT_ENCHANTER, 1000, 1,
               new String[]{"Use the midnight enchanter to put level 1 of a", "multi-level enchant on a piece of equipment"}
         ));
   public static final ArcanaAchievement MASTERPIECE_TO_NOTHING = ArcanaAchievements.register(
         new EventAchievement("Masterpiece to Nothing", "masterpiece_to_nothing", new ItemStack(Items.NETHERITE_CHESTPLATE), ArcanaRegistry.MIDNIGHT_ENCHANTER, 5000, 2,
               new String[]{"Turn a piece of equipment with at least 5", "max level enchantments into nebulous essence"}
         ));
   
   // Arcane Singularity
   public static final ArcanaAchievement ARCANE_QUASAR = ArcanaAchievements.register(
         new EventAchievement("Arcane Quasar", "arcane_quasar", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.ARCANE_SINGULARITY, 5000, 2,
               new String[]{"Fill up The Singularity with enchantments"}
         ));
   
   // Stellar Core
   public static final ArcanaAchievement SCRAP_TO_SCRAP = ArcanaAchievements.register(
         new EventAchievement("From Scrap to Scrap", "scrap_to_scrap", new ItemStack(Items.NETHERITE_SCRAP), ArcanaRegistry.STELLAR_CORE, 1000, 1,
               new String[]{"Salvage a piece of Netherite Equipment"}
         ));
   public static final ArcanaAchievement RECLAMATION = ArcanaAchievements.register(
         new ProgressAchievement("Reclamation", "reclamation", new ItemStack(Items.DIAMOND), ArcanaRegistry.STELLAR_CORE, 10000, 3,
               new String[]{"Salvage 1000 pieces of equipment"}, 1000
         ));
   
   // Radiant Fletchery
   public static final ArcanaAchievement OVERLY_EQUIPPED_ARCHER = ArcanaAchievements.register(
         new ConditionalsAchievement("Overly Equipped Archer", "overly_equipped_archer", new ItemStack(Items.BOW), ArcanaRegistry.RADIANT_FLETCHERY, 50000, 4,
               new String[]{"Craft all items requiring The Fletchery"},
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
                     ArcanaRegistry.SMOKE_ARROWS.getNameString()
               }
         ));
   public static final ArcanaAchievement FINALLY_USEFUL_2 = ArcanaAchievements.register(
         new EventAchievement("Finally Useful II", "finally_useful_2", new ItemStack(Items.TIPPED_ARROW), ArcanaRegistry.RADIANT_FLETCHERY, 1000, 1,
               new String[]{"Use The Fletchery to make tipped arrows"}
         ));
   
   // Totem of Vengeance
   public static final ArcanaAchievement REVENGEANCE = ArcanaAchievements.register(
         new EventAchievement("Revengeance", "revengeance", new ItemStack(Items.DIAMOND_SWORD), ArcanaRegistry.TOTEM_OF_VENGEANCE, 5000, 2,
               new String[]{"Activate a second Totem of Vengeance after", " failing to get revenge the first time"}
         ));
   public static final ArcanaAchievement TOO_ANGRY_TO_DIE = ArcanaAchievements.register(
         new TimedAchievement("Too Angry to Die", "too_angry_to_die", new ItemStack(Items.BLAZE_POWDER), ArcanaRegistry.TOTEM_OF_VENGEANCE, 10000, 3,
               new String[]{"Ignore 1000 damage while the Totem's effect is active"}, 1000, 1200
         ));
   
   // Aquatic Eversource
   public static final ArcanaAchievement POCKET_OCEAN = ArcanaAchievements.register(
         new ProgressAchievement("Pocket Ocean", "pocket_ocean", new ItemStack(Items.HEART_OF_THE_SEA), ArcanaRegistry.AQUATIC_EVERSOURCE, 1500, 1,
               new String[]{"Place 1000 water sources"}, 1000
         ));
   
   // Magmatic Eversource
   public static final ArcanaAchievement HELLGATE = ArcanaAchievements.register(
         new ProgressAchievement("Hellgate", "hellgate", new ItemStack(Items.CRIMSON_FENCE_GATE), ArcanaRegistry.MAGMATIC_EVERSOURCE, 2500, 2,
               new String[]{"Place 1000 lava sources"}, 1000
         ));
   
   // Altar of Transmutation
   public static final ArcanaAchievement STATE_ALCHEMIST = ArcanaAchievements.register(
         new ProgressAchievement("State Alchemist", "state_alchemist", new ItemStack(Items.QUARTZ), ArcanaRegistry.TRANSMUTATION_ALTAR, 10000, 3,
               new String[]{"Have your Altars transmute 10000 items"}, 10000
         ));
   
   // Aequalis Scientia
   public static final ArcanaAchievement PRICE_OF_KNOWLEDGE = ArcanaAchievements.register(
         new EventAchievement("The Price of Knowledge", "price_of_knowledge", ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), ArcanaRegistry.AEQUALIS_SCIENTIA, 10000, 3,
               new String[]{"Acquire an Aequalis Scientia"}
         ));
   public static final ArcanaAchievement QUESTIONABLE_EXCHANGE = ArcanaAchievements.register(
         new EventAchievement("A Questionable Exchange", "questionable_exchange", new ItemStack(Items.NETHERITE_HOE), ArcanaRegistry.AEQUALIS_SCIENTIA, 10000, 3,
               new String[]{"Use an Aequalis Scientia to transfer", " skill points between the same item type"}
         ));
   
   // Ensnarement Arrows
   public static final ArcanaAchievement WATERBOARDING = ArcanaAchievements.register(
         new EventAchievement("Waterboarding", "waterboarding", new ItemStack(Items.WATER_BUCKET), ArcanaRegistry.ENSNAREMENT_ARROWS, 1000, 1,
               new String[]{"Ensnare a drowning creature"}
         ));
   public static final ArcanaAchievement SHACKLED = ArcanaAchievements.register(
         new TimedAchievement("Shackled", "shackled", new ItemStack(Items.CHAIN), ArcanaRegistry.ENSNAREMENT_ARROWS, 1500, 1,
               new String[]{"Keep a creature ensnared for 60 consecutive seconds"}, 1200, 1211
         ));
   
   // Tracking Arrows
   public static final ArcanaAchievement TARGET_ACQUIRED = ArcanaAchievements.register(
         new EventAchievement("Target Acquired?", "target_acquired", new ItemStack(Items.TARGET), ArcanaRegistry.TRACKING_ARROWS, 1000, 1,
               new String[]{"Have a Tracking Arrow lock on to its firer"}
         ));
   public static final ArcanaAchievement ACTUAL_AIMBOT = ArcanaAchievements.register(
         new EventAchievement("Actual Aimbot", "actual_aimbot", new ItemStack(Items.BOW), ArcanaRegistry.TRACKING_ARROWS, 5000, 2,
               new String[]{"Have a Tracking Arrow hit its mark", " from over 250 horizontal blocks away"}
         ));
   public static final ArcanaAchievement THE_ARROW_KNOWS_WHERE_IT_IS = ArcanaAchievements.register(
         new EventAchievement("The Arrow Knows Where It Is", "the_arrow_knows_where_it_is", new ItemStack(Items.COMPASS), ArcanaRegistry.TRACKING_ARROWS, 2500, 2,
               new String[]{"Have a Tracking Arrow turn at least", " 90 degrees from its initial heading", " and stray 10 blocks from its initial course"}
         ));
   
   // Binary Blades
   public static final ArcanaAchievement STARBURST_STREAM = ArcanaAchievements.register(
         new TimedAchievement("Starburst Stream", "starburst_stream", new ItemStack(Items.NETHER_STAR), ArcanaRegistry.BINARY_BLADES, 2500, 2,
               new String[]{"Stay at maximum energy for 5 minutes"}, 6000, 6020
         ));
   
   // Graviton Maul
   public static final ArcanaAchievement QUICK_WAY_DOWN = ArcanaAchievements.register(
         new EventAchievement("The Quick Way Down", "quick_way_down", new ItemStack(Items.NETHERITE_BOOTS), ArcanaRegistry.GRAVITON_MAUL, 2500, 2,
               new String[]{"Rapid fall over 300 blocks"}
         ));
   public static final ArcanaAchievement RAISE_THE_ROOF = ArcanaAchievements.register(
         new EventAchievement("Raise the Roof", "raise_the_roof", new ItemStack(Items.NETHERITE_HELMET), ArcanaRegistry.GRAVITON_MAUL, 2500, 2,
               new String[]{"Almost die from hitting your head"}
         ));
   public static final ArcanaAchievement BONE_SMASHER = ArcanaAchievements.register(
         new EventAchievement("Bone Smasher", "bone_smasher", new ItemStack(Items.BONE), ArcanaRegistry.GRAVITON_MAUL, 5000, 2,
               new String[]{"Use the Maul to one-hit-kill a wither"}
         ));
   
   // Charm of Cetacea
   public static final ArcanaAchievement OCEAN_MIGRATION = ArcanaAchievements.register(
         new ProgressAchievement("Ocean Migration", "ocean_migration", new ItemStack(Items.HEART_OF_THE_SEA), ArcanaRegistry.CETACEA_CHARM, 10000, 3,
               new String[]{"Swim 10km while using the Charm"}, 1000000
         ));
   public static final ArcanaAchievement CEPHALOS_IN_A_POD = ArcanaAchievements.register(
         new ProgressAchievement("Cephalos in a Pod", "cephalos_in_a_pod", new ItemStack(Items.PRISMARINE_CRYSTALS), ArcanaRegistry.CETACEA_CHARM, 5000, 2,
               new String[]{"Swim 1km with a dolphin while using the Charm"}, 100000
         ));
   
   // Charm of Cleansing
   public static final ArcanaAchievement SEPTIC_SHOCK = ArcanaAchievements.register(
         new EventAchievement("Septic Shock", "septic_shock", new ItemStack(Items.PUFFERFISH), ArcanaRegistry.CLEANSING_CHARM, 2500, 2,
               new String[]{"Have the Charm activate while having 10 bad effects"}
         ));
   public static final ArcanaAchievement FOOD_POISONT = ArcanaAchievements.register(
         new EventAchievement("Food Poison't", "food_poisont", new ItemStack(Items.ROTTEN_FLESH), ArcanaRegistry.CLEANSING_CHARM, 1000, 1,
               new String[]{"Have the Charm block food poisoning"}
         ));
   public static final ArcanaAchievement CHRONIC_AILMENT = ArcanaAchievements.register(
         new TimedAchievement("Chronic Ailment", "chronic_ailment", new ItemStack(Items.FERMENTED_SPIDER_EYE), ArcanaRegistry.CLEANSING_CHARM, 1000, 1,
               new String[]{"Be re-afflicted by the same effect"," within a second of it getting cleansed"}, 1, 20
         ));
   
   // Greaves of Gaialtus
   public static final ArcanaAchievement TERRAFORMER = ArcanaAchievements.register(
         new ProgressAchievement("Terraformer", "terraformer", new ItemStack(Items.GRASS_BLOCK), ArcanaRegistry.GREAVES_OF_GAIALTUS, 10000, 3,
               new String[]{"Have the Greaves refill your hand with"," 10,000 blocks of natural stones or dirt"}, 10000
         ));
   public static final ArcanaAchievement MINERS_WALLET = ArcanaAchievements.register(
         new EventAchievement("Miner's Wallet", "miners_wallet", new ItemStack(Items.DIAMOND_BLOCK), ArcanaRegistry.GREAVES_OF_GAIALTUS, 1000, 1,
               new String[]{"Have the Greaves refill your hand with a diamond block"}
         ));
   
   // Spear of Tenbrous
   public static final ArcanaAchievement KILL_THEM_ALL = ArcanaAchievements.register(
         new ConditionalsAchievement("Kill Them All!", "kill_them_all", new ItemStack(Items.ENDER_EYE), ArcanaRegistry.SPEAR_OF_TENBROUS, 2500, 2,
               new String[]{"Use the Spear to kill one of each End mob"},
               new String[]{
                     EntityType.ENDERMAN.getName().getString(),
                     EntityType.ENDERMITE.getName().getString(),
                     EntityType.SHULKER.getName().getString(),
                     EntityType.ENDER_DRAGON.getName().getString()
               }
         ));
   public static final ArcanaAchievement HISTORY_CARVED_IN_STONE = ArcanaAchievements.register(
         new EventAchievement("History Carved in Stone", "history_carved_in_stone", new ItemStack(Items.FIRE_CHARGE), ArcanaRegistry.SPEAR_OF_TENBROUS, 100000, 5,
               new String[]{"Use the Spear to completely fill a max tier Soulstone"}
         ));
   
   
   static{
      excludedAchievements.addAll(getItemAchievements(ArcanaRegistry.PICKAXE_OF_CEPTYUS));
      excludedAchievements.addAll(getItemAchievements(ArcanaRegistry.WINGS_OF_ENDERIA));
      excludedAchievements.addAll(getItemAchievements(ArcanaRegistry.LEADERSHIP_CHARM));
      excludedAchievements.addAll(getItemAchievements(ArcanaRegistry.GREAVES_OF_GAIALTUS));
      excludedAchievements.addAll(getItemAchievements(ArcanaRegistry.SPEAR_OF_TENBROUS));
      excludedAchievements.add(ALL_ACHIEVEMENTS);
   }
   
   
   private static ArcanaAchievement register(ArcanaAchievement achievement){
      String id = achievement.id;
      registry.put(id, achievement);
      return achievement;
   }
   
   public static List<ArcanaAchievement> getItemAchievements(ArcanaItem item){
      ArrayList<ArcanaAchievement> achs = new ArrayList<>();
      for(Map.Entry<String, ArcanaAchievement> entry : registry.entrySet()){
         if(entry.getValue().getArcanaItem().getId().equals(item.getId())) achs.add(entry.getValue());
      }
      return achs;
   }
   
   public static void grant(ServerPlayerEntity player, ArcanaAchievement achievement){
      grant(player,achievement.id);
   }
   
   public static void grant(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof ProgressAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            ProgressAchievement newAch = baseAch.makeNew();
            newAch.setProgress(baseAch.getGoal());
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.setProgress(baseAch.getGoal());
            profile.setAchievement(itemId, achievement);
         }
         baseAch.announceAcquired(player);
      }else if(registry.get(id) instanceof EventAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         EventAchievement achievement = (EventAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            EventAchievement newAch = baseAch.makeNew();
            newAch.setAcquired(true);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.setAcquired(true);
            profile.setAchievement(itemId, achievement);
         }
         baseAch.announceAcquired(player);
      }else if(registry.get(id) instanceof ConditionalsAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            for(Map.Entry<String, Boolean> entry : newAch.getConditions().entrySet()){
               newAch.setCondition(entry.getKey(), true);
            }
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            for(Map.Entry<String, Boolean> entry : achievement.getConditions().entrySet()){
               achievement.setCondition(entry.getKey(), true);
            }
            profile.setAchievement(itemId, achievement);
         }
         baseAch.announceAcquired(player);
      }else if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            newAch.addProgress(baseAch.getGoal());
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            achievement.addProgress(baseAch.getGoal());
            profile.setAchievement(itemId, achievement);
         }
         baseAch.announceAcquired(player);
      }
   }
   
   public static void setCondition(ServerPlayerEntity player, String id, String condition, boolean set){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof ConditionalsAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(itemId, baseAch.id);
         boolean get;
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            get = newAch.setCondition(condition, set);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setCondition(condition, set);
            profile.setAchievement(itemId, achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static void progress(ServerPlayerEntity player, ArcanaAchievement ach, int toAdd){
      progress(player,ach.id,toAdd);
   }
   
   public static void progress(ServerPlayerEntity player, String id, int toAdd){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof ProgressAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(itemId, baseAch.id);
         boolean get;
         if(achievement == null){
            ProgressAchievement newAch = baseAch.makeNew();
            get = newAch.setProgress(toAdd);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setProgress(achievement.getProgress() + toAdd);
            profile.setAchievement(itemId, achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }else if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         boolean get;
         if(achievement == null){
            TimedAchievement newAch = baseAch.makeNew();
            get = newAch.addProgress(toAdd);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.addProgress(toAdd);
            profile.setAchievement(itemId, achievement);
         }
         if(get){
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static int getProgress(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof ProgressAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            return 0;
         }else{
            return achievement.getProgress();
         }
      }else if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            return 0;
         }else{
            return achievement.getProgress();
         }
      }
      return -1;
   }
   
   public static void revoke(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      ArcanaAchievement achieve = registry.get(id);
      if(achieve != null){
         profile.removeAchievement(achieve.getArcanaItem().getId(), id);
      }
   }
   
   public static void reset(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement != null){
            if(achievement.isAcquired()) return;
            achievement.reset();
            profile.setAchievement(itemId, achievement);
         }
      }
   }
   
   public static boolean isTimerActive(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = ArcanaNovum.data(player);
      if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getArcanaItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement != null){
            if(achievement.isAcquired()) return false;
            return achievement.isActive();
         }
      }
      return false;
   }
   
   public static HashMap<UUID,List<String>> getInvertedTracker(){
      HashMap<UUID,List<String>> invertedAchList = new HashMap<>();
      for(Map.Entry<String, List<UUID>> listEntry : ArcanaNovum.PLAYER_ACHIEVEMENT_TRACKER.entrySet()){
         for(UUID uuid : listEntry.getValue()){
            if(invertedAchList.containsKey(uuid)){
               List<String> curList = invertedAchList.get(uuid);
               curList.add(listEntry.getKey());
               invertedAchList.put(uuid,curList);
            }else{
               List<String> newList = new ArrayList<>();
               newList.add(listEntry.getKey());
               invertedAchList.put(uuid,newList);
            }
         }
      }
      return invertedAchList;
   }
}
