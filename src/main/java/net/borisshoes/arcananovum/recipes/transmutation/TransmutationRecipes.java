package net.borisshoes.arcananovum.recipes.transmutation;

import com.mojang.datafixers.util.Either;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.blocks.altars.TransmutationAltarBlockEntity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
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
import java.util.List;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class TransmutationRecipes {
   public static final ArrayList<TransmutationRecipe> TRANSMUTATION_RECIPES = new ArrayList<>();
   
   public static void initializeTransmutationRecipes(MinecraftServer server) {
      // Commutative Recipes
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stones", new ItemStack(Items.COAL,16), new ItemStack(Items.QUARTZ,12))
            .with(Items.COBBLESTONE, Items.STONE, Items.GRANITE, Items.POLISHED_GRANITE, Items.DIORITE, Items.POLISHED_DIORITE, Items.ANDESITE,
                  Items.POLISHED_ANDESITE, Items.DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE, Items.DEEPSLATE_TILES,
                  Items.CRACKED_DEEPSLATE_TILES, Items.CALCITE, Items.TUFF, Items.DRIPSTONE_BLOCK, Items.SMOOTH_STONE, Items.STONE_BRICKS,
                  Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.MOSSY_COBBLESTONE, Items.BASALT,
                  Items.POLISHED_BASALT, Items.SMOOTH_BASALT, Items.BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE_BRICKS,
                  Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
            .with(Either.left(ConventionalItemTags.STONES))
            .with(Either.left(ConventionalItemTags.COBBLESTONES))
            .withViewStack(new ItemStack(Items.COBBLESTONE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Dirts", new ItemStack(Items.EMERALD,6), new ItemStack(Items.QUARTZ,12))
            .with(Items.GRASS_BLOCK, Items.DIRT, Items.COARSE_DIRT, Items.ROOTED_DIRT, Items.DIRT_PATH, Items.PODZOL, Items.MYCELIUM)
            .withViewStack(new ItemStack(Items.GRASS_BLOCK)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Sandstones", new ItemStack(Items.REDSTONE,16), new ItemStack(Items.QUARTZ,12))
            .with(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE, Items.SMOOTH_SANDSTONE, Items.RED_SAND, Items.CHISELED_RED_SANDSTONE,
                  Items.CUT_RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE)
            .with(Either.left(ConventionalItemTags.SANDSTONE_BLOCKS))
            .withViewStack(new ItemStack(Items.SANDSTONE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Copper", new ItemStack(Items.REDSTONE,36), new ItemStack(Items.AMETHYST_SHARD,24))
            .with(Items.OXIDIZED_COPPER, Items.WEATHERED_COPPER, Items.EXPOSED_COPPER, Items.COPPER_BLOCK,
                  Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.CUT_COPPER,
                  Items.OXIDIZED_CUT_COPPER_STAIRS, Items.WEATHERED_CUT_COPPER_STAIRS, Items.EXPOSED_CUT_COPPER_STAIRS, Items.CUT_COPPER_STAIRS,
                  Items.OXIDIZED_CHISELED_COPPER, Items.WEATHERED_CHISELED_COPPER, Items.EXPOSED_CHISELED_COPPER, Items.CHISELED_COPPER,
                  Items.OXIDIZED_COPPER_GRATE, Items.WEATHERED_COPPER_GRATE, Items.EXPOSED_COPPER_GRATE, Items.COPPER_GRATE)
            .withViewStack(new ItemStack(Items.OXIDIZED_COPPER)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Logs", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Items.ACACIA_LOG, Items.BAMBOO_BLOCK, Items.BIRCH_LOG, Items.CHERRY_LOG, Items.CRIMSON_STEM,
                  Items.DARK_OAK_LOG, Items.JUNGLE_LOG, Items.MANGROVE_LOG, Items.OAK_LOG, Items.SPRUCE_LOG,
                  Items.WARPED_STEM, Items.PALE_OAK_LOG, Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_CHERRY_LOG, Items.STRIPPED_CRIMSON_STEM,
                  Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_MANGROVE_LOG,
                  Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_WARPED_STEM,
                  Items.STRIPPED_PALE_OAK_LOG)
            .with(Either.left(ItemTags.LOGS))
            .with(Either.left(ConventionalItemTags.STRIPPED_LOGS))
            .withViewStack(new ItemStack(Items.OAK_LOG)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wood", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Items.ACACIA_WOOD, Items.BAMBOO_BLOCK, Items.BIRCH_WOOD, Items.CHERRY_WOOD, Items.CRIMSON_HYPHAE,
                  Items.DARK_OAK_WOOD, Items.JUNGLE_WOOD, Items.MANGROVE_WOOD, Items.OAK_WOOD, Items.SPRUCE_WOOD,
                  Items.WARPED_HYPHAE, Items.PALE_OAK_WOOD, Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_BAMBOO_BLOCK,
                  Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_CHERRY_WOOD, Items.STRIPPED_CRIMSON_HYPHAE,
                  Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_JUNGLE_WOOD, Items.STRIPPED_MANGROVE_WOOD,
                  Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_WARPED_HYPHAE,
                  Items.STRIPPED_PALE_OAK_WOOD)
            .withViewStack(new ItemStack(Items.OAK_WOOD)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Planks", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.PLANKS))
            .withViewStack(new ItemStack(Items.OAK_PLANKS)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Stairs", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_STAIRS))
            .withViewStack(new ItemStack(Items.OAK_STAIRS)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Slabs", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_SLABS))
            .withViewStack(new ItemStack(Items.OAK_SLAB)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fences", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_FENCES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCES))
            .withViewStack(new ItemStack(Items.OAK_FENCE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fence Gates", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.FENCE_GATES))
            .with(Either.left(ConventionalItemTags.WOODEN_FENCE_GATES))
            .withViewStack(new ItemStack(Items.OAK_FENCE_GATE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Signs", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.SIGNS))
            .withViewStack(new ItemStack(Items.OAK_SIGN)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Hanging Signs", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.HANGING_SIGNS)).
            withViewStack(new ItemStack(Items.OAK_HANGING_SIGN)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Doors", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_DOORS))
            .withViewStack(new ItemStack(Items.OAK_DOOR)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Trapdoors", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_TRAPDOORS))
            .withViewStack(new ItemStack(Items.OAK_TRAPDOOR)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pressure Plates", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.WOODEN_PRESSURE_PLATES))
            .withViewStack(new ItemStack(Items.OAK_PRESSURE_PLATE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Buttons", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.BUTTONS))
            .withViewStack(new ItemStack(Items.OAK_BUTTON)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Boats", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.BOATS))
            .withViewStack(new ItemStack(Items.OAK_BOAT)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Chest boats", new ItemStack(Items.COPPER_INGOT,12), new ItemStack(Items.EMERALD,6))
            .with(Either.left(ItemTags.CHEST_BOATS))
            .withViewStack(new ItemStack(Items.OAK_CHEST_BOAT)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Seeds", new ItemStack(Items.LAPIS_LAZULI,16), new ItemStack(Items.EMERALD,24))
            .with(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS,
                  Items.PITCHER_POD, Items.POTATO, Items.CARROT, Items.COCOA_BEANS)
            .with(Either.left(ConventionalItemTags.SEEDS))
            .withViewStack(new ItemStack(Items.WHEAT_SEEDS)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Eggs", new ItemStack(Items.LAPIS_LAZULI,16), new ItemStack(Items.EMERALD,24))
            .with(Items.EGG, Items.BLUE_EGG, Items.BROWN_EGG, Items.TURTLE_EGG, Items.SNIFFER_EGG)
            .with(Either.left(ConventionalItemTags.EGGS))
            .withViewStack(new ItemStack(Items.EGG)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Leaves", new ItemStack(Items.LAPIS_LAZULI,16), new ItemStack(Items.EMERALD,12))
            .with(Either.right(BlockTags.LEAVES))
            .withViewStack(new ItemStack(Items.OAK_LEAVES)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Flowers and Plants", new ItemStack(Items.GLOWSTONE_DUST,16), new ItemStack(Items.LAPIS_LAZULI,32))
            .with(Items.PEONY, Items.ROSE_BUSH, Items.LILAC, Items.SUNFLOWER, Items.AZURE_BLUET, Items.OXEYE_DAISY,
                  Items.CORNFLOWER, Items.WITHER_ROSE, Items.TORCHFLOWER, Items.PITCHER_PLANT, Items.PINK_PETALS,
                  Items.DANDELION, Items.POPPY, Items.BLUE_ORCHID, Items.ALLIUM, Items.RED_TULIP, Items.ORANGE_TULIP,
                  Items.WHITE_TULIP, Items.PINK_TULIP, Items.SPORE_BLOSSOM, Items.FLOWERING_AZALEA, Items.OPEN_EYEBLOSSOM,
                  Items.CLOSED_EYEBLOSSOM, Items.CHORUS_FLOWER, Items.BUSH, Items.FIREFLY_BUSH, Items.SHORT_DRY_GRASS,
                  Items.TALL_DRY_GRASS, Items.CACTUS_FLOWER, Items.WILDFLOWERS, Items.LEAF_LITTER, Items.BIG_DRIPLEAF,
                  Items.SMALL_DRIPLEAF, Items.VINE, Items.FERN, Items.LARGE_FERN, Items.TALL_GRASS, Items.SHORT_GRASS,
                  Items.LILY_PAD, Items.PALE_HANGING_MOSS, Items.GLOW_LICHEN)
            .with(Either.left(ConventionalItemTags.FLOWERS))
            .withViewStack(new ItemStack(Items.POPPY)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fungi", new ItemStack(Items.GLOWSTONE_DUST,16), new ItemStack(Items.LAPIS_LAZULI,32))
            .with(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM)
            .withViewStack(new ItemStack(Items.RED_MUSHROOM)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Froglights", new ItemStack(Items.REDSTONE,48), new ItemStack(Items.EMERALD,48))
            .with(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)
            .withViewStack(new ItemStack(Items.OCHRE_FROGLIGHT)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Fish", new ItemStack(Items.REDSTONE,16), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH)
            .withViewStack(new ItemStack(Items.COD)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Coral", new ItemStack(Items.COPPER_INGOT,32), new ItemStack(Items.LAPIS_LAZULI,32))
            .with(Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK,
                  Items.HORN_CORAL_BLOCK, Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL,
                  Items.HORN_CORAL, Items.TUBE_CORAL_FAN, Items.BRAIN_CORAL_FAN, Items.BUBBLE_CORAL_FAN,
                  Items.FIRE_CORAL_FAN, Items.HORN_CORAL_FAN, Items.DEAD_TUBE_CORAL_BLOCK, Items.DEAD_BRAIN_CORAL_BLOCK,
                  Items.DEAD_BUBBLE_CORAL_BLOCK, Items.DEAD_FIRE_CORAL_BLOCK, Items.DEAD_HORN_CORAL_BLOCK,
                  Items.DEAD_TUBE_CORAL, Items.DEAD_BRAIN_CORAL, Items.DEAD_BUBBLE_CORAL, Items.DEAD_FIRE_CORAL,
                  Items.DEAD_HORN_CORAL, Items.DEAD_TUBE_CORAL_FAN, Items.DEAD_BRAIN_CORAL_FAN, Items.DEAD_BUBBLE_CORAL_FAN,
                  Items.DEAD_FIRE_CORAL_FAN, Items.DEAD_HORN_CORAL_FAN)
            .withViewStack(new ItemStack(Items.TUBE_CORAL_BLOCK)));
      
      
      CommutativeTransmutationRecipe goat = new CommutativeTransmutationRecipe("Goat Horns", new ItemStack(Items.DIAMOND,4), new ItemStack(Items.AMETHYST_SHARD,12));
      for(RegistryEntry<Instrument> entry : server.getRegistryManager().getOrThrow(RegistryKeys.INSTRUMENT).iterateEntries(InstrumentTags.GOAT_HORNS)){
         goat = goat.with(GoatHornItem.getStackForInstrument(Items.GOAT_HORN, entry));
      }
      TRANSMUTATION_RECIPES.add(goat.withViewStack(new ItemStack(Items.GOAT_HORN)));
      
      CommutativeTransmutationRecipe banners = new CommutativeTransmutationRecipe("Banner Patterns", new ItemStack(Items.REDSTONE,48), new ItemStack(Items.QUARTZ,24));
      for(RegistryEntry<Item> itemEntry : Registries.ITEM.getIndexedEntries()){
         if(itemEntry.value().getDefaultStack().contains(DataComponentTypes.PROVIDES_BANNER_PATTERNS)){
            banners = banners.with(itemEntry.value());
         }
      }
      TRANSMUTATION_RECIPES.add(banners.withViewStack(new ItemStack(Items.CREEPER_BANNER_PATTERN)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Heads", new ItemStack(Items.NETHERITE_SCRAP,4), new ItemStack(Items.REDSTONE,64))
            .with(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD).withViewStack(new ItemStack(Items.SKELETON_SKULL)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Smithing Templates", new ItemStack(Items.DIAMOND,16), new ItemStack(Items.LAPIS_LAZULI,64))
            .with(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE,
                  Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE)
            .withViewStack(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Pottery Sherds", new ItemStack(Items.AMETHYST_SHARD,36), new ItemStack(Items.LAPIS_LAZULI,24))
            .with(Either.left(ItemTags.DECORATED_POT_SHERDS))
            .withViewStack(new ItemStack(Items.ANGLER_POTTERY_SHERD)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Music Discs", new ItemStack(Items.DIAMOND,12), new ItemStack(Items.AMETHYST_SHARD,36))
            .with(Either.left(ConventionalItemTags.MUSIC_DISCS))
            .withViewStack(new ItemStack(Items.MUSIC_DISC_CAT)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Dyes", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.DYES))
            .withViewStack(new ItemStack(Items.WHITE_DYE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Terracotta", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.right(BlockTags.TERRACOTTA))
            .withViewStack(new ItemStack(Items.TERRACOTTA)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Wool", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ItemTags.WOOL))
            .withViewStack(new ItemStack(Items.WHITE_WOOL)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Carpet", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ItemTags.WOOL_CARPETS))
            .withViewStack(new ItemStack(Items.WHITE_CARPET)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glass", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.GLASS_BLOCKS_CHEAP))
            .withViewStack(new ItemStack(Items.GLASS)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glass Panes", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.GLASS_PANES))
            .withViewStack(new ItemStack(Items.GLASS_PANE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Candles", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ItemTags.CANDLES)).withViewStack(new ItemStack(Items.CANDLE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Glazed Terracotta", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.GLAZED_TERRACOTTAS))
            .withViewStack(new ItemStack(Items.WHITE_GLAZED_TERRACOTTA)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Concrete", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.CONCRETES))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Gravity Blocks", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.left(ConventionalItemTags.CONCRETE_POWDERS))
            .with(Either.left(ConventionalItemTags.SANDS))
            .with(Either.left(ConventionalItemTags.GRAVELS))
            .withViewStack(new ItemStack(Items.WHITE_CONCRETE_POWDER)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Banners", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.right(BlockTags.BANNERS))
            .withViewStack(new ItemStack(Items.WHITE_BANNER)));
      
      TRANSMUTATION_RECIPES.add(new CommutativeTransmutationRecipe("Beds", new ItemStack(Items.LAPIS_LAZULI,24), new ItemStack(Items.GLOWSTONE_DUST,12))
            .with(Either.right(BlockTags.BEDS))
            .withViewStack(new ItemStack(Items.WHITE_BED)));
      
      
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
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Sand Susification",new ItemStack(Items.SAND,1),new ItemStack(Items.SUSPICIOUS_SAND,1),new ItemStack(Items.SUSPICIOUS_STEW,1),new ItemStack(ArcanaRegistry.STARDUST,8)));
      
      TRANSMUTATION_RECIPES.add(new InfusionTransmutationRecipe("Gravel Susification",new ItemStack(Items.GRAVEL,1),new ItemStack(Items.SUSPICIOUS_GRAVEL,1),new ItemStack(Items.SUSPICIOUS_STEW,1),new ItemStack(ArcanaRegistry.STARDUST,8)));
      
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
      
      // Aequalis Scientia Recipes
      TRANSMUTATION_RECIPES.add(new AequalisUnattuneTransmutationRecipe("Aequalis Reconfiguration"));
      
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
