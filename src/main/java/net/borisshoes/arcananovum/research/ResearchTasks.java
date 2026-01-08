package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.TreeNode;
import net.borisshoes.borislib.utils.MinecraftUtils;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ResearchTasks {
   public static final Registry<ResearchTask> RESEARCH_TASKS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"research_task")), Lifecycle.stable());
   public static final String BIOMES_VISITED_TAG = "biomes";
   
   public static final ResourceKey<ResearchTask> OBTAIN_SPONGE = of("obtain_sponge");
   public static final ResourceKey<ResearchTask> OBTAIN_END_CRYSTAL = of("obtain_end_crystal");
   public static final ResourceKey<ResearchTask> OBTAIN_SPECTRAL_ARROW = of("obtain_spectral_arrow");
   public static final ResourceKey<ResearchTask> OBTAIN_AMETHYST_SHARD = of("obtain_amethyst_shard");
   public static final ResourceKey<ResearchTask> OBTAIN_NETHERITE_SWORD = of("obtain_netherite_sword");
   public static final ResourceKey<ResearchTask> OBTAIN_NETHER_STAR = of("obtain_nether_star");
   public static final ResourceKey<ResearchTask> OBTAIN_EYE_OF_ENDER = of("obtain_eye_of_ender");
   public static final ResourceKey<ResearchTask> OBTAIN_BOTTLES_OF_ENCHANTING = of("obtain_bottles_of_enchanting");
   public static final ResourceKey<ResearchTask> OBTAIN_ZOMBIE_HEAD = of("obtain_zombie_head");
   public static final ResourceKey<ResearchTask> OBTAIN_EGG = of("obtain_egg");
   public static final ResourceKey<ResearchTask> OBTAIN_BEACON = of("obtain_beacon");
   public static final ResourceKey<ResearchTask> OBTAIN_DIVINE_CATALYST = of("obtain_divine_catalyst");
   public static final ResourceKey<ResearchTask> OBTAIN_GOLD_INGOT = of("obtain_gold_ingot");
   public static final ResourceKey<ResearchTask> OBTAIN_EMERALD = of("obtain_emerald");
   public static final ResourceKey<ResearchTask> OBTAIN_DIAMOND = of("obtain_diamond");
   public static final ResourceKey<ResearchTask> OBTAIN_QUARTZ = of("obtain_quartz");
   public static final ResourceKey<ResearchTask> OBTAIN_CLOCK = of("obtain_clock");
   public static final ResourceKey<ResearchTask> OBTAIN_NETHERITE_INGOT = of("obtain_netherite_ingot");
   public static final ResourceKey<ResearchTask> OBTAIN_PISTON = of("obtain_piston");
   public static final ResourceKey<ResearchTask> OBTAIN_GLISTERING_MELON = of("obtain_glistering_melon");
   public static final ResourceKey<ResearchTask> OBTAIN_NETHERITE_PICKAXE = of("obtain_netherite_pickaxe");
   public static final ResourceKey<ResearchTask> OBTAIN_TNT = of("obtain_tnt");
   public static final ResourceKey<ResearchTask> OBTAIN_TIPPED_ARROW = of("obtain_tipped_arrow");
   public static final ResourceKey<ResearchTask> OBTAIN_LIGHTNING_ROD = of("obtain_lightning_rod");
   public static final ResourceKey<ResearchTask> OBTAIN_AMETHYST_CLUSTER = of("obtain_amethyst_cluster");
   public static final ResourceKey<ResearchTask> OBTAIN_ARCANE_TOME = of("obtain_arcane_tome");
   public static final ResourceKey<ResearchTask> OBTAIN_CREEPER_HEAD = of("obtain_creeper_head");
   public static final ResourceKey<ResearchTask> OBTAIN_HEAVY_CORE = of("obtain_heavy_core");
   public static final ResourceKey<ResearchTask> OBTAIN_HEART_OF_THE_SEA = of("obtain_heart_of_the_sea");
   public static final ResourceKey<ResearchTask> OBTAIN_STARDUST = of("obtain_stardust");
   public static final ResourceKey<ResearchTask> OBTAIN_NEBULOUS_ESSENCE = of("obtain_nebulous_essence");
   public static final ResourceKey<ResearchTask> OBTAIN_BLAST_FURNACE = of("obtain_blast_furnace");
   public static final ResourceKey<ResearchTask> OBTAIN_BLUE_ICE = of("obtain_blue_ice");
   public static final ResourceKey<ResearchTask> OBTAIN_ANVIL = of("obtain_anvil");
   public static final ResourceKey<ResearchTask> OBTAIN_MACE = of("obtain_mace");
   public static final ResourceKey<ResearchTask> OBTAIN_CONDUIT = of("obtain_conduit");
   public static final ResourceKey<ResearchTask> OBTAIN_ENCHANTED_GOLDEN_APPLE = of("obtain_enchanted_golden_apple");
   public static final ResourceKey<ResearchTask> OBTAIN_LEADERSHIP_CHARM = of("obtain_leadership_charm");
   public static final ResourceKey<ResearchTask> OBTAIN_WINGS_OF_ENDERIA = of("obtain_wings_of_enderia");
   public static final ResourceKey<ResearchTask> OBTAIN_PICKAXE_OF_CEPTYUS = of("obtain_pickaxe_of_ceptyus");
   public static final ResourceKey<ResearchTask> OBTAIN_SPEAR_OF_TENBROUS = of("obtain_spear_of_tenbrous");
   public static final ResourceKey<ResearchTask> OBTAIN_GREAVES_OF_GAIALTUS = of("obtain_pickaxe_of_gaialtus");
   public static final ResourceKey<ResearchTask> BREAK_SCULK = of("break_sculk");
   public static final ResourceKey<ResearchTask> BREAK_SPAWNER = of("break_spawner");
   public static final ResourceKey<ResearchTask> BREAK_OBSIDIAN = of("break_obsidian");
   public static final ResourceKey<ResearchTask> PLACE_TORCHES = of("place_torches");
   public static final ResourceKey<ResearchTask> USE_FIREWORK = of("use_firework");
   public static final ResourceKey<ResearchTask> USE_CAMPFIRE = of("use_campfire");
   public static final ResourceKey<ResearchTask> USE_FLINT_AND_STEEL = of("use_flint_and_steel");
   public static final ResourceKey<ResearchTask> USE_ENDER_PEARL = of("use_ender_pearl");
   public static final ResourceKey<ResearchTask> USE_ENDER_EYE = of("use_ender_eye");
   public static final ResourceKey<ResearchTask> USE_ENDER_CHEST = of("use_ender_chest");
   public static final ResourceKey<ResearchTask> USE_ENCHANTED_GOLDEN_APPLE = of("use_enchanted_golden_apple");
   public static final ResourceKey<ResearchTask> KILL_SLIME = of("kill_slime");
   public static final ResourceKey<ResearchTask> KILL_SQUID = of("kill_squid");
   public static final ResourceKey<ResearchTask> KILL_CONSTRUCT = of("kill_construct");
   public static final ResourceKey<ResearchTask> KILL_BLAZE = of("kill_blaze");
   public static final ResourceKey<ResearchTask> KILL_MAGMA_CUBE = of("kill_magma_cube");
   public static final ResourceKey<ResearchTask> KILL_EVOKER = of("kill_evoker");
   public static final ResourceKey<ResearchTask> CATCH_FISH = of("catch_fish");
   public static final ResourceKey<ResearchTask> SPRINT_TEN_KILOMETERS = of("sprint_ten_kilometers");
   public static final ResourceKey<ResearchTask> UNLOCK_AQUATIC_EVERSOURCE = of("unlock_aquatic_eversource");
   public static final ResourceKey<ResearchTask> UNLOCK_RUNIC_MATRIX = of("unlock_runic_matrix");
   public static final ResourceKey<ResearchTask> UNLOCK_STARLIGHT_FORGE = of("unlock_starlight_forge");
   public static final ResourceKey<ResearchTask> UNLOCK_TWILIGHT_ANVIL = of("unlock_twilight_anvil");
   public static final ResourceKey<ResearchTask> UNLOCK_TEMPORAL_MOMENT = of("unlock_temporal_moment");
   public static final ResourceKey<ResearchTask> UNLOCK_EXOTIC_MATTER = of("unlock_exotic_matter");
   public static final ResourceKey<ResearchTask> UNLOCK_SHULKER_CORE = of("unlock_shulker_core");
   public static final ResourceKey<ResearchTask> UNLOCK_MIDNIGHT_ENCHANTER = of("unlock_midnight_enchanter");
   public static final ResourceKey<ResearchTask> UNLOCK_STELLAR_CORE = of("unlock_stellar_core");
   public static final ResourceKey<ResearchTask> UNLOCK_ARCANE_SINGULARITY = of("unlock_arcane_singularity");
   public static final ResourceKey<ResearchTask> UNLOCK_RADIANT_FLETCHERY = of("unlock_radiant_fletchery");
   public static final ResourceKey<ResearchTask> UNLOCK_SOULSTONE = of("unlock_soulstone");
   public static final ResourceKey<ResearchTask> UNLOCK_CATALYTIC_MATRIX = of("unlock_catalytic_matrix");
   public static final ResourceKey<ResearchTask> UNLOCK_MUNDANE_CATALYST = of("unlock_mundane_catalyst");
   public static final ResourceKey<ResearchTask> UNLOCK_EMPOWERED_CATALYST = of("unlock_empowered_catalyst");
   public static final ResourceKey<ResearchTask> UNLOCK_EXOTIC_CATALYST = of("unlock_exotic_catalyst");
   public static final ResourceKey<ResearchTask> UNLOCK_SOVEREIGN_CATALYST = of("unlock_sovereign_catalyst");
   public static final ResourceKey<ResearchTask> UNLOCK_OVERFLOWING_QUIVER = of("unlock_overflowing_quiver");
   public static final ResourceKey<ResearchTask> UNLOCK_SPAWNER_HARNESS = of("unlock_spawner_harness");
   public static final ResourceKey<ResearchTask> UNLOCK_TRANSMUTATION_ALTAR = of("unlock_transmutation_altar");
   public static final ResourceKey<ResearchTask> UNLOCK_WAYSTONE = of("unlock_waystone");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ENCHANT_ITEM = of("advancement_enchant_item");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_CHARGE_RESPAWN_ANCHOR = of("advancement_charge_respawn_anchor");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS = of("advancement_obtain_ancient_debris");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ELYTRA = of("advancement_elytra");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_FIND_BASTION = of("advancement_find_bastion");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS = of("advancement_crafters_crafting_crafters");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_KILL_A_MOB = of("advancement_kill_a_mob");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_LEVITATE = of("advancement_levitate");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BALANCED_DIET = of("advancement_balanced_diet");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_DEFLECT_ARROW = of("advancement_deflect_arrow");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ADVENTURING_TIME = of("advancement_adventuring_time");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS = of("advancement_walk_on_powder_snow_with_leather_boots");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_CREATE_FULL_BEACON = of("advancement_create_full_beacon");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_SLEEP_IN_BED = of("advancement_sleep_in_bed");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_SHOOT_ARROW = of("advancement_shoot_arrow");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_SNIPER_DUEL = of("advancement_sniper_duel");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BULLSEYE = of("advancement_bullseye");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BREW_POTION = of("advancement_brew_potion");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN = of("advancement_obtain_crying_obsidian");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_DRAGON_BREATH = of("advancement_dragon_breath");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST = of("advancement_kill_mob_near_sculk_catalyst");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE = of("advancement_lightning_rod_with_villager_no_fire");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_TAME_AN_ANIMAL = of("advancement_tame_an_animal");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_TRADE = of("advancement_trade");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_USE_LODESTONE = of("advancement_use_lodestone");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_COMPLETE_CATALOGUE = of("advancement_complete_catalogue");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_WAX_ON = of("advancement_wax_on");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_WAX_OFF = of("advancement_wax_off");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_OL_BETSY = of("advancement_ol_betsy");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ARBALISTIC = of("advancement_arbalistic");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_TWO_BIRDS_ONE_ARROW = of("advancement_two_birds_one_arrow");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_WHOS_THE_PILLAGER_NOW = of("advancement_whos_the_pillager_now");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_TOTEM_OF_UNDYING = of("advancement_totem_of_undying");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_LAVA_BUCKET = of("advancement_lava_bucket");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER = of("advancement_allay_deliver_item_to_player");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK = of("advancement_allay_deliver_cake_to_note_block");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF = of("advancement_read_power_of_chiseled_bookshelf");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BREED_AN_ANIMAL = of("advancement_breed_an_animal");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_PLANT_SEED = of("advancement_plant_seed");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_PLANT_ANY_SNIFFER_SEED = of("advancement_plant_any_sniffer_seed");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_OBTAIN_NETHERITE_HOE = of("advancement_obtain_netherite_hoe");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BRED_ALL_ANIMALS = of("advancement_bred_all_animals");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_FALL_FROM_WORLD_HEIGHT = of("advancement_fall_from_world_height");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_EYE_SPY = of("advancement_eye_spy");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_ENTER_END = of("advancement_enter_end");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_FURIOUS_COCKTAIL = of("advancement_furious_cocktail");
   public static final ResourceKey<ResearchTask> ADVANCEMENT_OVER_OVERKILL = of("advancement_over_overkill");
   public static final ResourceKey<ResearchTask> DIMENSION_TRAVEL = of("dimension_travel");
   public static final ResourceKey<ResearchTask> CAT_SCARE = of("cat_scare");
   public static final ResourceKey<ResearchTask> RESONATE_BELL = of("resonate_bell");
   public static final ResourceKey<ResearchTask> RIPTIDE_TRIDENT = of("riptide_trident");
   public static final ResourceKey<ResearchTask> FISH_ITEM = of("fish_item");
   public static final ResourceKey<ResearchTask> FISH_MOB = of("fish_mob");
   public static final ResourceKey<ResearchTask> LEVEL_100 = of("level_100");
   public static final ResourceKey<ResearchTask> HUNGER_DAMAGE = of("hunger_damage");
   public static final ResourceKey<ResearchTask> DROWNING_DAMAGE = of("drowning_damage");
   public static final ResourceKey<ResearchTask> CONCENTRATION_DAMAGE = of("concentration_damage");
   public static final ResourceKey<ResearchTask> FEATHER_FALL = of("feather_fall");
   public static final ResourceKey<ResearchTask> FIND_SPAWNER = of("find_spawner");
   public static final ResourceKey<ResearchTask> USE_SOUL_SPEED = of("use_soul_speed");
   public static final ResourceKey<ResearchTask> ACTIVATE_MENDING = of("activate_mending");
   public static final ResourceKey<ResearchTask> OBTAIN_SILK_TOUCH = of("obtain_silk_touch");
   public static final ResourceKey<ResearchTask> INFUSE_ITEM = of("infuse_item");
   public static final ResourceKey<ResearchTask> HONEY_CLEANSE = of("honey_cleanse");
   public static final ResourceKey<ResearchTask> MILK_CLEANSE = of("milk_cleanse");
   public static final ResourceKey<ResearchTask> VISIT_DOZEN_BIOMES = of("visit_dozen_biomes");
   public static final ResourceKey<ResearchTask> TAME_CAT = of("tame_cat");
   public static final ResourceKey<ResearchTask> EFFECT_ABSORPTION = of("effect_absorption");
   public static final ResourceKey<ResearchTask> EFFECT_SWIFTNESS = of("effect_swiftness");
   public static final ResourceKey<ResearchTask> EFFECT_JUMP_BOOST = of("effect_jump_boost");
   public static final ResourceKey<ResearchTask> EFFECT_NIGHT_VISION = of("effect_night_vision");
   public static final ResourceKey<ResearchTask> EFFECT_SLOW_FALLING = of("effect_slow_falling");
   public static final ResourceKey<ResearchTask> EFFECT_BLINDNESS = of("effect_blindness");
   public static final ResourceKey<ResearchTask> EFFECT_SLOWNESS = of("effect_slowness");
   public static final ResourceKey<ResearchTask> EFFECT_FIRE_RESISTANCE = of("effect_fire_resistance");
   public static final ResourceKey<ResearchTask> EFFECT_STRENGTH = of("effect_strength");
   public static final ResourceKey<ResearchTask> EFFECT_WEAKNESS = of("effect_weakness");
   public static final ResourceKey<ResearchTask> EFFECT_NAUSEA = of("effect_nausea");
   public static final ResourceKey<ResearchTask> EFFECT_POISON = of("effect_poison");
   public static final ResourceKey<ResearchTask> EFFECT_DOLPHINS_GRACE = of("effect_dolphins_grace");
   
   @SuppressWarnings("unchecked")
   public static void registerResearchTasks(){
      ResearchTasks.register(OBTAIN_SPONGE, new ObtainResearchTask(
            OBTAIN_SPONGE.identifier().getPath(), Items.SPONGE,
            Component.literal("Obtain a Sponge"),
            new Component[]{
                  Component.literal("Water's self-replicating nature makes it very"),
                  Component.literal(" difficult to clean up in large quantities."),
                  Component.literal("Maybe something can make this task easier?")
            },
            new ItemStack(Items.SPONGE)
      ));
      
      ResearchTasks.register(OBTAIN_END_CRYSTAL, new ObtainResearchTask(
            OBTAIN_END_CRYSTAL.identifier().getPath(), Items.END_CRYSTAL,
            Component.literal("Obtain an End Crystal"),
            new Component[]{
                  Component.literal("This world contains some artifacts displaying"),
                  Component.literal(" some impressive properties and craftsmanship."),
                  Component.literal("Perhaps I can find one that can channel Arcana...")
            },
            new ItemStack(Items.END_CRYSTAL)
      ));
      
      ResearchTasks.register(OBTAIN_SPECTRAL_ARROW, new ObtainResearchTask(
            OBTAIN_SPECTRAL_ARROW.identifier().getPath(), Items.SPECTRAL_ARROW,
            Component.literal("Obtain a Spectral Arrow"),
            new Component[]{
                  Component.literal("Normal arrows are too fragile and shoddy to"),
                  Component.literal(" withstand arcane enhancement without any sort"),
                  Component.literal(" of reinforcement or arcane priming."),
                  Component.literal("I will find a substance to treat the arrows with.")
            },
            new ItemStack(Items.SPECTRAL_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_SHARD, new ObtainResearchTask(
            OBTAIN_AMETHYST_SHARD.identifier().getPath(), Items.AMETHYST_SHARD,
            Component.literal("Obtain an Amethyst Shard"),
            new Component[]{
                  Component.literal("Crystals are far more than just pretty rocks."),
                  Component.literal("Their ordered structure is perfect for withstanding"),
                  Component.literal(" immense energy and even changing and channeling it."),
                  Component.literal("Unfortunately, they take millennia to form naturally, "),
                  Component.literal(" and synthesizing them is beyond my capabilities."),
                  Component.literal("I must find a good source of suitable crystals.")
            },
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_SWORD, new ObtainResearchTask(
            OBTAIN_NETHERITE_SWORD.identifier().getPath(), Items.NETHERITE_SWORD,
            Component.literal("Obtain a Netherite Sword"),
            new Component[]{
                  Component.literal("Netherite Alloy has been quite the game-changer."),
                  Component.literal("Its hardness even outclasses diamond!"),
                  Component.literal("Theoretically, it should hold a sharper edge."),
                  Component.literal("This is something worth pursuing...")
            },
            new ItemStack(Items.NETHERITE_SWORD), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHER_STAR, new ObtainResearchTask(
            OBTAIN_NETHER_STAR.identifier().getPath(), Items.NETHER_STAR,
            Component.literal("Obtain a Nether Star"),
            new Component[]{
                  Component.literal("I have found that souls harbor a significant"),
                  Component.literal(" amount of power; the energy of life itself."),
                  Component.literal("Soul Sand seems to have a small amount of this"),
                  Component.literal(" energy stored within it, and I have found some"),
                  Component.literal(" evidence to suggest ancient beings feared an"),
                  Component.literal(" entity comprised of this soul-harboring material."),
                  Component.literal("I wish to find this entity, and study its potential.")
            },
            new ItemStack(Items.NETHER_STAR), USE_SOUL_SPEED
      ));
      
      ResearchTasks.register(OBTAIN_EYE_OF_ENDER, new ObtainResearchTask(
            OBTAIN_EYE_OF_ENDER.identifier().getPath(), Items.ENDER_EYE,
            Component.literal("Obtain an Eye of Ender"),
            new Component[]{
                  Component.literal("Endermen are the only interdimensional travellers"),
                  Component.literal(" that I have found, despite evidence of others."),
                  Component.literal("They also manage to do dimension hop without a portal."),
                  Component.literal("I believe their eyes have something to do with it,"),
                  Component.literal(" but upon their death, their eyes glaze over and"),
                  Component.literal(" can only be used to teleport a short distance."),
                  Component.literal("Maybe I can reawaken their natural abilities?")
            },
            new ItemStack(Items.ENDER_EYE), USE_ENDER_PEARL
      ));
      
      ResearchTasks.register(OBTAIN_BOTTLES_OF_ENCHANTING, new ObtainResearchTask(
            OBTAIN_BOTTLES_OF_ENCHANTING.identifier().getPath(), Items.EXPERIENCE_BOTTLE,
            Component.literal("Obtain Bottles of Experience"),
            new Component[]{
                  Component.literal("Experience is a strange substance, it is partly"),
                  Component.literal(" ethereal but still has some physicality to it."),
                  Component.literal("Finding a way to contain it would be valuable.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(OBTAIN_ZOMBIE_HEAD, new ObtainResearchTask(
            OBTAIN_ZOMBIE_HEAD.identifier().getPath(), Items.ZOMBIE_HEAD,
            Component.literal("Obtain a Zombie Head"),
            new Component[]{
                  Component.literal("Zombies are very versatile creatures, and"),
                  Component.literal(" their possible origins still elude me."),
                  Component.literal("I wonder how similar their brain is to other"),
                  Component.literal(" creatures I have encountered in the world.")
            },
            new ItemStack(Items.ZOMBIE_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_EGG, new ObtainResearchTask(
            OBTAIN_EGG.identifier().getPath(), Items.EGG,
            Component.literal("Obtain an Egg"),
            new Component[]{
                  Component.literal("Some species do not perform live birth, they gestate"),
                  Component.literal(" in external eggs until they are ready to be born."),
                  Component.literal("Maybe there is something I can gleam from them.")
            },
            new ItemStack(Items.EGG)
      ));
      
      ResearchTasks.register(OBTAIN_BEACON, new ObtainResearchTask(
            OBTAIN_BEACON.identifier().getPath(), Items.BEACON,
            Component.literal("Unlock the Power of the Nether Star"),
            new Component[]{
                  Component.literal("Nether Stars are truly fascinating! They appear to"),
                  Component.literal(" be a micro or even nano crystalline structure that"),
                  Component.literal(" is suited specifically for channelling soul energy!"),
                  Component.literal("I can think of 10,000 things I can use this for, but"),
                  Component.literal(" for now, I should just make something to contain it.")
            },
            new ItemStack(Items.BEACON), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(OBTAIN_DIVINE_CATALYST, new ObtainResearchTask(
            OBTAIN_DIVINE_CATALYST.identifier().getPath(), ArcanaRegistry.DIVINE_CATALYST.getItem(),
            Component.literal("Obtain a Divine Catalyst"),
            new Component[]{
                  Component.literal("")
            },
            ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST
      ));
      
      ResearchTasks.register(OBTAIN_GOLD_INGOT, new ObtainResearchTask(
            OBTAIN_GOLD_INGOT.identifier().getPath(), Items.GOLD_INGOT,
            Component.literal("Obtain a Gold Ingot"),
            new Component[]{
                  Component.literal("Most materials I have found exhibit low receptivity"),
                  Component.literal(" to enchantment and other forms of arcane infusion."),
                  Component.literal("I need something else... Something malleable, maybe?")
            },
            new ItemStack(Items.GOLD_INGOT)
      ));
      
      ResearchTasks.register(OBTAIN_EMERALD, new ObtainResearchTask(
            OBTAIN_EMERALD.identifier().getPath(), Items.EMERALD,
            Component.literal("Obtain an Emerald"),
            new Component[]{
                  Component.literal("Villagers are strange folk. They practically worship,"),
                  Component.literal(" these green gemstones that they barter and work for."),
                  Component.literal("I should try and get my hands on some...")
            },
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(OBTAIN_DIAMOND, new ObtainResearchTask(
            OBTAIN_DIAMOND.identifier().getPath(), Items.DIAMOND,
            Component.literal("Obtain a Diamond"),
            new Component[]{
                  Component.literal("Diamonds are rumored to be the world's most durable"),
                  Component.literal(" material, found deep within the caves."),
                  Component.literal("Somehow, I have never laid my hands on one..."),
                  Component.literal(" It's time to change that!")
            },
            new ItemStack(Items.DIAMOND)
      ));
      
      ResearchTasks.register(OBTAIN_QUARTZ, new ObtainResearchTask(
            OBTAIN_QUARTZ.identifier().getPath(), Items.QUARTZ,
            Component.literal("Obtain Quartz"),
            new Component[]{
                  Component.literal("Crystals are far more than just pretty rocks."),
                  Component.literal("Their ordered structure is perfect for withstanding"),
                  Component.literal(" immense energy and even changing and channeling it."),
                  Component.literal("Unfortunately, they take millennia to form naturally, "),
                  Component.literal(" and synthesizing them is beyond my capabilities."),
                  Component.literal("I wonder if the Nether produces crystal formations?")
            },
            new ItemStack(Items.QUARTZ)
      ));
      
      ResearchTasks.register(OBTAIN_CLOCK, new ObtainResearchTask(
            OBTAIN_CLOCK.identifier().getPath(), Items.CLOCK,
            Component.literal("Obtain a Clock"),
            new Component[]{
                  Component.literal("Time. It is such a strange... concept? phenomenon?"),
                  Component.literal("Whatever it is, I lose track of it frequently."),
                  Component.literal("I need something to automatically keep it for me.")
            },
            new ItemStack(Items.CLOCK)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_INGOT, new ObtainResearchTask(
            OBTAIN_NETHERITE_INGOT.identifier().getPath(), Items.NETHERITE_INGOT,
            Component.literal("Obtain a Netherite Ingot"),
            new Component[]{
                  Component.literal("This ancient debris is certainly something!"),
                  Component.literal("Fire-proof, blast-proof, harder than diamond!"),
                  Component.literal("Too hard, in fact. I can't even shape it into"),
                  Component.literal(" any tools or armors in it's pure state."),
                  Component.literal("Maybe I can alloy it with gold to make it more"),
                  Component.literal(" workable, and possibly more receptive to Arcana.")
            },
            new ItemStack(Items.NETHERITE_INGOT), ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_PISTON, new ObtainResearchTask(
            OBTAIN_PISTON.identifier().getPath(), Items.PISTON,
            Component.literal("Obtain a Piston"),
            new Component[]{
                  Component.literal("I need something that is able to automatically"),
                  Component.literal(" generate enough force to move blocks around."),
                  Component.literal("Maybe combining iron and redstone could work?")
            },
            new ItemStack(Items.PISTON)
      ));
      
      ResearchTasks.register(OBTAIN_GLISTERING_MELON, new ObtainResearchTask(
            OBTAIN_GLISTERING_MELON.identifier().getPath(), Items.GLISTERING_MELON_SLICE,
            Component.literal("Obtain a Glistering Melon"),
            new Component[]{
                  Component.literal("I don't know what these ancient alchemy books are"),
                  Component.literal(" on about, but they keep touting gold-encrusted"),
                  Component.literal(" melons as the main ingredient in the elixir of life."),
                  Component.literal("I swear on the stars' light... this better be real.")
            },
            new ItemStack(Items.GLISTERING_MELON_SLICE), ADVANCEMENT_BREW_POTION, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_PICKAXE, new ObtainResearchTask(
            OBTAIN_NETHERITE_PICKAXE.identifier().getPath(), Items.NETHERITE_PICKAXE,
            Component.literal("Obtain a Netherite Pickaxe"),
            new Component[]{
                  Component.literal("Netherite Alloy has been quite the game-changer."),
                  Component.literal("Its hardness even outclasses diamond!"),
                  Component.literal("Theoretically, it should make a phenomenal"),
                  Component.literal(" pickaxe, with increased speed and durability."),
                  Component.literal("If I can get enough debris, this is worth a try.")
            },
            new ItemStack(Items.NETHERITE_PICKAXE), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_TNT, new ObtainResearchTask(
            OBTAIN_TNT.identifier().getPath(), Items.TNT,
            Component.literal("Obtain a TNT"),
            new Component[]{
                  Component.literal("Creepers are fascinating creatures, with a unique"),
                  Component.literal(" capability for self-destructive obliteration."),
                  Component.literal("I would very much like to replicate this power.")
            },
            new ItemStack(Items.TNT)
      ));
      
      ResearchTasks.register(OBTAIN_TIPPED_ARROW, new ObtainResearchTask(
            OBTAIN_TIPPED_ARROW.identifier().getPath(), Items.TIPPED_ARROW,
            Component.literal("Obtain a Tipped Arrow"),
            new Component[]{
                  Component.literal("Arrows might be too shoddy to be enhanced by"),
                  Component.literal(" Arcana, however, I might be able to coat them"),
                  Component.literal(" with some potions to deliver an extra kick.")
            },
            new ItemStack(Items.TIPPED_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_LIGHTNING_ROD, new ObtainResearchTask(
            OBTAIN_LIGHTNING_ROD.identifier().getPath(), Items.LIGHTNING_ROD,
            Component.literal("Obtain a Lightning Rod"),
            new Component[]{
                  Component.literal("Lightning is an amazing natural occurrence!"),
                  Component.literal("Unfortunately, it seems to strike randomly during"),
                  Component.literal(" intense thunderstorms, which don't happen often."),
                  Component.literal("I might not be able to make lightning occur, but"),
                  Component.literal(" perhaps I can at the very least get it where I want."),
                  Component.literal("Lightning leaves a unique taste in the air, it smells"),
                  Component.literal(" like old copper. That is a good place to start.")
            },
            new ItemStack(Items.LIGHTNING_ROD)
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_CLUSTER, new ObtainResearchTask(
            OBTAIN_AMETHYST_CLUSTER.identifier().getPath(), Items.AMETHYST_CLUSTER,
            Component.literal("Obtain an Amethyst Cluster"),
            new Component[]{
                  Component.literal("Amethyst shards have proven to be invaluable."),
                  Component.literal("However, they are very fragile and break apart"),
                  Component.literal(" when I try to collect them from their geode."),
                  Component.literal("If I can get my hands on bigger chunks of them,"),
                  Component.literal(" the cluster will be capable of harnessing and"),
                  Component.literal(" focusing much greater amounts of arcane energy."),
                  Component.literal("Maybe this Silk Touch enchantment can lend a hand?")
            },
            new ItemStack(Items.AMETHYST_CLUSTER), OBTAIN_AMETHYST_SHARD, OBTAIN_SILK_TOUCH
      ));
      
      ResearchTasks.register(OBTAIN_ARCANE_TOME, new ObtainResearchTask(
            "obtain_tome_of_arcana_novum", ArcanaRegistry.ARCANE_TOME.getItem(),
            Component.literal("Obtain a Tome of Arcana Novum"),
            new Component[]{
                  Component.literal("I'll keep it real. How am I keeping my arcane "),
                  Component.literal(" notes without a tome to store them in?"),
                  Component.literal("This must be rectified immediately!")
            },
            ArcanaRegistry.ARCANE_TOME.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_CREEPER_HEAD, new ObtainResearchTask(
            OBTAIN_CREEPER_HEAD.identifier().getPath(), Items.CREEPER_HEAD,
            Component.literal("Obtain a Creeper Head"),
            new Component[]{
                  Component.literal("Creepers are very unique creatures."),
                  Component.literal("I wonder if their brains are equally unique?")
            },
            new ItemStack(Items.CREEPER_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_HEAVY_CORE, new ObtainResearchTask(
            OBTAIN_HEAVY_CORE.identifier().getPath(), Items.HEAVY_CORE,
            Component.literal("Obtain a Heavy Core"),
            new Component[]{
                  Component.literal("I have heard whispers of buried chambers, designed"),
                  Component.literal(" by some unknown entity or collective to test others."),
                  Component.literal("Supposedly, these chambers contain rewards for those"),
                  Component.literal(" who are capable enough to defeat their trials."),
                  Component.literal("One such reward is rumored to be an incredibly dense"),
                  Component.literal(" material, or maybe even a device of some sort."),
                  Component.literal("I might have a few uses for such a thing.")
            },
            new ItemStack(Items.HEAVY_CORE)
      ));
      
      ResearchTasks.register(OBTAIN_HEART_OF_THE_SEA, new ObtainResearchTask(
            OBTAIN_HEART_OF_THE_SEA.identifier().getPath(), Items.HEART_OF_THE_SEA,
            Component.literal("Obtain a Heart of the Sea"),
            new Component[]{
                  Component.literal("I do not believe these so called 'guardians' of"),
                  Component.literal(" the ocean monuments are their original builders."),
                  Component.literal("Their name suggests they are simply protectors."),
                  Component.literal("Whoever made the monuments must have a mastery over"),
                  Component.literal(" water, through natural or artificial means."),
                  Component.literal("Maybe I can find an artifact that holds the key?")
            },
            new ItemStack(Items.HEART_OF_THE_SEA)
      ));
      
      ResearchTasks.register(OBTAIN_STARDUST, new ObtainResearchTask(
            OBTAIN_STARDUST.identifier().getPath(), ArcanaRegistry.STARDUST,
            Component.literal("Obtain Stardust"),
            new Component[]{
                  Component.literal("Enchantments bind to equipment in a unique way."),
                  Component.literal("Beyond the enchantment Arcana, they alter the"),
                  Component.literal(" material they bind to, to a small extent."),
                  Component.literal("There may be a way to isolate and concentrate"),
                  Component.literal(" the enhanced material so that this effect can"),
                  Component.literal(" be replicated independently of enchantment."),
                  Component.literal("Theoretically, this would look like a powder"),
                  Component.literal(" that exhibits some latent arcane properties.")
            },
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.STARDUST)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_NEBULOUS_ESSENCE, new ObtainResearchTask(
            OBTAIN_NEBULOUS_ESSENCE.identifier().getPath(), ArcanaRegistry.NEBULOUS_ESSENCE,
            Component.literal("Obtain Nebulous Essence"),
            new Component[]{
                  Component.literal("Enchantment Arcana is a very strange thing."),
                  Component.literal("The Enchantment Table sends some type of Arcana"),
                  Component.literal(" into nearby books and pulls it back after the"),
                  Component.literal(" knowledge infuses the arcane energy."),
                  Component.literal("This altered arcane energy can produce any one"),
                  Component.literal(" of a vast many possible enchantment effects."),
                  Component.literal("The enchantment essence is nebulous until used."),
                  Component.literal("Maybe I can revert the enchantment Arcana back "),
                  Component.literal(" into this nebulous state, and isolate it.")
            },
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_BLAST_FURNACE, new ObtainResearchTask(
            OBTAIN_BLAST_FURNACE.identifier().getPath(), Items.BLAST_FURNACE,
            Component.literal("Obtain a Blast Furnace"),
            new Component[]{
                  Component.literal("Smelting ores must be done at high temperature."),
                  Component.literal("Furnaces take too long to reach such a high heat."),
                  Component.literal("If I reinforce one, maybe it will smelt faster.")
            },
            new ItemStack(Items.BLAST_FURNACE)
      ));
      
      ResearchTasks.register(OBTAIN_BLUE_ICE, new ObtainResearchTask(
            OBTAIN_BLUE_ICE.identifier().getPath(), Items.BLUE_ICE,
            Component.literal("Obtain a Blue Ice"),
            new Component[]{
                  Component.literal("The denser the ice, the more slippery it is."),
                  Component.literal("The denser the ice, the more water it contains."),
                  Component.literal("The denser the ice, the colder it remains."),
                  Component.literal("What is the densest ice that I can obtain?")
            },
            new ItemStack(Items.BLUE_ICE)
      ));
      
      ResearchTasks.register(OBTAIN_ANVIL, new ObtainResearchTask(
            OBTAIN_ANVIL.identifier().getPath(), Items.ANVIL,
            Component.literal("Obtain an Anvil"),
            new Component[]{
                  Component.literal("I wish I had a way to repair my equipment."),
                  Component.literal("I have some scrap metal that could be used to "),
                  Component.literal(" patch up my tools and armor but if I try to"),
                  Component.literal(" hammer it on, the surface beneath caves in."),
                  Component.literal("I need something sturdy to withstand my hammer.")
            },
            new ItemStack(Items.ANVIL)
      ));
      
      ResearchTasks.register(OBTAIN_MACE, new ObtainResearchTask(
            OBTAIN_MACE.identifier().getPath(), Items.MACE,
            Component.literal("Obtain a Mace"),
            new Component[]{
                  Component.literal("This heavy core is stranger than expected."),
                  Component.literal("It certainly lives up to the name, however"),
                  Component.literal(" its weight has some variation to it?"),
                  Component.literal("The dark core within seems to have some"),
                  Component.literal(" control over its local gravity field."),
                  Component.literal("I believe I can forge this into a powerful weapon.")
            },
            new ItemStack(Items.MACE), OBTAIN_HEAVY_CORE
      ));
      
      ResearchTasks.register(OBTAIN_CONDUIT, new ObtainResearchTask(
            OBTAIN_CONDUIT.identifier().getPath(), Items.CONDUIT,
            Component.literal("Obtain a Conduit"),
            new Component[]{
                  Component.literal("I believe I found the artifact I am looking for."),
                  Component.literal("Unfortunately, it seems mostly inert on its own."),
                  Component.literal("Given the architecture of the ocean monuments,"),
                  Component.literal(" it might be reasonable to assume that this oceanic"),
                  Component.literal(" heart needs some type of construct to activate.")
            },
            new ItemStack(Items.CONDUIT), OBTAIN_HEART_OF_THE_SEA
      ));
      
      ResearchTasks.register(OBTAIN_ENCHANTED_GOLDEN_APPLE, new ObtainResearchTask(
            OBTAIN_ENCHANTED_GOLDEN_APPLE.identifier().getPath(), Items.ENCHANTED_GOLDEN_APPLE,
            Component.literal("Obtain an Enchanted Golden Apple"),
            new Component[]{
                  Component.literal("There is a powerful item whose recipe is lost."),
                  Component.literal("Many have set out in search of the mysterious"),
                  Component.literal(" Enchanted Golden Apple, but few ever find one."),
                  Component.literal("I assume they used to be made through a secret"),
                  Component.literal(" Arcane recipe that has been lost to time."),
                  Component.literal("I wonder if I can extract some Arcana from one"),
                  Component.literal(" of these apples to create something... novel.")
            },
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(OBTAIN_LEADERSHIP_CHARM, new ObtainResearchTask(
            OBTAIN_LEADERSHIP_CHARM.identifier().getPath(), ArcanaRegistry.LEADERSHIP_CHARM.getItem(),
            Component.literal("The Charm of Leadership"),
            new Component[]{
                  Component.literal("I haven't been able to learn much of this"),
                  Component.literal(" ancient divine artifact, other than that"),
                  Component.literal(" it is gifted to those deemed worthy to"),
                  Component.literal(" lead others by a divine entity.")
            },
            ArcanaRegistry.LEADERSHIP_CHARM.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_WINGS_OF_ENDERIA, new ObtainResearchTask(
            OBTAIN_WINGS_OF_ENDERIA.identifier().getPath(), ArcanaRegistry.WINGS_OF_ENDERIA.getItem(),
            Component.literal("The Wings of Enderia"),
            new Component[]{
                  Component.literal("Enderia, the fabled Empress of The End..."),
                  Component.literal("I know little of her, other than she is quite"),
                  Component.literal(" possibly one of the oldest beings still alive."),
                  Component.literal("I also have learned that she is a mad tyrant, "),
                  Component.literal(" who rules over the Enderman with a cold heart."),
                  Component.literal("It is possible she also possesses Divine Arcana..."),
            },
            ArcanaRegistry.WINGS_OF_ENDERIA.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_PICKAXE_OF_CEPTYUS, new ObtainResearchTask(
            OBTAIN_PICKAXE_OF_CEPTYUS.identifier().getPath(), ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem(),
            Component.literal("The Pickaxe of Ceptyus"),
            new Component[]{
                  Component.literal("Archeological inquiry has dug up the name 'Ceptyus'"),
                  Component.literal(" on more than one occasion, mostly in the Deep Dark."),
                  Component.literal("This being was worshipped as a deity to the"),
                  Component.literal(" lost inhabitants of the ancient cities."),
                  Component.literal("Did Ceptyus bring the Sculk? Could Ceptyus BE the Sculk?"),
                  Component.literal("Whatever Ceptyus is, those who worshipped it favored"),
                  Component.literal(" underground exploration and mining a ton of ore."),
            },
            ArcanaRegistry.PICKAXE_OF_CEPTYUS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_SPEAR_OF_TENBROUS, new ObtainResearchTask(
            OBTAIN_SPEAR_OF_TENBROUS.identifier().getPath(), ArcanaRegistry.SPEAR_OF_TENBROUS.getItem(),
            Component.literal("The Spear of Tenbrous"),
            new Component[]{
                  Component.literal("Notes in the old Stronghold libraries speak of a void"),
                  Component.literal(" storm known as 'Tenbrous' that ravaged the End"),
                  Component.literal(" with bolts of piercing green lightning."),
                  Component.literal("Somehow, just hearing its name makes me sick to my stomach."),
                  Component.literal("However, none of my more recent sources mention"),
                  Component.literal(" anything of Tenbrous, or a storm within the End."),
                  Component.literal("Something interesting of note is the connection between"),
                  Component.literal(" Tenbrous and the green color associated with the End Portal"),
                  Component.literal(" and the color of Endermen's eyes after their demise."),
                  Component.literal("I wonder what happened to this ancient force of devastation.")
            },
            ArcanaRegistry.SPEAR_OF_TENBROUS.getPrefItemNoLore(), ADVANCEMENT_EYE_SPY, ADVANCEMENT_ENTER_END
      ));
      
      ResearchTasks.register(OBTAIN_GREAVES_OF_GAIALTUS, new ObtainResearchTask(
            OBTAIN_GREAVES_OF_GAIALTUS.identifier().getPath(), ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem(),
            Component.literal("The Greaves of Gaialtus"),
            new Component[]{
                  Component.literal("'Gaialtus' is a name that often comes up in the"),
                  Component.literal(" old tales of the worlds' original creation."),
                  Component.literal("That which carved the rivers from stone..."),
                  Component.literal("That which planted every seed and sapling..."),
                  Component.literal("Some say that Gaialtus whispers in the wind"),
                  Component.literal(" to encourage travellers on their long journey.")
            },
            ArcanaRegistry.GREAVES_OF_GAIALTUS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(BREAK_SCULK, new StatisticResearchTask<>(
            BREAK_SCULK.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.SCULK)), 1,
            Component.literal("Mine Sculk"),
            new Component[]{
                  Component.literal("I have heard horror stores from the depths."),
                  Component.literal("They speak of a night sky in the caves,"),
                  Component.literal(" glistening with a strange, but familiar glow."),
                  Component.literal("A substance that feels alive? And harbors "),
                  Component.literal(" more experience than any ore mined or smelted."),
                  Component.literal("They speak of an ancient warden who guards it...")
            },
            new ItemStack(Items.SCULK)
      ));
      
      ResearchTasks.register(BREAK_SPAWNER, new StatisticResearchTask<>(
            BREAK_SPAWNER.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.SPAWNER)), 1,
            Component.literal("Break a Spawner"),
            new Component[]{
                  Component.literal("These dungeons have so many great uses, if only"),
                  Component.literal(" I could make one of my own at my base."),
                  Component.literal("Why don't I just try to take this one with me?")
            },
            new ItemStack(Items.SPAWNER), FIND_SPAWNER
      ));
      
      ResearchTasks.register(BREAK_OBSIDIAN, new StatisticResearchTask<>(
            BREAK_OBSIDIAN.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.OBSIDIAN)), 256,
            Component.literal("Mine 4 stacks of Obsidian"),
            new Component[]{
                  Component.literal("Obsidian, yet another crystal to add to my collection."),
                  Component.literal("It's hardness and explosive resistance are such"),
                  Component.literal(" useful properties that I want to gather it in tons.")
            },
            new ItemStack(Items.OBSIDIAN)
      ));
      
      ResearchTasks.register(PLACE_TORCHES, new StatisticResearchTask<>(
            PLACE_TORCHES.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.TORCH)), 128,
            Component.literal("Place 2 stacks of Torches"),
            new Component[]{
                  Component.literal("A stupid creeper blew up my beautiful yard again!"),
                  Component.literal("I can't keep letting this happen! I am going to"),
                  Component.literal(" light up the entire area around my base!")
            },
            new ItemStack(Items.TORCH)
      ));
      
      ResearchTasks.register(USE_FIREWORK, new StatisticResearchTask<>(
            USE_FIREWORK.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.FIREWORK_ROCKET)), 1,
            Component.literal("Use a Firework"),
            new Component[]{
                  Component.literal("I wonder if there is a non-destructive use"),
                  Component.literal(" for gunpowder? Maybe something festive?")
            },
            new ItemStack(Items.FIREWORK_ROCKET)
      ));
      
      ResearchTasks.register(USE_CAMPFIRE, new StatisticResearchTask<>(
            USE_CAMPFIRE.identifier().getPath(), Either.left(Stats.INTERACT_WITH_CAMPFIRE), 1,
            Component.literal("Use a Campfire"),
            new Component[]{
                  Component.literal("Cooking food takes fuel that could be used on ores."),
                  Component.literal("Maybe there is alternative cooking method I can try.")
            },
            new ItemStack(Items.CAMPFIRE)
      ));
      
      ResearchTasks.register(USE_FLINT_AND_STEEL, new StatisticResearchTask<>(
            USE_FLINT_AND_STEEL.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.FLINT_AND_STEEL)), 1,
            Component.literal("Use a Flint and Steel"),
            new Component[]{
                  Component.literal("Fire is such a useful tool. I wish it didn't"),
                  Component.literal(" take a whole setup to start one though."),
                  Component.literal("Maybe there is a tool I can make to start fires.")
            },
            new ItemStack(Items.FLINT_AND_STEEL)
      ));
      
      ResearchTasks.register(USE_ENDER_PEARL, new StatisticResearchTask<>(
            USE_ENDER_PEARL.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENDER_PEARL)), 1,
            Component.literal("Use an Ender Pearl"),
            new Component[]{
                  Component.literal("A poor Enderman got caught out in the rain."),
                  Component.literal("As its flesh melted away from the water, only a"),
                  Component.literal(" strange green pearl remained from behind its eyes."),
                  Component.literal("I wonder why it didn't melt. What does it do?")
            },
            new ItemStack(Items.ENDER_PEARL)
      ));
      
      ResearchTasks.register(USE_ENDER_EYE, new StatisticResearchTask<>(
            USE_ENDER_EYE.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENDER_EYE)), 1,
            Component.literal("Use an Eye of Ender"),
            new Component[]{
                  Component.literal("Interestingly enough, Blaze Powder was the key"),
                  Component.literal(" to re-awakening the Enderman's eye."),
                  Component.literal("However, it does not appear to possess the"),
                  Component.literal(" enhanced teleportation abilities I expected."),
                  Component.literal("There may be more to this mystery yet.")
            },
            new ItemStack(Items.ENDER_EYE), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(USE_ENDER_CHEST, new StatisticResearchTask<>(
            USE_ENDER_CHEST.identifier().getPath(), Either.left(Stats.OPEN_ENDERCHEST), 1,
            Component.literal("Use an Ender Chest"),
            new Component[]{
                  Component.literal("I have found a new use for Eyes of Ender."),
                  Component.literal("Aside from their natural homing ability that"),
                  Component.literal(" I recently discovered, they seem to also peer"),
                  Component.literal(" through dimensions, maybe for extra navigation?"),
                  Component.literal("However the Endermen warp themselves, they briefly"),
                  Component.literal(" enter an interdimensional space, an astral void."),
                  Component.literal("I might be able to use this place for storage.")
            },
            new ItemStack(Items.ENDER_CHEST), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(USE_ENCHANTED_GOLDEN_APPLE, new StatisticResearchTask<>(
            USE_ENCHANTED_GOLDEN_APPLE.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENCHANTED_GOLDEN_APPLE)), 1,
            Component.literal("Eat an Enchanted Golden Apple"),
            new Component[]{
                  Component.literal("At last I have found an Enchanted Golden Apple!"),
                  Component.literal("They say this is the source of some of the most"),
                  Component.literal(" potent regeneration Arcana in the world."),
                  Component.literal("I suppose there is only one way to find out..."),
                  Component.literal("I wonder what it will taste like?")
            },
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), OBTAIN_ENCHANTED_GOLDEN_APPLE
      ));
      
      ResearchTasks.register(KILL_SLIME, new StatisticResearchTask<>(
            KILL_SLIME.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.SLIME)), 100,
            Component.literal("Hunt 100 Slimes"),
            new Component[]{
                  Component.literal("I've never encountered anything as sticky as slime."),
                  Component.literal("This substance will be of great use in large amounts.")
            },
            new ItemStack(Items.SLIME_BALL), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_SQUID, new StatisticResearchTask<>(
            KILL_SQUID.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.SQUID)), 25,
            Component.literal("Hunt 25 Squid"),
            new Component[]{
                  Component.literal("Squids have a strange self defense mechanism."),
                  Component.literal("It is quite unnerving and almost blinding."),
                  Component.literal("I wonder if their ink works on other creatures?")
            },
            new ItemStack(Items.INK_SAC), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_CONSTRUCT, new StatisticResearchTask<>(
            KILL_CONSTRUCT.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, ArcanaRegistry.NUL_CONSTRUCT_ENTITY)), 1,
            Component.literal("Defeat a Nul Construct"),
            new Component[]{
                  Component.literal("I want to push my sovereign catalyst further!"),
                  Component.literal("Divine Arcana is something I have yet to understand,"),
                  Component.literal(" but if I can siphon even a little bit into this"),
                  Component.literal(" catalyst then it will become immensely useful."),
                  Component.literal("Perhaps Divine Arcana has some relation to soul"),
                  Component.literal(" energy? Maybe that soul fueled monstrosity has"),
                  Component.literal(" some relation to a Divine entity?"),
                  Component.literal("Maybe I can make a stronger one, that is more"),
                  Component.literal(" powerful, to draw out some Divine Essence.")
            },
            ArcanaRegistry.NUL_MEMENTO.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST, OBTAIN_NETHER_STAR, OBTAIN_NETHERITE_INGOT, ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_BLAZE, new StatisticResearchTask<>(
            KILL_BLAZE.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.BLAZE)), 100,
            Component.literal("Hunt 100 Blazes"),
            new Component[]{
                  Component.literal("Blazes have a lot of powerful properties."),
                  Component.literal("Flight, fire immunity, flaming projectiles..."),
                  Component.literal("Not to mention their rods have innate alchemical"),
                  Component.literal(" properties that lead to many possible reactions."),
                  Component.literal("It would benefit me greatly to collect more rods.")
            },
            new ItemStack(Items.BLAZE_ROD), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_MAGMA_CUBE, new StatisticResearchTask<>(
            KILL_MAGMA_CUBE.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.MAGMA_CUBE)), 100,
            Component.literal("Hunt 100 Magma Cubes"),
            new Component[]{
                  Component.literal("Magma Cubes are like Slimes, but with the"),
                  Component.literal(" evolutionary advantages of being from the Nether."),
                  Component.literal("However, slime is nothing like magma cream."),
                  Component.literal("Magma cream is smooth and warm, instead of sticky"),
                  Component.literal("I wish to collect some to analyze it further.")
            },
            new ItemStack(Items.MAGMA_CREAM), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_EVOKER, new StatisticResearchTask<>(
            KILL_EVOKER.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.EVOKER)), 10,
            Component.literal("Defeat 10 Evokers"),
            new Component[]{
                  Component.literal("Illagers... The outcast villager faction."),
                  Component.literal("Somehow, they are quite skilled in the Arcane."),
                  Component.literal("However, their use of Arcana is twisted and dark."),
                  Component.literal("Perhaps this was the reason for their exile?"),
                  Component.literal("Regardless, perhaps there is something I can learn"),
                  Component.literal(" from their dark ways. I must visit their Arcanists.")
            },
            new ItemStack(Items.TOTEM_OF_UNDYING), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(CATCH_FISH, new StatisticResearchTask<>(
            CATCH_FISH.identifier().getPath(), Either.left(Stats.FISH_CAUGHT), 100,
            Component.literal("Catch 100 Fish"),
            new Component[]{
                  Component.literal("Fishing is such a relaxing activity."),
                  Component.literal("I should take the time to learn about the various"),
                  Component.literal(" aquatic critters as I catch my new few meals.")
            },
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(SPRINT_TEN_KILOMETERS, new StatisticResearchTask<>(
            SPRINT_TEN_KILOMETERS.identifier().getPath(), Either.left(Stats.SPRINT_ONE_CM), 1000000,
            Component.literal("Sprint a Quarter Marathon"),
            new Component[]{
                  Component.literal("In this vast world I must travel frequently."),
                  Component.literal("In the interest of getting better at moving around"),
                  Component.literal(" I should probably get some better cardio in.")
            },
            new ItemStack(Items.GOLDEN_BOOTS)
      ));
      
      
      ResearchTasks.register(UNLOCK_AQUATIC_EVERSOURCE, new ArcanaItemResearchTask(
            UNLOCK_AQUATIC_EVERSOURCE.identifier().getPath(), ArcanaRegistry.AQUATIC_EVERSOURCE,
            Component.literal("Research the Aquatic Eversource"),
            new Component[]{
                  Component.literal("This design needs to build on a simpler"),
                  Component.literal(" eversource that I have yet to make...")
            },
            ArcanaRegistry.AQUATIC_EVERSOURCE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_RUNIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_RUNIC_MATRIX.identifier().getPath(), ArcanaRegistry.RUNIC_MATRIX,
            Component.literal("Research the Runic Matrix"),
            new Component[]{
                  Component.literal("This design needs something capable of adapting"),
                  Component.literal(" to different forms of Arcana in a modular way."),
                  Component.literal("I think I must invent something else first...")
            },
            ArcanaRegistry.RUNIC_MATRIX.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_STARLIGHT_FORGE, new ArcanaItemResearchTask(
            UNLOCK_STARLIGHT_FORGE.identifier().getPath(), ArcanaRegistry.STARLIGHT_FORGE,
            Component.literal("Research the Starlight Forge"),
            new Component[]{
                  Component.literal("This blueprint expands on the infrastructure"),
                  Component.literal(" that is used to forge Arcane items."),
                  Component.literal("Right now I have no such infrastructure,"),
                  Component.literal(" I will return once I have completed some.")
            },
            ArcanaRegistry.STARLIGHT_FORGE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TWILIGHT_ANVIL, new ArcanaItemResearchTask(
            UNLOCK_TWILIGHT_ANVIL.identifier().getPath(), ArcanaRegistry.TWILIGHT_ANVIL,
            Component.literal("Research the Twilight Anvil"),
            new Component[]{
                  Component.literal("This blueprint would require some infrastructure"),
                  Component.literal(" to forge Netherite and augment Arcana."),
                  Component.literal("I will come back to this once I am prepared.")
            },
            ArcanaRegistry.TWILIGHT_ANVIL.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_TEMPORAL_MOMENT, new ArcanaItemResearchTask(
            UNLOCK_TEMPORAL_MOMENT.identifier().getPath(), ArcanaRegistry.TEMPORAL_MOMENT,
            Component.literal("Research the Temporal Moment"),
            new Component[]{
                  Component.literal("This design is based around the ability to"),
                  Component.literal(" manipulate time. I am afraid that is beyond"),
                  Component.literal(" my current capabilities."),
                  Component.literal("Until I make a new discovery, this is infeasible.")
            },
            ArcanaRegistry.TEMPORAL_MOMENT.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_MATTER, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_MATTER.identifier().getPath(), ArcanaRegistry.EXOTIC_MATTER,
            Component.literal("Research the Exotic Matter"),
            new Component[]{
                  Component.literal("This contraption requires a substance that"),
                  Component.literal(" can cause warps in the fabric of space-time."),
                  Component.literal("There is no point in continuing this schematic"),
                  Component.literal(" until I have discovered this type of fuel.")
            },
            ArcanaRegistry.EXOTIC_MATTER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SHULKER_CORE, new ArcanaItemResearchTask(
            UNLOCK_SHULKER_CORE.identifier().getPath(), ArcanaRegistry.SHULKER_CORE,
            Component.literal("Research the Shulker Core"),
            new Component[]{
                  Component.literal("This device relies on a gravimetric core to defy"),
                  Component.literal(" gravity itself. However, I currently do not have"),
                  Component.literal(" any schematics for such gravity manipulation."),
                  Component.literal("Developing that first would be a better plan.")
            },
            ArcanaRegistry.SHULKER_CORE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_MIDNIGHT_ENCHANTER, new ArcanaItemResearchTask(
            UNLOCK_MIDNIGHT_ENCHANTER.identifier().getPath(), ArcanaRegistry.MIDNIGHT_ENCHANTER,
            Component.literal("Research the Midnight Enchanter"),
            new Component[]{
                  Component.literal("This project requires advanced enchantment"),
                  Component.literal(" capabilities that I do not have right now."),
                  Component.literal("If I can upgrade my infrastructure a bit, "),
                  Component.literal(" then this project would be more feasible.")
            },
            ArcanaRegistry.MIDNIGHT_ENCHANTER.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_STELLAR_CORE, new ArcanaItemResearchTask(
            UNLOCK_STELLAR_CORE.identifier().getPath(), ArcanaRegistry.STELLAR_CORE,
            Component.literal("Research the Stellar Core"),
            new Component[]{
                  Component.literal("This schematic would require a reinforced"),
                  Component.literal(" frame that I do not have the capability"),
                  Component.literal(" to create. I should upgrade my forge setup"),
                  Component.literal(" and then return once I have better equipment.")
            },
            ArcanaRegistry.STELLAR_CORE.getPrefItemNoLore(), UNLOCK_TWILIGHT_ANVIL
      ));
      
      ResearchTasks.register(UNLOCK_ARCANE_SINGULARITY, new ArcanaItemResearchTask(
            UNLOCK_ARCANE_SINGULARITY.identifier().getPath(), ArcanaRegistry.ARCANE_SINGULARITY,
            Component.literal("Research the Arcane Singularity"),
            new Component[]{
                  Component.literal("This project requires Arcane infrastructure"),
                  Component.literal(" that is capable of complete mastery over"),
                  Component.literal(" Arcana infusion with absolute precision."),
                  Component.literal("Without that, there is no way I can make this.")
            },
            ArcanaRegistry.ARCANE_SINGULARITY.getPrefItemNoLore(), UNLOCK_STELLAR_CORE, UNLOCK_MIDNIGHT_ENCHANTER
      ));
      
      ResearchTasks.register(UNLOCK_RADIANT_FLETCHERY, new ArcanaItemResearchTask(
            UNLOCK_RADIANT_FLETCHERY.identifier().getPath(), ArcanaRegistry.RADIANT_FLETCHERY,
            Component.literal("Research the Radiant Fletchery"),
            new Component[]{
                  Component.literal("This equipment needs a dedicated workstation"),
                  Component.literal(" for my archery related inventions."),
                  Component.literal("I will work on that prior to completing this.")
            },
            ArcanaRegistry.RADIANT_FLETCHERY.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_SOULSTONE, new ArcanaItemResearchTask(
            UNLOCK_SOULSTONE.identifier().getPath(), ArcanaRegistry.SOULSTONE,
            Component.literal("Research the Soulstone"),
            new Component[]{
                  Component.literal("This schematic draws upon too much soul energy"),
                  Component.literal(" to be done with only Nether Stars and soul sand."),
                  Component.literal("I need to design a better soul harness first.")
            },
            ArcanaRegistry.SOULSTONE.getPrefItemNoLore(), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(UNLOCK_CATALYTIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_CATALYTIC_MATRIX.identifier().getPath(), ArcanaRegistry.CATALYTIC_MATRIX,
            Component.literal("Research the Catalytic Matrix"),
            new Component[]{
                  Component.literal("To begin delving into Arcane Augmentation,"),
                  Component.literal(" I must first adapt my Runic Matrix into a"),
                  Component.literal(" form designed specifically for catalysis.")
            },
            ArcanaRegistry.CATALYTIC_MATRIX.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_MUNDANE_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_MUNDANE_CATALYST.identifier().getPath(), ArcanaRegistry.MUNDANE_CATALYST,
            Component.literal("Research the Mundane Catalyst"),
            new Component[]{
                  Component.literal("I might be skipping steps here..."),
                  Component.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.MUNDANE_CATALYST.getPrefItemNoLore(), UNLOCK_CATALYTIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_EMPOWERED_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EMPOWERED_CATALYST.identifier().getPath(), ArcanaRegistry.EMPOWERED_CATALYST,
            Component.literal("Research the Empowered Catalyst"),
            new Component[]{
                  Component.literal("I might be skipping steps here..."),
                  Component.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.EMPOWERED_CATALYST.getPrefItemNoLore(), UNLOCK_MUNDANE_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_CATALYST.identifier().getPath(), ArcanaRegistry.EXOTIC_CATALYST,
            Component.literal("Research the Exotic Catalyst"),
            new Component[]{
                  Component.literal("I might be skipping steps here..."),
                  Component.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.EXOTIC_CATALYST.getPrefItemNoLore(), UNLOCK_EMPOWERED_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_SOVEREIGN_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_SOVEREIGN_CATALYST.identifier().getPath(), ArcanaRegistry.SOVEREIGN_CATALYST,
            Component.literal("Research the Sovereign Catalyst"),
            new Component[]{
                  Component.literal("I should maximize the potential of my Catalytic"),
                  Component.literal(" Matrix myself before attempting something as"),
                  Component.literal(" bold as chasing down Divine Arcana.")
            },
            ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), UNLOCK_EXOTIC_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_OVERFLOWING_QUIVER, new ArcanaItemResearchTask(
            UNLOCK_OVERFLOWING_QUIVER.identifier().getPath(), ArcanaRegistry.OVERFLOWING_QUIVER,
            Component.literal("Research the Overflowing Quiver"),
            new Component[]{
                  Component.literal("This blueprint is a bit too bold for me now."),
                  Component.literal("I should break this down into smaller steps,"),
                  Component.literal(" and design a non-runic quiver first.")
            },
            ArcanaRegistry.OVERFLOWING_QUIVER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SPAWNER_HARNESS, new ArcanaItemResearchTask(
            UNLOCK_SPAWNER_HARNESS.identifier().getPath(), ArcanaRegistry.SPAWNER_HARNESS,
            Component.literal("Research the Spawner Harness"),
            new Component[]{
                  Component.literal("This design requires absolute mastery over"),
                  Component.literal(" monster spawners. However, I have yet to"),
                  Component.literal(" successfully find a way to move them."),
                  Component.literal("That is a more important research endeavor.")
            },
            ArcanaRegistry.SPAWNER_HARNESS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TRANSMUTATION_ALTAR, new ArcanaItemResearchTask(
            UNLOCK_TRANSMUTATION_ALTAR.identifier().getPath(), ArcanaRegistry.TRANSMUTATION_ALTAR,
            Component.literal("Research the Transmutation Altar"),
            new Component[]{
                  Component.literal("My notes on this are scattered..."),
                  Component.literal("This has something to do with transmutation?"),
                  Component.literal("I think I need to learn how to transmute things...")
            },
            ArcanaRegistry.TRANSMUTATION_ALTAR.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_WAYSTONE, new ArcanaItemResearchTask(
            UNLOCK_WAYSTONE.identifier().getPath(), ArcanaRegistry.WAYSTONE,
            Component.literal("Research the Waystone"),
            new Component[]{
                  Component.literal("I need a way to help mark a location."),
                  Component.literal("Perhaps a stone can encode a static point?"),
                  Component.literal("I think I've seen something like this before...")
            },
            ArcanaRegistry.WAYSTONE.getPrefItemNoLore()
      ));
      
      
      ResearchTasks.register(ADVANCEMENT_ENCHANT_ITEM, new AdvancementResearchTask(
            ADVANCEMENT_ENCHANT_ITEM.identifier().getPath(), "story/enchant_item",
            Component.literal("Enchant an Item"),
            new Component[]{
                  Component.literal("I should get a grasp on rudimentary Arcane"),
                  Component.literal(" infusion methods before pursuing this.")
            },
            new ItemStack(Items.ENCHANTING_TABLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CHARGE_RESPAWN_ANCHOR, new AdvancementResearchTask(
            ADVANCEMENT_CHARGE_RESPAWN_ANCHOR.identifier().getPath(), "nether/charge_respawn_anchor",
            Component.literal("Anchor Your Respawn Point"),
            new Component[]{
                  Component.literal("Beds have rather explosive consequences in the Nether."),
                  Component.literal("There should be some way to work around this issue.")
            },
            new ItemStack(Items.RESPAWN_ANCHOR)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS.identifier().getPath(), "nether/obtain_ancient_debris",
            Component.literal("Discover the Strongest Material"),
            new Component[]{
                  Component.literal("Whispers speak of a material stronger than diamond!"),
                  Component.literal("It is rumored to be found deep within the Nether...")
            },
            new ItemStack(Items.ANCIENT_DEBRIS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ELYTRA, new AdvancementResearchTask(
            ADVANCEMENT_ELYTRA.identifier().getPath(), "end/elytra",
            Component.literal("Steal a Pair of Wings"),
            new Component[]{
                  Component.literal("Legends speak of the ancient species that once roamed"),
                  Component.literal(" the End with mastery over the skies and stars..."),
                  Component.literal("Maybe there are some artifacts of theirs left behind?")
            },
            new ItemStack(Items.ELYTRA)
      ));
      
      ResearchTasks.register(ADVANCEMENT_FIND_BASTION, new AdvancementResearchTask(
            ADVANCEMENT_FIND_BASTION.identifier().getPath(), "nether/find_bastion",
            Component.literal("Discover an Ancient Bastion"),
            new Component[]{
                  Component.literal("Most of the intelligent creatures in the Nether"),
                  Component.literal(" live in some structures of some sort."),
                  Component.literal("I wonder if there are any that wont attack me.")
            },
            new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS, new AdvancementResearchTask(
            ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS.identifier().getPath(), "adventure/crafters_crafting_crafters",
            Component.literal("Recursively Craft a Crafter"),
            new Component[]{
                  Component.literal("I wonder if an automated crafting matrix"),
                  Component.literal(" is capable of self-replication?")
            },
            new ItemStack(Items.CRAFTER)
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_A_MOB, new AdvancementResearchTask(
            ADVANCEMENT_KILL_A_MOB.identifier().getPath(), "adventure/kill_a_mob",
            Component.literal("Hunt a Mob"),
            new Component[]{
                  Component.literal("These creatures of the night will hunt me no more!")
            },
            new ItemStack(Items.DIAMOND_SWORD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LEVITATE, new AdvancementResearchTask(
            ADVANCEMENT_LEVITATE.identifier().getPath(), "end/levitate",
            Component.literal("Appreciate Levitation from a Great Height"),
            new Component[]{
                  Component.literal("I wonder how high up this unique effect can take me.")
            },
            new ItemStack(Items.SHULKER_BOX)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BALANCED_DIET, new AdvancementResearchTask(
            ADVANCEMENT_BALANCED_DIET.identifier().getPath(), "husbandry/balanced_diet",
            Component.literal("Sample All of the World's Cuisine"),
            new Component[]{
                  Component.literal("Every food has such a unique flavor and taste."),
                  Component.literal("I must try them all!")
            },
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DEFLECT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_DEFLECT_ARROW.identifier().getPath(), "story/deflect_arrow",
            Component.literal("Block an Arrow"),
            new Component[]{
                  Component.literal("Surely there is something that can block an arrow.")
            },
            new ItemStack(Items.SHIELD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ADVENTURING_TIME, new AdvancementResearchTask(
            ADVANCEMENT_ADVENTURING_TIME.identifier().getPath(), "adventure/adventuring_time",
            Component.literal("Discover all of the Overworld's Biomes"),
            new Component[]{
                  Component.literal("The Overworld has such unique biodiversity!"),
                  Component.literal("I must see it all!")
            },
            new ItemStack(Items.DIAMOND_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS, new AdvancementResearchTask(
            ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS.identifier().getPath(), "adventure/walk_on_powder_snow_with_leather_boots",
            Component.literal("Find a Way to Traverse Light Snow"),
            new Component[]{
                  Component.literal("This powdery snow is an absolute death trap!"),
                  Component.literal("There must be something that can keep me safe...")
            },
            new ItemStack(Items.LEATHER_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CREATE_FULL_BEACON, new AdvancementResearchTask(
            ADVANCEMENT_CREATE_FULL_BEACON.identifier().getPath(), "nether/create_full_beacon",
            Component.literal("Maximize a Beacon's Power"),
            new Component[]{
                  Component.literal("Even this little containment pedestal was enough"),
                  Component.literal(" to reveal some of the Nether Star's power!"),
                  Component.literal("I wonder to what heights I can push this?")
            },
            new ItemStack(Items.BEACON), OBTAIN_BEACON
      ));
      
      ResearchTasks.register(ADVANCEMENT_SLEEP_IN_BED, new AdvancementResearchTask(
            ADVANCEMENT_SLEEP_IN_BED.identifier().getPath(), "adventure/sleep_in_bed",
            Component.literal("Have a Sleep"),
            new Component[]{
                  Component.literal("Night time can be scary, perhaps I should sleep it off.")
            },
            new ItemStack(Items.RED_BED)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SHOOT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_SHOOT_ARROW.identifier().getPath(), "adventure/shoot_arrow",
            Component.literal("Become an Archer"),
            new Component[]{
                  Component.literal("If skeletons can do it, so can I!")
            },
            new ItemStack(Items.ARROW)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SNIPER_DUEL, new AdvancementResearchTask(
            ADVANCEMENT_SNIPER_DUEL.identifier().getPath(), "adventure/sniper_duel",
            Component.literal("Duel the World's Best Archers"),
            new Component[]{
                  Component.literal("Surely I can outclass a mere skeleton."),
                  Component.literal("I will defeat one from well over 50 blocks!")
            },
            new ItemStack(Items.BOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BULLSEYE, new AdvancementResearchTask(
            ADVANCEMENT_BULLSEYE.identifier().getPath(), "adventure/bullseye",
            Component.literal("Train Your Archery"),
            new Component[]{
                  Component.literal("My bow skills are a little lacking."),
                  Component.literal("Time to hit the range, and hit the target square on!")
            },
            new ItemStack(Items.TARGET), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREW_POTION, new AdvancementResearchTask(
            ADVANCEMENT_BREW_POTION.identifier().getPath(), "nether/brew_potion",
            Component.literal("Attempt Some Alchemy"),
            new Component[]{
                  Component.literal("If villagers can use a potion stand,"),
                  Component.literal(" I should give it a try myself.")
            },
            new ItemStack(Items.BREWING_STAND)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN.identifier().getPath(), "nether/obtain_crying_obsidian",
            Component.literal("Unlock Obsidian's Hidden Potential"),
            new Component[]{
                  Component.literal("Some of the old portal ruins I have found"),
                  Component.literal(" contain strange obsidian. It has been broken,"),
                  Component.literal(" but not physically? It looks like it's crying."),
                  Component.literal("I wonder what possibilities this could unlock.")
            },
            new ItemStack(Items.CRYING_OBSIDIAN)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DRAGON_BREATH, new AdvancementResearchTask(
            ADVANCEMENT_DRAGON_BREATH.identifier().getPath(), "end/dragon_breath",
            Component.literal("Take a Sample of Enderflame"),
            new Component[]{
                  Component.literal("Enderflame sticks and spreads like a liquid."),
                  Component.literal("It is also, obviously, alchemically active."),
                  Component.literal("I should take a sample for further study.")
            },
            new ItemStack(Items.DRAGON_BREATH), ADVANCEMENT_BREW_POTION
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST, new AdvancementResearchTask(
            ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST.identifier().getPath(), "adventure/kill_mob_near_sculk_catalyst",
            Component.literal("Discover the Sculk's True Nature"),
            new Component[]{
                  Component.literal("This 'Sculk' is unfathomable! If I had to guess"),
                  Component.literal(" it is like some sort of fungus, a giant organism!"),
                  Component.literal("To be this dense in experience, it must be absolutely"),
                  Component.literal(" ancient, and have a rich supply of nutrients to grow."),
                  Component.literal("How does it get nutrients when its underground?")
            },
            new ItemStack(Items.SCULK_CATALYST), BREAK_SCULK
      ));
      
      ResearchTasks.register(ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE, new AdvancementResearchTask(
            ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE.identifier().getPath(), "adventure/lightning_rod_with_villager_no_fire",
            Component.literal("Safely Harness Lightning"),
            new Component[]{
                  Component.literal("With my new lightning rod, I should put it to use!"),
                  Component.literal("I will protect the nearest village from lightning!"),
            },
            new ItemStack(Items.LIGHTNING_ROD), OBTAIN_LIGHTNING_ROD
      ));
      
      ResearchTasks.register(ADVANCEMENT_TAME_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_TAME_AN_ANIMAL.identifier().getPath(), "husbandry/tame_an_animal",
            Component.literal("Experience Pet Ownership"),
            new Component[]{
                  Component.literal("With so many critters out in the world"),
                  Component.literal(" surely at least one wants to be friends.")
            },
            new ItemStack(Items.BONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_TRADE, new AdvancementResearchTask(
            ADVANCEMENT_TRADE.identifier().getPath(), "adventure/trade",
            Component.literal("Learn to Trade"),
            new Component[]{
                  Component.literal("Villagers have a fairly sophisticated economy."),
                  Component.literal("They produce goods and barter amongst themselves."),
                  Component.literal("I should learn how to trade with them.")
            },
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_USE_LODESTONE, new AdvancementResearchTask(
            ADVANCEMENT_USE_LODESTONE.identifier().getPath(), "adventure/use_lodestone",
            Component.literal("Discover Geolocation"),
            new Component[]{
                  Component.literal("I wonder if there is any way to bind a compass"),
                  Component.literal(" to a fixed location instead of my bed?")
            },
            new ItemStack(Items.LODESTONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_COMPLETE_CATALOGUE, new AdvancementResearchTask(
            ADVANCEMENT_COMPLETE_CATALOGUE.identifier().getPath(), "husbandry/complete_catalogue",
            Component.literal("Become a Crazy Cat Lady"),
            new Component[]{
                  Component.literal("Cats are so adorable! I want them all!")
            },
            new ItemStack(Items.COD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_ON, new AdvancementResearchTask(
            ADVANCEMENT_WAX_ON.identifier().getPath(), "husbandry/wax_on",
            Component.literal("Protect Copper from the Elements"),
            new Component[]{
                  Component.literal("Copper would look so pretty if it didn't rust."),
                  Component.literal("I wonder if I can give it a protective coating?")
            },
            new ItemStack(Items.HONEYCOMB)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_OFF, new AdvancementResearchTask(
            ADVANCEMENT_WAX_OFF.identifier().getPath(), "husbandry/wax_off",
            Component.literal("Expose Copper to the Elements"),
            new Component[]{
                  Component.literal("Hmmm, maybe the rust is charming after all.")
            },
            new ItemStack(Items.IRON_AXE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OL_BETSY, new AdvancementResearchTask(
            ADVANCEMENT_OL_BETSY.identifier().getPath(), "adventure/ol_betsy",
            Component.literal("Try Out a New Type of Bow"),
            new Component[]{
                  Component.literal("Bows are fine and all, but some villagers"),
                  Component.literal(" have told me of their exiled members who"),
                  Component.literal(" favor a similar ranged weapon that fires"),
                  Component.literal(" slower but with more power."),
                  Component.literal("I should try one of these 'crossbows' out.")
            },
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_ARBALISTIC, new AdvancementResearchTask(
            ADVANCEMENT_ARBALISTIC.identifier().getPath(), "adventure/arbalistic",
            Component.literal("Master the Crossbow"),
            new Component[]{
                  Component.literal("These piercing bolts are quite the trick."),
                  Component.literal("I wonder how many mobs I can kill in one shot?")
            },
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_TWO_BIRDS_ONE_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_TWO_BIRDS_ONE_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_TWO_BIRDS_ONE_ARROW.identifier().getPath(), "adventure/two_birds_one_arrow",
            Component.literal("Use a Crossbow to Liberate the Night Skies"),
            new Component[]{
                  Component.literal("Crossbows pack quite the punch! And with a "),
                  Component.literal(" bit of enchantment, the arrows can whiz"),
                  Component.literal(" straight through multiple creatures!"),
                  Component.literal("Now I can knock two phantoms out of the sky at once!")
            },
            new ItemStack(Items.PHANTOM_MEMBRANE), ADVANCEMENT_OL_BETSY, ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(ADVANCEMENT_WHOS_THE_PILLAGER_NOW, new AdvancementResearchTask(
            ADVANCEMENT_WHOS_THE_PILLAGER_NOW.identifier().getPath(), "adventure/whos_the_pillager_now",
            Component.literal("Duel the World's Best Arbalesters"),
            new Component[]{
                  Component.literal("It's time for a duel! If I can beat the masters"),
                  Component.literal(" of the crossbow arts, who have been trained to"),
                  Component.literal(" hunt their former neighbors. Then, I should be"),
                  Component.literal(" ready to push this crossbow to its limits.")
            },
            new ItemStack(Items.CROSSBOW),ADVANCEMENT_OL_BETSY
      ));
      
      ResearchTasks.register(ADVANCEMENT_TOTEM_OF_UNDYING, new AdvancementResearchTask(
            ADVANCEMENT_TOTEM_OF_UNDYING.identifier().getPath(), "adventure/totem_of_undying",
            Component.literal("Discover a Form of Immortality"),
            new Component[]{
                  Component.literal("The villagers speak of their exiled brethren."),
                  Component.literal("They tell tales of their dark arcanists who"),
                  Component.literal(" have a method of cheating death itself."),
                  Component.literal("How can this be?!")
            },
            new ItemStack(Items.TOTEM_OF_UNDYING)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LAVA_BUCKET, new AdvancementResearchTask(
            ADVANCEMENT_LAVA_BUCKET.identifier().getPath(), "story/lava_bucket",
            Component.literal("Obtain Lava"),
            new Component[]{
                  Component.literal("Lava is such a useful substance."),
                  Component.literal("I should collect some for study.")
            },
            new ItemStack(Items.LAVA_BUCKET)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER.identifier().getPath(), "husbandry/allay_deliver_item_to_player",
            Component.literal("Befriend an Ancient Forest Sprite"),
            new Component[]{
                  Component.literal("Old tales mention the sprites of the dark forests."),
                  Component.literal("I've seen dark forests, and they are quite spriteless."),
                  Component.literal("I wonder where they have all gone?")
            },
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK.identifier().getPath(), "husbandry/allay_deliver_cake_to_note_block",
            Component.literal("Show Appreciation for the Forest Sprites"),
            new Component[]{
                  Component.literal("These 'Allays' are wonderful! And quite talented!"),
                  Component.literal("They also seem to have some innate Arcane abilities."),
                  Component.literal("I've noticed they like certain chimes, but I wonder"),
                  Component.literal(" if they like cake.")
            },
            new ItemStack(Items.CAKE), ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER
      ));
      
      ResearchTasks.register(ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF, new AdvancementResearchTask(
            ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF.identifier().getPath(), "adventure/read_power_of_chiseled_bookshelf",
            Component.literal("Discover the Power of Books"),
            new Component[]{
                  Component.literal("Chiseled bookshelves are quite fashionable."),
                  Component.literal("I wonder if they have any practical use beyond"),
                  Component.literal(" the obvious book-storing capabilities.")
            },
            new ItemStack(Items.CHISELED_BOOKSHELF)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREED_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_BREED_AN_ANIMAL.identifier().getPath(), "husbandry/breed_an_animal",
            Component.literal("Breed an Animal"),
            new Component[]{
                  Component.literal("Surely I can coax animals into reproducing...")
            },
            new ItemStack(Items.WHEAT)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_SEED.identifier().getPath(), "husbandry/plant_seed",
            Component.literal("Discover Agriculture"),
            new Component[]{
                  Component.literal("With just a few seeds, whole forests grow."),
                  Component.literal("I should learn how to do this myself.")
            },
            new ItemStack(Items.WHEAT_SEEDS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_ANY_SNIFFER_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_ANY_SNIFFER_SEED.identifier().getPath(), "husbandry/plant_any_sniffer_seed",
            Component.literal("Cultivate some Ancient Seeds"),
            new Component[]{
                  Component.literal("There are some ancient seeds which can only be found"),
                  Component.literal(" by a creature of ancient origins, lost to time."),
                  Component.literal("I wish to plant one of these long lost seeds.")
            },
            new ItemStack(Items.TORCHFLOWER_SEEDS), ADVANCEMENT_PLANT_SEED
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_NETHERITE_HOE, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_NETHERITE_HOE.identifier().getPath(), "husbandry/obtain_netherite_hoe",
            Component.literal("Show Dedication to Agriculture"),
            new Component[]{
                  Component.literal("If I am to fully understand agriculture,"),
                  Component.literal(" I must show complete dedication to the craft."),
                  Component.literal("What better way to do that than create a hoe"),
                  Component.literal(" from the strongest material there is.")
            },
            new ItemStack(Items.NETHERITE_HOE), ADVANCEMENT_PLANT_SEED, OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(ADVANCEMENT_BRED_ALL_ANIMALS, new AdvancementResearchTask(
            ADVANCEMENT_BRED_ALL_ANIMALS.identifier().getPath(), "husbandry/bred_all_animals",
            Component.literal("Domesticate Every Animal"),
            new Component[]{
                  Component.literal("In order to master the natural order,"),
                  Component.literal(" I must understand every animal."),
                  Component.literal("To do that I must see the creation of them all.")
            },
            new ItemStack(Items.GOLDEN_CARROT), ADVANCEMENT_BREED_AN_ANIMAL, ADVANCEMENT_TAME_AN_ANIMAL
      ));
      
      ResearchTasks.register(ADVANCEMENT_FALL_FROM_WORLD_HEIGHT, new AdvancementResearchTask(
            ADVANCEMENT_FALL_FROM_WORLD_HEIGHT.identifier().getPath(), "adventure/fall_from_world_height",
            Component.literal("Experience the Height of the World"),
            new Component[]{
                  Component.literal("I should get a better appreciation for how"),
                  Component.literal(" tall and deep this world goes."),
                  Component.literal("From the highest peak to the bedrock, I"),
                  Component.literal(" will experience it all at once!")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(ADVANCEMENT_EYE_SPY, new AdvancementResearchTask(
            ADVANCEMENT_EYE_SPY.identifier().getPath(), "story/follow_ender_eye",
            Component.literal("Follow an Eye of Ender"),
            new Component[]{
                  Component.literal("I used to think that these eyes always pointed"),
                  Component.literal(" in the same direction, but upon the use of"),
                  Component.literal(" some precise measuring tools, it looks like they"),
                  Component.literal(" point to a particular location far away."),
                  Component.literal("I guess it's time to pack my adventuring bags.")
            },
            new ItemStack(Items.MOSSY_STONE_BRICKS), USE_ENDER_EYE
      ));
      
      ResearchTasks.register(ADVANCEMENT_ENTER_END, new AdvancementResearchTask(
            ADVANCEMENT_ENTER_END.identifier().getPath(), "end/root",
            Component.literal("Cross the Ancient Portal"),
            new Component[]{
                  Component.literal("This Stronghold is an incredible structure!"),
                  Component.literal("I wonder who, or what made this stone fortress?"),
                  Component.literal("Did they also make the portals? Or is this stronghold"),
                  Component.literal(" here to guard or protect the portals?"),
                  Component.literal("The libraries here have so much useful knowledge from"),
                  Component.literal(" times before the creation of my current history tomes."),
                  Component.literal("I believe the eyes that led me here can open the portal...")
            },
            new ItemStack(Items.END_STONE), ADVANCEMENT_EYE_SPY
      ));
      
      ResearchTasks.register(ADVANCEMENT_FURIOUS_COCKTAIL, new AdvancementResearchTask(
            ADVANCEMENT_FURIOUS_COCKTAIL.identifier().getPath(), "nether/all_potions",
            Component.literal("Drink All the Potions... At Once"),
            new Component[]{
                  Component.literal("This is probably a really dumb idea..."),
                  Component.literal("But, I really want to drink every potion that"),
                  Component.literal(" I can possibly make and see what happens.")
            },
            new ItemStack(Items.POTION), ADVANCEMENT_BREW_POTION
      ));
      
      ResearchTasks.register(ADVANCEMENT_OVER_OVERKILL, new AdvancementResearchTask(
            ADVANCEMENT_OVER_OVERKILL.identifier().getPath(), "adventure/overoverkill",
            Component.literal("Smash a Mob into Oblivion"),
            new Component[]{
                  Component.literal("This Mace is more powerful than I could've imagined!"),
                  Component.literal("It appears to be able to amplify its gravity to a"),
                  Component.literal(" near infinite amount by storing my gravitational"),
                  Component.literal(" potential energy as it gets converted into speed."),
                  Component.literal("I should see if I can smash something like a "),
                  Component.literal(" zombie with the force of 10 sword hits!")
            },
            new ItemStack(Items.MACE), OBTAIN_MACE
      ));
      
      
      ResearchTasks.register(DIMENSION_TRAVEL, new CustomResearchTask(
            DIMENSION_TRAVEL.identifier().getPath(),
            Component.literal("Discover a New Dimension"),
            new Component[]{
                  Component.literal("I have seen ruined portals and heard rumors of"),
                  Component.literal(" worlds beyond the Overworld, but I have never"),
                  Component.literal(" travelled to one. I wonder what they're like?")
            },
            new ItemStack(Items.END_PORTAL_FRAME)
      ));
      
      ResearchTasks.register(CAT_SCARE, new CustomResearchTask(
            CAT_SCARE.identifier().getPath(),
            Component.literal("Watch a Cat Scare Off Predators"),
            new Component[]{
                  Component.literal("Cats are so cute! It almost makes you"),
                  Component.literal(" forget they are vicious predators!"),
                  Component.literal("I wonder if any mobs are scared of them?")
            },
            new ItemStack(Items.STRING), TAME_CAT
      ));
      
      ResearchTasks.register(RESONATE_BELL, new CustomResearchTask(
            RESONATE_BELL.identifier().getPath(),
            Component.literal("Reveal the Revealing Power of a Bell"),
            new Component[]{
                  Component.literal("Villages all have this central bell, and they"),
                  Component.literal(" place great value on it, like their lives"),
                  Component.literal(" depend on it to protect them or something."),
                  Component.literal("I wonder what is so special about it?")
            },
            new ItemStack(Items.BELL)
      ));
      
      ResearchTasks.register(RIPTIDE_TRIDENT, new CustomResearchTask(
            RIPTIDE_TRIDENT.identifier().getPath(),
            Component.literal("Use a Trident to Soar Through Water"),
            new Component[]{
                  Component.literal("I am trying to figure out ways to propel myself."),
                  Component.literal("I have heard that those with mastery over tridents"),
                  Component.literal(" can use them to move through the water quickly."),
                  Component.literal("I should see if I can try this out.")
            },
            new ItemStack(Items.TRIDENT)
      ));
      
      ResearchTasks.register(FISH_ITEM, new CustomResearchTask(
            FISH_ITEM.identifier().getPath(),
            Component.literal("Use a Fishing Rod to Grab an Item"),
            new Component[]{
                  Component.literal("I wish I could grab items from a further distance."),
                  Component.literal("Maybe there is some way I haven't thought of...")
            },
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(FISH_MOB, new CustomResearchTask(
            FISH_MOB.identifier().getPath(),
            Component.literal("Use a Fishing Rod to Grab a Mob"),
            new Component[]{
                  Component.literal("I wish I could pull some mobs closer to me."),
                  Component.literal("Maybe there is already a way to do this...")
            },
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(LEVEL_100, new CustomResearchTask(
            LEVEL_100.identifier().getPath(),
            Component.literal("Acquire a Century of Experience"),
            new Component[]{
                  Component.literal("I always spend experience almost as fast as"),
                  Component.literal(" I can accumulate it, or lose it entirely."),
                  Component.literal("I should try to reach a high level instead.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(HUNGER_DAMAGE, new CustomResearchTask(
            HUNGER_DAMAGE.identifier().getPath(),
            Component.literal("Experience Hunger Pains"),
            new Component[]{
                  Component.literal("I have always been spoiled for food."),
                  Component.literal("I wonder what it feels like to be truly hungry.")
            },
            new ItemStack(Items.ROTTEN_FLESH)
      ));
      
      ResearchTasks.register(DROWNING_DAMAGE, new CustomResearchTask(
            DROWNING_DAMAGE.identifier().getPath(),
            Component.literal("Experience a Lack of Oxygen"),
            new Component[]{
                  Component.literal("I should test the limits of how long I can"),
                  Component.literal(" hold my breath while working underwater.")
            },
            new ItemStack(Items.TURTLE_HELMET)
      ));
      
      ResearchTasks.register(CONCENTRATION_DAMAGE, new CustomResearchTask(
            CONCENTRATION_DAMAGE.identifier().getPath(),
            Component.literal("Experience Your Mind Collapsing"),
            new Component[]{
                  Component.literal("With every Arcana Item I hold it puts a burden on"),
                  Component.literal(" my mind. Growing ever closer to discomfort."),
                  Component.literal("I wonder how much I can handle?")
            },
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE))
      ));
      
      ResearchTasks.register(FEATHER_FALL, new CustomResearchTask(
            FEATHER_FALL.identifier().getPath(),
            Component.literal("Make a Fall Hurt Less"),
            new Component[]{
                  Component.literal("I keep spraining my ankles doing parkour."),
                  Component.literal("I should find a way to make it hurt less.")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(FIND_SPAWNER, new CustomResearchTask(
            FIND_SPAWNER.identifier().getPath(),
            Component.literal("Discover a Dungeon"),
            new Component[]{
                  Component.literal("I've heard rumors of old dungeons in caves."),
                  Component.literal("I don't know what they are or what they do."),
                  Component.literal("I suppose it is time for some spelunking.")
            },
            new ItemStack(Items.SPAWNER)
      ));
      
      ResearchTasks.register(USE_SOUL_SPEED, new CustomResearchTask(
            USE_SOUL_SPEED.identifier().getPath(),
            Component.literal("Discover the Power of Souls"),
            new Component[]{
                  Component.literal("Surely soul sand doesn't actually contain souls?"),
                  Component.literal("If it did, how are they trapped? What do they do?"),
                  Component.literal("My curiosity is getting the better of me, I must know."),
                  Component.literal("If it is true, there must be a way to use or free them."),
                  Component.literal("Maybe some of the Nether's inhabitants know...")
            },
            new ItemStack(Items.GOLDEN_BOOTS)
      ));
      
      ResearchTasks.register(ACTIVATE_MENDING, new CustomResearchTask(
            ACTIVATE_MENDING.identifier().getPath(),
            Component.literal("Discover the Restorative Effects of Experience"),
            new Component[]{
                  Component.literal("Experience has proven its use for enchanting."),
                  Component.literal("Some old tomes suggest that it can do more."),
                  Component.literal("A specific enchantment that lets it bind to"),
                  Component.literal(" physical materials and undo wear and tear."),
                  Component.literal("I haven't discovered any such enchantment"),
                  Component.literal(" through use of my enchantment table."),
                  Component.literal("I should try and track down any truth to this.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_SILK_TOUCH, new CustomResearchTask(
            OBTAIN_SILK_TOUCH.identifier().getPath(),
            Component.literal("Obtain Silk Touch"),
            new Component[]{
                  Component.literal("The process of destroying and re-placing blocks"),
                  Component.literal(" has proven to be too clumsy to relocate some"),
                  Component.literal(" more delicate materials like glass and grass."),
                  Component.literal("Perhaps there is a way to be more careful when"),
                  Component.literal(" using my tools to break down delicate blocks.")
            },
            new ItemStack(Items.GOLDEN_PICKAXE)
      ));
      
      ResearchTasks.register(INFUSE_ITEM, new CustomResearchTask(
            INFUSE_ITEM.identifier().getPath(),
            Component.literal("Infuse an Item with Stardust"),
            new Component[]{
                  Component.literal("My Starlight Forge has the potential to do much"),
                  Component.literal(" more than simply forge Arcane items."),
                  Component.literal("The power of the starlight that it harnesses "),
                  Component.literal(" should be able to imbue ordinary equipment with"),
                  Component.literal(" much better qualities with the help of Stardust.")
            },
            new ItemStack(Items.GOLDEN_SWORD), OBTAIN_STARDUST, UNLOCK_STELLAR_CORE
      ));
      
      ResearchTasks.register(HONEY_CLEANSE, new CustomResearchTask(
            HONEY_CLEANSE.identifier().getPath(),
            Component.literal("Use Honey as a Cure"),
            new Component[]{
                  Component.literal("Honey is pretty tasty, but some folk-tales"),
                  Component.literal(" say it can also be used as medicine to cure"),
                  Component.literal(" some degenerative and harmful effects."),
                  Component.literal("I should investigate these claims.")
            },
            new ItemStack(Items.HONEY_BOTTLE)
      ));
      
      ResearchTasks.register(MILK_CLEANSE, new CustomResearchTask(
            MILK_CLEANSE.identifier().getPath(),
            Component.literal("Use Milk as a Cure"),
            new Component[]{
                  Component.literal("Supposedly, milk is a time-tested cure-all"),
                  Component.literal(" for any and all effects, both good and bad."),
                  Component.literal("I can't say that I've tried it yet. It could be useful.")
            },
            new ItemStack(Items.MILK_BUCKET)
      ));
      
      ResearchTasks.register(VISIT_DOZEN_BIOMES, new CustomResearchTask(
            VISIT_DOZEN_BIOMES.identifier().getPath(),
            Component.literal("Explore the World"),
            new Component[]{
                  Component.literal("The worlds have such unique biodiversity!"),
                  Component.literal("I should become acquainted with a handful of"),
                  Component.literal(" varying biomes to understand them better.")
            },
            new ItemStack(Items.COMPASS)
      ));
      
      ResearchTasks.register(TAME_CAT, new CustomResearchTask(
            TAME_CAT.identifier().getPath(),
            Component.literal("Obtain a Feline Friend"),
            new Component[]{
                  Component.literal("Villages keep these adorable creatures with them."),
                  Component.literal("I believe they call them 'cats' and they look"),
                  Component.literal(" to be quite powerful allies to have."),
                  Component.literal("I should befriend one of my own.")
            },
            new ItemStack(Items.SALMON)
      ));
      
      ResearchTasks.register(EFFECT_ABSORPTION, new EffectResearchTask(
            EFFECT_ABSORPTION.identifier().getPath(), MobEffects.ABSORPTION,
            Component.literal("Experience Increased Constitution"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can boost my health?")
            },
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(EFFECT_SWIFTNESS, new EffectResearchTask(
            EFFECT_SWIFTNESS.identifier().getPath(), MobEffects.SPEED,
            Component.literal("Experience Swiftness"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can boost my speed?")
            },
            new ItemStack(Items.SUGAR)
      ));
      
      ResearchTasks.register(EFFECT_JUMP_BOOST, new EffectResearchTask(
            EFFECT_JUMP_BOOST.identifier().getPath(), MobEffects.JUMP_BOOST,
            Component.literal("Experience Increased Agility"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can boost my jumps?")
            },
            new ItemStack(Items.RABBIT_FOOT)
      ));
      
      ResearchTasks.register(EFFECT_NIGHT_VISION, new EffectResearchTask(
            EFFECT_NIGHT_VISION.identifier().getPath(), MobEffects.NIGHT_VISION,
            Component.literal("Experience Nocturnal Eyesight"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that lets me see better?")
            },
            new ItemStack(Items.GOLDEN_CARROT)
      ));
      
      ResearchTasks.register(EFFECT_SLOW_FALLING, new EffectResearchTask(
            EFFECT_SLOW_FALLING.identifier().getPath(), MobEffects.SLOW_FALLING,
            Component.literal("Experience a Slow Descent"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can slow my fall?")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(EFFECT_BLINDNESS, new EffectResearchTask(
            EFFECT_BLINDNESS.identifier().getPath(), MobEffects.BLINDNESS,
            Component.literal("Experience Blindness"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can blind enemies?")
            },
            new ItemStack(Items.INK_SAC)
      ));
      
      ResearchTasks.register(EFFECT_SLOWNESS, new EffectResearchTask(
            EFFECT_SLOWNESS.identifier().getPath(), MobEffects.SLOWNESS,
            Component.literal("Experience Sluggishness"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can slow enemies?")
            },
            new ItemStack(Items.COBWEB)
      ));
      
      ResearchTasks.register(EFFECT_FIRE_RESISTANCE, new EffectResearchTask(
            EFFECT_FIRE_RESISTANCE.identifier().getPath(), MobEffects.FIRE_RESISTANCE,
            Component.literal("Experience Fire Resistance"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that helps with fire?")
            },
            new ItemStack(Items.MAGMA_CREAM)
      ));
      
      ResearchTasks.register(EFFECT_STRENGTH, new EffectResearchTask(
            EFFECT_STRENGTH.identifier().getPath(), MobEffects.STRENGTH,
            Component.literal("Experience Increased Strength"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can make me stronger?")
            },
            new ItemStack(Items.IRON_SWORD)
      ));
      
      ResearchTasks.register(EFFECT_WEAKNESS, new EffectResearchTask(
            EFFECT_WEAKNESS.identifier().getPath(), MobEffects.WEAKNESS,
            Component.literal("Experience Decreased Strength"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that can weaken enemies?")
            },
            new ItemStack(Items.WOODEN_SWORD)
      ));
      
      ResearchTasks.register(EFFECT_NAUSEA, new EffectResearchTask(
            EFFECT_NAUSEA.identifier().getPath(), MobEffects.NAUSEA,
            Component.literal("Experience Nausea"),
            new Component[]{
                  Component.literal("I wonder if there's a consequence to eating bad food?")
            },
            new ItemStack(Items.PUFFERFISH)
      ));
      
      ResearchTasks.register(EFFECT_POISON, new EffectResearchTask(
            EFFECT_POISON.identifier().getPath(), MobEffects.POISON,
            Component.literal("Experience a Slow Pain"),
            new Component[]{
                  Component.literal("I wonder if there's an effect that slowly damages enemies?")
            },
            new ItemStack(Items.SPIDER_EYE)
      ));
      
      ResearchTasks.register(EFFECT_DOLPHINS_GRACE, new EffectResearchTask(
            EFFECT_DOLPHINS_GRACE.identifier().getPath(), MobEffects.DOLPHINS_GRACE,
            Component.literal("Experience the Dolphin's Grace"),
            new Component[]{
                  Component.literal("I wonder how dolphins slip through the water so quickly?")
            },
            new ItemStack(Items.TROPICAL_FISH)
      ));
      
      
      
      
   }
   
   private static ResearchTask register(ResourceKey<ResearchTask> key, ResearchTask task){
      Registry.register(RESEARCH_TASKS,key,task);
      return task;
   }
   
   private static ResourceKey<ResearchTask> of(String id){
      return ResourceKey.create(RESEARCH_TASKS.key(), Identifier.fromNamespaceAndPath(MOD_ID,id));
   }
   
   public static List<TreeNode<ResearchTask>> buildTaskTrees(ResourceKey<ResearchTask>[] tasks){
      List<TreeNode<ResearchTask>> taskTrees = new ArrayList<>();
      Set<ResearchTask> addedTasks = new HashSet<>();
      
      for(ResourceKey<ResearchTask> key : tasks){
         ResearchTask task = RESEARCH_TASKS.getValue(key);
         if(task == null || addedTasks.contains(task)) continue;
         taskTrees.add(generateTaskNode(task,null, addedTasks));
      }
      return taskTrees;
   }
   
   public static Set<ResearchTask> getUniqueTasks(ResourceKey<ResearchTask>[] tasks){
      Set<ResearchTask> addedTasks = new HashSet<>();
      
      for(ResourceKey<ResearchTask> key : tasks){
         ResearchTask task = RESEARCH_TASKS.getValue(key);
         if(task == null || addedTasks.contains(task)) continue;
         generateTaskNode(task,null, addedTasks);
      }
      return addedTasks;
   }
   
   private static TreeNode<ResearchTask> generateTaskNode(ResearchTask task, TreeNode<ResearchTask> parent, Set<ResearchTask> trackerSet){
      TreeNode<ResearchTask> node = new TreeNode<>(task, new HashSet<>(), parent);
      trackerSet.add(task);
      for(ResearchTask preReq : task.getPreReqs()){
         node.addChild(generateTaskNode(preReq,node,trackerSet));
      }
      return node;
   }
}
