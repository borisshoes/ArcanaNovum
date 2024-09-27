package net.borisshoes.arcananovum.recipes.transmutation;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.InstrumentTags;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TransmutationRecipes {
   public static final ArrayList<TransmutationRecipe> TRANSMUTATION_RECIPES = new ArrayList<>();
   
   static {
      // Commutative Recipes
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stones",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.COBBLESTONE),new ItemStack(Items.STONE), new ItemStack(Items.GRANITE), new ItemStack(Items.POLISHED_GRANITE), new ItemStack(Items.DIORITE), new ItemStack(Items.POLISHED_DIORITE),
            new ItemStack(Items.ANDESITE), new ItemStack(Items.POLISHED_ANDESITE), new ItemStack(Items.DEEPSLATE), new ItemStack(Items.COBBLED_DEEPSLATE), new ItemStack(Items.POLISHED_DEEPSLATE),
            new ItemStack(Items.DEEPSLATE_TILES), new ItemStack(Items.CRACKED_DEEPSLATE_TILES), new ItemStack(Items.CALCITE), new ItemStack(Items.TUFF), new ItemStack(Items.DRIPSTONE_BLOCK), new ItemStack(Items.SMOOTH_STONE),
            new ItemStack(Items.STONE_BRICKS), new ItemStack(Items.MOSSY_STONE_BRICKS), new ItemStack(Items.CRACKED_STONE_BRICKS), new ItemStack(Items.CHISELED_STONE_BRICKS), new ItemStack(Items.BLACKSTONE),
            new ItemStack(Items.POLISHED_BLACKSTONE), new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS), new ItemStack(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
      )),new ItemStack(Items.COAL,16),new ItemStack(Items.QUARTZ,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Copper",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.OXIDIZED_COPPER), new ItemStack(Items.WEATHERED_COPPER), new ItemStack(Items.EXPOSED_COPPER), new ItemStack(Items.COPPER_BLOCK),
            new ItemStack(Items.OXIDIZED_CUT_COPPER), new ItemStack(Items.WEATHERED_CUT_COPPER), new ItemStack(Items.EXPOSED_CUT_COPPER), new ItemStack(Items.CUT_COPPER),
            new ItemStack(Items.OXIDIZED_CUT_COPPER_STAIRS), new ItemStack(Items.WEATHERED_CUT_COPPER_STAIRS), new ItemStack(Items.EXPOSED_CUT_COPPER_STAIRS), new ItemStack(Items.CUT_COPPER_STAIRS),
            new ItemStack(Items.OXIDIZED_CHISELED_COPPER), new ItemStack(Items.WEATHERED_CHISELED_COPPER), new ItemStack(Items.EXPOSED_CHISELED_COPPER), new ItemStack(Items.CHISELED_COPPER),
            new ItemStack(Items.OXIDIZED_COPPER_GRATE), new ItemStack(Items.WEATHERED_COPPER_GRATE), new ItemStack(Items.EXPOSED_COPPER_GRATE), new ItemStack(Items.COPPER_GRATE)
      )),new ItemStack(Items.REDSTONE,36),new ItemStack(Items.AMETHYST_SHARD,24)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Logs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_LOG),new ItemStack(Items.BAMBOO_BLOCK),new ItemStack(Items.BIRCH_LOG),new ItemStack(Items.CHERRY_LOG),
            new ItemStack(Items.CRIMSON_STEM),new ItemStack(Items.DARK_OAK_LOG),new ItemStack(Items.JUNGLE_LOG),new ItemStack(Items.MANGROVE_LOG),
            new ItemStack(Items.OAK_LOG),new ItemStack(Items.SPRUCE_LOG),new ItemStack(Items.WARPED_STEM),
            new ItemStack(Items.STRIPPED_ACACIA_LOG),new ItemStack(Items.STRIPPED_BAMBOO_BLOCK),new ItemStack(Items.STRIPPED_BIRCH_LOG),new ItemStack(Items.STRIPPED_CHERRY_LOG),
            new ItemStack(Items.STRIPPED_CRIMSON_STEM),new ItemStack(Items.STRIPPED_DARK_OAK_LOG),new ItemStack(Items.STRIPPED_JUNGLE_LOG),new ItemStack(Items.STRIPPED_MANGROVE_LOG),
            new ItemStack(Items.STRIPPED_OAK_LOG),new ItemStack(Items.STRIPPED_SPRUCE_LOG),new ItemStack(Items.STRIPPED_WARPED_STEM)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wood",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_WOOD),new ItemStack(Items.BAMBOO_BLOCK),new ItemStack(Items.BIRCH_WOOD),new ItemStack(Items.CHERRY_WOOD),
            new ItemStack(Items.CRIMSON_HYPHAE),new ItemStack(Items.DARK_OAK_WOOD),new ItemStack(Items.JUNGLE_WOOD),new ItemStack(Items.MANGROVE_WOOD),
            new ItemStack(Items.OAK_WOOD),new ItemStack(Items.SPRUCE_WOOD),new ItemStack(Items.WARPED_HYPHAE),
            new ItemStack(Items.STRIPPED_ACACIA_WOOD),new ItemStack(Items.STRIPPED_BAMBOO_BLOCK),new ItemStack(Items.STRIPPED_BIRCH_WOOD),new ItemStack(Items.STRIPPED_CHERRY_WOOD),
            new ItemStack(Items.STRIPPED_CRIMSON_HYPHAE),new ItemStack(Items.STRIPPED_DARK_OAK_WOOD),new ItemStack(Items.STRIPPED_JUNGLE_WOOD),new ItemStack(Items.STRIPPED_MANGROVE_WOOD),
            new ItemStack(Items.STRIPPED_OAK_WOOD),new ItemStack(Items.STRIPPED_SPRUCE_WOOD),new ItemStack(Items.STRIPPED_WARPED_HYPHAE)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Planks",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_PLANKS),new ItemStack(Items.BAMBOO_PLANKS),new ItemStack(Items.BIRCH_PLANKS),new ItemStack(Items.CHERRY_PLANKS),
            new ItemStack(Items.CRIMSON_PLANKS),new ItemStack(Items.DARK_OAK_PLANKS),new ItemStack(Items.JUNGLE_PLANKS),new ItemStack(Items.MANGROVE_PLANKS),
            new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.SPRUCE_PLANKS),new ItemStack(Items.WARPED_PLANKS)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stairs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_STAIRS),new ItemStack(Items.BAMBOO_STAIRS),new ItemStack(Items.BIRCH_STAIRS),new ItemStack(Items.CHERRY_STAIRS),
            new ItemStack(Items.CRIMSON_STAIRS),new ItemStack(Items.DARK_OAK_STAIRS),new ItemStack(Items.JUNGLE_STAIRS),new ItemStack(Items.MANGROVE_STAIRS),
            new ItemStack(Items.OAK_STAIRS),new ItemStack(Items.SPRUCE_STAIRS),new ItemStack(Items.WARPED_STAIRS)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Slabs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_SLAB),new ItemStack(Items.BAMBOO_SLAB),new ItemStack(Items.BIRCH_SLAB),new ItemStack(Items.CHERRY_SLAB),
            new ItemStack(Items.CRIMSON_SLAB),new ItemStack(Items.DARK_OAK_SLAB),new ItemStack(Items.JUNGLE_SLAB),new ItemStack(Items.MANGROVE_SLAB),
            new ItemStack(Items.OAK_SLAB),new ItemStack(Items.SPRUCE_SLAB),new ItemStack(Items.WARPED_SLAB)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fences",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_FENCE),new ItemStack(Items.BAMBOO_FENCE),new ItemStack(Items.BIRCH_FENCE),new ItemStack(Items.CHERRY_FENCE),
            new ItemStack(Items.CRIMSON_FENCE),new ItemStack(Items.DARK_OAK_FENCE),new ItemStack(Items.JUNGLE_FENCE),new ItemStack(Items.MANGROVE_FENCE),
            new ItemStack(Items.OAK_FENCE),new ItemStack(Items.SPRUCE_FENCE),new ItemStack(Items.WARPED_FENCE)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fence Gates",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_FENCE_GATE),new ItemStack(Items.BAMBOO_FENCE_GATE),new ItemStack(Items.BIRCH_FENCE_GATE),new ItemStack(Items.CHERRY_FENCE_GATE),
            new ItemStack(Items.CRIMSON_FENCE_GATE),new ItemStack(Items.DARK_OAK_FENCE_GATE),new ItemStack(Items.JUNGLE_FENCE_GATE),new ItemStack(Items.MANGROVE_FENCE_GATE),
            new ItemStack(Items.OAK_FENCE_GATE),new ItemStack(Items.SPRUCE_FENCE_GATE),new ItemStack(Items.WARPED_FENCE_GATE)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Signs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_SIGN),new ItemStack(Items.BAMBOO_SIGN),new ItemStack(Items.BIRCH_SIGN),new ItemStack(Items.CHERRY_SIGN),
            new ItemStack(Items.CRIMSON_SIGN),new ItemStack(Items.DARK_OAK_SIGN),new ItemStack(Items.JUNGLE_SIGN),new ItemStack(Items.MANGROVE_SIGN),
            new ItemStack(Items.OAK_SIGN),new ItemStack(Items.SPRUCE_SIGN),new ItemStack(Items.WARPED_SIGN)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Hanging Signs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_HANGING_SIGN),new ItemStack(Items.BAMBOO_HANGING_SIGN),new ItemStack(Items.BIRCH_HANGING_SIGN),new ItemStack(Items.CHERRY_HANGING_SIGN),
            new ItemStack(Items.CRIMSON_HANGING_SIGN),new ItemStack(Items.DARK_OAK_HANGING_SIGN),new ItemStack(Items.JUNGLE_HANGING_SIGN),new ItemStack(Items.MANGROVE_HANGING_SIGN),
            new ItemStack(Items.OAK_HANGING_SIGN),new ItemStack(Items.SPRUCE_HANGING_SIGN),new ItemStack(Items.WARPED_HANGING_SIGN)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Doors",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_DOOR),new ItemStack(Items.BAMBOO_DOOR),new ItemStack(Items.BIRCH_DOOR),new ItemStack(Items.CHERRY_DOOR),
            new ItemStack(Items.CRIMSON_DOOR),new ItemStack(Items.DARK_OAK_DOOR),new ItemStack(Items.JUNGLE_DOOR),new ItemStack(Items.MANGROVE_DOOR),
            new ItemStack(Items.OAK_DOOR),new ItemStack(Items.SPRUCE_DOOR),new ItemStack(Items.WARPED_DOOR)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Trapdoors",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_TRAPDOOR),new ItemStack(Items.BAMBOO_TRAPDOOR),new ItemStack(Items.BIRCH_TRAPDOOR),new ItemStack(Items.CHERRY_TRAPDOOR),
            new ItemStack(Items.CRIMSON_TRAPDOOR),new ItemStack(Items.DARK_OAK_TRAPDOOR),new ItemStack(Items.JUNGLE_TRAPDOOR),new ItemStack(Items.MANGROVE_TRAPDOOR),
            new ItemStack(Items.OAK_TRAPDOOR),new ItemStack(Items.SPRUCE_TRAPDOOR),new ItemStack(Items.WARPED_TRAPDOOR)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pressure Plates",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_PRESSURE_PLATE),new ItemStack(Items.BAMBOO_PRESSURE_PLATE),new ItemStack(Items.BIRCH_PRESSURE_PLATE),new ItemStack(Items.CHERRY_PRESSURE_PLATE),
            new ItemStack(Items.CRIMSON_PRESSURE_PLATE),new ItemStack(Items.DARK_OAK_PRESSURE_PLATE),new ItemStack(Items.JUNGLE_PRESSURE_PLATE),new ItemStack(Items.MANGROVE_PRESSURE_PLATE),
            new ItemStack(Items.OAK_PRESSURE_PLATE),new ItemStack(Items.SPRUCE_PRESSURE_PLATE),new ItemStack(Items.WARPED_PRESSURE_PLATE)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Buttons",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_BUTTON),new ItemStack(Items.BAMBOO_BUTTON),new ItemStack(Items.BIRCH_BUTTON),new ItemStack(Items.CHERRY_BUTTON),
            new ItemStack(Items.CRIMSON_BUTTON),new ItemStack(Items.DARK_OAK_BUTTON),new ItemStack(Items.JUNGLE_BUTTON),new ItemStack(Items.MANGROVE_BUTTON),
            new ItemStack(Items.OAK_BUTTON),new ItemStack(Items.SPRUCE_BUTTON),new ItemStack(Items.WARPED_BUTTON)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Boats",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_BOAT),new ItemStack(Items.BAMBOO_RAFT),new ItemStack(Items.BIRCH_BOAT),new ItemStack(Items.CHERRY_BOAT),
            new ItemStack(Items.DARK_OAK_BOAT),new ItemStack(Items.JUNGLE_BOAT),new ItemStack(Items.MANGROVE_BOAT),
            new ItemStack(Items.OAK_BOAT),new ItemStack(Items.SPRUCE_BOAT)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Chest boats",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_CHEST_BOAT),new ItemStack(Items.BAMBOO_CHEST_RAFT),new ItemStack(Items.BIRCH_CHEST_BOAT),new ItemStack(Items.CHERRY_CHEST_BOAT),
            new ItemStack(Items.DARK_OAK_CHEST_BOAT),new ItemStack(Items.JUNGLE_CHEST_BOAT),new ItemStack(Items.MANGROVE_CHEST_BOAT),
            new ItemStack(Items.OAK_CHEST_BOAT),new ItemStack(Items.SPRUCE_CHEST_BOAT)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Seeds",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.BEETROOT_SEEDS), new ItemStack(Items.TORCHFLOWER_SEEDS),
            new ItemStack(Items.PITCHER_POD), new ItemStack(Items.POTATO), new ItemStack(Items.CARROT)
      )),new ItemStack(Items.LAPIS_LAZULI,16),new ItemStack(Items.EMERALD,24)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Leaves",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.OAK_LEAVES), new ItemStack(Items.SPRUCE_LEAVES), new ItemStack(Items.BIRCH_LEAVES), new ItemStack(Items.JUNGLE_LEAVES), new ItemStack(Items.ACACIA_LEAVES),
            new ItemStack(Items.CHERRY_LEAVES), new ItemStack(Items.DARK_OAK_LEAVES), new ItemStack(Items.MANGROVE_LEAVES), new ItemStack(Items.AZALEA_LEAVES), new ItemStack(Items.FLOWERING_AZALEA_LEAVES)
      )),new ItemStack(Items.LAPIS_LAZULI,16),new ItemStack(Items.EMERALD,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Flowers",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.PEONY), new ItemStack(Items.ROSE_BUSH), new ItemStack(Items.LILAC), new ItemStack(Items.SUNFLOWER), new ItemStack(Items.AZURE_BLUET),
            new ItemStack(Items.OXEYE_DAISY), new ItemStack(Items.CORNFLOWER), new ItemStack(Items.WITHER_ROSE), new ItemStack(Items.TORCHFLOWER), new ItemStack(Items.PITCHER_PLANT),
            new ItemStack(Items.PINK_PETALS), new ItemStack(Items.DANDELION), new ItemStack(Items.POPPY), new ItemStack(Items.BLUE_ORCHID), new ItemStack(Items.ALLIUM),
            new ItemStack(Items.RED_TULIP), new ItemStack(Items.ORANGE_TULIP), new ItemStack(Items.WHITE_TULIP), new ItemStack(Items.PINK_TULIP), new ItemStack(Items.SPORE_BLOSSOM),
            new ItemStack(Items.FLOWERING_AZALEA), new ItemStack(Items.CHORUS_FLOWER)
      )),new ItemStack(Items.GLOWSTONE_DUST,16),new ItemStack(Items.LAPIS_LAZULI,32)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Froglights",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.OCHRE_FROGLIGHT), new ItemStack(Items.PEARLESCENT_FROGLIGHT), new ItemStack(Items.VERDANT_FROGLIGHT)
      )),new ItemStack(Items.REDSTONE,48),new ItemStack(Items.EMERALD,48)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fish",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.COD), new ItemStack(Items.SALMON), new ItemStack(Items.TROPICAL_FISH), new ItemStack(Items.PUFFERFISH)
      )),new ItemStack(Items.REDSTONE,16),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Coral",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.TUBE_CORAL_BLOCK), new ItemStack(Items.BRAIN_CORAL_BLOCK), new ItemStack(Items.BUBBLE_CORAL_BLOCK), new ItemStack(Items.FIRE_CORAL_BLOCK), new ItemStack(Items.HORN_CORAL_BLOCK),
            new ItemStack(Items.TUBE_CORAL), new ItemStack(Items.BRAIN_CORAL), new ItemStack(Items.BUBBLE_CORAL), new ItemStack(Items.FIRE_CORAL), new ItemStack(Items.HORN_CORAL),
            new ItemStack(Items.TUBE_CORAL_FAN), new ItemStack(Items.BRAIN_CORAL_FAN), new ItemStack(Items.BUBBLE_CORAL_FAN), new ItemStack(Items.FIRE_CORAL_FAN), new ItemStack(Items.HORN_CORAL_FAN),
            new ItemStack(Items.DEAD_TUBE_CORAL_BLOCK), new ItemStack(Items.DEAD_BRAIN_CORAL_BLOCK), new ItemStack(Items.DEAD_BUBBLE_CORAL_BLOCK), new ItemStack(Items.DEAD_FIRE_CORAL_BLOCK), new ItemStack(Items.DEAD_HORN_CORAL_BLOCK),
            new ItemStack(Items.DEAD_TUBE_CORAL), new ItemStack(Items.DEAD_BRAIN_CORAL), new ItemStack(Items.DEAD_BUBBLE_CORAL), new ItemStack(Items.DEAD_FIRE_CORAL), new ItemStack(Items.DEAD_HORN_CORAL),
            new ItemStack(Items.DEAD_TUBE_CORAL_FAN), new ItemStack(Items.DEAD_BRAIN_CORAL_FAN), new ItemStack(Items.DEAD_BUBBLE_CORAL_FAN), new ItemStack(Items.DEAD_FIRE_CORAL_FAN), new ItemStack(Items.DEAD_HORN_CORAL_FAN)
      )),new ItemStack(Items.COPPER_INGOT,32),new ItemStack(Items.LAPIS_LAZULI,32)));

      // TODO: These need to be moved to something that can access the world registry as instrument is now a dynamic registry
//      ArrayList<ItemStack> goatHorns = new ArrayList<>();
//      for(RegistryEntry<Instrument> instrument : Registries.INSTRUMENT.getOrCreateEntryList(InstrumentTags.REGULAR_GOAT_HORNS)){
//         goatHorns.add(GoatHornItem.getStackForInstrument(Items.GOAT_HORN, instrument));
//      }
//      for(RegistryEntry<Instrument> instrument : Registries.INSTRUMENT.getOrCreateEntryList(InstrumentTags.SCREAMING_GOAT_HORNS)){
//         goatHorns.add(GoatHornItem.getStackForInstrument(Items.GOAT_HORN, instrument));
//      }
//      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Goat Horns",goatHorns,new ItemStack(Items.DIAMOND,4),new ItemStack(Items.AMETHYST_SHARD,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Banner Patterns",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.CREEPER_BANNER_PATTERN), new ItemStack(Items.FLOWER_BANNER_PATTERN), new ItemStack(Items.MOJANG_BANNER_PATTERN), new ItemStack(Items.PIGLIN_BANNER_PATTERN), new ItemStack(Items.GLOBE_BANNER_PATTERN),
            new ItemStack(Items.SKULL_BANNER_PATTERN)
      )),new ItemStack(Items.REDSTONE,48),new ItemStack(Items.QUARTZ,24)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Heads",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.SKELETON_SKULL), new ItemStack(Items.WITHER_SKELETON_SKULL), new ItemStack(Items.ZOMBIE_HEAD), new ItemStack(Items.CREEPER_HEAD), new ItemStack(Items.DRAGON_HEAD),
            new ItemStack(Items.PIGLIN_HEAD)
      )),new ItemStack(Items.NETHERITE_SCRAP,4),new ItemStack(Items.REDSTONE,64)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Smithing Templates",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE)
      )),new ItemStack(Items.DIAMOND,16),new ItemStack(Items.LAPIS_LAZULI,64)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pottery Sherds",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.SNORT_POTTERY_SHERD), new ItemStack(Items.SKULL_POTTERY_SHERD), new ItemStack(Items.HEARTBREAK_POTTERY_SHERD), new ItemStack(Items.HOWL_POTTERY_SHERD), new ItemStack(Items.MINER_POTTERY_SHERD),
            new ItemStack(Items.MOURNER_POTTERY_SHERD), new ItemStack(Items.PLENTY_POTTERY_SHERD), new ItemStack(Items.PRIZE_POTTERY_SHERD), new ItemStack(Items.SHEAF_POTTERY_SHERD), new ItemStack(Items.SHELTER_POTTERY_SHERD),
            new ItemStack(Items.FRIEND_POTTERY_SHERD), new ItemStack(Items.EXPLORER_POTTERY_SHERD), new ItemStack(Items.DANGER_POTTERY_SHERD), new ItemStack(Items.BURN_POTTERY_SHERD), new ItemStack(Items.BREWER_POTTERY_SHERD),
            new ItemStack(Items.BLADE_POTTERY_SHERD), new ItemStack(Items.ARMS_UP_POTTERY_SHERD), new ItemStack(Items.ARCHER_POTTERY_SHERD), new ItemStack(Items.ANGLER_POTTERY_SHERD), new ItemStack(Items.HEART_POTTERY_SHERD)
      )),new ItemStack(Items.AMETHYST_SHARD,36),new ItemStack(Items.LAPIS_LAZULI,24)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Music Discs",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.MUSIC_DISC_13), new ItemStack(Items.MUSIC_DISC_CAT), new ItemStack(Items.MUSIC_DISC_BLOCKS), new ItemStack(Items.MUSIC_DISC_CHIRP), new ItemStack(Items.MUSIC_DISC_FAR),
            new ItemStack(Items.MUSIC_DISC_MALL), new ItemStack(Items.MUSIC_DISC_MELLOHI), new ItemStack(Items.MUSIC_DISC_STAL), new ItemStack(Items.MUSIC_DISC_STRAD), new ItemStack(Items.MUSIC_DISC_WARD),
            new ItemStack(Items.MUSIC_DISC_11), new ItemStack(Items.MUSIC_DISC_WAIT), new ItemStack(Items.MUSIC_DISC_PIGSTEP), new ItemStack(Items.MUSIC_DISC_OTHERSIDE), new ItemStack(Items.MUSIC_DISC_5),
            new ItemStack(Items.MUSIC_DISC_RELIC), new ItemStack(Items.MUSIC_DISC_CREATOR), new ItemStack(Items.MUSIC_DISC_CREATOR_MUSIC_BOX), new ItemStack(Items.MUSIC_DISC_PRECIPICE)
      )),new ItemStack(Items.DIAMOND,12),new ItemStack(Items.AMETHYST_SHARD,36)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Dyes",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_DYE),new ItemStack(Items.ORANGE_DYE),new ItemStack(Items.MAGENTA_DYE),new ItemStack(Items.LIGHT_BLUE_DYE),
            new ItemStack(Items.RED_DYE),new ItemStack(Items.YELLOW_DYE),new ItemStack(Items.LIME_DYE),new ItemStack(Items.GREEN_DYE),
            new ItemStack(Items.BLUE_DYE),new ItemStack(Items.CYAN_DYE),new ItemStack(Items.PINK_DYE),new ItemStack(Items.PURPLE_DYE),
            new ItemStack(Items.BROWN_DYE),new ItemStack(Items.BLACK_DYE),new ItemStack(Items.GRAY_DYE),new ItemStack(Items.LIGHT_GRAY_DYE)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Terracotta",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_TERRACOTTA),new ItemStack(Items.ORANGE_TERRACOTTA),new ItemStack(Items.MAGENTA_TERRACOTTA),new ItemStack(Items.LIGHT_BLUE_TERRACOTTA),
            new ItemStack(Items.RED_TERRACOTTA),new ItemStack(Items.YELLOW_TERRACOTTA),new ItemStack(Items.LIME_TERRACOTTA),new ItemStack(Items.GREEN_TERRACOTTA),
            new ItemStack(Items.BLUE_TERRACOTTA),new ItemStack(Items.CYAN_TERRACOTTA),new ItemStack(Items.PINK_TERRACOTTA),new ItemStack(Items.PURPLE_TERRACOTTA),
            new ItemStack(Items.BROWN_TERRACOTTA),new ItemStack(Items.BLACK_TERRACOTTA),new ItemStack(Items.GRAY_TERRACOTTA),new ItemStack(Items.LIGHT_GRAY_TERRACOTTA), new ItemStack(Items.TERRACOTTA)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wool",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_WOOL),new ItemStack(Items.ORANGE_WOOL),new ItemStack(Items.MAGENTA_WOOL),new ItemStack(Items.LIGHT_BLUE_WOOL),
            new ItemStack(Items.RED_WOOL),new ItemStack(Items.YELLOW_WOOL),new ItemStack(Items.LIME_WOOL),new ItemStack(Items.GREEN_WOOL),
            new ItemStack(Items.BLUE_WOOL),new ItemStack(Items.CYAN_WOOL),new ItemStack(Items.PINK_WOOL),new ItemStack(Items.PURPLE_WOOL),
            new ItemStack(Items.BROWN_WOOL),new ItemStack(Items.BLACK_WOOL),new ItemStack(Items.GRAY_WOOL),new ItemStack(Items.LIGHT_GRAY_WOOL)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Carpet",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_CARPET),new ItemStack(Items.ORANGE_CARPET),new ItemStack(Items.MAGENTA_CARPET),new ItemStack(Items.LIGHT_BLUE_CARPET),
            new ItemStack(Items.RED_CARPET),new ItemStack(Items.YELLOW_CARPET),new ItemStack(Items.LIME_CARPET),new ItemStack(Items.GREEN_CARPET),
            new ItemStack(Items.BLUE_CARPET),new ItemStack(Items.CYAN_CARPET),new ItemStack(Items.PINK_CARPET),new ItemStack(Items.PURPLE_CARPET),
            new ItemStack(Items.BROWN_CARPET),new ItemStack(Items.BLACK_CARPET),new ItemStack(Items.GRAY_CARPET),new ItemStack(Items.LIGHT_GRAY_CARPET)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glass",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_STAINED_GLASS),new ItemStack(Items.ORANGE_STAINED_GLASS),new ItemStack(Items.MAGENTA_STAINED_GLASS),new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS),
            new ItemStack(Items.RED_STAINED_GLASS),new ItemStack(Items.YELLOW_STAINED_GLASS),new ItemStack(Items.LIME_STAINED_GLASS),new ItemStack(Items.GREEN_STAINED_GLASS),
            new ItemStack(Items.BLUE_STAINED_GLASS),new ItemStack(Items.CYAN_STAINED_GLASS),new ItemStack(Items.PINK_STAINED_GLASS),new ItemStack(Items.PURPLE_STAINED_GLASS),
            new ItemStack(Items.BROWN_STAINED_GLASS),new ItemStack(Items.BLACK_STAINED_GLASS),new ItemStack(Items.GRAY_STAINED_GLASS),new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS), new ItemStack(Items.GLASS)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glass Panes",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_STAINED_GLASS_PANE),new ItemStack(Items.ORANGE_STAINED_GLASS_PANE),new ItemStack(Items.MAGENTA_STAINED_GLASS_PANE),new ItemStack(Items.LIGHT_BLUE_STAINED_GLASS_PANE),
            new ItemStack(Items.RED_STAINED_GLASS_PANE),new ItemStack(Items.YELLOW_STAINED_GLASS_PANE),new ItemStack(Items.LIME_STAINED_GLASS_PANE),new ItemStack(Items.GREEN_STAINED_GLASS_PANE),
            new ItemStack(Items.BLUE_STAINED_GLASS_PANE),new ItemStack(Items.CYAN_STAINED_GLASS_PANE),new ItemStack(Items.PINK_STAINED_GLASS_PANE),new ItemStack(Items.PURPLE_STAINED_GLASS_PANE),
            new ItemStack(Items.BROWN_STAINED_GLASS_PANE),new ItemStack(Items.BLACK_STAINED_GLASS_PANE),new ItemStack(Items.GRAY_STAINED_GLASS_PANE),new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE), new ItemStack(Items.GLASS_PANE)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Candles",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_CANDLE),new ItemStack(Items.ORANGE_CANDLE),new ItemStack(Items.MAGENTA_CANDLE),new ItemStack(Items.LIGHT_BLUE_CANDLE),
            new ItemStack(Items.RED_CANDLE),new ItemStack(Items.YELLOW_CANDLE),new ItemStack(Items.LIME_CANDLE),new ItemStack(Items.GREEN_CANDLE),
            new ItemStack(Items.BLUE_CANDLE),new ItemStack(Items.CYAN_CANDLE),new ItemStack(Items.PINK_CANDLE),new ItemStack(Items.PURPLE_CANDLE),
            new ItemStack(Items.BROWN_CANDLE),new ItemStack(Items.BLACK_CANDLE),new ItemStack(Items.GRAY_CANDLE),new ItemStack(Items.LIGHT_GRAY_CANDLE)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glazed Terracotta",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_GLAZED_TERRACOTTA),new ItemStack(Items.ORANGE_GLAZED_TERRACOTTA),new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA),new ItemStack(Items.LIGHT_BLUE_GLAZED_TERRACOTTA),
            new ItemStack(Items.RED_GLAZED_TERRACOTTA),new ItemStack(Items.YELLOW_GLAZED_TERRACOTTA),new ItemStack(Items.LIME_GLAZED_TERRACOTTA),new ItemStack(Items.GREEN_GLAZED_TERRACOTTA),
            new ItemStack(Items.BLUE_GLAZED_TERRACOTTA),new ItemStack(Items.CYAN_GLAZED_TERRACOTTA),new ItemStack(Items.PINK_GLAZED_TERRACOTTA),new ItemStack(Items.PURPLE_GLAZED_TERRACOTTA),
            new ItemStack(Items.BROWN_GLAZED_TERRACOTTA),new ItemStack(Items.BLACK_GLAZED_TERRACOTTA),new ItemStack(Items.GRAY_GLAZED_TERRACOTTA),new ItemStack(Items.LIGHT_GRAY_GLAZED_TERRACOTTA)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Concrete",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_CONCRETE),new ItemStack(Items.ORANGE_CONCRETE),new ItemStack(Items.MAGENTA_CONCRETE),new ItemStack(Items.LIGHT_BLUE_CONCRETE),
            new ItemStack(Items.RED_CONCRETE),new ItemStack(Items.YELLOW_CONCRETE),new ItemStack(Items.LIME_CONCRETE),new ItemStack(Items.GREEN_CONCRETE),
            new ItemStack(Items.BLUE_CONCRETE),new ItemStack(Items.CYAN_CONCRETE),new ItemStack(Items.PINK_CONCRETE),new ItemStack(Items.PURPLE_CONCRETE),
            new ItemStack(Items.BROWN_CONCRETE),new ItemStack(Items.BLACK_CONCRETE),new ItemStack(Items.GRAY_CONCRETE),new ItemStack(Items.LIGHT_GRAY_CONCRETE)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Gravity Blocks",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_CONCRETE_POWDER),new ItemStack(Items.ORANGE_CONCRETE_POWDER),new ItemStack(Items.MAGENTA_CONCRETE_POWDER),new ItemStack(Items.LIGHT_BLUE_CONCRETE_POWDER),
            new ItemStack(Items.RED_CONCRETE_POWDER),new ItemStack(Items.YELLOW_CONCRETE_POWDER),new ItemStack(Items.LIME_CONCRETE_POWDER),new ItemStack(Items.GREEN_CONCRETE_POWDER),
            new ItemStack(Items.BLUE_CONCRETE_POWDER),new ItemStack(Items.CYAN_CONCRETE_POWDER),new ItemStack(Items.PINK_CONCRETE_POWDER),new ItemStack(Items.PURPLE_CONCRETE_POWDER),
            new ItemStack(Items.BROWN_CONCRETE_POWDER),new ItemStack(Items.BLACK_CONCRETE_POWDER),new ItemStack(Items.GRAY_CONCRETE_POWDER),new ItemStack(Items.LIGHT_GRAY_CONCRETE_POWDER),
            new ItemStack(Items.GRAVEL), new ItemStack(Items.SAND), new ItemStack(Items.RED_SAND)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Banners",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_BANNER),new ItemStack(Items.ORANGE_BANNER),new ItemStack(Items.MAGENTA_BANNER),new ItemStack(Items.LIGHT_BLUE_BANNER),
            new ItemStack(Items.RED_BANNER),new ItemStack(Items.YELLOW_BANNER),new ItemStack(Items.LIME_BANNER),new ItemStack(Items.GREEN_BANNER),
            new ItemStack(Items.BLUE_BANNER),new ItemStack(Items.CYAN_BANNER),new ItemStack(Items.PINK_BANNER),new ItemStack(Items.PURPLE_BANNER),
            new ItemStack(Items.BROWN_BANNER),new ItemStack(Items.BLACK_BANNER),new ItemStack(Items.GRAY_BANNER),new ItemStack(Items.LIGHT_GRAY_BANNER)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Beds",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHITE_BED),new ItemStack(Items.ORANGE_BED),new ItemStack(Items.MAGENTA_BED),new ItemStack(Items.LIGHT_BLUE_BED),
            new ItemStack(Items.RED_BED),new ItemStack(Items.YELLOW_BED),new ItemStack(Items.LIME_BED),new ItemStack(Items.GREEN_BED),
            new ItemStack(Items.BLUE_BED),new ItemStack(Items.CYAN_BED),new ItemStack(Items.PINK_BED),new ItemStack(Items.PURPLE_BED),
            new ItemStack(Items.BROWN_BED),new ItemStack(Items.BLACK_BED),new ItemStack(Items.GRAY_BED),new ItemStack(Items.LIGHT_GRAY_BED)
      )),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      
      // Infusion Recipes
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Cream Reduction",new ItemStack(Items.MAGMA_CREAM,1),new ItemStack(Items.SLIME_BALL,1),new ItemStack(Items.CHARCOAL,16),new ItemStack(Items.SUGAR,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Honeycomb Decomposition",new ItemStack(Items.HONEYCOMB_BLOCK,1),new ItemStack(Items.HONEYCOMB,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Amethyst Decomposition",new ItemStack(Items.AMETHYST_BLOCK,1),new ItemStack(Items.AMETHYST_SHARD,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Prismarine Decomposition",new ItemStack(Items.PRISMARINE,1),new ItemStack(Items.PRISMARINE_SHARD,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Prismarine Brick Decomposition",new ItemStack(Items.PRISMARINE_BRICKS,1),new ItemStack(Items.PRISMARINE_SHARD,9),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Quartz Decomposition",new ItemStack(Items.QUARTZ_BLOCK,1),new ItemStack(Items.QUARTZ,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Glowstone Decomposition",new ItemStack(Items.GLOWSTONE,1),new ItemStack(Items.GLOWSTONE_DUST,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Magma Decomposition",new ItemStack(Items.MAGMA_BLOCK,1),new ItemStack(Items.MAGMA_CREAM,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Nether Wart Decomposition",new ItemStack(Items.NETHER_WART_BLOCK,1),new ItemStack(Items.NETHER_WART,9),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Blaze Rod Decomposition",new ItemStack(Items.BLAZE_ROD,1),new ItemStack(Items.BLAZE_POWDER,6),new ItemStack(Items.GUNPOWDER,24),new ItemStack(ArcanaRegistry.STARDUST,8)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Obsidian Infusion",new ItemStack(Items.MAGMA_BLOCK,4),new ItemStack(Items.OBSIDIAN,1),new ItemStack(Items.ICE,12),new ItemStack(Items.REDSTONE,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Crying Obsidian Infusion",new ItemStack(Items.OBSIDIAN,1),new ItemStack(Items.CRYING_OBSIDIAN,1),new ItemStack(Items.REDSTONE,16),new ItemStack(Items.GLOWSTONE_DUST,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Glowstone Infusion",new ItemStack(Items.REDSTONE,4),new ItemStack(Items.GLOWSTONE_DUST,1),new ItemStack(Items.REDSTONE,16),new ItemStack(Items.QUARTZ,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Ender Infusion",new ItemStack(Items.ENDER_PEARL,1),new ItemStack(Items.ENDER_EYE,2),new ItemStack(Items.GLOWSTONE_DUST,12),new ItemStack(Items.BLAZE_POWDER,24)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Refined Flesh",new ItemStack(Items.ROTTEN_FLESH,4),new ItemStack(Items.LEATHER,1),new ItemStack(Items.REDSTONE,8),new ItemStack(Items.SUGAR,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Carbon Compression",new ItemStack(Items.COAL,12),new ItemStack(Items.DIAMOND,1),new ItemStack(Items.REDSTONE,16),new ItemStack(Items.GUNPOWDER,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Refined Netherite",new ItemStack(Items.ANCIENT_DEBRIS,3),new ItemStack(Items.NETHERITE_INGOT,1),new ItemStack(Items.GOLD_INGOT,16),new ItemStack(Items.GLOWSTONE_DUST,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Condensed String",new ItemStack(Items.STRING,16),new ItemStack(Items.COBWEB,1),new ItemStack(Items.SLIME_BLOCK,4),new ItemStack(Items.HONEY_BLOCK,4)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Key Ominousification",new ItemStack(Items.TRIAL_KEY,1),new ItemStack(Items.OMINOUS_TRIAL_KEY,1),new ItemStack(Items.OMINOUS_BOTTLE,1),new ItemStack(Items.DIAMOND,8)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Liquid Experience",new ItemStack(Items.SCULK,64),new ItemStack(Items.EXPERIENCE_BOTTLE,16),new ItemStack(Items.GLASS_BOTTLE,16),new ItemStack(Items.LAPIS_LAZULI,32)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Sculk Infusion",new ItemStack(Items.SCULK_VEIN,4),new ItemStack(Items.SCULK,1),new ItemStack(Items.GUNPOWDER,16),new ItemStack(Items.DIAMOND,4)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Sculk Shard Infusion",new ItemStack(Items.SCULK,4),new ItemStack(Items.ECHO_SHARD,1),new ItemStack(Items.AMETHYST_SHARD,36),new ItemStack(Items.DIAMOND,12)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Divine Paper Infusion",new ItemStack(ArcanaRegistry.SOVEREIGN_ARCANE_PAPER,2),new ItemStack(ArcanaRegistry.DIVINE_ARCANE_PAPER,1),ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(),new ItemStack(Items.DIAMOND,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Nautilus Synthesis",new ItemStack(Items.TURTLE_SCUTE,4),new ItemStack(Items.NAUTILUS_SHELL,1),new ItemStack(Items.PRISMARINE_CRYSTALS,16),new ItemStack(Items.QUARTZ,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Oceanic Heart Synthesis",new ItemStack(Items.NAUTILUS_SHELL,8),new ItemStack(Items.HEART_OF_THE_SEA,1),new ItemStack(Items.PRISMARINE_CRYSTALS,32),new ItemStack(Items.QUARTZ,32)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Trident Synthesis",new ItemStack(Items.DIAMOND_SWORD,1),new ItemStack(Items.TRIDENT,1),new ItemStack(Items.PRISMARINE_CRYSTALS,16),new ItemStack(Items.HEART_OF_THE_SEA,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Resonator Synthesis",new ItemStack(Items.GOLD_BLOCK,1),new ItemStack(Items.BELL,1),new ItemStack(Items.QUARTZ,16),new ItemStack(Items.AMETHYST_SHARD,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Gravity Core Synthesis",new ItemStack(Items.NETHERITE_BLOCK,1),new ItemStack(Items.HEAVY_CORE,1),new ItemStack(Items.IRON_BLOCK,32),new ItemStack(Items.POLISHED_TUFF,32)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Enchanted Golden Apple Synthesis",new ItemStack(Items.TOTEM_OF_UNDYING,1),new ItemStack(Items.ENCHANTED_GOLDEN_APPLE,1),new ItemStack(Items.GOLD_BLOCK,8),new ItemStack(Items.GOLDEN_APPLE,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Dragon Egg Synthesis",new ItemStack(Items.DRAGON_EGG,1),new ItemStack(Items.DRAGON_EGG,2),new ItemStack(Items.DRAGON_BREATH,64),new ItemStack(Items.DIAMOND,64)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Divine Catalyst Synthesis",ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(),ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(),ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(),ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore()));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Aequalis Scientia Synthesis",ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(),ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore(),new ItemStack(Items.AMETHYST_BLOCK,64),new ItemStack(Items.DIAMOND_BLOCK,1)));
      
      // Permutation Recipes
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("Faeries' Stew", new ItemStack(Items.MUSHROOM_STEW,1), MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 1)), new ItemStack(Items.NETHER_WART),  (stack, server) -> {
         ItemStack stewStack = new ItemStack(Items.SUSPICIOUS_STEW);
         stewStack.set(DataComponentTypes.RARITY, Rarity.RARE);
         stewStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+".faeries_stew"));
         List<SuspiciousStewEffectsComponent.StewEffect> effects = new ArrayList<>();
         Registry<StatusEffect> effectRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.STATUS_EFFECT);
         List<RegistryEntry.Reference<StatusEffect>> effectEntries = effectRegistry.streamEntries().toList();
         int i = 0;
         while(i < 10 && (Math.random() < 0.25 || i == 0)){
            effects.add(new SuspiciousStewEffectsComponent.StewEffect(effectEntries.get((int)(Math.random()*effectEntries.size())),(int)(Math.random()*580 + 20)));
            i++;
         }
         SuspiciousStewEffectsComponent comp = new SuspiciousStewEffectsComponent(effects);
         stewStack.set(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS,comp);
         return stewStack;
      }, Text.literal("A 'Delicious' Stew")));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("Book Exchange", new ItemStack(Items.ENCHANTED_BOOK,1), MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 15)), new ItemStack(Items.LAPIS_LAZULI,5),  (stack, server) -> {
         ItemStack newBook = new ItemStack(Items.BOOK);
         ArrayList<RegistryEntry<Enchantment>> enchants = new ArrayList<>();
         server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getIndexedEntries().forEach(enchants::add);
         return EnchantmentHelper.enchant(Random.create(), newBook, (int)(Math.random()*30+1),enchants.stream());
      }, Text.literal("A Random ").append(Text.translatable(Items.ENCHANTED_BOOK.getTranslationKey()))));
      
      // Aequalis Scientia Recipes
      TRANSMUTATION_RECIPES.add(new AequalisSkillTransmutationRecipe("Transfer Skill Points"));
      
      TRANSMUTATION_RECIPES.add(new AequalisCatalystTransmutationRecipe("Reclaim Catalysts"));
   }
   
   
   public static TransmutationRecipe findMatchingRecipe(ItemStack positive, ItemStack negative, ItemStack re1, ItemStack re2, ItemStack aequalis, TransmutationAltarBlockEntity altar){
      TransmutationRecipe matching = null;
      for(TransmutationRecipe recipe : TRANSMUTATION_RECIPES){
         if(recipe.canTransmute(positive,negative,re1,re2,aequalis,altar)){
            matching = recipe;
         }
      }
      return matching;
   }
}
