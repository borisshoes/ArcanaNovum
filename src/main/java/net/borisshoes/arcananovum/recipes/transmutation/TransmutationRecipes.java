package net.borisshoes.arcananovum.recipes.transmutation;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.items.AequalisScientia;
import net.borisshoes.arcananovum.utils.ArcanaItemUtils;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.InstrumentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TransmutationRecipes {
   public static final ArrayList<TransmutationRecipe> TRANSMUTATION_RECIPES = new ArrayList<>();
   
   public static void initializeTransmutationRecipes(MinecraftServer server) {
      // Commutative Recipes
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stones",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.COBBLESTONE),new ItemStack(Items.STONE), new ItemStack(Items.GRANITE), new ItemStack(Items.POLISHED_GRANITE), new ItemStack(Items.DIORITE), new ItemStack(Items.POLISHED_DIORITE),
            new ItemStack(Items.ANDESITE), new ItemStack(Items.POLISHED_ANDESITE), new ItemStack(Items.DEEPSLATE), new ItemStack(Items.COBBLED_DEEPSLATE), new ItemStack(Items.POLISHED_DEEPSLATE),
            new ItemStack(Items.DEEPSLATE_TILES), new ItemStack(Items.CRACKED_DEEPSLATE_TILES), new ItemStack(Items.CALCITE), new ItemStack(Items.TUFF), new ItemStack(Items.DRIPSTONE_BLOCK), new ItemStack(Items.SMOOTH_STONE),
            new ItemStack(Items.STONE_BRICKS), new ItemStack(Items.MOSSY_STONE_BRICKS), new ItemStack(Items.CRACKED_STONE_BRICKS), new ItemStack(Items.CHISELED_STONE_BRICKS), new ItemStack(Items.BLACKSTONE),
            new ItemStack(Items.POLISHED_BLACKSTONE), new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS), new ItemStack(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
      )),new ItemStack(Items.COAL,16),new ItemStack(Items.QUARTZ,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Sandstones",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.SANDSTONE),new ItemStack(Items.CHISELED_SANDSTONE), new ItemStack(Items.CUT_SANDSTONE), new ItemStack(Items.SMOOTH_SANDSTONE),
            new ItemStack(Items.RED_SAND),new ItemStack(Items.CHISELED_RED_SANDSTONE), new ItemStack(Items.CUT_RED_SANDSTONE), new ItemStack(Items.SMOOTH_RED_SANDSTONE)
      )),new ItemStack(Items.REDSTONE,16),new ItemStack(Items.QUARTZ,12)));
      
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
            new ItemStack(Items.OAK_LOG),new ItemStack(Items.SPRUCE_LOG),new ItemStack(Items.WARPED_STEM),new ItemStack(Items.PALE_OAK_LOG),
            new ItemStack(Items.STRIPPED_ACACIA_LOG),new ItemStack(Items.STRIPPED_BAMBOO_BLOCK),new ItemStack(Items.STRIPPED_BIRCH_LOG),new ItemStack(Items.STRIPPED_CHERRY_LOG),
            new ItemStack(Items.STRIPPED_CRIMSON_STEM),new ItemStack(Items.STRIPPED_DARK_OAK_LOG),new ItemStack(Items.STRIPPED_JUNGLE_LOG),new ItemStack(Items.STRIPPED_MANGROVE_LOG),
            new ItemStack(Items.STRIPPED_OAK_LOG),new ItemStack(Items.STRIPPED_SPRUCE_LOG),new ItemStack(Items.STRIPPED_WARPED_STEM),new ItemStack(Items.STRIPPED_PALE_OAK_LOG)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wood",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.ACACIA_WOOD),new ItemStack(Items.BAMBOO_BLOCK),new ItemStack(Items.BIRCH_WOOD),new ItemStack(Items.CHERRY_WOOD),
            new ItemStack(Items.CRIMSON_HYPHAE),new ItemStack(Items.DARK_OAK_WOOD),new ItemStack(Items.JUNGLE_WOOD),new ItemStack(Items.MANGROVE_WOOD),
            new ItemStack(Items.OAK_WOOD),new ItemStack(Items.SPRUCE_WOOD),new ItemStack(Items.WARPED_HYPHAE),new ItemStack(Items.PALE_OAK_WOOD),
            new ItemStack(Items.STRIPPED_ACACIA_WOOD),new ItemStack(Items.STRIPPED_BAMBOO_BLOCK),new ItemStack(Items.STRIPPED_BIRCH_WOOD),new ItemStack(Items.STRIPPED_CHERRY_WOOD),
            new ItemStack(Items.STRIPPED_CRIMSON_HYPHAE),new ItemStack(Items.STRIPPED_DARK_OAK_WOOD),new ItemStack(Items.STRIPPED_JUNGLE_WOOD),new ItemStack(Items.STRIPPED_MANGROVE_WOOD),
            new ItemStack(Items.STRIPPED_OAK_WOOD),new ItemStack(Items.STRIPPED_SPRUCE_WOOD),new ItemStack(Items.STRIPPED_WARPED_HYPHAE),new ItemStack(Items.STRIPPED_PALE_OAK_WOOD)
      )),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Planks",Either.left(ItemTags.PLANKS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stairs",Either.left(ItemTags.WOODEN_STAIRS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Slabs",Either.left(ItemTags.WOODEN_SLABS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fences",Either.left(ItemTags.WOODEN_FENCES),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fence Gates",Either.left(ItemTags.FENCE_GATES),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Signs",Either.left(ItemTags.SIGNS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Hanging Signs",Either.left(ItemTags.HANGING_SIGNS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Doors",Either.left(ItemTags.WOODEN_DOORS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Trapdoors",Either.left(ItemTags.WOODEN_TRAPDOORS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pressure Plates",Either.left(ItemTags.WOODEN_PRESSURE_PLATES),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Buttons",Either.left(ItemTags.BUTTONS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Boats",Either.left(ItemTags.BOATS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Chest boats",Either.left(ItemTags.CHEST_BOATS),new ItemStack(Items.COPPER_INGOT,12),new ItemStack(Items.EMERALD,6)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Seeds",new ArrayList<>(Arrays.asList(
            new ItemStack(Items.WHEAT_SEEDS), new ItemStack(Items.PUMPKIN_SEEDS), new ItemStack(Items.MELON_SEEDS), new ItemStack(Items.BEETROOT_SEEDS), new ItemStack(Items.TORCHFLOWER_SEEDS),
            new ItemStack(Items.PITCHER_POD), new ItemStack(Items.POTATO), new ItemStack(Items.CARROT)
      )),new ItemStack(Items.LAPIS_LAZULI,16),new ItemStack(Items.EMERALD,24)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Leaves",Either.right(BlockTags.LEAVES),new ItemStack(Items.LAPIS_LAZULI,16),new ItemStack(Items.EMERALD,12)));
      
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
      
      ArrayList<ItemStack> goatHorns = new ArrayList<>();
      for(RegistryEntry<Instrument> entry : server.getRegistryManager().getOrThrow(RegistryKeys.INSTRUMENT).iterateEntries(InstrumentTags.GOAT_HORNS)){
         goatHorns.add(GoatHornItem.getStackForInstrument(Items.GOAT_HORN, entry));
      }
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Goat Horns", goatHorns, new ItemStack(Items.DIAMOND,4), new ItemStack(Items.AMETHYST_SHARD,12)));
      
      ArrayList<ItemStack> bannerPatterns = new ArrayList<>();
      for(RegistryEntry<Item> itemEntry : Registries.ITEM.getIndexedEntries()){
         if(itemEntry.value() instanceof BannerPatternItem){
            bannerPatterns.add(new ItemStack(itemEntry.value()));
         }
      }
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Banner Patterns", bannerPatterns, new ItemStack(Items.REDSTONE,48), new ItemStack(Items.QUARTZ,24)));
      
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
            new ItemStack(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), new ItemStack(Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE),
            new ItemStack(Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE)
      )),new ItemStack(Items.DIAMOND,16),new ItemStack(Items.LAPIS_LAZULI,64)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pottery Sherds",Either.left(ItemTags.DECORATED_POT_SHERDS),new ItemStack(Items.AMETHYST_SHARD,36),new ItemStack(Items.LAPIS_LAZULI,24)));
      
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
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Terracotta",Either.right(BlockTags.TERRACOTTA),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wool", Either.left(ItemTags.WOOL),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Carpet",Either.left(ItemTags.WOOL_CARPETS),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
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
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Candles",Either.left(ItemTags.CANDLES),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
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
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Banners",Either.right(BlockTags.BANNERS),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Beds",Either.right(BlockTags.BEDS),new ItemStack(Items.LAPIS_LAZULI,24),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
      // Infusion Recipes
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Cream Reduction",new ItemStack(Items.MAGMA_CREAM,1),new ItemStack(Items.SLIME_BALL,1),new ItemStack(Items.CHARCOAL,16),new ItemStack(Items.SUGAR,16)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Sand Decomposition",new ItemStack(Items.SANDSTONE,1),new ItemStack(Items.SAND,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Fiber Decomposition",new ItemStack(Items.WHITE_WOOL,1),new ItemStack(Items.STRING,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Honeycomb Decomposition",new ItemStack(Items.HONEYCOMB_BLOCK,1),new ItemStack(Items.HONEYCOMB,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Amethyst Decomposition",new ItemStack(Items.AMETHYST_BLOCK,1),new ItemStack(Items.AMETHYST_SHARD,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Prismarine Decomposition",new ItemStack(Items.PRISMARINE,1),new ItemStack(Items.PRISMARINE_SHARD,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Prismarine Brick Decomposition",new ItemStack(Items.PRISMARINE_BRICKS,1),new ItemStack(Items.PRISMARINE_SHARD,9),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Quartz Decomposition",new ItemStack(Items.QUARTZ_BLOCK,1),new ItemStack(Items.QUARTZ,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Nether Brick Decomposition",new ItemStack(Items.NETHER_BRICKS,1),new ItemStack(Items.NETHER_BRICK,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Glowstone Decomposition",new ItemStack(Items.GLOWSTONE,1),new ItemStack(Items.GLOWSTONE_DUST,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Magma Decomposition",new ItemStack(Items.MAGMA_BLOCK,1),new ItemStack(Items.MAGMA_CREAM,4),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Nether Wart Decomposition",new ItemStack(Items.NETHER_WART_BLOCK,1),new ItemStack(Items.NETHER_WART,9),new ItemStack(Items.GUNPOWDER,4),new ItemStack(ArcanaRegistry.STARDUST,1)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Blaze Rod Decomposition",new ItemStack(Items.BLAZE_ROD,1),new ItemStack(Items.BLAZE_POWDER,6),new ItemStack(Items.GUNPOWDER,24),new ItemStack(ArcanaRegistry.STARDUST,8)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Breeze Rod Decomposition",new ItemStack(Items.BREEZE_ROD,1),new ItemStack(Items.WIND_CHARGE,9),new ItemStack(Items.GUNPOWDER,24),new ItemStack(ArcanaRegistry.STARDUST,8)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Blackstone Infusion",new ItemStack(Items.BLACKSTONE,1),new ItemStack(Items.GILDED_BLACKSTONE,1),new ItemStack(Items.GLOWSTONE_DUST,24),new ItemStack(Items.GOLD_INGOT,12)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Nether Brick Infusion",new ItemStack(Items.NETHER_BRICKS,1),new ItemStack(Items.RED_NETHER_BRICKS,1),new ItemStack(Items.NETHER_WART,32),new ItemStack(Items.GLOWSTONE_DUST,12)));
      
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
      
      //TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Dragon Egg Synthesis",new ItemStack(Items.DRAGON_EGG,1),new ItemStack(Items.DRAGON_EGG,2),new ItemStack(Items.DRAGON_BREATH,64),new ItemStack(Items.DIAMOND,64)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Divine Catalyst Synthesis",ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(),ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(),ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore(),ArcanaRegistry.SOVEREIGN_CATALYST.getPrefItemNoLore()));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Aequalis Scientia Synthesis",ArcanaRegistry.DIVINE_CATALYST.getPrefItemNoLore(),ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore(),new ItemStack(Items.AMETHYST_BLOCK,64),new ItemStack(Items.DIAMOND_BLOCK,1)));
      
      // Permutation Recipes
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("Faeries' Stew", new ItemStack(Items.MUSHROOM_STEW,1), MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 1)), new ItemStack(Items.NETHER_WART),  (stack, minecraftServer) -> {
         ItemStack stewStack = new ItemStack(Items.SUSPICIOUS_STEW);
         stewStack.set(DataComponentTypes.RARITY, Rarity.RARE);
         stewStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+".faeries_stew"));
         List<SuspiciousStewEffectsComponent.StewEffect> effects = new ArrayList<>();
         Registry<StatusEffect> effectRegistry = minecraftServer.getRegistryManager().getOrThrow(RegistryKeys.STATUS_EFFECT);
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
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("Book Exchange", new ItemStack(Items.ENCHANTED_BOOK,1), MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 15)), new ItemStack(Items.LAPIS_LAZULI,5),  (stack, minecraftServer) -> {
         ItemStack newBook = new ItemStack(Items.BOOK);
         ArrayList<RegistryEntry<Enchantment>> enchants = new ArrayList<>();
         minecraftServer.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getIndexedEntries().forEach(enchants::add);
         return EnchantmentHelper.enchant(Random.create(), newBook, (int)(Math.random()*30+1),enchants.stream());
      }, Text.literal("A Random ").append(Text.translatable(Items.ENCHANTED_BOOK.getTranslationKey()))));
      
      TRANSMUTATION_RECIPES.add(new PermutationTransmutationRecipe("Aequalis Reconfiguration", ArcanaRegistry.AEQUALIS_SCIENTIA.getPrefItemNoLore(), MiscUtils.removeLore(new ItemStack(ArcanaRegistry.NEBULOUS_ESSENCE, 48)), new ItemStack(Items.AMETHYST_SHARD,48),  (stack, minecraftServer) -> {
         if(!(ArcanaItemUtils.identifyItem(stack) instanceof AequalisScientia aeq)) return stack;
         ArcanaItem.putProperty(stack, AequalisScientia.TRANSMUTATION_TAG,"");
         aeq.buildItemLore(stack,server);
         return stack;
      }, Text.literal("An Unattuned ").append(Text.translatable(ArcanaRegistry.AEQUALIS_SCIENTIA.getItem().getTranslationKey()))));
      
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
