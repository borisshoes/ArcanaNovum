package net.borisshoes.arcananovum.research;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TreeNode;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class ResearchTasks {
   public static final Registry<ResearchTask> RESEARCH_TASKS = new SimpleRegistry<>(RegistryKey.ofRegistry(Identifier.of(MOD_ID,"research_task")), Lifecycle.stable());
   
   public static final RegistryKey<ResearchTask> OBTAIN_SPONGE = of("obtain_sponge");
   public static final RegistryKey<ResearchTask> OBTAIN_END_CRYSTAL = of("obtain_end_crystal");
   public static final RegistryKey<ResearchTask> OBTAIN_SPECTRAL_ARROW = of("obtain_spectral_arrow");
   public static final RegistryKey<ResearchTask> OBTAIN_AMETHYST_SHARD = of("obtain_amethyst_shard");
   public static final RegistryKey<ResearchTask> OBTAIN_NETHERITE_SWORD = of("obtain_netherite_sword");
   public static final RegistryKey<ResearchTask> OBTAIN_NETHER_STAR = of("obtain_nether_star");
   public static final RegistryKey<ResearchTask> OBTAIN_EYE_OF_ENDER = of("obtain_eye_of_ender");
   public static final RegistryKey<ResearchTask> OBTAIN_BOTTLES_OF_ENCHANTING = of("obtain_bottles_of_enchanting");
   public static final RegistryKey<ResearchTask> OBTAIN_ZOMBIE_HEAD = of("obtain_zombie_head");
   public static final RegistryKey<ResearchTask> OBTAIN_EGG = of("obtain_egg");
   public static final RegistryKey<ResearchTask> OBTAIN_BEACON = of("obtain_beacon");
   public static final RegistryKey<ResearchTask> OBTAIN_DIVINE_CATALYST = of("obtain_divine_catalyst");
   public static final RegistryKey<ResearchTask> OBTAIN_GOLD_INGOT = of("obtain_gold_ingot");
   public static final RegistryKey<ResearchTask> OBTAIN_EMERALD = of("obtain_emerald");
   public static final RegistryKey<ResearchTask> OBTAIN_DIAMOND = of("obtain_diamond");
   public static final RegistryKey<ResearchTask> OBTAIN_QUARTZ = of("obtain_quartz");
   public static final RegistryKey<ResearchTask> OBTAIN_CLOCK = of("obtain_clock");
   public static final RegistryKey<ResearchTask> OBTAIN_NETHERITE_INGOT = of("obtain_netherite_ingot");
   public static final RegistryKey<ResearchTask> OBTAIN_PISTON = of("obtain_piston");
   public static final RegistryKey<ResearchTask> OBTAIN_GLISTERING_MELON = of("obtain_glistering_melon");
   public static final RegistryKey<ResearchTask> OBTAIN_NETHERITE_PICKAXE = of("obtain_netherite_pickaxe");
   public static final RegistryKey<ResearchTask> OBTAIN_TNT = of("obtain_tnt");
   public static final RegistryKey<ResearchTask> OBTAIN_TIPPED_ARROW = of("obtain_tipped_arrow");
   public static final RegistryKey<ResearchTask> OBTAIN_LIGHTNING_ROD = of("obtain_lightning_rod");
   public static final RegistryKey<ResearchTask> OBTAIN_AMETHYST_CLUSTER = of("obtain_amethyst_cluster");
   public static final RegistryKey<ResearchTask> OBTAIN_ARCANE_TOME = of("obtain_arcane_tome");
   public static final RegistryKey<ResearchTask> OBTAIN_CREEPER_HEAD = of("obtain_creeper_head");
   public static final RegistryKey<ResearchTask> OBTAIN_HEAVY_CORE = of("obtain_heavy_core");
   public static final RegistryKey<ResearchTask> OBTAIN_HEART_OF_THE_SEA = of("obtain_heart_of_the_sea");
   public static final RegistryKey<ResearchTask> OBTAIN_STARDUST = of("obtain_stardust");
   public static final RegistryKey<ResearchTask> OBTAIN_NEBULOUS_ESSENCE = of("obtain_nebulous_essence");
   public static final RegistryKey<ResearchTask> OBTAIN_BLAST_FURNACE = of("obtain_blast_furnace");
   public static final RegistryKey<ResearchTask> OBTAIN_BLUE_ICE = of("obtain_blue_ice");
   public static final RegistryKey<ResearchTask> OBTAIN_ANVIL = of("obtain_anvil");
   public static final RegistryKey<ResearchTask> OBTAIN_ENCHANTED_GOLDEN_APPLE = of("obtain_enchanted_golden_apple");
   public static final RegistryKey<ResearchTask> OBTAIN_LEADERSHIP_CHARM = of("obtain_leadership_charm");
   public static final RegistryKey<ResearchTask> OBTAIN_WINGS_OF_ENDERIA = of("obtain_wings_of_enderia");
   public static final RegistryKey<ResearchTask> OBTAIN_PICKAXE_OF_CEPTYUS = of("obtain_pickaxe_of_ceptyus");
   public static final RegistryKey<ResearchTask> BREAK_SCULK = of("break_sculk");
   public static final RegistryKey<ResearchTask> BREAK_SPAWNER = of("break_spawner");
   public static final RegistryKey<ResearchTask> BREAK_OBSIDIAN = of("break_obsidian");
   public static final RegistryKey<ResearchTask> PLACE_TORCHES = of("place_torches");
   public static final RegistryKey<ResearchTask> USE_FIREWORK = of("use_firework");
   public static final RegistryKey<ResearchTask> USE_CAMPFIRE = of("use_campfire");
   public static final RegistryKey<ResearchTask> USE_FLINT_AND_STEEL = of("use_flint_and_steel");
   public static final RegistryKey<ResearchTask> USE_ENDER_PEARL = of("use_ender_pearl");
   public static final RegistryKey<ResearchTask> USE_ENDER_EYE = of("use_ender_eye");
   public static final RegistryKey<ResearchTask> USE_ENDER_CHEST = of("use_ender_chest");
   public static final RegistryKey<ResearchTask> KILL_SLIME = of("kill_slime");
   public static final RegistryKey<ResearchTask> KILL_SQUID = of("kill_squid");
   public static final RegistryKey<ResearchTask> KILL_CONSTRUCT = of("kill_construct");
   public static final RegistryKey<ResearchTask> KILL_BLAZE = of("kill_blaze");
   public static final RegistryKey<ResearchTask> KILL_MAGMA_CUBE = of("kill_magma_cube");
   public static final RegistryKey<ResearchTask> KILL_EVOKER = of("kill_evoker");
   public static final RegistryKey<ResearchTask> UNLOCK_RUNIC_MATRIX = of("unlock_runic_matrix");
   public static final RegistryKey<ResearchTask> UNLOCK_STARLIGHT_FORGE = of("unlock_starlight_forge");
   public static final RegistryKey<ResearchTask> UNLOCK_TWILIGHT_ANVIL = of("unlock_twilight_anvil");
   public static final RegistryKey<ResearchTask> UNLOCK_TEMPORAL_MOMENT = of("unlock_temporal_moment");
   public static final RegistryKey<ResearchTask> UNLOCK_EXOTIC_MATTER = of("unlock_exotic_matter");
   public static final RegistryKey<ResearchTask> UNLOCK_SHULKER_CORE = of("unlock_shulker_core");
   public static final RegistryKey<ResearchTask> UNLOCK_MIDNIGHT_ENCHANTER = of("unlock_midnight_enchanter");
   public static final RegistryKey<ResearchTask> UNLOCK_STELLAR_CORE = of("unlock_stellar_core");
   public static final RegistryKey<ResearchTask> UNLOCK_ARCANE_SINGULARITY = of("unlock_arcane_singularity");
   public static final RegistryKey<ResearchTask> UNLOCK_RADIANT_FLETCHERY = of("unlock_radiant_fletchery");
   public static final RegistryKey<ResearchTask> UNLOCK_SOULSTONE = of("unlock_soulstone");
   public static final RegistryKey<ResearchTask> UNLOCK_CATALYTIC_MATRIX = of("unlock_catalytic_matrix");
   public static final RegistryKey<ResearchTask> UNLOCK_MUNDANE_CATALYST = of("unlock_mundane_catalyst");
   public static final RegistryKey<ResearchTask> UNLOCK_EMPOWERED_CATALYST = of("unlock_empowered_catalyst");
   public static final RegistryKey<ResearchTask> UNLOCK_EXOTIC_CATALYST = of("unlock_exotic_catalyst");
   public static final RegistryKey<ResearchTask> UNLOCK_SOVEREIGN_CATALYST = of("unlock_sovereign_catalyst");
   public static final RegistryKey<ResearchTask> UNLOCK_OVERFLOWING_QUIVER = of("unlock_overflowing_quiver");
   public static final RegistryKey<ResearchTask> UNLOCK_SPAWNER_HARNESS = of("unlock_spawner_harness");
   public static final RegistryKey<ResearchTask> UNLOCK_TRANSMUTATION_ALTAR = of("unlock_transmutation_altar");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ENCHANT_ITEM = of("advancement_enchant_item");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_CHARGE_RESPAWN_ANCHOR = of("advancement_charge_respawn_anchor");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS = of("advancement_obtain_ancient_debris");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ELYTRA = of("advancement_elytra");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_FIND_BASTION = of("advancement_find_bastion");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS = of("advancement_crafters_crafting_crafters");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_KILL_A_MOB = of("advancement_kill_a_mob");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_LEVITATE = of("advancement_levitate");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_BALANCED_DIET = of("advancement_balanced_diet");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_DEFLECT_ARROW = of("advancement_deflect_arrow");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ADVENTURING_TIME = of("advancement_adventuring_time");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS = of("advancement_walk_on_powder_snow_with_leather_boots");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_CREATE_FULL_BEACON = of("advancement_create_full_beacon");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_SLEEP_IN_BED = of("advancement_sleep_in_bed");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_SHOOT_ARROW = of("advancement_shoot_arrow");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_SNIPER_DUEL = of("advancement_sniper_duel");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_BULLSEYE = of("advancement_bullseye");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_BREW_POTION = of("advancement_brew_potion");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN = of("advancement_obtain_crying_obsidian");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_DRAGON_BREATH = of("advancement_dragon_breath");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST = of("advancement_kill_mob_near_sculk_catalyst");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE = of("advancement_lightning_rod_with_villager_no_fire");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_TAME_AN_ANIMAL = of("advancement_tame_an_animal");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_TRADE = of("advancement_trade");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_USE_LODESTONE = of("advancement_use_lodestone");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_COMPLETE_CATALOGUE = of("advancement_complete_catalogue");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_WAX_ON = of("advancement_wax_on");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_WAX_OFF = of("advancement_wax_off");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_OL_BETSY = of("advancement_ol_betsy");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ARBALISTIC = of("advancement_arbalistic");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_TWO_BIRDS_ONE_ARROW = of("advancement_two_birds_one_arrow");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_WHOS_THE_PILLAGER_NOW = of("advancement_whos_the_pillager_now");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_TOTEM_OF_UNDYING = of("advancement_totem_of_undying");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_LAVA_BUCKET = of("advancement_lava_bucket");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER = of("advancement_allay_deliver_item_to_player");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK = of("advancement_allay_deliver_cake_to_note_block");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF = of("advancement_read_power_of_chiseled_bookshelf");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_BREED_AN_ANIMAL = of("advancement_breed_an_animal");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_PLANT_SEED = of("advancement_plant_seed");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_PLANT_ANY_SNIFFER_SEED = of("advancement_plant_any_sniffer_seed");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_OBTAIN_NETHERITE_HOE = of("advancement_obtain_netherite_hoe");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_BRED_ALL_ANIMALS = of("advancement_bred_all_animals");
   public static final RegistryKey<ResearchTask> ADVANCEMENT_FALL_FROM_WORLD_HEIGHT = of("advancement_fall_from_world_height");
   public static final RegistryKey<ResearchTask> DIMENSION_TRAVEL = of("dimension_travel");
   public static final RegistryKey<ResearchTask> CAT_SCARE = of("cat_scare");
   public static final RegistryKey<ResearchTask> RESONATE_BELL = of("resonate_bell");
   public static final RegistryKey<ResearchTask> RIPTIDE_TRIDENT = of("riptide_trident");
   public static final RegistryKey<ResearchTask> FISH_ITEM = of("fish_item");
   public static final RegistryKey<ResearchTask> FISH_MOB = of("fish_mob");
   public static final RegistryKey<ResearchTask> LEVEL_100 = of("level_100");
   public static final RegistryKey<ResearchTask> HUNGER_DAMAGE = of("hunger_damage");
   public static final RegistryKey<ResearchTask> CONCENTRATION_DAMAGE = of("concentration_damage");
   public static final RegistryKey<ResearchTask> FEATHER_FALL = of("feather_fall");
   public static final RegistryKey<ResearchTask> FIND_SPAWNER = of("find_spawner");
   public static final RegistryKey<ResearchTask> USE_SOUL_SPEED = of("use_soul_speed");
   public static final RegistryKey<ResearchTask> ACTIVATE_MENDING = of("activate_mending");
   public static final RegistryKey<ResearchTask> OBTAIN_SILK_TOUCH = of("obtain_silk_touch");
   public static final RegistryKey<ResearchTask> EFFECT_ABSORPTION = of("effect_absorption");
   public static final RegistryKey<ResearchTask> EFFECT_SWIFTNESS = of("effect_swiftness");
   public static final RegistryKey<ResearchTask> EFFECT_JUMP_BOOST = of("effect_jump_boost");
   public static final RegistryKey<ResearchTask> EFFECT_NIGHT_VISION = of("effect_night_vision");
   public static final RegistryKey<ResearchTask> EFFECT_SLOW_FALLING = of("effect_slow_falling");
   public static final RegistryKey<ResearchTask> EFFECT_BLINDNESS = of("effect_blindness");
   public static final RegistryKey<ResearchTask> EFFECT_SLOWNESS = of("effect_slowness");
   public static final RegistryKey<ResearchTask> EFFECT_FIRE_RESISTANCE = of("effect_fire_resistance");
   public static final RegistryKey<ResearchTask> EFFECT_STRENGTH = of("effect_strength");
   public static final RegistryKey<ResearchTask> EFFECT_WEAKNESS = of("effect_weakness");
   
   @SuppressWarnings("unchecked")
   public static void registerResearchTasks(){
      ResearchTasks.register(OBTAIN_SPONGE, new ObtainResearchTask(
            OBTAIN_SPONGE.getValue().getPath(), Items.SPONGE,
            Text.literal("Obtain a Sponge"),
            new Text[]{
                  Text.literal("Water's self-replicating nature makes it very"),
                  Text.literal(" difficult to clean up in large quantities."),
                  Text.literal("Maybe something can make this task easier?")
            },
            new ItemStack(Items.SPONGE)
      ));
      
      ResearchTasks.register(OBTAIN_END_CRYSTAL, new ObtainResearchTask(
            OBTAIN_END_CRYSTAL.getValue().getPath(), Items.END_CRYSTAL,
            Text.literal("Obtain an End Crystal"),
            new Text[]{
                  Text.literal("This world contains some artifacts displaying"),
                  Text.literal(" some impressive properties and craftsmanship."),
                  Text.literal("Perhaps I can find one that can channel Arcana...")
            },
            new ItemStack(Items.END_CRYSTAL)
      ));
      
      ResearchTasks.register(OBTAIN_SPECTRAL_ARROW, new ObtainResearchTask(
            OBTAIN_SPECTRAL_ARROW.getValue().getPath(), Items.SPECTRAL_ARROW,
            Text.literal("Obtain a Spectral Arrow"),
            new Text[]{
                  Text.literal("Normal arrows are too fragile and shoddy to"),
                  Text.literal(" withstand arcane enhancement without any sort"),
                  Text.literal(" of reinforcement or arcane priming."),
                  Text.literal("I will find a substance to treat the arrows with.")
            },
            new ItemStack(Items.SPECTRAL_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_SHARD, new ObtainResearchTask(
            OBTAIN_AMETHYST_SHARD.getValue().getPath(), Items.AMETHYST_SHARD,
            Text.literal("Obtain an Amethyst Shard"),
            new Text[]{
                  Text.literal("Crystals are far more than just pretty rocks."),
                  Text.literal("Their ordered structure is perfect for withstanding"),
                  Text.literal(" immense energy and even changing and channeling it."),
                  Text.literal("Unfortunately, they take millennia to form naturally, "),
                  Text.literal(" and synthesizing them is beyond my capabilities."),
                  Text.literal("I must find a good source of suitable crystals.")
            },
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_SWORD, new ObtainResearchTask(
            OBTAIN_NETHERITE_SWORD.getValue().getPath(), Items.NETHERITE_SWORD,
            Text.literal("Obtain a Netherite Sword"),
            new Text[]{
                  Text.literal("Netherite Alloy has been quite the game-changer."),
                  Text.literal("Its hardness even outclasses diamond!"),
                  Text.literal("Theoretically, it should hold a sharper edge."),
                  Text.literal("This is something worth pursuing...")
            },
            new ItemStack(Items.NETHERITE_SWORD), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHER_STAR, new ObtainResearchTask(
            OBTAIN_NETHER_STAR.getValue().getPath(), Items.NETHER_STAR,
            Text.literal("Obtain a Nether Star"),
            new Text[]{
                  Text.literal("I have found that souls harbor a significant"),
                  Text.literal(" amount of power; the energy of life itself."),
                  Text.literal("Soul Sand seems to have a small amount of this"),
                  Text.literal(" energy stored within it, and I have found some"),
                  Text.literal(" evidence to suggest ancient beings feared an"),
                  Text.literal(" entity comprised of this soul-harboring material."),
                  Text.literal("I wish to find this entity, and study its potential.")
            },
            new ItemStack(Items.NETHER_STAR), USE_SOUL_SPEED
      ));
      
      ResearchTasks.register(OBTAIN_EYE_OF_ENDER, new ObtainResearchTask(
            OBTAIN_EYE_OF_ENDER.getValue().getPath(), Items.ENDER_EYE,
            Text.literal("Obtain an Eye of Ender"),
            new Text[]{
                  Text.literal("Endermen are the only interdimensional travellers"),
                  Text.literal(" that I have found, despite evidence of others."),
                  Text.literal("They also manage to do dimension hop without a portal."),
                  Text.literal("I believe their eyes have something to do with it,"),
                  Text.literal(" but upon their death, their eyes glaze over and"),
                  Text.literal(" can only be used to teleport a short distance."),
                  Text.literal("Maybe I can reawaken their natural abilities?")
            },
            new ItemStack(Items.ENDER_EYE), USE_ENDER_PEARL
      ));
      
      ResearchTasks.register(OBTAIN_BOTTLES_OF_ENCHANTING, new ObtainResearchTask(
            OBTAIN_BOTTLES_OF_ENCHANTING.getValue().getPath(), Items.EXPERIENCE_BOTTLE,
            Text.literal("Obtain Bottles of Experience"),
            new Text[]{
                  Text.literal("Experience is a strange substance, it is partly"),
                  Text.literal(" ethereal but still has some physicality to it."),
                  Text.literal("Finding a way to contain it would be valuable.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(OBTAIN_ZOMBIE_HEAD, new ObtainResearchTask(
            OBTAIN_ZOMBIE_HEAD.getValue().getPath(), Items.ZOMBIE_HEAD,
            Text.literal("Obtain a Zombie Head"),
            new Text[]{
                  Text.literal("Zombies are very versatile creatures, and"),
                  Text.literal(" their possible origins still elude me."),
                  Text.literal("I wonder how similar their brain is to other"),
                  Text.literal(" creatures I have encountered in the world.")
            },
            new ItemStack(Items.ZOMBIE_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_EGG, new ObtainResearchTask(
            OBTAIN_EGG.getValue().getPath(), Items.EGG,
            Text.literal("Obtain an Egg"),
            new Text[]{
                  Text.literal("Some species do not perform live birth, they gestate"),
                  Text.literal(" in external eggs until they are ready to be born."),
                  Text.literal("Maybe there is something I can gleam from them.")
            },
            new ItemStack(Items.EGG)
      ));
      
      ResearchTasks.register(OBTAIN_BEACON, new ObtainResearchTask(
            OBTAIN_BEACON.getValue().getPath(), Items.BEACON,
            Text.literal("Unlock the Power of the Nether Star"),
            new Text[]{
                  Text.literal("Nether Stars are truly fascinating! They appear to"),
                  Text.literal(" be a micro or even nano crystalline structure that"),
                  Text.literal(" is suited specifically for channelling soul energy!"),
                  Text.literal("I can think of 10,000 things I can use this for, but"),
                  Text.literal(" for now, I should just make something to contain it.")
            },
            new ItemStack(Items.BEACON), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(OBTAIN_DIVINE_CATALYST, new ObtainResearchTask(
            OBTAIN_DIVINE_CATALYST.getValue().getPath(), ArcanaRegistry.DIVINE_CATALYST.getItem(),
            Text.literal("Obtain a Divine Catalyst"),
            new Text[]{
                  Text.literal("")
            },
            ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST
      ));
      
      ResearchTasks.register(OBTAIN_GOLD_INGOT, new ObtainResearchTask(
            OBTAIN_GOLD_INGOT.getValue().getPath(), Items.GOLD_INGOT,
            Text.literal("Obtain a Gold Ingot"),
            new Text[]{
                  Text.literal("Most materials I have found exhibit low receptivity"),
                  Text.literal(" to enchantment and other forms of arcane infusion."),
                  Text.literal("I need something else... Something malleable, maybe?")
            },
            new ItemStack(Items.GOLD_INGOT)
      ));
      
      ResearchTasks.register(OBTAIN_EMERALD, new ObtainResearchTask(
            OBTAIN_EMERALD.getValue().getPath(), Items.EMERALD,
            Text.literal("Obtain an Emerald"),
            new Text[]{
                  Text.literal("Villagers are strange folk. They practically worship,"),
                  Text.literal(" these green gemstones that they barter and work for."),
                  Text.literal("I should try and get my hands on some...")
            },
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(OBTAIN_DIAMOND, new ObtainResearchTask(
            OBTAIN_DIAMOND.getValue().getPath(), Items.DIAMOND,
            Text.literal("Obtain a Diamond"),
            new Text[]{
                  Text.literal("Diamonds are rumored to be the world's most durable"),
                  Text.literal(" material, found deep within the caves."),
                  Text.literal("Somehow, I have never laid my hands on one..."),
                  Text.literal(" It's time to change that!")
            },
            new ItemStack(Items.DIAMOND)
      ));
      
      ResearchTasks.register(OBTAIN_QUARTZ, new ObtainResearchTask(
            OBTAIN_QUARTZ.getValue().getPath(), Items.QUARTZ,
            Text.literal("Obtain Quartz"),
            new Text[]{
                  Text.literal("Crystals are far more than just pretty rocks."),
                  Text.literal("Their ordered structure is perfect for withstanding"),
                  Text.literal(" immense energy and even changing and channeling it."),
                  Text.literal("Unfortunately, they take millennia to form naturally, "),
                  Text.literal(" and synthesizing them is beyond my capabilities."),
                  Text.literal("I wonder if the Nether produces crystal formations?")
            },
            new ItemStack(Items.QUARTZ)
      ));
      
      ResearchTasks.register(OBTAIN_CLOCK, new ObtainResearchTask(
            OBTAIN_CLOCK.getValue().getPath(), Items.CLOCK,
            Text.literal("Obtain a Clock"),
            new Text[]{
                  Text.literal("Time. It is such a strange... concept? phenomenon?"),
                  Text.literal("Whatever it is, I lose track of it frequently."),
                  Text.literal("I need something to automatically keep it for me.")
            },
            new ItemStack(Items.CLOCK)
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_INGOT, new ObtainResearchTask(
            OBTAIN_NETHERITE_INGOT.getValue().getPath(), Items.NETHERITE_INGOT,
            Text.literal("Obtain a Netherite Ingot"),
            new Text[]{
                  Text.literal("This ancient debris is certainly something!"),
                  Text.literal("Fire-proof, blast-proof, harder than diamond!"),
                  Text.literal("Too hard, in fact. I can't even shape it into"),
                  Text.literal(" any tools or armors in it's pure state."),
                  Text.literal("Maybe I can alloy it with gold to make it more"),
                  Text.literal(" workable, and possibly more receptive to Arcana.")
            },
            new ItemStack(Items.NETHERITE_INGOT), ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_PISTON, new ObtainResearchTask(
            OBTAIN_PISTON.getValue().getPath(), Items.PISTON,
            Text.literal("Obtain a Piston"),
            new Text[]{
                  Text.literal("I need something that is able to automatically"),
                  Text.literal(" generate enough force to move blocks around."),
                  Text.literal("Maybe combining iron and redstone could work?")
            },
            new ItemStack(Items.PISTON)
      ));
      
      ResearchTasks.register(OBTAIN_GLISTERING_MELON, new ObtainResearchTask(
            OBTAIN_GLISTERING_MELON.getValue().getPath(), Items.GLISTERING_MELON_SLICE,
            Text.literal("Obtain a Glistering Melon"),
            new Text[]{
                  Text.literal("I don't know what these ancient alchemy books are"),
                  Text.literal(" on about, but they keep touting gold-encrusted"),
                  Text.literal(" melons as the main ingredient in the elixir of life."),
                  Text.literal("I swear on the stars' light... this better be real.")
            },
            new ItemStack(Items.GLISTERING_MELON_SLICE), ADVANCEMENT_BREW_POTION, OBTAIN_GOLD_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_NETHERITE_PICKAXE, new ObtainResearchTask(
            OBTAIN_NETHERITE_PICKAXE.getValue().getPath(), Items.NETHERITE_PICKAXE,
            Text.literal("Obtain a Netherite Pickaxe"),
            new Text[]{
                  Text.literal("Netherite Alloy has been quite the game-changer."),
                  Text.literal("Its hardness even outclasses diamond!"),
                  Text.literal("Theoretically, it should make a phenomenal"),
                  Text.literal(" pickaxe, with increased speed and durability."),
                  Text.literal("If I can get enough debris, this is worth a try.")
            },
            new ItemStack(Items.NETHERITE_PICKAXE), OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(OBTAIN_TNT, new ObtainResearchTask(
            OBTAIN_TNT.getValue().getPath(), Items.TNT,
            Text.literal("Obtain a TNT"),
            new Text[]{
                  Text.literal("Creepers are fascinating creatures, with a unique"),
                  Text.literal(" capability for self-destructive obliteration."),
                  Text.literal("I would very much like to replicate this power.")
            },
            new ItemStack(Items.TNT)
      ));
      
      ResearchTasks.register(OBTAIN_TIPPED_ARROW, new ObtainResearchTask(
            OBTAIN_TIPPED_ARROW.getValue().getPath(), Items.TIPPED_ARROW,
            Text.literal("Obtain a Tipped Arrow"),
            new Text[]{
                  Text.literal("Arrows might be too shoddy to be enhanced by"),
                  Text.literal(" Arcana, however, I might be able to coat them"),
                  Text.literal(" with some potions to deliver an extra kick.")
            },
            new ItemStack(Items.TIPPED_ARROW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(OBTAIN_LIGHTNING_ROD, new ObtainResearchTask(
            OBTAIN_LIGHTNING_ROD.getValue().getPath(), Items.LIGHTNING_ROD,
            Text.literal("Obtain a Lightning Rod"),
            new Text[]{
                  Text.literal("Lightning is an amazing natural occurrence!"),
                  Text.literal("Unfortunately, it seems to strike randomly during"),
                  Text.literal(" intense thunderstorms, which don't happen often."),
                  Text.literal("I might not be able to make lightning occur, but"),
                  Text.literal(" perhaps I can at the very least get it where I want."),
                  Text.literal("Lightning leaves a unique taste in the air, it smells"),
                  Text.literal(" like old copper. That is a good place to start.")
            },
            new ItemStack(Items.LIGHTNING_ROD)
      ));
      
      ResearchTasks.register(OBTAIN_AMETHYST_CLUSTER, new ObtainResearchTask(
            OBTAIN_AMETHYST_CLUSTER.getValue().getPath(), Items.AMETHYST_CLUSTER,
            Text.literal("Obtain an Amethyst Cluster"),
            new Text[]{
                  Text.literal("Amethyst shards have proven to be invaluable."),
                  Text.literal("However, they are very fragile and break apart"),
                  Text.literal(" when I try to collect them from their geode."),
                  Text.literal("If I can get my hands on bigger chunks of them,"),
                  Text.literal(" the cluster will be capable of harnessing and"),
                  Text.literal(" focusing much greater amounts of arcane energy."),
                  Text.literal("Maybe this Silk Touch enchantment can lend a hand?")
            },
            new ItemStack(Items.AMETHYST_CLUSTER), OBTAIN_AMETHYST_SHARD, OBTAIN_SILK_TOUCH
      ));
      
      ResearchTasks.register(OBTAIN_ARCANE_TOME, new ObtainResearchTask(
            "obtain_tome_of_arcana_novum", ArcanaRegistry.ARCANE_TOME.getItem(),
            Text.literal("Obtain a Tome of Arcana Novum"),
            new Text[]{
                  Text.literal("I'll keep it real. How am I keeping my arcane "),
                  Text.literal(" notes without a tome to store them in?"),
                  Text.literal("This must be rectified immediately!")
            },
            ArcanaRegistry.ARCANE_TOME.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_CREEPER_HEAD, new ObtainResearchTask(
            OBTAIN_CREEPER_HEAD.getValue().getPath(), Items.CREEPER_HEAD,
            Text.literal("Obtain a Creeper Head"),
            new Text[]{
                  Text.literal("Creepers are very unique creatures."),
                  Text.literal("I wonder if their brains are equally unique?")
            },
            new ItemStack(Items.CREEPER_HEAD)
      ));
      
      ResearchTasks.register(OBTAIN_HEAVY_CORE, new ObtainResearchTask(
            OBTAIN_HEAVY_CORE.getValue().getPath(), Items.HEAVY_CORE,
            Text.literal("Obtain a Heavy Core"),
            new Text[]{
                  Text.literal("I have heard whispers of buried chambers, designed"),
                  Text.literal(" by some unknown entity or collective to test others."),
                  Text.literal("Supposedly, these chambers contain rewards for those"),
                  Text.literal(" who are capable enough to defeat their trials."),
                  Text.literal("One such reward is rumored to be an incredibly dense"),
                  Text.literal(" material, or maybe even a device of some sort."),
                  Text.literal("I might have a few uses for such a thing.")
            },
            new ItemStack(Items.HEAVY_CORE)
      ));
      
      ResearchTasks.register(OBTAIN_HEART_OF_THE_SEA, new ObtainResearchTask(
            OBTAIN_HEART_OF_THE_SEA.getValue().getPath(), Items.HEART_OF_THE_SEA,
            Text.literal("Obtain a Heart of the Sea"),
            new Text[]{
                  Text.literal("I do not believe these so called 'guardians' of"),
                  Text.literal(" the ocean monuments are their original builders."),
                  Text.literal("Their name suggests they are simply protectors."),
                  Text.literal("Whoever made the monuments must have a mastery over"),
                  Text.literal(" water, through natural or artificial means."),
                  Text.literal("Maybe I can find an artifact that holds the key?")
            },
            new ItemStack(Items.HEART_OF_THE_SEA)
      ));
      
      ResearchTasks.register(OBTAIN_STARDUST, new ObtainResearchTask(
            OBTAIN_STARDUST.getValue().getPath(), ArcanaRegistry.STARDUST,
            Text.literal("Obtain Stardust"),
            new Text[]{
                  Text.literal("Enchantments bind to equipment in a unique way."),
                  Text.literal("Beyond the enchantment Arcana, they alter the"),
                  Text.literal(" material they bind to, to a small extent."),
                  Text.literal("There may be a way to isolate and concentrate"),
                  Text.literal(" the enhanced material so that this effect can"),
                  Text.literal(" be replicated independently of enchantment."),
                  Text.literal("Theoretically, this would look like a powder"),
                  Text.literal(" that exhibits some latent arcane properties.")
            },
            MiscUtils.removeLore(new ItemStack(ArcanaRegistry.STARDUST)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_NEBULOUS_ESSENCE, new ObtainResearchTask(
            OBTAIN_NEBULOUS_ESSENCE.getValue().getPath(), ArcanaRegistry.NEBULOUS_ESSENCE,
            Text.literal("Obtain Nebulous Essence"),
            new Text[]{
                  Text.literal("Enchantment Arcana is a very strange thing."),
                  Text.literal("The Enchantment Table sends some type of Arcana"),
                  Text.literal(" into nearby books and pulls it back after the"),
                  Text.literal(" knowledge infuses the arcane energy."),
                  Text.literal("This altered arcane energy can produce any one"),
                  Text.literal(" of a vast many possible enchantment effects."),
                  Text.literal("The enchantment essence is nebulous until used."),
                  Text.literal("Maybe I can revert the enchantment Arcana back "),
                  Text.literal(" into this nebulous state, and isolate it.")
            },
            MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE)), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_BLAST_FURNACE, new ObtainResearchTask(
            OBTAIN_BLAST_FURNACE.getValue().getPath(), Items.BLAST_FURNACE,
            Text.literal("Obtain a Blast Furnace"),
            new Text[]{
                  Text.literal("Smelting ores must be done at high temperature."),
                  Text.literal("Furnaces take too long to reach such a high heat."),
                  Text.literal("If I reinforce one, maybe it will smelt faster.")
            },
            new ItemStack(Items.BLAST_FURNACE)
      ));
      
      ResearchTasks.register(OBTAIN_BLUE_ICE, new ObtainResearchTask(
            OBTAIN_BLUE_ICE.getValue().getPath(), Items.BLUE_ICE,
            Text.literal("Obtain a Blue Ice"),
            new Text[]{
                  Text.literal("The denser the ice, the more slippery it is."),
                  Text.literal("The denser the ice, the more water it contains."),
                  Text.literal("The denser the ice, the colder it remains."),
                  Text.literal("What is the densest ice that I can obtain?")
            },
            new ItemStack(Items.BLUE_ICE)
      ));
      
      ResearchTasks.register(OBTAIN_ANVIL, new ObtainResearchTask(
            OBTAIN_ANVIL.getValue().getPath(), Items.ANVIL,
            Text.literal("Obtain an Anvil"),
            new Text[]{
                  Text.literal("I wish I had a way to repair my equipment."),
                  Text.literal("I have some scrap metal that could be used to "),
                  Text.literal(" patch up my tools and armor but if I try to"),
                  Text.literal(" hammer it on, the surface beneath caves in."),
                  Text.literal("I need something sturdy to withstand my hammer.")
            },
            new ItemStack(Items.ANVIL)
      ));
      
      ResearchTasks.register(OBTAIN_ENCHANTED_GOLDEN_APPLE, new ObtainResearchTask(
            OBTAIN_ENCHANTED_GOLDEN_APPLE.getValue().getPath(), Items.ENCHANTED_GOLDEN_APPLE,
            Text.literal("Obtain an Enchanted Golden Apple"),
            new Text[]{
                  Text.literal("There is a powerful item whose recipe is lost."),
                  Text.literal("Many have set out in search of the mysterious"),
                  Text.literal(" Enchanted Golden Apple, but few ever find one."),
                  Text.literal("I assume they used to be made through a secret"),
                  Text.literal(" Arcane recipe that has been lost to time."),
                  Text.literal("I wonder if I can extract some Arcana from one"),
                  Text.literal(" of these apples to create something... novel.")
            },
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(OBTAIN_LEADERSHIP_CHARM, new ObtainResearchTask(
            OBTAIN_LEADERSHIP_CHARM.getValue().getPath(), ArcanaRegistry.LEADERSHIP_CHARM.getItem(),
            Text.literal("The Charm of Leadership"),
            new Text[]{
                  Text.literal("I haven't been able to learn much of this"),
                  Text.literal(" ancient divine artifact, other than that"),
                  Text.literal(" it is gifted to those deemed worthy to"),
                  Text.literal(" lead others by a divine entity.")
            },
            ArcanaRegistry.LEADERSHIP_CHARM.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_WINGS_OF_ENDERIA, new ObtainResearchTask(
            OBTAIN_WINGS_OF_ENDERIA.getValue().getPath(), ArcanaRegistry.WINGS_OF_ENDERIA.getItem(),
            Text.literal("The Wings of Enderia"),
            new Text[]{
                  Text.literal("Enderia, the fabled Empress of The End..."),
                  Text.literal("I know little of her, other than she is quite"),
                  Text.literal(" possibly one of the oldest beings still alive."),
                  Text.literal("I also have learned that she is a mad tyrant, "),
                  Text.literal(" who rules over the Enderman with a cold heart."),
                  Text.literal("It is possible she also possesses Divine Arcana..."),
            },
            ArcanaRegistry.WINGS_OF_ENDERIA.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(OBTAIN_PICKAXE_OF_CEPTYUS, new ObtainResearchTask(
            OBTAIN_PICKAXE_OF_CEPTYUS.getValue().getPath(), ArcanaRegistry.PICKAXE_OF_CEPTYUS.getItem(),
            Text.literal("The Pickaxe of Ceptyus"),
            new Text[]{
                  Text.literal("Archeological inquiry has dug up the name 'Ceptyus'"),
                  Text.literal(" on more than one occasion, mostly in the Deep Dark."),
                  Text.literal("This being was worshipped as a deity to the"),
                  Text.literal(" lost inhabitants of the ancient cities."),
                  Text.literal("Did Ceptyus bring the Sculk? Could Ceptyus BE the Sculk?"),
                  Text.literal("Whatever Ceptyus is, those who worshipped it favored"),
                  Text.literal(" underground exploration and mining a ton of ore."),
            },
            ArcanaRegistry.PICKAXE_OF_CEPTYUS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(BREAK_SCULK, new StatisticResearchTask<>(
            BREAK_SCULK.getValue().getPath(), Either.right(new Pair<>(Stats.MINED, Blocks.SCULK)), 1,
            Text.literal("Mine Sculk"),
            new Text[]{
                  Text.literal("I have heard horror stores from the depths."),
                  Text.literal("They speak of a night sky in the caves,"),
                  Text.literal(" glistening with a strange, but familiar glow."),
                  Text.literal("A substance that feels alive? And harbors "),
                  Text.literal(" more experience than any ore mined or smelted."),
                  Text.literal("They speak of an ancient warden who guards it...")
            },
            new ItemStack(Items.SCULK)
      ));
      
      ResearchTasks.register(BREAK_SPAWNER, new StatisticResearchTask<>(
            BREAK_SPAWNER.getValue().getPath(), Either.right(new Pair<>(Stats.MINED, Blocks.SPAWNER)), 1,
            Text.literal("Break a Spawner"),
            new Text[]{
                  Text.literal("These dungeons have so many great uses, if only"),
                  Text.literal(" I could make one of my own at my base."),
                  Text.literal("Why don't I just try to take this one with me?")
            },
            new ItemStack(Items.SPAWNER), FIND_SPAWNER
      ));
      
      ResearchTasks.register(BREAK_OBSIDIAN, new StatisticResearchTask<>(
            BREAK_OBSIDIAN.getValue().getPath(), Either.right(new Pair<>(Stats.MINED, Blocks.OBSIDIAN)), 256,
            Text.literal("Mine 4 stacks of Obsidian"),
            new Text[]{
                  Text.literal("Obsidian, yet another crystal to add to my collection."),
                  Text.literal("It's hardness and explosive resistance are such"),
                  Text.literal(" useful properties that I want to gather it in tons.")
            },
            new ItemStack(Items.OBSIDIAN)
      ));
      
      ResearchTasks.register(PLACE_TORCHES, new StatisticResearchTask<>(
            PLACE_TORCHES.getValue().getPath(), Either.right(new Pair<>(Stats.USED, Items.TORCH)), 128,
            Text.literal("Place 2 stacks of Torches"),
            new Text[]{
                  Text.literal("A stupid creeper blew up my beautiful yard again!"),
                  Text.literal("I can't keep letting this happen! I am going to"),
                  Text.literal(" light up the entire area around my base!")
            },
            new ItemStack(Items.TORCH)
      ));
      
      ResearchTasks.register(USE_FIREWORK, new StatisticResearchTask<>(
            USE_FIREWORK.getValue().getPath(), Either.right(new Pair<>(Stats.USED, Items.FIREWORK_ROCKET)), 1,
            Text.literal("Use a Firework"),
            new Text[]{
                  Text.literal("I wonder if there is a non-destructive use"),
                  Text.literal(" for gunpowder? Maybe something festive?")
            },
            new ItemStack(Items.FIREWORK_ROCKET)
      ));
      
      ResearchTasks.register(USE_CAMPFIRE, new StatisticResearchTask<>(
            USE_CAMPFIRE.getValue().getPath(), Either.left(Stats.INTERACT_WITH_CAMPFIRE), 1,
            Text.literal("Use a Campfire"),
            new Text[]{
                  Text.literal("Cooking food takes fuel that could be used on ores."),
                  Text.literal("Maybe there is alternative cooking method I can try.")
            },
            new ItemStack(Items.CAMPFIRE)
      ));
      
      ResearchTasks.register(USE_FLINT_AND_STEEL, new StatisticResearchTask<>(
            USE_FLINT_AND_STEEL.getValue().getPath(), Either.right(new Pair<>(Stats.USED, Items.FLINT_AND_STEEL)), 1,
            Text.literal("Use a Flint and Steel"),
            new Text[]{
                  Text.literal("Fire is such a useful tool. I wish it didn't"),
                  Text.literal(" take a whole setup to start one though."),
                  Text.literal("Maybe there is a tool I can make to start fires.")
            },
            new ItemStack(Items.FLINT_AND_STEEL)
      ));
      
      ResearchTasks.register(USE_ENDER_PEARL, new StatisticResearchTask<>(
            USE_ENDER_PEARL.getValue().getPath(), Either.right(new Pair<>(Stats.USED, Items.ENDER_PEARL)), 1,
            Text.literal("Use an Ender Pearl"),
            new Text[]{
                  Text.literal("A poor Enderman got caught out in the rain."),
                  Text.literal("As its flesh melted away from the water, only a"),
                  Text.literal(" strange green pearl remained from behind its eyes."),
                  Text.literal("I wonder why it didn't melt. What does it do?")
            },
            new ItemStack(Items.ENDER_PEARL)
      ));
      
      ResearchTasks.register(USE_ENDER_EYE, new StatisticResearchTask<>(
            USE_ENDER_EYE.getValue().getPath(), Either.right(new Pair<>(Stats.USED, Items.ENDER_EYE)), 1,
            Text.literal("Use an Eye of Ender"),
            new Text[]{
                  Text.literal("Interestingly enough, Blaze Powder was the key"),
                  Text.literal(" to re-awakening the Enderman's eye."),
                  Text.literal("However, it does not appear to possess the"),
                  Text.literal(" enhanced teleportation abilities I expected."),
                  Text.literal("There may be more to this mystery yet.")
            },
            new ItemStack(Items.ENDER_EYE), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(USE_ENDER_CHEST, new StatisticResearchTask<>(
            USE_ENDER_CHEST.getValue().getPath(), Either.left(Stats.OPEN_ENDERCHEST), 1,
            Text.literal("Use an Ender Chest"),
            new Text[]{
                  Text.literal("I have found a new use for Eyes of Ender."),
                  Text.literal("Aside from their natural homing ability that"),
                  Text.literal(" I recently discovered, they seem to also peer"),
                  Text.literal(" through dimensions, maybe for extra navigation?"),
                  Text.literal("However the Endermen warp themselves, they briefly"),
                  Text.literal(" enter an interdimensional space, an astral void."),
                  Text.literal("I might be able to use this place for storage.")
            },
            new ItemStack(Items.ENDER_CHEST), OBTAIN_EYE_OF_ENDER
      ));
      
      ResearchTasks.register(KILL_SLIME, new StatisticResearchTask<>(
            KILL_SLIME.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, EntityType.SLIME)), 100,
            Text.literal("Hunt 100 Slimes"),
            new Text[]{
                  Text.literal("I've never encountered anything as sticky as slime."),
                  Text.literal("This substance will be of great use in large amounts.")
            },
            new ItemStack(Items.SLIME_BALL), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_SQUID, new StatisticResearchTask<>(
            KILL_SQUID.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, EntityType.SQUID)), 25,
            Text.literal("Hunt 25 Squid"),
            new Text[]{
                  Text.literal("Squids have a strange self defense mechanism."),
                  Text.literal("It is quite unnerving and almost blinding."),
                  Text.literal("I wonder if their ink works on other creatures?")
            },
            new ItemStack(Items.INK_SAC), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_CONSTRUCT, new StatisticResearchTask<>(
            KILL_CONSTRUCT.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, ArcanaRegistry.NUL_CONSTRUCT_ENTITY)), 1,
            Text.literal("Defeat a Nul Construct"),
            new Text[]{
                  Text.literal("I want to push my sovereign catalyst further!"),
                  Text.literal("Divine Arcana is something I have yet to understand,"),
                  Text.literal(" but if I can siphon even a little bit into this"),
                  Text.literal(" catalyst then it will become immensely useful."),
                  Text.literal("Perhaps Divine Arcana has some relation to soul"),
                  Text.literal(" energy? Maybe that soul fueled monstrosity has"),
                  Text.literal(" some relation to a Divine entity?"),
                  Text.literal("Maybe I can make a stronger one, that is more"),
                  Text.literal(" powerful, to draw out some Divine Essence.")
            },
            ArcanaRegistry.NUL_MEMENTO.getPrefItemNoLore(), UNLOCK_SOVEREIGN_CATALYST, OBTAIN_NETHER_STAR, OBTAIN_NETHERITE_INGOT, ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_BLAZE, new StatisticResearchTask<>(
            KILL_BLAZE.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, EntityType.BLAZE)), 100,
            Text.literal("Hunt 100 Blazes"),
            new Text[]{
                  Text.literal("Blazes have a lot of powerful properties."),
                  Text.literal("Flight, fire immunity, flaming projectiles..."),
                  Text.literal("Not to mention their rods have innate alchemical"),
                  Text.literal(" properties that lead to many possible reactions."),
                  Text.literal("It would benefit me greatly to collect more rods.")
            },
            new ItemStack(Items.BLAZE_ROD), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_MAGMA_CUBE, new StatisticResearchTask<>(
            KILL_MAGMA_CUBE.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, EntityType.MAGMA_CUBE)), 100,
            Text.literal("Hunt 100 Magma Cubes"),
            new Text[]{
                  Text.literal("Magma Cubes are like Slimes, but with the"),
                  Text.literal(" evolutionary advantages of being from the Nether."),
                  Text.literal("However, slime is nothing like magma cream."),
                  Text.literal("Magma cream is smooth and warm, instead of sticky"),
                  Text.literal("I wish to collect some to analyze it further.")
            },
            new ItemStack(Items.MAGMA_CREAM), ADVANCEMENT_KILL_A_MOB
      ));
      
      ResearchTasks.register(KILL_EVOKER, new StatisticResearchTask<>(
            KILL_EVOKER.getValue().getPath(), Either.right(new Pair<>(Stats.KILLED, EntityType.EVOKER)), 10,
            Text.literal("Defeat 10 Evokers"),
            new Text[]{
                  Text.literal("Illagers... The outcast villager faction."),
                  Text.literal("Somehow, they are quite skilled in the Arcane."),
                  Text.literal("However, their use of Arcana is twisted and dark."),
                  Text.literal("Perhaps this was the reason for their exile?"),
                  Text.literal("Regardless, perhaps there is something I can learn"),
                  Text.literal(" from their dark ways. I must visit their Arcanists.")
            },
            new ItemStack(Items.TOTEM_OF_UNDYING), ADVANCEMENT_KILL_A_MOB
      ));
      
      
      
      ResearchTasks.register(UNLOCK_RUNIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_RUNIC_MATRIX.getValue().getPath(), ArcanaRegistry.RUNIC_MATRIX,
            Text.literal("Research the Runic Matrix"),
            new Text[]{
                  Text.literal("This design needs something capable of adapting"),
                  Text.literal(" to different forms of Arcana in a modular way."),
                  Text.literal("I think I must invent something else first...")
            },
            ArcanaRegistry.RUNIC_MATRIX.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_STARLIGHT_FORGE, new ArcanaItemResearchTask(
            UNLOCK_STARLIGHT_FORGE.getValue().getPath(), ArcanaRegistry.STARLIGHT_FORGE,
            Text.literal("Research the Starlight Forge"),
            new Text[]{
                  Text.literal("This blueprint expands on the infrastructure"),
                  Text.literal(" that is used to forge Arcane items."),
                  Text.literal("Right now I have no such infrastructure,"),
                  Text.literal(" I will return once I have completed some.")
            },
            ArcanaRegistry.STARLIGHT_FORGE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TWILIGHT_ANVIL, new ArcanaItemResearchTask(
            UNLOCK_TWILIGHT_ANVIL.getValue().getPath(), ArcanaRegistry.TWILIGHT_ANVIL,
            Text.literal("Research the Twilight Anvil"),
            new Text[]{
                  Text.literal("This blueprint would require some infrastructure"),
                  Text.literal(" to forge Netherite and augment Arcana."),
                  Text.literal("I will come back to this once I am prepared.")
            },
            ArcanaRegistry.TWILIGHT_ANVIL.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_TEMPORAL_MOMENT, new ArcanaItemResearchTask(
            UNLOCK_TEMPORAL_MOMENT.getValue().getPath(), ArcanaRegistry.TEMPORAL_MOMENT,
            Text.literal("Research the Temporal Moment"),
            new Text[]{
                  Text.literal("This design is based around the ability to"),
                  Text.literal(" manipulate time. I am afraid that is beyond"),
                  Text.literal(" my current capabilities."),
                  Text.literal("Until I make a new discovery, this is infeasible.")
            },
            ArcanaRegistry.TEMPORAL_MOMENT.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_MATTER, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_MATTER.getValue().getPath(), ArcanaRegistry.EXOTIC_MATTER,
            Text.literal("Research the Exotic Matter"),
            new Text[]{
                  Text.literal("This contraption requires a substance that"),
                  Text.literal(" can cause warps in the fabric of space-time."),
                  Text.literal("There is no point in continuing this schematic"),
                  Text.literal(" until I have discovered this type of fuel.")
            },
            ArcanaRegistry.EXOTIC_MATTER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SHULKER_CORE, new ArcanaItemResearchTask(
            UNLOCK_SHULKER_CORE.getValue().getPath(), ArcanaRegistry.SHULKER_CORE,
            Text.literal("Research the Shulker Core"),
            new Text[]{
                  Text.literal("This device relies on a gravimetric core to defy"),
                  Text.literal(" gravity itself. However, I currently do not have"),
                  Text.literal(" any schematics for such gravity manipulation."),
                  Text.literal("Developing that first would be a better plan.")
            },
            ArcanaRegistry.SHULKER_CORE.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_MIDNIGHT_ENCHANTER, new ArcanaItemResearchTask(
            UNLOCK_MIDNIGHT_ENCHANTER.getValue().getPath(), ArcanaRegistry.MIDNIGHT_ENCHANTER,
            Text.literal("Research the Midnight Enchanter"),
            new Text[]{
                  Text.literal("This project requires advanced enchantment"),
                  Text.literal(" capabilities that I do not have right now."),
                  Text.literal("If I can upgrade my infrastructure a bit, "),
                  Text.literal(" then this project would be more feasible.")
            },
            ArcanaRegistry.MIDNIGHT_ENCHANTER.getPrefItemNoLore(), UNLOCK_STARLIGHT_FORGE
      ));
      
      ResearchTasks.register(UNLOCK_STELLAR_CORE, new ArcanaItemResearchTask(
            UNLOCK_STELLAR_CORE.getValue().getPath(), ArcanaRegistry.STELLAR_CORE,
            Text.literal("Research the Stellar Core"),
            new Text[]{
                  Text.literal("This schematic would require a reinforced"),
                  Text.literal(" frame that I do not have the capability"),
                  Text.literal(" to create. I should upgrade my forge setup"),
                  Text.literal(" and then return once I have better equipment.")
            },
            ArcanaRegistry.STELLAR_CORE.getPrefItemNoLore(), UNLOCK_TWILIGHT_ANVIL
      ));
      
      ResearchTasks.register(UNLOCK_ARCANE_SINGULARITY, new ArcanaItemResearchTask(
            UNLOCK_ARCANE_SINGULARITY.getValue().getPath(), ArcanaRegistry.ARCANE_SINGULARITY,
            Text.literal("Research the Arcane Singularity"),
            new Text[]{
                  Text.literal("This project requires Arcane infrastructure"),
                  Text.literal(" that is capable of complete mastery over"),
                  Text.literal(" Arcana infusion with absolute precision."),
                  Text.literal("Without that, there is no way I can make this.")
            },
            ArcanaRegistry.ARCANE_SINGULARITY.getPrefItemNoLore(), UNLOCK_STELLAR_CORE, UNLOCK_MIDNIGHT_ENCHANTER
      ));
      
      ResearchTasks.register(UNLOCK_RADIANT_FLETCHERY, new ArcanaItemResearchTask(
            UNLOCK_RADIANT_FLETCHERY.getValue().getPath(), ArcanaRegistry.RADIANT_FLETCHERY,
            Text.literal("Research the Radiant Fletchery"),
            new Text[]{
                  Text.literal("This equipment needs a dedicated workstation"),
                  Text.literal(" for my archery related inventions."),
                  Text.literal("I will work on that prior to completing this.")
            },
            ArcanaRegistry.RADIANT_FLETCHERY.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_SOULSTONE, new ArcanaItemResearchTask(
            UNLOCK_SOULSTONE.getValue().getPath(), ArcanaRegistry.SOULSTONE,
            Text.literal("Research the Soulstone"),
            new Text[]{
                  Text.literal("This schematic draws upon too much soul energy"),
                  Text.literal(" to be done with only Nether Stars and soul sand."),
                  Text.literal("I need to design a better soul harness first.")
            },
            ArcanaRegistry.SOULSTONE.getPrefItemNoLore(), OBTAIN_NETHER_STAR
      ));
      
      ResearchTasks.register(UNLOCK_CATALYTIC_MATRIX, new ArcanaItemResearchTask(
            UNLOCK_CATALYTIC_MATRIX.getValue().getPath(), ArcanaRegistry.CATALYTIC_MATRIX,
            Text.literal("Research the Catalytic Matrix"),
            new Text[]{
                  Text.literal("To begin delving into Arcane Augmentation,"),
                  Text.literal(" I must first adapt my Runic Matrix into a"),
                  Text.literal(" form designed specifically for catalysis.")
            },
            ArcanaRegistry.CATALYTIC_MATRIX.getPrefItemNoLore(), UNLOCK_RUNIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_MUNDANE_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_MUNDANE_CATALYST.getValue().getPath(), ArcanaRegistry.MUNDANE_CATALYST,
            Text.literal("Research the Mundane Catalyst"),
            new Text[]{
                  Text.literal("I might be skipping steps here..."),
                  Text.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.MUNDANE_CATALYST.getPrefItemNoLore(), UNLOCK_CATALYTIC_MATRIX
      ));
      
      ResearchTasks.register(UNLOCK_EMPOWERED_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EMPOWERED_CATALYST.getValue().getPath(), ArcanaRegistry.EMPOWERED_CATALYST,
            Text.literal("Research the Empowered Catalyst"),
            new Text[]{
                  Text.literal("I might be skipping steps here..."),
                  Text.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.EMPOWERED_CATALYST.getPrefItemNoLore(), UNLOCK_MUNDANE_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_EXOTIC_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_EXOTIC_CATALYST.getValue().getPath(), ArcanaRegistry.EXOTIC_CATALYST,
            Text.literal("Research the Exotic Catalyst"),
            new Text[]{
                  Text.literal("I might be skipping steps here..."),
                  Text.literal("I should understand lesser catalysts first.")
            },
            ArcanaRegistry.EXOTIC_CATALYST.getPrefItemNoLore(), UNLOCK_EMPOWERED_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_SOVEREIGN_CATALYST, new ArcanaItemResearchTask(
            UNLOCK_SOVEREIGN_CATALYST.getValue().getPath(), ArcanaRegistry.SOVEREIGN_CATALYST,
            Text.literal("Research the Sovereign Catalyst"),
            new Text[]{
                  Text.literal("I should maximize the potential of my Catalytic"),
                  Text.literal(" Matrix myself before attempting something as"),
                  Text.literal(" bold as chasing down Divine Arcana.")
            },
            ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(), UNLOCK_EXOTIC_CATALYST
      ));
      
      ResearchTasks.register(UNLOCK_OVERFLOWING_QUIVER, new ArcanaItemResearchTask(
            UNLOCK_OVERFLOWING_QUIVER.getValue().getPath(), ArcanaRegistry.OVERFLOWING_QUIVER,
            Text.literal("Research the Overflowing Quiver"),
            new Text[]{
                  Text.literal("This blueprint is a bit too bold for me now."),
                  Text.literal("I should break this down into smaller steps,"),
                  Text.literal(" and design a non-runic quiver first.")
            },
            ArcanaRegistry.OVERFLOWING_QUIVER.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_SPAWNER_HARNESS, new ArcanaItemResearchTask(
            UNLOCK_SPAWNER_HARNESS.getValue().getPath(), ArcanaRegistry.SPAWNER_HARNESS,
            Text.literal("Research the Spawner Harness"),
            new Text[]{
                  Text.literal("This design requires absolute mastery over"),
                  Text.literal(" monster spawners. However, I have yet to"),
                  Text.literal(" successfully find a way to move them."),
                  Text.literal("That is a more important research endeavor.")
            },
            ArcanaRegistry.SPAWNER_HARNESS.getPrefItemNoLore()
      ));
      
      ResearchTasks.register(UNLOCK_TRANSMUTATION_ALTAR, new ArcanaItemResearchTask(
            UNLOCK_TRANSMUTATION_ALTAR.getValue().getPath(), ArcanaRegistry.TRANSMUTATION_ALTAR,
            Text.literal("Research the Transmutation Altar"),
            new Text[]{
                  Text.literal("My notes on this are scattered..."),
                  Text.literal("This has something to do with transmutation?"),
                  Text.literal("I think I need to learn how to transmute things...")
            },
            ArcanaRegistry.TRANSMUTATION_ALTAR.getPrefItemNoLore()
      ));
      
      
      ResearchTasks.register(ADVANCEMENT_ENCHANT_ITEM, new AdvancementResearchTask(
            ADVANCEMENT_ENCHANT_ITEM.getValue().getPath(), "story/enchant_item",
            Text.literal("Enchant an Item"),
            new Text[]{
                  Text.literal("I should get a grasp on rudimentary Arcane"),
                  Text.literal(" infusion methods before pursuing this.")
            },
            new ItemStack(Items.ENCHANTING_TABLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CHARGE_RESPAWN_ANCHOR, new AdvancementResearchTask(
            ADVANCEMENT_CHARGE_RESPAWN_ANCHOR.getValue().getPath(), "nether/charge_respawn_anchor",
            Text.literal("Anchor Your Respawn Point"),
            new Text[]{
                  Text.literal("Beds have rather explosive consequences in the Nether."),
                  Text.literal("There should be some way to work around this issue.")
            },
            new ItemStack(Items.RESPAWN_ANCHOR)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_ANCIENT_DEBRIS.getValue().getPath(), "nether/obtain_ancient_debris",
            Text.literal("Discover the Strongest Material"),
            new Text[]{
                  Text.literal("Whispers speak of a material stronger than diamond!"),
                  Text.literal("It is rumored to be found deep within the Nether...")
            },
            new ItemStack(Items.ANCIENT_DEBRIS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ELYTRA, new AdvancementResearchTask(
            ADVANCEMENT_ELYTRA.getValue().getPath(), "end/elytra",
            Text.literal("Steal a Pair of Wings"),
            new Text[]{
                  Text.literal("Legends speak of the ancient species that once roamed"),
                  Text.literal(" the End with mastery over the skies and stars..."),
                  Text.literal("Maybe there are some artifacts of theirs left behind?")
            },
            new ItemStack(Items.ELYTRA)
      ));
      
      ResearchTasks.register(ADVANCEMENT_FIND_BASTION, new AdvancementResearchTask(
            ADVANCEMENT_FIND_BASTION.getValue().getPath(), "nether/find_bastion",
            Text.literal("Discover an Ancient Bastion"),
            new Text[]{
                  Text.literal("Most of the intelligent creatures in the Nether"),
                  Text.literal(" live in some structures of some sort."),
                  Text.literal("I wonder if there are any that wont attack me.")
            },
            new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS, new AdvancementResearchTask(
            ADVANCEMENT_CRAFTERS_CRAFTING_CRAFTERS.getValue().getPath(), "adventure/crafters_crafting_crafters",
            Text.literal("Recursively Craft a Crafter"),
            new Text[]{
                  Text.literal("I wonder if an automated crafting matrix"),
                  Text.literal(" is capable of self-replication?")
            },
            new ItemStack(Items.CRAFTER)
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_A_MOB, new AdvancementResearchTask(
            ADVANCEMENT_KILL_A_MOB.getValue().getPath(), "adventure/kill_a_mob",
            Text.literal("Hunt a Mob"),
            new Text[]{
                  Text.literal("These creatures of the night will hunt me no more!")
            },
            new ItemStack(Items.DIAMOND_SWORD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LEVITATE, new AdvancementResearchTask(
            ADVANCEMENT_LEVITATE.getValue().getPath(), "end/levitate",
            Text.literal("Appreciate Levitation from a Great Height"),
            new Text[]{
                  Text.literal("I wonder how high up this unique effect can take me.")
            },
            new ItemStack(Items.SHULKER_BOX)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BALANCED_DIET, new AdvancementResearchTask(
            ADVANCEMENT_BALANCED_DIET.getValue().getPath(), "husbandry/balanced_diet",
            Text.literal("Sample All of the World's Cuisine"),
            new Text[]{
                  Text.literal("Every food has such a unique flavor and taste."),
                  Text.literal("I must try them all!")
            },
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DEFLECT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_DEFLECT_ARROW.getValue().getPath(), "story/deflect_arrow",
            Text.literal("Block an Arrow"),
            new Text[]{
                  Text.literal("Surely there is something that can block an arrow.")
            },
            new ItemStack(Items.SHIELD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ADVENTURING_TIME, new AdvancementResearchTask(
            ADVANCEMENT_ADVENTURING_TIME.getValue().getPath(), "adventure/adventuring_time",
            Text.literal("Discover all of the Overworld's Biomes"),
            new Text[]{
                  Text.literal("The Overworld has such unique biodiversity!"),
                  Text.literal("I must see it all!")
            },
            new ItemStack(Items.DIAMOND_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS, new AdvancementResearchTask(
            ADVANCEMENT_WALK_ON_POWDER_SNOW_WITH_LEATHER_BOOTS.getValue().getPath(), "adventure/walk_on_powder_snow_with_leather_boots",
            Text.literal("Find a Way to Traverse Light Snow"),
            new Text[]{
                  Text.literal("This powdery snow is an absolute death trap!"),
                  Text.literal("There must be something that can keep me safe...")
            },
            new ItemStack(Items.LEATHER_BOOTS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_CREATE_FULL_BEACON, new AdvancementResearchTask(
            ADVANCEMENT_CREATE_FULL_BEACON.getValue().getPath(), "nether/create_full_beacon",
            Text.literal("Maximize a Beacon's Power"),
            new Text[]{
                  Text.literal("Even this little containment pedestal was enough"),
                  Text.literal(" to reveal some of the Nether Star's power!"),
                  Text.literal("I wonder to what heights I can push this?")
            },
            new ItemStack(Items.BEACON), OBTAIN_BEACON
      ));
      
      ResearchTasks.register(ADVANCEMENT_SLEEP_IN_BED, new AdvancementResearchTask(
            ADVANCEMENT_SLEEP_IN_BED.getValue().getPath(), "adventure/sleep_in_bed",
            Text.literal("Have a Sleep"),
            new Text[]{
                  Text.literal("Night time can be scary, perhaps I should sleep it off.")
            },
            new ItemStack(Items.RED_BED)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SHOOT_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_SHOOT_ARROW.getValue().getPath(), "adventure/shoot_arrow",
            Text.literal("Become an Archer"),
            new Text[]{
                  Text.literal("If skeletons can do it, so can I!")
            },
            new ItemStack(Items.ARROW)
      ));
      
      ResearchTasks.register(ADVANCEMENT_SNIPER_DUEL, new AdvancementResearchTask(
            ADVANCEMENT_SNIPER_DUEL.getValue().getPath(), "adventure/sniper_duel",
            Text.literal("Duel the World's Best Archers"),
            new Text[]{
                  Text.literal("Surely I can outclass a mere skeleton."),
                  Text.literal("I will defeat one from well over 50 blocks!")
            },
            new ItemStack(Items.BOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BULLSEYE, new AdvancementResearchTask(
            ADVANCEMENT_BULLSEYE.getValue().getPath(), "adventure/bullseye",
            Text.literal("Train Your Archery"),
            new Text[]{
                  Text.literal("My bow skills are a little lacking."),
                  Text.literal("Time to hit the range, and hit the target square on!")
            },
            new ItemStack(Items.TARGET), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREW_POTION, new AdvancementResearchTask(
            ADVANCEMENT_BREW_POTION.getValue().getPath(), "nether/brew_potion",
            Text.literal("Attempt Some Alchemy"),
            new Text[]{
                  Text.literal("If villagers can use a potion stand,"),
                  Text.literal(" I should give it a try myself.")
            },
            new ItemStack(Items.BREWING_STAND)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN.getValue().getPath(), "nether/obtain_crying_obsidian",
            Text.literal("Unlock Obsidian's Hidden Potential"),
            new Text[]{
                  Text.literal("Some of the old portal ruins I have found"),
                  Text.literal(" contain strange obsidian. It has been broken,"),
                  Text.literal(" but not physically? It looks like it's crying."),
                  Text.literal("I wonder what possibilities this could unlock.")
            },
            new ItemStack(Items.CRYING_OBSIDIAN)
      ));
      
      ResearchTasks.register(ADVANCEMENT_DRAGON_BREATH, new AdvancementResearchTask(
            ADVANCEMENT_DRAGON_BREATH.getValue().getPath(), "end/dragon_breath",
            Text.literal("Take a Sample of Enderflame"),
            new Text[]{
                  Text.literal("Enderflame sticks and spreads like a liquid."),
                  Text.literal("It is also, obviously, alchemically active."),
                  Text.literal("I should take a sample for further study.")
            },
            new ItemStack(Items.DRAGON_BREATH), ADVANCEMENT_BREW_POTION
      ));
      
      ResearchTasks.register(ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST, new AdvancementResearchTask(
            ADVANCEMENT_KILL_MOB_NEAR_SCULK_CATALYST.getValue().getPath(), "adventure/kill_mob_near_sculk_catalyst",
            Text.literal("Discover the Sculk's True Nature"),
            new Text[]{
                  Text.literal("This 'Sculk' is unfathomable! If I had to guess"),
                  Text.literal(" it is like some sort of fungus, a giant organism!"),
                  Text.literal("To be this dense in experience, it must be absolutely"),
                  Text.literal(" ancient, and have a rich supply of nutrients to grow."),
                  Text.literal("How does it get nutrients when its underground?")
            },
            new ItemStack(Items.SCULK_CATALYST), BREAK_SCULK
      ));
      
      ResearchTasks.register(ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE, new AdvancementResearchTask(
            ADVANCEMENT_LIGHTNING_ROD_WITH_VILLAGER_NO_FIRE.getValue().getPath(), "adventure/lightning_rod_with_villager_no_fire",
            Text.literal("Safely Harness Lightning"),
            new Text[]{
                  Text.literal("With my new lightning rod, I should put it to use!"),
                  Text.literal("I will protect the nearest village from lightning!"),
            },
            new ItemStack(Items.LIGHTNING_ROD), OBTAIN_LIGHTNING_ROD
      ));
      
      ResearchTasks.register(ADVANCEMENT_TAME_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_TAME_AN_ANIMAL.getValue().getPath(), "husbandry/tame_an_animal",
            Text.literal("Experience Pet Ownership"),
            new Text[]{
                  Text.literal("With so many critters out in the world"),
                  Text.literal(" surely at least one wants to be friends.")
            },
            new ItemStack(Items.BONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_TRADE, new AdvancementResearchTask(
            ADVANCEMENT_TRADE.getValue().getPath(), "adventure/trade",
            Text.literal("Learn to Trade"),
            new Text[]{
                  Text.literal("Villagers have a fairly sophisticated economy."),
                  Text.literal("They produce goods and barter amongst themselves."),
                  Text.literal("I should learn how to trade with them.")
            },
            new ItemStack(Items.EMERALD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_USE_LODESTONE, new AdvancementResearchTask(
            ADVANCEMENT_USE_LODESTONE.getValue().getPath(), "nether/use_lodestone",
            Text.literal("Discover Geolocation"),
            new Text[]{
                  Text.literal("I wonder if there is any way to bind a compass"),
                  Text.literal(" to a fixed location instead of my bed?")
            },
            new ItemStack(Items.LODESTONE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_COMPLETE_CATALOGUE, new AdvancementResearchTask(
            ADVANCEMENT_COMPLETE_CATALOGUE.getValue().getPath(), "husbandry/complete_catalogue",
            Text.literal("Become a Crazy Cat Lady"),
            new Text[]{
                  Text.literal("Cats are so adorable! I want them all!")
            },
            new ItemStack(Items.COD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_ON, new AdvancementResearchTask(
            ADVANCEMENT_WAX_ON.getValue().getPath(), "husbandry/wax_on",
            Text.literal("Protect Copper from the Elements"),
            new Text[]{
                  Text.literal("Copper would look so pretty if it didn't rust."),
                  Text.literal("I wonder if I can give it a protective coating?")
            },
            new ItemStack(Items.HONEYCOMB)
      ));
      
      ResearchTasks.register(ADVANCEMENT_WAX_OFF, new AdvancementResearchTask(
            ADVANCEMENT_WAX_OFF.getValue().getPath(), "husbandry/wax_off",
            Text.literal("Expose Copper to the Elements"),
            new Text[]{
                  Text.literal("Hmmm, maybe the rust is charming after all.")
            },
            new ItemStack(Items.IRON_AXE)
      ));
      
      ResearchTasks.register(ADVANCEMENT_OL_BETSY, new AdvancementResearchTask(
            ADVANCEMENT_OL_BETSY.getValue().getPath(), "adventure/ol_betsy",
            Text.literal("Try Out a New Type of Bow"),
            new Text[]{
                  Text.literal("Bows are fine and all, but some villagers"),
                  Text.literal(" have told me of their exiled members who"),
                  Text.literal(" favor a similar ranged weapon that fires"),
                  Text.literal(" slower but with more power."),
                  Text.literal("I should try one of these 'crossbows' out.")
            },
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_SHOOT_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_ARBALISTIC, new AdvancementResearchTask(
            ADVANCEMENT_ARBALISTIC.getValue().getPath(), "adventure/arbalistic",
            Text.literal("Master the Crossbow"),
            new Text[]{
                  Text.literal("These piercing bolts are quite the trick."),
                  Text.literal("I wonder how many mobs I can kill in one shot?")
            },
            new ItemStack(Items.CROSSBOW), ADVANCEMENT_TWO_BIRDS_ONE_ARROW
      ));
      
      ResearchTasks.register(ADVANCEMENT_TWO_BIRDS_ONE_ARROW, new AdvancementResearchTask(
            ADVANCEMENT_TWO_BIRDS_ONE_ARROW.getValue().getPath(), "adventure/two_birds_one_arrow",
            Text.literal("Use a Crossbow to Liberate the Night Skies"),
            new Text[]{
                  Text.literal("Crossbows pack quite the punch! And with a "),
                  Text.literal(" bit of enchantment, the arrows can whiz"),
                  Text.literal(" straight through multiple creatures!"),
                  Text.literal("Now I can knock two phantoms out of the sky at once!")
            },
            new ItemStack(Items.PHANTOM_MEMBRANE), ADVANCEMENT_OL_BETSY, ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(ADVANCEMENT_WHOS_THE_PILLAGER_NOW, new AdvancementResearchTask(
            ADVANCEMENT_WHOS_THE_PILLAGER_NOW.getValue().getPath(), "adventure/whos_the_pillager_now",
            Text.literal("Duel the World's Best Arbalesters"),
            new Text[]{
                  Text.literal("It's time for a duel! If I can beat the masters"),
                  Text.literal(" of the crossbow arts, who have been trained to"),
                  Text.literal(" hunt their former neighbors. Then, I should be"),
                  Text.literal(" ready to push this crossbow to its limits.")
            },
            new ItemStack(Items.CROSSBOW),ADVANCEMENT_OL_BETSY
      ));
      
      ResearchTasks.register(ADVANCEMENT_TOTEM_OF_UNDYING, new AdvancementResearchTask(
            ADVANCEMENT_TOTEM_OF_UNDYING.getValue().getPath(), "adventure/totem_of_undying",
            Text.literal("Discover a Form of Immortality"),
            new Text[]{
                  Text.literal("The villagers speak of their exiled brethren."),
                  Text.literal("They tell tales of their dark arcanists who"),
                  Text.literal(" have a method of cheating death itself."),
                  Text.literal("How can this be?!")
            },
            new ItemStack(Items.TOTEM_OF_UNDYING)
      ));
      
      ResearchTasks.register(ADVANCEMENT_LAVA_BUCKET, new AdvancementResearchTask(
            ADVANCEMENT_LAVA_BUCKET.getValue().getPath(), "story/lava_bucket",
            Text.literal("Obtain Lava"),
            new Text[]{
                  Text.literal("Lava is such a useful substance."),
                  Text.literal("I should collect some for study.")
            },
            new ItemStack(Items.LAVA_BUCKET)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER.getValue().getPath(), "husbandry/allay_deliver_item_to_player",
            Text.literal("Befriend an Ancient Forest Sprite"),
            new Text[]{
                  Text.literal("Old tales mention the sprites of the dark forests."),
                  Text.literal("I've seen dark forests, and they are quite spriteless."),
                  Text.literal("I wonder where they have all gone?")
            },
            new ItemStack(Items.AMETHYST_SHARD)
      ));
      
      ResearchTasks.register(ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK, new AdvancementResearchTask(
            ADVANCEMENT_ALLAY_DELIVER_CAKE_TO_NOTE_BLOCK.getValue().getPath(), "husbandry/allay_deliver_cake_to_note_block",
            Text.literal("Show Appreciation for the Forest Sprites"),
            new Text[]{
                  Text.literal("These 'Allays' are wonderful! And quite talented!"),
                  Text.literal("They also seem to have some innate Arcane abilities."),
                  Text.literal("I've noticed they like certain chimes, but I wonder"),
                  Text.literal(" if they like cake.")
            },
            new ItemStack(Items.CAKE), ADVANCEMENT_ALLAY_DELIVER_ITEM_TO_PLAYER
      ));
      
      ResearchTasks.register(ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF, new AdvancementResearchTask(
            ADVANCEMENT_READ_POWER_OF_CHISELED_BOOKSHELF.getValue().getPath(), "adventure/read_power_of_chiseled_bookshelf",
            Text.literal("Discover the Power of Books"),
            new Text[]{
                  Text.literal("Chiseled bookshelves are quite fashionable."),
                  Text.literal("I wonder if they have any practical use beyond"),
                  Text.literal(" the obvious book-storing capabilities.")
            },
            new ItemStack(Items.CHISELED_BOOKSHELF)
      ));
      
      ResearchTasks.register(ADVANCEMENT_BREED_AN_ANIMAL, new AdvancementResearchTask(
            ADVANCEMENT_BREED_AN_ANIMAL.getValue().getPath(), "husbandry/breed_an_animal",
            Text.literal("Breed an Animal"),
            new Text[]{
                  Text.literal("Surely I can coax animals into reproducing...")
            },
            new ItemStack(Items.WHEAT)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_SEED.getValue().getPath(), "husbandry/plant_seed",
            Text.literal("Discover Agriculture"),
            new Text[]{
                  Text.literal("With just a few seeds, whole forests grow."),
                  Text.literal("I should learn how to do this myself.")
            },
            new ItemStack(Items.WHEAT_SEEDS)
      ));
      
      ResearchTasks.register(ADVANCEMENT_PLANT_ANY_SNIFFER_SEED, new AdvancementResearchTask(
            ADVANCEMENT_PLANT_ANY_SNIFFER_SEED.getValue().getPath(), "husbandry/plant_any_sniffer_seed",
            Text.literal("Cultivate some Ancient Seeds"),
            new Text[]{
                  Text.literal("There are some ancient seeds which can only be found"),
                  Text.literal(" by a creature of ancient origins, lost to time."),
                  Text.literal("I wish to plant one of these long lost seeds.")
            },
            new ItemStack(Items.TORCHFLOWER_SEEDS), ADVANCEMENT_PLANT_SEED
      ));
      
      ResearchTasks.register(ADVANCEMENT_OBTAIN_NETHERITE_HOE, new AdvancementResearchTask(
            ADVANCEMENT_OBTAIN_NETHERITE_HOE.getValue().getPath(), "husbandry/obtain_netherite_hoe",
            Text.literal("Show Dedication to Agriculture"),
            new Text[]{
                  Text.literal("If I am to fully understand agriculture,"),
                  Text.literal(" I must show complete dedication to the craft."),
                  Text.literal("What better way to do that than create a hoe"),
                  Text.literal(" from the strongest material there is.")
            },
            new ItemStack(Items.NETHERITE_HOE), ADVANCEMENT_PLANT_SEED, OBTAIN_NETHERITE_INGOT
      ));
      
      ResearchTasks.register(ADVANCEMENT_BRED_ALL_ANIMALS, new AdvancementResearchTask(
            ADVANCEMENT_BRED_ALL_ANIMALS.getValue().getPath(), "husbandry/bred_all_animals",
            Text.literal("Domesticate Every Animal"),
            new Text[]{
                  Text.literal("In order to master the natural order,"),
                  Text.literal(" I must understand every animal."),
                  Text.literal("To do that I must see the creation of them all.")
            },
            new ItemStack(Items.GOLDEN_CARROT), ADVANCEMENT_BREED_AN_ANIMAL, ADVANCEMENT_TAME_AN_ANIMAL
      ));
      
      ResearchTasks.register(ADVANCEMENT_FALL_FROM_WORLD_HEIGHT, new AdvancementResearchTask(
            ADVANCEMENT_FALL_FROM_WORLD_HEIGHT.getValue().getPath(), "adventure/fall_from_world_height",
            Text.literal("Experience the Height of the World"),
            new Text[]{
                  Text.literal("I should get a better appreciation for how"),
                  Text.literal(" tall and deep this world goes."),
                  Text.literal("From the highest peak to the bedrock, I"),
                  Text.literal(" will experience it all at once!")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      
      ResearchTasks.register(DIMENSION_TRAVEL, new CustomResearchTask(
            DIMENSION_TRAVEL.getValue().getPath(),
            Text.literal("Discover a New Dimension"),
            new Text[]{
                  Text.literal("I have seen ruined portals and heard rumors of"),
                  Text.literal(" worlds beyond the Overworld, but I have never"),
                  Text.literal(" travelled to one. I wonder what they're like?")
            },
            new ItemStack(Items.END_PORTAL_FRAME)
      ));
      
      ResearchTasks.register(CAT_SCARE, new CustomResearchTask(
            CAT_SCARE.getValue().getPath(),
            Text.literal("Watch a Cat Scare Off Predators"),
            new Text[]{
                  Text.literal("Cats are so cute! It almost makes you"),
                  Text.literal(" forget they are vicious predators!"),
                  Text.literal("I wonder if any mobs are scared of them?")
            },
            new ItemStack(Items.STRING)
      ));
      
      ResearchTasks.register(RESONATE_BELL, new CustomResearchTask(
            RESONATE_BELL.getValue().getPath(),
            Text.literal("Reveal the Revealing Power of a Bell"),
            new Text[]{
                  Text.literal("Villages all have this central bell, and they"),
                  Text.literal(" place great value on it, like their lives"),
                  Text.literal(" depend on it to protect them or something."),
                  Text.literal("I wonder what is so special about it?")
            },
            new ItemStack(Items.BELL)
      ));
      
      ResearchTasks.register(RIPTIDE_TRIDENT, new CustomResearchTask(
            RIPTIDE_TRIDENT.getValue().getPath(),
            Text.literal("Use a Trident to Soar Through Water"),
            new Text[]{
                  Text.literal("I am trying to figure out ways to propel myself."),
                  Text.literal("I have heard that those with mastery over tridents"),
                  Text.literal(" can use them to move through the water quickly."),
                  Text.literal("I should see if I can try this out.")
            },
            new ItemStack(Items.TRIDENT)
      ));
      
      ResearchTasks.register(FISH_ITEM, new CustomResearchTask(
            FISH_ITEM.getValue().getPath(),
            Text.literal("Use a Fishing Rod to Grab an Item"),
            new Text[]{
                  Text.literal("I wish I could grab items from a further distance."),
                  Text.literal("Maybe there is some way I haven't thought of...")
            },
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(FISH_MOB, new CustomResearchTask(
            FISH_MOB.getValue().getPath(),
            Text.literal("Use a Fishing Rod to Grab a Mob"),
            new Text[]{
                  Text.literal("I wish I could pull some mobs closer to me."),
                  Text.literal("Maybe there is already a way to do this...")
            },
            new ItemStack(Items.FISHING_ROD)
      ));
      
      ResearchTasks.register(LEVEL_100, new CustomResearchTask(
            LEVEL_100.getValue().getPath(),
            Text.literal("Acquire a Century of Experience"),
            new Text[]{
                  Text.literal("I always spend experience almost as fast as"),
                  Text.literal(" I can accumulate it, or lose it entirely."),
                  Text.literal("I should try to reach a high level instead.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE)
      ));
      
      ResearchTasks.register(HUNGER_DAMAGE, new CustomResearchTask(
            HUNGER_DAMAGE.getValue().getPath(),
            Text.literal("Experience Hunger Pains"),
            new Text[]{
                  Text.literal("I have always been spoiled for food."),
                  Text.literal("I wonder what it feels like to be truly hungry.")
            },
            new ItemStack(Items.ROTTEN_FLESH)
      ));
      
      ResearchTasks.register(CONCENTRATION_DAMAGE, new CustomResearchTask(
            CONCENTRATION_DAMAGE.getValue().getPath(),
            Text.literal("Experience Your Mind Collapsing"),
            new Text[]{
                  Text.literal("With every Arcana Item I hold it puts a burden on"),
                  Text.literal(" my mind. Growing ever closer to discomfort."),
                  Text.literal("I wonder how much I can handle?")
            },
            MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE))
      ));
      
      ResearchTasks.register(FEATHER_FALL, new CustomResearchTask(
            FEATHER_FALL.getValue().getPath(),
            Text.literal("Make a Fall Hurt Less"),
            new Text[]{
                  Text.literal("I keep spraining my ankles doing parkour."),
                  Text.literal("I should find a way to make it hurt less.")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(FIND_SPAWNER, new CustomResearchTask(
            FIND_SPAWNER.getValue().getPath(),
            Text.literal("Discover a Dungeon"),
            new Text[]{
                  Text.literal("I've heard rumors of old dungeons in caves."),
                  Text.literal("I don't know what they are or what they do."),
                  Text.literal("I suppose it is time for some spelunking.")
            },
            new ItemStack(Items.SPAWNER)
      ));
      
      ResearchTasks.register(USE_SOUL_SPEED, new CustomResearchTask(
            USE_SOUL_SPEED.getValue().getPath(),
            Text.literal("Discover the Power of Souls"),
            new Text[]{
                  Text.literal("Surely soul sand doesn't actually contain souls?"),
                  Text.literal("If it did, how are they trapped? What do they do?"),
                  Text.literal("My curiosity is getting the better of me, I must know."),
                  Text.literal("If it is true, there must be a way to use or free them."),
                  Text.literal("Maybe some of the Nether's inhabitants know...")
            },
            new ItemStack(Items.GOLDEN_BOOTS)
      ));
      
      ResearchTasks.register(ACTIVATE_MENDING, new CustomResearchTask(
            ACTIVATE_MENDING.getValue().getPath(),
            Text.literal("Discover the Restorative Effects of Experience"),
            new Text[]{
                  Text.literal("Experience has proven its use for enchanting."),
                  Text.literal("Some old tomes suggest that it can do more."),
                  Text.literal("A specific enchantment that lets it bind to"),
                  Text.literal(" physical materials and undo wear and tear."),
                  Text.literal("I haven't discovered any such enchantment"),
                  Text.literal(" through use of my enchantment table."),
                  Text.literal("I should try and track down any truth to this.")
            },
            new ItemStack(Items.EXPERIENCE_BOTTLE), ADVANCEMENT_ENCHANT_ITEM
      ));
      
      ResearchTasks.register(OBTAIN_SILK_TOUCH, new CustomResearchTask(
            OBTAIN_SILK_TOUCH.getValue().getPath(),
            Text.literal("Obtain Silk Touch"),
            new Text[]{
                  Text.literal("The process of destroying and re-placing blocks"),
                  Text.literal(" has proven to be too clumsy to relocate some"),
                  Text.literal(" more delicate materials like glass and grass."),
                  Text.literal("Perhaps there is a way to be more careful when"),
                  Text.literal(" using my tools to break down delicate blocks.")
            },
            new ItemStack(Items.GOLDEN_PICKAXE)
      ));
      
      
      ResearchTasks.register(EFFECT_ABSORPTION, new EffectResearchTask(
            EFFECT_ABSORPTION.getValue().getPath(), StatusEffects.ABSORPTION,
            Text.literal("Experience Increased Constitution"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can boost my health?")
            },
            new ItemStack(Items.GOLDEN_APPLE)
      ));
      
      ResearchTasks.register(EFFECT_SWIFTNESS, new EffectResearchTask(
            EFFECT_SWIFTNESS.getValue().getPath(), StatusEffects.SPEED,
            Text.literal("Experience Swiftness"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can boost my speed?")
            },
            new ItemStack(Items.SUGAR)
      ));
      
      ResearchTasks.register(EFFECT_JUMP_BOOST, new EffectResearchTask(
            EFFECT_JUMP_BOOST.getValue().getPath(), StatusEffects.JUMP_BOOST,
            Text.literal("Experience Increased Agility"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can boost my jumps?")
            },
            new ItemStack(Items.RABBIT_FOOT)
      ));
      
      ResearchTasks.register(EFFECT_NIGHT_VISION, new EffectResearchTask(
            EFFECT_NIGHT_VISION.getValue().getPath(), StatusEffects.NIGHT_VISION,
            Text.literal("Experience Nocturnal Eyesight"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that lets me see better?")
            },
            new ItemStack(Items.GOLDEN_CARROT)
      ));
      
      ResearchTasks.register(EFFECT_SLOW_FALLING, new EffectResearchTask(
            EFFECT_SLOW_FALLING.getValue().getPath(), StatusEffects.SLOW_FALLING,
            Text.literal("Experience a Slow Descent"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can slow my fall?")
            },
            new ItemStack(Items.FEATHER)
      ));
      
      ResearchTasks.register(EFFECT_BLINDNESS, new EffectResearchTask(
            EFFECT_BLINDNESS.getValue().getPath(), StatusEffects.BLINDNESS,
            Text.literal("Experience Blindness"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can blind enemies?")
            },
            new ItemStack(Items.INK_SAC)
      ));
      
      ResearchTasks.register(EFFECT_SLOWNESS, new EffectResearchTask(
            EFFECT_SLOWNESS.getValue().getPath(), StatusEffects.SLOWNESS,
            Text.literal("Experience Sluggishness"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can slow enemies?")
            },
            new ItemStack(Items.COBWEB)
      ));
      
      ResearchTasks.register(EFFECT_FIRE_RESISTANCE, new EffectResearchTask(
            EFFECT_FIRE_RESISTANCE.getValue().getPath(), StatusEffects.FIRE_RESISTANCE,
            Text.literal("Experience Fire Resistance"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that helps with fire?")
            },
            new ItemStack(Items.MAGMA_CREAM)
      ));
      
      ResearchTasks.register(EFFECT_STRENGTH, new EffectResearchTask(
            EFFECT_STRENGTH.getValue().getPath(), StatusEffects.STRENGTH,
            Text.literal("Experience Increased Strength"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can make me stronger?")
            },
            new ItemStack(Items.IRON_SWORD)
      ));
      
      ResearchTasks.register(EFFECT_WEAKNESS, new EffectResearchTask(
            EFFECT_WEAKNESS.getValue().getPath(), StatusEffects.WEAKNESS,
            Text.literal("Experience Decreased Strength"),
            new Text[]{
                  Text.literal("I wonder if there's an effect that can weaken enemies?")
            },
            new ItemStack(Items.WOODEN_SWORD)
      ));
      
      
      
      
   }
   
   private static ResearchTask register(RegistryKey<ResearchTask> key, ResearchTask task){
      Registry.register(RESEARCH_TASKS,key,task);
      return task;
   }
   
   private static RegistryKey<ResearchTask> of(String id){
      return RegistryKey.of(RESEARCH_TASKS.getKey(),Identifier.of(MOD_ID,id));
   }
   
   public static List<TreeNode<ResearchTask>> buildTaskTrees(RegistryKey<ResearchTask>[] tasks){
      List<TreeNode<ResearchTask>> taskTrees = new ArrayList<>();
      Set<ResearchTask> addedTasks = new HashSet<>();
      
      for(RegistryKey<ResearchTask> key : tasks){
         ResearchTask task = RESEARCH_TASKS.get(key);
         if (task == null || addedTasks.contains(task)) continue;
         taskTrees.add(generateTaskNode(task,null, addedTasks));
      }
      return taskTrees;
   }
   
   public static Set<ResearchTask> getUniqueTasks(RegistryKey<ResearchTask>[] tasks){
      Set<ResearchTask> addedTasks = new HashSet<>();
      
      for(RegistryKey<ResearchTask> key : tasks){
         ResearchTask task = RESEARCH_TASKS.get(key);
         if (task == null || addedTasks.contains(task)) continue;
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
