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
   public static final ResourceKey<ResearchTask> OBTAIN_LANTERN = of("obtain_lantern");
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
   public static final ResourceKey<ResearchTask> WALK_ONE_KILOMETER = of("walk_one_kilometer");
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
   public static final ResourceKey<ResearchTask> ADVANCEMENT_BARTER_PIGLIN = of("advancement_barter_piglin");
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
            new ItemStack(Items.SPONGE)
      ));
      
      ResearchTasks.register(OBTAIN_END_CRYSTAL, new ObtainResearchTask(
            OBTAIN_END_CRYSTAL.identifier().getPath(), Items.END_CRYSTAL,
            new ItemStack(Items.END_CRYSTAL)
      ));
      
      ResearchTasks.register(OBTAIN_SPECTRAL_ARROW, new ObtainResearchTask(
            OBTAIN_SPECTRAL_ARROW.identifier().getPath(), Items.SPECTRAL_ARROW,
            new ItemStack(Items.SPECTRAL_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_SHARD, new ObtainResearchTask(
            OBTAIN_AMETHYST_SHARD.identifier().getPath(), Items.AMETHYST_SHARD,
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_SWORD, new ObtainResearchTask(
            OBTAIN_NETHERITE_SWORD.identifier().getPath(), Items.NETHERITE_SWORD,
            new ItemStack(Items.NETHERITE_SWORD), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHER_STAR, new ObtainResearchTask(
            OBTAIN_NETHER_STAR.identifier().getPath(), Items.NETHER_STAR,
            new ItemStack(Items.NETHER_STAR), USE_SOUL_SPEED
      ));
      
      ResearchTasks.register(OBTAIN_EYE_OF_ENDER, new ObtainResearchTask(
            OBTAIN_EYE_OF_ENDER.identifier().getPath(), Items.ENDER_EYE,
            new ItemStack(Items.ENDER_EYE), USE_ENDER_PEARL
      ));
      
      ResearchTasks.register(OBTAIN_BOTTLES_OF_ENCHANTING, new ObtainResearchTask(
            OBTAIN_BOTTLES_OF_ENCHANTING.identifier().getPath(), Items.EXPERIENCE_BOTTLE,
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(OBTAIN_ZOMBIE_HEAD, new ObtainResearchTask(
            OBTAIN_ZOMBIE_HEAD.identifier().getPath(), Items.ZOMBIE_HEAD,
            new ItemStack(Items.ZOMBIE_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_EGG, new ObtainResearchTask(
            OBTAIN_EGG.identifier().getPath(), Items.EGG,
            new ItemStack(Items.EGG)
      ));
      
      ResearchTasks.register(OBTAIN_BEACON, new ObtainResearchTask(
            OBTAIN_BEACON.identifier().getPath(), Items.BEACON,
            new ItemStack(Items.BEACON), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(OBTAIN_DIVINE_CATALYST, new ObtainResearchTask(
            OBTAIN_DIVINE_CATALYST.identifier().getPath(), ArcanaRegistry.DIVINE_CATALYST.getItem(),
            ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST
      ));
      
      ResearchTasks.register(OBTAIN_GOLD_INGOT, new ObtainResearchTask(
            OBTAIN_GOLD_INGOT.identifier().getPath(), Items.GOLD_INGOT,
            new ItemStack(Items.GOLD_INGOT)
      ));
      
      ResearchTasks.register(OBTAIN_EMERALD, new ObtainResearchTask(
            OBTAIN_EMERALD.identifier().getPath(), Items.EMERALD,
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(OBTAIN_DIAMOND, new ObtainResearchTask(
            OBTAIN_DIAMOND.identifier().getPath(), Items.DIAMOND,
            new ItemStack(Items.DIAMOND)
      ));
      
      ResearchTasks.register(OBTAIN_QUARTZ, new ObtainResearchTask(
            OBTAIN_QUARTZ.identifier().getPath(), Items.QUARTZ,
            new ItemStack(Items.QUARTZ)
      ));
      
      ResearchTasks.register(OBTAIN_CLOCK, new ObtainResearchTask(
            OBTAIN_CLOCK.identifier().getPath(), Items.CLOCK,
            new ItemStack(Items.CLOCK)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_INGOT, new ObtainResearchTask(
            OBTAIN_NETHERITE_INGOT.identifier().getPath(), Items.NETHERITE_INGOT,
            new ItemStack(Items.NETHERITE_INGOT), ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_PISTON, new ObtainResearchTask(
            OBTAIN_PISTON.identifier().getPath(), Items.PISTON,
            new ItemStack(Items.PISTON)
      ));
      
      ResearchTasks.register(OBTAIN_GLISTERING_MELON, new ObtainResearchTask(
            OBTAIN_GLISTERING_MELON.identifier().getPath(), Items.GLISTERING_MELON_SLICE,
            new ItemStack(Items.GLISTERING_MELON_SLICE), ADVANCEMENT_BREW_POTION, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_PICKAXE, new ObtainResearchTask(
            OBTAIN_NETHERITE_PICKAXE.identifier().getPath(), Items.NETHERITE_PICKAXE,
            new ItemStack(Items.NETHERITE_PICKAXE), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_TNT, new ObtainResearchTask(
            OBTAIN_TNT.identifier().getPath(), Items.TNT,
            new ItemStack(Items.TNT)
      ));
      
      ResearchTasks.register(OBTAIN_TIPPED_ARROW, new ObtainResearchTask(
            OBTAIN_TIPPED_ARROW.identifier().getPath(), Items.TIPPED_ARROW,
            new ItemStack(Items.TIPPED_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_LIGHTNING_ROD, new ObtainResearchTask(
            OBTAIN_LIGHTNING_ROD.identifier().getPath(), Items.LIGHTNING_ROD,
            new ItemStack(Items.LIGHTNING_ROD)
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_CLUSTER, new ObtainResearchTask(
            OBTAIN_AMETHYST_CLUSTER.identifier().getPath(), Items.AMETHYST_CLUSTER,
            new ItemStack(Items.AMETHYST_CLUSTER), OBTAIN_AMETHYST_SHARD, OBTAIN_SILK_TOUCH
      ));
      
      ResearchTasks.register(OBTAIN_ARCANE_TOME, new ObtainResearchTask(
            "obtain_tome_of_arcana_novum", ArcanaRegistry.ARCANE_TOME.getItem(),
            ArcanaRegistry.ARCANE_TOME.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_CREEPER_HEAD, new ObtainResearchTask(
            OBTAIN_CREEPER_HEAD.identifier().getPath(), Items.CREEPER_HEAD,
            new ItemStack(Items.CREEPER_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_HEAVY_CORE, new ObtainResearchTask(
            OBTAIN_HEAVY_CORE.identifier().getPath(), Items.HEAVY_CORE,
            new ItemStack(Items.HEAVY_CORE)
      ));
      
      ResearchTasks.register(OBTAIN_HEART_OF_THE_SEA, new ObtainResearchTask(
            OBTAIN_HEART_OF_THE_SEA.identifier().getPath(), Items.HEART_OF_THE_SEA,
            new ItemStack(Items.HEART_OF_THE_SEA)
      ));
      
      ResearchTasks.register(OBTAIN_STARDUST, new ObtainResearchTask(
            OBTAIN_STARDUST.identifier().getPath(), ArcanaRegistry.STARDUST,
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.STARDUST)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_NEBULOUS_ESSENCE, new ObtainResearchTask(
            OBTAIN_NEBULOUS_ESSENCE.identifier().getPath(), ArcanaRegistry.NEBULOUS_ESSENCE,
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_BLAST_FURNACE, new ObtainResearchTask(
            OBTAIN_BLAST_FURNACE.identifier().getPath(), Items.BLAST_FURNACE,
            new ItemStack(Items.BLAST_FURNACE)
      ));
      
      ResearchTasks.register(OBTAIN_BLUE_ICE, new ObtainResearchTask(
            OBTAIN_BLUE_ICE.identifier().getPath(), Items.BLUE_ICE,
            new ItemStack(Items.BLUE_ICE)
      ));
      
      ResearchTasks.register(OBTAIN_ANVIL, new ObtainResearchTask(
            OBTAIN_ANVIL.identifier().getPath(), Items.ANVIL,
            new ItemStack(Items.ANVIL)
      ));
      
      ResearchTasks.register(OBTAIN_MACE, new ObtainResearchTask(
            OBTAIN_MACE.identifier().getPath(), Items.MACE,
            new ItemStack(Items.MACE), OBTAIN_HEAVY_CORE
      ));
      
      ResearchTasks.register(OBTAIN_CONDUIT, new ObtainResearchTask(
            OBTAIN_CONDUIT.identifier().getPath(), Items.CONDUIT,
            new ItemStack(Items.CONDUIT), OBTAIN_HEART_OF_THE_SEA
      ));
      
      ResearchTasks.register(OBTAIN_ENCHANTED_GOLDEN_APPLE, new ObtainResearchTask(
            OBTAIN_ENCHANTED_GOLDEN_APPLE.identifier().getPath(), Items.ENCHANTED_GOLDEN_APPLE,
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(OBTAIN_LEADERSHIP_CHARM, new ObtainResearchTask(
            OBTAIN_LEADERSHIP_CHARM.identifier().getPath(), ArcanaRegistry.LEADERSHIP_CHARM.getItem(),
            ArcanaRegistry.LEADERSHIP_CHARM.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_WINGS_OF_ENDERIA, new ObtainResearchTask(
            OBTAIN_WINGS_OF_ENDERIA.identifier().getPath(), ArcanaRegistry.WINGS_OF_ENDERIA.getItem(),
            ArcanaRegistry.WINGS_OF_ENDERIA.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_PICKAXE_OF_CEPTYUS, new ObtainResearchTask(
            OBTAIN_PICKAXE_OF_CEPTYUS.identifier().getPath(), ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem(),
            ArcanaRegistry.PICKAXE_OF_CEPTYUS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_SPEAR_OF_TENBROUS, new ObtainResearchTask(
            OBTAIN_SPEAR_OF_TENBROUS.identifier().getPath(), ArcanaRegistry.SPEAR_OF_TENBROUS.getItem(),
            ArcanaRegistry.SPEAR_OF_TENBROUS.getPrefItemNoLore(), ADVANCEMENT_EYE_SPY, ADVANCEMENT_ENTER_END
      ));
      
      ResearchTasks.register(OBTAIN_GREAVES_OF_GAIALTUS, new ObtainResearchTask(
            OBTAIN_GREAVES_OF_GAIALTUS.identifier().getPath(), ArcanaRegistry.GREAVES_OF_GAIALTUS.getItem(),
            ArcanaRegistry.GREAVES_OF_GAIALTUS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_LANTERN, new ObtainResearchTask(
            OBTAIN_LANTERN.identifier().getPath(), Items.LANTERN,
            new ItemStack(Items.LANTERN)
      ));
      
      ResearchTasks.register(BREAK_SCULK, new StatisticResearchTask<>(
            BREAK_SCULK.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.SCULK)), 1,
            new ItemStack(Items.SCULK)
      ));
      
      ResearchTasks.register(BREAK_SPAWNER, new StatisticResearchTask<>(
            BREAK_SPAWNER.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.SPAWNER)), 1,
            new ItemStack(Items.SPAWNER), FIND_SPAWNER
      ));
      
      ResearchTasks.register(BREAK_OBSIDIAN, new StatisticResearchTask<>(
            BREAK_OBSIDIAN.identifier().getPath(), Either.right(new Tuple<>(Stats.BLOCK_MINED, Blocks.OBSIDIAN)), 256,
            new ItemStack(Items.OBSIDIAN)
      ));
      
      ResearchTasks.register(PLACE_TORCHES, new StatisticResearchTask<>(
            PLACE_TORCHES.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.TORCH)), 128,
            new ItemStack(Items.TORCH)
      ));
      
      ResearchTasks.register(USE_FIREWORK, new StatisticResearchTask<>(
            USE_FIREWORK.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.FIREWORK_ROCKET)), 1,
            new ItemStack(Items.FIREWORK_ROCKET)
      ));
      
      ResearchTasks.register(USE_CAMPFIRE, new StatisticResearchTask<>(
            USE_CAMPFIRE.identifier().getPath(), Either.left(Stats.INTERACT_WITH_CAMPFIRE), 1,
            new ItemStack(Items.CAMPFIRE)
      ));
      
      ResearchTasks.register(USE_FLINT_AND_STEEL, new StatisticResearchTask<>(
            USE_FLINT_AND_STEEL.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.FLINT_AND_STEEL)), 1,
            new ItemStack(Items.FLINT_AND_STEEL)
      ));
      
      ResearchTasks.register(USE_ENDER_PEARL, new StatisticResearchTask<>(
            USE_ENDER_PEARL.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENDER_PEARL)), 1,
            new ItemStack(Items.ENDER_PEARL)
      ));
      
      ResearchTasks.register(USE_ENDER_EYE, new StatisticResearchTask<>(
            USE_ENDER_EYE.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENDER_EYE)), 1,
            new ItemStack(Items.ENDER_EYE), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(USE_ENDER_CHEST, new StatisticResearchTask<>(
            USE_ENDER_CHEST.identifier().getPath(), Either.left(Stats.OPEN_ENDERCHEST), 1,
            new ItemStack(Items.ENDER_CHEST), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(USE_ENCHANTED_GOLDEN_APPLE, new StatisticResearchTask<>(
            USE_ENCHANTED_GOLDEN_APPLE.identifier().getPath(), Either.right(new Tuple<>(Stats.ITEM_USED, Items.ENCHANTED_GOLDEN_APPLE)), 1,
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), OBTAIN_ENCHANTED_GOLDEN_APPLE
      ));
      
      ResearchTasks.register(KILL_SLIME, new StatisticResearchTask<>(
            KILL_SLIME.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.SLIME)), 100,
            new ItemStack(Items.SLIME_BALL), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_SQUID, new StatisticResearchTask<>(
            KILL_SQUID.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.SQUID)), 25,
            new ItemStack(Items.INK_SAC), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_CONSTRUCT, new StatisticResearchTask<>(
            KILL_CONSTRUCT.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, ArcanaRegistry.NUL_CONSTRUCT_ENTITY)), 1,
            ArcanaRegistry.NUL_MEMENTO.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST, OBTAIN_NETHER_STAR, OBTAIN_NETHERITE_INGOT, ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_BLAZE, new StatisticResearchTask<>(
            KILL_BLAZE.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.BLAZE)), 100,
            new ItemStack(Items.BLAZE_ROD), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_MAGMA_CUBE, new StatisticResearchTask<>(
            KILL_MAGMA_CUBE.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.MAGMA_CUBE)), 100,
            new ItemStack(Items.MAGMA_CREAM), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_EVOKER, new StatisticResearchTask<>(
            KILL_EVOKER.identifier().getPath(), Either.right(new Tuple<>(Stats.ENTITY_KILLED, EntityType.EVOKER)), 10,
            new ItemStack(Items.TOTEM_OF_UNDYING), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(CATCH_FISH, new StatisticResearchTask<>(
            CATCH_FISH.identifier().getPath(), Either.left(Stats.FISH_CAUGHT), 100,
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(SPRINT_TEN_KILOMETERS, new StatisticResearchTask<>(
            SPRINT_TEN_KILOMETERS.identifier().getPath(), Either.left(Stats.SPRINT_ONE_CM), 1000000,
            new ItemStack(Items.GOLDEN_BOOTS), WALK_ONE_KILOMETER
      ));
      
      ResearchTasks.register(WALK_ONE_KILOMETER, new StatisticResearchTask<>(
            WALK_ONE_KILOMETER.identifier().getPath(), Either.left(Stats.WALK_ONE_CM), 100000,
            new ItemStack(Items.LEATHER_BOOTS)
      ));
      
      
      ResearchTasks.register(UNLOCK_AQUATIC_EVERSOURCE, new ArcanaItemResearchTask(
            UNLOCK_AQUATIC_EVERSOURCE.identifier().getPath(), ArcanaRegistry.AQUATIC_EVERSOURCE,
            ArcanaRegistry.AQUATIC_EVERSOURCE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_RUNIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_RUNIC_MATRIX.identifier().getPath(), ArcanaRegistry.RUNIC_MATRIX,
            ArcanaRegistry.RUNIC_MATRIX.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_STARLIGHT_FORGE, new ArcanaItemResearchTask(
            UNLOCK_STARLIGHT_FORGE.identifier().getPath(), ArcanaRegistry.STARLIGHT_FORGE,
            ArcanaRegistry.STARLIGHT_FORGE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TWILIGHT_ANVIL, new ArcanaItemResearchTask(
            UNLOCK_TWILIGHT_ANVIL.identifier().getPath(), ArcanaRegistry.TWILIGHT_ANVIL,
            ArcanaRegistry.TWILIGHT_ANVIL.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_TEMPORAL_MOMENT, new ArcanaItemResearchTask(
            UNLOCK_TEMPORAL_MOMENT.identifier().getPath(), ArcanaRegistry.TEMPORAL_MOMENT,
            ArcanaRegistry.TEMPORAL_MOMENT.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_MATTER, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_MATTER.identifier().getPath(), ArcanaRegistry.EXOTIC_MATTER,
            ArcanaRegistry.EXOTIC_MATTER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SHULKER_CORE, new ArcanaItemResearchTask(
            UNLOCK_SHULKER_CORE.identifier().getPath(), ArcanaRegistry.SHULKER_CORE,
            ArcanaRegistry.SHULKER_CORE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_MIDNIGHT_ENCHANTER, new ArcanaItemResearchTask(
            UNLOCK_MIDNIGHT_ENCHANTER.identifier().getPath(), ArcanaRegistry.MIDNIGHT_ENCHANTER,
            ArcanaRegistry.MIDNIGHT_ENCHANTER.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_STELLAR_CORE, new ArcanaItemResearchTask(
            UNLOCK_STELLAR_CORE.identifier().getPath(), ArcanaRegistry.STELLAR_CORE,
            ArcanaRegistry.STELLAR_CORE.getPrefItemNoLore(), UNLOCK_TWILIGHT_ANVIL
      ));
      
      ResearchTasks.register(UNLOCK_ARCANE_SINGULARITY, new ArcanaItemResearchTask(
            UNLOCK_ARCANE_SINGULARITY.identifier().getPath(), ArcanaRegistry.ARCANE_SINGULARITY,
            ArcanaRegistry.ARCANE_SINGULARITY.getPrefItemNoLore(), UNLOCK_STELLAR_CORE, UNLOCK_MIDNIGHT_ENCHANTER
      ));
      
      ResearchTasks.register(UNLOCK_RADIANT_FLETCHERY, new ArcanaItemResearchTask(
            UNLOCK_RADIANT_FLETCHERY.identifier().getPath(), ArcanaRegistry.RADIANT_FLETCHERY,
            ArcanaRegistry.RADIANT_FLETCHERY.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_SOULSTONE, new ArcanaItemResearchTask(
            UNLOCK_SOULSTONE.identifier().getPath(), ArcanaRegistry.SOULSTONE,
            ArcanaRegistry.SOULSTONE.getPrefItemNoLore(), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(UNLOCK_CATALYTIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_CATALYTIC_MATRIX.identifier().getPath(), ArcanaRegistry.CATALYTIC_MATRIX,
            ArcanaRegistry.CATALYTIC_MATRIX.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_MUNDANE_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_MUNDANE_CATALYST.identifier().getPath(), ArcanaRegistry.MUNDANE_CATALYST,
            ArcanaRegistry.MUNDANE_CATALYST.getPrefItemNoLore(), UNLOCK_CATALYTIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_EMPOWERED_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EMPOWERED_CATALYST.identifier().getPath(), ArcanaRegistry.EMPOWERED_CATALYST,
            ArcanaRegistry.EMPOWERED_CATALYST.getPrefItemNoLore(), UNLOCK_MUNDANE_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_CATALYST.identifier().getPath(), ArcanaRegistry.EXOTIC_CATALYST,
            ArcanaRegistry.EXOTIC_CATALYST.getPrefItemNoLore(), UNLOCK_EMPOWERED_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_SOVEREIGN_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_SOVEREIGN_CATALYST.identifier().getPath(), ArcanaRegistry.SOVEREIGN_CATALYST,
            ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), UNLOCK_EXOTIC_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_OVERFLOWING_QUIVER, new ArcanaItemResearchTask(
            UNLOCK_OVERFLOWING_QUIVER.identifier().getPath(), ArcanaRegistry.OVERFLOWING_QUIVER,
            ArcanaRegistry.OVERFLOWING_QUIVER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SPAWNER_HARNESS, new ArcanaItemResearchTask(
            UNLOCK_SPAWNER_HARNESS.identifier().getPath(), ArcanaRegistry.SPAWNER_HARNESS,
            ArcanaRegistry.SPAWNER_HARNESS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TRANSMUTATION_ALTAR, new ArcanaItemResearchTask(
            UNLOCK_TRANSMUTATION_ALTAR.identifier().getPath(), ArcanaRegistry.TRANSMUTATION_ALTAR,
            ArcanaRegistry.TRANSMUTATION_ALTAR.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_WAYSTONE, new ArcanaItemResearchTask(
            UNLOCK_WAYSTONE.identifier().getPath(), ArcanaRegistry.WAYSTONE,
            ArcanaRegistry.WAYSTONE.getPrefItemNoLore()
      ));
      
      
      ResearchTasks.register(ADVANCEMENT_ENCHANT_ITEM, new AdvancementResearchTask(
            ADVANCEMENT_ENCHANT_ITEM.identifier().getPath(), "story/enchant_item",
            new ItemStack(Items.ENCHANTING_TABLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CHARGE_RESPAWN_ANCHOR, new AdvancementResearchTask(
            ADVANCEMENT_CHARGE_RESPAWN_ANCHOR.identifier().getPath(), "nether/charge_respawn_anchor",
            new ItemStack(Items.RESPAWN_ANCHOR)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS.identifier().getPath(), "nether/obtain_ancient_debris",
            new ItemStack(Items.ANCIENT_DEBRIS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ELYTRA, new AdvancementResearchTask(
            ADVANCEMENT_ELYTRA.identifier().getPath(), "end/elytra",
            new ItemStack(Items.ELYTRA)
      ));
      
      ResearchTasks.register(ADVANCEMENT_FIND_BASTION, new AdvancementResearchTask(
            ADVANCEMENT_FIND_BASTION.identifier().getPath(), "nether/find_bastion",
            new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS, new AdvancementResearchTask(
            ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS.identifier().getPath(), "adventure/crafters_crafting_crafters",
            new ItemStack(Items.CRAFTER)
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_A_MOB, new AdvancementResearchTask(
            ADVANCEMENT_KILL_A_MOB.identifier().getPath(), "adventure/kill_a_mob",
            new ItemStack(Items.DIAMOND_SWORD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LEVITATE, new AdvancementResearchTask(
            ADVANCEMENT_LEVITATE.identifier().getPath(), "end/levitate",
            new ItemStack(Items.SHULKER_BOX)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BALANCED_DIET, new AdvancementResearchTask(
            ADVANCEMENT_BALANCED_DIET.identifier().getPath(), "husbandry/balanced_diet",
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DEFLECT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_DEFLECT_ARROW.identifier().getPath(), "story/deflect_arrow",
            new ItemStack(Items.SHIELD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ADVENTURING_TIME, new AdvancementResearchTask(
            ADVANCEMENT_ADVENTURING_TIME.identifier().getPath(), "adventure/adventuring_time",
            new ItemStack(Items.DIAMOND_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS, new AdvancementResearchTask(
            ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS.identifier().getPath(), "adventure/walk_on_powder_snow_with_leather_boots",
            new ItemStack(Items.LEATHER_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CREATE_FULL_BEACON, new AdvancementResearchTask(
            ADVANCEMENT_CREATE_FULL_BEACON.identifier().getPath(), "nether/create_full_beacon",
            new ItemStack(Items.BEACON), OBTAIN_BEACON
      ));
      
      ResearchTasks.register(ADVANCEMENT_SLEEP_IN_BED, new AdvancementResearchTask(
            ADVANCEMENT_SLEEP_IN_BED.identifier().getPath(), "adventure/sleep_in_bed",
            new ItemStack(Items.RED_BED)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SHOOT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_SHOOT_ARROW.identifier().getPath(), "adventure/shoot_arrow",
            new ItemStack(Items.ARROW)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SNIPER_DUEL, new AdvancementResearchTask(
            ADVANCEMENT_SNIPER_DUEL.identifier().getPath(), "adventure/sniper_duel",
            new ItemStack(Items.BOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BULLSEYE, new AdvancementResearchTask(
            ADVANCEMENT_BULLSEYE.identifier().getPath(), "adventure/bullseye",
            new ItemStack(Items.TARGET), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREW_POTION, new AdvancementResearchTask(
            ADVANCEMENT_BREW_POTION.identifier().getPath(), "nether/brew_potion",
            new ItemStack(Items.BREWING_STAND)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN.identifier().getPath(), "nether/obtain_crying_obsidian",
            new ItemStack(Items.CRYING_OBSIDIAN)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DRAGON_BREATH, new AdvancementResearchTask(
            ADVANCEMENT_DRAGON_BREATH.identifier().getPath(), "end/dragon_breath",
            new ItemStack(Items.DRAGON_BREATH), ADVANCEMENT_BREW_POTION
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST, new AdvancementResearchTask(
            ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST.identifier().getPath(), "adventure/kill_mob_near_sculk_catalyst",
            new ItemStack(Items.SCULK_CATALYST), BREAK_SCULK
      ));
      
      ResearchTasks.register(ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE, new AdvancementResearchTask(
            ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE.identifier().getPath(), "adventure/lightning_rod_with_villager_no_fire",
            new ItemStack(Items.LIGHTNING_ROD), OBTAIN_LIGHTNING_ROD
      ));
      
      ResearchTasks.register(ADVANCEMENT_TAME_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_TAME_AN_ANIMAL.identifier().getPath(), "husbandry/tame_an_animal",
            new ItemStack(Items.BONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_TRADE, new AdvancementResearchTask(
            ADVANCEMENT_TRADE.identifier().getPath(), "adventure/trade",
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_USE_LODESTONE, new AdvancementResearchTask(
            ADVANCEMENT_USE_LODESTONE.identifier().getPath(), "adventure/use_lodestone",
            new ItemStack(Items.LODESTONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_COMPLETE_CATALOGUE, new AdvancementResearchTask(
            ADVANCEMENT_COMPLETE_CATALOGUE.identifier().getPath(), "husbandry/complete_catalogue",
            new ItemStack(Items.COD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_ON, new AdvancementResearchTask(
            ADVANCEMENT_WAX_ON.identifier().getPath(), "husbandry/wax_on",
            new ItemStack(Items.HONEYCOMB)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_OFF, new AdvancementResearchTask(
            ADVANCEMENT_WAX_OFF.identifier().getPath(), "husbandry/wax_off",
            new ItemStack(Items.IRON_AXE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OL_BETSY, new AdvancementResearchTask(
            ADVANCEMENT_OL_BETSY.identifier().getPath(), "adventure/ol_betsy",
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_ARBALISTIC, new AdvancementResearchTask(
            ADVANCEMENT_ARBALISTIC.identifier().getPath(), "adventure/arbalistic",
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_TWO_BIRDS_ONE_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_TWO_BIRDS_ONE_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_TWO_BIRDS_ONE_ARROW.identifier().getPath(), "adventure/two_birds_one_arrow",
            new ItemStack(Items.PHANTOM_MEMBRANE), ADVANCEMENT_OL_BETSY, ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(ADVANCEMENT_WHOS_THE_PILLAGER_NOW, new AdvancementResearchTask(
            ADVANCEMENT_WHOS_THE_PILLAGER_NOW.identifier().getPath(), "adventure/whos_the_pillager_now",
            new ItemStack(Items.CROSSBOW),ADVANCEMENT_OL_BETSY
      ));
      
      ResearchTasks.register(ADVANCEMENT_TOTEM_OF_UNDYING, new AdvancementResearchTask(
            ADVANCEMENT_TOTEM_OF_UNDYING.identifier().getPath(), "adventure/totem_of_undying",
            new ItemStack(Items.TOTEM_OF_UNDYING)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LAVA_BUCKET, new AdvancementResearchTask(
            ADVANCEMENT_LAVA_BUCKET.identifier().getPath(), "story/lava_bucket",
            new ItemStack(Items.LAVA_BUCKET)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER.identifier().getPath(), "husbandry/allay_deliver_item_to_player",
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK.identifier().getPath(), "husbandry/allay_deliver_cake_to_note_block",
            new ItemStack(Items.CAKE), ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER
      ));
      
      ResearchTasks.register(ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF, new AdvancementResearchTask(
            ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF.identifier().getPath(), "adventure/read_power_of_chiseled_bookshelf",
            new ItemStack(Items.CHISELED_BOOKSHELF)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREED_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_BREED_AN_ANIMAL.identifier().getPath(), "husbandry/breed_an_animal",
            new ItemStack(Items.WHEAT)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_SEED.identifier().getPath(), "husbandry/plant_seed",
            new ItemStack(Items.WHEAT_SEEDS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_ANY_SNIFFER_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_ANY_SNIFFER_SEED.identifier().getPath(), "husbandry/plant_any_sniffer_seed",
            new ItemStack(Items.TORCHFLOWER_SEEDS), ADVANCEMENT_PLANT_SEED
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_NETHERITE_HOE, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_NETHERITE_HOE.identifier().getPath(), "husbandry/obtain_netherite_hoe",
            new ItemStack(Items.NETHERITE_HOE), ADVANCEMENT_PLANT_SEED, OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(ADVANCEMENT_BRED_ALL_ANIMALS, new AdvancementResearchTask(
            ADVANCEMENT_BRED_ALL_ANIMALS.identifier().getPath(), "husbandry/bred_all_animals",
            new ItemStack(Items.GOLDEN_CARROT), ADVANCEMENT_BREED_AN_ANIMAL, ADVANCEMENT_TAME_AN_ANIMAL
      ));
      
      ResearchTasks.register(ADVANCEMENT_FALL_FROM_WORLD_HEIGHT, new AdvancementResearchTask(
            ADVANCEMENT_FALL_FROM_WORLD_HEIGHT.identifier().getPath(), "adventure/fall_from_world_height",
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(ADVANCEMENT_EYE_SPY, new AdvancementResearchTask(
            ADVANCEMENT_EYE_SPY.identifier().getPath(), "story/follow_ender_eye",
            new ItemStack(Items.MOSSY_STONE_BRICKS), USE_ENDER_EYE
      ));
      
      ResearchTasks.register(ADVANCEMENT_ENTER_END, new AdvancementResearchTask(
            ADVANCEMENT_ENTER_END.identifier().getPath(), "end/root",
            new ItemStack(Items.END_STONE), ADVANCEMENT_EYE_SPY
      ));
      
      ResearchTasks.register(ADVANCEMENT_FURIOUS_COCKTAIL, new AdvancementResearchTask(
            ADVANCEMENT_FURIOUS_COCKTAIL.identifier().getPath(), "nether/all_potions",
            new ItemStack(Items.POTION), ADVANCEMENT_BREW_POTION
      ));
      
      ResearchTasks.register(ADVANCEMENT_OVER_OVERKILL, new AdvancementResearchTask(
            ADVANCEMENT_OVER_OVERKILL.identifier().getPath(), "adventure/overoverkill",
            new ItemStack(Items.MACE), OBTAIN_MACE
      ));
      
      ResearchTasks.register(ADVANCEMENT_BARTER_PIGLIN, new AdvancementResearchTask(
            ADVANCEMENT_BARTER_PIGLIN.identifier().getPath(), "nether/distract_piglin",
            new ItemStack(Items.GOLD_INGOT), ADVANCEMENT_FIND_BASTION, OBTAIN_GOLD_INGOT
      ));
      
      
      ResearchTasks.register(DIMENSION_TRAVEL, new CustomResearchTask(
            DIMENSION_TRAVEL.identifier().getPath(),
            new ItemStack(Items.END_PORTAL_FRAME)
      ));
      
      ResearchTasks.register(CAT_SCARE, new CustomResearchTask(
            CAT_SCARE.identifier().getPath(),
            new ItemStack(Items.STRING), TAME_CAT
      ));
      
      ResearchTasks.register(RESONATE_BELL, new CustomResearchTask(
            RESONATE_BELL.identifier().getPath(),
            new ItemStack(Items.BELL)
      ));
      
      ResearchTasks.register(RIPTIDE_TRIDENT, new CustomResearchTask(
            RIPTIDE_TRIDENT.identifier().getPath(),
            new ItemStack(Items.TRIDENT)
      ));
      
      ResearchTasks.register(FISH_ITEM, new CustomResearchTask(
            FISH_ITEM.identifier().getPath(),
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(FISH_MOB, new CustomResearchTask(
            FISH_MOB.identifier().getPath(),
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(LEVEL_100, new CustomResearchTask(
            LEVEL_100.identifier().getPath(),
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(HUNGER_DAMAGE, new CustomResearchTask(
            HUNGER_DAMAGE.identifier().getPath(),
            new ItemStack(Items.ROTTEN_FLESH)
      ));
      
      ResearchTasks.register(DROWNING_DAMAGE, new CustomResearchTask(
            DROWNING_DAMAGE.identifier().getPath(),
            new ItemStack(Items.TURTLE_HELMET)
      ));
      
      ResearchTasks.register(CONCENTRATION_DAMAGE, new CustomResearchTask(
            CONCENTRATION_DAMAGE.identifier().getPath(),
            MinecraftUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE))
      ));
      
      ResearchTasks.register(FEATHER_FALL, new CustomResearchTask(
            FEATHER_FALL.identifier().getPath(),
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(FIND_SPAWNER, new CustomResearchTask(
            FIND_SPAWNER.identifier().getPath(),
            new ItemStack(Items.SPAWNER)
      ));
      
      ResearchTasks.register(USE_SOUL_SPEED, new CustomResearchTask(
            USE_SOUL_SPEED.identifier().getPath(),
            new ItemStack(Items.GOLDEN_BOOTS)
      ));
      
      ResearchTasks.register(ACTIVATE_MENDING, new CustomResearchTask(
            ACTIVATE_MENDING.identifier().getPath(),
            new ItemStack(Items.EXPERIENCE_BOTTLE), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_SILK_TOUCH, new CustomResearchTask(
            OBTAIN_SILK_TOUCH.identifier().getPath(),
            new ItemStack(Items.GOLDEN_PICKAXE)
      ));
      
      ResearchTasks.register(INFUSE_ITEM, new CustomResearchTask(
            INFUSE_ITEM.identifier().getPath(),
            new ItemStack(Items.GOLDEN_SWORD), OBTAIN_STARDUST, UNLOCK_STELLAR_CORE
      ));
      
      ResearchTasks.register(HONEY_CLEANSE, new CustomResearchTask(
            HONEY_CLEANSE.identifier().getPath(),
            new ItemStack(Items.HONEY_BOTTLE)
      ));
      
      ResearchTasks.register(MILK_CLEANSE, new CustomResearchTask(
            MILK_CLEANSE.identifier().getPath(),
            new ItemStack(Items.MILK_BUCKET)
      ));
      
      ResearchTasks.register(VISIT_DOZEN_BIOMES, new CustomResearchTask(
            VISIT_DOZEN_BIOMES.identifier().getPath(),
            new ItemStack(Items.COMPASS)
      ));
      
      ResearchTasks.register(TAME_CAT, new CustomResearchTask(
            TAME_CAT.identifier().getPath(),
            new ItemStack(Items.SALMON)
      ));
      
      ResearchTasks.register(EFFECT_ABSORPTION, new EffectResearchTask(
            EFFECT_ABSORPTION.identifier().getPath(), MobEffects.ABSORPTION,
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(EFFECT_SWIFTNESS, new EffectResearchTask(
            EFFECT_SWIFTNESS.identifier().getPath(), MobEffects.SPEED,
            new ItemStack(Items.SUGAR)
      ));
      
      ResearchTasks.register(EFFECT_JUMP_BOOST, new EffectResearchTask(
            EFFECT_JUMP_BOOST.identifier().getPath(), MobEffects.JUMP_BOOST,
            new ItemStack(Items.RABBIT_FOOT)
      ));
      
      ResearchTasks.register(EFFECT_NIGHT_VISION, new EffectResearchTask(
            EFFECT_NIGHT_VISION.identifier().getPath(), MobEffects.NIGHT_VISION,
            new ItemStack(Items.GOLDEN_CARROT)
      ));
      
      ResearchTasks.register(EFFECT_SLOW_FALLING, new EffectResearchTask(
            EFFECT_SLOW_FALLING.identifier().getPath(), MobEffects.SLOW_FALLING,
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(EFFECT_BLINDNESS, new EffectResearchTask(
            EFFECT_BLINDNESS.identifier().getPath(), MobEffects.BLINDNESS,
            new ItemStack(Items.INK_SAC)
      ));
      
      ResearchTasks.register(EFFECT_SLOWNESS, new EffectResearchTask(
            EFFECT_SLOWNESS.identifier().getPath(), MobEffects.SLOWNESS,
            new ItemStack(Items.COBWEB)
      ));
      
      ResearchTasks.register(EFFECT_FIRE_RESISTANCE, new EffectResearchTask(
            EFFECT_FIRE_RESISTANCE.identifier().getPath(), MobEffects.FIRE_RESISTANCE,
            new ItemStack(Items.MAGMA_CREAM)
      ));
      
      ResearchTasks.register(EFFECT_STRENGTH, new EffectResearchTask(
            EFFECT_STRENGTH.identifier().getPath(), MobEffects.STRENGTH,
            new ItemStack(Items.IRON_SWORD)
      ));
      
      ResearchTasks.register(EFFECT_WEAKNESS, new EffectResearchTask(
            EFFECT_WEAKNESS.identifier().getPath(), MobEffects.WEAKNESS,
            new ItemStack(Items.WOODEN_SWORD)
      ));
      
      ResearchTasks.register(EFFECT_NAUSEA, new EffectResearchTask(
            EFFECT_NAUSEA.identifier().getPath(), MobEffects.NAUSEA,
            new ItemStack(Items.PUFFERFISH)
      ));
      
      ResearchTasks.register(EFFECT_POISON, new EffectResearchTask(
            EFFECT_POISON.identifier().getPath(), MobEffects.POISON,
            new ItemStack(Items.SPIDER_EYE)
      ));
      
      ResearchTasks.register(EFFECT_DOLPHINS_GRACE, new EffectResearchTask(
            EFFECT_DOLPHINS_GRACE.identifier().getPath(), MobEffects.DOLPHINS_GRACE,
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
