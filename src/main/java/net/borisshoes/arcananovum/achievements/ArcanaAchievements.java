package net.borisshoes.arcananovum.achievements;

import net.borisshoes.arcananovum.cardinalcomponents.IArcanaProfileComponent;
import net.borisshoes.arcananovum.items.SpawnerInfuser;
import net.borisshoes.arcananovum.items.arrows.ExpulsionArrows;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.MagicItems;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class ArcanaAchievements {
   public static final HashMap<String, ArcanaAchievement> registry = new HashMap<>();
   
   // Arcane Flak Arrows
   public static final ArcanaAchievement AA_ARTILLERY = ArcanaAchievements.register("aa_artillery",
         new EventAchievement("Anti-Air Artillery", "aa_artillery", new ItemStack(Items.FIREWORK_STAR), MagicItems.ARCANE_FLAK_ARROWS, 5000,2,
         new String[]{"Kill 5 phantoms with a single Arcane Flak Arrow"}
   ));
   
   // Blink Arrows
   public static final ArcanaAchievement NOW_YOU_SEE_ME = ArcanaAchievements.register("now_you_see_me",
         new EventAchievement("Now You See Me", "now_you_see_me", new ItemStack(Items.FEATHER), MagicItems.BLINK_ARROWS, 1000,1,
         new String[]{"Teleport over 100 blocks with a Blink Arrow"}
   ));
   
   // Concussion Arrows
   public static final ArcanaAchievement SHOCK_AWE = ArcanaAchievements.register("shock_awe",
         new EventAchievement("Shock and Awe", "shock_awe", new ItemStack(Items.BLACK_DYE), MagicItems.CONCUSSION_ARROWS, 1000,1,
         new String[]{"Stun 10 mobs with a single Concussion Arrow"}
   ));
   
   // Detonation Arrows
   public static final ArcanaAchievement SAFETY_THIRD = ArcanaAchievements.register("safety_third",
         new EventAchievement("Safety Third", "safety_third", new ItemStack(Items.TNT), MagicItems.DETONATION_ARROWS, 1000,1,
         new String[]{"Almost kill yourself with your own Detonation Arrow"}
   ));
   
   // Expulsion Arrows
   public static final ArcanaAchievement JUMP_PAD = ArcanaAchievements.register("jump_pad",
         new EventAchievement("Jump Pad", "jump_pad", new ItemStack(Items.SLIME_BLOCK), MagicItems.EXPULSION_ARROWS, 1000,1,
         new String[]{"Launch yourself in the air with an Expulsion Arrow"}
   ));
   
   // Graviton Arrows
   public static final ArcanaAchievement BRING_TOGETHER = ArcanaAchievements.register("bring_together",
         new EventAchievement("Bringing People Together", "bring_together", new ItemStack(Items.COBWEB), MagicItems.GRAVITON_ARROWS, 1000,1,
         new String[]{"Attract 10 mobs with a single Graviton Arrow"}
   ));
   
   // Photonic Arrows
   public static final ArcanaAchievement X = ArcanaAchievements.register("x",
         new EventAchievement("X", "x", new ItemStack(Items.STRUCTURE_VOID), MagicItems.PHOTONIC_ARROWS, 5000,2,
         new String[]{"Kill 10 mobs with a single Photonic Arrow"}
   ));
   
   // Siphoning Arrows
   public static final ArcanaAchievement CIRCLE_OF_LIFE = ArcanaAchievements.register("circle_of_life",
         new EventAchievement("Circle of Life", "circle_of_life", new ItemStack(Items.APPLE), MagicItems.SIPHONING_ARROWS, 2000,1,
         new String[]{"Kill a mob with a Siphoning Arrow","when you are at half a heart"}
   ));

   // Smoke Arrows
   public static final ArcanaAchievement SMOKE_SCREEN = ArcanaAchievements.register("smoke_screen",
         new EventAchievement("Smoke Screen", "smoke_screen", new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), MagicItems.SMOKE_ARROWS, 1000,1,
         new String[]{"Hide amongst your own smoke along with 3 hostile mobs"}
   ));
   
   // Storm Arrows
   public static final ArcanaAchievement SHOCK_THERAPY = ArcanaAchievements.register("shock_therapy",
         new EventAchievement("Shock Conversion Therapy", "shock_therapy", new ItemStack(Items.SUSPICIOUS_STEW), MagicItems.STORM_ARROWS, 3000,2,
         new String[]{"Use a Storm Arrow to convert a red","mooshroom to a brown mooshroom"}
   ));
   
   // Tether Arrows
   public static final ArcanaAchievement SPIDERMAN = ArcanaAchievements.register("spiderman",
         new TimedAchievement("Friendly Neighborhood Menace", "spiderman", new ItemStack(Items.COBWEB), MagicItems.TETHER_ARROWS, 1000,1,
         new String[]{"Swing between 5 tall structures in under 15 seconds"}, 5, 300
   ));
   
   // Charm of Cinders
   public static final ArcanaAchievement PYROMANIAC = ArcanaAchievements.register("pyromaniac",
         new EventAchievement("Pyromaniac", "pyromaniac", new ItemStack(Items.BLAZE_POWDER), MagicItems.CINDERS_CHARM, 1000,1,
         new String[]{"Set a dozen creatures on fire with a single", "use of the Charm of Cinder's cone of flame"}
   ));
   public static final ArcanaAchievement CAKE_DAY = ArcanaAchievements.register("cake_day",
         new EventAchievement("Happy Cake Day", "cake_day", new ItemStack(Items.CAKE), MagicItems.CINDERS_CHARM, 1000,1,
         new String[]{"Use the Charm of Cinders to light a birthday candle"}
   ));
   public static final ArcanaAchievement GLASSBLOWER = ArcanaAchievements.register("glassblower",
         new ProgressAchievement("Glassblower", "glassblower", new ItemStack(Items.GLASS), MagicItems.CINDERS_CHARM, 10000,3,
         new String[]{"Smelt over 10,000 glass with the", "Charm of Cinder's auto-smelt ability"}, 10000
   ));
   
   // Charm of Feasting
   public static final ArcanaAchievement TARRARE = ArcanaAchievements.register("tarrare",
         new ConditionalsAchievement("Tarrare", "tarrare", new ItemStack(Items.SPIDER_EYE), MagicItems.FEASTING_CHARM, 5000,2,
         new String[]{"Have the Feasting Charm feed you a","Poisonous Potato, Spider Eye, Rotten Flesh","Suspicious Stew, Raw Chicken and Pufferfish"},
         new String[]{"Poisonous Potato", "Spider Eye", "Rotten Flesh", "Suspicious Stew", "Raw Chicken", "Pufferfish"}
   ));
   
   // Charm of Felidae
   public static final ArcanaAchievement LAND_ON_FEET = ArcanaAchievements.register("land_on_feet",
         new EventAchievement("Always on Your Feet", "land_on_feet", new ItemStack(Items.LEATHER_BOOTS), MagicItems.FELIDAE_CHARM, 3000,1,
         new String[]{"Have the Felidae Charm save you from a lethal fall"}
   ));
   public static final ArcanaAchievement INFILTRATION = ArcanaAchievements.register("infiltration",
         new EventAchievement("Infiltration", "infiltration", new ItemStack(Items.CREEPER_HEAD), MagicItems.FELIDAE_CHARM, 1000,1,
         new String[]{"Walk amongst a group of creepers"}
   ));
   
   // Charm of Leadership
   public static final ArcanaAchievement RAID_LEADER = ArcanaAchievements.register("raid_leader",
         new EventAchievement("Raid Leader", "raid_leader", new ItemStack(Items.DIAMOND_SWORD), MagicItems.LEADERSHIP_CHARM, 1000,1,
         new String[]{"Have at least 5 players in the", "Charm of Leadership's radius"}
   ));
   
   // Charm of Light
   public static final ArcanaAchievement ENLIGHTENED = ArcanaAchievements.register("enlightened",
         new ProgressAchievement("Enlightened", "enlightened", new ItemStack(Items.LIGHT), MagicItems.LIGHT_CHARM, 5000,2,
         new String[]{"Place 1000 lights with the Charm of Light"}, 1000
   ));
   
   // Charm of Magnetism
   public static final ArcanaAchievement MAGNETS = ArcanaAchievements.register("magnets",
         new EventAchievement("That's Not How Magnets Work", "magnets", new ItemStack(Items.IRON_ORE), MagicItems.MAGNETISM_CHARM, 1000,1,
         new String[]{"Put at least 25 different items with", "the Charm of Magnetism's active ability"}
   ));
   
   // Ancient Dowsing Rod
   public static final ArcanaAchievement MOTHERLOAD = ArcanaAchievements.register("motherload",
         new EventAchievement("Found the Motherload", "motherload", new ItemStack(Items.ANCIENT_DEBRIS), MagicItems.ANCIENT_DOWSING_ROD, 1000,1,
         new String[]{"Reveal at least 10 debris with a single", "use of the Ancient Dowsing Rod"}
   ));
   public static final ArcanaAchievement ARCHEOLOGIST = ArcanaAchievements.register("archeologist",
         new ProgressAchievement("Ancient Archeologist", "archeologist", new ItemStack(Items.NETHERITE_SCRAP), MagicItems.ANCIENT_DOWSING_ROD, 10000,2,
         new String[]{"Reveal 1,000 total debris with", "the Ancient Dowsing Rod"}, 1000
   ));
   
   // Arcane Tome / Generic
   public static final ArcanaAchievement INTRO_ARCANA = ArcanaAchievements.register("intro_arcana",
         new EventAchievement("Intro to Arcana", "intro_arcana", new ItemStack(Items.BOOK), MagicItems.ARCANE_TOME, 1000,1,
         new String[]{"Craft your first Non-Mundane Magic Item"}
   ));
   public static final ArcanaAchievement INTERMEDIATE_ARTIFICE = ArcanaAchievements.register("intermediate_artifice",
         new ProgressAchievement("Intermediate Artifice", "intermediate_artifice", new ItemStack(Items.ENCHANTED_BOOK), MagicItems.ARCANE_TOME, 5000,2,
         new String[]{"Craft 10 Non-Mundane Magic Items"}, 10
   ));
   public static final ArcanaAchievement CLOSE_CALL = ArcanaAchievements.register("close_call",
         new EventAchievement("Close Call", "close_call", new ItemStack(Items.SKELETON_SKULL), MagicItems.ARCANE_TOME, 1000,1,
         new String[]{"Survive concentration damage with half a heart"}
   ));
   public static final ArcanaAchievement ARTIFICIAL_DIVINITY = ArcanaAchievements.register("artificial_divinity",
         new EventAchievement("Artificial Divinity", "artificial_divinity", new ItemStack(Items.GOLDEN_APPLE), MagicItems.ARCANE_TOME, 10000,2,
         new String[]{"Create a Legendary Magic Item"}
   ));
   public static final ArcanaAchievement GOD_BOON = ArcanaAchievements.register("god_boon",
         new EventAchievement("Boon of the True Gods", "god_boon", new ItemStack(Items.DRAGON_EGG), MagicItems.ARCANE_TOME, 10000,3,
         new String[]{"Obtain a Mythical Magic Item"}
   ));
   public static final ArcanaAchievement ARCANE_ADDICT = ArcanaAchievements.register("arcane_addict",
         new EventAchievement("Arcane Addict", "arcane_addict", new ItemStack(Items.KNOWLEDGE_BOOK), MagicItems.ARCANE_TOME, 25000,3,
         new String[]{"Have 30 Magic Items taking concentration"}
   ));
   public static final ArcanaAchievement ALL_ACHIEVEMENTS = ArcanaAchievements.register("all_achievements",
         new EventAchievement("One With the Abyss", "all_achievements", new ItemStack(Items.ENDER_EYE), MagicItems.ARCANE_TOME, 100000,10,
         new String[]{"Unlock all Non-Mythical related Achievements", "(Grants Mythical Achievements honorarily)"}
   )); //TODO
   
   // Brain in a Jar
   public static final ArcanaAchievement BREAK_BANK = ArcanaAchievements.register("break_bank",
         new EventAchievement("Break the Bank", "break_bank", new ItemStack(Items.ENDER_CHEST), MagicItems.BRAIN_JAR, 10000,2,
         new String[]{"Completely fill the Brain in a Jar"}
   ));
   public static final ArcanaAchievement CERTIFIED_REPAIR = ArcanaAchievements.register("certified_repair",
         new ProgressAchievement("Certified Repairman", "certified_repair", new ItemStack(Items.EXPERIENCE_BOTTLE), MagicItems.BRAIN_JAR, 25000,3,
         new String[]{"Repair 100,000 damage with the", "Brain in a Jar's Mending ability"}, 100000
   ));
   
   // Continuum Anchor
   public static final ArcanaAchievement TIMEY_WIMEY = ArcanaAchievements.register("timey_wimey",
         new ProgressAchievement("Timey Wimey", "timey_wimey", new ItemStack(Items.CLOCK), MagicItems.CONTINUUM_ANCHOR, 25000,3,
         new String[]{"Have your Continuum Anchors keep", "chunks loaded for a total of a month"}, 2629744
   ));
   
   // Essence Egg
   public static final ArcanaAchievement SOUL_CONVERSION = ArcanaAchievements.register("soul_conversion",
         new EventAchievement("Soul Conversion", "soul_conversion", new ItemStack(Items.SPAWNER), MagicItems.ESSENCE_EGG, 5000,2,
         new String[]{"Use the Essence Egg to switch a spawner type"}
   ));
   public static final ArcanaAchievement SOUL_FOR_SOUL = ArcanaAchievements.register("soul_for_soul",
         new ProgressAchievement("A Soul for a Soul", "soul_for_soul", new ItemStack(Items.CHICKEN_SPAWN_EGG), MagicItems.ESSENCE_EGG, 10000,2,
         new String[]{"Use the Essence Egg to spawn 25 new creatures"}, 25
   ));
   
   // Fractal Sponge
   public static final ArcanaAchievement BURNING_DESPAIR = ArcanaAchievements.register("burning_despair",
         new EventAchievement("Burning Despair", "burning_despair", new ItemStack(Items.LAVA_BUCKET), MagicItems.FRACTAL_SPONGE, 5000,2,
         new String[]{"Have one of your Fractal Sponges burn in lava"}
   ));
   public static final ArcanaAchievement OCEAN_CLEANUP = ArcanaAchievements.register("ocean_cleanup",
         new ProgressAchievement("Ocean Cleanup", "ocean_cleanup", new ItemStack(Items.SEAGRASS), MagicItems.FRACTAL_SPONGE, 2500,2,
         new String[]{"Remove a total of 10,000 blocks of","water or lava with the Fractal Sponge"}, 10000
   ));
   
   // Igneous Collider
   public static final ArcanaAchievement ENDLESS_EXTRUSION = ArcanaAchievements.register("endless_extrusion",
         new ProgressAchievement("Endless Extrusion", "endless_extrusion", new ItemStack(Items.OBSIDIAN), MagicItems.IGNEOUS_COLLIDER, 15000,3,
         new String[]{"Make over 10,000 blocks of obsidian or","crying obsidian with your Igneous Colliders"},10000
   ));
   public static final ArcanaAchievement EXPENSIVE_INFUSION = ArcanaAchievements.register("expensive_infusion",
         new EventAchievement("Expensive Infusion", "expensive_infusion", new ItemStack(Items.CRYING_OBSIDIAN), MagicItems.IGNEOUS_COLLIDER, 7500,2,
         new String[]{"Make a piece of crying obsidian with","one of your Igneous Colliders"}
   ));
   
   // Levitation Harness
   public static final ArcanaAchievement TO_THE_MOON = ArcanaAchievements.register("to_the_moon",
         new EventAchievement("Fly Me to the Moon", "to_the_moon", new ItemStack(Items.END_STONE), MagicItems.LEVITATION_HARNESS, 2500,1,
         new String[]{"Reach a height of over 1000 blocks","with the Levitation Harness"}
   ));
   public static final ArcanaAchievement FREQUENT_FLIER = ArcanaAchievements.register("frequent_flier",
         new ProgressAchievement("Frequent Flier", "frequent_flier", new ItemStack(Items.FEATHER), MagicItems.LEVITATION_HARNESS, 15000,3,
         new String[]{"Use the Levitation Harness for 24 Hours"}, 86400
   ));
   
   // Overflowing Quiver
   public static final ArcanaAchievement SPARE_STOCK = ArcanaAchievements.register("spare_stock",
         new ProgressAchievement("Spare Stock", "spare_stock", new ItemStack(Items.ARROW), MagicItems.OVERFLOWING_QUIVER, 5000,2,
         new String[]{"Have the Overflowing Quiver restock 200 arrows"}, 200
   ));
   public static final ArcanaAchievement DIVERSE_ARSENAL = ArcanaAchievements.register("diverse_arsenal",
         new EventAchievement("Diverse Arsenal", "diverse_arsenal", new ItemStack(Items.TIPPED_ARROW), MagicItems.OVERFLOWING_QUIVER, 2000,1,
         new String[]{"Put 9 different Tipped Arrows in your quiver"}
   ));
   
   // Pearl of Recall
   public static final ArcanaAchievement BACK_TO_HELL = ArcanaAchievements.register("back_to_hell",
         new EventAchievement("Back to Hell", "back_to_hell", new ItemStack(Items.NETHERRACK), MagicItems.PEARL_OF_RECALL, 1500,1,
         new String[]{"Use a Pearl of Recall with the","destination in The Nether"}
   ));
   public static final ArcanaAchievement ASCENDING_TO_HEAVEN = ArcanaAchievements.register("ascending_to_heaven",
         new EventAchievement("Ascending to Heaven", "ascending_to_heaven", new ItemStack(Items.END_STONE), MagicItems.PEARL_OF_RECALL, 1500,1,
         new String[]{"Use a Pearl of Recall with","the destination in The End"}
   ));
   
   // Pickaxe of Pluto
   public static final ArcanaAchievement BACK_IN_THE_MINE = ArcanaAchievements.register("back_in_the_mine",
         new ProgressAchievement("So We Back in the Mine", "back_in_the_mine", new ItemStack(Items.DIAMOND_PICKAXE), MagicItems.PICKAXE_OF_PLUTO, 5000,2,
         new String[]{"Spend one hour at full haste"}, 72000
   ));
   public static final ArcanaAchievement DIGGY_HOLE = ArcanaAchievements.register("diggy_hole",
         new ProgressAchievement("Diggy Diggy Hole", "diggy_hole", new ItemStack(Items.COBBLESTONE), MagicItems.PICKAXE_OF_PLUTO, 50000,5,
         new String[]{"Mine 1,000,000 stone with the Pickaxe of Pluto"},1000000
   ));
   public static final ArcanaAchievement MINE_DIAMONDS = ArcanaAchievements.register("mine_diamonds",
         new EventAchievement("Mine Diamonds", "mine_diamonds", new ItemStack(Items.DIAMOND), MagicItems.PICKAXE_OF_PLUTO, 2500,1,
         new String[]{"Mine at least 12 diamond ore with a single use","of the Pickaxe of Pluto's vein mine ability"}
   ));
   
   // Runic Bow
   public static final ArcanaAchievement JUST_LIKE_ARCHER = ArcanaAchievements.register("just_like_archer",
         new ProgressAchievement("Just Like <Famous Fictional Archer>", "just_like_archer", new ItemStack(Items.BOW), MagicItems.RUNIC_BOW, 7500,3,
         new String[]{"Shoot 1,000 Runic Arrows"}, 1000
   ));
   public static final ArcanaAchievement AIMBOT = ArcanaAchievements.register("aimbot",
         new EventAchievement("Aimbot", "aimbot", new ItemStack(Items.TARGET), MagicItems.RUNIC_BOW, 5000,2,
         new String[]{"Hit a creature from over 100","blocks away with a Runic Arrow"}
   ));
   
   // Runic Quiver
   public static final ArcanaAchievement UNLIMITED_STOCK = ArcanaAchievements.register("unlimited_stock",
         new ProgressAchievement("Unlimited Stock", "unlimited_stock", new ItemStack(Items.ARROW), MagicItems.RUNIC_QUIVER, 5000,2,
         new String[]{"Have the Runic Quiver restock 500 arrows"}, 500
   ));
   public static final ArcanaAchievement ARROW_FOR_EVERY_FOE = ArcanaAchievements.register("arrow_for_every_foe",
         new TimedAchievement("An Arrow for Every Foe", "arrow_for_every_foe", new ItemStack(Items.TIPPED_ARROW), MagicItems.RUNIC_QUIVER, 10000,3,
         new String[]{"Shoot and hit 9 different Runic","Arrows in under 20 seconds"},9,400
   ));
   
   // Shadow Stalker's Glaive
   public static final ArcanaAchievement OMAE_WA = ArcanaAchievements.register("omae_wa",
         new TimedAchievement("Omae Wa Mou Shinderiu", "omae_wa", new ItemStack(Items.WITHER_SKELETON_SKULL), MagicItems.SHADOW_STALKERS_GLAIVE, 7500,3,
         new String[]{"Kill a player or warden within a","second of stalking behind them"},1,20
   ));
   public static final ArcanaAchievement SHADOW_FURY = ArcanaAchievements.register("shadow_fury",
         new TimedAchievement("Shadow Fury", "shadow_fury", new ItemStack(Items.WITHER_ROSE), MagicItems.SHADOW_STALKERS_GLAIVE, 5000,2,
         new String[]{"Stalk behind, and then kill","8 mobs within 20 seconds"},8, 400
   ));
   
   // Shield of Fortitude
   public static final ArcanaAchievement BUILT_LIKE_TANK = ArcanaAchievements.register("built_like_tank",
         new EventAchievement("Built Like a Tank", "built_like_tank", new ItemStack(Items.IRON_BLOCK), MagicItems.SHIELD_OF_FORTITUDE, 2500,2,
         new String[]{"Have over 100 absorption hearts at","once from the Shield of Fortitude"}
   ));
   
   // Shulker Core
   public static final ArcanaAchievement MILE_HIGH = ArcanaAchievements.register("mile_high",
         new EventAchievement("Mile High Club", "mile_high", new ItemStack(Items.PHANTOM_MEMBRANE), MagicItems.SHULKER_CORE, 1500,1,
         new String[]{"Use the Shulker Core to reach a","height of one mile (1610 blocks)"}
   ));
   
   // Sojourner's Boots
   public static final ArcanaAchievement RUNNING = ArcanaAchievements.register("running",
         new TimedAchievement("Running in the 90s", "running", new ItemStack(Items.GOLDEN_BOOTS), MagicItems.SOJOURNER_BOOTS, 1000,1,
         new String[]{"Spend 5 consecutive minutes at full speed"},5900 ,6001
   ));
   public static final ArcanaAchievement PHEIDIPPIDES = ArcanaAchievements.register("pheidippides",
         new ProgressAchievement("Pheidippides", "pheidippides", new ItemStack(Items.NETHERITE_BOOTS), MagicItems.SOJOURNER_BOOTS, 100000,5,
         new String[]{"Run a total of 246 kilometers at full speed"}, 24600000
   ));
   
   // Soulstone
   public static final ArcanaAchievement TOOK_A_VILLAGE = ArcanaAchievements.register("took_a_village",
         new EventAchievement("It Took a Village", "took_a_village", new ItemStack(Items.EMERALD), MagicItems.SOULSTONE, 5000,2,
         new String[]{"Obtain a tier 3 Villager Soulstone"}
   ));
   public static final ArcanaAchievement PHILOSOPHER_STONE = ArcanaAchievements.register("philosopher_stone",
         new EventAchievement("Philosopher's Stone", "philosopher_stone", new ItemStack(Items.REDSTONE_BLOCK), MagicItems.SOULSTONE, 10000,3,
         new String[]{"Obtain a tier 5 Soulstone"}
   ));
   public static final ArcanaAchievement PRIME_EVIL = ArcanaAchievements.register("prime_evil",
         new EventAchievement("Prime Evil", "prime_evil", new ItemStack(Items.BLACKSTONE), MagicItems.SOULSTONE, 100000,5,
         new String[]{"Obtain a max tier Soulstone"}
   ));
   
   // Spawner Harness
   public static final ArcanaAchievement FINALLY_USEFUL = ArcanaAchievements.register("finally_useful",
         new EventAchievement("Finally Useful", "finally_useful", new ItemStack(Items.IRON_NUGGET), MagicItems.SPAWNER_HARNESS, 1000,1,
         new String[]{"Use the Spawner Harness on a","silverfish spawner from a stronghold"}
   ));
   
   // Spawner Infuser
   public static final ArcanaAchievement HUMBLE_NECROMANCER = ArcanaAchievements.register("humble_necromancer",
         new EventAchievement("A Humble Necromancer", "humble_necromancer", new ItemStack(Items.SKELETON_SKULL), MagicItems.SPAWNER_INFUSER, 1000,1,
         new String[]{"Upgrade a single stat in a Spawner Infuser"}
   ));
   public static final ArcanaAchievement SCULK_HUNGERS = ArcanaAchievements.register("sculk_hungers",
         new EventAchievement("The Sculk Hungers", "sculk_hungers", new ItemStack(Items.SCULK), MagicItems.SPAWNER_INFUSER, 10000,2,
         new String[]{"Put 4 stacks of points into a single","stat in a Spawner Infuser"}
   ));
   public static final ArcanaAchievement ARCHLICH = ArcanaAchievements.register("archlich",
         new EventAchievement("Archlich", "archlich", new ItemStack(Items.WITHER_SKELETON_SKULL), MagicItems.SPAWNER_INFUSER, 25000,3,
         new String[]{"Put 8 stacks of points into a Spawner Infuser"}
   ));
   public static final ArcanaAchievement INNOCENT_SOULS = ArcanaAchievements.register("innocent_souls",
         new EventAchievement("Souls of the Innocent", "innocent_souls", new ItemStack(Items.SOUL_SAND), MagicItems.SPAWNER_INFUSER, 50000,3,
         new String[]{"Use a max tier Soulstone in the Spawner Infuser"}
   ));
   public static final ArcanaAchievement POWER_OVERWHELMING = ArcanaAchievements.register("power_overwhelming",
         new EventAchievement("POWER OVERWHELMING!", "power_overwhelming", new ItemStack(Items.END_CRYSTAL), MagicItems.SPAWNER_INFUSER, 100000,5,
         new String[]{"Reach the full potential of the Spawner Infuser"}
   ));
   
   // Stasis Pearl
   public static final ArcanaAchievement PEARL_HANG = ArcanaAchievements.register("pearl_hang",
         new EventAchievement("Pearl Hang", "pearl_hang", new ItemStack(Items.CLOCK), MagicItems.STASIS_PEARL, 1000,1,
         new String[]{"Leave a pearl in stasis for","5 minutes before re-activating it"}
   ));
   public static final ArcanaAchievement INSTANT_TRANSMISSION = ArcanaAchievements.register("instant_transmission",
         new EventAchievement("Instant Transmission", "instant_transmission", new ItemStack(Items.ENDER_EYE), MagicItems.STASIS_PEARL, 1500,1,
         new String[]{"Use a Stasis Pearl to teleport a","distance of over 1000 blocks"}
   ));
   
   // Telescoping Beacon
   public static final ArcanaAchievement ART_OF_THE_DEAL = ArcanaAchievements.register("art_of_the_deal",
         new EventAchievement("The Art of the Deal", "art_of_the_deal", new ItemStack(Items.EMERALD_BLOCK), MagicItems.TELESCOPING_BEACON, 5000,2,
         new String[]{"Use the Telescoping Beacon to","deploy a tier 4 Emerald Beacon"}
   ));
   public static final ArcanaAchievement ACQUISITION_RULES = ArcanaAchievements.register("acquisition_rules",
         new EventAchievement("The Rules of Acquisition", "acquisition_rules", new ItemStack(Items.GOLD_BLOCK), MagicItems.TELESCOPING_BEACON, 10000,2,
         new String[]{"Use the Telescoping Beacon to","deploy a tier 4 Gold Beacon"}
   ));
   public static final ArcanaAchievement BEJEWELED = ArcanaAchievements.register("bejeweled",
         new EventAchievement("Bejeweled", "bejeweled", new ItemStack(Items.DIAMOND_BLOCK), MagicItems.TELESCOPING_BEACON, 25000,3,
         new String[]{"Use the Telescoping Beacon to","deploy a tier 4 Diamond Beacon"}
   ));
   public static final ArcanaAchievement CLINICALLY_INSANE = ArcanaAchievements.register("clinically_insane",
         new EventAchievement("Clinically Insane", "clinically_insane", new ItemStack(Items.NETHERITE_BLOCK), MagicItems.TELESCOPING_BEACON, 100000,5,
         new String[]{"Use the Telescoping Beacon to","deploy a tier 4 Netherite Beacon"}
   ));
   
   // Wings of Zephyr
   public static final ArcanaAchievement SEE_GLASS = ArcanaAchievements.register("see_glass",
         new EventAchievement("Can't See Glass", "see_glass", new ItemStack(Items.GLASS), MagicItems.WINGS_OF_ZEPHYR, 5000,2,
         new String[]{"Have The Armored Wings of Zephyr save you","from lethal kinetic damage from ramming into a block"}
   ));
   public static final ArcanaAchievement ANGEL_OF_DEATH = ArcanaAchievements.register("angel_of_death",
         new EventAchievement("Angel of Death", "angel_of_death", new ItemStack(Items.WITHER_SKELETON_SKULL), MagicItems.WINGS_OF_ZEPHYR, 1000,1,
         new String[]{"Kill a mob while flying with The Armored Wings of Zephyr"}
   ));
   public static final ArcanaAchievement CROW_FATHER = ArcanaAchievements.register("crow_father",
         new EventAchievement("Crow Father", "crow_father", new ItemStack(Items.FEATHER), MagicItems.WINGS_OF_ZEPHYR, 1500,1,
         new String[]{"Have two parrots on your shoulders while","wearing The Armored Wings of Zephyr"}
   ));
   
   
   
   private static ArcanaAchievement register(String id, ArcanaAchievement achievement){
      registry.put(id,achievement);
      return achievement;
   }
   
   public static List<ArcanaAchievement> getItemAchievements(MagicItem item){
      ArrayList<ArcanaAchievement> achs = new ArrayList<>();
      for(Map.Entry<String, ArcanaAchievement> entry : registry.entrySet()){
         if(entry.getValue().getMagicItem().getId().equals(item.getId())) achs.add(entry.getValue());
      }
      return achs;
   }
   
   public static void grant(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(registry.get(id) instanceof ProgressAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
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
         String itemId = baseAch.getMagicItem().getId();
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
         String itemId = baseAch.getMagicItem().getId();
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            for(int i = 0; i < newAch.getConditions().length; i++){
               newAch.setCondition(i, true);
            }
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            for(int i = 0; i < achievement.getConditions().length; i++){
               achievement.setCondition(i, true);
            }
            profile.setAchievement(itemId, achievement);
         }
      }else if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
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
   
   public static void setCondition(ServerPlayerEntity player, String id, int condition, boolean set){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(registry.get(id) instanceof ConditionalsAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         ConditionalsAchievement achievement = (ConditionalsAchievement) profile.getAchievement(itemId, baseAch.id);
         boolean get;
         if(achievement == null){
            ConditionalsAchievement newAch = baseAch.makeNew();
            get = newAch.setCondition(condition,set);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setCondition(condition,set);
            profile.setAchievement(itemId, achievement);
         }
         if(get) {
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static void progress(ServerPlayerEntity player, String id, int toAdd){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(registry.get(id) instanceof ProgressAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         ProgressAchievement achievement = (ProgressAchievement) profile.getAchievement(itemId, baseAch.id);
         boolean get;
         if(achievement == null){
            ProgressAchievement newAch = baseAch.makeNew();
            get = newAch.setProgress(toAdd);
            profile.setAchievement(itemId, newAch);
         }else{
            if(achievement.isAcquired()) return;
            get = achievement.setProgress(achievement.getProgress()+toAdd);
            profile.setAchievement(itemId, achievement);
         }
         if(get) {
            baseAch.announceAcquired(player);
         }
      }else if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
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
         if(get) {
            baseAch.announceAcquired(player);
         }
      }
   }
   
   public static void reset(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement != null){
            if(achievement.isAcquired()) return;
            achievement.reset();
            profile.setAchievement(itemId, achievement);
         }
      }
   }
   
   public static boolean isTimerActive(ServerPlayerEntity player, String id){
      IArcanaProfileComponent profile = PLAYER_DATA.get(player);
      if(registry.get(id) instanceof TimedAchievement baseAch){
         String itemId = baseAch.getMagicItem().getId();
         TimedAchievement achievement = (TimedAchievement) profile.getAchievement(itemId, baseAch.id);
         if(achievement != null){
            if(achievement.isAcquired()) return false;
            return achievement.isActive();
         }
      }
      return false;
   }
}
